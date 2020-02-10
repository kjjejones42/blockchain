package blockchain;

import java.io.Serializable;
import java.util.*;
import java.util.stream.*;
import java.security.PublicKey;

class Blockchain implements Serializable {

    static final long serialVersionUID = 1;
    static final String SELF_TRANSACTION_ID = "SELF_TRANSACTION_ID";
    static final int REWARD_PER_BLOCK = 100;
    static final String COIN_NAME = "VC";

    private int zeroes;
    private int transactionId = 1;
    private final List<Block> blockChain = new ArrayList<>();
    private Block blockToMine;
    private String NChangeMessage;
    private List<Transaction> transactions = new ArrayList<>();
    private int initialTransactionId;
    private Map<String, Integer> userTotalMap;
    private Map<String, PublicKey> userMap;

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

    private synchronized void addTransactionToMap(Transaction transaction){
        int amount = transaction.getAmount();
        userTotalMap.merge(transaction.getTo(), amount, Integer::sum);
        userTotalMap.merge(transaction.getFrom(), -1 * amount, Integer::sum);
    }

    private synchronized void generateMap(){    
        Set<Transaction> tran = blockChain.stream().flatMap(b -> b.getTransactions().stream()).collect(Collectors.toSet());        
        tran.addAll(blockToMine.getTransactions());
        tran.addAll(transactions);
        Map<String, Integer> map1 = tran.stream().collect(Collectors.toMap(
            Transaction::getFrom,
            t -> -1 * t.getAmount(),
            (p,n) -> p + n));
        Map<String, Integer> map2 = tran.stream().collect(Collectors.toMap(
            Transaction::getTo,
            t -> t.getAmount(),
            (p,n) -> p + n));
        Map<String, Integer> map = Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey, 
                Map.Entry::getValue,
                (p, n) -> p + n));
        map.remove(SELF_TRANSACTION_ID);
        userTotalMap = map;
    }

    synchronized private void generateNewBlockToMine(Block previousBlock){
        if (previousBlock == null) {            
            blockToMine = new Block(1, Block.DEFAULTHASH);
        } else {
            blockToMine = new Block(previousBlock.getId() + 1, previousBlock.getSelfSha256Hash());   
        }
        incrementTransactionId();
        int min = transactions.stream().mapToInt(t -> t.getId()).min().orElse(transactionId);
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

    synchronized int getUserCredit(String userId){
        Integer result = getMap().get(userId);        
        return result == null ? 0 : result;
    }

    synchronized Map<String, Integer> getMap(){
        if (userTotalMap == null){
            generateMap();
        }
        return userTotalMap;
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
            List<Transaction> transactions = block.getTransactions();
            int maxTransactionId = transactions.get(transactions.size() - 1).getId();
            int lastTransactionId = Integer.MIN_VALUE;
            for (Transaction transaction : transactions){
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
        if (transaction.isSignatureValid() && getUserCredit(transaction.getFrom()) > transaction.getAmount()){            
            incrementTransactionId();
            transaction.setId(transactionId);
            addTransactionToMap(transaction);
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
