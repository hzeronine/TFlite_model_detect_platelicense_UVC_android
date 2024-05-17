package com.example.platedetect2.utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.platedetect2.R;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.Arrays;

public class IRHelper {

    public enum UsbPermission { Unknown, Requested, Granted, Denied }
    public static final String INTENT_ACTION_GRANT_USB =  "com.example.platedetect2.GRANT_USB";
    public static IRHelper instance;
    private DeviceListControl instanceDeviceList = DeviceListControl.getInstance();
    private Context mContext;
    private SerialInputOutputManager.Listener mListener;
    public String Command;
    IRBytesStored instanceIRBytesStored = IRBytesStored.getInstance();


    public static final int WRITE_WAIT_MILLIS = 2000;
    public static final int READ_WAIT_MILLIS = 2000;
    private final Handler mainLooper;
    public SerialInputOutputManager usbIoManager;
    private IRHelper.aControlLines controlLines;
    public UsbSerialPort usbSerialPort;
    private int deviceId, portNum;
    public boolean withIoManager = true;
    private UsbPermission usbPermission = UsbPermission.Unknown;
    final private int  baudRate = 19600;

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    private boolean connected = false;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(INTENT_ACTION_GRANT_USB.equals(intent.getAction())) {
                usbPermission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        ? UsbPermission.Granted : UsbPermission.Denied;
                connect();
            }
        }
    };

    public IRHelper(Context context, SerialInputOutputManager.Listener listener) {
        mContext = context;
        mListener = listener;
        mainLooper = new Handler(Looper.getMainLooper());
    }

    public static IRHelper getInstance(Context context, SerialInputOutputManager.Listener listener){
        if(instance == null){
            instance = new IRHelper(context,listener);
        }
        return instance;
    }

    public UsbPermission getUsbPermission() {
        return usbPermission;
    }

    public boolean isConnected() {
        return connected;
    }

    public static IRHelper getNewInstance(Context context, SerialInputOutputManager.Listener listener){
        instance = new IRHelper(context,listener);
        return instance;
    }
    public void setControlLines(aControlLines controlLines){
        this.controlLines = controlLines;
    }
    public void register(){
        ContextCompat.registerReceiver(mContext, broadcastReceiver, new IntentFilter(INTENT_ACTION_GRANT_USB), ContextCompat.RECEIVER_NOT_EXPORTED);
    }
    public void unregister(){
        mContext.unregisterReceiver(broadcastReceiver);
    }
    public void HandlerPost(){
        mainLooper.post(this::connect);
    }
    public void connect() {
        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        for(UsbDevice v : usbManager.getDeviceList().values())
            if(instanceDeviceList.FindArduinoDevice(v.getVendorId())){
                device = v;
                break;
            }
        if(device == null) {
            status("connection failed: device not found");
            return;
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if(driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        if(driver == null) {
            status("connection failed: no driver for device");
            return;
        }
        if(driver.getPorts().size() < portNum) {
            status("connection failed: not enough ports at device");
            return;
        }
        usbSerialPort = driver.getPorts().get(portNum);
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if(usbConnection == null && usbPermission == UsbPermission.Unknown && !usbManager.hasPermission(driver.getDevice())) {
            usbPermission = UsbPermission.Requested;
            int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_MUTABLE : 0;
            Intent intent = new Intent(INTENT_ACTION_GRANT_USB);
            intent.setPackage(mContext.getPackageName());
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(mContext, 0, intent, flags);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }
        if(usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice()))
                status("connection failed: permission denied");
            else
                status("connection failed: open failed");
            return;
        }

        try {
            usbSerialPort.open(usbConnection);
            try{
                usbSerialPort.setParameters(baudRate, 8, 1, UsbSerialPort.PARITY_NONE);
            }catch (UnsupportedOperationException e){
                status("unsupport setparameters");
            }
            if(withIoManager) {
                usbIoManager = new SerialInputOutputManager(usbSerialPort, mListener);
                usbIoManager.start();
            }
            status("connected");
            connected = true;
            controlLines.start();
        } catch (Exception e) {
            status("connection failed: " + e.getMessage());
            disconnect();
        }
    }

    public void disconnect() {
        connected = false;
        controlLines.stop();
        if(usbIoManager != null) {
            usbIoManager.setListener(null);
            usbIoManager.stop();
        }
        usbIoManager = null;
        try {
            usbSerialPort.close();
        } catch (IOException ignored) {}
        usbSerialPort = null;
    }

    public void send(String str) {
        if(!connected) {
            Toast.makeText(mContext, "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if(Command == "DataReceive"){
                byte[] data = instanceIRBytesStored.getIR_array_data();
                usbSerialPort.write(data, WRITE_WAIT_MILLIS);
            }
            byte[] data = (str + '\n').getBytes();
            SpannableStringBuilder spn = new SpannableStringBuilder();
            spn.append("send " + data.length + " bytes\n");
            spn.append(HexDump.dumpHexString(data)).append("\n");
            spn.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            controlLines.spnRespone(spn);
//            receiveText.append(spn);
            usbSerialPort.write(data, WRITE_WAIT_MILLIS);
        } catch (Exception e) {
            mListener.onRunError(e);
        }
    }

    private void read() {
        if(!connected) {
            Toast.makeText(mContext, "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            byte[] buffer = new byte[8192];
            int len = usbSerialPort.read(buffer, READ_WAIT_MILLIS);
            receive(Arrays.copyOf(buffer, len));
        } catch (IOException e) {
            // when using read with timeout, USB bulkTransfer returns -1 on timeout _and_ errors
            // like connection loss, so there is typically no exception thrown here on error
            status("connection lost: " + e.getMessage());
            disconnect();
        }
    }

    public void receive(byte[] data) {
        SpannableStringBuilder spn = new SpannableStringBuilder();
        spn.append("receive " + data.length + " bytes\n");
        if(Command == "Stored")
            instanceIRBytesStored.setIRData(data);
        if(data.length > 0)
            spn.append(HexDump.dumpHexString(data)).append("\n");
        spn.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorRecieveText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        controlLines.spnRespone(spn);
    }

    public void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str+'\n');
        spn.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        controlLines.spnRespone(spn);
    }

    public interface aControlLines{
        void toggle(View v);
        void run();
        void start();
        void stop();
        void spnRespone(SpannableStringBuilder spn);
    }
}
