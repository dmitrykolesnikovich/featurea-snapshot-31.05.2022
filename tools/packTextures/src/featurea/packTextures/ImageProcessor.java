package featurea.packTextures;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ImageProcessor {

    private static Pattern indexPattern = Pattern.compile("(.+)_(\\d+)$");
    private final Settings settings;
    private final HashMap<String, TexturePacker.Rect> crcs = new HashMap();
    private final MyArray<TexturePacker.Rect> rects = new MyArray();

    public ImageProcessor(Settings settings) {
        this.settings = settings;
    }

    public void addImage(String frameName, File file) {
        BufferedImage image;
        try {
            image = ImageIO.read(file);
        } catch (IOException ex) {
            throw new RuntimeException("Error reading image: " + file, ex);
        } catch (IllegalArgumentException e) {
            throw e;
        }
        if (image == null) {
            throw new RuntimeException("Unable to read image: " + file);
        }
        TexturePacker.Rect rect = addImage(image, frameName);
        if (rect != null && settings.limitMemory) rect.unloadImage(file);
    }

    public MyArray<TexturePacker.Rect> getImages() {
        return rects;
    }

    public TexturePacker.Rect processImage(BufferedImage image, String name) {
        if (image.getType() != BufferedImage.TYPE_4BYTE_ABGR) {
            BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
            newImage.getGraphics().drawImage(image, 0, 0, null);
            image = newImage;
        }
        TexturePacker.Rect rect = null;
        if (name.endsWith(".9")) {
            name = name.substring(0, name.length() - 2);
            int[] splits = getSplits(image, name);
            int[] pads = getPads(image, name, splits);
            BufferedImage newImage = new BufferedImage(image.getWidth() - 2, image.getHeight() - 2, BufferedImage.TYPE_4BYTE_ABGR);
            newImage.getGraphics()
                    .drawImage(image, 0, 0, newImage.getWidth(), newImage.getHeight(), 1, 1, image.getWidth() - 1, image.getHeight() - 1, null);
            image = newImage;
            rect = new TexturePacker.Rect(image, 0, 0, image.getWidth(), image.getHeight(), true);
            rect.splits = splits;
            rect.pads = pads;
            rect.canRotate = false;
        } else {
            rect = stripWhitespace(image);
            if (rect == null) return null;
        }
        int index = -1;
        if (settings.useIndexes) {
            Matcher matcher = indexPattern.matcher(name);
            if (matcher.matches()) {
                name = matcher.group(1);
                index = Integer.parseInt(matcher.group(2));
            }
        }
        /*if (name.startsWith("/")) {
          name = name.substring(1, name.length());
        }*/
        rect.name = name;
        rect.index = index;
        return rect;
    }

    /*internal API*/

    private static String splitError(int x, int y, int[] rgba, String name) {
        throw new RuntimeException("Invalid " +
                name +
                " ninepatch split pixel at " +
                x +
                ", " +
                y +
                ", rgba: " +
                rgba[0] +
                ", " +
                rgba[1] +
                ", " +
                rgba[2] +
                ", " +
                rgba[3]);
    }

    private static int[] getSplits(BufferedImage image, String name) {
        WritableRaster raster = image.getRaster();
        int startX = getSplitPoint(raster, name, 1, 0, true, true);
        int endX = getSplitPoint(raster, name, startX, 0, false, true);
        int startY = getSplitPoint(raster, name, 0, 1, true, false);
        int endY = getSplitPoint(raster, name, 0, startY, false, false);
        getSplitPoint(raster, name, endX + 1, 0, true, true);
        getSplitPoint(raster, name, 0, endY + 1, true, false);
        if (startX == 0 && endX == 0 && startY == 0 && endY == 0) return null;
        if (startX != 0) {
            startX--;
            endX = raster.getWidth() - 2 - (endX - 1);
        } else {
            endX = raster.getWidth() - 2;
        }
        if (startY != 0) {
            startY--;
            endY = raster.getHeight() - 2 - (endY - 1);
        } else {
            endY = raster.getHeight() - 2;
        }
        return new int[]{startX, endX, startY, endY};
    }

    private static int[] getPads(BufferedImage image, String name, int[] splits) {
        WritableRaster raster = image.getRaster();
        int bottom = raster.getHeight() - 1;
        int right = raster.getWidth() - 1;
        int startX = getSplitPoint(raster, name, 1, bottom, true, true);
        int startY = getSplitPoint(raster, name, right, 1, true, false);
        int endX = 0;
        int endY = 0;
        if (startX != 0) endX = getSplitPoint(raster, name, startX + 1, bottom, false, true);
        if (startY != 0) endY = getSplitPoint(raster, name, right, startY + 1, false, false);
        getSplitPoint(raster, name, endX + 1, bottom, true, true);
        getSplitPoint(raster, name, right, endY + 1, true, false);
        if (startX == 0 && endX == 0 && startY == 0 && endY == 0) {
            return null;
        }
        if (startX == 0 && endX == 0) {
            startX = -1;
            endX = -1;
        } else {
            if (startX > 0) {
                startX--;
                endX = raster.getWidth() - 2 - (endX - 1);
            } else {
                endX = raster.getWidth() - 2;
            }
        }
        if (startY == 0 && endY == 0) {
            startY = -1;
            endY = -1;
        } else {
            if (startY > 0) {
                startY--;
                endY = raster.getHeight() - 2 - (endY - 1);
            } else {
                endY = raster.getHeight() - 2;
            }
        }
        int[] pads = new int[]{startX, endX, startY, endY};
        if (splits != null && Arrays.equals(pads, splits)) {
            return null;
        }
        return pads;
    }

    private static int getSplitPoint(WritableRaster raster, String name, int startX, int startY, boolean startPoint, boolean xAxis) {
        int[] rgba = new int[4];
        int next = xAxis ? startX : startY;
        int end = xAxis ? raster.getWidth() : raster.getHeight();
        int breakA = startPoint ? 255 : 0;
        int x = startX;
        int y = startY;
        while (next != end) {
            if (xAxis) {
                x = next;
            } else {
                y = next;
            }
            raster.getPixel(x, y, rgba);
            if (rgba[3] == breakA) return next;
            if (!startPoint && (rgba[0] != 0 || rgba[1] != 0 || rgba[2] != 0 || rgba[3] != 255))
                splitError(x, y, rgba, name);
            next++;
        }
        return 0;
    }

    private static String hash(BufferedImage image) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            int width = image.getWidth();
            int height = image.getHeight();
            if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
                BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                newImage.getGraphics().drawImage(image, 0, 0, null);
                image = newImage;
            }
            WritableRaster raster = image.getRaster();
            int[] pixels = new int[width];
            for (int y = 0; y < height; y++) {
                raster.getDataElements(0, y, width, 1, pixels);
                for (int x = 0; x < width; x++) {
                    hash(digest, pixels[x]);
                }
            }
            hash(digest, width);
            hash(digest, height);
            return new BigInteger(1, digest.digest()).toString(16);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void hash(MessageDigest digest, int value) {
        digest.update((byte) (value >> 24));
        digest.update((byte) (value >> 16));
        digest.update((byte) (value >> 8));
        digest.update((byte) value);
    }

    private TexturePacker.Rect addImage(BufferedImage image, String name) {
        TexturePacker.Rect rect = processImage(image, name);
        if (rect == null) {
            System.out.println("Ignoring blank input image: " + name);
            return null;
        }
        if (settings.alias) {
            String crc = hash(rect.getImage(this));
            TexturePacker.Rect existing = crcs.get(crc);
            if (existing != null) {
                existing.aliases.add(new TexturePacker.Alias(rect));
                return null;
            }
            crcs.put(crc, rect);
        }
        rects.add(rect);
        return rect;
    }

    private TexturePacker.Rect stripWhitespace(BufferedImage source) {
        WritableRaster alphaRaster = source.getAlphaRaster();
        if (alphaRaster == null || (!settings.stripWhitespaceX && !settings.stripWhitespaceY)) {
            return new TexturePacker.Rect(source, 0, 0, source.getWidth(), source.getHeight(), false);
        }
        final byte[] a = new byte[1];
        int top = 0;
        int bottom = source.getHeight();
        if (settings.stripWhitespaceX) {
            outer:
            for (int y = 0; y < source.getHeight(); y++) {
                for (int x = 0; x < source.getWidth(); x++) {
                    alphaRaster.getDataElements(x, y, a);
                    int alpha = a[0];
                    if (alpha < 0) alpha += 256;
                    if (alpha > settings.alphaThreshold) break outer;
                }
                top++;
            }
            outer:
            for (int y = source.getHeight(); --y >= top; ) {
                for (int x = 0; x < source.getWidth(); x++) {
                    alphaRaster.getDataElements(x, y, a);
                    int alpha = a[0];
                    if (alpha < 0) alpha += 256;
                    if (alpha > settings.alphaThreshold) break outer;
                }
                bottom--;
            }
        }
        int left = 0;
        int right = source.getWidth();
        if (settings.stripWhitespaceY) {
            outer:
            for (int x = 0; x < source.getWidth(); x++) {
                for (int y = top; y < bottom; y++) {
                    alphaRaster.getDataElements(x, y, a);
                    int alpha = a[0];
                    if (alpha < 0) alpha += 256;
                    if (alpha > settings.alphaThreshold) break outer;
                }
                left++;
            }
            outer:
            for (int x = source.getWidth(); --x >= left; ) {
                for (int y = top; y < bottom; y++) {
                    alphaRaster.getDataElements(x, y, a);
                    int alpha = a[0];
                    if (alpha < 0) alpha += 256;
                    if (alpha > settings.alphaThreshold) break outer;
                }
                right--;
            }
        }
        int newWidth = right - left;
        int newHeight = bottom - top;
        if (newWidth <= 0 || newHeight <= 0) {
            if (settings.ignoreBlankImages) {
                return null;
            } else {
                return new TexturePacker.Rect(ImageProcessorData.emptyImage, 0, 0, 1, 1, false);
            }
        }
        return new TexturePacker.Rect(source, left, top, newWidth, newHeight, false);
    }

}

// >> IMPORTANT do not replace it to ImageProcessor because JavaFX does not recognize system menu bar
class ImageProcessorData {
    public static final BufferedImage emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
}
// <<
