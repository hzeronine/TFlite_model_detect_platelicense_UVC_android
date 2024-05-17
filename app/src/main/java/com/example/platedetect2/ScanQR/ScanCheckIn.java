package com.example.platedetect2.ScanQR;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.platedetect2.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Callback;


public class ScanCheckIn extends AppCompatActivity {
    private EditText qrCodeTxt;
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanqr);

        qrCodeTxt = findViewById(R.id.qrCideTxt);
        previewView = findViewById(R.id.previewView);
        // checking for camera permissions
        if (ContextCompat.checkSelfPermission(ScanCheckIn.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            init();
        }
        else{
            ActivityCompat.requestPermissions(ScanCheckIn.this, new String[]{Manifest.permission.CAMERA}, 101);
        }




        Intent intent = getIntent();
        if (intent != null) {
            String userEmail = intent.getStringExtra("user_email");
            int locationId = intent.getIntExtra("location_id", -1);
            String locationName = intent.getStringExtra("location_name");

            // Sử dụng dữ liệu nhận được
            Log.d("MainActivity", "User Email: " + userEmail);
            Log.d("MainActivity", "Location ID: " + locationId);
            Log.d("MainActivity", "Location Name: " + locationName);
        }
    }

    private void init() {
        cameraProviderListenableFuture = ProcessCameraProvider.getInstance(ScanCheckIn.this);
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
        }, ContextCompat.getMainExecutor (ScanCheckIn.this));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            init();
        }
        else{
            Toast.makeText(ScanCheckIn.this,"Permissions Denied", Toast.LENGTH_SHORT).show();
        }
    }
    boolean flag = false;
    private void bindImageAnalysis(ProcessCameraProvider processCameraProvider) {
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy (ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        imageAnalysis.setAnalyzer (ContextCompat.getMainExecutor (ScanCheckIn.this), new ImageAnalysis. Analyzer () {
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
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing (CameraSelector.LENS_FACING_BACK).build();
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
            postCheckIn();
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

    private void postCheckIn() {
        // Khởi tạo Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://c2se-14-sts-api.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Tạo một instance của ApiService từ Retrofit
        ApiService apiService = retrofit.create(ApiService.class);
        String licensePlate = "AAAA34";
        int user_id = 14;

        CheckIn requestBody = new CheckIn(licensePlate, user_id);
        Call<CheckIn> call = apiService.postCheckIn(requestBody);

        call.enqueue(new Callback<CheckIn>() {
            @Override
            public void onResponse(Call<CheckIn> call, Response<CheckIn> response) {
                Toast.makeText(ScanCheckIn.this, "Thafnh cong", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<CheckIn> call, Throwable throwable) {

            }
        });

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
                        flag = false;
                        Toast.makeText(ScanCheckIn.this, "Token: " + token, Toast.LENGTH_SHORT).show();

                    }
                } else {
                    // Xử lý khi không nhận được kết quả thành công
                    Toast.makeText(ScanCheckIn.this, "Lỗi khi gọi API", Toast.LENGTH_SHORT).show();
                }



            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                // Xử lý khi gặp lỗi trong quá trình gọi API
                Toast.makeText(ScanCheckIn.this, "Lỗi khi gọi API", Toast.LENGTH_SHORT).show();
            }
        });
    }

}