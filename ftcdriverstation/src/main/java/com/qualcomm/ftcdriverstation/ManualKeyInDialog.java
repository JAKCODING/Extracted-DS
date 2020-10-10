package com.qualcomm.ftcdriverstation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class ManualKeyInDialog extends AlertDialog.Builder {
    Button doneBtn;
    EditText input;
    Listener listener;
    String title;

    public static abstract class Listener {
        public abstract void onInput(String str);
    }

    public ManualKeyInDialog(Context context, String str, Listener listener2) {
        super(context);
        this.title = str;
        this.listener = listener2;
    }

    public AlertDialog show() {
        setTitle(this.title);
        setCancelable(false);
        View inflate = create().getLayoutInflater().inflate(R.layout.custom_input_dialog_layout, (ViewGroup) null, false);
        this.input = (EditText) inflate.findViewById(R.id.input);
        setView(inflate);
        setPositiveButton(17039370, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                ManualKeyInDialog.this.listener.onInput(ManualKeyInDialog.this.input.getText().toString());
                ManualKeyInDialog.setSoftInputMode(ManualKeyInDialog.this.getContext(), 1);
            }
        });
        setNegativeButton(17039360, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                ManualKeyInDialog.setSoftInputMode(ManualKeyInDialog.this.getContext(), 1);
            }
        });
        AlertDialog show = super.show();
        this.doneBtn = show.getButton(-1);
        setSoftInputMode(getContext(), 2);
        return show;
    }

    public static void setSoftInputMode(Context context, int i) {
        ((InputMethodManager) context.getSystemService("input_method")).toggleSoftInput(i, 0);
    }
}
