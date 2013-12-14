package info.kmichel.color;

public class Illuminant {
    public final XYZColor white;

    public static final Illuminant C = new Illuminant(new XYZColor(0.98074f, 1.0f, 1.18232f));
    public static final Illuminant D65 = new Illuminant(new XYZColor(0.95047f, 1.0f, 1.08883f));

    public Illuminant(final XYZColor white) {
        this.white = white;
    }

    public Illuminant(final xyYColor white) {
        this.white = white.asXYZ();
    }
}
