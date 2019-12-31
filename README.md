# HelloWorld
Android camera project

## Introduction
This is a android application aimed to run TensorFlow image recognization modles on mobile phone, to test their working efficiency and give quick report in real time on your phone screen.

## How to use
*This application works better on phones with SDK API Level21(Android 5.0)*
1. Download project and import all files into your Android Studio
2. Run dependency download
3. Goto folder name *assets* under path *app/main/src*, and put your modles in it. Per requirment from Google, file name should not contain underscore.
4. Go find approximately line 216, examples are below:
>     public modelUtils model = new modelUtils(modelUtils.ResNet50_PATH,"input_2","output_1","traditional",224);
change the things there accordingly, **all 5 parameters may needs changing**, you may feel free to add some constant value in **modelUtils**  class, in which I defined something to organize model utilization.

Parameter explaination: 
    public String MODEL_PATH;   //Path of model
    public String INPUT_NAME;   //Input layer name of model
    public String OUTPUT_NAME;  //Output layer name of model
    public int Resize_Para;     //Model's input img size  
    public String OUTPUT_TYPE;  //Output of model, see as **MainActicity.java line 569 Class Monitor**. It is advisible for you to make some change to it, to make it more handy for yourself.

5. Test your app as your wish.

**Actually, currently I have added file selection function, but I am still working on customlizing parameter part so I would not release it until it is fully operational  **

## Next step
if get vacant, I would  make it easier to adjust. like make modles more easier to remove or add.

## Contact
Feel free to contact me via email: lv4399@126.com, as you can see from my account, I am a Chinese developer.
