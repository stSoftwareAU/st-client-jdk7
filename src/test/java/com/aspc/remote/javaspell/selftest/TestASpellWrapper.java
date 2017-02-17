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
package com.aspc.remote.javaspell.selftest;

import com.aspc.remote.javaspell.ASpellException;
import com.aspc.remote.javaspell.ASpellWrapper;
import com.aspc.remote.javaspell.SpellCheckResult;
import com.aspc.remote.util.misc.CLogger;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.commons.logging.Log;

/**
 * Unit test for simple App.
 */
public class TestASpellWrapper  extends TestCase{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.javaspell.selftest.TestASpellWrapper");//#LOGGER-NOPMD

    public void testCheckString() throws IOException, ASpellException{

        ASpellWrapper aSpell = new ASpellWrapper("en");
        List<SpellCheckResult> result = aSpell.checkString("This is eeb test");
        Assert.assertEquals(1, result.size());
        if (result.size() == 1){
            System.out.println(result);
            Assert.assertEquals("eeb", result.get(0).getWord());
            Assert.assertEquals(2, result.get(0).getWordIndex());
            Assert.assertEquals(8, result.get(0).getStartIndex());
        }
    }

    public void testSupported()
    {
        assertTrue( "check supported", ASpellWrapper.isSupported());
    }

    public void testLocale() throws IOException
    {
        Locale[] listLocales = ASpellWrapper.listLocales();

        if( listLocales.length < 1)
        {
            fail( "no supported locales");
        }
        boolean supportsEnglish=false;
        for( Locale l: listLocales)
        {
            LOGGER.info( l);
            if( l.getLanguage().equalsIgnoreCase("en"))
            {
                supportsEnglish=true;
            }
        }
        assertTrue( "has english", supportsEnglish);
    }

    public void testURL() throws IOException, ASpellException{
        String text="\n" +
            "\n" +
            "So you want to build the Enterprise. Don't we all! Well good news: according to some quick, messy, napkin math, it's possible. Kind of. The bad news? It's going to be stupid expensive. But not unfathomably so! Start scrounging up your space-pennies.\n" +
            "One little constraint\n" +
            "Since we can's predict the future, or even come close to gauging the cost of development for revolutionary new inventions or substances like warp and impulse drives, shields, and teleporters, we're going to stick to what we know. It might not make us a real Enterprise, but it's about as close as you're going to get.\n" +
            " \n" +
            "http://gizmodo.com/how-much-would-it-cost-to-build-the-starship-enterprise-506174071\n";

        ASpellWrapper aSpell = new ASpellWrapper("en");
        List<SpellCheckResult> result = aSpell.checkString(text);

        Assert.assertEquals(1, result.size());
        if (result.size() == 1){
            System.out.println(result);
            Assert.assertEquals("teleporters", result.get(0).getWord());
            Assert.assertEquals(75, result.get(0).getWordIndex());
            Assert.assertEquals(179, result.get(0).getStartIndex());
        }
    }

    /**
     * Creates a new unit test
     * @param name the name of the unit
     */
    public TestASpellWrapper( final String name )
{
        super( name );
    }

    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
{
        TestSuite suite = new TestSuite( TestASpellWrapper.class );
        return suite;
    }

    /**
     * Entry point to run this test standalone
     * @param args the arguments to the test
     */
    public static void main( String[] args )
    {
        Test test=suite();
//        test = TestSuite.createTest(TestReSTBuilder.class, "testWrongPassword");
        TestRunner.run( test);

        System.exit(0);
    }
}
