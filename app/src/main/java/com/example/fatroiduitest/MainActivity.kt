package com.example.fatroiduitest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.fatroiduitest.MainActivity.constants.TAG
import com.example.fatroiduitest.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    object constants {
        const val TAG = "hank"
    }
    private lateinit var binding: ActivityMainBinding
    private val REQUEST_CAMERA_PERMISSION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "onCreate")
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(!hasCameraPermission()) {
            requestCameraPermission()
        }

        binding.btnGlsurfaceview.setOnClickListener {
            Log.e(TAG, "開始錄影")
            val intent = Intent(this, GLSurfaceViewActivity::class.java)
            startActivity(intent)
        }
        binding.btnSurfaceview.setOnClickListener {
            val intent = Intent(this, SurfaceViewActivity::class.java)
            startActivity(intent)
        }
    }


    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // 要求相機權限
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    // 處理權限請求結果
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 權限獲得，開始相機預覽
            } else {
                // 權限被拒絕，顯示提示
            }
        }
    }
}