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
        Thread.currentThread().setName("Blockchain Transaction Submitter");
        Random rand = new Random();
        List<String> userIds;
        while (true){
            userIds = blockchain.getUsers();
            try {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                if (userIds.size() > 1) {
                    String from = userIds.get(rand.nextInt(userIds.size()));
                    String to = from;
                    while (to.equals(from)){
                        to = minerIds.get(rand.nextInt(minerIds.size()));
                    }
                    Transaction transaction = new Transaction(from, to, rand.nextInt(101), privKey, pubKey);
                    blockchain.submitTransaction(transaction);
                    Thread.sleep(delay);
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
