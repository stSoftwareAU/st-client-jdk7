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
package com.aspc.remote.util.misc.selftest;

import com.aspc.remote.util.misc.CheckDigit;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * TestCheckDigit
 * 
 * @author Aloysius George
 * @since November 16, 2006, 8:51 PM
 */  

public class TestCheckDigit extends TestCase
{    
    /**
     * Creates new TestCheckDigit TestUnit
     * @param name The name of the test unit
     */
    public TestCheckDigit(String name)
    {
        super( name);
    }

    /**
     * Main method
     * @param args commandline args
     */
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }

    /**
     * 
     * @return the value
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestCheckDigit.class);
        return suite;
    }
    
    
    /**
     * tests the checkDigit number calculation
     * @throws Exception A serious problem
     */
    public void testCalculateCheckDigit() throws Exception
    {
        
        String num = "139";        
        assertEquals( "Check digit should be 6",6,CheckDigit.calculateCheckDigit( num));
               
        num = "8296";
        assertEquals( "Check digit should be 6",6,CheckDigit.calculateCheckDigit( num));
        
        num=  "0102 123456789 0204 046599";
        assertEquals( "Check digit",9,CheckDigit.calculateCheckDigit( num));
        
        num=  "4 9 9 8 6 5 5 5 9"; 
        assertEquals( "Check digit",3,CheckDigit.calculateCheckDigit( num));
        
        num = "96438";
        assertEquals( "Check digit should be 7",7,CheckDigit.calculateCheckDigit( num));

    }
    
    /**
     * tests verification of check digit in a number
     * @throws Exception a test failure
     */
    public void testValidateCheckDigit() throws Exception
    {
        String num = "1 2 3 4 - 5 6 7 8 - 9 0 1 2 - 3 4 5 2";        
        assertEquals( "should be valid",true,CheckDigit.validateCheckDigit( num ) );
    }
    
    /**
     * tests stripping non-digit characters from a String
     * @throws Exception a test failure
     */
    public void testStripNonDigit() throws Exception
    {
        String s = " 1 2 3 4  5 6   abc";
        assertEquals( "should have stripped non-digits", "123456", CheckDigit.stripNonDigit( s ) );
    }
}

