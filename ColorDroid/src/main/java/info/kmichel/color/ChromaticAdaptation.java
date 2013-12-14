package info.kmichel.color;


import info.kmichel.math.Matrix3x3;
import info.kmichel.math.Vector1x3;

public class ChromaticAdaptation {
    public final Matrix3x3 cone_response_domain;
    public final XYZColor source_white;
    public final XYZColor destination_white;

    public final Matrix3x3 adaptation_matrix;

    public static final Matrix3x3 xyz_scaling = new Matrix3x3(
            1.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 1.0f);

    public static final Matrix3x3 von_kries = new Matrix3x3(
            0.4002400f, 0.7076000f, -0.0808100f,
            -0.2263000f, 1.1653200f, 0.0457000f,
            0.0000000f, 0.0000000f, 0.9182200f);

    public static final Matrix3x3 bradford = new Matrix3x3(
            0.8951000f, 0.2664000f, -0.1614000f,
            -0.7502000f, 1.7135000f, 0.0367000f,
            0.0389000f, -0.0685000f, 1.0296000f);

    public ChromaticAdaptation(final Matrix3x3 cone_response_domain, final XYZColor source_white, final XYZColor destination_white) {
        this.cone_response_domain = cone_response_domain;
        this.source_white = source_white;
        this.destination_white = destination_white;

        adaptation_matrix = buildAdaptationMatrix();
    }

    private Matrix3x3 buildAdaptationMatrix() {
        final Matrix3x3 inverse_cone_response_domain = cone_response_domain.inverse();

        final Vector1x3 source_response = cone_response_domain.multiply(source_white.asVector());
        final Vector1x3 destination_response = cone_response_domain.multiply(destination_white.asVector());

        final Matrix3x3 tmp = new Matrix3x3(
                destination_response.a / source_response.a, 0.0f, 0.0f,
                0.0f, destination_response.b / source_response.b, 0.0f,
                0.0f, 0.0f, destination_response.c / source_response.c);

        return inverse_cone_response_domain.multiply(tmp.multiply(cone_response_domain));
    }

    public XYZColor adapt(final XYZColor color) {
        return new XYZColor(adaptation_matrix.multiply(color.asVector()));
    }
}
