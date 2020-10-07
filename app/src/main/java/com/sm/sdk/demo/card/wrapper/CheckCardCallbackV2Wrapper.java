package com.sm.sdk.demo.card.wrapper;

import android.os.Bundle;
import android.os.RemoteException;

import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;


public class CheckCardCallbackV2Wrapper extends CheckCardCallbackV2.Stub {
    @Override
    public void findMagCard(Bundle info) throws RemoteException {

    }

    @Override
    public void findICCard(String atr) throws RemoteException {

    }

    @Override
    public void findRFCard(String uuid) throws RemoteException {

    }

    @Override
    public void onError(int code, String message) throws RemoteException {

    }

    @Override
    public void findICCardEx(Bundle info) throws RemoteException {

    }

    @Override
    public void findRFCardEx(Bundle info) throws RemoteException {

    }

    @Override
    public void onErrorEx(Bundle info) throws RemoteException {

    }
}
