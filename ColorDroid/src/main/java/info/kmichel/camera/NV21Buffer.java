package info.kmichel.camera;

import android.graphics.ImageFormat;

public class NV21Buffer {
    public final int width;
    public final int height;
    public final byte[] data;

    public NV21Buffer(final int width, final int height) {
        this.width = width;
        this.height = height;
        final int BYTE_SIZE = 8;
        final int buffer_size = width * height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / BYTE_SIZE;
        data = new byte[buffer_size];
    }

    public void decode(final int[] ycbcr_buffer, final int left, final int top, final int right, final int bottom) {
        final int input_size = width * height;
        final int output_width = right - left;
        final int output_height = bottom - top;
        final int output_size = output_width * output_height;
        assert (output_width >= 0);
        assert (output_height >= 0);
        assert (ycbcr_buffer.length == output_size);

        assert (left % 2 == 0);
        assert (top % 2 == 0);
        assert (right % 2 == 0);
        assert (bottom % 2 == 0);

        int output_row_start = 0;
        for (int row_start = top * width; row_start < bottom * width; row_start += 2 * width) {
            for (int column = left; column < right; column += 2) {
                // Read four pixels of Y in a 2x2 block
                final int block_start = row_start + column;
                final int y1 = data[block_start] & 0xff;
                final int y2 = data[block_start + 1] & 0xff;
                final int y3 = data[block_start + width] & 0xff;
                final int y4 = data[block_start + width + 1] & 0xff;

                // Rounding is important here, do not simplify
                final int small_block_start = ((row_start >> 2) + (column >> 1)) << 1;
                // We add input_size because U and V are after the Y plane
                // We only have a single U and V value pair for the whole 2x2 block
                final int cr = data[input_size + small_block_start] & 0xff;
                final int cb = data[input_size + small_block_start + 1] & 0xff;

                final int out_start = output_row_start + column - left;
                ycbcr_buffer[out_start] = y1 | cb << 8 | cr << 16 | 0xff << 24;
                ycbcr_buffer[out_start + 1] = y2 | cb << 8 | cr << 16 | 0xff << 24;
                ycbcr_buffer[out_start + output_width] = y3 | cb << 8 | cr << 16 | 0xff << 24;
                ycbcr_buffer[out_start + output_width + 1] = y4 | cb << 8 | cr << 16 | 0xff << 24;
            }
            output_row_start += 2 * output_width;
        }
    }
}
