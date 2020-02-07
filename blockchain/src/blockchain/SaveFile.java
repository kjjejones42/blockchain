package blockchain;

import java.io.Serializable;

class SaveFile implements Serializable{
    static final long serialVersionUID = 1;
    byte[] RSAEncryptedAESKey;
    byte[] AESEncryptedFile; 
}
