package com.example.jaeho.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

public class VoiceThread extends HandlerThread {

    private static final String TAG = VoiceThread.class.getSimpleName();
    private Handler handler;
    private WeakReference<VoiceThreadCallback> callback;
    public VoiceThread() {
        super(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
    }

    public void setCallback(VoiceThreadCallback callback){
        this.callback = new WeakReference<>(callback);
    }

    // Get a reference to worker thread's handler after looper is prepared
    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        handler = new CustomHandler(getLooper());
    }

    // Used by UI thread to send a message to the worker thread's message queue
    public void addMessage(int message){
        if(handler != null) {
            handler.sendEmptyMessage(message);
        }
    }

    // Used by UI thread to send a runnable to the worker thread's message queue
    public void postRunnable(Runnable runnable){
        if(handler != null) {
            handler.post(runnable);
        }
    }

    private class CustomHandler extends Handler {
        CustomHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message message){
            super.handleMessage(message);
            Log.i("VoiceThread", String.format("%d - %d", Thread.currentThread().getId(), message.what));
            if(callback != null && callback.get() != null){
                Message newMessage = new Message();
                newMessage.what = message.what;
                Bundle bundle = new Bundle();
                bundle.putString("text", "OMFG");
                newMessage.setData(bundle);
                callback.get().onPublish(newMessage);
            }
//            switch (msg.what){
//                case 1:
//                    try {
//                        Thread.sleep(1000);
//                        if(!Thread.interrupted() && mUiThreadCallback != null && mUiThreadCallback.get() != null){
//                            Message message = Util.createMessage(Util.MESSAGE_ID, "Thread " + String.valueOf(Thread.currentThread().getId()) + " completed");
//                            mUiThreadCallback.get().publishToUiThread(message);
//                        }
//                    } catch (InterruptedException e){
//                        Log.e(Util.LOG_TAG,"HandlerThread interrupted");
//                    }
//                    break;
//                default:
//                    break;
//            }
        }
    }

    public interface VoiceThreadCallback {
        void onPublish(Message message);
    }
}
