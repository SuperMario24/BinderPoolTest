package com.example.saber.binderpooltest;

import android.os.RemoteException;

/**
 * Created by saber on 2017/5/4.
 */

public class ComputeImpl extends ICompute.Stub {
    @Override
    public int add(int a, int b) throws RemoteException {
        return a + b;
    }
}
