package info.kmichel.colordroid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NamedLuvColor {
    public final String name;
    public final float l;
    public final float u;
    public final float v;

    NamedLuvColor(final String name, final float l, final float u, final float v) {
        this.name = name;
        this.l = l;
        this.u = u;
        this.v = v;
    }

    public float getSquaredDistance(final NamedLuvColor color) {
        final float delta_l = color.l - l;
        final float delta_u = color.u - u;
        final float delta_v = color.v - v;
        return delta_l * delta_l + delta_u * delta_u + delta_v * delta_v;
    }

    public float getDistanceOnAxis(final NamedLuvColor color, final int axis) {
        switch (axis % 3) {
            case 0:
                return l - color.l;
            case 1:
                return u - color.u;
            case 2:
                return v - color.v;
            default:
                throw new IllegalStateException("Axis should be in [0, 3[ range");
        }
    }

    public static List<Comparator<NamedLuvColor>> getComparators() {
        final List<Comparator<NamedLuvColor>> comparators = new ArrayList<Comparator<NamedLuvColor>>(3);
        comparators.add(new Comparator<NamedLuvColor>() {
            @Override
            public int compare(final NamedLuvColor a, final NamedLuvColor b) {
                return a.l < b.l ? -1 : a.l > b.l ? 1 : 0;
            }
        });
        comparators.add(new Comparator<NamedLuvColor>() {
            @Override
            public int compare(final NamedLuvColor a, final NamedLuvColor b) {
                return a.u < b.u ? -1 : a.u > b.u ? 1 : 0;
            }
        });
        comparators.add(new Comparator<NamedLuvColor>() {
            @Override
            public int compare(final NamedLuvColor a, final NamedLuvColor b) {
                return a.v < b.v ? -1 : a.v > b.v ? 1 : 0;
            }
        });
        return comparators;
    }
}
