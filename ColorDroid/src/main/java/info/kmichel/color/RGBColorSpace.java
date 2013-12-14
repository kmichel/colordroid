package info.kmichel.color;

import info.kmichel.math.Matrix3x3;
import info.kmichel.math.MutableVector1x3;
import info.kmichel.math.Vector1x3;

public class RGBColorSpace {
    public final XYZColor red;
    public final XYZColor green;
    public final XYZColor blue;
    public final XYZColor white;

    private final Matrix3x3 rgb_to_xyz_matrix;
    private final Matrix3x3 xyz_to_rgb_matrix;

    public RGBColorSpace(final xyYColor red, final xyYColor green, final xyYColor blue, final xyYColor white) {
        this.red = red.asXYZ();
        this.green = green.asXYZ();
        this.blue = blue.asXYZ();
        this.white = white.asXYZ();

        rgb_to_xyz_matrix = buildRGBToXYZMatrix();
        xyz_to_rgb_matrix = rgb_to_xyz_matrix.inverse();
    }

    public RGBColorSpace(final XYZColor red, final XYZColor green, final XYZColor blue, final XYZColor white) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.white = white;

        rgb_to_xyz_matrix = buildRGBToXYZMatrix();
        xyz_to_rgb_matrix = rgb_to_xyz_matrix.inverse();
    }

    private Matrix3x3 buildRGBToXYZMatrix() {
        final Matrix3x3 primaries = new Matrix3x3(
                red.X, green.X, blue.X,
                red.Y, green.Y, blue.Y,
                red.Z, green.Z, blue.Z);
        final Matrix3x3 inverse_primaries = primaries.inverse();
        final Vector1x3 tmp = inverse_primaries.multiply(white.asVector());
        return new Matrix3x3(
                tmp.a * red.X, tmp.b * green.X, tmp.c * blue.X,
                tmp.a * red.Y, tmp.b * green.Y, tmp.c * blue.Y,
                tmp.a * red.Z, tmp.b * green.Z, tmp.c * blue.Z);
    }

    public XYZColor convert(final RGBColor color) {
        return new XYZColor(rgb_to_xyz_matrix.multiply(color.asVector()));
    }

    public RGBColor convert(final XYZColor color) {
        return new RGBColor(xyz_to_rgb_matrix.multiply(color.asVector()));
    }

    public void convertRGBToXYZ(final MutableVector1x3 input, final MutableVector1x3 output) {
        rgb_to_xyz_matrix.multiply(input, output);
    }

}
