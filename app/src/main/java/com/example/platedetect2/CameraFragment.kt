package com.example.platedetect2


import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.platedetect2.databinding.FragmentCameraBinding
import com.example.platedetect2.utils.MyCameraFilter
import com.example.platedetect2.utils.ObjectDetectorHelper
import com.example.platedetect2.Dialog.ProgressHelper
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.serenegiant.usb.USBMonitor
import org.tensorflow.lite.task.vision.detector.Detection
import java.util.LinkedList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.math.max

class CameraFragment : Fragment(), ObjectDetectorHelper.DetectorListener {

    private val TAG = "ObjectDetection"

    private var _fragmentCameraBinding: FragmentCameraBinding? = null

    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    private lateinit var objectDetectorHelper: ObjectDetectorHelper
    private lateinit var bitmapBuffer: Bitmap
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var founded: Boolean? = false;
    private var resultText: String? = "";
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    var cameraFilterS : MyCameraFilter? = null
    var myUSBMonitor: USBMonitor? = null

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.fragment)
                .navigate(CameraFragmentDirections.actionCameraToPermissions())
        }
    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        // Shut down our background executor
        cameraExecutor.shutdown()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)

        return fragmentCameraBinding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        objectDetectorHelper = ObjectDetectorHelper(
            context = requireContext(),
            objectDetectorListener = this)
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Wait for the views to be properly laid out
        fragmentCameraBinding.viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera()
        }

        // Attach listeners to UI control widgets
        initBottomSheetControls()
    }

    private fun initBottomSheetControls() {
        // When clicked, lower detection score threshold floor
        fragmentCameraBinding.bottomSheetLayout.thresholdMinus.setOnClickListener {
            if (objectDetectorHelper.threshold >= 0.1) {
                objectDetectorHelper.threshold -= 0.1f
                updateControlsUi()
            }
        }

        // When clicked, raise detection score threshold floor
        fragmentCameraBinding.bottomSheetLayout.thresholdPlus.setOnClickListener {
            if (objectDetectorHelper.threshold <= 0.8) {
                objectDetectorHelper.threshold += 0.1f
                updateControlsUi()
            }
        }

        // When clicked, reduce the number of objects that can be detected at a time
        fragmentCameraBinding.bottomSheetLayout.maxResultsMinus.setOnClickListener {
            if (objectDetectorHelper.maxResults > 1) {
                objectDetectorHelper.maxResults--
                updateControlsUi()
            }
        }

        // When clicked, increase the number of objects that can be detected at a time
        fragmentCameraBinding.bottomSheetLayout.maxResultsPlus.setOnClickListener {
            if (objectDetectorHelper.maxResults < 5) {
                objectDetectorHelper.maxResults++
                updateControlsUi()
            }
        }

        // When clicked, decrease the number of threads used for detection
        fragmentCameraBinding.bottomSheetLayout.threadsMinus.setOnClickListener {
            if (objectDetectorHelper.numThreads > 1) {
                objectDetectorHelper.numThreads--
                updateControlsUi()
            }
        }

        // When clicked, increase the number of threads used for detection
        fragmentCameraBinding.bottomSheetLayout.threadsPlus.setOnClickListener {
            if (objectDetectorHelper.numThreads < 4) {
                objectDetectorHelper.numThreads++
                updateControlsUi()
            }
        }


    }

    // Update the values displayed in the bottom sheet. Reset detector.
    private fun updateControlsUi() {
        fragmentCameraBinding.bottomSheetLayout.maxResultsValue.text =
            objectDetectorHelper.maxResults.toString()
        fragmentCameraBinding.bottomSheetLayout.thresholdValue.text =
            String.format("%.2f", objectDetectorHelper.threshold)
        fragmentCameraBinding.bottomSheetLayout.threadsValue.text =
            objectDetectorHelper.numThreads.toString()

        objectDetectorHelper.clearObjectDetector()
        fragmentCameraBinding.overlay.clear()
    }

    override fun onStart() {
        super.onStart()
    }

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                bindCameraUseCases()
            },
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

