package ru.skalix.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
    static String data = "Updating...";
    static boolean trigger = false;
    static String ip = "127.0.0.1";
    static int port = 2007;
    static int timer_delay = 300000;

    private TCPC client;
    private Thread conn;
    private Timer tmr;
    private MTimerTask mtm;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        tmr = new Timer();
        mtm = new MTimerTask();

        conn = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (trigger) {
                        client = new TCPC(ip, port);
                        if (client.Connect()) {
                            Log.d("myLogs", "Connected!");
                            client.Send("GET");
                            data = client.Read();
                            client.Disconnect();
                            client = null;
                            trigger = false;
                        } else {
                            Log.d("myLogs", "Failed connect!");
                            client = null;
                            data = "Fail!";
                        }
                    }
                }
            }
        });
        conn.start();
        tmr.schedule(mtm, 1000, timer_delay);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Служба запущена", Toast.LENGTH_SHORT).show();
        Log.d("myLogs", "Start service");
        ip = intent.getStringExtra("IP");
        port = intent.getIntExtra("PORT", 2007);
        timer_delay = intent.getIntExtra("TIMER", 300000);
        TestWidget.widget_delay = intent.getIntExtra("WTIMER", 300000);
        data = intent.getStringExtra("DATA");
        Log.d("myLogs", "IP: " + ip);
        Log.d("myLogs", "PORT: " + port);
        Log.d("myLogs", "TIMER: " + timer_delay);
        Log.d("myLogs", "WIDGET_UPDATE: " + TestWidget.widget_delay);
        Log.d("myLogs", "DATA: " + data);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (client != null) {
            client.Disconnect();
            client = null;
        }
        if (conn != null) {
            conn.interrupt();
            conn = null;
        }
        if (tmr != null) {
            tmr.cancel();
            tmr = null;
        }
        data = "OFF";
        Toast.makeText(this, "Служба остановлена", Toast.LENGTH_SHORT).show();
        Log.d("myLogs", "Stop service.");
    }
}

class MTimerTask extends TimerTask {

    @Override
    public void run() {
        MyService.trigger = true;
    }
}