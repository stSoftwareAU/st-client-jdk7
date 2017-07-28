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
package com.aspc.remote.html.selftest;

import com.aspc.remote.html.HTMLPage;
import com.aspc.remote.util.misc.CLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.commons.logging.Log;

/**
 *  check the GWT modules. 
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author         Nigel Leck
 *  
 *  @since          October 22, 2004
 */
public class TestGWT extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.html.selftest.TestGWT");//#LOGGER-NOPMD
    
    /**
     * Creates new VirtualDBTestUnit
     * @param name The name of the test unit
     */
    public TestGWT(String name)
    {
        super( name);
    }

    /**
     * @param args The command line arguments
     */
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }

    /**
     * @return the value
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestGWT.class);
        return suite;
    }

    /**
     * GWT ( as of version 1.4) can only handle one module. 
     *
     * @throws Exception a serious problem
     */
    public void testOnlyOneModuleCanBeLoaded() throws Exception
    {
        HTMLPage page = new HTMLPage();

        page.addGWT( "com.aspc.gwt.commander.Commander");
        page.addGWT( "com.aspc.gwt.doublebuffer.Reload");

        String temp = page.generate();
              
        if( !temp.contains("com.aspc.gwt.hybrids.CommanderReload.nocache.js"))
        {
            LOGGER.info( temp);
            fail( "should have found the hybrid gwt module");
        }
    }
}
