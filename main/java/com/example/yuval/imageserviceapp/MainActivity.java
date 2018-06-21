package com.example.yuval.imageserviceapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Start the operation of the image service.
     * @param view a view object
     */
    public void imageServiceStart(View view) {
        Intent startServiceIntent = new Intent(this, PhotosTransferService.class);
        startService(startServiceIntent);
    }

    /**
     * Stop the operation of the image service.
     * @param view a view object
     */
    public void imageServiceStop(View view) {
        Intent stopServiceIntent = new Intent(this, PhotosTransferService.class);
        stopService(stopServiceIntent);
    }
}
