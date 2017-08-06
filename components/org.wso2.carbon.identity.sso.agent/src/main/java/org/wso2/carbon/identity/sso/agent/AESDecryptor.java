package org.wso2.carbon.identity.sso.agent;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class AESDecryptor {

    private static final String ALGORITHM =  "AES/CBC/PKCS5Padding";

    public static String decode(String encryptedSecret,
                                String cipherKey)
            throws Exception {

        SecretKey key = new SecretKeySpec(cipherKey.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        byte[] iv = new byte[cipher.getBlockSize()];

        IvParameterSpec ivParams = new IvParameterSpec(iv);

        cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
        byte[] decodedValue = new Base64().decode(encryptedSecret.getBytes("UTF-8"));

        byte[] decryptedValue = cipher.doFinal(decodedValue);

        return new String(decryptedValue, "UTF-8");
    }

}
