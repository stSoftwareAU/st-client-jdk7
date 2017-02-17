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

import com.aspc.remote.database.InvalidDataException;
import com.aspc.remote.memory.HashMapFactory;
import static com.aspc.remote.util.misc.DateUtil.GMT_ZONE;
import java.io.StreamTokenizer;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.annotation.CheckReturnValue;
import javax.annotation.*;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;

/**
 *  TimeUtil handles timestamps
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Jason McGrath
 *  @since       May 17, 2001
 */
public final class TimeUtil
{
    public static final String REST_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    /**
     *
     */
    public static final String END_OF_DAY = "END_OF_DAY";

    /** the current timestamp */
    public static final String CURRENT_TIMESTAMP="CURRENT_TIMESTAMP";

    /**
     *
     */
    public static final String START_OF_DAY = "START_OF_DAY";
    /**
     *
     */
    public static final String MARKET_CLOSE = "MARKET_CLOSE";
    
    public static final String DEFAULT_TIME_FORMAT = "d MMM yyyy HH:mm:ss";
    public static final String DEFAULT_DATE_FORMAT = "d MMM yyyy";
    
    @CheckReturnValue @Nonnull
    public static GregorianCalendar getGC( final @Nullable TimeZone timeZone)
    {
        TimeZone tz = timeZone;

        if( tz == null) tz = GMT_ZONE;

        GregorianCalendar gc = new GregorianCalendar( tz);

        return gc;
    }
    
    /**
     * Returns the difference in the start dates and now as xx Hrs xx Min xx Secs.
     * @param start The start time
     * @return A user readable time difference
     */
    @CheckReturnValue @Nonnull
    public static String getDiff(final @Nonnull Date start)
    {
        return getDiff( start, new Date());
    }

    /**
     * Returns the difference in the two dates as xx Hrs xx Min xx Secs.
     * @param start The start time
     * @param end The end time
     * @return A user readable time difference
     */
    @CheckReturnValue @Nonnull
    public static String getDiff( final @Nonnull Date start, final @Nullable Date end)
    {
        return getDiff(start, end, null);
    }

    /**
     * Returns the difference in the two dates as xx Hrs xx Min xx Secs.
     * @param start The start time
     * @param end The end time
     * @param pars the parameters
     * @return A user readable time difference
     */
    @CheckReturnValue @Nonnull
    public static String getDiff( final Date start, final Date end, final TimePars pars)
    {
        long startMS;

        if( start != null)
        {
            startMS = start.getTime();
        }
        else
        {
            return getDiff( System.currentTimeMillis());
        }

        if( end == null)
        {
            return getDiff( startMS);
        }

        long endMS = end.getTime();

        return getDiff( startMS, endMS, pars);
    }

    /**
     * Returns the difference in the two dates as xx Hrs xx Min xx Secs.
     * @param start The start time
     * @return A user readable time difference
     */
    @CheckReturnValue @Nonnull
    public static String getDiff( final long start)
    {
        return getDiff( start, System.currentTimeMillis());
    }

    /**
     * Returns the difference in the two dates as xx Hrs xx Min xx Secs.
     * @param start The start time
     * @param end The end time
     * @return A user readable time difference
     */
    @CheckReturnValue @Nonnull
    public static String getDiff( long start, long end)
    {
        return getDiff( start, end, null);
    }

