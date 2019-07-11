package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;


/**
 * A basic Camera preview class
 */
class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            Log.d("Success", "surfaceCreated: preview started");
        } catch (IOException e) {
            Log.d("Preview start err", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        // 这个方法用来改变相机的状态的

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d("Preview start Err:", "Error starting camera preview: " + e.getMessage());
        }
    }
}


class CameraActivity extends Activity {
    public static Camera openCamera() {
        Camera camera = null;

        // 查找默认相机的标识，设定前摄像头
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int numberOfCameras = Camera.getNumberOfCameras();
        int defaultCameraId = 0;
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                defaultCameraId = i;
            }
        }

        try {
            camera = Camera.open(defaultCameraId);
        } catch (Exception e) {
            // 这里提示用户摄像头被其他程序占用
        }
        return camera; //返回值如果是null那么调用失败
    }
}

class Notify {
    public void Notify(int eventID) {
        // 声音播放和文字改变代码于此做
    }

}

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.helloworld.MESSAGE";
    public boolean doIt = false;
    public Camera mCamera;
    public CameraPreview mPreview;
    public Boolean safeToTakePic = false;
    int signal = 0;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

                //此处显示请求不通过的信息

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        1);

                // requestPermissions的最后一个是个自定义用于识别请求到的权限的整型值
            }
        }
        // fuck virtual button on the bottom of Huawei mobile
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    //开始以及重新开始的时候查找相机
    protected void onResume() {

        super.onResume();

        // Create an instance of Camera
        mCamera = CameraActivity.openCamera();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        if (mCamera != null) {
            // 旋转方向
            mCamera.setDisplayOrientation(90);
        }
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        mCamera.startPreview();
        Log.d("Success", "onResume: Preview started");
        safeToTakePic = true;

        //安卓不能在操作UI的时候写死循环，要另开线程操作
        new Monitor().start();

    }


    @Override
    protected void onPause() {
        super.onPause();
        stopCamera();
    }

    private void stopCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
            Log.d("Success", "Released Camera.");
        }
    }

    // 这是针对拍到照片的listener
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {


            // 在这里实现拍到照片后的动作
            // Instantiate an ImageView and define its properties, Bitmap should be transformed into drawable first.
            displayImg(data);
            if (data != null) {
                Log.d("Success", "onPictureTaken: Got picture data");
            }
            safeToTakePic = true;
        }
    };

    protected void displayImg(byte[] data) {

        //转换二进制矩阵为bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        ImageView imageView = findViewById(R.id.imageCaptured);
        imageView.setImageBitmap(bitmap);
    }

    // Monitor main function

    class Monitor extends Thread{

        public int alertSender(int eventID, SoundPool soundPool, int[] sound, int signal) {
            TextView editText = findViewById(R.id.Situation);
            String update = null;
            int color = MainActivity.this.getResources().getColor(R.color.dangerDirve);

            // The most severe event should have the highest priority. currently this function is not functioning
            switch (eventID) {
                case 0:
                    update = MainActivity.this.getString(R.string.abnormal_2);
                    signal = soundPool.play(sound[0], 1, 1, 0, 0, 1);
                    break;
                case 1:
                    update = MainActivity.this.getString(R.string.abnormal_3);
                    signal = soundPool.play(sound[1], 1, 1, 0, 0, 1);
                    break;
                case 2:
                    update = MainActivity.this.getString(R.string.abnormal_4);
                    signal = soundPool.play(sound[2], 1, 1, 0, 0, 1);
                    break;
                case 3:
                    update = MainActivity.this.getString(R.string.normal);
                    color = MainActivity.this.getResources().getColor(R.color.safeDrive);
            }
            Log.d("stage reached", "alertSender: I am here");
            editText.setText(update);
            editText.setTextColor(color);
            return signal;
        }

        @Override
        public void run(){
            int eventID;
            SoundPool soundPool = new SoundPool(5, AudioManager.STREAM_SYSTEM, 5);
            int[] sound = new int[3];

            // put them in front of every thing after putting them into onResume().
            sound[0] = soundPool.load(MainActivity.this, R.raw.alarm_1, 1);
            sound[1] = soundPool.load(MainActivity.this, R.raw.alarm_2, 1);
            sound[2] = soundPool.load(MainActivity.this, R.raw.alarm_3, 1);
            //wait for audio to load.
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            eventID = (int) (Math.random() * 3);
            while (true) {
            try {
                    if (safeToTakePic && doIt) {
                        mCamera.takePicture(null, null, mPicture);
                        safeToTakePic = false;
                        soundPool.stop(signal);
                        signal = alertSender(eventID, soundPool, sound, signal);
                        Thread.sleep(1000);
                        mCamera.startPreview();
                        safeToTakePic = true;
                    }
                } catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

// tap on textview to switch on or off whole function.
    public void switchSys(View view) {
        // Production stage, delete this thing, and move useful things to onResume().
        doIt = !doIt;
        Log.d("Status", "switchSys: Value is: " + doIt);
    }


}
