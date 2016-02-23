package com.gmail.huashadow.swipebackbasedinxposed;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

/**
 * Created by Administrator on 2016/2/22.
 */
public class MiscUtils {
    public static boolean isLauncher(Context context, String packageName) {
        ActivityInfo homeInfo = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).resolveActivityInfo(context.getPackageManager(), 0);
        if (homeInfo != null) {
            return homeInfo.packageName.equals(packageName);
        } else {
            return false;
        }
    }
}
