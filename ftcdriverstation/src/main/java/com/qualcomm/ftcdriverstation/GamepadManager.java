package com.qualcomm.ftcdriverstation;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.input.InputManager.InputDeviceListener;
import android.preference.PreferenceManager;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;
import com.qualcomm.ftccommon.SoundPlayer;
import com.qualcomm.hardware.logitech.LogitechGamepadF310;
import com.qualcomm.hardware.microsoft.MicrosoftGamepadXbox360;
import com.qualcomm.hardware.sony.SonyGamepadPS4;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.firstinspires.ftc.robotcore.internal.ui.GamepadUser;
import org.firstinspires.ftc.robotcore.internal.ui.RobotCoreGamepadManager;

public class GamepadManager implements RobotCoreGamepadManager, InputDeviceListener {
   protected static int SOUND_ID_GAMEPAD_CONNECT;
   protected static int SOUND_ID_GAMEPAD_DISCONNECT;
   public static final String TAG = "GamepadManager";
   protected final Context context;
   protected boolean debug = false;
   protected boolean enabled = true;
   protected Map gamepadIdToGamepadMap = new ConcurrentHashMap();
   protected Map gamepadIdToPlaceholderMap = new ConcurrentHashMap();
   protected Map gamepadIndicators = null;
   protected SharedPreferences preferences;
   protected Set recentlyUnassignedUsers = Collections.newSetFromMap(new ConcurrentHashMap());
   protected ArrayList removedGamepadMemories = new ArrayList();
   protected GamepadTypeOverrideMapper typeOverrideMapper;
   protected Map userToGamepadIdMap = new ConcurrentHashMap();

   public GamepadManager(Context var1) {
      this.context = var1;
      this.typeOverrideMapper = new GamepadTypeOverrideMapper(var1);
      SoundPlayer.getInstance().preload(var1, SOUND_ID_GAMEPAD_CONNECT);
      SoundPlayer.getInstance().preload(var1, SOUND_ID_GAMEPAD_DISCONNECT);
   }

   public void assignGamepad(int var1, GamepadUser var2) {
      synchronized(this){}

      Throwable var10000;
      label717: {
         boolean var10001;
         try {
            if (this.debug) {
               RobotLog.dd("GamepadManager", "assigning gampadId=%d user=%s", var1, var2);
            }
         } catch (Throwable var78) {
            var10000 = var78;
            var10001 = false;
            break label717;
         }

         Iterator var3;
         try {
            var3 = this.removedGamepadMemories.iterator();
         } catch (Throwable var77) {
            var10000 = var77;
            var10001 = false;
            break label717;
         }

         try {
            while(var3.hasNext()) {
               GamepadManager.RemovedGamepadMemory var4 = (GamepadManager.RemovedGamepadMemory)var3.next();
               if (var4.user == var2) {
                  this.removedGamepadMemories.remove(var4);
                  break;
               }
            }
         } catch (Throwable var76) {
            var10000 = var76;
            var10001 = false;
            break label717;
         }

         Iterator var5;
         try {
            var5 = this.userToGamepadIdMap.entrySet().iterator();
         } catch (Throwable var74) {
            var10000 = var74;
            var10001 = false;
            break label717;
         }

         while(true) {
            Entry var80;
            Integer var82;
            try {
               if (!var5.hasNext()) {
                  break;
               }

               var80 = (Entry)var5.next();
               if ((Integer)var80.getValue() != var1) {
                  continue;
               }

               var82 = (Integer)this.userToGamepadIdMap.get(var2);
            } catch (Throwable var75) {
               var10000 = var75;
               var10001 = false;
               break label717;
            }

            if (var82 != null) {
               int var6;
               try {
                  var6 = var82;
               } catch (Throwable var73) {
                  var10000 = var73;
                  var10001 = false;
                  break label717;
               }

               if (var6 == var1) {
                  return;
               }
            }

            try {
               this.internalUnassignUser((GamepadUser)var80.getKey());
            } catch (Throwable var72) {
               var10000 = var72;
               var10001 = false;
               break label717;
            }
         }

         label685:
         try {
            this.userToGamepadIdMap.put(var2, var1);
            this.recentlyUnassignedUsers.remove(var2);
            ((GamepadIndicator)this.gamepadIndicators.get(var2)).setState(GamepadIndicator.State.VISIBLE);
            Gamepad var81 = (Gamepad)this.gamepadIdToGamepadMap.get(var1);
            var81.setUser(var2);
            var81.refreshTimestamp();
            RobotLog.vv("GamepadManager", "assigned id=%d user=%s type=%s class=%s", var1, var2, var81.type(), var81.getClass().getSimpleName());
            SoundPlayer.getInstance().play(this.context, SOUND_ID_GAMEPAD_CONNECT, 1.0F, 0, 1.0F);
            return;
         } catch (Throwable var71) {
            var10000 = var71;
            var10001 = false;
            break label685;
         }
      }

      Throwable var79 = var10000;
      throw var79;
   }

