package blockchain;

import java.io.Serializable;
import java.security.*;

class Message implements Serializable {
    static final long serialVersionUID = 0;
    final String userId;
    final String message;
    byte[] signature;
    int id;
    PublicKey publicKey;

    Message(String userId, String message){
        this.userId = userId;
        this.message = message;
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
        return (userId + message).getBytes();
    }

    public byte[] getSignature(){
        return signature;
    }

    @Override
    public String toString() {
        return String.format("%03d", id) + "| User \"" + userId + "\" says: " + message;
    }
}
