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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;

/**
 *  DateUtil misc utilities for handling dates.
 *
 *  @See TimeUtil for time related utilities.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       18 August 1998
 */
public final class DateUtil
{
    /**
     * User date NOW
     */
    public static final String TYPE_NOW="NOW";

    /**
     * User date TODAY
     */
    public static final String TYPE_TODAY="TODAY";

    /**
     * User date TOMORROW
     */
    public static final String TYPE_TOMORROW="TOMORROW";

    /**
     * User date YESTERDAY
     */
    public static final String TYPE_YESTERDAY="YESTERDAY";

    /**
     * get the date difference with the default time zone. Use the
     * <code>dateDiff( Date start, Date end, String type, TimeZone tzStart, TimeZone tzEnd)</code>
     * method as possible as you could.
     *
     * @param start start date
     * @param end end date
     * @param type return value type
     * @return long value of the given type
     */
    @CheckReturnValue
    public static long dateDiff( Date start, Date end, String type)
    {
        return dateDiff( start, end, type, GMT_ZONE, GMT_ZONE);
    }

    /**
     *
     * get the date difference with the given time zones and dates
     *
     * @param start start date
     * @param end end date
     * @param type return value type
     * @param tzStart time zone of the start date
     * @param tzEnd time zone of the end date
     * @return long value of the given type
     */
    @CheckReturnValue
    public static long dateDiff( Date start, Date end, String type, TimeZone tzStart, TimeZone tzEnd)
    {
        long ms;
        long secs;
        Calendar c1 = makeGC(tzStart, start);

        Calendar c2 = makeGC(tzEnd, end);

        ms = (c2.getTimeInMillis() + c2.get(Calendar.DST_OFFSET)) - (c1.getTimeInMillis() + c1.get(Calendar.DST_OFFSET));

        if( type.equalsIgnoreCase( "Milleseconds"))
        {
            return ms;
        }

        secs = ms/1000;

        int w, d, h, m, s;
        w = (int) secs/60/60/24/7;
        d = (int) secs/60/60/24;
        h = (int) secs/60/60;
        m = (int) secs/60;
        s = (int) secs;

        if( type.equalsIgnoreCase( "Weeks"))
        {
            return w;
        }

        if( type.equalsIgnoreCase( "Days"))
        {
            return d;
        }

        if( type.equalsIgnoreCase( "Hours"))
        {
            return h;
        }

        if( type.equalsIgnoreCase( "Minutes"))
        {
            return m;
        }


        if( type.equalsIgnoreCase( "Seconds"))
        {
            return s;
        }

        return s;
    }

    @CheckReturnValue @Nonnull
    public static String toJSON(final @Nonnull Date date ) 
    {        
        SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssz" );
        
        TimeZone tz = TimeZone.getTimeZone( "UTC" );
        
        df.setTimeZone( tz );

        String output = df.format( date );

        int inset0 = 9;
        int inset1 = 6;
        
        String s0 = output.substring( 0, output.length() - inset0 );
        String s1 = output.substring( output.length() - inset1, output.length() );

        String result = s0 + s1;

        result = result.replaceAll( "UTC", "+00:00" );
        
        return result;        
    }

    /**
     *
     * @param start start date
     * @param type type of value
     * @param value value to be added
     
     * @return added date
     */
    @CheckReturnValue @Nonnull
    public static Date dateAdd( Date start, String type, int value)
    {
        Calendar c = makeGC(GMT_ZONE, start);

        int field = -1;

        if( type.equalsIgnoreCase( "Weeks"))
        {
            field = Calendar.WEEK_OF_YEAR;
        }

        if( type.equalsIgnoreCase( "Days"))
        {
            field = Calendar.DAY_OF_YEAR;
        }

        if( type.equalsIgnoreCase( "Hours"))
        {
            assert false: "DateUtil is for Dates ie. GMT whole days, can't add " + type;
            field = Calendar.HOUR_OF_DAY;
        }

        if( type.equalsIgnoreCase( "Minutes"))
        {
            assert false: "DateUtil is for Dates ie. GMT whole days, can't add " + type;
            field = Calendar.MINUTE;
        }

        if( type.equalsIgnoreCase( "Seconds"))
        {
            assert false: "DateUtil is for Dates ie. GMT whole days, can't add " + type;
            field = Calendar.SECOND;
        }

        if(field == -1)
        {
            assert false: "DateUtil add unknown type: " + type;
            return start;
        }

        c.add(field, value);
        return c.getTime();
    }

