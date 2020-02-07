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

    private PrivateKey privateKey = null;
    private PublicKey publicKey = null;
    private SecretKey AESKey = null;

    private static Encryptor instance;

    static public Encryptor getInstance(){
        if (instance == null){
            instance = new Encryptor();
        }
        return instance;
    }
        
    private void getPrivateKeyFromBytes(byte[] keyBytes){   
        try {
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            privateKey = kf.generatePrivate(spec);               
        } catch (Exception e) {
            throw new RuntimeException(e);
        }     
    }

    private void getPublicKeyFromBytes(byte[] keyBytes){   
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            publicKey = kf.generatePublic(spec);               
        } catch (Exception e) {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void loadKeysFromFile(){
        try {
            KeyFile kf = KeyFile.class.cast(byteArrayToObject(Files.readAllBytes(Paths.get(KEYS_PATH))));
            getPrivateKeyFromBytes(kf.b);
            getPublicKeyFromBytes(kf.a);
        } catch (Exception e) {
            KeyPair kp = generatePublicAndPrivateKeys();  
            privateKey = kp.getPrivate();
            publicKey = kp.getPublic();          
            saveKeysToFile();   
        }

    }

    private SecretKey getAESKey(){
        try {
            if (AESKey == null){
                KeyGenerator k = KeyGenerator.getInstance("AES");
                k.init(128);
                AESKey = k.generateKey();
            }
            return AESKey;            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] AESEncrypt(byte[] input){
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, getAESKey());
            return c.doFinal(input);            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] AESDecrypt(byte[] input){
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, getAESKey());
            return c.doFinal(input);            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] RSAEncrypt(byte[] input){
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.ENCRYPT_MODE, getPublicKey());
            return c.doFinal(input);            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] RSADecrypt(byte[] input){
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.DECRYPT_MODE, getPrivateKey());
            return c.doFinal(input);            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object byteArrayToObject(byte[] arr){
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(arr);
            ObjectInputStream oos = new ObjectInputStream(bis);
            return oos.readObject();            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setAESKey(SecretKey sk){
        AESKey = sk;
    }

    KeyPair generatePublicAndPrivateKeys(){
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            return kpg.generateKeyPair();          
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
	
	PrivateKey getPrivateKey(){
        try {
            if (privateKey == null){
                loadKeysFromFile();
            }
            return privateKey;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	PublicKey getPublicKey(){
        try {
            if (publicKey == null){
                loadKeysFromFile();
            }
            return publicKey;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    void loadAESKeyFromBytes(byte[] encodedKey){
        encodedKey = RSADecrypt(encodedKey);
        setAESKey(new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES"));
    }

    byte[] getRSAEncryptedAESKey(){
        try {
            return RSAEncrypt(getAESKey().getEncoded());       
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }     

    byte[] objToAESEncryptedBytes(Object obj){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            return AESEncrypt(bos.toByteArray());            
        } catch (Exception e) {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Object AESEncryptedBytesToObj(byte[] arr){
        return AESDecrypt(arr);
    }
}
