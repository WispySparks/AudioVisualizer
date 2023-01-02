# AudioVisualizer

Project where I'm going to implement my own MP3 Decoder and so I will take in MP3 Files and decode/decompress them into PCM Samples which I can then grab the amplitude from using fourier stuff or something(?) and then use some sort of visual like a wave or graphic to visualize the music as it plays from the pcm sample's amplitude.

### Steps:
- (CURRENTLY WORKING ON) Implement MP3 Decoder to get the raw PCM Data from the compressed audio data in the MP3 file.
- (DONE) (Not gonna be used in the real program) I can then play the raw PCM using Java's source data lines to make sure I've decoded the mp3 files/ wav files correctly.
- Use something called a Fast Fourier Transform to grab the frequencies from the PCM Data and then use something to visualize that like colors or size as the music plays.
- (MOSTLY IMPLEMENTED) I will also implement a way to get the relative volume of windows of the audio and then visualize that as well.
- I will use JavaFX's MediaPlayer to actually play the music because it is pretty convenient and I will line up my graph with it.
- (FINISHED) (WAV DECODER/FILE) I might also do this for WAVE files too because it's a lot simpler and the raw PCM Data is already right there in the file uncompressed.

Currently working on decoding mp3s and the gui and getting the volume of windows of an audio file (just wavs for now).
