/**
 *  STS Remote library
 *
 *  Copyright (C) 2006  stSoftware Pty Ltd
 *
 *  stSoftware.com.au
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

import com.aspc.remote.util.misc.FileUtil;
import java.io.File;
import java.io.FileWriter;
import com.aspc.remote.util.net.*;

/**
 * Check a file store Location.
 *
 * give this class two url's for a file store location. check that the file store
 * exists, we can connect, can send a file to tmp folder.
 *
 * check that normal (admin) user can rename the file into the 'docs' folder.
 *
 * check that the restricted user has readonly to the 'docs' folder.
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED test case</i>
 *
 * @author      Luke Butt
 * @since       30 August 2005, 16:21
 */
public class LocationCheck 
{
    private final String adminUrl;
    private final String userUrl;
    private File testFile;
    
    /**
     * Creates a new LocationCheck, with given admin url and user url
     *
     * @param adminURL the admin user url for the location
     * @param userURL the restricted user url for the location
     */
    public LocationCheck( String adminURL, String userURL ) 
    {
        this.adminUrl = adminURL;
        this.userUrl = userURL;
    }
    
    /**
     * Check that the location behaviour is as expected.
     * This means that we can connect and then the privileges are as expected.
     * @throws Exception the location is not ok
     */
    @SuppressWarnings("empty-statement")
    public void check() throws Exception
    {
        createTestFile();
        
        String tmpnam = "tmp/" + testFile.getName();
        String docnam = "docs/" + testFile.getName();
        /* these should work */
        NetUtil.sendData( testFile, userUrl, tmpnam );
        NetUtil.renameData( adminUrl, tmpnam, docnam ); 
        NetUtil.sendData( testFile, adminUrl, docnam ); // modify an existing file
        NetUtil.removeData( adminUrl, docnam );
        
        /* these should fail */
        boolean failed = false;
        String msg = "";
        
        try
        {
            NetUtil.sendData( testFile, userUrl, tmpnam ); // allowed
            
            try
            {
                NetUtil.sendData( testFile, userUrl, docnam );
                failed = true;
                msg += "user url was able to create file in docs folder, ";
                NetUtil.removeData( adminUrl, docnam ); // clean up
            }
            catch ( Exception e ) 
            {
                ;// ignore, we want it to fail
            }
            
            try
            {
                NetUtil.renameData( userUrl, tmpnam, docnam );
                failed = true;
                msg += "user url was able to rename file to docs folder, ";
            }
            catch ( Exception e )
            {
                NetUtil.renameData( adminUrl, tmpnam, docnam ); // set up next test
            }
            
            try
            {
                NetUtil.sendData( testFile, userUrl, docnam );
                failed = true;
                msg += "user url was able to modify file in docs folder, ";
            }
            catch ( Exception e ) 
            {
                ;// ignore, we want it to fail
            }

            
            try
            {
                NetUtil.removeData( userUrl, docnam );
                failed = true;
                msg += "user url was able to delete file in docs folder, ";
            }
            catch ( Exception e )
            {
                NetUtil.removeData( adminUrl, docnam ); // clean up
            }
            
        }
        finally
        {
            deleteTestFile();
        }
        if( failed )
        {
            throw new Exception(msg);
        }
    }
    
    private void createTestFile() throws Exception
    {
        String filler = "This is a test file for the LocationCheck class"
                + "\n\nluke@stsoftware.com.au";
        testFile = File.createTempFile("chk", null,FileUtil.makeQuarantineDirectory() );
        try (FileWriter fw = new FileWriter(testFile)) {
            fw.write(filler);
        }
    }
    
    private void deleteTestFile()
    {
        if( testFile != null && testFile.exists() )
        {
            testFile.delete();
        }
    }
}
