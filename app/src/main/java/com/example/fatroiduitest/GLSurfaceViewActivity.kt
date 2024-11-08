package com.example.fatroiduitest

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.fatroiduitest.databinding.ActivityGlsurfaceviewBinding
import com.example.fatroiduitest.glsurfaceview.CameraGLSurfaceView

class GLSurfaceViewActivity: AppCompatActivity() {
    private lateinit var binding: ActivityGlsurfaceviewBinding
    private lateinit var cameraGLSurfaceView: CameraGLSurfaceView
    private var isPreviewStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGlsurfaceviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraGLSurfaceView = binding.glSurfaceView
        cameraGLSurfaceView.setBufferImgView(binding.ivBufferImg)
        binding.btnPreview.setOnClickListener {
            // 並啟動相機預覽
            if (!isPreviewStarted) {
                cameraGLSurfaceView.visibility = View.VISIBLE
                isPreviewStarted = true
            }
        }
        binding.btnCancel.setOnClickListener {
            // 停止相機預覽並隱藏
            if (isPreviewStarted) {
                cameraGLSurfaceView.visibility = View.INVISIBLE
                cameraGLSurfaceView.releaseCamera()
                isPreviewStarted = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cameraGLSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        // Activity 進入後台時停止預覽，釋放資源
        if (isPreviewStarted) {
            cameraGLSurfaceView.releaseCamera()
            cameraGLSurfaceView.onPause()
            isPreviewStarted = false
        }
    }
}