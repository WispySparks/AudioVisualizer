package audiovisualizer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioMagician {

    /**
     * 
     * @param pcm data
     * @param samplingFrequency in Hz
     * @return audio volume in dB, each value represents 400 ms of audio.
     */
    public double[] getAudioVolume(byte[] pcm, double samplingFrequency, int bitsPerSample, int numChannels) {
        final double windowSeconds = 0.4;
        int[][] parsedPCM = parsePCMData(pcm, bitsPerSample, numChannels, ByteOrder.LITTLE_ENDIAN);
        // 400 ms window
        int windowSize = (int) (samplingFrequency * windowSeconds); // window length in samples
        int numWindows = parsedPCM[0].length / windowSize; // number of 400 ms windows = number of samples in a channel / window size
        double[][] rmsNums = new double[numWindows][windowSize];
        double[] decibels = new double[numWindows];
        for (int currentWindow = 0; currentWindow < numWindows; currentWindow++) {
            for (int currentSample = 0; currentSample < windowSize; currentSample++) {
                int offset = currentWindow * windowSize;
                double val = 0;
                for (int channel = 0; channel < numChannels; channel++) {
                    val += parsedPCM[0][currentSample + offset];
                }
                val /= numChannels;
                rmsNums[currentWindow][currentSample] = val;
            }
        }
        for (int i = 0; i < numWindows; i++) {
            decibels[i] = rootMeanSquare(rmsNums[i]);
        }
        for (int i = 0; i < decibels.length; i++) {
            decibels[i] = 20*Math.log10(decibels[i]);
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
                    case 4 -> buffer.getInt();
                    default -> throw new IllegalArgumentException("Unsupported audio format has " + bytesPerSample + " bytes per sample. Only support 1, 2, and 4.");
                };
                parsedPCM[channel][sample] = currentSample;
                pos += bytesPerSample;
            }
        }
        return parsedPCM;
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
