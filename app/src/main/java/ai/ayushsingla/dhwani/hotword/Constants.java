package ai.ayushsingla.dhwani.hotword;

import java.io.File;

import android.os.Environment;

public class Constants {
    public static final String ASSETS_RES_DIR = "dhwani";
    public static final String DEFAULT_WORK_SPACE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dhwani/";
    public static final String ALEXA_UMDL = "alexa.umdl";
    public static final String NAME_PMDL = "trainedModel.pmdl";
    public static final String ACTIVE_RES = "common.res";
    public static final String SAVE_AUDIO = Constants.DEFAULT_WORK_SPACE + File.separatorChar + "recording.pcm";
    public static final int SAMPLE_RATE = 16000;
}
