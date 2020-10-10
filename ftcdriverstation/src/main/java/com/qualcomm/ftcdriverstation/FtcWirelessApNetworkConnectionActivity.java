package com.qualcomm.ftcdriverstation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.robocol.Heartbeat;
import com.qualcomm.robotcore.robocol.RobocolDatagram;
import com.qualcomm.robotcore.robot.RobotState;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.wifi.DriverStationAccessPointAssistant;
import com.qualcomm.robotcore.wifi.NetworkConnection;

import org.firstinspires.ftc.robotcore.internal.network.CallbackResult;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.network.RecvLoopRunnable;
import org.firstinspires.ftc.robotcore.internal.ui.BaseActivity;

public class FtcWirelessApNetworkConnectionActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "FtcWirelessApNetworkConnection";
    /* access modifiers changed from: private */
    public Heartbeat heartbeat = new Heartbeat();
    private NetworkConnection networkConnection;
    private NetworkConnectionHandler networkConnectionHandler = NetworkConnectionHandler.getInstance();
    /* access modifiers changed from: private */
    public RobotState robotState;
    private TextView textViewCurrentAp;
    /* access modifiers changed from: private */
    public TextView textViewWirelessApStatus;

    public String getTag() {
        return TAG;
    }

    public void onClick(View view) {
    }

    /* access modifiers changed from: protected */
    public void setTextView(final TextView textView, final String str) {
        runOnUiThread(new Runnable() {
            public void run() {
                textView.setText(str);
            }
        });
    }

    protected class RecvLoopCallback extends RecvLoopRunnable.DegenerateCallback {
        protected RecvLoopCallback() {
        }

        public CallbackResult heartbeatEvent(RobocolDatagram robocolDatagram, long j) {
            try {
                FtcWirelessApNetworkConnectionActivity.this.heartbeat.fromByteArray(robocolDatagram.getData());
                RobotState unused = FtcWirelessApNetworkConnectionActivity.this.robotState = RobotState.fromByte(FtcWirelessApNetworkConnectionActivity.this.heartbeat.getRobotState());
                FtcWirelessApNetworkConnectionActivity.this.setTextView(FtcWirelessApNetworkConnectionActivity.this.textViewWirelessApStatus, FtcWirelessApNetworkConnectionActivity.this.robotState.toString());
            } catch (RobotCoreException e) {
                RobotLog.logStackTrace(e);
            }
            return CallbackResult.HANDLED;
        }
    }

    /* access modifiers changed from: protected */
    public FrameLayout getBackBar() {
        return (FrameLayout) findViewById(R.id.backbar);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_ftc_wireless_ap_connection);
        this.networkConnection = DriverStationAccessPointAssistant.getDriverStationAccessPointAssistant(getBaseContext());
        this.textViewCurrentAp = (TextView) findViewById(R.id.textViewCurrentAp);
        ((Button) findViewById(R.id.buttonWirelessApSettings)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FtcWirelessApNetworkConnectionActivity.this.startActivity(new Intent("android.settings.WIFI_SETTINGS"));
            }
        });
    }

    public void onStart() {
        super.onStart();
        this.textViewCurrentAp.setText(this.networkConnection.getConnectionOwnerName());
        this.networkConnection.discoverPotentialConnections();
    }

    public void onStop() {
        super.onStop();
        this.networkConnection.cancelPotentialConnections();
    }
}
