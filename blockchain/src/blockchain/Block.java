package blockchain;

import java.io.Serializable;
import java.util.*;
import java.security.*;

class Block implements Serializable {

    static final long serialVersionUID = 1;
    static final String DEFAULTHASH = "0";

    private final long id;
    private final long timeStamp;
    private final String prevSha256Hash;
    private String selfSha256Hash = DEFAULTHASH;
    private int magicNumber;
    private long timeToGenerate;
    private String minedBy;
    private boolean isHashSet = false;
    private List<Transaction> transactions;
    private String preliminaryHash = null;
    private transient MessageDigest digest;

    Block(long id, String prevBlockHash) {
        this.timeStamp = new Date().getTime();
        this.id = id;
        this.prevSha256Hash = prevBlockHash;
    }

    Block(Block block){
        this.id = block.id;
        this.timeStamp = block.timeStamp;
        this.prevSha256Hash = block.prevSha256Hash;
        this.selfSha256Hash = block.selfSha256Hash;
        this.magicNumber = block.magicNumber;
        this.timeToGenerate = block.timeToGenerate;
        this.minedBy = block.minedBy;
        this.isHashSet = block.isHashSet;
        this.transactions = new ArrayList<>(block.transactions);
        this.preliminaryHash = block.preliminaryHash;        
    }

    private String applySha256(String input) {
        try {
            if (digest == null){
                digest = MessageDigest.getInstance("SHA-256");
            }
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte elem : hash) {
                String hex = Integer.toHexString(0xff & elem);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void setSelfSha256Hash() {
        this.selfSha256Hash = generateSelfSha256Hash();
    }

    private String generateSelfSha256Hash(){
        return applySha256(getPreliminaryHash() + magicNumber);
    }

    private synchronized void generatePreliminaryHash() {
        preliminaryHash = prevSha256Hash + id + timeStamp + transactions.stream().map(m -> new String(m.getSignature())).reduce("", (p,n) -> p + n);
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

    List<Transaction> getTransactions(){
        return this.transactions;
    }

    void setTransactions(List<Transaction> transactions){        
        this.transactions = transactions;
        generatePreliminaryHash();
    }

    void addTransaction(Transaction transaction){
        transactions.add(transaction);
    }

    void sortTransactions(){
        transactions.sort(Comparator.comparingInt(Transaction::getId));
    }

    String getPreliminaryHash(){
        return preliminaryHash;
    }

    boolean isValidMagicNumber(int magicNumber, int zeroes) {
        String sha256Hash = applySha256(getPreliminaryHash() + magicNumber);
        return sha256Hash.startsWith("0".repeat(zeroes));
    }

    synchronized void setMinedDetails(BlockchainSubmission submission){
        if (!isHashSet && isValidMagicNumber(submission.magicNumber, submission.zeroes)){
            isHashSet = true;
            this.magicNumber = submission.magicNumber;
            this.minedBy = submission.minerId;
            this.timeToGenerate = submission.timeToGenerate;
            setSelfSha256Hash();
        }
    }

    @Override
    public String toString() {
        StringBuilder s =  new StringBuilder(
            "Block:\n" +
            "Created by: " + minedBy + "\n" +
            minedBy + " gets " + Blockchain.REWARD_PER_BLOCK + " " + Blockchain.COIN_NAME + "\n" +
            "Id: " + id + "\n" +
            "Timestamp: " + timeStamp + "\n" +
            "Magic number: " + magicNumber + "\n" +
            "Hash of the previous block:\n" + prevSha256Hash + "\n" +
            "Hash of the block:\n" + selfSha256Hash + 
            "\nBlock data: ");
        if (!transactions.isEmpty()){
            for (Transaction transaction : transactions){
                s.append("\n").append(transaction.toString());
            }
        } else {
            s.append("no Transactions");
        }
        s.append("\nBlock was generating for ").append(timeToGenerate / 1000f).append(" seconds");
        return s.toString();
    }
}
