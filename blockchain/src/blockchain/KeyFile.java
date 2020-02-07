package blockchain;

import java.io.Serializable;

class KeyFile implements Serializable {
    static final long serialVersionUID = 1;
    final byte[] publicKey;
    final byte[] privateKey;

    KeyFile(byte[] publicKey, byte[] privateKey){
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }
}
