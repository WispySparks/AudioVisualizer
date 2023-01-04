package audiovisualizer.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioMagician {

    public static final double windowSeconds = 0.4;

    /**
     * 
     * @param pcm data
     * @param samplingFrequency in Hz
     * @param bitsPerSample number of bits that makes up a sample
     * @param numChannels number of channels in pcm
     * @return Audio volume of windows of time relative to each other (sort of decibels), each value represents 400 ms of audio. 
     * The last value might represent less than 400 ms if the pcm doesn't divide evenly into the pcm length.
     */
    public double[] getAudioVolume(byte[] pcm, double samplingFrequency, int bitsPerSample, int numChannels) {
        // 400 ms window
        final int windowSize = (int) (samplingFrequency * windowSeconds); // window length in samples
        int[][] parsedPCM = parsePCMData(pcm, bitsPerSample, numChannels, ByteOrder.LITTLE_ENDIAN);
        double[][] weightedPCM = applyAWeighting(parsedPCM);
        int numWindows = weightedPCM[0].length / windowSize; // number of 400 ms windows = number of samples in a channel / window size
        int remainder = weightedPCM[0].length % windowSize;
        if (remainder != 0) numWindows++; // add another window if it doesn't divide perfectly
        double[][] rmsNums = new double[numWindows][windowSize]; // root mean square numbers
        double[] decibels = new double[numWindows]; // volume numbers
        // go through every window of time and grab the number of samples in that window and then take the rms of those samples
        for (int currentWindow = 0; currentWindow < numWindows; currentWindow++) { 
            for (int currentSample = 0; currentSample < windowSize; currentSample++) {
                int offset = currentWindow * windowSize; // this is how many windows we've already gone by in terms of samples
                double val = 0;
                // also average out the current sample across all channels
                for (int channel = 0; channel < numChannels; channel++) {
                    if (currentSample + offset >= weightedPCM[channel].length) continue; // This is for when we're on remainder window
                    val += weightedPCM[channel][currentSample + offset];
                }
                val /= numChannels;
                rmsNums[currentWindow][currentSample] = val;
            }
        }
        for (int i = 0; i < numWindows; i++) {
            decibels[i] = rootMeanSquare(rmsNums[i]);
        }
        for (int i = 0; i < decibels.length; i++) {
            if (decibels[i] == 0) continue;
            decibels[i] = 20 * Math.log10(decibels[i]) + 2;
        }
        return decibels;
    }

    /**
     * Parses raw PCM data into samples based on the number of channels and bits per sample specified. Supports 8, 16, and 32 bits per sample.
     * @param pcm data
     * @param bitsPerSample bits for each value of a sample
     * @param numChannels number of channels pcm data is broken up into. 1 = mono, ect.
     * @return A list of channels that in turn are list's of the samples
     */
    public int[][] parsePCMData(byte[] pcm, int bitsPerSample, int numChannels, ByteOrder order) {
        if (order == null) throw new IllegalArgumentException("ByteOrder is null.");
        if (numChannels < 1) throw new IllegalArgumentException("Number of channels is < 1.");
        int bytesPerSample = bitsPerSample / 8;
        int samples = pcm.length / numChannels / bytesPerSample;
        int[][] parsedPCM = new int[numChannels][samples];
        int pos = 0;
        for (int sample = 0; sample < samples; sample++) {
            for (int channel = 0; channel < numChannels; channel++) {
                ByteBuffer buffer = ByteBuffer.wrap(getNBytes(pcm, pos, bytesPerSample));
                buffer = (order == ByteOrder.LITTLE_ENDIAN) ? buffer.order(ByteOrder.LITTLE_ENDIAN) : buffer.order(ByteOrder.BIG_ENDIAN);
                int currentSample = switch(bytesPerSample) {
                    case 1 -> buffer.get();
                    case 2 -> buffer.getShort();
                    case 3 -> {
                        short s = buffer.getShort();
                        byte b = buffer.get();
                        yield (b << 16) | s;
                    }
                    case 4 -> buffer.getInt();
                    default -> throw new IllegalArgumentException("Unsupported audio format has " + bytesPerSample + " bytes per sample. Only support 1, 2, 3, and 4.");
                };
                parsedPCM[channel][sample] = currentSample;
                pos += bytesPerSample;
            }
        }
        return parsedPCM;
    }

    private double[][] applyAWeighting(int[][] pcm) {
        double[][] aWeighted = new double[pcm.length][pcm[0].length];
        for (int i = 0; i < pcm.length; i++) {
            for (int j = 0; j < pcm[i].length; j++) {
                aWeighted[i][j] = aWeight(pcm[i][j]);
            }
        } 
        return aWeighted;
    }

    /**
     * {@link} https://en.wikipedia.org/wiki/A-weighting#A
     */
    private double aWeight(double f) {
        double y = (Math.pow(12194, 2) * Math.pow(f, 4)) /
        ( ( Math.pow(f, 2) + Math.pow(20.6, 2) ) * 
        Math.sqrt( (Math.pow(f, 2) + Math.pow(107.7, 2)) * (Math.pow(f, 2) + Math.pow(737.9, 2)) ) * 
        ( Math.pow(f, 2) + Math.pow(12194, 2) ) );
        return y;
    }

    private double rootMeanSquare(double[] numbers) {
        if (numbers == null || numbers.length <= 0) return 0;
        double sum = 0.0;
        for (int i = 0; i < numbers.length; i++) {
            double num = numbers[i];
            sum += num * num;
        }
        return Math.sqrt(sum / numbers.length);
    }

    private byte[] getNBytes(byte[] arr, int start, int len) {
        byte[] nArr = new byte[len];
        for (int i = 0; i < len; i++) {
            nArr[i] = arr[start+i];
        }
        return nArr;
    }

}
