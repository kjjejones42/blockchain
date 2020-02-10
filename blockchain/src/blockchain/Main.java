package blockchain;

import java.io.*;

class Main {

    private static String PATH = "blockchain.db";
    private static Encryptor encryptor;

    private static Blockchain load(String path) {
        try {
            FileInputStream fileIn = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            SaveFile sf = (SaveFile) in.readObject();
            encryptor.loadRSAEncryptedAESKeyFromBytes(sf.a);
            Blockchain bc = Blockchain.class.cast(encryptor.AESEncryptedBytesToObj(sf.b));
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
                SaveFile sf = new SaveFile(
                    encryptor.getRSAEncryptedAESKey(),
                    encryptor.objToAESEncryptedBytes(bc));
                FileOutputStream fileOut = new FileOutputStream(path);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(sf);
                out.close();
                encryptor.saveRSAKeysToFile();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        encryptor = Encryptor.getInstance();
        final Blockchain blockchain;
        if (new File(PATH).exists()) {
            blockchain = load(PATH);
        } else {
            blockchain = new Blockchain(0);
        }
        MinerManager minerManager = new MinerManager(blockchain, 10);
        Thread submitter = new Thread(new TransactionSubmitter(blockchain, 10));
        submitter.start();
        for (int i = 0; i < 5; i++) {
            minerManager.mine();
            System.out.println(blockchain.getLatestCompleteBlock());
            System.out.println(blockchain.getNChangeMessage() + "\n");
            new Thread(() -> save(blockchain, PATH)).start();
        }
        minerManager.close();
        save(blockchain, PATH);
    }
}
