package com.example.fatroiduitest

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import java.io.ByteArrayOutputStream

class CamerUtils {
    companion object {
        // 取得鏡頭ID
        fun getFrontCameraId(): Int? {
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

        fun convertYUVToBitmap(data: ByteArray, width: Int, height: Int): Bitmap {
            // YuvImage 需要參數: YUV 數據、影像格式、寬度、高度、圖片範圍
            val yuvImage = YuvImage(data, ImageFormat.NV21, width, height, null)
            val baos = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, baos) // 將 YUV 轉換為 JPEG 格式
            val jpegData = baos.toByteArray() // 獲取 JPEG 數據
            return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size) // 將 JPEG 數據解碼為 Bitmap
        }
    }
}