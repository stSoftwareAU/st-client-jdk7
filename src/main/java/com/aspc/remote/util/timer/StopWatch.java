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

import com.aspc.remote.util.misc.ConcurrentDecimalFormat;
import com.aspc.remote.util.misc.TimeUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 *  Stopwatch
 *
 *  <br>
 *  <i>THREAD MODE: Single thread</i>
 *
 *  @author      Nigel Leck
 *  @since       3 June 2015
 */
public final class StopWatch
{
    private final ArrayList<Lap> laps=new ArrayList();
    private final long nanoCreationTime=System.nanoTime();
    private Lap lap;
    private static final ConcurrentDecimalFormat NF=new ConcurrentDecimalFormat("#,##0");
//    private static final char ASCII_SIZES[];
//    private static final char OLD_SIZES[]={
//        '.',
//        ',',
//        ':',
//        ';',
//        '|',
//        '%',
//        '&',
//        '$',
//        '#',
//        '@'
//    };

    private static final char ASCII_SIZES[]={
        ' ',      // 0. 
        '.',      // 1. 
        ',',      // 2. 
        ':',      // 3. 
        'x', // 4. low asterisk
        'X', // 5. two asterisk
        '@', // 6.
        '\u2042', // 7. three asterisk
        //'/', // 4. 
//        '%', // 5. five dot punctuation
//        '#', // 6. Reference mark
        
        '\u2591', // 8. 25% light shade
        '\u2592', // 9. 50% medium shade
        '\u2593', // 10. 75% dark shade
        //'\u2588', // 10. full block
        
        //'\u2024', // one dot leader
        //'\u2025', // two dot leader
        //'\u2026', // three dot leader
//        '\u2581', 
//        '\u2582',
//        '\u2583',
//        '\u2584',
//        '\u2585',
//        '\u2586',
//        '\u2587',
//        '\u2588',
    };

    
    public StopWatch()
    {
        this( false);
    }
    
    public StopWatch(final boolean autoStart)
    {
        if(autoStart)
        {
            start();
        }
    }
    
    /**
     * The date that this stopwatch was created. 
     * @return the date of creation
     */
    public Date creationDate()
    {
        long now=System.currentTimeMillis();
        return new Date( now-durationMS());
    }
    public long duration()
    {
        long nanoEnd=System.nanoTime();
        return nanoEnd-nanoCreationTime;
    }
    public long durationMS()
    {
        return duration()/1000/1000;
    }
    
    public void test()
    {
        long nanoEnd=System.nanoTime();

        for( Lap l: laps)
        {
            long s = l.start();
            if( s > nanoEnd) 
            {
                throw new IllegalArgumentException( "lap start greater than now");
            }
            if( s < nanoCreationTime) 
            {
                throw new IllegalArgumentException( "lap start greater than stop watch start");
            }
            long e = l.end();
            if( e > nanoEnd) 
            {
                throw new IllegalArgumentException( "lap end greater than now");
            }
            if( e < nanoCreationTime) 
            {
                throw new IllegalArgumentException( "lap end greater than stop watch start");
            }
            
            if( e<s)
            {
                 throw new IllegalArgumentException( "lap end before start");
            }
        }
    }
    public long median()
    {
        long times[]=new long[laps.size()];
        for( int pos=0;pos< times.length;pos++)
        {
            times[pos]=laps.get(pos).duration();
        }
                
        Arrays.sort(times);
        int middle = times.length/2;
        if(times.length%2 == 1) {
            return times[middle];
        } else {
            return (times[middle-1] + times[middle]) / 2;
        }
    }
        
    /**
     * The maximum lap duration in nanoseconds.
     * @return the maximum nanoseconds
     */
    @CheckReturnValue @Nonnegative
    public long max()
    {
        long max=-1;
        for( Lap l: laps)
        {
            long duration = l.duration();
            if( duration>max) max=duration;
        }
        
        return max;
    }
        
    /**
     * The average lap duration in nanoseconds.
     * @return the average nanoseconds
     */
    @CheckReturnValue @Nonnegative
    public long avg()
    {
        int c=count();
        if( c == 0) return -1;
        return total()/c;
    }
        
    public long total()
    {
        long total=0;
        for( Lap tmpLap: laps)
        {
            total+=tmpLap.duration();
        }
        
        return total;
    }   
    
    /**
     * The minimum lap duration in nanoseconds.
     * @return the minimum nanoseconds
     */
    @CheckReturnValue @Nonnegative
    public long min()
    {
        long min=-1;
        for( Lap tmpLap: laps)
        {
            long duration = tmpLap.duration();
            if( duration<min||min==-1) min=duration;
        }
        
        return min;
    }
    
    public int count()
    {
        return laps.size();
    }

    public long start()
    {
        if( lap != null)
        {
            stop();
        }
        
        lap=new Lap( );
        return lap.start();
    }    
     
