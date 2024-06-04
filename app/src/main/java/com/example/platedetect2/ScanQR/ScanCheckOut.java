package com.example.platedetect2.ScanQR;

import static com.example.platedetect2.FcmNotificationSender.Post_Calling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.platedetect2.Dialog.ProgressHelper;
import com.example.platedetect2.R;
import com.example.platedetect2.UVC_Camera_two;
import com.example.platedetect2.utils.IRBytesStored;
import com.example.platedetect2.utils.IRHelper;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ScanCheckOut extends AppCompatActivity implements SerialInputOutputManager.Listener {
    private EditText qrCodeTxt;
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
    IRBytesStored instance = IRBytesStored.getInstance();
    private final Handler mainLooper;
    private TextView receiveText;
    private IRHelper.aControlLines controlLines;

    IRHelper instanceIRHelper;

    public ScanCheckOut() {
        mainLooper = new Handler(Looper.getMainLooper());
    }
    View receiveBtn;
    View sendBtn;
    TextView sendText;
    View BottomSheetView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanqr);


        instanceIRHelper = IRHelper.getNewInstance(getApplicationContext(), this);
        BottomSheetView = findViewById(R.id.bottom_sheet_layout);
        controlLines = new ControlLines(BottomSheetView);
        receiveText = BottomSheetView.findViewById(R.id.receive_text);
        receiveBtn = BottomSheetView.findViewById(R.id.receive_btn);
        sendBtn = BottomSheetView.findViewById(R.id.send_btn);
        sendText = BottomSheetView.findViewById(R.id.send_text);
        instanceIRHelper.setControlLines(controlLines);

        qrCodeTxt = findViewById(R.id.qrCideTxt);
        previewView = findViewById(R.id.previewView);
        ((TextView)findViewById(R.id.Location)).setText("Cơ sở hiện tại: " + IRBytesStored.getInstance().getEmailLocation().getLocationName());

        // checking for camera permissions
        if (ContextCompat.checkSelfPermission(ScanCheckOut.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            init();
        }
        else{
            ActivityCompat.requestPermissions(ScanCheckOut.this, new String[]{Manifest.permission.CAMERA}, 101);
        }


    }


    private void init() {
        cameraProviderListenableFuture = ProcessCameraProvider.getInstance(ScanCheckOut.this);
        cameraProviderListenableFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                    bindImageAnalysis(cameraProvider);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, ContextCompat.getMainExecutor (ScanCheckOut.this));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            init();
        }
        else{
            Toast.makeText(ScanCheckOut.this,"Permissions Denied", Toast.LENGTH_SHORT).show();
        }
    }
    boolean flag = false;
    private void bindImageAnalysis(ProcessCameraProvider processCameraProvider) {
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy (ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        imageAnalysis.setAnalyzer (ContextCompat.getMainExecutor (ScanCheckOut.this), new ImageAnalysis. Analyzer () {
            @Override
            public void analyze (@NonNull ImageProxy image) {
                Image mediaImage = image.getImage();
                if (mediaImage !=null) {
                    InputImage image2 = InputImage.fromMediaImage (mediaImage, image.getImageInfo().getRotationDegrees ());
                    BarcodeScanner scanner = BarcodeScanning.getClient();
                    Task<List<Barcode>> results = scanner.process(image2);
                    results.addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
//                            Toast.makeText(getApplicationContext(), flag + "", Toast.LENGTH_SHORT).show();;
                            for (Barcode barcode : barcodes) {
                                final String getValue = barcode.getRawValue();
                                //qrCodeTxt.setText(getValue);
                                keyAuthen(getValue);
                            }
                            image.close();
                            mediaImage.close();


                        }
                    });
                }
            }
        });
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing (CameraSelector.LENS_FACING_FRONT).build();
        preview.setSurfaceProvider (previewView.getSurfaceProvider());
        processCameraProvider.bindToLifecycle (this, cameraSelector, imageAnalysis, preview);
    }
    long timeLimited;
    long currentTimeMillis;
    MD5Encoder md5Encoder = new MD5Encoder();

    String uid_QR;
    long millis_QR;
    String md5Enc_QR;
    String key = "4:1715328016773:2ec3799b7582e481c022e224c1825879";
    String token_user;
    private void keyAuthen(String keyQR) {
        if(flag == true)
            return;
        String[] parts = splitString(keyQR);

        uid_QR = parts[0];
        millis_QR = Long.parseLong(parts[1]);
        md5Enc_QR = parts[2];

        System.out.println("UID: " + uid_QR);
        System.out.println("Millis: " + millis_QR);
        System.out.println("MD5Enc: " + md5Enc_QR);


        currentTimeMillis = System.currentTimeMillis();
        timeLimited = currentTimeMillis - millis_QR;
        if(timeLimited < 65000) {
            Log.d("Test", "Mã QR còn hạn sử dụng");
            flag = true;
            callApiGetToken(uid_QR);
        } else {
            Log.d("Test", "Mã QR đã hết hạn sử dụng");
            qrCodeTxt.setText("Mã QR đã hết hạn sử dụng");
        }
    }

    private void extracted(long millis_QR, String md5Enc_QR, MD5Encoder md5Encoder) {
        String md5Enc_check = md5Encoder.encodeToMD5(token_user + millis_QR);
        Log.d("Test", "md5Enc_check: " + token_user + " : " + millis_QR);

        if(md5Enc_QR.equals(md5Enc_check)) {
            Log.d("Test", md5Enc_QR + "\n" + md5Enc_check + "\n" + "Xác nhận trùng mã khóa");
            qrCodeTxt.setText("Xác nhận trùng mã khóa");

        } else {
            Log.d("Test", md5Enc_QR + "\n" + md5Enc_check);
            Log.d("Test", "Xác nhận khóa thất bại");
            qrCodeTxt.setText("Xác nhận khóa thất bại");
        }
    }

    public static String[] splitString(String input) {
        return input.split(":");
    }
    boolean flag_close = false;
    private void postCheckOut() {
        // Khởi tạo Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://c2se-14-sts-api.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Tạo một instance của ApiService từ Retrofit
        ApiService apiService = retrofit.create(ApiService.class);

        //Truyền thông số vào đây
        String licensePlate = IRBytesStored.getInstance().getLiscensePlate();
        String user_id = uid_QR;
        int locationId = IRBytesStored.getInstance().getEmailLocation().getLocationId();

        CheckOut requestBody = new CheckOut(licensePlate, user_id, locationId);
        Call<CheckOut> call = apiService.postCheckOut(requestBody);

        call.enqueue(new Callback<CheckOut>() {
            @Override
            public void onResponse(Call<CheckOut> call, Response<CheckOut> response) {
                mainLooper.post(new Runnable() {
                    @Override
                    public void run() {
                        if(flag_close == true) {
                            return;
                        }
                        flag_close = true;
                        int code = response.code();
                        if(code >= 200 && code < 300) {
                            if(!ProgressHelper.isDialogVisible()){
                                ProgressHelper.showSuccessDialog(ScanCheckOut.this,"Thành Công");
                                instanceIRHelper.Command = "OpenBarrier";
                                instanceIRHelper.send(instanceIRHelper.Command);
                                mainLooper.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(ProgressHelper.isDialogVisible()){
                                            ProgressHelper.dismissDialog();
                                            instanceIRHelper.Command = "CloseBarrier";
                                            instanceIRHelper.send(instanceIRHelper.Command);

                                            sendNotification();

                                            Intent intent = new Intent(getApplicationContext(), UVC_Camera_two.class);
                                            startActivity(intent);
                                            finish();

                                        }
                                    }
                                }, 5000);
                            }
                        } else if (code >= 400 && code < 500) {
                            Toast.makeText(ScanCheckOut.this, "Xe này đã check out trên server", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), UVC_Camera_two.class);
                            startActivity(intent);
                            finish();

                        } else if (code >= 500) {
                            Toast.makeText(ScanCheckOut.this, "Không thể kết nối trên server", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), UVC_Camera_two.class);
                            startActivity(intent);
                            finish();

                        }


                    }
                });
            }

            @Override
            public void onFailure(Call<CheckOut> call, Throwable throwable) {

            }
        });

    }
    private void sendNotification(){
        String fcmServerKey = "AAAA-aEDMr4:APA91bFkulQb-yKqZHCdfMMvTnAWHu6eHSaFsPTkTiM4CN4nux4zGjFOpEnk_NXESGI3i98JmZX0AJj7tqyFsxmhhOU5AP4v0fHmxVNNA6olETuUvwhpCg6ip_0NT3kXa-eWUFeC0rP_";
        String receiverToken = "clFBYzo6TZOpCIgXAEadZF:APA91bHzfK1gnfrKwUlFT5nhuIY8RO94EEB1juOnyFyWJdY1vcaOMcgAPrg8H8qrRbxjexyB6W4YKqL1DjMqL-mA-2sjzL0aY3Xfjiv4iPEe0F838JcB8N_GTpGb-GWTefWoExNUeO4J";
        String notificationTitle = "Thông báo";
        String notificationBody = "Đã thanh toán tiền -2.000đ";

        // Gửi thông báo
        Post_Calling(fcmServerKey, receiverToken,notificationBody, notificationTitle);
    }
    private void callApiGetToken(String userId) {
        // Khởi tạo Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://c2se-14-sts-api.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Tạo một instance của ApiService từ Retrofit
        ApiService apiService = retrofit.create(ApiService.class);

        // Gọi API getUserToken và enqueue để thực hiện gọi bất đồng bộ
        apiService.getUserToken(userId).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                if (response.isSuccessful()) {
                    // Xử lý kết quả thành công
                    TokenResponse tokenResponse = response.body();
                    if (tokenResponse != null) {
                        String token = tokenResponse.getToken();
                        token_user = token;
                        extracted(millis_QR, md5Enc_QR, md5Encoder);
                        if(flag == true){
                            flag = false;
                            postCheckOut();
                        }
                    }
                } else {
                    // Xử lý khi không nhận được kết quả thành công
                    Toast.makeText(ScanCheckOut.this, "Lỗi khi gọi API", Toast.LENGTH_SHORT).show();
                }



            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                // Xử lý khi gặp lỗi trong quá trình gọi API
                Toast.makeText(ScanCheckOut.this, "Lỗi khi gọi API", Toast.LENGTH_SHORT).show();
            }
        });
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
            btn_clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    receiveText.setText("");
                }
            });
            rtsBtn.setOnClickListener(this::toggle);
            dtrBtn.setOnClickListener(this::toggle);
        }

        public void toggle(View v) {
            ToggleButton btn = (ToggleButton) v;
            if (!instanceIRHelper.isConnected()) {
                btn.setChecked(!btn.isChecked());
                Toast.makeText(getApplicationContext(), "not connected", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "getSupportedControlLines() failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            receiveText.append(spn);
        }

        @Override
        public void spnStatus(SpannableStringBuilder spn) {

        }
    }
}