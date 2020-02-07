package blockchain;

import java.io.Serializable;
import java.security.*;

class Transaction implements Serializable {
    static final long serialVersionUID = 0;
    private final String from;
    private final String to;
    private final float amount;
    private byte[] signature;
    private int id;
    private PublicKey publicKey;

    Transaction(String userId, String message, float amount, PrivateKey privKey, PublicKey pubKey){
        this.from = userId;
        this.to = message;
        this.amount = amount;
        sign(privKey, pubKey);
    }

    public boolean isSignatureValid(){        
        try {
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(publicKey);
            sig.update(getPreliminaryHash());
            boolean result = sig.verify(signature); 	
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getPreliminaryHash(){
        return (from + to + amount).getBytes();
    }

    public byte[] getSignature(){
        return signature;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

	void sign(PrivateKey privKey, PublicKey pubKey) {
        if (signature != null){
            throw new RuntimeException("Attempted to sign a message that is already signed");
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
        return String.format("%03d", id) + "| User \"" + from + "\" gave \"" + to + "\" " + amount;
    }
}
