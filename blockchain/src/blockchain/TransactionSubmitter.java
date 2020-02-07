package blockchain;

import java.security.*;

class TransactionSubmitter implements Runnable {
    private Blockchain blockchain;
    private PrivateKey privKey;
    private PublicKey pubKey;
    private int delay;

    TransactionSubmitter(Blockchain blockchain, int delay){
        this.blockchain = blockchain;
        this.delay = delay;
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
                Transaction Transaction = new Transaction(
                    Integer.toString(i++),
                    "noone", 0f, privKey, pubKey);
                blockchain.submitTransaction(Transaction);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
