package com.example.yuval.imageserviceapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class PhotosTransferService extends Service {
    IntentFilter _intentFilter = new IntentFilter();
    BroadcastReceiver _broadcastReceiver;
    private CameraStorageAdapter _cameraStorageAdapter;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Image service started...", Toast.LENGTH_SHORT).show();
        _broadcastReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onReceive(Context context, Intent intent) {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                            transferCameraPhotos(context);
                        }
                    }
                }
            }
        };
        this.registerReceiver(_broadcastReceiver, _intentFilter);
        return START_STICKY;
    }

    /**
     * Sends the photos from the DCIM folder to the image service.
     *
     * @param context Context object
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void transferCameraPhotos(Context context) {
        //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        NotificationChannel notificationChannel = new NotificationChannel("ImageService_Channel",
                "Image Service App Channel",
                NotificationManager.IMPORTANCE_LOW);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.BLUE);
        notificationChannel.enableVibration(true);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "ImageService_Channel");
        final int id = 1;
        final NotificationManager NM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NM.createNotificationChannel(notificationChannel);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentTitle("Photos Transfer");
        builder.setContentText("Transfer in progress...").setPriority(NotificationCompat.PRIORITY_LOW);
        Runnable photosTransferTask = new Runnable() {
            @Override
            public void run() {
                try {
                    _cameraStorageAdapter.fetchPhotosFromDCIM();
                    List<File> cameraPhotos = _cameraStorageAdapter.getCameraPhotos();
                    int progressBarPercentage = 0;
                    for (File photo : cameraPhotos) {
                        TcpClientAdapter adapter = new TcpClientAdapter();
                        adapter.sendPhotoToService(photo);
                        progressBarPercentage = progressBarPercentage + 100 / cameraPhotos.size();
                        builder.setProgress(100, progressBarPercentage, false);
                        NM.notify(id, builder.build());
                    }
                    builder.setProgress(0, 0, false);
                    builder.setContentText("Transfer Done");
                    NM.notify(id, builder.build());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread photosTransferThread = new Thread(photosTransferTask);
        photosTransferThread.start();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Image service stopped...", Toast.LENGTH_SHORT).show();
        this.unregisterReceiver(_broadcastReceiver);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _intentFilter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");
        _intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        _cameraStorageAdapter = new CameraStorageAdapter();
    }
}
