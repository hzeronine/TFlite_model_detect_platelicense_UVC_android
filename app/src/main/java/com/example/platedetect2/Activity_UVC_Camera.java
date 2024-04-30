package com.example.platedetect2;


import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;

import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import androidx.appcompat.app.AppCompatActivity;



//import com.jiangdg.usbcamera.UVCCameraHelper;
//import com.serenegiant.usb.common.AbstractUVCCameraHandler;
//import com.serenegiant.usb.widget.CameraViewInterface;
//import com.serenegiant.usb.widget.UVCCameraTextureView;


public class Activity_UVC_Camera extends AppCompatActivity {

//    private final Object mSync = new Object();
//    UVCCameraHelper mCameraHelper;
//    private UVCCameraTextureView mUVCCameraView;
//    private boolean isPreview = false;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_uvc_camera);
//        mCameraHelper = UVCCameraHelper.getInstance(UVCCameraHelper.FRAME_FORMAT_MJPEG);
//
//        mUVCCameraView = (UVCCameraTextureView) findViewById(R.id.textureView);
//        mUVCCameraView.setCallback(cameraCallback);
//        Log.d("asdasd",mUVCCameraView.getSurfaceTexture()+"");
//
//
//        mCameraHelper.setOnPreviewFrameListener(new AbstractUVCCameraHandler.OnPreViewResultListener() {
//            @Override
//            public void onPreviewResult(byte[] data) {
//
//            }
//        });
//
//        mCameraHelper.initUSBMonitor(this, mUVCCameraView,listener);
//    }
//    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {
//
//        @Override
//        public void onAttachDev(UsbDevice usbDevice) {
//
//        }
//
//        @Override
//        public void onDettachDev(UsbDevice usbDevice) {
//
//        }
//
//        @Override
//        public void onConnectDev(UsbDevice usbDevice, boolean b) {
//            if(!b ){
//                if (!isPreview && mCameraHelper.isCameraOpened()) {
//                    mCameraHelper.startPreview(mUVCCameraView);
//                    isPreview = true;
//                }
//            }else{
//                mCameraHelper.stopPreview();
//                isPreview = false;
//            }
//        }
//
//        @Override
//        public void onDisConnectDev(UsbDevice usbDevice) {
//
//        }
//    };
//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (mCameraHelper != null) {
//            mCameraHelper.registerUSB();
//        }
//    }
//    @Override
//    protected void onDestroy(){
//        super.onDestroy();
//        if (mCameraHelper != null) {
//            mCameraHelper.release();
//        }
//    }
//    @Override
//    protected void onStop(){
//        super.onStop();
//        if (mCameraHelper != null) {
//            mCameraHelper.unregisterUSB();
//        }
//    }
//    private CameraViewInterface.Callback cameraCallback = new CameraViewInterface.Callback() {
//        @Override
//        public void onSurfaceCreated(CameraViewInterface cameraViewInterface, Surface surface) {
//            if (!isPreview && mCameraHelper.isCameraOpened()) {
//                mCameraHelper.startPreview(mUVCCameraView);
//                isPreview = true;
//            }
//        }
//
//        @Override
//        public void onSurfaceChanged(CameraViewInterface cameraViewInterface, Surface surface, int i, int i1) {
//        }
//
//        @Override
//        public void onSurfaceDestroy(CameraViewInterface cameraViewInterface, Surface surface) {
//            if (isPreview && mCameraHelper.isCameraOpened()) {
//                mCameraHelper.stopPreview();
//                isPreview = false;
//            }
//        }
//    };
}
