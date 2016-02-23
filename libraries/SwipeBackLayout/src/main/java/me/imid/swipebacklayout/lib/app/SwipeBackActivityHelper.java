package me.imid.swipebacklayout.lib.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;

import me.imid.swipebacklayout.lib.SwipeBackLayout;

/**
 * @author Yrom
 * @author PeterCxy
 */
public class SwipeBackActivityHelper {
    protected Activity mActivity;

    private SwipeBackLayout mSwipeBackLayout;
    private boolean hasSetBackground = false;

    public SwipeBackActivityHelper(Activity activity) {
        mActivity = activity;
    }

    @SuppressWarnings("deprecation")
    public void onActivityCreate() {
        mSwipeBackLayout = new SwipeBackLayout(mActivity, getGlobalContext());
        mActivity.getWindow().setFormat(PixelFormat.TRANSLUCENT);

        // Set background on swiped
        getSwipeBackLayout().addSwipeListener(new SwipeBackLayout.SwipeListener() {
            @Override
            public void onScrollStateChange(int state, float scrollPercent) {

            }

            @Override
            public void onEdgeTouch(int edgeFlag) {
                setBackground();
                Log.d("SwipeBackMod", "onEdgeTouch");
            }

            @Override
            public void onScrollOverThreshold() {

            }
        });
    }

    public void onPostCreate() {
        mSwipeBackLayout.attachToActivity(mActivity);
    }

    public void onFinish() {
        setBackground();
    }

    public View findViewById(int id) {
        if (mSwipeBackLayout != null) {
            return mSwipeBackLayout.findViewById(id);
        }
        return null;
    }

    public SwipeBackLayout getSwipeBackLayout() {
        return mSwipeBackLayout;
    }

    protected Context getGlobalContext() {
        try {
            return mActivity.createPackageContext("com.gmail.huashadow.swipebackbasedinxposed",
                    Context.CONTEXT_IGNORE_SECURITY);
        } catch (Exception e) {
            return mActivity;
        }
    }

    private void setBackground() {
        if (!hasSetBackground) {
            mActivity.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
            hasSetBackground = true;
        }
    }
}
