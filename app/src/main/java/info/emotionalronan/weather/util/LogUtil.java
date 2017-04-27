package info.emotionalronan.weather.util;

import android.util.Log;

/**
 * Created by YG on 2017/4/27.
 */

public class LogUtil {
    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int NOTHING = 6;
    public static  int level = VERBOSE;

    //这样 在程序上线  之前使用level  VERBOSE 就可以打印 log
    //程序上线后  将level 改为NOTHING  就可以屏蔽掉 log
    public static void setLevel(int level) {
        LogUtil.level = level;
    }

    public static void v(String tag,String msg){
        if(level <= VERBOSE){
            Log.v(tag,msg);
        }
    }
    public static void d(String tag,String msg){
        if(level <= DEBUG){
            Log.v(tag,msg);
        }
    }
    public static void i(String tag,String msg){
        if(level <= INFO){
            Log.v(tag,msg);
        }
    }
    public static void w(String tag,String msg){
        if(level <= WARN){
            Log.v(tag,msg);
        }
    }
    public static void e(String tag,String msg){
        if(level <= ERROR){
            Log.v(tag,msg);
        }
    }


}
