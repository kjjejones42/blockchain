package blockchain;

import java.util.*;
import java.security.*;

class MessageSubmitter implements Runnable {
    private Blockchain blockchain;
    private PrivateKey privKey;
    private PublicKey pubKey;

    MessageSubmitter(Blockchain blockchain){
        this.blockchain = blockchain;
        KeyPair kp = Encryptor.generatePublicAndPrivateKeys();
        privKey = kp.getPrivate();
        pubKey = kp.getPublic();
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
                    "This message was generated on the timestamp " + new Date().getTime());
                message.signature = Encryptor.sign(message.getPreliminaryHash(), privKey);
                message.publicKey = pubKey;
                blockchain.submitMessage(message);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
