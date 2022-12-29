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
    enum MPEGVersion {
        MPEG_1,
        MPEG_2,
        MPEG_2_5,
    }

    enum Layer {
        LAYER1,
        LAYER2,
        LAYER3,
    }

    enum Mode {
        STEREO,
        JOINT_STEREO,
        DUAL_CHANNEL,
        SINGLE_CHANNEL
    }

    enum Emphasis {
        NONE,
        MICROSECONDS_50_15,
        RESERVED,
        CCITT_J_17
    }
}
