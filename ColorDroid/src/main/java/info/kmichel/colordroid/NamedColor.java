package info.kmichel.colordroid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import info.kmichel.color.LuvColor;
import info.kmichel.color.MunsellColor;
import info.kmichel.math.KDTreeElement;

public class NamedColor implements KDTreeElement<NamedColor> {
    public final String short_name;
    public final MunsellColor munsell_color;
    public final LuvColor luv_color;

    public NamedColor(final String short_name, final MunsellColor munsell_color, final LuvColor luv_color) {
        this.short_name = short_name;
        this.munsell_color = munsell_color;
        this.luv_color = luv_color;
    }

    @Override
    public float getSquaredDistance(final NamedColor named_color) {
        final float delta_l = named_color.luv_color.L - luv_color.L;
        final float delta_u = named_color.luv_color.u - luv_color.u;
        final float delta_v = named_color.luv_color.v - luv_color.v;
        return delta_l * delta_l + delta_u * delta_u + delta_v * delta_v;
    }

    @Override
    public float getDistanceOnAxis(final NamedColor named_color, final int axis) {
        switch (axis % 3) {
            case 0:
                return named_color.luv_color.L - luv_color.L;
            case 1:
                return named_color.luv_color.u - luv_color.u;
            case 2:
                return named_color.luv_color.v - luv_color.v;
            default:
                throw new IllegalStateException("Axis should be in [0, 3[ range");
        }
    }

    public static List<Comparator<NamedColor>> getComparators() {
        final List<Comparator<NamedColor>> comparators = new ArrayList<Comparator<NamedColor>>(3);
        comparators.add(new Comparator<NamedColor>() {
            @Override
            public int compare(final NamedColor a, final NamedColor b) {
                return a.luv_color.L < b.luv_color.L ? -1 : a.luv_color.L > b.luv_color.L ? 1 : 0;
            }
        });
        comparators.add(new Comparator<NamedColor>() {
            @Override
            public int compare(final NamedColor a, final NamedColor b) {
                return a.luv_color.u < b.luv_color.u ? -1 : a.luv_color.u > b.luv_color.u ? 1 : 0;
            }
        });
        comparators.add(new Comparator<NamedColor>() {
            @Override
            public int compare(final NamedColor a, final NamedColor b) {
                return a.luv_color.v < b.luv_color.v ? -1 : a.luv_color.v > b.luv_color.v ? 1 : 0;
            }
        });
        return comparators;
    }
}
