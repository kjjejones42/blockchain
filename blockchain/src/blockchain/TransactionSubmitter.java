package blockchain;

import java.security.*;
import java.util.List;
import java.util.Random;

class TransactionSubmitter implements Runnable {
    private Blockchain blockchain;
    private PrivateKey privKey;
    private PublicKey pubKey;
    private int delay;
    private List<String> minerIds;

    TransactionSubmitter(Blockchain blockchain, int delay){
        this.blockchain = blockchain;
        this.delay = delay;
        this.minerIds = MinerManager.getListOfMiners();
        KeyPair kp = Encryptor.getInstance().generatePublicAndPrivateKeys();
        this.privKey = kp.getPrivate();
        this.pubKey = kp.getPublic();
    }
    
    @Override
    public void run() {
        int i = 1;
        Thread.currentThread().setName("Blockchain Transaction Submitter");
        while (true){
            try {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                Random rand = new Random();
                String from = minerIds.get(rand.nextInt(minerIds.size()));
                String to = from;
                while (to.equals(from)){
                    to = minerIds.get(rand.nextInt(minerIds.size()));
                }
                Transaction transaction = new Transaction(
                    from,
                    to, rand.nextFloat() * 100, privKey, pubKey);
                new Thread(() -> blockchain.submitTransaction(transaction)).start();
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
