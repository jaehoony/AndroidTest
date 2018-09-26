package com.example.jaeho.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.lang.ref.WeakReference;

//https://github.com/frank-tan/AndroidMultithreadingBlogs
public class MainActivity extends AppCompatActivity implements VoiceThread.VoiceThreadCallback {

    private UiHandler uiHandler;
    private VoiceThread voiceThread;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textView = findViewById(R.id.textView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                voiceThread.addMessage(1);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Initialize the handler for UI thread to handle message from worker threads
        uiHandler = new UiHandler(Looper.getMainLooper(), textView);

        // create and start a new HandlerThread worker thread
        voiceThread = new VoiceThread();
        voiceThread.setCallback(this);
        voiceThread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // clear the message queue of HandlerThread worker thread and stop the current task
        if(voiceThread != null){
            voiceThread.quit();
            voiceThread.interrupt();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPublish(Message message) {
        // add the message from worker thread to UI thread's message queue
        if(uiHandler != null){
            Log.i("MainActivity", String.format("%d - %d", Thread.currentThread().getId(), message.what));
            uiHandler.sendMessage(message);
        }
    }

    private class UiHandler extends Handler {
        private WeakReference<TextView> textView;

        UiHandler(Looper looper, TextView textView){
            super(looper);
            this.textView = new WeakReference<>(textView);
        }

        // This method will run on UI thread
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(textView != null && textView.get() != null) {
                String text = msg.getData().getString("text");
                textView.get().setText(text);
            }
        }
    }
}
