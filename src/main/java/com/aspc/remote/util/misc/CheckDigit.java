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
package com.aspc.remote.util.misc;

/**
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 * TODO: Think this may have been better as a singeton or a static method of StringUtilities. 
 * @author Aloysius George
 * @since November 14, 2006, 1:04 AM
 */         
public final class CheckDigit
{
    
   
    /**
     * calculates and returns a single digit value for a number according based on Modulus 10 principle
     * @param value string
     * @return The contactNumber is not a valid number
     */
    public static int calculateCheckDigit(final String value)
    {
        String contNum = value.replaceAll(" ","");
        contNum = contNum.trim();
        
        int sum = 0;
        int numDigits = contNum.length();

        for(int i=0; i<numDigits ; i++)
        {
            int pos = numDigits - 1 - i;
            int digit = Character.getNumericValue(contNum.charAt(pos));
            if( digit >= 0)
            {
                int weight;
                if(i % 2 == 0)
                {
                    weight = (2 * digit) - (int) (digit / 5) * 9;
                }
                else
                {
                    weight = digit;
                }
                sum = sum + weight;
            }
        }
        sum = Math.abs(sum) + 10;
 
        return (10 - (sum % 10)) % 10;
    }
    
    /**
     * validates a check sum digit
     * @param value the value to verify
     * @return the value
     */
    public static boolean validateCheckDigit( final String value )
    {
        String numbers = stripNonDigit( value );
        
        int cut = numbers.length() - 1; // the last character
        int check = Character.getNumericValue( numbers.charAt( cut ) );
        numbers = numbers.substring( 0, cut );
        
        return ( check == calculateCheckDigit( numbers ) );
    }
    
    /**
     * strips out any characters that are not digits
     * @param value the string to collapse
     * @return the value
     */
    public static String stripNonDigit( final String value )
    {
        char[] buf = new char[ value.length() ];
        
        int pos = 0;
        for( int i = 0; i < value.length(); i++ )
        {
            if( Character.isDigit( value.charAt( i ) ) )
            {
                buf[ pos++ ] = value.charAt( i );
            }
        }
        
        return new String( buf, 0, pos );
    }
       
   /** Creates a new instance of CheckDigit */
    private CheckDigit()
    {
    }
}

