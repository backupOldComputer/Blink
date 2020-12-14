package src;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;

public class WDivH{
    public static final int box = 100;
    private static int[] divCount = new int[box*3+1];
    public static void main(String[] args) throws Exception {
	for(int i=0;i<args.length;++i){
		BufferedImage m = ImageIO.read(new FileInputStream(args[i]));
		double len = (double)box;
		double folder = m.getWidth() * len /m.getHeight();
		int lenMul = (int)folder;
		divCount[lenMul] += 1;
		
		if( lenMul==150 ){	//lenMul==66)
			ImageIO.write(m, "png", new File(args[i]+".png"));
		}
		if(i%100==0) System.out.println("还剩"+(args.length-i)); 
	}
	System.out.println("150---------"+divCount[150]);
	showCount();
    }
    public static void showCount(){
	for(int i=1;i<=box*3;++i){
		if(i%10==1)
			System.out.println(i/10+"\t");
		System.out.print(divCount[i]+"   ");
	}
    }
}
/*
150---------2619
151---------0
152---------0
0	
0   0   0   0   0   0   0   0   0   0   1	
0   0   0   0   0   0   0   0   0   0   2	
0   0   0   0   0   0   0   0   0   0   3	
0   0   0   0   0   0   0   0   0   0   4	
0   0   0   0   0   0   0   0   0   0   5	
0   0   0   0   0   0   0   0   0   0   6	
1   0   0   2   0   3951   0   0   0   0   7	
0   0   0   0   2   0   0   0   0   0   8	
0   0   0   0   0   0   0   0   0   0   9	
0   1   0   0   0   0   0   0   0   0   10	
0   0   0   0   0   0   0   0   0   0   11	
0   0   0   0   0   0   0   0   0   0   12	
0   0   0   0   0   0   0   0   0   0   13	
0   0   0   0   0   1   0   0   0   0   14	
0   0   0   0   0   0   0   0   100   2619   15	
0   0   0   0   0   0   0   0   0   0   16	
0   0   0   0   0   0   0   0   0   0   17	
0   0   0   0   0   0   0   0   0   0   18	
0   0   0   0   0   0   0   0   0   0   19	
0   0   0   0   0   0   0   0   0   0   20	
0   0   0   0   0   0   0   0   0   0   21	
0   0   0   0   0   0   0   0   0   0   22	
0   0   0   0   0   0   0   0   0   0   23	
0   0   0   0   0   0   0   0   0   0   24	
0   0   0   0   0   4   0   0   0   0   25	
0   0   0   0   0   0   0   0   0   0   26	
0   0   0   0   0   0   0   0   0   0   27	
0   0   0   0   0   0   0   0   0   0   28	
0   0   0   0   0   0   0   0   0   0   29	
0   0   0   0   0   0   0   0   0   0
*/
