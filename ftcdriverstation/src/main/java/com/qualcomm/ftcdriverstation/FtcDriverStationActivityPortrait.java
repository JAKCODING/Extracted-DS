package com.qualcomm.ftcdriverstation;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.qualcomm.robotcore.util.BatteryChecker;
import com.qualcomm.robotcore.util.RobotLog;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.ui.UILocation;

public class FtcDriverStationActivityPortrait extends FtcDriverStationActivityBase {
   protected EditText matchNumField;

   protected void clearMatchNumber() {
      this.matchNumField.setText("");
   }

   protected void disableMatchLoggingUI() {
      RobotLog.ii("DriverStation", "Hide match logging UI");
      this.matchNumField.setVisibility(4);
      this.matchNumField.setEnabled(false);
      this.findViewById(2131230986).setVisibility(4);
   }

   protected void doMatchNumFieldBehaviorInit() {
      this.matchNumField.setText("");
      this.matchNumField.setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            FtcDriverStationActivityPortrait.this.matchNumField.setText("");
         }
      });
      this.matchNumField.setOnEditorActionListener(new OnEditorActionListener() {
         public boolean onEditorAction(TextView var1, int var2, KeyEvent var3) {
            if (var2 == 6) {
               var2 = FtcDriverStationActivityPortrait.this.validateMatchEntry(var1.getText().toString());
               if (var2 == -1) {
                  AppUtil.getInstance().showToast(UILocation.ONLY_LOCAL, FtcDriverStationActivityPortrait.this.getString(2131624244));
                  FtcDriverStationActivityPortrait.this.matchNumField.setText("");
               } else {
                  FtcDriverStationActivityPortrait.this.sendMatchNumber(var2);
               }
            }

            return false;
         }
      });
      this.findViewById(2131230818).requestFocus();
      if (!this.preferencesHelper.readBoolean(this.getString(2131624434), false)) {
         this.disableMatchLoggingUI();
      }

   }

   protected void enableMatchLoggingUI() {
      RobotLog.ii("DriverStation", "Show match logging UI");
      this.matchNumField.setVisibility(0);
      this.matchNumField.setEnabled(true);
      this.findViewById(2131230986).setVisibility(0);
   }

   protected int getMatchNumber() {
      return Integer.parseInt(this.matchNumField.getText().toString());
   }

   public View getPopupMenuAnchor() {
      return this.buttonMenu;
   }

   protected void pingStatus(String var1) {
      this.setTextView(this.textPingStatus, var1);
   }

   public void subclassOnCreate() {
      this.setContentView(2131427364);
      this.matchNumField = (EditText)this.findViewById(2131230987);
   }

   public void updateBatteryStatus(BatteryChecker.BatteryStatus var1) {
      TextView var2 = this.dsBatteryInfo;
      StringBuilder var3 = new StringBuilder();
      var3.append(Double.toString(var1.percent));
      var3.append("%");
      this.setTextView(var2, var3.toString());
      this.setBatteryIcon(var1, this.dsBatteryIcon);
   }

   protected void updateRcBatteryStatus(BatteryChecker.BatteryStatus var1) {
      TextView var2 = this.rcBatteryTelemetry;
      StringBuilder var3 = new StringBuilder();
      var3.append(Double.toString(var1.percent));
      var3.append("%");
      this.setTextView(var2, var3.toString());
      this.setBatteryIcon(var1, this.rcBatteryIcon);
   }
}
