/*
 *  Copyright (c) 2001-2004 ASP Converters pty ltd
 *
 *  www.stSoftware.com.au
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
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
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
public class TestSecurity extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.formula.selftest.TestSecurity");//#LOGGER-NOPMD

    /**
     * Creates new VirtualDBTestUnit
     * @param name The name of the test unit
     */
    public TestSecurity(String name)
    {
        super( name);
    }

    /**
     * @param args  */
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }

    /**
     * @return the value
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestSecurity.class);
        return suite;
    }

    /**
     *
     * @throws Exception a serious problem
     */
    public void testNoJavaAccess() throws Exception
    {
        //LOGGER.info(File.listRoots()[0]);
        String accessToJavaScript=//"importPackage(java.io);\n"+
//                "function X(){"+
            "var roots = java.io.File.listRoots().length;\n";
            //"if( roots.length>0) return 'full'; else return 'none';";
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager( );
        ScriptEngine engine = scriptEngineManager.getEngineByName("JavaScript");
        SimpleBindings b1=new SimpleBindings();
        engine.eval(accessToJavaScript,b1);
        Object val=((Bindings)b1.get( "nashorn.global")).get("roots");
       
        if( val instanceof Number)
        {
            if( ((Number)val).intValue() < 1) 
            {
                fail( "no roots " + val);
            }
        }
        else
        {
            fail( "not a number " + val);
        }
        
        Formula formula = new Formula(
            accessToJavaScript
        );

//        SimpleBindings bindings=new SimpleBindings();

        try
        {
            ServerSecurityManager.modeUserScriptAccess(true);
            SimpleBindings b2=new SimpleBindings();
            engine.eval(accessToJavaScript,b2);
            Object val2=((Bindings)b2.get( "nashorn.global")).get("roots");

            if( val2 instanceof Number)
            {
                if( ((Number)val2).intValue() > 0) 
                {
                    fail( "access roots " + val2);
                }
            }
            else
            {
                fail( "not a number " + val2);
            }
        }
        catch( Exception e)
        {
            LOGGER.info( "this is good", e);
        }
        finally
        {
            ServerSecurityManager.modeUserScriptAccess(false);
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

    @Override
    /**
     * Close any resources used by the test case. <B>DO NOT RELY ON THE TEAR DOWN RUNNING</B> when debugging we can
     * stop the test case halfway through. The <code>setUp</code> must handle this condition.
     * 
     * @throws Exception A serious problem
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();

        QueueLog.flush(1000);
    }


}
