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
package com.aspc.remote.html.selftest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import com.aspc.remote.html.*;
import com.aspc.remote.util.misc.CLogger;
import org.apache.commons.logging.Log;

/**
 *  Check we detect fav icon.
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author         Nigel Leck
 *
 *  @since          30 March 2016
 */
public class TestPageHead extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.html.selftest.TestPageHead");//#LOGGER-NOPMD

    /**
     * Creates new VirtualDBTestUnit
     * @param name The name of the test unit
     */
    public TestPageHead(String name)
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
        TestSuite suite = new TestSuite(TestPageHead.class);
        return suite;
    }

    /**
     * Should have detected favorite icon.
     *
     * @throws Exception a serious problem
     */
    public void testFavIconPath() throws Exception
    {
        String favPath="/docs/web/st/images/Swagger-logo.png?height=16&width=16";
        String head="<link rel=\"shortcut icon\" type=\"image/png\" href=\"" + favPath + "\"/>";
        HTMLPage page = new HTMLPage(2);
        page.addHeadTag(head);

        String temp=page.getFavIconPath();

        assertEquals( "should detect favIcon", favPath, temp);

    }

    /**
     * Should have detected favorite icon.
     *
     * @throws Exception a serious problem
     */
    public void testFavIconPath2() throws Exception
    {
        String favPath="/docs/web/st/images/Swagger-logo.png?height=16&width=16";
        String head="<link\nhref='" + favPath + "'\nrel=\"shortcut icon\"/>";
        HTMLPage page = new HTMLPage(2);
        page.addHeadTag(head);

        String temp=page.getFavIconPath();

        assertEquals( "should detect favIcon", favPath, temp);

    }
}
