package com.example.helloworld;

public class modelUtils {
    // deprecated static variables
    public static final String VGG_PATH = "file:///android_asset/vgg16net.pb";
    public static final String Google_PATH = "file:///android_asset/GoogleNet.pb";
    public static final String ICPv2_PATH = "file:///android_asset/InceptionV2.pb";
    public static final String ResNet50_PATH = "file:///android_asset/Resnet50.pb";

    public String MODEL_PATH;   //模型地址
    public String INPUT_NAME;   //模型输入层名字
    public String OUTPUT_NAME;  //模型输出层名字
    public int Resize_Para;     //模型输入图像大小
    public String OUTPUT_TYPE;  //模型是四输出还是六输出（左右手是否分别输出），可用值为 traditional（6） new（4），参见MainActicity.java 569行 Monitor 类

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



