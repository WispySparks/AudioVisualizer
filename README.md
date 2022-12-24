# AudioVisualizer

Project where I'm going to implement my own MP3 Decoder and so I will take in MP3 Files and decode/decompress them into PCM Samples which I can then grab the amplitude from using fourier stuff or something(?) and then use some sort of visual like a wave or graphic to visualize the music as it plays from the pcm sample's amplitude.

Steps:
Implement MP3 Decoder to get the raw PCM Data from the compressed audio data in the MP3 file.
I can then play the raw PCM using Java's source data lines to make sure I've decoded the mp3 files correctly.
Use something called a Fast Fourier Transform to grab the amplitude from the PCM Data and then use something to visualize that like a wave as the music plays.
I can either play the music from my decoded data or just use JavaFX's media player to play it.
I might also do this for WAVE files too because it's a lot simpler and the raw PCM Data is already right there in the file uncompressed.
