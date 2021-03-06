package blockchain;

import java.io.Serializable;
import java.security.*;

class Transaction implements Serializable {
    static final long serialVersionUID = 0;
    final String from;
    final String to;
    final int amount;
    private byte[] signature;
    private int id;
    private PublicKey publicKey;

    Transaction(String from, String to, int amount, PrivateKey privKey, PublicKey pubKey){
        this.from = from;
        this.to = to;
        this.amount = amount;
        sign(privKey, pubKey);
    }

    boolean isSignatureValid(){    
        if (isAdminTransaction()){
            return true;
        }    
        try {
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(publicKey);
            sig.update(getPreliminaryHash());
            return sig.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    boolean isAdminTransaction(){
        return from.equals(Blockchain.SELF_TRANSACTION_ID);
    }

    byte[] getPreliminaryHash(){
        return (from + to + amount).getBytes();
    }

    byte[] getSignature(){
        return signature;
    }

    int getId(){
        return id;
    }

    void setId(int id){
        this.id = id;
    }

    String getTo(){
        return to;
    }

    String getFrom(){
        return from;
    }

    int getAmount(){
        return amount;
    }

    PublicKey getPublicKey(){
        return publicKey;
    }

	void sign(PrivateKey privKey, PublicKey pubKey) {
        if (signature != null){
            throw new RuntimeException("Attempted to sign a transaction that is already signed");
        }
        try {
            publicKey = pubKey;
            Signature rsa = Signature.getInstance("SHA1withRSA"); 
            rsa.initSign(privKey);
            rsa.update(getPreliminaryHash());
            signature = rsa.sign();            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        if (isAdminTransaction()){
            return  String.format("% 4d",id) + " | \"" + to + "\" was awarded " + amount + " " + Blockchain.COIN_NAME + " for mining the block.";
        }
        return String.format("% 4d",id) + " | User \"" + from + "\" gave \"" + to + "\" " +  amount + " " + Blockchain.COIN_NAME;
    }
}
