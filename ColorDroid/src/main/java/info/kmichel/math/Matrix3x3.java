package info.kmichel.math;

public class Matrix3x3 {
    public final float a;
    public final float b;
    public final float c;
    public final float d;
    public final float e;
    public final float f;
    public final float g;
    public final float h;
    public final float i;

    public Matrix3x3(final float a, final float b, final float c,
                     final float d, final float e, final float f,
                     final float g, final float h, final float i) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
        this.g = g;
        this.h = h;
        this.i = i;
    }

    public float determinant() {
        return (a * e * i
                + b * f * g
                + c * d * h
                - c * e * g
                - b * d * i
                - a * f * h);
    }

    public Matrix3x3 inverse() {
        return new Matrix3x3(
                e * i - f * h,
                c * h - b * i,
                b * f - c * e,
                f * g - d * i,
                a * i - c * g,
                c * d - a * f,
                d * h - e * g,
                b * g - a * h,
                a * e - b * d).multiply(1 / determinant());
    }

    public Matrix3x3 multiply(final float scalar) {
        return new Matrix3x3(
                a * scalar, b * scalar, c * scalar,
                d * scalar, e * scalar, f * scalar,
                g * scalar, h * scalar, i * scalar);
    }

    public Vector1x3 multiply(final Vector1x3 vector) {
        return new Vector1x3(
                a * vector.a + b * vector.b + c * vector.c,
                d * vector.a + e * vector.b + f * vector.c,
                g * vector.a + h * vector.b + i * vector.c);
    }

    public void multiply(final MutableVector1x3 input, final MutableVector1x3 output) {
        assert (input != output);
        output.a = a * input.a + b * input.b + c * input.c;
        output.b = d * input.a + e * input.b + f * input.c;
        output.c = g * input.a + h * input.b + i * input.c;
    }

    public Matrix3x3 multiply(final Matrix3x3 matrix) {
        return new Matrix3x3(
                a * matrix.a + b * matrix.d + c * matrix.g,
                a * matrix.b + b * matrix.e + c * matrix.h,
                a * matrix.c + b * matrix.f + c * matrix.i,
                d * matrix.a + e * matrix.d + f * matrix.g,
                d * matrix.b + e * matrix.e + f * matrix.h,
                d * matrix.c + e * matrix.f + f * matrix.i,
                g * matrix.a + h * matrix.d + i * matrix.g,
                g * matrix.b + h * matrix.e + i * matrix.h,
                g * matrix.c + h * matrix.f + i * matrix.i);
    }
}
