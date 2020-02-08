package blockchain;

import java.io.Serializable;
import java.util.*;

class Blockchain implements Serializable {

    static final long serialVersionUID = 1;
    static final String SELF_TRANSACTION_ID = "SELF_TRANSACTION_ID";
    static final float REWARD_PER_BLOCK = 100f;
    static final String COIN_NAME = "VC";

    private int zeroes;
    private int transactionId = 1;
    private final List<Block> blockChain = new ArrayList<>();
    private Block blockToMine;
    private String NChangeMessage;
    private List<Transaction> transactions = new ArrayList<>();
    private int initialTransactionId;

    Blockchain(int zeroes) {
        this.zeroes = zeroes;
        generateNewBlockToMine(null);
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
        if (previousBlock == null) {            
            blockToMine = new Block(1, Block.DEFAULTHASH);
        } else {
            blockToMine = new Block(previousBlock.getId() + 1, previousBlock.getSelfSha256Hash());   
        }
        int min = transactions.stream().mapToInt(t -> t.getId()).min().orElse(2);
        incrementTransactionId();
        initialTransactionId = min - 1;
        blockToMine.setTransactions(transactions);
        this.transactions = new ArrayList<>();
    }

    synchronized private void addApprovedSubmission(Block block, BlockchainSubmission submission) {
        block.setMinedDetails(submission);
        blockChain.add(block);

        processNChange(block.getTimeToGenerate());
        generateNewBlockToMine(block);
    }

    synchronized private Block getNewBlockForMinerId(String minerId){        
        Block block = new Block(blockToMine);
        Transaction reward = getRewardTransaction(minerId);
        block.addTransaction(reward);
        block.sortTransactions();
        return block;        
    }

    synchronized private Transaction getRewardTransaction(String minerId){
        Encryptor e = Encryptor.getInstance();
        Transaction transaction = new Transaction(Blockchain.SELF_TRANSACTION_ID, minerId, REWARD_PER_BLOCK, e.getPrivateKey(), e.getPublicKey());
        transaction.setId(initialTransactionId);
        return transaction;
    }

    synchronized private void incrementTransactionId(){
        transactionId++;        
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
            int maxTransactionId = block.getTransactions().stream().mapToInt(Transaction -> Transaction.getId()).max().orElse(Integer.MAX_VALUE);
            int lastTransactionId = Integer.MIN_VALUE;
            for (Transaction transaction : block.getTransactions()){
                int id = transaction.getId();
                if (!transaction.isAdminTransaction() && (id > maxTransactionId || id < lastTransactionId || !transaction.isSignatureValid())){
                    return false;
                }
                lastTransactionId = id;
            }
        }
        return true;
    }

    synchronized void submitTransaction(Transaction transaction){
        if (transaction.isSignatureValid()){            
            incrementTransactionId();
            transaction.setId(transactionId);
            transactions.add(transaction);
        }
    }

    synchronized boolean submitSubmission(BlockchainSubmission submission){
        Block block = new Block(getBlockToMine(submission.minerId));       
        if (block.isValidMagicNumber(submission.magicNumber, zeroes)){
            addApprovedSubmission(block, submission);
            return true;
        }
        return false;
    }

    synchronized Block getLatestCompleteBlock() {
        return blockChain.isEmpty() ? null : blockChain.get(blockChain.size() - 1);
    }

    synchronized Block getBlockToMine(String minerId) {
        return getNewBlockForMinerId(minerId);
    }

    synchronized void incrementZeroes() {
        this.zeroes++;
    }

    synchronized void decrementZeroes() {
        this.zeroes--;
    }
}
