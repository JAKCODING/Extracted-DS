package com.qualcomm.ftcdriverstation;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import com.qualcomm.robotcore.wifi.NetworkConnection;
import com.qualcomm.robotcore.wifi.NetworkConnectionFactory;
import com.qualcomm.robotcore.wifi.NetworkType;
import com.qualcomm.robotcore.wifi.SoftApAssistant;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.firstinspires.ftc.robotcore.internal.network.CallbackResult;
import org.firstinspires.ftc.robotcore.internal.network.PreferenceRemoterDS;
import org.firstinspires.ftc.robotcore.internal.ui.BaseActivity;

public class FtcPairNetworkConnectionActivity extends BaseActivity implements View.OnClickListener, NetworkConnection.NetworkConnectionCallback {
    public static final String TAG = "FtcPairNetworkConnection";
    private String connectionOwnerIdentity;
    private String connectionOwnerPassword;
    private ScheduledFuture<?> discoveryFuture;
    private EditText editTextSoftApPassword;
    /* access modifiers changed from: private */
    public boolean filterForTeam = true;
    /* access modifiers changed from: private */
    public NetworkConnection networkConnection;
    private SharedPreferences sharedPref;
    private int teamNum;
    private TextView textViewSoftApPasswordLabel;

    public String getTag() {
        return TAG;
    }

