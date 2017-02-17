/**
 *  STS Remote library
 *
 *  Copyright (C) 2006  stSoftware Pty Ltd
 *
 *  www.stsoftware.com.au
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *
 *  info AT stsoftware.com.au
 *
 *  or by snail mail to:
 *
 *  stSoftware
 *  building C, level 1,
 *  14 Rodborough Rd
 *  Frenchs Forest 2086
 *  Australia.
 */
package com.aspc.remote.util.crypto;

import com.aspc.remote.util.misc.CLogger;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import java.security.Key;

import org.apache.commons.logging.Log;

/**
 *  NetUtil
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       August 9, 1999, 5:21 PM
 */
public class SymmetricEncrypt
{

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.crypto.SymmetricEncrypt");//#LOGGER-NOPMD

    /**
     */
    public SymmetricEncrypt()
    {
    }

    private static final KeyGenerator keyGen;
    private static final String strHexVal = "0123456789abcdef";

    static
    {
        KeyGenerator tmpGen=null;
        /**
         *  Step 1. Generate an AES key using KeyGenerator
         *          Initialize the key size to 128
         *
         */
        try
        {
            tmpGen = KeyGenerator.getInstance("AES");
            tmpGen.init(128);

        }
        catch (Exception exp)
        {
            LOGGER.info(" Exception inside constructor " , exp);
        }

        keyGen = tmpGen;
    }

    /**
     *
     * @return  the value
     */
    public SecretKey getSecret()
    {
        SecretKey secretKey = keyGen.generateKey();
        return secretKey;
    }

    /**
     *  Step2. Create a Cipher by specifying the following parameters
     *          a. Algorithm name - here it is AES
     *
     * @param byteDataToEncrypt data to encrypt
     * @param secretKey the key
     * @param Algorithm the algorithm
     * @return the value
     * @throws Exception a serious problem.
     */
    public byte[] encryptData(byte[] byteDataToEncrypt, Key secretKey, String Algorithm) throws Exception
    {
        byte[] byteCipherText = new byte[200];

        Cipher aesCipher = Cipher.getInstance(Algorithm);

        /**
         *  Step 3. Initialize the Cipher for Encryption
         */
        if (Algorithm.equals("AES"))
        {
            aesCipher.init(Cipher.ENCRYPT_MODE, secretKey, aesCipher.getParameters());
        }
        else if (Algorithm.equals("RSA/ECB/PKCS1Padding"))
        {
            aesCipher.init(Cipher.ENCRYPT_MODE, secretKey);
        }

        /**
         *  Step 4. Encrypt the Data
         *          1. Declare / Initialize the Data. Here the data is of type String
         *          2. Convert the Input Text to Bytes
         *          3. Encrypt the bytes using doFinal method
         */
        byteCipherText = aesCipher.doFinal(byteDataToEncrypt);
        //strCipherText = new String(StringUtilities.encodeBase64(byteCipherText));


        return byteCipherText;
    }

    /**
     *  Step 5. Decrypt the Data
     *          1. Initialize the Cipher for Decryption
     *          2. Decrypt the cipher bytes using doFinal method
     * @param byteCipherText decrypt
     * @param secretKey the key
     * @param Algorithm the algorithm
     * @return the value
     * @throws Exception a serious problem.
     */
    public byte[] decryptData(byte[] byteCipherText, Key secretKey, String Algorithm) throws Exception
    {
        byte[] byteDecryptedText = new byte[200];

        Cipher aesCipher = Cipher.getInstance(Algorithm);
        if (Algorithm.equals("AES"))
        {
            aesCipher.init(Cipher.DECRYPT_MODE, secretKey, aesCipher.getParameters());
        }
        else if (Algorithm.equals("RSA/ECB/PKCS1Padding"))
        {
            aesCipher.init(Cipher.DECRYPT_MODE, secretKey);
        }

        byteDecryptedText = aesCipher.doFinal(byteCipherText);
        //strDecryptedText = new String(byteDecryptedText);

        return byteDecryptedText;
    }

    /**
     *
     * @param pInput the input
     * @return the value
     */
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public byte[] convertStringToByteArray(final String pInput)
    {
        String strInput = pInput.toLowerCase();
        byte[] byteConverted = new byte[(strInput.length() + 1) / 2];
        int j = 0;
        int interimVal;
        int nibble = -1;

        for (int i = 0; i < strInput.length(); ++i)
        {
            interimVal = strHexVal.indexOf(strInput.charAt(i));
            if (interimVal >= 0)
            {
                if (nibble < 0)
                {
                    nibble = interimVal;
                }
                else
                {
                    byteConverted[j++] = (byte) ((nibble << 4) + interimVal);
                    nibble = -1;
                }
            }
        }

        if (nibble >= 0)
        {
            byteConverted[j++] = (byte) (nibble << 4);
        }

        if (j < byteConverted.length)
        {
            byte[] byteTemp = new byte[j];
            System.arraycopy(byteConverted, 0, byteTemp, 0, j);
            byteConverted = byteTemp;
        }

        return byteConverted;
    }
}