    /**
     *
     * @param dt
     * @param timeZone the timezone.
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public static Date weekEnd( final Date dt,  final TimeZone timeZone)
    {
        GregorianCalendar localGC = makeGC(timeZone, dt);
        GregorianCalendar gc = makeGC(GMT_ZONE, dt);
//        gc.setTime( dt);
        gc.set(Calendar.HOUR_OF_DAY, 0);
        gc.set(Calendar.MINUTE, 0);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);

        if( 
            localGC.get( Calendar.DAY_OF_WEEK) != Calendar.SUNDAY
        )
        {
            gc.add( Calendar.DATE, 1);
            return weekEnd(gc.getTime(), timeZone);
        }
        if( gc.get( Calendar.DAY_OF_WEEK)== Calendar.SATURDAY)
        {
            gc.add( Calendar.DATE, 1);
        }
        else if( gc.get( Calendar.DAY_OF_WEEK)== Calendar.MONDAY)
        {
            gc.add( Calendar.DATE, -1);
        }
        
        Date weekEndDT=gc.getTime();
        assert weekEndDT.getTime() + 25 * 60 * 60 * 1000> dt.getTime(): weekEndDT + " should be the same or greater date " + dt;
        assert weekEndDT.getTime() < dt.getTime() + 7 * 24 * 60 * 60 * 1000: weekEndDT + " can only skip 7 days " + dt;
        
        return weekEndDT;
    }

    /**
     * This function checks if the day passed is a week day
     * @param day WeekDay
     * @return boolean True if it is a week day else false
     */
    @CheckReturnValue
    public static boolean isWeekDay( final int day )
    {
        boolean result = true;

        if( day == Calendar.SATURDAY || day == Calendar.SUNDAY )
        {
            result =  false;
        }

        return result;
    }

    /**
     * This function returns the first business/week day of the month of the date passed
     * @param timeZone the user's timezone
     * @param date the date to check
     * @return int the first week/business day of the month
     */
    @CheckReturnValue
    public static int getFirstWeekDayOfMonth(final TimeZone timeZone, Date date )
    {
        /**
         * Firstly get first day of month and start off with it
         */
        GregorianCalendar gc = DateUtil.makeGC( timeZone, date);
        gc.set( Calendar.DAY_OF_MONTH, 1 );

        while( true )
        {
            /**
             * Now get day of week
             */
            int dayOfWeek = gc.get( Calendar.DAY_OF_WEEK);

            /**
             * If day of the week is a weekday then break out of it
             */
            if( isWeekDay( dayOfWeek ) )
            {
                return gc.get( Calendar.DAY_OF_MONTH);
            }

            /**
             * Now increment the day of month
             */
            gc.add( Calendar.DAY_OF_MONTH, 1);
        }
    }


