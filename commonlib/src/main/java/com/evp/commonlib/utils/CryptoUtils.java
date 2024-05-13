package com.evp.commonlib.utils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class CryptoUtils {
    private static final String TAG = "CryptoUtils";

    private CryptoUtils() { }

    public static String sha1(String str) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
            digest.update(str.getBytes());
            byte[] messageDigest = digest.digest();
            // Create Hex String
            StringBuilder hexString = new StringBuilder();

            for (byte i : messageDigest) {
                String shaHex = Integer.toHexString(i & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            LogUtils.w(TAG, e);
        }
        return "";
    }

    public static byte[] sha1(byte[] data) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
            digest.update(data);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            LogUtils.e(TAG, e);
        }
        return "".getBytes();
    }

    public static KeyPair genRsaKeyPair(int keySize) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(keySize);
        return kpg.generateKeyPair();
    }

    public static byte[] decryptWithRsaPubKey(KeyPair rsaKeyPair, byte[] dataToDecrypt) {
        byte[] decrypted;
        try {
            final Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsa.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
            rsa.update(dataToDecrypt);
            decrypted = rsa.doFinal();
        } catch (Exception e) {
            LogUtils.e(TAG, e);

            return "".getBytes();
        }
        return decrypted;
    }

    public static byte[] calcTleKcv(byte[] key) {
        byte[] dataToEncrypt = new byte[16];
        byte[] ecrypted;
        try {
            final Cipher des = Cipher.getInstance("DESede/ECB/NoPadding");
            final SecretKey secretKey = new SecretKeySpec(key, "DESede");
            des.init(Cipher.ENCRYPT_MODE, secretKey);
            ecrypted = des.doFinal(dataToEncrypt);
        } catch (Exception e) {
            LogUtils.e(TAG, e);
            return "".getBytes();
        }
        return ecrypted;
    }
}
