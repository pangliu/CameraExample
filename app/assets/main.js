// toast('Hello, AutoX.js');

// 定義要打開的應用程式包名
var packageName = "com.globe.gcash.android";

// 檢查應用是否已經安裝
if (app.getPackageName(packageName)) {
    // 打開 GCash 應用
    app.launchPackage(packageName);
    toast("已啟動 GCash");
} else {
    // 如果未安裝，提示未安裝
    toast("GCash 應用未安裝");
}