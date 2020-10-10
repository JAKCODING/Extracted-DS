package com.qualcomm.ftcdriverstation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.FrameLayout;
import com.google.gson.Gson;
import com.qualcomm.ftccommon.configuration.EditActivity;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.wifi.NetworkType;
import org.firstinspires.ftc.robotcore.internal.network.DeviceNameManagerFactory;
import org.firstinspires.ftc.robotcore.internal.network.WifiDirectDeviceNameManager;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;

public class FtcDriverStationSettingsActivity extends EditActivity {
    protected static final String CLIENT_CONNECTED = "CLIENT_CONNECTED";
    protected static final String HAS_SPEAKER = "HAS_SPEAKER";
    protected static final String RESULT = "RESULT";
    public static final String TAG = "FtcDriverStationSettingsActivity";
    protected boolean clientConnected = false;
    protected boolean hasSpeaker = false;
    protected NetworkType lastNetworkType;
    protected PreferencesHelper prefHelper;
    protected Result result = new Result();

    public static class Result {
        public boolean prefAdvancedClicked = false;
        public boolean prefLogsClicked = false;
        public boolean prefPairClicked = false;
        public boolean prefPairingMethodChanged = false;

        public String serialize() {
            return new Gson().toJson((Object) this);
        }

        public static Result deserialize(String str) {
            return (Result) new Gson().fromJson(str, Result.class);
        }
    }

    public static class SettingsFragment extends PreferenceFragment {
        protected FtcDriverStationSettingsActivity activity;
        protected PreferencesHelper prefHelper;

        public void setProperties(FtcDriverStationSettingsActivity ftcDriverStationSettingsActivity, PreferencesHelper preferencesHelper) {
            this.activity = ftcDriverStationSettingsActivity;
            this.prefHelper = preferencesHelper;
        }

        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            boolean z = getArguments().getBoolean(FtcDriverStationSettingsActivity.CLIENT_CONNECTED);
            boolean z2 = getArguments().getBoolean(FtcDriverStationSettingsActivity.HAS_SPEAKER);
            addPreferencesFromResource(R.xml.app_settings);
            if (!z) {
                findPreference(getString(R.string.pref_device_name_rc)).setEnabled(false);
                findPreference(getString(R.string.pref_app_theme_rc)).setEnabled(false);
                findPreference(getString(R.string.pref_sound_on_off_rc)).setEnabled(false);
            }
            if (!z2) {
                findPreference(getString(R.string.pref_sound_on_off_rc)).setEnabled(false);
            }
            findPreference(getString(R.string.pref_pair_rc)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    NetworkType fromString = NetworkType.fromString(SettingsFragment.this.prefHelper.readString(SettingsFragment.this.getString(R.string.pref_pairing_kind), NetworkType.globalDefaultAsString()));
                    RobotLog.vv(FtcDriverStationSettingsActivity.TAG, "prefPair clicked " + fromString);
                    SettingsFragment.this.activity.result.prefPairClicked = true;
                    if (fromString == NetworkType.WIFIDIRECT) {
                        SettingsFragment.this.startActivity(new Intent(SettingsFragment.this.activity, FtcPairNetworkConnectionActivity.class));
                        return false;
                    }
                    SettingsFragment.this.startActivity(new Intent(SettingsFragment.this.activity, FtcWirelessApNetworkConnectionActivity.class));
                    return false;
                }
            });
            findPreference(getString(R.string.pref_launch_advanced_rc_settings)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    RobotLog.vv(FtcDriverStationSettingsActivity.TAG, "prefAdvanced clicked");
                    SettingsFragment.this.activity.result.prefAdvancedClicked = true;
                    return false;
                }
            });
            findPreference(getString(R.string.pref_debug_driver_station_logs)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    RobotLog.vv(FtcDriverStationSettingsActivity.TAG, "prefLogs clicked");
                    SettingsFragment.this.activity.result.prefLogsClicked = true;
                    return false;
                }
            });
            findPreference(getString(R.string.pref_device_name)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object obj) {
                    if ((obj instanceof String) && WifiDirectDeviceNameManager.validDeviceName((String) obj)) {
                        return true;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsFragment.this.getActivity());
                    builder.setTitle(SettingsFragment.this.getString(R.string.prefedit_device_name_invalid_title));
                    builder.setMessage(SettingsFragment.this.getString(R.string.prefedit_device_name_invalid_text));
                    builder.setPositiveButton(17039370, (DialogInterface.OnClickListener) null);
                    builder.show();
                    return false;
                }
            });
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
        PreferencesHelper preferencesHelper = new PreferencesHelper(TAG, (Context) this);
        this.prefHelper = preferencesHelper;
        this.lastNetworkType = NetworkType.fromString(preferencesHelper.readString(getString(R.string.pref_pairing_kind), NetworkType.globalDefaultAsString()));
        this.clientConnected = this.prefHelper.readBoolean(getString(R.string.pref_rc_connected), false);
        this.hasSpeaker = this.prefHelper.readBoolean(getString(R.string.pref_has_speaker_rc), true);
        DeviceNameManagerFactory.getInstance().initializeDeviceNameIfNecessary();
        SettingsFragment settingsFragment = new SettingsFragment();
        Bundle bundle2 = new Bundle();
        bundle2.putBoolean(CLIENT_CONNECTED, this.clientConnected);
        bundle2.putBoolean(HAS_SPEAKER, this.hasSpeaker);
        settingsFragment.setArguments(bundle2);
        settingsFragment.setProperties(this, this.prefHelper);
        getFragmentManager().beginTransaction().replace(R.id.container, settingsFragment).commit();
    }

    /* access modifiers changed from: protected */
    public void checkForPairingMethodChange() {
        if (NetworkType.fromString(this.prefHelper.readString(getString(R.string.pref_pairing_kind), NetworkType.globalDefaultAsString())) != this.lastNetworkType) {
            this.result.prefPairingMethodChanged = true;
        }
    }

    /* access modifiers changed from: protected */
    public void finishOk() {
        checkForPairingMethodChange();
        Bundle bundle = new Bundle();
        bundle.putString(RESULT, this.result.serialize());
        Intent intent = new Intent();
        intent.putExtras(bundle);
        finishOk(intent);
    }
}
