package com.qualcomm.ftcdriverstation;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class ManualKeyInDialog extends Builder {
   Button doneBtn;
   EditText input;
   ManualKeyInDialog.Listener listener;
   String title;

   public ManualKeyInDialog(Context var1, String var2, ManualKeyInDialog.Listener var3) {
      super(var1);
      this.title = var2;
      this.listener = var3;
   }

   public static void setSoftInputMode(Context var0, int var1) {
      ((InputMethodManager)var0.getSystemService("input_method")).toggleSoftInput(var1, 0);
   }

   public AlertDialog show() {
      this.setTitle(this.title);
      this.setCancelable(false);
      View var1 = this.create().getLayoutInflater().inflate(2131427387, (ViewGroup)null, false);
      this.input = (EditText)var1.findViewById(2131230938);
      this.setView(var1);
      this.setPositiveButton(17039370, new OnClickListener() {
         public void onClick(DialogInterface var1, int var2) {
            var1.dismiss();
            ManualKeyInDialog.this.listener.onInput(ManualKeyInDialog.this.input.getText().toString());
            ManualKeyInDialog.setSoftInputMode(ManualKeyInDialog.this.getContext(), 1);
         }
      });
      this.setNegativeButton(17039360, new OnClickListener() {
         public void onClick(DialogInterface var1, int var2) {
            var1.cancel();
            ManualKeyInDialog.setSoftInputMode(ManualKeyInDialog.this.getContext(), 1);
         }
      });
      AlertDialog var2 = super.show();
      this.doneBtn = var2.getButton(-1);
      setSoftInputMode(this.getContext(), 2);
      return var2;
   }

   public abstract static class Listener {
      public abstract void onInput(String var1);
   }
}
