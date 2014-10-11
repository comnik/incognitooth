package incognitects.incognitooth;

import android.content.SharedPreferences;
import android.util.Base64;
import android.widget.Toast;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class EncryptUtil {

    final SharedPreferences prefs;

    final static String PRIVATE_KEY = "private_key";
    final static String PUBLIC_KEY = "public_key";

    public EncryptUtil(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public PublicKey getPublicKey() {
        return getPublicKey(PUBLIC_KEY);
    }

    public PublicKey getPublicKey(String recipient) {
        String publicKey = prefs.getString(recipient, null);
        if (publicKey == null) {
            return null;
        }

        try {
            byte[] keyBytes = Base64.decode(publicKey.getBytes("utf-8"), Base64.DEFAULT);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey key = keyFactory.generatePublic(spec);
            return key;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Generate key which contains a pair of private and public key using 1024
     * bytes. Store the set of keys in Private.key and Public.key files.
     */
    public void generateKey() {
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            final KeyPair key = keyGen.generateKeyPair();

            String privateKey = Base64.encodeToString(key.getPrivate().getEncoded(), Base64.DEFAULT);
            String publicKey = Base64.encodeToString(key.getPublic().getEncoded(), Base64.DEFAULT);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PRIVATE_KEY, privateKey);
            editor.putString(PUBLIC_KEY, publicKey);
            boolean success = editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] encrypt(String text, PublicKey key) {
        byte[] cipherText = null;
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("RSA");
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    public String decrypt(byte[] text, PrivateKey key) {
        byte[] decryptedText = null;
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("RSA");

            // decrypt the text using the private key
            cipher.init(Cipher.DECRYPT_MODE, key);
            decryptedText = cipher.doFinal(text);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new String(decryptedText);
    }

}

