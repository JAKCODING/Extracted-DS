package com.qualcomm.ftcdriverstation;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import com.qualcomm.hardware.logitech.LogitechGamepadF310;
import com.qualcomm.hardware.microsoft.MicrosoftGamepadXbox360;
import com.qualcomm.hardware.sony.SonyGamepadPS4;
import com.qualcomm.robotcore.hardware.Gamepad;
import java.util.ArrayList;
import java.util.Iterator;

public class EditGamepadTypeOverridesActivity extends Activity {
   boolean changesMade = false;
   KeyEventCapturingProgressDialog detectionDialog;
   ArrayList entries = new ArrayList();
   ListView listView;
   GamepadTypeOverrideMapper mapper;
   GamepadOverrideEntryAdapter overrideEntryAdapter;

   public boolean dispatchKeyEvent(KeyEvent var1) {
      if (var1.getKeyCode() == 4 && var1.getAction() == 0) {
         this.handleCancelClicked((View)null);
         return true;
      } else {
         return super.dispatchKeyEvent(var1);
      }
   }

   public void handleAddEntryClicked(View var1) {
      KeyEventCapturingProgressDialog var2 = new KeyEventCapturingProgressDialog(this);
      this.detectionDialog = var2;
      var2.setTitle("Gamepad identification");
      this.detectionDialog.setMessage("Please press any key on the gamepad.");
      this.detectionDialog.setCancelable(false);
      this.detectionDialog.setProgressStyle(0);
      this.detectionDialog.setButton(-2, "Abort", new OnClickListener() {
         public void onClick(DialogInterface var1, int var2) {
            Toast.makeText(EditGamepadTypeOverridesActivity.this, "Aborted gamepad detection", Toast.LENGTH_SHORT).show();
         }
      });
      this.detectionDialog.setListener(new KeyEventCapturingProgressDialog.Listener() {
         public void onKeyDown(KeyEvent var1) {
            if (Gamepad.isGamepadDevice(var1.getDeviceId()) && EditGamepadTypeOverridesActivity.this.detectionDialog != null && EditGamepadTypeOverridesActivity.this.detectionDialog.isShowing()) {
               EditGamepadTypeOverridesActivity.this.detectionDialog.dismiss();
               InputDevice var4 = InputDevice.getDevice(var1.getDeviceId());
               int var2 = var4.getVendorId();
               int var3 = var4.getProductId();
               Iterator var5 = EditGamepadTypeOverridesActivity.this.entries.iterator();

               while(var5.hasNext()) {
                  if (((GamepadTypeOverrideMapper.GamepadTypeOverrideEntry)var5.next()).usbIdsMatch(var2, var3)) {
                     EditGamepadTypeOverridesActivity.this.showEntryAlreadyExistsDialog(var2, var3);
                     return;
                  }
               }

               if (!LogitechGamepadF310.matchesVidPid(var2, var3) && !MicrosoftGamepadXbox360.matchesVidPid(var2, var3) && !SonyGamepadPS4.matchesVidPid(var2, var3)) {
                  EditGamepadTypeOverridesActivity.this.showGamepadTypeChooserDialog(var2, var3);
               } else {
                  EditGamepadTypeOverridesActivity.this.showOfficiallySupportedDialog(var2, var3);
               }
            }

         }
      });
      this.detectionDialog.show();
   }

