package com.qualcomm.ftcdriverstation;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import java.util.ArrayList;
import java.util.List;
import org.firstinspires.ftc.robotcore.internal.system.Misc;
import org.firstinspires.ftc.robotcore.internal.system.PermissionValidatorActivity;

public class PermissionValidatorWrapper extends PermissionValidatorActivity {
   private final String TAG = "PermissionValidatorWrapper";
   protected List robotControllerPermissions = new ArrayList() {
      {
         this.add("android.permission.WRITE_EXTERNAL_STORAGE");
         this.add("android.permission.READ_EXTERNAL_STORAGE");
         this.add("android.permission.ACCESS_COARSE_LOCATION");
      }
   };
   private SharedPreferences sharedPreferences;

   public String mapPermissionToExplanation(String var1) {
      if (var1.equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
         return Misc.formatForUser(2131624382);
      } else if (var1.equals("android.permission.READ_EXTERNAL_STORAGE")) {
         return Misc.formatForUser(2131624381);
      } else {
         return var1.equals("android.permission.ACCESS_COARSE_LOCATION") ? Misc.formatForUser(2131624380) : Misc.formatForUser(2131624383);
      }
   }

   protected void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
      this.permissions = this.robotControllerPermissions;
   }

   protected Class onStartApplication() {
      FtcDriverStationActivityBase.setPermissionsValidated();
      String var1 = this.getResources().getString(2131624245);
      String var2 = this.getResources().getString(2131624186);
      return this.sharedPreferences.getString(var1, var2).equals(var2) ? FtcDriverStationActivityPortrait.class : FtcDriverStationActivityLandscape.class;
   }
}
