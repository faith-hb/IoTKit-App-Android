package com.cylan.jiafeigou.support.stat;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.tencent.stat.StatConfig;
import com.tencent.stat.StatService;

import java.util.Properties;

/**
 * Created by hunt on 16-4-5.
 */
public class MtaManager {

    private static boolean debug = false;

    public static void init(Context context, boolean debug) {
        MtaManager.debug = debug;
        Log.d("MtaManager", "MtaManager: " + StatConfig.getAppKey(context));
    }

    /**
     * onResume and onStop must be called .
     *
     * @param context
     */
    public static void onResume(Context context) {
        StatService.onResume(context);
    }

    public static void onPause(Context context) {
        StatService.onPause(context);
    }


    /**
     * 统计按钮被点击次数，统计对象：value 按钮
     *
     * @param context
     * @param eventId
     * @param value
     */
    public static void eventClick(final Context context, final String eventId, final String value) {
        if (debug)
            return;
        sendEvent(new Runnable() {
            @Override
            public void run() {
                Properties properties = new Properties();
                properties.setProperty(eventId, value);
                StatService.trackCustomKVEvent(context, eventId, properties);
            }
        });
    }

    public static void eventClick(Context context, String eventId) {
        if (debug)
            return;
        eventClick(context, eventId, "");
    }

    public static void customEvent(final Context context, final String eventId, final String value) {
        if (debug)
            return;
        sendEvent(new Runnable() {
            @Override
            public void run() {
                StatService.trackCustomEvent(context, eventId, value);
            }
        });

    }

    public static void customEvent(Context context, String eventId) {
        if (debug)
            return;
        customEvent(context, eventId, "");
    }

    private static Handler handler;

    static {
        HandlerThread thread = new HandlerThread("custom-mta");
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    private static void sendEvent(Runnable runnable) {
        handler.post(runnable);
    }
}