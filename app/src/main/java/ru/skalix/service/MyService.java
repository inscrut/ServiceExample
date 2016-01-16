package ru.skalix.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
    public static String data = "";
    public static boolean updater = false;
    private String ip = "127.0.0.1";
    private int port = 2007;
    private int timer_delay = 300000; //5 min

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
                for (;;) {
                    if (updater) {
                        client = new TCPC(ip, port);
                        if (client.Connect() && client != null) {
                            Log.d("myLogs", "Connected to " + ip + ":" + port);
                            client.Send("GET");
                            data = client.Read();
                            client.Disconnect();
                            client = null;
                            updater = false;
                            refreshWidget();
                        } else {
                            Log.d("myLogs", "Failed connect!");
                            client = null;
                            data = "Fail!";
                            refreshWidget();
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
        data = intent.getStringExtra("DATA");
        Log.d("myLogs", "IP: " + ip);
        Log.d("myLogs", "PORT: " + port);
        Log.d("myLogs", "TIMER: " + timer_delay);
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
        refreshWidget();
        Toast.makeText(this, "Служба остановлена", Toast.LENGTH_SHORT).show();
        Log.d("myLogs", "Stop service.");
    }

    public void refreshWidget(){
        Intent i = new Intent(TestWidget.FORCE_WIDGET_UPDATE);
        sendBroadcast(i);
    }
}

class MTimerTask extends TimerTask {
    @Override
    public void run() {
        MyService.updater = true;
    }
}