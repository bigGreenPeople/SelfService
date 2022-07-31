package com.shark.selfservice;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.ContextImpl;
import android.content.Context;
import android.ddm.DdmHandleAppName;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import com.shark.daemon.SharkSystemServerService;

public class MyService {
    public final static String TAG = "SharkChilli";
    private static SharkSystemServerService systemServerService = null;

    //运行命令:unshare -m sh -c "sh sharkdaemon $@&"
    //  app_process -Djava.class.path=app-debug.apk /data/local/tmp com.shark.selfservice.MyService

    //u:r:magisk:s0                  system         694     1 4596128 142512 SyS_epoll_wait      0 S lspd
    //u:r:magisk:s0                  root         10681     1 13764416 137520 SyS_epoll_wait     0 S app_process
    public static void main(String[] args) {
        System.out.println("开启");
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            Log.e(TAG, "Uncaught exception", e);
            System.exit(1);
        });
        Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
        Looper.prepareMainLooper();

        DdmHandleAppName.setAppName("sharkdaemon", 0);

//        Log.e(TAG, "getAppName："+ DdmHandleAppName.getAppName());

        ActivityThread activityThread = ActivityThread.systemMain();
//        ContextImpl systemContext = activityThread.getSystemContext();
//        ActivityManager mActivityManager = (ActivityManager) systemContext
//                .getSystemService(Context.ACTIVITY_SERVICE);
//        int pid = android.os.Process.myPid();

//        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
//                .getRunningAppProcesses()) {
//            if (appProcess.pid == pid) {
//                Log.i(TAG, "main: "+appProcess.processName);
//            }
//        }
        int systemServerMaxRetry = 1;
        systemServerService = new SharkSystemServerService(systemServerMaxRetry);
        systemServerService.putBinderForSystemServer();

        new Thread(() -> {
            int i = 0;
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.i(TAG, Process.myPid() + ":" + "MyService:运行中 " + i++);
            }
        }).start();
        Looper.loop();
        throw new RuntimeException("Main thread loop unexpectedly exited");
    }


    private static void waitSystemService(String name) {
        while (android.os.ServiceManager.getService(name) == null) {
            try {
                Log.i(TAG, "service " + name + " is not started, wait 1s.");
                //noinspection BusyWait
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.i(TAG, Log.getStackTraceString(e));
            }
        }
    }
}
