package com.yunianvh.libusbcamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.pedro.rtmp.utils.ConnectCheckerRtmp;
import com.pedro.rtplibrary.rtmp.RtmpUsbCamera;
import com.pedro.rtplibrary.util.RecordController;
import com.pedro.rtplibrary.view.LightOpenGlView;
import com.pedro.rtplibrary.view.OpenGlView;
import com.hzeronine.usb.DeviceFilter;
import com.hzeronine.usb.USBMonitor;
import com.hzeronine.usb.UVCCamera;
import com.hzeronine.utils.HandlerThreadHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class USBCameraHelper implements SurfaceHolder.Callback {
    public volatile static USBCameraHelper mUSBCameraInit;

    public static USBCameraHelper getInstance() {
        if (mUSBCameraInit == null) {
            synchronized (USBCameraHelper.class) {
                if (mUSBCameraInit == null) {
                    mUSBCameraInit = new USBCameraHelper();
                }
            }
        }
        return mUSBCameraInit;
    }

    private static final String TAG = "USBCameraHelper";
    private final Object mSync = new Object();
    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;
    private Surface mPreviewSurface;
    private Context mContext;

    /**
     * Event Handler
     */
    private Handler mWorkerHandler;
    private long mWorkerThreadID = -1;
    private RtmpUsbCamera rtmpUsbCamera;
    private int width = 1920, height = 1080, fps = 30, bitrate = 1024 * 1024 * 3, rotation = 0;
    private boolean isPreviewing = true;
    private boolean isTakePicture = false;
    private USBCameraCallBack usbCameraCallBack;

    public void setUSBCameraCallBack(USBCameraCallBack usbCameraCallBack) {
        this.usbCameraCallBack = usbCameraCallBack;
    }

    public interface USBCameraCallBack {
        void onOpenCamera(boolean isOpen);

        void onPreview(boolean isPreview);
    }

    public void init(final Context context, ConnectCheckerRtmp connectCheckerRtmp) {
        isPreviewing = false;
        mContext = context;
        rtmpUsbCamera = new RtmpUsbCamera(context, connectCheckerRtmp);
        init();
    }

    public void init(final Context context, final OpenGlView openGlView, ConnectCheckerRtmp connectCheckerRtmp) {
        isPreviewing = true;
        mContext = context;
        rtmpUsbCamera = new RtmpUsbCamera(openGlView, connectCheckerRtmp);
        openGlView.getHolder().addCallback(this);
        init();
    }

    public void init(final Context context, final LightOpenGlView lightOpenGlView, ConnectCheckerRtmp connectCheckerRtmp) {
        isPreviewing = true;
        mContext = context;
        rtmpUsbCamera = new RtmpUsbCamera(lightOpenGlView, connectCheckerRtmp);
        lightOpenGlView.getHolder().addCallback(this);
        init();
    }

    private void init() {
        if (mWorkerHandler == null) {
            mWorkerHandler = HandlerThreadHandler.createHandler(TAG);
            mWorkerThreadID = mWorkerHandler.getLooper().getThread().getId();
        }
        mUSBMonitor = new USBMonitor(mContext, mOnDeviceConnectListener);
    }

    public synchronized void start() {
        if (mUSBMonitor != null) {
            mUSBMonitor.register();
        }
        synchronized (mSync) {
            if (mUVCCamera != null) {
                mUVCCamera.startPreview();
            }
        }
    }

    public synchronized void stop() {
        synchronized (mSync) {
            stopPreview();

            if (mUSBMonitor != null) {
                mUSBMonitor.unregister();
            }
        }
    }

    public synchronized void destory() {
        synchronized (mSync) {
            releaseCamera();

            if (mUSBMonitor != null) {
                mUSBMonitor.destroy();
                mUSBMonitor = null;
            }
        }
    }

    public void startRecord(String path) {
        if (!checkCameraOpened()) return;
        try {
            if (isStreaming()) {
                rtmpUsbCamera.startRecord(path, null);
            } else if (rtmpUsbCamera.prepareVideo(width, height, fps, bitrate, rotation) && rtmpUsbCamera.prepareAudio()) {
                rtmpUsbCamera.startRecord(path, null);
            }

            //不带预览时需要重新绑定一下Surface
            if (!isPreviewing && !isStreaming()) {
                doLog("不带预览时需要重新绑定一下Surface");
                startPreview();
            }
        } catch (IOException e) {
            e.getMessage();
        }
    }

    public void startRecord(String path, RecordController.Listener listener) {
        if (!checkCameraOpened()) return;
        try {
            if (isStreaming()) {
                rtmpUsbCamera.startRecord(path, listener);
            } else if (rtmpUsbCamera.prepareVideo(width, height, fps, bitrate, rotation) && rtmpUsbCamera.prepareAudio()) {
                rtmpUsbCamera.startRecord(path, listener);
            }

            //不带预览时需要重新绑定一下Surface
            if (!isPreviewing && !isStreaming()) {
                doLog("不带预览时需要重新绑定一下Surface");
                startPreview();
            }
        } catch (IOException e) {
            listener.onStatusChange(RecordController.Status.STOPPED);
            e.getMessage();
        }
    }

    public void stopRecord() {
        if (!checkCameraOpened()) return;
        rtmpUsbCamera.stopRecord();

        //不带预览时需要主动关闭camera
        if (!isPreviewing && !isStreaming()) {
            stopPreview();
        }
    }

    public boolean isRecording() {
        return rtmpUsbCamera.isRecording();
    }

    public void startStream(String url) {
        if (!checkCameraOpened()) return;
        if (isRecording()) {
            rtmpUsbCamera.startStream(url);
        } else if (rtmpUsbCamera.prepareVideo(width, height, fps, bitrate, rotation) && rtmpUsbCamera.prepareAudio()) {
            rtmpUsbCamera.startStream(url);
        }

        //不带预览时需要重新绑定一下Surface
        if (!isPreviewing && !isRecording()) {
            startPreview();
        }
    }

    public void stopStream() {
        if (!checkCameraOpened()) return;
        rtmpUsbCamera.stopStream();

        //不带预览时需要主动关闭camera
        if (!isPreviewing && !isRecording()) {
            stopPreview();
        }
    }

    public boolean isStreaming() {
        return rtmpUsbCamera.isStreaming();
    }

    public interface PictureBack {
        void onPictureBitMap(Bitmap bmp);
    }

    public void takePicture(final PictureBack picCallBack) {
        if (!checkCameraOpened()) return;
        long delayMillis = 0;
        //不带预览时需要重新绑定一下Surface
        if (!isPreviewing && !isStreaming() && !isRecording()) {
            isTakePicture = true;
            startPreview();
            delayMillis = 1000 * 5;
        }

        mWorkerHandler.postDelayed(() -> {
            try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                mUVCCamera.setFrameCallback(frame -> {
                    final byte[] yuv = new byte[frame.capacity()];
                    frame.get(yuv);

                    YuvImage yuvImage = new YuvImage(yuv, ImageFormat.NV21, width, height, null);
                    yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, stream);

                    Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                    if (picCallBack != null) picCallBack.onPictureBitMap(bmp);
                    mUVCCamera.setFrameCallback(null, UVCCamera.PIXEL_FORMAT_YUV420SP);

                    //不带预览时需要主动关闭camera
                    if (!isPreviewing && !isStreaming() && !isRecording()) {
                        stopPreview();
                        isTakePicture = false;
                    }
                }, UVCCamera.PIXEL_FORMAT_YUV420SP);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }, delayMillis);
    }

    public List<UsbDevice> getDeviceList() {
        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(mContext, R.xml.device_filter);
        final List<UsbDevice> deviceList = mUSBMonitor.getDeviceList(filter);
        return deviceList;
    }

    public boolean checkCameraOpened() {
        if (mUVCCamera == null) {
            doLog(mContext.getResources().getString(R.string.msg_camera_open_fail));
            return false;
        }
        if (rtmpUsbCamera == null) {
            doLog("RtmpUsbCamera 未初始化");
            return false;
        }
        return true;
    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            doLog(mContext.getResources().getString(R.string.msg_usb_device_attached));
            requestPermission();
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            doLog("onConnect:" + device.getDeviceName() + " createNew:" + createNew);
            releaseCamera();
            queueEvent(() -> {
                final UVCCamera camera = new UVCCamera();
                try {
                    camera.open(ctrlBlock);
                    if (usbCameraCallBack != null) usbCameraCallBack.onOpenCamera(true);
                } catch (UnsupportedOperationException e) {
                    releaseCamera();
                    doLog("onConnect UnsupportedOperationException:" + e);
                    if (usbCameraCallBack != null) usbCameraCallBack.onOpenCamera(false);
                    return;
                } catch (Exception e) {
                    releaseCamera();
                    doLog("onConnect Exception:" + e);
                    if (usbCameraCallBack != null) usbCameraCallBack.onOpenCamera(false);
                    return;
                }
                setPreviewSize(camera, width, height);
                releaseSurface();
                initSurface();
                camera.setPreviewDisplay(mPreviewSurface);
                camera.startPreview();
                synchronized (mSync) {
                    mUVCCamera = camera;
                }
            }, 0);
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            releaseCamera();
        }

        @Override
        public void onDettach(final UsbDevice device) {
            stopPreview();
            doLog(mContext.getResources().getString(R.string.msg_usb_device_detached));
        }

        @Override
        public void onCancel(final UsbDevice device) {
        }
    };

    public synchronized void startPreview() {
        synchronized (mSync) {
            if (mUVCCamera != null) {
                releaseSurface();
                initSurface();
                mUVCCamera.setPreviewDisplay(mPreviewSurface);
                mUVCCamera.startPreview();
            }
        }
    }

    public synchronized void stopPreview() {
        synchronized (mSync) {
            if (mUVCCamera != null) {
                mUVCCamera.stopPreview();
            }
            rtmpUsbCamera.stopPreview(isTakePicture);
        }
    }

    public void setPreviewSize(int width, int height, int bitrate, int rotation) {
        if (!checkCameraOpened()) return;
        this.bitrate = bitrate;
        this.rotation = rotation;
        setPreviewSize(mUVCCamera, width, height);
    }

    public synchronized void setPreviewSize(UVCCamera camera, int width, int height) {
        if (camera == null) return;
        synchronized (mSync) {
            this.width = width;
            this.height = height;
            doLog("setPreviewSize width:" + width + " height:" + height);
            try {
                camera.setPreviewSize(width, height, UVCCamera.FRAME_FORMAT_MJPEG);
            } catch (final IllegalArgumentException e) {
                try {
                    camera.setPreviewSize(width, height, UVCCamera.DEFAULT_PREVIEW_MODE);
                } catch (final IllegalArgumentException e1) {
                    camera.destroy();
                    doLog("setPreviewSize IllegalArgumentException:" + e1);
                    if (usbCameraCallBack != null) usbCameraCallBack.onPreview(false);
                    return;
                }
            }
            if (usbCameraCallBack != null) usbCameraCallBack.onPreview(true);
        }
    }

    private void initSurface() {
        if (rtmpUsbCamera == null) return;
        if (rtmpUsbCamera.isOnPreview()) rtmpUsbCamera.stopStream();
        rtmpUsbCamera.startPreview(width, height, isTakePicture);
        final SurfaceTexture st = rtmpUsbCamera.getGlInterface().getSurfaceTexture();
        if (st != null) mPreviewSurface = new Surface(st);
        if (mPreviewSurface == null) doLog("未初始化预览控件  Surface:" + (st != null));
    }

    private void releaseSurface() {
        if (mPreviewSurface == null) return;
        mPreviewSurface.release();
        mPreviewSurface = null;
    }

    /**
     * Request USB device permission.
     */
    private void requestPermission() {
        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(mContext, R.xml.device_filter);
        final List<UsbDevice> deviceList = mUSBMonitor.getDeviceList(filter);

        if (deviceList == null || deviceList.size() == 0) {
            return;
        }

        if (mUSBMonitor != null) {
            mUSBMonitor.requestPermission(deviceList.get(0));
        }
    }

    /**
     * Release the USB camera device.
     */
    private synchronized void releaseCamera() {
        synchronized (mSync) {
            if (mUVCCamera != null) {
                try {
                    mUVCCamera.setStatusCallback(null);
                    mUVCCamera.setButtonCallback(null);
                    mUVCCamera.close();
                    mUVCCamera.destroy();
                } catch (final Exception e) {
                    doLog("releaseCamera:" + e);
                }
                mUVCCamera = null;
            }
            releaseSurface();
        }
    }

    /**
     * Run runnable specified on worker thread
     * the same runnable that is unexecuted is cancelled (executed only later)
     *
     * @param task
     * @param delayMillis
     */
    protected final synchronized void queueEvent(final Runnable task, final long delayMillis) {
        if ((task == null) || (mWorkerHandler == null)) return;
        try {
            mWorkerHandler.removeCallbacks(task);
            if (delayMillis > 0) {
                mWorkerHandler.postDelayed(task, delayMillis);
            } else if (mWorkerThreadID == Thread.currentThread().getId()) {
                task.run();
            } else {
                mWorkerHandler.post(task);
            }
        } catch (final Exception e) {
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        doLog("surfaceChanged:" + i1 + " " + i2);
        rtmpUsbCamera.startPreview(width, height, isTakePicture);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        doLog("surfaceDestroyed");
        rtmpUsbCamera.stopPreview(isTakePicture);
//        stopPreview();
    }

    private void doLog(String msg) {
        Log.e(TAG, msg);
    }
}
