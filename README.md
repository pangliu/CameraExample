情況1: 使用 Camera + SurfaceView

camera 呼叫流程：

camera.setPreviewDisplay(surfaceHolder) -> setCameraCallback -> camera.startPreview

其中 setCameraCallback 包含 setPreviewCallbackWithBuffer() 與 setPreviewCallback() 兩種模式
且 setCallback 後還需要加入 camera.addCallbackBuffer(buffer) 才會在之後呼叫 callback

當使用 setPreviewCallbackWithBuffer() 時，onPreviewFrame(data: ByteArray?, camera: Camera) 所回傳的 byteArray 確實為畫面的原始資料可被替換

