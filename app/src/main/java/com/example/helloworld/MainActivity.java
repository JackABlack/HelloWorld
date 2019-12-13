package com.example.helloworld;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;


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
            setCameraParams(640,480);
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
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d("Preview start Err:", "Error starting camera preview: " + e.getMessage());
        }
    }

    private void setCameraParams(int width, int height) {
        Log.i("info", "setCameraParams  width=" + width + "  height=" + height);
        Camera.Parameters parameters = mCamera.getParameters();
        /*************************** 获取摄像头支持的PictureSize列表********************/
        List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
        for (Camera.Size size : pictureSizeList) {
            Log.i("info", "摄像头支持的分辨率：" + " size.width=" + size.width + "  size.height=" + size.height);
        }
        Camera.Size picSize = getBestSupportedSize(pictureSizeList, ((float) height / width));//从列表中选取合适的分辨率
        if (null == picSize) {
            picSize = parameters.getPictureSize();
        }
        Log.e("info", "我们选择的摄像头分辨率：" + "picSize.width=" + picSize.width + "  picSize.height=" + picSize.height);
        // 根据选出的PictureSize重新设置SurfaceView大小
        parameters.setPictureSize(picSize.width, picSize.height);
        /*************************** 获取摄像头支持的PreviewSize列表********************/
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
        for (Camera.Size size : previewSizeList) {
            Log.i("info", "摄像支持可预览的分辨率：" + " size.width=" + size.width + "  size.height=" + size.height);
        }
        Camera.Size preSize = getBestSupportedSize(previewSizeList, ((float) height) / width);
        if (null != preSize) {
            Log.e("info", "我们选择的预览分辨率：" + "preSize.width=" + preSize.width + "  preSize.height=" + preSize.height);
            parameters.setPreviewSize(preSize.width, preSize.height);
        }
        mCamera.cancelAutoFocus();
        mCamera.setParameters(parameters);
    }

    private Camera.Size getBestSupportedSize(List<Camera.Size> sizes, float screenRatio) {
        Camera.Size largestSize = null;
        int DEFAULT_PHOTO_WIDTH = 640;
        int DEFAULT_PHOTO_HEIGHT = 480;
        int largestArea = 0;
        for (Camera.Size size : sizes) {
            if ((float) size.height / (float) size.width == screenRatio) {
                if (size.width == DEFAULT_PHOTO_WIDTH && size.height == DEFAULT_PHOTO_HEIGHT) {
                    // 包含特定的尺寸，直接取该尺寸
                    largestSize = size;
                    break;
                } else if (size.height == DEFAULT_PHOTO_HEIGHT || size.width == DEFAULT_PHOTO_WIDTH) {
                    largestSize = size;
                    break;
                }
                int area = size.height + size.width;
                if (area > largestArea) {//找出最大的合适尺寸
                    largestArea = area;
                    largestSize = size;
                }
            } else if (size.height == DEFAULT_PHOTO_HEIGHT || size.width == DEFAULT_PHOTO_WIDTH) {
                largestSize = size;
                break;
            }
        }
        if (largestSize == null) {
            largestSize = sizes.get(sizes.size() - 1);
        }
        return largestSize;
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

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("tensorflow_inference");
        Log.d("Success","Tensorflow_loaded");
    }
    statisticUtils statistic = new statisticUtils();
    public boolean doIt = false;
    public Camera mCamera;
    public CameraPreview mPreview;
    public Boolean safeToTakePic = false;
    int signal = 0;
    int eventID = 0;
    String situation;
    private final String VGG_PATH = "file:///android_asset/vgg16net.pb";
    private final String Google_PATH = "file:///android_asset/GoogleNet.pb";
    private final String ICPv2_PATH = "file:///android_asset/InceptionV2.pb";
    private final String ResNet50_PATH = "file:///android_asset/Resnet50.pb";
    private String INPUT_NAME = "input_2";
    private String OUTPUT_NAME = "output_1";
    private TensorFlowInferenceInterface tf;

    //ARRAY TO HOLD THE PREDICTIONS AND FLOAT VALUES TO HOLD THE IMAGE DATA
    //保存图片和图片尺寸的
    float[] PREDICTIONS = new float[1000];
    private float[] floatValues;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // Here, thisActivity is the current activity, Require a camera permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

                //此处显示请求不通过的信息
                Toast.makeText(this,"请提供摄像头权限以继续",Toast.LENGTH_SHORT);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        1);
                // requestPermissions的最后一个是个自定义用于识别请求到的权限的整型值
            }
        }
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                //此处显示请求不通过的信息
                Toast.makeText(this,"请提供存储权限以继续",Toast.LENGTH_SHORT);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
                // requestPermissions的最后一个是个自定义用于识别请求到的权限的整型值
            }
        }

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // fuck virtual button on the bottom of Huawei mobile
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        String MODEL_PATH = "";
        doIt = false;
        switch (item.getItemId()) {
//            case R.id.loadModel:

            case R.id.VGG16:
                Toast.makeText(this, R.string.VGG16, Toast.LENGTH_LONG).show();
                model.setModel(modelUtils.VGG_PATH,"input_2","output_1","traditional",224);
                break;
            case R.id.ICPv2:
                Toast.makeText(this, "InceptionV2 Loaded", Toast.LENGTH_LONG).show();
                MODEL_PATH = ICPv2_PATH;
                INPUT_NAME = "Placeholder";
                OUTPUT_NAME = "Softmax";
                break;
            case R.id.GoogleNet:
                Toast.makeText(this, R.string.GoogleNet, Toast.LENGTH_LONG).show();
                MODEL_PATH = Google_PATH;
                INPUT_NAME = "input_1";
                OUTPUT_NAME = "output_1";
                break;
            case R.id.ResNet50:
                Toast.makeText(this, R.string.ResNet50, Toast.LENGTH_LONG).show();
                MODEL_PATH = ResNet50_PATH;
                INPUT_NAME = "input_2";
                OUTPUT_NAME = "output_1";
                break;


            default:
                break;
        }
        tf.close();
        tf = loadTFModel(getAssets(),model.MODEL_PATH);
        return true;
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
        safeToTakePic = true;
        tf = loadTFModel(getAssets(),model.MODEL_PATH);
        long threadId=Thread.currentThread().getId();
        Log.d("Info", "Thread: " + threadId);
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
        public void onPictureTaken(final byte[] data, Camera camera) {

            // 在这里实现拍到照片后的动作
            // Instantiate an ImageView and define its properties, Bitmap should be transformed into drawable first.
            mCamera.startPreview();
            safeToTakePic = true;
            HandlerThread handlerThread = new HandlerThread("handerThread");
            handlerThread.start();
            Handler proceedHandler = new Handler(handlerThread.getLooper()) {
                public void handleMessage(Message msg) {
                    try {
                        displayImg(data);
                    } catch (Exception e) {
                        Log.e("ERROR", "runValue: failed to get runValue ");
                    }
                    if (data != null) {
                        Log.d("Success", "onPictureTaken: Got picture data");
                    }
                }
            };
            proceedHandler.sendEmptyMessage(1);


        }
    };
    protected void displayImg(byte[] data)throws Exception {

        //转换二进制矩阵为bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        final String runDuration = process(bitmap) + "";
        statistic.push(Integer.parseInt(runDuration));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //这里面进行UI的更新操作
                TextView editText = findViewById(R.id.runValue);
                editText.setText(runDuration);
                editText = findViewById(R.id.avgValue);
                editText.setText(statistic.getAvg() + "");
                editText = findViewById(R.id.TimeValue);
                editText.setText(statistic.getRunTimes() + "");
            }
        });
