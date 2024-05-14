package com.example.platedetect2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.utils.widget.ImageFilterView;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;

import com.example.platedetect2.utils.NV21ToBitmap;
import com.example.platedetect2.utils.ObjectDetectorHelper;
import com.example.platedetect2.Dialog.ProgressHelper;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.herohan.uvcapp.CameraHelper;
import com.herohan.uvcapp.ICameraHelper;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.widget.AspectRatioSurfaceView;
import android.Manifest;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.TextView;

import org.tensorflow.lite.task.vision.detector.Detection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UVC_Camera_two extends AppCompatActivity implements ObjectDetectorHelper.DetectorListener{
    private final String TAG = UVC_Camera_two.class.getSimpleName();
    private ExecutorService singleThreadExecutor;
    Size size;
    ICameraHelper mCameraHelper;
    AspectRatioSurfaceView mCameraView;
    ObjectDetectorHelper detectorHelper;
    ImageFilterView imageCropView;
    TextView textvip;
    TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uvc_camera_two);
        mCameraView = findViewById(R.id.CameraView);
        imageCropView = findViewById(R.id.image_crop_result);
        textvip = findViewById(R.id.textViewvip);
        List<String> needPermissions = new ArrayList<>();
        needPermissions.add(Manifest.permission.CAMERA);
        XXPermissions.with(this)
                .permission(needPermissions)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if(!all)
                            return;
                    }
                });

        singleThreadExecutor = new ThreadPoolExecutor(
                1, 1, 1, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2), new ThreadPoolExecutor.DiscardPolicy());

        detectorHelper = new ObjectDetectorHelper(0.7f, 2,
                4, 0, 6,
                getApplicationContext(), this);
        detectorHelper.setupObjectDetector();

        findViewById(R.id.floatingButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callCallBack();
            }
        });

        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                if(mCameraHelper != null){
                    mCameraHelper.addSurface(holder.getSurface(), false);
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                if(mCameraHelper != null){
                    mCameraHelper.removeSurface(holder.getSurface());
                }
            }
        });
        size = new Size(
                (int)0x07,
                mCameraView.getWidth(),
                mCameraView.getHeight(),
                30,
                new ArrayList<>(30));
        mCameraHelper = new CameraHelper();
        mCameraHelper.setStateCallback(mStateListener);
        mNv21ToBitmap = new NV21ToBitmap(this);
    }
    NV21ToBitmap mNv21ToBitmap;
    private IFrameCallback frameCallback = new IFrameCallback() {
        @Override
        public void onFrame(ByteBuffer frame) {
//            Toast.makeText(getApplicationContext(),"denday",Toast.LENGTH_LONG).show();
//            imageCropView.setImageBitmap(bitmap);
            singleThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(frame);
                    detectorHelper.detect(bitmap, 0);
                }
            });
        }
    };
    private ICameraHelper.StateCallback mStateListener = new ICameraHelper.StateCallback() {
        @Override
        public void onAttach(UsbDevice device) {
            mCameraHelper.selectDevice(device);
        }

        @Override
        public void onDeviceOpen(UsbDevice device, boolean isFirstOpen) {
            Log.d("deviceID",device.getVendorId()+"");
            mCameraHelper.openCamera();

        }

        @Override
        public void onCameraOpen(UsbDevice device) {
            mCameraHelper.startPreview();
                    Size size = mCameraHelper.getPreviewSize();
            if(size != null){
                        int width = size.width;
                        int height = size.height;
                        mCameraView.setAspectRatio(width,height);
                    }
            mCameraHelper.addSurface(mCameraView.getHolder().getSurface(), false);
            if(mCameraHelper.isCameraOpened()) {
                mCameraHelper.setFrameCallback(new IFrameCallback() {
                    @Override
                    public void onFrame(ByteBuffer frame) {

                        byte[] nv21 = new byte[frame.remaining()];
                        frame.get(nv21, 0, nv21.length);

                        Bitmap bitmap = mNv21ToBitmap.nv21ToBitmap(nv21, size.width, size.height);
                        singleThreadExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                detectorHelper.detect(bitmap, 0);
                            }
                        });
                    }
                },UVCCamera.PIXEL_FORMAT_NV21);

            }
        }
        @Override
        public void onCameraClose(UsbDevice device) {
            if(mCameraHelper != null){
                mCameraHelper.removeSurface(mCameraView.getHolder().getSurface());
            }
        }

        @Override
        public void onDeviceClose(UsbDevice device) {

        }

        @Override
        public void onDetach(UsbDevice device) {

        }

        @Override
        public void onCancel(UsbDevice device) {

        }
    };
    private void callCallBack() {
    }
    /**
     * this is API function after detected
     */
    @Override
    public void onError(@NonNull String error) {

    }
    boolean founded = false;
    String resultText = "";
    @Override
    public void onResults(@Nullable List<Detection> results, @NonNull Bitmap image,
                          long inferenceTime, int imageHeight, int imageWidth) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (results != null && results.size() > 0) {
                    List<Detection> modelResults = results;
                    float scaleFactor = Math.max(image.getWidth() * 1f / imageWidth, image.getHeight() * 1f / imageHeight);
                    Detection detection = modelResults.get(0);
                    RectF boundingBox = detection.getBoundingBox();
                    int left = (int) (boundingBox.left * scaleFactor);
                    int top = (int) (boundingBox.top * scaleFactor);
                    int right = (int) (boundingBox.right * scaleFactor);
                    int bottom = (int) (boundingBox.bottom * scaleFactor);
                    int adjustedLeft = left < 0 ? 0 : left;
                    int adjustedWidth = right - adjustedLeft;
                    Log.d("shape", image.getHeight() + ":" + image.getWidth());
                    if (image.getHeight() < (top - bottom - top))
                        return;
                    Bitmap croppedBitmap = Bitmap.createBitmap(image, adjustedLeft, top, adjustedWidth, bottom - top);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageCropView.setImageBitmap(croppedBitmap);
                        }
                    });
                    Task<Text> text = recognizer.process(InputImage.fromBitmap(croppedBitmap, 0))
                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {
                            String recognition = text.getText();

                            if(recognition == null || recognition == "" || recognition.length() < 8)
                                return;
                            recognition = recognition.trim().replace(".","").replace(" ", "");
                            Log.d("foundResult",  recognition + ":\n" + resultText + ":\n" + founded);
                            if(recognition.equals(resultText) && resultText != ""){
                                founded = true;
                            }else if((recognition.equals(resultText) == false || resultText == "") && founded != true){
                                resultText = recognition;
                                founded = false;
                                if(ProgressHelper.isDialogVisible()){
                                    textvip.setText("");
                                }
                            }
                        }
                    });
                }else{
                    founded = false;
                }

                if (founded == true){
                    Log.d("foundResult", resultText.toString());
                    if(!ProgressHelper.isDialogVisible())
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textvip.setText(resultText);
                            }
                        });

                    }
                else if (ProgressHelper.isDialogVisible()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textvip.setText(resultText);
                        }
                    });
                }
            }
        }).start();

    }
}