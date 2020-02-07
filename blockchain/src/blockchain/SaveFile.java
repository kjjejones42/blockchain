package blockchain;

import java.io.Serializable;

class SaveFile implements Serializable{
    static final long serialVersionUID = 1;
    final byte[] RSAEncryptedAESKey;
    final byte[] AESEncryptedFile; 

    SaveFile(byte[] RSAEncryptedAESKey, byte[] AESEncryptedFile){
        this.RSAEncryptedAESKey = RSAEncryptedAESKey;
        this.AESEncryptedFile = AESEncryptedFile;
    }
}
