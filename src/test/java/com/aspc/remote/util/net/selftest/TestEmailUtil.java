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
import com.aspc.remote.util.net.*;
import com.aspc.remote.database.InvalidDataException;
import com.aspc.remote.util.misc.CProperties;
import java.util.HashMap;
import javax.mail.MessagingException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * check the email utilities
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED test case</i>
 *
 * @author luke
 * @since July 29, 2013
 */
public class TestEmailUtil extends TestCase
{
    /**
     * Creates a new unit test
     * @param name the name of the unit
     */
    public TestEmailUtil( final String name )
{
        super( name );
    }

    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
{
        TestSuite suite = new TestSuite( TestEmailUtil.class );
        return suite;
    }

    /**
     * Entry point to run this test standalone
     * @param args the arguments to the test
     */
    public static void main( String[] args )
    {
        Test test=suite();
//        test=TestSuite.createTest(TestEmailUtil.class , "testValidateSMTP");
        TestRunner.run( test );

        LOGGER.info( "TestEmailUtil completed." );
        System.exit(0);
    }

    public void testValidateSMTP() throws Exception
    {
        String host=CProperties.getProperty("mail.smtp.host");
        if( host == null) return;
        String user=CProperties.getProperty("mail.smtp.user");
        String pw=CProperties.getProperty("mail.smtp.password");
        int port =Integer.parseInt(CProperties.getProperty("mail.smtp.port","0"));
        
        EmailUtil.ConnectionSecurity security=EmailUtil.ConnectionSecurity.NONE;
        
        if( "true".equals(CProperties.getProperty("mail.smtp.starttls.enable")))
        {
            security=EmailUtil.ConnectionSecurity.STARTTLS;
        }
        else if( "true".equals(CProperties.getProperty("mail.smtp.ssl.enable")))
        {
            security=EmailUtil.ConnectionSecurity.SSL_TLS;
        }
            
        EmailUtil.validateSMTP(host, port, user, pw, security);
    }
    
    public void testInvalidateSMTP() throws Exception
    {
        String host="smtp.stspftware.com.au";
        String user="hacker";
        String pw="xxx";

        for( EmailUtil.ConnectionSecurity security: EmailUtil.ConnectionSecurity.values() )
        {            
            String name=host + " " + security;
            try
            {
                EmailUtil.validateSMTP(host, 0, user, pw, security);
                fail( "must not valid " + name);
            }
            catch( MessagingException me)
            {
                LOGGER.info(name, me);
            }
        }
    }

    /**
     * Check known good emails
     * @throws Exception a test failure.
     */
    public void testKnownGood() throws Exception
    {
        String emails[]={
            "test@sensory7.com",
            "test@acuitas.com",
//            "test@barbador.com",
            "test@tagfinancial.sydney",
            "nick.van.pmd@hotmail.com",
            "bob@optusnet.com.au",
            "niceandsimple@example.com",
            "very.common@example.com",
            "a.little.lengthy.but.fine@dept.stsoftware.com",
            "disposable.style.email.with+symbol@example.com",

            /**
             * Valid but stupid email addresses.
             */
            //"user@[IPv6:2001:db8:1ff::a0b:dbd0]",
            //"\"much.more unusual\"@example.com",
            //"\"very.unusual.@.unusual.com\"@example.com",
            //"\"very.(),:;<>[]\\\".VERY.\\\"very@\\\\ \\\"very\\\".unusual\"@strange.example.com",
            //"!#$%&'*+-/=?^_`{}|~@example.org",
            //"\"()<>[]:,;@\\\\\\\"!#$%&'*+-/=?^_`{}| ~.a\"@example.org",
            //"\" \"@example.org",
            "AJBristow&Sons@AJBristowAndSons.com.au",
            "mchettle@bigpond.net.au",
            "ianm@igm.com.au",
            "support@stSoftware.com",
            "ganeshkarki1@inditimes.com",
            "bijan-ahmadian@hotmail.com " // tollerate trailing spaces
        };

        HashMap cache=new HashMap();
        for( String email: emails)
        {
            try
            {
                EmailUtil.validate(email, cache);
            }
            catch( InvalidDataException ide)
            {
                EmailUtil.validate(email, null);
                fail( email + " caused " + ide.getMessage());
            }
        }
    }

    /**
     * Check known good emails
     * @throws Exception a test failure.
     */
    public void testKnownBad() throws Exception
    {
        String emails[]={
            "sales@aspconverters.com.au",
            "Abc.example.com",
            "A@b@c@example.com",
            "a\"b(c)d,e:f;g<h>i[j\\k]l@example.com",
            "just\"not\"right@example.com",
            "this is\"not\\allowed@example.com",
            "this\\ still\\\"not\\\\allowed@example.com",
            "jessica@promotechnics.com.au",
            "contact@promotechnics.com.au",
            "pdummer@wallington-dumer.com",
            "nigel@s16161616161sh.com",
            "DONOTPROMO admin@bchq.com.au"
        };

        HashMap cache=new HashMap();
        for( String email: emails)
        {
            try
            {
                EmailUtil.validate(email, cache);
                fail( "invalid email should have failed " + email);
            }
            catch( InvalidDataException ide)
            {
                LOGGER.info( "known bad email " + email);
            }
        }
    }



    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.net.selftest.TestEmailUtil");//#LOGGER-NOPMD
}
