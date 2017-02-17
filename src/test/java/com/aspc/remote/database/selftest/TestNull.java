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
import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.database.CSQL;
import com.aspc.remote.database.DataBase;
import com.aspc.remote.database.NotFoundException;
import com.aspc.remote.database.TableUtil;

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
public class TestNull extends DBTestUnit
{
    /**
     * Creates a new unit test
     * @param name the name of the unit
     */
    public TestNull( final String name )
    {
        super( name );
    }
    
    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite( TestNull.class  );
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
    public void testConvertNull1() throws Exception
    {
        CSQL sql = new CSQL( db);

        sql.findOne(
            "SELECT " +
            tu.convertNull("iCol", 0) +"," +
            tu.convertNull("inCol", 0) +"," +
            tu.convertNull("cCol", "") +"," +
            tu.convertNull("cnCol", "") +"," +
            tu.convertNull("dCol", 0) +"," +
            tu.convertNull("dnCol", 0) +"," +
            tu.convertNull("lCol", 0) +"," +
            tu.convertNull("lnCol", 0) +"," +
            tu.convertNull("tCol", "") +"," +
            tu.convertNull("tnCol", "") +
            "\nFROM zz_dummy" +
            "\nWHERE name='BLANK-1'"
        );

        assertEquals( "iCol", 0, sql.getInt(1));
        assertEquals( "inCol", 0, sql.getInt(2));
        assertEquals( "cCol", 0, sql.getInt(3));
        assertEquals( "cnCol", 0, sql.getInt(4));
        assertEquals( "dCol", 0, sql.getInt(5));
        assertEquals( "dnCol", 0, sql.getInt(6));
        assertEquals( "lCol", 0, sql.getInt(7));
        assertEquals( "lnCol", 0, sql.getInt(8));
        assertEquals( "tCol", 0, sql.getInt(9));
        assertEquals( "tnCol", 0, sql.getInt(10));
    }

    /**
     * Check convert to null
     * @throws Exception a test failure.
     */
    public void testConvertNull2() throws Exception
    {
        CSQL sql = new CSQL( db);

        sql.findOne(
            "SELECT " +
            tu.convertNull("iCol", 2) +"," +
            tu.convertNull("inCol", 2) +"," +
            tu.convertNull("cCol", "2") +"," +
            tu.convertNull("cnCol", "2") +"," +
            tu.convertNull("dCol", 2) +"," +
            tu.convertNull("dnCol", 2) +"," +
            tu.convertNull("lCol", 2) +"," +
            tu.convertNull("lnCol", 2) +"," +
            tu.convertNull("tCol", "2") +"," +
            tu.convertNull("tnCol", "2") +
            "\nFROM zz_dummy" +
            "\nWHERE name='BLANK-1'"
        );

        assertEquals( "iCol", 0, sql.getInt(1));
        assertEquals( "inCol", 2, sql.getInt(2));
        assertEquals( "cCol", 0, sql.getInt(3));
        assertEquals( "cnCol", 2, sql.getInt(4));
        assertEquals( "dCol", 0, sql.getInt(5));
        assertEquals( "dnCol", 2, sql.getInt(6));
        assertEquals( "lCol", 0, sql.getInt(7));
        assertEquals( "lnCol", 2, sql.getInt(8));
        assertEquals( "tCol", 0, sql.getInt(9));
        assertEquals( "tnCol", 2, sql.getInt(10));
    }

    /**
     * Check is blank cause
     * @throws Exception a test failure.
     */
    public void testIsBlankClause() throws Exception
    {
        CSQL sql = new CSQL( db);

        String checks[][]={
           // {"iCol","N"},
           // {"inCol","Y"},
            {"cCol","Y"},
            {"cnCol","Y"},
          //  {"dCol","N"},
          //  {"dnCol","Y"},
         //   {"lCol","N"},
         //   {"lnCol","Y"},
            {"tCol","Y"},
            {"tnCol","Y"},
        };

        for (String[] check : checks) {
            try {
                sql.findOne("SELECT name" +
                        "\nFROM zz_dummy" +
                        "\nWHERE name='BLANK-1'" +
                        "\nAND " + tu.isBlankClause(check[0]));
                if (check[1].equals("N")) {
                    fail("should have NOT found " + check[0]);
                }
            } catch (NotFoundException nf) {
                if (check[1].equals("Y")) {
                    fail("should have found " + check[0]);
                }
            }
        }
    }

