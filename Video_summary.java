
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.lang.Math;

public class Video_summary {
	static double sim1;
	static int width = 480;
	static int height = 270;
	static InputStream is;
	static int [] num = new int[4500];
	static double [][] histgram1=new double [3][256];
	static double [][] histgram2=new double [3][256];
	static int[][][] r1 = new int[900][9][16];
	static int[][][] r2 = new int[900][9][16];
    static int[][][] g1 = new int[900][9][16];
    static int[][][] g2 = new int[900][9][16];
    static int[][][] b1 = new int[900][9][16];
    static int[][][] b2 = new int[900][9][16];
    static int [] frame = new int[4500];
    //static int [] frame_final = new int[4500];
	//static BufferedImage img1 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 	//static BufferedImage img2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	
	public static int[] Histgram(String[] args){ 
		double result=0.0;
		int result1=0;
		int z = 0;
		int z1 = 0;
		int q = 1;
		double r= 0.0;
		double r3=0.0;
		int count = 1;
		
	
		
		try {
			File file = new File(args[0]);
			is = new FileInputStream(file);
            

		    long len = width*height*3;
		    
			byte[] bytes= new byte[(int)len];
            

			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}

			  int ind = 0;
			  for(int y = 0; y < (height); y++){
				for(int x = 0; x < width; x++){
				  int r5 = (bytes[ind]& 0xff);
				  int g5 = (bytes[ind+height*width]& 0xff);
				  int b5 = (bytes[ind+height*width*2]& 0xff);
				
				  histgram1[0][r5]++;
				  histgram1[1][g5]++;
				  histgram1[2][b5]++;
				  ind++;
			   }
			  }
			  for(int i = 0; i < 256; i++){
				  histgram1[0][i]/=width*height;
				  histgram1[1][i]/=width*height;
				  histgram1[2][i]/=width*height;
				 // histgram2[2][i]/=width*height;
				//System.out.println(histgram1[0][y]);
				//System.out.println(histgram2[0][y]);
				}

			
			for(int y=1; y < 4500; y++){
				
				
				byte[] bytes1= new byte[(int)len];
	            int offset1 = 0;
				int numRead1 = 0;
				while (offset1 < bytes1.length && (numRead1=is.read(bytes1, offset1, bytes1.length-offset1)) >= 0) {
					offset1 += numRead1;}
			   int ind1 = 0;
			   for(int y1 = 0; y1 < height; y1++){
                for(int x = 0; x < width; x++){
				  int r6 = (bytes1[ind1]& 0xff);
            	  int g6 = (bytes1[ind1+height*width]& 0xff);
				  int b6 = (bytes1[ind1+height*width*2]& 0xff);
				  histgram2[0][r6]++;
			      histgram2[1][g6]++;
			      histgram2[2][b6]++;
			      ind1++;
                }
			} 
			   for(int i = 0; i < 256; i++){
				  
				  histgram2[0][i]/=width*height;				  
			      histgram2[1][i]/=width*height;
				  histgram2[2][i]/=width*height;
				//System.out.println(histgram1[0][y]);
				//System.out.println(histgram2[0][y]);
				}
			   result= Similarity();
			   r= result; 
			   if (count<=5){
				   r3 += r;
			   }
			   else{
				   r3= r3/20;
				   //System.out.println("this is:" +r3);
				   z1= (int) (r3/0.01);
				   if(z!=z1){
					   z=z1;
					   if((y-10)>0){
					   num[q]=y-10;
					   //System.out.println(q+"th:"+num[q]);
					   q++;
					   }
				    }
				   count= 1;
				   r3 = result;
			   }
			 
	          // System.out.println(result);
	           for(int i = 0; i < 256; i++){
				   histgram2[0][i]=0;
				   histgram2[1][i]=0;
		       	   histgram2[2][i]=0;
	     }
	     result = 0.0;
	     count++;
	     
	}
		num[q]=4499;
		is.close();
}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
				
