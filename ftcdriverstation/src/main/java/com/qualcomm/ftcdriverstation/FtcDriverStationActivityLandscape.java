package com.qualcomm.ftcdriverstation;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qualcomm.ftcdriverstation.FtcDriverStationActivityBase;
import com.qualcomm.ftcdriverstation.ManualKeyInDialog;
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
    WiFiStatsView wiFiStatsView = WiFiStatsView.PING_CHAN;

    enum WiFiStatsView {
        PING_CHAN,
        DBM_LINK
    }

    public void subclassOnCreate() {
        setContentView(R.layout.activity_ds_land_main);
        this.headerColorLeft = (ImageView) findViewById(R.id.headerColorLeft);
        this.headerColorRight = (LinearLayout) findViewById(R.id.headerColorRight);
        this.configAndTimerRegion = findViewById(R.id.configAndTimerRegion);
        this.telemetryRegion = findViewById(R.id.telemetryRegion);
        this.practiceTimerManager = new PracticeTimerManager(this, (ImageView) findViewById(R.id.practiceTimerStartStopBtn), (TextView) findViewById(R.id.practiceTimerTimeView));
        this.matchLoggingContainer = findViewById(R.id.matchNumContainer);
        this.matchNumTxtView = (TextView) findViewById(R.id.matchNumTextField);
        this.networkSignalLevel = (ImageView) findViewById(R.id.networkSignalLevel);
        this.layoutPingChan = (LinearLayout) findViewById(R.id.layoutPingChan);
        this.textDbmLink = (TextView) findViewById(R.id.textDbmLink);
        this.network_ssid = (TextView) findViewById(R.id.network_ssid);
        this.dividerRcBatt12vBatt = findViewById(R.id.dividerRcBatt12vBatt);
    }

    public View getPopupMenuAnchor() {
        return this.wifiInfo;
    }

    /* access modifiers changed from: protected */
    public void doMatchNumFieldBehaviorInit() {
        this.matchLoggingContainer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                new ManualKeyInDialog(FtcDriverStationActivityLandscape.this.context, "Enter Match Number", new ManualKeyInDialog.Listener() {
                    public void onInput(String str) {
                        int validateMatchEntry = FtcDriverStationActivityLandscape.this.validateMatchEntry(str);
                        if (validateMatchEntry == -1) {
                            AppUtil.getInstance().showToast(UILocation.ONLY_LOCAL, FtcDriverStationActivityLandscape.this.getString(R.string.invalidMatchNumber));
                            FtcDriverStationActivityLandscape.this.clearMatchNumber();
                            return;
                        }
                        FtcDriverStationActivityLandscape.this.matchNumTxtView.setText(Integer.toString(validateMatchEntry));
                        FtcDriverStationActivityLandscape.this.sendMatchNumber(validateMatchEntry);
                    }
                }).show();
            }
        });
        if (!this.preferencesHelper.readBoolean(getString(R.string.pref_match_logging_on_off), false)) {
            disableMatchLoggingUI();
        }
    }

    /* access modifiers changed from: protected */
    public void enableMatchLoggingUI() {
        RobotLog.ii(FtcDriverStationActivityBase.TAG, "Show match logging UI");
        this.matchLoggingContainer.setVisibility(0);
    }

    /* access modifiers changed from: protected */
    public void disableMatchLoggingUI() {
        RobotLog.ii(FtcDriverStationActivityBase.TAG, "Hide match logging UI");
        this.matchLoggingContainer.setVisibility(8);
    }

    /* access modifiers changed from: protected */
    public int getMatchNumber() throws NumberFormatException {
        return Integer.parseInt(this.matchNumTxtView.getText().toString());
    }

    /* access modifiers changed from: protected */
    public void clearMatchNumber() {
        this.matchNumTxtView.setText("NONE");
    }

    /* access modifiers changed from: protected */
    public void dimAndDisableAllControls() {
        super.dimAndDisableAllControls();
        setOpacity(this.configAndTimerRegion, 0.3f);
        setOpacity(this.telemetryRegion, 0.3f);
    }

    /* access modifiers changed from: protected */
    public void uiRobotControllerIsDisconnected() {
        super.uiRobotControllerIsDisconnected();
        runOnUiThread(new Runnable() {
            public void run() {
                FtcDriverStationActivityLandscape.this.headerColorRight.setBackground(FtcDriverStationActivityLandscape.this.getResources().getDrawable(R.drawable.lds_header_shadow_disconnected));
                FtcDriverStationActivityLandscape.this.headerColorLeft.setBackgroundColor(FtcDriverStationActivityLandscape.this.getResources().getColor(R.color.lds_header_red_gradient_start));
                FtcDriverStationActivityLandscape.this.textWifiDirectStatus.setText("Disconnected");
            }
        });
    }

    /* access modifiers changed from: protected */
    public void uiRobotControllerIsConnected(FtcDriverStationActivityBase.ControlPanelBack controlPanelBack) {
        super.uiRobotControllerIsConnected(controlPanelBack);
        runOnUiThread(new Runnable() {
            public void run() {
                FtcDriverStationActivityLandscape.this.headerColorRight.setBackground(FtcDriverStationActivityLandscape.this.getResources().getDrawable(R.drawable.lds_header_shadow_connected));
                FtcDriverStationActivityLandscape.this.headerColorLeft.setBackgroundColor(FtcDriverStationActivityLandscape.this.getResources().getColor(R.color.lds_header_green_gradient_start));
                FtcDriverStationActivityLandscape.this.textWifiDirectStatus.setText("Robot Connected");
            }
        });
    }

    /* access modifiers changed from: protected */
    public void enableAndBrightenForConnected(FtcDriverStationActivityBase.ControlPanelBack controlPanelBack) {
        super.enableAndBrightenForConnected(controlPanelBack);
        setOpacity(this.configAndTimerRegion, 1.0f);
        setOpacity(this.telemetryRegion, 1.0f);
    }

    /* access modifiers changed from: protected */
    public void assumeClientConnect(FtcDriverStationActivityBase.ControlPanelBack controlPanelBack) {
        RobotLog.vv(FtcDriverStationActivityBase.TAG, "Assuming client connected");
        setClientConnected(true);
        uiRobotControllerIsConnected(controlPanelBack);
    }

    /* access modifiers changed from: protected */
    public void showWifiStatus(final boolean z, final String str) {
        runOnUiThread(new Runnable() {
            public void run() {
                FtcDriverStationActivityLandscape.this.textWifiDirectStatusShowingRC = z;
                FtcDriverStationActivityLandscape.this.textWifiDirectStatus.setText(str);
                if (str.equals(FtcDriverStationActivityLandscape.this.getString(R.string.wifiStatusDisconnected)) || str.equals(FtcDriverStationActivityLandscape.this.getString(R.string.actionlistenerfailure_busy)) || str.equals(FtcDriverStationActivityLandscape.this.getString(R.string.wifiStatusNotPaired))) {
                    FtcDriverStationActivityLandscape.this.headerColorRight.setBackground(FtcDriverStationActivityLandscape.this.getResources().getDrawable(R.drawable.lds_header_shadow_disconnected));
                    FtcDriverStationActivityLandscape.this.headerColorLeft.setBackgroundColor(FtcDriverStationActivityLandscape.this.getResources().getColor(R.color.lds_header_red_gradient_start));
                } else if (str.equals(FtcDriverStationActivityLandscape.this.getString(R.string.wifiStatusConnecting)) || str.equals(FtcDriverStationActivityLandscape.this.getString(R.string.wifiStatusSearching))) {
                    FtcDriverStationActivityLandscape.this.headerColorRight.setBackground(FtcDriverStationActivityLandscape.this.getResources().getDrawable(R.drawable.lds_header_shadow_connecting));
                    FtcDriverStationActivityLandscape.this.headerColorLeft.setBackgroundColor(FtcDriverStationActivityLandscape.this.getResources().getColor(R.color.lds_header_yellow_gradient_start));
                } else {
                    FtcDriverStationActivityLandscape.this.textWifiDirectStatus.setText("Robot Connected");
                    String str = str;
                    if (str.contains("DIRECT-") && str.contains("RC")) {
                        str = str.substring(10);
                    }
                    TextView textView = FtcDriverStationActivityLandscape.this.network_ssid;
                    textView.setText("Network: " + str);
                    FtcDriverStationActivityLandscape.this.headerColorRight.setBackground(FtcDriverStationActivityLandscape.this.getResources().getDrawable(R.drawable.lds_header_shadow_connected));
                    FtcDriverStationActivityLandscape.this.headerColorLeft.setBackgroundColor(FtcDriverStationActivityLandscape.this.getResources().getColor(R.color.lds_header_green_gradient_start));
                }
            }
        });
    }

    public void updateBatteryStatus(BatteryChecker.BatteryStatus batteryStatus) {
        TextView textView = this.dsBatteryInfo;
        setTextView(textView, "DS: " + Math.round(batteryStatus.percent) + "%");
        setBatteryIcon(batteryStatus, this.dsBatteryIcon);
    }

    /* access modifiers changed from: protected */
    public void updateRcBatteryStatus(BatteryChecker.BatteryStatus batteryStatus) {
        TextView textView = this.rcBatteryTelemetry;
        setTextView(textView, "RC: " + Math.round(batteryStatus.percent) + "%");
        setBatteryIcon(batteryStatus, this.rcBatteryIcon);
    }

    /* access modifiers changed from: protected */
    public void displayRcBattery(boolean z) {
        super.displayRcBattery(z);
        this.dividerRcBatt12vBatt.setVisibility(z ? 0 : 8);
    }

    /* access modifiers changed from: protected */
    public void pingStatus(String str) {
        TextView textView = this.textPingStatus;
        setTextView(textView, "Ping: " + str + " - ");
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        this.practiceTimerManager.reset();
        NetworkConnection networkConnection = this.networkConnectionHandler.getNetworkConnection();
        if (networkConnection.getNetworkType() == NetworkType.WIRELESSAP) {
            ((DriverStationAccessPointAssistant) networkConnection).unregisterNetworkHealthListener(this);
        }
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        NetworkConnection networkConnection = this.networkConnectionHandler.getNetworkConnection();
        if (networkConnection.getNetworkType() == NetworkType.WIRELESSAP) {
            ((DriverStationAccessPointAssistant) networkConnection).registerNetworkHealthListener(this);
        }
    }

    public void onNetworkHealthUpdate(final int i, final int i2) {
        int round = (int) Math.round(((double) ((float) (rssiToWiFiSignal(i, 5) + linkSpeedToWiFiSignal(i2, 5)))) / 2.0d);
        final int i3 = round != 0 ? round != 1 ? round != 2 ? round != 3 ? round != 4 ? round != 5 ? 0 : R.drawable.ic_signal_bars_5 : R.drawable.ic_signal_bars_4 : R.drawable.ic_signal_bars_3 : R.drawable.ic_signal_bars_2 : R.drawable.ic_signal_bars_1 : R.drawable.ic_signal_bars_0;
        runOnUiThread(new Runnable() {
            public void run() {
                FtcDriverStationActivityLandscape.this.networkSignalLevel.setBackgroundResource(i3);
                FtcDriverStationActivityLandscape.this.textDbmLink.setText(String.format("%ddBm Link %dMb", new Object[]{Integer.valueOf(i), Integer.valueOf(i2)}));
            }
        });
    }

    public int linkSpeedToWiFiSignal(int i, int i2) {
        float f = (float) i;
        if (f <= 6.0f) {
            return 0;
        }
        if (f >= 54.0f) {
            return i2;
        }
        return Math.round((f - 6.0f) / (48.0f / ((float) (i2 - 1))));
    }

    public int rssiToWiFiSignal(int i, int i2) {
        float f = (float) i;
        if (f <= -90.0f) {
            return 0;
        }
        if (f >= -55.0f) {
            return i2;
        }
        return Math.round((f - -90.0f) / (35.0f / ((float) (i2 - 1))));
    }

    public void toggleWifiStatsView(View view) {
        if (this.wiFiStatsView == WiFiStatsView.PING_CHAN) {
            this.wiFiStatsView = WiFiStatsView.DBM_LINK;
            this.textDbmLink.setVisibility(0);
            this.layoutPingChan.setVisibility(8);
        } else if (this.wiFiStatsView == WiFiStatsView.DBM_LINK) {
            this.wiFiStatsView = WiFiStatsView.PING_CHAN;
            this.layoutPingChan.setVisibility(0);
            this.textDbmLink.setVisibility(8);
        }
    }
}
