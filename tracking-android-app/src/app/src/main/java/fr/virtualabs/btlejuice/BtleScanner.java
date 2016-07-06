package fr.virtualabs.btlejuice;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

@TargetApi(21)
public class BtleScanner extends AppCompatActivity {

    private DeviceAdapter mAdapter;
    private ListView mList;
    private TextView mStatusBar;
    private BluetoothManager mBleManager;
    private BluetoothAdapter mBleAdapter;
    private BluetoothLeScanner mLEScanner;
    private android.os.Handler mHandler;
    private boolean mScanning;
    private boolean mPermissions;
    private ScanSettings mSettings;
    private List<ScanFilter> mFilters;
    private DeviceRanging mRanging;

    protected static final String TAG = "MonitoringActivity";
    private final static int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int SCAN_PERIOD = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btle_scanner);
        mPermissions = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                mPermissions = false;
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        mAdapter = new DeviceAdapter(
                this,
                R.layout.device_info,
                new ArrayList<DeviceInfo>()
        );

        mHandler = new Handler();
        mList=(ListView)findViewById(R.id.deviceList);
        mList.setAdapter(mAdapter);
        mStatusBar = (TextView)findViewById(R.id.status);

        handleScan();
    }

    public void handleScan() {
        if (!mPermissions)
            return;

        mRanging = new DeviceRanging(mAdapter, mList, mStatusBar, getApplicationContext());
        if (!mScanning) {
            Log.d(TAG, "Scan started");
            mRanging.scanLeDevice(true);
            mScanning = true;
        } else {
            Log.d(TAG, "Scan resumed");
            mRanging.resumeScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissions = true;
                    handleScan();
                    Log.d(TAG, "Coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleScan();
    }

    @Override
    protected void onDestroy() {
        if (mRanging != null)
            mRanging.scanLeDevice(false);
        super.onDestroy();
    }

    public void setStatus(String status) {
        mStatusBar.setText(status);
    }

}
