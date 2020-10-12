package com.qualcomm.ftcdriverstation;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.KeyEvent;

public class KeyEventCapturingProgressDialog extends ProgressDialog {
   KeyEventCapturingProgressDialog.Listener listener;

   public KeyEventCapturingProgressDialog(Context var1) {
      super(var1);
   }

   public KeyEventCapturingProgressDialog(Context var1, int var2) {
      super(var1, var2);
   }

   public boolean onKeyDown(int var1, KeyEvent var2) {
      KeyEventCapturingProgressDialog.Listener var3 = this.listener;
      if (var3 != null) {
         var3.onKeyDown(var2);
      }

      return true;
   }

   public boolean onKeyUp(int var1, KeyEvent var2) {
      return true;
   }

   public void setListener(KeyEventCapturingProgressDialog.Listener var1) {
      this.listener = var1;
   }

   interface Listener {
      void onKeyDown(KeyEvent var1);
   }
}