    /**
     * Check is blank cause
     * @throws Exception a test failure.
     */
    public void testIsBlankClause2() throws Exception
    {
        CSQL sql = new CSQL( db);

        String checks[][]={
           // {"iCol","N"},
           // {"inCol","Y"},
            {"cCol","N"},
            {"cnCol","Y"},
          //  {"dCol","N"},
          //  {"dnCol","Y"},
         //   {"lCol","N"},
         //   {"lnCol","Y"},
            {"tCol","N"},
            {"tnCol","Y"},
        };

        for (String[] check : checks) {
            try {
                sql.findOne("SELECT name" +
                        "\nFROM zz_dummy" +
                        "\nWHERE name='BLANK-2'" +
                        "\nAND " + tu.isBlankClause(check[0]));
                if (check[1].equals("N")) {
                    fail("should have NOT found " + check[0]);
                }
            } catch (NotFoundException nf) {
                if (check[1].equals("Y")) {
                    fail("should have found " + check[0]);
                }
            }
        }
    }

    /**
     * Check not blank cause
     * @throws Exception a test failure.
     */
    public void testNotBlankClause() throws Exception
    {
        CSQL sql = new CSQL( db);

        String checks[][]={
           // {"iCol","N"},
           // {"inCol","Y"},
            {"cCol","Y"},
            {"cnCol","Y"},
          //  {"dCol","N"},
          //  {"dnCol","Y"},
         //   {"lCol","N"},
         //   {"lnCol","Y"},
            {"tCol","Y"},
            {"tnCol","Y"},
        };

        for (String[] check : checks) {
            try {
                sql.findOne("SELECT name" +
                        "\nFROM zz_dummy" +
                        "\nWHERE name='DATA-1'" +
                        "\nAND " + tu.notBlankClause(check[0]));
                if (check[1].equals("N")) {
                    fail("should have NOT found " + check[0]);
                }
            } catch (NotFoundException nf) {
                if (check[1].equals("Y")) {
                    fail("should have found " + check[0]);
                }
            }
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
        tu = TableUtil.find( db);
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
            "name varchar(255), inCol int NULL, cnCol varchar(255) NULL, iCol int NOT NULL, cCol varchar(255) NOT NULL, dnCol " +
            tu.getDoubleTypeName()  + " NULL, dCol " +
            tu.getDoubleTypeName() + " NOT NULL, lnCol " + tu.getLongTypeName() + " NULL," +
            "lCol " + tu.getLongTypeName() + " NOT NULL," +
            "tCol " + tu.getTextTypeName() + " NOT NULL," +
            "tnCol " + tu.getTextTypeName() + " NULL",
            null
        );

        CSQL sql = new CSQL( db);

        sql.perform("INSERT INTO zz_dummy(name,iCol,inCol,cCol,cnCol,dCol,dnCol,lCol,lnCol,tCol,tnCol)VALUES('BLANK-1',0,NULL,' ',NULL,0.0,NULL,0,NULL,' ',NULL)");
        sql.perform("INSERT INTO zz_dummy(name,iCol,inCol,cCol,cnCol,dCol,dnCol,lCol,lnCol,tCol,tnCol)VALUES('BLANK-2',0,NULL,'AAA',NULL,0.0,NULL,0,NULL,'AAA',NULL)");
        sql.perform("INSERT INTO zz_dummy(name,iCol,inCol,cCol,cnCol,dCol,dnCol,lCol,lnCol,tCol,tnCol)VALUES('DATA-1',1,1,'AAA','AAA',1,1,1,1,'AAA','AAA')");
    }
    private DataBase db;
    private TableUtil tu;

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.selftest.TestNull");//#LOGGER-NOPMD
}
