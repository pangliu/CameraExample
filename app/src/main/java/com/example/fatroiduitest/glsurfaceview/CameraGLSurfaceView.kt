package com.example.fatroiduitest.glsurfaceview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import com.example.fatroiduitest.CamerUtils
import com.example.fatroiduitest.MainActivity.constants.TAG
import com.example.fatroiduitest.glsurfaceview.utils.CameraDrawer
import com.example.fatroiduitest.glsurfaceview.utils.OpenGLUtils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraGLSurfaceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
): GLSurfaceView(context, attrs),
    GLSurfaceView.Renderer,
    SurfaceTexture.OnFrameAvailableListener,
    Camera.PreviewCallback{

    private var camera: Camera? = null
    private var drawer: CameraDrawer? = null
    private var textureId: Int = 0
    private var surfaceTexture: SurfaceTexture? = null
    private var mImgview: ImageView? = null

    init {
        Log.e(TAG, "GLSurfaceView init")
        setEGLContextClientVersion(2)  // 使用 OpenGL ES 2.0
        setRenderer(this)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.e(TAG, "onSurfaceCreated")
        /**
         * 1.創建 OpenGL 紋理
         * 2.與 OpenGL相關的函數只能在 GLSurfaceView.Renderer 相關 callback 執行
         */
        textureId = OpenGLUtils.getExternalOESTextureID()
        Log.e(TAG, "startCamera: $textureId")
        // 2. 創建 SurfaceTexture 並綁定到紋理
        surfaceTexture = SurfaceTexture(textureId)
        surfaceTexture?.setOnFrameAvailableListener(this)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.e(TAG, "onSurfaceChanged")
        GLES20.glViewport(0, 0, width, height)
        startPreview(surfaceTexture)
    }

//    @Deprecated("Deprecated in Java")
    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        Log.e(TAG, "onPreviewFrame")
        if(null != camera) {
            val previewWidth = camera.parameters?.previewSize?.width
            val previewHeight = camera.parameters?.previewSize?.height
            val bitmap = OpenGLUtils.convertYUVtoRGB(data!!, previewWidth!!, previewHeight!!)
            // 將 Bitmap 發送給 renderer 更新紋理
            this.queueEvent {
                updateTexture(bitmap)
            }
            setByteArrayToImageView(camera, data!!)
            camera.addCallbackBuffer(data)
        }
    }

    fun updateTexture(bitmap: Bitmap) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        Log.e(TAG, "onDrawFrame gl: $gl")
        if (gl == null) return
        // 繪製畫面
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        surfaceTexture?.updateTexImage()
        drawer?.draw(textureId, true)

    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        Log.e(TAG, "onFrameAvailable")
        requestRender()
    }

    // 相機初始化
    fun startPreview(surfaceTexture: SurfaceTexture?) {
        Log.e(TAG, "startCamera")

        camera = CamerUtils.getFrontCameraId()?.let {
            Camera.open(it).apply {
                parameters = parameters.apply {
                    previewFormat = ImageFormat.NV21 // 設定相機預覽格式為 NV21
                }
                val width = parameters?.previewSize?.width
                val height = parameters?.previewSize?.height
                val bufferSize = width!! * height!! * 3 / 2 // YUV_420 格式
                val buffer1 = ByteArray(bufferSize)
                setPreviewCallbackWithBuffer(this@CameraGLSurfaceView)
                // 添加初始緩衝區
                addCallbackBuffer(buffer1)
                setPreviewTexture(surfaceTexture)
                startPreview()
            }
        }
        drawer = CameraDrawer()
    }

    fun releaseCamera() {
        camera?.apply {
            stopPreview()
//            setPreviewCallback(null)
            setPreviewCallbackWithBuffer(null)
            release()
            camera = null
        }
    }

    fun setBufferImgView(imgview: ImageView) {
        this.mImgview = imgview
    }

    // 將 buffer 裡的 byetarray 轉換為 Bitmap 放置 imageview 上
    private fun setByteArrayToImageView(camera: Camera?, data: ByteArray) {
        try {
            var width = camera?.parameters?.previewSize?.width
            var height = camera?.parameters?.previewSize?.height
            // 將 YUV 轉換為 Bitmap
            val bitmap = CamerUtils.convertYUVToBitmap(data!!, width!!, height!!)
            mImgview?.setImageBitmap(bitmap)
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }
}