    /**
     * This function returns the first given day of the month of the date passed
     * @param timeZone the user's time zone
     * @param date the date to check
     * @param occurence Example, 2 represents the 2nd occurrence of the day from 1 to 31.
     * @param givenDay the day to find out in the month
     * @return int the first given day of the month of the date passed
     * @throws Exception A serious problem occurred
     */
    @CheckReturnValue
    public static int getDayOfMonth(final TimeZone timeZone, Date date, int givenDay, int occurence )throws Exception
    {
        if(occurence < -5 || occurence > 5)
        {
            throw new Exception("Bad Occurence:" + occurence+". The given day cannot occur in a month morethan: -5/+5 ");
        }
        /**
         * Firstly get first day of month and start off with it
         */
        GregorianCalendar gc = DateUtil.makeGC( timeZone, date);
        int month;
        if( occurence >= 0)
        {
            gc.set( Calendar.DAY_OF_MONTH, 1 );
            month = gc.get(Calendar.MONTH);
        }
        else
        {
            gc.set( Calendar.DAY_OF_MONTH, gc.getActualMaximum( Calendar.DAY_OF_MONTH) );
            month = gc.get(Calendar.MONTH);
        }

        int i = 0;
        int tempMonth;
        while( true)
        {
            /**
             * Now get day of week
             */
            int dayOfWeek = gc.get( Calendar.DAY_OF_WEEK);

            /**
             * If day of the week is a give day then break out of it
             */
            if(dayOfWeek == givenDay)
            {
                if( occurence >= 0)
                {
                    if( ++i == occurence )
                    {
                         return gc.get( Calendar.DAY_OF_MONTH);
                    }
                }
                else
                {
                    if( --i == occurence )
                    {
                         return gc.get( Calendar.DAY_OF_MONTH);
                    }
                }
            }

            /**
             * Now increment the day of month
             */
            if( occurence >= 0)
            {
                gc.add( Calendar.DAY_OF_MONTH, 1);
            }
            else
            {
                gc.add( Calendar.DAY_OF_MONTH, -1);
            }
            tempMonth = gc.get(Calendar.MONTH);
            if(month != tempMonth && (i==4 || i == -4) )
            {
                 throw new Exception("Bad Occurence:" + occurence+". The given day doesn't occur in a given month");
            }
        }
    }

    /**
     * This function returns the Weekend day of the month like., first, second, third ..etc based on occurrence.
     * if occurrence is 2 then 2nd Weekend day of month. if occurrence is -2 then last second weekend day of the month
     * @param timeZone the user's time zone
     * @param date the date to check
     * @param occurence can have values like -4,-3, -2, -1, 1, 2, 3,4
     * @return int the weekend day of the month
     */
    @CheckReturnValue
    public static int getWeekendDayOfMonth(final TimeZone timeZone, Date date, int occurence )
    {
        /**
         * Firstly get first day of month and start off with it
         */
        GregorianCalendar gc = DateUtil.makeGC( timeZone, date);
        if( occurence >= 0)
        {
            gc.set( Calendar.DAY_OF_MONTH, 1 );
        }
        else
        {
            gc.set( Calendar.DAY_OF_MONTH, gc.getActualMaximum( Calendar.DAY_OF_MONTH) );
        }

        int i = 0;
        while( true )
        {
            /**
             * Now get day of week
             */
            int dayOfWeek = gc.get( Calendar.DAY_OF_WEEK);

            /**
             * If day of the week is a weekday then break out of it
             */
            if( isWeekDay( dayOfWeek ) == false )
            {
                if( occurence >= 0)
                {
                    if( ++i == occurence )
                    {
                         return gc.get( Calendar.DAY_OF_MONTH);
                    }
                }
                else
                {
                    if( --i == occurence )
                    {
                         return gc.get( Calendar.DAY_OF_MONTH);
                    }
                }
            }

            /**
             * Now increment the day of month
             */
            if( occurence >= 0)
            {
                gc.add( Calendar.DAY_OF_MONTH, 1);
            }
            else
            {
                gc.add( Calendar.DAY_OF_MONTH, -1);
            }
        }
    }

    /**
     * This function returns the WeekDay/business day of the month like., first, second, third ..etc based on occurrence.
     * if occurrence is 2 then 2nd business day of month. if occurrence is -2 then last second business day of the month
     * @param timeZone the user's time zone
     * @param date the date to check
     * @param occurence can have values like -20... -3, -2, -1, 1, 2, 3 ...20
     * @return int the first weekday/business day of the month
     * @throws Exception A serious problem occurs
     */
    @CheckReturnValue @Nonnull
    public static int getWeekDayOfMonth(final TimeZone timeZone, Date date, int occurence )throws Exception
    {
        if(occurence < -23 || occurence > 23)
        {
            throw new Exception("Bad Occurence:" + occurence +" Buisness day cannot be more than count 23");
        }
        /**
         * Firstly get first day of month and start off with it
         */
        GregorianCalendar gc = DateUtil.makeGC( timeZone, date);
        if( occurence >= 0)
        {
            gc.set( Calendar.DAY_OF_MONTH, 1 );
        }
        else
        {
            gc.set( Calendar.DAY_OF_MONTH, gc.getActualMaximum( Calendar.DAY_OF_MONTH) );
        }

        int i = 0;
        while( true )
        {
            /**
             * Now get day of week
             */
            int dayOfWeek = gc.get( Calendar.DAY_OF_WEEK);

            /**
             * If day of the week is a weekday then break out of it
             */
            if( isWeekDay( dayOfWeek ) )
            {
                if( occurence >= 0)
                {
                    if( ++i == occurence )
                    {
                         return gc.get( Calendar.DAY_OF_MONTH);
                    }
                }
                else
                {
                    if( --i == occurence )
                    {
                         return gc.get( Calendar.DAY_OF_MONTH);
                    }
                }
            }

            /**
             * Now increment the day of month
             */
            if( occurence >= 0)
            {
                gc.add( Calendar.DAY_OF_MONTH, 1);
            }
            else
            {
                gc.add( Calendar.DAY_OF_MONTH, -1);
            }
        }
    }

