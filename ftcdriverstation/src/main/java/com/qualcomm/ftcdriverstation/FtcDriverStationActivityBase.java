package com.qualcomm.ftcdriverstation;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.input.InputManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qualcomm.ftccommon.ClassManagerFactory;
import com.qualcomm.ftccommon.CommandList;
import com.qualcomm.ftccommon.ConfigWifiDirectActivity;
import com.qualcomm.ftccommon.FtcAboutActivity;
import com.qualcomm.ftccommon.FtcEventLoopHandler;
import com.qualcomm.ftccommon.LaunchActivityConstantsList;
import com.qualcomm.ftccommon.SoundPlayer;
import com.qualcomm.ftccommon.configuration.EditParameters;
import com.qualcomm.ftccommon.configuration.FtcLoadFileActivity;
import com.qualcomm.ftccommon.configuration.RobotConfigFile;
import com.qualcomm.ftccommon.configuration.RobotConfigFileManager;
import com.qualcomm.ftcdriverstation.FtcDriverStationSettingsActivity;
import com.qualcomm.ftcdriverstation.GamepadIndicator;
import com.qualcomm.ftcdriverstation.OpModeSelectionDialogFragment;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.exception.RobotProtocolException;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationTypeManager;
import com.qualcomm.robotcore.hardware.configuration.Utility;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.robocol.Heartbeat;
import com.qualcomm.robotcore.robocol.PeerDiscovery;
import com.qualcomm.robotcore.robocol.RobocolDatagram;
import com.qualcomm.robotcore.robocol.RobocolParsableBase;
import com.qualcomm.robotcore.robocol.TelemetryMessage;
import com.qualcomm.robotcore.robot.RobotState;
import com.qualcomm.robotcore.util.BatteryChecker;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.ImmersiveMode;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.RollingAverage;
import com.qualcomm.robotcore.wifi.NetworkConnection;
import com.qualcomm.robotcore.wifi.NetworkType;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.firstinspires.ftc.driverstation.internal.StopWatchDrawable;
import org.firstinspires.ftc.ftccommon.external.SoundPlayingRobotMonitor;
import org.firstinspires.ftc.ftccommon.internal.ProgramAndManageActivity;
import org.firstinspires.ftc.robotcore.external.Predicate;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.android.AndroidTextToSpeech;
import org.firstinspires.ftc.robotcore.external.stream.CameraStreamClient;
import org.firstinspires.ftc.robotcore.internal.network.CallbackResult;
import org.firstinspires.ftc.robotcore.internal.network.DeviceNameListener;
import org.firstinspires.ftc.robotcore.internal.network.DeviceNameManagerFactory;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.network.PeerStatusCallback;
import org.firstinspires.ftc.robotcore.internal.network.PreferenceRemoterDS;
import org.firstinspires.ftc.robotcore.internal.network.RecvLoopRunnable;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.internal.network.StartResult;
import org.firstinspires.ftc.robotcore.internal.network.WifiMuteEvent;
import org.firstinspires.ftc.robotcore.internal.network.WifiMuteStateMachine;
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.Assert;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;
import org.firstinspires.ftc.robotcore.internal.ui.FilledPolygonDrawable;
import org.firstinspires.ftc.robotcore.internal.ui.GamepadUser;
import org.firstinspires.ftc.robotcore.internal.ui.ProgressParameters;
import org.firstinspires.ftc.robotcore.internal.ui.ThemedActivity;
import org.firstinspires.ftc.robotcore.internal.ui.UILocation;

public abstract class FtcDriverStationActivityBase extends ThemedActivity implements NetworkConnection.NetworkConnectionCallback, RecvLoopRunnable.RecvLoopCallback, SharedPreferences.OnSharedPreferenceChangeListener, OpModeSelectionDialogFragment.OpModeSelectionDialogListener, BatteryChecker.BatteryWatcher, PeerStatusCallback, WifiMuteStateMachine.Callback {
    protected static final float FULLY_OPAQUE = 1.0f;
    protected static final int MATCH_NUMBER_LOWER_BOUND = 0;
    protected static final int MATCH_NUMBER_UPPER_BOUND = 1000;
    protected static final float PARTLY_OPAQUE = 0.3f;
    public static final String TAG = "DriverStation";
    protected static final boolean debugBattery = false;
    protected static boolean permissionsValidated = false;
    protected double V12BatteryMin;
    protected String V12BatteryMinString;
    protected TextView activeConfigText;
    private final AndroidTextToSpeech androidTextToSpeech;
    protected AppUtil appUtil;
    protected BatteryChecker batteryChecker;
    protected View batteryInfo;
    protected Button buttonAutonomous;
    protected View buttonInit;
    protected View buttonInitStop;
    protected ImageButton buttonMenu;
    protected View buttonStart;
    protected ImageButton buttonStop;
    protected Button buttonTeleOp;
    protected ImageView cameraStreamImageView;
    protected LinearLayout cameraStreamLayout;
    protected boolean cameraStreamOpen;
    protected View chooseOpModePrompt;
    protected boolean clientConnected;
    protected String connectionOwner;
    protected String connectionOwnerPassword;
    protected Context context;
    protected View controlPanelBack;
    protected TextView currentOpModeName;
    protected boolean debugLogging;
    protected final OpModeMeta defaultOpMode;
    protected DeviceNameManagerCallback deviceNameManagerCallback;
    protected StartResult deviceNameManagerStartResult;
    protected boolean disconnectFromPeerOnActivityStop;
    protected ImageView dsBatteryIcon;
    protected TextView dsBatteryInfo;
    protected Map<GamepadUser, GamepadIndicator> gamepadIndicators = new HashMap();
    protected GamepadManager gamepadManager;
    protected Heartbeat heartbeatRecv = new Heartbeat();
    protected ImmersiveMode immersion;
    protected ElapsedTime lastUiUpdate;
    private InputManager mInputManager;
    protected NetworkConnectionHandler networkConnectionHandler;
    protected OpModeCountDownTimer opModeCountDown;
    protected boolean opModeUseTimer;
    protected List<OpModeMeta> opModes;
    protected RollingAverage pingAverage;
    protected StartResult prefRemoterStartResult;
    protected SharedPreferences preferences;
    protected PreferencesHelper preferencesHelper;
    protected boolean processUserActivity;
    protected OpModeMeta queuedOpMode;
    protected OpModeMeta queuedOpModeWhenMuted;
    protected View rcBatteryContainer;
    protected ImageView rcBatteryIcon;
    protected TextView rcBatteryTelemetry;
    protected boolean rcHasIndependentBattery;
    protected TextView robotBatteryMinimum;
    protected TextView robotBatteryTelemetry;
    protected RobotConfigFileManager robotConfigFileManager;
    protected RobotState robotState;
    protected TextView systemTelemetry;
    protected int systemTelemetryOriginalColor;
    protected Telemetry.DisplayFormat telemetryMode;
    protected TextView textBytesPerSecond;
    protected TextView textDeviceName;
    protected TextView textDsUiStateIndicator;
    protected TextView textPingStatus;
    protected TextView textTelemetry;
    protected TextView textWifiChannel;
    protected TextView textWifiDirectStatus;
    protected boolean textWifiDirectStatusShowingRC;
    protected View timerAndTimerSwitch;
    protected UIState uiState;
    protected Thread uiThread;
    protected Utility utility;
    protected View wifiInfo;
    protected WifiMuteStateMachine wifiMuteStateMachine;

    protected enum ControlPanelBack {
        NO_CHANGE,
        DIM,
        BRIGHT
    }

    /* access modifiers changed from: protected */
    public abstract void clearMatchNumber();

    /* access modifiers changed from: protected */
    public abstract void disableMatchLoggingUI();

    /* access modifiers changed from: protected */
    public abstract void doMatchNumFieldBehaviorInit();

    /* access modifiers changed from: protected */
    public abstract void enableMatchLoggingUI();

    /* access modifiers changed from: protected */
    public abstract int getMatchNumber() throws NumberFormatException;

    public abstract View getPopupMenuAnchor();

    public String getTag() {
        return TAG;
    }

    /* access modifiers changed from: protected */
    public abstract void pingStatus(String str);

    public abstract void subclassOnCreate();

    /* access modifiers changed from: protected */
    public abstract void updateRcBatteryStatus(BatteryChecker.BatteryStatus batteryStatus);

    public FtcDriverStationActivityBase() {
        OpModeMeta opModeMeta = new OpModeMeta("$Stop$Robot$");
        this.defaultOpMode = opModeMeta;
        this.queuedOpMode = opModeMeta;
        this.queuedOpModeWhenMuted = opModeMeta;
        this.opModes = new LinkedList();
        this.opModeUseTimer = false;
        this.pingAverage = new RollingAverage(10);
        this.lastUiUpdate = new ElapsedTime();
        this.uiState = UIState.UNKNOWN;
        this.telemetryMode = Telemetry.DisplayFormat.CLASSIC;
        this.debugLogging = false;
        this.networkConnectionHandler = NetworkConnectionHandler.getInstance();
        this.appUtil = AppUtil.getInstance();
        this.deviceNameManagerStartResult = new StartResult();
        this.prefRemoterStartResult = new StartResult();
        this.deviceNameManagerCallback = new DeviceNameManagerCallback();
        this.processUserActivity = false;
        this.disconnectFromPeerOnActivityStop = true;
        this.androidTextToSpeech = new AndroidTextToSpeech();
    }

