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
    private List<Message> messages = new ArrayList<>();
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

    synchronized String getNChangeMessage() {
        return NChangeMessage;
    }

    synchronized int getZeroes() {
        return this.zeroes;
    }

    synchronized boolean isBlockchainValid() {
        for (int i = 0; i < blockChain.size(); i++) {
            Block block = blockChain.get(i);
            Block prevBlock = i == 0 ? null : blockChain.get(i - 1);
            String currHashOfPrev = block.getPrevBlockHash();
            String prevHashActual = i == 0 ? Block.DEFAULTHASH : prevBlock.getSelfSha256Hash();
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
        if (message.isSignatureValid()){
            message.id = ++messageId;
            messages.add(message);
        }
    }

    synchronized void addSubmission(BlockchainSubmission submission) {
        Block block = new Block(getBlockToMine());
        block.addMessage(getInitialMessage(submission.minerId));
        block.setMinedDetails(submission);
        blockChain.add(block);

        processNChange(block.getTimeToGenerate());

        blockToMine = new Block(block.getId() + 1, block.getSelfSha256Hash());            
        blockToMine.setMessages(messages);
        initialMessageId = ++messageId;
        this.messages = new ArrayList<>();
    }

    synchronized Message getInitialMessage(int minerId){
        Message message = new Message("Miner " + minerId, "Give me a coin", Encryptor.getPrivateKey(), Encryptor.getPublicKey());
        message.id = initialMessageId;
        return message;
    }

    synchronized boolean submitSubmission(BlockchainSubmission submission){
        Block block = new Block(getBlockToMine());        
        block.addMessage(getInitialMessage(submission.minerId));
        if (block.isValidMagicNumber(submission.magicNumber, zeroes)){
            addSubmission(submission);
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
