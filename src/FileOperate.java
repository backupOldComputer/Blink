package src;

import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.swing.*;

/**此类处理文件系统的创建和写入操作。FileOperate的所有公有方法都不抛出异常给上级。*/
public class FileOperate {
    public static final boolean IS_WIN = false;
    public static final String SEP = IS_WIN ? "\\\\" : "/";
    public static final String WAREHOUSE_DIR = "..";	//位于工程文件夹上级
    public static final String mWAREHOUSE = "mWarehouse";
    public static final String cWAREHOUSE = "cWarehouse";
    public static final String pWAREHOUSE = "pWarehouse";
    public static final String M_IMPORT_SUCCESS = "SUCCESS";

    public static CipherInputStream getInputStream(final SecretKey sks, File f) {
        CipherInputStream result = null;
        try{
            result = BlockCipher.getInputStream(sks, f);
            /*
        } catch (java.io.FileNotFoundException fnfe){
            JOptionPane.showMessageDialog(null, "选中目录的层次不对");
            fnfe.printStackTrace();
            System.exit(0x1e0e1);
            return null;
            */
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            System.exit(0xea);
        }
        return result;
    }
    public static SecretKey password2Key(String pw){return BlockCipher.bytes2SecretKey(BlockCipher.hashString(pw));}
    private static void makeDirs(String path) {
        File dir = new File(path);
        System.out.println("正在创建："+dir);
        if( ! dir.exists()){
            boolean success = dir.mkdirs();
            if( ! success) throw new AssertionError();
        }else if( ! dir.isDirectory()){
            JOptionPane.showMessageDialog(null,"程序退出：待创建文件夹"+dir+"与已有文件重名");
            System.exit(0xe);
        }
    }
    public static String importAll(final SecretKey sks) {
        File[] mRoot = new File(WAREHOUSE_DIR+SEP+mWAREHOUSE).listFiles();
        if(mRoot==null) return "blank";
        try {
            for (File file : mRoot) {
                File[] m1 = file.listFiles();
                if(m1==null) throw new AssertionError();
                for (File value : m1) {
//                    int start = value.getPath().indexof(mWAREHOUSE + SEP, mRoot.size());
                    String dir = value.getPath().replaceFirst(mWAREHOUSE + SEP, cWAREHOUSE + SEP);
                    makeDirs(dir);
                    File[] m2 = value.listFiles();
                    if(m2==null) {
                        System.out.println("请确认二级目录");
                        return "dir structure";
                    }
                    for (File item : m2) {
                        String inFile = item.getPath();
                        String outFile = inFile.replaceFirst(mWAREHOUSE + SEP, cWAREHOUSE + SEP);
                        BlockCipher.encryptFile(sks, inFile, outFile);//BlockCipher共享了缓冲区，不是多线程安全的
                    }
                }
            }
        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            e.printStackTrace();
            System.exit(0x10);
        }
        return M_IMPORT_SUCCESS;
    }
    private static void exportFile(final SecretKey sks, File file) {
        String inFile = file.getPath();
        String outFile= inFile.replaceFirst(cWAREHOUSE+SEP, pWAREHOUSE+SEP);
        try {
            BlockCipher.decryptFile(sks, inFile, outFile);
        } catch (InvalidKeyException | IOException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
            e.printStackTrace();
            System.exit(0x10);
        }
    }
    public static void exportFa(final SecretKey sks, File file) {//todo 复杂性：返回值
        if(file.isFile()){
            exportFile(sks, file);
            return;
        }
        if(file.isDirectory()){
            String dir = file.getPath().replaceFirst(cWAREHOUSE+SEP, pWAREHOUSE+SEP);
            makeDirs(dir);
            File[] fs = file.listFiles();
            if(fs==null) throw new AssertionError();
            for (File f : fs) {
                exportFa(sks, f);
            }
        }
    }
}
