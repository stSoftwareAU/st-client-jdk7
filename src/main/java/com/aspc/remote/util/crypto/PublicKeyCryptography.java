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
import com.aspc.remote.util.misc.StringUtilities;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.*;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
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
public final class PublicKeyCryptography
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.crypto.PublicKeyCryptography");//#LOGGER-NOPMD

    private final X509Certificate recvcert;
    private final PublicKey publicKey;
    private final Key privateKey;
    private static final ConcurrentHashMap<String, PublicKeyCryptography> LOADED=new ConcurrentHashMap<> ();
    /**
     * @param factory The factory to create the pools with
     */
    private PublicKeyCryptography(
        final String keyStoreFile,
        final String keyStorePW,
        final String keyStoreAlias
    ) throws Exception
    {
        KeyStore ks;
        ks = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] password = keyStorePW.toCharArray();
        try (java.io.FileInputStream fis = new java.io.FileInputStream(keyStoreFile)) {
            ks.load(fis, password);
        }

        // 2.2 Creating an X509 Certificate of the Receiver

        //MessageDigest md = MessageDigest.getInstance("MD5");
        recvcert = (X509Certificate) ks.getCertificate(keyStoreAlias);

        // 2.3 Getting the Receivers public Key from the Certificate
        publicKey = recvcert.getPublicKey();
        privateKey =ks.getKey(keyStoreAlias, password);
    }

    /**
     *
     * @param keyStoreFile the file
     * @param keyStorePW the password
     * @param keyStoreAlias the alias
     * @return the value
     * @throws Exception a serious problem.
     */
    public static PublicKeyCryptography load(
        final String keyStoreFile,
        final String keyStorePW,
        final String keyStoreAlias
    ) throws Exception
    {
        LOGGER.info( "file: " + keyStoreFile + " pw: " + keyStorePW + " alias: " + keyStoreAlias );
        PublicKeyCryptography pc = new PublicKeyCryptography(keyStoreFile, keyStorePW, keyStoreAlias);

        LOADED.put( pc.getSerialNumberString(), pc);

        return pc;
    }

    /**
     *
     * @param serialNumber the number
     * @return the value
     */
    public static PublicKeyCryptography get( final String serialNumber)
    {
        return LOADED.get( serialNumber.toLowerCase());
    }

    /**
     *
     * @return the value
     */
    public String getSerialNumberString()
    {
        return recvcert.getSerialNumber().toString(16);
    }

    /**
     *
     * @param encryptedData the data
     * @return the value
     * @throws Exception a serious problem.
     */
    public String decrypt64(final String encryptedData) throws Exception
    {
        SymmetricEncrypt encryptUtil = new SymmetricEncrypt();

        StringTokenizer st = new StringTokenizer( encryptedData.trim(), "\n");
        StringBuilder sb=new StringBuilder();
        while( st.hasMoreElements())
        {
            byte[] byteCipherText = StringUtilities.decodeBase64(st.nextToken().trim().getBytes(StandardCharsets.UTF_8));

            byte []temp = encryptUtil.decryptData(byteCipherText, privateKey, "RSA/ECB/PKCS1Padding") ;

            sb.append( new String( temp));
        }

        return sb.toString();
    }

    private static final int SIZE=80;
    /**
     *
     * @param data the data
     * @return the value
     * @throws Exception a serious problem.
     */
    public String encrypt64(final String data) throws Exception
    {
        SymmetricEncrypt encryptUtil = new SymmetricEncrypt();

        StringBuilder sb=new StringBuilder();
        for( int pos=0; pos *SIZE < data.length(); pos++)
        {
            if(pos > 0) sb.append( "\n");

            String str;
            if( (pos + 1) * SIZE < data.length())
            {
                str = data.substring(pos * SIZE, (pos + 1) * SIZE);
            }
            else
            {
                str = data.substring(pos * SIZE);
            }
            byte []temp = encryptUtil.encryptData(str.getBytes(StandardCharsets.UTF_8), publicKey, "RSA/ECB/PKCS1Padding") ;


            sb.append( StringUtilities.encodeBase64( temp));
        }

        return sb.toString();
    }
}
