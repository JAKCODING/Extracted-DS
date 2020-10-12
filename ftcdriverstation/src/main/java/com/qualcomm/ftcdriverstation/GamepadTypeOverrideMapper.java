package com.qualcomm.ftcdriverstation;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.ArraySet;
import com.qualcomm.hardware.logitech.LogitechGamepadF310;
import com.qualcomm.hardware.microsoft.MicrosoftGamepadXbox360;
import com.qualcomm.hardware.sony.SonyGamepadPS4;
import com.qualcomm.robotcore.hardware.Gamepad;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class GamepadTypeOverrideMapper {
   static final String KEY_GAMEPAD_MAPPING = "GAMEPAD_MAPPING";
   Context context;
   Set serializedEntries;
   SharedPreferences sharedPreferences;

   GamepadTypeOverrideMapper(Context var1) {
      this.context = var1;
      SharedPreferences var2 = PreferenceManager.getDefaultSharedPreferences(var1);
      this.sharedPreferences = var2;
      this.serializedEntries = var2.getStringSet("GAMEPAD_MAPPING", (Set)null);
   }

   static String checkForClash(Set var0, GamepadTypeOverrideMapper.GamepadTypeOverrideEntry var1) {
      Iterator var2 = var0.iterator();

      String var3;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         var3 = (String)var2.next();
      } while(!GamepadTypeOverrideMapper.GamepadTypeOverrideEntry.fromString(var3).usbIdsMatch(var1));

      return var3;
   }

   void addOrUpdate(GamepadTypeOverrideMapper.GamepadTypeOverrideEntry var1) {
      synchronized(this){}

      Throwable var10000;
      label314: {
         Set var2;
         boolean var10001;
         try {
            var2 = this.sharedPreferences.getStringSet("GAMEPAD_MAPPING", (Set)null);
            this.serializedEntries = var2;
         } catch (Throwable var44) {
            var10000 = var44;
            var10001 = false;
            break label314;
         }

         if (var2 != null) {
            String var46;
            try {
               var46 = checkForClash(var2, var1);
            } catch (Throwable var43) {
               var10000 = var43;
               var10001 = false;
               break label314;
            }

            if (var46 != null) {
               try {
                  this.serializedEntries.remove(var46);
               } catch (Throwable var42) {
                  var10000 = var42;
                  var10001 = false;
                  break label314;
               }
            }

            try {
               this.serializedEntries.add(var1.toString());
            } catch (Throwable var41) {
               var10000 = var41;
               var10001 = false;
               break label314;
            }
         } else {
            try {
               ArraySet var47 = new ArraySet();
               this.serializedEntries = var47;
               var47.add(var1.toString());
            } catch (Throwable var40) {
               var10000 = var40;
               var10001 = false;
               break label314;
            }
         }

         label295:
         try {
            this.sharedPreferences.edit().putStringSet("GAMEPAD_MAPPING", this.serializedEntries).commit();
            return;
         } catch (Throwable var39) {
            var10000 = var39;
            var10001 = false;
            break label295;
         }
      }

      Throwable var45 = var10000;
      throw var45;
   }

   void delete(GamepadTypeOverrideMapper.GamepadTypeOverrideEntry var1) {
      synchronized(this){}

      Throwable var10000;
      label306: {
         Set var2;
         boolean var10001;
         try {
            var2 = this.sharedPreferences.getStringSet("GAMEPAD_MAPPING", (Set)null);
            this.serializedEntries = var2;
         } catch (Throwable var33) {
            var10000 = var33;
            var10001 = false;
            break label306;
         }

         if (var2 == null) {
            return;
         }

         boolean var3 = false;

         Iterator var36;
         try {
            var36 = var2.iterator();
         } catch (Throwable var31) {
            var10000 = var31;
            var10001 = false;
            break label306;
         }

         while(true) {
            try {
               if (!var36.hasNext()) {
                  break;
               }

               if (!((String)var36.next()).equals(var1.toString())) {
                  continue;
               }
            } catch (Throwable var32) {
               var10000 = var32;
               var10001 = false;
               break label306;
            }

            var3 = true;
         }

         if (var3) {
            label282: {
               try {
                  this.serializedEntries.remove(var1.toString());
                  this.sharedPreferences.edit().putStringSet("GAMEPAD_MAPPING", this.serializedEntries).commit();
               } catch (Throwable var29) {
                  var10000 = var29;
                  var10001 = false;
                  break label282;
               }

               return;
            }
         } else {
            label284:
            try {
               IllegalArgumentException var35 = new IllegalArgumentException();
               throw var35;
            } catch (Throwable var30) {
               var10000 = var30;
               var10001 = false;
               break label284;
            }
         }
      }

      Throwable var34 = var10000;
      throw var34;
   }

   ArrayList getEntries() {
      synchronized(this){}

      Throwable var10000;
      label207: {
         Set var1;
         boolean var10001;
         try {
            var1 = this.sharedPreferences.getStringSet("GAMEPAD_MAPPING", (Set)null);
            this.serializedEntries = var1;
         } catch (Throwable var22) {
            var10000 = var22;
            var10001 = false;
            break label207;
         }

         if (var1 == null) {
            label191: {
               ArrayList var23;
               try {
                  var23 = new ArrayList();
               } catch (Throwable var19) {
                  var10000 = var19;
                  var10001 = false;
                  break label191;
               }

               return var23;
            }
         } else {
            label203: {
               ArrayList var2;
               Iterator var24;
               try {
                  var2 = new ArrayList();
                  var24 = this.serializedEntries.iterator();
               } catch (Throwable var21) {
                  var10000 = var21;
                  var10001 = false;
                  break label203;
               }

               while(true) {
                  try {
                     if (var24.hasNext()) {
                        var2.add(GamepadTypeOverrideMapper.GamepadTypeOverrideEntry.fromString((String)var24.next()));
                        continue;
                     }
                  } catch (Throwable var20) {
                     var10000 = var20;
                     var10001 = false;
                     break;
                  }

                  return var2;
               }
            }
         }
      }

      Throwable var25 = var10000;
      throw var25;
   }

   GamepadTypeOverrideMapper.GamepadTypeOverrideEntry getEntryFor(int var1, int var2) {
      synchronized(this){}

      Throwable var10000;
      label83: {
         boolean var10001;
         Iterator var3;
         try {
            var3 = this.getEntries().iterator();
         } catch (Throwable var11) {
            var10000 = var11;
            var10001 = false;
            break label83;
         }

         while(true) {
            boolean var5;
            GamepadTypeOverrideMapper.GamepadTypeOverrideEntry var12;
            try {
               if (!var3.hasNext()) {
                  return null;
               }

               var12 = (GamepadTypeOverrideMapper.GamepadTypeOverrideEntry)var3.next();
               var5 = var12.usbIdsMatch(var1, var2);
            } catch (Throwable var10) {
               var10000 = var10;
               var10001 = false;
               break;
            }

            if (var5) {
               return var12;
            }
         }
      }

      Throwable var4 = var10000;
      throw var4;
   }

   void setEntries(ArrayList var1) {
      synchronized(this){}

      Throwable var10000;
      label414: {
         boolean var10001;
         label406: {
            try {
               if (this.serializedEntries != null) {
                  this.serializedEntries.clear();
                  break label406;
               }
            } catch (Throwable var44) {
               var10000 = var44;
               var10001 = false;
               break label414;
            }

            try {
               ArraySet var2 = new ArraySet();
               this.serializedEntries = var2;
            } catch (Throwable var42) {
               var10000 = var42;
               var10001 = false;
               break label414;
            }
         }

         try {
            if (var1.isEmpty()) {
               this.sharedPreferences.edit().remove("GAMEPAD_MAPPING").commit();
               return;
            }
         } catch (Throwable var43) {
            var10000 = var43;
            var10001 = false;
            break label414;
         }

         Iterator var47;
         try {
            var47 = var1.iterator();
         } catch (Throwable var40) {
            var10000 = var40;
            var10001 = false;
            break label414;
         }

         while(true) {
            try {
               if (!var47.hasNext()) {
                  break;
               }

               GamepadTypeOverrideMapper.GamepadTypeOverrideEntry var45 = (GamepadTypeOverrideMapper.GamepadTypeOverrideEntry)var47.next();
               this.serializedEntries.add(var45.toString());
            } catch (Throwable var41) {
               var10000 = var41;
               var10001 = false;
               break label414;
            }
         }

         label381:
         try {
            this.sharedPreferences.edit().remove("GAMEPAD_MAPPING").commit();
            this.sharedPreferences.edit().putStringSet("GAMEPAD_MAPPING", this.serializedEntries).commit();
            return;
         } catch (Throwable var39) {
            var10000 = var39;
            var10001 = false;
            break label381;
         }
      }

      Throwable var46 = var10000;
      throw var46;
   }

   static class GamepadTypeOverrideEntry {
      Gamepad.Type mappedType;
      int pid;
      int vid;

      GamepadTypeOverrideEntry(int var1, int var2, Gamepad.Type var3) {
         this.vid = var1;
         this.pid = var2;
         this.mappedType = var3;
      }

      static GamepadTypeOverrideMapper.GamepadTypeOverrideEntry fromString(String var0) {
         String[] var1 = var0.split(":");
         return new GamepadTypeOverrideMapper.GamepadTypeOverrideEntry(Integer.valueOf(var1[0]), Integer.valueOf(var1[1]), Gamepad.Type.valueOf(var1[2]));
      }

      Gamepad createGamepad() {
         switch(mappedType) {
            case SONY_PS4:
               return new SonyGamepadPS4();
            case XBOX_360:
               return new MicrosoftGamepadXbox360();
            case LOGITECH_F310:
               return new LogitechGamepadF310();
            default:
               throw new IllegalStateException();
         }
         /*int var1 = null.$SwitchMap$com$qualcomm$robotcore$hardware$Gamepad$Type[this.mappedType.ordinal()];
         if (var1 != 1) {
            if (var1 != 2) {
               if (var1 == 3) {
                  return new SonyGamepadPS4();
               } else {
                  throw new IllegalStateException();
               }
            } else {
               return new MicrosoftGamepadXbox360();
            }
         } else {
            return new LogitechGamepadF310();
         }*/
      }

      public boolean equals(GamepadTypeOverrideMapper.GamepadTypeOverrideEntry var1) {
         int var2 = this.vid;
         int var3 = var1.vid;
         boolean var4 = false;
         boolean var5;
         if (var2 == var3) {
            var5 = true;
         } else {
            var5 = false;
         }

         if (this.pid == var1.pid) {
            var4 = true;
         }

         return this.mappedType.equals(var1.mappedType) & var5 & true & var4;
      }

      public String toString() {
         return String.format("%d:%d:%s", this.vid, this.pid, this.mappedType.toString());
      }

      public boolean usbIdsMatch(int var1, int var2) {
         int var3 = this.vid;
         boolean var4 = false;
         boolean var5;
         if (var3 == var1) {
            var5 = true;
         } else {
            var5 = false;
         }

         if (this.pid == var2) {
            var4 = true;
         }

         return var5 & true & var4;
      }

      public boolean usbIdsMatch(GamepadTypeOverrideMapper.GamepadTypeOverrideEntry var1) {
         int var2 = this.vid;
         int var3 = var1.vid;
         boolean var4 = false;
         boolean var5;
         if (var2 == var3) {
            var5 = true;
         } else {
            var5 = false;
         }

         if (this.pid == var1.pid) {
            var4 = true;
         }

         return var5 & true & var4;
      }
   }
}
