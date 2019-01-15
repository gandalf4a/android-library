package org.ironman.framework;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Looper;

import org.ironman.framework.util.LogUtil;
import org.ironman.framework.util.Singleton;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.WeakReference;

public final class JEnvironment {

    private static final String TAG = JEnvironment.class.getSimpleName();
    private static final String DEV_NULL = "/dev/null";

    private static final Singleton<Application> sApplication = new Singleton<Application>() {
        @Override
        protected Application create() {
            if (Looper.getMainLooper() == null) {
                Looper.prepareMainLooper();
            }
            if (ActivityThread.currentActivityThread() == null) {
                runQuietly(new Runnable() {
                    @Override
                    public void run() {
                        ActivityThread.systemMain();
                    }
                });
            }
            return ActivityThread.currentApplication();
        }
    };

    public static Application getApplication() {
        return sApplication.get();
    }

    public static PackageManager getPackageManager() {
        return getApplication().getPackageManager();
    }

    public static ActivityManager getActivityManager() {
        return getSystemService(Context.ACTIVITY_SERVICE);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getSystemService(String name) {
        return (T) JEnvironment.getApplication().getSystemService(name);
    }

    private static void runQuietly(Runnable runnable) {
        PrintStream out = System.out;
        PrintStream err = System.err;
        FileOutputStream os = null;
        PrintStream ps = null;

        try {
            try {
                os = new FileOutputStream(DEV_NULL);
                ps = new PrintStream(os);
                System.setOut(ps);
                System.setErr(ps);
            } catch (Exception e) {
                LogUtil.printErrStackTrace(TAG, e, null);
            }

            runnable.run();

        } finally {
            if (out != System.out) {
                System.setOut(out);
            }
            if (err != System.err) {
                System.setErr(err);
            }
            if (ps != null) {
                closeQuietly(ps);
            }
            if (os != null) {
                closeQuietly(os);
            }
        }
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            LogUtil.printErrStackTrace(TAG, e, null);
        }
    }
}