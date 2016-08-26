package com.iamwent.tinter;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;

/**
 * Created by iamwent on 8/26/16.
 *
 * @author iamwent
 * @since 8/26/16
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "tag";

    protected abstract
    @LayoutRes
    int provideContentViewId();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(provideContentViewId());
        setTranslucentStatusBar();

    }

    private void setTranslucentStatusBar() {
        int sdkInt = Build.VERSION.SDK_INT;

        Log.d(TAG, "setTranslucentStatusBar: " + sdkInt);
        Log.d(TAG, "root id: " + R.id.root);

        if (sdkInt >= Build.VERSION_CODES.LOLLIPOP) {
            setTranslucentStatusBarLollipop();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setTranslucentStatusBarLollipop() {
        ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0).setFitsSystemWindows(true);
    }
}
