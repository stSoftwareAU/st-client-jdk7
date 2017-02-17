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

import com.aspc.remote.database.CSQL;
import com.aspc.remote.database.DataBase;
import com.aspc.remote.database.TableUtil;
import com.aspc.remote.util.misc.CLogger;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.commons.logging.Log;

/**
 * Check that next number works
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED test case</i>
 *
 * @author Nigel
 * @since 17 Oct 2011
 */
public class TestCloseConnection extends DBTestUnit
{
    /**
     * Creates a new unit test
     * @param name the name of the unit
     */
    public TestCloseConnection( final String name )
    {
        super( name );
    }

    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite( TestCloseConnection.class  );
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
     * Check convert to null
     * @throws Exception a test failure.
     */
    public void testAddBatch() throws Exception
    {
        TableUtil tu = TableUtil.find(db);
        if( tu.doesTableExist("zz_batch"))
        {
            tu.dropTable( "zz_batch");
        }
        
        tu.createTable("zz_batch", "name varchar(255)", null);
        CSQL sql = new CSQL( db);

        for( int i=0; i <10; i++)
        {
            try
            {
                sql.perform("SELECT zzzz FROM zz_batch");
            }
            catch( Exception e)
            {
                LOGGER.debug("this is ok");//This is ok
            }

            sql.perform("SELECT * FROM zz_batch");
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
    }
    private DataBase db;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.selftest.TestCloseConnection");//#LOGGER-NOPMD
}
