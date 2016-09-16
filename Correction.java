

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class Correction {
	public int[] Meta(int[] a) {
		int[] corr=new int[2000];
		int b=0;
		for(int i=1;i<4500; i++){
			if(a[i]!=0){
				if((a[i]-a[i-1])!=1&&(a[i+1]-a[i])==1){
					corr[b]=i;
					b++;}
				if((a[i]-a[i-1])==1&&(a[i+1]-a[i])!=1){
					corr[b]=i;
					b++;
				}
			}
		}
		int j=0;
		int[] z= new int[b];
		for(int i=0;i<2000; i++){
		   if(corr[i]!=0){
			   z[j]=corr[i];
			   j++;
		   }
		}
		return z;
	}	
	
	public void correctionFile(String[] args, int[] corr, int[] videoMetaData) {
		//read and set needed bytes
		File file;
		InputStream is;
		OutputStream os;
		int width = 480;
		int height = 270;
		long len = width*height*3;
		//byte[] bytes  = new byte[(int)len];
		byte[][] output_bytes = new byte[videoMetaData.length][(int)len];
		//ArrayList al = new ArrayList()
		
		try {
			file = new File(args[0]);
			is = new FileInputStream(file);
			//long length = file.length();

			//long frame_num = length / len;
			//System.out.println(frame_num);
			
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
					//al.add(bytes);
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
		//use corr to group the frames into scenaries
		//for each scenary, use the first frame as reference frame and correct the rest frame and save into bytes
		for (int i = 0; i < (int)(corr.length/2); i++) {
			byte[] key_frame = output_bytes[corr[2*i]];
			for (int j = 1; j <= corr[2*i+1] - corr[2*i]; j++) {
				output_bytes[corr[2*i]+j] = correct(key_frame, output_bytes[corr[2*i]+j]);
			}
		}
		//write data into a file
		try {
			os = new FileOutputStream("./output_correction.rgb");
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
	
	public byte[] correct(byte[] bytes1, byte[] bytes2){
		int width = 480;
		int height = 270;
		int[][] gray1 = new int[height][width];
		int[][] gray2 = new int[height][width];
		int ind = 0;
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				byte r = bytes1[ind];
				byte g = bytes1[ind+height*width];
				byte b = bytes1[ind+height*width*2]; 

				int ri = r & 0xff;
				int gi = g & 0xff;
				int bi = b & 0xff;
				int s = (30*ri + 59*gi + 11*bi+50)/100;
				gray1[y][x] = s;

				ind++;
			}
		}
		
		ind = 0;
		for(int y = 0; y < height; y++){

			for(int x = 0; x < width; x++){

				byte r = bytes2[ind];
				byte g = bytes2[ind+height*width];
				byte b = bytes2[ind+height*width*2]; 
				
				int ri = r & 0xff;
				int gi = g & 0xff;
				int bi = b & 0xff;
				int s = (30*ri + 59*gi + 11*bi+50)/100;
				gray2[y][x] = s;
				ind++;
			}
		}

		//int[] vect = findGlobalVector(gray1, gray2);
		int[] vect = findRegGlobalVector(gray1, gray2);
		vect[0] = (int) (vect[0]*1.1);
		vect[1] = (int) (vect[1]*1.1);
		bytes2 = compensation(bytes2, vect);
		bytes2 = fullframe(bytes1, bytes2, vect);
		return bytes2;
	}

	public int[][] calculateProjection(int[][] pixs) {  // 计算各区域块的行，列最大特征峰的坐标位置
		int[][] ret = new int[2][16*9];
		for (int r = 0; r < 9; r++) {
			for (int c = 0; c < 16; c++) {
				long sumy = 0;
				long sumx = 0;
				int x = 1;
				int y = 1;
				for (int i = 0; i < 30; i++) {  //计算行投影,for y
					long temp = 0;
					for (int j = 0; j < 30; j++) {
						temp += pixs[r*30+i][c*30+j];
						//System.out.println(Integer.toString((int)temp));
					}
					if (sumy < temp) {
						sumy = temp;
						y = i;
						//System.out.println(Integer.toString(i));
					}
				}
				for (int j = 0; j < 30; j++) {  //计算列投影,for x
					long temp = 0;
					for (int i = 0; i < 30; i++) {
						temp += pixs[r*30+i][c*30+j];
					}
					if (sumx < temp) {
						sumx = temp;
						x = j;
					}
				}
				ret[0][r*16+c] = x + c*30;
				ret[1][r*16+c] = y + r*30;
				//System.out.println(Integer.toString(c+r*16)+"-- x: "+Integer.toString(ret[0][r*16+c])+", y: "+Integer.toString(ret[1][r*16+c]));
			}
		}
		return ret;
	}
	
	public int[][] calculateMotionVector(int[][] keyVector, int[][] curVector) { //计算各区域的运动矢量 dx=keyX-curX, dy=keyY-curY;
		int[][] ret = new int[2][16*9];
		for (int i = 0; i < 16*9; i++) {
			ret[0][i] = keyVector[0][i] - curVector[0][i];
			ret[1][i] = keyVector[1][i] - curVector[1][i];
			//--------------------------------------------------
			//System.out.println(Integer.toString(i)+"-- dx: "+Integer.toString(ret[0][i])+", dy: "+Integer.toString(ret[1][i]));
		}
		return ret;
	}
	
	public int[] calculateGlobalVector(int[][] motionVector) { //通过直方图统计，算出全局运动矢量
		int[] ret = new int[2];
		int[] histgramx =new int[480*2];
		int[] histgramy =new int[270*2];
		for (int i = 0; i < 16*9; i++) {
			histgramx[motionVector[0][i]+480]++;
			histgramy[motionVector[1][i]+270]++;
		}
		int max_x = 0, dx = 0;
		int max_y = 0, dy = 0;
		for (int i = 0; i < 480*2; i++) {
			if (max_x < histgramx[i]) {
				max_x = histgramx[i];
				dx = i - 480;
				//System.out.println(dx);
			}
		}
		for (int i = 0; i < 270*2; i++) {
			if (max_y < histgramy[i]) {
				max_y = histgramy[i];
				dy = i - 270;
			}
		}
		ret[0] = dx;
		ret[1] = dy;
		//System.out.println("global-- dx: "+Integer.toString(ret[0])+", dy: "+Integer.toString(ret[1]));
		return ret;
	}
	
	public long[][] calculateRegProjection(int[][] pixs) { //常规方法算出投影
		long[][] ret = new long[2][480];
		long[] sumr = new long[270];
		long[] sumc = new long[480];
		for (int i = 0; i < 270; i++) {  //计算行投影,for y
			long temp = 0;
			for (int j = 0; j < 480; j++) {
				temp += pixs[i][j];
			}
			//System.out.println(Integer.toString(pixs[10][100]));
			//System.out.println(Long.toString(temp));
			sumr[i] = temp;
		}
		for (int j = 0; j < 480; j++) {  //计算列投影,for x
			long temp = 0;
			for (int i = 0; i < 270; i++) {
				temp += pixs[i][j];
			}
			sumc[j] = temp;
		}
		//System.out.println(Long.toString(sumr[0]));
		//System.out.println(Integer.toString(c+r*16)+"-- x: "+Integer.toString(ret[0][r*16+c])+", y: "+Integer.toString(ret[1][r*16+c]));
		long rsum = 0;
		long csum = 0;
		long rmean = 0;
		long cmean = 0;
		for (int i = 0; i < 270; i ++) {
			rsum += sumr[i];
		}
		rmean = (rsum / 270);
		for (int i = 0; i < 480; i ++) {
			csum += sumc[i];
		}
		cmean = (csum / 480);
		for(int i = 0; i < 480; i++) {
			ret[0][i] = sumc[i] - cmean;
			if (i < 270) {
				ret[1][i] = sumr[i] - rmean;
			}
		}
		//System.out.println(Long.toString(csum));
		return ret;
	}
	
	public int[] calculateRegGlobalVector(long[][] key_proj, long[][] cur_proj) { //常规方法算出全局运动矢量
		int[] ret = new int[2];
		long min = Long.MAX_VALUE;
		for (int i = 0; i < 270; i++) {
			long temp = 0;
			int counter = 0;
			for (int j = 0; j < 270; j++) {
				if(j+i < 270) {
					//System.out.println(Double.toString(cur_proj[1][j+i]));
					temp += Math.abs(cur_proj[1][j+i] - key_proj[1][j]);
					counter++;
				}
			}
			
			if (min > temp/counter) {
				min = temp/counter;
				ret[1] = i;
				//System.out.println(Long.toString(min));
			}
		}
		min = Long.MAX_VALUE;;
		for (int i = 0; i < 480; i++) {
			long temp = 0;
			int counter = 0;
			for (int j = 0; j < 480; j++) {
				if(j+i < 480) {
					temp += Math.abs(cur_proj[0][j+i] - key_proj[0][j]);
					counter++;
				}
			}
			if (min > temp/counter) {
				min = temp/counter;
				ret[0] = i;
			}
		}
		return ret;
	}
	
	public byte[] compensation(byte[] frame, int[] delta) {
		byte[] new_frame = new byte[480*270*3];
		int ind = 0;
		int height = 270;
		int width = 480;
		int deltaX = -delta[0];
		int deltaY = -delta[1];
		for(int y = 0; y < height; y++){

			for(int x = 0; x < width; x++){

				byte r = frame[ind];
				byte g = frame[ind+height*width];
				byte b = frame[ind+height*width*2]; 
				
				if (x + deltaX < width && x + deltaX >= 0 && y + deltaY < height && y + deltaY >= 0) {
					new_frame[ind+deltaX+deltaY*width] = r;
					new_frame[ind+height*width+deltaX+deltaY*width] = g;
					new_frame[ind+height*width*2+deltaX+deltaY*width] = b;
				} else {
					
				}
				//gray2[y][x] = s;
				//System.out.println(Integer.toString(s));
				//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
				//img2.setRGB(x,y,pix);
				ind++;
			}
		}
		return new_frame;
	}
	
	public byte[] fullframe(byte[] rframe, byte[] cframe, int[]delta) {
		int height = 270;
		int width = 480;

		if (delta[0] > 0) {
			for(int y = 0; y < height; y++){
				for(int x = width-Math.abs(delta[0]); x < width; x++){
					cframe[y*width+x] = rframe[y*width+x];
					cframe[y*width+x+width*height] = rframe[y*width+x+width*height];
					cframe[y*width+x+width*height*2] = rframe[y*width+x+width*height*2];
				}
			}
		} else {
			for(int y = 0; y < height; y++){
				for(int x = 0; x < Math.abs(delta[0]); x++){
					cframe[y*width+x] = rframe[y*width+x];
					cframe[y*width+x+width*height] = rframe[y*width+x+width*height];
					cframe[y*width+x+width*height*2] = rframe[y*width+x+width*height*2];
				}
			}
		}
		if (delta[1] > 0) {
			for(int y = height-Math.abs(delta[1]); y < height; y++){
				for(int x = 0; x < width; x++){
					cframe[y*width+x] = rframe[y*width+x];
					cframe[y*width+x+width*height] = rframe[y*width+x+width*height];
					cframe[y*width+x+width*height*2] = rframe[y*width+x+width*height*2];
				}
			}
		} else {
			for(int y = 0; y < Math.abs(delta[1]); y++){
				for(int x = 0; x < width; x++){
					cframe[y*width+x] = rframe[y*width+x];
					cframe[y*width+x+width*height] = rframe[y*width+x+width*height];
					cframe[y*width+x+width*height*2] = rframe[y*width+x+width*height*2];
				}
			}
		}
		return cframe;
	}
			
	public int[] findGlobalVector(int[][] key, int[][] cur) {
		int[][] keyMotion = calculateProjection(key);
		int[][] curMotion = calculateProjection(cur);
		int[][] motionVector = calculateMotionVector(keyMotion, curMotion);
		int[] delta =  calculateGlobalVector(motionVector);
		//rebuild
		//if (Math.abs(delta[0]) > 15 ||  Math.abs(delta[1]) > 15) {
		//	delta[0] = 0;
		//	delta[1] = 0;
		//}
		return delta;
	}
	
	public int[] findRegGlobalVector(int[][] key, int[][] cur) {
		long[][] keyMotion = calculateRegProjection(key);
		long[][] curMotion = calculateRegProjection(cur);
		int[]delta = calculateRegGlobalVector(keyMotion, curMotion);
		//rebuild
		//System.out.println("global-- dx: "+Integer.toString(delta[0])+", dy: "+Integer.toString(delta[1]));
		if (Math.abs(delta[0]) > 15 ||  Math.abs(delta[1]) > 15) {
			delta[0] = 0;
			delta[1] = 0;
		}
		return delta;
	}
}
