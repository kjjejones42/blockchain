package blockchain;

import java.util.*;
import java.util.concurrent.*;

class Miner implements Callable<BlockchainSubmission> {

    private final String id;
    private Block block;
    private int leadingZeroes;
    private boolean isRunning;

    Miner(String id) {
        this.id = id;
    }

    String getId(){
        return id;
    }

    boolean isRunning(){
        return isRunning;
    }

    synchronized void set(Block block, int leadingZeroes) {
        this.block = new Block(block);
        this.leadingZeroes = leadingZeroes;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    synchronized public BlockchainSubmission call() {
        try {                
            isRunning = true;
            Thread.currentThread().setName(id);
            long startTime = new Date().getTime();

            Random rand = new Random();
            int magicNumber;
            do {
                if (Thread.currentThread().isInterrupted()) {
                    isRunning = false;
                    return null;
                }
                magicNumber = Math.abs(rand.nextInt());
            } while (!block.isValidMagicNumber(magicNumber, leadingZeroes));
            isRunning = false;
            return new BlockchainSubmission(magicNumber, id, new Date().getTime() - startTime, leadingZeroes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
