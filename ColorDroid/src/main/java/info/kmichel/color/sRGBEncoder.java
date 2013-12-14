package info.kmichel.color;

public final class sRGBEncoder {

    private sRGBEncoder() {
    }

    static EncodedRGBColor encode(final RGBColor color) {
        return new EncodedRGBColor(
                quantize(compand(color.red)),
                quantize(compand(color.green)),
                quantize(compand(color.blue)));
    }

    static int quantize(final float value) {
        return (int) Math.floor(255 * value + 0.5);
    }

    static float compand(final float value) {
        final float K = 12.92f;
        final float epsilon = 0.0031308f;
        final float offset = 0.055f;
        final float exponent = 2.4f;
        return value > epsilon ? (1 + offset) * (float) Math.pow(value, 1.0 / exponent) - offset : value * K;
    }
}
