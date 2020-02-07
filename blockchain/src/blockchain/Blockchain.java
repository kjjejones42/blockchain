package blockchain;

import java.io.Serializable;
import java.util.*;

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
                if (id > maxMessageId || id < lastMessageId || !message.isSignatureValid()){
                    return false;
                }
                lastMessageId = id;
            }
        }
        return true;
    }

    synchronized void submitMessage(Message message){
        message.id = messageId++;
        if (message.isSignatureValid()){
            messages.add(message);
        }
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
