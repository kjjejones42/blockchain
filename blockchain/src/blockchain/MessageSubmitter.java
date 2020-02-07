package blockchain;

import java.util.*;

class MessageSubmitter implements Runnable {
    private Blockchain blockchain;

    MessageSubmitter(Blockchain blockchain){
        this.blockchain = blockchain;
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
                blockchain.submitMessage(message);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
