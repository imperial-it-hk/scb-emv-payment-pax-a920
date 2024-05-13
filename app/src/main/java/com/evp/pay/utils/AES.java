package com.evp.pay.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class AES {
    public static byte[] zeroPadding(byte[] input) {
        if (input.length % 16 == 0) {
            return input;
        }
        int newLength = ((input.length / 16) + 1) * 16;
        byte[] result = new byte[newLength];
        System.arraycopy(input, 0, result, 0, input.length);
        return result;
    }

    public static byte[] clean(byte[] input) {
        int i = input.length - 1;
        while (i >= 0 && input[i] == 0)
        {
            --i;
        }
        return Arrays.copyOf(input, i + 1);
    }

    public static byte[] encrypt(byte[] input, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance("AES_256/CBC/NOPADDING");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(new byte[16]));		// empty IV
        return cipher.doFinal(zeroPadding(input));
    }

    public static byte[] decrypt(byte[] data, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance("AES_256/CBC/NOPADDING");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(new byte[16]));		// empty IV
        return cipher.doFinal(data);
    }
}
