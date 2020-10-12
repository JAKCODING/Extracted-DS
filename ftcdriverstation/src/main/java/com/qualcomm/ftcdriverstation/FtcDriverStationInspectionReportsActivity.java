package com.qualcomm.ftcdriverstation;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.widget.FrameLayout;
import org.firstinspires.ftc.robotcore.internal.network.DeviceNameManagerFactory;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;
import org.firstinspires.ftc.robotcore.internal.ui.BaseActivity;

public class FtcDriverStationInspectionReportsActivity extends BaseActivity {
   protected static final String CLIENT_CONNECTED = "CLIENT_CONNECTED";
   public static final String TAG = "FtcDriverStationInspectionReportsActivity";

   protected FrameLayout getBackBar() {
      return (FrameLayout)this.findViewById(2131230804);
   }

   public String getTag() {
      return "FtcDriverStationInspectionReportsActivity";
   }

   protected void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.setContentView(2131427371);
      DeviceNameManagerFactory.getInstance().initializeDeviceNameIfNecessary();
      FtcDriverStationInspectionReportsActivity.SettingsFragment var2 = new FtcDriverStationInspectionReportsActivity.SettingsFragment();
      var1 = new Bundle();
      var1.putBoolean("CLIENT_CONNECTED", (new PreferencesHelper("FtcDriverStationInspectionReportsActivity", this)).readBoolean(this.getString(2131624448), false));
      var2.setArguments(var1);
      this.getFragmentManager().beginTransaction().replace(2131230855, var2).commit();
   }

   public static class SettingsFragment extends PreferenceFragment {
      protected boolean clientConnected = false;

      public void onCreate(Bundle var1) {
         super.onCreate(var1);
         this.clientConnected = this.getArguments().getBoolean("CLIENT_CONNECTED");
         this.addPreferencesFromResource(2131820550);
         if (!this.clientConnected) {
            this.findPreference(this.getString(2131624426)).setEnabled(false);
         }

      }
   }
}
