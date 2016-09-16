import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;


public class AudioPlayer implements LineListener {
	boolean playCompleted;
	public Clip audioClip;
	public boolean is_pause;
	public boolean is_stop;
	AudioInputStream audioStream;
	AudioFormat format;
	DataLine.Info info;
	
	void play(String audioFilePath) {
		File audioFile = new File(audioFilePath);
		try {
			audioStream = AudioSystem.getAudioInputStream(audioFile);
			
			format = audioStream.getFormat();
			
			DataLine.Info info = new DataLine.Info(Clip.class, format);//(Clip.class, format);
			
			audioClip = (Clip) AudioSystem.getLine(info);
			
			audioClip.addLineListener(this);
			
			audioClip.open(audioStream);
			
			audioClip.start();
			
			while (!playCompleted) {
				//wait for the playback completes
				try {
					//System.out.println("Playing.");
					Thread.sleep(100);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
			audioStream.close();
			audioClip.close();
			
		} catch (UnsupportedAudioFileException ex) {
			ex.printStackTrace();
		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void update(LineEvent event) {
		LineEvent.Type type = event.getType();
		
		if (type == LineEvent.Type.START) {
			System.out.println("Playback started.");
		} else if (type == LineEvent.Type.STOP) {
			if (is_stop) {
				playCompleted = true;
				System.out.println("Playback stop.");
				return;
			}
			if(is_pause) {
				System.out.println("Playback pause.");
				return;
			} 
			if(is_pause == false && is_stop == false) {
				//System.out.println("Playback completed.");
				return;
			}
			playCompleted = true;
			System.out.println("Playback stop.");
		}
	}
}
