package src;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;

public class SelectWDivH{
    public static final int TARGET = 150 ; // 66 ; // 对大部分相机而言，纵向图宽高比约为0.66;
    public static final int BOX = 100;
    private static int[] divCount = new int[BOX*3+1];
    public static void main(String[] args) throws Exception {
/*
	if(args.length == 0) {
		System.out.println("用-r选项生成横向图的png副本，-c或其他选项生成纵向图的png副本");
		return;
	}
*/
	for(int i=0;i<args.length;++i){
		BufferedImage m = ImageIO.read(new FileInputStream(args[i]));
		double len = (double)BOX;
		double folder = m.getWidth() * len /m.getHeight();
		int lenMul = (int)folder;
		divCount[lenMul] += 1;
		
		if( lenMul==TARGET )
			ImageIO.write(m, "png", new File(args[i]+".png"));
		
		if(i%100==0) System.out.println("还剩"+(args.length-i)); 
	}
	System.out.println("66----------"+divCount[66]);
	System.out.println("150---------"+divCount[150]);
	showCount();
    }
    public static void showCount() {
	for(int i=1;i<=BOX*3;++i){
		if(i%10==1)
			System.out.print("\n"+(i/10)+"::");
		System.out.print(divCount[i]+"\t");
	}
    }
}
