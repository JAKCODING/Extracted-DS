package com.qualcomm.ftcdriverstation;

import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
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
   protected FtcDriverStationSettingsActivity.Result result = new FtcDriverStationSettingsActivity.Result();

   protected void checkForPairingMethodChange() {
      if (NetworkType.fromString(this.prefHelper.readString(this.getString(2131624445), NetworkType.globalDefaultAsString())) != this.lastNetworkType) {
         this.result.prefPairingMethodChanged = true;
      }

   }

   protected void finishOk() {
      this.checkForPairingMethodChange();
      Bundle var1 = new Bundle();
      var1.putString("RESULT", this.result.serialize());
      Intent var2 = new Intent();
      var2.putExtras(var1);
      this.finishOk(var2);
   }

   protected FrameLayout getBackBar() {
      return (FrameLayout)this.findViewById(2131230804);
   }

   protected void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.setContentView(2131427371);
      PreferencesHelper var3 = new PreferencesHelper("FtcDriverStationSettingsActivity", this);
      this.prefHelper = var3;
      this.lastNetworkType = NetworkType.fromString(var3.readString(this.getString(2131624445), NetworkType.globalDefaultAsString()));
      this.clientConnected = this.prefHelper.readBoolean(this.getString(2131624448), false);
      this.hasSpeaker = this.prefHelper.readBoolean(this.getString(2131624421), true);
      DeviceNameManagerFactory.getInstance().initializeDeviceNameIfNecessary();
      FtcDriverStationSettingsActivity.SettingsFragment var2 = new FtcDriverStationSettingsActivity.SettingsFragment();
      var1 = new Bundle();
      var1.putBoolean("CLIENT_CONNECTED", this.clientConnected);
      var1.putBoolean("HAS_SPEAKER", this.hasSpeaker);
      var2.setArguments(var1);
      var2.setProperties(this, this.prefHelper);
      this.getFragmentManager().beginTransaction().replace(2131230855, var2).commit();
   }

   public static class Result {
      public boolean prefAdvancedClicked = false;
      public boolean prefLogsClicked = false;
      public boolean prefPairClicked = false;
      public boolean prefPairingMethodChanged = false;

      public static FtcDriverStationSettingsActivity.Result deserialize(String var0) {
         return (FtcDriverStationSettingsActivity.Result)(new Gson()).fromJson(var0, FtcDriverStationSettingsActivity.Result.class);
      }

      public String serialize() {
         return (new Gson()).toJson((Object)this);
      }
   }

   public static class SettingsFragment extends PreferenceFragment {
      protected FtcDriverStationSettingsActivity activity;
      protected PreferencesHelper prefHelper;

      public void onCreate(Bundle var1) {
         super.onCreate(var1);
         boolean var2 = this.getArguments().getBoolean("CLIENT_CONNECTED");
         boolean var3 = this.getArguments().getBoolean("HAS_SPEAKER");
         this.addPreferencesFromResource(2131820545);
         if (!var2) {
            this.findPreference(this.getString(2131624410)).setEnabled(false);
            this.findPreference(this.getString(2131624398)).setEnabled(false);
            this.findPreference(this.getString(2131624450)).setEnabled(false);
         }

         if (!var3) {
            this.findPreference(this.getString(2131624450)).setEnabled(false);
         }

         this.findPreference(this.getString(2131624444)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference var1) {
               NetworkType var2 = NetworkType.fromString(SettingsFragment.this.prefHelper.readString(SettingsFragment.this.getString(2131624445), NetworkType.globalDefaultAsString()));
               StringBuilder var3 = new StringBuilder();
               var3.append("prefPair clicked ");
               var3.append(var2);
               RobotLog.vv("FtcDriverStationSettingsActivity", var3.toString());
               SettingsFragment.this.activity.result.prefPairClicked = true;
               if (var2 == NetworkType.WIFIDIRECT) {
                  SettingsFragment.this.startActivity(new Intent(SettingsFragment.this.activity, FtcPairNetworkConnectionActivity.class));
               } else {
                  SettingsFragment.this.startActivity(new Intent(SettingsFragment.this.activity, FtcWirelessApNetworkConnectionActivity.class));
               }

               return false;
            }
         });
         this.findPreference(this.getString(2131624424)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference var1) {
               RobotLog.vv("FtcDriverStationSettingsActivity", "prefAdvanced clicked");
               SettingsFragment.this.activity.result.prefAdvancedClicked = true;
               return false;
            }
         });
         this.findPreference(this.getString(2131624406)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference var1) {
               RobotLog.vv("FtcDriverStationSettingsActivity", "prefLogs clicked");
               SettingsFragment.this.activity.result.prefLogsClicked = true;
               return false;
            }
         });
         this.findPreference(this.getString(2131624407)).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference var1, Object var2) {
               if (var2 instanceof String && WifiDirectDeviceNameManager.validDeviceName((String)var2)) {
                  return true;
               } else {
                  Builder var3 = new Builder(SettingsFragment.this.getActivity());
                  var3.setTitle(SettingsFragment.this.getString(2131624479));
                  var3.setMessage(SettingsFragment.this.getString(2131624478));
                  var3.setPositiveButton(17039370, (OnClickListener)null);
                  var3.show();
                  return false;
               }
            }
         });
      }

      public void setProperties(FtcDriverStationSettingsActivity var1, PreferencesHelper var2) {
         this.activity = var1;
         this.prefHelper = var2;
      }
   }
}
