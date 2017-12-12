package ai.ayushsingla.dhwani.remote;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VoiceSample {

    @SerializedName("wave")
    @Expose
    private String wave;

    public VoiceSample(String wave) {
        this.wave = wave;
    }

    public String getWave() {
        return wave;
    }

    public void setWave(String wave) {
        this.wave = wave;
    }

}