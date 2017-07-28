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
package com.aspc.remote.util.misc.selftest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.aspc.remote.util.misc.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.w3c.dom.Document;

/**
 *  Check DocumentUtil
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author         Nigel Leck
 *  
 *  @since          13 February @014
 */
public class TestDocumentUtil extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.selftest.TestDocumentUtil");//#LOGGER-NOPMD
    private SecurityManager originalSecurityManager;

    /**
     * Creates new VirtualDBTestUnit
     * @param name The name of the test unit
     */
    public TestDocumentUtil(String name)
    {
        super( name);
    }

    /**
     * run the tests
     * @param args the args
     */
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }

    /**
     * create a test suite
     * @return the suite of tests
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestDocumentUtil.class);
        return suite;
    }

    public void testBlank() throws Exception
    {
        Document doc = DocumentUtil.newDocument();
        
        DocumentUtil.docToString(doc);
    }
    
    /**
     * check tolerance
     * @throws Exception a serious problem
     */
    public void testTolerance() throws Exception
    {
        //String xml = FileUtil.readFile(
        File f=new File(
            System.getProperty("SRC_DIR") + "/com/aspc/remote/util/misc/selftest/sample2.xml"
        //    )
        );
        
        try
        {
            DocumentUtil.loadDocument(f);
            fail( "should not parse the xml");
        }
        catch( Exception e)
        {
            
        }
        
        try
        {
            DocumentUtil.loadDocument(f, DocumentUtil.PARSER.VALIDATE);
            fail( "validate should not parse the xml");
        }
        catch( Exception e)
        {
            
        }
        Document doc = DocumentUtil.loadDocument(f, DocumentUtil.PARSER.TOLERANT);
        
        String xml=DocumentUtil.docToString(doc);
        
        LOGGER.info( xml);
    }
    
    /**
     * read a XSLT file
     * @throws Exception a serious problem
     */
    public void testSample1() throws Exception
    {
        check( "sample1");
    }
    private void check( final String name) throws Exception
    {
        ServerSecurityManager ssm = new ServerSecurityManager();

        System.setSecurityManager( ssm);

        String xslt = FileUtil.readFile(
            new File(
                System.getProperty("SRC_DIR") + "/com/aspc/remote/util/misc/selftest/" + name + ".xslt"
            )
        );

        Transformer transformer = DocumentUtil.newTransformer(xslt);

        ByteArrayOutputStream out= new ByteArrayOutputStream();

        Document doc=DocumentUtil.loadDocument(
            new File(
            System.getProperty("SRC_DIR") + "/com/aspc/remote/util/misc/selftest/" + name + ".rss"
        ));
        try
        {
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(out);
            ServerSecurityManager.modeUserXlstAccess(true);
            try
            {
                transformer.transform(domSource, streamResult);
            }
            finally
            {
                ServerSecurityManager.modeUserXlstAccess(false);
            }
            String data = out.toString(StandardCharsets.UTF_8.name());
            LOGGER.info( data);
        }
        catch( TransformerException e)
        {
            LOGGER.warn( "could not transform", e);
            fail( "could not transform");
        }
        catch (UnsupportedEncodingException e)
        {
            LOGGER.warn( "could not encode", e);
            fail( "could not transform");
        }
    }

    /**
     * Setup
     * @throws Exception a serious problem
     */
    @Override
    protected void setUp() throws Exception
    {
        originalSecurityManager = System.getSecurityManager();
        try
        {
            System.setSecurityManager( null);
        }
        catch( Exception e)
        {
            LOGGER.warn( "Try to clear secutiry manager", e);
        }
    }

    /**
     * Tear down
     * @throws Exception a serious problem
     */
    @Override
    protected void tearDown() throws Exception
    {
        try
        {
            System.setSecurityManager(originalSecurityManager);
        }
        catch( Throwable t)
        {
            LOGGER.warn( "don't care", t);
        }
    }
}
