package info.kmichel.color;

public class EncodedRGBColor {
    final int red;
    final int green;
    final int blue;

    EncodedRGBColor(final int red, final int green, final int blue) {
        this.red = Math.min(255, Math.max(0, red));
        this.green = Math.min(255, Math.max(0, green));
        this.blue = Math.min(255, Math.max(0, blue));
    }

    public int asRGBA(final int alpha) {
        final int clamped_alpha = Math.min(255, Math.max(0, alpha));
        return (clamped_alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
