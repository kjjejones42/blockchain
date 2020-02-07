package blockchain;

import java.io.Serializable;

class SaveFile implements Serializable{
    static final long serialVersionUID = 1;
    final byte[] a;
    final byte[] b; 

    SaveFile(byte[] RSAEncryptedAESKey, byte[] AESEncryptedFile){
        this.a = RSAEncryptedAESKey;
        this.b = AESEncryptedFile;
    }
}
