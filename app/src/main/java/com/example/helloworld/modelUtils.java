package com.example.helloworld;

public class modelUtils {
    // deprecated static variables
    public static final String VGG_PATH = "file:///android_asset/vgg16net.pb";
    public static final String Google_PATH = "file:///android_asset/GoogleNet.pb";
    public static final String ICPv2_PATH = "file:///android_asset/InceptionV2.pb";
    public static final String ResNet50_PATH = "file:///android_asset/Resnet50.pb";

    public String MODEL_PATH;
    public String INPUT_NAME;
    public String OUTPUT_NAME;
    public int Resize_Para;
    public String OUTPUT_TYPE;

    public modelUtils(String MODEL_PATH,String INPUT_NAME,String OUTPUT_NAME,String OUTPUT_TYPE, int Resize_Para){
        this.MODEL_PATH = MODEL_PATH;
        this.INPUT_NAME = INPUT_NAME;
        this.OUTPUT_NAME = OUTPUT_NAME;
        this.Resize_Para = Resize_Para;
        this.OUTPUT_TYPE = OUTPUT_TYPE;
    }

    public void setModel(String INPUT_NAME,String OUTPUT_NAME,String OUTPUT_TYPE, int Resize_Para){
        this.INPUT_NAME = INPUT_NAME;
        this.OUTPUT_NAME = OUTPUT_NAME;
        this.Resize_Para = Resize_Para;
        this.OUTPUT_TYPE = OUTPUT_TYPE;
    }
}