    /* access modifiers changed from: protected */
    public FrameLayout getBackBar() {
        return (FrameLayout) findViewById(R.id.backbar);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_ftc_network_connection);
        NetworkType fromString = NetworkType.fromString(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(NetworkConnectionFactory.NETWORK_CONNECTION_TYPE, NetworkType.globalDefaultAsString()));
        this.editTextSoftApPassword = (EditText) findViewById(R.id.editTextSoftApPassword);
        this.textViewSoftApPasswordLabel = (TextView) findViewById(R.id.textViewSoftApPasswordLabel);
        NetworkConnection networkConnection2 = NetworkConnectionFactory.getNetworkConnection(fromString, getBaseContext());
        this.networkConnection = networkConnection2;
        String deviceName = networkConnection2.getDeviceName();
        if (deviceName != "") {
            this.teamNum = getTeamNumber(deviceName);
        } else {
            this.teamNum = 0;
            deviceName = getString(R.string.wifi_direct_name_unknown);
        }
        TextView textView = (TextView) findViewById(R.id.textWifiInstructions);
        TextView textView2 = (TextView) findViewById(R.id.textViewWifiName);
        TextView textView3 = (TextView) findViewById(R.id.textViewWifiNameLabel);
        if (fromString == NetworkType.WIFIDIRECT) {
            textView.setText(getString(R.string.pair_instructions));
            textView2.setVisibility(0);
            textView2.setText(deviceName);
            textView3.setVisibility(0);
        } else if (fromString == NetworkType.SOFTAP) {
            textView.setText(getString(R.string.softap_instructions));
            textView2.setVisibility(4);
            textView3.setVisibility(4);
        }
        ((Switch) findViewById(R.id.wifi_filter)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (z) {
                    boolean unused = FtcPairNetworkConnectionActivity.this.filterForTeam = true;
                } else {
                    boolean unused2 = FtcPairNetworkConnectionActivity.this.filterForTeam = false;
                }
                FtcPairNetworkConnectionActivity.this.updateDevicesList();
            }
        });
    }

    public void onStart() {
        super.onStart();
        RobotLog.ii(TAG, "Starting Pairing with Driver Station activity");
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.sharedPref = defaultSharedPreferences;
        this.connectionOwnerIdentity = defaultSharedPreferences.getString(getString(R.string.pref_connection_owner_identity), getString(R.string.connection_owner_default));
        TextView textView = (TextView) findViewById(R.id.textViewSoftApPasswordInstructions);
        if (this.networkConnection.getNetworkType() == NetworkType.SOFTAP) {
            this.connectionOwnerPassword = this.sharedPref.getString(getString(R.string.pref_connection_owner_password), getString(R.string.connection_owner_password_default));
            this.textViewSoftApPasswordLabel.setVisibility(0);
            this.editTextSoftApPassword.setVisibility(0);
            this.editTextSoftApPassword.setText(this.connectionOwnerPassword);
            textView.setVisibility(0);
        } else {
            this.textViewSoftApPasswordLabel.setVisibility(4);
            this.editTextSoftApPassword.setVisibility(4);
            textView.setVisibility(4);
        }
        this.networkConnection.enable();
        this.networkConnection.setCallback(this);
        updateDevicesList();
        this.discoveryFuture = ThreadPool.getDefaultScheduler().scheduleAtFixedRate(new Runnable() {
            public void run() {
                FtcPairNetworkConnectionActivity.this.networkConnection.discoverPotentialConnections();
            }
        }, 0, 10000, TimeUnit.MILLISECONDS);
    }

    public void onStop() {
        super.onStop();
        this.discoveryFuture.cancel(false);
        this.networkConnection.cancelPotentialConnections();
        this.networkConnection.disable();
        this.connectionOwnerPassword = this.editTextSoftApPassword.getText().toString();
        SharedPreferences.Editor edit = this.sharedPref.edit();
        edit.putString(getString(R.string.pref_connection_owner_password), this.connectionOwnerPassword);
        edit.apply();
    }

    public void onClick(View view) {
        if (view instanceof PeerRadioButton) {
            PeerRadioButton peerRadioButton = (PeerRadioButton) view;
            if (peerRadioButton.getId() == 0) {
                this.connectionOwnerIdentity = getString(R.string.connection_owner_default);
                this.connectionOwnerPassword = getString(R.string.connection_owner_password_default);
            } else {
                this.connectionOwnerIdentity = peerRadioButton.getDeviceIdentity();
            }
            SharedPreferences.Editor edit = this.sharedPref.edit();
            edit.putString(getString(R.string.pref_connection_owner_identity), this.connectionOwnerIdentity);
            edit.apply();
            RobotLog.ii(TAG, "Setting Driver Station name to " + this.connectionOwnerIdentity);
        }
    }

    /* renamed from: com.qualcomm.ftcdriverstation.FtcPairNetworkConnectionActivity$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$qualcomm$robotcore$wifi$NetworkConnection$NetworkEvent;

        static {
            int[] iArr = new int[NetworkConnection.NetworkEvent.values().length];
            $SwitchMap$com$qualcomm$robotcore$wifi$NetworkConnection$NetworkEvent = iArr;
            try {
                iArr[NetworkConnection.NetworkEvent.PEERS_AVAILABLE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
        }
    }

    public CallbackResult onNetworkConnectionEvent(NetworkConnection.NetworkEvent networkEvent) {
        CallbackResult callbackResult = CallbackResult.NOT_HANDLED;
        if (AnonymousClass3.$SwitchMap$com$qualcomm$robotcore$wifi$NetworkConnection$NetworkEvent[networkEvent.ordinal()] != 1) {
            return callbackResult;
        }
        updateDevicesList();
        return CallbackResult.HANDLED;
    }

    private int getTeamNumber(String str) {
        int indexOf = str.indexOf(45);
        if (indexOf == -1) {
            return 0;
        }
        try {
            return Integer.parseInt(str.substring(0, indexOf));
        } catch (NumberFormatException unused) {
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public void updateDevicesList() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroupDevices);
        radioGroup.clearCheck();
        radioGroup.removeAllViews();
        PeerRadioButton peerRadioButton = new PeerRadioButton(this);
        String string = getString(R.string.connection_owner_default);
        peerRadioButton.setId(0);
        peerRadioButton.setText("None\nDo not pair with any device");
        peerRadioButton.setPadding(0, 0, 0, 24);
        peerRadioButton.setOnClickListener(this);
        peerRadioButton.setDeviceIdentity(string);
        if (this.connectionOwnerIdentity.equalsIgnoreCase(string)) {
            peerRadioButton.setChecked(true);
        }
        radioGroup.addView(peerRadioButton);
        Map<String, String> treeMap = new TreeMap<>();
        if (this.networkConnection.getNetworkType() == NetworkType.WIFIDIRECT) {
            treeMap = buildMap(((WifiDirectAssistant) this.networkConnection).getPeers());
        } else if (this.networkConnection.getNetworkType() == NetworkType.SOFTAP) {
            treeMap = buildResultsMap(((SoftApAssistant) this.networkConnection).getScanResults());
        }
        int i = 1;
        for (String next : treeMap.keySet()) {
            if (this.filterForTeam) {
                if (!next.contains(this.teamNum + "-") && !next.startsWith("0000-")) {
                }
            }
            String str = treeMap.get(next);
            PeerRadioButton peerRadioButton2 = new PeerRadioButton(this);
            int i2 = i + 1;
            peerRadioButton2.setId(i);
            if (this.networkConnection.getNetworkType() == NetworkType.WIFIDIRECT) {
                next = next + "\n" + str;
            } else if (this.networkConnection.getNetworkType() != NetworkType.SOFTAP) {
                next = "";
            }
            peerRadioButton2.setText(next);
            peerRadioButton2.setPadding(0, 0, 0, 24);
            peerRadioButton2.setDeviceIdentity(str);
            if (str.equalsIgnoreCase(this.connectionOwnerIdentity)) {
                peerRadioButton2.setChecked(true);
            }
            peerRadioButton2.setOnClickListener(this);
            radioGroup.addView(peerRadioButton2);
            i = i2;
        }
    }

    public Map<String, String> buildResultsMap(List<ScanResult> list) {
        TreeMap treeMap = new TreeMap();
        for (ScanResult next : list) {
            treeMap.put(next.SSID, next.SSID);
        }
        return treeMap;
    }

    public Map<String, String> buildMap(List<WifiP2pDevice> list) {
        TreeMap treeMap = new TreeMap();
        for (WifiP2pDevice next : list) {
            treeMap.put(PreferenceRemoterDS.getInstance().getDeviceNameForWifiP2pGroupOwner(next.deviceName), next.deviceAddress);
        }
        return treeMap;
    }

    public static class PeerRadioButton extends RadioButton {
        private String deviceIdentity = "";

        public PeerRadioButton(Context context) {
            super(context);
        }

        public String getDeviceIdentity() {
            return this.deviceIdentity;
        }

        public void setDeviceIdentity(String str) {
            this.deviceIdentity = str;
        }
    }
}
