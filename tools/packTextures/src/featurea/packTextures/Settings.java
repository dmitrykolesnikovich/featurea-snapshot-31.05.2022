package featurea.packTextures;

/*package*/ class Settings {

    static final boolean POT = true;
    static final int MIN_WIDTH = 16, MIN_HEIGHT = 16;
    static final int MAX_WIDTH = 2048 * 2, MAX_HEIGHT = 2048;
    static final String OUTPUT_EXTENSION = "png";
    int paddingX = 8;
    int paddingY = 8;
    boolean edgePadding = true;
    boolean duplicatePadding = false;
    boolean rotation;
    boolean forceSquareOutput = false;
    boolean stripWhitespaceX;
    boolean stripWhitespaceY;
    int alphaThreshold;
    boolean alias = false; // quickfix for pack GIF todo improve this
    boolean ignoreBlankImages = true;
    boolean fast;
    boolean debug;
    boolean premultiplyAlpha;
    boolean useIndexes = true;
    boolean bleed = true;
    boolean limitMemory = true;

    boolean isEdgeTilingPath(String file) {
        return false;
    }

}
