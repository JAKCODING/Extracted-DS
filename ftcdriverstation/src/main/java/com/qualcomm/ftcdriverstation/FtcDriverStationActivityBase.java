package com.qualcomm.ftcdriverstation;

import android.app.ActivityManager;
import android.app.ActivityManager.AppTask;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.PorterDuff.Mode;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.PopupMenu.OnMenuItemClickListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qualcomm.ftccommon.ClassManagerFactory;
import com.qualcomm.ftccommon.ConfigWifiDirectActivity;
import com.qualcomm.ftccommon.FtcAboutActivity;
import com.qualcomm.ftccommon.LaunchActivityConstantsList;
import com.qualcomm.ftccommon.SoundPlayer;
import com.qualcomm.ftccommon.configuration.EditParameters;
import com.qualcomm.ftccommon.configuration.FtcLoadFileActivity;
import com.qualcomm.ftccommon.configuration.RobotConfigFile;
import com.qualcomm.ftccommon.configuration.RobotConfigFileManager;
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
import java.util.Iterator;
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
import org.firstinspires.ftc.robotcore.internal.ui.ThemedActivity;
import org.firstinspires.ftc.robotcore.internal.ui.UILocation;

public abstract class FtcDriverStationActivityBase extends ThemedActivity implements NetworkConnection.NetworkConnectionCallback, RecvLoopRunnable.RecvLoopCallback, OnSharedPreferenceChangeListener, OpModeSelectionDialogFragment.OpModeSelectionDialogListener, BatteryChecker.BatteryWatcher, PeerStatusCallback, WifiMuteStateMachine.Callback {
   protected static final float FULLY_OPAQUE = 1.0F;
   protected static final int MATCH_NUMBER_LOWER_BOUND = 0;
   protected static final int MATCH_NUMBER_UPPER_BOUND = 1000;
   protected static final float PARTLY_OPAQUE = 0.3F;
   public static final String TAG = "DriverStation";
   protected static final boolean debugBattery = false;
   protected static boolean permissionsValidated;
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
   protected FtcDriverStationActivityBase.DeviceNameManagerCallback deviceNameManagerCallback;
   protected StartResult deviceNameManagerStartResult;
   protected boolean disconnectFromPeerOnActivityStop;
   protected ImageView dsBatteryIcon;
   protected TextView dsBatteryInfo;
   protected Map gamepadIndicators = new HashMap();
   protected GamepadManager gamepadManager;
   protected Heartbeat heartbeatRecv = new Heartbeat();
   protected ImmersiveMode immersion;
   protected ElapsedTime lastUiUpdate;
   private InputManager mInputManager;
   protected NetworkConnectionHandler networkConnectionHandler;
   protected FtcDriverStationActivityBase.OpModeCountDownTimer opModeCountDown;
   protected boolean opModeUseTimer;
   protected List opModes;
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
   protected FtcDriverStationActivityBase.UIState uiState;
   protected Thread uiThread;
   protected Utility utility;
   protected View wifiInfo;
   protected WifiMuteStateMachine wifiMuteStateMachine;

   public FtcDriverStationActivityBase() {
      OpModeMeta var1 = new OpModeMeta("$Stop$Robot$");
      this.defaultOpMode = var1;
      this.queuedOpMode = var1;
      this.queuedOpModeWhenMuted = var1;
      this.opModes = new LinkedList();
      this.opModeUseTimer = false;
      this.pingAverage = new RollingAverage(10);
      this.lastUiUpdate = new ElapsedTime();
      this.uiState = FtcDriverStationActivityBase.UIState.UNKNOWN;
      this.telemetryMode = Telemetry.DisplayFormat.CLASSIC;
      this.debugLogging = false;
      this.networkConnectionHandler = NetworkConnectionHandler.getInstance();
      this.appUtil = AppUtil.getInstance();
      this.deviceNameManagerStartResult = new StartResult();
      this.prefRemoterStartResult = new StartResult();
      this.deviceNameManagerCallback = new FtcDriverStationActivityBase.DeviceNameManagerCallback();
      this.processUserActivity = false;
      this.disconnectFromPeerOnActivityStop = true;
      this.androidTextToSpeech = new AndroidTextToSpeech();
   }

   private void checkRcIndependentBattery(SharedPreferences var1) {
      this.rcHasIndependentBattery = var1.getBoolean(this.getString(2131624419), true);
   }

   private String getBestRobotControllerName() {
      return this.networkConnectionHandler.getConnectionOwnerName();
   }

   private CallbackResult handleCommandSetTelemetryDisplayFormat(String var1) {
      boolean var10001;
      Telemetry.DisplayFormat var7;
      label45: {
         int var2;
         try {
            var7 = Telemetry.DisplayFormat.valueOf(var1);
            if (var7 == this.telemetryMode) {
               break label45;
            }

            var2 = null.$SwitchMap$org$firstinspires$ftc$robotcore$external$Telemetry$DisplayFormat[var7.ordinal()];
         } catch (IllegalArgumentException var6) {
            var10001 = false;
            return CallbackResult.HANDLED;
         }

         if (var2 != 1) {
            if (var2 == 2 || var2 == 3) {
               try {
                  this.textTelemetry.setTypeface(Typeface.DEFAULT);
               } catch (IllegalArgumentException var5) {
                  var10001 = false;
                  return CallbackResult.HANDLED;
               }
            }
         } else {
            try {
               this.textTelemetry.setTypeface(Typeface.MONOSPACE);
            } catch (IllegalArgumentException var4) {
               var10001 = false;
               return CallbackResult.HANDLED;
            }
         }
      }

      try {
         this.telemetryMode = var7;
      } catch (IllegalArgumentException var3) {
         var10001 = false;
      }

      return CallbackResult.HANDLED;
   }

   private CallbackResult handleCommandStartProgramAndManageResp(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         Intent var2 = new Intent(AppUtil.getDefContext(), ProgramAndManageActivity.class);
         var2.putExtra("RC_WEB_INFO", var1);
         this.startActivityForResult(var2, LaunchActivityConstantsList.RequestCode.PROGRAM_AND_MANAGE.ordinal());
      }

