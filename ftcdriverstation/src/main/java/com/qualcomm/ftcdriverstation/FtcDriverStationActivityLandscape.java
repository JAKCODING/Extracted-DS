package com.qualcomm.ftcdriverstation;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.qualcomm.robotcore.util.BatteryChecker;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.wifi.DriverStationAccessPointAssistant;
import com.qualcomm.robotcore.wifi.NetworkConnection;
import com.qualcomm.robotcore.wifi.NetworkType;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.ui.UILocation;

public class FtcDriverStationActivityLandscape extends FtcDriverStationActivityBase implements DriverStationAccessPointAssistant.ConnectedNetworkHealthListener {
   View configAndTimerRegion;
   View dividerRcBatt12vBatt;
   ImageView headerColorLeft;
   LinearLayout headerColorRight;
   LinearLayout layoutPingChan;
   View matchLoggingContainer;
   TextView matchNumTxtView;
   ImageView networkSignalLevel;
   TextView network_ssid;
   PracticeTimerManager practiceTimerManager;
   View telemetryRegion;
   TextView textDbmLink;
   FtcDriverStationActivityLandscape.WiFiStatsView wiFiStatsView;

   public FtcDriverStationActivityLandscape() {
      this.wiFiStatsView = FtcDriverStationActivityLandscape.WiFiStatsView.PING_CHAN;
   }

   protected void assumeClientConnect(FtcDriverStationActivityBase.ControlPanelBack var1) {
      RobotLog.vv("DriverStation", "Assuming client connected");
      this.setClientConnected(true);
      this.uiRobotControllerIsConnected(var1);
   }

   protected void clearMatchNumber() {
      this.matchNumTxtView.setText("NONE");
   }

   protected void dimAndDisableAllControls() {
      super.dimAndDisableAllControls();
      this.setOpacity(this.configAndTimerRegion, 0.3F);
      this.setOpacity(this.telemetryRegion, 0.3F);
   }

   protected void disableMatchLoggingUI() {
      RobotLog.ii("DriverStation", "Hide match logging UI");
      this.matchLoggingContainer.setVisibility(8);
   }

   protected void displayRcBattery(boolean var1) {
      super.displayRcBattery(var1);
      View var2 = this.dividerRcBatt12vBatt;
      byte var3;
      if (var1) {
         var3 = 0;
      } else {
         var3 = 8;
      }

      var2.setVisibility(var3);
   }