   public void automagicallyReassignIfPossible(InputDevice var1, GamepadManager.RemovedGamepadMemory var2) {
      if (var2.memoriesAmbiguousIfBothPositionsEmpty() && this.userToGamepadIdMap.isEmpty()) {
         this.removedGamepadMemories.clear();
         RobotLog.vv("GamepadManager", "Input device %d was considered for automatic recovery after dropping off the USB bus; however due to ambiguity, no recovery will be performed, and all memories of previously connected gamepads will be forgotten.", var1.getId());
         Toast.makeText(this.context, "Gamepad not recovered due to ambiguity", 0).show();
      } else {
         if (this.userToGamepadIdMap.get(var2.user) == null) {
            Object var3;
            if (var2.type == Gamepad.Type.XBOX_360) {
               var3 = new MicrosoftGamepadXbox360();
            } else if (var2.type == Gamepad.Type.LOGITECH_F310) {
               var3 = new LogitechGamepadF310();
            } else {
               if (var2.type != Gamepad.Type.SONY_PS4) {
                  throw new IllegalStateException();
               }

               var3 = new SonyGamepadPS4();
            }

            RobotLog.vv("GamepadManager", "Input device %d has been automagically recovered based on USB VID and PID after dropping off the USB bus; treating as %s because that's what we were treating it as when it dropped.", var1.getId(), var3.getClass().getSimpleName());
            ((Gamepad)var3).setVidPid(var1.getVendorId(), var1.getProductId());
            this.gamepadIdToGamepadMap.put(var1.getId(), var3);
            this.assignGamepad(var1.getId(), var2.user);
            Toast.makeText(this.context, String.format("Gamepad %d auto-recovered", var2.user.id), 0).show();
         }

      }
   }

   public void clearGamepadAssignments() {
      synchronized(this){}

      Throwable var10000;
      label132: {
         Iterator var1;
         boolean var10001;
         try {
            var1 = this.userToGamepadIdMap.keySet().iterator();
         } catch (Throwable var12) {
            var10000 = var12;
            var10001 = false;
            break label132;
         }

         while(true) {
            try {
               if (var1.hasNext()) {
                  this.unassignUser((GamepadUser)var1.next());
                  continue;
               }
            } catch (Throwable var13) {
               var10000 = var13;
               var10001 = false;
               break;
            }

            try {
               this.removedGamepadMemories.clear();
               return;
            } catch (Throwable var11) {
               var10000 = var11;
               var10001 = false;
               break;
            }
         }
      }

      Throwable var14 = var10000;
      throw var14;
   }

   public void clearTrackedGamepads() {
      synchronized(this){}

      try {
         this.gamepadIdToGamepadMap.clear();
         this.gamepadIdToPlaceholderMap.clear();
      } finally {
         ;
      }

   }

   public void close() {
   }

