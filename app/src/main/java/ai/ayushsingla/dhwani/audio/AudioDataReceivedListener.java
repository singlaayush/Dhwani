package ai.ayushsingla.dhwani.audio;

public interface AudioDataReceivedListener {
    void start();

    void onAudioDataReceived(byte[] data, int length);

    void stop();
}