   protected void doMatchNumFieldBehaviorInit() {
      this.matchLoggingContainer.setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            (new ManualKeyInDialog(FtcDriverStationActivityLandscape.this.context, "Enter Match Number", new ManualKeyInDialog.Listener() {
               public void onInput(String var1) {
                  int var2 = FtcDriverStationActivityLandscape.this.validateMatchEntry(var1);
                  if (var2 == -1) {
                     AppUtil.getInstance().showToast(UILocation.ONLY_LOCAL, FtcDriverStationActivityLandscape.this.getString(2131624244));
                     FtcDriverStationActivityLandscape.this.clearMatchNumber();
                  } else {
                     FtcDriverStationActivityLandscape.this.matchNumTxtView.setText(Integer.toString(var2));
                     FtcDriverStationActivityLandscape.this.sendMatchNumber(var2);
                  }

               }
            })).show();
         }
      });
      if (!this.preferencesHelper.readBoolean(this.getString(2131624434), false)) {
         this.disableMatchLoggingUI();
      }

   }

   protected void enableAndBrightenForConnected(FtcDriverStationActivityBase.ControlPanelBack var1) {
      super.enableAndBrightenForConnected(var1);
      this.setOpacity(this.configAndTimerRegion, 1.0F);
      this.setOpacity(this.telemetryRegion, 1.0F);
   }

   protected void enableMatchLoggingUI() {
      RobotLog.ii("DriverStation", "Show match logging UI");
      this.matchLoggingContainer.setVisibility(0);
   }

   protected int getMatchNumber() throws NumberFormatException {
      return Integer.parseInt(this.matchNumTxtView.getText().toString());
   }

   public View getPopupMenuAnchor() {
      return this.wifiInfo;
   }

   public int linkSpeedToWiFiSignal(int var1, int var2) {
      float var3 = (float)var1;
      if (var3 <= 6.0F) {
         return 0;
      } else {
         return var3 >= 54.0F ? var2 : Math.round((var3 - 6.0F) / (48.0F / (float)(var2 - 1)));
      }
   }

   public void onNetworkHealthUpdate(final int var1, final int var2) {
      final int var3 = (int)Math.round((double)((float)(this.rssiToWiFiSignal(var1, 5) + this.linkSpeedToWiFiSignal(var2, 5))) / 2.0D);
      if (var3 != 0) {
         if (var3 != 1) {
            if (var3 != 2) {
               if (var3 != 3) {
                  if (var3 != 4) {
                     if (var3 != 5) {
                        var3 = 0;
                     } else {
                        var3 = 2131165323;
                     }
                  } else {
                     var3 = 2131165322;
                  }
               } else {
                  var3 = 2131165321;
               }
            } else {
               var3 = 2131165320;
            }
         } else {
            var3 = 2131165319;
         }
      } else {
         var3 = 2131165318;
      }

      this.runOnUiThread(new Runnable() {
         public void run() {
            FtcDriverStationActivityLandscape.this.networkSignalLevel.setBackgroundResource(var3);
            FtcDriverStationActivityLandscape.this.textDbmLink.setText(String.format("%ddBm Link %dMb", var1, var2));
         }
      });
   }

   protected void onPause() {
      super.onPause();
      this.practiceTimerManager.reset();
      NetworkConnection var1 = this.networkConnectionHandler.getNetworkConnection();
      if (var1.getNetworkType() == NetworkType.WIRELESSAP) {
         ((DriverStationAccessPointAssistant)var1).unregisterNetworkHealthListener(this);
      }

   }

   protected void onResume() {
      super.onResume();
      NetworkConnection var1 = this.networkConnectionHandler.getNetworkConnection();
      if (var1.getNetworkType() == NetworkType.WIRELESSAP) {
         ((DriverStationAccessPointAssistant)var1).registerNetworkHealthListener(this);
      }

   }

   protected void pingStatus(String var1) {
      TextView var2 = this.textPingStatus;
      StringBuilder var3 = new StringBuilder();
      var3.append("Ping: ");
      var3.append(var1);
      var3.append(" - ");
      this.setTextView(var2, var3.toString());
   }

   public int rssiToWiFiSignal(int var1, int var2) {
      float var3 = (float)var1;
      if (var3 <= -90.0F) {
         return 0;
      } else {
         return var3 >= -55.0F ? var2 : Math.round((var3 + 90.0F) / (35.0F / (float)(var2 - 1)));
      }
   }

   protected void showWifiStatus(final boolean var1, final String var2) {
      this.runOnUiThread(new Runnable() {
         public void run() {
            FtcDriverStationActivityLandscape.this.textWifiDirectStatusShowingRC = var1;
            FtcDriverStationActivityLandscape.this.textWifiDirectStatus.setText(var2);
            if (!var2.equals(FtcDriverStationActivityLandscape.this.getString(2131624680)) && !var2.equals(FtcDriverStationActivityLandscape.this.getString(2131623995)) && !var2.equals(FtcDriverStationActivityLandscape.this.getString(2131624683))) {
               if (!var2.equals(FtcDriverStationActivityLandscape.this.getString(2131624679)) && !var2.equals(FtcDriverStationActivityLandscape.this.getString(2131624684))) {
                  FtcDriverStationActivityLandscape.this.textWifiDirectStatus.setText("Robot Connected");
                  String var1x = var2;
                  String var2x = var1x;
                  if (var1x.contains("DIRECT-")) {
                     var2x = var1x;
                     if (var2.contains("RC")) {
                        var2x = var2.substring(10);
                     }
                  }

                  TextView var4 = FtcDriverStationActivityLandscape.this.network_ssid;
                  StringBuilder var3 = new StringBuilder();
                  var3.append("Network: ");
                  var3.append(var2x);
                  var4.setText(var3.toString());
                  FtcDriverStationActivityLandscape.this.headerColorRight.setBackground(FtcDriverStationActivityLandscape.this.getResources().getDrawable(2131165343));
                  FtcDriverStationActivityLandscape.this.headerColorLeft.setBackgroundColor(FtcDriverStationActivityLandscape.this.getResources().getColor(2131034196));
               } else {
                  FtcDriverStationActivityLandscape.this.headerColorRight.setBackground(FtcDriverStationActivityLandscape.this.getResources().getDrawable(2131165344));
                  FtcDriverStationActivityLandscape.this.headerColorLeft.setBackgroundColor(FtcDriverStationActivityLandscape.this.getResources().getColor(2131034200));
               }
            } else {
               FtcDriverStationActivityLandscape.this.headerColorRight.setBackground(FtcDriverStationActivityLandscape.this.getResources().getDrawable(2131165345));
               FtcDriverStationActivityLandscape.this.headerColorLeft.setBackgroundColor(FtcDriverStationActivityLandscape.this.getResources().getColor(2131034198));
            }

         }
      });
   }

   public void subclassOnCreate() {
      this.setContentView(2131427359);
      this.headerColorLeft = (ImageView)this.findViewById(2131230918);
      this.headerColorRight = (LinearLayout)this.findViewById(2131230919);
      this.configAndTimerRegion = this.findViewById(2131230850);
      this.telemetryRegion = this.findViewById(2131231110);
      this.practiceTimerManager = new PracticeTimerManager(this, (ImageView)this.findViewById(2131231031), (TextView)this.findViewById(2131231032));
      this.matchLoggingContainer = this.findViewById(2131230984);
      this.matchNumTxtView = (TextView)this.findViewById(2131230987);
      this.networkSignalLevel = (ImageView)this.findViewById(2131230997);
      this.layoutPingChan = (LinearLayout)this.findViewById(2131230948);
      this.textDbmLink = (TextView)this.findViewById(2131231117);
      this.network_ssid = (TextView)this.findViewById(2131230999);
      this.dividerRcBatt12vBatt = this.findViewById(2131230882);
   }

   public void toggleWifiStatsView(View var1) {
      if (this.wiFiStatsView == FtcDriverStationActivityLandscape.WiFiStatsView.PING_CHAN) {
         this.wiFiStatsView = FtcDriverStationActivityLandscape.WiFiStatsView.DBM_LINK;
         this.textDbmLink.setVisibility(0);
         this.layoutPingChan.setVisibility(8);
      } else if (this.wiFiStatsView == FtcDriverStationActivityLandscape.WiFiStatsView.DBM_LINK) {
         this.wiFiStatsView = FtcDriverStationActivityLandscape.WiFiStatsView.PING_CHAN;
         this.layoutPingChan.setVisibility(0);
         this.textDbmLink.setVisibility(8);
      }

   }

   protected void uiRobotControllerIsConnected(FtcDriverStationActivityBase.ControlPanelBack var1) {
      super.uiRobotControllerIsConnected(var1);
      this.runOnUiThread(new Runnable() {
         public void run() {
            FtcDriverStationActivityLandscape.this.headerColorRight.setBackground(FtcDriverStationActivityLandscape.this.getResources().getDrawable(2131165343));
            FtcDriverStationActivityLandscape.this.headerColorLeft.setBackgroundColor(FtcDriverStationActivityLandscape.this.getResources().getColor(2131034196));
            FtcDriverStationActivityLandscape.this.textWifiDirectStatus.setText("Robot Connected");
         }
      });
   }

   protected void uiRobotControllerIsDisconnected() {
      super.uiRobotControllerIsDisconnected();
      this.runOnUiThread(new Runnable() {
         public void run() {
            FtcDriverStationActivityLandscape.this.headerColorRight.setBackground(FtcDriverStationActivityLandscape.this.getResources().getDrawable(2131165345));
            FtcDriverStationActivityLandscape.this.headerColorLeft.setBackgroundColor(FtcDriverStationActivityLandscape.this.getResources().getColor(2131034198));
            FtcDriverStationActivityLandscape.this.textWifiDirectStatus.setText("Disconnected");
         }
      });
   }

   public void updateBatteryStatus(BatteryChecker.BatteryStatus var1) {
      TextView var2 = this.dsBatteryInfo;
      StringBuilder var3 = new StringBuilder();
      var3.append("DS: ");
      var3.append(Math.round(var1.percent));
      var3.append("%");
      this.setTextView(var2, var3.toString());
      this.setBatteryIcon(var1, this.dsBatteryIcon);
   }

   protected void updateRcBatteryStatus(BatteryChecker.BatteryStatus var1) {
      TextView var2 = this.rcBatteryTelemetry;
      StringBuilder var3 = new StringBuilder();
      var3.append("RC: ");
      var3.append(Math.round(var1.percent));
      var3.append("%");
      this.setTextView(var2, var3.toString());
      this.setBatteryIcon(var1, this.rcBatteryIcon);
   }

   static enum WiFiStatsView {
      DBM_LINK,
      PING_CHAN;

      static {
         FtcDriverStationActivityLandscape.WiFiStatsView var0 = new FtcDriverStationActivityLandscape.WiFiStatsView("DBM_LINK", 1);
         DBM_LINK = var0;
      }
   }
}