   public void considerInputDeviceForAutomagicReassignment(InputDevice var1) {
      Iterator var2 = this.removedGamepadMemories.iterator();

      GamepadManager.RemovedGamepadMemory var3;
      do {
         if (!var2.hasNext()) {
            var3 = null;
            break;
         }

         var3 = (GamepadManager.RemovedGamepadMemory)var2.next();
      } while(!var3.isTargetForAutomagicReassignment(var1));

      if (var3 != null) {
         try {
            this.automagicallyReassignIfPossible(var1, var3);
         } catch (IllegalStateException var4) {
            var4.printStackTrace();
         }

         this.removedGamepadMemories.remove(var3);
      }

   }

   public Gamepad getAssignedGamepadById(Integer var1) {
      synchronized(this){}
      if (var1 != null) {
         try {
            Iterator var2 = this.userToGamepadIdMap.entrySet().iterator();

            while(var2.hasNext()) {
               Entry var3 = (Entry)var2.next();
               if (((Integer)var3.getValue()).equals(var1)) {
                  Gamepad var6 = this.getGamepadById((Integer)var3.getValue());
                  return var6;
               }
            }
         } finally {
            ;
         }
      }

      return null;
   }

   public Gamepad getGamepadById(Integer var1) {
      synchronized(this){}
      if (var1 != null) {
         Gamepad var4;
         try {
            var4 = (Gamepad)this.gamepadIdToGamepadMap.get(var1);
         } finally {
            ;
         }

         return var4;
      } else {
         return null;
      }
   }

   public List getGamepadsForTransmission() {
      synchronized(this){}

      Throwable var10000;
      label351: {
         boolean var10001;
         try {
            if (!this.enabled) {
               List var36 = Collections.emptyList();
               return var36;
            }
         } catch (Throwable var34) {
            var10000 = var34;
            var10001 = false;
            break label351;
         }

         ArrayList var1;
         Iterator var2;
         try {
            var1 = new ArrayList(2);
            var2 = this.userToGamepadIdMap.entrySet().iterator();
         } catch (Throwable var32) {
            var10000 = var32;
            var10001 = false;
            break label351;
         }

         while(true) {
            try {
               if (!var2.hasNext()) {
                  break;
               }

               var1.add(this.getGamepadById((Integer)((Entry)var2.next()).getValue()));
            } catch (Throwable var33) {
               var10000 = var33;
               var10001 = false;
               break label351;
            }
         }

         try {
            var2 = this.recentlyUnassignedUsers.iterator();
         } catch (Throwable var31) {
            var10000 = var31;
            var10001 = false;
            break label351;
         }

         while(true) {
            try {
               if (var2.hasNext()) {
                  GamepadUser var3 = (GamepadUser)var2.next();
                  RobotLog.vv("GamepadManager", "transmitting synthetic gamepad user=%s", var3);
                  Gamepad var4 = new Gamepad();
                  var4.setGamepadId(-2);
                  var4.refreshTimestamp();
                  var4.setUser(var3);
                  var1.add(var4);
                  this.recentlyUnassignedUsers.remove(var3);
                  continue;
               }
            } catch (Throwable var30) {
               var10000 = var30;
               var10001 = false;
               break;
            }

            return var1;
         }
      }

      Throwable var35 = var10000;
      throw var35;
   }

