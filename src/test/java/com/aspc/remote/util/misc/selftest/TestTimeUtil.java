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
package com.aspc.remote.util.misc.selftest;

import com.aspc.remote.database.InvalidDataException;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.DateUtil;
import com.aspc.remote.util.misc.TimeUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.commons.logging.Log;

/**
 *  check time utilities
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author         Nigel Leck
 *  @since          October 22, 2004
 */
public class TestTimeUtil extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.selftest.TestTimeUtil");//#LOGGER-NOPMD

    /**
     * Creates new VirtualDBTestUnit
     * @param name The name of the test unit
     */
    public TestTimeUtil(String name)
    {
        super( name);
    }

    /**
     * run the tests
     * @param args the args
     */
    public static void main(String[] args)
    {
        Test test=suite();
//        test = TestSuite.createTest(TestTimeUtil.class, "testApplyDuration");
        TestRunner.run(test);
    }

    /**
     * create a test suite
     * @return the suite of tests
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestTimeUtil.class);
        return suite;
    }

    /**
     * Handling of 12 hour times are no longer lenient as of JDK7 'h' is only available when parser is set to lenient
     * https://bugs.openjdk.java.net/browse/JDK-4396385
     * 
     * @throws ParseException a serious problem.
     */
    public void test1stOct2008() throws ParseException
    {
        String checks[][]={
            {TimeUtil.REST_TIME_FORMAT,"2008-10-01T00:00:00.000+00:00"},
            {"ddd MMM yyyy HH:mm:ss.S","1 Oct 2008 00:00:00.000"},
            {"dd MMM yyyy HH:mm:ss.S","1 Oct 2008 00:00:00.000"},
            {"d MMM yyyy HH:mm:ss.S","1 Oct 2008 00:00:00.000"},
            {"ddd MMM yyyy hh:mm:ss.S","1 Oct 2008 00:00:00.000"},
            {"dd MMM yyyy hh:mm:ss.S","1 Oct 2008 00:00:00.000"},
            {"d MMM yyyy hh:mm:ss.S","1 Oct 2008 00:00:00.000"},

            {"ddd MMM yyyy HH:mm:ss","1 Oct 2008 00:00:00"},
            {"dd MMM yyyy HH:mm:ss","1 Oct 2008 00:00:00"},
            {"d MMM yyyy HH:mm:ss","1 Oct 2008 00:00:00"},
            {"ddd MMM yyyy hh:mm:ss","1 Oct 2008 00:00:00"},
            {"dd MMM yyyy hh:mm:ss","1 Oct 2008 00:00:00"},
            {"d MMM yyyy hh:mm:ss","1 Oct 2008 00:00:00"},

            {"ddd MMM yyyy HH:mm","1 Oct 2008 00:00"},
            {"dd MMM yyyy HH:mm","1 Oct 2008 00:00"},
            {"d MMM yyyy HH:mm","1 Oct 2008 00:00"},
            {"ddd MMM yyyy hh:mm","1 Oct 2008 00:00"},
            {"dd MMM yyyy hh:mm","1 Oct 2008 00:00"},
            {"d MMM yyyy hh:mm","1 Oct 2008 00:00"},

            {"ddd MMM yyyy HH","1 Oct 2008 00"},
            {"dd MMM yyyy HH","1 Oct 2008 00"},
            {"d MMM yyyy HH","1 Oct 2008 00"},
            {"ddd MMM yyyy hh","1 Oct 2008 00"},
            {"dd MMM yyyy hh","1 Oct 2008 00"},
            {"d MMM yyyy hh","1 Oct 2008 00"},

            {"ddd MMM yyyy","1 Oct 2008"},
            {"dd MMM yyyy","1 Oct 2008"},
            {"d MMM yyyy","1 Oct 2008"},
            {"ddd MMM yyyy","1 Oct 2008"},
            {"dd MMM yyyy","1 Oct 2008"},
            {"d MMM yyyy","1 Oct 2008"},

        };
        final long expectedMS=1222819200000L;
        for( String[] check: checks)
        {
            String format=check[0];
            String value=check[1];
            Date oct1=TimeUtil.parse(format,value, DateUtil.GMT_ZONE);
            String msg="parse( " + format +"," + value + ")=" + oct1;
            LOGGER.info( msg);
            
            assertEquals( msg, expectedMS, oct1.getTime());
        }
    }
    
    public void testInOut() throws InvalidDataException, ParseException
    {
        Date inTS=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat(TimeUtil.REST_TIME_FORMAT);
        String out=sdf.format(inTS);
        Date outTS=sdf.parse(out);
        
        assertEquals( inTS + "->" + out + "->" + outTS, inTS, outTS);
    }
    
    
    public void testRestTS() throws InvalidDataException
    {
        String tmp="2013-10-22T14:21:20.347+11:00";
        Date ts=TimeUtil.parseUserTime(tmp, null);
        
        LOGGER.info( ts);
        LOGGER.info( ts.getTime());
        
        assertEquals( "2013-10-22T14:21:20.347+11:00", 1382412080347L,ts.getTime());
    }
    
    public void testRSS() throws Exception
    {
       String list[]={
           "09 Nov 2014 7:52:46 +1100",
           "9 Nov 2014 7:52:46 +1100",
           "9 Nov 2014 07:52:46 +1100",
           "Sun 9 Nov 2014 07:52:46 +1100",
           "Sun, 9 Nov 2014 07:52:46 +1100",
           "Sun, 09 Nov 2014 07:52:46 +1100",
           "9 Nov 2014 07:52:46@GMT+11:00"
       };
       
        parseDate(list);
    }
    /**
     * Check new format that starts with month and AM/PM marker.
     *
     * @throws Exception a serious problem
     */
    public void testFormatStartsWithMonth() throws Exception
    {
        String list[] = {"Aug  4 2005",
                         "JAN 20 1999",
                         "Jun  1 2000",
                        "Jan 31 2002 12:00:00:000AM",
                        "Jan 31, 2002 12:00:00:000AM",
                        "Jan 31 2002 12:00:00:000 AM",
                        "Jan 31, 2002 12:00:00:000 AM",
                        "Jan 31 2002 12:00:00 AM",
                        "Jan 31, 2002 12:00:00 AM",
                        "Jan 31 2002 12:00:00AM",
                        "Jan 31, 2002 12:00:00AM",
                        "Jan 31 2002 12:00:00",
                        "Jan 31, 2002 12:00:00",
                        "Jan 31 2002 12:00:00:000",
                        "Jan 31, 2002 12:00:00:000",
                        "Jan 3 2002 12:00:00:000AM",
                        "Jan 3, 2002 12:00:00:000AM",
                        "Jan 3 2002 12:00:00:000 AM",
                        "Jan 3, 2002 12:00:00:000 AM",
                        "Jan 3 2002 12:00:00 AM",
                        "Jan 3, 2002 12:00:00 AM",
                        "Jan 3 2002 12:00:00AM",
                        "Jan 3, 2002 12:00:00AM",
                        "Jan 3 2002 12:00:00",
                        "Jan 3, 2002 12:00:00",
                        "Jan 3 2002 12:00:00:000",
                        "Jan 3, 2002 12:00:00:000",
                        "Jan 3 2002 2:00:00:000AM",
                        "Jan 3, 2002 2:00:00:000AM",
                        "Jan 3 2002 2:00:00:000 AM",
                        "Jan 3, 2002 2:00:00:000 AM",
                        "Jan 3 2002 2:00:00 AM",
                        "Jan 3, 2002 2:00:00 AM",
                        "Jan 3 2002 2:00:00AM",
                        "Jan 3, 2002 2:00:00AM",
                        "Jan 3 2002 2:00:00",
                        "Jan 3, 2002 2:00:00",
                        "Jan 3 2002 2:00:00:000",
                        "Jan 3, 2002 2:00:00:000"};
        TimeZone tz = TimeZone.getDefault();
        for (String list1 : list) {
            Date date;
            String temp = list1;
            date = TimeUtil.parseUserTime(temp, tz);
            LOGGER.info( temp + "->" + date);
        }
    }

    /**
     * check off set
     * @throws Exception a serious problem
     */
    public void testParseOffset() throws Exception
    {
        Date nyTime = TimeUtil.parseUserTime("8 Jan 1994 15:00@GMT-05:00", null);
        Date gmtTime = TimeUtil.parseUserTime("8 Jan 1994 15:00@GMT", null);

        if( nyTime.getTime() <= gmtTime.getTime())
        {
            String gmt = TimeUtil.getStdFormat( gmtTime, TimeZone.getTimeZone("GMT"));
            String ny = TimeUtil.getStdFormat( nyTime, TimeZone.getTimeZone("GMT-05:00"));

            String temp = "ny=" + ny + "{" + nyTime + "}" + ", gmt=" + gmt + "{" + gmtTime+ "}";

            fail( temp);
        }
    }

    /**
     * check parse of time
     * @throws Exception a serious problem
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testParseTime() throws Exception
    {
        try
        {
            TimeUtil.parseUserTime("8 Jan 1994 1500@GMT", null);
        }
        catch (Exception e)
        {
            fail("It should parse when time is '8 Jan 1994 1500' " + e.getMessage());
        }

        try
        {
            TimeUtil.parseUserTime("8 Jan 1994 15:00@GMT", null);
        }
        catch (Exception e)
        {
            fail("It should parse when time is '8 Jan 1994 15:00' " + e.getMessage());
        }

        try
        {
            TimeUtil.parseUserTime("8 Jan 1999 XX:XX@GMT", null);
            fail("It should not parse for invalid time '8 Jan 1999 XX:XX'");
        }
        catch (Exception e)
        {
            //ok;
        }
    }

    /**
     * check the parsing of daylight saving times.
     *
     * A problem was found parsing dates that are within the time of the daylight savings change over.
     * When the clock is changed back to 2am from 3am. The issue is that the time 2:23 occurs twice.
     *
     * @throws Exception a serious problem
     *
    public void testDaylight() throws Exception
    {
        TimeZone tz = TimeZone.getTimeZone("US/Eastern");
        String list[] = {
            "09 March 2008 02:23:04",
            "09 March 2008 01:23:04", // starts DST in NY
            "09 March 2008 03:23:04",
            "02 Nov 2008 02:23:04",
            "02 Nov 2008 01:23:04",   // end DST in NY
            "02 Nov 2008 03:23:04",
        };

        for( int loop = 0; loop < 2; loop++)
        {
            for( int i = 0; i < list.length; i++)
            {
                Date date;

                String temp = list[i];
                String format = "dd MMM yyyy HH:mm:ss";

                if( loop == 1)
                {
                    format = "d MMM yyyy HH:mm:ss";
                    temp = temp.substring(1);
                }
                date = TimeUtil.parseUserTime(temp, tz);

                String match;

                match = TimeUtil.format( format, date, tz);

                assertEquals( "should match the requested time", temp, match);
            }
        }
    }*/

    /**
     * Check the special formats
     *
     * @throws Exception a serious problem
     */
    public void testSpecial() throws Exception
    {
        String list[] = {
            "23 Apr 2015 08:33:41 UTC",
            "27 APR 2015 04:46:59 UTC",
            "2011-11-16T21:39:06.044",
            "2011-11-16T21:11:41",
            "09 March 2008 02:23:04",
            "09 March 2008 01:23:04", // starts DST in NY
            "09 March 2008 03:23:04",
            "02 Nov 2008 02:23:04",
            "02 Nov 2008 01:23:04",   // end DST in NY
            "02 Nov 2008 03:23:04",
            "03 Apr 2005 02:23:04",
            "03 Apr 2005 02:23:04",
            "09 JAN 2003 06:16:53 GMT",
            "current_timestamp()",
            DateUtil.TYPE_NOW,
            DateUtil.TYPE_TODAY,
            DateUtil.TYPE_TOMORROW,
            DateUtil.TYPE_YESTERDAY,
            "30-Sept-06",
            "30-September-06",
            "Wed Dec 31 23:58:18 1969",
            "Tue Apr  3 15:39:54 2007",
            "13 Feb 2006 23:31:11 GMT",
            "2016-04-18T07:48:04Z"

        };

        TimeZone tz = TimeZone.getTimeZone("US/Eastern");

        for (String list1 : list) {
            Date date;
            String temp = list1;
            date = TimeUtil.parseUserTime(temp, tz);
            LOGGER.info( temp + "->" + date);
        }
        for (String list1 : list) {
            Date date;
            String temp = list1;
            date = TimeUtil.parseUserTime(temp, null);
            LOGGER.info( temp + "->" + date);
        }
    }

    /**
     * Check the timestamp in milliseconds
     *
     * @throws Exception a serious problem
     */
    public void testTimeStampInMilliseconds() throws Exception
    {
        Date now = new Date();

        Date time = TimeUtil.parseUserTime("" + now.getTime(), null);

        assertEquals( "should parse milliseconds since epilog", now, time);
    }

    /**
     * Check the TODAY format
     *
     * @throws Exception a serious problem
     */
    public void testToday() throws Exception
    {

        TimeZone tz = TimeZone.getDefault();
        Date today = TimeUtil.parseUserTime(DateUtil.TYPE_TODAY, tz);

        LOGGER.info( "TODAY =" + today);

        GregorianCalendar gc = new GregorianCalendar( tz);
        gc.setTime( today);

        int hourOfDay = gc.get( Calendar.HOUR_OF_DAY);

        assertEquals( "start of day is the morning not midday", 0, hourOfDay);
    }

     /**
     * Check the TODAY format
     *
     * @throws Exception a serious problem
     */
    public void testWeekDays() throws Exception
    {
        TimeZone tz = TimeZone.getDefault();
        Date start = TimeUtil.parse("yyyy-MM-dd", "2015-11-01", tz);
        Date end = TimeUtil.parse("yyyy-MM-dd", "2015-11-06", tz);
        
        //working days
        double weekDays = (double) DateUtil.getWeekDaysBetween(tz, start, end);
        
        assertEquals( "should be 4 week days but found "+ weekDays, weekDays, 4.0);

    }
    
    /**
     * http://stackoverflow.com/questions/9581692/recommended-date-format-for-rest-get-api
     * @throws Exception 
     */
    public void testParseRestTS() throws Exception
    {
        String lists[][] = {
            {
                "2016-04-18T07:48:04Z",
                "18 April 2016 7:48:04@GMT",
                "2016-04-18T07:48:04.000Z",
                "2016-04-18T07:48:04Z",
                "20160418T074804Z",
            },
            {
                "2016-04-18T20:48:04Z",
                "18 April 2016 20:48:04@GMT",
                "2016-04-18T20:48:04.000Z",
                "2016-04-18T20:48:04Z",
                "20160418T204804Z",                
            },
            {
                "2016-04-18T08:15:57.000Z",
                "2016-04-18T08:15:57Z"
            },
            {
                "2016-04-18T08:15:57.197Z",
                "2016-04-18T08:15:57.197Z"
            }
                
        };

        for( String list[]:lists)
        {
            parseDate(list);
        }
    }
    
    /**
     * check parse of timezones.
     * @throws Exception a serious problem
     */
    public void testParseTimeZone() throws Exception
    {
        String list0[] = {
            "Sun, 06 Nov 1994 08:49:37 GMT", // RFC 822, updated by RFC 1123
//            "Sunday, 06-Nov-94 08:49:37 GMT", // RFC 850, obsoleted by RFC 1036
            "Sun Nov  6 08:49:37 1994", // ANSI C's asctime() format
        };

        parseDate(list0);
        
        String list1[] = {
            "8 Jan 1994 15:00@GMT-0500",
            "8 Jan 1994 15:00@GMT - 0500",
            "8 Jan 1994 15:00@GMT -0500",
            "8 Jan 1994 15:00@GMT -05:00",
            "8 Jan 1994 15:00@GMT-05:00",
            "8-Jan-1994 20:00@GMT",
            "8 Jan 1994 20:00"
        };

        parseDate(list1);

        String list2[] = {
            "Thu Apr  6 15:56:00 1994",
            "1994-04-06T15:56:00",
            "1994-04-06T15:56:00.000",
            "Thu Apr 6 15:56:00 1994",
            //"Thu Apr 06 10:56:00 EDT 1994", // EDT and EST both pointing to US time up there. is not working in java 1.5.0.07
            //"Thu Apr 07 01:56:00 EST 1994", // EDT and EST both pointing to US time up there.
            "06 Apr 1994 15:56:00",
            "06 Apr 1994 155600",
            "06-Apr-1994 15:56:00",
            "06-Apr-1994 155600",
            "06.Apr.1994 15:56:00",
            "06.Apr.1994 155600",
            "06 Apr 94 15:56:00",
            "06 Apr 94 155600",
            "06-Apr-94 15:56:00",
            "06-Apr-94 155600",
            "06.Apr.94 15:56:00",
            "06.Apr.94 155600",
            "06 April 1994 15:56:00",
            "06 April    1994 15:56:00",
            "06 April 1994 155600",
            "06-April-1994 15:56:00",
            "06-April-1994 155600",
            "06.April.1994 15:56:00",
            "06.April.1994 155600",
            "06 April 94 15:56:00",
            "06 April 94 155600",
            "06-April-94 15:56:00",
            "06-April-94 155600",

            "6 Apr 1994 15:56:00",
            "6 Apr 1994 155600",
            "6-Apr-1994 15:56:00",
            "6-Apr-1994 155600",
            "6 Apr 94 15:56:00",
            "6 Apr 94 155600",
            "6-Apr-94 15:56:00",
            "6-Apr-94 155600",
            "6 April 1994 15:56:00",
            "6 April 1994 155600",
            "6-April-1994 15:56:00",
            "6-April-1994 155600",
            "6 April 94 15:56:00",
            "6 April 94 155600",
            "6-April-94 15:56:00",
            "6-April-94 155600",

            "06 Apr 1994 15:56",
            "06 Apr 1994 1556",
            "06-Apr-1994 15:56",
            "06-Apr-1994 1556",
            "06 Apr 94 15:56",
            "06 Apr 94 1556",
            "06-Apr-94 15:56",
            "06-Apr-94 1556",
            "06 April 1994 15:56",
            "06 April 1994 1556",
            "06-April-1994 15:56",
            "06 Apri 1994 1556",
            "06-Apri-1994 15:56",
            "06-April-1994 1556",
            "06 April 94 15:56",
            "06 April 94 1556",
            "06-April-94 15:56",
            "06-April-94 1556",

            "6 Apr 1994 15:56",
            "6 Apr 1994 1556",
            "6-Apr-1994 15:56",
            "6-Apr-1994 1556",
            "6 Apr 94 15:56",
            "6 Apr 94 1556",
            "6-Apr-94 15:56",
            "6-Apr-94 1556",
            "6 April 1994 15:56",
            "6 April 1994 1556",
            "6-April-1994 15:56",
            "6-April-1994 1556",
            "6 April 94 15:56",
            "6 April 94 1556",
            "6-April-94 15:56",
            "6-April-94 1556",
        };
        parseDate(list2);

        String list3[] = {
            "06 April 1994",
            "06-April-1994",
            "06-April-94",
            "06.April.1994",
            "06.April.94",
            "06 April 94",
            "06 Apr 1994",
            "06-Apr-1994",
            "06-Apr-94",
            "06.Apr.1994",
            "06.Apr.94",
            "06 Apr 94",

            "6 April 1994",
            "6-April-1994",
            "6-April-94",
            "6 April 94",
            "6 Apri 1994",
            "6-Apri-1994",
            "6-Apri-94",
            "6 Apri 94",
            "6 Apr 1994",
            "6-Apr-1994",
            "6-Apr-94",
            "6 Apr 94",
            "06/April/1994",
            "06/April/94",
            "06/Apr/1994",
            "06/Apr/94",
            "6/April/1994",
            "6/April/94",
            "6/Apr/1994",
            "6/Apr/94",
        };
        parseDate(list3);

        String list4[] = {
            "15:43:00",
            "15:43"
        };
        parseDate(list4);
    }

    private void parseDate(String list[]) throws Exception
    {

        Date lastDate = null;

        for (String list1 : list) {
            Date date;
            String temp = list1;
            date = TimeUtil.parseUserTime(temp, null);
            if( lastDate != null)
            {
                assertEquals( "different result for " + temp, lastDate, date);
            }
            lastDate = date;
        }
    }
    
    public void testApplyDuration() throws Exception
    {
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        Date d1 = DateUtil.parse("yyyy-MM-dd", "2016-05-17");
        
        Date d2 = TimeUtil.addDurationToDate(d1, "2 year", gmt);
        
        Calendar c1 = Calendar.getInstance(gmt);
        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance(gmt);
        c2.setTime(d2);
        
        assertEquals("diff should be 2 years", 2, c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR));
    }
}
