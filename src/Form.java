package src;

import javax.crypto.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**此类处理界面及监听器内部类*/
public class Form extends JFrame implements ActionListener {
    private final javax.swing.Timer hdp = new javax.swing.Timer( Tool.CLOCK_HDP , this);
    private final BlockingQueue<Image> scales = new ArrayBlockingQueue<>( Tool.QUEUE_SIZE );
    private final SecretKey sks;
    private final Properties xml;
    private Image total;

    @Override
    public void actionPerformed(ActionEvent e) {//hdp.start()之前不检查scales.isEmpty()会导致窗体卡住
        nextImage();
    }
    private void nextImage() {
        if(scales.isEmpty()){
            hdp.stop();
            Tool.savePropAsk(xml); //太卡时可能会导致多问几次
        } else {
            takeFromQueue();
            repaint();
        }
    }
    private void takeFromQueue() {
        try {
            total = scales.take();
        } catch (InterruptedException e) {
            JOptionPane.showMessageDialog(null, "程序退出：队列take()异常");
            e.printStackTrace();
            System.exit(0xead);
        }
    }
    public static void main(String[] args) {
        Form frame = new Form();
        frame.setVisible(true);
    }
    public Form(){
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setLayout(new BorderLayout());
        xml = Tool.readProp();
        sks = Tool.passwordDialog();

        JPanel panelWest = new JPanel(new GridLayout(5,1));
        panelWest.add(buttonImport());
        panelWest.add(buttonExport());
        panelWest.add(buttonDecrypt());
//        panelWest.add(buttonHDP());
        panelWest.add(buttonSetClock());
        panelWest.add(buttonNext());
        this.add(panelWest, BorderLayout.WEST);
        JPanel panelImage = new JPanel(){
            @Override
            public void paint(Graphics g){
                if(total != null) g.drawImage(total,0,0,null);//###为什么背景不行
            }
        };
        this.add(panelImage, BorderLayout.CENTER);
    }
    private JButton buttonImport() {
        JButton bi = new JButton("入库");
        bi.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int option = JOptionPane.showConfirmDialog(bi, "窗体将隐藏，完成后将得到弹窗通知",
                        "准备好文件了吗？", JOptionPane.YES_NO_OPTION);
                if(option != JOptionPane.YES_OPTION) return;
                Form.this.setVisible(false);
                Tool.doImport(sks);
                Form.this.setVisible(true);
            }
        });
        return bi;
    }
    private JButton buttonExport() {
        JButton be = new JButton("出库");
        be.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new Thread(() -> {
                    File[] files = Tool.openFileChooser(JFileChooser.FILES_AND_DIRECTORIES, null);
                    if(files!=null){
                        for (File file : files){
                            Tool.doExport(sks, file);
                            if( ! file.isDirectory() )//直接选中非目录文件将写入配置文件
                                Tool.increaseProp(file, xml);
                        }
                    }
                    Tool.savePropAsk(xml);
                }).start();
            }
        });
        return be;
    }
    private JButton buttonSetClock() {
        JButton bc = new JButton("放映或者重设间隔");
        bc.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if( ! hdp.isRunning()){
                    hdp.start();
                    return;
                }
                hdp.stop();
                String[] choice = new String[]{"暂停","1","2","3","4","5","6"};
                int index = JOptionPane.showOptionDialog(bc,"请选择幻灯片间隔（秒）","看几分钟？",
                        JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,null,choice,"暂停");
                if(index == 0)
                    Tool.savePropAsk(xml);
                else {
                    hdp.setDelay( 1000*index );
                    hdp.start();
                }
            }
        });
        return bc;
    }
    private JButton buttonNext() {
        JButton bEast = new JButton();
        bEast.setText(Tool.TEXT_NEXT);
        bEast.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                nextImage();
            }
        });
        return bEast;
    }
    private JButton buttonDecrypt() {
        JButton jp = new JButton("读取密文");
        jp.addMouseListener(new AdapterDecrypt());
        return jp;
    }
    class AdapterDecrypt extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent me) {
            //选中的文件夹集合
            File[] dirs = Tool.openFileChooser(JFileChooser.DIRECTORIES_ONLY, Tool.defaultIfCancel());
            //输入前缀正则表达式以挑选制定标签图片
            String prefixReg = JOptionPane.showInputDialog(null,
                    "请输入正则表达式前缀，程序将会用(input).*进行匹配", Tool.DEFAULT_REG);
            //选出所有匹配正则表达式且未读过的图片
            ArrayList<File> pictures = readMatchExceptProp(dirs, prefixReg);
            //选择阅读时间，并算出待阅读图片数量
            final int count = Math.min(pictures.size(), Tool.minutesToCount(Tool.chooseMinutes()));
            //把pictures的前count项乱序
            Tool.randomSortPrefix(new Random(), pictures, count);
            //缩放并put进多线程队列
            new Thread(() -> decryptScalePut(pictures, count)).start();
            String m = "已从"+ dirs.length +"个文件夹中随机抽取" + count +"张图片";
            JOptionPane.showMessageDialog(null, m);
        }
        private ArrayList<File> readMatchExceptProp(File[] directories, String prefixReg) {
            ArrayList<File> pictures = new ArrayList<>();
            for (File dir : directories) {
                File[] totalFiles = dir.listFiles();
                if (totalFiles == null) {
                    JOptionPane.showMessageDialog(null, "请注意目录层次");
                    System.exit(11);
                }
                for (File f : totalFiles)
                    if (Tool.isNewPicture(f, xml) && Tool.matchPrefix(f.getName(), prefixReg))
                        pictures.add(f);
                if (Tool.DEBUG) System.out.println("已读入" + dir.getPath());
            }
            if(pictures.isEmpty()){
                JOptionPane.showMessageDialog(null,"正则表达式无匹配或ALL_HAS_READ，请手动检查");
                System.exit(0xC08F19);
            }
            return pictures;
        }
        private void decryptScalePut(ArrayList<File> pictures, int count) {
                for (int i = 0; i < count; i++) {
                    File f = pictures.get(i);
                    if(Tool.DEBUG)System.out.println("isDir()=="+f.isDirectory()+"\t"+f.getPath());
                    BufferedImage bImage = readImageIO(f);
                    if (bImage == null) {
                        int option = JOptionPane.showConfirmDialog(null,
                                "读取到非图片文件或密码错误，是否跳过当前文件并继续？",
                                "解密或读取异常", JOptionPane.YES_NO_OPTION);
                        if (option == JOptionPane.NO_OPTION) System.exit(0xa55d);
                        continue;
                    }
                    System.out.println("ready to put:\t" + f.getPath());
                    putToQueue(bImage);
                    Tool.increaseProp(f, xml);//put后已读标记+1
                }
        }
        private BufferedImage readImageIO(File file) {
            try {
                return ImageIO.read(Tool.doGetInputStream(sks, file));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "程序退出：ImageIO.read()异常");
                e.printStackTrace();
                System.exit(0x10ead);
                return null;
            }
        }
        private void putToQueue(BufferedImage bImage) {
            try {
                scales.put(Tool.scalePH(bImage));
            } catch (InterruptedException e) {
                JOptionPane.showMessageDialog(null, "程序退出：队列put()异常");
                e.printStackTrace();
                System.exit(0xead);
            }
        }
    }
}