//        val cameraSelector =
//            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_EXTERNAL).build()
//        cameraFilterS = MyCameraFilter("0")
//        val cameraSelector = CameraSelector.Builder().addCameraFilter(cameraFilterS!!).build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_EXTERNAL)
            .build();

        preview =
            Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .build()

        imageAnalyzer =
            ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(cameraExecutor, fun(image: ImageProxy) {
                        if (!::bitmapBuffer.isInitialized) {
                            // The image rotation and RGB image buffer are initialized only once
                            // the analyzer has started running
                            bitmapBuffer = Bitmap.createBitmap(
                                image.width,
                                image.height,
                                Bitmap.Config.ARGB_8888
                            )
                        }

                        detectObjects(image)
                    })
                }


        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector!!, preview, imageAnalyzer)


            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun detectObjects(image: ImageProxy) {
        // Copy out RGB bits to the shared bitmap buffer
        image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }

        val imageRotation = image.imageInfo.rotationDegrees

        objectDetectorHelper.detect(bitmapBuffer, imageRotation)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation = fragmentCameraBinding.viewFinder.display.rotation
    }



    override fun onResults(
        results: MutableList<Detection>?,
        image: Bitmap,
        inferenceTime: Long,
        imageHeight: Int,
        imageWidth: Int
    ) {
        thread {
            if (results?.size!! > 0){
                var model_results : List<Detection> = results ?: LinkedList<Detection>();
                var scaleFactor = max(image.width * 1f / imageWidth, image.height * 1f / imageHeight)
                val boundingBox = model_results[0].boundingBox
                val left = (boundingBox.left * scaleFactor).toInt()
                val top = (boundingBox.top * scaleFactor).toInt()
                val right = (boundingBox.right * scaleFactor).toInt()
                val bottom = (boundingBox.bottom * scaleFactor).toInt()
                val adjustedLeft = if (left < 0) 0 else left
                val adjustedWidth = right - adjustedLeft
                Log.d("shape","${image.height}:${image.width}")
                if(image.height < (top - bottom - top))
                    return@thread
                val croppedBitmap = Bitmap.createBitmap(image, adjustedLeft, top, adjustedWidth, bottom - top)
                activity?.runOnUiThread{
                    fragmentCameraBinding.bottomSheetLayout.imageCropResult.setImageBitmap(croppedBitmap)
                }
                val result = recognizer.process(InputImage.fromBitmap(croppedBitmap,0))
                    .addOnSuccessListener { visionText ->
                        var Text = visionText.text
//                        Toast.makeText(context,Text,Toast.LENGTH_LONG).show();
                        if(Text == null || Text == "" || Text.length < 8)
                            return@addOnSuccessListener
                        Text = Text.trim().replace(".","").replace(" ","")
                        Log.d("foundResult",  "${Text}:${resultText}:${founded}")
                        if(Text == resultText && resultText != ""){
                            founded = true
                        }else if((Text != resultText || resultText == "") && founded != true){
                            resultText = Text
                            founded = false
                            if(ProgressHelper.isDialogVisible()){
                                ProgressHelper.hidesDialog();
                            }
                        }

                    }
                    .addOnFailureListener { e ->
                        // Task failed with an exception
                        // ...
                    }
            }else{
                founded = false
            }

            if (founded == true){
                Log.d("foundResult", resultText.toString())
                if(!ProgressHelper.isDialogVisible())
                    activity?.runOnUiThread{
                        ProgressHelper.showDialog(context,resultText)
                    }
                else if (ProgressHelper.isDialogGone()){
                    activity?.runOnUiThread{
                        ProgressHelper.UpdateImage(resultText)
                        ProgressHelper.ShowDialog();
                    }
                }
            }
        }
        activity?.runOnUiThread {
            fragmentCameraBinding.bottomSheetLayout.inferenceTimeVal.text =
                String.format("%d ms", inferenceTime)

            // Pass necessary information to OverlayView for drawing on the canvas


            fragmentCameraBinding.overlay.setResults(
                results ?: LinkedList<Detection>(),
                imageHeight,
                imageWidth
            )
            // Force a redraw

        }
        fragmentCameraBinding.overlay.invalidate()
    }
    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

//    @ExperimentalCamera2Interop
//    fun selectExternalOrBestCamera(provider: ProcessCameraProvider):CameraSelector? {
//
//        val cam2Infos = provider.availableCameraInfos.map {
//            Camera2CameraInfo.from(it)
//        }.sortedByDescending {
//            // HARDWARE_LEVEL is Int type, with the order of:
//            // LEGACY < LIMITED < FULL < LEVEL_3 < EXTERNAL
//            it.getCameraCharacteristic(CameraCharacteristics.LENS_FACING)
//        }
//        provider.availableCameraInfos.forEach{ cameraInfo ->
//            val cam2Info = Camera2CameraInfo.from(cameraInfo)
//            Toast.makeText(context,"Camera ID: ${cam2Info.cameraId}",Toast.LENGTH_LONG).show()
//        }
//        return when {
//            cam2Infos.isNotEmpty() -> {
//                CameraSelector.Builder()
//                    .addCameraFilter {
//                        it.filter { camInfo ->
//                            // cam2Infos[0] is either EXTERNAL or best built-in camera
//                            val thisCamId = Camera2CameraInfo.from(camInfo).cameraId
//                            thisCamId == cam2Infos[0].cameraId
//                        }
//                    }.build()
//            }
//            else -> null
//        }
//    }
}

