package com.qualcomm.ftcdriverstation;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.ScanResult;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import com.qualcomm.robotcore.wifi.NetworkConnection;
import com.qualcomm.robotcore.wifi.NetworkConnectionFactory;
import com.qualcomm.robotcore.wifi.NetworkType;
import com.qualcomm.robotcore.wifi.SoftApAssistant;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.internal.network.CallbackResult;
import org.firstinspires.ftc.robotcore.internal.network.PreferenceRemoterDS;
import org.firstinspires.ftc.robotcore.internal.ui.BaseActivity;

public class FtcPairNetworkConnectionActivity extends BaseActivity implements OnClickListener, NetworkConnection.NetworkConnectionCallback {
   public static final String TAG = "FtcPairNetworkConnection";
   private String connectionOwnerIdentity;
   private String connectionOwnerPassword;
   private ScheduledFuture discoveryFuture;
   private EditText editTextSoftApPassword;
   private boolean filterForTeam = true;
   private NetworkConnection networkConnection;
   private SharedPreferences sharedPref;
   private int teamNum;
   private TextView textViewSoftApPasswordLabel;

   private int getTeamNumber(String var1) {
      int var2 = var1.indexOf(45);
      int var3 = 0;
      if (var2 != -1) {
         var1 = var1.substring(0, var2);

         try {
            var2 = Integer.parseInt(var1);
         } catch (NumberFormatException var4) {
            return var3;
         }

         var3 = var2;
      }

      return var3;
   }

   private void updateDevicesList() {
      RadioGroup var1 = (RadioGroup)this.findViewById(2131231036);
      var1.clearCheck();
      var1.removeAllViews();
      FtcPairNetworkConnectionActivity.PeerRadioButton var2 = new FtcPairNetworkConnectionActivity.PeerRadioButton(this);
      String var3 = this.getString(2131624149);
      var2.setId(0);
      var2.setText("None\nDo not pair with any device");
      var2.setPadding(0, 0, 0, 24);
      var2.setOnClickListener(this);
      var2.setDeviceIdentity(var3);
      if (this.connectionOwnerIdentity.equalsIgnoreCase(var3)) {
         var2.setChecked(true);
      }

      var1.addView(var2);
      Object var9 = new TreeMap();
      if (this.networkConnection.getNetworkType() == NetworkType.WIFIDIRECT) {
         var9 = this.buildMap(((WifiDirectAssistant)this.networkConnection).getPeers());
      } else if (this.networkConnection.getNetworkType() == NetworkType.SOFTAP) {
         var9 = this.buildResultsMap(((SoftApAssistant)this.networkConnection).getScanResults());
      }

      Iterator var4 = ((Map)var9).keySet().iterator();
      int var5 = 1;

      while(true) {
         StringBuilder var6;
         do {
            if (!var4.hasNext()) {
               return;
            }

            var3 = (String)var4.next();
            if (!this.filterForTeam) {
               break;
            }

            var6 = new StringBuilder();
            var6.append(this.teamNum);
            var6.append("-");
         } while(!var3.contains(var6.toString()) && !var3.startsWith("0000-"));

         String var7 = (String)((Map)var9).get(var3);
         FtcPairNetworkConnectionActivity.PeerRadioButton var10 = new FtcPairNetworkConnectionActivity.PeerRadioButton(this);
         var10.setId(var5);
         if (this.networkConnection.getNetworkType() == NetworkType.WIFIDIRECT) {
            StringBuilder var8 = new StringBuilder();
            var8.append(var3);
            var8.append("\n");
            var8.append(var7);
            var3 = var8.toString();
         } else if (this.networkConnection.getNetworkType() != NetworkType.SOFTAP) {
            var3 = "";
         }

         var10.setText(var3);
         var10.setPadding(0, 0, 0, 24);
         var10.setDeviceIdentity(var7);
         if (var7.equalsIgnoreCase(this.connectionOwnerIdentity)) {
            var10.setChecked(true);
         }

         var10.setOnClickListener(this);
         var1.addView(var10);
         ++var5;
      }
   }

   public Map buildMap(List var1) {
      TreeMap var2 = new TreeMap();
      Iterator var3 = var1.iterator();

      while(var3.hasNext()) {
         WifiP2pDevice var4 = (WifiP2pDevice)var3.next();
         var2.put(PreferenceRemoterDS.getInstance().getDeviceNameForWifiP2pGroupOwner(var4.deviceName), var4.deviceAddress);
      }

      return var2;
   }

   public Map buildResultsMap(List var1) {
      TreeMap var2 = new TreeMap();
      Iterator var4 = var1.iterator();

      while(var4.hasNext()) {
         ScanResult var3 = (ScanResult)var4.next();
         var2.put(var3.SSID, var3.SSID);
      }

      return var2;
   }

   protected FrameLayout getBackBar() {
      return (FrameLayout)this.findViewById(2131230804);
   }

   public String getTag() {
      return "FtcPairNetworkConnection";
   }

