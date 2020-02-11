package com.lzy.lib.rxevent;

import android.app.Activity;

/**
 * desc: Activity工具 <br/>
 * time: 2017/9/11 下午3:18 <br/>
 * author: 居廉 <br/>
 * since V 1.p <br/>
 */
public class ToolActivity {

    /**
     * Activity 是否Activity生命周期结束
     *
     * @param activity activity
     * @return true：已经结束
     */
    public static boolean isActivityFinishing(Activity activity) {
        return (activity == null || activity.isDestroyed());
    }

}
