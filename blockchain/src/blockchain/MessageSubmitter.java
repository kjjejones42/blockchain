package blockchain;

import java.util.*;
import java.security.*;

class MessageSubmitter implements Runnable {
    private Blockchain blockchain;
    private PrivateKey privKey;
    private PublicKey pubKey;
    private int delay;

    MessageSubmitter(Blockchain blockchain, int delay){
        this.blockchain = blockchain;
        this.delay = delay;
        KeyPair kp = Encryptor.generatePublicAndPrivateKeys();
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
                Message message = new Message(
                    Integer.toString(i++),
                    "This message was generated on the timestamp " + new Date().getTime(), privKey, pubKey);
                blockchain.submitMessage(message);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
