package com.android.api;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.TextureView;

public class UsbCameraManager {

    private static ICameraService cameraService;
    private static volatile Activity activity;

    private UsbCameraManager() {}

    /**
     * 设置预览画面显示位置
     * @param TextureView实例
     */
    public static void setTextureView(TextureView tv) {
        LogUtils.d("set texture view");
        CameraService.setTextureView(tv);
    }

    /**
     * 开启摄像头预览
     * @param Activity实例
     */
    public static void startUsbCameraPreview(Activity a) {
        LogUtils.d("start usb camera preview");
        activity = a;
        bindCameraService();
    }

    /**
     * 停止摄像头预览
     */
    public static void stopUsbCameraPreview() {
        LogUtils.d("stop usb camera preview");
        try {
            cameraService.stopUsbCameraPreview();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        unBindCameraService();
    }

    // 绑定服务
    private static void bindCameraService() {
        LogUtils.d("bind camera service");
        Intent intent = new Intent(activity, CameraService.class);
        activity.startService(intent);
        boolean flag = activity.bindService(intent, cameraServiceConnection, Context.BIND_AUTO_CREATE);
        LogUtils.d("isBind -> " + flag);
    }

    // 解绑服务
    private static void unBindCameraService() {
        LogUtils.d("unBind camera service");
        Intent intent = new Intent(activity, CameraService.class);
        activity.unbindService(cameraServiceConnection);
        activity.stopService(intent);
    }

    private static ServiceConnection cameraServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LogUtils.d("onServiceConnected");
            cameraService = ICameraService.Stub.asInterface(iBinder);

            // 开启摄像头预览
            try {
                cameraService.startUsbCameraPreview();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtils.d("onServiceDisconnected");

            // 停止摄像头预览
            try {
                cameraService.stopUsbCameraPreview();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            cameraService = null;
        }
    };
}