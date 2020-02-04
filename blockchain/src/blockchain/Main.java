package blockchain;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.io.*;
import java.util.concurrent.*;

class StringUtil {
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

    Block(long id) {
        this(id, DEFAULTHASH);
    }

    Block(long id, String prevBlockHash) {
        this.id = id;
        this.prevSha256Hash = prevBlockHash;
    }

    String getSelfSha256Hash() {
        return selfSha256Hash;
    }

    boolean isHashSet(){
        return isHashSet;
    }

    String getPrevBlockHash() {
        return prevSha256Hash;
    }

    private void setSelfSha256Hash() {
        this.selfSha256Hash = generateSelfSha256Hash();
    }

    private String generateSelfSha256Hash(){
        return StringUtil.applySha256(getPreliminaryHash() + magicNumber);
    }

    void setMagicNumber(int magicNumber) {
        this.magicNumber = magicNumber;
    }

    void setMinedBy(int minedBy) {
        this.minedBy = minedBy;
    }

    void setTimeToGenerate(long timeToGenerate) {
        this.timeToGenerate = timeToGenerate;
    }

    long getId() {
        return this.id;
    }

    long getTimeToGenerate() {
        return this.timeToGenerate;
    }

    synchronized String getPreliminaryHash() {
        return prevSha256Hash + id + timeStamp;
    }

    synchronized void setMinedDetails(int magicNumber, int id, long timeToGenerate){     
        if (!isHashSet){               
            isHashSet = true;
            this.setMagicNumber(magicNumber);
            this.setMinedBy(id);
            this.setTimeToGenerate(timeToGenerate);
            this.setSelfSha256Hash();
        }
    }

    @Override
    public String toString() {
        return "Block:\n" +
                "Created by miner # " + minedBy + "\n" +
                "Id: " + id + "\n" +
                "Timestamp: " + timeStamp + "\n" +
                "Magic number: " + magicNumber + "\n" +
                "Hash of the previous block:\n" + prevSha256Hash + "\n" +
                "Hash of the block:\n" + selfSha256Hash + "\n" +
                "Block was generating for " + timeToGenerate / 1000f + " seconds";
    }
}

class Blockchain implements Serializable, Iterable<Block> {

    static final long serialVersionUID = 1;
    private int zeroes;
    private final List<Block> blockChain = new ArrayList<>();
    private Block blockToMine;

    synchronized int getZeroes() {
        return this.zeroes;
    }

    Blockchain(int zeroes) {
        this.zeroes = zeroes;
        blockToMine = new Block(0);
    }

    @Override
    public Iterator<Block> iterator() {
        return blockChain.iterator();
    }

    synchronized boolean isBlockchainValid() {
        for (int i = 0; i < blockChain.size(); i++) {
            String currHashOfPrev = blockChain.get(i).getPrevBlockHash();
            String prevHashActual = i == 0 ? Block.DEFAULTHASH : blockChain.get(i - 1).getSelfSha256Hash();
            if (!currHashOfPrev.equals(prevHashActual)) {
                return false;
            }
        }
        return true;
    }

    synchronized void addToChain(Block block) {
        blockChain.add(block);
        blockToMine = new Block(block.getId() + 1, block.getSelfSha256Hash());
    }

    synchronized Block getLatestCompleteBlock() {
        return blockChain.get(blockChain.size() - 1);
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

class Miner implements Callable<Block> {

    private final int id;
    private Block block;
    private int zeroes;

    Miner(int id) {
        this.id = id;
    }

    void setBlockAndZeroes(Block block, int zeroes) {
        this.block = block;
        this.zeroes = zeroes;
    }

    private boolean isValidMagicNumber(String hash, int magicNumber, String zeroes) {
        String sha256Hash = StringUtil.applySha256(hash + magicNumber);
        return sha256Hash.startsWith(zeroes);
    }

    @Override
    public Block call() {
        Thread.currentThread().setName("Miner " + id);
        long startTime = new Date().getTime();

        String hash = block.getPreliminaryHash();
        String zeroesString = "0".repeat(zeroes);

        Random rand = new Random();
        int magicNumber;
        do {
            if (Thread.currentThread().isInterrupted()) {
                throw new RuntimeException();
            }
            magicNumber = Math.abs(rand.nextInt());
        } while (!isValidMagicNumber(hash, magicNumber, zeroesString));
        block.setMinedDetails(magicNumber, id, new Date().getTime() - startTime);
        return block;
    }

}

class MinerManager {

    private final Blockchain blockchain;
    private final List<Miner> miners = new ArrayList<>();
    private final ExecutorService executor;
    private String NChangeMessage;

    /**
     * @param blockchain           The BlockChain object to mine.
     * @param threads              The number of concurrent Miner threads.
     */
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

    synchronized String getNChangeMessage() {
        return NChangeMessage;
    }

    private synchronized void processNChange(long timeToGenerate){
        if (timeToGenerate > 60 * 1000) {
            blockchain.decrementZeroes();
            NChangeMessage = "N was decreased to " + blockchain.getZeroes();
        } else if (timeToGenerate < 10 * 1000) {
            blockchain.incrementZeroes();
            NChangeMessage = "N was increased to " + blockchain.getZeroes();
        } else {
            NChangeMessage = "N stays the same";
        }
    }

    synchronized void mine() {
        try {
            int zeroes = blockchain.getZeroes();
            Block latestBlock = blockchain.getBlockToMine();
            for (Miner miner : miners) {
                miner.setBlockAndZeroes(latestBlock, zeroes);
            }
            Block block = executor.invokeAny(miners);
            processNChange(block.getTimeToGenerate());
            blockchain.addToChain(block);
            if (!blockchain.isBlockchainValid()){
                close();
                throw new Exception();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}

public class Main {

    private static String PATH = "blockchain.db";

    private static Blockchain load(String path) {
        try {
            FileInputStream fileIn = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            Blockchain bc = (Blockchain) in.readObject();
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
                FileOutputStream fileOut = new FileOutputStream(path);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(bc);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        Blockchain blockchain;
        if (new File(PATH).exists()) {
            blockchain = load(PATH);
        } else {
            blockchain = new Blockchain(0);
        }
        MinerManager minerManager = new MinerManager(blockchain, 10);
        for (int i = 0; i < 5; i++) {
            minerManager.mine();
            Block newBlock = blockchain.getLatestCompleteBlock();
            System.out.println(newBlock);
            System.out.println(minerManager.getNChangeMessage() + "\n");
            new Thread(() -> save(blockchain, PATH)).start();
        }
        minerManager.close();
        save(blockchain, PATH);
        scanner.close();
    }
}
