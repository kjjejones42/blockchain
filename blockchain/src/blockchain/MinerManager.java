package blockchain;

import java.util.*;
import java.util.concurrent.*;


class MinerManager {

    private final Blockchain blockchain;
    private final List<Miner> miners = new ArrayList<>();
    private final ExecutorService executor;

    MinerManager(Blockchain blockchain, int threads) {
        this.blockchain = blockchain;
        this.executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            miners.add(new Miner("miner" + i, blockchain));
        }
    }

    synchronized void close() {
        executor.shutdown();
    }

    synchronized void mine() {
        try {
            boolean done = false;
            while (!done){
                int zeroes = blockchain.getZeroes();
                Block block = blockchain.getBlockToMine();
                for (Miner miner : miners) {
                    while (miner.isRunning()){}
                    miner.set(block, zeroes);
                }
                BlockchainSubmission result = executor.invokeAny(miners);
                done = blockchain.submitSubmission(result);
                if (!blockchain.isBlockchainValid()){
                    throw new Exception("Blockchain is invalid");
                }
            }
        } catch (Exception e) {            
            close();
            throw new RuntimeException(e);
        }
    }

}