      return CallbackResult.HANDLED;
   }

   private CallbackResult handleCommandTextToSpeech(String var1) {
      RobotCoreCommandList.TextToSpeech var2 = RobotCoreCommandList.TextToSpeech.deserialize(var1);
      String var3 = var2.getText();
      var1 = var2.getLanguageCode();
      String var4 = var2.getCountryCode();
      if (var1 != null && !var1.isEmpty()) {
         if (var4 != null && !var4.isEmpty()) {
            this.androidTextToSpeech.setLanguageAndCountry(var1, var4);
         } else {
            this.androidTextToSpeech.setLanguage(var1);
         }
      }

      this.androidTextToSpeech.speak(var3);
      return CallbackResult.HANDLED;
   }

   private void onPeersAvailableSoftAP() {
      if (this.networkConnectionHandler.connectionMatches(this.getString(2131624149))) {
         this.showWifiStatus(false, this.getString(2131624683));
      } else {
         this.showWifiStatus(false, this.getString(2131624684));
      }

      this.networkConnectionHandler.handlePeersAvailable();
   }

   private void onPeersAvailableWifiDirect() {
      if (!this.networkConnectionHandler.connectingOrConnected()) {
         this.onPeersAvailableSoftAP();
      }
   }

   public static void setPermissionsValidated() {
      permissionsValidated = true;
   }

   private void updateRcBatteryIndependence(SharedPreferences var1) {
      this.updateRcBatteryIndependence(var1, true);
   }

   private void updateRcBatteryIndependence(SharedPreferences var1, boolean var2) {
      this.checkRcIndependentBattery(var1);
      RobotLog.vv("DriverStation", "updateRcBatteryIndependence(%s)", this.rcHasIndependentBattery);
      if (var2) {
         this.displayRcBattery(this.rcHasIndependentBattery);
      }

   }

   protected void assertUiThread() {
      boolean var1;
      if (Thread.currentThread() == this.uiThread) {
         var1 = true;
      } else {
         var1 = false;
      }

      Assert.assertTrue(var1);
   }

   protected void assumeClientConnect(FtcDriverStationActivityBase.ControlPanelBack var1) {
      RobotLog.vv("DriverStation", "Assuming client connected");
      if (this.uiState == FtcDriverStationActivityBase.UIState.UNKNOWN || this.uiState == FtcDriverStationActivityBase.UIState.DISCONNECTED || this.uiState == FtcDriverStationActivityBase.UIState.CANT_CONTINUE) {
         this.setClientConnected(true);
         this.uiRobotControllerIsConnected(var1);
      }

   }

   protected void assumeClientConnectAndRefreshUI(FtcDriverStationActivityBase.ControlPanelBack var1) {
      this.assumeClientConnect(var1);
      this.requestUIState();
   }

   protected void assumeClientDisconnect() {
      RobotLog.vv("DriverStation", "Assuming client disconnected");
      this.setClientConnected(false);
      this.enableAndResetTimer(false);
      this.opModeCountDown.disable();
      this.queuedOpMode = this.defaultOpMode;
      this.opModes.clear();
      this.pingStatus(2131624391);
      this.stopKeepAlives();
      this.networkConnectionHandler.clientDisconnect();
      RobocolParsableBase.initializeSequenceNumber(10000);
      RobotLog.clearGlobalErrorMsg();
      this.setRobotState(RobotState.UNKNOWN);
      this.uiRobotControllerIsDisconnected();
   }

   protected void brightenControlPanelBack() {
      this.setOpacity(this.controlPanelBack, 1.0F);
   }

   protected void checkConnectedEnableBrighten(FtcDriverStationActivityBase.ControlPanelBack var1) {
      if (!this.clientConnected) {
         RobotLog.vv("DriverStation", "auto-rebrightening for connected state");
         this.enableAndBrightenForConnected(var1);
         this.setClientConnected(true);
         this.requestUIState();
      }

   }

   protected abstract void clearMatchNumber();

   protected void clearMatchNumberIfNecessary() {
      if (this.queuedOpMode.flavor == OpModeMeta.Flavor.TELEOP) {
         this.clearMatchNumber();
      }

   }

   protected void clearSystemTelemetry() {
      this.setVisibility(this.systemTelemetry, 8);
      this.setTextView(this.systemTelemetry, "");
      this.setTextColor(this.systemTelemetry, this.systemTelemetryOriginalColor);
      RobotLog.clearGlobalErrorMsg();
      RobotLog.clearGlobalWarningMsg();
   }

   protected void clearUserTelemetry() {
      this.setTextView(this.textTelemetry, "");
   }

   public CallbackResult commandEvent(Command var1) {
      CallbackResult var2 = CallbackResult.NOT_HANDLED;

      Exception var10000;
      CallbackResult var52;
      label431: {
         String var3;
         String var4;
         boolean var10001;
         try {
            var3 = var1.getName();
            var4 = var1.getExtra();
         } catch (Exception var51) {
            var10000 = var51;
            var10001 = false;
            break label431;
         }

         byte var5 = -1;

         label432: {
            label433: {
               label434: {
                  label435: {
                     label436: {
                        label437: {
                           label438: {
                              label439: {
                                 label440: {
                                    label441: {
                                       label442: {
                                          label443: {
                                             label444: {
                                                label445: {
                                                   label446: {
                                                      label447: {
                                                         label448: {
                                                            label449: {
                                                               label450: {
                                                                  label451: {
                                                                     label452: {
                                                                        label453: {
                                                                           try {
                                                                              switch(var3.hashCode()) {
                                                                              case -1530733715:
                                                                                 break label446;
                                                                              case -1121067382:
                                                                                 break label450;
                                                                              case -992356734:
                                                                                 break label452;
                                                                              case -939314969:
                                                                                 break label433;
                                                                              case -856964827:
                                                                                 break label437;
                                                                              case -362340438:
                                                                                 break label440;
                                                                              case -321815447:
                                                                                 break label443;
                                                                              case -206959740:
                                                                                 break label444;
                                                                              case -44710726:
                                                                                 break label448;
                                                                              case 78754538:
                                                                                 break label449;
                                                                              case 202444237:
                                                                                 break label453;
                                                                              case 323288778:
                                                                                 break label435;
                                                                              case 619130094:
                                                                                 break label436;
                                                                              case 739339659:
                                                                                 break label441;
                                                                              case 857479075:
                                                                                 break label442;
                                                                              case 899701436:
                                                                                 break label445;
                                                                              case 1332202628:
                                                                                 break label447;
                                                                              case 1506024019:
                                                                                 break label451;
                                                                              case 1509292278:
                                                                                 break;
                                                                              case 1510318778:
                                                                                 break label434;
                                                                              case 1661597945:
                                                                                 break label438;
                                                                              case 1852830809:
                                                                                 break label439;
                                                                              default:
                                                                                 break label432;
                                                                              }
                                                                           } catch (Exception var50) {
                                                                              var10000 = var50;
                                                                              var10001 = false;
                                                                              break label431;
                                                                           }

                                                                           try {
                                                                              if (!var3.equals("CMD_RECEIVE_FRAME_BEGIN")) {
                                                                                 break label432;
                                                                              }
                                                                           } catch (Exception var46) {
                                                                              var10000 = var46;
                                                                              var10001 = false;
                                                                              break label431;
                                                                           }

                                                                           var5 = 18;
                                                                           break label432;
                                                                        }

                                                                        try {
                                                                           if (!var3.equals("CMD_STOP_PLAYING_SOUNDS")) {
                                                                              break label432;
                                                                           }
                                                                        } catch (Exception var38) {
                                                                           var10000 = var38;
                                                                           var10001 = false;
                                                                           break label431;
                                                                        }

                                                                        var5 = 16;
                                                                        break label432;
                                                                     }

                                                                     try {
                                                                        if (!var3.equals("CMD_DISMISS_DIALOG")) {
                                                                           break label432;
                                                                        }
                                                                     } catch (Exception var30) {
                                                                        var10000 = var30;
                                                                        var10001 = false;
                                                                        break label431;
                                                                     }

                                                                     var5 = 10;
                                                                     break label432;
                                                                  }

                                                                  try {
                                                                     if (!var3.equals("CMD_DISMISS_ALL_DIALOGS")) {
                                                                        break label432;
                                                                     }
                                                                  } catch (Exception var45) {
                                                                     var10000 = var45;
                                                                     var10001 = false;
                                                                     break label431;
                                                                  }

                                                                  var5 = 11;
                                                                  break label432;
                                                               }

                                                               try {
                                                                  if (!var3.equals("CMD_SHOW_TOAST")) {
                                                                     break label432;
                                                                  }
                                                               } catch (Exception var29) {
                                                                  var10000 = var29;
                                                                  var10001 = false;
                                                                  break label431;
                                                               }

                                                               var5 = 6;
                                                               break label432;
                                                            }

                                                            try {
                                                               if (!var3.equals("CMD_STREAM_CHANGE")) {
                                                                  break label432;
                                                               }
                                                            } catch (Exception var37) {
                                                               var10000 = var37;
                                                               var10001 = false;
                                                               break label431;
                                                            }

                                                            var5 = 17;
                                                            break label432;
                                                         }

                                                         try {
                                                            if (!var3.equals("CMD_REQUEST_SOUND")) {
                                                               break label432;
                                                            }
                                                         } catch (Exception var36) {
                                                            var10000 = var36;
                                                            var10001 = false;
                                                            break label431;
                                                         }

                                                         var5 = 15;
                                                         break label432;
                                                      }

                                                      try {
                                                         if (!var3.equals("CMD_NOTIFY_USER_DEVICE_LIST")) {
                                                            break label432;
                                                         }
                                                      } catch (Exception var44) {
                                                         var10000 = var44;
                                                         var10001 = false;
                                                         break label431;
                                                      }

                                                      var5 = 2;
                                                      break label432;
                                                   }

                                                   try {
                                                      if (!var3.equals("CMD_NOTIFY_OP_MODE_LIST")) {
                                                         break label432;
                                                      }
                                                   } catch (Exception var28) {
                                                      var10000 = var28;
                                                      var10001 = false;
                                                      break label431;
                                                   }

                                                   var5 = 1;
                                                   break label432;
                                                }

                                                try {
                                                   if (!var3.equals("CMD_NOTIFY_RUN_OP_MODE")) {
                                                      break label432;
                                                   }
                                                } catch (Exception var43) {
                                                   var10000 = var43;
                                                   var10001 = false;
                                                   break label431;
                                                }

                                                var5 = 5;
                                                break label432;
                                             }

                                             try {
                                                if (!var3.equals("CMD_ROBOT_CONTROLLER_PREFERENCE")) {
                                                   break label432;
                                                }
                                             } catch (Exception var35) {
                                                var10000 = var35;
                                                var10001 = false;
                                                break label431;
                                             }

                                             var5 = 13;
                                             break label432;
                                          }

                                          try {
                                             if (!var3.equals("CMD_PLAY_SOUND")) {
                                                break label432;
                                             }
                                          } catch (Exception var34) {
                                             var10000 = var34;
                                             var10001 = false;
                                             break label431;
                                          }

                                          var5 = 14;
                                          break label432;
                                       }

                                       try {
                                          if (!var3.equals("CMD_NOTIFY_INIT_OP_MODE")) {
                                             break label432;
                                          }
                                       } catch (Exception var42) {
                                          var10000 = var42;
                                          var10001 = false;
                                          break label431;
                                       }

                                       var5 = 4;
                                       break label432;
                                    }

                                    try {
                                       if (!var3.equals("CMD_NOTIFY_ROBOT_STATE")) {
                                          break label432;
                                       }
                                    } catch (Exception var41) {
                                       var10000 = var41;
                                       var10001 = false;
                                       break label431;
                                    }

                                    var5 = 0;
                                    break label432;
                                 }

                                 try {
                                    if (!var3.equals("CMD_SET_TELEM_DISPL_FORMAT")) {
                                       break label432;
                                    }
                                 } catch (Exception var33) {
                                    var10000 = var33;
                                    var10001 = false;
                                    break label431;
                                 }

                                 var5 = 21;
                                 break label432;
                              }

                              try {
                                 if (!var3.equals("CMD_TEXT_TO_SPEECH")) {
                                    break label432;
                                 }
                              } catch (Exception var49) {
                                 var10000 = var49;
                                 var10001 = false;
                                 break label431;
                              }

                              var5 = 20;
                              break label432;
                           }

                           try {
                              if (!var3.equals("CMD_START_DS_PROGRAM_AND_MANAGE_RESP")) {
                                 break label432;
                              }
                           } catch (Exception var48) {
                              var10000 = var48;
                              var10001 = false;
                              break label431;
                           }

                           var5 = 12;
                           break label432;
                        }

                        try {
                           if (!var3.equals("CMD_SHOW_DIALOG")) {
                              break label432;
                           }
                        } catch (Exception var32) {
                           var10000 = var32;
                           var10001 = false;
                           break label431;
                        }

                        var5 = 9;
                        break label432;
                     }

                     try {
                        if (!var3.equals("CMD_NOTIFY_ACTIVE_CONFIGURATION")) {
                           break label432;
                        }
                     } catch (Exception var40) {
                        var10000 = var40;
                        var10001 = false;
                        break label431;
                     }

                     var5 = 3;
                     break label432;
                  }

                  try {
                     if (!var3.equals("CMD_SHOW_PROGRESS")) {
                        break label432;
                     }
                  } catch (Exception var39) {
                     var10000 = var39;
                     var10001 = false;
                     break label431;
                  }

                  var5 = 7;
                  break label432;
               }

               try {
                  if (!var3.equals("CMD_RECEIVE_FRAME_CHUNK")) {
                     break label432;
                  }
               } catch (Exception var47) {
                  var10000 = var47;
                  var10001 = false;
                  break label431;
               }

               var5 = 19;
               break label432;
            }

            try {
               if (!var3.equals("CMD_DISMISS_PROGRESS")) {
                  break label432;
               }
            } catch (Exception var31) {
               var10000 = var31;
               var10001 = false;
               break label431;
            }

            var5 = 8;
         }

         switch(var5) {
         case 0:
            try {
               var52 = this.handleNotifyRobotState(var4);
               return var52;
            } catch (Exception var27) {
               var10000 = var27;
               var10001 = false;
               break;
            }
         case 1:
            try {
               var52 = this.handleCommandNotifyOpModeList(var4);
               return var52;
            } catch (Exception var26) {
               var10000 = var26;
               var10001 = false;
               break;
            }
         case 2:
            try {
               var52 = this.handleCommandNotifyUserDeviceList(var4);
               return var52;
            } catch (Exception var25) {
               var10000 = var25;
               var10001 = false;
               break;
            }
         case 3:
            try {
               var52 = this.handleCommandNotifyActiveConfig(var4);
               return var52;
            } catch (Exception var24) {
               var10000 = var24;
               var10001 = false;
               break;
            }
         case 4:
            try {
               var52 = this.handleCommandNotifyInitOpMode(var4);
               return var52;
            } catch (Exception var23) {
               var10000 = var23;
               var10001 = false;
               break;
            }
         case 5:
            try {
               var52 = this.handleCommandNotifyStartOpMode(var4);
               return var52;
            } catch (Exception var22) {
               var10000 = var22;
               var10001 = false;
               break;
            }
         case 6:
            try {
               var52 = this.handleCommandShowToast(var4);
               return var52;
            } catch (Exception var21) {
               var10000 = var21;
               var10001 = false;
               break;
            }
         case 7:
            try {
               var52 = this.handleCommandShowProgress(var4);
               return var52;
            } catch (Exception var20) {
               var10000 = var20;
               var10001 = false;
               break;
            }
         case 8:
            try {
               var52 = this.handleCommandDismissProgress();
               return var52;
            } catch (Exception var19) {
               var10000 = var19;
               var10001 = false;
               break;
            }
         case 9:
            try {
               var52 = this.handleCommandShowDialog(var4);
               return var52;
            } catch (Exception var18) {
               var10000 = var18;
               var10001 = false;
               break;
            }
         case 10:
            try {
               var52 = this.handleCommandDismissDialog(var1);
               return var52;
            } catch (Exception var17) {
               var10000 = var17;
               var10001 = false;
               break;
            }
         case 11:
            try {
               var52 = this.handleCommandDismissAllDialogs(var1);
               return var52;
            } catch (Exception var16) {
               var10000 = var16;
               var10001 = false;
               break;
            }
         case 12:
            try {
               var52 = this.handleCommandStartProgramAndManageResp(var4);
               return var52;
            } catch (Exception var15) {
               var10000 = var15;
               var10001 = false;
               break;
            }
         case 13:
            try {
               var52 = PreferenceRemoterDS.getInstance().handleCommandRobotControllerPreference(var4);
               return var52;
            } catch (Exception var14) {
               var10000 = var14;
               var10001 = false;
               break;
            }
         case 14:
            try {
               var52 = SoundPlayer.getInstance().handleCommandPlaySound(var4);
               return var52;
            } catch (Exception var13) {
               var10000 = var13;
               var10001 = false;
               break;
            }
         case 15:
            try {
               var52 = SoundPlayer.getInstance().handleCommandRequestSound(var1);
               return var52;
            } catch (Exception var12) {
               var10000 = var12;
               var10001 = false;
               break;
            }
         case 16:
            try {
               var52 = SoundPlayer.getInstance().handleCommandStopPlayingSounds(var1);
               return var52;
            } catch (Exception var11) {
               var10000 = var11;
               var10001 = false;
               break;
            }
         case 17:
            try {
               var52 = CameraStreamClient.getInstance().handleStreamChange(var4);
               return var52;
            } catch (Exception var10) {
               var10000 = var10;
               var10001 = false;
               break;
            }
         case 18:
            try {
               var52 = CameraStreamClient.getInstance().handleReceiveFrameBegin(var4);
               return var52;
            } catch (Exception var9) {
               var10000 = var9;
               var10001 = false;
               break;
            }
         case 19:
            try {
               var52 = CameraStreamClient.getInstance().handleReceiveFrameChunk(var4);
               return var52;
            } catch (Exception var8) {
               var10000 = var8;
               var10001 = false;
               break;
            }
         case 20:
            try {
               var52 = this.handleCommandTextToSpeech(var4);
               return var52;
            } catch (Exception var7) {
               var10000 = var7;
               var10001 = false;
               break;
            }
         case 21:
            try {
               var52 = this.handleCommandSetTelemetryDisplayFormat(var4);
               return var52;
            } catch (Exception var6) {
               var10000 = var6;
               var10001 = false;
               break;
            }
         default:
            var52 = var2;
            return var52;
         }
      }

      Exception var53 = var10000;
      RobotLog.logStackTrace(var53);
      var52 = var2;
      return var52;
   }

   protected void dimAndDisableAllControls() {
      this.dimControlPanelBack();
      this.setOpacity(this.wifiInfo, 0.3F);
      this.setOpacity(this.batteryInfo, 0.3F);
      this.disableAndDimOpModeMenu();
      this.disableOpModeControls();
   }

   protected void dimControlPanelBack() {
      this.setOpacity(this.controlPanelBack, 0.3F);
   }

   protected void disableAndDim(View var1) {
      this.setOpacity(var1, 0.3F);
      this.setEnabled(var1, false);
   }

   protected void disableAndDimOpModeMenu() {
      this.disableAndDim(this.buttonAutonomous);
      this.disableAndDim(this.buttonTeleOp);
      this.disableAndDim(this.currentOpModeName);
      this.disableAndDim(this.chooseOpModePrompt);
   }

   protected abstract void disableMatchLoggingUI();

   protected void disableOpModeControls() {
      this.setEnabled(this.buttonInit, false);
      this.setVisibility(this.buttonInit, 0);
      this.setVisibility(this.buttonStart, 4);
      this.setVisibility(this.buttonStop, 4);
      this.setVisibility(this.buttonInitStop, 4);
      this.setVisibility(this.timerAndTimerSwitch, 4);
      this.hideCameraStream();
   }

   public boolean dispatchGenericMotionEvent(MotionEvent var1) {
      if (Gamepad.isGamepadDevice(var1.getDeviceId())) {
         this.gamepadManager.handleGamepadEvent(var1);
         return true;
      } else {
         return super.dispatchGenericMotionEvent(var1);
      }
   }

   public boolean dispatchKeyEvent(KeyEvent var1) {
      if (Gamepad.isGamepadDevice(var1.getDeviceId())) {
         this.gamepadManager.handleGamepadEvent(var1);
         return true;
      } else {
         return super.dispatchKeyEvent(var1);
      }
   }

   protected void displayDeviceName(final String var1) {
      this.runOnUiThread(new Runnable() {
         public void run() {
            FtcDriverStationActivityBase.this.textDeviceName.setText(var1);
         }
      });
   }

   protected void displayRcBattery(boolean var1) {
      View var2 = this.rcBatteryContainer;
      byte var3;
      if (var1) {
         var3 = 0;
      } else {
         var3 = 8;
      }

      var2.setVisibility(var3);
   }

   protected abstract void doMatchNumFieldBehaviorInit();

   public CallbackResult emptyEvent(RobocolDatagram var1) {
      return CallbackResult.NOT_HANDLED;
   }

   protected void enableAndBrighten(View var1) {
      this.setOpacity(var1, 1.0F);
      this.setEnabled(var1, true);
   }

   protected void enableAndBrightenForConnected(FtcDriverStationActivityBase.ControlPanelBack var1) {
      this.setControlPanelBack(var1);
      this.setOpacity(this.wifiInfo, 1.0F);
      this.setOpacity(this.batteryInfo, 1.0F);
      this.enableAndBrightenOpModeMenu();
   }

   protected void enableAndBrightenOpModeMenu() {
      this.enableAndBrighten(this.buttonAutonomous);
      this.enableAndBrighten(this.buttonTeleOp);
      this.setOpacity(this.currentOpModeName, 1.0F);
      this.setOpacity(this.chooseOpModePrompt, 1.0F);
   }

   protected void enableAndResetTimer(boolean var1) {
      if (!var1) {
         this.opModeCountDown.disable();
      } else {
         this.stopTimerAndReset();
         this.opModeCountDown.enable();
      }

      this.opModeUseTimer = var1;
   }

   protected void enableAndResetTimerForQueued() {
      boolean var1;
      if (this.queuedOpMode.flavor == OpModeMeta.Flavor.AUTONOMOUS) {
         var1 = true;
      } else {
         var1 = false;
      }

      this.enableAndResetTimer(var1);
   }

   protected abstract void enableMatchLoggingUI();

   protected void enforcePermissionValidator() {
      if (!permissionsValidated) {
         RobotLog.vv("DriverStation", "Redirecting to permission validator");
         this.startActivity(new Intent(AppUtil.getDefContext(), PermissionValidatorWrapper.class));
         this.finish();
      } else {
         RobotLog.vv("DriverStation", "Permissions validated already");
      }

   }

   protected List filterOpModes(Predicate var1) {
      LinkedList var2 = new LinkedList();
      Iterator var3 = this.opModes.iterator();

      while(var3.hasNext()) {
         OpModeMeta var4 = (OpModeMeta)var3.next();
         if (var1.test(var4)) {
            var2.add(var4);
         }
      }

      return var2;
   }

   public CallbackResult gamepadEvent(RobocolDatagram var1) {
      return CallbackResult.NOT_HANDLED;
   }

   protected abstract int getMatchNumber() throws NumberFormatException;

   protected OpModeMeta getOpModeMeta(String var1) {
      List var2 = this.opModes;
      synchronized(var2){}

      Throwable var10000;
      boolean var10001;
      label216: {
         Iterator var3;
         try {
            var3 = this.opModes.iterator();
         } catch (Throwable var24) {
            var10000 = var24;
            var10001 = false;
            break label216;
         }

         try {
            while(var3.hasNext()) {
               OpModeMeta var4 = (OpModeMeta)var3.next();
               if (var4.name.equals(var1)) {
                  return var4;
               }
            }
         } catch (Throwable var23) {
            var10000 = var23;
            var10001 = false;
            break label216;
         }

         label204:
         try {
            return new OpModeMeta(var1);
         } catch (Throwable var22) {
            var10000 = var22;
            var10001 = false;
            break label204;
         }
      }

      while(true) {
         Throwable var25 = var10000;

         try {
            throw var25;
         } catch (Throwable var21) {
            var10000 = var21;
            var10001 = false;
            continue;
         }
      }
   }

   public abstract View getPopupMenuAnchor();

   public String getTag() {
      return "DriverStation";
   }

   protected CallbackResult handleCommandDismissAllDialogs(Command var1) {
      this.appUtil.dismissAllDialogs(UILocation.ONLY_LOCAL);
      return CallbackResult.HANDLED;
   }

   protected CallbackResult handleCommandDismissDialog(Command var1) {
      this.appUtil.dismissDialog(UILocation.ONLY_LOCAL, RobotCoreCommandList.DismissDialog.deserialize(var1.getExtra()));
      return CallbackResult.HANDLED;
   }

   protected CallbackResult handleCommandDismissProgress() {
      this.appUtil.dismissProgress(UILocation.ONLY_LOCAL);
      return CallbackResult.HANDLED;
   }

   protected CallbackResult handleCommandNotifyActiveConfig(String var1) {
      RobotLog.vv("DriverStation", "%s.handleCommandRequestActiveConfigResp(%s)", this.getClass().getSimpleName(), var1);
      final RobotConfigFile var2 = this.robotConfigFileManager.getConfigFromString(var1);
      this.robotConfigFileManager.setActiveConfig(var2);
      this.appUtil.runOnUiThread(this, new Runnable() {
         public void run() {
            FtcDriverStationActivityBase.this.activeConfigText.setText(var2.getName());
         }
      });
      return CallbackResult.HANDLED_CONTINUE;
   }

   protected CallbackResult handleCommandNotifyInitOpMode(String var1) {
      if (this.uiState == FtcDriverStationActivityBase.UIState.CANT_CONTINUE) {
         return CallbackResult.HANDLED;
      } else {
         StringBuilder var2 = new StringBuilder();
         var2.append("Robot Controller initializing op mode: ");
         var2.append(var1);
         RobotLog.vv("DriverStation", var2.toString());
         this.stopTimerPreservingRemainingTime();
         if (this.isDefaultOpMode(var1)) {
            this.androidTextToSpeech.stop();
            this.stopKeepAlives();
            this.runOnUiThread(new Runnable() {
               public void run() {
                  FtcDriverStationActivityBase.this.telemetryMode = Telemetry.DisplayFormat.CLASSIC;
                  FtcDriverStationActivityBase.this.textTelemetry.setTypeface(Typeface.DEFAULT);
               }
            });
            this.handleDefaultOpModeInitOrStart(false);
         } else {
            this.clearUserTelemetry();
            this.startKeepAlives();
            if (this.setQueuedOpModeIfDifferent(var1)) {
               RobotLog.vv("DriverStation", "timer: init new opmode");
               this.enableAndResetTimerForQueued();
            } else if (this.opModeCountDown.isEnabled()) {
               RobotLog.vv("DriverStation", "timer: init w/ timer enabled");
               this.opModeCountDown.resetCountdown();
            } else {
               RobotLog.vv("DriverStation", "timer: init w/o timer enabled");
            }

            this.uiWaitingForStartEvent();
         }

         return CallbackResult.HANDLED;
      }
   }

   protected CallbackResult handleCommandNotifyOpModeList(String var1) {
      this.assumeClientConnect(FtcDriverStationActivityBase.ControlPanelBack.NO_CHANGE);
      this.opModes = (List)(new Gson()).fromJson(var1, (new TypeToken() {
      }).getType());
      StringBuilder var2 = new StringBuilder();
      var2.append("Received the following op modes: ");
      var2.append(this.opModes.toString());
      RobotLog.vv("DriverStation", var2.toString());
      return CallbackResult.HANDLED;
   }

   protected CallbackResult handleCommandNotifyStartOpMode(String var1) {
      if (this.uiState == FtcDriverStationActivityBase.UIState.CANT_CONTINUE) {
         return CallbackResult.HANDLED;
      } else {
         StringBuilder var2 = new StringBuilder();
         var2.append("Robot Controller starting op mode: ");
         var2.append(var1);
         RobotLog.vv("DriverStation", var2.toString());
         if (this.isDefaultOpMode(var1)) {
            this.androidTextToSpeech.stop();
            this.stopKeepAlives();
            this.handleDefaultOpModeInitOrStart(true);
         } else {
            if (this.setQueuedOpModeIfDifferent(var1)) {
               RobotLog.vv("DriverStation", "timer: started new opmode: auto-initing timer");
               this.enableAndResetTimerForQueued();
            }

            this.uiWaitingForStopEvent();
            if (this.opModeUseTimer) {
               this.opModeCountDown.start();
            } else {
               this.stopTimerAndReset();
            }
         }

         return CallbackResult.HANDLED;
      }
   }

   protected CallbackResult handleCommandNotifyUserDeviceList(String var1) {
      ConfigurationTypeManager.getInstance().deserializeUserDeviceTypes(var1);
      return CallbackResult.HANDLED;
   }

   protected CallbackResult handleCommandShowDialog(String var1) {
      RobotCoreCommandList.ShowDialog var3 = RobotCoreCommandList.ShowDialog.deserialize(var1);
      AppUtil.DialogParams var2 = new AppUtil.DialogParams(UILocation.ONLY_LOCAL, var3.title, var3.message);
      var2.uuidString = var3.uuidString;
      this.appUtil.showDialog(var2);
      return CallbackResult.HANDLED;
   }

   protected CallbackResult handleCommandShowProgress(String var1) {
      RobotCoreCommandList.ShowProgress var2 = RobotCoreCommandList.ShowProgress.deserialize(var1);
      this.appUtil.showProgress(UILocation.ONLY_LOCAL, var2.message, var2);
      return CallbackResult.HANDLED;
   }

   protected CallbackResult handleCommandShowToast(String var1) {
      RobotCoreCommandList.ShowToast var2 = RobotCoreCommandList.ShowToast.deserialize(var1);
      this.appUtil.showToast(UILocation.ONLY_LOCAL, var2.message, var2.duration);
      return CallbackResult.HANDLED;
   }

   protected void handleDefaultOpModeInitOrStart(boolean var1) {
      if (this.isDefaultOpMode(this.queuedOpMode)) {
         this.uiWaitingForOpModeSelection();
      } else {
         this.uiWaitingForInitEvent();
         if (!var1) {
            this.runDefaultOpMode();
         }
      }

   }

   protected CallbackResult handleNotifyRobotState(String var1) {
      this.setRobotState(RobotState.fromByte(Integer.valueOf(var1)));
      return CallbackResult.HANDLED;
   }

   protected void handleOpModeInit() {
      if (this.uiState == FtcDriverStationActivityBase.UIState.WAITING_FOR_INIT_EVENT) {
         this.traceUiStateChange("ui:uiWaitingForAck", FtcDriverStationActivityBase.UIState.WAITING_FOR_ACK);
         this.sendMatchNumberIfNecessary();
         this.networkConnectionHandler.sendCommand(new Command("CMD_INIT_OP_MODE", this.queuedOpMode.name));
         if (!this.queuedOpMode.name.equals(this.defaultOpMode.name)) {
            this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.RUNNING_OPMODE);
         }

         this.hideCameraStream();
      }
   }

   protected void handleOpModeQueued(OpModeMeta var1) {
      if (this.setQueuedOpModeIfDifferent(var1)) {
         this.enableAndResetTimerForQueued();
      }

      this.uiWaitingForInitEvent();
   }

   protected void handleOpModeStart() {
      if (this.uiState == FtcDriverStationActivityBase.UIState.WAITING_FOR_START_EVENT) {
         this.traceUiStateChange("ui:uiWaitingForAck", FtcDriverStationActivityBase.UIState.WAITING_FOR_ACK);
         this.networkConnectionHandler.sendCommand(new Command("CMD_RUN_OP_MODE", this.queuedOpMode.name));
      }
   }

   protected void handleOpModeStop() {
      if (this.uiState == FtcDriverStationActivityBase.UIState.WAITING_FOR_START_EVENT || this.uiState == FtcDriverStationActivityBase.UIState.WAITING_FOR_STOP_EVENT) {
         this.traceUiStateChange("ui:uiWaitingForAck", FtcDriverStationActivityBase.UIState.WAITING_FOR_ACK);
         this.clearMatchNumberIfNecessary();
         this.initDefaultOpMode();
         this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.STOPPED_OPMODE);
      }
   }

   protected CallbackResult handleReportGlobalError(String var1) {
      StringBuilder var2 = new StringBuilder();
      var2.append("Received error from robot controller: ");
      var2.append(var1);
      RobotLog.ee("DriverStation", var2.toString());
      RobotLog.setGlobalErrorMsg(var1);
      return CallbackResult.HANDLED;
   }

   public CallbackResult heartbeatEvent(RobocolDatagram var1, long var2) {
      try {
         this.heartbeatRecv.fromByteArray(var1.getData());
         RobotLog.processTimeSynch(this.heartbeatRecv.t0, this.heartbeatRecv.t1, this.heartbeatRecv.t2, var2);
         double var4 = this.heartbeatRecv.getElapsedSeconds();
         this.heartbeatRecv.getSequenceNumber();
         this.setRobotState(RobotState.fromByte(this.heartbeatRecv.getRobotState()));
         this.pingAverage.addNumber((int)(var4 * 1000.0D));
         if (this.lastUiUpdate.time() > 0.5D) {
            this.lastUiUpdate.reset();
            this.networkStatus();
         }
      } catch (RobotCoreException var6) {
         RobotLog.logStackTrace(var6);
      }

      return CallbackResult.HANDLED;
   }

   protected void hideCameraStream() {
      this.cameraStreamOpen = false;
      this.gamepadManager.setEnabled(true);
      this.setVisibility(this.cameraStreamLayout, 4);
      this.setVisibility(this.buttonStart, 0);
   }

   protected void initDefaultOpMode() {
      this.networkConnectionHandler.sendCommand(new Command("CMD_INIT_OP_MODE", this.defaultOpMode.name));
   }

   protected void initializeNetwork() {
      this.updateLoggingPrefs();
      NetworkType var1 = NetworkConnectionHandler.getDefaultNetworkType(this);
      this.connectionOwner = this.preferences.getString(this.getString(2131624404), this.getString(2131624149));
      this.connectionOwnerPassword = this.preferences.getString(this.getString(2131624405), this.getString(2131624150));
      this.networkConnectionHandler.init(NetworkConnectionHandler.newWifiLock(), var1, this.connectionOwner, this.connectionOwnerPassword, this, this.gamepadManager);
      if (this.networkConnectionHandler.isNetworkConnected()) {
         RobotLog.vv("Robocol", "Spoofing a Network Connection event...");
         this.onNetworkConnectionEvent(NetworkConnection.NetworkEvent.CONNECTION_INFO_AVAILABLE);
      }

   }

   protected boolean isDefaultOpMode(String var1) {
      return this.defaultOpMode.name.equals(var1);
   }

   protected boolean isDefaultOpMode(OpModeMeta var1) {
      return this.isDefaultOpMode(var1.name);
   }

   protected void networkStatus() {
      this.pingStatus(String.format("%dms", this.pingAverage.getAverage()));
      long var1 = this.networkConnectionHandler.getBytesPerSecond();
      if (var1 > 0L) {
         this.showBytesPerSecond(var1);
      }

   }

   public void onActivityResult(int var1, int var2, Intent var3) {
      RobotLog.vv("DriverStation", "onActivityResult(request=%d)", var1);
      if (var1 == LaunchActivityConstantsList.RequestCode.SETTINGS_DRIVER_STATION.ordinal()) {
         if (var3 != null) {
            FtcDriverStationSettingsActivity.Result var4 = FtcDriverStationSettingsActivity.Result.deserialize(var3.getExtras().getString("RESULT"));
            if (var4.prefLogsClicked) {
               this.updateLoggingPrefs();
            }

            if (var4.prefPairingMethodChanged) {
               RobotLog.ii("DriverStation", "Pairing method changed in settings activity, shutdown network to force complete restart");
               this.startOrRestartNetwork();
            }

            if (var4.prefPairClicked) {
               this.startOrRestartNetwork();
            }

            if (var4.prefAdvancedClicked) {
               this.networkConnectionHandler.sendCommand(new Command("CMD_RESTART_ROBOT"));
            }
         }
      } else if (var1 == LaunchActivityConstantsList.RequestCode.CONFIGURE_DRIVER_STATION.ordinal()) {
         this.requestUIState();
         this.networkConnectionHandler.sendCommand(new Command("CMD_RESTART_ROBOT"));
      }

   }

   public void onClickButtonAutonomous(View var1) {
      this.showOpModeDialog(this.filterOpModes(new Predicate() {
         public boolean test(OpModeMeta var1) {
            boolean var2;
            if (var1.flavor == OpModeMeta.Flavor.AUTONOMOUS) {
               var2 = true;
            } else {
               var2 = false;
            }

            return var2;
         }
      }), 2131624357);
   }

   public void onClickButtonInit(View var1) {
      this.handleOpModeInit();
   }

   public void onClickButtonStart(View var1) {
      this.handleOpModeStart();
   }

   public void onClickButtonStop(View var1) {
      this.handleOpModeStop();
   }

   public void onClickButtonTeleOp(View var1) {
      this.showOpModeDialog(this.filterOpModes(new Predicate() {
         public boolean test(OpModeMeta var1) {
            boolean var2;
            if (var1.flavor == OpModeMeta.Flavor.TELEOP) {
               var2 = true;
            } else {
               var2 = false;
            }

            return var2;
         }
      }), 2131624358);
   }

   public void onClickDSBatteryToast(View var1) {
      this.showToast(this.getString(2131624622));
   }

   public void onClickRCBatteryToast(View var1) {
      this.showToast(this.getString(2131624635));
   }

   public void onClickRobotBatteryToast(View var1) {
      this.resetBatteryStats();
      this.showToast(this.getString(2131624634));
   }

   public void onClickTimer(View var1) {
      boolean var2 = this.opModeUseTimer ^ true;
      this.opModeUseTimer = var2;
      this.enableAndResetTimer(var2);
   }

   public void onConfigurationChanged(Configuration var1) {
      super.onConfigurationChanged(var1);
   }

   protected void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.enforcePermissionValidator();
      this.uiThread = Thread.currentThread();
      this.subclassOnCreate();
      this.gamepadManager = new GamepadManager(this);
      this.context = this;
      this.utility = new Utility(this);
      this.opModeCountDown = new FtcDriverStationActivityBase.OpModeCountDownTimer();
      this.rcHasIndependentBattery = false;
      PreferenceManager.setDefaultValues(this, 2131820545, false);
      this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
      this.preferencesHelper = new PreferencesHelper("DriverStation", this.preferences);
      DeviceNameManagerFactory.getInstance().start(this.deviceNameManagerStartResult);
      PreferenceRemoterDS.getInstance().start(this.prefRemoterStartResult);
      NetworkConnectionHandler.getInstance().registerPeerStatusCallback(this);
      this.setClientConnected(false);
      if (permissionsValidated) {
         RobotLog.ii("DriverStation", "Processing all classes through class filter");
         ClassManagerFactory.registerResourceFilters();
         ClassManagerFactory.processAllClasses();
      }

      this.robotConfigFileManager = new RobotConfigFileManager(this);
      this.textDeviceName = (TextView)this.findViewById(2131231118);
      this.textDsUiStateIndicator = (TextView)this.findViewById(2131231119);
      this.textWifiDirectStatus = (TextView)this.findViewById(2131231147);
      this.textWifiDirectStatusShowingRC = false;
      this.textWifiChannel = (TextView)this.findViewById(2131231185);
      this.textPingStatus = (TextView)this.findViewById(2131231120);
      this.textBytesPerSecond = (TextView)this.findViewById(2131230816);
      this.textTelemetry = (TextView)this.findViewById(2131231125);
      TextView var2 = (TextView)this.findViewById(2131231124);
      this.systemTelemetry = var2;
      this.systemTelemetryOriginalColor = var2.getCurrentTextColor();
      this.rcBatteryContainer = this.findViewById(2131231038);
      this.rcBatteryTelemetry = (TextView)this.findViewById(2131231040);
      this.robotBatteryMinimum = (TextView)this.findViewById(2131231047);
      this.rcBatteryIcon = (ImageView)this.findViewById(2131231041);
      this.dsBatteryInfo = (TextView)this.findViewById(2131230885);
      this.robotBatteryTelemetry = (TextView)this.findViewById(2131231048);
      this.dsBatteryIcon = (ImageView)this.findViewById(2131230722);
      this.immersion = new ImmersiveMode(this.getWindow().getDecorView());
      this.doMatchNumFieldBehaviorInit();
      LinearLayout var3 = (LinearLayout)this.findViewById(2131230834);
      this.cameraStreamLayout = var3;
      var3.setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            FtcDriverStationActivityBase.this.networkConnectionHandler.sendCommand(new Command("CMD_REQUEST_FRAME"));
         }
      });
      this.cameraStreamImageView = (ImageView)this.findViewById(2131230833);
      CameraStreamClient.getInstance().setListener(new CameraStreamClient.Listener() {
         public void onFrameBitmap(final Bitmap var1) {
            FtcDriverStationActivityBase.this.runOnUiThread(new Runnable() {
               public void run() {
                  FtcDriverStationActivityBase.this.cameraStreamImageView.setImageBitmap(var1);
               }
            });
         }

         public void onStreamAvailableChange(boolean var1) {
            FtcDriverStationActivityBase.this.invalidateOptionsMenu();
            if (FtcDriverStationActivityBase.this.cameraStreamOpen && !var1) {
               FtcDriverStationActivityBase.this.hideCameraStream();
            }

         }
      });
      this.buttonInit = this.findViewById(2131230818);
      this.buttonInitStop = this.findViewById(2131230819);
      this.buttonStart = this.findViewById(2131230822);
      this.controlPanelBack = this.findViewById(2131230860);
      this.batteryInfo = this.findViewById(2131230810);
      this.wifiInfo = this.findViewById(2131231192);
      ((ImageButton)this.findViewById(2131230823)).setImageDrawable(new FilledPolygonDrawable(((ColorDrawable)this.findViewById(2131230824).getBackground()).getColor(), 3));
      ((ImageView)this.findViewById(2131231155)).setImageDrawable(new StopWatchDrawable(((ColorDrawable)this.findViewById(2131231156).getBackground()).getColor()));
      this.gamepadIndicators.put(GamepadUser.ONE, new GamepadIndicator(this, 2131231179, 2131231178));
      this.gamepadIndicators.put(GamepadUser.TWO, new GamepadIndicator(this, 2131231181, 2131231180));
      this.gamepadManager.setGamepadIndicators(this.gamepadIndicators);
      var2 = (TextView)this.findViewById(2131230787);
      this.activeConfigText = var2;
      var2.setText(" ");
      this.timerAndTimerSwitch = this.findViewById(2131231151);
      this.buttonAutonomous = (Button)this.findViewById(2131230817);
      this.buttonTeleOp = (Button)this.findViewById(2131230826);
      this.currentOpModeName = (TextView)this.findViewById(2131230868);
      this.chooseOpModePrompt = this.findViewById(2131230844);
      this.buttonStop = (ImageButton)this.findViewById(2131230825);
      ImageButton var4 = (ImageButton)this.findViewById(2131230989);
      this.buttonMenu = var4;
      var4.setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            FtcDriverStationActivityBase var2 = FtcDriverStationActivityBase.this;
            PopupMenu var3 = new PopupMenu(var2, var2.getPopupMenuAnchor());
            var3.setOnMenuItemClickListener(new OnMenuItemClickListener() {
               public boolean onMenuItemClick(MenuItem var1) {
                  return FtcDriverStationActivityBase.this.onOptionsItemSelected(var1);
               }
            });
            FtcDriverStationActivityBase.this.onCreateOptionsMenu(var3.getMenu());
            var3.show();
         }
      });
      this.preferences.registerOnSharedPreferenceChangeListener(this);
      this.gamepadManager.open();
      BatteryChecker var5 = new BatteryChecker(this, (long)300000);
      this.batteryChecker = var5;
      var5.startBatteryMonitoring();
      this.resetBatteryStats();
      this.pingStatus(2131624391);
      this.mInputManager = (InputManager)this.getSystemService("input");
      this.networkConnectionHandler.pushNetworkConnectionCallback(this);
      this.networkConnectionHandler.pushReceiveLoopCallback(this);
      this.startOrRestartNetwork();
      DeviceNameManagerFactory.getInstance().registerCallback(this.deviceNameManagerCallback);
      ((WifiManager)AppUtil.getDefContext().getApplicationContext().getSystemService("wifi")).setWifiEnabled(true);
      WifiMuteStateMachine var6 = new WifiMuteStateMachine();
      this.wifiMuteStateMachine = var6;
      var6.initialize();
      this.wifiMuteStateMachine.start();
      this.wifiMuteStateMachine.registerCallback(this);
      this.processUserActivity = true;
      SoundPlayingRobotMonitor.prefillSoundCache();
      RobotLog.logBuildConfig(BuildConfig.class);
      RobotLog.logDeviceInfo();
      this.androidTextToSpeech.initialize();
   }

   public boolean onCreateOptionsMenu(Menu var1) {
      this.getMenuInflater().inflate(2131492864, var1);
      if (this.uiState == FtcDriverStationActivityBase.UIState.WAITING_FOR_START_EVENT && CameraStreamClient.getInstance().isStreamAvailable()) {
         var1.findItem(2131230768).setVisible(true);
      } else {
         var1.findItem(2131230768).setVisible(false);
      }

      return true;
   }

   protected void onDestroy() {
      super.onDestroy();
      RobotLog.vv("DriverStation", "onDestroy()");
      this.androidTextToSpeech.close();
      this.gamepadManager.close();
      DeviceNameManagerFactory.getInstance().unregisterCallback(this.deviceNameManagerCallback);
      this.networkConnectionHandler.removeNetworkConnectionCallback(this);
      this.networkConnectionHandler.removeReceiveLoopCallback(this);
      this.shutdown();
      PreferenceRemoterDS.getInstance().stop(this.prefRemoterStartResult);
      DeviceNameManagerFactory.getInstance().stop(this.deviceNameManagerStartResult);
      RobotLog.cancelWriteLogcatToDisk();
   }

   public CallbackResult onNetworkConnectionEvent(NetworkConnection.NetworkEvent var1) {
      CallbackResult var2 = CallbackResult.NOT_HANDLED;
      StringBuilder var3 = new StringBuilder();
      var3.append("Received networkConnectionEvent: ");
      var3.append(var1.toString());
      RobotLog.i(var3.toString());
      String var4;
      CallbackResult var5;
      switch(null.$SwitchMap$com$qualcomm$robotcore$wifi$NetworkConnection$NetworkEvent[var1.ordinal()]) {
      case 1:
         if (this.networkConnectionHandler.isWifiDirect()) {
            this.onPeersAvailableWifiDirect();
         } else {
            this.onPeersAvailableSoftAP();
         }

         var5 = CallbackResult.HANDLED;
         break;
      case 2:
         RobotLog.ee("DriverStation", "Wifi Direct - connected as Group Owner, was expecting Peer");
         this.showWifiStatus(false, this.getString(2131624681));
         ConfigWifiDirectActivity.launch(this.getBaseContext(), ConfigWifiDirectActivity.Flag.WIFI_DIRECT_DEVICE_NAME_INVALID);
         var5 = CallbackResult.HANDLED;
         break;
      case 3:
         this.showWifiStatus(false, this.getString(2131624679));
         var5 = CallbackResult.HANDLED;
         break;
      case 4:
         this.showWifiStatus(false, this.getString(2131624678));
         var5 = CallbackResult.HANDLED;
         break;
      case 5:
         this.showWifiStatus(true, this.getBestRobotControllerName());
         this.showWifiChannel();
         if (!NetworkConnection.isDeviceNameValid(this.networkConnectionHandler.getDeviceName())) {
            RobotLog.ee("DriverStation", "Wifi-Direct device name contains non-printable characters");
            ConfigWifiDirectActivity.launch(this.getBaseContext(), ConfigWifiDirectActivity.Flag.WIFI_DIRECT_DEVICE_NAME_INVALID);
         } else if (this.networkConnectionHandler.connectedWithUnexpectedDevice()) {
            this.showWifiStatus(false, this.getString(2131624682));
            if (this.networkConnectionHandler.isWifiDirect()) {
               ConfigWifiDirectActivity.launch(this.getBaseContext(), ConfigWifiDirectActivity.Flag.WIFI_DIRECT_FIX_CONFIG);
            } else {
               if (this.connectionOwner == null && this.connectionOwnerPassword == null) {
                  this.showWifiStatus(false, this.getString(2131624683));
                  return CallbackResult.HANDLED;
               }

               this.networkConnectionHandler.startConnection(this.connectionOwner, this.connectionOwnerPassword);
            }

            return CallbackResult.HANDLED;
         }

         this.networkConnectionHandler.handleConnectionInfoAvailable();
         this.networkConnectionHandler.cancelConnectionSearch();
         this.assumeClientConnectAndRefreshUI(FtcDriverStationActivityBase.ControlPanelBack.NO_CHANGE);
         var5 = CallbackResult.HANDLED;
         break;
      case 6:
         var4 = this.getString(2131624680);
         this.showWifiStatus(false, var4);
         StringBuilder var6 = new StringBuilder();
         var6.append("Network Connection - ");
         var6.append(var4);
         RobotLog.vv("DriverStation", var6.toString());
         this.networkConnectionHandler.discoverPotentialConnections();
         this.assumeClientDisconnect();
         var5 = CallbackResult.HANDLED;
         break;
      case 7:
         var4 = this.getString(2131624180, new Object[]{this.networkConnectionHandler.getFailureReason()});
         this.showWifiStatus(false, var4);
         var3 = new StringBuilder();
         var3.append("Network Connection - ");
         var3.append(var4);
         RobotLog.vv("DriverStation", var3.toString());
         var5 = var2;
         break;
      default:
         var5 = var2;
      }

      return var5;
   }

   public void onOpModeSelectionClick(OpModeMeta var1) {
      this.handleOpModeQueued(var1);
   }

   public boolean onOptionsItemSelected(MenuItem var1) {
      this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.ACTIVITY_OTHER);
      this.wifiMuteStateMachine.maskEvent(WifiMuteEvent.STOPPED_OPMODE);
      switch(var1.getItemId()) {
      case 2131230760:
         this.startActivity(new Intent(AppUtil.getDefContext(), FtcAboutActivity.class));
         return true;
      case 2131230768:
         if (this.cameraStreamOpen) {
            this.hideCameraStream();
         } else {
            this.showCameraStream();
         }

         return true;
      case 2131230769:
         EditParameters var4 = new EditParameters();
         Intent var2 = new Intent(AppUtil.getDefContext(), FtcLoadFileActivity.class);
         var4.putIntent(var2);
         this.startActivityForResult(var2, LaunchActivityConstantsList.RequestCode.CONFIGURE_DRIVER_STATION.ordinal());
         return true;
      case 2131230773:
         this.finishAffinity();
         Iterator var3 = ((ActivityManager)this.getSystemService("activity")).getAppTasks().iterator();

         while(var3.hasNext()) {
            ((AppTask)var3.next()).finishAndRemoveTask();
         }

         AppUtil.getInstance().exitApplication();
         return true;
      case 2131230776:
         this.startActivityForResult(new Intent(this.getBaseContext(), FtcDriverStationInspectionReportsActivity.class), LaunchActivityConstantsList.RequestCode.INSPECTIONS.ordinal());
         return true;
      case 2131230782:
         RobotLog.vv("DriverStation", "action_program_and_manage clicked");
         this.networkConnectionHandler.sendCommand(new Command("CMD_START_DS_PROGRAM_AND_MANAGE"));
         return true;
      case 2131230783:
         this.networkConnectionHandler.sendCommand(new Command("CMD_RESTART_ROBOT"));
         this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.ACTIVITY_START);
         this.wifiMuteStateMachine.maskEvent(WifiMuteEvent.STOPPED_OPMODE);
         return true;
      case 2131230784:
         this.startActivityForResult(new Intent(this.getBaseContext(), FtcDriverStationSettingsActivity.class), LaunchActivityConstantsList.RequestCode.SETTINGS_DRIVER_STATION.ordinal());
         return true;
      default:
         return super.onOptionsItemSelected(var1);
      }
   }

   protected void onPause() {
      super.onPause();
      RobotLog.vv("DriverStation", "onPause()");
      this.gamepadManager.clearGamepadAssignments();
      this.gamepadManager.clearTrackedGamepads();
      this.mInputManager.unregisterInputDeviceListener(this.gamepadManager);
      this.initDefaultOpMode();
   }

   public void onPeerConnected() {
      RobotLog.vv("DriverStation", "robot controller connected");
      this.assumeClientConnectAndRefreshUI(FtcDriverStationActivityBase.ControlPanelBack.NO_CHANGE);
      PreferenceRemoterDS.getInstance().sendInformationalPrefsToRc();
   }

   public void onPeerDisconnected() {
      RobotLog.logStackTrace(new Throwable("Peer disconnected"));
      RobotLog.vv("DriverStation", "robot controller disconnected");
      this.assumeClientDisconnect();
   }

   public void onPendingCancel() {
      this.processUserActivity = true;
      StringBuilder var1 = new StringBuilder();
      var1.append("Pending Wifi Cancel: ");
      var1.append(this.queuedOpMode.name);
      RobotLog.ii("DriverStation", var1.toString());
   }

   public void onPendingOn() {
      this.processUserActivity = false;
      StringBuilder var1 = new StringBuilder();
      var1.append("Pending Wifi Off: ");
      var1.append(this.queuedOpMode.name);
      RobotLog.ii("DriverStation", var1.toString());
   }

   protected void onResume() {
      super.onResume();
      RobotLog.vv("DriverStation", "onResume()");
      this.disconnectFromPeerOnActivityStop = true;
      this.updateRcBatteryIndependence(this.preferences);
      this.resetBatteryStats();
      this.mInputManager.registerInputDeviceListener(this.gamepadManager, (Handler)null);
      this.pingStatus(2131624391);
   }

   public void onSharedPreferenceChanged(SharedPreferences var1, String var2) {
      RobotLog.vv("DriverStation", "onSharedPreferenceChanged() pref=%s", var2);
      if (var2.equals(this.context.getString(2131624411))) {
         final String var3 = var1.getString(var2, "");
         if (var3.length() > 0) {
            this.runOnUiThread(new Runnable() {
               public void run() {
                  if (FtcDriverStationActivityBase.this.textWifiDirectStatusShowingRC) {
                     FtcDriverStationActivityBase.this.textWifiDirectStatus.setText(var3);
                  }

               }
            });
         }
      } else if (var2.equals(this.getString(2131624419))) {
         this.updateRcBatteryIndependence(this.preferences);
      } else if (!var2.equals(this.getString(2131624397)) && var2.equals("pref_wifip2p_channel")) {
         RobotLog.vv("DriverStation", "pref_wifip2p_channel changed.");
         this.showWifiChannel();
      }

      this.updateLoggingPrefs();
   }

   protected void onStart() {
      super.onStart();
      RobotLog.onApplicationStart();
      RobotLog.vv("DriverStation", "onStart()");
      Iterator var1 = this.gamepadIndicators.values().iterator();

      while(var1.hasNext()) {
         ((GamepadIndicator)var1.next()).setState(GamepadIndicator.State.INVISIBLE);
      }

      this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.ACTIVITY_START);
      this.wifiMuteStateMachine.unMaskEvent(WifiMuteEvent.STOPPED_OPMODE);
      FtcAboutActivity.setBuildTimeFromBuildConfig("2020-09-21T09:05:36.688-0700");
   }

   protected void onStop() {
      super.onStop();
      RobotLog.vv("DriverStation", "onStop()");
      this.pingStatus(2131624392);
      this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.ACTIVITY_STOP);
      if (this.disconnectFromPeerOnActivityStop) {
         RobotLog.ii("DriverStation", "App appears to be exiting. Destroying activity so that another DS can connect");
         this.finish();
      }

   }

   public void onUserInteraction() {
      if (this.processUserActivity) {
         this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.USER_ACTIVITY);
      }

   }

   public void onWifiOff() {
      this.queuedOpModeWhenMuted = this.queuedOpMode;
      StringBuilder var1 = new StringBuilder();
      var1.append("Wifi Off: ");
      var1.append(this.queuedOpMode.name);
      RobotLog.ii("DriverStation", var1.toString());
   }

   public void onWifiOn() {
      this.queuedOpMode = this.queuedOpModeWhenMuted;
      this.processUserActivity = true;
      StringBuilder var1 = new StringBuilder();
      var1.append("Wifi On: ");
      var1.append(this.queuedOpMode.name);
      RobotLog.ii("DriverStation", var1.toString());
   }

   public void onWindowFocusChanged(boolean var1) {
      super.onWindowFocusChanged(var1);
      if (var1) {
         this.immersion.hideSystemUI();
         this.getWindow().setFlags(134217728, 134217728);
      }

   }

   public CallbackResult packetReceived(RobocolDatagram var1) throws RobotCoreException {
      return CallbackResult.NOT_HANDLED;
   }

   public CallbackResult peerDiscoveryEvent(RobocolDatagram var1) throws RobotCoreException {
      try {
         PeerDiscovery var2 = PeerDiscovery.forReceive();
         var2.fromByteArray(var1.getData());
         if (var2.getPeerType() == PeerDiscovery.PeerType.NOT_CONNECTED_DUE_TO_PREEXISTING_CONNECTION) {
            this.reportGlobalError(this.getString(2131624013), false);
            this.showRobotBatteryVoltage("$no$voltage$sensor$");
         } else {
            this.networkConnectionHandler.updateConnection(var1);
         }
      } catch (RobotProtocolException var3) {
         this.reportGlobalError(var3.getMessage(), false);
         this.networkConnectionHandler.stopPeerDiscovery();
         RobotLog.setGlobalErrorMsgSticky(true);
         Thread.currentThread().interrupt();
         this.showRobotBatteryVoltage("$no$voltage$sensor$");
      }

      return CallbackResult.HANDLED;
   }

   protected void pingStatus(int var1) {
      this.pingStatus(this.context.getString(var1));
   }

   protected abstract void pingStatus(String var1);

   public CallbackResult reportGlobalError(String var1, boolean var2) {
      if (!RobotLog.getGlobalErrorMsg().equals(var1)) {
         StringBuilder var3 = new StringBuilder();
         var3.append("System telemetry error: ");
         var3.append(var1);
         RobotLog.ee("DriverStation", var3.toString());
         RobotLog.clearGlobalErrorMsg();
         RobotLog.setGlobalErrorMsg(var1);
      }

      TextView var5 = this.systemTelemetry;
      AppUtil.getInstance();
      this.setTextColor(var5, AppUtil.getColor(2131034272));
      this.setVisibility(this.systemTelemetry, 0);
      StringBuilder var4 = new StringBuilder();
      RobotState var6 = this.robotState;
      if (var6 != null && var6 != RobotState.UNKNOWN) {
         var4.append(String.format(this.getString(2131624182), this.robotState.toString(this)));
      }

      if (var2) {
         var4.append(this.getString(2131624183));
      }

      var4.append(String.format(this.getString(2131624180), var1));
      this.setTextView(this.systemTelemetry, var4.toString());
      this.stopTimerAndReset();
      this.uiRobotCantContinue();
      return CallbackResult.HANDLED;
   }

   protected void reportGlobalWarning(String var1) {
      StringBuilder var2;
      if (!RobotLog.getGlobalWarningMessage().equals(var1)) {
         var2 = new StringBuilder();
         var2.append("System telemetry warning: ");
         var2.append(var1);
         RobotLog.ee("DriverStation", var2.toString());
         RobotLog.clearGlobalWarningMsg();
         RobotLog.setGlobalWarningMessage(var1);
      }

      TextView var3 = this.systemTelemetry;
      AppUtil.getInstance();
      this.setTextColor(var3, AppUtil.getColor(2131034274));
      this.setVisibility(this.systemTelemetry, 0);
      var2 = new StringBuilder();
      var2.append(String.format(this.getString(2131624184), var1));
      this.setTextView(this.systemTelemetry, var2.toString());
   }

   protected void requestUIState() {
      this.networkConnectionHandler.sendCommand(new Command("CMD_REQUEST_UI_STATE"));
   }

   protected void resetBatteryStats() {
      this.V12BatteryMin = Double.POSITIVE_INFINITY;
      this.V12BatteryMinString = "";
   }

   protected void runDefaultOpMode() {
      this.networkConnectionHandler.sendCommand(new Command("CMD_RUN_OP_MODE", this.defaultOpMode.name));
      this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.STOPPED_OPMODE);
   }

   protected void sendMatchNumber(int var1) {
      this.sendMatchNumber(String.valueOf(var1));
   }

   protected void sendMatchNumber(String var1) {
      this.networkConnectionHandler.sendCommand(new Command("CMD_SET_MATCH_NUMBER", var1));
   }

   protected void sendMatchNumberIfNecessary() {
      try {
         this.sendMatchNumber(this.getMatchNumber());
      } catch (NumberFormatException var2) {
         this.sendMatchNumber(0);
      }

   }

   protected void setBG(final View var1, final Drawable var2) {
      this.runOnUiThread(new Runnable() {
         public void run() {
            var1.setBackground(var2);
         }
      });
   }

   protected void setBGColor(final View var1, final int var2) {
      this.runOnUiThread(new Runnable() {
         public void run() {
            var1.setBackgroundColor(var2);
         }
      });
   }

   protected void setBatteryIcon(final BatteryChecker.BatteryStatus var1, final ImageView var2) {
      this.runOnUiThread(new Runnable() {
         public void run() {
            ImageView var1x;
            int var2x;
            if (var1.percent <= 15.0D) {
               var1x = var2;
               if (var1.isCharging) {
                  var2x = 2131165329;
               } else {
                  var2x = 2131165328;
               }

               var1x.setImageResource(var2x);
               var2.setColorFilter(FtcDriverStationActivityBase.this.getResources().getColor(2131034247), Mode.MULTIPLY);
            } else if (var1.percent > 15.0D && var1.percent <= 45.0D) {
               var1x = var2;
               if (var1.isCharging) {
                  var2x = 2131165333;
               } else {
                  var2x = 2131165332;
               }

               var1x.setImageResource(var2x);
               if (var1.percent <= 30.0D) {
                  var2.setColorFilter(FtcDriverStationActivityBase.this.getResources().getColor(2131034248), Mode.MULTIPLY);
               } else {
                  var2.setColorFilter(FtcDriverStationActivityBase.this.getResources().getColor(2131034275), Mode.MULTIPLY);
               }
            } else if (var1.percent > 45.0D && var1.percent <= 65.0D) {
               var1x = var2;
               if (var1.isCharging) {
                  var2x = 2131165335;
               } else {
                  var2x = 2131165334;
               }

               var1x.setImageResource(var2x);
               var2.setColorFilter(FtcDriverStationActivityBase.this.getResources().getColor(2131034275), Mode.MULTIPLY);
            } else if (var1.percent > 65.0D && var1.percent <= 85.0D) {
               var1x = var2;
               if (var1.isCharging) {
                  var2x = 2131165337;
               } else {
                  var2x = 2131165336;
               }

               var1x.setImageResource(var2x);
               var2.setColorFilter(FtcDriverStationActivityBase.this.getResources().getColor(2131034275), Mode.MULTIPLY);
            } else {
               var1x = var2;
               if (var1.isCharging) {
                  var2x = 2131165331;
               } else {
                  var2x = 2131165330;
               }

               var1x.setImageResource(var2x);
               var2.setColorFilter(FtcDriverStationActivityBase.this.getResources().getColor(2131034275), Mode.MULTIPLY);
            }

         }
      });
   }

   protected void setButtonText(final Button var1, final String var2) {
      this.runOnUiThread(new Runnable() {
         public void run() {
            var1.setText(var2);
         }
      });
   }

   protected boolean setClientConnected(boolean var1) {
      boolean var2 = this.clientConnected;
      this.clientConnected = var1;
      this.preferencesHelper.writeBooleanPrefIfDifferent(this.getString(2131624448), var1);
      return var2;
   }

   protected void setControlPanelBack(FtcDriverStationActivityBase.ControlPanelBack var1) {
      int var2 = null.$SwitchMap$com$qualcomm$ftcdriverstation$FtcDriverStationActivityBase$ControlPanelBack[var1.ordinal()];
      if (var2 != 2) {
         if (var2 == 3) {
            this.brightenControlPanelBack();
         }
      } else {
         this.dimControlPanelBack();
      }

   }

   protected void setEnabled(final View var1, final boolean var2) {
      this.runOnUiThread(new Runnable() {
         public void run() {
            var1.setEnabled(var2);
         }
      });
   }

   protected void setImageResource(final ImageButton var1, final int var2) {
      this.runOnUiThread(new Runnable() {
         public void run() {
            var1.setImageResource(var2);
         }
      });
   }

   protected void setOpacity(final View var1, final float var2) {
      this.runOnUiThread(new Runnable() {
         public void run() {
            var1.setAlpha(var2);
         }
      });
   }

   protected boolean setQueuedOpModeIfDifferent(String var1) {
      return this.setQueuedOpModeIfDifferent(this.getOpModeMeta(var1));
   }

   protected boolean setQueuedOpModeIfDifferent(OpModeMeta var1) {
      if (!var1.name.equals(this.queuedOpMode.name)) {
         this.queuedOpMode = var1;
         this.showQueuedOpModeName();
         return true;
      } else {
         return false;
      }
   }

   protected void setRobotState(RobotState var1) {
      if (this.robotState != var1) {
         this.robotState = var1;
         if (var1 == RobotState.STOPPED) {
            this.traceUiStateChange("ui:uiRobotStopped", FtcDriverStationActivityBase.UIState.ROBOT_STOPPED);
            this.disableAndDimOpModeMenu();
            this.disableOpModeControls();
            this.dimControlPanelBack();
         }

         if (var1 == RobotState.EMERGENCY_STOP) {
            WifiMuteStateMachine var2 = this.wifiMuteStateMachine;
            if (var2 != null) {
               var2.consumeEvent(WifiMuteEvent.STOPPED_OPMODE);
            }
         }
      }

   }

   protected void setTextColor(final TextView var1, final int var2) {
      this.runOnUiThread(new Runnable() {
         public void run() {
            var1.setTextColor(var2);
         }
      });
   }

   protected void setTextView(final TextView var1, final CharSequence var2) {
      this.runOnUiThread(new Runnable() {
         public void run() {
            var1.setText(var2);
         }
      });
   }

   protected void setTimerButtonEnabled(boolean var1) {
      this.setEnabled(this.timerAndTimerSwitch, var1);
      this.setEnabled(this.findViewById(2131231152), var1);
      this.setEnabled(this.findViewById(2131231155), var1);
      this.setEnabled(this.findViewById(2131231160), var1);
      this.setEnabled(this.findViewById(2131231159), var1);
      this.setEnabled(this.findViewById(2131231158), var1);
   }

   protected void setUserTelemetry(String var1) {
      int var2 = null.$SwitchMap$org$firstinspires$ftc$robotcore$external$Telemetry$DisplayFormat[this.telemetryMode.ordinal()];
      if (var2 != 1 && var2 != 2) {
         if (var2 == 3) {
            this.setTextView(this.textTelemetry, Html.fromHtml(var1.replace("\n", "<br>")));
         }
      } else {
         this.setTextView(this.textTelemetry, var1);
      }

   }

   protected void setVisibility(final View var1, final int var2) {
      this.runOnUiThread(new Runnable() {
         public void run() {
            var1.setVisibility(var2);
         }
      });
   }

   protected void showBytesPerSecond(final long var1) {
      this.runOnUiThread(new Runnable() {
         public void run() {
            FtcDriverStationActivityBase.this.textBytesPerSecond.setText(String.valueOf(var1));
         }
      });
   }

   protected void showCameraStream() {
      this.cameraStreamOpen = true;
      this.gamepadManager.setEnabled(false);
      this.setVisibility(this.cameraStreamLayout, 0);
      this.setVisibility(this.buttonStart, 4);
      this.networkConnectionHandler.sendCommand(new Command("CMD_REQUEST_FRAME"));
      this.showToast(this.getString(2131624618));
   }

   protected void showOpModeDialog(List var1, int var2) {
      this.stopTimerPreservingRemainingTime();
      this.initDefaultOpMode();
      OpModeSelectionDialogFragment var3 = new OpModeSelectionDialogFragment();
      var3.setOnSelectionDialogListener(this);
      var3.setOpModes(var1);
      var3.setTitle(var2);
      var3.show(this.getFragmentManager(), "op_mode_selection");
   }

   protected void showQueuedOpModeName() {
      this.showQueuedOpModeName(this.queuedOpMode);
   }

   protected void showQueuedOpModeName(OpModeMeta var1) {
      if (this.isDefaultOpMode(var1)) {
         this.setVisibility(this.currentOpModeName, 8);
         this.setVisibility(this.chooseOpModePrompt, 0);
      } else {
         this.setTextView(this.currentOpModeName, var1.name);
         this.setVisibility(this.currentOpModeName, 0);
         this.setVisibility(this.chooseOpModePrompt, 8);
      }

   }

   protected void showRobotBatteryVoltage(String var1) {
      RelativeLayout var2 = (RelativeLayout)this.findViewById(2131231050);
      View var3 = this.findViewById(2131231042);
      TextView var4 = (TextView)this.findViewById(2131231043);
      if (var1.equals("$no$voltage$sensor$")) {
         this.setVisibility(var3, 8);
         this.setVisibility(var4, 0);
         this.resetBatteryStats();
         this.setBG(var2, this.findViewById(2131231037).getBackground());
      } else {
         this.setVisibility(var3, 0);
         this.setVisibility(var4, 8);
         double var5 = Double.valueOf(var1);
         if (var5 < this.V12BatteryMin) {
            this.V12BatteryMin = var5;
            this.V12BatteryMinString = var1;
         }

         var4 = this.robotBatteryTelemetry;
         StringBuilder var12 = new StringBuilder();
         var12.append(var1);
         var12.append(" V");
         this.setTextView(var4, var12.toString());
         TextView var11 = this.robotBatteryMinimum;
         StringBuilder var13 = new StringBuilder();
         var13.append("( ");
         var13.append(this.V12BatteryMinString);
         var13.append(" V )");
         this.setTextView(var11, var13.toString());
         double var7 = (double)10.0F;
         double var9 = (double)14.0F;
         this.setBGColor(var2, Color.HSVToColor(new float[]{(float)Range.scale(Range.clip(var5, var7, var9), var7, var9, (double)0.0F, (double)128.0F), 1.0F, 0.6F}));
      }

   }

   public void showToast(String var1) {
      this.appUtil.showToast(UILocation.ONLY_LOCAL, var1);
   }

   protected void showWifiChannel() {
      this.runOnUiThread(new Runnable() {
         public void run() {
            StringBuilder var1;
            String var3;
            if (FtcDriverStationActivityBase.this.networkConnectionHandler.getWifiChannel() > 0) {
               var1 = new StringBuilder();
               var1.append("ch ");
               var1.append(FtcDriverStationActivityBase.this.networkConnectionHandler.getWifiChannel());
               var3 = var1.toString();
               FtcDriverStationActivityBase.this.textWifiChannel.setText(var3);
               FtcDriverStationActivityBase.this.textWifiChannel.setVisibility(0);
            } else {
               int var2 = FtcDriverStationActivityBase.this.preferences.getInt(FtcDriverStationActivityBase.this.getString(2131624455), -1);
               if (var2 == -1) {
                  RobotLog.vv("DriverStation", "pref_wifip2p_channel: showWifiChannel prefChannel not found");
                  FtcDriverStationActivityBase.this.textWifiChannel.setVisibility(8);
               } else {
                  RobotLog.vv("DriverStation", "pref_wifip2p_channel: showWifiChannel prefChannel = %d", var2);
                  var1 = new StringBuilder();
                  var1.append("ch ");
                  var1.append(Integer.toString(var2));
                  var3 = var1.toString();
                  FtcDriverStationActivityBase.this.textWifiChannel.setText(var3);
                  FtcDriverStationActivityBase.this.textWifiChannel.setVisibility(0);
               }
            }

         }
      });
   }

   protected void showWifiStatus(final boolean var1, final String var2) {
      this.runOnUiThread(new Runnable() {
         public void run() {
            FtcDriverStationActivityBase.this.textWifiDirectStatusShowingRC = var1;
            FtcDriverStationActivityBase.this.textWifiDirectStatus.setText(var2);
         }
      });
   }

   protected void shutdown() {
      this.networkConnectionHandler.stop();
      this.networkConnectionHandler.shutdown();
   }

   public void startActivity(Intent var1, Bundle var2) {
      this.disconnectFromPeerOnActivityStop = false;
      super.startActivity(var1, var2);
   }

   public void startActivityForResult(Intent var1, int var2, Bundle var3) {
      this.disconnectFromPeerOnActivityStop = false;
      super.startActivityForResult(var1, var2, var3);
   }

   protected void startKeepAlives() {
      NetworkConnectionHandler var1 = this.networkConnectionHandler;
      if (var1 != null) {
         var1.startKeepAlives();
      }

   }

   protected void startOrRestartNetwork() {
      RobotLog.vv("DriverStation", "startOrRestartNetwork()");
      this.assumeClientDisconnect();
      this.showWifiStatus(false, this.getString(2131624680));
      this.initializeNetwork();
   }

   protected void stopKeepAlives() {
      NetworkConnectionHandler var1 = this.networkConnectionHandler;
      if (var1 != null) {
         var1.stopKeepAlives();
      }

   }

   void stopTimerAndReset() {
      this.opModeCountDown.stop();
      this.opModeCountDown.resetCountdown();
   }

   void stopTimerPreservingRemainingTime() {
      this.opModeCountDown.stopPreservingRemainingTime();
   }

   public abstract void subclassOnCreate();

   public CallbackResult telemetryEvent(RobocolDatagram var1) {
      TelemetryMessage var2;
      try {
         var2 = new TelemetryMessage(var1.getData());
      } catch (RobotCoreException var9) {
         RobotLog.logStackTrace(var9);
         return CallbackResult.HANDLED;
      }

      if (var2.getRobotState() != RobotState.UNKNOWN) {
         this.setRobotState(var2.getRobotState());
      }

      Map var3 = var2.getDataStrings();
      Object var10;
      if (var2.isSorted()) {
         var10 = new TreeSet(var3.keySet());
      } else {
         var10 = var3.keySet();
      }

      Iterator var4 = ((Collection)var10).iterator();
      String var11 = "";
      boolean var5 = false;

      String var7;
      StringBuilder var12;
      StringBuilder var15;
      while(var4.hasNext()) {
         String var6 = (String)var4.next();
         if (var6.equals("$Robot$Battery$Level$")) {
            this.showRobotBatteryVoltage((String)var3.get(var6));
         } else {
            var7 = var11;
            if (var6.length() > 0) {
               var7 = var11;
               if (var6.charAt(0) != 0) {
                  var15 = new StringBuilder();
                  var15.append(var11);
                  var15.append(var6);
                  var15.append(": ");
                  var7 = var15.toString();
               }
            }

            var12 = new StringBuilder();
            var12.append(var7);
            var12.append((String)var3.get(var6));
            var12.append("\n");
            var11 = var12.toString();
            var5 = true;
         }
      }

      var15 = new StringBuilder();
      var15.append(var11);
      var15.append("\n");
      var7 = var15.toString();
      Map var13 = var2.getDataNumbers();
      if (var2.isSorted()) {
         var10 = new TreeSet(var13.keySet());
      } else {
         var10 = var13.keySet();
      }

      Iterator var14 = ((Collection)var10).iterator();

      for(var11 = var7; var14.hasNext(); var5 = true) {
         String var8 = (String)var14.next();
         var7 = var11;
         if (var8.length() > 0) {
            var7 = var11;
            if (var8.charAt(0) != 0) {
               var15 = new StringBuilder();
               var15.append(var11);
               var15.append(var8);
               var15.append(": ");
               var7 = var15.toString();
            }
         }

         var12 = new StringBuilder();
         var12.append(var7);
         var12.append(var13.get(var8));
         var12.append("\n");
         var11 = var12.toString();
      }

      var7 = var2.getTag();
      if (var7.equals("$System$None$")) {
         this.clearSystemTelemetry();
      } else if (var7.equals("$System$Error$")) {
         this.reportGlobalError((String)var3.get(var7), true);
      } else if (var7.equals("$System$Warning$")) {
         this.reportGlobalWarning((String)var3.get(var7));
      } else if (var7.equals("$RobotController$Battery$Status$")) {
         this.updateRcBatteryStatus(BatteryChecker.BatteryStatus.deserialize((String)var3.get(var7)));
      } else if (var7.equals("$Robot$Battery$Level$")) {
         this.showRobotBatteryVoltage((String)var3.get(var7));
      } else if (var5) {
         this.setUserTelemetry(var11);
      }

      return CallbackResult.HANDLED;
   }

   protected void traceUiStateChange(String var1, FtcDriverStationActivityBase.UIState var2) {
      RobotLog.vv("DriverStation", var1);
      this.uiState = var2;
      this.setTextView(this.textDsUiStateIndicator, var2.indicator);
      this.invalidateOptionsMenu();
   }

   protected void uiRobotCantContinue() {
      this.traceUiStateChange("ui:uiRobotCantContinue", FtcDriverStationActivityBase.UIState.CANT_CONTINUE);
      this.disableAndDimOpModeMenu();
      this.disableOpModeControls();
      this.dimControlPanelBack();
   }

   protected void uiRobotControllerIsConnected(FtcDriverStationActivityBase.ControlPanelBack var1) {
      this.traceUiStateChange("ui:uiRobotControllerIsConnected", FtcDriverStationActivityBase.UIState.CONNNECTED);
      this.enableAndBrightenForConnected(var1);
      AppUtil.getInstance().dismissAllDialogs(UILocation.ONLY_LOCAL);
      AppUtil.getInstance().dismissProgress(UILocation.ONLY_LOCAL);
      this.setTextView(this.rcBatteryTelemetry, "");
      this.setTextView(this.robotBatteryTelemetry, "");
      this.showWifiChannel();
      this.hideCameraStream();
   }

   protected void uiRobotControllerIsDisconnected() {
      this.traceUiStateChange("ui:uiRobotControllerIsDisconnected", FtcDriverStationActivityBase.UIState.DISCONNECTED);
      this.dimAndDisableAllControls();
   }

   protected void uiWaitingForInitEvent() {
      this.traceUiStateChange("ui:uiWaitingForInitEvent", FtcDriverStationActivityBase.UIState.WAITING_FOR_INIT_EVENT);
      this.checkConnectedEnableBrighten(FtcDriverStationActivityBase.ControlPanelBack.BRIGHT);
      this.brightenControlPanelBack();
      this.showQueuedOpModeName();
      this.enableAndBrightenOpModeMenu();
      this.setEnabled(this.buttonInit, true);
      this.setVisibility(this.buttonInit, 0);
      this.setVisibility(this.buttonStart, 4);
      this.setVisibility(this.buttonStop, 4);
      this.setVisibility(this.buttonInitStop, 4);
      this.setTimerButtonEnabled(true);
      this.setVisibility(this.timerAndTimerSwitch, 0);
      this.hideCameraStream();
   }

   protected void uiWaitingForOpModeSelection() {
      this.traceUiStateChange("ui:uiWaitingForOpModeSelection", FtcDriverStationActivityBase.UIState.WAITING_FOR_OPMODE_SELECTION);
      this.checkConnectedEnableBrighten(FtcDriverStationActivityBase.ControlPanelBack.DIM);
      this.dimControlPanelBack();
      this.enableAndBrightenOpModeMenu();
      this.showQueuedOpModeName();
      this.disableOpModeControls();
   }

   protected void uiWaitingForStartEvent() {
      this.traceUiStateChange("ui:uiWaitingForStartEvent", FtcDriverStationActivityBase.UIState.WAITING_FOR_START_EVENT);
      this.checkConnectedEnableBrighten(FtcDriverStationActivityBase.ControlPanelBack.BRIGHT);
      this.showQueuedOpModeName();
      this.enableAndBrightenOpModeMenu();
      this.setVisibility(this.buttonStart, 0);
      this.setVisibility(this.buttonInit, 4);
      this.setVisibility(this.buttonStop, 4);
      this.setVisibility(this.buttonInitStop, 0);
      this.setTimerButtonEnabled(true);
      this.setVisibility(this.timerAndTimerSwitch, 0);
      this.hideCameraStream();
   }

   protected void uiWaitingForStopEvent() {
      this.traceUiStateChange("ui:uiWaitingForStopEvent", FtcDriverStationActivityBase.UIState.WAITING_FOR_STOP_EVENT);
      this.checkConnectedEnableBrighten(FtcDriverStationActivityBase.ControlPanelBack.BRIGHT);
      this.showQueuedOpModeName();
      this.enableAndBrightenOpModeMenu();
      this.setVisibility(this.buttonStop, 0);
      this.setVisibility(this.buttonInit, 4);
      this.setVisibility(this.buttonStart, 4);
      this.setVisibility(this.buttonInitStop, 4);
      this.setTimerButtonEnabled(false);
      this.setVisibility(this.timerAndTimerSwitch, 0);
      this.hideCameraStream();
   }

   protected void updateLoggingPrefs() {
      boolean var1 = this.preferences.getBoolean(this.getString(2131624406), false);
      this.debugLogging = var1;
      this.gamepadManager.setDebug(var1);
      if (this.preferences.getBoolean(this.getString(2131624434), false)) {
         this.enableMatchLoggingUI();
      } else {
         this.disableMatchLoggingUI();
      }

   }

   protected abstract void updateRcBatteryStatus(BatteryChecker.BatteryStatus var1);

   protected int validateMatchEntry(String var1) {
      int var2;
      try {
         var2 = Integer.parseInt(var1);
      } catch (NumberFormatException var3) {
         RobotLog.logStackTrace(var3);
         return -1;
      }

      if (var2 >= 0 && var2 <= 1000) {
         return var2;
      } else {
         return -1;
      }
   }

   protected static enum ControlPanelBack {
      BRIGHT,
      DIM,
      NO_CHANGE;

      static {
         FtcDriverStationActivityBase.ControlPanelBack var0 = new FtcDriverStationActivityBase.ControlPanelBack("BRIGHT", 2);
         BRIGHT = var0;
      }
   }

   protected class DeviceNameManagerCallback implements DeviceNameListener {
      public void onDeviceNameChanged(String var1) {
         FtcDriverStationActivityBase.this.displayDeviceName(var1);
      }
   }

   private class OpModeCountDownTimer {
      public static final long MS_COUNTDOWN_INTERVAL = 30000L;
      public static final long MS_PER_S = 1000L;
      public static final long MS_TICK = 1000L;
      public static final long TICK_INTERVAL = 1L;
      private CountDownTimer countDownTimer = null;
      private boolean enabled = false;
      private long msRemaining = 30000L;
      private View timerStopWatch = FtcDriverStationActivityBase.this.findViewById(2131231155);
      private View timerSwitchOff = FtcDriverStationActivityBase.this.findViewById(2131231158);
      private View timerSwitchOn = FtcDriverStationActivityBase.this.findViewById(2131231159);
      private TextView timerText = (TextView)FtcDriverStationActivityBase.this.findViewById(2131231160);

      public OpModeCountDownTimer() {
      }

      private void displaySecondsRemaining(long var1) {
         if (this.enabled) {
            FtcDriverStationActivityBase.this.setTextView(this.timerText, String.valueOf(var1));
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

      public void enable() {
         if (!this.enabled) {
            FtcDriverStationActivityBase.this.setVisibility(this.timerText, 0);
            FtcDriverStationActivityBase.this.setVisibility(this.timerStopWatch, 8);
            FtcDriverStationActivityBase.this.setVisibility(this.timerSwitchOn, 0);
            FtcDriverStationActivityBase.this.setVisibility(this.timerSwitchOff, 8);
            this.enabled = true;
            this.displaySecondsRemaining(this.getSecondsRemaining());
         }

      }

      public long getSecondsRemaining() {
         return this.msRemaining / 1000L;
      }

      public boolean isEnabled() {
         return this.enabled;
      }

      public void resetCountdown() {
         this.setMsRemaining(30000L);
      }

      public void setMsRemaining(long var1) {
         this.msRemaining = var1;
         if (this.enabled) {
            this.displaySecondsRemaining(var1 / 1000L);
         }

      }

      public void start() {
         if (this.enabled) {
            StringBuilder var1 = new StringBuilder();
            var1.append("Starting to run current op mode for ");
            var1.append(this.getSecondsRemaining());
            var1.append(" seconds");
            RobotLog.vv("DriverStation", var1.toString());
            FtcDriverStationActivityBase.this.appUtil.synchronousRunOnUiThread(new Runnable() {
               public void run() {
                  CountDownTimer var1 = OpModeCountDownTimer.this.countDownTimer;
                  if (var1 != null) {
                     var1.cancel();
                  }

                  OpModeCountDownTimer.this.countDownTimer = (new CountDownTimer(OpModeCountDownTimer.this.msRemaining, 1000L) {
                     public void onFinish() {
                        FtcDriverStationActivityBase.this.assertUiThread();
                        RobotLog.vv("DriverStation", "Stopping current op mode, timer expired");
                        OpModeCountDownTimer.this.resetCountdown();
                        FtcDriverStationActivityBase.this.handleOpModeStop();
                     }

                     public void onTick(long var1) {
                        FtcDriverStationActivityBase.this.assertUiThread();
                        OpModeCountDownTimer.this.setMsRemaining(var1);
                        StringBuilder var3 = new StringBuilder();
                        var3.append("Running current op mode for ");
                        var3.append(var1 / 1000L);
                        var3.append(" seconds");
                        RobotLog.vv("DriverStation", var3.toString());
                     }
                  }).start();
               }
            });
         }

      }

      public void stop() {
         FtcDriverStationActivityBase.this.appUtil.synchronousRunOnUiThread(new Runnable() {
            public void run() {
               if (OpModeCountDownTimer.this.countDownTimer != null) {
                  OpModeCountDownTimer.this.countDownTimer.cancel();
                  OpModeCountDownTimer.this.countDownTimer = null;
               }

            }
         });
      }

      public void stopPreservingRemainingTime() {
         // $FF: Couldn't be decompiled
      }
   }

   protected static enum UIState {
      CANT_CONTINUE("E"),
      CONNNECTED("C"),
      DISCONNECTED("X"),
      ROBOT_STOPPED,
      UNKNOWN("U"),
      WAITING_FOR_ACK("KW"),
      WAITING_FOR_INIT_EVENT("K"),
      WAITING_FOR_OPMODE_SELECTION("M"),
      WAITING_FOR_START_EVENT("S"),
      WAITING_FOR_STOP_EVENT("P");

      public final String indicator;

      static {
         FtcDriverStationActivityBase.UIState var0 = new FtcDriverStationActivityBase.UIState("ROBOT_STOPPED", 9, "Z");
         ROBOT_STOPPED = var0;
      }

      private UIState(String var3) {
         this.indicator = var3;
      }
   }
}
