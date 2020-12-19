package src;
import java.io.*;
import java.awt.Color;
import java.awt.image.*;
import javax.imageio.*;

public class MS2Frame{
    public static final boolean DEBUG = true;
    public static final int repeat = DEBUG ? 2 : 3;
    public static final int HALF_HDP = DEBUG ? 6 : 12;
    public static final String FRAME_FORMAT = "PNG"; //我的ffmpeg不支持tiff
    
    private static boolean F_OUT_DEBUG = false;
    private static String F_OUT_PATH = "F_OUT_DEBUG";
    private static int F_OUT_SUFFIX = 0;
    private static OutputStream sOut() throws FileNotFoundException {
	if( ! F_OUT_DEBUG) return System.out;
	new File(F_OUT_PATH).mkdir();
	return new FileOutputStream(F_OUT_PATH+"/"+(++F_OUT_SUFFIX));
    }
    public static String pathTranslate(String path) {
	String result = path.substring(0, path.length() - 4 );
	// path.replaceFirst("pWarehouse/S", "pWarehouse/M");
	if(new File(result).isFile()) 
		return result;
	else
		throw new AssertionError();
    }
    public static void main(String[] sPaths) throws Exception {
	if(sPaths == null || sPaths.length == 0){
		System.out.println("以指示选区的掩码图片的路经集作为参数，程序会自动去掉后缀以匹配原图片");
		return;
	}
	for(int k=0;k< sPaths.length ;++k){
		BufferedImage s = ImageIO.read(new FileInputStream(sPaths[k]));
		BufferedImage m = ImageIO.read(new FileInputStream(pathTranslate(sPaths[k])));
		pictureToFrames(m, s);
	}
    }
    public static void pictureToFrames(BufferedImage m, BufferedImage s) throws Exception {
	if( s.getWidth() > m.getWidth() || s.getHeight() > m.getHeight() )
		throw new AssertionError();
	int[][] step = sToStep(m,s);
	//递归写入
//	for(int t=0;t<repeat;++t) recurWrite(m, step, 0); //用frameIndex=0启动递归写入
	/*循环写入
	*/
	BufferedImage[] frames = new BufferedImage[HALF_HDP*2];
	frames[0] = m;
	frames[frames.length-1] = m;
	for(int k=1;k< HALF_HDP ;k++){
		frames[k] = nextFrame(imClone(frames[k-1]), step, 1);
		frames[frames.length-k-1] = frames[k];
	}
	for(int t=0;t<repeat;++t)	//TODO:写入优化
		for(int f=0;f<frames.length;++f)
			ImageIO.write(frames[f], FRAME_FORMAT, sOut());
    }
    public static int[][] sToStep(final BufferedImage m, final BufferedImage s) throws Exception {
	int[][] step = new int[s.getWidth()][s.getHeight()];
	for(int i=0; i<s.getWidth(); ++i){
		for(int j=0; j<s.getHeight(); ++j){
			if(s.getRGB(i,j) != Color.WHITE.getRGB()){
				step[i][j] = 0;
				continue;
			}//选区外为0，选区内为step值
			int red = ( m.getRGB(i,j)/0x10000 ) & 0xff ;
			int stepRed = (255 - red)/HALF_HDP;	//256做被减数有时会导致颜色抖动
			while(stepRed<16) { //通过此阈值处理过亮的像素
				Color source = new Color(m.getRGB(i,j), true);
				Color dest = source.darker();
				m.setRGB(i,j,dest.getRGB());
				red = ( m.getRGB(i,j)/0x10000 ) & 0xff ;
				stepRed = (255 - red)/HALF_HDP;	//256做被减数有时会导致颜色抖动
			}
		//	if(stepRed<1) stepRed = 0 - red/HALF_HDP;
			step[i][j] = 0x10000*stepRed;
		/*	if(stepRed<1){
				step[i][j]= -( (m.getRGB(i,j)&0x00ffffff)%0x10000 )/HALF_HDP;
				if(DEBUG)System.err.print(step[i][j]+" ");
			}
		*/
		//	if(DEBUG)System.err.print(stepRed+"#");
		}
	}
	if(DEBUG)ImageIO.write(s, FRAME_FORMAT, new FileOutputStream("step.png"));
	return step;
    }
    /** 写入第frameIndex帧到第(2*HALF_HDP-frameIndex-1)帧 */
    public static void recurWrite(BufferedImage m, final int[][] step, final int frameIndex) throws Exception {
	if(frameIndex == HALF_HDP) return; //frameIndex自增到了中间就不再展开新递归
	if(frameIndex < 0 || frameIndex > HALF_HDP)
		throw new AssertionError();
	//写入第frameIndex帧
	ImageIO.write(m, FRAME_FORMAT, sOut());
	nextFrame(m, step, 1);
	//递归写入中间帧
	recurWrite(imClone(m), step, 1+frameIndex);
	//写入第(2*HALF_HDP-frameIndex-1)帧
	ImageIO.write(m, FRAME_FORMAT, sOut());
//	if(frameIndex != 0) nextFrame(m, step, -1);
    }
    /** 此方法会修改m */
    public static BufferedImage nextFrame(BufferedImage m, final int[][] step, final int addOrSub){
	for(int i=0; i<step.length; ++i)
		for(int j=0; j<step[0].length; ++j){
			m.setRGB( i, j, m.getRGB(i,j) + addOrSub * step[i][j] );
		}
	return m;
    }
    public static BufferedImage imClone(BufferedImage bimage){
	BufferedImage bimage2 = new BufferedImage(bimage.getWidth(), bimage.getHeight(), bimage.getType());
        bimage2.setData(bimage.getData());
	return bimage2;
    }
}