   public Gamepad guessGamepadType(KeyEvent var1, GamepadManager.GamepadUserPointer var2) {
      synchronized(this){}

      Throwable var10000;
      label1095: {
         GamepadManager.GamepadPlaceholder var3;
         boolean var10001;
         try {
            var3 = (GamepadManager.GamepadPlaceholder)this.gamepadIdToPlaceholderMap.get(var1.getDeviceId());
         } catch (Throwable var112) {
            var10000 = var112;
            var10001 = false;
            break label1095;
         }

         GamepadManager.GamepadPlaceholder var4 = var3;
         if (var3 == null) {
            try {
               var4 = new GamepadManager.GamepadPlaceholder();
               this.gamepadIdToPlaceholderMap.put(var1.getDeviceId(), var4);
            } catch (Throwable var111) {
               var10000 = var111;
               var10001 = false;
               break label1095;
            }
         }

         label1099: {
            label1096: {
               try {
                  var4.update(var1);
                  if (!var4.key105depressed) {
                     break label1096;
                  }

                  if (!var4.key97depressed && !var4.key98depressed) {
                     return null;
                  }
               } catch (Throwable var114) {
                  var10000 = var114;
                  var10001 = false;
                  break label1095;
               }

               label1046: {
                  try {
                     this.gamepadIdToPlaceholderMap.remove(var1.getDeviceId());
                     if (var4.key97depressed) {
                        var2.val = GamepadUser.ONE;
                        break label1046;
                     }
                  } catch (Throwable var107) {
                     var10000 = var107;
                     var10001 = false;
                     break label1095;
                  }

                  try {
                     var2.val = GamepadUser.TWO;
                  } catch (Throwable var106) {
                     var10000 = var106;
                     var10001 = false;
                     break label1095;
                  }
               }

               SonyGamepadPS4 var115;
               try {
                  var115 = new SonyGamepadPS4();
               } catch (Throwable var105) {
                  var10000 = var105;
                  var10001 = false;
                  break label1095;
               }

               return var115;
            }

            try {
               if (var4.key108depressed && (var4.key96depressed || var4.key97depressed)) {
                  break label1099;
               }
            } catch (Throwable var113) {
               var10000 = var113;
               var10001 = false;
               break label1095;
            }

            return null;
         }

         label1060: {
            try {
               this.gamepadIdToPlaceholderMap.remove(var1.getDeviceId());
               if (var4.key96depressed) {
                  var2.val = GamepadUser.ONE;
                  break label1060;
               }
            } catch (Throwable var110) {
               var10000 = var110;
               var10001 = false;
               break label1095;
            }

            try {
               var2.val = GamepadUser.TWO;
            } catch (Throwable var109) {
               var10000 = var109;
               var10001 = false;
               break label1095;
            }
         }

         MicrosoftGamepadXbox360 var117;
         try {
            var117 = new MicrosoftGamepadXbox360();
         } catch (Throwable var108) {
            var10000 = var108;
            var10001 = false;
            break label1095;
         }

         return var117;
      }

      Throwable var116 = var10000;
      throw var116;
   }

