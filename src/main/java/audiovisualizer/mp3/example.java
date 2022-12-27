package audiovisualizer.mp3;

import java.io.FileInputStream;

public class example { // Example MP1 Decoder //! from jonolick.com public domain 

    private final int channels = 1;

    public int readBits(FileInputStream stream, int amount) {
        return 1;
    }

    //? Read bit allocations
    public int[][] dummyReadBitAllocations(FileInputStream stream) {
        int mode = 1;
        int amount = 4;
        int modeExtension = 0;
        int bound = mode == 1 ? (modeExtension+1)*4 : 32;
        int[][] bitAlloc = new int[32][2];
        for (int i = 0; i < bound; ++i) {
            for (int ch = 0; ch < channels; ++ch) {
                bitAlloc[i][ch] = readBits(stream, amount);
            }
        }
        for (int i = bound; i < 32; ++i) {
            bitAlloc[i][1] = bitAlloc[i][0] = readBits(stream, amount);
        }
        return bitAlloc;
    }

    //? Read scale indexes
    public int[][] dummyReadScaleIndexes(FileInputStream stream) {
        int[][] bitAlloc = dummyReadBitAllocations(stream);
        int amount = 6;
        int[][] scaleIdx = new int[32][2];
        for (int i = 0; i < 32; ++i) {
            for (int ch = 0; ch < channels; ++ch) {
                scaleIdx[i][ch] = bitAlloc[i][ch] == 1 /* not sure on this one */ ? readBits(stream, amount) : 63;
            }
        }
        return scaleIdx;
    }

}