    public long stop()
    {
        long endNs=-1;
        if( lap != null)
        {
            endNs=lap.end();

            laps.add(lap);
            lap=null; 
        }       
        
        return endNs;
    }
    private static final long NS_FORMAT_LIMIT=5L*1000L;
    private static final long US_FORMAT_LIMIT=5L*1000L*1000L;
    
    public String formatNano( final long nano)
    {
        if( nano < 0) throw new IllegalArgumentException("nano must be positive was " + nano);
        if( nano == 0) return "0 ms";
        
        if( nano < NS_FORMAT_LIMIT)
        {
            return NF.format(nano) + " ns";
        }
        else if( nano < US_FORMAT_LIMIT)
        {
            return NF.format(Math.round((double)nano/1000.0)) + " " + (char)0x00B5 +"s";
        }
        else
        {
            return TimeUtil.getDiff(0, Math.round((double)nano/1000.0/1000.0));            
        }
    }

    public String asciiArtTimeline()
    {
        int spots[]=new int[100];
        
        long nanoEnd=System.nanoTime();
        
        long nanoPeriod=nanoEnd-nanoCreationTime;
        int total = count();
        for(Lap l: laps)
        {
            long start=l.start();
            long end=l.end();
            
            long startOffset=start-nanoCreationTime;
            long endOffset=end-nanoCreationTime;
            
            int startPos = (int)Math.round( (double)startOffset*100.0/(double)nanoPeriod);
            if( startPos >= spots.length) startPos=spots.length -1;
            int endPos = (int)Math.round((double)endOffset*100.0/(double)nanoPeriod);
            if( endPos >= spots.length) endPos=spots.length -1;
            
            spots[startPos]++;
            for( int pos = startPos +1; pos< endPos;pos++)
            {
                spots[pos]++;
            }
        }
        
        StringBuilder art=new StringBuilder();
        for( int count: spots)
        {
            if( count > 0)
            {
                int percent = Math.round((float)(count* 100 + 4)/(float)total);
                int pos=Math.round((float)percent/(float)10);
                pos+=1; // (don't show blanks)
                if( pos >= ASCII_SIZES.length)
                {
                    pos=ASCII_SIZES.length-1;
                }
                art.append(ASCII_SIZES[pos]);
            }
            else
            {
                art.append(" ");
            }
        }
        return art.toString();
    }
    
    public String asciiArt()
    {
        int size =laps.size();
        long times[]=new long[100];

        double expander=(double)times.length/(double)size;

        for( int pos=0;pos< 100;pos++)
        {
            long duration=0;
            int lPos=(int)(pos/expander);
            if( lPos < laps.size())
            {
                Lap tmpLap=laps.get(lPos);
                duration=tmpLap.duration();
            }
            if( times[pos]< duration)
            {
                times[pos]=duration;
            }
        } 

        long max=max();
        long min=min();
        long totalHeight=max-min;
        
        StringBuilder art=new StringBuilder();

        for( long duration: times)
        {
            int percent=0;
            if( totalHeight>0)
            {
                long height=duration-min;

                percent=Math.round((float)(height*100 + 4)/(float)totalHeight);
            }

            int pos=Math.round((float)percent/(float)10);
            if( pos > ASCII_SIZES.length)
            {
                assert false: "more than 100% " + pos;
                pos=ASCII_SIZES.length-1;
            }
            art.append(ASCII_SIZES[pos]);
        }
        
        return art.toString();
    }
    
    public String summary( final String title)
    {
        String tmpTitle=title;
        if( tmpTitle == null)
        {
            tmpTitle="";
        }
        else 
        {
            if( tmpTitle.equals( " ") == false && tmpTitle.endsWith(":") == false)
            {
                tmpTitle=tmpTitle.trim() + " ";
            }
        }
            
        String maxString="n/a";
        String minString="n/a";
        String avgString="n/a";
        String medString="n/a";
        String maxTitle="Max";
        String minTitle="Min";
        if( laps.size()>0)
        {
            long max=max();
            long min=min();
            maxString=formatNano(max);
            minString=formatNano( min);
            avgString=formatNano(avg());
            medString=formatNano(median());
            
            Lap first=laps.get(0);
            Lap last=laps.get(laps.size()-1);
            
            if( max == first.duration())
            {
                maxTitle=(char)0x21E4 + "Max";
            }
            if( min == last.duration())
            {
                minTitle="Min" + (char)0x21E5;
            }
        }
        
        String details = maxTitle + ": " + maxString + ", " + minTitle + ": " + minString + ", Avg: " + avgString + ", Median: " + medString + ", Count: " + NF.format(laps.size());
        
        String full= tmpTitle + details;
        
        return full;
    }
    
    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        return summary( "");
    }
    
//    static
//    {
//        ASCII_SIZES=NEW_ASCII_SIZES;
//    }
//        String osName=System.getProperty("os.name");
//        if( true || osName.equalsIgnoreCase("Windows"))
//        {
//            ASCII_SIZES=OLD_ASCII_SIZES;
//        }
//        else
//        {
//            ASCII_SIZES=NEW_ASCII_SIZES;
//        }
//    }
}
