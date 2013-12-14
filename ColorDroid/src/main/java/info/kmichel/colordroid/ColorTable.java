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

import info.kmichel.color.ChromaticAdaptation;
import info.kmichel.color.Illuminant;
import info.kmichel.color.LuvColor;
import info.kmichel.color.LuvColorSpace;
import info.kmichel.color.xyYColor;
import info.kmichel.math.KDTree;

public class ColorTable {
    private static final String TAG = "ColorTable";

    private KDTree color_tree;

    public void loadMunsellData(final Context context) throws IOException {
        final List<NamedLuvColor> colors = new ArrayList<NamedLuvColor>();

        final InputStream input_stream = context.getAssets().open("munsell/real.dat");
        final InputStreamReader input_reader = new InputStreamReader(input_stream);
        final BufferedReader reader = new BufferedReader(input_reader);

        final Map<String, String> alias = new HashMap<String, String>();

        alias.put("2.5R", "Red");
        alias.put("5R", "Red");
        alias.put("7.5R", "Red");
        alias.put("10R", "Red");
        alias.put("2.5YR", "Orange");
        alias.put("5YR", "Orange");
        alias.put("7.5YR", "Orange");
        alias.put("10YR", "Orange");
        alias.put("2.5Y", "Yellow");
        alias.put("5Y", "Yellow");
        alias.put("7.5Y", "Yellow");
        alias.put("10Y", "Yellow");
        alias.put("2.5GY", "Lime");
        alias.put("5GY", "Lime");
        alias.put("7.5GY", "Lime");
        alias.put("10GY", "Lime");
        alias.put("2.5G", "Green");
        alias.put("5G", "Green");
        alias.put("7.5G", "Green");
        alias.put("10G", "Green");
        alias.put("2.5BG", "Blue-Green");
        alias.put("5BG", "Blue-Green");
        alias.put("7.5BG", "Blue-Green");
        alias.put("10BG", "Blue-Green");
        alias.put("2.5B", "Blue");
        alias.put("5B", "Blue");
        alias.put("7.5B", "Blue");
        alias.put("10B", "Blue");
        alias.put("2.5PB", "Purple-Blue");
        alias.put("5PB", "Purple-Blue");
        alias.put("7.5PB", "Purple-Blue");
        alias.put("10PB", "Purple-Blue");
        alias.put("2.5P", "Purple");
        alias.put("5P", "Purple");
        alias.put("7.5P", "Purple");
        alias.put("10P", "Purple");
        alias.put("2.5RP", "Red-Purple");
        alias.put("5RP", "Red-Purple");
        alias.put("7.5RP", "Red-Purple");
        alias.put("10RP", "Red-Purple");

        final ChromaticAdaptation c_to_d65 = new ChromaticAdaptation(ChromaticAdaptation.bradford, Illuminant.C.white, Illuminant.D65.white);

        final LuvColorSpace luv_color_space = new LuvColorSpace(Illuminant.D65.white);

        int line_number = 1;
        while (true) {
            final String line = reader.readLine();
            if (line == null)
                break;
            final String[] fields = line.split("\\s+");
            if (fields.length != 7) {
                Log.d(TAG, "Ignored line " + line_number + ":" + line);
                continue;
            }
            if (fields[1].equals("H") || fields[1].equals("h"))
                continue;
            try {
                final float x = Float.parseFloat(fields[4]);
                final float y = Float.parseFloat(fields[5]);
                final float Y = Float.parseFloat(fields[6]) * 0.01f;
                //noinspection ObjectAllocationInLoop
                final LuvColor luv_color = luv_color_space.convert(
                        c_to_d65.adapt(new xyYColor(x, y, Y).asXYZ()));
                //noinspection ObjectAllocationInLoop
                colors.add(new NamedLuvColor(alias.get(fields[1]), luv_color.L, luv_color.u, luv_color.v));
                line_number += 1;
            } catch (final NumberFormatException e) {
                Log.e(TAG, "Ignored line " + line_number + ":" + line, e);
            }
        }

        final KDTree local_color_tree = KDTree.buildTree(colors, NamedLuvColor.getComparators());
        synchronized (this) {
            color_tree = local_color_tree;
        }
    }

    public synchronized NamedLuvColor getNearestColor(final float l, final float u, final float v) {
        if (color_tree != null)
            return color_tree.getNearestElement(new NamedLuvColor("Target", l, u, v));
        else
            return null;
    }
}