   public void handleCancelClicked(View var1) {
      if (!this.changesMade) {
         this.finish();
      } else {
         Builder var2 = new Builder(this);
         var2.setCancelable(false);
         var2.setTitle("Discard changes?");
         var2.setMessage("Discard changes and exit?");
         var2.setNegativeButton("Cancel", new OnClickListener() {
            public void onClick(DialogInterface var1, int var2) {
            }
         });
         var2.setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface var1, int var2) {
               EditGamepadTypeOverridesActivity.this.finish();
            }
         });
         var2.show();
      }
   }

   public void handleSaveClicked(View var1) {
      if (!this.changesMade) {
         this.finish();
      } else {
         Builder var2 = new Builder(this);
         var2.setCancelable(false);
         var2.setTitle("Save changes?");
         var2.setMessage("Save changes and exit?");
         var2.setNegativeButton("Cancel", new OnClickListener() {
            public void onClick(DialogInterface var1, int var2) {
            }
         });
         var2.setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface var1, int var2) {
               EditGamepadTypeOverridesActivity.this.mapper.setEntries(EditGamepadTypeOverridesActivity.this.entries);
               EditGamepadTypeOverridesActivity.this.finish();
            }
         });
         var2.show();
      }
   }

   protected void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.setContentView(R.layout.activity_edit_gamepad_type_overrides);
      GamepadTypeOverrideMapper var2 = new GamepadTypeOverrideMapper(this);
      this.mapper = var2;
      this.entries.addAll(var2.getEntries());
      this.listView = (ListView)this.findViewById(R.id.overridesList);
      GamepadOverrideEntryAdapter var3 = new GamepadOverrideEntryAdapter(this, 17367044, this.entries);
      this.overrideEntryAdapter = var3;
      this.listView.setAdapter(var3);
      this.listView.setOnItemClickListener(new OnItemClickListener() {
         public void onItemClick(AdapterView var1, View var2, int var3, long var4) {
         }
      });
      this.listView.setOnItemLongClickListener(new OnItemLongClickListener() {
         public boolean onItemLongClick(AdapterView var1, View var2, final int var3, long var4) {
            Builder var7 = new Builder(EditGamepadTypeOverridesActivity.this);
            GamepadTypeOverrideMapper.GamepadTypeOverrideEntry var6 = (GamepadTypeOverrideMapper.GamepadTypeOverrideEntry)EditGamepadTypeOverridesActivity.this.entries.get(var3);
            var7.setTitle("Delete entry?");
            var7.setMessage(String.format("Delete entry which maps %d:%d to %s?", var6.vid, var6.pid, var6.mappedType.toString()));
            var7.setPositiveButton("Confirm", new OnClickListener() {
               public void onClick(DialogInterface var1, int var2) {
                  EditGamepadTypeOverridesActivity.this.entries.remove(var3);
                  EditGamepadTypeOverridesActivity.this.overrideEntryAdapter.notifyDataSetChanged();
                  EditGamepadTypeOverridesActivity.this.changesMade = true;
               }
            });
            var7.setNegativeButton("Cancel", new OnClickListener() {
               public void onClick(DialogInterface var1, int var2) {
               }
            });
            var7.show();
            return false;
         }
      });
   }

   public void showEntryAlreadyExistsDialog(int var1, int var2) {
      Builder var3 = new Builder(this);
      var3.setCancelable(false);
      var3.setTitle("Already Exists");
      var3.setMessage(String.format("An entry which maps %d:%d already exits. If you'd like to change the mapping target, please delete the current entry first.", var1, var2));
      var3.setNeutralButton("OK", new OnClickListener() {
         public void onClick(DialogInterface var1, int var2) {
         }
      });
      var3.show();
   }

   public void showGamepadTypeChooserDialog(final int var1, final int var2) {
      SelectGamepadMappingDialog var3 = new SelectGamepadMappingDialog(this);
      var3.setListener(new SelectGamepadMappingDialog.Listener() {
         public void onOk(Gamepad.Type var1x) {
            EditGamepadTypeOverridesActivity.this.entries.add(new GamepadTypeOverrideMapper.GamepadTypeOverrideEntry(var1, var2, var1x));
            EditGamepadTypeOverridesActivity.this.overrideEntryAdapter.notifyDataSetChanged();
            EditGamepadTypeOverridesActivity.this.changesMade = true;
         }
      });
      var3.show();
   }

   public void showOfficiallySupportedDialog(int var1, int var2) {
      Builder var3 = new Builder(this);
      var3.setCancelable(false);
      var3.setTitle("Officially Supported");
      var3.setMessage(String.format("The USB identifier %d:%d is that of an officially supported gamepad. Overrides are not allowed for officially supported gamepads.", var1, var2));
      var3.setNeutralButton("OK", new OnClickListener() {
         public void onClick(DialogInterface var1, int var2) {
         }
      });
      var3.show();
   }
}