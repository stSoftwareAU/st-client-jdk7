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
package com.aspc.remote.rest.internal.selftest;
import com.aspc.remote.rest.internal.AWSReSTAuthorization;
import com.aspc.remote.rest.internal.ReSTUtil;
import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.CLogger;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Check we can calculate the AWS authorization header.
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED test case</i>
 *
 * @author Nigel Leck
 * @since 12 August 2017
 */
public class TestAWS extends TestCase
{
    /**
     * Creates a new unit test
     * @param name the name of the unit
     */
    public TestAWS( final String name )
{
        super( name );
    }

    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
{
        TestSuite suite = new TestSuite( TestAWS.class );
        return suite;
    }

    /**
     * Entry point to run this test standalone
     * @param args the arguments to the test
     */
    public static void main( String[] args )
    {
        TestRunner.run( suite() );

        System.exit(0);
    }

    private HttpURLConnection create( final String url) throws MalformedURLException
    {
        HttpURLConnection c2=new HttpURLConnection(new URL(url)){
            @Override
            public void disconnect() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean usingProxy() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void connect() throws IOException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        };

        return c2;
    }
    public void testGetPuppy() throws Exception
    {
        AWSReSTAuthorization awsAuth=new AWSReSTAuthorization("AKIAIOSFODNN7EXAMPLE", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");

        String url="https://johnsmith.s3.amazonaws.com/photos/puppy.jpg";
        HttpURLConnection c=create(url);


        c.setRequestProperty("Date", "Tue, 27 Mar 2007 19:36:42 +0000");
        awsAuth.setRequestProperty(c);

        String auth = c.getRequestProperty(ReSTUtil.HEADER_AUTHORIZATION);

        assertEquals(url, "AWS AKIAIOSFODNN7EXAMPLE:bWq2s1WEIj+Ydj0vQ697zp+IXMU=", auth);

    }
    /**
     *  PUT /photos/puppy.jpg HTTP/1.1
     *  Content-Type: image/jpeg
     *  Content-Length: 94328
     *  Host: johnsmith.s3.amazonaws.com
     *  Date: Tue, 27 Mar 2007 21:15:45 +0000
     *
     *  Authorization: AWS AKIAIOSFODNN7EXAMPLE:MyyxeRY7whkBe+bq8fHCL/2kKUg=
     *
     * @throws Exception
     */
    public void testPutPuppy() throws Exception
    {
        AWSReSTAuthorization awsAuth=new AWSReSTAuthorization("AKIAIOSFODNN7EXAMPLE", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");

        String url="https://johnsmith.s3.amazonaws.com/photos/puppy.jpg";
        HttpURLConnection c=create(url);

        c.setRequestMethod("PUT");
        c.setRequestProperty("Date", "Tue, 27 Mar 2007 21:15:45 +0000");
        c.setRequestProperty("Content-Type", "image/jpeg");
        awsAuth.setRequestProperty(c);

        String auth = c.getRequestProperty(ReSTUtil.HEADER_AUTHORIZATION);

        assertEquals(url, "AWS AKIAIOSFODNN7EXAMPLE:MyyxeRY7whkBe+bq8fHCL/2kKUg=", auth);

    }

    /**
     *  GET /?prefix=photos&max-keys=50&marker=puppy HTTP/1.1
     *  User-Agent: Mozilla/5.0
     *  Host: johnsmith.s3.amazonaws.com
     *  Date: Tue, 27 Mar 2007 19:42:41 +0000
     *
     *  Authorization: AWS AKIAIOSFODNN7EXAMPLE:htDYFYduRNen8P9ZfE/s9SuKy0U=
     *
     * @throws Exception
     */
    public void testList() throws Exception
    {
        AWSReSTAuthorization awsAuth=new AWSReSTAuthorization("AKIAIOSFODNN7EXAMPLE", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");

        String url="https://johnsmith.s3.amazonaws.com/?prefix=photos&max-keys=50&marker=puppy";
        HttpURLConnection c=create(url);

        c.setRequestMethod("GET");
        c.setRequestProperty("Date", "Tue, 27 Mar 2007 19:42:41 +0000");

        awsAuth.setRequestProperty(c);

        String auth = c.getRequestProperty(ReSTUtil.HEADER_AUTHORIZATION);

        assertEquals(url, "AWS AKIAIOSFODNN7EXAMPLE:htDYFYduRNen8P9ZfE/s9SuKy0U=", auth);

    }

    /**
     * PUT /db-backup.dat.gz HTTP/1.1
     * User-Agent: curl/7.15.5
     * Host: static.johnsmith.net:8080
     * Date: Tue, 27 Mar 2007 21:06:08 +0000
     *
     * x-amz-acl: public-read
     * content-type: application/x-download
     * Content-MD5: 4gJE4saaMU4BqNR0kLY+lw==
     * X-Amz-Meta-ReviewedBy: joe@johnsmith.net
     * X-Amz-Meta-ReviewedBy: jane@johnsmith.net
     * X-Amz-Meta-FileChecksum: 0x02661779
     * X-Amz-Meta-ChecksumAlgorithm: crc32
     * Content-Disposition: attachment; filename=database.dat
     * Content-Encoding: gzip
     * Content-Length: 5913339
     *
     * Authorization: AWS AKIAIOSFODNN7EXAMPLE:ilyl83RwaSoYIEdixDQcA4OnAnc=
     * @throws java.lang.Exception
     */
     public void testMD5() throws Exception
    {
        AWSReSTAuthorization awsAuth=new AWSReSTAuthorization("AKIAIOSFODNN7EXAMPLE", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");

        /*
         * PUT\n
         * 4gJE4saaMU4BqNR0kLY+lw==\n
         * application/x-download\n
         * Tue, 27 Mar 2007 21:06:08 +0000\n
         *
         * x-amz-acl:public-read\n
         * x-amz-meta-checksumalgorithm:crc32\n
         * x-amz-meta-filechecksum:0x02661779\n
         * x-amz-meta-reviewedby:joe@johnsmith.net,jane@johnsmith.net\n
         * /static.johnsmith.net/db-backup.dat.gz
        */

        String url="http://static.johnsmith.net:8080/db-backup.dat.gz";
        HttpURLConnection c=create(url);

        c.setRequestMethod("PUT");
        c.setRequestProperty("Date", "Tue, 27 Mar 2007 21:06:08 +0000");
        c.setRequestProperty("Content-MD5","4gJE4saaMU4BqNR0kLY+lw==");
        c.setRequestProperty("Content-Type", "application/x-download");

        c.setRequestProperty("x-amz-acl", "public-read");
        c.setRequestProperty("X-Amz-Meta-ReviewedBy", "joe@johnsmith.net,jane@johnsmith.net");
        c.setRequestProperty("X-Amz-Meta-FileChecksum", "0x02661779");
        c.setRequestProperty("X-Amz-Meta-ChecksumAlgorithm", "crc32");

        awsAuth.setRequestProperty(c);

        String auth = c.getRequestProperty(ReSTUtil.HEADER_AUTHORIZATION);

        assertEquals(url, "AWS AKIAIOSFODNN7EXAMPLE:ilyl83RwaSoYIEdixDQcA4OnAnc=", auth);

    }
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.rest.internal.selftest.TestAWS");//#LOGGER-NOPMD
}
