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
package com.aspc.remote.util.links.selftest;
import com.aspc.remote.database.selftest.*;
import com.aspc.remote.util.links.LinkConnection;
import com.aspc.remote.util.links.LinkGroup;
import com.aspc.remote.util.links.LinkManager;
import com.aspc.remote.util.links.LinkType;
import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.CLogger;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Check LinkGroup
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED test case</i>
 *
 * @author Nigel
 * @since 27 July 2007
 */
public class TestGroup extends DBTestUnit
{
    /**
     * Creates a new unit test
     * @param name the name of the unit
     */
    public TestGroup( final String name )
    {
        super( name );
    }
    
    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite( TestGroup.class  );
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
     * Limit the group
     * @throws Exception a test failure.
     */
    public void testLimitByGroup() throws Exception
    {
        LinkGroup lg = new LinkGroup( "TEST_GROUP");
        lg.setMaximumConnections(10);

        LinkType lt1 = new LinkType("TEST_TYPE1");
        LinkManager.addType(lt1);
        lt1.setMaximumConnections(10);
        lt1.setMinimumConnections(5);
        lt1.setGroup( lg);

        LinkType lt2 = new LinkType("TEST_TYPE2");
        LinkManager.addType(lt2);
        lt2.setMaximumConnections(10);
        lt2.setMinimumConnections(5);
        lt2.setGroup( lg);

        for( int i=0; i<10;i++)
        {
            LinkManager.addConnection("TEST_TYPE1", new LinkConnection(lt1, ""));
        }

        for( int i=0; i<10;i++)
        {
            LinkConnection lc = new LinkConnection(lt1, "");
            LinkManager.addConnection("TEST_TYPE1", lc);
        }

        for( int i=0; i<20;i++)
        {
            LinkManager.addConnection("TEST_TYPE2", new LinkConnection(lt2, ""));
        }

        ConcurrentHashMap<Object, LinkConnection>    checkout=new ConcurrentHashMap<>();
        lt1.testLines(checkout);
        lt2.testLines(checkout);

        lt1.testLines(checkout);
        lt2.testLines(checkout);

        int c1 = LinkManager.countConnections("TEST_TYPE1");
        int c2 = LinkManager.countConnections("TEST_TYPE2");

        assertEquals( "should be limited total connections", 10, c1 + c2);
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.links.selftest.TestGroup");//#LOGGER-NOPMD
}