    /**
     * Returns the difference in the two dates as xx Hrs xx Min xx Secs.
     * @param start The start time
     * @param end The end time
     * @param pars the parameters
     * @return A user readable time difference
     */
    @CheckReturnValue @Nonnull
    public static String getDiff( final long start, final long end, final TimePars pars)
    {
        boolean longFormat = pars != null && pars.longformat;

        String td = "";
        long ms;
        long secs;

        ms = end - start;

        if( ms < 5000)
        {
            return ms + " ms";
        }
        secs = Math.round( (double)ms/1000.0);

        int w, d, h, m, s;
        w = (int) secs/60/60/24/7;
        d = (int) secs/60/60/24 % 7;
        h = (int) secs/60/60 % 24;
        m = (int) secs/60 % 60;
        s = (int) secs % 60;


        if( w != 0)
        {
            if( w > 1)
            {
                td += w + " Weeks ";
            }
            else
            {
                td += w + " Week ";
            }
        }

        if( d != 0)
        {
            if( d > 1)
            {
                td += d + " Days ";
            }
            else
            {
                td += d + " Day ";
            }
        }

        if( h != 0)
        {
            if( h > 1)
            {
                if(longFormat)
                {
                    td += h + " Hours ";
                }
                else
                {
                    td += h + " Hrs ";
                }
            }
            else
            {
                if(longFormat)
                {
                    td += h + " Hour ";
                }
                else
                {
                    td += h + " Hr ";
                }
            }
        }

        if( h != 0 || m != 0)
        {
            if(longFormat)
            {
                td += m + " Minutes ";
            }
            else
            {
                td += m + " Min ";
            }
        }

        if( d == 0 && w == 0 && h == 0)
        {
            if( s != 0)
            {
                if(longFormat)
                {
                    td += s + " Seconds";
                }
                else
                {
                    td += s + " Secs";
                }
            }
        }

        return td.trim();
    }

    // Constructors
    private TimeUtil()
    {
    }

