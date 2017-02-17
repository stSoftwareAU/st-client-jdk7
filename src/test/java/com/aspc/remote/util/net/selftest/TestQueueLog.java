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
package com.aspc.remote.util.net.selftest;
import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.QueueLog;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * check the parsing of the URL
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED test case</i>
 *
 * @author Liam
 * @since 26 Feb 2013
 */
public class TestQueueLog extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.net.selftest.TestQueueLog");//#LOGGER-NOPMD

    /**
     * Test mask credit card
     * @throws Exception a test failure.
     */
    public void testMaskCreditCard() throws Exception
    {
        String visaPattern = "[^0-9]4[0-9]{15}[^0-9]|";
        String masterCardPattern = "[^0-9]5[1-5][0-9]{14}[^0-9]|";
        String americanExpress = "[^0-9]3[47][0-9]{13}[^0-9]|";
        String diners = "[^0-9]3(?:0[0-5]|[68][0-9])[0-9]{11}[^0-9]";

        String pattern = masterCardPattern + visaPattern + americanExpress + diners;

        QueueLog.addPatternMask(pattern,"******",null);

         String value = "UPDATE_CREDIT_CARD_DETAILS BANK_CARD_NUMBER 4444333322221111 BANK_CARD_TYPE 'VISA'";
         String tmp = QueueLog.maskLogMessage(value);
         assertFalse("Should have masked the string:"+value, value.equals(tmp));

         value = "UPDATE_CREDIT_CARD_DETAILS BANK_CARD_NUMBER '4444333322221111' BANK_CARD_TYPE 'VISA'";
         tmp = QueueLog.maskLogMessage(value);
         assertFalse("Should have masked the string:"+value, value.equals(tmp));


         value = "UPDATE_CREDIT_CARD_DETAILS BANK_CARD_NUMBER ***4444333322221111*** BANK_CARD_TYPE 'VISA'";
         tmp = QueueLog.maskLogMessage(value);
         assertFalse("Should have masked the string:"+value, value.equals(tmp));


         value = "UPDATE_CREDIT_CARD_DETAILS BANK_CARD_NUMBER 66644443333222211111 BANK_CARD_TYPE 'VISA'";
         tmp = QueueLog.maskLogMessage(value);
         assertTrue("Should have not masked the string:"+value, value.equals(tmp));


         value = "'4444333322221111'";
         tmp = QueueLog.maskLogMessage(value);
         assertFalse("Should have masked the string:"+value, value.equals(tmp));

         value = "UPDATE_CREDIT_CARD_DETAILS BANK_CARD_NUMBER '5444333322221111' BANK_CARD_TYPE 'MASTER'";
         tmp = QueueLog.maskLogMessage(value);
         assertFalse("Should have masked the string:"+value, value.equals(tmp));

         value = "UPDATE_CREDIT_CARD_DETAILS BANK_CARD_NUMBER '55444333322221111' BANK_CARD_TYPE 'MASTER'";
         tmp = QueueLog.maskLogMessage(value);
         assertTrue("Should have not masked the string:"+value, value.equals(tmp));

         value = "UPDATE_CREDIT_CARD_DETAILS BANK_CARD_NUMBER '40412550157' BANK_CARD_TYPE 'MASTER'";
         tmp = QueueLog.maskLogMessage(value);
         assertTrue("Should have not masked the string:"+value, value.equals(tmp));
    }


     /**
     * Test Server Connect URL
     * @throws Exception a test failure.
     */
    public void testServerConnectURL() throws Exception
    {
         String pattern = "[a-zA-Z0-9._%+-]+:(\\S+)@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}";

         String mask = "****";

         QueueLog.addPatternMask(pattern,mask,1);

         String value = "something else nigel:nigel#123@host.com bbbbb";
         String tmp = QueueLog.maskLogMessage(value);
         assertFalse("Should have masked the string:"+tmp, value.equals(tmp));
         assertTrue("Should be nigel:****@host.com instead of "+tmp,"something else nigel:****@host.com bbbbb".equals(tmp));

         value = "something else 'nigel:nigel123@host.com'";
         tmp = QueueLog.maskLogMessage(value);
         assertFalse("Should have masked the string:"+value, value.equals(tmp));

         value = "nigel:nigel123@host.com/abc";
         tmp = QueueLog.maskLogMessage(value);
         assertFalse("Should have masked the string:"+value, value.equals(tmp));

         value = "nigel@host.com";
         tmp = QueueLog.maskLogMessage(value);
         assertTrue("Should have not masked the string:"+value, value.equals(tmp));


         value = "nigel:**&pass$@host.com/abc";
         tmp = QueueLog.maskLogMessage(value);
         assertFalse("Should have masked the string:"+value, value.equals(tmp));
    }



      /**
     * Test Server Connect URL
     * @throws Exception a test failure.
     */
    public void testDuplicatePatterns() throws Exception
    {
         String pattern1 = "[a-zA-Z0-9._%+-]+:(\\S+)@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}";

         String mask = "****";

        QueueLog.addPatternMask(pattern1,mask,1);

        int size = QueueLog.getLogMaskCount();

        QueueLog.addPatternMask(pattern1,mask,1);
        QueueLog.addPatternMask(pattern1,mask,1);
        QueueLog.addPatternMask(pattern1,mask,1);
        QueueLog.addPatternMask(pattern1,mask,1);
        QueueLog.addPatternMask(pattern1,mask,1);

        int size2 = QueueLog.getLogMaskCount();

        assertTrue("Should be the same size", (size == size2) );

        String visaPattern = "[^0-9](4[0-9]{15})[^0-9]";
        QueueLog.addPatternMask(visaPattern,"******",1);

         String value = "something else nigel:nigel#123@host.com bbbbb liam:liam123@host.com";
         String tmp = QueueLog.maskLogMessage(value);
         assertFalse("Should have masked the string:"+tmp, value.equals(tmp));
         assertTrue("Should be nigel:****@host.com instead of "+tmp,"something else nigel:****@host.com bbbbb liam:****@host.com".equals(tmp));

         value = "something else nigel:nigel#123@host.com bbbbb liam:liam123@host.com VISA 4444333322221111 CVV";
         tmp = QueueLog.maskLogMessage(value);
         assertEquals(
            "Match",
            "something else nigel:****@host.com bbbbb liam:****@host.com VISA******CVV".replace(" ", "").replace("****", "*").replace("**", "*").replace("**", "*").replace("**", "*"), 
            tmp.replace(" ", "").replace("****", "*").replace("**", "*").replace("**", "*").replace("**", "*")
         );

         value = "Should not mask this string *****";
         tmp = QueueLog.maskLogMessage(value);
         assertTrue("Should return the same string ","Should not mask this string *****".equals(tmp));
    }

    /**
     * Creates a new unit test
     * @param name the name of the unit
     */
    public TestQueueLog( final String name )
    {
        super( name );
    }

    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite( TestQueueLog.class );
        return suite;
    }

    /**
     * Entry point to run this test standalone
     * @param args the arguments to the test
     */
    public static void main( String[] args )
    {
        TestRunner.run( suite() );
    }
}
