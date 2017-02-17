/*
 *  Copyright (c) 2001-2004 ASP Converters pty ltd
 *
 *  www.aspconverters.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.formula.selftest;

import com.aspc.remote.formula.Formula;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.QueueLog;
import com.aspc.remote.util.misc.ServerSecurityManager;
import javax.script.SimpleBindings;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.commons.logging.Log;

/**
 *  Formula
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author         Nigel Leck
 * @since 29 September 2006
 *  
 */
public class TestJIT extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.formula.selftest.TestJIT");//#LOGGER-NOPMD

    /**
     * Creates new VirtualDBTestUnit
     * @param name The name of the test unit
     */
    public TestJIT(String name)
    {
        super( name);
    }

    /**
     * @param args  */
    public static void main(String[] args)
    {
        TestRunner.run(suite());
        QueueLog.flush(10000);
    }

    /**
     * @return the value
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestJIT.class);
        return suite;
    }

    /**
     *
     * @throws Exception a serious problem
     */
    public void testCompileWhenRepeatedlyCalled() throws Exception
    {
        SimpleBindings bindings=new SimpleBindings();

        Formula formula = new Formula("1+1" );

        for( int i =0; i < Formula.JIT_CALL_LIMIT * 2; i++)
        {
            Object result;
            result = formula.compute( bindings);

            assertEquals( "1+1", 2, result);

            assertEquals( i + " calls ", i >= Formula.JIT_CALL_LIMIT, formula.isCompiled());
        }
    }

    @Override
    /**
     * Set up the data universe ready for the test. A test unit maybe stopped half way through the processing so
     * we can not rely on the tear down process to set up the data for the next run.
     *
     * @throws Exception A serious problem
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        SecurityManager sm = System.getSecurityManager();
        if( sm == null)
        {
            sm = new ServerSecurityManager();
            System.setSecurityManager(sm);
        }
    }
}
