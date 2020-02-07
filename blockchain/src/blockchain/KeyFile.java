package blockchain;

import java.io.Serializable;

class KeyFile implements Serializable {
    static final long serialVersionUID = 1;
    final byte[] a;
    final byte[] b;

    KeyFile(byte[] publicKey, byte[] privateKey){
        this.b = privateKey;
        this.a = publicKey;
    }
}
