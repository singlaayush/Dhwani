package ai.ayushsingla.dhwani.audio;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ai.ayushsingla.dhwani.hotword.MsgEnum;
import ai.ayushsingla.dhwani.hotword.Constants;
import ai.ayushsingla.dhwani.SnowboyDetect;

public class RecordingThread {
    static {
        System.loadLibrary("snowboy-detect-android");
    }

    private static final String TAG = RecordingThread.class.getSimpleName();

    private static final String ACTIVE_RES = Constants.ACTIVE_RES;
    private static final String NAME_PMDL = Constants.NAME_PMDL;
    private static final String ALEXA_UMDL = Constants.ALEXA_UMDL;

    private boolean shouldContinue;
    private Handler handler = null;
    private Thread thread;

    private static String strEnvWorkSpace = Constants.DEFAULT_WORK_SPACE;
    private String activeModel = strEnvWorkSpace + NAME_PMDL;
    private String alexaModel = strEnvWorkSpace + ALEXA_UMDL;
    private String commonRes = strEnvWorkSpace + ACTIVE_RES;
    private SnowboyDetect detector;

    public RecordingThread(Handler handler, Context ctx) {
        this.handler = handler;
        File nameModel = new File(strEnvWorkSpace + NAME_PMDL);
        if (nameModel.exists()) {
            detector = new SnowboyDetect(commonRes, activeModel);
        } else {
            detector = new SnowboyDetect(commonRes, alexaModel);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String name_sensitivity = String.valueOf(prefs.getFloat("name_slider", (float) 0.48));

        Log.d("Name Sensitivity", name_sensitivity);

        detector.SetSensitivity(name_sensitivity); //DO NOT MODIFY UNLESS SPECIFIED
        detector.ApplyFrontend(true);
    }

    private void sendMessage(MsgEnum what, Object obj) {
        if (null != handler) {
            Message msg = handler.obtainMessage(what.ordinal(), obj);
            handler.sendMessage(msg);
        }
    }

    public void startRecording() {
        if (thread != null)
            return;

        shouldContinue = true;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                record();
            }
        });
        thread.start();
    }

    public void stopRecording() {
        if (thread == null)
            return;

        shouldContinue = false;
        thread = null;
    }

    private void record() {
        Log.v(TAG, "Start");
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        // Buffer size in bytes: for 0.1 second of audio
        int bufferSize = (int) (Constants.SAMPLE_RATE * 0.1 * 2);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = Constants.SAMPLE_RATE * 2;
        }

        byte[] audioBuffer = new byte[bufferSize];
        AudioRecord record = new AudioRecord(
                MediaRecorder.AudioSource.DEFAULT,
                Constants.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "Audio Record can't initialize!");
            return;
        }
        record.startRecording();
        Log.v(TAG, "Start recording");

        long shortsRead = 0;
        detector.Reset();
        while (shouldContinue) {
            record.read(audioBuffer, 0, audioBuffer.length);
            // Converts to short array.
            short[] audioData = new short[audioBuffer.length / 2];
            ByteBuffer.wrap(audioBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(audioData);

            shortsRead += audioData.length;

            // hotword detection.
            int result = detector.RunDetection(audioData, audioData.length);

            if (result == -2) {
                sendMessage(MsgEnum.MSG_VAD_NOSPEECH, null);
            } else if (result == -1) {
                sendMessage(MsgEnum.MSG_ERROR, "Unknown Detection Error");
            } else if (result == 0) {
                sendMessage(MsgEnum.MSG_VAD_SPEECH, null);
            } else if (result > 0) {
                sendMessage(MsgEnum.MSG_ACTIVE, result);
                Log.i("Dhwani", "Hotword " + Integer.toString(result) + " detected!");
            }
        }

        record.stop();
        record.release();
        Log.v(TAG, String.format("Recording stopped. Samples read: %d", shortsRead));
    }
}
