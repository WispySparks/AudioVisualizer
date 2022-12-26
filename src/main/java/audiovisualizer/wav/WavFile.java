package audiovisualizer.wav;

public record WavFile(

    short audioFormat,
    short numChannels,
    int sampleRate,
    int byteRate,
    short blockAlign, 
    int bitsPerSample,
    byte[] data

) {
    
}
