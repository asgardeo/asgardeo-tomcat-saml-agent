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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.sso.agent;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Class for decrypt the encrypted values using AES algorithm.
 */
public class AESDecryptor {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SALT = "84B03D034B409D4E";
    private static final int KEY_DERIVATION_ITERATION_COUNT = 65536;
    private static final int KEY_SIZE = 128;

    /**
     *Decrypt and decrypt the encrypted values.
     * @param encryptedSecret encrypted value.
     * @param cipherKey password used for encryption.
     * @return
     * @throws SSOAgentException If an error occurred.
     */
    public static String decrypt(String encryptedSecret, char[] cipherKey) throws SSOAgentException {

        try {

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            // Generate the secret key with 128 bits.
            KeySpec spec = new PBEKeySpec(cipherKey, SALT.getBytes(StandardCharsets.UTF_8),
                    KEY_DERIVATION_ITERATION_COUNT, KEY_SIZE);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey key = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            // Create an initialization vector with Cipher's block size.
            byte[] iv = new byte[cipher.getBlockSize()];
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParams);

            // Decode the encrypted value.
            byte[] decodedValue = new Base64().decode(encryptedSecret.getBytes(StandardCharsets.UTF_8));
            // Decrypt the encrypted value and get the plain text password.
            byte[] decryptedValue = cipher.doFinal(decodedValue);
            return new String(decryptedValue, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new SSOAgentException("Error while decoding the encrypted value.", ex);
        }
    }
}
