package info.kmichel.color;


import info.kmichel.math.MutableVector1x3;

public class YCbCrColor {
    final float y;
    final float cb;
    final float cr;

    public YCbCrColor(final int y_quant, final int cb_quant, final int cr_quant, final YCbCrQuantization quantization_range) {
        switch (quantization_range) {
            case STANDARD_RANGE:
                y = (y_quant - 16) / 219.f;
                cb = (cb_quant - 128) / 224.f;
                cr = (cr_quant - 128) / 224.f;
                break;
            case FULL_RANGE:
                y = y_quant / 255.f;
                cb = (cb_quant - 128) / 255.f;
                cr = (cr_quant - 128) / 255.f;
                break;
            default:
                throw new IllegalArgumentException("Invalid quantization range");
        }
    }

    public static YCbCrColor unpack(final int pixel, final YCbCrQuantization quantization_range) {
        return new YCbCrColor(
                pixel & 0xff,
                (pixel >> 8) & 0xff,
                (pixel >> 16) & 0xff,
                quantization_range);
    }

    public static void unpack(final int pixel, final YCbCrQuantization quantization_range, final MutableVector1x3 output) {
        final int y_quant = pixel & 0xff;
        final int cb_quant = (pixel >> 8) & 0xff;
        final int cr_quant = (pixel >> 16) & 0xff;
        switch (quantization_range) {
            case STANDARD_RANGE:
                output.a = (y_quant - 16) / 219.f;
                output.b = (cb_quant - 128) / 224.f;
                output.c = (cr_quant - 128) / 224.f;
                break;
            case FULL_RANGE:
                output.a = y_quant / 255.f;
                output.b = (cb_quant - 128) / 255.f;
                output.c = (cr_quant - 128) / 255.f;
                break;
            default:
                throw new IllegalArgumentException("Invalid quantization range");
        }
    }
}
