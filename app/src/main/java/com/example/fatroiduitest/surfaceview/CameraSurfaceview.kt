package com.example.fatroiduitest.surfaceview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.ImageView
import com.example.fatroiduitest.MainActivity.constants.TAG
import java.io.ByteArrayOutputStream

class CameraSurfaceview @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SurfaceView(context, attrs), Camera.PreviewCallback, SurfaceHolder.Callback {
    private var camera: Camera? = null
    private var processedBitmap: Bitmap? = null
    private val surfaceLock = Any() // 用於同步鎖
    private var bufferImgView: ImageView? = null
//    private var glSurfaceView: CustomGLSurfaceView? = null

    init {
        holder.addCallback(this)
    }

    fun setImageView(imageView: ImageView) {
        this.bufferImgView = imageView
    }

//    fun setglSurfaceView(glSurfaceView: CustomGLSurfaceView) {
//        this.glSurfaceView = glSurfaceView
//    }

    // 公開方法：開始相機預覽
    fun startCameraPreview() {
        Log.e(TAG, "startCameraPreview")
        try {
            camera = getFrontCameraId()?.let {
                Camera.open(it).apply {
                    setPreviewDisplay(holder)
                    setPreviewCallbackWithBuffer(this@CameraSurfaceview) // 設置預覽回調
                    parameters = parameters.apply {
                        previewFormat = ImageFormat.NV21 // 設定相機預覽格式為 NV21
                    }
                }
            }
            val width = camera?.parameters?.previewSize?.width
            val height = camera?.parameters?.previewSize?.height
            val bufferSize = width!! * height!! * 3 / 2 // YUV_420 格式
            val buffer1 = ByteArray(bufferSize)
            val buffer2 = ByteArray(bufferSize)
            val buffer3 = ByteArray(bufferSize)
            val buffer4 = ByteArray(bufferSize)
            camera?.addCallbackBuffer(buffer1)
            camera?.addCallbackBuffer(buffer2)
            camera?.addCallbackBuffer(buffer3)
            camera?.addCallbackBuffer(buffer4)
            camera?.setDisplayOrientation(90)
            camera?.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 公開方法：停止相機預覽
    fun stopCameraPreview() {
        camera?.apply {
            stopPreview()
            setPreviewCallbackWithBuffer(null)
            release()
        }
        camera = null
    }

//    override fun onPreviewFrame(data: ByteArray?, camera: Camera) {
//        if (holder.surface == null || !holder.surface.isValid()) return // 確保 surface 可用
//        data?.let {
//            processedBitmap = convertYUVToBitmap(it, camera)
//            synchronized(surfaceLock) {
//                var canvas: Canvas? = null
//                try {
//                    canvas = holder.lockCanvas()
//                    canvas?.let { c ->
//                        c.drawColor(Color.BLACK)
//                        processedBitmap?.let { bmp ->
//                            c.drawBitmap(bmp, null, Rect(0, 0, width, height), null)
//                        }
//                    }
//                    Log.e(TAG, "onPreviewFrame success")
//                } catch (e: Exception) {
//                    Log.e(TAG, "onPreviewFrame exception: ${e.message}")
//                } finally {
//                    canvas?.let { holder.unlockCanvasAndPost(it) }
//                    Log.e(TAG, "onPreviewFrame finally")
//                }
//            }
//            setByteArrayToImageView(camera, data!!)
//            camera.addCallbackBuffer(data)
//        }
//    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera) {
//        val filteredBitmap = applyGrayScaleFilter(data!!, camera)
//        bufferImgView?.setImageBitmap(filteredBitmap)
//        filteredBitmap?.let {
//            drawFilteredBitmap(it)
//        }
        camera.addCallbackBuffer(data)
        Log.e(TAG, "onPreviewFrame success")
    }
    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.e(TAG, "video surfaceCreated")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // 可以根據需求調整相機參數
        Log.e(TAG, "video surfaceChanged")
//        camera?.apply {
//            stopPreview()
            // 設置相機的顯示旋轉角度為 90 度
//            setDisplayOrientation(90)
//            startPreview()
//        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.e(TAG, "video surfaceDestroyed")
        stopCameraPreview() // 停止相機並釋放資源
    }


    // 將 YUV 格式的 byteArray 轉換為 Bitmap
    private fun convertYUVToBitmap(data: ByteArray, camera: Camera): Bitmap? {
        val parameters = camera.parameters
        val width = parameters.previewSize.width
        val height = parameters.previewSize.height
        val yuvImage = YuvImage(data, parameters.previewFormat, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    // 取得鏡頭ID
    private fun getFrontCameraId(): Int? {
        val cameraCount = Camera.getNumberOfCameras()
        for (i in 0 until cameraCount) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(i, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return i // 返回前鏡頭的 ID
            }
        }
        return null // 如果沒有前鏡頭，則返回 null
    }

    // 將 buffer 裡的 byetarray 轉換為 Bitmap 放置 imageview 上
    private fun setByteArrayToImageView(camera: Camera?, data: ByteArray) {
        try {
            // 將 YUV 轉換為 Bitmap
            val bitmap = convertYUVToBitmap(data!!, camera!!)
            bufferImgView?.setImageBitmap(bitmap)
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyGrayScaleFilter(data: ByteArray, camera: Camera): Bitmap? {
        val parameters = camera.parameters
        val width = parameters.previewSize.width
        val height = parameters.previewSize.height

        // 將 YUV 圖像數據轉換為 Bitmap
        val yuvImage = YuvImage(data, parameters.previewFormat, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        // 確保 Bitmap 轉換成功
        if (bitmap == null) {
            return null
        }

        // 創建一個新的 Bitmap 並應用灰度濾鏡
        val grayBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)
                val gray = (0.3 * red + 0.59 * green + 0.11 * blue).toInt()
                grayBitmap.setPixel(x, y, Color.rgb(gray, gray, gray))
            }
        }
        return grayBitmap
    }

    private fun drawFilteredBitmap(bitmap: Bitmap) {
        val canvas = holder.lockCanvas()
        canvas?.let {
            // 清除畫布並繪製過濾後的 Bitmap
            it.drawColor(Color.BLACK)  // 清除畫布，避免舊畫面殘留
            it.drawBitmap(bitmap, null, Rect(0, 0, width, height), null)
            holder.unlockCanvasAndPost(it)
        }
    }

}