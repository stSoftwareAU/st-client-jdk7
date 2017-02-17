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
package com.aspc.remote.util.net.selftest;

import com.aspc.remote.util.crypto.CryptoUtil;
import com.aspc.remote.util.misc.FileUtil;
import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import java.io.*;
import java.security.*;
import javax.crypto.*;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests the NetUtil encrypt file and decrypt file operations
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED test case</i>
 *
 * @author      luke
 * @since       November 28, 2005
 */
public class TestEncryption extends TestCase
{
    /**
     * Creates a new unit test
     * @param name the name of the unit
     */
    public TestEncryption( final String name )
    {
        super( name );
    }
    
    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite( TestEncryption.class );
        return suite;
    }
    
    /**
     * Entry point to run this test standalone
     * @param args the arguments to the test
     */
    public static void main( String[] args )
    {
        TestRunner.run( suite() );
        
        LOGGER.info( "TestEncryption completed." );
        
        System.exit(0);
    }
    
    /**
     * Sets up a small test file and a large test file for the encrypt and decrypt methods
     * @throws Exception a setup failure
     */
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        small = File.createTempFile( "temp", "sml" );
        small.deleteOnExit();
        fill( small, sml );
        
        large = File.createTempFile( "temp", "lrg" );
        large.deleteOnExit();
        fill( large, lrg );
    }
    
    /**
     * Tests that a file is encrypted and decrypted
     * @throws Exception a test failure
     */
    public void testEncryptDecrypt() throws Exception
    {
        File enc = File.createTempFile( "temp", "enc" );
        enc.deleteOnExit();
        
        KeyGenerator kgen = KeyGenerator.getInstance( CryptoUtil.DEFAULT_ENCRYPTION_ALGORITHM );
        kgen.init(128);
        SecretKey skey = kgen.generateKey();
        byte[] key = skey.getEncoded();
        
        SecureRandom sr = new SecureRandom();
        byte[] random = new byte[16];
        sr.nextBytes( random );
        
        try
        {
            CryptoUtil.encryptFile( small, enc, key, random );
        }
        catch( Exception e )
        {
            info( e );
            fail( "small file encryption operation failed" );
        }
        
        File dec = File.createTempFile( "temp", "dec" );
        dec.deleteOnExit();
        
        try
        {
            CryptoUtil.decryptFile( enc, dec, key, random );
        }
        catch( Exception e )
        {
            info( e );
            fail( "small file decryption operation failed" );
        }
        
        byte[] org = FileUtil.generateCheckSum( small );
        String chkSum = new String( StringUtilities.encodeBase64( org ), "ascii" );

        if( FileUtil.isValid( dec, chkSum, -1 ) == false )
        {
            fail( "small file failed checksum" );
        }
        
        try
        {
            CryptoUtil.encryptFile( large, enc, key, random );
        }
        catch( Exception e )
        {
            info( e );
            fail( "large file encryption operation failed" );
        }
        
        try
        {
            CryptoUtil.decryptFile( enc, dec, key, random );
        }
        catch( Exception e )
        {
            info( e );
            fail( "large file decryption operation failed" );
        }
        
        org = FileUtil.generateCheckSum( large );
        chkSum = new String( StringUtilities.encodeBase64( org ), "ascii" );
        if( FileUtil.isValid( dec, chkSum, -1 ) == false )
        {
            fail( "large file failed checksum" );
        }
    }
    
    /**
     * tests encrypt/decrypt for 128
     * @throws Exception a test failure
     */
    public void test128()throws Exception
    {
        File enc = File.createTempFile( "temp", "enc" );
        enc.deleteOnExit();
        
        KeyGenerator kgen = KeyGenerator.getInstance( CryptoUtil.DEFAULT_ENCRYPTION_ALGORITHM );
        kgen.init(128);
        SecretKey skey = kgen.generateKey();
        byte[] key = skey.getEncoded();
        
        SecureRandom sr = new SecureRandom();
        byte[] random = new byte[16];
        sr.nextBytes( random );
        
        try
        {
            CryptoUtil.encryptFile( small, enc, key, random );
        }
        catch( Exception e )
        {
            info( e );
            fail( "128 bit encryption failed" );
        }
        
        File dec = File.createTempFile( "temp", "dec" );
        dec.deleteOnExit();
        
        try
        {
            CryptoUtil.decryptFile( enc, dec, key, random );
        }
        catch( Exception e )
        {
            info( e );
            fail( "128 bit decryption failed" );
        }
        /*
        kgen.init( 256 );
        skey = kgen.generateKey();
        key = skey.getEncoded();
        
        try
        {
            FileUtil.encryptFile( small, enc, key, random );
            
            fail( "256 bit encryption should fail" );
        }
        catch( Exception e ) // good
        {
            info( e );
        }
        
        try
        {
            FileUtil.decryptFile( enc, dec, key, random );
            
            fail( "256 bit decryption should fail" );
        }
        catch( Exception e ) //good
        {
            info( e );
        }*/
    }
    
    /*
     * Fills the given file with the given number of lines of text
     */
    private void fill( final File file, final int lines ) throws Exception
    {
        try (FileWriter fw = new FileWriter( file )) {
            for( int i = 0; i < lines; i++ )
            {
                fw.write( LINE_OF_TEXT );
            }}
    }
    
    private static void info( final Exception e )
    {
        LOGGER.info( e.getMessage() );
        StackTraceElement[] err = e.getStackTrace();
        for (StackTraceElement err1 : err) {
            LOGGER.info(err1);
        }
    }
    
    private File small, large;
    
    private static final int sml = 100, lrg = 10000;//NOPMD
    
    private static final String LINE_OF_TEXT = "Lorem ipsum dolor sit amet," +
            " consectetur adipisicing elit," +
            " sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\n";
    
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.net.selftest.TestEncryption");//#LOGGER-NOPMD
}
