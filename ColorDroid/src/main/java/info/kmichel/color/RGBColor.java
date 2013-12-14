package info.kmichel.color;

import info.kmichel.math.Vector1x3;

public class RGBColor {
    final float red;
    final float green;
    final float blue;

    public RGBColor(final float red, final float green, final float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public RGBColor(final Vector1x3 vector) {
        red = vector.a;
        green = vector.b;
        blue = vector.c;
    }

    public Vector1x3 asVector() {
        return new Vector1x3(red, green, blue);
    }
}