//        ImageView imageView = findViewById(R.id.imageCaptured);
//        imageView.setImageBitmap(bitmap);
        Log.d("DURATION", "runtime cost: " + runDuration + "ms");
    }

    //FUNCTION TO COMPUTE THE MAXIMUM PREDICTION AND ITS CONFIDENCE
    public Object[] argmax(float[] array){

        int best = -1;
        float best_confidence = 0.0f;
        for(int i = 0;i < array.length;i++){
            float value = array[i];
            if (value > best_confidence){
                best_confidence = value;
                best = i;
            }
        }
        return new Object[]{best,best_confidence};
    }

    @SuppressLint("StaticFieldLeak")
    public int process(final Bitmap bitmap) throws ExecutionException, InterruptedException {

        //Runs inference in background thread
         int result = new AsyncTask<Integer,Integer,Integer>(){

            @Override
            protected Integer doInBackground(Integer ...params){
                long inTime = System.currentTimeMillis();
                //Resize the image into 224 x 224 and rotate
                Bitmap resized_image = ImageUtils.processBitmap(bitmap,224);
                Bitmap rotated_image = ImageUtils.rotateBitmap(resized_image,90);

                //Normalize the pixels
                floatValues = ImageUtils.normalizeBitmap(rotated_image,224,127.5f,1.0f);

                //Pass input into the tensorflow
                tf.feed(INPUT_NAME,floatValues,1,224,224,3);

                //compute predictions
                tf.run(new String[]{model.OUTPUT_NAME});

                //copy the output into the PREDICTIONS array
                tf.fetch(model.OUTPUT_NAME,PREDICTIONS);

                long outTime = System.currentTimeMillis();
                TextView editText = findViewById(R.id.avgValue);
                //Obtained highest prediction
                Object[] results = argmax(PREDICTIONS);

                int class_index = (Integer) results[0];
                float confidence = (Float) results[1];
                Log.d("Success", "Prediction Result: " + confidence);
                if (confidence >= 0.75){
                    eventID = class_index;
                }else{
                    eventID = 0;
                }
//                try{
//                    String conf = String.valueOf(confidence * 100).substring(0,5);
//                    Convert predicted class index into actual label name
//                    situation = ImageUtils.getLabel(getAssets().open("labels.json"),class_index);
//
//                } catch (Exception e){
//                }
                return (int)(outTime - inTime);
            }

        }.execute(0).get();
         return result;
    }

    public TensorFlowInferenceInterface loadTFModel(AssetManager assetManager, String MODEL_PATH){
        TensorFlowInferenceInterface tf;
        long inTime = System.currentTimeMillis();
        tf = new TensorFlowInferenceInterface(assetManager,MODEL_PATH);
        long outTime = System.currentTimeMillis();
        TextView editText = findViewById(R.id.loadValue);
        String result = ((int)(outTime - inTime)) + "";
        editText.setText(result);
        editText = findViewById(R.id.ModelName);
        String[] temp = model.MODEL_PATH.split("/");
        result = temp[temp.length - 1];
        editText.setText(result);
        return tf;
    }

    // Monitor main function
    class Monitor extends Thread{

        int alertSender(int eventID, SoundPool soundPool, int[] sound, int signal) {
            final TextView editText = findViewById(R.id.Situation);
            int color = MainActivity.this.getResources().getColor(R.color.dangerDirve);
            // The most severe event should have the highest priority. currently this function is not functioning
            switch (eventID) {
                case 0:
                    color = MainActivity.this.getResources().getColor(R.color.safeDrive);
                    situation = "安全驾驶";
                    break;
                case 1:
                    signal = soundPool.play(sound[1], 1, 1, 0, 0, 1);
                    situation = "玩手机1";
                    break;
                case 2:
                    signal = soundPool.play(sound[2], 1, 1, 0, 0, 1);
                    situation = "打电话1";
                    break;
                case 3:
                    signal = soundPool.play(sound[0], 1, 1, 0, 0, 1);
                    situation = "玩手机2";
                    break;
                case 4:
                    signal = soundPool.play(sound[2], 1, 1, 0, 0, 1);
                    situation = "打电话2";
                    break;
                case 5:
                    signal = soundPool.play(sound[0], 1, 1, 0, 0, 1);
                    situation = "喝水";
            }
            Log.d("content", "alertSender: " + situation);
            final int innerColor = color;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //这里面进行UI的更新操作
                    editText.setText(situation);
                    editText.setTextColor(innerColor);
                }
            });
            return signal;
        }

        @Override
        public void run(){
            SoundPool soundPool = new SoundPool(5, AudioManager.STREAM_SYSTEM, 5);
            int[] sound = new int[3];
            int eventIdOld = 0;

            // put them in front of every thing after putting them into onResume().
            sound[0] = soundPool.load(MainActivity.this, R.raw.no_drink, 1);
            sound[1] = soundPool.load(MainActivity.this, R.raw.no_play, 1);
            sound[2] = soundPool.load(MainActivity.this, R.raw.no_call, 1);
            //wait for audio to load.
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (true) {
            try {
                    if (safeToTakePic && doIt) {
                        mCamera.takePicture(null, null, mPicture);
                        safeToTakePic = false;
                        if(eventID != eventIdOld){
                            if (eventID != 0){
                                soundPool.stop(signal);
                            }
                            eventIdOld = eventID;
                            signal = alertSender(eventID, soundPool, sound, signal);
                        }
                        Thread.sleep(1000);
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
        if (doIt == false){
            try{
                String displayText = "Average time consumed per round: " + statistic.getAvg();
                Toast.makeText(this, displayText, Toast.LENGTH_LONG).show();
            }catch (ArithmeticException e){
                Toast.makeText(this, R.string.warnOnQuickSwitch, Toast.LENGTH_LONG).show();
            }
            statistic.reset();
        }
    }
}


