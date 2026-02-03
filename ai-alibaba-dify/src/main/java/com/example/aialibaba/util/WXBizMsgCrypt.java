package com.example.aialibaba.util;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 * Simplified WXBizMsgCrypt for WeCom message encryption/decryption.
 * In production, please use the official version from WeChat.
 */
public class WXBizMsgCrypt {
    static Charset CHARSET = Charset.forName("utf-8");
    Base64 base64 = new Base64();
    byte[] aesKey;
    String token;
    String receiveid;

    public WXBizMsgCrypt(String token, String encodingAesKey, String receiveid) throws Exception {
        if (encodingAesKey.length() != 43) {
            throw new Exception("AES Key length error");
        }
        this.token = token;
        this.receiveid = receiveid;
        aesKey = Base64.decodeBase64(encodingAesKey + "=");
    }

    public String VerifyURL(String msgSignature, String timeStamp, String nonce, String echoStr) throws Exception {
        String signature = SHA1.getSHA1(token, timeStamp, nonce, echoStr);
        if (!signature.equals(msgSignature)) {
            throw new Exception("Signature verification failed");
        }
        return decrypt(echoStr);
    }

    public String DecryptMsg(String msgSignature, String timeStamp, String nonce, String postData) throws Exception {
        // XML parsing and signature verification should be here
        // This is a simplified version
        String encrypt = XMLParse.extract(postData);
        String signature = SHA1.getSHA1(token, timeStamp, nonce, encrypt);
        if (!signature.equals(msgSignature)) {
            throw new Exception("Signature verification failed");
        }
        return decrypt(encrypt);
    }

    public String EncryptMsg(String replyMsg, String timeStamp, String nonce) throws Exception {
        String encrypt = encrypt(getRandomStr(), replyMsg);
        if (timeStamp == "") {
            timeStamp = Long.toString(System.currentTimeMillis() / 1000L);
        }
        String signature = SHA1.getSHA1(token, timeStamp, nonce, encrypt);
        return XMLParse.generate(encrypt, signature, timeStamp, nonce);
    }

    private String decrypt(String text) throws Exception {
        byte[] original;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec key_spec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(aesKey, 0, 16));
            cipher.init(Cipher.DECRYPT_MODE, key_spec, iv);
            byte[] encrypted = Base64.decodeBase64(text);
            original = cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw new Exception("Decrypt AES error");
        }
        // PKCS7 decoding and content extraction logic goes here
        // For brevity, assuming it's done correctly in full version
        return new String(original, CHARSET); 
    }

    private String encrypt(String randomStr, String text) throws Exception {
        // Encryption logic goes here
        return "";
    }

    private String getRandomStr() {
        String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 16; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
}
