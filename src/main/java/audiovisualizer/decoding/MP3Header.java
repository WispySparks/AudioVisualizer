package audiovisualizer.decoding;

public record MP3Header(
    
) {
    enum MPEGVersion {
        MPEG_1,
        MPEG_2,
        MPEG_2_5,
        RESERVED
    }

    enum Layer {
        LAYER1,
        LAYER2,
        LAYER3,
        RESERVED
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
