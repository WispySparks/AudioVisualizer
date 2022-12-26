package audiovisualizer.mp3;

public class example { // Example MP1 Decoder
    /* 
    ! from jonolick.com public domain
    ? Read bit allocations
    int bitAlloc[32][2] = {0};
    for(int i = 0; i < bound; ++i) {
        for(int ch = 0; ch < channels; ++ch) {
            bitAlloc[i][ch] = jo_readBits(data,at,4);
        }
    }
    for(int i = bound; i < 32; ++i) {
        bitAlloc[i][1] = bitAlloc[i][0] = jo_readBits(data,at,4);
    }

    ? Read scale indexes
    int scaleIdx[32][2];
    for(int i = 0; i < 32; ++i) {
        for(int ch = 0; ch < channels; ++ch) {
            scaleIdx[i][ch] = bitAlloc[i][ch] ? jo_readBits(data,at,6) : 63;
        }
    }

    ? Read & compute output samples
    short pcm[12][2][32];
    for(int s = 0; s < 12; ++s) {
        ? Read normalized, quantized band samples
        int samples[32][2] = {0};
        for(int i = 0; i < bound; ++i) {
            for(int ch = 0; ch < channels; ++ch) {
                if(bitAlloc[i][ch]) {
                    samples[i][ch] = jo_readBits(data,at,bitAlloc[i][ch]+1);
                }
            }
        }
        for(int i = bound; i < 32; ++i) {
            if(bitAlloc[i][0]) {
                samples[i][1] = samples[i][0] = jo_readBits(data,at,bitAlloc[i][0]+1);
            }
        }
        ? Compute bands: Dequantize & Denormalize
        double bandTbl[2][32] = {0};
        for(int i = 0; i < 32; ++i) {
            for(int ch = 0; ch < channels; ++ch) {
                int b = bitAlloc[i][ch];
                if(b++) {
                    int samp = samples[i][ch];
                    double f = ((samp >> b-1) & 1) ? 0 : -1;
                    f += (samp & ((1<<b-1)-1)) / (double)(1<<b-1);
                    f = (f+1.0/(1<<b-1)) * (1<<b)/((1<<b)-1.0);
                    f *= s_jo_multTbl[scaleIdx[i][ch]];
                    bandTbl[ch][i] = f;
                }
            }
        }
        ? Convert subbands to PCM
        for(int ch = 0; ch < channels; ++ch) {
            bufOffset[ch] = (bufOffset[ch] + 0x3C0) & 0x3ff;
            double *bufOffsetPtr = buf[ch] + bufOffset[ch];
            const double *f = s_jo_filterTbl[0];
            for(int i = 0; i < 64; ++i) {
                double sum = 0;
                for(int j = 0; j < 32; ++j) {
                    sum += *f++ * bandTbl[ch][j];
                }
                bufOffsetPtr[i] = sum;
            }
            const double *w = s_jo_windowTbl;
            for(int i = 0; i < 32; ++i) {
                double sum = 0;
                for(int j = 0; j < 16; ++j) {
                    int k = i | (j + (j+1 & -2)) << 5;
                    sum += *w++ * buf[ch][(k + bufOffset[ch]) & 0x3ff];
                }
                int ss = int(sum * 0x8000);
                ss = ss > SHRT_MAX ? SHRT_MAX : ss < SHRT_MIN ? SHRT_MIN : ss;
                pcm[s][ch][i] = ss;
            }
        }
    }
     */
}
