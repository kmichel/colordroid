package info.kmichel;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PreferenceSummary implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "PreferenceSummary";

    private final Preference preference;

    public PreferenceSummary(final Preference preference) {
        this.preference = preference;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void bindPreferences(final PreferenceFragment fragment, final int xml_id) {
        for (final String key : getPreferences(fragment.getResources(), xml_id))
            //noinspection ObjectAllocationInLoop
            new PreferenceSummary(fragment.findPreference(key)).bindToValue();
    }

    public static void bindPreferences(final PreferenceActivity activity, final int xml_id) {
        for (final String key : getPreferences(activity.getResources(), xml_id))
            //noinspection ObjectAllocationInLoop,deprecation
            new PreferenceSummary(activity.findPreference(key)).bindToValue();
    }

    public static List<String> getPreferences(final Resources resources, final int xml_id) {
        final List<String> keys = new ArrayList<String>();
        if (resources == null)
            return keys;
        final XmlResourceParser parser = resources.getXml(xml_id);
        if (parser == null)
            return keys;
        try {
            for (int eventType = parser.getEventType(); eventType != XmlResourceParser.END_DOCUMENT; eventType = parser.next())
                if (eventType == XmlResourceParser.START_TAG) {
                    final String tag_name = parser.getName();
                    if ("ListPreference".equals(tag_name) || "EditTextPreference".equals(tag_name)) {
                        final String key = parser.getAttributeValue("http://schemas.android.com/apk/res/android", "key");
                        if (key != null)
                            keys.add(key);
                    }
                }
        } catch (final IOException e) {
            Log.e(TAG, "Error while binding preference summary", e);
        } catch (final XmlPullParserException e) {
            Log.e(TAG, "Error while binding preference summary", e);
        }
        return keys;
    }

    public void bindToValue() {
        preference.setOnPreferenceChangeListener(this);
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object value) {
        final String stringValue = value.toString();
        if (preference instanceof ListPreference) {
            final ListPreference listPreference = (ListPreference) preference;
            final int index = listPreference.findIndexOfValue(stringValue);
            final CharSequence[] entries = listPreference.getEntries();
            if (entries != null)
                preference.setSummary(index >= 0 && index < entries.length ? entries[index] : null);
        } else {
            preference.setSummary(stringValue);
        }
        return true;
    }
}
