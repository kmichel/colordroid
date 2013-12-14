package info.kmichel.colordroid;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import info.kmichel.PreferenceSummary;
import info.kmichel.StrictModeHelper;
import info.kmichel.StrictModeSeverity;

public class DeprecatedSettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new StrictModeHelper(this).setSeverity(StrictModeSeverity.LOG_ON_VIOLATE);
        //noinspection deprecation
        addPreferencesFromResource(R.xml.preferences);
        PreferenceSummary.bindPreferences(this, R.xml.preferences);
    }

}
