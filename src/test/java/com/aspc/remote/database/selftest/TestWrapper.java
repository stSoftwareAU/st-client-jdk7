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
package com.aspc.remote.database.selftest;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.database.CSQL;
import com.aspc.remote.database.DataBase;
import com.aspc.remote.database.TableUtil;
import com.aspc.remote.database.internal.wrapper.WrapperAssertError;
import java.sql.ResultSet;


import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Check that next number works
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED test case</i>
 *
 * @author Nigel
 * @since 27 July 2007
 */
public class TestWrapper extends DBTestUnit
{
    /**
     * Creates a new unit test
     * @param name the name of the unit
     */
    public TestWrapper( final String name )
    {
        super( name );
    }
    
    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite( TestWrapper.class  );
        return suite;
    }
    
    /**
     * Entry point to run this test standalone
     * @param args the arguments to the test
     */
    public static void main( String[] args )
    {
        parseArgs( args );
        TestRunner.run( suite() );
    }


    /**
     * check that we must always call approximate = true 
     * @throws Exception a test failure.
     */
    public void testGetIndexInfo() throws Exception
    {
        Connection c = db.checkOutConnection();
        DatabaseMetaData metaData = c.getMetaData();
        metaData.getIndexInfo(null, null, "trans_header", false, true);
        try
        {
            metaData.getIndexInfo(null, null, "trans_header", false, false);
            fail( "Should not allowed call with approximate equals false");
        }
        catch( WrapperAssertError wae)
        {
            LOGGER.debug("expected", wae);
        }
    }

    /**
     * Check that we detect non-closed statements
     * @throws Exception a test failure.
     */
    public void testNonClosed() throws Exception
    {
        Connection c = db.checkOutConnection();

        c.createStatement();

        try
        {
            db.checkInConnection(c);
            fail( "should not have been able to check in");
        }
        catch( WrapperAssertError wae)
        {
            LOGGER.debug("expected", wae);
        }        
    }
    
    /**
     * Check that we detect non-closed statements
     * @throws Exception a test failure.
     */
    public void testColums() throws Exception
    {
        TableUtil tu = TableUtil.find(db); 
        
        boolean flag = tu.doesColumnExist("aspc_server", "zzzzz1");
        
        assertFalse( "column should not exist", flag);
                
    }


    /**
     * Check that we detect non-closed statements
     * @throws Exception a test failure.
     */
    public void testNonClosed2() throws Exception
    {
        Connection c = db.checkOutConnection();

        c.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);

        try
        {
            db.checkInConnection(c);
            fail( "should not have been able to check in");
        }
        catch( WrapperAssertError wae)
        {
            LOGGER.debug("expected", wae);
        }
    }

    /**
     * Check that we detect non-closed statements
     * @throws Exception a test failure.
     */
    public void testNonClosed3() throws Exception
    {
        Connection c = db.checkOutConnection();

        c.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

        try
        {
            db.checkInConnection(c);
            fail( "should not have been able to check in");
        }
        catch( WrapperAssertError wae)
        {
            LOGGER.debug("expected", wae);
        }
    }

    /**
     * Check that we detect non-closed statements
     * @throws Exception a test failure.
     */
    public void testPrepareCallNonClosed() throws Exception
    {
        Connection c = db.checkOutConnection();

        c.prepareCall("SELECT * FROM zz_dummy");

        try
        {
            db.checkInConnection(c);
            fail( "should not have been able to check in");
        }
        catch( WrapperAssertError wae)
        {
            LOGGER.debug("expected", wae);
        }
    }

    /**
     * Check that we detect non-closed statements
     * @throws Exception a test failure.
     */
    public void testPrepareNonClosed2() throws Exception
    {
        Connection c = db.checkOutConnection();

        c.prepareCall("SELECT * FROM zz_dummy", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);

        try
        {
            db.checkInConnection(c);
            fail( "should not have been able to check in");
        }
        catch( WrapperAssertError wae)
        {
            LOGGER.debug("expected", wae);
        }
    }

    /**
     * Check that we detect non-closed statements
     * @throws Exception a test failure.
     */
    public void testPrepareNonClosed3() throws Exception
    {
        Connection c = db.checkOutConnection();

        c.prepareCall("SELECT * FROM zz_dummy",ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

        try
        {
            db.checkInConnection(c);
            fail( "should not have been able to check in");
        }
        catch( WrapperAssertError wae)
        {
            LOGGER.debug("expected", wae);
        }
    }

    /**
     * Set up the data universe ready for the test. A test unit maybe stopped half way through the processing so
     * we can not rely on the tear down process to set up the data for the next run.
     *
     * @throws Exception A serious problem
     */
    @Override
    @SuppressWarnings("empty-statement")
    protected void setUp() throws Exception
    {
        super.setUp();

        db = DataBase.getCurrent();
        TableUtil tu = TableUtil.find( db);
        try
        {
            tu.dropTable( "zz_dummy");
        }
        catch( Exception e)
        {
            ;// I don't care
        }

        tu.createTable(
            "zz_dummy",
            "name varchar(255)",
            null
        );

        CSQL sql = new CSQL( db);

        sql.perform("INSERT INTO zz_dummy(name)VALUES('TEST1')");
        sql.perform("INSERT INTO zz_dummy(name)VALUES('TEST2')");
    }

    private DataBase db;

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.selftest.TestWrapper");//#LOGGER-NOPMD
}
