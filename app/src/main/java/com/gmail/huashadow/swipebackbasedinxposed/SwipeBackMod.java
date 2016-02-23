package com.gmail.huashadow.swipebackbasedinxposed;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;

/**
 * Created by Administrator on 2016/2/19.
 */
public class SwipeBackMod implements IXposedHookZygoteInit {

    private static final String TAG = SwipeBackMod.class.getSimpleName();

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        Log.d(TAG, "initZygote");

        hookActivityRecord();
        hookActivityOnCreate();
        hookActivityOnPostCreate();
        hookActivityFindViewById();
    }

    private void hookActivityRecord() {
        try {
            Class<?> ar = findClass("com.android.server.am.ActivityRecord", null);
            if (ar != null) {
                XposedBridge.hookAllConstructors(ar, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam params) throws Throwable {
                        boolean isHome = false;
                        if (Build.VERSION.SDK_INT >= 19) {
                            isHome = (Boolean) callMethod(params.thisObject, "isHomeActivity");
                        } else {
                            isHome = getBooleanField(params.thisObject, "isHomeActivity");
                        }

                        if (!isHome) {
                            // fullscreen = false means transparent
                            setBooleanField(params.thisObject, "fullscreen", false);
                        }
                    }
                });
            }
        } catch (XposedHelpers.ClassNotFoundError e) {
        }
    }

    private void hookActivityOnCreate() {
        XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String className = param.thisObject.getClass().getSimpleName();
                Log.d(TAG, className + " onCreate beforeHookedMethod");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String className = param.thisObject.getClass().getSimpleName();
                Log.d(TAG, className + " onCreate afterHookedMethod");

                Activity activity = (Activity) (param.thisObject);
                String packageName = activity.getApplicationInfo().packageName;
                if (MiscUtils.isLauncher(activity, packageName)) {
                    Log.d(TAG, className + " onCreate afterHookedMethod is launcher skip");
                    return;
                }

                Log.d(TAG, className + " onCreate afterHookedMethod 1");
                SwipeBackActivityHelper helper = new SwipeBackActivityHelper(activity);
                Log.d(TAG, className + " onCreate afterHookedMethod 2");
                try {
                    helper.onActivityCreate();
                } catch (Exception e) {
                    Log.e(TAG, className + " onCreate afterHookedMethod", e);
                }

                Log.d(TAG, className + " onCreate afterHookedMethod 3");
                helper.getSwipeBackLayout().setEnableGesture(true);
                helper.getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
                helper.getSwipeBackLayout().setSensitivity(activity, 1);
                Log.d(TAG, className + " onCreate afterHookedMethod 4");
                Log.d(TAG, className + " onCreate afterHookedMethod 5");
                setAdditionalInstanceField(activity, "helper", helper);
                Log.d(TAG, className + " onCreate afterHookedMethod 6");
            }
        });
    }

    private void hookActivityOnPostCreate() {
        XposedHelpers.findAndHookMethod(Activity.class, "onPostCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                SwipeBackActivityHelper helper = (SwipeBackActivityHelper)
                        getAdditionalInstanceField(param.thisObject, "helper");
                Log.d(TAG, "onPostCreate");
                if (helper != null) {
                    helper.onPostCreate();
                    Log.d(TAG, "onPostCreate helper not null");
                }
            }
        });
    }

    private void hookActivityFindViewById() {
        findAndHookMethod(Activity.class, "findViewById", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam params) throws Throwable {
                if (params.getResult() == null) {
                    SwipeBackActivityHelper helper =
                            (SwipeBackActivityHelper) (getAdditionalInstanceField(params.thisObject, "helper"));
                    if (helper != null) {
                        params.setResult(helper.findViewById((Integer) params.args[0]));
                    }
                }
            }
        });
    }
}