    /* access modifiers changed from: protected */
    public void setBatteryIcon(final BatteryChecker.BatteryStatus batteryStatus, final ImageView imageView) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (batteryStatus.percent <= 15.0d) {
                    imageView.setImageResource(batteryStatus.isCharging ? R.drawable.icon_battery0_charging : R.drawable.icon_battery0);
                    imageView.setColorFilter(FtcDriverStationActivityBase.this.getResources().getColor(R.color.phoneBatteryCritical), PorterDuff.Mode.MULTIPLY);
                } else if (batteryStatus.percent > 15.0d && batteryStatus.percent <= 45.0d) {
                    imageView.setImageResource(batteryStatus.isCharging ? R.drawable.icon_battery25_charging : R.drawable.icon_battery25);
                    if (batteryStatus.percent <= 30.0d) {
                        imageView.setColorFilter(FtcDriverStationActivityBase.this.getResources().getColor(R.color.phoneBatteryLow), PorterDuff.Mode.MULTIPLY);
                    } else {
                        imageView.setColorFilter(FtcDriverStationActivityBase.this.getResources().getColor(R.color.text_white), PorterDuff.Mode.MULTIPLY);
                    }
                } else if (batteryStatus.percent > 45.0d && batteryStatus.percent <= 65.0d) {
                    imageView.setImageResource(batteryStatus.isCharging ? R.drawable.icon_battery50_charging : R.drawable.icon_battery50);
                    imageView.setColorFilter(FtcDriverStationActivityBase.this.getResources().getColor(R.color.text_white), PorterDuff.Mode.MULTIPLY);
                } else if (batteryStatus.percent <= 65.0d || batteryStatus.percent > 85.0d) {
                    imageView.setImageResource(batteryStatus.isCharging ? R.drawable.icon_battery100_charging : R.drawable.icon_battery100);
                    imageView.setColorFilter(FtcDriverStationActivityBase.this.getResources().getColor(R.color.text_white), PorterDuff.Mode.MULTIPLY);
                } else {
                    imageView.setImageResource(batteryStatus.isCharging ? R.drawable.icon_battery75_charging : R.drawable.icon_battery75);
                    imageView.setColorFilter(FtcDriverStationActivityBase.this.getResources().getColor(R.color.text_white), PorterDuff.Mode.MULTIPLY);
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    public void displayRcBattery(boolean z) {
        this.rcBatteryContainer.setVisibility(z ? 0 : 8);
    }

    private void checkRcIndependentBattery(SharedPreferences sharedPreferences) {
        this.rcHasIndependentBattery = sharedPreferences.getBoolean(getString(R.string.pref_has_independent_phone_battery_rc), true);
    }

    private void updateRcBatteryIndependence(SharedPreferences sharedPreferences, boolean z) {
        checkRcIndependentBattery(sharedPreferences);
        RobotLog.vv(TAG, "updateRcBatteryIndependence(%s)", Boolean.valueOf(this.rcHasIndependentBattery));
        if (z) {
            displayRcBattery(this.rcHasIndependentBattery);
        }
    }

    private void updateRcBatteryIndependence(SharedPreferences sharedPreferences) {
        updateRcBatteryIndependence(sharedPreferences, true);
    }

    public void onWifiOn() {
        this.queuedOpMode = this.queuedOpModeWhenMuted;
        this.processUserActivity = true;
        RobotLog.ii(TAG, "Wifi On: " + this.queuedOpMode.name);
    }

    public void onWifiOff() {
        this.queuedOpModeWhenMuted = this.queuedOpMode;
        RobotLog.ii(TAG, "Wifi Off: " + this.queuedOpMode.name);
    }

    public void onPendingOn() {
        this.processUserActivity = false;
        RobotLog.ii(TAG, "Pending Wifi Off: " + this.queuedOpMode.name);
    }

    public void onPendingCancel() {
        this.processUserActivity = true;
        RobotLog.ii(TAG, "Pending Wifi Cancel: " + this.queuedOpMode.name);
    }

    private class OpModeCountDownTimer {
        public static final long MS_COUNTDOWN_INTERVAL = 30000;
        public static final long MS_PER_S = 1000;
        public static final long MS_TICK = 1000;
        public static final long TICK_INTERVAL = 1;
        /* access modifiers changed from: private */
        public CountDownTimer countDownTimer = null;
        private boolean enabled = false;
        /* access modifiers changed from: private */
        public long msRemaining = MS_COUNTDOWN_INTERVAL;
        private View timerStopWatch;
        private View timerSwitchOff;
        private View timerSwitchOn;
        private TextView timerText;

        public OpModeCountDownTimer() {
            this.timerStopWatch = FtcDriverStationActivityBase.this.findViewById(R.id.timerStopWatch);
            this.timerText = (TextView) FtcDriverStationActivityBase.this.findViewById(R.id.timerText);
            this.timerSwitchOn = FtcDriverStationActivityBase.this.findViewById(R.id.timerSwitchOn);
            this.timerSwitchOff = FtcDriverStationActivityBase.this.findViewById(R.id.timerSwitchOff);
        }

        private void displaySecondsRemaining(long j) {
            if (this.enabled) {
                FtcDriverStationActivityBase.this.setTextView(this.timerText, String.valueOf(j));
            }
        }

        public void enable() {
            if (!this.enabled) {
                FtcDriverStationActivityBase.this.setVisibility(this.timerText, 0);
                FtcDriverStationActivityBase.this.setVisibility(this.timerStopWatch, 8);
                FtcDriverStationActivityBase.this.setVisibility(this.timerSwitchOn, 0);
                FtcDriverStationActivityBase.this.setVisibility(this.timerSwitchOff, 8);
                this.enabled = true;
                displaySecondsRemaining(getSecondsRemaining());
            }
        }

        public void disable() {
            FtcDriverStationActivityBase.this.setTextView(this.timerText, "");
            FtcDriverStationActivityBase.this.setVisibility(this.timerText, 8);
            FtcDriverStationActivityBase.this.setVisibility(this.timerStopWatch, 0);
            FtcDriverStationActivityBase.this.setVisibility(this.timerSwitchOn, 8);
            FtcDriverStationActivityBase.this.setVisibility(this.timerSwitchOff, 0);
            this.enabled = false;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void start() {
            if (this.enabled) {
                RobotLog.vv(FtcDriverStationActivityBase.TAG, "Starting to run current op mode for " + getSecondsRemaining() + " seconds");
                FtcDriverStationActivityBase.this.appUtil.synchronousRunOnUiThread(new Runnable() {
                    public void run() {
                        CountDownTimer access$000 = OpModeCountDownTimer.this.countDownTimer;
                        if (access$000 != null) {
                            access$000.cancel();
                        }
                        CountDownTimer unused = OpModeCountDownTimer.this.countDownTimer = new CountDownTimer(OpModeCountDownTimer.this.msRemaining, 1000) {
                            public void onTick(long j) {
                                FtcDriverStationActivityBase.this.assertUiThread();
                                OpModeCountDownTimer.this.setMsRemaining(j);
                                RobotLog.vv(FtcDriverStationActivityBase.TAG, "Running current op mode for " + (j / 1000) + " seconds");
                            }

                            public void onFinish() {
                                FtcDriverStationActivityBase.this.assertUiThread();
                                RobotLog.vv(FtcDriverStationActivityBase.TAG, "Stopping current op mode, timer expired");
                                OpModeCountDownTimer.this.resetCountdown();
                                FtcDriverStationActivityBase.this.handleOpModeStop();
                            }
                        }.start();
                    }
                });
            }
        }

        public void stop() {
            FtcDriverStationActivityBase.this.appUtil.synchronousRunOnUiThread(new Runnable() {
                public void run() {
                    if (OpModeCountDownTimer.this.countDownTimer != null) {
                        OpModeCountDownTimer.this.countDownTimer.cancel();
                        CountDownTimer unused = OpModeCountDownTimer.this.countDownTimer = null;
                    }
                }
            });
        }

        public void stopPreservingRemainingTime() {
            CountDownTimer countDownTimer2 = this.countDownTimer;
            long j = this.msRemaining;
            if (countDownTimer2 != null) {
                synchronized (countDownTimer2) {
                    j = this.msRemaining;
                }
            }
            stop();
            setMsRemaining(j);
        }

        public long getSecondsRemaining() {
            return this.msRemaining / 1000;
        }

        public void resetCountdown() {
            setMsRemaining(MS_COUNTDOWN_INTERVAL);
        }

        public void setMsRemaining(long j) {
            this.msRemaining = j;
            if (this.enabled) {
                displaySecondsRemaining(j / 1000);
            }
        }
    }

    protected enum UIState {
        UNKNOWN("U"),
        CANT_CONTINUE("E"),
        DISCONNECTED("X"),
        CONNNECTED("C"),
        WAITING_FOR_OPMODE_SELECTION("M"),
        WAITING_FOR_INIT_EVENT("K"),
        WAITING_FOR_ACK("KW"),
        WAITING_FOR_START_EVENT("S"),
        WAITING_FOR_STOP_EVENT("P"),
        ROBOT_STOPPED("Z");
        
        public final String indicator;

        private UIState(String str) {
            this.indicator = str;
        }
    }

    /* access modifiers changed from: protected */
    public void enforcePermissionValidator() {
        if (!permissionsValidated) {
            RobotLog.vv(TAG, "Redirecting to permission validator");
            startActivity(new Intent(AppUtil.getDefContext(), PermissionValidatorWrapper.class));
            finish();
            return;
        }
        RobotLog.vv(TAG, "Permissions validated already");
    }

    public static void setPermissionsValidated() {
        permissionsValidated = true;
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        enforcePermissionValidator();
        this.uiThread = Thread.currentThread();
        subclassOnCreate();
        this.gamepadManager = new GamepadManager(this);
        this.context = this;
        this.utility = new Utility(this);
        this.opModeCountDown = new OpModeCountDownTimer();
        this.rcHasIndependentBattery = false;
        PreferenceManager.setDefaultValues(this, R.xml.app_settings, false);
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.preferencesHelper = new PreferencesHelper(TAG, this.preferences);
        DeviceNameManagerFactory.getInstance().start(this.deviceNameManagerStartResult);
        PreferenceRemoterDS.getInstance().start(this.prefRemoterStartResult);
        NetworkConnectionHandler.getInstance().registerPeerStatusCallback(this);
        setClientConnected(false);
        if (permissionsValidated) {
            RobotLog.ii(TAG, "Processing all classes through class filter");
            ClassManagerFactory.registerResourceFilters();
            ClassManagerFactory.processAllClasses();
        }
        this.robotConfigFileManager = new RobotConfigFileManager(this);
        this.textDeviceName = (TextView) findViewById(R.id.textDeviceName);
        this.textDsUiStateIndicator = (TextView) findViewById(R.id.textDsUiStateIndicator);
        this.textWifiDirectStatus = (TextView) findViewById(R.id.textWifiDirectStatus);
        this.textWifiDirectStatusShowingRC = false;
        this.textWifiChannel = (TextView) findViewById(R.id.wifiChannel);
        this.textPingStatus = (TextView) findViewById(R.id.textPingStatus);
        this.textBytesPerSecond = (TextView) findViewById(R.id.bps);
        this.textTelemetry = (TextView) findViewById(R.id.textTelemetry);
        TextView textView = (TextView) findViewById(R.id.textSystemTelemetry);
        this.systemTelemetry = textView;
        this.systemTelemetryOriginalColor = textView.getCurrentTextColor();
        this.rcBatteryContainer = findViewById(R.id.rcBatteryContainer);
        this.rcBatteryTelemetry = (TextView) findViewById(R.id.rcBatteryTelemetry);
        this.robotBatteryMinimum = (TextView) findViewById(R.id.robotBatteryMinimum);
        this.rcBatteryIcon = (ImageView) findViewById(R.id.rc_battery_icon);
        this.dsBatteryInfo = (TextView) findViewById(R.id.dsBatteryInfo);
        this.robotBatteryTelemetry = (TextView) findViewById(R.id.robotBatteryTelemetry);
        this.dsBatteryIcon = (ImageView) findViewById(R.id.DS_battery_icon);
        this.immersion = new ImmersiveMode(getWindow().getDecorView());
        doMatchNumFieldBehaviorInit();
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.cameraStreamLayout);
        this.cameraStreamLayout = linearLayout;
        linearLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FtcDriverStationActivityBase.this.networkConnectionHandler.sendCommand(new Command(RobotCoreCommandList.CMD_REQUEST_FRAME));
            }
        });
        this.cameraStreamImageView = (ImageView) findViewById(R.id.cameraStreamImageView);
        CameraStreamClient.getInstance().setListener(new CameraStreamClient.Listener() {
            public void onStreamAvailableChange(boolean z) {
                FtcDriverStationActivityBase.this.invalidateOptionsMenu();
                if (FtcDriverStationActivityBase.this.cameraStreamOpen && !z) {
                    FtcDriverStationActivityBase.this.hideCameraStream();
                }
            }

            public void onFrameBitmap(final Bitmap bitmap) {
                FtcDriverStationActivityBase.this.runOnUiThread(new Runnable() {
                    public void run() {
                        FtcDriverStationActivityBase.this.cameraStreamImageView.setImageBitmap(bitmap);
                    }
                });
            }
        });
        this.buttonInit = findViewById(R.id.buttonInit);
        this.buttonInitStop = findViewById(R.id.buttonInitStop);
        this.buttonStart = findViewById(R.id.buttonStart);
        this.controlPanelBack = findViewById(R.id.controlPanel);
        this.batteryInfo = findViewById(R.id.battery_info_layout);
        this.wifiInfo = findViewById(R.id.wifi_info_layout);
        ((ImageButton) findViewById(R.id.buttonStartArrow)).setImageDrawable(new FilledPolygonDrawable(((ColorDrawable) findViewById(R.id.buttonStartArrowColor).getBackground()).getColor(), 3));
        ((ImageView) findViewById(R.id.timerStopWatch)).setImageDrawable(new StopWatchDrawable(((ColorDrawable) findViewById(R.id.timerStopWatchColorHolder).getBackground()).getColor()));
        this.gamepadIndicators.put(GamepadUser.ONE, new GamepadIndicator(this, R.id.user1_icon_clicked, R.id.user1_icon_base));
        this.gamepadIndicators.put(GamepadUser.TWO, new GamepadIndicator(this, R.id.user2_icon_clicked, R.id.user2_icon_base));
        this.gamepadManager.setGamepadIndicators(this.gamepadIndicators);
        TextView textView2 = (TextView) findViewById(R.id.activeConfigName);
        this.activeConfigText = textView2;
        textView2.setText(" ");
        this.timerAndTimerSwitch = findViewById(R.id.timerAndTimerSwitch);
        this.buttonAutonomous = (Button) findViewById(R.id.buttonAutonomous);
        this.buttonTeleOp = (Button) findViewById(R.id.buttonTeleOp);
        this.currentOpModeName = (TextView) findViewById(R.id.currentOpModeName);
        this.chooseOpModePrompt = findViewById(R.id.chooseOpModePrompt);
        this.buttonStop = (ImageButton) findViewById(R.id.buttonStop);
        ImageButton imageButton = (ImageButton) findViewById(R.id.menu_buttons);
        this.buttonMenu = imageButton;
        imageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FtcDriverStationActivityBase ftcDriverStationActivityBase = FtcDriverStationActivityBase.this;
                PopupMenu popupMenu = new PopupMenu(ftcDriverStationActivityBase, ftcDriverStationActivityBase.getPopupMenuAnchor());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        return FtcDriverStationActivityBase.this.onOptionsItemSelected(menuItem);
                    }
                });
                FtcDriverStationActivityBase.this.onCreateOptionsMenu(popupMenu.getMenu());
                popupMenu.show();
            }
        });
        this.preferences.registerOnSharedPreferenceChangeListener(this);
        this.gamepadManager.open();
        BatteryChecker batteryChecker2 = new BatteryChecker(this, (long) 300000);
        this.batteryChecker = batteryChecker2;
        batteryChecker2.startBatteryMonitoring();
        resetBatteryStats();
        pingStatus((int) R.string.ping_status_no_heartbeat);
        this.mInputManager = (InputManager) getSystemService("input");
        this.networkConnectionHandler.pushNetworkConnectionCallback(this);
        this.networkConnectionHandler.pushReceiveLoopCallback(this);
        startOrRestartNetwork();
        DeviceNameManagerFactory.getInstance().registerCallback(this.deviceNameManagerCallback);
        ((WifiManager) AppUtil.getDefContext().getApplicationContext().getSystemService("wifi")).setWifiEnabled(true);
        WifiMuteStateMachine wifiMuteStateMachine2 = new WifiMuteStateMachine();
        this.wifiMuteStateMachine = wifiMuteStateMachine2;
        wifiMuteStateMachine2.initialize();
        this.wifiMuteStateMachine.start();
        this.wifiMuteStateMachine.registerCallback(this);
        this.processUserActivity = true;
        SoundPlayingRobotMonitor.prefillSoundCache();
        RobotLog.logBuildConfig(BuildConfig.class);
        RobotLog.logDeviceInfo();
        this.androidTextToSpeech.initialize();
    }

    /* access modifiers changed from: protected */
    public void startOrRestartNetwork() {
        RobotLog.vv(TAG, "startOrRestartNetwork()");
        assumeClientDisconnect();
        showWifiStatus(false, getString(R.string.wifiStatusDisconnected));
        initializeNetwork();
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        RobotLog.onApplicationStart();
        RobotLog.vv(TAG, "onStart()");
        for (GamepadIndicator state : this.gamepadIndicators.values()) {
            state.setState(GamepadIndicator.State.INVISIBLE);
        }
        this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.ACTIVITY_START);
        this.wifiMuteStateMachine.unMaskEvent(WifiMuteEvent.STOPPED_OPMODE);
        FtcAboutActivity.setBuildTimeFromBuildConfig(BuildConfig.BUILD_TIME);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        RobotLog.vv(TAG, "onResume()");
        this.disconnectFromPeerOnActivityStop = true;
        updateRcBatteryIndependence(this.preferences);
        resetBatteryStats();
        this.mInputManager.registerInputDeviceListener(this.gamepadManager, (Handler) null);
        pingStatus((int) R.string.ping_status_no_heartbeat);
    }

    /* access modifiers changed from: protected */
    public void initializeNetwork() {
        updateLoggingPrefs();
        NetworkType defaultNetworkType = NetworkConnectionHandler.getDefaultNetworkType(this);
        this.connectionOwner = this.preferences.getString(getString(R.string.pref_connection_owner_identity), getString(R.string.connection_owner_default));
        this.connectionOwnerPassword = this.preferences.getString(getString(R.string.pref_connection_owner_password), getString(R.string.connection_owner_password_default));
        this.networkConnectionHandler.init(NetworkConnectionHandler.newWifiLock(), defaultNetworkType, this.connectionOwner, this.connectionOwnerPassword, this, this.gamepadManager);
        if (this.networkConnectionHandler.isNetworkConnected()) {
            RobotLog.vv("Robocol", "Spoofing a Network Connection event...");
            onNetworkConnectionEvent(NetworkConnection.NetworkEvent.CONNECTION_INFO_AVAILABLE);
        }
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        RobotLog.vv(TAG, "onPause()");
        this.gamepadManager.clearGamepadAssignments();
        this.gamepadManager.clearTrackedGamepads();
        this.mInputManager.unregisterInputDeviceListener(this.gamepadManager);
        initDefaultOpMode();
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        RobotLog.vv(TAG, "onStop()");
        pingStatus((int) R.string.ping_status_stopped);
        this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.ACTIVITY_STOP);
        if (this.disconnectFromPeerOnActivityStop) {
            RobotLog.ii(TAG, "App appears to be exiting. Destroying activity so that another DS can connect");
            finish();
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        RobotLog.vv(TAG, "onDestroy()");
        this.androidTextToSpeech.close();
        this.gamepadManager.close();
        DeviceNameManagerFactory.getInstance().unregisterCallback(this.deviceNameManagerCallback);
        this.networkConnectionHandler.removeNetworkConnectionCallback(this);
        this.networkConnectionHandler.removeReceiveLoopCallback(this);
        shutdown();
        PreferenceRemoterDS.getInstance().stop(this.prefRemoterStartResult);
        DeviceNameManagerFactory.getInstance().stop(this.deviceNameManagerStartResult);
        RobotLog.cancelWriteLogcatToDisk();
    }

    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (z) {
            this.immersion.hideSystemUI();
            getWindow().setFlags(134217728, 134217728);
        }
    }

    public void showToast(String str) {
        this.appUtil.showToast(UILocation.ONLY_LOCAL, str);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
        RobotLog.vv(TAG, "onSharedPreferenceChanged() pref=%s", str);
        if (str.equals(this.context.getString(R.string.pref_device_name_rc_display))) {
            final String string = sharedPreferences.getString(str, "");
            if (string.length() > 0) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (FtcDriverStationActivityBase.this.textWifiDirectStatusShowingRC) {
                            FtcDriverStationActivityBase.this.textWifiDirectStatus.setText(string);
                        }
                    }
                });
            }
        } else if (str.equals(getString(R.string.pref_has_independent_phone_battery_rc))) {
            updateRcBatteryIndependence(this.preferences);
        } else if (!str.equals(getString(R.string.pref_app_theme)) && str.equals("pref_wifip2p_channel")) {
            RobotLog.vv(TAG, "pref_wifip2p_channel changed.");
            showWifiChannel();
        }
        updateLoggingPrefs();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ftc_driver_station, menu);
        if (this.uiState != UIState.WAITING_FOR_START_EVENT || !CameraStreamClient.getInstance().isStreamAvailable()) {
            menu.findItem(R.id.action_camera_stream).setVisible(false);
        } else {
            menu.findItem(R.id.action_camera_stream).setVisible(true);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.ACTIVITY_OTHER);
        this.wifiMuteStateMachine.maskEvent(WifiMuteEvent.STOPPED_OPMODE);
        switch (menuItem.getItemId()) {
            case R.id.action_about /*2131230760*/:
                startActivity(new Intent(AppUtil.getDefContext(), FtcAboutActivity.class));
                return true;
            case R.id.action_camera_stream /*2131230768*/:
                if (this.cameraStreamOpen) {
                    hideCameraStream();
                } else {
                    showCameraStream();
                }
                return true;
            case R.id.action_configure /*2131230769*/:
                EditParameters editParameters = new EditParameters();
                Intent intent = new Intent(AppUtil.getDefContext(), FtcLoadFileActivity.class);
                editParameters.putIntent(intent);
                startActivityForResult(intent, LaunchActivityConstantsList.RequestCode.CONFIGURE_DRIVER_STATION.ordinal());
                return true;
            case R.id.action_exit_app /*2131230773*/:
                finishAffinity();
                for (ActivityManager.AppTask finishAndRemoveTask : ((ActivityManager) getSystemService("activity")).getAppTasks()) {
                    finishAndRemoveTask.finishAndRemoveTask();
                }
                AppUtil.getInstance().exitApplication();
                return true;
            case R.id.action_inspection_mode /*2131230776*/:
                startActivityForResult(new Intent(getBaseContext(), FtcDriverStationInspectionReportsActivity.class), LaunchActivityConstantsList.RequestCode.INSPECTIONS.ordinal());
                return true;
            case R.id.action_program_and_manage /*2131230782*/:
                RobotLog.vv(TAG, "action_program_and_manage clicked");
                this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_START_DS_PROGRAM_AND_MANAGE));
                return true;
            case R.id.action_restart_robot /*2131230783*/:
                this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_RESTART_ROBOT));
                this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.ACTIVITY_START);
                this.wifiMuteStateMachine.maskEvent(WifiMuteEvent.STOPPED_OPMODE);
                return true;
            case R.id.action_settings /*2131230784*/:
                startActivityForResult(new Intent(getBaseContext(), FtcDriverStationSettingsActivity.class), LaunchActivityConstantsList.RequestCode.SETTINGS_DRIVER_STATION.ordinal());
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        RobotLog.vv(TAG, "onActivityResult(request=%d)", Integer.valueOf(i));
        if (i == LaunchActivityConstantsList.RequestCode.SETTINGS_DRIVER_STATION.ordinal()) {
            if (intent != null) {
                FtcDriverStationSettingsActivity.Result deserialize = FtcDriverStationSettingsActivity.Result.deserialize(intent.getExtras().getString("RESULT"));
                if (deserialize.prefLogsClicked) {
                    updateLoggingPrefs();
                }
                if (deserialize.prefPairingMethodChanged) {
                    RobotLog.ii(TAG, "Pairing method changed in settings activity, shutdown network to force complete restart");
                    startOrRestartNetwork();
                }
                if (deserialize.prefPairClicked) {
                    startOrRestartNetwork();
                }
                if (deserialize.prefAdvancedClicked) {
                    this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_RESTART_ROBOT));
                }
            }
        } else if (i == LaunchActivityConstantsList.RequestCode.CONFIGURE_DRIVER_STATION.ordinal()) {
            requestUIState();
            this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_RESTART_ROBOT));
        }
    }

    /* access modifiers changed from: protected */
    public void updateLoggingPrefs() {
        boolean z = this.preferences.getBoolean(getString(R.string.pref_debug_driver_station_logs), false);
        this.debugLogging = z;
        this.gamepadManager.setDebug(z);
        if (this.preferences.getBoolean(getString(R.string.pref_match_logging_on_off), false)) {
            enableMatchLoggingUI();
        } else {
            disableMatchLoggingUI();
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    public boolean dispatchGenericMotionEvent(MotionEvent motionEvent) {
        if (!Gamepad.isGamepadDevice(motionEvent.getDeviceId())) {
            return super.dispatchGenericMotionEvent(motionEvent);
        }
        this.gamepadManager.handleGamepadEvent(motionEvent);
        return true;
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (!Gamepad.isGamepadDevice(keyEvent.getDeviceId())) {
            return super.dispatchKeyEvent(keyEvent);
        }
        this.gamepadManager.handleGamepadEvent(keyEvent);
        return true;
    }

    public CallbackResult onNetworkConnectionEvent(NetworkConnection.NetworkEvent networkEvent) {
        CallbackResult callbackResult = CallbackResult.NOT_HANDLED;
        RobotLog.i("Received networkConnectionEvent: " + networkEvent.toString());
        switch (AnonymousClass24.$SwitchMap$com$qualcomm$robotcore$wifi$NetworkConnection$NetworkEvent[networkEvent.ordinal()]) {
            case 1:
                if (this.networkConnectionHandler.isWifiDirect()) {
                    onPeersAvailableWifiDirect();
                } else {
                    onPeersAvailableSoftAP();
                }
                return CallbackResult.HANDLED;
            case 2:
                RobotLog.ee(TAG, "Wifi Direct - connected as Group Owner, was expecting Peer");
                showWifiStatus(false, getString(R.string.wifiStatusErrorConnectedAsGroupOwner));
                ConfigWifiDirectActivity.launch(getBaseContext(), ConfigWifiDirectActivity.Flag.WIFI_DIRECT_DEVICE_NAME_INVALID);
                return CallbackResult.HANDLED;
            case 3:
                showWifiStatus(false, getString(R.string.wifiStatusConnecting));
                return CallbackResult.HANDLED;
            case 4:
                showWifiStatus(false, getString(R.string.wifiStatusConnected));
                return CallbackResult.HANDLED;
            case 5:
                showWifiStatus(true, getBestRobotControllerName());
                showWifiChannel();
                if (!NetworkConnection.isDeviceNameValid(this.networkConnectionHandler.getDeviceName())) {
                    RobotLog.ee(TAG, "Wifi-Direct device name contains non-printable characters");
                    ConfigWifiDirectActivity.launch(getBaseContext(), ConfigWifiDirectActivity.Flag.WIFI_DIRECT_DEVICE_NAME_INVALID);
                } else if (this.networkConnectionHandler.connectedWithUnexpectedDevice()) {
                    showWifiStatus(false, getString(R.string.wifiStatusErrorWrongDevice));
                    if (this.networkConnectionHandler.isWifiDirect()) {
                        ConfigWifiDirectActivity.launch(getBaseContext(), ConfigWifiDirectActivity.Flag.WIFI_DIRECT_FIX_CONFIG);
                    } else if (this.connectionOwner == null && this.connectionOwnerPassword == null) {
                        showWifiStatus(false, getString(R.string.wifiStatusNotPaired));
                        return CallbackResult.HANDLED;
                    } else {
                        this.networkConnectionHandler.startConnection(this.connectionOwner, this.connectionOwnerPassword);
                    }
                    return CallbackResult.HANDLED;
                }
                this.networkConnectionHandler.handleConnectionInfoAvailable();
                this.networkConnectionHandler.cancelConnectionSearch();
                assumeClientConnectAndRefreshUI(ControlPanelBack.NO_CHANGE);
                return CallbackResult.HANDLED;
            case 6:
                String string = getString(R.string.wifiStatusDisconnected);
                showWifiStatus(false, string);
                RobotLog.vv(TAG, "Network Connection - " + string);
                this.networkConnectionHandler.discoverPotentialConnections();
                assumeClientDisconnect();
                return CallbackResult.HANDLED;
            case 7:
                String string2 = getString(R.string.dsErrorMessage, new Object[]{this.networkConnectionHandler.getFailureReason()});
                showWifiStatus(false, string2);
                RobotLog.vv(TAG, "Network Connection - " + string2);
                return callbackResult;
            default:
                return callbackResult;
        }
    }

    private String getBestRobotControllerName() {
        return this.networkConnectionHandler.getConnectionOwnerName();
    }

    private void onPeersAvailableWifiDirect() {
        if (!this.networkConnectionHandler.connectingOrConnected()) {
            onPeersAvailableSoftAP();
        }
    }

    private void onPeersAvailableSoftAP() {
        if (this.networkConnectionHandler.connectionMatches(getString(R.string.connection_owner_default))) {
            showWifiStatus(false, getString(R.string.wifiStatusNotPaired));
        } else {
            showWifiStatus(false, getString(R.string.wifiStatusSearching));
        }
        this.networkConnectionHandler.handlePeersAvailable();
    }

    public void onClickButtonInit(View view) {
        handleOpModeInit();
    }

    public void onClickButtonStart(View view) {
        handleOpModeStart();
    }

    public void onClickTimer(View view) {
        boolean z = !this.opModeUseTimer;
        this.opModeUseTimer = z;
        enableAndResetTimer(z);
    }

    /* access modifiers changed from: protected */
    public void enableAndResetTimer(boolean z) {
        if (!z) {
            this.opModeCountDown.disable();
        } else {
            stopTimerAndReset();
            this.opModeCountDown.enable();
        }
        this.opModeUseTimer = z;
    }

    /* access modifiers changed from: protected */
    public void enableAndResetTimerForQueued() {
        enableAndResetTimer(this.queuedOpMode.flavor == OpModeMeta.Flavor.AUTONOMOUS);
    }

    /* access modifiers changed from: package-private */
    public void stopTimerPreservingRemainingTime() {
        this.opModeCountDown.stopPreservingRemainingTime();
    }

    /* access modifiers changed from: package-private */
    public void stopTimerAndReset() {
        this.opModeCountDown.stop();
        this.opModeCountDown.resetCountdown();
    }

    public void onClickButtonAutonomous(View view) {
        showOpModeDialog(filterOpModes(new Predicate<OpModeMeta>() {
            public boolean test(OpModeMeta opModeMeta) {
                return opModeMeta.flavor == OpModeMeta.Flavor.AUTONOMOUS;
            }
        }), R.string.opmodeDialogTitleAutonomous);
    }

    public void onClickButtonTeleOp(View view) {
        showOpModeDialog(filterOpModes(new Predicate<OpModeMeta>() {
            public boolean test(OpModeMeta opModeMeta) {
                return opModeMeta.flavor == OpModeMeta.Flavor.TELEOP;
            }
        }), R.string.opmodeDialogTitleTeleOp);
    }

    /* access modifiers changed from: protected */
    public List<OpModeMeta> filterOpModes(Predicate<OpModeMeta> predicate) {
        LinkedList linkedList = new LinkedList();
        for (OpModeMeta next : this.opModes) {
            if (predicate.test(next)) {
                linkedList.add(next);
            }
        }
        return linkedList;
    }

    /* access modifiers changed from: protected */
    public void showOpModeDialog(List<OpModeMeta> list, int i) {
        stopTimerPreservingRemainingTime();
        initDefaultOpMode();
        OpModeSelectionDialogFragment opModeSelectionDialogFragment = new OpModeSelectionDialogFragment();
        opModeSelectionDialogFragment.setOnSelectionDialogListener(this);
        opModeSelectionDialogFragment.setOpModes(list);
        opModeSelectionDialogFragment.setTitle(i);
        opModeSelectionDialogFragment.show(getFragmentManager(), "op_mode_selection");
    }

    /* access modifiers changed from: protected */
    public void showCameraStream() {
        this.cameraStreamOpen = true;
        this.gamepadManager.setEnabled(false);
        setVisibility(this.cameraStreamLayout, 0);
        setVisibility(this.buttonStart, 4);
        this.networkConnectionHandler.sendCommand(new Command(RobotCoreCommandList.CMD_REQUEST_FRAME));
        showToast(getString(R.string.toastDisableGamepadsStream));
    }

    /* access modifiers changed from: protected */
    public void hideCameraStream() {
        this.cameraStreamOpen = false;
        this.gamepadManager.setEnabled(true);
        setVisibility(this.cameraStreamLayout, 4);
        setVisibility(this.buttonStart, 0);
    }

    public void onClickButtonStop(View view) {
        handleOpModeStop();
    }

    public void onOpModeSelectionClick(OpModeMeta opModeMeta) {
        handleOpModeQueued(opModeMeta);
    }

    /* access modifiers changed from: protected */
    public void shutdown() {
        this.networkConnectionHandler.stop();
        this.networkConnectionHandler.shutdown();
    }

    public CallbackResult packetReceived(RobocolDatagram robocolDatagram) throws RobotCoreException {
        return CallbackResult.NOT_HANDLED;
    }

    public void onPeerConnected() {
        RobotLog.vv(TAG, "robot controller connected");
        assumeClientConnectAndRefreshUI(ControlPanelBack.NO_CHANGE);
        PreferenceRemoterDS.getInstance().sendInformationalPrefsToRc();
    }

    public void onPeerDisconnected() {
        RobotLog.logStackTrace(new Throwable("Peer disconnected"));
        RobotLog.vv(TAG, "robot controller disconnected");
        assumeClientDisconnect();
    }

    public CallbackResult peerDiscoveryEvent(RobocolDatagram robocolDatagram) throws RobotCoreException {
        try {
            PeerDiscovery forReceive = PeerDiscovery.forReceive();
            forReceive.fromByteArray(robocolDatagram.getData());
            if (forReceive.getPeerType() == PeerDiscovery.PeerType.NOT_CONNECTED_DUE_TO_PREEXISTING_CONNECTION) {
                reportGlobalError(getString(R.string.anotherDsIsConnectedError), false);
                showRobotBatteryVoltage(FtcEventLoopHandler.NO_VOLTAGE_SENSOR);
            } else {
                this.networkConnectionHandler.updateConnection(robocolDatagram);
            }
        } catch (RobotProtocolException e) {
            reportGlobalError(e.getMessage(), false);
            this.networkConnectionHandler.stopPeerDiscovery();
            RobotLog.setGlobalErrorMsgSticky(true);
            Thread.currentThread().interrupt();
            showRobotBatteryVoltage(FtcEventLoopHandler.NO_VOLTAGE_SENSOR);
        }
        return CallbackResult.HANDLED;
    }

    public CallbackResult heartbeatEvent(RobocolDatagram robocolDatagram, long j) {
        try {
            this.heartbeatRecv.fromByteArray(robocolDatagram.getData());
            RobotLog.processTimeSynch(this.heartbeatRecv.t0, this.heartbeatRecv.t1, this.heartbeatRecv.t2, j);
            double elapsedSeconds = this.heartbeatRecv.getElapsedSeconds();
            this.heartbeatRecv.getSequenceNumber();
            setRobotState(RobotState.fromByte(this.heartbeatRecv.getRobotState()));
            this.pingAverage.addNumber((int) (elapsedSeconds * 1000.0d));
            if (this.lastUiUpdate.time() > 0.5d) {
                this.lastUiUpdate.reset();
                networkStatus();
            }
        } catch (RobotCoreException e) {
            RobotLog.logStackTrace(e);
        }
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public void networkStatus() {
        pingStatus(String.format("%dms", new Object[]{Integer.valueOf(this.pingAverage.getAverage())}));
        long bytesPerSecond = this.networkConnectionHandler.getBytesPerSecond();
        if (bytesPerSecond > 0) {
            showBytesPerSecond(bytesPerSecond);
        }
    }

    /* access modifiers changed from: protected */
    public void setRobotState(RobotState robotState2) {
        WifiMuteStateMachine wifiMuteStateMachine2;
        if (this.robotState != robotState2) {
            this.robotState = robotState2;
            if (robotState2 == RobotState.STOPPED) {
                traceUiStateChange("ui:uiRobotStopped", UIState.ROBOT_STOPPED);
                disableAndDimOpModeMenu();
                disableOpModeControls();
                dimControlPanelBack();
            }
            if (robotState2 == RobotState.EMERGENCY_STOP && (wifiMuteStateMachine2 = this.wifiMuteStateMachine) != null) {
                wifiMuteStateMachine2.consumeEvent(WifiMuteEvent.STOPPED_OPMODE);
            }
        }
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleNotifyRobotState(String str) {
        setRobotState(RobotState.fromByte(Integer.valueOf(str).intValue()));
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleReportGlobalError(String str) {
        RobotLog.ee(TAG, "Received error from robot controller: " + str);
        RobotLog.setGlobalErrorMsg(str);
        return CallbackResult.HANDLED;
    }

    public CallbackResult commandEvent(Command command) {
        CallbackResult callbackResult = CallbackResult.NOT_HANDLED;
        try {
            String name = command.getName();
            String extra = command.getExtra();
            char c = 65535;
            switch (name.hashCode()) {
                case -1530733715:
                    if (name.equals(RobotCoreCommandList.CMD_NOTIFY_OP_MODE_LIST)) {
                        c = 1;
                        break;
                    }
                    break;
                case -1121067382:
                    if (name.equals(RobotCoreCommandList.CMD_SHOW_TOAST)) {
                        c = 6;
                        break;
                    }
                    break;
                case -992356734:
                    if (name.equals(RobotCoreCommandList.CMD_DISMISS_DIALOG)) {
                        c = 10;
                        break;
                    }
                    break;
                case -939314969:
                    if (name.equals(RobotCoreCommandList.CMD_DISMISS_PROGRESS)) {
                        c = 8;
                        break;
                    }
                    break;
                case -856964827:
                    if (name.equals(RobotCoreCommandList.CMD_SHOW_DIALOG)) {
                        c = 9;
                        break;
                    }
                    break;
                case -362340438:
                    if (name.equals(RobotCoreCommandList.CMD_SET_TELEMETRY_DISPLAY_FORMAT)) {
                        c = 21;
                        break;
                    }
                    break;
                case -321815447:
                    if (name.equals(CommandList.CmdPlaySound.Command)) {
                        c = 14;
                        break;
                    }
                    break;
                case -206959740:
                    if (name.equals(RobotCoreCommandList.CMD_ROBOT_CONTROLLER_PREFERENCE)) {
                        c = 13;
                        break;
                    }
                    break;
                case -44710726:
                    if (name.equals(CommandList.CmdRequestSound.Command)) {
                        c = 15;
                        break;
                    }
                    break;
                case 78754538:
                    if (name.equals(RobotCoreCommandList.CMD_STREAM_CHANGE)) {
                        c = 17;
                        break;
                    }
                    break;
                case 202444237:
                    if (name.equals(CommandList.CmdStopPlayingSounds.Command)) {
                        c = 16;
                        break;
                    }
                    break;
                case 323288778:
                    if (name.equals(RobotCoreCommandList.CMD_SHOW_PROGRESS)) {
                        c = 7;
                        break;
                    }
                    break;
                case 619130094:
                    if (name.equals(RobotCoreCommandList.CMD_NOTIFY_ACTIVE_CONFIGURATION)) {
                        c = 3;
                        break;
                    }
                    break;
                case 739339659:
                    if (name.equals(RobotCoreCommandList.CMD_NOTIFY_ROBOT_STATE)) {
                        c = 0;
                        break;
                    }
                    break;
                case 857479075:
                    if (name.equals(RobotCoreCommandList.CMD_NOTIFY_INIT_OP_MODE)) {
                        c = 4;
                        break;
                    }
                    break;
                case 899701436:
                    if (name.equals(RobotCoreCommandList.CMD_NOTIFY_RUN_OP_MODE)) {
                        c = 5;
                        break;
                    }
                    break;
                case 1332202628:
                    if (name.equals(RobotCoreCommandList.CMD_NOTIFY_USER_DEVICE_LIST)) {
                        c = 2;
                        break;
                    }
                    break;
                case 1506024019:
                    if (name.equals(RobotCoreCommandList.CMD_DISMISS_ALL_DIALOGS)) {
                        c = 11;
                        break;
                    }
                    break;
                case 1509292278:
                    if (name.equals(RobotCoreCommandList.CMD_RECEIVE_FRAME_BEGIN)) {
                        c = 18;
                        break;
                    }
                    break;
                case 1510318778:
                    if (name.equals(RobotCoreCommandList.CMD_RECEIVE_FRAME_CHUNK)) {
                        c = 19;
                        break;
                    }
                    break;
                case 1661597945:
                    if (name.equals(CommandList.CMD_START_DS_PROGRAM_AND_MANAGE_RESP)) {
                        c = 12;
                        break;
                    }
                    break;
                case 1852830809:
                    if (name.equals(RobotCoreCommandList.CMD_TEXT_TO_SPEECH)) {
                        c = 20;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    return handleNotifyRobotState(extra);
                case 1:
                    return handleCommandNotifyOpModeList(extra);
                case 2:
                    return handleCommandNotifyUserDeviceList(extra);
                case 3:
                    return handleCommandNotifyActiveConfig(extra);
                case 4:
                    return handleCommandNotifyInitOpMode(extra);
                case 5:
                    return handleCommandNotifyStartOpMode(extra);
                case 6:
                    return handleCommandShowToast(extra);
                case 7:
                    return handleCommandShowProgress(extra);
                case 8:
                    return handleCommandDismissProgress();
                case 9:
                    return handleCommandShowDialog(extra);
                case 10:
                    return handleCommandDismissDialog(command);
                case 11:
                    return handleCommandDismissAllDialogs(command);
                case 12:
                    return handleCommandStartProgramAndManageResp(extra);
                case 13:
                    return PreferenceRemoterDS.getInstance().handleCommandRobotControllerPreference(extra);
                case 14:
                    return SoundPlayer.getInstance().handleCommandPlaySound(extra);
                case 15:
                    return SoundPlayer.getInstance().handleCommandRequestSound(command);
                case 16:
                    return SoundPlayer.getInstance().handleCommandStopPlayingSounds(command);
                case 17:
                    return CameraStreamClient.getInstance().handleStreamChange(extra);
                case 18:
                    return CameraStreamClient.getInstance().handleReceiveFrameBegin(extra);
                case 19:
                    return CameraStreamClient.getInstance().handleReceiveFrameChunk(extra);
                case 20:
                    return handleCommandTextToSpeech(extra);
                case 21:
                    return handleCommandSetTelemetryDisplayFormat(extra);
                default:
                    return callbackResult;
            }
        } catch (Exception e) {
            RobotLog.logStackTrace(e);
            return callbackResult;
        }
    }

    public CallbackResult telemetryEvent(RobocolDatagram robocolDatagram) {
        try {
            TelemetryMessage telemetryMessage = new TelemetryMessage(robocolDatagram.getData());
            if (telemetryMessage.getRobotState() != RobotState.UNKNOWN) {
                setRobotState(telemetryMessage.getRobotState());
            }
            Map<String, String> dataStrings = telemetryMessage.getDataStrings();
            String str = "";
            boolean z = false;
            for (String next : telemetryMessage.isSorted() ? new TreeSet<>(dataStrings.keySet()) : dataStrings.keySet()) {
                if (next.equals(EventLoopManager.ROBOT_BATTERY_LEVEL_KEY)) {
                    showRobotBatteryVoltage(dataStrings.get(next));
                } else {
                    if (next.length() > 0 && next.charAt(0) != 0) {
                        str = str + next + ": ";
                    }
                    str = str + dataStrings.get(next) + "\n";
                    z = true;
                }
            }
            String str2 = str + "\n";
            Map<String, Float> dataNumbers = telemetryMessage.getDataNumbers();
            for (String next2 : telemetryMessage.isSorted() ? new TreeSet<>(dataNumbers.keySet()) : dataNumbers.keySet()) {
                if (next2.length() > 0 && next2.charAt(0) != 0) {
                    str2 = str2 + next2 + ": ";
                }
                str2 = str2 + dataNumbers.get(next2) + "\n";
                z = true;
            }
            String tag = telemetryMessage.getTag();
            if (tag.equals(EventLoopManager.SYSTEM_NONE_KEY)) {
                clearSystemTelemetry();
            } else if (tag.equals(EventLoopManager.SYSTEM_ERROR_KEY)) {
                reportGlobalError(dataStrings.get(tag), true);
            } else if (tag.equals(EventLoopManager.SYSTEM_WARNING_KEY)) {
                reportGlobalWarning(dataStrings.get(tag));
            } else if (tag.equals(EventLoopManager.RC_BATTERY_STATUS_KEY)) {
                updateRcBatteryStatus(BatteryChecker.BatteryStatus.deserialize(dataStrings.get(tag)));
            } else if (tag.equals(EventLoopManager.ROBOT_BATTERY_LEVEL_KEY)) {
                showRobotBatteryVoltage(dataStrings.get(tag));
            } else if (z) {
                setUserTelemetry(str2);
            }
            return CallbackResult.HANDLED;
        } catch (RobotCoreException e) {
            RobotLog.logStackTrace(e);
            return CallbackResult.HANDLED;
        }
    }

    /* access modifiers changed from: protected */
    public void showRobotBatteryVoltage(String str) {
        String str2 = str;
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.robot_battery_background);
        View findViewById = findViewById(R.id.rc_battery_layout);
        TextView textView = (TextView) findViewById(R.id.rc_no_voltage_sensor);
        if (str2.equals(FtcEventLoopHandler.NO_VOLTAGE_SENSOR)) {
            setVisibility(findViewById, 8);
            setVisibility(textView, 0);
            resetBatteryStats();
            setBG(relativeLayout, findViewById(R.id.rcBatteryBackgroundReference).getBackground());
            return;
        }
        setVisibility(findViewById, 0);
        setVisibility(textView, 8);
        double doubleValue = Double.valueOf(str).doubleValue();
        if (doubleValue < this.V12BatteryMin) {
            this.V12BatteryMin = doubleValue;
            this.V12BatteryMinString = str2;
        }
        TextView textView2 = this.robotBatteryTelemetry;
        setTextView(textView2, str2 + " V");
        TextView textView3 = this.robotBatteryMinimum;
        setTextView(textView3, "( " + this.V12BatteryMinString + " V )");
        double d = (double) 10.0f;
        double d2 = (double) 14.0f;
        setBGColor(relativeLayout, Color.HSVToColor(new float[]{(float) Range.scale(Range.clip(doubleValue, d, d2), d, d2, (double) 0.0f, (double) 128.0f), 1.0f, 0.6f}));
    }

    /* access modifiers changed from: protected */
    public void setBGColor(final View view, final int i) {
        runOnUiThread(new Runnable() {
            public void run() {
                view.setBackgroundColor(i);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setBG(final View view, final Drawable drawable) {
        runOnUiThread(new Runnable() {
            public void run() {
                view.setBackground(drawable);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void resetBatteryStats() {
        this.V12BatteryMin = Double.POSITIVE_INFINITY;
        this.V12BatteryMinString = "";
    }

    /* access modifiers changed from: protected */
    public void setUserTelemetry(String str) {
        int i = AnonymousClass24.$SwitchMap$org$firstinspires$ftc$robotcore$external$Telemetry$DisplayFormat[this.telemetryMode.ordinal()];
        if (i == 1 || i == 2) {
            setTextView(this.textTelemetry, str);
        } else if (i == 3) {
            setTextView(this.textTelemetry, Html.fromHtml(str.replace("\n", "<br>")));
        }
    }

    /* access modifiers changed from: protected */
    public void clearUserTelemetry() {
        setTextView(this.textTelemetry, "");
    }

    /* access modifiers changed from: protected */
    public void clearSystemTelemetry() {
        setVisibility(this.systemTelemetry, 8);
        setTextView(this.systemTelemetry, "");
        setTextColor(this.systemTelemetry, this.systemTelemetryOriginalColor);
        RobotLog.clearGlobalErrorMsg();
        RobotLog.clearGlobalWarningMsg();
    }

    public CallbackResult gamepadEvent(RobocolDatagram robocolDatagram) {
        return CallbackResult.NOT_HANDLED;
    }

    public CallbackResult emptyEvent(RobocolDatagram robocolDatagram) {
        return CallbackResult.NOT_HANDLED;
    }

    public CallbackResult reportGlobalError(String str, boolean z) {
        if (!RobotLog.getGlobalErrorMsg().equals(str)) {
            RobotLog.ee(TAG, "System telemetry error: " + str);
            RobotLog.clearGlobalErrorMsg();
            RobotLog.setGlobalErrorMsg(str);
        }
        TextView textView = this.systemTelemetry;
        AppUtil.getInstance();
        setTextColor(textView, AppUtil.getColor(R.color.text_error));
        setVisibility(this.systemTelemetry, 0);
        StringBuilder sb = new StringBuilder();
        RobotState robotState2 = this.robotState;
        if (!(robotState2 == null || robotState2 == RobotState.UNKNOWN)) {
            sb.append(String.format(getString(R.string.dsRobotStatus), new Object[]{this.robotState.toString(this)}));
        }
        if (z) {
            sb.append(getString(R.string.dsToAttemptRecovery));
        }
        sb.append(String.format(getString(R.string.dsErrorMessage), new Object[]{str}));
        setTextView(this.systemTelemetry, sb.toString());
        stopTimerAndReset();
        uiRobotCantContinue();
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public void reportGlobalWarning(String str) {
        if (!RobotLog.getGlobalWarningMessage().equals(str)) {
            RobotLog.ee(TAG, "System telemetry warning: " + str);
            RobotLog.clearGlobalWarningMsg();
            RobotLog.setGlobalWarningMessage(str);
        }
        TextView textView = this.systemTelemetry;
        AppUtil.getInstance();
        setTextColor(textView, AppUtil.getColor(R.color.text_warning));
        setVisibility(this.systemTelemetry, 0);
        setTextView(this.systemTelemetry, String.format(getString(R.string.dsWarningMessage), new Object[]{str}));
    }

    /* access modifiers changed from: protected */
    public void uiRobotCantContinue() {
        traceUiStateChange("ui:uiRobotCantContinue", UIState.CANT_CONTINUE);
        disableAndDimOpModeMenu();
        disableOpModeControls();
        dimControlPanelBack();
    }

    /* access modifiers changed from: protected */
    public void disableOpModeControls() {
        setEnabled(this.buttonInit, false);
        setVisibility(this.buttonInit, 0);
        setVisibility(this.buttonStart, 4);
        setVisibility(this.buttonStop, 4);
        setVisibility(this.buttonInitStop, 4);
        setVisibility(this.timerAndTimerSwitch, 4);
        hideCameraStream();
    }

    /* access modifiers changed from: protected */
    public void dimAndDisableAllControls() {
        dimControlPanelBack();
        setOpacity(this.wifiInfo, PARTLY_OPAQUE);
        setOpacity(this.batteryInfo, PARTLY_OPAQUE);
        disableAndDimOpModeMenu();
        disableOpModeControls();
    }

    /* access modifiers changed from: protected */
    public void uiRobotControllerIsDisconnected() {
        traceUiStateChange("ui:uiRobotControllerIsDisconnected", UIState.DISCONNECTED);
        dimAndDisableAllControls();
    }

    /* access modifiers changed from: protected */
    public void uiRobotControllerIsConnected(ControlPanelBack controlPanelBack2) {
        traceUiStateChange("ui:uiRobotControllerIsConnected", UIState.CONNNECTED);
        enableAndBrightenForConnected(controlPanelBack2);
        AppUtil.getInstance().dismissAllDialogs(UILocation.ONLY_LOCAL);
        AppUtil.getInstance().dismissProgress(UILocation.ONLY_LOCAL);
        setTextView(this.rcBatteryTelemetry, "");
        setTextView(this.robotBatteryTelemetry, "");
        showWifiChannel();
        hideCameraStream();
    }

    /* access modifiers changed from: protected */
    public void enableAndBrightenForConnected(ControlPanelBack controlPanelBack2) {
        setControlPanelBack(controlPanelBack2);
        setOpacity(this.wifiInfo, 1.0f);
        setOpacity(this.batteryInfo, 1.0f);
        enableAndBrightenOpModeMenu();
    }

    /* access modifiers changed from: protected */
    public void checkConnectedEnableBrighten(ControlPanelBack controlPanelBack2) {
        if (!this.clientConnected) {
            RobotLog.vv(TAG, "auto-rebrightening for connected state");
            enableAndBrightenForConnected(controlPanelBack2);
            setClientConnected(true);
            requestUIState();
        }
    }

    /* access modifiers changed from: protected */
    public void uiWaitingForOpModeSelection() {
        traceUiStateChange("ui:uiWaitingForOpModeSelection", UIState.WAITING_FOR_OPMODE_SELECTION);
        checkConnectedEnableBrighten(ControlPanelBack.DIM);
        dimControlPanelBack();
        enableAndBrightenOpModeMenu();
        showQueuedOpModeName();
        disableOpModeControls();
    }

    /* access modifiers changed from: protected */
    public void uiWaitingForInitEvent() {
        traceUiStateChange("ui:uiWaitingForInitEvent", UIState.WAITING_FOR_INIT_EVENT);
        checkConnectedEnableBrighten(ControlPanelBack.BRIGHT);
        brightenControlPanelBack();
        showQueuedOpModeName();
        enableAndBrightenOpModeMenu();
        setEnabled(this.buttonInit, true);
        setVisibility(this.buttonInit, 0);
        setVisibility(this.buttonStart, 4);
        setVisibility(this.buttonStop, 4);
        setVisibility(this.buttonInitStop, 4);
        setTimerButtonEnabled(true);
        setVisibility(this.timerAndTimerSwitch, 0);
        hideCameraStream();
    }

    /* access modifiers changed from: protected */
    public void setTimerButtonEnabled(boolean z) {
        setEnabled(this.timerAndTimerSwitch, z);
        setEnabled(findViewById(R.id.timerBackground), z);
        setEnabled(findViewById(R.id.timerStopWatch), z);
        setEnabled(findViewById(R.id.timerText), z);
        setEnabled(findViewById(R.id.timerSwitchOn), z);
        setEnabled(findViewById(R.id.timerSwitchOff), z);
    }

    /* access modifiers changed from: protected */
    public void uiWaitingForStartEvent() {
        traceUiStateChange("ui:uiWaitingForStartEvent", UIState.WAITING_FOR_START_EVENT);
        checkConnectedEnableBrighten(ControlPanelBack.BRIGHT);
        showQueuedOpModeName();
        enableAndBrightenOpModeMenu();
        setVisibility(this.buttonStart, 0);
        setVisibility(this.buttonInit, 4);
        setVisibility(this.buttonStop, 4);
        setVisibility(this.buttonInitStop, 0);
        setTimerButtonEnabled(true);
        setVisibility(this.timerAndTimerSwitch, 0);
        hideCameraStream();
    }

    /* access modifiers changed from: protected */
    public void uiWaitingForStopEvent() {
        traceUiStateChange("ui:uiWaitingForStopEvent", UIState.WAITING_FOR_STOP_EVENT);
        checkConnectedEnableBrighten(ControlPanelBack.BRIGHT);
        showQueuedOpModeName();
        enableAndBrightenOpModeMenu();
        setVisibility(this.buttonStop, 0);
        setVisibility(this.buttonInit, 4);
        setVisibility(this.buttonStart, 4);
        setVisibility(this.buttonInitStop, 4);
        setTimerButtonEnabled(false);
        setVisibility(this.timerAndTimerSwitch, 0);
        hideCameraStream();
    }

    /* access modifiers changed from: protected */
    public boolean isDefaultOpMode(String str) {
        return this.defaultOpMode.name.equals(str);
    }

    /* access modifiers changed from: protected */
    public boolean isDefaultOpMode(OpModeMeta opModeMeta) {
        return isDefaultOpMode(opModeMeta.name);
    }

    /* access modifiers changed from: protected */
    public OpModeMeta getOpModeMeta(String str) {
        synchronized (this.opModes) {
            for (OpModeMeta next : this.opModes) {
                if (next.name.equals(str)) {
                    return next;
                }
            }
            return new OpModeMeta(str);
        }
    }

    /* access modifiers changed from: protected */
    public void showQueuedOpModeName() {
        showQueuedOpModeName(this.queuedOpMode);
    }

    /* access modifiers changed from: protected */
    public void showQueuedOpModeName(OpModeMeta opModeMeta) {
        if (isDefaultOpMode(opModeMeta)) {
            setVisibility(this.currentOpModeName, 8);
            setVisibility(this.chooseOpModePrompt, 0);
            return;
        }
        setTextView(this.currentOpModeName, opModeMeta.name);
        setVisibility(this.currentOpModeName, 0);
        setVisibility(this.chooseOpModePrompt, 8);
    }

    /* access modifiers changed from: protected */
    public void traceUiStateChange(String str, UIState uIState) {
        RobotLog.vv(TAG, str);
        this.uiState = uIState;
        setTextView(this.textDsUiStateIndicator, uIState.indicator);
        invalidateOptionsMenu();
    }

    /* access modifiers changed from: protected */
    public void assumeClientConnectAndRefreshUI(ControlPanelBack controlPanelBack2) {
        assumeClientConnect(controlPanelBack2);
        requestUIState();
    }

    /* access modifiers changed from: protected */
    public void assumeClientConnect(ControlPanelBack controlPanelBack2) {
        RobotLog.vv(TAG, "Assuming client connected");
        if (this.uiState == UIState.UNKNOWN || this.uiState == UIState.DISCONNECTED || this.uiState == UIState.CANT_CONTINUE) {
            setClientConnected(true);
            uiRobotControllerIsConnected(controlPanelBack2);
        }
    }

    /* access modifiers changed from: protected */
    public void assumeClientDisconnect() {
        RobotLog.vv(TAG, "Assuming client disconnected");
        setClientConnected(false);
        enableAndResetTimer(false);
        this.opModeCountDown.disable();
        this.queuedOpMode = this.defaultOpMode;
        this.opModes.clear();
        pingStatus((int) R.string.ping_status_no_heartbeat);
        stopKeepAlives();
        this.networkConnectionHandler.clientDisconnect();
        RobocolParsableBase.initializeSequenceNumber(10000);
        RobotLog.clearGlobalErrorMsg();
        setRobotState(RobotState.UNKNOWN);
        uiRobotControllerIsDisconnected();
    }

    /* access modifiers changed from: protected */
    public boolean setClientConnected(boolean z) {
        boolean z2 = this.clientConnected;
        this.clientConnected = z;
        this.preferencesHelper.writeBooleanPrefIfDifferent(getString(R.string.pref_rc_connected), z);
        return z2;
    }

    /* access modifiers changed from: protected */
    public void handleOpModeQueued(OpModeMeta opModeMeta) {
        if (setQueuedOpModeIfDifferent(opModeMeta)) {
            enableAndResetTimerForQueued();
        }
        uiWaitingForInitEvent();
    }

    /* access modifiers changed from: protected */
    public boolean setQueuedOpModeIfDifferent(String str) {
        return setQueuedOpModeIfDifferent(getOpModeMeta(str));
    }

    /* access modifiers changed from: protected */
    public boolean setQueuedOpModeIfDifferent(OpModeMeta opModeMeta) {
        if (opModeMeta.name.equals(this.queuedOpMode.name)) {
            return false;
        }
        this.queuedOpMode = opModeMeta;
        showQueuedOpModeName();
        return true;
    }

    /* access modifiers changed from: protected */
    public int validateMatchEntry(String str) {
        try {
            int parseInt = Integer.parseInt(str);
            if (parseInt < 0 || parseInt > 1000) {
                return -1;
            }
            return parseInt;
        } catch (NumberFormatException e) {
            RobotLog.logStackTrace(e);
            return -1;
        }
    }

    /* access modifiers changed from: protected */
    public void sendMatchNumber(String str) {
        this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_SET_MATCH_NUMBER, str));
    }

    /* access modifiers changed from: protected */
    public void sendMatchNumber(int i) {
        sendMatchNumber(String.valueOf(i));
    }

    /* access modifiers changed from: protected */
    public void sendMatchNumberIfNecessary() {
        try {
            sendMatchNumber(getMatchNumber());
        } catch (NumberFormatException unused) {
            sendMatchNumber(0);
        }
    }

    /* access modifiers changed from: protected */
    public void clearMatchNumberIfNecessary() {
        if (this.queuedOpMode.flavor == OpModeMeta.Flavor.TELEOP) {
            clearMatchNumber();
        }
    }

    /* access modifiers changed from: protected */
    public void handleOpModeInit() {
        if (this.uiState == UIState.WAITING_FOR_INIT_EVENT) {
            traceUiStateChange("ui:uiWaitingForAck", UIState.WAITING_FOR_ACK);
            sendMatchNumberIfNecessary();
            this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_INIT_OP_MODE, this.queuedOpMode.name));
            if (!this.queuedOpMode.name.equals(this.defaultOpMode.name)) {
                this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.RUNNING_OPMODE);
            }
            hideCameraStream();
        }
    }

    /* access modifiers changed from: protected */
    public void handleOpModeStart() {
        if (this.uiState == UIState.WAITING_FOR_START_EVENT) {
            traceUiStateChange("ui:uiWaitingForAck", UIState.WAITING_FOR_ACK);
            this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_RUN_OP_MODE, this.queuedOpMode.name));
        }
    }

    /* access modifiers changed from: protected */
    public void handleOpModeStop() {
        if (this.uiState == UIState.WAITING_FOR_START_EVENT || this.uiState == UIState.WAITING_FOR_STOP_EVENT) {
            traceUiStateChange("ui:uiWaitingForAck", UIState.WAITING_FOR_ACK);
            clearMatchNumberIfNecessary();
            initDefaultOpMode();
            this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.STOPPED_OPMODE);
        }
    }

    /* access modifiers changed from: protected */
    public void initDefaultOpMode() {
        this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_INIT_OP_MODE, this.defaultOpMode.name));
    }

    /* access modifiers changed from: protected */
    public void runDefaultOpMode() {
        this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_RUN_OP_MODE, this.defaultOpMode.name));
        this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.STOPPED_OPMODE);
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandNotifyInitOpMode(String str) {
        if (this.uiState == UIState.CANT_CONTINUE) {
            return CallbackResult.HANDLED;
        }
        RobotLog.vv(TAG, "Robot Controller initializing op mode: " + str);
        stopTimerPreservingRemainingTime();
        if (isDefaultOpMode(str)) {
            this.androidTextToSpeech.stop();
            stopKeepAlives();
            runOnUiThread(new Runnable() {
                public void run() {
                    FtcDriverStationActivityBase.this.telemetryMode = Telemetry.DisplayFormat.CLASSIC;
                    FtcDriverStationActivityBase.this.textTelemetry.setTypeface(Typeface.DEFAULT);
                }
            });
            handleDefaultOpModeInitOrStart(false);
        } else {
            clearUserTelemetry();
            startKeepAlives();
            if (setQueuedOpModeIfDifferent(str)) {
                RobotLog.vv(TAG, "timer: init new opmode");
                enableAndResetTimerForQueued();
            } else if (this.opModeCountDown.isEnabled()) {
                RobotLog.vv(TAG, "timer: init w/ timer enabled");
                this.opModeCountDown.resetCountdown();
            } else {
                RobotLog.vv(TAG, "timer: init w/o timer enabled");
            }
            uiWaitingForStartEvent();
        }
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandNotifyStartOpMode(String str) {
        if (this.uiState == UIState.CANT_CONTINUE) {
            return CallbackResult.HANDLED;
        }
        RobotLog.vv(TAG, "Robot Controller starting op mode: " + str);
        if (isDefaultOpMode(str)) {
            this.androidTextToSpeech.stop();
            stopKeepAlives();
            handleDefaultOpModeInitOrStart(true);
        } else {
            if (setQueuedOpModeIfDifferent(str)) {
                RobotLog.vv(TAG, "timer: started new opmode: auto-initing timer");
                enableAndResetTimerForQueued();
            }
            uiWaitingForStopEvent();
            if (this.opModeUseTimer) {
                this.opModeCountDown.start();
            } else {
                stopTimerAndReset();
            }
        }
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public void handleDefaultOpModeInitOrStart(boolean z) {
        if (isDefaultOpMode(this.queuedOpMode)) {
            uiWaitingForOpModeSelection();
            return;
        }
        uiWaitingForInitEvent();
        if (!z) {
            runDefaultOpMode();
        }
    }

    /* access modifiers changed from: protected */
    public void requestUIState() {
        this.networkConnectionHandler.sendCommand(new Command(RobotCoreCommandList.CMD_REQUEST_UI_STATE));
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandNotifyOpModeList(String str) {
        assumeClientConnect(ControlPanelBack.NO_CHANGE);
        this.opModes = (List) new Gson().fromJson(str, new TypeToken<Collection<OpModeMeta>>() {
        }.getType());
        RobotLog.vv(TAG, "Received the following op modes: " + this.opModes.toString());
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandNotifyUserDeviceList(String str) {
        ConfigurationTypeManager.getInstance().deserializeUserDeviceTypes(str);
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandNotifyActiveConfig(String str) {
        RobotLog.vv(TAG, "%s.handleCommandRequestActiveConfigResp(%s)", getClass().getSimpleName(), str);
        final RobotConfigFile configFromString = this.robotConfigFileManager.getConfigFromString(str);
        this.robotConfigFileManager.setActiveConfig(configFromString);
        this.appUtil.runOnUiThread(this, new Runnable() {
            public void run() {
                FtcDriverStationActivityBase.this.activeConfigText.setText(configFromString.getName());
            }
        });
        return CallbackResult.HANDLED_CONTINUE;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandShowToast(String str) {
        RobotCoreCommandList.ShowToast deserialize = RobotCoreCommandList.ShowToast.deserialize(str);
        this.appUtil.showToast(UILocation.ONLY_LOCAL, deserialize.message, deserialize.duration);
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandShowProgress(String str) {
        RobotCoreCommandList.ShowProgress deserialize = RobotCoreCommandList.ShowProgress.deserialize(str);
        this.appUtil.showProgress(UILocation.ONLY_LOCAL, deserialize.message, (ProgressParameters) deserialize);
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandDismissProgress() {
        this.appUtil.dismissProgress(UILocation.ONLY_LOCAL);
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandShowDialog(String str) {
        RobotCoreCommandList.ShowDialog deserialize = RobotCoreCommandList.ShowDialog.deserialize(str);
        AppUtil.DialogParams dialogParams = new AppUtil.DialogParams(UILocation.ONLY_LOCAL, deserialize.title, deserialize.message);
        dialogParams.uuidString = deserialize.uuidString;
        this.appUtil.showDialog(dialogParams);
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandDismissDialog(Command command) {
        this.appUtil.dismissDialog(UILocation.ONLY_LOCAL, RobotCoreCommandList.DismissDialog.deserialize(command.getExtra()));
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandDismissAllDialogs(Command command) {
        this.appUtil.dismissAllDialogs(UILocation.ONLY_LOCAL);
        return CallbackResult.HANDLED;
    }

    private CallbackResult handleCommandStartProgramAndManageResp(String str) {
        if (str != null && !str.isEmpty()) {
            Intent intent = new Intent(AppUtil.getDefContext(), ProgramAndManageActivity.class);
            intent.putExtra(LaunchActivityConstantsList.RC_WEB_INFO, str);
            startActivityForResult(intent, LaunchActivityConstantsList.RequestCode.PROGRAM_AND_MANAGE.ordinal());
        }
        return CallbackResult.HANDLED;
    }

    private CallbackResult handleCommandSetTelemetryDisplayFormat(String str) {
        try {
            Telemetry.DisplayFormat valueOf = Telemetry.DisplayFormat.valueOf(str);
            if (valueOf != this.telemetryMode) {
                int i = AnonymousClass24.$SwitchMap$org$firstinspires$ftc$robotcore$external$Telemetry$DisplayFormat[valueOf.ordinal()];
                if (i == 1) {
                    this.textTelemetry.setTypeface(Typeface.MONOSPACE);
                } else if (i == 2 || i == 3) {
                    this.textTelemetry.setTypeface(Typeface.DEFAULT);
                }
            }
            this.telemetryMode = valueOf;
        } catch (IllegalArgumentException unused) {
        }
        return CallbackResult.HANDLED;
    }

    private CallbackResult handleCommandTextToSpeech(String str) {
        RobotCoreCommandList.TextToSpeech deserialize = RobotCoreCommandList.TextToSpeech.deserialize(str);
        String text = deserialize.getText();
        String languageCode = deserialize.getLanguageCode();
        String countryCode = deserialize.getCountryCode();
        if (languageCode != null && !languageCode.isEmpty()) {
            if (countryCode == null || countryCode.isEmpty()) {
                this.androidTextToSpeech.setLanguage(languageCode);
            } else {
                this.androidTextToSpeech.setLanguageAndCountry(languageCode, countryCode);
            }
        }
        this.androidTextToSpeech.speak(text);
        return CallbackResult.HANDLED;
    }

    public void onClickRCBatteryToast(View view) {
        showToast(getString(R.string.toastRobotControllerBattery));
    }

    public void onClickRobotBatteryToast(View view) {
        resetBatteryStats();
        showToast(getString(R.string.toastRobotBattery));
    }

    public void onClickDSBatteryToast(View view) {
        showToast(getString(R.string.toastDriverStationBattery));
    }

    /* access modifiers changed from: protected */
    public void showWifiStatus(final boolean z, final String str) {
        runOnUiThread(new Runnable() {
            public void run() {
                FtcDriverStationActivityBase.this.textWifiDirectStatusShowingRC = z;
                FtcDriverStationActivityBase.this.textWifiDirectStatus.setText(str);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void showWifiChannel() {
        runOnUiThread(new Runnable() {
            public void run() {
                if (FtcDriverStationActivityBase.this.networkConnectionHandler.getWifiChannel() > 0) {
                    FtcDriverStationActivityBase.this.textWifiChannel.setText("ch " + FtcDriverStationActivityBase.this.networkConnectionHandler.getWifiChannel());
                    FtcDriverStationActivityBase.this.textWifiChannel.setVisibility(0);
                    return;
                }
                int i = FtcDriverStationActivityBase.this.preferences.getInt(FtcDriverStationActivityBase.this.getString(R.string.pref_wifip2p_channel), -1);
                if (i == -1) {
                    RobotLog.vv(FtcDriverStationActivityBase.TAG, "pref_wifip2p_channel: showWifiChannel prefChannel not found");
                    FtcDriverStationActivityBase.this.textWifiChannel.setVisibility(8);
                    return;
                }
                RobotLog.vv(FtcDriverStationActivityBase.TAG, "pref_wifip2p_channel: showWifiChannel prefChannel = %d", Integer.valueOf(i));
                FtcDriverStationActivityBase.this.textWifiChannel.setText("ch " + Integer.toString(i));
                FtcDriverStationActivityBase.this.textWifiChannel.setVisibility(0);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void showBytesPerSecond(final long j) {
        runOnUiThread(new Runnable() {
            public void run() {
                FtcDriverStationActivityBase.this.textBytesPerSecond.setText(String.valueOf(j));
            }
        });
    }

    protected class DeviceNameManagerCallback implements DeviceNameListener {
        protected DeviceNameManagerCallback() {
        }

        public void onDeviceNameChanged(String str) {
            FtcDriverStationActivityBase.this.displayDeviceName(str);
        }
    }

    /* access modifiers changed from: protected */
    public void displayDeviceName(final String str) {
        runOnUiThread(new Runnable() {
            public void run() {
                FtcDriverStationActivityBase.this.textDeviceName.setText(str);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void assertUiThread() {
        Assert.assertTrue(Thread.currentThread() == this.uiThread);
    }

    /* access modifiers changed from: protected */
    public void setButtonText(final Button button, final String str) {
        runOnUiThread(new Runnable() {
            public void run() {
                button.setText(str);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setTextView(final TextView textView, final CharSequence charSequence) {
        runOnUiThread(new Runnable() {
            public void run() {
                textView.setText(charSequence);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setTextColor(final TextView textView, final int i) {
        runOnUiThread(new Runnable() {
            public void run() {
                textView.setTextColor(i);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setOpacity(final View view, final float f) {
        runOnUiThread(new Runnable() {
            public void run() {
                view.setAlpha(f);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setImageResource(final ImageButton imageButton, final int i) {
        runOnUiThread(new Runnable() {
            public void run() {
                imageButton.setImageResource(i);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setVisibility(final View view, final int i) {
        runOnUiThread(new Runnable() {
            public void run() {
                view.setVisibility(i);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setEnabled(final View view, final boolean z) {
        runOnUiThread(new Runnable() {
            public void run() {
                view.setEnabled(z);
            }
        });
    }

    /* renamed from: com.qualcomm.ftcdriverstation.FtcDriverStationActivityBase$24  reason: invalid class name */
    static /* synthetic */ class AnonymousClass24 {
        static final /* synthetic */ int[] $SwitchMap$com$qualcomm$ftcdriverstation$FtcDriverStationActivityBase$ControlPanelBack;
        static final /* synthetic */ int[] $SwitchMap$com$qualcomm$robotcore$wifi$NetworkConnection$NetworkEvent;
        static final /* synthetic */ int[] $SwitchMap$org$firstinspires$ftc$robotcore$external$Telemetry$DisplayFormat;

        /* JADX WARNING: Can't wrap try/catch for region: R(28:0|(2:1|2)|3|(2:5|6)|7|9|10|11|13|14|15|16|17|18|19|21|22|23|24|25|26|27|28|29|30|31|32|(3:33|34|36)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(29:0|1|2|3|(2:5|6)|7|9|10|11|13|14|15|16|17|18|19|21|22|23|24|25|26|27|28|29|30|31|32|(3:33|34|36)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(32:0|1|2|3|5|6|7|9|10|11|13|14|15|16|17|18|19|21|22|23|24|25|26|27|28|29|30|31|32|33|34|36) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0039 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0043 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x005e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0068 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x0072 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x007d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x0088 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:33:0x0093 */
        static {
            /*
                com.qualcomm.ftcdriverstation.FtcDriverStationActivityBase$ControlPanelBack[] r0 = com.qualcomm.ftcdriverstation.FtcDriverStationActivityBase.ControlPanelBack.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$qualcomm$ftcdriverstation$FtcDriverStationActivityBase$ControlPanelBack = r0
                r1 = 1
                com.qualcomm.ftcdriverstation.FtcDriverStationActivityBase$ControlPanelBack r2 = com.qualcomm.ftcdriverstation.FtcDriverStationActivityBase.ControlPanelBack.NO_CHANGE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$qualcomm$ftcdriverstation$FtcDriverStationActivityBase$ControlPanelBack     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.ftcdriverstation.FtcDriverStationActivityBase$ControlPanelBack r3 = com.qualcomm.ftcdriverstation.FtcDriverStationActivityBase.ControlPanelBack.DIM     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                r2 = 3
                int[] r3 = $SwitchMap$com$qualcomm$ftcdriverstation$FtcDriverStationActivityBase$ControlPanelBack     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.ftcdriverstation.FtcDriverStationActivityBase$ControlPanelBack r4 = com.qualcomm.ftcdriverstation.FtcDriverStationActivityBase.ControlPanelBack.BRIGHT     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                org.firstinspires.ftc.robotcore.external.Telemetry$DisplayFormat[] r3 = org.firstinspires.ftc.robotcore.external.Telemetry.DisplayFormat.values()
                int r3 = r3.length
                int[] r3 = new int[r3]
                $SwitchMap$org$firstinspires$ftc$robotcore$external$Telemetry$DisplayFormat = r3
                org.firstinspires.ftc.robotcore.external.Telemetry$DisplayFormat r4 = org.firstinspires.ftc.robotcore.external.Telemetry.DisplayFormat.MONOSPACE     // Catch:{ NoSuchFieldError -> 0x0039 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0039 }
                r3[r4] = r1     // Catch:{ NoSuchFieldError -> 0x0039 }
            L_0x0039:
                int[] r3 = $SwitchMap$org$firstinspires$ftc$robotcore$external$Telemetry$DisplayFormat     // Catch:{ NoSuchFieldError -> 0x0043 }
                org.firstinspires.ftc.robotcore.external.Telemetry$DisplayFormat r4 = org.firstinspires.ftc.robotcore.external.Telemetry.DisplayFormat.CLASSIC     // Catch:{ NoSuchFieldError -> 0x0043 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0043 }
                r3[r4] = r0     // Catch:{ NoSuchFieldError -> 0x0043 }
            L_0x0043:
                int[] r3 = $SwitchMap$org$firstinspires$ftc$robotcore$external$Telemetry$DisplayFormat     // Catch:{ NoSuchFieldError -> 0x004d }
                org.firstinspires.ftc.robotcore.external.Telemetry$DisplayFormat r4 = org.firstinspires.ftc.robotcore.external.Telemetry.DisplayFormat.HTML     // Catch:{ NoSuchFieldError -> 0x004d }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x004d }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x004d }
            L_0x004d:
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent[] r3 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.values()
                int r3 = r3.length
                int[] r3 = new int[r3]
                $SwitchMap$com$qualcomm$robotcore$wifi$NetworkConnection$NetworkEvent = r3
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r4 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.PEERS_AVAILABLE     // Catch:{ NoSuchFieldError -> 0x005e }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x005e }
                r3[r4] = r1     // Catch:{ NoSuchFieldError -> 0x005e }
            L_0x005e:
                int[] r1 = $SwitchMap$com$qualcomm$robotcore$wifi$NetworkConnection$NetworkEvent     // Catch:{ NoSuchFieldError -> 0x0068 }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r3 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.CONNECTED_AS_GROUP_OWNER     // Catch:{ NoSuchFieldError -> 0x0068 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0068 }
                r1[r3] = r0     // Catch:{ NoSuchFieldError -> 0x0068 }
            L_0x0068:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$wifi$NetworkConnection$NetworkEvent     // Catch:{ NoSuchFieldError -> 0x0072 }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.CONNECTING     // Catch:{ NoSuchFieldError -> 0x0072 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0072 }
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0072 }
            L_0x0072:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$wifi$NetworkConnection$NetworkEvent     // Catch:{ NoSuchFieldError -> 0x007d }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.CONNECTED_AS_PEER     // Catch:{ NoSuchFieldError -> 0x007d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x007d }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x007d }
            L_0x007d:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$wifi$NetworkConnection$NetworkEvent     // Catch:{ NoSuchFieldError -> 0x0088 }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.CONNECTION_INFO_AVAILABLE     // Catch:{ NoSuchFieldError -> 0x0088 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0088 }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0088 }
            L_0x0088:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$wifi$NetworkConnection$NetworkEvent     // Catch:{ NoSuchFieldError -> 0x0093 }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.DISCONNECTED     // Catch:{ NoSuchFieldError -> 0x0093 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0093 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0093 }
            L_0x0093:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$wifi$NetworkConnection$NetworkEvent     // Catch:{ NoSuchFieldError -> 0x009e }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.ERROR     // Catch:{ NoSuchFieldError -> 0x009e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009e }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009e }
            L_0x009e:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.ftcdriverstation.FtcDriverStationActivityBase.AnonymousClass24.<clinit>():void");
        }
    }

    /* access modifiers changed from: protected */
    public void setControlPanelBack(ControlPanelBack controlPanelBack2) {
        int i = AnonymousClass24.$SwitchMap$com$qualcomm$ftcdriverstation$FtcDriverStationActivityBase$ControlPanelBack[controlPanelBack2.ordinal()];
        if (i == 2) {
            dimControlPanelBack();
        } else if (i == 3) {
            brightenControlPanelBack();
        }
    }

    /* access modifiers changed from: protected */
    public void dimControlPanelBack() {
        setOpacity(this.controlPanelBack, PARTLY_OPAQUE);
    }

    /* access modifiers changed from: protected */
    public void brightenControlPanelBack() {
        setOpacity(this.controlPanelBack, 1.0f);
    }

    /* access modifiers changed from: protected */
    public void disableAndDimOpModeMenu() {
        disableAndDim(this.buttonAutonomous);
        disableAndDim(this.buttonTeleOp);
        disableAndDim(this.currentOpModeName);
        disableAndDim(this.chooseOpModePrompt);
    }

    /* access modifiers changed from: protected */
    public void enableAndBrightenOpModeMenu() {
        enableAndBrighten(this.buttonAutonomous);
        enableAndBrighten(this.buttonTeleOp);
        setOpacity(this.currentOpModeName, 1.0f);
        setOpacity(this.chooseOpModePrompt, 1.0f);
    }

    /* access modifiers changed from: protected */
    public void disableAndDim(View view) {
        setOpacity(view, PARTLY_OPAQUE);
        setEnabled(view, false);
    }

    /* access modifiers changed from: protected */
    public void enableAndBrighten(View view) {
        setOpacity(view, 1.0f);
        setEnabled(view, true);
    }

    /* access modifiers changed from: protected */
    public void pingStatus(int i) {
        pingStatus(this.context.getString(i));
    }

    /* access modifiers changed from: protected */
    public void startKeepAlives() {
        NetworkConnectionHandler networkConnectionHandler2 = this.networkConnectionHandler;
        if (networkConnectionHandler2 != null) {
            networkConnectionHandler2.startKeepAlives();
        }
    }

    /* access modifiers changed from: protected */
    public void stopKeepAlives() {
        NetworkConnectionHandler networkConnectionHandler2 = this.networkConnectionHandler;
        if (networkConnectionHandler2 != null) {
            networkConnectionHandler2.stopKeepAlives();
        }
    }

    public void onUserInteraction() {
        if (this.processUserActivity) {
            this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.USER_ACTIVITY);
        }
    }

    public void startActivityForResult(Intent intent, int i, Bundle bundle) {
        this.disconnectFromPeerOnActivityStop = false;
        super.startActivityForResult(intent, i, bundle);
    }

    public void startActivity(Intent intent, Bundle bundle) {
        this.disconnectFromPeerOnActivityStop = false;
        super.startActivity(intent, bundle);
    }
}
