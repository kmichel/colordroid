package info.kmichel;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

public class StrictModeHelper {
    private static final String TAG = "StrictModeHelper";

    private final Activity activity;

    public StrictModeHelper(final Activity activity) {
        this.activity = activity;
    }

    public void setSeverity(final StrictModeSeverity severity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            final ApplicationInfo application_info = activity.getApplicationInfo();
            if (application_info != null && (application_info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                Log.d(TAG, "Setting strict mode severity to " + severity + " in activity " + activity);
                final StrictMode.ThreadPolicy.Builder thread_policy_builder = new StrictMode.ThreadPolicy.Builder();
                final StrictMode.VmPolicy.Builder vm_policy_builder = new StrictMode.VmPolicy.Builder();
                thread_policy_builder.detectAll().penaltyLog();
                vm_policy_builder.detectAll().penaltyLog();
                if (severity == StrictModeSeverity.KILL_ON_VIOLATE) {
                    thread_policy_builder.penaltyDeath();
                    vm_policy_builder.penaltyDeath();
                }
                StrictMode.setThreadPolicy(thread_policy_builder.build());
                StrictMode.setVmPolicy(vm_policy_builder.build());
            }
        }
    }
}
