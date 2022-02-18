package featurea.packTextures;

import java.util.Comparator;

import static featurea.math.MathKt.nextPowerOfTwo;

/*package*/ class MaxRectsPacker {

    private Settings settings;
    private RectComparator rectComparator = new RectComparator();
    private FreeRectChoiceHeuristic[] methods = FreeRectChoiceHeuristic.values();
    private MaxRects maxRects = new MaxRects();
    private TimSort timSort = new TimSort();

    public MaxRectsPacker(Settings settings) {
        this.settings = settings;
    }

    public MyArray<TexturePacker.Page> pack(MyArray<TexturePacker.Rect> inputRects) {
        for (int i = 0, nn = inputRects.size; i < nn; i++) {
            TexturePacker.Rect rect = inputRects.get(i);
            rect.width += settings.paddingX;
            rect.height += settings.paddingY;
        }
        if (settings.fast) {
            if (settings.rotation) {
                timSort.doSort(inputRects.items, new Comparator<TexturePacker.Rect>() {
                    public int compare(TexturePacker.Rect o1, TexturePacker.Rect o2) {
                        int n1 = o1.width > o1.height ? o1.width : o1.height;
                        int n2 = o2.width > o2.height ? o2.width : o2.height;
                        return n2 - n1;
                    }
                }, 0, inputRects.size);
            } else {
                timSort.doSort(inputRects.items, new Comparator<TexturePacker.Rect>() {
                    public int compare(TexturePacker.Rect o1, TexturePacker.Rect o2) {
                        return o2.width - o1.width;
                    }
                }, 0, inputRects.size);
            }
        }
        MyArray<TexturePacker.Page> pages = new MyArray();
        while (inputRects.size > 0) {
            TexturePacker.Page result = packPage(inputRects);
            pages.add(result);
            inputRects = result.remainingRects;
        }
        return pages;
    }

    private TexturePacker.Page packPage(MyArray<TexturePacker.Rect> inputRects) {
        int edgePaddingX = 0, edgePaddingY = 0;
        if (!settings.duplicatePadding) {
            edgePaddingX = settings.paddingX;
            edgePaddingY = settings.paddingY;
        }
        int minWidth = Integer.MAX_VALUE;
        int minHeight = Integer.MAX_VALUE;
        for (int i = 0, nn = inputRects.size; i < nn; i++) {
            TexturePacker.Rect rect = inputRects.get(i);
            minWidth = Math.min(minWidth, rect.width);
            minHeight = Math.min(minHeight, rect.height);
            if (settings.rotation) {
                if ((rect.width > settings.MAX_WIDTH || rect.height > settings.MAX_HEIGHT) &&
                        (rect.width > settings.MAX_HEIGHT || rect.height > settings.MAX_WIDTH)) {
                    throw new RuntimeException("Image does not fit with max page size " +
                            settings.MAX_WIDTH +
                            "x" +
                            settings.MAX_HEIGHT +
                            " and padding " +
                            settings.paddingX +
                            "," +
                            settings.paddingY +
                            ": " +
                            rect);
                }
            } else {
                if (rect.width > settings.MAX_WIDTH) {
                    throw new RuntimeException(
                            "Image does not fit with max page width " + settings.MAX_WIDTH + " and paddingX " + settings.paddingX + ": " + rect);
                }
                if (rect.height > settings.MAX_HEIGHT && (!settings.rotation || rect.width > settings.MAX_HEIGHT)) {
                    throw new RuntimeException(
                            "Image does not fit in max page height " + settings.MAX_HEIGHT + " and paddingY " + settings.paddingY + ": " + rect);
                }
            }
        }
        minWidth = Math.max(minWidth, settings.MIN_WIDTH);
        minHeight = Math.max(minHeight, settings.MIN_HEIGHT);
        BinarySearch widthSearch = new BinarySearch(minWidth, settings.MAX_WIDTH, settings.fast ? 25 : 15, settings.POT);
        BinarySearch heightSearch = new BinarySearch(minHeight, settings.MAX_HEIGHT, settings.fast ? 25 : 15, settings.POT);
        int width = widthSearch.reset(), height = heightSearch.reset(), i = 0;
        TexturePacker.Page bestResult = null;
        while (true) {
            TexturePacker.Page bestWidthResult = null;
            while (width != -1) {
                TexturePacker.Page result = packAtSize(true, width - edgePaddingX, height - edgePaddingY, inputRects);
                if (++i % 70 == 0) System.out.println();
                System.out.print("");
                bestWidthResult = getBest(bestWidthResult, result);
                width = widthSearch.next(result == null);
            }
            bestResult = getBest(bestResult, bestWidthResult);
            height = heightSearch.next(bestWidthResult == null);
            if (height == -1) break;
            width = widthSearch.reset();
        }
        if (bestResult == null) {
            bestResult = packAtSize(false, settings.MAX_WIDTH - edgePaddingX, settings.MAX_HEIGHT - edgePaddingY, inputRects);
        }
        timSort.doSort(bestResult.outputRects.items, rectComparator, 0, bestResult.outputRects.size);
        return bestResult;
    }

    private TexturePacker.Page packAtSize(boolean fully, int width, int height, MyArray<TexturePacker.Rect> inputRects) {
        TexturePacker.Page bestResult = null;
        for (int i = 0, n = methods.length; i < n; i++) {
            maxRects.init(width, height);
            TexturePacker.Page result;
            if (!settings.fast) {
                result = maxRects.pack(inputRects, methods[i]);
            } else {
                MyArray<TexturePacker.Rect> remaining = new MyArray();
                for (int ii = 0, nn = inputRects.size; ii < nn; ii++) {
                    TexturePacker.Rect rect = inputRects.get(ii);
                    if (maxRects.insert(rect, methods[i]) == null) {
                        while (ii < nn) remaining.add(inputRects.get(ii++));
                    }
                }
                result = maxRects.getResult();
                result.remainingRects = remaining;
            }
            if (fully && result.remainingRects.size > 0) continue;
            if (result.outputRects.size == 0) continue;
            bestResult = getBest(bestResult, result);
        }
        return bestResult;
    }

    private TexturePacker.Page getBest(TexturePacker.Page result1, TexturePacker.Page result2) {
        if (result1 == null) return result2;
        if (result2 == null) return result1;
        return result1.occupancy > result2.occupancy ? result1 : result2;
    }

    public enum FreeRectChoiceHeuristic {
        BestShortSideFit, BestLongSideFit, BestAreaFit, BottomLeftRule, ContactPointRule
    }

    static class BinarySearch {
        int min, max, fuzziness, low, high, current;
        boolean pot;

        public BinarySearch(int min, int max, int fuzziness, boolean pot) {
            this.pot = pot;
            this.fuzziness = pot ? 0 : fuzziness;
            this.min = pot ? (int) (Math.log(nextPowerOfTwo(min)) / Math.log(2)) : min;
            this.max = pot ? (int) (Math.log(nextPowerOfTwo(max)) / Math.log(2)) : max;
        }

        public int reset() {
            low = min;
            high = max;
            current = (low + high) >>> 1;
            return pot ? (int) Math.pow(2, current) : current;
        }

        public int next(boolean result) {
            if (low >= high) return -1;
            if (result) {
                low = current + 1;
            } else {
                high = current - 1;
            }
            current = (low + high) >>> 1;
            if (Math.abs(low - high) < fuzziness) return -1;
            return pot ? (int) Math.pow(2, current) : current;
        }
    }

    class MaxRects {
        private final MyArray<TexturePacker.Rect> usedRectangles = new MyArray();
        private final MyArray<TexturePacker.Rect> freeRectangles = new MyArray();
        private int binWidth;
        private int binHeight;

        public void init(int width, int height) {
            binWidth = width;
            binHeight = height;
            usedRectangles.clear();
            freeRectangles.clear();
            TexturePacker.Rect n = new TexturePacker.Rect();
            n.x = 0;
            n.y = 0;
            n.width = width;
            n.height = height;
            freeRectangles.add(n);
        }

        public TexturePacker.Rect insert(TexturePacker.Rect rect, FreeRectChoiceHeuristic method) {
            TexturePacker.Rect newNode = scoreRect(rect, method);
            if (newNode.height == 0) return null;
            int numRectanglesToProcess = freeRectangles.size;
            for (int i = 0; i < numRectanglesToProcess; ++i) {
                if (splitFreeNode(freeRectangles.get(i), newNode)) {
                    freeRectangles.removeIndex(i);
                    --i;
                    --numRectanglesToProcess;
                }
            }
            pruneFreeList();
            TexturePacker.Rect bestNode = new TexturePacker.Rect();
            bestNode.set(rect);
            bestNode.score1 = newNode.score1;
            bestNode.score2 = newNode.score2;
            bestNode.x = newNode.x;
            bestNode.y = newNode.y;
            bestNode.width = newNode.width;
            bestNode.height = newNode.height;
            bestNode.rotated = newNode.rotated;
            usedRectangles.add(bestNode);
            return bestNode;
        }

        public TexturePacker.Page pack(MyArray<TexturePacker.Rect> rects, FreeRectChoiceHeuristic method) {
            rects = new MyArray(rects);
            while (rects.size > 0) {
                int bestRectIndex = -1;
                TexturePacker.Rect bestNode = new TexturePacker.Rect();
                bestNode.score1 = Integer.MAX_VALUE;
                bestNode.score2 = Integer.MAX_VALUE;
                for (int i = 0; i < rects.size; i++) {
                    TexturePacker.Rect newNode = scoreRect(rects.get(i), method);
                    if (newNode.score1 < bestNode.score1 || (newNode.score1 == bestNode.score1 && newNode.score2 < bestNode.score2)) {
                        bestNode.set(rects.get(i));
                        bestNode.score1 = newNode.score1;
                        bestNode.score2 = newNode.score2;
                        bestNode.x = newNode.x;
                        bestNode.y = newNode.y;
                        bestNode.width = newNode.width;
                        bestNode.height = newNode.height;
                        bestNode.rotated = newNode.rotated;
                        bestRectIndex = i;
                    }
                }
                if (bestRectIndex == -1) break;
                placeRect(bestNode);
                rects.removeIndex(bestRectIndex);
            }
            TexturePacker.Page result = getResult();
            result.remainingRects = rects;
            return result;
        }

        public TexturePacker.Page getResult() {
            int w = 0, h = 0;
            for (int i = 0; i < usedRectangles.size; i++) {
                TexturePacker.Rect rect = usedRectangles.get(i);
                w = Math.max(w, rect.x + rect.width);
                h = Math.max(h, rect.y + rect.height);
            }
            TexturePacker.Page result = new TexturePacker.Page();
            result.outputRects = new MyArray(usedRectangles);
            result.occupancy = getOccupancy();
            result.width = w;
            result.height = h;
            return result;
        }

        private void placeRect(TexturePacker.Rect node) {
            int numRectanglesToProcess = freeRectangles.size;
            for (int i = 0; i < numRectanglesToProcess; i++) {
                if (splitFreeNode(freeRectangles.get(i), node)) {
                    freeRectangles.removeIndex(i);
                    --i;
                    --numRectanglesToProcess;
                }
            }
            pruneFreeList();
            usedRectangles.add(node);
        }

        private TexturePacker.Rect scoreRect(TexturePacker.Rect rect, FreeRectChoiceHeuristic method) {
            int width = rect.width;
            int height = rect.height;
            int rotatedWidth = height - settings.paddingY + settings.paddingX;
            int rotatedHeight = width - settings.paddingX + settings.paddingY;
            boolean rotate = rect.canRotate && settings.rotation;
            TexturePacker.Rect newNode = null;
            switch (method) {
                case BestShortSideFit:
                    newNode = findPositionForNewNodeBestShortSideFit(width, height, rotatedWidth, rotatedHeight, rotate);
                    break;
                case BottomLeftRule:
                    newNode = findPositionForNewNodeBottomLeft(width, height, rotatedWidth, rotatedHeight, rotate);
                    break;
                case ContactPointRule:
                    newNode = findPositionForNewNodeContactPoint(width, height, rotatedWidth, rotatedHeight, rotate);
                    newNode.score1 = -newNode.score1;
                    break;
                case BestLongSideFit:
                    newNode = findPositionForNewNodeBestLongSideFit(width, height, rotatedWidth, rotatedHeight, rotate);
                    break;
                case BestAreaFit:
                    newNode = findPositionForNewNodeBestAreaFit(width, height, rotatedWidth, rotatedHeight, rotate);
                    break;
            }
            if (newNode.height == 0) {
                newNode.score1 = Integer.MAX_VALUE;
                newNode.score2 = Integer.MAX_VALUE;
            }
            return newNode;
        }

        private float getOccupancy() {
            int usedSurfaceArea = 0;
            for (int i = 0; i < usedRectangles.size; i++) {
                usedSurfaceArea += usedRectangles.get(i).width * usedRectangles.get(i).height;
            }
            return (float) usedSurfaceArea / (binWidth * binHeight);
        }

        private TexturePacker.Rect findPositionForNewNodeBottomLeft(int width,
                                                                    int height,
                                                                    int rotatedWidth,
                                                                    int rotatedHeight,
                                                                    boolean rotate) {
            TexturePacker.Rect bestNode = new TexturePacker.Rect();
            bestNode.score1 = Integer.MAX_VALUE;
            for (int i = 0; i < freeRectangles.size; i++) {
                if (freeRectangles.get(i).width >= width && freeRectangles.get(i).height >= height) {
                    int topSideY = freeRectangles.get(i).y + height;
                    if (topSideY < bestNode.score1 || (topSideY == bestNode.score1 && freeRectangles.get(i).x < bestNode.score2)) {
                        bestNode.x = freeRectangles.get(i).x;
                        bestNode.y = freeRectangles.get(i).y;
                        bestNode.width = width;
                        bestNode.height = height;
                        bestNode.score1 = topSideY;
                        bestNode.score2 = freeRectangles.get(i).x;
                        bestNode.rotated = false;
                    }
                }
                if (rotate && freeRectangles.get(i).width >= rotatedWidth && freeRectangles.get(i).height >= rotatedHeight) {
                    int topSideY = freeRectangles.get(i).y + rotatedHeight;
                    if (topSideY < bestNode.score1 || (topSideY == bestNode.score1 && freeRectangles.get(i).x < bestNode.score2)) {
                        bestNode.x = freeRectangles.get(i).x;
                        bestNode.y = freeRectangles.get(i).y;
                        bestNode.width = rotatedWidth;
                        bestNode.height = rotatedHeight;
                        bestNode.score1 = topSideY;
                        bestNode.score2 = freeRectangles.get(i).x;
                        bestNode.rotated = true;
                    }
                }
            }
            return bestNode;
        }

        private TexturePacker.Rect findPositionForNewNodeBestShortSideFit(int width,
                                                                          int height,
                                                                          int rotatedWidth,
                                                                          int rotatedHeight,
                                                                          boolean rotate) {
            TexturePacker.Rect bestNode = new TexturePacker.Rect();
            bestNode.score1 = Integer.MAX_VALUE;
            for (int i = 0; i < freeRectangles.size; i++) {
                if (freeRectangles.get(i).width >= width && freeRectangles.get(i).height >= height) {
                    int leftoverHoriz = Math.abs(freeRectangles.get(i).width - width);
                    int leftoverVert = Math.abs(freeRectangles.get(i).height - height);
                    int shortSideFit = Math.min(leftoverHoriz, leftoverVert);
                    int longSideFit = Math.max(leftoverHoriz, leftoverVert);
                    if (shortSideFit < bestNode.score1 || (shortSideFit == bestNode.score1 && longSideFit < bestNode.score2)) {
                        bestNode.x = freeRectangles.get(i).x;
                        bestNode.y = freeRectangles.get(i).y;
                        bestNode.width = width;
                        bestNode.height = height;
                        bestNode.score1 = shortSideFit;
                        bestNode.score2 = longSideFit;
                        bestNode.rotated = false;
                    }
                }
                if (rotate && freeRectangles.get(i).width >= rotatedWidth && freeRectangles.get(i).height >= rotatedHeight) {
                    int flippedLeftoverHoriz = Math.abs(freeRectangles.get(i).width - rotatedWidth);
                    int flippedLeftoverVert = Math.abs(freeRectangles.get(i).height - rotatedHeight);
                    int flippedShortSideFit = Math.min(flippedLeftoverHoriz, flippedLeftoverVert);
                    int flippedLongSideFit = Math.max(flippedLeftoverHoriz, flippedLeftoverVert);
                    if (flippedShortSideFit < bestNode.score1 || (flippedShortSideFit == bestNode.score1 && flippedLongSideFit < bestNode.score2)) {
                        bestNode.x = freeRectangles.get(i).x;
                        bestNode.y = freeRectangles.get(i).y;
                        bestNode.width = rotatedWidth;
                        bestNode.height = rotatedHeight;
                        bestNode.score1 = flippedShortSideFit;
                        bestNode.score2 = flippedLongSideFit;
                        bestNode.rotated = true;
                    }
                }
            }
            return bestNode;
        }

        private TexturePacker.Rect findPositionForNewNodeBestLongSideFit(int width,
                                                                         int height,
                                                                         int rotatedWidth,
                                                                         int rotatedHeight,
                                                                         boolean rotate) {
            TexturePacker.Rect bestNode = new TexturePacker.Rect();
            bestNode.score2 = Integer.MAX_VALUE;
            for (int i = 0; i < freeRectangles.size; i++) {
                if (freeRectangles.get(i).width >= width && freeRectangles.get(i).height >= height) {
                    int leftoverHoriz = Math.abs(freeRectangles.get(i).width - width);
                    int leftoverVert = Math.abs(freeRectangles.get(i).height - height);
                    int shortSideFit = Math.min(leftoverHoriz, leftoverVert);
                    int longSideFit = Math.max(leftoverHoriz, leftoverVert);
                    if (longSideFit < bestNode.score2 || (longSideFit == bestNode.score2 && shortSideFit < bestNode.score1)) {
                        bestNode.x = freeRectangles.get(i).x;
                        bestNode.y = freeRectangles.get(i).y;
                        bestNode.width = width;
                        bestNode.height = height;
                        bestNode.score1 = shortSideFit;
                        bestNode.score2 = longSideFit;
                        bestNode.rotated = false;
                    }
                }
                if (rotate && freeRectangles.get(i).width >= rotatedWidth && freeRectangles.get(i).height >= rotatedHeight) {
                    int leftoverHoriz = Math.abs(freeRectangles.get(i).width - rotatedWidth);
                    int leftoverVert = Math.abs(freeRectangles.get(i).height - rotatedHeight);
                    int shortSideFit = Math.min(leftoverHoriz, leftoverVert);
                    int longSideFit = Math.max(leftoverHoriz, leftoverVert);
                    if (longSideFit < bestNode.score2 || (longSideFit == bestNode.score2 && shortSideFit < bestNode.score1)) {
                        bestNode.x = freeRectangles.get(i).x;
                        bestNode.y = freeRectangles.get(i).y;
                        bestNode.width = rotatedWidth;
                        bestNode.height = rotatedHeight;
                        bestNode.score1 = shortSideFit;
                        bestNode.score2 = longSideFit;
                        bestNode.rotated = true;
                    }
                }
            }
            return bestNode;
        }

        private TexturePacker.Rect findPositionForNewNodeBestAreaFit(int width,
                                                                     int height,
                                                                     int rotatedWidth,
                                                                     int rotatedHeight,
                                                                     boolean rotate) {
            TexturePacker.Rect bestNode = new TexturePacker.Rect();
            bestNode.score1 = Integer.MAX_VALUE;
            for (int i = 0; i < freeRectangles.size; i++) {
                int areaFit = freeRectangles.get(i).width * freeRectangles.get(i).height - width * height;
                if (freeRectangles.get(i).width >= width && freeRectangles.get(i).height >= height) {
                    int leftoverHoriz = Math.abs(freeRectangles.get(i).width - width);
                    int leftoverVert = Math.abs(freeRectangles.get(i).height - height);
                    int shortSideFit = Math.min(leftoverHoriz, leftoverVert);
                    if (areaFit < bestNode.score1 || (areaFit == bestNode.score1 && shortSideFit < bestNode.score2)) {
                        bestNode.x = freeRectangles.get(i).x;
                        bestNode.y = freeRectangles.get(i).y;
                        bestNode.width = width;
                        bestNode.height = height;
                        bestNode.score2 = shortSideFit;
                        bestNode.score1 = areaFit;
                        bestNode.rotated = false;
                    }
                }
                if (rotate && freeRectangles.get(i).width >= rotatedWidth && freeRectangles.get(i).height >= rotatedHeight) {
                    int leftoverHoriz = Math.abs(freeRectangles.get(i).width - rotatedWidth);
                    int leftoverVert = Math.abs(freeRectangles.get(i).height - rotatedHeight);
                    int shortSideFit = Math.min(leftoverHoriz, leftoverVert);
                    if (areaFit < bestNode.score1 || (areaFit == bestNode.score1 && shortSideFit < bestNode.score2)) {
                        bestNode.x = freeRectangles.get(i).x;
                        bestNode.y = freeRectangles.get(i).y;
                        bestNode.width = rotatedWidth;
                        bestNode.height = rotatedHeight;
                        bestNode.score2 = shortSideFit;
                        bestNode.score1 = areaFit;
                        bestNode.rotated = true;
                    }
                }
            }
            return bestNode;
        }

        private int commonIntervalLength(int i1start, int i1end, int i2start, int i2end) {
            if (i1end < i2start || i2end < i1start) return 0;
            return Math.min(i1end, i2end) - Math.max(i1start, i2start);
        }

        private int contactPointScoreNode(int x, int y, int width, int height) {
            int score = 0;
            if (x == 0 || x + width == binWidth) score += height;
            if (y == 0 || y + height == binHeight) score += width;
            for (int i = 0; i < usedRectangles.size; i++) {
                if (usedRectangles.get(i).x == x + width || usedRectangles.get(i).x + usedRectangles.get(i).width == x) {
                    score += commonIntervalLength(usedRectangles.get(i).y, usedRectangles.get(i).y + usedRectangles.get(i).height, y, y + height);
                }
                if (usedRectangles.get(i).y == y + height || usedRectangles.get(i).y + usedRectangles.get(i).height == y) {
                    score += commonIntervalLength(usedRectangles.get(i).x, usedRectangles.get(i).x + usedRectangles.get(i).width, x, x + width);
                }
            }
            return score;
        }

        private TexturePacker.Rect findPositionForNewNodeContactPoint(int width,
                                                                      int height,
                                                                      int rotatedWidth,
                                                                      int rotatedHeight,
                                                                      boolean rotate) {
            TexturePacker.Rect bestNode = new TexturePacker.Rect();
            bestNode.score1 = -1;
            for (int i = 0; i < freeRectangles.size; i++) {
                if (freeRectangles.get(i).width >= width && freeRectangles.get(i).height >= height) {
                    int score = contactPointScoreNode(freeRectangles.get(i).x, freeRectangles.get(i).y, width, height);
                    if (score > bestNode.score1) {
                        bestNode.x = freeRectangles.get(i).x;
                        bestNode.y = freeRectangles.get(i).y;
                        bestNode.width = width;
                        bestNode.height = height;
                        bestNode.score1 = score;
                        bestNode.rotated = false;
                    }
                }
                if (rotate && freeRectangles.get(i).width >= rotatedWidth && freeRectangles.get(i).height >= rotatedHeight) {
                    int score = contactPointScoreNode(freeRectangles.get(i).x, freeRectangles.get(i).y, rotatedWidth, rotatedHeight);
                    if (score > bestNode.score1) {
                        bestNode.x = freeRectangles.get(i).x;
                        bestNode.y = freeRectangles.get(i).y;
                        bestNode.width = rotatedWidth;
                        bestNode.height = rotatedHeight;
                        bestNode.score1 = score;
                        bestNode.rotated = true;
                    }
                }
            }
            return bestNode;
        }

        private boolean splitFreeNode(TexturePacker.Rect freeNode, TexturePacker.Rect usedNode) {
            if (usedNode.x >= freeNode.x + freeNode.width ||
                    usedNode.x + usedNode.width <= freeNode.x ||
                    usedNode.y >= freeNode.y + freeNode.height ||
                    usedNode.y + usedNode.height <= freeNode.y) {
                return false;
            }
            if (usedNode.x < freeNode.x + freeNode.width && usedNode.x + usedNode.width > freeNode.x) {
                if (usedNode.y > freeNode.y && usedNode.y < freeNode.y + freeNode.height) {
                    TexturePacker.Rect newNode = new TexturePacker.Rect(freeNode);
                    newNode.height = usedNode.y - newNode.y;
                    freeRectangles.add(newNode);
                }
                if (usedNode.y + usedNode.height < freeNode.y + freeNode.height) {
                    TexturePacker.Rect newNode = new TexturePacker.Rect(freeNode);
                    newNode.y = usedNode.y + usedNode.height;
                    newNode.height = freeNode.y + freeNode.height - (usedNode.y + usedNode.height);
                    freeRectangles.add(newNode);
                }
            }
            if (usedNode.y < freeNode.y + freeNode.height && usedNode.y + usedNode.height > freeNode.y) {
                if (usedNode.x > freeNode.x && usedNode.x < freeNode.x + freeNode.width) {
                    TexturePacker.Rect newNode = new TexturePacker.Rect(freeNode);
                    newNode.width = usedNode.x - newNode.x;
                    freeRectangles.add(newNode);
                }
                if (usedNode.x + usedNode.width < freeNode.x + freeNode.width) {
                    TexturePacker.Rect newNode = new TexturePacker.Rect(freeNode);
                    newNode.x = usedNode.x + usedNode.width;
                    newNode.width = freeNode.x + freeNode.width - (usedNode.x + usedNode.width);
                    freeRectangles.add(newNode);
                }
            }
            return true;
        }

        private void pruneFreeList() {
            for (int i = 0; i < freeRectangles.size; i++) {
                for (int j = i + 1; j < freeRectangles.size; ++j) {
                    if (isContainedIn(freeRectangles.get(i), freeRectangles.get(j))) {
                        freeRectangles.removeIndex(i);
                        --i;
                        break;
                    }
                    if (isContainedIn(freeRectangles.get(j), freeRectangles.get(i))) {
                        freeRectangles.removeIndex(j);
                        --j;
                    }
                }
            }
        }

        private boolean isContainedIn(TexturePacker.Rect a, TexturePacker.Rect b) {
            return a.x >= b.x && a.y >= b.y && a.x + a.width <= b.x + b.width && a.y + a.height <= b.y + b.height;
        }
    }

    class RectComparator implements Comparator<TexturePacker.Rect> {
        @Override
        public int compare(TexturePacker.Rect o1, TexturePacker.Rect o2) {
            return o1.name.compareTo(o2.name);
        }
    }

}