    /**
     * This function returns the WeekDays between start date and end date,
     * @param timeZone  the user's time zone
     * @param start  start date
     * @param end  end date
     * @return weekdays
     * @throws Exception A serious problem occurs
     */
    @CheckReturnValue
    public static long getWeekDaysBetween(final TimeZone timeZone, Date start, Date end)throws Exception
    {
        long weekdays = 0;
        long days = dateDiff(start, end, "Days");

        GregorianCalendar gc = DateUtil.makeGC( timeZone, start);

        for(int i=0; i<days; i++)
        {
            /**
             * Now get day of week
             */
            int dayOfWeek = gc.get( Calendar.DAY_OF_WEEK);

            if( isWeekDay( dayOfWeek ) )
            {
              weekdays++;
            }
            gc.add( Calendar.DAY_OF_YEAR, 1);
        }
        return weekdays;
    }
    
    /**
     * This function returns the last business/weekday day of the month of the date passed
     * @param timeZone the user's timezone
     * @param date the date to check
     * @return int the last weekday/business day of the month
     */
    @CheckReturnValue
    public static int getLastWeekDayOfMonth( final TimeZone timeZone, final Date date )
    {
        /**
         * Firstly get last day of month and start off with it
         */
        GregorianCalendar gc = DateUtil.makeGC( timeZone, date);
        gc.set( Calendar.DAY_OF_MONTH, gc.getActualMaximum( Calendar.DAY_OF_MONTH) );

        while( true )
        {
            /**
             * Now get day of week
             */
            int dayOfWeek = gc.get( Calendar.DAY_OF_WEEK);

            /**
             * If day of the week is a weekday then break out of it
             */
            if( isWeekDay( dayOfWeek ) )
            {
                return gc.get( Calendar.DAY_OF_MONTH);
            }

            /**
             * Now decrement the day of month
             */
            gc.add( Calendar.DAY_OF_MONTH, -1);
        }
    }

    /**
     * getDayOfWeek returns the day of week in integer
     *
     * @param timeZone The time zone
     * @param date The date to be checked
     *
     * @return day of week
     */
    @CheckReturnValue
    public static int getDayOfWeekInt( final TimeZone timeZone, final Date date )
    {
        Calendar c1 = makeGC(timeZone, date);
        int dayOfWk = c1.get(Calendar.DAY_OF_WEEK);
        return dayOfWk;
    }

    /**
     * getDayOfWeek returns the day of week
     *
     * @param timeZone The time zone
     * @param date The date to be checked
     * @param inLongFormat The format of the weekday format i.e. [true: Sunday] | [false: Sun]
     *
     * @return day of week
     */
    @CheckReturnValue @Nonnull
    public static String getDayOfWeek( final TimeZone timeZone, final Date date, boolean inLongFormat )
    {
        String strDayOfWk = "";
        String[] strDay;
        if (inLongFormat == true)
        {
            strDay = WEEKDAY_LONG;
        }
        else
        {
            strDay = WEEKDAY_SHORT;
        }
        int iDayOfWk = getDayOfWeekInt(timeZone, date);
        switch (iDayOfWk)
        {
            case Calendar.SUNDAY:
            case Calendar.MONDAY:
            case Calendar.TUESDAY:
            case Calendar.WEDNESDAY:
            case Calendar.THURSDAY:
            case Calendar.FRIDAY:
            case Calendar.SATURDAY:
                strDayOfWk = strDay[iDayOfWk - 1];
                break;
            default:
                break;
        }
        return strDayOfWk;
    }

