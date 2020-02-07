package blockchain;

import java.io.Serializable;

class KeyFile implements Serializable {
    static final long serialVersionUID = 1;
    byte[] publicKey;
    byte[] privateKey;
}
