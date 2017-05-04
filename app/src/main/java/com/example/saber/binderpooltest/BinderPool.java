package com.example.saber.binderpooltest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.concurrent.CountDownLatch;

/**
 * Created by saber on 2017/5/4.
 * Binder连接池的具体实现
 * 1.绑定远程service
 * 2.客户端通过queryBinder获取对应的Binder
 */

public class BinderPool {

    private static final String TAG = "BinderPool";

    public static final int BINDER_NONE = -1;
    public static final int BINDER_COMPUTE = 0;
    public static final int BINDER_SECURITY_CENTER = 1;

    private Context mContext;
    private IBinderPool mBinderPool;
    private static volatile BinderPool sInstance;
    private CountDownLatch mConnectBinderPoolCountDownLatch;

    private BinderPool(Context context){
        mContext = context.getApplicationContext();
        connectBinderPoolService();
    }

    public static BinderPool getInstance(Context context){
        if(sInstance == null){
            synchronized (BinderPool.class){
                if(sInstance == null){
                    sInstance = new BinderPool(context);
                }
            }
        }
        return sInstance;
    }


    private synchronized void connectBinderPoolService() {
        mConnectBinderPoolCountDownLatch = new CountDownLatch(1);
        Intent service = new Intent(mContext,BinderPoolService.class);
        mContext.bindService(service,mBinderPoolConnection,Context.BIND_AUTO_CREATE);
        try {
            mConnectBinderPoolCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * query binder by binderCode from binder pool
     */
    public IBinder queryBinder(int binderCode){
        IBinder binder = null;
        try {
            if(mBinderPool != null){
                binder = mBinderPool.queryBinder(binderCode);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return binder;
    }

    private ServiceConnection mBinderPoolConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinderPool = IBinderPool.Stub.asInterface(service);
            //设置死亡代理
            try {
                mBinderPool.asBinder().linkToDeath(mBinderPoolDeathRecipient,0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mConnectBinderPoolCountDownLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //ignored
        }
    };

    /**
     * 设置死亡代理
     */
    private IBinder.DeathRecipient mBinderPoolDeathRecipient = new IBinder.DeathRecipient(){

        @Override
        public void binderDied() {
            Log.w(TAG,"binder died.");
            //置空
            mBinderPool.asBinder().unlinkToDeath(mBinderPoolDeathRecipient,0);
            mBinderPool = null;
            //再次连接
            connectBinderPoolService();
        }
    };


    /**
     * AIDL接口的实现类
     */
    public static class BinderPoolImpl extends IBinderPool.Stub{

        public BinderPoolImpl(){
            super();
        }

        @Override
        public IBinder queryBinder(int binderCode) throws RemoteException {
            IBinder binder = null;
            switch (binderCode){
                case BINDER_SECURITY_CENTER:
                    binder = new SecurityCenterImpl();
                    break;
                case BINDER_COMPUTE:
                    binder = new ComputeImpl();
                    break;
            }

            return binder;
        }
    }

}