   public void handleGamepadEvent(KeyEvent var1) {
      synchronized(this){}

      Throwable var10000;
      label1235: {
         InputDevice var2;
         Gamepad var3;
         boolean var10001;
         try {
            var2 = InputDevice.getDevice(var1.getDeviceId());
            var3 = (Gamepad)this.gamepadIdToGamepadMap.get(var2.getId());
         } catch (Throwable var156) {
            var10000 = var156;
            var10001 = false;
            break label1235;
         }

         if (var3 != null) {
            label1236: {
               try {
                  if (!var3.start || !var3.a && !var3.b) {
                     break label1236;
                  }
               } catch (Throwable var157) {
                  var10000 = var157;
                  var10001 = false;
                  break label1235;
               }

               try {
                  if (var3.a) {
                     this.assignGamepad(var2.getId(), GamepadUser.ONE);
                  }
               } catch (Throwable var158) {
                  var10000 = var158;
                  var10001 = false;
                  break label1235;
               }

               try {
                  if (var3.b) {
                     this.assignGamepad(var2.getId(), GamepadUser.TWO);
                  }
               } catch (Throwable var155) {
                  var10000 = var155;
                  var10001 = false;
                  break label1235;
               }
            }

            try {
               var3.update(var1);
               this.indicateGamepad(this.getAssignedGamepadById(var1.getDeviceId()));
            } catch (Throwable var154) {
               var10000 = var154;
               var10001 = false;
               break label1235;
            }
         } else {
            try {
               var3 = this.knownInputDeviceToGamepad(var2);
            } catch (Throwable var153) {
               var10000 = var153;
               var10001 = false;
               break label1235;
            }

            if (var3 != null) {
               try {
                  var3.update(var1);
                  var3.setVidPid(var2.getVendorId(), var2.getProductId());
                  this.gamepadIdToGamepadMap.put(var2.getId(), var3);
                  RobotLog.vv("GamepadManager", "Input device %d has been autodetected based on USB VID and PID as %s", var2.getId(), var3.getClass().getSimpleName());
               } catch (Throwable var148) {
                  var10000 = var148;
                  var10001 = false;
                  break label1235;
               }

               return;
            }

            try {
               var3 = this.overriddenInputDeviceToGamepad(var2);
            } catch (Throwable var152) {
               var10000 = var152;
               var10001 = false;
               break label1235;
            }

            if (var3 != null) {
               try {
                  var3.update(var1);
                  var3.setVidPid(var2.getVendorId(), var2.getProductId());
                  this.gamepadIdToGamepadMap.put(var2.getId(), var3);
                  RobotLog.vv("GamepadManager", "Input device %d has a USB VID and PID that has an entry in override list; treating as %s", var2.getId(), var3.getClass().getSimpleName());
               } catch (Throwable var149) {
                  var10000 = var149;
                  var10001 = false;
                  break label1235;
               }

               return;
            }

            Gamepad var160;
            GamepadManager.GamepadUserPointer var162;
            try {
               var162 = new GamepadManager.GamepadUserPointer();
               var160 = this.guessGamepadType(var1, var162);
            } catch (Throwable var151) {
               var10000 = var151;
               var10001 = false;
               break label1235;
            }

            if (var160 != null) {
               try {
                  var160.setVidPid(var2.getVendorId(), var2.getProductId());
                  this.gamepadIdToGamepadMap.put(var2.getId(), var160);
                  RobotLog.vv("GamepadManager", "Using quantum superposition to guess that input device %d should be treated as %s", var2.getId(), var160.getClass().getSimpleName());
                  if (var162.val == GamepadUser.ONE) {
                     this.assignGamepad(var2.getId(), GamepadUser.ONE);
                     return;
                  }
               } catch (Throwable var159) {
                  var10000 = var159;
                  var10001 = false;
                  break label1235;
               }

               try {
                  this.assignGamepad(var2.getId(), GamepadUser.TWO);
               } catch (Throwable var150) {
                  var10000 = var150;
                  var10001 = false;
                  break label1235;
               }
            }
         }

         return;
      }

      Throwable var161 = var10000;
      throw var161;
   }

   public void handleGamepadEvent(MotionEvent var1) {
      synchronized(this){}

      Throwable var10000;
      label78: {
         boolean var10001;
         Gamepad var2;
         try {
            var2 = this.getGamepadById(var1.getDeviceId());
         } catch (Throwable var8) {
            var10000 = var8;
            var10001 = false;
            break label78;
         }

         if (var2 == null) {
            return;
         }

         try {
            var2.update(var1);
            this.indicateGamepad(this.getAssignedGamepadById(var1.getDeviceId()));
         } catch (Throwable var7) {
            var10000 = var7;
            var10001 = false;
            break label78;
         }

         return;
      }

      Throwable var9 = var10000;
      throw var9;
   }

   protected void indicateGamepad(Gamepad var1) {
      if (var1 != null) {
         ((GamepadIndicator)this.gamepadIndicators.get(var1.getUser())).setState(GamepadIndicator.State.INDICATE);
      }

   }

   protected void internalUnassignUser(GamepadUser var1) {
      ((GamepadIndicator)this.gamepadIndicators.get(var1)).setState(GamepadIndicator.State.INVISIBLE);
      this.userToGamepadIdMap.remove(var1);
      this.recentlyUnassignedUsers.add(var1);
   }

