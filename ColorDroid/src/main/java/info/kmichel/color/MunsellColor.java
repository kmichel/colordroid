package info.kmichel.color;

public class MunsellColor {
    public static class MunsellHue {
        public final MunsellHueBand band;
        public final float value;

        public MunsellHue(final MunsellHueBand band, final float value) {
            this.band = band;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("%1.1f%s", value, band.toString());
        }
    }

    public enum MunsellHueBand {
        R,
        YR,
        Y,
        GY,
        G,
        BG,
        B,
        PB,
        P,
        RP
    }

    public final MunsellHue hue;
    public final int value;
    public final int chroma;

    public MunsellColor(final MunsellHue hue, final int value, final int chroma) {
        this.hue = hue;
        this.value = value;
        this.chroma = chroma;
    }

    @Override
    public String toString() {
        return String.format("%s %d %d", hue, value, chroma);
    }

    public int getSegment() {
        final int segment_in_band = (int) Math.floor(hue.value / 2.5f);
        switch (hue.band) {
            case R:
                return segment_in_band;
            case YR:
                return 5 + segment_in_band;
            case Y:
                return 10 + segment_in_band;
            case GY:
                return 15 + segment_in_band;
            case G:
                return 20 + segment_in_band;
            case BG:
                return 25 + segment_in_band;
            case B:
                return 30 + segment_in_band;
            case PB:
                return 35 + segment_in_band;
            case P:
                return 40 + segment_in_band;
            case RP:
                return 45 + segment_in_band;
            default:
                throw new IllegalStateException("Invalid Hue Band");
        }
    }
}
