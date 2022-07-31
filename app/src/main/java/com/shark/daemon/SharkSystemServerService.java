package com.shark.daemon;

import android.os.Build;
import android.os.IBinder;
import android.os.IServiceCallback;
import android.os.IServiceManager;
import android.os.RemoteException;
import android.util.Log;

import com.android.internal.os.BinderInternal;

import hidden.HiddenApiBridge;

public class SharkSystemServerService extends ISharkSystemServerService.Stub implements IBinder.DeathRecipient {
    private IBinder originService = null;
    public static final String PROXY_SERVICE_NAME = "serial";
    public final static String TAG = "SharkChilli";
    private int requested;


    public void putBinderForSystemServer() {
        android.os.ServiceManager.addService(PROXY_SERVICE_NAME, this);
        binderDied();
    }

    public SharkSystemServerService(int maxRetry) {
        Log.d(TAG, "SharkSystemServerService::SharkSystemServerService");
        requested = -maxRetry;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Registers a callback when system is registering an authentic "serial" service
            // And we are proxying all requests to that system service
            IServiceCallback serviceCallback = new IServiceCallback.Stub() {
                @Override
                public void onRegistration(String name, IBinder binder) {
                    Log.d(TAG, "SharkSystemServerService::SharkSystemServerService onRegistration: " + name + " " + binder);
                    if (name.equals(PROXY_SERVICE_NAME) && binder != null && binder != SharkSystemServerService.this) {
                        Log.d(TAG, "Register " + name + " " + binder);
                        originService = binder;
                        SharkSystemServerService.this.linkToDeath();
                    }
                }

                @Override
                public IBinder asBinder() {
                    return this;
                }
            };
            try {
                getSystemServiceManager().registerForNotifications(PROXY_SERVICE_NAME, serviceCallback);
            } catch (Throwable e) {
                Log.e(TAG, "unregister: ", e);
            }
        }
    }


    public static IServiceManager getSystemServiceManager() {
        return IServiceManager.Stub.asInterface(HiddenApiBridge.Binder_allowBlocking(BinderInternal.getContextObject()));
    }

    public void linkToDeath() {
        try {
            originService.linkToDeath(this, 0);
        } catch (Throwable e) {
            Log.e(TAG, "system server service: link to death", e);
        }
    }

    @Override
    public void binderDied() {
        if (originService != null) {
            originService.unlinkToDeath(this, 0);
            originService = null;
        }
    }

    @Override
    public void test() throws RemoteException {
        Log.i("SharkChilli", "SharkSystemServerService:test invoke................................................... ");
    }
}
