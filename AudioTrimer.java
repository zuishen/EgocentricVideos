import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioTrimer {
	File audioFile;
	private static final int BUFFER_SIZE = 3200;
	public AudioTrimer(String audioFilePath) {
		audioFile = new File(audioFilePath);
	}
	
	public void trimWrite(int[] metadata) {
		try {
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
			AudioFormat format = audioStream.getFormat();
			//SourceDataLine audioLine = (SourceDataLine) AudioSystem.getLine(info);
			//audioLine.open(format);
			//System.out.println("Playback Started");
			//OutputStream os = new FileOutputStream("./output.wav");
			int bytesRead = 0;
			int flag = 0;
			int offset = 0;
			int i = 0;
						
			byte[] bytesBuffer = new byte[metadata.length*BUFFER_SIZE];
			byte[] bytesBufferTemp = new byte[BUFFER_SIZE];
			//bytesRead = audioStream.read(bytesBuffer);
			while (offset <= bytesBuffer.length - BUFFER_SIZE && bytesRead != -1) {
				if (flag == metadata[i]) {
					bytesRead = audioStream.read(bytesBuffer, offset, BUFFER_SIZE);
					offset += bytesRead;
					i++;
				//bytesRead = audioStream.read(bytesBuffer);
				} else {
					bytesRead = audioStream.read(bytesBufferTemp);
				}
				flag++;
			}
			audioStream.close();
			ByteArrayInputStream bais = new ByteArrayInputStream(bytesBuffer);
			long length = (long)(bytesBuffer.length / format.getFrameSize());
			AudioInputStream newAudioStream = new AudioInputStream(bais, format, length);
			File fileout = new File("output.wav");
			AudioSystem.write(newAudioStream, AudioSystem.getAudioFileFormat(audioFile).getType(), fileout);
			//os.flush();
			//os.close();
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
