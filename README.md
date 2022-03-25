# ImageTransmission

## 一、功能简介

C03 充当 USB 摄像头，使用 HDMI - USB 转接线连接到机顶盒，机顶盒再通过显示器输出 C03 画面内容。使用安卓 Camera2 以及 AudioRecord 和 AudioTrack 完成视频和音频的传输。

## 二、SDK 使用指南

1. 导入 SDK

将 classes.jar 拷贝至 Android 工程的 libs 目录下，修改 build.gradle 文件，编译项目。

```
dependencies {
    implementation files('libs/classes.jar') // add
}
```

2. 主要接口

```
interface ICameraService {
    void startC03Preview(); // 开启C03预览
    void stopC03Preview(); // 关闭C03预览
}
```

3. 添加用户权限

在工程 AndroidManifest.xml 文件中添加如下权限，注册 service。

```
<uses-permission android:name="android.permission.CAMERA"/>
<uses-permission android:name="android.permission.RECORD_AUDIO"/>

<service android:name="com.android.api.CameraService" android:exported="true"/>
```

4. 布局文件添加 TextureView

android:layout_width 和 android:layout_height 指定画面显示大小。

```
<TextureView
    android:id="@+id/tv"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

5. 示例 Activity

```
public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "wlzhou";
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_PERMISSIONS_CODE = 1;
    private TextureView textureView;
    private ICameraService cameraService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "[onCreate]");
        super.onCreate(savedInstanceState);
        hideTitle();
        setContentView(R.layout.activity_camera);
        initView();
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "[onStart]");
        // 绑定服务
        bindCameraService();
    }
    @Override
    protected void onStop() {
        Log.d(TAG, "[onStop]");
        super.onStop();
        try {
            cameraService.stopC03Preview();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        // 解绑服务
        unBindCameraService();
    }
    // 设置窗口没有标题
    private void hideTitle() {
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean flag = true;
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    flag = false;
                    break;
                }
            }
        }
        if (flag) {
            try {
                cameraService.startC03Preview();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(CameraActivity.this, "no permission", Toast.LENGTH_SHORT).show();
        }
    }
    private void initView() {
        textureView = findViewById(R.id.tv);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                Toast.makeText(CameraActivity.this, "created", Toast.LENGTH_SHORT).show();
                CameraService.setTextureView(textureView);
            }
            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
            }
            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                return false;
            }
            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
            }
        });
    }
    private boolean hasPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(CameraActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }
    private void bindCameraService() {
        Intent intent = new Intent(this, CameraService.class);
        startService(intent);
        boolean b = bindService(intent, cameraServiceConnection, BIND_AUTO_CREATE);
        Log.d(TAG, "[bindCameraService] isBind -> " + b);
    }
    private void unBindCameraService() {
        Intent intent = new Intent(this, CameraService.class);
        unbindService(cameraServiceConnection);
        stopService(intent);
    }
    private ServiceConnection cameraServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "[onServiceConnected]");
            cameraService = ICameraService.Stub.asInterface(iBinder);
            if (!hasPermissions()) {
                requestPermissions(REQUIRED_PERMISSIONS, REQUEST_PERMISSIONS_CODE);
            } else {
                try {
                    cameraService.startC03Preview();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected");
            cameraService = null;
        }
    };
}
```

## 三、功能操作步骤

使用 HDMI - USB 转接线连接 C03 和机顶盒，完成 C03 到机顶盒的镜像传输。注：HDMI 端连接 C03，USB 端连接机顶盒。

图片: https://uploader.shimo.im/f/1OggR9mhBhoMiS3I.png!thumbnail?accessToken=eyJhbGciOiJIUzI1NiIsImtpZCI6ImRlZmF1bHQiLCJ0eXAiOiJKV1QifQ.eyJhdWQiOiJhY2Nlc3NfcmVzb3VyY2UiLCJleHAiOjE2NDgxODkzOTMsImciOiJYS3E0TTRtb0VFYzE1S2tOIiwiaWF0IjoxNjQ4MTg5MDkzLCJ1c2VySWQiOjI4Mjk1ODQ0fQ.YsHARB3AjJuFMrFgaswUtEmtAPJU3BMZlV77OpPIuW8

## 四、完整 Demo 下载

https://github.com/hiwlzhou/ImageTransmission/tree/master/app_camera
https://github.com/hiwlzhou/ImageTransmission/tree/master/app_api