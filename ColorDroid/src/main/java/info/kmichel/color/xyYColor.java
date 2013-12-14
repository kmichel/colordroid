package info.kmichel.color;

public class xyYColor {
    public final float x;
    public final float y;
    public final float Y;

    public xyYColor(final float x, final float y, final float Y) {
        this.x = x;
        this.y = y;
        this.Y = Y;
    }

    public XYZColor asXYZ() {
        if (y == 0)
            return new XYZColor(0, 0, 0);
        else
            return new XYZColor(
                    x * Y / y,
                    Y,
                    (1 - x - y) * Y / y);
    }
}
