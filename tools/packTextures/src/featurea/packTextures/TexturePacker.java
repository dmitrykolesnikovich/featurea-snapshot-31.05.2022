package featurea.packTextures;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static featurea.jvm.FileKt.createNewFileAndDirs;
import static featurea.math.MathKt.nextPowerOfTwo;

public class TexturePacker {

    private final Settings settings = new Settings();
    private final MaxRectsPacker maxRectsPacker;
    private final ImageProcessor imageProcessor;
    private final Set<String> frameNames = new HashSet<>();

    public TexturePacker() {
        if (Settings.POT) {
            if (Settings.MAX_WIDTH != nextPowerOfTwo(Settings.MAX_WIDTH)) {
                throw new RuntimeException("If pot is true, maxWidth must be a power of two: " + settings.MAX_WIDTH);
            }
            if (Settings.MAX_HEIGHT != nextPowerOfTwo(Settings.MAX_HEIGHT)) {
                throw new RuntimeException("If pot is true, maxHeight must be a power of two: " + settings.MAX_HEIGHT);
            }
        }
        maxRectsPacker = new MaxRectsPacker(settings);
        imageProcessor = new ImageProcessor(settings);
    }

    public void addImage(String frameName, File file) {
        if (frameNames.add(frameName)) {
            imageProcessor.addImage(frameName, file);
        }
    }

    public MyArray<Page> pack(File outputDir, String packFileName) {
        outputDir.mkdirs();

        MyArray<Page> pages = maxRectsPacker.pack(imageProcessor.getImages());
        writeImages(outputDir, pages, packFileName);
        try {
            writePackFile(outputDir, pages, packFileName);
        } catch (IOException ex) {
            throw new RuntimeException("Error writing pack file.", ex);
        }
        return pages;
    }

    /*internals*/

