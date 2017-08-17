/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.sso.encryptor;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

import java.io.Console;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import javax.crypto.spec.SecretKeySpec;

/**
 * Class is used for encrypt a password using AES.
 */
public class Encryption {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SALT = "84B03D034B409D4E";
    private static final int KEY_DERIVATION_ITERATION_COUNT = 4096;
    private static final int KEY_SIZE = 128;

    /**
     * Encrypt and encrypt the plain text.
     *
     * @param secret    Password to be encrypted.
     * @param cipherKey Password use for encryption.
     * @return Encrypted value.
     * @throws Exception If an error occurred while encrypting.
     */

    private static String encrypt(String secret, char[] cipherKey) throws EncryptingException {

        try {

            // Change char array to byte array.
            ByteBuffer buf = StandardCharsets.UTF_8.encode(CharBuffer.wrap(cipherKey));
            byte[] secretKey = new byte[buf.limit()];
            buf.get(secretKey);

            PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
            gen.init(secretKey, SALT.getBytes(), KEY_DERIVATION_ITERATION_COUNT);
            byte[] key = ((KeyParameter) gen.generateDerivedParameters(KEY_SIZE)).getKey();

            SecretKey secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            // Create an initialization vector with Cipher's block size.
            byte[] iv = new byte[cipher.getBlockSize()];
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParams);

            // Encrypt the password.
            byte[] encryptedVal = cipher.doFinal(secret.getBytes(StandardCharsets.UTF_8));

            // Encode the password.
            byte[] encodedVal = new Base64().encode(encryptedVal);
            return new String(encodedVal, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException |
                NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException ex) {
            throw new EncryptingException("Error while encrypting", ex);
        }
    }

    /**
     * Main entry point.
     *
     * @param args The password you wanted to encrypt.
     * @throws Exception If an error occurred.
     */
    public static void main(String[] args) {

        String encryptedVal;
        Console console = System.console();
        if (console == null) {
            System.err.println("Couldn't get Console instance");
            System.exit(0);
        }

        // Get the password used for encryption.
        char passwordArray[] = console.readPassword("Please Enter a password you want to use for the encryption: ");
        try {
            encryptedVal = encrypt(args[0], passwordArray);
            Files.write(Paths.get("./encrypted_password.txt"), encryptedVal.getBytes(StandardCharsets.UTF_8));
        } catch (EncryptingException | IOException ex) {
            System.err.println("Error occurred while encrypting or while writing in to the file ");
            ex.printStackTrace();
            return;
        }
        Arrays.fill(passwordArray, (char) 0);
        System.out.println("File with encrypted value created successfully in your current location");
    }
}
