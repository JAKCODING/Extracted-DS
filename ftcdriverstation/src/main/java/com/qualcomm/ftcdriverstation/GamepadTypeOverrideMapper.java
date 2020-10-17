package com.qualcomm.ftcdriverstation;

import android.content.*;
import android.preference.*;
import android.util.*;
import java.util.*;
import com.qualcomm.robotcore.hardware.*;
import com.qualcomm.hardware.logitech.*;
import com.qualcomm.hardware.microsoft.*;
import com.qualcomm.hardware.sony.*;

public class GamepadTypeOverrideMapper
{
    static final String KEY_GAMEPAD_MAPPING = "GAMEPAD_MAPPING";
    Context context;
    Set<String> serializedEntries;
    SharedPreferences sharedPreferences;

    GamepadTypeOverrideMapper(final Context context) {
        super();
        this.context = context;
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.sharedPreferences = defaultSharedPreferences;
        this.serializedEntries = (Set<String>)defaultSharedPreferences.getStringSet("GAMEPAD_MAPPING", (Set)null);
    }

    static String checkForClash(final Set<String> set, final GamepadTypeOverrideEntry gamepadTypeOverrideEntry) {
        for (final String s : set) {
            if (GamepadTypeOverrideEntry.fromString(s).usbIdsMatch(gamepadTypeOverrideEntry)) {
                return s;
            }
        }
        return null;
    }

    void addOrUpdate(final GamepadTypeOverrideEntry gamepadTypeOverrideEntry) {
        synchronized (this) {
            final Set stringSet = this.sharedPreferences.getStringSet("GAMEPAD_MAPPING", (Set)null);
            this.serializedEntries = (Set<String>)stringSet;
            if (stringSet != null) {
                final String checkForClash = checkForClash(stringSet, gamepadTypeOverrideEntry);
                if (checkForClash != null) {
                    this.serializedEntries.remove(checkForClash);
                }
                this.serializedEntries.add(gamepadTypeOverrideEntry.toString());
            }
            else {
                (this.serializedEntries = (Set<String>)new ArraySet()).add(gamepadTypeOverrideEntry.toString());
            }
            this.sharedPreferences.edit().putStringSet("GAMEPAD_MAPPING", (Set)this.serializedEntries).commit();
        }
    }

    void delete(final GamepadTypeOverrideEntry gamepadTypeOverrideEntry) {
        synchronized (this) {
            final Set stringSet = this.sharedPreferences.getStringSet("GAMEPAD_MAPPING", (Set)null);
            this.serializedEntries = (Set<String>)stringSet;
            if (stringSet == null) {
                return;
            }
            boolean b = false;
            final Iterator<String> iterator = stringSet.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().equals(gamepadTypeOverrideEntry.toString())) {
                    b = true;
                }
            }
            if (b) {
                this.serializedEntries.remove(gamepadTypeOverrideEntry.toString());
                this.sharedPreferences.edit().putStringSet("GAMEPAD_MAPPING", (Set)this.serializedEntries).commit();
                return;
            }
            throw new IllegalArgumentException();
        }
    }

    ArrayList<GamepadTypeOverrideEntry> getEntries() {
        synchronized (this) {
            final Set stringSet = this.sharedPreferences.getStringSet("GAMEPAD_MAPPING", (Set)null);
            this.serializedEntries = (Set<String>)stringSet;
            if (stringSet == null) {
                return new ArrayList<GamepadTypeOverrideEntry>();
            }
            final ArrayList<GamepadTypeOverrideEntry> list = new ArrayList<GamepadTypeOverrideEntry>();
            final Iterator<String> iterator = this.serializedEntries.iterator();
            while (iterator.hasNext()) {
                list.add(GamepadTypeOverrideEntry.fromString(iterator.next()));
            }
            return list;
        }
    }

    GamepadTypeOverrideEntry getEntryFor(final int n, final int n2) {
        synchronized (this) {
            for (final GamepadTypeOverrideEntry gamepadTypeOverrideEntry : this.getEntries()) {
                if (gamepadTypeOverrideEntry.usbIdsMatch(n, n2)) {
                    return gamepadTypeOverrideEntry;
                }
            }
            return null;
        }
    }

    void setEntries(final ArrayList<GamepadTypeOverrideEntry> list) {
        synchronized (this) {
            if (this.serializedEntries != null) {
                this.serializedEntries.clear();
            }
            else {
                this.serializedEntries = (Set<String>)new ArraySet();
            }
            if (list.isEmpty()) {
                this.sharedPreferences.edit().remove("GAMEPAD_MAPPING").commit();
            }
            else {
                final Iterator<GamepadTypeOverrideEntry> iterator = list.iterator();
                while (iterator.hasNext()) {
                    this.serializedEntries.add(iterator.next().toString());
                }
                this.sharedPreferences.edit().remove("GAMEPAD_MAPPING").commit();
                this.sharedPreferences.edit().putStringSet("GAMEPAD_MAPPING", (Set)this.serializedEntries).commit();
            }
        }
    }

    static class GamepadTypeOverrideEntry
    {
        Gamepad.Type mappedType;
        int pid;
        int vid;

        GamepadTypeOverrideEntry(final int vid, final int pid, final Gamepad.Type mappedType) {
            super();
            this.vid = vid;
            this.pid = pid;
            this.mappedType = mappedType;
        }

        static GamepadTypeOverrideEntry fromString(final String s) {
            final String[] split = s.split(":");
            return new GamepadTypeOverrideEntry(Integer.valueOf(split[0]), Integer.valueOf(split[1]), Gamepad.Type.valueOf(split[2]));
        }

        Gamepad createGamepad() {
            final int n = this.mappedType.ordinal();
            if (n == 1) {
                return new LogitechGamepadF310();
            }
            if (n == 2) {
                return new MicrosoftGamepadXbox360();
            }
            if (n == 3) {
                return new SonyGamepadPS4();
            }
            throw new IllegalStateException();
        }

        public boolean equals(final GamepadTypeOverrideEntry gamepadTypeOverrideEntry) {
            final int vid = this.vid;
            final int vid2 = gamepadTypeOverrideEntry.vid;
            boolean b = false;
            final boolean b2 = vid == vid2;
            if (this.pid == gamepadTypeOverrideEntry.pid) {
                b = true;
            }
            return this.mappedType.equals(gamepadTypeOverrideEntry.mappedType) & (b2 & true & b);
        }

        @Override
        public String toString() {
            return String.format("%d:%d:%s", this.vid, this.pid, this.mappedType.toString());
        }
}
