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

import com.aspc.remote.html.HTMLForm;
import com.aspc.remote.html.HTMLPage;
import com.aspc.remote.html.HTMLTextArea;
import com.aspc.remote.util.misc.CLogger;
import java.io.FileWriter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.commons.logging.Log;

/**
 *  test html text html
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author         Nigel Leck
 *  
 *  @since          October 22, 2004
 */
public class TestTextArea extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.html.selftest.TestTextArea");//#LOGGER-NOPMD

    /**
     * Creates new VirtualDBTestUnit
     * @param name The name of the test unit
     */
    public TestTextArea(String name)
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
        TestSuite suite = new TestSuite(TestTextArea.class);
        return suite;
    }

    /**
     *
     * @throws Exception a serious problem
     */
    public void testDisable() throws Exception
    {
        HTMLPage page = new HTMLPage();

        HTMLForm form = new HTMLForm( "/");
        page.addComponent( form);
        HTMLTextArea area = new HTMLTextArea(  "TEXT", 10, 40);
        form.addComponent( area);
        area.setDisabled(true);

        String temp = page.generate();

//        try (FileWriter w = new FileWriter( System.getProperty( "user.home") + "/temp.html" )) {
//            w.write( temp);
//        }

        if( !temp.toUpperCase().contains("DISABLED"))
        {
            LOGGER.info( temp);
            fail( "Should be disabled.");
        }
    }
}
