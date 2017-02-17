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

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 * @author  stephen
 * @since 29 September 2006
 */

public final class NumUtil
{
    private static final ConcurrentDecimalFormat NUMBER_FORMAT = new ConcurrentDecimalFormat( "#,##0.#");

    private NumUtil()
    {
        
    }
    private static final String[] MAJOR_NAMES = {
        "",
        " thousand",
        " million",
        " billion",
        " trillion",
        " quadrillion",
        " quintillion"
    };
    
    private static final String[] TENS_NAMES = {
        "",
        " ten",
        " twenty",
        " thirty",
        " fourty",
        " fifty",
        " sixty",
        " seventy",
        " eighty",
        " ninety"
    };
    
    private static final String[] NUM_NAMES = {
        "",
        " one",
        " two",
        " three",
        " four",
        " five",
        " six",
        " seven",
        " eight",
        " nine",
        " ten",
        " eleven",
        " twelve",
        " thirteen",
        " fourteen",
        " fifteen",
        " sixteen",
        " seventeen",
        " eighteen",
        " nineteen"
    };
    
    private static String convertLessThanOneThousand(int number)//NOPMD
    {
        String soFar;
        
        if (number % 100 < 20)
        {
            soFar = NUM_NAMES[number % 100];
            number /= 100;
        }
        else
        {
            soFar = NUM_NAMES[number % 10];
            number /= 10;
            
            soFar = TENS_NAMES[number % 10] + soFar;
            number /= 10;
        }
        if (number == 0) return soFar;
        return NUM_NAMES[number] + " hundred" + soFar;
    }
    
    /**
     * 
     * @param numDollarCents 
     * @return the value
     */
    public static String convertToDollarAndCents(double numDollarCents)
    {
        int dollars = (int)Math.floor(numDollarCents);
        int cent = (int)Math.floor(( numDollarCents - dollars)* 100.0f);
        
        String dw;
        if( dollars == 1)
        {
            dw = "dollar";
        }
        else
        {
            dw = "dollars";
        }
        
        String cw;
        
        if (cent==1)
        {
            
            cw = "cent";
        }
        
        else
        {
            
            cw ="cents";
        }
        
        String s = convertToWords( dollars ) + " " + dw + " and " + convertToWords( cent ) + " " + cw;
        
        return s;
    }
    
    /**
     * Displays a number in terms of kb, mb etc.
     *
     * @param size The amount of memory
     * @return The display amount
     */
    @Nonnull @CheckReturnValue
    public static String convertMemoryToHumanReadable( final long size)
    {
        final long gig   = 1024 * 1024 * 1024,
                   meg   = 1024 * 1024,
                   k   = 1024;
        final long tillion=1024 * gig;
        long aSize = Math.abs( size);

        if( aSize > tillion * 5 )
        {
            return NUMBER_FORMAT.format( (double)size/(double)tillion) + "t";
        }
        else if( aSize > gig * 5 )
        {
            return NUMBER_FORMAT.format( (double)size/(double)gig) + "g";
        }
        else if( aSize > meg * 5 )
        {
            return NUMBER_FORMAT.format( (double)size/(double)meg) + "m";
        }
        else if( aSize > k * 5)
        {
            return NUMBER_FORMAT.format( (double)size/(double)k) + "k";
        }

        return size + "b";
    }
    
    /**
     * 
     * @param number 
     * @return the value
     */
    public static String convertToWords(double number)
    {
        int left = (int)Math.floor(number);
        int right = (int)Math.floor(( number - left)* 100.0f);
        
        String s = convertToWords( left ) + " point " + convertToWords( right );
        
        return s;
    }
    
    /**
     * 
     * @param number 
     * @return the value
     */
    public static String convertToWords(int number)//NOPMD
    {
        /* special case */
        if (number == 0)
        { return "zero"; }
        
        String prefix = "";
        
        if (number < 0)
        {
            number = -number;
            prefix = "negative";
        }
        
        String soFar = "";
        int place = 0;
        
        do
        {
            int n = number % 1000;
            if (n != 0)
            {
                String s = convertLessThanOneThousand(n);
                soFar = s + MAJOR_NAMES[place] + soFar;
            }
            place++;
            number /= 1000;
        } while (number > 0);
        
        return (prefix + soFar).trim();
    }
    
}
