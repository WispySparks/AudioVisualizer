package audiovisualizer.wav;

public record Wav(

    short audioFormat,
    short numChannels,
    int sampleRate,
    int byteRate,
    short blockAlign, 
    int bitsPerSample,
    byte[] data

) {
    
}
