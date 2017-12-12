package ai.ayushsingla.dhwani.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import ai.ayushsingla.dhwani.R;
import ai.ayushsingla.dhwani.audio.RecordingService;
import ai.ayushsingla.dhwani.hotword.AppResCopy;

public class MainActivity extends AppCompatActivity {
    Drawable deton, detoff;
    ImageButton detection, training;
    Intent intent, serviceIntent;

    @SuppressLint("StaticFieldLeak")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAllPermissions();
        intent = new Intent(this, TrainActivity.class);
        serviceIntent = new Intent(this, RecordingService.class);
        setContentView(R.layout.activity_main);
        detection = (ImageButton) this.findViewById(R.id.detection);
        training = (ImageButton) this.findViewById(R.id.training);
        deton = getResources().getDrawable(R.drawable.deton);
        detoff = getResources().getDrawable(R.drawable.detoff);

        AppResCopy.copyResFromAssetsToSD(this);

        if (RecordingService.on) {
            detection.setImageDrawable(deton);
        } else {
            detection.setImageDrawable(detoff);
        }
        detection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (detection.getDrawable().equals(deton)) {
                    detection.setImageDrawable(detoff);
                    stopService(serviceIntent);
                } else {
                    detection.setImageDrawable(deton);
                    startService(serviceIntent);
                }
            }
        });
        training.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void checkAllPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        }
        checkAudioPermissions();
        checkCameraPermissions();
        checkStoragePermissions();
    }

    public void checkAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 5);
        }
    }

    public void checkStoragePermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 6);
        }
    }

    public void checkCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 7);
        }
    }
}