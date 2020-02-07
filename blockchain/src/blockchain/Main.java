package blockchain;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.*;
import java.nio.file.Paths;

import java.security.*;
import java.security.spec.*;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

class SaveFile implements Serializable{
    static final long serialVersionUID = 1;
    byte[] RSAEncryptedAESKey;
    byte[] AESEncryptedFile; 
}

class KeyFile implements Serializable {
    static final long serialVersionUID = 1;
    byte[] publicKey;
    byte[] privateKey;
}

class Encryptor {
 
    private static final String KEYS_PATH = "keys";

    private static PrivateKey privateKey = null;
    private static PublicKey publicKey = null;
    private static SecretKey AESKey = null;
        
    static private void getPrivateKeyFromBytes(byte[] keyBytes){   
        try {
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            privateKey = kf.generatePrivate(spec);               
        } catch (Exception e) {
            throw new RuntimeException(e);
        }     
    }
    static private void getPublicKeyFromBytes(byte[] keyBytes){   
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            publicKey = kf.generatePublic(spec);               
        } catch (Exception e) {
            throw new RuntimeException(e);
        }     
    }

    static private void saveKeysToFile(){
        try {
            KeyFile kf = new KeyFile();
            kf.privateKey = getPrivateKey().getEncoded();
            kf.publicKey = getPublicKey().getEncoded();
            FileOutputStream fos = new FileOutputStream(KEYS_PATH);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(kf);
            oos.close();            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    static private void loadKeysFromFile(){
        try {
            KeyFile kf = byteArrayToObject(Files.readAllBytes(Paths.get(KEYS_PATH)), KeyFile.class);
            getPrivateKeyFromBytes(kf.privateKey);
            getPublicKeyFromBytes(kf.publicKey);
        } catch (Exception e) {
            generatePublicAndPrivateKeys();
        }

    }

    static private void generatePublicAndPrivateKeys(){
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair keyPair = kpg.generateKeyPair();
            
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();
            saveKeysToFile();             
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
	
    static private SecretKey getAESKey(){
        try {
            if (AESKey == null){
                KeyGenerator k = KeyGenerator.getInstance("AES");
                k.init(128);
                AESKey = k.generateKey();
            }
            return AESKey;            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static private byte[] AESEncrypt(byte[] input){
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, getAESKey());
            return c.doFinal(input);            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static private byte[] RSAEncrypt(byte[] input){
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.ENCRYPT_MODE, getPublicKey());
            return c.doFinal(input);            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static private byte[] RSADecrypt(byte[] input){
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.DECRYPT_MODE, getPrivateKey());
            return c.doFinal(input);            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	static private PrivateKey getPrivateKey(){
        try {
            if (privateKey == null){
                loadKeysFromFile();
            }
            return privateKey;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	static PublicKey getPublicKey(){
        try {
            if (publicKey == null){
                loadKeysFromFile();
            }
            return publicKey;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    static <T> T byteArrayToObject(byte[] arr, Class<T> clazz){
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(arr);
            ObjectInputStream oos = new ObjectInputStream(bis);
            return clazz.cast(oos.readObject());            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void setAESKey(SecretKey sk){
        AESKey = sk;
    }

    static SecretKey loadSecretKey(byte[] encodedKey){
        encodedKey = RSADecrypt(encodedKey);
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    }

    static byte[] getRSAEncryptedAESKey(){
        try {
            return RSAEncrypt(getAESKey().getEncoded());       
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    

	static byte[] sign(byte[] data) {
        try {
            Signature rsa = Signature.getInstance("SHA1withRSA"); 
            rsa.initSign(getPrivateKey());
            rsa.update(data);
            return rsa.sign();            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
	static boolean verifySignature(byte[] data, byte[] signature) {
        try {
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(getPublicKey());
            sig.update(data);		
            return sig.verify(signature);            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    static byte[] AESDecrypt(byte[] input){
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, getAESKey());
            return c.doFinal(input);            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    static byte[] objToAESEncryptedBytes(Object obj){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            return AESEncrypt(bos.toByteArray());            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte elem : hash) {
                String hex = Integer.toHexString(0xff & elem);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

class BlockchainSubmission {
    final int magicNumber;
    final int minerId;
    final long timeToGenerate;
    final int zeroes;

    BlockchainSubmission(int magicNumber, int minerId, long timeToGenerate, int zeroes){
        this.magicNumber = magicNumber;
        this.minerId = minerId;
        this.timeToGenerate = timeToGenerate;
        this.zeroes = zeroes;
    }
}

class Message implements Serializable {
    static final long serialVersionUID = 0;
    final String userId;
    final String message;
    byte[] signature;
    int id;
    PublicKey publicKey;

    Message(String userId, String message){
        this.userId = userId;
        this.message = message;
    }

    public byte[] getPreliminaryHash(){
        return (userId + message + id).getBytes();
    }

    @Override
    public String toString() {
        return String.format("%03d", id) + "| User \"" + userId + "\" says: " + message;
    }
}

class Block implements Serializable {

    static final long serialVersionUID = 1;
    static final String DEFAULTHASH = "0";

    private final long id;
    private final long timeStamp = new Date().getTime();
    private final String prevSha256Hash;
    private String selfSha256Hash = DEFAULTHASH;
    private int magicNumber;
    private long timeToGenerate;
    private int minedBy;
    private boolean isHashSet = false;
    private List<Message> messages;

    Block(long id, String prevBlockHash) {
        this.id = id;
        this.prevSha256Hash = prevBlockHash;
    }

    private void setSelfSha256Hash() {
        this.selfSha256Hash = generateSelfSha256Hash();
    }

    private String generateSelfSha256Hash(){
        return Encryptor.applySha256(getPreliminaryHash() + magicNumber);
    }

    String getPrevBlockHash() {
        return prevSha256Hash;
    }

    String getSelfSha256Hash() {
        return selfSha256Hash;
    }

    long getId() {
        return this.id;
    }

    long getTimeToGenerate() {
        return this.timeToGenerate;
    }

    List<Message> getMessages(){
        return this.messages;
    }

    void setMessages(List<Message> messages){
        this.messages = messages;
    }

    public boolean isValidMagicNumber(int magicNumber, int zeroes) {
        String sha256Hash = Encryptor.applySha256(getPreliminaryHash() + magicNumber);
        return sha256Hash.startsWith("0".repeat(zeroes));
    }

    public synchronized String getPreliminaryHash() {
        return prevSha256Hash + id + timeStamp;
    }

    public synchronized void setMinedDetails(BlockchainSubmission submission){
        if (!isHashSet && isValidMagicNumber(submission.magicNumber, submission.zeroes)){
            isHashSet = true;
            this.magicNumber = submission.magicNumber;
            this.minedBy = submission.minerId;
            this.timeToGenerate = submission.timeToGenerate;
            this.setSelfSha256Hash();
        }
    }

    @Override
    public String toString() {
        StringBuilder s =  new StringBuilder(
            "Block:\n" +
            "Created by miner # " + minedBy + "\n" +
            "Id: " + id + "\n" +
            "Timestamp: " + timeStamp + "\n" +
            "Magic number: " + magicNumber + "\n" +
            "Hash of the previous block:\n" + prevSha256Hash + "\n" +
            "Hash of the block:\n" + selfSha256Hash + 
            "\nBlock data: ");
        if (!messages.isEmpty()){
            for (Message message : messages){
                s.append("\n").append(message.toString());
            }
        } else {
            s.append("no messages");
        }
        s.append("\nBlock was generating for " + timeToGenerate / 1000f + " seconds");
        return s.toString();
    }
}

class Blockchain implements Serializable {

    static final long serialVersionUID = 1;
    private volatile int zeroes;
    private int messageId = 0;
    private final List<Block> blockChain = new ArrayList<>();
    private Block blockToMine;
    private String NChangeMessage;
    private volatile List<Message> messages = new ArrayList<>();

    Blockchain(int zeroes) {
        this.zeroes = zeroes;
        blockToMine = new Block(1, Block.DEFAULTHASH);
        blockToMine.setMessages(new ArrayList<>());
    }

    private synchronized void processNChange(long timeToGenerate){
        if (timeToGenerate > 60 * 1000) {
            decrementZeroes();
            NChangeMessage = "N was decreased to " + getZeroes();
        } else if (timeToGenerate < 10 * 1000) {
            incrementZeroes();
            NChangeMessage = "N was increased to " + getZeroes();
        } else {
            NChangeMessage = "N stays the same";
        }
    }

    synchronized String getNChangeMessage() {
        return NChangeMessage;
    }

    synchronized int getZeroes() {
        return this.zeroes;
    }

    synchronized boolean isBlockchainValid() {
        for (int i = 0; i < blockChain.size(); i++) {
            Block block = blockChain.get(i);
            String currHashOfPrev = block.getPrevBlockHash();
            String prevHashActual = i == 0 ? Block.DEFAULTHASH : blockChain.get(i - 1).getSelfSha256Hash();
            if (!currHashOfPrev.equals(prevHashActual)) {
                return false;
            }
            int maxMessageId = block.getMessages().stream().mapToInt(message -> message.id).max().orElse(0);
            int lastMessageId = Integer.MIN_VALUE;
            for (Message message : block.getMessages()){
                int id = message.id;
                if (id > maxMessageId || id < lastMessageId ||!Encryptor.verifySignature(message.getPreliminaryHash(), message.signature)){
                    return false;
                }
                lastMessageId = id;
            }
        }
        return true;
    }

    synchronized void submitMessage(Message message){
        message.id = messageId++;
        message.signature = Encryptor.sign(message.getPreliminaryHash());
        message.publicKey = Encryptor.getPublicKey();
        messages.add(message);
    }

    synchronized void addToChain(Block block) {
        synchronized (block){
            blockChain.add(block);
            processNChange(block.getTimeToGenerate());
            blockToMine = new Block(block.getId() + 1, block.getSelfSha256Hash());            
            blockToMine.setMessages(messages);
            this.messages = new ArrayList<>();
        }
    }

    synchronized boolean submitSubmission(BlockchainSubmission submission){
        Block block = getBlockToMine();
        if (block.isValidMagicNumber(submission.magicNumber, zeroes)){
            block.setMinedDetails(submission);
            addToChain(block);
            return true;
        }
        return false;
    }

    synchronized Block getLatestCompleteBlock() {
        return blockChain.isEmpty() ? null : blockChain.get(blockChain.size() - 1);
    }

    synchronized Block getBlockToMine() {
        return blockToMine;
    }

    synchronized void incrementZeroes() {
        this.zeroes++;
    }

    synchronized void decrementZeroes() {
        this.zeroes--;
    }
}

class Miner implements Callable<BlockchainSubmission> {

    private final int id;
    private Block block;
    private int leadingZeroes;
    private boolean isRunning = false;

    Miner(int id) {
        this.id = id;
    }

    boolean isRunning(){
        return isRunning;
    }

    void set(Block block, int leadingZeroes) {
        this.block = block;
        this.leadingZeroes = leadingZeroes;
    }

    @Override
    synchronized public BlockchainSubmission call() {
        isRunning = true;
        Thread.currentThread().setName("Miner " + id);
        long startTime = new Date().getTime();

        Random rand = new Random();
        int magicNumber;
        do {
            if (Thread.currentThread().isInterrupted()) {
                isRunning = false;
                throw new RuntimeException();
            }
            magicNumber = Math.abs(rand.nextInt());
        } while (!block.isValidMagicNumber(magicNumber, leadingZeroes));
        isRunning = false;
        return new BlockchainSubmission(magicNumber, id, new Date().getTime() - startTime, leadingZeroes);
    }

}

class MinerManager {

    private final Blockchain blockchain;
    private final List<Miner> miners = new ArrayList<>();
    private final ExecutorService executor;

    MinerManager(Blockchain blockchain, int threads) {
        this.blockchain = blockchain;
        this.executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            miners.add(new Miner(i));
        }
    }

    synchronized void close() {
        executor.shutdown();
    }

    synchronized void mine() {
        try {
            boolean done = false;
            while (!done){
                int zeroes = blockchain.getZeroes();
                Block block = blockchain.getBlockToMine();
                for (Miner miner : miners) {
                    while (miner.isRunning()){}
                    miner.set(block, zeroes);
                }
                BlockchainSubmission result = executor.invokeAny(miners);
                done = blockchain.submitSubmission(result);
                if (!blockchain.isBlockchainValid()){
                    throw new Exception();
                }
            }
        } catch (Exception e) {            
            close();
            throw new RuntimeException(e);
        }
    }

}

class MessageSubmitter implements Runnable {
    private Blockchain blockchain;

    MessageSubmitter(Blockchain blockchain){
        this.blockchain = blockchain;
    }
    @Override
    public void run() {
        int i = 1;
        Thread.currentThread().setName("Blockchain Message Submitter");
        while (true){
            try {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                Message message = new Message(
                        Integer.toString(i++),
                        "This message was generated on the timestamp " + new Date().getTime());
                blockchain.submitMessage(message);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}

public class Main {

    private static String PATH = "blockchain.db";

    private static Blockchain load(String path) {
        try {
            FileInputStream fileIn = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            SaveFile sf = (SaveFile) in.readObject();
            SecretKey sk = Encryptor.loadSecretKey(sf.RSAEncryptedAESKey);
            Encryptor.setAESKey(sk);
            Blockchain bc = Encryptor.<Blockchain>byteArrayToObject(Encryptor.AESDecrypt(sf.AESEncryptedFile), Blockchain.class);
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
            save(blockchain, PATH);
            new Thread(() -> save(blockchain, PATH)).start();
        }
        submitter.interrupt();
        minerManager.close();
        save(blockchain, PATH);
    }
}
