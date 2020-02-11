package blockchain;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


class MinerManager {

    private final Blockchain blockchain;
    private ExecutorService executor;
    static private List<Miner> miners;

    MinerManager(Blockchain blockchain, int threads) {
        this.blockchain = blockchain;
        Miner[] arr = new Miner[threads];
        for (int i = 0; i < threads; i++){
            arr[i] = new Miner("Miner" + String.format("%02d",i + 1));
        }
        miners = List.of(arr);
        executor = Executors.newFixedThreadPool(threads);
    }

    static synchronized List<String> getListOfMiners(){
        return miners.stream().map(Miner::getId).collect(Collectors.toList());
    }

    synchronized void close() {
        executor.shutdown();
    }

    synchronized void mine() {
        try {
            boolean done = false;
            while (!done){
                int zeroes = blockchain.getZeroes();
                for (Miner miner : miners) {
                    while (miner.isRunning()){}
                    Block block = blockchain.getBlockToMine(miner.getId());
                    miner.set(block, zeroes);
                }
                BlockchainSubmission result = executor.invokeAny(miners);
                done = blockchain.submitSubmission(result);
                if (!blockchain.isBlockchainValid()){
                    throw new RuntimeException("Blockchain is invalid");
                }
            }
        } catch (InterruptedException | ExecutionException e) {         
            close();
            throw new RuntimeException(e);
        }
    }
}