   /* try {
			File file = new File(args[0]);
			is = new FileInputStream(file);
		    long len = width*height*3;
		    byte[] bytes= new byte[(int)len];
		    int offset = 0;
		    int numRead = 0;
		    while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
					offset += numRead;			}
            int ind = 0;
			int yoffset, xoffset;
			for(int y = 0; y < 30; y++){
				 yoffset= y*9;
		 		 for(int x = 0; x < 30; x++){
					 xoffset= x*16;
				     for(int i = 0; i < 9; i++){
						 for(int j = 0; j < 16; j++){
							 r1[ind][i][j] = (bytes[(yoffset+i)*width+xoffset+j]& 0xff);
						     g1[ind][i][j] = (bytes[(yoffset+i)*width+xoffset+j+width*height]& 0xff);
						     b1[ind][i][j] = (bytes[(yoffset+i)*width+xoffset+j+width*height*2]& 0xff);  
					     }  
					 }
		             ind++;
				 }
		  }
         //int b=0;
         int o=0;
         int a1=0;
         byte[] bytes1= new byte[(int)len];
         for(int y3=1; y3 < 4500; y3++){
           if(num[y3]!=0){
             for(int y=a1+1; y <= num[y3]; y++){
	            int offset1 = 0;
				int numRead1 = 0;
				while (offset1 < bytes1.length && (numRead1=is.read(bytes1, offset1, bytes1.length-offset1)) >= 0) {
					      offset1 += numRead1;}
				int ind1 = 0;
				int yoffset1, xoffset1;
		   	    for(int y1 = 0; y1 < 30; y1++){
	    			  yoffset1= y1*9;
				      for(int x = 0; x < 30; x++){
				     	  xoffset1= x*16;
						  for(int i = 0; i < 9; i++){
							  for(int j = 0; j < 16; j++){
								  r2[ind1][i][j] = (bytes1[(yoffset1+i)*width+xoffset1+j]& 0xff);
								  g2[ind1][i][j] = (bytes1[(yoffset1+i)*width+xoffset1+j+width*height]& 0xff);
								  b2[ind1][i][j] = (bytes1[(yoffset1+i)*width+xoffset1+j+width*height*2]& 0xff);
							 }  
						  }
							  ind1++;
						}
		   	    }
		   	    
					double  flag = 0;
					for(int s=0;s<900;s++){
						result1= Equal(s);
			     		if(result1==1)
						    {
									//Distance(z,ind1);
								flag++;
								result1=0;
									//System.out.println("hello");
							}
					   }
							//  System.out.println("flag:"+flag);
					int a=0;
              	    for(int y2 = 0; y2 < 30; y2++){
           				  //yoffset= y*9;
           			for(int x = 0; x < 30; x++){
           			  //xoffset= x*16;
           			  for(int i = 0; i < 9; i++){
           				  for(int j = 0; j < 16; j++){
           						  r1[a][i][j] = r2[a][i][j];
           						  g1[a][i][j] = g2[a][i][j];
           						  b1[a][i][j] = b2[a][i][j];
                                 }
			                  }
           				  a++;
           				}
           			  }
						flag/=900;
						if(flag<0.2){
							if((y-frame[o])>=60&&(y-frame[o])<=120)
							{ for (int i=frame[o];i<y;i++)
							  {System.out.println("this is:"+i);
								frame[o+1]=i+1;
								  o++;}
								  o--;
							}
		                     frame[o+1]=y;
		                     
                         }
            
            a1= num[y3];
            if(frame[o]!=num[y3])
            {
              frame[o+1]=num[y3];
              o++;
       	    }
          }
        }
      }
    }
    
    
				
			
			
	    catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        /*int e=1;
        for(int i=1;i<4500; i++){
          if(frame[i]!=0){
            if((frame[i+1]-frame[i])>2){
        	  if((frame[i+1]-frame[i])>=20){
        		for(int f=frame[i];f<frame[i+1];f++)
        			 frame_final[e]=f;
        		     e++;
               }
        	  else{
        	    frame_final[e]=frame[i];
        		frame_final[e+1]=frame[i+1];
        		e++;
        	 }
            }
          }
        }*/
		int e=1;
		frame[0]=0;
		for(int i=1;i<4500; i++){
			if(num[i]!=0){
				if((num[i]-num[i-1])>=15&&(num[i]-num[i-1])<=30){
					for(int j=num[i-1];j<num[i]-1;j++){
						frame[e]=j+1;
						e++;
					}
				}
				frame[e]=num[i];
				e++;
			}
		}
        return frame;
   } 
			
			
 public static int Equal(int n){
	    double count= 0;
		for(int x = 0; x < 9; x++){
			for(int y = 0; y < 16; y++){
		      for(int i = 0; i < 9; i++){
		    	  for(int j = 0; j < 16; j++){
		    	       if(r1[n][x][y]==r2[n][i][j]&& g1[n][x][y]==g2[n][i][j]&&b1[n][x][y]==b2[n][i][j]){
						          count++;
						          i=9;
						          j=16;
					         }
					     }
					  }
				   }
		}
				count/=144;
				//System.out.println(count);
				if(count>=0.5) { return 1;}
				else return 0;
		    }

	
	public static double Similarity(){
		double sim= 0.0;
		for(int i = 0; i < 3; i++){
		   for(int j = 0; j < 256; j++){
			sim+= Math.pow((histgram1[i][j]-histgram2[i][j]),2);
			}
		  }
		sim = Math.sqrt(sim);
		//System.out.println(sim/6);
		return sim;
    }

	public static void main(String[] args) {
		Histgram(args);
		//for(int i=0;i<4500;i++){
			//System.out.println(frame[i]);
		//}
		   
	}
}




