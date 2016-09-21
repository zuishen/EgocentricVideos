
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.*;
import java.io.*;
import java.util.Date;
import java.util.TimerTask;
import java.util.Timer;

import javax.swing.*;



public class AVPlayer{
	//JFrame frame;
	JPanel panel;
	JLabel lbIm1;
	JLabel lbIm2;
	int height;
	int width;
	
	BufferedImage img;
	File file;
	AudioPlayer playSound;
	InputStream is;
	
	Timer timer;
	long current;
	long start;
	int frame_counter;
	
	boolean is_video_pause;
	boolean is_video_fastforward;
	boolean is_video_rewind;
	
	public void initialize(String[] args, int frame_length){
		width = 480;
		height = 270;
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		try {
				//RandomAccessFile fff = new RandomAccessFile("abc", "r");
				file = new File(args[0]);
				is = new FileInputStream(file);

				long length = file.length();
				long len = width*height*3;
				long frame_num = length / len;
				System.out.println(frame_num);
				byte[] bytes = new byte[(int)len];
			
				int offset = 0;
				int numRead = 0;	
				while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
					offset += numRead;
				}
				
				int ind = 0;
				for(int y = 0; y < height; y++){
					for(int x = 0; x < width; x++){
						//byte a = 0;
						byte r = bytes[ind];
						byte g = bytes[ind+height*width];
						byte b = bytes[ind+height*width*2]; 

						int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
						//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
						img.setRGB(x,y,pix);
						ind++;
					}
				}
			} catch (FileNotFoundException e) {
			e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		// Use labels to display the images
		//frame = new JFrame();
		panel = new JPanel();
		GridBagLayout gLayout = new GridBagLayout();
		//frame.getContentPane().setLayout(gLayout);
		panel.setLayout(gLayout);

		JLabel lbText1 = new JLabel("Video: " + args[0]);
		lbText1.setHorizontalAlignment(SwingConstants.LEFT);
		JLabel lbText2 = new JLabel("Audio: " + args[1]);
		lbText2.setHorizontalAlignment(SwingConstants.LEFT);
		JButton pp = new JButton("play");
		JButton stop = new JButton("stop");
		JButton rw = new JButton("<<");
		JButton ff = new JButton(">>");
		JButton pause = new JButton("pause");
		pause.setEnabled(false);
		stop.setEnabled(false);
		ff.setEnabled(false);
	 	rw.setEnabled(false);

		pp.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e)
	            {
	                //Execute when button is pressed
				 	playWAV(args[1]);  //play sound
				 	//set button init state 
				 	pp.setEnabled(false);
				 	stop.setEnabled(true);
				 	pause.setEnabled(true);
				 	ff.setEnabled(true);
				 	rw.setEnabled(true);
				 	is_video_pause = false;
				 	is_video_fastforward = false;
				 	is_video_rewind = false;
				 	frame_counter = 0;
				 	System.out.println("video start");		
				 	Date now = new Date();
				 	start = now.getTime()+4000;
				 	timer = new Timer();
				 	timer.scheduleAtFixedRate(new TimerTask() {
					      public void run() {
					    	  	if (playSound.playCompleted == true || frame_counter >= frame_length) {
					    	  	//it will delete the old data and timer
					    	  	//new a input stream and set buttons and boolean variable to initial state
					    	  		timer.cancel();
					    	  		try {
					    	  				is = new FileInputStream(file);
					    	  				frame_counter = 1;
									} catch (FileNotFoundException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
					    	  		
								pp.setEnabled(true);
								pause.setEnabled(false);
								pause.setText("pause");
								stop.setEnabled(false);
								ff.setEnabled(false);
							 	rw.setEnabled(false);
							 	playSound.is_pause = false;
							 	is_video_fastforward = false;
							 	is_video_rewind = false;
					    	  	} 
						    	if (is_video_pause == false) { 
						    		Date now = new Date();
						    		current = now.getTime();

						    		if (current - start <= 6700 && current - start >= 6000) {
						    			start = current;	
						    		} else {
						    			byte[] bytes = new byte[height*width*3];
						    			try {
						    					int offset = 0;
						    					int numRead = 0;	
						    					int ind = 0;
						    					while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
						    						offset += numRead;
						    					}
		
						    					for(int y = 0; y < height; y++){
						    						for(int x = 0; x < width; x++){
						    							//byte a = 0;
						    							byte r = bytes[ind];
						    							byte g = bytes[ind+height*width];
						    							byte b = bytes[ind+height*width*2]; 

						    							int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
						    							//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
						    							img.setRGB(x,y,pix);
						    							ind++;
						    						}
						    					}
						    					frame_counter++;
						    				} catch (IOException e1) {
						    					// TODO Auto-generated catch block
						    					e1.printStackTrace();
						    				}
								
						    			lbIm1.setIcon(new ImageIcon(img));
						    			lbIm1.repaint();
						    		}
						   	}
					 }}, 66, 66);
	            }
		});

		pause.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (playSound.is_pause == false && playSound.audioClip.isRunning()) {
					//if the video is running and pause is clicked
					System.out.println("pause");
					//video pause -- means making the run function of timer to do nothing
					is_video_pause = true;
					//store the calibration data
					Date now = new Date();
					current = now.getTime();
					start = current - start;
					//sound pause
					playSound.is_pause = true;
					playSound.audioClip.stop();
					pause.setText("resume");
					ff.setEnabled(false);
				 	rw.setEnabled(false);
				} else {
					//if the video is paused when resume button is clicked
					//make timer function work
					is_video_pause = false;
					//resume calibration data
					Date now = new Date();
					current = now.getTime();
					start = current - start;
					//sound resume
					playSound.is_pause = false;
					playSound.audioClip.start();
					pause.setText("pause");
					ff.setEnabled(true);
				 	rw.setEnabled(true);
				}
			}
		});
		
		stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//when stop button is clicked
				//sound part
				//the timer will set state back to init
				//playSound.playCompleted = true;
				if (playSound.audioClip.isRunning()) {
					playSound.is_stop = true;
					playSound.audioClip.stop();
				} else {
					playSound.audioClip.start();
					playSound.is_stop = true;
					playSound.audioClip.stop();
				}
			}
		});
		
		ff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (!is_video_fastforward) { 
					pause.setEnabled(false);
					stop.setEnabled(false);
					is_video_fastforward = true;
					if (frame_counter + 150 >= frame_length) {
						playSound.is_stop = true;
						playSound.audioClip.stop();
					} else {
						try {
								is_video_pause = true;
								playSound.audioClip.stop();
								playSound.audioClip.setMicrosecondPosition(playSound.audioClip.getMicrosecondPosition()+10010000);
								is.skip(150*width*height*3);
								playSound.audioClip.start();
								frame_counter += 150;
								is_video_pause = false;
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							ff.setText("ok");
							rw.setEnabled(false);
					}
				} else {
					pause.setEnabled(true);
					stop.setEnabled(true);
					is_video_fastforward = false;
					ff.setText(">>");
					rw.setEnabled(true);
				}
			}	
		});
		
		rw.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (!is_video_rewind) { 
					pause.setEnabled(false);
					stop.setEnabled(false);
					is_video_rewind = true;
					is_video_pause = true;
					playSound.audioClip.stop();
					try {
							InputStream new_is = new FileInputStream(file);
					
							if (frame_counter < 150) {
								frame_counter = 0;
								playSound.audioClip.setMicrosecondPosition(0);
							} else {
								frame_counter = frame_counter - 150;
								playSound.audioClip.setMicrosecondPosition(playSound.audioClip.getMicrosecondPosition()-10000000);
							}
							for(int i = 0; i < frame_counter; i++) {
								byte[] bytes = new byte[height*width*3];
		    				
		    						int offset = 0;
		    						int numRead = 0;	
		    						try {
		    								while (offset < bytes.length && (numRead=new_is.read(bytes, offset, bytes.length-offset)) >= 0) {
		    									offset += numRead;
		    								}
		    							} catch (IOException e1) {
		    								// TODO Auto-generated catch block
		    								e1.printStackTrace();
		    							}
							}
							is = new_is;
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					Date now = new Date();
		    			start = now.getTime(); 
					playSound.audioClip.start();
					is_video_pause = false;
					rw.setText("ok");
					ff.setEnabled(false);
				} else {
					pause.setEnabled(true);
					stop.setEnabled(true);
					is_video_rewind = false;
					rw.setText("<<");
					ff.setEnabled(true);
				}	
			}		
		});
		
		lbIm1 = new JLabel(new ImageIcon(img));
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		//frame.getContentPane().add(lbText1, c);
		panel.add(lbText1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 1;
		//frame.getContentPane().add(lbText2, c);
		panel.add(lbText2, c);
		
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 2;
		//frame.getContentPane().add(pp, c);
		panel.add(pp, c);
		
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 2;
		//frame.getContentPane().add(pause, c);
		panel.add(pause, c);
		
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 0.5;
		c.gridx = 2;
		c.gridy = 2;
		//frame.getContentPane().add(stop, c);
		panel.add(stop, c);
		
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 0.5;
		c.gridx = 3;
		c.gridy = 2;
		//frame.getContentPane().add(rw, c);
		panel.add(rw, c);
		
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 0.5;
		c.gridx = 4;
		c.gridy = 2;
		//frame.getContentPane().add(ff, c);
		panel.add(ff, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 5;
		//frame.getContentPane().add(lbIm1, c);
		panel.add(lbIm1, c);

		//frame.pack();
		//frame.setVisible(true);	
	}
	
	public void playWAV(String filename){
		// opens the inputStream
		new Thread(
	            new Runnable() {
	                public void run() {
	                    try {
	                        // PLAY AUDIO CODE
	                    		// plays the sound
	                    		// initializes the playSound Object
	                			playSound = new AudioPlayer();
	                			//state after click play button
	                			playSound.is_stop = false;
	                			playSound.is_pause = false;
	                			playSound.play(filename);
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                }
	            }).start();
	}
	
	public JPanel getPanel() {
		return this.panel;
	}
	
}