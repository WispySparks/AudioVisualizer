package audiovisualizer.mp3;

public record MP3Header(

    MPEGVersion version,
    Layer layer,
    boolean errorProtection,
    double bitrate,
    double samplingFrequency,
    boolean padded,
    Mode mode,
    int modeExtensionNumber,
    boolean intensityStereo,
    boolean msStereo,
    boolean copyrighted,
    boolean original,
    Emphasis emphasis
    
) {
    public enum MPEGVersion {
        MPEG_1,
        MPEG_2,
        MPEG_2_5,
    }

    public enum Layer {
        LAYER1,
        LAYER2,
        LAYER3,
    }

    public enum Mode {
        STEREO,
        JOINT_STEREO,
        DUAL_CHANNEL,
        SINGLE_CHANNEL
    }

    public enum Emphasis {
        NONE,
        MICROSECONDS_50_15,
        CCITT_J_17
    }
}
