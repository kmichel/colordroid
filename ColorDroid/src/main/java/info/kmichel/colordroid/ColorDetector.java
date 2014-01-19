package info.kmichel.colordroid;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import info.kmichel.camera.NV21Buffer;
import info.kmichel.color.Illuminant;
import info.kmichel.color.LuvColorSpace;
import info.kmichel.color.YCbCrColor;
import info.kmichel.color.YCbCrColorSpace;
import info.kmichel.color.YCbCrQuantization;
import info.kmichel.math.MutableVector1x3;

public class ColorDetector {
    private static final String TAG = "ColorDetector";

    private final ColorTable color_table;
    private final YCbCrColorSpace ycbcr_color_space;
    private final LuvColorSpace luv_color_space;
    private final YCbCrQuantization quantization_range;
    private final int ycbcr_buffer_width;
    private final int ycbcr_buffer_height;
    private final int[] ycbcr_buffer;
    private final int averaging_width;
    private final int averaging_height;

    public ColorDetector() {
        color_table = new ColorTable();

        ycbcr_color_space = new YCbCrColorSpace(YCbCrColorSpace.EncodingStandard.BT601_625);
        luv_color_space = new LuvColorSpace(Illuminant.D65.white);
        quantization_range = YCbCrQuantization.FULL_RANGE;

        ycbcr_buffer_width = 64;
        ycbcr_buffer_height = 64;
        ycbcr_buffer = new int[ycbcr_buffer_width * ycbcr_buffer_height];
        averaging_width = 12;
        averaging_height = 12;
    }

    public void loadMunsellData(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    color_table.loadMunsellData(context);
                } catch (final IOException e) {
                    Log.e(TAG, "Failed loading Munsell data", e);
                }
            }
        }).start();
    }

    public String detect_color(final NV21Buffer image) {
        // XXX: the nv21 buffer could be smaller than the ycbcrbuffer
        final int left = (image.width - ycbcr_buffer_width) / 4 * 2;
        final int right = left + ycbcr_buffer_width;
        final int top = (image.height - ycbcr_buffer_height) / 4 * 2;
        final int bottom = top + ycbcr_buffer_height;
        image.decode(ycbcr_buffer, left, top, right, bottom);
        return detect_color(ycbcr_buffer, ycbcr_buffer_width, ycbcr_buffer_height);
    }

    public String detect_color(final int[] ycbcr_buffer, final int buffer_width, final int buffer_height) {
        final int x_center = buffer_width / 2;
        final int y_center = buffer_height / 2;

        float L = 0;
        float u = 0;
        float v = 0;

        final MutableVector1x3 ycbcr_color = new MutableVector1x3(0, 0, 0);
        final MutableVector1x3 rgb_color = new MutableVector1x3(0, 0, 0);
        final MutableVector1x3 xyz_color = new MutableVector1x3(0, 0, 0);
        final MutableVector1x3 luv_color = new MutableVector1x3(0, 0, 0);

        final int row_start = x_center - averaging_width / 2;
        final int column_start = y_center - averaging_height / 2;
        for (int row = row_start; row < row_start + averaging_height; ++row)
            for (int col = column_start; col < column_start + averaging_width; ++col) {
                final int pixel = ycbcr_buffer[row * buffer_width + col];

                YCbCrColor.unpack(pixel, quantization_range, ycbcr_color);
                ycbcr_color_space.convertYCbCrToRGB(ycbcr_color, rgb_color);
                ycbcr_color_space.rgb_color_space.convertRGBToXYZ(rgb_color, xyz_color);
                luv_color_space.convertXYZToLuv(xyz_color, luv_color);

                L += luv_color.a;
                u += luv_color.b;
                v += luv_color.c;
            }

        L /= averaging_height * averaging_width;
        u /= averaging_height * averaging_width;
        v /= averaging_height * averaging_width;

        return detect_color(L, u, v);
    }

    public String detect_color(final float L, final float u, final float v) {
        final NamedColor named_color = color_table.getNearestColor(L, u, v);
        if (named_color == null)
            return "";
        return named_color.short_name;
    }

}
