package com.qualcomm.ftcdriverstation;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.qualcomm.ftcdriverstation.GamepadTypeOverrideMapper;
import java.util.List;

public class GamepadOverrideEntryAdapter extends BaseAdapter implements ListAdapter {
    private Activity activity;
    private List<GamepadTypeOverrideMapper.GamepadTypeOverrideEntry> gamepadOverrideEntries;
    private int list_id;

    public long getItemId(int i) {
        return 0;
    }

    public GamepadOverrideEntryAdapter(Activity activity2, int i, List<GamepadTypeOverrideMapper.GamepadTypeOverrideEntry> list) {
        this.activity = activity2;
        this.gamepadOverrideEntries = list;
        this.list_id = i;
    }

    public int getCount() {
        return this.gamepadOverrideEntries.size();
    }

    public Object getItem(int i) {
        return this.gamepadOverrideEntries.get(i);
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = this.activity.getLayoutInflater().inflate(this.list_id, viewGroup, false);
        }
        GamepadTypeOverrideMapper.GamepadTypeOverrideEntry gamepadTypeOverrideEntry = this.gamepadOverrideEntries.get(i);
        ((TextView) view.findViewById(16908309)).setText(String.format("Mapped as %s", new Object[]{gamepadTypeOverrideEntry.mappedType.toString()}));
        ((TextView) view.findViewById(16908308)).setText(String.format("VID: %d, PID: %d", new Object[]{Integer.valueOf(gamepadTypeOverrideEntry.vid), Integer.valueOf(gamepadTypeOverrideEntry.pid)}));
        return view;
    }
}
