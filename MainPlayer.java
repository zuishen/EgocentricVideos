import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainPlayer {
	private JFrame mainFrame;
	private JPanel mainPanel;
	
	public MainPlayer(){
		mainFrame = new JFrame();
		mainPanel = new JPanel();
		
		BoxLayout bLayout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
		mainPanel.setLayout(bLayout);
	    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
	}
	
	public void addPanel(JPanel jp) {
		mainPanel.add(jp);
	}
	
	public void showFrame() {
		mainFrame.add(mainPanel);
		mainFrame.pack();
	    mainFrame.setVisible(true);
	}
	
	public void generateVideoFile(String[] args, int[] videoMetaData) {
		File file;
		InputStream is;
		OutputStream os;
		int width = 480;
		int height = 270;
		long len = width*height*3;

		byte[][] output_bytes = new byte[videoMetaData.length][(int)len];
		
		try {
			file = new File(args[0]);
			is = new FileInputStream(file);

			
			for (int i = 0, j = 0; i < 4500; i++) {
				int offset = 0;
				int numRead = 0;	
				byte[] bytes  = new byte[(int)len];
				if(j >= videoMetaData.length) {
					break;
				}
				while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
					offset += numRead;
				}
				if (i == videoMetaData[j]) {
					//System.out.println(videoMetaData[j]);
					output_bytes[j] = bytes;
					j++;
				}
			}
			is.close();			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			os = new FileOutputStream("./output.rgb");
			for(int i = 0; i < videoMetaData.length; i++) {
				os.write(output_bytes[i]);
			}
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		if (args.length < 2) {
		    System.err.println("usage: java -jar AVPlayer.jar [RGB file] [WAV file]");
		    return;
		}
		
		MainPlayer mp = new MainPlayer();
		
		AVPlayer ren = new AVPlayer();
		ren.initialize(args, 4500);
		mp.addPanel(ren.getPanel());
		
		//this area is for the panel after convert, add summarization and index code here
		//summarization code	  精简视频
		
		int[] md;
		Video_summary vs = new Video_summary();
		md = vs.Histgram(args);
		vs = null;
		int frame_num = 0;
		for(int i = 0; i < md.length; i++) {
			if (i != 0 && md[i] != 0) {
				frame_num++;
			}
		}
		int[] metadata = new int[frame_num+1];
		for(int i = 0; i < metadata.length; i++) {
			metadata[i] = md[i];
		}
		System.out.println(Arrays.toString(metadata));
		
		//----
		//indexing code	 检索
		if(args.length > 2) {
			PicIndexing pi = new PicIndexing(metadata, args[0], args[2]);
			pi.process();
			int[] temp = pi.getMetaData();
			//System.out.println("indexes: " + temp[0] + "next: " + temp[1]);
			int[] oldmd = metadata;
			metadata = new int[oldmd[temp[1]] - oldmd[temp[0]+1]];
			metadata[0] = oldmd[temp[0]];
			for(int i=1; i<metadata.length; i++)
				metadata[i] = metadata[i-1] + 1;
			//----
		} else {
		//Correcting code	  去抖
		Correction vc = new Correction();
		int[] ma = vc.Meta(md);
		vc.correctionFile(args, ma, metadata);
		//---------------
		}
		//生成窗口
		
		mp.generateVideoFile(args, metadata);
		AudioTrimer at = new AudioTrimer(args[1]);
		at.trimWrite(metadata);
		//System.out.println(Arrays.toString(metadata));
		String[] args1 = {"output_correction.rgb", "output.wav"};
		if(args.length > 2) {
			args1[0] = "output.rgb";
		}
		//String[] args2 = {"output.rgb", "output.wav"};
		//String[] args2 = {args[0], args[1]};
		//AVPlayer ren = new AVPlayer();
		//ren.initialize(args2, metadata.length);
		//mp.addPanel(ren.getPanel());
		AVPlayer ren1 = new AVPlayer();
		ren1.initialize(args1, metadata.length);
		mp.addPanel(ren1.getPanel());

		mp.showFrame();
	}
}