    /**
     * Compares two images pixel by pixel
     */
    private static boolean compareBufferedImages(BufferedImage image1, BufferedImage image2) {
        // The images must be the same size
        if (image1.getWidth() == image2.getWidth() && image1.getHeight() == image2.getHeight()) {
            int width = image1.getWidth();
            int height = image1.getHeight();
            // Loop over every pixel
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Compare the pixels for equality
                    if (image1.getRGB(x, y) != image2.getRGB(x, y)) {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
        return true;
    }

    private void writeImages(File outputDir, MyArray<Page> pages, String packFileName) {
        String imageName = packFileName;
        int dotIndex = imageName.lastIndexOf('.');
        if (dotIndex != -1) imageName = imageName.substring(0, dotIndex);
        for (int partIndex = 0; partIndex < pages.size; partIndex++) {
            Page page = pages.get(partIndex);
            int width = page.width, height = page.height;
            int paddingX = settings.paddingX;
            int paddingY = settings.paddingY;
            if (settings.duplicatePadding) {
                paddingX /= 2;
                paddingY /= 2;
            }
            width -= settings.paddingX;
            height -= settings.paddingY;
            if (settings.edgePadding) {
                page.x = paddingX;
                page.y = paddingY;
                width += paddingX * 2;
                height += paddingY * 2;
            }
            if (settings.POT) {
                width = nextPowerOfTwo(width);
                height = nextPowerOfTwo(height);
            }
            width = Math.max(settings.MIN_WIDTH, width);
            height = Math.max(settings.MIN_HEIGHT, height);
            if (settings.forceSquareOutput) {
                if (width > height) {
                    height = width;
                } else {
                    width = height;
                }
            }

            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
            for (Rect rect : page.outputRects) {
                BufferedImage image = rect.getImage(imageProcessor);
                int iw = image.getWidth();
                int ih = image.getHeight();
                int rectX = page.x + rect.x, rectY = page.y + page.height - rect.y - rect.height;

                // >> IMPORTANT edgeTiling logic
                if (settings.isEdgeTilingPath(rect.name)) {
                    rectX -= 1;
                    rectY -= 1;
                    iw += 2;
                    ih += 2;
                }
                // <<

                if (settings.duplicatePadding) {
                    int amountX = settings.paddingX / 2;
                    int amountY = settings.paddingY / 2;
                    if (rect.rotated) {
                        for (int i = 1; i <= amountX; i++) {
                            for (int j = 1; j <= amountY; j++) {
                                plot(bufferedImage, rectX - j, rectY + iw - 1 + i, getRGB(image, rect.name, 0, 0));
                                plot(bufferedImage, rectX + ih - 1 + j, rectY + iw - 1 + i, getRGB(image, rect.name, 0, ih - 1));
                                plot(bufferedImage, rectX - j, rectY - i, getRGB(image, rect.name, iw - 1, 0));
                                plot(bufferedImage, rectX + ih - 1 + j, rectY - i, getRGB(image, rect.name, iw - 1, ih - 1));
                            }
                        }
                        for (int i = 1; i <= amountY; i++) {
                            for (int j = 0; j < iw; j++) {
                                plot(bufferedImage, rectX - i, rectY + iw - 1 - j, getRGB(image, rect.name, j, 0));
                                plot(bufferedImage, rectX + ih - 1 + i, rectY + iw - 1 - j, getRGB(image, rect.name, j, ih - 1));
                            }
                        }
                        for (int i = 1; i <= amountX; i++) {
                            for (int j = 0; j < ih; j++) {
                                plot(bufferedImage, rectX + j, rectY - i, getRGB(image, rect.name, iw - 1, j));
                                plot(bufferedImage, rectX + j, rectY + iw - 1 + i, getRGB(image, rect.name, 0, j));
                            }
                        }
                    } else {
                        for (int i = 1; i <= amountX; i++) {
                            for (int j = 1; j <= amountY; j++) {
                                bufferedImage.setRGB(rectX - i, rectY - j, getRGB(image, rect.name, 0, 0));
                                bufferedImage.setRGB(rectX - i, rectY + ih - 1 + j, getRGB(image, rect.name, 0, ih - 1));
                                bufferedImage.setRGB(rectX + iw - 1 + i, rectY - j, getRGB(image, rect.name, iw - 1, 0));
                                bufferedImage.setRGB(rectX + iw - 1 + i, rectY + ih - 1 + j, getRGB(image, rect.name, iw - 1, ih - 1));
                            }
                        }
                        for (int i = 1; i <= amountY; i++) {
                            copy(image, rect.name, 0, 0, iw, 1, bufferedImage, rectX, rectY - i, rect.rotated);
                            copy(image, rect.name, 0, ih - 1, iw, 1, bufferedImage, rectX, rectY + ih - 1 + i, rect.rotated);
                        }
                        for (int i = 1; i <= amountX; i++) {
                            copy(image, rect.name, 0, 0, 1, ih, bufferedImage, rectX - i, rectY, rect.rotated);
                            copy(image, rect.name, iw - 1, 0, 1, ih, bufferedImage, rectX + iw - 1 + i, rectY, rect.rotated);
                        }
                    }
                }
                copy(image, rect.name, 0, 0, iw, ih, bufferedImage, rectX, rectY, rect.rotated);
                if (settings.debug) {
                    g.setColor(Color.magenta);
                    g.drawRect(rectX, rectY, rect.width - settings.paddingX - 1, rect.height - settings.paddingY - 1);
                }
            }
            if (settings.bleed && !settings.premultiplyAlpha && !settings.OUTPUT_EXTENSION.equalsIgnoreCase("jpg")) {
                bufferedImage = new ColorBleedEffect().processImage(bufferedImage, 2);
                g = (Graphics2D) bufferedImage.getGraphics();
            }
            if (settings.debug) {
                g.setColor(Color.magenta);
                g.drawRect(0, 0, width - 1, height - 1);
            }
            ImageOutputStream ios = null;
            if (settings.premultiplyAlpha) {
                bufferedImage.getColorModel().coerceData(bufferedImage.getRaster(), true);
            }
            File outputFile = new File(outputDir, imageName + ".part" + partIndex + "." + Settings.OUTPUT_EXTENSION);
            try {
                page.imageName = outputFile.getName();
                page.canvasWidth = bufferedImage.getWidth();
                page.canvasHeight = bufferedImage.getHeight();
                BufferedImage outputBufferedImage;
                if (outputFile.exists()) {
                    outputBufferedImage = ImageIO.read(outputFile);
                } else {
                    outputBufferedImage = null;
                }
                System.out.println("[TexturePacker] outputFile: " + outputFile.getAbsolutePath() + " (" + outputFile.exists() + ")");
                System.out.println("[TexturePacker] inputBufferedImage: " + bufferedImage);
                System.out.println("[TexturePacker] outputBufferedImage: " + outputBufferedImage);
                if (!outputFile.exists() || !compareBufferedImages(bufferedImage, outputBufferedImage)) {
                    createNewFileAndDirs(outputFile);
                    ImageIO.write(bufferedImage, "png", outputFile);
                    System.out.println("[INFO] Generated " + outputFile);
                }
            } catch (IOException ex) {
                throw new RuntimeException("Error writing file: " + outputFile, ex);
            } finally {
                if (ios != null) {
                    try {
                        ios.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void plot(BufferedImage dst, int x, int y, int argb) {
        if (0 <= x && x < dst.getWidth() && 0 <= y && y < dst.getHeight()) dst.setRGB(x, y, argb);
    }

    private void copy(BufferedImage src, String file, int x, int y, int w, int h, BufferedImage dst, int dx, int dy, boolean rotated) {
        if (rotated) {
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    dst.setRGB(dx + j, dy + w - i - 1, getRGB(src, file, x + i, y + j));
                }
            }
        } else {
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    dst.setRGB(dx + i, dy + j, getRGB(src, file, x + i, y + j));
                }
            }
        }
    }

    private void writePackFile(File outputDir, MyArray<Page> pages, String packFileName) throws IOException {
        File packFile = new File(outputDir, packFileName);

        if (!packFile.exists()) packFile.createNewFile();
        Writer writer = new OutputStreamWriter(new FileOutputStream(packFile), StandardCharsets.UTF_8);
        for (Page page : pages) {
            writer.write("\n"); // todo avoid this
            writer.write(page.imageName + "\n");
            writer.write("size: " + page.canvasWidth + "," + page.canvasHeight + "\n");
            writer.write("format: RGBA8888\n");
            writer.write("filter: Nearest,Nearest\n");
            writer.write("repeat: none\n");
            for (Rect rect : page.outputRects) {
                writeRect(writer, page, rect);
                for (Alias alias : rect.aliases) {
                    Rect aliasRect = new Rect();
                    aliasRect.set(rect);
                    alias.apply(aliasRect);
                    writeRect(writer, page, aliasRect);
                }
            }
        }
        writer.close();
    }

    private void writeRect(Writer writer, Page page, Rect rect) throws IOException {
        writer.write(rect.name + "\n");
        writer.write("  rotate: " + rect.rotated + "\n");
        writer.write("  xy: " + (page.x + rect.x) + ", " + (page.y + page.height - rect.height - rect.y) + "\n");
        writer.write("  size: " + rect.regionWidth + ", " + rect.regionHeight + "\n");
        if (rect.splits != null) {
            writer.write("  split: " + rect.splits[0] + ", " + rect.splits[1] + ", " + rect.splits[2] + ", " + rect.splits[3] + "\n");
        }
        if (rect.pads != null) {
            if (rect.splits == null) writer.write("  split: 0, 0, 0, 0\n");
            writer.write("  pad: " + rect.pads[0] + ", " + rect.pads[1] + ", " + rect.pads[2] + ", " + rect.pads[3] + "\n");
        }
        writer.write("  orig: " + rect.originalWidth + ", " + rect.originalHeight + "\n");
        writer.write("  offset: " + rect.offsetX + ", " + (rect.originalHeight - rect.regionHeight - rect.offsetY) + "\n");
        writer.write("  index: " + rect.index + "\n");
    }
    // <<

    // >> IMPORTANT edgeTiling logic
    private int getRGB(BufferedImage image, String file, int x, int y) {
        if (settings.isEdgeTilingPath(file)) {
            if (x >= 1 && x <= image.getWidth() && y >= 1 && y <= image.getHeight()) {
                return image.getRGB(x - 1, y - 1);
            } else {
                if (x == 0) {
                    x = 0;
                } else if (x == image.getWidth() - 1 + 2) {
                    x -= 2;
                } else {
                    x -= 1;
                }
                if (y == 0) {
                    y = 0;
                } else if (y == image.getHeight() - 1 + 2) {
                    y -= 2;
                } else {
                    y -= 1;
                }
                return image.getRGB(x, y);
            }
        } else {
            return image.getRGB(x, y);
        }
    }

    public static class Page {
        String imageName;
        MyArray<Rect> outputRects, remainingRects;
        float occupancy;
        int x, y, width, height;
        int canvasWidth, canvasHeight;
    }

    static class Alias {
        String name;
        int index;
        int[] splits;
        int[] pads;
        int offsetX, offsetY, originalWidth, originalHeight;

        Alias(Rect rect) {
            name = rect.name;
            index = rect.index;
            splits = rect.splits;
            pads = rect.pads;
            offsetX = rect.offsetX;
            offsetY = rect.offsetY;
            originalWidth = rect.originalWidth;
            originalHeight = rect.originalHeight;
        }

        void apply(Rect rect) {
            rect.name = name;
            rect.index = index;
            rect.splits = splits;
            rect.pads = pads;
            rect.offsetX = offsetX;
            rect.offsetY = offsetY;
            rect.originalWidth = originalWidth;
            rect.originalHeight = originalHeight;
        }
    }

    static class Rect {
        String name;
        int offsetX, offsetY, regionWidth, regionHeight, originalWidth, originalHeight;
        int x, y, width, height;
        int index;
        boolean rotated;
        Set<Alias> aliases = new HashSet<Alias>();
        int[] splits;
        int[] pads;
        boolean canRotate = true;
        int score1, score2;
        private boolean isPatch;
        private BufferedImage image;
        private File file;

        Rect(BufferedImage source, int left, int top, int newWidth, int newHeight, boolean isPatch) {
            image = new BufferedImage(source.getColorModel(), source.getRaster().createWritableChild(left, top, newWidth, newHeight, 0, 0, null),
                    source.getColorModel().isAlphaPremultiplied(), null);
            offsetX = left;
            offsetY = top;
            regionWidth = newWidth;
            regionHeight = newHeight;
            originalWidth = source.getWidth();
            originalHeight = source.getHeight();
            width = newWidth;
            height = newHeight;
            this.isPatch = isPatch;
        }

        Rect() {
        }

        Rect(Rect rect) {
            x = rect.x;
            y = rect.y;
            width = rect.width;
            height = rect.height;
        }

        void unloadImage(File file) {
            this.file = file;
            image = null;
        }

        BufferedImage getImage(ImageProcessor imageProcessor) {
            if (image != null) return image;
            BufferedImage image;
            try {
                image = ImageIO.read(file);
            } catch (IOException ex) {
                throw new RuntimeException("Error reading image: " + file, ex);
            }
            if (image == null) throw new RuntimeException("Unable to read image: " + file);
            String name = this.name;
            if (isPatch) name += ".9";
            return imageProcessor.processImage(image, name).getImage(null);
        }

        void set(Rect rect) {
            name = rect.name;
            image = rect.image;
            offsetX = rect.offsetX;
            offsetY = rect.offsetY;
            regionWidth = rect.regionWidth;
            regionHeight = rect.regionHeight;
            originalWidth = rect.originalWidth;
            originalHeight = rect.originalHeight;
            x = rect.x;
            y = rect.y;
            width = rect.width;
            height = rect.height;
            index = rect.index;
            rotated = rect.rotated;
            aliases = rect.aliases;
            splits = rect.splits;
            pads = rect.pads;
            canRotate = rect.canRotate;
            score1 = rect.score1;
            score2 = rect.score2;
            file = rect.file;
            isPatch = rect.isPatch;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Rect other = (Rect) obj;
            if (name == null) {
                if (other.name != null) return false;
            } else if (!name.equals(other.name)) return false;
            return true;
        }

        @Override
        public String toString() {
            return name + "[" + x + "," + y + " " + width + "x" + height + "]";
        }
    }

}
