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

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.DateUtil;
import com.aspc.remote.util.misc.TimeUtil;
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
 *  test date utilities
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author         Jason McGrath
 *  @since          November 23, 2005
 */
public class TestDateUtil extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.selftest.TestDateUtil");//#LOGGER-NOPMD

    /**
     * Creates new VirtualDBTestUnit
     * @param name The name of the test unit
     */
    public TestDateUtil(String name)
    {
        super( name);
    }

    /**
     * @param args  */
    public static void main(String[] args)
    {
        Test t=suite();
//        t=TestSuite.createTest(TestDateUtil.class, "testWeekEndSpecificTime");
        TestRunner.run(t);
    }

    /**
     * @return the value
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestDateUtil.class);
        return suite;
    }

    /**
     *
     * @throws Exception a serious problem
     */
    public void testValidDateParse() throws Exception
    {
        TimeZone tz = TimeZone.getTimeZone("Europe/Moscow");

        String dates[] = {
            "1994-01-08",
            "8 Jan 1994",
            "8-Jan-1994",
            "8/Jan/1994",
            "08 Jan 1994",
            " 08  Jan  1994 ",
            "08 JANUARY 1994 ",
            "08 JAN 94 ",
            "8-Jan-1994@Europe/Moscow"

        };

        Date last = null;

        for (String date : dates) {
            Date d = DateUtil.parseUserDate(date, tz, false);
            if (last != null) {
                assertEquals("Mismatch on " + date, last, d);
            }
            last = d;
        }
    }

    /**
     * Sunday mornings we are getting the wrong date.
     */
    public void testWeekEndSpecificTime()
    {
        Date now = new Date(1443299206348L);

        Date weekEnd = DateUtil.weekEnd(now, TimeZone.getTimeZone("Australia/Sydney"));

        LOGGER.info( "week end: " + weekEnd);
        Date weekEnd2 = DateUtil.weekEnd(weekEnd, TimeZone.getTimeZone("Australia/Sydney"));

        assertEquals( "sunday is sunday", weekEnd, weekEnd2);
    }
    
    /**
     * Sunday mornings we are getting the wrong date.
     */
    public void testWeekEndSpecificTime2()
    {
        Date now = new Date(1448784828609L);

        Date weekEnd = DateUtil.weekEnd(now, TimeZone.getTimeZone("Australia/Sydney"));

        LOGGER.info( "week end: " + weekEnd);
        if( weekEnd.getTime()>1448784828609L)
        {
            fail( "should be 29 Nov 2015 was " + weekEnd);
        }
    }

    /**
     * Sunday mornings we are getting the wrong date.
     */
    public void testWeekEnd()
    {
        GregorianCalendar gc = new GregorianCalendar( DateUtil.GMT_ZONE);

        gc.set( Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        gc.set(Calendar.HOUR_OF_DAY, 10);
        gc.set(Calendar.MINUTE, 0);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);

        Date checkTime=gc.getTime();

        Date weekEnd = DateUtil.weekEnd(checkTime, TimeZone.getTimeZone("Australia/Sydney"));
        gc.set(Calendar.HOUR_OF_DAY, 0);
        //gc.add( Calendar.DATE, 7);
        Date checkDT= gc.getTime();
        if( checkDT.getTime() != weekEnd.getTime())
        {
            fail( "Calculated wrong " + checkDT + " != " + weekEnd);
        }
    }

    /**
     * Check that we get invalid exceptions
     *
     * @throws Exception a serious problem
     */
    @SuppressWarnings("empty-statement")
    public void testInvalidDateParse() throws Exception
    {
        TimeZone tz = TimeZone.getTimeZone("Europe/Moscow");

        String dates[] = {
            "28",
            "8-Jan",
            "911",
            "9/11"
        };

        for (String temp : dates) {
            try
            {
                DateUtil.parseUserDate(temp, tz, false);

                fail( "Should not been able to parse " + temp);
            }
            catch( Exception e)
            {
                ;// Expected
            }
        }
    }

    /**
     *
     * @throws Exception a serious problem
     */
    @SuppressWarnings("empty-statement")
    public void testConvertTimeIntoDate() throws Exception
    {

        Date dtA = DateUtil.parseUserDate("8 Jan 1994 18:12:09", TimeZone.getTimeZone("GMT"), false);
        Date dtB = DateUtil.parseUserDate("8 Jan 1994", TimeZone.getTimeZone("GMT"), false);

        if( dtA.equals( dtB) == false)
        {
            String temp = "dtA=" + dtA + ", dtB=" + dtB;

            fail( temp);
        }

        try
        {
            //Date dtC =
            DateUtil.parseUserDate("8 Jan XXXX 18:12:09", TimeZone.getTimeZone("GMT"), false);
            fail( "Date '8 Jan XXXX 18:12:09' should have been invalid ");
        }
        catch( Exception e)
        {
            ;
        }
    }

    /**
     *
     * @throws Exception a serious problem
     */
    public void testDateDiffWithDaylightSavingTime() throws Exception
    {
        TimeZone tz = TimeZone.getTimeZone("Australia/Sydney");
        //start date is not using daylight saving time
        Calendar c1 = TimeUtil.getGC(tz);
        c1.set(Calendar.YEAR, 2008);
        c1.set(Calendar.MONTH, 9);
        c1.set(Calendar.DAY_OF_MONTH, 3);
        c1.set(Calendar.HOUR_OF_DAY, 15);
        c1.set(Calendar.MINUTE, 10);
        c1.set(Calendar.SECOND, 20);

        //end date is not using daylight saving time
        Calendar c2 = TimeUtil.getGC(tz);
        c2.set(Calendar.YEAR, 2008);
        c2.set(Calendar.MONTH, 9);
        c2.set(Calendar.DAY_OF_MONTH, 4);
        c2.set(Calendar.HOUR_OF_DAY, 15);
        c2.set(Calendar.MINUTE, 10);
        c2.set(Calendar.SECOND, 20);
        assertEquals("should return 1", 1, DateUtil.dateDiff(c1.getTime(), c2.getTime(), "days", tz, tz));

        //end date is using daylight saving time
        c2 = TimeUtil.getGC(tz);
        c2.set(Calendar.YEAR, 2008);
        c2.set(Calendar.MONTH, 9);
        c2.set(Calendar.DAY_OF_MONTH, 5);
        c2.set(Calendar.HOUR_OF_DAY, 15);
        c2.set(Calendar.MINUTE, 10);
        c2.set(Calendar.SECOND, 20);
        assertEquals("should return 2", 2, DateUtil.dateDiff(c1.getTime(), c2.getTime(), "days", tz, tz));

        //start date is using daylight saving time
        c1.set(Calendar.YEAR, 2008);
        c1.set(Calendar.MONTH, 3);
        c1.set(Calendar.DAY_OF_MONTH, 4);
        c1.set(Calendar.HOUR_OF_DAY, 15);
        c1.set(Calendar.MINUTE, 10);
        c1.set(Calendar.SECOND, 20);

        //end date is using daylight saving time
        c2.set(Calendar.YEAR, 2008);
        c2.set(Calendar.MONTH, 3);
        c2.set(Calendar.DAY_OF_MONTH, 5);
        c2.set(Calendar.HOUR_OF_DAY, 15);
        c2.set(Calendar.MINUTE, 10);
        c2.set(Calendar.SECOND, 20);
        assertEquals("should return 1", 1, DateUtil.dateDiff(c1.getTime(), c2.getTime(), "days", tz, tz));

        //end date is not using daylight saving time
        c2.set(Calendar.YEAR, 2008);
        c2.set(Calendar.MONTH, 3);
        c2.set(Calendar.DAY_OF_MONTH, 7);
        c2.set(Calendar.HOUR_OF_DAY, 14);
        c2.set(Calendar.MINUTE, 30);
        c2.set(Calendar.SECOND, 20);
        assertEquals("should be 72 hours", 72, ((c2.getTimeInMillis() - c1.getTimeInMillis())/(3600*1000)));
        assertEquals("should be 2 days", 2, DateUtil.dateDiff(c1.getTime(), c2.getTime(), "days", tz, tz));
    }

}
