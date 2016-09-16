import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class PicIndexing {
	private static final int width = 480;
	private static final int height= 270;
	
	int[] metadata;
	String vfilename;
	String pfilename;
	int[] submd;
	double[] similarities;
	BufferedImage bPic;
	double[][] his1;
/*
	public static void main(String[] args) {
		int[] metadata = new int[4500];
		for (int i = 0; i< metadata.length; i++) {
			metadata[i] = i;
		}
		PicIndexing pi = new PicIndexing(metadata, "/Users/yuao/Downloads/CS576-EgoCentricVideos/Videos/Alireza_Day2_001/Alireza_Day2_001.rgb", "/Users/yuao/Downloads/QueryImages/Alireza_Day2_001/3099.rgb");
		pi.process();
		System.out.println("indexes: " + metadata[pi.getMetaData()[0]] + "next: " + metadata[pi.getMetaData()[1]]);
	}
*/
	public PicIndexing (int[] md, String filename, String basePicf) {
		this.metadata = md;
		this.vfilename = filename;
		this.pfilename = basePicf;
		submd = new int[2];
		this.similarities = new double[md.length];
		//bPic = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		try {
			File file = new File(pfilename);
			System.out.println("file length:" + file.length());
			InputStream is = new FileInputStream(file);
			long len = file.length();
			byte[] bytes = new byte[(int)len];
			his1 = new double [3][256];
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}
			System.out.println("you have read:" + offset);
			
			for(int ind = 0; ind < (int)(len/3); ind++) {
				int r = bytes[ind] & 0xff;
				int g = bytes[ind+(int)(len/3)] & 0xff;
				int b = bytes[ind+(int)(len/3)*2] & 0xff;
				his1[0][r] ++;
				his1[1][g] ++;
				his1[2][b] ++;
//				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
//				//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
//				bPic.setRGB(x,y,pix);
			}
			double red = 0, green = 0, blue = 0;
			for(int j=0;j<256;j++) {
				red+=his1[0][j];  
				green+=his1[1][j];  
		        blue+=his1[2][j];  
		    }
			for(int j=0;j<256;j++)//将直方图每个像素值的总个数进行量化  
		    {
				his1[0][j]/=red;
		        his1[1][j]/=green;  
		        his1[2][j]/=blue;  
		    }
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void process() {
		try {
			File file = new File(vfilename);
			InputStream is = new FileInputStream(file);

			long length = file.length();
			long len = width*height*3;
			long frame_num = length / len;
			System.out.println(frame_num);
			byte[] bytes = new byte[(int)len];
			BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			int currFrame = 0;
			int mdIndex = 0;
			//double[][] his1 = getHistogram(bPic);
			while (currFrame < frame_num && mdIndex < metadata.length) {
				int offset = 0;
				int numRead = 0;	
				while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
					offset += numRead;
				}
				if(metadata[mdIndex] != currFrame) {
					currFrame++;
					continue;
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
						frame.setRGB(x,y,pix);
						ind++;
					}
				}
				double[][] his2 = getHistogram(frame);
				similarities[mdIndex] = getSimilarity(his1, his2);
				mdIndex ++;
				currFrame++;
			}
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int maxIndex = findMinIndex(similarities);
		if(maxIndex == 0) {
			submd[0] = 0;
			int i = 2;
			for(; i<metadata.length; i++) {
				if(similarities[i-1] > similarities[i])
					break;
			}
			submd[1] = i;
		} else if (maxIndex == metadata.length - 1) {
			submd[1] = maxIndex-1;
			int i = maxIndex-2;
			for(; i>=0; i--) {
				if(similarities[i-1] < similarities[i])
					break;
			}
			submd[0] = i;
		} else {
//			if(similarities[maxIndex-1] > similarities[maxIndex+1]) {
//				submd[0] = metadata[maxIndex];
//				submd[1] = metadata[maxIndex+1];
//			} else {
//				submd[0] = metadata[maxIndex-1];
//				submd[1] = metadata[maxIndex];
//			}
			int i = maxIndex-1;
			for(; i>=0; i--) {
				if(similarities[i-1] < similarities[i])
					break;
			}
			submd[0] = i;
			i = maxIndex + 1;
			for(; i<metadata.length; i++) {
				if(similarities[i-1] > similarities[i])
					break;
			}
			submd[1] = i;
		}
	}
	
	public int[] getMetaData() {
		return submd;
	}

	private double [][] getHistogram(BufferedImage img)  
    {
	   double [][] histgram=new double [3][256];
       int width=img.getWidth();//图片宽度  
       int height=img.getHeight();//图片高度  
       int pix[]= new int [width*height];//像素个数  
       int r,g,b;//记录R、G、B的值  
       pix = img.getRGB(0, 0, width, height, pix, 0, width);//将图片的像素值存到数组里  
       for(int i=0; i<width*height; i++)   
       {    
           r = pix[i]>>16 & 0xff; //提取R   
           g = pix[i]>>8 & 0xff;    
           b = pix[i] & 0xff;     
           histgram[0][r] ++;    
           histgram[1][g] ++;    
           histgram[2][b] ++;    
       }
       double red =0,green=0,blue=0;
       for(int j=0;j<256;j++){
           red+=histgram[0][j];  
           green+=histgram[1][j];  
           blue+=histgram[2][j];  
       }
       for(int j=0;j<256;j++)//将直方图每个像素值的总个数进行量化  
       {  
           histgram[0][j]/=red;  
           histgram[1][j]/=green;  
           histgram[2][j]/=blue;  
       }
       return histgram;  
    }  
	private double getSimilarity(double [][] Rhistgram,double  [][] Dhistgram)  
    {
          double similar=(double)0.0;//相似度  
          for(int i=0;i<3;i++)  
          {  
              for(int j=0;j<Rhistgram[i].length;j++)  
              {  
                  similar+=(Rhistgram[i][j]-Dhistgram[i][j])*(Rhistgram[i][j]-Dhistgram[i][j]);  
              }  
          }
          //similar=Math.sqrt(similar);  
          //similar=similar/3;
          return similar;
    }
	
	private int findMinIndex(double[] sim) {
		double min = Double.MAX_VALUE;
		int mini = 0;
		System.out.println("how many similarities? ; " + sim.length);
		System.out.println("values: ");
		for (int i=0; i<sim.length; i++) {
			//System.out.print(sim[i] + "   ");
			mini = sim[i] < min ? i : mini;
			min = min < sim[i] ? min : sim[i];
		}
		System.out.println();
		return mini;
	}
}
