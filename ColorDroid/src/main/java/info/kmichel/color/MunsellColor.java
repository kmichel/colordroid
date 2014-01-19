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
}
