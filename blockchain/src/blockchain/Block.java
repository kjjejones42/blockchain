package blockchain;

import java.io.Serializable;
import java.util.*;

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
