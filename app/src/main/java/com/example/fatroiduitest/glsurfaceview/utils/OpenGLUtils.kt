package com.example.fatroiduitest.glsurfaceview.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Log
import com.example.fatroiduitest.MainActivity.constants.TAG
import java.io.ByteArrayOutputStream
import javax.microedition.khronos.opengles.GL10

class OpenGLUtils {
    companion object {

        fun getExternalOESTextureID(): Int {
            val texture = IntArray(1)
            GLES20.glGenTextures(1, texture, 0)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0])
            GLES20.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S,
                GL10.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T,
                GL10.GL_CLAMP_TO_EDGE
            )
            return texture[0]
        }

        fun createTexture(): Int {
            val texture = IntArray(1)
            GLES20.glGenTextures(1, texture, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0])
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
            return texture[0]
        }

        fun createProgram(vertexSource: String, fragmentSource: String): Int {
            // 1. load shader
            val vertexShader: Int = OpenGLUtils.loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
            if (vertexShader == GLES20.GL_NONE) {
                return GLES20.GL_NONE
            }
            val fragmentShader: Int =
                OpenGLUtils.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
            if (fragmentShader == GLES20.GL_NONE) {
                return GLES20.GL_NONE
            }

            // 2. create gl program
            val program = GLES20.glCreateProgram()
            if (program == GLES20.GL_NONE) {
                return GLES20.GL_NONE
            }

            // 3. attach shader
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)

            // we can delete shader after attach
            GLES20.glDeleteShader(vertexShader)
            GLES20.glDeleteShader(fragmentShader)

            // 4. link program
            GLES20.glLinkProgram(program)

            // 5. check link status
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == GLES20.GL_FALSE) { // link failed
                GLES20.glDeleteProgram(program) // delete program
                return GLES20.GL_NONE
            }
            return program
        }

        fun loadShader(type: Int, source: String): Int {
            // 1. create shader
            var shader = GLES20.glCreateShader(type)
            if (shader == GLES20.GL_NONE) {
                Log.e(TAG, "create shared failed! type: $type")
                return GLES20.GL_NONE
            }
            // 2. load shader source
            GLES20.glShaderSource(shader, source)
            // 3. compile shared source
            GLES20.glCompileShader(shader)
            // 4. check compile status
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == GLES20.GL_FALSE) { // compile failed
                Log.e(TAG, "Error compiling shader. type: $type:")
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader))
                GLES20.glDeleteShader(shader) // delete shader
                shader = GLES20.GL_NONE
            }
            return shader
        }

        fun convertYUVtoRGB(yuvData: ByteArray, width: Int, height: Int): Bitmap {
            val yuvImage = YuvImage(yuvData, ImageFormat.NV21, width, height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
            val imageBytes = out.toByteArray()
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
    }
}