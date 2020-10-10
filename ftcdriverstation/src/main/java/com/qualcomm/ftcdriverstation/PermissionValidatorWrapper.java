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
    protected List<String> robotControllerPermissions = new ArrayList<String>() {
        {
            add("android.permission.WRITE_EXTERNAL_STORAGE");
            add("android.permission.READ_EXTERNAL_STORAGE");
            add("android.permission.ACCESS_COARSE_LOCATION");
        }
    };
    private SharedPreferences sharedPreferences;

    public String mapPermissionToExplanation(String str) {
        if (str.equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
            return Misc.formatForUser((int) R.string.permDsWriteExternalStorageExplain);
        }
        if (str.equals("android.permission.READ_EXTERNAL_STORAGE")) {
            return Misc.formatForUser((int) R.string.permDsReadExternalStorageExplain);
        }
        if (str.equals("android.permission.ACCESS_COARSE_LOCATION")) {
            return Misc.formatForUser((int) R.string.permAccessLocationExplain);
        }
        return Misc.formatForUser((int) R.string.permGenericExplain);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.permissions = this.robotControllerPermissions;
    }

    /* access modifiers changed from: protected */
    public Class onStartApplication() {
        FtcDriverStationActivityBase.setPermissionsValidated();
        String string = getResources().getString(R.string.key_ds_layout);
        String string2 = getResources().getString(R.string.ds_ui_portrait);
        if (this.sharedPreferences.getString(string, string2).equals(string2)) {
            return FtcDriverStationActivityPortrait.class;
        }
        return FtcDriverStationActivityLandscape.class;
    }
}
