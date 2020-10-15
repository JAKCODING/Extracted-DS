package com.qualcomm.ftcdriverstation;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;

public class OpModeSelectionDialogFragment extends DialogFragment {
   private OpModeSelectionDialogFragment.OpModeSelectionDialogListener listener = null;
   private List opModes = new LinkedList();
   private int title = 0;

   public Dialog onCreateDialog(Bundle var1) {
      View var2 = LayoutInflater.from(this.getActivity()).inflate(2131427420, (ViewGroup)null);
      ((TextView)var2.findViewById(2131231013)).setText(this.title);
      Builder var3 = new Builder(this.getActivity());
      var3.setCustomTitle(var2);
      ArrayAdapter var4 = new ArrayAdapter(this.getActivity(), 2131427419, 2131231011, this.opModes) {
         public View getView(int var1, View var2, ViewGroup var3) {
            View var6 = super.getView(var1, var2, var3);
            ImageView var5 = (ImageView)var6.findViewById(2131231012);
            byte var4;
            if (var1 < OpModeSelectionDialogFragment.this.opModes.size() - 1 && !((OpModeMeta)OpModeSelectionDialogFragment.this.opModes.get(var1)).group.equals(((OpModeMeta)OpModeSelectionDialogFragment.this.opModes.get(var1 + 1)).group)) {
               var4 = 0;
            } else {
               var4 = 8;
            }

            var5.setVisibility(var4);
            return var6;
         }
      };
      var3.setTitle(this.title);
      var3.setAdapter(var4, new OnClickListener() {
         public void onClick(DialogInterface var1, int var2) {
            if (OpModeSelectionDialogFragment.this.listener != null) {
               OpModeSelectionDialogFragment.this.listener.onOpModeSelectionClick((OpModeMeta)OpModeSelectionDialogFragment.this.opModes.get(var2));
            }

         }
      });
      return var3.create();
   }

   public void onStart() {
      super.onStart();
      Dialog var1 = this.getDialog();
      var1.findViewById(var1.getContext().getResources().getIdentifier("android:id/titleDivider", (String)null, (String)null)).setBackground(var1.findViewById(2131231014).getBackground());
   }

   public void setOnSelectionDialogListener(OpModeSelectionDialogFragment.OpModeSelectionDialogListener var1) {
      this.listener = var1;
   }

   public void setOpModes(List<OpModeMeta> list) {
      LinkedList linkedList = new LinkedList(list);
      this.opModes = linkedList;
      Collections.sort(linkedList, new Comparator<OpModeMeta>() {
         public int compare(OpModeMeta opModeMeta, OpModeMeta opModeMeta2) {
            int compareTo = opModeMeta.group.compareTo(opModeMeta2.group);
            return compareTo == 0 ? opModeMeta.name.compareTo(opModeMeta2.name) : compareTo;
         }
      });
   }

   public void setTitle(int var1) {
      this.title = var1;
   }

   public interface OpModeSelectionDialogListener {
      void onOpModeSelectionClick(OpModeMeta var1);
   }
}
