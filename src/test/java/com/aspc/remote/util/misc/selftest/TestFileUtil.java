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
package com.aspc.remote.util.misc.selftest;

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.FileUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.commons.logging.Log;

/**
 *  Check File Utilities.
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author         Nigel Leck
 *  @since          March 2016
 */
public class TestFileUtil extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.selftest.TestFileUtil");//#LOGGER-NOPMD

    /**
     * Creates new VirtualDBTestUnit
     * @param name The name of the test unit
     */
    public TestFileUtil(String name)
    {
        super( name);
    }

    /**
     * run the tests
     * @param args the args
     */
    public static void main(String[] args)
    {
        Test test=suite();
//        test = TestSuite.createTest(TestFileUtil.class, "test1stOct2008");
        TestRunner.run(test);
    }

    /**
     * create a test suite
     * @return the suite of tests
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestFileUtil.class);
        return suite;
    }

    /**
     * What is the default cache dirs.
     *
     */
    public void testDefaultCacheDirs()
    {
        System.getProperties().remove(FileUtil.DOC_CACHE);
        System.getProperties().remove(FileUtil.CACHE_DIR);

        String tmpDir=System.getProperty("java.io.tmpdir");

        String calculatedDocCache=FileUtil.getDocCachePath();
        assertEquals( "default doc cache dir", tmpDir+"/cache/docs/", calculatedDocCache);

        String calculatedCache=FileUtil.getCachePath();
        assertEquals( "default cache dir", tmpDir+"/cache/", calculatedCache);
    }

    /**
     * Cache dirs if DOC_CACHE is set.
     *
     */
    public void testDocCacheSet()
    {
        String tmpDir=System.getProperty("java.io.tmpdir");
        String expectedDocCache=tmpDir + "/st/cache/docs";
        System.getProperties().setProperty(FileUtil.DOC_CACHE,expectedDocCache);
        System.getProperties().remove(FileUtil.CACHE_DIR);

        String calculatedDocCache=FileUtil.getDocCachePath();
        assertEquals( "default doc cache dir", expectedDocCache + "/", calculatedDocCache);

        String calculatedCache=FileUtil.getCachePath();
        assertEquals( "default cache dir", tmpDir+"/st/cache/", calculatedCache);
    }


    /**
     * Cache dirs if CACHE_DIR is set.
     *
     */
    public void testCacheSet()
    {
        String tmpDir=System.getProperty("java.io.tmpdir");
        String expectedCache=tmpDir + "/st/cache";
        System.getProperties().setProperty(FileUtil.CACHE_DIR,expectedCache);
        System.getProperties().remove(FileUtil.DOC_CACHE);

        String calculatedCache=FileUtil.getCachePath();
        assertEquals( "default cache dir", tmpDir+"/st/cache/", calculatedCache);

        String calculatedDocCache=FileUtil.getDocCachePath();
        assertEquals( "default doc cache dir", expectedCache + "/docs/", calculatedDocCache);
    }

    /**
     * ends in '/'
     *
     */
    public void testCacheSet2()
    {
        String tmpDir=System.getProperty("java.io.tmpdir");
        String expectedCache=tmpDir + "/st/cache/";
        System.getProperties().setProperty(FileUtil.CACHE_DIR,expectedCache);
        System.getProperties().remove(FileUtil.DOC_CACHE);

        String calculatedCache=FileUtil.getCachePath();
        assertEquals( "default cache dir", tmpDir+"/st/cache/", calculatedCache);

        String calculatedDocCache=FileUtil.getDocCachePath();
        assertEquals( "default doc cache dir", expectedCache + "docs/", calculatedDocCache);
    }

    public void testBoth()
    {
        String tmpDir=System.getProperty("java.io.tmpdir");

        System.getProperties().setProperty(FileUtil.CACHE_DIR,tmpDir + "/st/other");
        System.getProperties().setProperty(FileUtil.DOC_CACHE, tmpDir + "/st/docs");

        String calculatedCache=FileUtil.getCachePath();
        assertEquals( "default cache dir", tmpDir+"/st/other/", calculatedCache);

        String calculatedDocCache=FileUtil.getDocCachePath();
        assertEquals( "default doc cache dir", tmpDir + "/st/docs/", calculatedDocCache);
    }

    @Override
    /**
     * Close any resources used by the test case. <B>DO NOT RELY ON THE TEAR DOWN RUNNING</B> when debugging we can
     * stop the test case halfway through. The <code>setUp</code> must handle this condition.
     * 
     * @throws java.lang.Exception A serious problem
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        if( docCache==null)
        {
            System.getProperties().remove(FileUtil.DOC_CACHE);
        }
        else
        {
            System.setProperty(FileUtil.DOC_CACHE, docCache);
        }

        if( cacheDir==null)
        {
            System.getProperties().remove(FileUtil.CACHE_DIR);
        }
        else
        {
            System.setProperty(FileUtil.CACHE_DIR, cacheDir);
        }
    }

    @Override
    /**
     * Set up the data universe ready for the test. A test unit maybe stopped half way through the processing so
     * we can not rely on the tear down process to set up the data for the next run.
     * 
     * @throws java.lang.Exception A serious problem
     */
    protected void setUp() throws Exception {
        super.setUp();

        docCache = System.getProperty(FileUtil.DOC_CACHE, null);
        cacheDir = System.getProperty(FileUtil.CACHE_DIR, null);
    }

    private String docCache;
    private String cacheDir;
}
