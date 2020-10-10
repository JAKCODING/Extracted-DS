package com.qualcomm.ftcdriverstation;

import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.qualcomm.robotcore.util.BatteryChecker;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.ui.UILocation;

public class FtcDriverStationActivityPortrait extends FtcDriverStationActivityBase {
    protected EditText matchNumField;

    public void subclassOnCreate() {
        setContentView(R.layout.activity_ftc_driver_station);
        this.matchNumField = (EditText) findViewById(R.id.matchNumTextField);
    }

    /* access modifiers changed from: protected */
    public void updateRcBatteryStatus(BatteryChecker.BatteryStatus batteryStatus) {
        TextView textView = this.rcBatteryTelemetry;
        setTextView(textView, Double.toString(batteryStatus.percent) + "%");
        setBatteryIcon(batteryStatus, this.rcBatteryIcon);
    }

    public View getPopupMenuAnchor() {
        return this.buttonMenu;
    }

    public void updateBatteryStatus(BatteryChecker.BatteryStatus batteryStatus) {
        TextView textView = this.dsBatteryInfo;
        setTextView(textView, Double.toString(batteryStatus.percent) + "%");
        setBatteryIcon(batteryStatus, this.dsBatteryIcon);
    }

    /* access modifiers changed from: protected */
    public void pingStatus(String str) {
        setTextView(this.textPingStatus, str);
    }

    /* access modifiers changed from: protected */
    public void enableMatchLoggingUI() {
        RobotLog.ii(FtcDriverStationActivityBase.TAG, "Show match logging UI");
        this.matchNumField.setVisibility(0);
        this.matchNumField.setEnabled(true);
        findViewById(R.id.matchNumLabel).setVisibility(0);
    }

    /* access modifiers changed from: protected */
    public void disableMatchLoggingUI() {
        RobotLog.ii(FtcDriverStationActivityBase.TAG, "Hide match logging UI");
        this.matchNumField.setVisibility(4);
        this.matchNumField.setEnabled(false);
        findViewById(R.id.matchNumLabel).setVisibility(4);
    }

    /* access modifiers changed from: protected */
    public int getMatchNumber() {
        return Integer.parseInt(this.matchNumField.getText().toString());
    }

    /* access modifiers changed from: protected */
    public void clearMatchNumber() {
        this.matchNumField.setText("");
    }

    /* access modifiers changed from: protected */
    public void doMatchNumFieldBehaviorInit() {
        this.matchNumField.setText("");
        this.matchNumField.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FtcDriverStationActivityPortrait.this.matchNumField.setText("");
            }
        });
        this.matchNumField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i != 6) {
                    return false;
                }
                int validateMatchEntry = FtcDriverStationActivityPortrait.this.validateMatchEntry(textView.getText().toString());
                if (validateMatchEntry == -1) {
                    AppUtil.getInstance().showToast(UILocation.ONLY_LOCAL, FtcDriverStationActivityPortrait.this.getString(R.string.invalidMatchNumber));
                    FtcDriverStationActivityPortrait.this.matchNumField.setText("");
                    return false;
                }
                FtcDriverStationActivityPortrait.this.sendMatchNumber(validateMatchEntry);
                return false;
            }
        });
        findViewById(R.id.buttonInit).requestFocus();
        if (!this.preferencesHelper.readBoolean(getString(R.string.pref_match_logging_on_off), false)) {
            disableMatchLoggingUI();
        }
    }
}
