package ai.ayushsingla.dhwani.audio;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import ai.ayushsingla.dhwani.activities.MainActivity;
import ai.ayushsingla.dhwani.R;
import ai.ayushsingla.dhwani.hotword.MsgEnum;
import ai.ayushsingla.dhwani.miscellaneous.DismissButtonReceiver;

import static android.app.Notification.PRIORITY_MAX;
import static android.content.ContentValues.TAG;

public class RecordingService extends Service {

    public static int NOTIFICATION_ID = 780;
    public static int NOTIFICATION_ID_TWO = 880;
    private RecordingThread recordingThread = null;
    private Vibrator vibrator = null;
    private Intent intent;
    private NotificationManager notificationManager;
    public static boolean on = false;

    @Override
    public void onCreate() {
        recordingThread = new RecordingThread(handle, this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        on = true;
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_alert",
                    "Dhwani Channel",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        startRecording();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Dhwani")
                .setContentText("Running to detect name...")
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID_TWO, notification);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("HandlerLeak")
    public Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            MsgEnum message = MsgEnum.getMsgEnum(msg.what);
            switch (message) {
                case MSG_ACTIVE:
                    notificationManager.notify(NOTIFICATION_ID, nameCalledNotification(getApplicationContext(), MainActivity.class));
                    vibrator.vibrate(1000);
                    Log.i(TAG, msg.toString());
                    break;
                case MSG_RECORD_START:
                    break;
                case MSG_INFO:
                    break;
                case MSG_VAD_SPEECH:
                    Log.i(TAG, String.valueOf(msg.obj));
                    break;
                case MSG_VAD_NOSPEECH:
                    Log.i(TAG, String.valueOf(msg.obj));
                    break;
                case MSG_ERROR:
                    Log.e(TAG, msg.toString());
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    public Notification nameCalledNotification(Context context, Class<?> home) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Someone just called out your name!")
                .setChannelId("channel_alert")
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentText("Take a look around!");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        } else {
            mBuilder.setPriority(PRIORITY_MAX);
        }


        Intent resultIntent = new Intent(context, home);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(home);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Intent buttonIntent = new Intent(this, DismissButtonReceiver.class);
        buttonIntent.putExtra("notificationId", NOTIFICATION_ID);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 0, buttonIntent, 0);

        mBuilder.setContentIntent(resultPendingIntent)
                .addAction(R.mipmap.ic_launcher, "Dismiss", dismissPendingIntent)
                .setAutoCancel(true);
        Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        return notification;
    }

    private void startRecording() {
        recordingThread.startRecording();
        Log.i(TAG, "Recording started.");
    }

    private void stopRecording() {
        recordingThread.stopRecording();
        Log.i(TAG, "Recording stopped.");
    }

    @Override
    public void onDestroy() {
        stopRecording();
        on = false;
        super.onDestroy();
    }
}
