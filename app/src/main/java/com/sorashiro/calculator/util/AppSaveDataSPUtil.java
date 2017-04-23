package com.sorashiro.calculator.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Sora
 * @date 2016/11/7
 * <p>
 * A class to use SharedPreferences conveniently.
 * MUST call init() BEFORE call other methods!
 * 一个方便使用SharedPreferences的类。
 * 调用其他方法前，请务必先调用init()方法！
 */

public class AppSaveDataSPUtil {

    private static Context sContext;

    //主目录
    private static final String DATA_CONFIG = "data_config";

    //是否自动计算
    private static final String IF_A_C = "if_auto_calc";

    private static SharedPreferences        sSharedPreferences;
    private static SharedPreferences.Editor sEditor;

    private AppSaveDataSPUtil() {
    }

    //修改或获得数据前必须先调用该函数
    public static void init(Context context) {
        if (sContext != null) {
            sContext = null;
        }
        sContext = context;
        sSharedPreferences = context.getSharedPreferences(DATA_CONFIG, Context.MODE_PRIVATE);
        sEditor = sSharedPreferences.edit();
        sEditor.commit();
    }

    public static void setIfAutoCalc(boolean ifAutoCalc) {
        sEditor.putBoolean(IF_A_C, ifAutoCalc);
        sEditor.commit();
    }

    public static boolean getIfAutoCalc() {
        return sSharedPreferences.getBoolean(IF_A_C, true);
    }

}
