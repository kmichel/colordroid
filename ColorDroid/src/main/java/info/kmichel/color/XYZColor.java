package info.kmichel.color;

import info.kmichel.math.Vector1x3;

public class XYZColor {
    final float X;
    final float Y;
    final float Z;

    public XYZColor(final float X, final float Y, final float Z) {
        this.X = X;
        this.Y = Y;
        this.Z = Z;
    }

    public XYZColor(final Vector1x3 vector) {
        X = vector.a;
        Y = vector.b;
        Z = vector.c;
    }

    public Vector1x3 asVector() {
        return new Vector1x3(X, Y, Z);
    }

    public xyYColor asxyY(final xyYColor white) {
        if (X + Y + Z == 0)
            return new xyYColor(
                    white.x,
                    white.y,
                    0);
        else
            return new xyYColor(
                    X / (X + Y + Z),
                    Y / (X + Y + Z),
                    Y);
    }
}
