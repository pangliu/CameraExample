package com.example.fatroiduitest

import android.graphics.ImageFormat
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.fatroiduitest.MainActivity.constants.TAG
import com.example.fatroiduitest.databinding.ActivityMainBinding
import com.example.fatroiduitest.databinding.ActivitySurfaceviewBinding

class SurfaceViewActivity: AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var binding: ActivitySurfaceviewBinding
    private lateinit var surfaceView: SurfaceView
    private lateinit var surfaceHolder: SurfaceHolder
    private var camera: Camera? = null
    private var isPreviewing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurfaceviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        surfaceView = binding.sfvCamera
        surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)
        binding.btnStart.setOnClickListener {
            openCameraIfNeeded()
            startCameraPreview()
        }
        binding.btnClose.setOnClickListener {
            stopCameraPreview()
        }
    }
    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.e(TAG, "surfaceCreated")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.e(TAG, "surfaceChanged")
        camera?.stopPreview()
        try {
            camera?.setPreviewDisplay(holder)
            camera?.startPreview()
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.e(TAG, "surfaceDestroyed")
        stopCameraPreview()
    }

    private fun openCameraIfNeeded() {
        Log.e(TAG, "openCameraIfNeeded: $camera")
        if(camera == null) {
            binding.sfvCamera.visibility = View.VISIBLE
            camera = CamerUtils.getFrontCameraId()?.let { Camera.open(it) }
        }
    }

    private fun startCameraPreview() {
        try {
            Log.e(TAG, "startCameraPreview isPreviewing: $isPreviewing, surfaceHolder: ${surfaceHolder.surface.isValid}")
            if(!isPreviewing && surfaceHolder.surface.isValid) {
                // 設定相機參數
                val paraments = camera?.parameters
                paraments?.apply {
                    previewFormat = ImageFormat.NV21
                }
                camera?.parameters = paraments
                // 設置預覽顯示區域
                camera?.setPreviewDisplay(surfaceHolder)
                // 設定 callback
                setCameraCallback()
                camera?.startPreview()
                isPreviewing = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopCameraPreview() {
        binding.sfvCamera.visibility = View.INVISIBLE
        isPreviewing = false
        camera?.stopPreview()
        camera?.setPreviewCallbackWithBuffer(null)
        camera?.release()
        camera = null
    }

    private fun setCameraCallback() {
        Log.e("hank", "setCameraCallback")
        val width = camera?.parameters?.previewSize?.width
        val height = camera?.parameters?.previewSize?.height
        val bufferSize = width!! * height!! * 3 / 2 // YUV_420 格式
        val buffer1 = ByteArray(bufferSize)
        val buffer2 = ByteArray(bufferSize)
        val buffer3 = ByteArray(bufferSize)
        val buffer4 = ByteArray(bufferSize)
//        camera?.setPreviewCallback(object : Camera.PreviewCallback {
//            override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
////                Log.e("hank", "setPreviewCallback onPreviewFrame test")
//                data?.forEachIndexed { index, _ ->
//                    data[index] = 0
//                }
//                camera?.addCallbackBuffer(data)
//            }
//        })
        // 設置預覽回調，並使用緩衝區
        camera?.setPreviewCallbackWithBuffer(object : Camera.PreviewCallback {
            override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
//                Log.e(TAG, "withBuffer onPreviewFrame: $data")
                setByteArrayToImageView(camera, data!!)
                camera?.addCallbackBuffer(data)
            }
        })
        // 添加初始緩衝區
        camera?.addCallbackBuffer(buffer1)
        camera?.addCallbackBuffer(buffer2)
        camera?.addCallbackBuffer(buffer3)
        camera?.addCallbackBuffer(buffer4)
    }

    // 將 buffer 裡的 byetarray 轉換為 Bitmap 放置 imageview 上
    private fun setByteArrayToImageView(camera: Camera?, data: ByteArray) {
        try {
            var width = camera?.parameters?.previewSize?.width
            var height = camera?.parameters?.previewSize?.height
            // 將 YUV 轉換為 Bitmap
            val bitmap = CamerUtils.convertYUVToBitmap(data!!, width!!, height!!)
            binding.ivBufferImg.setImageBitmap(bitmap)
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }
}