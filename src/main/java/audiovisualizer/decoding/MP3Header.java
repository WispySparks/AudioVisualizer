package audiovisualizer.decoding;

public record MP3Header(

    MPEGVersion version,
    Layer layer,
    boolean errorProtection,
    double bitrate,
    double samplingFrequency,
    boolean padded,
    Mode mode,
    boolean intensityStereo,
    boolean msStereo,
    JointStereoBands bands,
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

    enum JointStereoBands {
        BAND_4_31,
        BAND_8_31,
        BAND_12_31,
        BAND_16_31
    }

    enum Emphasis {
        NONE,
        MICROSECONDS_50_15,
        RESERVED,
        CCITT_J_17
    }
}
