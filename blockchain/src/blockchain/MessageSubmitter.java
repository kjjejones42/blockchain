package blockchain;

import java.security.*;

class MessageSubmitter implements Runnable {
    private Blockchain blockchain;
    private PrivateKey privKey;
    private PublicKey pubKey;
    private int delay;

    MessageSubmitter(Blockchain blockchain, int delay){
        this.blockchain = blockchain;
        this.delay = delay;
        KeyPair kp = Encryptor.getInstance().generatePublicAndPrivateKeys();
        this.privKey = kp.getPrivate();
        this.pubKey = kp.getPublic();
    }
    
    @Override
    public void run() {
        int i = 1;
        Thread.currentThread().setName("Blockchain Message Submitter");
        while (true){
            try {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                Transaction message = new Transaction(
                    Integer.toString(i++),
                    "noone", 0f, privKey, pubKey);
                blockchain.submitMessage(message);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
