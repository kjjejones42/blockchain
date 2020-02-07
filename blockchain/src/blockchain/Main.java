package blockchain;

import java.io.*;
import javax.crypto.*;

public class Main {

    private static String PATH = "blockchain.db";

    private static Blockchain load(String path) {
        try {
            FileInputStream fileIn = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            SaveFile sf = (SaveFile) in.readObject();
            SecretKey sk = Encryptor.loadSecretKey(sf.RSAEncryptedAESKey);
            Encryptor.setAESKey(sk);
            Blockchain bc = Blockchain.class.cast(Encryptor.byteArrayToObject(Encryptor.AESDecrypt(sf.AESEncryptedFile)));
            in.close();
            return bc;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static void save(Blockchain bc, String path) {
        synchronized (bc) {
            try {
                SaveFile sf = new SaveFile();
                sf.RSAEncryptedAESKey = Encryptor.getRSAEncryptedAESKey();
                sf.AESEncryptedFile = Encryptor.objToAESEncryptedBytes(bc);
                FileOutputStream fileOut = new FileOutputStream(path);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(sf);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        final Blockchain blockchain;
        if (new File(PATH).exists()) {
            blockchain = load(PATH);
        } else {
            blockchain = new Blockchain(0);
        }
        MinerManager minerManager = new MinerManager(blockchain, 10);
        Thread submitter = new Thread(new MessageSubmitter(blockchain));
        submitter.start();
        for (int i = 0; i < 5; i++) {
            minerManager.mine();
            System.out.println(blockchain.getLatestCompleteBlock());
            System.out.println(blockchain.getNChangeMessage() + "\n");
            new Thread(() -> save(blockchain, PATH)).start();
        }
        submitter.interrupt();
        minerManager.close();
        save(blockchain, PATH);
    }
}