    /**
     *
     * @param userDateString
     * @param userTimeZone
     * @param defaultToEndOfDay
     * @param parseMilliseconds default true, dateString can contain milliseconds
     * @throws com.aspc.remote.database.InvalidDataException
     * @return the value
     */
    @SuppressWarnings("AssignmentToForLoopParameter")
    @CheckReturnValue @Nonnull
    public static Date parseUserTime(
        final @Nonnull String    userDateString,
        final @Nullable TimeZone  userTimeZone,
        final boolean   defaultToEndOfDay,
        final boolean   parseMilliseconds
    ) throws InvalidDataException
    {
        TimeZone tz = userTimeZone;
        String dateString = userDateString.trim().toUpperCase();

        if(parseMilliseconds && dateString.matches("(-|\\+|)[0-9]+"))
        {
            try
            {
                long msecs;

                msecs = Long.parseLong( dateString);

                return new Date( msecs);
            }
            catch( NumberFormatException nf)
            {
                // This is ok
            }
        }
        StringBuilder format = null;

        // Get the timezone
        String tempDateString = dateString;
        int pos = tempDateString.indexOf( "@");
        if( pos != -1)
        {
            // TimeZone is case sensitive ( don't uppercase)
            String tzStr = userDateString.substring( userDateString.indexOf("@") + 1);
            tzStr = StringUtilities.replace( tzStr, " ", "");

            tz = TimeZone.getTimeZone( tzStr);
            tempDateString = tempDateString.substring( 0, pos).trim();
        }
        else if( dateString.endsWith( "GMT"))
        {
            tz = DateUtil.GMT_ZONE;
            tempDateString = tempDateString.substring(0, tempDateString.length() - 3).trim();
        }
        else if( dateString.endsWith( "Z"))
        {
            if( dateString.matches("[0-9]{8}T[0-9]{6}Z"))
            {
                tz=DateUtil.GMT_ZONE;
                format=new StringBuilder("yyyyMMdd'T'HHmmss'Z'");
            }
            else
            {
                tz = DateUtil.GMT_ZONE;
                tempDateString = tempDateString.substring(0, tempDateString.length() - 1).trim();
            }
        }

        tempDateString = tempDateString.replace( ",", " ");
        
        while( tempDateString.contains("  "))
        {
            tempDateString = StringUtilities.replace( tempDateString, "  ", " ");
        }

        if(tempDateString.equals(DateUtil.TYPE_TODAY))
        {
            return DateUtil.getToday( tz);
        }
        else if( dateString.equals(DateUtil.TYPE_NOW) || dateString.startsWith(CURRENT_TIMESTAMP) )
        {
            return new Date( );
        }
        else if( dateString.equals(DateUtil.TYPE_TOMORROW))
        {
            return DateUtil.getTommorrow( tz);
        }
        else if( dateString.equals(DateUtil.TYPE_YESTERDAY))
        {
            return DateUtil.getYesterday( tz);
        }

        // Try 1500 or 150000
        int len = tempDateString.length();
        if( len == 4 || len == 6)
        {
            try
            {
                Long.parseLong( tempDateString);

                tempDateString = format( "d-MMM-yyyy ", null, tz) + tempDateString;
            }
            catch( NumberFormatException nf)
            {
                // This is ok
            }
        }

        //
        if( len == 8 && tempDateString.charAt(2) == ':' &&  tempDateString.charAt(5) == ':')
        {
            tempDateString = format( "d-MMM-yyyy ", null, tz) + tempDateString;
        }

        // Try 15:43 HH:mm
        if( len == 5 && tempDateString.charAt(2) == ':')
        {
            tempDateString = format( "d-MMM-yyyy ", null, tz) + tempDateString;
        }


        if(len >= 10 && format==null)
        {
            if(
                tempDateString.startsWith("JAN")||
                tempDateString.startsWith("FEB")||
                tempDateString.startsWith("MAR")||
                tempDateString.startsWith("APR")||
                tempDateString.startsWith("MAY")||
                tempDateString.startsWith("JUN")||
                tempDateString.startsWith("JUL")||
                tempDateString.startsWith("AUG")||
                tempDateString.startsWith("SEP")||
                tempDateString.startsWith("OCT")||
                tempDateString.startsWith("NOV")||
                tempDateString.startsWith("DEC")
            )
            {
                //more check to fix the date string to this format "MMM dd, yyyy hh:mm:ss""
                if(len == 10)
                {
                    format = new StringBuilder( "MMM d yyyy");
                }
                else if(len == 11)
                {
                    format = new StringBuilder( "MMM dd yyyy");
                }
                else if(len >= 18)
                {
                    if(
                        tempDateString.charAt(5) == ' ' ||
                        tempDateString.charAt(5) == ',')
                    {
                        tempDateString = tempDateString.substring(0,4)+ "0"+ tempDateString.substring(4);
                    }


                    if(tempDateString.charAt(6) == ',')
                    {
                        if(tempDateString.charAt(14) == ':')
                        {
                            tempDateString = tempDateString.substring(0,12)+ "0"+ tempDateString.substring(12);
                        }
                    }
                     else if(tempDateString.charAt(13) == ':')
                    {
                         tempDateString = tempDateString.substring(0,12)+ "0"+ tempDateString.substring(12);
                    }

                    len = tempDateString.length();

                    if(
                        tempDateString.endsWith("AM") ||
                        tempDateString.endsWith("PM")
                    )
                    {
                         /*   "Jan 31 2002 12:00:00:000AM",
                        "Jan 31, 2002 12:00:00:000AM",
                        "Jan 31 2002 12:00:00:000 AM",
                        "Jan 31, 2002 12:00:00:000 AM",
                        "Jan 31 2002 12:00:00 AM",
                        "Jan 31, 2002 12:00:00 AM",
                        "Jan 31 2002 12:00:00AM",
                        "Jan 31, 2002 12:00:00AM" */

                        String noon = "AM";

                        if( tempDateString.endsWith("PM"))
                        {
                            noon = "PM";
                        }

                        if(len >26 || len >21)
                        {
                            if(tempDateString.charAt(6) == ',')
                            {
                                tempDateString = tempDateString.substring(0,21);
                            }
                            else
                            {
                                tempDateString = tempDateString.substring(0,20);
                            }

                            tempDateString +=" "+noon;
                        }

                        len = tempDateString.length();
                        if( len > 23 )
                        {
                            format = new StringBuilder( "MMM dd, yyyy hh:mm:ss a");
                        }
                        else
                        {
                             format = new StringBuilder("MMM dd yyyy hh:mm:ss a");
                        }
                    }
                    else
                    {
                        /* "Jan 31 2002 12:00:00",
                            "Jan 31, 2002 12:00:00",
                            "Jan 31 2002 12:00:00:000",
                            "Jan 31, 2002 12:00:00:000" */

                        if(len >21)
                        {
                            if(tempDateString.charAt(6) == ',')
                            {
                                tempDateString = tempDateString.substring(0,21);
                            }
                            else
                            {
                                tempDateString = tempDateString.substring(0,20);
                            }
                        }

                        len = tempDateString.length();

                        if( len > 20 )
                        {
                            format = new StringBuilder( "MMM dd, yyyy HH:mm:ss");
                        }
                        else
                        {
                             format = new StringBuilder("MMM dd yyyy HH:mm:ss");
                        }
                    }
                }
            }
            else if( len > 22)
            {
                /**
                 * THU APR 07 01:56:00 EST 1994
                 * THU APR 6 15:56:00 1994
                 */
                if(
                    tempDateString.startsWith( "MON") ||
                    tempDateString.startsWith( "TUE") ||
                    tempDateString.startsWith( "WED") ||
                    tempDateString.startsWith( "THU") ||
                    tempDateString.startsWith( "FRI") ||
                    tempDateString.startsWith( "SAT") ||
                    tempDateString.startsWith( "SUN")
                )
                {
                    tempDateString = tempDateString.substring( 3).trim();
                    if( tempDateString.matches("[A-Z]{3} [0-9]+ [0-9]+:[0-9]+:[0-9]+ .*"))
                    if( len > 26)
                    {
                        format = new StringBuilder( "MMM dd HH:mm:ss z yyyy");
                    }
                    else
                    {
                        format = new StringBuilder( "MMM dd HH:mm:ss yyyy");
                    }
                }
            }
            if( format == null)
            {
                if( tempDateString.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]T[0-9][0-9]:[0-9][0-9]:[0-9][0-9]"))
                {
                    format=new StringBuilder("yyyy-MM-dd'T'HH:mm:ss");
                }
                else if( tempDateString.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]T[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\\.[0-9][0-9][0-9]"))
                {
                    format=new StringBuilder("yyyy-MM-dd'T'HH:mm:ss.SSS");
                }
                else if( tempDateString.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]"))
                {
                    format=new StringBuilder("yyyy-MM-dd");
                }
                else if( tempDateString.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]T[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\\.[0-9][0-9][0-9][0-9\\+:\\-]+"))
                {
                    format=new StringBuilder( REST_TIME_FORMAT);
                }
                else if( tempDateString.matches("[0-9]+ [A-Z]{3} [0-9]{4} [0-9]+:[0-9]{2}:[0-9]{2} ([\\-\\+][0-9:]+|UTC)"))
                {
                    format=new StringBuilder( "d MMM yyyy HH:mm:ss z");
                }
            }
        }
    
        if( format == null)
        {
            format = new StringBuilder();

            int section = 0;
            int monthSize = 0;
            for( int i = 0; i < tempDateString.length(); i++)
            {
                char c = tempDateString.charAt( i);

                switch (c) {
                    case ' ':
                    case '-':
                    case '/':
                    case '.':
                        if(
                                section == 0 || // Day
                                section == 1 || // Month
                                section == 2    // year
                                )
                        {
                            if( section < 2)
                            {
                                format.append( c);
                            }
                            else
                            {
                                format.append( " ");
                            }
                            
                            section++;
                        }
                        else
                        {
                            throw new InvalidDataException(
                                "invalid timestamp format '" + dateString + "' ( date portion)"
                            );
                        }   break;
                    case ':':
                        if(
                                section == 3 || // hour
                                section == 4 || // Minute
                                section == 5    // Second
                                )
                        {
                            if( section < 5)
                            {
                                format.append( ":");
                            }
                            
                            section++;
                        }
                        else
                        {
                            throw new InvalidDataException(
                                    "invalid timestamp format '" + dateString + "' ( time portion)"
                            );
                        }   break;
                    default:
                        if( section == 3 && format.indexOf("HH") != -1) section++;
                        if( section == 4 && format.indexOf("mm") != -1) section++;
                        if( section == 5 && format.indexOf("ss") != -1) section++;
                        
                        switch( section)
                        {
                            case 0:
                                format.append( "d");
                                break;
                            case 1:
                                monthSize++;
                                
                                // Handle Sept
                                if( monthSize > 3 && monthSize < 10)
                                {
                                    tempDateString = tempDateString.substring( 0, i) + tempDateString.substring( i + 1);
                                    i--;
                                }
                                else
                                {
                                    format.append( "M");
                                }
                                break;
                            case 2:
                                format.append( "y");
                                break;
                            case 3:
                                format.append( "H");
                                break;
                            case 4:
                                format.append( "m");
                                break;
                            case 5:
                                format.append( "s");
                                break;
                            default:
                                throw new InvalidDataException(
                                        "invalid timestamp format '" + dateString + "' ( invalid section)"
                                );
                        }
                }
            }

            if( format.indexOf( "yy") == -1)
            {
                throw new InvalidDataException(
                    "invalid timestamp format '" + dateString + "' ( missing year)"
                );
            }
        }

        Date date;
        try
        {
            date = parse( format.toString(), tempDateString, tz);
        }
        catch( ParseException pe)
        {
             throw new InvalidDataException(
                "invalid timestamp '" + dateString + "' not in format '" + format + "'",
                pe
            );
        }

        return date;
    }

    /**
     *
     * @param userDateString
     * @param userTimeZone
     * @param defaultToEndOfDay
     * @throws com.aspc.remote.database.InvalidDataException
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public static Date parseUserTime(
        final @Nonnull String    userDateString,
        final @Nullable TimeZone  userTimeZone,
        final boolean defaultToEndOfDay
    ) throws InvalidDataException
    {
        return parseUserTime( userDateString, userTimeZone, defaultToEndOfDay, true);
    }

    /**
     *
     * @param durationString
     * @throws com.aspc.remote.database.InvalidDataException
     * @return the value
     */
    @CheckReturnValue @Nullable 
    public static String parseUserDuration( String durationString) throws InvalidDataException
    {
        if( StringUtilities.isBlank( durationString))
        {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        java.io.StringReader sr = new java.io.StringReader( durationString);
        java.io.StreamTokenizer st = new java.io.StreamTokenizer( sr);
        st.parseNumbers();

        DecimalFormat nf = new DecimalFormat( "#.##");

        try
        {
            int t = st.nextToken();
            while( t != StreamTokenizer.TT_EOF)
            {
                float num;
                String fmt = "Hrs";
                //int multiplier = 60 * 60 * 1000;

                if( t != StreamTokenizer.TT_NUMBER)
                {
                   throw new InvalidDataException(
                        "Duration must be in the format '<number> <type>' e.g. 3 Hrs (" + durationString + ")"
                    );
                }
                num = (float)st.nval;

                t = st.nextToken();
                if( t != StreamTokenizer.TT_EOF)
                {
                    if( t != StreamTokenizer.TT_WORD)
                    {
                       throw new InvalidDataException(
                            "Duration must be in the format 'number fmt' e.g. 3 Hrs (" + durationString + ")"
                        );
                    }
                    fmt = st.sval.toUpperCase();

                    switch (fmt) {
                        case "YEARS":
                        case "YEAR":
                            fmt = "Years";
                            break;
                        case "MONTHS":
                        case "MONTH":
                        case "MN":
                            fmt = "Months";
                            if( num % 1 > 0)
                            {
                                throw new InvalidDataException( "Months must be a whole number");
                            }   
                            break;
                        case "WEEKS":
                        case "WEEK":
                        case "WK":
                        case "W":
                            fmt = "Weeks";
                            break;
                        case "DAYS":
                        case "DAY":
                        case "DY":
                        case "D":
                            fmt = "Days";
                            //multiplier = 24 * 60 * 60 * 1000;
                            break;
                        case "HOURS":
                        case "HOUR":
                        case "HRS":
                        case "HR":
                        case "H":
                            fmt = "Hrs";
                            break;
                        case "MINUTES":
                        case "MINUTE":
                        case "MINS":
                        case "MIN":
                        case "M":
                            fmt = "Mins";
                            break;
                        case "SECONDS":
                        case "SECOND":
                        case "SECS":
                        case "SEC":
                        case "S":
                            fmt = "Secs";
                            break;
                    // pjs 21 jun 04 -- added millisecond case
                        case "MILLISECONDS":
                        case "MILLISECOND":
                        case "MILLISECS":
                        case "MILLISEC":
                        case "MS":
                            fmt = "Ms";
                            break;
                        default:
                            throw new InvalidDataException(
                                    "Duration must be in the format 'number fmt' e.g. 3 Hrs (" + durationString + ")"
                            );
                    }

                }
                if( sb.length() > 0)
                {
                    sb.append( " ");
                }
                sb.append(nf.format(num)).append(" ").append( fmt);

                t = st.nextToken();
            }

            return sb.toString();
        }
        catch( java.io.IOException e)
        {
            throw new InvalidDataException(
                "Duration must be in the format <N T> where N is a number and T is duration type (SECS, MINS, HRS, DAYS)",
                e
            );
        }
    }

    /**
     *
     * @param durationString
     * @throws com.aspc.remote.database.InvalidDataException
     * @return the value
     */
    @CheckReturnValue
    public static long convertDurationToMs( final @Nullable String durationString) throws InvalidDataException
    {
        if( StringUtilities.isBlank( durationString))
        {
            return 0;
        }

        String pd = parseUserDuration( durationString);
        long res = 0;

        StringTokenizer st = new StringTokenizer( pd, " ");
        while( st.hasMoreTokens())
        {
            float num = Float.parseFloat(st.nextToken());
            String fmt = st.nextToken().toUpperCase().trim();
            long multiplier;

            switch (fmt) {
                case "YEARS":
                    /* http://www.kylesconverter.com/time/years-to-milliseconds */
                    multiplier = 31556952000L;
                    break;
                case "MONTHS":
                    multiplier = 28L * 24L * 60L * 60L * 1000L;
                    break;
                case "WEEKS":
                    multiplier = 7L * 24L * 60L * 60L * 1000L;
                    break;
                case "DAYS":
                    multiplier = 24L * 60L * 60L * 1000L;
                    break;
                case "HRS":
                    multiplier = 60 * 60 * 1000;
                    break;
                case "MINS":
                    multiplier = 60 * 1000;
                    break;
                case "SECS":
                    multiplier = 1000;
                    break;
                case "MS":
                    multiplier = 1;
                    break;
                case "":
                    multiplier = 60 * 60 * 1000;
                    break;
                default:
                    throw new InvalidDataException("invalid duration: " + durationString);
//                default:
//                    break;
            }

            res += (long)(num * multiplier);
        }

        return res;
    }


    /**
     *
     * @param startDt
     * @param durationString
     * @param tz timezone
     * @throws com.aspc.remote.database.InvalidDataException
     * @return the value
     */
    @CheckReturnValue @Nullable
    public static Date addDurationToDate( final Date startDt, final String durationString, final TimeZone tz) throws InvalidDataException
    {
        return applyDurationToDate( startDt, durationString, false, tz);
    }


    /**
     *
     * @param startDt
     * @param durationString
     * @param tz timezone
     * @throws com.aspc.remote.database.InvalidDataException
     * @return the value
     */
    @CheckReturnValue @Nullable
    public static Date subtractDurationFromDate( final Date startDt, final String durationString, final TimeZone tz) throws InvalidDataException
    {
        return applyDurationToDate( startDt, durationString, true, tz);
    }

    @CheckReturnValue @Nullable
    private static Date applyDurationToDate( final Date startDt,
                                             final String durationString,
                                             final boolean subtract,
                                             final TimeZone tz) throws InvalidDataException
    {
        if( startDt == null || StringUtilities.isBlank( durationString))
        {
            return null;
        }

        TimeZone wrkTz = tz;
        if(wrkTz == null)
        {
            wrkTz = DateUtil.GMT_ZONE;
        }

        String pd = parseUserDuration( durationString);
        GregorianCalendar gc = DateUtil.makeGC( wrkTz, startDt);

        StringTokenizer st = new StringTokenizer( pd, " ");
        while( st.hasMoreTokens())
        {
            float f = Float.parseFloat(st.nextToken());
            String fmt = st.nextToken().toUpperCase();

            int num;

            // If whole number then just use as is otherwise convert
            if( f % 1 == 0)
            {
                num = (int)f;
            }
            else
            {
                int multiplier = 0;

                switch (fmt) {
                    case "YEARS":
                        fmt = "DAYS";
                        multiplier = 365;
                        break;
                    case "WEEKS":
                        fmt = "DAYS";
                        multiplier = 7;
                        break;
                    case "DAYS":
                        fmt = "HRS";
                        multiplier = 24;
                        break;
                    case "HRS":
                        fmt = "MINS";
                        multiplier = 60;
                        break;
                    case "MINS":
                        fmt = "SECS";
                        multiplier = 60;
                        break;
                    case "SECS":
                        fmt = "MS";
                        multiplier = 1000;
                        break;
                    case "MS":
                        multiplier = 1;
                        break;
                    default:
                        break;
                }
                num = (int)(f*multiplier);
            }

            if( subtract)
            {
                num *= -1;
            }

            switch (fmt) {
                case "YEARS":
                    gc.add( GregorianCalendar.YEAR, num);
                    break;
                case "MONTHS":
                    gc.add( GregorianCalendar.MONTH, num);
                    break;
                case "WEEKS":
                    gc.add( GregorianCalendar.WEEK_OF_YEAR, num);
                    break;
                case "DAYS":
                    gc.add( GregorianCalendar.DATE, num);
                    break;
                case "HRS":
                    gc.add( GregorianCalendar.HOUR_OF_DAY, num);
                    break;
                case "MINS":
                    gc.add( GregorianCalendar.MINUTE, num);
                    break;
                case "SECS":
                    gc.add( GregorianCalendar.SECOND, num);
                    break;
                case "MS":
                    gc.add( GregorianCalendar.MILLISECOND, num);
                    break;
                default:
                    break;
            }

        }

        return gc.getTime();
    }

    @CheckReturnValue @Nonnull
    private static Date parseTimeComponent( String dateString, TimeZone timeZone, boolean parseMilliseconds )throws InvalidDataException
    {
        try
        {
            long msecs;

            msecs = Long.parseLong( dateString);

            if(!parseMilliseconds)
            {
               throw new InvalidDataException(
                    "Timestamp must be in the format 'dd MMM yyyy HH:mm:ss@z' ( " + dateString + ")"
                );
            }
            else
            {
                return new Date( msecs);
            }
        }
        catch( NumberFormatException nf)
        {
            Date date = parseUserTime( dateString, timeZone,  false, parseMilliseconds);

            return date;
        }
    }

    /**
     * Parse a date from the passed pattern.                                    <BR>
     *
     * We use a thread local to store a HashMap of simple date formatters by the pattern
     * string. This allows us to have the performance benefit of having a static
     * formatter without the <i>multi-thread</i> issues associated with static formatters.
     *
     * @param pattern The date pattern to use
     * @param source the textual version of the date
     * @param tz The timezone to use ( default to GMT)
     * @return The date object parsed
     * @throws java.text.ParseException if can't parse the source
     */
    public static @CheckReturnValue @Nonnull Date parse( final @Nonnull String pattern, final @Nonnull String source, final @Nullable TimeZone tz) throws ParseException
    {
        if( StringUtilities.isBlank(pattern)) throw new ParseException("pattern is mandatory", 0);
        if( StringUtilities.isBlank(source)) throw new ParseException("source is mandatory", 0);        
        
        SimpleDateFormat sdf = makeWorker( pattern, tz);

        Date d;

        try
        {
            d =sdf.parse(source);
        }
        catch( ParseException pe)
        {
            /**
             * This maybe because the date is in the cross over of the daylight savings time.
             */
            try
            {
                sdf.setLenient(true);
                Date now;

                now = sdf.parse(source);

                /** 
                 * as of JDK7 'h' is only available when parser is set to lenient
                 * https://bugs.openjdk.java.net/browse/JDK-4396385
                 */
                if( pattern.contains("h"))
                {
                    return now;
                }
                
                Date previous = new Date( now.getTime() - 60 * 60 * 1000);

                Date next = new Date( now.getTime() + 60 * 60 * 1000);

                TimeZone tempTZ = sdf.getTimeZone();

                if( tempTZ.inDaylightTime(now))
                {
                    if(
                        tempTZ.inDaylightTime(previous) == false ||
                        tempTZ.inDaylightTime(next) == false
                    )
                    {
                        return now;
                    }
                    else
                    {
                        throw pe;
                    }
                }
                else
                {
                    if(
                        tempTZ.inDaylightTime(previous) ||
                        tempTZ.inDaylightTime(next)
                    )
                    {
                        return now;
                    }
                    else
                    {
                        throw pe;
                    }
                }
            }
            finally
            {
                sdf.setLenient(false);
            }
        }

        return d;
    }

    @CheckReturnValue @Nonnull
    private static SimpleDateFormat makeWorker(final @Nonnull String pattern, final @Nullable TimeZone userZone)
    {
        HashMap map = (HashMap)LOCAL.get();

        if( map == null)
        {
            map = HashMapFactory.create();
            LOCAL.set( map);
        }

        SimpleDateFormat sdf = (SimpleDateFormat)map.get(pattern);

        if( sdf == null)
        {
            sdf = new SimpleDateFormat( pattern, Locale.ENGLISH);
            sdf.setLenient(false);

            map.put( pattern, sdf);
        }

        TimeZone tz = userZone;

        if( tz == null) tz = DateUtil.GMT_ZONE;

        sdf.setTimeZone(tz);

        return sdf;
    }

    /**
     *
     * @param dateString
     * @param timeZone
     * @param timeOfDay
     * @throws com.aspc.remote.database.InvalidDataException
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public static Date parseLocalUserTime( String dateString, TimeZone timeZone, String timeOfDay  ) throws InvalidDataException
    {
        Date aDate = parseTimeComponent( dateString, timeZone, false );
        assert aDate!=null;
//        if ( aDate != null )
//        {
//            return aDate;
//        }
//        else
//        {
            try
            {
                aDate = DateUtil.parseUserDate( dateString, TimeZone.getTimeZone("GMT" ), true);

                // the Date returned is 00:00 GMT - we want to get the date part & the set the time
                // according to timezone

                GregorianCalendar gc = new GregorianCalendar( TimeZone.getTimeZone("GMT") );
                gc.setTime( aDate );
                int day = gc.get( GregorianCalendar.DAY_OF_MONTH );
                int month = gc.get( GregorianCalendar.MONTH );
                int year = gc.get( GregorianCalendar.YEAR );
                gc.clear();
                gc.setTimeZone( timeZone );

                // set to the end of the day in the local timeZone
                if ( timeOfDay.equalsIgnoreCase( END_OF_DAY ) )
                {
                    gc.set( year, month, day, 23, 59, 59 );
                    gc.set( GregorianCalendar.MILLISECOND, 999 );
                }
                //set to the start of the day in the local timeZone
                else if ( timeOfDay.equalsIgnoreCase( START_OF_DAY ) )
                {
                    gc.set( year, month, day, 0, 0, 0 );
                    gc.set( GregorianCalendar.MILLISECOND, 0 );
                }
                else if ( timeOfDay.equalsIgnoreCase( MARKET_CLOSE ) )
                {
                    gc.set( year, month, day, 16, 0, 0 );
                    gc.set( GregorianCalendar.MILLISECOND, 0 );
                }
                else
                {
                    throw new InvalidDataException("Invalid Time specified ");
                }

                return gc.getTime();
            }
            catch( InvalidDataException e)
            {
               throw new InvalidDataException(
                    "Timestamp must be in the format 'dd MMM yyyy HH:mm:ss@z' ( " + dateString + ")",
                    e
                );
            }
//        }
    }

    /**
     * return dateTime in d MMM yyyy HH:mm:ss
     *
     * @param date
     * @param timeZone
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public static String getStdFormat( Date date, TimeZone timeZone)
    {
        return format( DEFAULT_TIME_FORMAT, date, timeZone);
    }

    /**
     * Format a date to the passed pattern.                                    <BR>
     *
     * We use a thread local to store a HashMap of simple date formatters by the pattern
     * string. This allows us to have the performance benefit of having a static
     * formatter without the <i>multi-thread</i> issues associated with static formatters.
     *
     * @param pattern The date pattern to use
     * @param date the date to be formatted ( default to NOW)
     * @param tz The timezone to use ( default to GMT)
     * @return The formatted date string
     */
    @CheckReturnValue @Nonnull
    public static String format(final @Nonnull String pattern, final @Nullable Date date, final @Nullable TimeZone tz)
    {
        SimpleDateFormat sdf = makeWorker(pattern, tz);

        String formattedDate;
        formattedDate = sdf.format((date == null) ? new Date() : date);

        return formattedDate;
    }

    /**
     *
     * @param userDateString
     * @param userTimeZone
     * @throws com.aspc.remote.database.InvalidDataException
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public static Date parseUserTime(
        final @Nonnull String    userDateString,
        final @Nullable TimeZone  userTimeZone
    ) throws InvalidDataException
    {
        return parseUserTime( userDateString, userTimeZone, true);
    }

    private static final ThreadLocal LOCAL = new ThreadLocal();
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.TimeUtil");//#LOGGER-NOPMD
}
