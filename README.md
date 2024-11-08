
## 情況1: 使用 Camera + SurfaceView
- camera 呼叫流程：camera.setPreviewDisplay(surfaceHolder) -> setCameraCallback -> camera.startPreview

- 其中 setCameraCallback 包含 setPreviewCallbackWithBuffer() 與 setPreviewCallback() 兩種模式且 setCallback 後還需要加入 camera.addCallbackBuffer(buffer) 才會在之後呼叫 callback

- 當使用 setPreviewCallbackWithBuffer() 時，onPreviewFrame(data: ByteArray?, camera: Camera) 所回傳的 byteArray 確實為畫面的原始資料可被替換
## setPreviewCallbackWithBuffer
```javascript
private fun setCameraCallback() {
        val width = camera?.parameters?.previewSize?.width
        val height = camera?.parameters?.previewSize?.height
        val bufferSize = width!! * height!! * 3 / 2 // YUV_420 格式
        val buffer1 = ByteArray(bufferSize)
        // 設置預覽回調，並使用緩衝區
        camera?.setPreviewCallbackWithBuffer(object : Camera.PreviewCallback {
            override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
                /**
                 * 此時回傳的 ByteArray 確實為 camera 取得的 byteArray，且可以被替換
                 * 但 surfaceView 上面的渲染不會變目前無法測出該如何替換 surfaceView 上面的渲染畫面
                 */
                camera?.addCallbackBuffer(data)
            }
        })
        // 添加初始緩衝區，要呼叫這個 method，之後才會呼叫 callback
        camera?.addCallbackBuffer(buffer1)
      
```
- 使用 setPreviewCallbackWithBuffer 設置的回調允許我們預先分配一塊內存緩衝區，並將其重複使用，從而避免頻繁的內存分配。
- 在每次回調後，必須手動將緩衝區重新添加到相機的緩衝池中，否則相機將不會再使用這個緩衝區。通常，回調中需要調用 camera.addCallbackBuffer(data) 將緩衝區釋放並重新加入池中，供下一幀使用。
- 優點：通過重複使用緩衝區，可以顯著降低內存壓力和垃圾回收次數，從而提高性能。
- 適用場景：適合需要高性能和低延遲的場景，特別是處理高幀率相機預覽數據時。


## setPreviewCallback
```javascript
camera?.setPreviewCallback(object : Camera.PreviewCallback {
            override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
                // 此時拿到的 ByteArray 無法替換
                camera?.addCallbackBuffer(data)
            }
        })
```
- 使用 setPreviewCallback 設置的回調，Android 相機在每次生成預覽幀時都會自動分配一個新的緩衝區給該幀。
- 缺點：由於每幀都分配新內存，這種方式可能會導致頻繁的內存分配與釋放，增加垃圾回收（GC）壓力，並可能導致性能瓶頸，特別是在高幀率的情況下。
- 適用場景：適用於簡單的預覽處理，且對性能要求不高的情況。