package com.example.platedetect2.Module;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.utils.HandlerThreadHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyUSBMonitor {
    private static final boolean DEBUG = false;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String TAG = "MyUSBMonitor";
    Context mContext;
    UsbDevice mDevice;
    UsbManager mUSBManager;
    private final Handler mAsyncHandler;
    private PendingIntent mPermissionIntent = null;
    resultCallBack mOnDeviceConnectListener;

    public MyUSBMonitor(Context context, resultCallBack listener) {
        if (DEBUG) Log.v(TAG, "MyUSBMonitor:Constructor");
        this.mContext = context;
        this.mUSBManager = (UsbManager) context.getSystemService(context.USB_SERVICE);
        this.mAsyncHandler = HandlerThreadHandler.createHandler(TAG);
        this.mOnDeviceConnectListener = listener;
    }

    public HashMap<String, UsbDevice> getListDevices(){
        return mUSBManager.getDeviceList();
    }

    public void signin (){
        if(mPermissionIntent == null){
            if (DEBUG) Log.v(TAG, "MyUSBMonitor:signin");
            mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            mContext.registerReceiver(usbReceiver, filter);
            return;
        }

        if (DEBUG) Log.v(TAG, "MyUSBMonitor:signined");
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if(device != null){
                        Toast.makeText(context, "Thiết bị USB đã kết nối222", Toast.LENGTH_SHORT).show();
                        mOnDeviceConnectListener.grandted();
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Toast.makeText(context, "Thiết bị USB đã kết nối", Toast.LENGTH_SHORT).show();
                synchronized (this) {
                    HashMap<String, UsbDevice> deviceList = mUSBManager.getDeviceList();
                    for (UsbDevice device : deviceList.values()) {
                        // Kiểm tra xem thiết bị có phải là camera USB không, có thể sử dụng các tiêu chí khác nhau để nhận biết camera USB
                        if (mUSBManager.hasPermission(device) == false) {
                            // Nếu là camera USB, yêu cầu quyền truy cập
                            mUSBManager.requestPermission(device, mPermissionIntent);
                        }
                    }
                }
            }
        }
    };

    public List<UsbDevice> getDeviceList(final List<DeviceFilter> filters) throws IllegalStateException {
        final HashMap<String, UsbDevice> deviceList = mUSBManager.getDeviceList();
        final List<UsbDevice> result = new ArrayList<UsbDevice>();
        if (deviceList != null) {
            if ((filters == null) || filters.isEmpty()) {
                result.addAll(deviceList.values());
            } else {
                for (final UsbDevice device: deviceList.values() ) {
                    for (final DeviceFilter filter: filters) {
                        if ((filter != null) && filter.matches(device)) {
                            // when filter matches
                            if (!filter.isExclude) {
                                result.add(device);
                            }
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }
    //Interface
    public interface resultCallBack{
        void grandted();
    }
}
