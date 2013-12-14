package info.kmichel.color;

import info.kmichel.math.MutableVector1x3;

public class LuvColorSpace {
    public final XYZColor white;

    private final float u_prime_r;
    private final float v_prime_r;

    public LuvColorSpace(final XYZColor white) {
        this.white = white;
        u_prime_r = (4 * white.X) / (white.X + 15 * white.Y + 3 * white.Z);
        v_prime_r = (9 * white.Y) / (white.X + 15 * white.Y + 3 * white.Z);
    }

    public LuvColor convert(final XYZColor color) {
        final float uv_divisor = (color.X + 15 * color.Y + 3 * color.Z);
        // When the XYZ color is 'black', we give it the same chromacity coordinates as our reference white
        final float u_prime = uv_divisor == 0 ? u_prime_r : (4 * color.X) / uv_divisor;
        final float v_prime = uv_divisor == 0 ? v_prime_r : (9 * color.Y) / uv_divisor;

        final float y_r = color.Y / white.Y;

        // These are the non-standard 'perfect' values for K and epsilon
        final float K = 24389.f / 27.f;
        final float epsilon = 216.f / 24389.f;
        final float L = y_r > epsilon ? 116 * (float) Math.pow(y_r, 1.0 / 3) - 16 : K * y_r;
        final float u = 13 * L * (u_prime - u_prime_r);
        final float v = 13 * L * (v_prime - v_prime_r);

        return new LuvColor(L, u, v);
    }

    public XYZColor convert(final LuvColor color) {
        // These are the non-standard 'perfect' values for K and epsilon
        final float K = 24389.f / 27.f;
        final float epsilon = 216.f / 24389.f;
        final float Y = color.L > K * epsilon ? (float) Math.pow((color.L + 16) / 116, 3) : color.L / K;

        final float a = (1.f / 3) * ((52 * color.L) / (color.u + 13 * color.L * u_prime_r) - 1);
        final float b = -5 * Y;
        final float c = -1.f / 3;
        final float d = Y * (((39 * color.L) / (color.v + 13 * color.L * v_prime_r)) - 5);

        final float X = (d - b) / (a - c);
        final float Z = X * a + b;

        return new XYZColor(X, Y, Z);
    }

    public void convertXYZToLuv(final MutableVector1x3 input, final MutableVector1x3 output) {
        final float X = input.a;
        final float Y = input.b;
        final float Z = input.c;

        final float uv_divisor = (X + 15 * Y + 3 * Z);
        // When the XYZ color is 'black', we give it the same chromacity coordinates as our reference white
        final float u_prime = uv_divisor == 0 ? u_prime_r : (4 * X) / uv_divisor;
        final float v_prime = uv_divisor == 0 ? v_prime_r : (9 * Y) / uv_divisor;

        final float y_r = Y / white.Y;

        // These are the non-standard 'perfect' values for K and epsilon
        final float K = 24389.f / 27.f;
        final float epsilon = 216.f / 24389.f;
        final float L = y_r > epsilon ? 116 * (float) Math.pow(y_r, 1.0 / 3) - 16 : K * y_r;
        final float u = 13 * L * (u_prime - u_prime_r);
        final float v = 13 * L * (v_prime - v_prime_r);

        output.a = L;
        output.b = u;
        output.c = v;
    }
}
