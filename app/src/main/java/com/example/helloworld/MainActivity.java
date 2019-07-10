package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;


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

        // 查找默认相机的标识
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

class Notify{
    public void Notify(int eventID){
        // 声音播放和文字改变代码于此做
    }

}

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.helloworld.MESSAGE";
    public static boolean doIt = true;
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


    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }


    // 这是针对拍到照片的listener
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d("Create File err", "Error creating media file, check storage permissions");
                return;
            }

//            try {
                // 在这里实现拍到照片后的动作
                // Instantiate an ImageView and define its properties, Bitmap should be transformed into drawable first.
                displayImg(data);
                safeToTakePic = true;
//                FileOutputStream fos = new FileOutputStream(pictureFile);
//                fos.write(data);
//                fos.close();
//            } catch (FileNotFoundException e) {
//                Log.d("Not Found", "File not found: " + e.getMessage());
//            } catch (IOException e) {
//                Log.d("Access File", "Error accessing file: " + e.getMessage());
//            }
        }
    };

    protected void displayImg(byte[] data){

        //转换二进制矩阵为bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
        ImageView imageView = findViewById(R.id.imageCaptured);
        imageView.setImageBitmap(bitmap);
    }

    //开始以及重新开始的时候查找相机
    protected void onResume(){

        super.onResume();
        // Create an instance of Camera
        mCamera = CameraActivity.openCamera();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        if (mCamera != null) {
            // 旋转方向
            mCamera.setDisplayOrientation(90);
        }
        Log.d("Running normal","Good to go");
        FrameLayout preview = findViewById(R.id.camera_preview);
        Log.d("Running normal","preview create");

        //display preview
        preview.addView(mPreview);
        mCamera.startPreview();
//        while( doIt ){
//            try {
////
//                Log.d("Running normal","preview start");
//                safeToTakePic = true;
//                if (safeToTakePic){
////
//                    Log.d("Success","Captured pic");
//                }
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }


    @Override
    protected void onPause(){
        super.onPause();
        stopCamera();
    }

    private void stopCamera(){
        if(mCamera != null){
            mCamera.release();
            mCamera = null;
            Log.d("Success","Released Camera.");
        }
    }

    public void changeCondition(View view) {
        // Production stage, delete this thing, and move useful things to onResume().
        int eventID = (int)(Math.random() * 3);
        SoundPool soundPool = new SoundPool(5, AudioManager.STREAM_SYSTEM,5);
        int[] sound = new int[3];

        // put them in front of every thing after putting them into onResume().
        sound[0] = soundPool.load(this,R.raw.alarm_1,1);
        sound[1] = soundPool.load(this,R.raw.alarm_2,1);
        sound[2] = soundPool.load(this,R.raw.alarm_3,1);
        mCamera.takePicture(null, null, mPicture);
        soundPool.stop(signal);
        signal = alertSender(eventID,soundPool,sound,signal);
    }

    public int alertSender(int eventID,SoundPool soundPool,int[] sound,int signal){
        TextView editText = findViewById(R.id.Situation);
        String update = null;
        int color = this.getResources().getColor(R.color.dangerDirve);
        //Sound related stuff


        // The most severe event should have the highest priority. currently not functioning
        switch (eventID){
            case 0:
                update = this.getString(R.string.abnormal_2);
                signal = soundPool.play(sound[0],1, 1, 0, 0, 1);
                break;
            case 1:
                update = this.getString(R.string.abnormal_3);
                signal = soundPool.play(sound[1],1, 1, 0, 0, 1);
                break;
            case 2:
                update = this.getString(R.string.abnormal_4);
                signal = soundPool.play(sound[2],1, 1, 0, 0, 1);
                break;
            case 3:
                update = this.getString(R.string.normal);
                color = this.getResources().getColor(R.color.safeDrive);
        }
        Log.d("stage reached", "alertSender: I am here");
        editText.setText(update);
        editText.setTextColor(color);
        return signal;
    }

//    public void clickButton(View view) {
//        //why not functional? Needs a public void class, very strict.
//        Intent intent = new Intent(this, DisplayMessageActivity.class);    // a new object, can send message from A to B, OVER ACTIVITIES! like a pipe
//        // the initiator's value is: 1.who starts the view; 2.who receives intent's call
//        EditText editText = (EditText) findViewById(R.id.editText);                         // editText is a kinda dataType, is used in activity_xxx.xml
//        String message = editText.getText().toString();
//        intent.putExtra(EXTRA_MESSAGE, message);                                             // function putExtra can
//        startActivity(intent);
//    }

}
