package blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.security.*;
import java.security.spec.*;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;


class Encryptor {
 
    private static final String KEYS_PATH = "keys.db";

    private static Encryptor instance;

    private final KeyFactory kf;
    private final Cipher AESCipher;
    private final Cipher RSACipher;
    private final KeyPairGenerator kpg;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private SecretKey AESKey;
    

    static public Encryptor getInstance(){
        if (instance == null){
            instance = new Encryptor();
        }
        return instance;
    }

    Encryptor(){
        try {
            kf = KeyFactory.getInstance("RSA");            
            AESCipher = Cipher.getInstance("AES");        
            RSACipher = Cipher.getInstance("RSA");
            kpg = KeyPairGenerator.getInstance("RSA");            
            kpg.initialize(2048);            
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }
        
    private void getPrivateKeyFromBytes(byte[] keyBytes){   
        try {
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            privateKey = kf.generatePrivate(spec);               
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }     
    }

    private void getPublicKeyFromBytes(byte[] keyBytes){   
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            publicKey = kf.generatePublic(spec);                
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }     
    }

    private void saveKeysToFile(){
        try {
            KeyFile kf = new KeyFile(
                getPublicKey().getEncoded(),
                getPrivateKey().getEncoded());
            FileOutputStream fos = new FileOutputStream(KEYS_PATH);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(kf);
            oos.close();            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void loadKeysFromFile(){
        try {
            KeyFile kf = KeyFile.class.cast(byteArrayToObject(Files.readAllBytes(Paths.get(KEYS_PATH))));
            getPrivateKeyFromBytes(kf.b);
            getPublicKeyFromBytes(kf.a);
        } catch (IOException e) {
            KeyPair kp = generatePublicAndPrivateKeys();  
            privateKey = kp.getPrivate();
            publicKey = kp.getPublic();          
            saveKeysToFile();   
        }

    }

    private SecretKey getAESKey(){
        if (AESKey == null){
            try {
                KeyGenerator k = KeyGenerator.getInstance("AES");
                k.init(128);
                AESKey = k.generateKey();
            }
             catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        return AESKey;                    
    }

    private byte[] AESEncrypt(byte[] input){
        try {
            AESCipher.init(Cipher.ENCRYPT_MODE, getAESKey());
            return AESCipher.doFinal(input);     
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] AESDecrypt(byte[] input){
        try {
            AESCipher.init(Cipher.DECRYPT_MODE, getAESKey());
            return AESCipher.doFinal(input);   
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] RSAEncrypt(byte[] input){
        try {
            RSACipher.init(Cipher.ENCRYPT_MODE, getPublicKey());
            return RSACipher.doFinal(input);            
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] RSADecrypt(byte[] input){
        try {
            RSACipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
            return RSACipher.doFinal(input);            
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    private Object byteArrayToObject(byte[] arr){
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(arr);
            ObjectInputStream oos = new ObjectInputStream(bis);
            return oos.readObject();            
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void setAESKey(SecretKey sk){
        AESKey = sk;
    }

    KeyPair generatePublicAndPrivateKeys(){
        return kpg.generateKeyPair();      
    }
	
	PrivateKey getPrivateKey(){
        if (privateKey == null){
            loadKeysFromFile();
        }
        return privateKey;
    }

	PublicKey getPublicKey(){
        if (publicKey == null){
            loadKeysFromFile();
        }
        return publicKey;
    }
    
    void loadRSAEncryptedAESKeyFromBytes(byte[] encodedKey){
        encodedKey = RSADecrypt(encodedKey);
        setAESKey(new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES"));
    }

    byte[] getRSAEncryptedAESKey(){
        byte[] key = getAESKey().getEncoded();
        return RSAEncrypt(key);  
    }     

    byte[] objToAESEncryptedBytes(Object obj){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            return AESEncrypt(bos.toByteArray());            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Object AESEncryptedBytesToObj(byte[] arr){
        try {
            arr = AESDecrypt(arr);
            ByteArrayInputStream bis = new ByteArrayInputStream(arr);
            ObjectInputStream ois = new ObjectInputStream(bis);
            return ois.readObject();            
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    String applySha256(String input) {
        try {            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte elem : hash) {
                String hex = Integer.toHexString(0xff & elem);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
