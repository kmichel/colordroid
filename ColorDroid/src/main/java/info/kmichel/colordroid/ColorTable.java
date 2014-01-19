package info.kmichel.colordroid;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.kmichel.color.ChromaticAdaptation;
import info.kmichel.color.Illuminant;
import info.kmichel.color.LuvColor;
import info.kmichel.color.LuvColorSpace;
import info.kmichel.color.MunsellColor;
import info.kmichel.color.xyYColor;
import info.kmichel.math.KDTree;

public class ColorTable {
    private static final String TAG = "ColorTable";

    private KDTree<NamedColor> color_tree;

    public void loadMunsellData(final Context context) throws IOException {
        final List<NamedColor> colors = new ArrayList<NamedColor>();

        final InputStream input_stream = context.getAssets().open("munsell/real.dat");
        final InputStreamReader input_reader = new InputStreamReader(input_stream);
        final BufferedReader reader = new BufferedReader(input_reader);

        final Map<String, String> alias = new HashMap<String, String>();

        alias.put("R", "Red");
        alias.put("YR", "Orange");
        alias.put("Y", "Yellow");
        alias.put("GY", "Lime");
        alias.put("G", "Green");
        alias.put("BG", "Blue-Green");
        alias.put("B", "Blue");
        alias.put("PB", "Purple-Blue");
        alias.put("P", "Purple");
        alias.put("RP", "Red-Purple");

        final ChromaticAdaptation c_to_d65 = new ChromaticAdaptation(ChromaticAdaptation.bradford, Illuminant.C.white, Illuminant.D65.white);

        final LuvColorSpace luv_color_space = new LuvColorSpace(Illuminant.D65.white);

        int line_number = 0;
        final String float_pattern = "(\\d+(?:\\.\\d+)?)";
        final String int_pattern = "(\\d+)";
        final Pattern pattern = Pattern.compile(
                "^\\s*" + float_pattern + "([A-Z]+)\\s+" + int_pattern + "\\s+" + int_pattern + "\\s+"
                        + float_pattern + "\\s+" + float_pattern + "\\s+" + float_pattern + "\\s*");
        while (true) {
            line_number += 1;
            final String line = reader.readLine();
            if (line == null)
                break;
            final Matcher matcher = pattern.matcher(line);
            if (!matcher.find()) {
                if (line_number != 1)
                    Log.d(TAG, "Ignored line " + line_number + ":" + line);
                continue;
            }
            try {
                final float munsell_hue_value = Float.parseFloat(matcher.group(1));
                final MunsellColor.MunsellHueBand munsell_hue_band = MunsellColor.MunsellHueBand.valueOf(matcher.group(2));
                final int munsell_value = Integer.parseInt(matcher.group(3), 10);
                final int munsell_chroma = Integer.parseInt(matcher.group(4), 10);

                final float x = Float.parseFloat(matcher.group(5));
                final float y = Float.parseFloat(matcher.group(6));
                final float Y = Float.parseFloat(matcher.group(7)) * 0.01f;
                //noinspection ObjectAllocationInLoop
                final LuvColor luv_color = luv_color_space.convert(
                        c_to_d65.adapt(new xyYColor(x, y, Y).asXYZ()));
                //noinspection ObjectAllocationInLoop
                final MunsellColor.MunsellHue munsell_hue = new MunsellColor.MunsellHue(munsell_hue_band, munsell_hue_value);
                //noinspection ObjectAllocationInLoop
                final MunsellColor munsell_color = new MunsellColor(munsell_hue, munsell_value, munsell_chroma);
                //noinspection ObjectAllocationInLoop
                final NamedColor named_color = new NamedColor(alias.get(munsell_hue_band.toString()), munsell_color, luv_color);
                //noinspection ObjectAllocationInLoop
                colors.add(named_color);
            } catch (final NumberFormatException e) {
                Log.e(TAG, "Ignored line " + line_number + ":" + line, e);
            }
        }

        final KDTree<NamedColor> local_color_tree = KDTree.buildTree(colors, NamedColor.getComparators());
        synchronized (this) {
            color_tree = local_color_tree;
        }
    }

    public synchronized NamedColor getNearestColor(final float l, final float u, final float v) {
        if (color_tree != null)
            return color_tree.getNearestElement(new NamedColor("Target", null, new LuvColor(l, u, v)));
        else
            return null;
    }
}
