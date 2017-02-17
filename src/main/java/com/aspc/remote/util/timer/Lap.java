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
package com.aspc.remote.util.timer;

/**
 *  Stopwatch
 *
 *  <br>
 *  <i>THREAD MODE: Single thread</i>
 *
 *  @author      Nigel Leck
 *  @since       3 June 2015
 */
public final class Lap
{
    private final long nanoStart=System.nanoTime();
    private long nanoEnd;
    
    public Lap()
    {
        
    }
    
    /**
     * When was the lap started ? 
     * @return the nano time. 
     */
    public long start()
    {
        return nanoStart;
    }
    
    /**
     * When was the lap ended ? 
     * @return the end time. 
     */    
    public long end()
    {
        if( nanoEnd ==0)
        {
            nanoEnd=System.nanoTime();
        }
        return nanoEnd;
    }

    /**
     * The duration. 
     * @return the difference.
     */
    public long duration()
    {
        long tmpEnd=nanoEnd;
        if( tmpEnd < nanoStart)
        {
            tmpEnd=System.nanoTime();
        }
//        assert nanoStart != 0 && nanoEnd != 0: "lap not stopped " + nanoStart + ", " + nanoEnd;
        
        return tmpEnd-nanoStart;
    }   
    
    /**
     * The duration in MILLISECONDS.
     * @return the difference.
     */
    public long durationMS()
    {        
        return duration()/1000/1000;
    }  
}
