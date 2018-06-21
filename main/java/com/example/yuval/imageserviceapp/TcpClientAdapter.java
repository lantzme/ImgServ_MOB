package com.example.yuval.imageserviceapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class TcpClientAdapter {
    /**
     * Sends the input photo to the service.
     * @param photo File object
     */
    public void sendPhotoToService(File photo) {
        try {
            InetAddress serverAddr = InetAddress.getByName("10.0.2.2");
            Socket socket = new Socket(serverAddr, 8001);
            try {
                OutputStream output = socket.getOutputStream();
                InputStream input = socket.getInputStream();
                output.write("Begin".getBytes());
                boolean ackResult = waitForAcknowledgement(input);
                if (ackResult) {
                    //Send the photo's name to the service
                    output.write(getFileNameBytes(photo));
                }
                ackResult = waitForAcknowledgement(input);
                if (ackResult) {
                    //Send the photo to the service
                    output.write(convertPhotoFileTOBytes(photo));
                }
                ackResult = waitForAcknowledgement(input);
                if (ackResult) {
                    output.write("End".getBytes());
                }
                waitForAcknowledgement(input);
                output.flush();
            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            } finally {
                socket.close();
            }
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
    }

    /**
     * Returns true if received '1' from the service, otherwise false.
     * @param input
     * @return boolean
     * @throws IOException socket reading Exception
     */
    private boolean waitForAcknowledgement(InputStream input) throws IOException {
        byte[] ack = new byte[1];
        int result = input.read(ack);
        return result == 1;
    }

    /**
     * Returns the input photo file as an array of bytes.
     * @param file File object
     * @return byte[]
     * @throws IOException invalid path Exception
     */
    private byte[] convertPhotoFileTOBytes(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        Bitmap bm = BitmapFactory.decodeStream(fis);
        return getBytesFromBitmap(bm);
    }

    /**
     * Returns the input bitmap as an array of bytes.
     * @param bitmap Bitmap object
     * @return byte[]
     */
    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

    /**
     * Returns the input file name as an array of bytes.
     * @param photo File object
     * @return byte[]
     */
    private byte[] getFileNameBytes(File photo) {
        return photo.getName().getBytes();
    }
}
