package com.qualcomm.ftcdriverstation;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import com.qualcomm.robotcore.hardware.Gamepad;

public class SelectGamepadMappingDialog extends Builder {
   private ArrayAdapter fieldTypeAdapter;
   private Spinner fieldTypeSpinner;
   private SelectGamepadMappingDialog.Listener listener;

   public SelectGamepadMappingDialog(Context var1) {
      super(var1);
   }

   private void setupTypeSpinner() {
      this.fieldTypeAdapter = new ArrayAdapter(this.getContext(), 17367048);
      Gamepad.Type[] var1 = Gamepad.Type.values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         Gamepad.Type var4 = var1[var3];
         this.fieldTypeAdapter.add(var4.toString());
      }

      this.fieldTypeAdapter.setDropDownViewResource(17367049);
      this.fieldTypeSpinner.setAdapter(this.fieldTypeAdapter);
   }

   public void setListener(SelectGamepadMappingDialog.Listener var1) {
      this.listener = var1;
   }

   public AlertDialog show() {
      this.setTitle("Choose Mapping");
      LayoutInflater var1 = this.create().getLayoutInflater();
      FrameLayout var2 = new FrameLayout(this.getContext());
      this.setView(var2);
      var1.inflate(2131427389, var2);
      this.fieldTypeSpinner = (Spinner)var2.findViewById(2131230915);
      this.setupTypeSpinner();
      this.setPositiveButton("OK", new OnClickListener() {
         public void onClick(DialogInterface var1, int var2) {
            if (SelectGamepadMappingDialog.this.listener != null) {
               SelectGamepadMappingDialog.this.listener.onOk(Gamepad.Type.valueOf((String)SelectGamepadMappingDialog.this.fieldTypeSpinner.getSelectedItem()));
            }

         }
      });
      this.setNegativeButton("Cancel", new OnClickListener() {
         public void onClick(DialogInterface var1, int var2) {
         }
      });
      return super.show();
   }

   interface Listener {
      void onOk(Gamepad.Type var1);
   }
}
