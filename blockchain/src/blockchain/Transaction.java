package blockchain;

import java.io.Serializable;
import java.security.*;

class Transaction implements Serializable {
    static final long serialVersionUID = 0;
    final String from;
    final String to;
    final float amount;
    private byte[] signature;
    private int id;
    private PublicKey publicKey;

    Transaction(String from, String to, float amount, PrivateKey privKey, PublicKey pubKey){
        this.from = from;
        this.to = to;
        this.amount = amount;
        sign(privKey, pubKey);
    }

    public boolean isSignatureValid(){    
        if (isAdminTransaction()){
            return true;
        }    
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
    
    public boolean isAdminTransaction(){
        return from.equals(Blockchain.SELF_TRANSACTION_ID);
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
            return  String.format("% 3d",id) + " | \"" + to + "\" was awarded " + amount + " " + Blockchain.COIN_NAME + " for mining the block.";
        }
        return String.format("% 3d",id) + " | User \"" + from + "\" gave \"" + to + "\" " + amount;
    }
}