   public Gamepad knownInputDeviceToGamepad(InputDevice var1) {
      synchronized(this){}

      MicrosoftGamepadXbox360 var6;
      try {
         int var2 = var1.getVendorId();
         int var3 = var1.getProductId();
         if (!MicrosoftGamepadXbox360.matchesVidPid(var2, var3)) {
            if (LogitechGamepadF310.matchesVidPid(var2, var3)) {
               LogitechGamepadF310 var8 = new LogitechGamepadF310();
               return var8;
            }

            if (!SonyGamepadPS4.matchesVidPid(var2, var3)) {
               return null;
            }

            SonyGamepadPS4 var7 = new SonyGamepadPS4();
            return var7;
         }

         var6 = new MicrosoftGamepadXbox360();
      } finally {
         ;
      }

      return var6;
   }

   public void onAssignedGamepadDropped(Gamepad var1) {
      SoundPlayer.getInstance().play(this.context, SOUND_ID_GAMEPAD_DISCONNECT, 1.0F, 0, 1.0F);
      Toast.makeText(this.context, String.format("Gamepad %d connection lost", var1.getUser().id), 0).show();
      GamepadManager.RemovedGamepadMemory var2 = new GamepadManager.RemovedGamepadMemory();
      var2.vid = var1.vid;
      var2.pid = var1.pid;
      var2.user = var1.getUser();
      var2.type = var1.type();
      if (var1.getUser() == GamepadUser.ONE) {
         var1 = this.getAssignedGamepadById((Integer)this.userToGamepadIdMap.get(GamepadUser.TWO));
      } else {
         var1 = this.getAssignedGamepadById((Integer)this.userToGamepadIdMap.get(GamepadUser.ONE));
      }

      if (var1 != null) {
         var2.othergamepad_vid = var1.vid;
         var2.othergamepad_pid = var1.pid;
      }

      this.removedGamepadMemories.add(var2);
   }

   public void onInputDeviceAdded(int var1) {
      synchronized(this){}

      try {
         RobotLog.vv("GamepadManager", String.format("New input device (id = %d) detected.", var1));
         this.considerInputDeviceForAutomagicReassignment(InputDevice.getDevice(var1));
      } finally {
         ;
      }

   }

   public void onInputDeviceChanged(int var1) {
      synchronized(this){}

      try {
         RobotLog.vv("GamepadManager", String.format("Input device (id = %d) modified.", var1));
      } finally {
         ;
      }

   }

   public void onInputDeviceRemoved(int var1) {
      synchronized(this){}

      Throwable var10000;
      label116: {
         boolean var10001;
         Gamepad var2;
         try {
            RobotLog.vv("GamepadManager", String.format("Input device (id = %d) removed.", var1));
            var2 = this.getAssignedGamepadById(var1);
         } catch (Throwable var14) {
            var10000 = var14;
            var10001 = false;
            break label116;
         }

         if (var2 != null) {
            try {
               this.onAssignedGamepadDropped(var2);
            } catch (Throwable var13) {
               var10000 = var13;
               var10001 = false;
               break label116;
            }
         }

         label104:
         try {
            this.removeGamepad(var1);
            return;
         } catch (Throwable var12) {
            var10000 = var12;
            var10001 = false;
            break label104;
         }
      }

      Throwable var15 = var10000;
      throw var15;
   }

   public void open() {
      this.preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
   }

   public Gamepad overriddenInputDeviceToGamepad(InputDevice var1) {
      synchronized(this){}

      Throwable var10000;
      label78: {
         boolean var10001;
         GamepadTypeOverrideMapper.GamepadTypeOverrideEntry var10;
         try {
            int var2 = var1.getVendorId();
            int var3 = var1.getProductId();
            var10 = this.typeOverrideMapper.getEntryFor(var2, var3);
         } catch (Throwable var9) {
            var10000 = var9;
            var10001 = false;
            break label78;
         }

         if (var10 == null) {
            return null;
         }

         Gamepad var12;
         try {
            var12 = var10.createGamepad();
         } catch (Throwable var8) {
            var10000 = var8;
            var10001 = false;
            break label78;
         }

         return var12;
      }

      Throwable var11 = var10000;
      throw var11;
   }

