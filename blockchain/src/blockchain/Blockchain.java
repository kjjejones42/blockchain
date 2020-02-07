package blockchain;

import java.io.Serializable;
import java.util.*;

class Blockchain implements Serializable {

    static final long serialVersionUID = 1;

    private int zeroes;
    private int messageId = 1;
    private final List<Block> blockChain = new ArrayList<>();
    private Block blockToMine;
    private String NChangeMessage;
    private List<Transaction> messages = new ArrayList<>();
    private int initialMessageId = messageId;

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

    synchronized private void generateNewBlockToMine(Block previousBlock){
        blockToMine = new Block(previousBlock.getId() + 1, previousBlock.getSelfSha256Hash());            
        blockToMine.setMessages(messages);
        incrementMessageId();
        initialMessageId = messageId;
        this.messages = new ArrayList<>();
    }

    synchronized private void addApprovedSubmission(BlockchainSubmission submission) {
        Block block = new Block(getBlockToMine());
        block.addMessage(getInitialMessage(submission.minerId));
        block.setMinedDetails(submission);
        blockChain.add(block);

        processNChange(block.getTimeToGenerate());
        generateNewBlockToMine(block);
    }

    synchronized private void incrementMessageId(){
        messageId++;        
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
            int maxMessageId = block.getMessages().stream().mapToInt(message -> message.getId()).max().orElse(Integer.MAX_VALUE);
            int lastMessageId = Integer.MIN_VALUE;
            for (Transaction message : block.getMessages()){
                int id = message.getId();
                if (id > maxMessageId || id < lastMessageId || !message.isSignatureValid()){
                    return false;
                }
                lastMessageId = id;
            }
        }
        return true;
    }

    synchronized void submitMessage(Transaction message){
        if (message.isSignatureValid()){            
            incrementMessageId();
            message.setId(messageId);
            messages.add(message);
        }
    }
    synchronized Transaction getInitialMessage(String minerId){
        Encryptor e = Encryptor.getInstance();
        Transaction message = new Transaction(minerId, minerId, 100f, e.getPrivateKey(), e.getPublicKey());
        message.setId(initialMessageId);
        return message;
    }

    synchronized boolean submitSubmission(BlockchainSubmission submission){
        Block block = new Block(getBlockToMine());        
        block.addMessage(getInitialMessage(submission.minerId));
        if (block.isValidMagicNumber(submission.magicNumber, zeroes)){
            addApprovedSubmission(submission);
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
