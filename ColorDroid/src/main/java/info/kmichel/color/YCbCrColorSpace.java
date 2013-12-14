package info.kmichel.color;


import info.kmichel.math.MutableVector1x3;

public class YCbCrColorSpace {

    public enum EncodingStandard {
        BT601_525,
        BT601_625,
        BT709
    }

    public final LuminanceCoefficients luminance_coefficients;
    public final RGBColorSpace rgb_color_space;

    public YCbCrColorSpace(final EncodingStandard encoding_standard) {
        // TODO: move those created variables as statics
        // TODO: simplify these since the sum of the three values is one, use standard Kr Kb names
        final LuminanceCoefficients bt601_coefficients = new LuminanceCoefficients(.299f, .587f, .114f);
        final LuminanceCoefficients bt709_coefficients = new LuminanceCoefficients(.2126f, .7152f, .0722f);

        // TODO: try sRGB color space
        // TODO: dig inside video encoder to see which conversion and space it uses
        final RGBColorSpace bt601_525_color_space = new RGBColorSpace(
                new xyYColor(0.6300f, 0.3400f, 1.0f),
                new xyYColor(0.3100f, 0.5950f, 1.0f),
                new xyYColor(0.1550f, 0.0700f, 1.0f),
                new xyYColor(0.3127f, 0.3290f, 1.0f));
        final RGBColorSpace bt601_625_color_space = new RGBColorSpace(
                new xyYColor(0.6400f, 0.3300f, 1.0f),
                new xyYColor(0.2900f, 0.6000f, 1.0f),
                new xyYColor(0.1500f, 0.0600f, 1.0f),
                new xyYColor(0.3127f, 0.3290f, 1.0f));
        final RGBColorSpace bt709_color_space = new RGBColorSpace(
                new xyYColor(0.6400f, 0.3300f, 1.0f),
                new xyYColor(0.3000f, 0.6000f, 1.0f),
                new xyYColor(0.1500f, 0.0600f, 1.0f),
                new xyYColor(0.3127f, 0.3290f, 1.0f));

        switch (encoding_standard) {
            case BT601_525:
                luminance_coefficients = bt601_coefficients;
                rgb_color_space = bt601_525_color_space;
                break;
            case BT601_625:
                luminance_coefficients = bt601_coefficients;
                rgb_color_space = bt601_625_color_space;
                break;
            case BT709:
                luminance_coefficients = bt709_coefficients;
                rgb_color_space = bt709_color_space;
                break;
            default:
                throw new IllegalArgumentException("Invalid encoding standard");
        }
    }

    public XYZColor decode(final YCbCrColor color) {
        return rgb_color_space.convert(splitComponents(color));
    }

    public RGBColor splitComponents(final YCbCrColor color) {
        // TODO: re compare with spec
        final float blue = (2 * (luminance_coefficients.green + luminance_coefficients.red)) * color.cb + color.y;
        final float red = (2 * (luminance_coefficients.green + luminance_coefficients.blue)) * color.cr + color.y;
        final float green = (color.y - luminance_coefficients.red * red - luminance_coefficients.blue * blue) / luminance_coefficients.green;

        return new RGBColor(linearize(red), linearize(green), linearize(blue));
    }

    private static float linearize(final float value) {
        // This is 'as in spec' but it has a discontinuity
        final float K = 4.5f;
        final float epsilon = 0.018f;
        final float offset = 0.099f;
        final float exponent = 1 / 0.45f;
        return value >= K * epsilon ? (float) Math.pow((value + offset) / (1 + offset), exponent) : value / K;
    }

    public void convertYCbCrToRGB(final MutableVector1x3 input, final MutableVector1x3 output) {
        final float Y = input.a;
        final float Cb = input.b;
        final float Cr = input.c;

        final float blue = (2 * (luminance_coefficients.green + luminance_coefficients.red)) * Cb + Y;
        final float red = (2 * (luminance_coefficients.green + luminance_coefficients.blue)) * Cr + Y;
        final float green = (Y - luminance_coefficients.red * red - luminance_coefficients.blue * blue) / luminance_coefficients.green;

        output.a = linearize(red);
        output.b = linearize(green);
        output.c = linearize(blue);
    }
}
