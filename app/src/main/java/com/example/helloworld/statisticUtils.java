package com.example.helloworld;

public class statisticUtils {
    private int totalDuration;
    private int runTimes;

    public statisticUtils(){
        int totalDuration = 0;
        int runTimes = 0;
    }

    public void push(int time){
        totalDuration += time;
        runTimes = runTimes + 1;
    }

    public int getAvg(){
        return totalDuration / runTimes;
    }

    public void reset(){
        totalDuration = 0;
        runTimes = 0;
    }

    public int getRunTimes(){
        return runTimes;
    }

}