   public void removeGamepad(int var1) {
      synchronized(this){}

      Throwable var10000;
      label132: {
         boolean var10001;
         Gamepad var2;
         try {
            var2 = this.getAssignedGamepadById(var1);
         } catch (Throwable var14) {
            var10000 = var14;
            var10001 = false;
            break label132;
         }

         if (var2 != null) {
            try {
               this.internalUnassignUser(var2.getUser());
            } catch (Throwable var13) {
               var10000 = var13;
               var10001 = false;
               break label132;
            }
         }

         label120:
         try {
            this.gamepadIdToGamepadMap.remove(var1);
            if (this.gamepadIdToPlaceholderMap.get(var1) != null) {
               this.gamepadIdToPlaceholderMap.remove(var1);
            }

            return;
         } catch (Throwable var12) {
            var10000 = var12;
            var10001 = false;
            break label120;
         }
      }

      Throwable var15 = var10000;
      throw var15;
   }

   public void setDebug(boolean var1) {
      this.debug = var1;
   }

   public void setEnabled(boolean var1) {
      this.enabled = var1;
   }

   public void setGamepadIndicators(Map var1) {
      this.gamepadIndicators = var1;
   }

   public void unassignUser(GamepadUser var1) {
      synchronized(this){}

      Throwable var10000;
      label116: {
         boolean var10001;
         Integer var2;
         try {
            var2 = (Integer)this.userToGamepadIdMap.get(var1);
         } catch (Throwable var14) {
            var10000 = var14;
            var10001 = false;
            break label116;
         }

         if (var2 != null) {
            try {
               this.gamepadIdToGamepadMap.remove(var2);
            } catch (Throwable var13) {
               var10000 = var13;
               var10001 = false;
               break label116;
            }
         }

         label104:
         try {
            this.internalUnassignUser(var1);
            return;
         } catch (Throwable var12) {
            var10000 = var12;
            var10001 = false;
            break label104;
         }
      }

      Throwable var15 = var10000;
      throw var15;
   }

   class GamepadPlaceholder {
      boolean key105depressed = false;
      boolean key108depressed = false;
      boolean key96depressed = false;
      boolean key97depressed = false;
      boolean key98depressed = false;

      boolean pressed(KeyEvent var1) {
         boolean var2;
         if (var1.getAction() == 0) {
            var2 = true;
         } else {
            var2 = false;
         }

         return var2;
      }

      void update(KeyEvent var1) {
         if (var1.getKeyCode() == 105) {
            this.key105depressed = this.pressed(var1);
         } else if (var1.getKeyCode() == 108) {
            this.key108depressed = this.pressed(var1);
         } else if (var1.getKeyCode() == 96) {
            this.key96depressed = this.pressed(var1);
         } else if (var1.getKeyCode() == 97) {
            this.key97depressed = this.pressed(var1);
         } else if (var1.getKeyCode() == 98) {
            this.key98depressed = this.pressed(var1);
         }

      }
   }

   class GamepadUserPointer {
      GamepadUser val;
   }

   class RemovedGamepadMemory {
      int othergamepad_pid;
      int othergamepad_vid;
      int pid;
      Gamepad.Type type;
      GamepadUser user;
      int vid;

      boolean isTargetForAutomagicReassignment(InputDevice var1) {
         boolean var2;
         if (var1.getVendorId() == this.vid && var1.getProductId() == this.pid) {
            var2 = true;
         } else {
            var2 = false;
         }

         return var2;
      }

      boolean memoriesAmbiguousIfBothPositionsEmpty() {
         boolean var1;
         if (this.vid == this.othergamepad_vid && this.pid == this.othergamepad_pid) {
            var1 = true;
         } else {
            var1 = false;
         }

         return var1;
      }
   }
}
