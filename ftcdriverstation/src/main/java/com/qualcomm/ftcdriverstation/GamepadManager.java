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
   protected static int SOUND_ID_GAMEPAD_CONNECT = R.raw.controller_connection;
   protected static int SOUND_ID_GAMEPAD_DISCONNECT = R.raw.controller_dropped;
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

   public GamepadManager(Context context) {
      this.context = context;
      this.typeOverrideMapper = new GamepadTypeOverrideMapper(context);
      SoundPlayer.getInstance().preload(context, SOUND_ID_GAMEPAD_CONNECT);
      SoundPlayer.getInstance().preload(context, SOUND_ID_GAMEPAD_DISCONNECT);
   }

   public synchronized void assignGamepad(int i, GamepadUser gamepadUser) {
      if (this.debug) {
         RobotLog.dd(TAG, "assigning gampadId=%d user=%s", i, gamepadUser);
      }
      Iterator<RemovedGamepadMemory> it = this.removedGamepadMemories.iterator();
      while (true) {
         if (!it.hasNext()) {
            break;
         }
         RemovedGamepadMemory next = it.next();
         if (next.user == gamepadUser) {
            this.removedGamepadMemories.remove(next);
            break;
         }
      }
      for (Map.Entry next2 : this.userToGamepadIdMap.entrySet()) {
         if (((Integer) next2.getValue()).intValue() == i) {
            Integer num = this.userToGamepadIdMap.get(gamepadUser);
            if (num == null || num.intValue() != i) {
               internalUnassignUser((GamepadUser) next2.getKey());
            } else {
               return;
            }
         }
      }
      this.userToGamepadIdMap.put(gamepadUser, Integer.valueOf(i));
      this.recentlyUnassignedUsers.remove(gamepadUser);
      this.gamepadIndicators.get(gamepadUser).setState(GamepadIndicator.State.VISIBLE);
      Gamepad gamepad = this.gamepadIdToGamepadMap.get(Integer.valueOf(i));
      gamepad.setUser(gamepadUser);
      gamepad.refreshTimestamp();
      RobotLog.vv(TAG, "assigned id=%d user=%s type=%s class=%s", Integer.valueOf(i), gamepadUser, gamepad.type(), gamepad.getClass().getSimpleName());
      SoundPlayer.getInstance().play(this.context, SOUND_ID_GAMEPAD_CONNECT, 1.0f, 0, 1.0f);

   }
   }

   public void automagicallyReassignIfPossible(InputDevice inputDevice, RemovedGamepadMemory removedGamepadMemory) {
      MicrosoftGamepadXbox360 microsoftGamepadXbox360;
      if (removedGamepadMemory.memoriesAmbiguousIfBothPositionsEmpty() && this.userToGamepadIdMap.isEmpty()) {
         this.removedGamepadMemories.clear();
         RobotLog.vv((String)TAG, (String)"Input device %d was considered for automatic recovery after dropping off the USB bus; however due to ambiguity, no recovery will be performed, and all memories of previously connected gamepads will be forgotten.", (Object[])new Object[]{inputDevice.getId()});
         Toast.makeText((Context)this.context, (CharSequence)"Gamepad not recovered due to ambiguity", (int)0).show();
         return;
      }
      if (this.userToGamepadIdMap.get((Object)removedGamepadMemory.user) != null) return;
      if (removedGamepadMemory.type == Gamepad.Type.XBOX_360) {
         microsoftGamepadXbox360 = new MicrosoftGamepadXbox360();
      } else if (removedGamepadMemory.type == Gamepad.Type.LOGITECH_F310) {
         microsoftGamepadXbox360 = new LogitechGamepadF310();
      } else {
         if (removedGamepadMemory.type != Gamepad.Type.SONY_PS4) throw new IllegalStateException();
         microsoftGamepadXbox360 = new SonyGamepadPS4();
      }
      RobotLog.vv((String)TAG, (String)"Input device %d has been automagically recovered based on USB VID and PID after dropping off the USB bus; treating as %s because that's what we were treating it as when it dropped.", (Object[])new Object[]{inputDevice.getId(), microsoftGamepadXbox360.getClass().getSimpleName()});
      microsoftGamepadXbox360.setVidPid(inputDevice.getVendorId(), inputDevice.getProductId());
      this.gamepadIdToGamepadMap.put(inputDevice.getId(), microsoftGamepadXbox360);
      this.assignGamepad(inputDevice.getId(), removedGamepadMemory.user);
      Toast.makeText((Context)this.context, (CharSequence)String.format("Gamepad %d auto-recovered", removedGamepadMemory.user.id), (int)0).show();
   }

   public void clearGamepadAssignments() {
      synchronized (this) {
         Iterator iterator = this.userToGamepadIdMap.keySet().iterator();
         do {
            if (!iterator.hasNext()) {
               this.removedGamepadMemories.clear();
               return;
            }
            this.unassignUser((GamepadUser)iterator.next());
         } while (true);
      }
   }

   public void clearTrackedGamepads() {
      synchronized (this) {
         this.gamepadIdToGamepadMap.clear();
         this.gamepadIdToPlaceholderMap.clear();
         return;
      }
   }

   public void close() {
   }

   public void considerInputDeviceForAutomagicReassignment(InputDevice inputDevice) {
      RemovedGamepadMemory removedGamepadMemory2;
      block3 : {
         for (RemovedGamepadMemory removedGamepadMemory2 : this.removedGamepadMemories) {
            if (!removedGamepadMemory2.isTargetForAutomagicReassignment(inputDevice)) continue;
            break block3;
         }
         removedGamepadMemory2 = null;
      }
      if (removedGamepadMemory2 == null) return;
      try {
         this.automagicallyReassignIfPossible(inputDevice, removedGamepadMemory2);
      }
      catch (IllegalStateException illegalStateException) {
         illegalStateException.printStackTrace();
      }
      this.removedGamepadMemories.remove((Object)removedGamepadMemory2);
   }

   public Gamepad getAssignedGamepadById(Integer n) {
      synchronized (this) {
         Map.Entry entry;
         if (n == null) return null;
         Iterator iterator = this.userToGamepadIdMap.entrySet().iterator();
         do {
            if (!iterator.hasNext()) return null;
         } while (!((Integer)(entry = iterator.next()).getValue()).equals(n));
         n = this.getGamepadById((Integer)entry.getValue());
         return n;
      }
   }

   public Gamepad getGamepadById(Integer n) {
      synchronized (this) {
         if (n == null) return null;
         return (Gamepad)this.gamepadIdToGamepadMap.get(n);
      }
   }

   public List<Gamepad> getGamepadsForTransmission() {
      synchronized (this) {
         if (!this.enabled) {
            return Collections.emptyList();
         }
         ArrayList<Gamepad> arrayList = new ArrayList<Gamepad>(2);
         Gamepad gamepad = this.userToGamepadIdMap.entrySet().iterator();
         while (gamepad.hasNext()) {
            arrayList.add(this.getGamepadById((Integer)gamepad.next().getValue()));
         }
         Iterator iterator = this.recentlyUnassignedUsers.iterator();
         while (iterator.hasNext()) {
            GamepadUser gamepadUser = (GamepadUser)iterator.next();
            RobotLog.vv((String)TAG, (String)"transmitting synthetic gamepad user=%s", (Object[])new Object[]{gamepadUser});
            gamepad = new Gamepad();
            gamepad.setGamepadId(-2);
            gamepad.refreshTimestamp();
            gamepad.setUser(gamepadUser);
            arrayList.add(gamepad);
            this.recentlyUnassignedUsers.remove((Object)gamepadUser);
         }
         return arrayList;
      }
   }

   public Gamepad guessGamepadType(KeyEvent keyEvent, GamepadUserPointer gamepadUserPointer) {
      synchronized (this) {
         GamepadPlaceholder gamepadPlaceholder;
         GamepadPlaceholder gamepadPlaceholder2 = gamepadPlaceholder = (GamepadPlaceholder)this.gamepadIdToPlaceholderMap.get(keyEvent.getDeviceId());
         if (gamepadPlaceholder == null) {
            gamepadPlaceholder2 = new GamepadPlaceholder(this);
            this.gamepadIdToPlaceholderMap.put(keyEvent.getDeviceId(), gamepadPlaceholder2);
         }
         gamepadPlaceholder2.update(keyEvent);
         if (gamepadPlaceholder2.key105depressed) {
            if (!gamepadPlaceholder2.key97depressed) {
               if (!gamepadPlaceholder2.key98depressed) return null;
            }
            this.gamepadIdToPlaceholderMap.remove(keyEvent.getDeviceId());
            gamepadUserPointer.val = gamepadPlaceholder2.key97depressed ? GamepadUser.ONE : GamepadUser.TWO;
            keyEvent = new SonyGamepadPS4();
            return keyEvent;
         }
         if (!gamepadPlaceholder2.key108depressed) return null;
         if (!gamepadPlaceholder2.key96depressed) {
            if (!gamepadPlaceholder2.key97depressed) return null;
         }
         this.gamepadIdToPlaceholderMap.remove(keyEvent.getDeviceId());
         gamepadUserPointer.val = gamepadPlaceholder2.key96depressed ? GamepadUser.ONE : GamepadUser.TWO;
         keyEvent = new MicrosoftGamepadXbox360();
         return keyEvent;
      }
   }

   public void handleGamepadEvent(final KeyEvent keyEvent) {
      synchronized (this) {
         InputDevice inputDevice = InputDevice.getDevice((int)keyEvent.getDeviceId());
         Gamepad gamepad = (Gamepad)this.gamepadIdToGamepadMap.get(inputDevice.getId());
         if (gamepad != null) {
            if (gamepad.start && (gamepad.a || gamepad.b)) {
               if (gamepad.a) {
                  this.assignGamepad(inputDevice.getId(), GamepadUser.ONE);
               }
               if (gamepad.b) {
                  this.assignGamepad(inputDevice.getId(), GamepadUser.TWO);
               }
            }
            gamepad.update(keyEvent);
            this.indicateGamepad(this.getAssignedGamepadById(Integer.valueOf(keyEvent.getDeviceId())));
         } else {
            gamepad = this.knownInputDeviceToGamepad(inputDevice);
            if (gamepad != null) {
               gamepad.update(keyEvent);
               gamepad.setVidPid(inputDevice.getVendorId(), inputDevice.getProductId());
               this.gamepadIdToGamepadMap.put(inputDevice.getId(), gamepad);
               RobotLog.vv((String)TAG, (String)"Input device %d has been autodetected based on USB VID and PID as %s", (Object[])new Object[]{inputDevice.getId(), gamepad.getClass().getSimpleName()});
               return;
            }
            gamepad = this.overriddenInputDeviceToGamepad(inputDevice);
            if (gamepad != null) {
               gamepad.update(keyEvent);
               gamepad.setVidPid(inputDevice.getVendorId(), inputDevice.getProductId());
               this.gamepadIdToGamepadMap.put(inputDevice.getId(), gamepad);
               RobotLog.vv((String)TAG, (String)"Input device %d has a USB VID and PID that has an entry in override list; treating as %s", (Object[])new Object[]{inputDevice.getId(), gamepad.getClass().getSimpleName()});
               return;
            }
            gamepad = new GamepadUserPointer(this);
            if ((keyEvent = this.guessGamepadType(keyEvent, (GamepadUserPointer)gamepad)) == null) return;
            keyEvent.setVidPid(inputDevice.getVendorId(), inputDevice.getProductId());
            this.gamepadIdToGamepadMap.put(inputDevice.getId(), keyEvent);
            RobotLog.vv((String)TAG, (String)"Using quantum superposition to guess that input device %d should be treated as %s", (Object[])new Object[]{inputDevice.getId(), keyEvent.getClass().getSimpleName()});
            if (gamepad.val == GamepadUser.ONE) {
               this.assignGamepad(inputDevice.getId(), GamepadUser.ONE);
            } else {
               this.assignGamepad(inputDevice.getId(), GamepadUser.TWO);
            }
         }
         return;
      }

   }

   public void handleGamepadEvent(MotionEvent motionEvent) {
      synchronized (this) {
         Gamepad gamepad = this.getGamepadById(Integer.valueOf(motionEvent.getDeviceId()));
         if (gamepad == null) {
            return;
         }
         gamepad.update(motionEvent);
         this.indicateGamepad(this.getAssignedGamepadById(Integer.valueOf(motionEvent.getDeviceId())));
         return;
      }
   }

   protected void indicateGamepad(Gamepad gamepad) {
      if (gamepad == null) return;
      ((GamepadIndicator)this.gamepadIndicators.get((Object)gamepad.getUser())).setState(GamepadIndicator.State.INDICATE);
   }

   protected void internalUnassignUser(GamepadUser gamepadUser) {
      ((GamepadIndicator)this.gamepadIndicators.get((Object)gamepadUser)).setState(GamepadIndicator.State.INVISIBLE);
      this.userToGamepadIdMap.remove((Object)gamepadUser);
      this.recentlyUnassignedUsers.add(gamepadUser);
   }

   public Gamepad knownInputDeviceToGamepad(InputDevice inputDevice) {
      synchronized (this) {
         int n = inputDevice.getVendorId();
         int n2 = inputDevice.getProductId();
         if (MicrosoftGamepadXbox360.matchesVidPid((int)n, (int)n2)) {
            inputDevice = new MicrosoftGamepadXbox360();
            return inputDevice;
         }
         if (LogitechGamepadF310.matchesVidPid((int)n, (int)n2)) {
            inputDevice = new LogitechGamepadF310();
            return inputDevice;
         }
         if (!SonyGamepadPS4.matchesVidPid((int)n, (int)n2)) return null;
         inputDevice = new SonyGamepadPS4();
         return inputDevice;
      }
   }

   public void onAssignedGamepadDropped(Gamepad gamepad) {
      SoundPlayer.getInstance().play(this.context, SOUND_ID_GAMEPAD_DISCONNECT, 1.0f, 0, 1.0f);
      Toast.makeText((Context)this.context, (CharSequence)String.format("Gamepad %d connection lost", gamepad.getUser().id), (int)0).show();
      RemovedGamepadMemory removedGamepadMemory = new RemovedGamepadMemory(this);
      removedGamepadMemory.vid = gamepad.vid;
      removedGamepadMemory.pid = gamepad.pid;
      removedGamepadMemory.user = gamepad.getUser();
      removedGamepadMemory.type = gamepad.type();
      gamepad = gamepad.getUser() == GamepadUser.ONE ? this.getAssignedGamepadById((Integer)this.userToGamepadIdMap.get((Object)GamepadUser.TWO)) : this.getAssignedGamepadById((Integer)this.userToGamepadIdMap.get((Object)GamepadUser.ONE));
      if (gamepad != null) {
         removedGamepadMemory.othergamepad_vid = gamepad.vid;
         removedGamepadMemory.othergamepad_pid = gamepad.pid;
      }
      this.removedGamepadMemories.add(removedGamepadMemory);
   }

   public void onInputDeviceAdded(int n) {
      synchronized (this) {
         RobotLog.vv((String)TAG, (String)String.format("New input device (id = %d) detected.", n));
         this.considerInputDeviceForAutomagicReassignment(InputDevice.getDevice((int)n));
         return;
      }
   }

   public void onInputDeviceChanged(int n) {
      synchronized (this) {
         RobotLog.vv((String)TAG, (String)String.format("Input device (id = %d) modified.", n));
         return;
      }
   }

   public void onInputDeviceRemoved(int n) {
      synchronized (this) {
         RobotLog.vv((String)TAG, (String)String.format("Input device (id = %d) removed.", n));
         Gamepad gamepad = this.getAssignedGamepadById(Integer.valueOf(n));
         if (gamepad != null) {
            this.onAssignedGamepadDropped(gamepad);
         }
         this.removeGamepad(n);
         return;
      }
   }

   public void open() {
      this.preferences = PreferenceManager.getDefaultSharedPreferences((Context)this.context);
   }

   public Gamepad overriddenInputDeviceToGamepad(InputDevice inputDevice) {
      synchronized (this) {
         int n = inputDevice.getVendorId();
         int n2 = inputDevice.getProductId();
         inputDevice = this.typeOverrideMapper.getEntryFor(n, n2);
         if (inputDevice == null) {
            return null;
         }
         inputDevice = inputDevice.createGamepad();
         return inputDevice;
      }
   }

   public void removeGamepad(int n) {
      synchronized (this) {
         Gamepad gamepad = this.getAssignedGamepadById(Integer.valueOf(n));
         if (gamepad != null) {
            this.internalUnassignUser(gamepad.getUser());
         }
         this.gamepadIdToGamepadMap.remove(n);
         if (this.gamepadIdToPlaceholderMap.get(n) == null) return;
         this.gamepadIdToPlaceholderMap.remove(n);
         return;
      }
   }

   public void setDebug(boolean bl) {
      this.debug = bl;
   }

   public void setEnabled(boolean bl) {
      this.enabled = bl;
   }

   public void setGamepadIndicators(Map<GamepadUser, GamepadIndicator> map) {
      this.gamepadIndicators = map;
   }

   public void unassignUser(GamepadUser gamepadUser) {
      synchronized (this) {
         Integer n = (Integer)this.userToGamepadIdMap.get((Object)gamepadUser);
         if (n != null) {
            this.gamepadIdToGamepadMap.remove(n);
         }
         this.internalUnassignUser(gamepadUser);
         return;
      }
   }

}
