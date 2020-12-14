package src;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;

public class MS2Frame{
//    private static int index = 0;
    public static final String FRAME_SUFFIX = "PNG";
    public static final int WHITE = -1;
    public static final int SUM = 24;
    public static void main(String[] args) throws Exception {
	for(int i=0;i<args.length;++i){
		BufferedImage s = ImageIO.read(new FileInputStream(args[i]));
		BufferedImage m = ImageIO.read(new FileInputStream(s2m(args[i])));
		ms2Frame(m,s);
	}
    }
    public static String s2m(String path){
	return path.substring(0, path.length() - 4 );
	//return path.replaceFirst("pWarehouse/S", "pWarehouse/M");
    }
    public static void ms2Frame(BufferedImage m, BufferedImage s)throws Exception {
//	String path = "./frames/"+s.getHeight();
//	new File(path).mkdirs();
//	String prefix = path+"/"+s.hashCode();
	ImageIO.write(m, FRAME_SUFFIX, System.out);//new File(prefix + extend(0,3) +"."+FRAME_SUFFIX));
        for(int f=1; f<=SUM; ++f){
		for(int i=0; i<m.getWidth(); ++i){
			for(int j=0; j<m.getHeight(); ++j){
				if(s.getRGB(i,j) != WHITE)
					continue;
				int source = m.getRGB(i,j);
				int red = (source&0xff0000)/0x10000;
				int step = (0xff - red)/SUM;
				m.setRGB(i, j, source + 0x10000*f*step );
			}
		}
		ImageIO.write(m, FRAME_SUFFIX, System.out);//new File(prefix + extend(f,3) +"."+FRAME_SUFFIX));
	}
        for(int f=1; f<=SUM; ++f){
		for(int i=0; i<m.getWidth(); ++i){
			for(int j=0; j<m.getHeight(); ++j){
				if(s.getRGB(i,j) != WHITE)
					continue;
				int source = m.getRGB(i,j);
				int red = (source&0xff0000)/0x10000;
				int step = (red)/SUM;
				m.setRGB(i, j, source - 0x10000*f*step );
			}
		}
		ImageIO.write(m, FRAME_SUFFIX, System.out);//new File(prefix + extend(f,3) +"."+FRAME_SUFFIX));
	}
    }
    public static String extend(int num, int length){
	String result = ""+num;
	int sub = length - result.length();
	switch(sub){
		case 0: return result;
		case 1: return "0"+result;
		case 2: return "00"+result;
		case 3: return "000"+result;
	}
	throw new AssertionError();
    }
}
