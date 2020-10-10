package com.qualcomm.ftcdriverstation;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.widget.FrameLayout;

import org.firstinspires.ftc.robotcore.internal.network.DeviceNameManagerFactory;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;
import org.firstinspires.ftc.robotcore.internal.ui.BaseActivity;

public class FtcDriverStationInspectionReportsActivity extends BaseActivity {
    protected static final String CLIENT_CONNECTED = "CLIENT_CONNECTED";
    public static final String TAG = "FtcDriverStationInspectionReportsActivity";

    public String getTag() {
        return TAG;
    }

    public static class SettingsFragment extends PreferenceFragment {
        protected boolean clientConnected = false;

        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            this.clientConnected = getArguments().getBoolean(FtcDriverStationInspectionReportsActivity.CLIENT_CONNECTED);
            addPreferencesFromResource(R.xml.inspection);
            if (!this.clientConnected) {
                findPreference(getString(R.string.pref_launch_inspect_rc)).setEnabled(false);
            }
        }
    }

    /* access modifiers changed from: protected */
    public FrameLayout getBackBar() {
        return (FrameLayout) findViewById(R.id.backbar);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_generic_settings);
        DeviceNameManagerFactory.getInstance().initializeDeviceNameIfNecessary();
        SettingsFragment settingsFragment = new SettingsFragment();
        Bundle bundle2 = new Bundle();
        bundle2.putBoolean(CLIENT_CONNECTED, new PreferencesHelper(TAG, (Context) this).readBoolean(getString(R.string.pref_rc_connected), false));
        settingsFragment.setArguments(bundle2);
        getFragmentManager().beginTransaction().replace(R.id.container, settingsFragment).commit();
    }
}
