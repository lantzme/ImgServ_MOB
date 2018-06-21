package com.example.yuval.imageserviceapp;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CameraStorageAdapter {
    List<File> _photos;
    File _dcim;

    /**
     * The constructor of the class
     */
    public CameraStorageAdapter() {
        _photos = new ArrayList<>();
        _dcim = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "Camera");
    }

    /**
     * Scan recursively input directory and creates a list of its photos.
     *
     * @param innerDirectory File object
     */
    private void scanDirectory(File innerDirectory) {
        File[] filesInDirectory = innerDirectory.listFiles();
        for (File file : filesInDirectory) {
            if (!file.isDirectory()) {
                synchronized (this) {
                    _photos.add(file);
                }
            } else {
                scanDirectory(file);
            }
        }
    }

    /**
     * Scan recursively the DCIM directory and creates a list of its photos.
     *
     * @throws Exception Invalid camera url Exception
     */
    public void fetchPhotosFromDCIM() throws Exception {
        File[] cameraPhotoFiles = _dcim.listFiles();
        if (cameraPhotoFiles == null) {
            throw new Exception("Invalid camera url, or no pics to fetch.");
        } else {
            for (File photo : cameraPhotoFiles) {
                if (!photo.isDirectory()) {
                    synchronized (this) {
                        _photos.add(photo);
                    }
                } else {
                    scanDirectory(photo);
                }
            }
        }
    }

    /**
     * Returns a list of the camera photos.
     *
     * @return List<File>
     */
    public List<File> getCameraPhotos() {
        return _photos;
    }
}
