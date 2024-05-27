package com.example.platedetect2;


import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.utils.widget.ImageFilterButton;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.platedetect2.Dialog.AlterDialogSelection;
import com.example.platedetect2.utils.CustomProber;
import com.example.platedetect2.utils.DeviceListControl;
import com.example.platedetect2.utils.IRBytesStored;
import com.example.platedetect2.utils.IRHelper;
import com.example.platedetect2.utils.MySharedPreferences;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.util.ArrayList;

public class DevicesFragment extends Fragment {



    public static class ListItem {
        UsbDevice device;
        int port;
        UsbSerialDriver driver;

        ListItem(UsbDevice device, int port, UsbSerialDriver driver) {
            this.device = device;
            this.port = port;
            this.driver = driver;
        }
        public UsbDevice getDevice() {
            return device;
        }

        public void setDevice(UsbDevice device) {
            this.device = device;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public UsbSerialDriver getDriver() {
            return driver;
        }

        public void setDriver(UsbSerialDriver driver) {
            this.driver = driver;
        }
    }

    private final ArrayList<ListItem> listItems = new ArrayList<>();
    private int baudRate = 19200;
    private boolean withIoManager = true;
    private DeviceListControl instance = DeviceListControl.getInstance();
    private IRBytesStored storedInstance = IRBytesStored.getInstance();
    String ACTION_USB_PERMISSION = "com.example.platedetect2.USB_PERMISSION";
    MySharedPreferences mMySharedPreferences;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
                refresh();
            }
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())){
                refresh();
            }
            if(ACTION_USB_PERMISSION.equals(intent.getAction())){
                refresh();
            }
        }
    };
    public DevicesFragment() {

    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        IR_setup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(instance.Arduino_isSet() == false)
                    return;
                mMySharedPreferences.putIntValue("CameraVeronID", Camera_VeronID);
                mMySharedPreferences.putIntValue("ArduinoVeronID", Arduino_VeronID);
                mMySharedPreferences.putIRValue("OpenIR", storedInstance.getIR_array_OpenData());
                mMySharedPreferences.putIRValue("CloseIR", storedInstance.getIR_array_CloseData());
                Fragment fragment = new IR_setup_fragment();
                getParentFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "terminal")
                        .addToBackStack(this.getClass().getName()).commit();
            }
        });
        r_check_in.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.isChecked()){
                    storedInstance.setStatusCheck(0);
                    Toast.makeText(getActivity(),"check in: " +storedInstance.getStatusCheck(),Toast.LENGTH_SHORT ).show();
                }
            }
        });
        r_check_out.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.isChecked()){
                    storedInstance.setStatusCheck(1);
                    Toast.makeText(getActivity(),"check in: " +storedInstance.getStatusCheck(),Toast.LENGTH_SHORT ).show();
                }
            }
        });
        IR_selection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listItems.size() <= 0){
                    Toast.makeText(getActivity(),"No device found",Toast.LENGTH_SHORT).show();
                    return;
                }
                dialogFragment = new AlterDialogSelection(listItems,1101 );
                dialogFragment.show(getChildFragmentManager(),"DeviceSelection");
            }
        });
        Camera_selection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listItems.size() <= 0){
                    Toast.makeText(getActivity(),"No device found",Toast.LENGTH_SHORT).show();
                    return;
                }
                dialogFragment = new AlterDialogSelection(listItems,1102);
                dialogFragment.show(getChildFragmentManager(),"DeviceSelection");
            }
        });
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(instance.Arduino_isSet() == false)
                {
                    Toast.makeText(getActivity(),"Connect IR Device First!", Toast.LENGTH_SHORT).show();
                    return;
                } else if(instance.Camera_isSet() == false)
                {
                    Toast.makeText(getActivity(),"Connect Camera Device First!", Toast.LENGTH_SHORT).show();
                    return;

                } else if (storedInstance.getStatusCheck() == -1) {
                    Toast.makeText(getActivity(),"Select kind of devices please!", Toast.LENGTH_SHORT).show();
                    return;

                } else if (storedInstance.isIR_isSet() == false) {
                    Toast.makeText(getActivity(),"No IR data found set up first please!", Toast.LENGTH_SHORT).show();
                    return;

                }
                mMySharedPreferences.putIntValue("CameraVeronID", Camera_VeronID);
                mMySharedPreferences.putIntValue("ArduinoVeronID", Arduino_VeronID);
                mMySharedPreferences.putIRValue("OpenIR", storedInstance.getIR_array_OpenData());
                mMySharedPreferences.putIRValue("CloseIR", storedInstance.getIR_array_CloseData());


                Intent intent = new Intent(getContext(), UVC_Camera_two.class);
                startActivity(intent);
            }
        });
    }
    int Camera_VeronID = -1;
    int Arduino_VeronID = -1;
    private void init() {
        byte[] IROpen_data = mMySharedPreferences.getIRValue("OpenIR");
        byte[] IRClosedata = mMySharedPreferences.getIRValue("CloseIR");
        Camera_VeronID = mMySharedPreferences.getIntValue("CameraVeronID");
        Arduino_VeronID = mMySharedPreferences.getIntValue("ArduinoVeronID");
        Toast.makeText(getActivity(),Camera_VeronID +"" , Toast.LENGTH_SHORT).show();
        Toast.makeText(getActivity(),Arduino_VeronID +"" , Toast.LENGTH_SHORT).show();
//        if(IROpen_data != null){
////            storedInstance.setIR_OpenData(IROpen_data);
//        }
//        if(IRClosedata != null)
//        {
////            storedInstance.setIR_CloseData(IRClosedata);
//        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    View root;
    TextView IR_selection,Camera_selection;
    RadioButton r_check_in, r_check_out;
    DialogFragment dialogFragment;
    ImageFilterButton IR_setup_btn;
    TextView edt_location;
    AppCompatButton start_btn;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.device_list_header,container,false);
        IR_selection = root.findViewById(R.id.IR_devices_selection);
        Camera_selection = root.findViewById(R.id.Camera_devices_selection);
        r_check_in = root.findViewById(R.id.radio_checkIn);
        r_check_out = root.findViewById(R.id.radio_checkOut);
        IR_setup_btn = root.findViewById(R.id.IR_setup_data_btn);
        edt_location = root.findViewById(R.id.edt_location);
        start_btn = root.findViewById(R.id.start_btn);
        mMySharedPreferences = new MySharedPreferences(getActivity());
        init();
        refresh();

        String text = IRBytesStored.getInstance().getEmailLocation().getLocationName();
        edt_location.setText(Html.fromHtml("Cơ sở hiện tại: <b><font color = #000000>" + text + "</font></b>"));
        return root;
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_devices, menu);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
        IntentFilter intentFilter = new IntentFilter(IRHelper.INTENT_ACTION_GRANT_USB);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        getActivity().registerReceiver( broadcastReceiver, intentFilter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.refresh) {
            refresh();
            return true;
        } else if (id ==R.id.baud_rate) {
            final String[] values = getResources().getStringArray(R.array.baud_rates);
            int pos = java.util.Arrays.asList(values).indexOf(String.valueOf(baudRate));
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Baud rate");
            builder.setSingleChoiceItems(values, pos, (dialog, which) -> {
                baudRate = Integer.parseInt(values[which]);
                dialog.dismiss();
            });
            builder.create().show();
            return true;
        } else if (id ==R.id.read_mode) {
            final String[] values = getResources().getStringArray(R.array.read_modes);
            int pos = withIoManager ? 0 : 1; // read_modes[0]=event/io-manager, read_modes[1]=direct
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Read mode");
            builder.setSingleChoiceItems(values, pos, (dialog, which) -> {
                withIoManager = (which == 0);
                dialog.dismiss();
            });
            builder.create().show();
            return true;
        } else if (id ==R.id.start) {
            if(instance.Arduino_isSet() == false)
            {
              Toast.makeText(getActivity(),"Connect IR Device First!", Toast.LENGTH_SHORT).show();
              return true;
            } else if(instance.Camera_isSet() == false)
            {
                Toast.makeText(getActivity(),"Connect Camera Device First!", Toast.LENGTH_SHORT).show();
                return true;
            } else if (storedInstance.getStatusCheck() == -1) {
                Toast.makeText(getActivity(),"Select kind of devices please!", Toast.LENGTH_SHORT).show();
                return true;
            }
            Intent intent = new Intent(getContext(), UVC_Camera_two.class);
            startActivity(intent);
//            getActivity().finish();
//            Fragment fragment = new ArduinoConnectionActivity();
//            getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "terminal")
//                    .addToBackStack(null).commit();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    void refresh() {
        UsbManager usbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        UsbSerialProber usbDefaultProber = UsbSerialProber.getDefaultProber();
        UsbSerialProber usbCustomProber = CustomProber.getCustomProber();
        listItems.clear();
        instance.setCamera_isSet(false);
        instance.setArduino_isSet(false);
        if(dialogFragment != null && dialogFragment.isResumed()){
            dialogFragment.dismiss();
            dialogFragment = null;
        }
        for(UsbDevice device : usbManager.getDeviceList().values()) {
            if(!usbManager.hasPermission(device)){
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
                usbManager.requestPermission(device,mPermissionIntent);
                continue;
            }
            UsbSerialDriver driver = usbDefaultProber.probeDevice(device);
            if(driver == null) {
                driver = usbCustomProber.probeDevice(device);
            }
            ListItem newDataItem = null;

            if(driver != null) {
                for(int port = 0; port < driver.getPorts().size(); port++){
                    newDataItem = new ListItem(device, port, driver);
                    listItems.add(newDataItem);
                }
            } else {
                newDataItem = new ListItem(device, 0, null);
                listItems.add(newDataItem);
            }

            DeviceListControl.ModelDevice ModelData =
                    new DeviceListControl.ModelDevice(newDataItem.device,
                            newDataItem.device.getVendorId(), newDataItem.port, newDataItem.driver);

            if(instance.getCameraDevice().size() > 0 ){
                if(instance.getCameraDevice().get(0).getvendorId() == device.getVendorId()){
                    instance.setCamera_isSet(true);
                    Camera_selection.setText(device.getProductName());
                }
            }else if(( Camera_VeronID != -1 && Camera_VeronID == device.getVendorId())  ){
                instance.setCamera_isSet(true);
                instance.getCameraDevice().clear();
                instance.addNewCameraDevice(ModelData);
                Camera_selection.setText(newDataItem.getDevice().getProductName());
                Toast.makeText(getActivity(),"Setup Camera success",Toast.LENGTH_SHORT).show();
            }

            if((instance.getArduinoDevice().size() > 0
                    && instance.getArduinoDevice().get(0).getvendorId() == device.getVendorId())){
                instance.setArduino_isSet(true);
                IR_selection.setText(device.getProductName());
            }else if(( Arduino_VeronID != -1 && Arduino_VeronID == device.getVendorId()) ){
                instance.setArduino_isSet(true);
                instance.getArduinoDevice().clear();
                instance.addNewArduinoDevice(ModelData);
                IR_selection.setText(newDataItem.getDevice().getProductName());
                Toast.makeText(getActivity(),"Setup IR success",Toast.LENGTH_SHORT).show();
            }
        }

        if(instance.Camera_isSet() == false){
            Camera_selection.setText(R.string.hint_device);

        }
        if(instance.Arduino_isSet() == false){
            IR_selection.setText(R.string.hint_device);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        Toast.makeText(getActivity(),"arduino",Toast.LENGTH_SHORT).show();
        ListItem newDataItem = listItems.get(resultCode);
        DeviceListControl.ModelDevice ModelData =
                new DeviceListControl.ModelDevice(newDataItem.device,
                        newDataItem.device.getVendorId(), newDataItem.port, newDataItem.driver);
        if (requestCode == 1101){
            Arduino_VeronID = ModelData.getvendorId();
            instance.setArduino_isSet(true);
            instance.getArduinoDevice().clear();
            instance.addNewArduinoDevice(ModelData);
            IR_selection.setText(newDataItem.getDevice().getProductName());
            Toast.makeText(getActivity(),"Setup IR success",Toast.LENGTH_SHORT).show();
        } else if (requestCode == 1102) {
            Camera_VeronID = ModelData.getvendorId();
            instance.setCamera_isSet(true);
            instance.getCameraDevice().clear();
            instance.addNewCameraDevice(ModelData);
            Camera_selection.setText(newDataItem.getDevice().getProductName());
            Toast.makeText(getActivity(),"Setup Camera success",Toast.LENGTH_SHORT).show();
        }
    }
}