    /**
     * initializes a calendar with the passed in timezone ( GMT if null) and the date ( current time if null)
     * @param timeZone the user's timezone
     * @param date the date to set the calendar to.
     * @return the initialized calendar.
     */
    @CheckReturnValue @Nonnull
    public static GregorianCalendar makeGC( final TimeZone timeZone, final Date date )
    {
        TimeZone tz = timeZone;

        if( tz == null) tz = GMT_ZONE;

        GregorianCalendar gc = new GregorianCalendar( tz);

        if( date != null)
        {
            gc.setTime( date);
        }

        return gc;
    }

    /**
     * Gets current date with no time
     * @return Date
     * @param timeZone the user's timezone
     */
    @CheckReturnValue @Nonnull
    public static GregorianCalendar getTodayGC( final TimeZone timeZone)
    {
        TimeZone tz = timeZone;

        if( tz == null) tz = GMT_ZONE;

        GregorianCalendar gc = new GregorianCalendar( tz);

        gc.set(Calendar.HOUR_OF_DAY, 0);
        gc.set(Calendar.MINUTE, 0);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);

        return gc;
    }

    /**
     * Gets current date with no time
     * @return Date
     * @param timeZone the user's timezone
     */
    @CheckReturnValue @Nonnull
    public static Date getToday( final TimeZone timeZone)
    {
        return getTodayGC( timeZone).getTime();
    }

    /**
     * This function returns the date for tomorrow
     * @return Date the result date
     * @param timeZone the user's timezone
     */
    @CheckReturnValue @Nonnull
    public static Date getTommorrow(final TimeZone timeZone)
    {
        GregorianCalendar gc;
        gc = getTodayGC( timeZone);
        gc.add(Calendar.DATE, 1);

        return gc.getTime();
    }

    /**
     * This function returns the date for yesterday
     * @return Date the result date
     * @param timeZone the user's timezone
     */
    @CheckReturnValue @Nonnull
    public static Date getYesterday(final TimeZone timeZone)
    {
        GregorianCalendar gc;
        gc = getTodayGC( timeZone);
        gc.add(Calendar.DATE, -1);

        return gc.getTime();
    }

    /**
     * This function returns the current year
     * @param timeZone the user's timezone
     * @return the year
     */
    @CheckReturnValue
    public static int getCurrentYear(final TimeZone timeZone)
    {
        GregorianCalendar gc = new GregorianCalendar(timeZone);
        gc.setTime(new Date());
        return gc.get(GregorianCalendar.YEAR);
    }

    /**
     *
     * @return the value
     * @param userDate
     * @param timeZone the user's timezone
     * @param allowFutureDates
     * @throws com.aspc.remote.database.InvalidDataException
     */
    @CheckReturnValue @Nonnull
    public static Date parseUserDate(
        final String userDate,
        final TimeZone timeZone,
        final boolean allowFutureDates
    ) throws InvalidDataException
    {
        Date aDate = null;

        try
        {
            aDate = TimeUtil.parseUserTime( userDate, timeZone, false, false);
        }
        catch( InvalidDataException ide)
        {
            
        }
        catch( Exception e)
        {
            LOGGER.warn( userDate, e);
            assert false: e.toString();
        }

        if( aDate == null)
        {
            String dateString = userDate.trim().toUpperCase();

            while( dateString.contains("  "))
            {
                dateString = StringUtilities.replace( dateString, "  ", " ");
            }
            if( dateString.length() > 5 &&  dateString.startsWith( "TODAY" ) && !dateString.contains("TD") )
            {
                int days = Integer.parseInt( dateString.substring( 5 ) );
                GregorianCalendar gc = new GregorianCalendar( timeZone);
                gc.add( GregorianCalendar.DATE, days );
                gc.set(Calendar.HOUR_OF_DAY, 0);
                gc.set(Calendar.MINUTE, 0);
                gc.set(Calendar.SECOND, 0);
                gc.set(Calendar.MILLISECOND, 0);
                aDate = gc.getTime();
            }
            else
            {
                throw new InvalidDataException(
                    "Dates must be in the format 'd MMM yyyy' ( " + dateString + ")"
                );
            }
        }

        if( aDate.getTime() < JAN1ST1900)
        {
            LOGGER.warn( "WARNING: " + aDate + " < 1 Jan 1900 you probably entered a 2-digit year");
        }

        if( allowFutureDates == false)
        {
            if( aDate.after(new Date()))
            {
                throw new InvalidDataException(
                    "You can't have future dates '" + getStdFormat( aDate, timeZone) + "'"
                );
            }
        }

        TimeZone tempTZ=timeZone;

        // Get the timezone
        int pos = userDate.indexOf( '@');
        if( pos != -1)
        {
            // TimeZone is case sensitive ( don't uppercase)
            String tzStr = userDate.substring( pos + 1);
            tzStr = StringUtilities.replace( tzStr, " ", "");

            tempTZ = TimeZone.getTimeZone( tzStr);
        }

        String t = getStdFormat( aDate, tempTZ);

        Date resultDate;
        try
        {
            resultDate = TimeUtil.parse( STD_FORMAT, t, null);
        }
        catch( Exception e)
        {
            throw new InvalidDataException( e.toString(), e);
        }

        return resultDate;
    }

    /**
     *
     * @param date
     * @param timeZone the user's timezone
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public static String getStdFormat( final @Nonnull Date date, final @Nullable TimeZone timeZone)
    {
        String text;

        text = TimeUtil.format( STD_FORMAT, date, timeZone);

        return text;
    }

    /**
     *
     * @param format
     * @param value the value
     * @throws Exception a serious problem
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public static Date parse( final @Nonnull String format, final @Nonnull String value) throws Exception
    {
        return TimeUtil.parse( format, value, null);
    }

    
    /**
     * Different in months
     * @param start start date
     * @param end end date
     * @return Different in months
     */
    @CheckReturnValue
    public static int monthsBetween(final @Nonnull Date start, final @Nonnull Date end)
    {
        Calendar date1 = makeGC(GMT_ZONE, end);
//        date1.setTime(end);
        
        Calendar date2 = makeGC(GMT_ZONE, start);
//        date2.setTime(start);
        
        double monthsBetween ;
        //difference in month for years
        monthsBetween = (date1.get(Calendar.YEAR)-date2.get(Calendar.YEAR))*12;
        //difference in month for months
        monthsBetween += date1.get(Calendar.MONTH)-date2.get(Calendar.MONTH);
        //difference in month for days
        if(date1.get(Calendar.DAY_OF_MONTH)!=date1.getActualMaximum(Calendar.DAY_OF_MONTH)
                && date1.get(Calendar.DAY_OF_MONTH)!=date1.getActualMaximum(Calendar.DAY_OF_MONTH) )
        {
            monthsBetween += ((date1.get(Calendar.DAY_OF_MONTH)-date2.get(Calendar.DAY_OF_MONTH))/31d);
        }
        return (int)Math.round(Math.abs(monthsBetween));
    }

    /**
     * the GMT ZONE.
     */
    public static final TimeZone            GMT_ZONE = TimeZone.getTimeZone("GMT");

    /**
     * RFC1123 PATTERN.
     */
    public static final String              RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";

    /**
     * String for the day of the week.
     */
    private static final String[] WEEKDAY_SHORT = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri",
    "Sat" };
    private static final String[] WEEKDAY_LONG = { 
        "Sunday", 
        "Monday", 
        "Tuesday",
        "Wednesday", 
        "Thursday", 
        "Friday", 
        "Saturday" 
    };

    private static final String   STD_FORMAT = "d MMM yyyy";

    /** January 1st 1900 in milli seconds */
    private static final long               JAN1ST1900;//NOPMD

    /** Don't allow creation of DateUtil */
    private DateUtil()
    {
    }

    static
    {
        GregorianCalendar gc = new GregorianCalendar(1900,0,1);
        gc.setTimeZone( GMT_ZONE);
        JAN1ST1900 = gc.getTime().getTime();
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.DateUtil");//#LOGGER-NOPMD
}
