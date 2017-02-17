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
import com.aspc.remote.util.misc.FixedWidthRecord;
import org.apache.commons.logging.Log;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import junit.framework.TestCase;
/**
 * Tests the behaviour of fixed width records.
 * @author      luke
 * @since       May 22, 2007
 */
public class TestFixedWidthRecord extends TestCase
{
    /**
     * Creates a new unit test
     * @param name the name of the unit
     */
    public TestFixedWidthRecord( final String name )
    {
        super( name );
    }
    
    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite( TestFixedWidthRecord.class );
        return suite;
    }
    
    /**
     * Entry point to run this test standalone
     * @param args the arguments to the test
     */
    public static void main( String[] args )
    {
        //parseArgs( args );
        TestRunner.run( suite() );
        
        info( "TestFixedWidthRecord completed." );
        System.exit(0);
    }
    
    /**
     * Tests a simple fixed width record.
     * @throws Exception a test failure
     */
    public void testSimple() throws Exception
    {
        FixedWidthRecord fwr = new FixedWidthRecord( "id 0 1 . left, code 1 5 0 right, name 5 10 space right" );
        fwr.set( "id", "D" );
        fwr.set( "code", "777" );
        fwr.set( "name", "zZz" );
        
        info( "0123456789" );
        info( fwr );
        
        assertEquals( "fwr should be 10 characters wide", 10, fwr.toString().length() );
        
        String spec = "id 0 1 D left," +
                "code 1 10 0 right," +
                "fill 10 11 space left," +
                "name 11 30 space left," +
                "amt 30 36 0 right";
        fwr = new FixedWidthRecord(spec);
        fwr.set( "code", "777" );
        fwr.set( "name", "john smith" );
        fwr.set( "amt", String.valueOf( 517*100 ) );
        
        info( fwr );
        
        assertEquals( "record should be 36 characters wide", 36, fwr.toString().length() );
    }
    
    private static void info( final Object o )
    {
        LOGGER.info( o );
    }
    
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.selftest.TestFixedWidthRecord");//#LOGGER-NOPMD
}
