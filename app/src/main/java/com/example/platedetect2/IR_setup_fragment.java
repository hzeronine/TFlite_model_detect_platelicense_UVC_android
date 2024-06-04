package com.example.platedetect2;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.platedetect2.utils.DeviceListControl;
import com.example.platedetect2.utils.IRBytesStored;
import com.example.platedetect2.utils.IRHelper;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.EnumSet;

import dev.sagar.progress_button.ProgressButton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link IR_setup_fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IR_setup_fragment extends Fragment implements SerialInputOutputManager.Listener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public IR_setup_fragment() {
        mainLooper = new Handler(Looper.getMainLooper());
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment IR_setup_fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static IR_setup_fragment newInstance(String param1, String param2) {
        IR_setup_fragment fragment = new IR_setup_fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setup_button();
    }

    private final Handler mainLooper;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    View root;
    IRHelper instanceIRHelper;
    IRBytesStored instanceIRStoredHelper;
    View BottomSheetView;
    ControlLines controlLines;
    View sendBtn;
    TextView sendText, txt_status_device;
    RelativeLayout Open_IR_Set_btn, Close_IR_Set_btn;
    private TextView receiveText;
    int btn_setup_pressed = -1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_ir_setup_fragment, container, false);
        BottomSheetView = root.findViewById(R.id.bottom_sheet_layout);
        controlLines = new ControlLines(BottomSheetView);
        Open_IR_Set_btn = root.findViewById(R.id.OpenSetup_btn);
        Close_IR_Set_btn = root.findViewById(R.id.CloseSetup_btn);
        txt_status_device = root.findViewById(R.id.txt_status_device);
        receiveText = BottomSheetView.findViewById(R.id.receive_text);
        sendBtn = BottomSheetView.findViewById(R.id.send_btn);
        sendText = BottomSheetView.findViewById(R.id.send_text);
        instanceIRHelper = IRHelper.getNewInstance(getActivity(), this);
        instanceIRStoredHelper = IRBytesStored.getInstance();

        instanceIRHelper.setControlLines(controlLines);

        sendBtn.setOnClickListener(v ->{
            instanceIRHelper.Command = sendText.getText().toString();
            instanceIRHelper.send(instanceIRHelper.Command);
        });
        root.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getParentFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                }
            }
        });
        return root;
    }

    private void setup_button() {
        if(instanceIRStoredHelper.getIR_array_OpenData() == null){
            instanceIRStoredHelper.setIR_isSet(true);
            Open_IR_Set_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(instanceIRHelper.isConnected()){
                        if(btn_setup_pressed != -1 ){
                            Toast.makeText(getActivity(), "Other function receive is acting",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        btn_setup_pressed = 1;
                        Open_IR_Set_btn.findViewById(R.id.Open_progress_bar).setVisibility(View.VISIBLE);
                        ((TextView)Open_IR_Set_btn.findViewById(R.id.status_set_open_btn)).setText("Hãy Nhấn Nút Mở!");

                        instanceIRHelper.Command = "receiveOpen";
                        instanceIRHelper.send(instanceIRHelper.Command);
                    }
                }
            });
        }
        else{
//            mainLooper.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    instanceIRHelper.Command = "OpenDataReceive";
//                    instanceIRHelper.send("OpenDataReceive");
//                    instanceIRStoredHelper.setIR_isSet(true);
//                }
//            }, 0);
            instanceIRStoredHelper.setIR_isSet(true);
            Re_setup_open();

        }

        if(instanceIRStoredHelper.getIR_array_CloseData() == null){
            instanceIRStoredHelper.setIR_isSet(true);
            Close_IR_Set_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(instanceIRHelper.isConnected()){
                        if(btn_setup_pressed != -1 ){
                            Toast.makeText(getActivity(), "Other function receive is acting",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        btn_setup_pressed = 2;
                        Close_IR_Set_btn.findViewById(R.id.Close_progress_bar).setVisibility(View.VISIBLE);
                        ((TextView)Close_IR_Set_btn.findViewById(R.id.status_set_close_btn)).setText("Hãy Nhấn Nút Đóng!");
                        instanceIRHelper.Command = "receiveClose";
                        instanceIRHelper.send(instanceIRHelper.Command);

                    }
                }
            });
        } else{
//            mainLooper.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    instanceIRHelper.Command = "CloseDataReceive";
//                    instanceIRHelper.send("CloseDataReceive");
//                    instanceIRStoredHelper.setIR_isSet(true);
//                }
//            }, 1500);
            instanceIRStoredHelper.setIR_isSet(true);
            Re_setup_close();

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        instanceIRHelper.register();
    }

    @Override
    public void onStop() {
        instanceIRHelper.unregister();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!instanceIRHelper.isConnected() && (instanceIRHelper.getUsbPermission() == IRHelper.UsbPermission.Unknown || instanceIRHelper.getUsbPermission()  == IRHelper.UsbPermission.Granted))
            instanceIRHelper.HandlerPost();
    }

    @Override
    public void onPause() {
        if(instanceIRHelper.isConnected()) {
            instanceIRHelper.status("disconnected");
            instanceIRHelper.disconnect();
        }
        super.onPause();
    }
    @Override
    public void onNewData(byte[] data) {
        mainLooper.post(() -> {
            if(instanceIRHelper.Command != null &&
                    (instanceIRHelper.Command.equals("receiveOpen")
                            || instanceIRHelper.Command.equals("receiveClose"))){
                instanceIRHelper.Command = "Stored";
                return;
            }
            if(instanceIRHelper.Command != null &&
                    (instanceIRHelper.Command.equals("Stored") && btn_setup_pressed == 1)){
                instanceIRHelper.send(instanceIRHelper.Command);
                instanceIRHelper.Command = "OpenStored";
                refresh();
            }
            if(instanceIRHelper.Command != null &&
                    (instanceIRHelper.Command.equals("Stored") && btn_setup_pressed == 2)){
                instanceIRHelper.send(instanceIRHelper.Command);
                instanceIRHelper.Command = "CloseStored";
                refresh();
            }
            instanceIRHelper.receive(data);
        });
    }

    @Override
    public void onRunError(Exception e) {
        mainLooper.post(() -> {
            instanceIRHelper.status("connection lost: " + e.getMessage());
            disconnect();
        });
    }

    private void disconnect() {
        instanceIRHelper.setConnected(false);
        controlLines.stop();
        if(instanceIRHelper.usbIoManager != null) {
            instanceIRHelper.usbIoManager.setListener(null);
            instanceIRHelper.usbIoManager.stop();
        }
        instanceIRHelper.usbIoManager = null;
        try {
            instanceIRHelper.usbSerialPort.close();
        } catch (IOException ignored) {}
        instanceIRHelper.usbSerialPort = null;
    }

    private void refresh() {
        if(btn_setup_pressed == 1){
            if(instanceIRStoredHelper.getIR_array_OpenData() != null && instanceIRStoredHelper.getIR_array_CloseData() != null){
                instanceIRStoredHelper.setIR_isSet(true);
            }
            Re_setup_open();
        }
        if(btn_setup_pressed == 2){
            if(instanceIRStoredHelper.getIR_array_OpenData() != null && instanceIRStoredHelper.getIR_array_CloseData() != null){
                instanceIRStoredHelper.setIR_isSet(true);
            }
            Re_setup_close();
        }
    }

    private void Re_setup_open() {
        btn_setup_pressed = -1;
        Open_IR_Set_btn.setBackgroundColor(getResources().getColor(R.color.finished_setup,null));
        Open_IR_Set_btn.findViewById(R.id.Open_progress_bar).setVisibility(View.GONE);
        ImageFilterView delete_btn = root.findViewById(R.id.btn_delete_data_open);
        delete_btn.setVisibility(View.VISIBLE);
        ((TextView)Open_IR_Set_btn.findViewById(R.id.status_set_open_btn)).setText("Kiểm Nghiệm Mở");
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instanceIRStoredHelper.setIR_isSet(false);
                Open_IR_Set_btn.setBackgroundColor(getResources().getColor(R.color.blue,null));
                ((TextView)Open_IR_Set_btn.findViewById(R.id.status_set_open_btn)).setText("Cài Đặt Tần Số Mở Cổng");
                Open_IR_Set_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(instanceIRHelper.isConnected()){
                            if(btn_setup_pressed != -1 ){
                                Toast.makeText(getActivity(), "Other function receive is acting",Toast.LENGTH_SHORT).show();
                                return;
                            }

                            btn_setup_pressed = 1;
                            Open_IR_Set_btn.findViewById(R.id.Open_progress_bar).setVisibility(View.VISIBLE);
                            ((TextView)Open_IR_Set_btn.findViewById(R.id.status_set_open_btn)).setText("Hãy Nhấn Nút Mở!");

                            instanceIRHelper.Command = "receiveOpen";
                            instanceIRHelper.send(instanceIRHelper.Command);
                        }
                    }
                });
                v.setVisibility(View.GONE);
            }
        });
        Open_IR_Set_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instanceIRHelper.Command = "OpenBarrier";
                instanceIRHelper.send(instanceIRHelper.Command);
            }
        });
    }

    private void Re_setup_close() {
        btn_setup_pressed = -1;
        Close_IR_Set_btn.setBackgroundColor(getResources().getColor(R.color.finished_setup,null));
        Close_IR_Set_btn.findViewById(R.id.Close_progress_bar).setVisibility(View.GONE);
        ImageFilterView delete_btn = root.findViewById(R.id.btn_delete_data_close);
        delete_btn.setVisibility(View.VISIBLE);
        ((TextView)Close_IR_Set_btn.findViewById(R.id.status_set_close_btn)).setText("Kiểm Nghiệm Đóng");
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instanceIRStoredHelper.setIR_isSet(false);
                Close_IR_Set_btn.setBackgroundColor(getResources().getColor(R.color.blue,null));
                ((TextView)Close_IR_Set_btn.findViewById(R.id.status_set_close_btn)).setText("Cài Đặt Tần Số Đóng Cổng");
                Close_IR_Set_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(instanceIRHelper.isConnected()){
                            if(btn_setup_pressed != -1 ){
                                Toast.makeText(getActivity(), "Other function receive is acting",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            btn_setup_pressed = 2;
                            Close_IR_Set_btn.findViewById(R.id.Close_progress_bar).setVisibility(View.VISIBLE);
                            ((TextView)Close_IR_Set_btn.findViewById(R.id.status_set_close_btn)).setText("Hãy Nhấn Nút Đóng!");
                            instanceIRHelper.Command = "receiveClose";
                            instanceIRHelper.send(instanceIRHelper.Command);

                        }
                    }
                });
                v.setVisibility(View.GONE);
            }
        });
        Close_IR_Set_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instanceIRHelper.Command = "CloseBarrier";
                instanceIRHelper.send(instanceIRHelper.Command);
            }
        });
    }

    class ControlLines implements IRHelper.aControlLines {
        private static final int refreshInterval = 200; // msec

        private final Runnable runnable;
        private final ToggleButton rtsBtn, ctsBtn, dtrBtn, dsrBtn, cdBtn, riBtn;
        private final AppCompatButton btn_clear;

        ControlLines(View view) {
            runnable = this::run; // w/o explicit Runnable, a new lambda would be created on each postDelayed, which would not be found again by removeCallbacks

            rtsBtn = view.findViewById(R.id.controlLineRts);
            ctsBtn = view.findViewById(R.id.controlLineCts);
            dtrBtn = view.findViewById(R.id.controlLineDtr);
            dsrBtn = view.findViewById(R.id.controlLineDsr);
            cdBtn = view.findViewById(R.id.controlLineCd);
            riBtn = view.findViewById(R.id.controlLineRi);
            btn_clear = view.findViewById(R.id.clear);
            View receiveBtn = view.findViewById(R.id.receive_btn);
            receiveBtn.setVisibility(View.GONE);
            btn_clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    receiveText.setText("");
                }
            });
            rtsBtn.setOnClickListener(this::toggle);
            dtrBtn.setOnClickListener(this::toggle);
            rtsBtn.setChecked(true);
        }

        public void toggle(View v) {
            ToggleButton btn = (ToggleButton) v;
            if (!instanceIRHelper.isConnected()) {
                btn.setChecked(!btn.isChecked());
                Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
                return;
            }
            String ctrl = "";
            try {
                if (btn.equals(rtsBtn)) { ctrl = "RTS"; instanceIRHelper.usbSerialPort.setRTS(btn.isChecked()); }
                if (btn.equals(dtrBtn)) { ctrl = "DTR"; instanceIRHelper.usbSerialPort.setDTR(btn.isChecked()); }
            } catch (IOException e) {
                instanceIRHelper.status("set" + ctrl + "() failed: " + e.getMessage());
            }
        }

        public void run() {
            if (!instanceIRHelper.isConnected())
                return;
            try {
                EnumSet<UsbSerialPort.ControlLine> controlLines = instanceIRHelper.usbSerialPort.getControlLines();
                rtsBtn.setChecked(controlLines.contains(UsbSerialPort.ControlLine.RTS));
                ctsBtn.setChecked(controlLines.contains(UsbSerialPort.ControlLine.CTS));
                dtrBtn.setChecked(controlLines.contains(UsbSerialPort.ControlLine.DTR));
                dsrBtn.setChecked(controlLines.contains(UsbSerialPort.ControlLine.DSR));
                cdBtn.setChecked(controlLines.contains(UsbSerialPort.ControlLine.CD));
                riBtn.setChecked(controlLines.contains(UsbSerialPort.ControlLine.RI));
                mainLooper.postDelayed(runnable, refreshInterval);
            } catch (Exception e) {
                instanceIRHelper.status("getControlLines() failed: " + e.getMessage() + " -> stopped control line refresh");
            }
        }

        public void start() {
            if (!instanceIRHelper.isConnected())
                return;
            try {
                EnumSet<UsbSerialPort.ControlLine> controlLines = instanceIRHelper.usbSerialPort.getSupportedControlLines();
                if (!controlLines.contains(UsbSerialPort.ControlLine.RTS)) rtsBtn.setVisibility(View.INVISIBLE);
                if (!controlLines.contains(UsbSerialPort.ControlLine.CTS)) ctsBtn.setVisibility(View.INVISIBLE);
                if (!controlLines.contains(UsbSerialPort.ControlLine.DTR)) dtrBtn.setVisibility(View.INVISIBLE);
                if (!controlLines.contains(UsbSerialPort.ControlLine.DSR)) dsrBtn.setVisibility(View.INVISIBLE);
                if (!controlLines.contains(UsbSerialPort.ControlLine.CD))   cdBtn.setVisibility(View.INVISIBLE);
                if (!controlLines.contains(UsbSerialPort.ControlLine.RI))   riBtn.setVisibility(View.INVISIBLE);
                run();
            } catch (Exception e) {
                Toast.makeText(getActivity(), "getSupportedControlLines() failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                rtsBtn.setVisibility(View.INVISIBLE);
                ctsBtn.setVisibility(View.INVISIBLE);
                dtrBtn.setVisibility(View.INVISIBLE);
                dsrBtn.setVisibility(View.INVISIBLE);
                cdBtn.setVisibility(View.INVISIBLE);
                cdBtn.setVisibility(View.INVISIBLE);
                riBtn.setVisibility(View.INVISIBLE);
            }
        }

        public void stop() {
            mainLooper.removeCallbacks(runnable);
            rtsBtn.setChecked(false);
            ctsBtn.setChecked(false);
            dtrBtn.setChecked(false);
            dsrBtn.setChecked(false);
            cdBtn.setChecked(false);
            riBtn.setChecked(false);
        }

        @Override
        public void spnRespone(SpannableStringBuilder spn) {
//            Toast.makeText(getActivity(),spn+" " +instanceIRHelper.Command, Toast.LENGTH_SHORT).show();

            receiveText.append(spn);
        }

        @Override
        public void spnStatus(SpannableStringBuilder spn) {
            txt_status_device.setText(spn);
        }
    }

}