package blockchain;

import java.util.*;
import java.util.concurrent.*;

class Miner implements Callable<BlockchainSubmission> {

    private final int id;
    private Block block;
    private Blockchain blockchain;
    private int leadingZeroes;
    private boolean isRunning;

    Miner(int id, Blockchain blockchain) {
        this.id = id;
        this.blockchain = blockchain;
    }

    int getId(){
        return id;
    }

    boolean isRunning(){
        return isRunning;
    }

    void set(Block block, int leadingZeroes) {
        this.block = new Block(block);
        this.leadingZeroes = leadingZeroes;
        Message message = blockchain.getInitialMessage(id);
        this.block.addMessage(message);
    }

    @Override
    public String toString() {
        return "Miner " + id;
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
