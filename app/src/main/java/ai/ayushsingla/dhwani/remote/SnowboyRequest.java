package ai.ayushsingla.dhwani.remote;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SnowboyRequest {

    @SerializedName("name")
    @Expose
    private String name = "unknown";
    @SerializedName("language")
    @Expose
    private String language = "en";
    @SerializedName("microphone")
    @Expose
    private String microphone = "phone microphone";
    @SerializedName("token")
    @Expose
    private String token = "e7725d1db40fe02915a6c3de39f409692d4a3935";
    @SerializedName("voice_samples")
    @Expose
    private List<VoiceSample> voiceSamples = null;

    public SnowboyRequest(String name, String token, List<VoiceSample> voiceSamples) {
        this.name = name;
        this.token = token;
        this.voiceSamples = voiceSamples;
    }

    public SnowboyRequest(List<VoiceSample> voiceSamples) {
        this.voiceSamples = voiceSamples;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setVoiceSamples(List<VoiceSample> voiceSamples) {
        this.voiceSamples = voiceSamples;
    }

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }

    public List<VoiceSample> getVoiceSamples() {
        return voiceSamples;
    }
}
