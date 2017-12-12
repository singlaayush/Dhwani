package ai.ayushsingla.dhwani.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import ai.ayushsingla.dhwani.R;
import ai.ayushsingla.dhwani.audio.WavAudioRecorder;

import ai.ayushsingla.dhwani.hotword.Constants;
import ai.ayushsingla.dhwani.remote.SnowboyClient;
import ai.ayushsingla.dhwani.remote.SnowboyRequest;
import ai.ayushsingla.dhwani.remote.VoiceSample;
import at.markushi.ui.CircleButton;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;

public class TrainActivity extends Activity {

    private Button btnNext, btnTest;
    private TextView textDisplay, recordNumber;
    private WavAudioRecorder mRecorder;
    private String base64wav1;
    private String base64wav2;
    private String base64wav3;
    private static String mRcordFilePath = Environment.getExternalStorageDirectory() + "/dhwani/samples";
    private int fileNumber = 1;
    public static Context context;
    public String state;
    public boolean fileState = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);
        context = getApplicationContext();

        recordNumber = (TextView) this.findViewById(R.id.recordNumber);
        textDisplay = (TextView) this.findViewById(R.id.textInstr);
        CircleButton btnControl = (CircleButton) this.findViewById(R.id.record);
        btnNext = (Button) this.findViewById(R.id.btnNext);
        btnTest = (Button) this.findViewById(R.id.btnTest);
        File directory = new File(mRcordFilePath);
        directory.mkdirs();

        btnNext.setVisibility(View.INVISIBLE);
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/lato.ttf");
        recordNumber.setTypeface(typeFace);
        textDisplay.setTypeface(typeFace);

        state = "START";

        recordNumber.setText(String.valueOf(fileNumber));
        textDisplay.setText("TAP TO " + state + " RECORDING");
        mRecorder = WavAudioRecorder.getInstance();

        btnControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (WavAudioRecorder.State.INITIALIZING == mRecorder.getState()) {
                    mRecorder.setOutputFile(mRcordFilePath + "/wav" + fileNumber + ".wav");
                    mRecorder.prepare();
                    mRecorder.start();
                    state = "STOP";
                    textDisplay.setText("TAP TO " + state + " RECORDING");
                    Toast.makeText(getApplicationContext(), "Say your name!", Toast.LENGTH_SHORT).show();
                } else if (WavAudioRecorder.State.ERROR == mRecorder.getState()) {
                    mRecorder.release();
                    mRecorder = WavAudioRecorder.getInstance();
                    mRecorder.setOutputFile(mRcordFilePath + "/wav" + fileNumber + ".wav");
                    state = "START";
                    textDisplay.setText(R.string.trainingRecordingErrorText);
                } else {
                    mRecorder.stop();
                    mRecorder.reset();
                    state = "START";
                    textDisplay.setText("TAP TO " + state + " RECORDING");
                    File recording = new File(mRcordFilePath + "/wav" + fileNumber + ".wav");
                    if (recording.exists()) {
                        btnNext.setVisibility(View.VISIBLE);
                    } else {
                        textDisplay.setText(R.string.trainingRecordingErrorText);
                    }
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                if (fileNumber < 3) {
                    fileNumber++;
                    recordNumber.setText(String.valueOf(fileNumber));
                    btnNext.setVisibility(View.INVISIBLE);
                } else {
                    btnNext.setVisibility(View.INVISIBLE);
                    btnTest.setVisibility(View.INVISIBLE);
                    recordNumber.setText("~");
                    textDisplay.setText(R.string.trainingPlaceholderTest);
                    sendNetworkRequest();
                }
            }
        });

        btnTest.setVisibility(View.INVISIBLE);
        btnTest.setActivated(false);
        /*
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnNext.setVisibility(View.INVISIBLE);
                btnTest.setVisibility(View.INVISIBLE);
                recordNumber.setText("~");
                textDisplay.setText(R.string.trainingPlaceholderTest);
                sendNetworkRequest();
            }
        });
        */
    }

    private void sendNetworkRequest() {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okclient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("https://snowboy.kitt.ai/")
                .client(okclient)
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();


        SnowboyClient client = retrofit.create(SnowboyClient.class);

        createWavBase64();

        //Log
        Log.d("wav1length", Integer.toString(base64wav1.length()));
        Log.d("wav2length", Integer.toString(base64wav2.length()));
        Log.d("wav3length", Integer.toString(base64wav3.length()));


        SnowboyRequest request = new SnowboyRequest(Arrays.asList(new VoiceSample(base64wav1), new VoiceSample(base64wav2), new VoiceSample(base64wav3)));

        //Log
        Gson gson = new Gson();
        String Json = gson.toJson(request);
        Log.d("Generated JSON String", Json);

        Call<ResponseBody> call = client.postPMDL(request);

        call.enqueue(new Callback<ResponseBody>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                Log.d("onResponse", response.toString());
                Log.d("onResponse isSuccessful", String.valueOf(response.isSuccessful()));
                Log.d("onResponse Message", response.message());
                if (response.isSuccessful()) {
                    Log.d("onResponse Method", "server contacted, and has file!");
                    Log.d("response content-type", response.body().contentType().type());
                    boolean success = writeResponseBodyToDisk(response.body());
                    Log.d("Writing PMDL File: ", "Is Successful? - " + success);
                    if (fileState) {
                        Intent intent = new Intent(context, MainActivity.class);
                        startActivity(intent);
                    } else {
                        recordNumber.setText("~");
                        textDisplay.setText(R.string.trainingFailureText);
                    }
                } else {
                    Log.e("onResponse Method: ", "server contact failed");
                    recordNumber.setText("~");
                    textDisplay.setText(R.string.trainingFailureText);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("onFailure", call.toString());
                Log.e("onFailure Method", "server contact failed", t);
                Toast.makeText(TrainActivity.this, "Error.", Toast.LENGTH_SHORT).show();
                recordNumber.setText("~");
                textDisplay.setText(R.string.trainingFailureText);
            }
        });
    }

    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            File trainedModelFile = new File(Constants.DEFAULT_WORK_SPACE + Constants.NAME_PMDL);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[1024];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(trainedModelFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();
                fileState = true;
                return true;
            } catch (IOException e) {
                Log.e("Inner Catch", "Error!", e);
                return fileState;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            Log.e("Outer Catch", "Error!", e);
            return fileState;
        }
    }

    private void createWavBase64() {
        File wav1 = new File(mRcordFilePath + "/wav1.wav");
        File wav2 = new File(mRcordFilePath + "/wav2.wav");
        File wav3 = new File(mRcordFilePath + "/wav3.wav");
        base64wav1 = getWavBase64(wav1);
        base64wav2 = getWavBase64(wav2);
        base64wav3 = getWavBase64(wav3);
    }

    private String getWavBase64(File file) {
        InputStream inputStream = null;//You can get an inputStream using any IO API
        try {
            inputStream = new FileInputStream(file.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            Log.e("Error", "File was not found");
        }
        byte[] buffer = new byte[104857600];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Base64OutputStream output64 = new Base64OutputStream(output, Base64.NO_WRAP);
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output64.write(buffer, 0, bytesRead);
            }
            output64.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("WAV: ", Integer.toString(output.toString().length()));
        return output.toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mRecorder) {
            mRecorder.release();
        }
    }
}