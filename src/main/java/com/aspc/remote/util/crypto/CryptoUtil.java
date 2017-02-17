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

import com.aspc.remote.util.misc.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.logging.Log;

/**
 *  FileUtil
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 * @author Luke
 * @since 17 September 1998
 */
public final class CryptoUtil
{
    /**
     * Cipher type.
     */
    public static final String CIPHER_AES_CBC_PKC5PADDING="AES/CBC/PKCS5Padding";

    /**
     * The default encryption key size.
     * int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
     */
    public static final int DEFAULT_ENCRYPTION_KEY_SIZE=128;

    /**
     * The default encryption algorithm.
     */
    public static final String DEFAULT_ENCRYPTION_ALGORITHM="AES";

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.crypto.CryptoUtil");//#LOGGER-NOPMD

    private CryptoUtil()
    {
    }

    /**
     * Generate a AES key.
     * @return the generated key.
     * @throws java.security.NoSuchAlgorithmException should not happen.
     */
    public static byte[] generateKeyAES() throws NoSuchAlgorithmException
    {
        /* generate AES key */
        KeyGenerator kgen = KeyGenerator.getInstance(DEFAULT_ENCRYPTION_ALGORITHM );
        kgen.init( DEFAULT_ENCRYPTION_KEY_SIZE );
        byte[] cipher = kgen.generateKey().getEncoded();

        return cipher;
    }

    /**
     * Encrypts inFile to outFile using AES/CBC/PKCS5Padding cipher
     * @param targetFile the encrypted file
     * @param inFile the file to be encrypted
     * @param cipher the AES 128 bit key
     * @param initVector the 128 bit initialization vector
     * @throws Exception a failure to encrypt the file
     */
    public static void encryptFile(
        final File inFile,
        final File targetFile,
        final byte[] cipher,
        final byte[] initVector
    ) throws Exception
    {
        File tempFile = File.createTempFile( targetFile.getName(), "encrypt", targetFile.getParentFile());

        // Create Cipher
        Cipher aes = Cipher.getInstance( CIPHER_AES_CBC_PKC5PADDING);
        SecretKeySpec skey = new SecretKeySpec( cipher, DEFAULT_ENCRYPTION_ALGORITHM);
        IvParameterSpec iv = null;
        if( StringUtilities.isBlank( initVector) == false)
        {
            iv = new IvParameterSpec(initVector);
        }
        aes.init(Cipher.ENCRYPT_MODE, skey, iv );

        // Create stream
        FileOutputStream out = null;
        BufferedOutputStream bos = null;
        CipherOutputStream cos = null;

        FileInputStream in = null;

        try
        {
            out = new FileOutputStream(tempFile);
            bos = new BufferedOutputStream(out);
            cos = new CipherOutputStream(bos, aes);

            in = new FileInputStream(inFile);

            byte array[] = new byte[16384];
            while( true)
            {
                int len = in.read(array);

                if( len == -1) break;

                cos.write( array, 0, len);
            }
        }
        finally
        {
            try
            {
                if( in != null )
                {
                    in.close();
                }
            }
            catch(IOException e)
            {
                LOGGER.warn("Exception causing when closing input stream " + in , e );
            }

            try
            {
                if( cos != null )
                {
                    cos.close();
                }
            }
            catch(IOException e)
            {
                LOGGER.warn("Exception causing when closing output stream " + cos, e );
            }

            try
            {
                if( bos != null )
                {
                    bos.close();
                }
            }
            catch(IOException e)
            {
                LOGGER.warn("Exception causing when closing output stream " + bos , e );
            }

            try
            {
                if( out!= null )
                {
                    out.close();
                }
            }
            catch(IOException e)
            {
                LOGGER.warn("Exception causing when closing ouput stream " + out , e );
            }

        }

        FileUtil.replaceTargetWithTempFile( tempFile, targetFile);
    }

    /**
     * Decrypts file.
     * @param targetFile Output file
     * @param inFile Input file
     * @param cipher Cipher code
     * @param initVector Init vector
     * @throws Exception If something went wrong
     */
    public static void decryptFile(
        final File inFile,
        final File targetFile,
        final byte[] cipher,
        final byte[] initVector
    ) throws Exception
    {
        File tempFile = File.createTempFile( targetFile.getName(), "decrypt", targetFile.getParentFile());

        Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec skey = new SecretKeySpec(cipher, "AES");
        IvParameterSpec iv = null;
        if( StringUtilities.isBlank( initVector) == false)
        {
            iv = new IvParameterSpec(initVector);
        }
        aes.init(Cipher.DECRYPT_MODE, skey, iv );

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        CipherInputStream cis = null;

        FileOutputStream fos = null;

        try
        {
            fis = new FileInputStream( inFile );
            bis = new BufferedInputStream( fis );
            cis = new CipherInputStream( bis, aes );

            fos = new FileOutputStream( tempFile );
            byte[] buf = new byte[16384];

            while( true )
            {
                int len = cis.read(buf);
                if( len == -1 )
                {
                    break;
                }
                fos.write(buf, 0, len);
            }
        }
        catch( RuntimeException |IOException e)
        {
            throw new IOException( "Could not decrypt: " + inFile, e);
        }
        finally
        {
            try
            {
                if( cis != null )
                {
                    cis.close();
                }
            }
            catch(IOException e)
            {
                LOGGER.warn("Exception causing when closing input stream " + cis, e );
            }

            try
            {
                if( bis != null )
                {
                    bis.close();
                }
            }
            catch(IOException e)
            {
                LOGGER.warn("Exception causing when closing input stream " + bis, e);
            }

            try
            {
                if( fis != null )
                {
                    fis.close();
                }
            }
            catch(IOException e)
            {
                LOGGER.warn("Exception causing when closing input stream " + fis , e );
            }

            try
            {
                if( fos!= null )
                {
                    fos.close();
                }
            }
            catch(IOException e)
            {
                LOGGER.warn("Exception causing when closing ouput stream " + fos, e );
            }

        }

        FileUtil.replaceTargetWithTempFile( tempFile, targetFile);
    }
}
