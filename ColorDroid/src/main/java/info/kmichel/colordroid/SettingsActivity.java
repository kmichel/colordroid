package info.kmichel.colordroid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

import info.kmichel.StrictModeHelper;
import info.kmichel.StrictModeSeverity;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new StrictModeHelper(this).setSeverity(StrictModeSeverity.LOG_ON_VIOLATE);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
