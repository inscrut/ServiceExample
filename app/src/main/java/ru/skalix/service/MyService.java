package ru.skalix.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
    static String data;
    static boolean trigger = true;
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

        client = new TCPC("127.0.0.1", 2007);
        conn = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (client != null && trigger) {
                        if (client.Connect()) {
                            Log.d("myLogs", "Connected!");
                            client.Send("GET");
                            data = client.Read();
                            client.Disconnect();
                            trigger = false;
                        } else {
                            Log.d("myLogs", "Failed connect!");
                        }
                    }
                }
            }
        });

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        conn.start();
        tmr.schedule(mtm, 0, 5000);
        Toast.makeText(this, "Служба запущена", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (client != null) client.Disconnect();
        conn.interrupt();
        tmr.cancel();
        Toast.makeText(this, "Служба остановлена", Toast.LENGTH_SHORT).show();
    }
}

class MTimerTask extends TimerTask {

    @Override
    public void run() {
        MyService.trigger = true;
    }
}