   public void onClick(View var1) {
      if (var1 instanceof FtcPairNetworkConnectionActivity.PeerRadioButton) {
         FtcPairNetworkConnectionActivity.PeerRadioButton var2 = (FtcPairNetworkConnectionActivity.PeerRadioButton)var1;
         if (var2.getId() == 0) {
            this.connectionOwnerIdentity = this.getString(2131624149);
            this.connectionOwnerPassword = this.getString(2131624150);
         } else {
            this.connectionOwnerIdentity = var2.getDeviceIdentity();
         }

         Editor var3 = this.sharedPref.edit();
         var3.putString(this.getString(2131624404), this.connectionOwnerIdentity);
         var3.apply();
         StringBuilder var4 = new StringBuilder();
         var4.append("Setting Driver Station name to ");
         var4.append(this.connectionOwnerIdentity);
         RobotLog.ii("FtcPairNetworkConnection", var4.toString());
      }

   }

   protected void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.setContentView(2131427367);
      NetworkType var2 = NetworkType.fromString(PreferenceManager.getDefaultSharedPreferences(this.getBaseContext()).getString("NETWORK_CONNECTION_TYPE", NetworkType.globalDefaultAsString()));
      this.editTextSoftApPassword = (EditText)this.findViewById(2131230887);
      this.textViewSoftApPasswordLabel = (TextView)this.findViewById(2131231143);
      NetworkConnection var6 = NetworkConnectionFactory.getNetworkConnection(var2, this.getBaseContext());
      this.networkConnection = var6;
      String var7 = var6.getDeviceName();
      if (var7 != "") {
         this.teamNum = this.getTeamNumber(var7);
      } else {
         this.teamNum = 0;
         var7 = this.getString(2131624687);
      }

      TextView var3 = (TextView)this.findViewById(2131231148);
      TextView var4 = (TextView)this.findViewById(2131231144);
      TextView var5 = (TextView)this.findViewById(2131231145);
      if (var2 == NetworkType.WIFIDIRECT) {
         var3.setText(this.getString(2131624372));
         var4.setVisibility(0);
         var4.setText(var7);
         var5.setVisibility(0);
      } else if (var2 == NetworkType.SOFTAP) {
         var3.setText(this.getString(2131624571));
         var4.setVisibility(4);
         var5.setVisibility(4);
      }

      ((Switch)this.findViewById(2131231191)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
         public void onCheckedChanged(CompoundButton var1, boolean var2) {
            if (var2) {
               FtcPairNetworkConnectionActivity.this.filterForTeam = true;
            } else {
               FtcPairNetworkConnectionActivity.this.filterForTeam = false;
            }

            FtcPairNetworkConnectionActivity.this.updateDevicesList();
         }
      });
   }

   public CallbackResult onNetworkConnectionEvent(NetworkConnection.NetworkEvent var1) {
      CallbackResult var2 = CallbackResult.NOT_HANDLED;
      CallbackResult var3;
      if (var1.ordinal() != 1) {
         var3 = var2;
      } else {
         this.updateDevicesList();
         var3 = CallbackResult.HANDLED;
      }

      return var3;
   }

   public void onStart() {
      super.onStart();
      RobotLog.ii("FtcPairNetworkConnection", "Starting Pairing with Driver Station activity");
      SharedPreferences var1 = PreferenceManager.getDefaultSharedPreferences(this);
      this.sharedPref = var1;
      this.connectionOwnerIdentity = var1.getString(this.getString(2131624404), this.getString(2131624149));
      TextView var2 = (TextView)this.findViewById(2131231142);
      if (this.networkConnection.getNetworkType() == NetworkType.SOFTAP) {
         this.connectionOwnerPassword = this.sharedPref.getString(this.getString(2131624405), this.getString(2131624150));
         this.textViewSoftApPasswordLabel.setVisibility(0);
         this.editTextSoftApPassword.setVisibility(0);
         this.editTextSoftApPassword.setText(this.connectionOwnerPassword);
         var2.setVisibility(View.VISIBLE);
      } else {
         this.textViewSoftApPasswordLabel.setVisibility(View.INVISIBLE);
         this.editTextSoftApPassword.setVisibility(View.INVISIBLE);
         var2.setVisibility(View.INVISIBLE);
      }

      this.networkConnection.enable();
      this.networkConnection.setCallback(this);
      this.updateDevicesList();
      this.discoveryFuture = ThreadPool.getDefaultScheduler().scheduleAtFixedRate(new Runnable() {
         public void run() {
            FtcPairNetworkConnectionActivity.this.networkConnection.discoverPotentialConnections();
         }
      }, 0L, 10000L, TimeUnit.MILLISECONDS);
   }

   public void onStop() {
      super.onStop();
      this.discoveryFuture.cancel(false);
      this.networkConnection.cancelPotentialConnections();
      this.networkConnection.disable();
      this.connectionOwnerPassword = this.editTextSoftApPassword.getText().toString();
      Editor var1 = this.sharedPref.edit();
      var1.putString(this.getString(2131624405), this.connectionOwnerPassword);
      var1.apply();
   }

   public static class PeerRadioButton extends RadioButton {
      private String deviceIdentity = "";

      public PeerRadioButton(Context var1) {
         super(var1);
      }

      public String getDeviceIdentity() {
         return this.deviceIdentity;
      }

      public void setDeviceIdentity(String var1) {
         this.deviceIdentity = var1;
      }
   }
}
