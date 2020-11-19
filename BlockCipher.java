import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/**此类处理加密流和解密流*/
public class BlockCipher {
    public static final int KEY_LENGTH = 128; //256的代价是轮数和破解理论可能性
    public static final int IO_BLOCK_SIZE = 1024 * 64; //经测试最佳IO块大小：64KB
    public static byte[] block = new byte[IO_BLOCK_SIZE];
    public static final String HASH_ALGORITHM = "MD5";//"SHA-1";
    public static final String ALGORITHM = "AES";
    public static final String MODE_PAD = "/CBC/PKCS5Padding";

    public static CipherInputStream getInputStream(final SecretKey sks, File f)
            throws IOException, NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException {
        FileInputStream in = new FileInputStream(f);
        Cipher cp = Cipher.getInstance(ALGORITHM+MODE_PAD);
        byte[] ivRand = new byte[cp.getBlockSize()];
        int length = in.read(ivRand);
        if(length!=cp.getBlockSize()) throw new AssertionError();
        cp.init(Cipher.DECRYPT_MODE, sks, new IvParameterSpec(ivRand));
        return new CipherInputStream(in, cp);
    }
    public static void encryptFile(final SecretKey sks, String inFile, String outFile)
            throws IOException, NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException {
        FileInputStream in = new FileInputStream(inFile);
        FileOutputStream out = new FileOutputStream(outFile);
        Cipher cp = Cipher.getInstance(ALGORITHM+MODE_PAD);
        byte[] ivRand = new byte[cp.getBlockSize()];
        (new java.util.Random()).nextBytes(ivRand);
        out.write(ivRand);
        cp.init(Cipher.ENCRYPT_MODE, sks, new IvParameterSpec(ivRand));
        CipherOutputStream cOut = new CipherOutputStream(out, cp);
        int round = in.available()/block.length;
        for(int i=1; i<=round; ++i){
            in.read(block);
            cOut.write(block);
        }
        int rest = in.available();
        if(rest>0) {
            byte[] restBlock = new byte[rest];
            in.read(restBlock);
            cOut.write(restBlock);
        }
        cOut.close();
        out.close();
        in.close();
    }
    public static void decryptFile(final SecretKey sks, String inFile, String outFile) throws InvalidKeyException,
            IOException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        FileInputStream in = new FileInputStream(inFile);
        FileOutputStream out = new FileOutputStream(outFile);
        Cipher cp = Cipher.getInstance(ALGORITHM+MODE_PAD);
        byte[] ivRand = new byte[cp.getBlockSize()];
        in.read(ivRand);
        cp.init(Cipher.DECRYPT_MODE, sks, new IvParameterSpec(ivRand));
        CipherOutputStream cOut = new CipherOutputStream(out, cp);
        int round = in.available() / block.length;
        for(int i=1; i<=round; ++i){
            in.read(block);
            cOut.write(block);
        }
        int rest = in.available();
        if(rest>0) {
            byte[] restBlock = new byte[rest];
            in.read(restBlock);
            cOut.write(restBlock);
        }
        cOut.close();
        out.close();
        in.close();
    }
    public static byte[] hashString(String pw) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new AssertionError();
        }
        md.update(pw.getBytes());
        return md.digest();
    }
    public static SecretKey bytes2SecretKey(byte[] b) { return bytes2SecretKey(b, KEY_LENGTH/8); }
    public static SecretKey bytes2SecretKey(byte[] bytesKey, int size){
        int offset = bytesKey.length - size;
        if(offset>=0) return new SecretKeySpec(bytesKey, offset, size, ALGORITHM);
        byte[] b = new byte[size];
        for(int i = 0; i < size; ++i) {
            int j = i + offset;
            b[i] = (j<0) ? 0 : bytesKey[j];
        }
        return new SecretKeySpec(b, ALGORITHM);
    }
}
