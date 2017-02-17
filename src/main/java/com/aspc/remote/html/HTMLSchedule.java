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
package com.aspc.remote.html;

import org.apache.commons.logging.Log;
import com.aspc.remote.html.scripts.*;
import com.aspc.remote.html.style.*;
import com.aspc.remote.html.internal.*;
import com.aspc.remote.util.misc.*;
import java.util.*;
import java.text.*;
import java.awt.*;

/**
 *  HTMLColorPicker.java
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       January 9, 2001, 2:11 PM
 */
public class HTMLSchedule extends HTMLComponent
{
    /**
     * Creates new HTMLColorPicker
     * @param timeZone
     */

    public HTMLSchedule( TimeZone timeZone)
    {
        resources = new Hashtable();
        tasks = new Hashtable();
        taskPopups = new Hashtable();

        useWorkingHours = true;
        compressFmt = true;

        this.timeZone = timeZone;
        this.calendar = new GregorianCalendar( timeZone);
    }

    /**
     *
     * @param startDate
     * @param endDate
     */
    public void setDisplayDateRange( Date startDate, Date endDate)
    {

        if( startDate != null)
        {
            displayStartDate = makeDateStartOfDay( startDate);
        }

        if( endDate != null)
        {
            displayEndDate = makeDateStartOfDay( endDate);
        }
    }

    /**
     *
     * @return the value
     */
    public Date getDisplayStartDate( )
    {
        return displayStartDate;
    }

    /**
     *
     * @return the value
     */
    public Date getDisplayEndDate( )
    {
        return displayEndDate;
    }

    /**
     *
     * @return the value
     */
    public TimeZone getTimeZone()
    {
        return timeZone;
    }


    /**
     * Add a new task to be shown in the schedule
     * @param id
     * @param desc
     * @param resource
     * @param startDateTime
     * @param endDateTime
     * @param url
     * @throws Exception a serious problem
     */
    public void addTask( String id,//NOPMD
                         String desc,
                         String resource,
                         Date startDateTime,
                         Date endDateTime,//NOPMD
                         String url) throws Exception
    {
        if( id == null || StringUtilities.isBlank(id))
        {
            id = Integer.toString( tasks.size() + 1);
        }

        if( resource == null)
        {
            throw new Exception( "HTMLSchedult:addTask(" + id + ", " + desc + ", " + null + ", " + startDateTime + ", " + endDateTime + ") - Error: no resource");
        }


        // Validate date/times and convert to current timezone

        if( startDateTime == null)
        {
            throw new Exception( "A task cannot be added to the schedule that does not have a start time id:" + id);
        }

        if( endDateTime != null)
        {
            if( endDateTime.getTime() < startDateTime.getTime())
            {
                throw new Exception( "HTMLSchedult:addTask(" + id + ", " + desc + ", " + resource + ", " + startDateTime + ", " + endDateTime + ") - Error: end date cannot be before start date");
            }
        }
        else
        {
            endDateTime = startDateTime;
        }

        // Create a new task object and add to list
        SchedTask task = new SchedTask( id, desc, resource, startDateTime, endDateTime, url);
        tasks.put( task.getID(), task);

        // Maintain list of unique resources
        Integer row = (Integer)resources.get( resource);
        if( row == null)
        {
            row = resources.size();
            resources.put( resource, row);
        }
    }

    /**
     *
     * @param v the value
     */
    public void setShowOnlyWorkingHours( boolean v)
    {
        useWorkingHours = v;
    }


    /**
     *
     * @param v the value
     */
    public void setShowCompressedFmt( boolean v)
    {
        compressFmt = v;
    }

    /**
     *
     * @param browser
     */
    @Override
    protected void compile( ClientBrowser browser)
    {
        try
        {
            build();
        }
        catch( Exception e)
        {
            LOGGER.error( "compile", e);
        }

        super.compile( browser);
    }


    /**
     *
     */
    protected void build()
    {
        //HTMLPage page = getParentPage();

        // Determine statistics based on supplied tasks
        setUp();

        if( displayStartDate == null)
        {
            HTMLText txt = new HTMLText( "No date range specified.");
            txt.setColor( Color.red);
            LOGGER.error( "No date range specified for the schedule.");
            iAddComponent( txt);
            return;
        }

        // Construct main table used to display schedule
        HTMLTable mainTable = new HTMLTable();
        mainTable.setBorder(1);
        mainTable.setCellSpacing( 0);
        mainTable.setCellPadding( 2);


        //Date stDt,
        //     endDt;
        //int col,
        int row =0;

        // Add a column for each day and a column for each time segment within that day
        fillInGroupRow( mainTable, displayStartDate, displayEndDate, row, 1);
        row = dataRowOffset;

        // Add tasks to schedule
        Enumeration tasksEnum = tasks.keys();

        try
        {
            while( tasksEnum.hasMoreElements())
            {
                String taskId = (String)tasksEnum.nextElement();

                SchedTask task = (SchedTask)tasks.get( taskId);

                fillInTaskBar( mainTable, task);
            }
            iAddComponent( mainTable);
        }
        catch( Exception e)
        {
            HTMLText txt = new HTMLText( e.getMessage());
            txt.setColor( Color.red);
            LOGGER.error( "Error creating schedule.", e);
            iAddComponent( txt);
        }

    }


    /**
     *
     * @param mainTable
     * @param startDate
     * @param endDate
     * @param rowOffset
     * @param colOffset
     */
    protected void fillInGroupRow(
                    HTMLTable mainTable,
                    Date startDate,
                    Date endDate,
                    int rowOffset,
                    int colOffset )
    {

        // Format for dates on first row
        SimpleDateFormat df = new SimpleDateFormat( "EEE, MMM d, ''yy");//NOPMD
        df.setTimeZone( getTimeZone());

        int col = colOffset;
        int row = rowOffset;
        int dayCol = colOffset;

        Date currDt = startDate;

        Color headerColor = new Color( 0x999999);
        Color timeColor = new Color( 0xCCCCFF);

        // Set color of empty cell at top left
        mainTable.setCellBackGroundColor( headerColor, row, 0);
        mainTable.setCellBackGroundColor( timeColor, row+1, 0);

        // Create a column for each day and a column for each time segment within that day
        while( currDt.getTime() <= displayEndDate.getTime() &&
               currDt.getTime() <= endDate.getTime() )
        {
            // Create date string
            String dateDesc = df.format( currDt);

            // Add text to column and setup correct color and span enough columns to cover
            // a number of time segments as well as a number of half hour block within each time segment
            HTMLText dateTxt = new HTMLText( dateDesc);
            dateTxt.setNoWrap( true);

            mainTable.setCell( dateTxt, row, dayCol);
            mainTable.setCellColSpan( columnsPerDay, row, dayCol);
            mainTable.setCellBackGroundColor( headerColor, row, dayCol);

            // Add columns for time segments
            int  tmpStartHour,
                 tmpEndHour;

            tmpEndHour = timeSegmentStartHour;
            tmpStartHour = timeSegmentStartHour;

            if( compressFmt == true)
            {
                tmpEndHour = timeSegmentStartHour + (minsPerTimeSegment/60);
            }


            // Add time segments
            for( int i=0; i< timeSegmentsPerDay; i++)
            {
                // Create date string
                String timeDesc = "" + tmpStartHour;

                // Add text to cell and setup correct color and span enough columns to cover
                // a number of half hour block within each time segment
                HTMLText txt = new HTMLText( timeDesc);
                txt.setNoWrap( true);
                txt.setFontSize( 10);

                mainTable.setCell( txt, row + 1, col);
                mainTable.setCellBackGroundColor( timeColor, row + 1, col);
                mainTable.setCellColSpan( BREAKS_PER_TIME_SEGMENT, row + 1, col);
                mainTable.setCellAlignment( "center", row + 1, col);
                mainTable.setCellWidth( "40", row + 1, col);

                col++;

                tmpStartHour = tmpStartHour + (minsPerTimeSegment/60);
                if( compressFmt == true)
                {
                    tmpEndHour = tmpEndHour + (minsPerTimeSegment/60);
                }
            }

            // Process next day
            currDt = DateUtil.dateAdd( currDt, "Days", 1);
            dayCol++;
        }

        int maxDayCol = dayCol-1;

        HTMLText spc = new HTMLText( " ");
        spc.setNoWrap( true);
        spc.setFontSize( 1);

        row+=2;

        // Add a blank line with a border for each day
        mainTable.setCell( spc, row , 0);

        for( int i = 0; i < maxDayCol; i++)
        {
            mainTable.setCell( spc, row , i + colOffset);
            mainTable.setCellColSpan( columnsPerDay, row, i + colOffset);
        }

        row++;

        // Add list of resource to left hand side column
        Enumeration resourceEnum = resources.keys();

        Color evenColor = new Color( 0xCCCCCC);


        while( resourceEnum.hasMoreElements())
        {
            String resource = (String)resourceEnum.nextElement();
            Integer resOffset = (Integer)resources.get( resource);

            // Allow three rows for each resource
            int resRow = ( resOffset*3 ) + row;

            // Add the resource to the far left column
            HTMLText txt = new HTMLText( resource);
            txt.setNoWrap( true);
            mainTable.setCell( txt, resRow, 0);
            mainTable.setCellRowSpan( 3, resRow, 0);
            if( resRow % 2 != 0)
            {
                mainTable.setCellBackGroundColor( evenColor, resRow, 0);
            }


            // Add taskbar to each day for this resource
            for( int i = 0; i < maxDayCol; i++)
            {
                mainTable.setCell( spc, resRow, i+1);
                mainTable.setCellColSpan( columnsPerDay, resRow, i+1);
                mainTable.setCell( spc, resRow+2, i);
                mainTable.setCellColSpan( columnsPerDay, resRow+2, i);
                if( resRow % 2 != 0)
                {
                    mainTable.setCellBackGroundColor( evenColor, resRow, i+1);
                    mainTable.setCellBackGroundColor( evenColor, resRow+2, i);
                }

                // Fill in last column so that we get the borders on each row
                for( int x = 0; x < (columnsPerDay); x++)
                {
                    int c = x + ( i * columnsPerDay);
                    if( resRow % 2 != 0)
                    {
                        mainTable.setCellBackGroundColor( evenColor, resRow+1, c );
                    }

                    mainTable.setCell( spc, resRow+1, c);
                    mainTable.setCellWidth( "10", resRow+1, c);
                }
            }
        }
    }

    /**
     *
     * @param mainTable
     * @param task
     * @throws Exception a serious problem
     */
    protected void fillInTaskBar( HTMLTable mainTable, SchedTask task) throws Exception
    {

        Date startTime = task.getStartDateTime();
        Date endTime = task.getEndDateTime();

        // Validate date params
        if( startTime == null) return;
        if( endTime == null)
        {
            endTime = startTime;
        }

        /* Adjust task so that its start and end times are within the displayed days and times
         * If the both start and end time are outside the scope of the displayed days then
         * null is returned.
         * This needs to be done since it makes it easier to determine how to display the task
         * in cases such as when the start date is a date before the first date displayed, but
         * the end date is within the displayed date range.  This method will return the times
         * so that the start time would be the begin on the first day
         */
        Date fmtStartTime = adjustTaskTime( startTime, NEAREST_NEXT);
        Date fmtEndTime = adjustTaskTime( endTime, NEAREST_PREVIOUS);


        if( fmtStartTime == null ||
            fmtEndTime == null ||
            fmtStartTime.after( fmtEndTime)
          )
        {
            return;
        }


        Integer startCol = getRelativeColumn( fmtStartTime);
        Integer endCol = getRelativeColumn( fmtEndTime);


        // Find row based on resource
        //Enumeration resourceEnum = resources.keys();

        Integer resRow = (Integer)resources.get( task.getResource());
        if( resRow == null)
        {
            throw new RuntimeException( "Generate schedule - Error: Resource (" + task.getResource() + ") missing.");
        }

        int row = resRow;

        row*=3;
        row++;
        row+= dataRowOffset;


        //String id = task.getID();

        //int col = startCol.intValue();

        for( int x = startCol;
                 x <= endCol;
                 x++)
        {
            Vector cellIds = (Vector)mainTable.getCellTag( row, x);
            if( cellIds == null)
            {
                cellIds = new Vector();//NOPMD
            }
            cellIds.addElement( task.getID());

            int colorOffset = cellIds.size()-1;
            if( colorOffset >= taskColors.length)
            {
                colorOffset = taskColors.length -1;
            }


            Color curr = taskColors[colorOffset];
            mainTable.setCellBackGroundColor( curr, row, x);

            HTMLDiv div = createDiv( cellIds);

            HTMLMouseEvent me;
            me = new HTMLMouseEvent(
                        HTMLMouseEvent.onClickEvent,
                        "popup.mouseIn( '" + div.getId() + "', event)"
                        );
            mainTable.addCellEvent( me, row, x);

            mainTable.setCellTag( cellIds, row, x);
        }
    }

    /**
     *
     * @param taskIds
     * @return the value
     */
    protected HTMLDiv createDiv( Vector taskIds)
    {
        //HTMLPage page = getParentPage();

        String tmpID;

        tmpID = "DIV";
        ListIterator it;

        if( taskIds.size() > 1)
        {
            Collections.sort(
                taskIds,
                new Comparator()
                {
                    @Override
                    public int compare( Object o1, Object o2)
                    {
                        String      s1 = (String)o1,
                                    s2 = (String)o2;

                        return s1.compareToIgnoreCase( s2);
                    }
                }
            );
        }

        it = taskIds.listIterator();
        while( it.hasNext())
        {
            String taskId = (String)it.next();
            tmpID += "_" + taskId;
        }


        HTMLDiv div;
        div = (HTMLDiv)taskPopups.get( tmpID);
        if( div != null)
        {
            return div;
        }


        div = new HTMLDiv( tmpID);

        div.setWidth( "318");
        div.setHeight( "103");
        div.setOverflow( "scroll");

        /*HTMLMouseEvent me;
        me = new HTMLMouseEvent(
                HTMLMouseEvent.onMouseOverEvent,
                "popup.mouseIn( '" + id + "', event)"
                );

        div.addMouseEvent(me);

        me = new HTMLMouseEvent(
                HTMLMouseEvent.onMouseOutEvent,
                "popup.mouseOut( '" + id + "')"
                );
        div.addMouseEvent(me);
        */
        //div.setPosition( HTMLStyleSheet.POSITION_ABSOLUTE);
        HTMLTable table = new HTMLTable();
        table.setWidth( "300");

        SimpleDateFormat df = new SimpleDateFormat( "EEE, MMM d, ''yy HH:mm");//NOPMD
        df.setTimeZone( getTimeZone());


        it = taskIds.listIterator();

        int row = 1;
        HTMLTable infoTab = new HTMLTable();
        infoTab.setWidth("100%");

        HTMLAnchor btn2 = new HTMLAnchor( "javascript:setStyleVisible('" + tmpID + "', false)");
        btn2.addText( "Close");
        infoTab.setCell( btn2, 0, 1);
        infoTab.setCellAlignment( "right", 0, 1);
        table.setCell( infoTab, 0, 0);


        //table.setBorder(1);
        while( it.hasNext())
        {
            HTMLTable taskTable = new HTMLTable();
            taskTable.setBorder(1);
            taskTable.setWidth( "100%");

            String taskId = (String)it.next();
            SchedTask task = (SchedTask)tasks.get( taskId);

            String linkUrl = task.getURL();

            String taskDesc;
            taskDesc = "" + taskId;

            HTMLComponent comp;

            if( linkUrl != null &&
                linkUrl.equals( "") == false)
            {
                HTMLAnchor a = new HTMLAnchor( linkUrl);
                a.addText( taskDesc);
                comp = a;
            }
            else
            {
                HTMLText txt = new HTMLText( taskDesc);
                comp = txt;
            }

            // Task id table
            HTMLTable taskIdTable = new HTMLTable();
            taskIdTable.setWidth( "100%");

            taskIdTable.setCell( "Task :" , 0, 0);
            taskIdTable.setCell( comp , 0, 1);
            taskIdTable.setCellAlignment( "right" , 0, 1);
            taskIdTable.setCell( task.getDescription(), 1, 0);
            taskIdTable.setCellColSpan( 2, 1, 0);
            taskIdTable.setCellBackGroundColor(new Color( 0xCCCCFF), 1, 0);

            //taskIdTable.setCellBackGroundColor(Color.lightGray, row, 0);

            // Start and end time table
            HTMLTable timeTable = new HTMLTable();
            timeTable.setWidth( "100%");

            timeTable.setCell( "Start :", 0, 0);
            timeTable.setCell( df.format( task.getStartDateTime()), 0, 1);
            timeTable.setCellAlignment( "right", 0, 1);

            timeTable.setCell( "End :", 1, 0);
            timeTable.setCell( df.format( task.getEndDateTime()), 1, 1);
            timeTable.setCellAlignment( "right", 1, 1);

            taskTable.setCell( taskIdTable, 0, 0);
            taskTable.setCell( timeTable, 1, 0);

            //taskTable.setCell( task.getDescription(), 2, 0);
            //taskTable.setCellBackGroundColor(new Color( 0xCCCCFF), 2, 0);


            table.setCell( taskTable, row, 0);
            row++;
        }

        HTMLTable main = new HTMLTable();
        div.addComponent(main);

        table.setBackGroundColor(new Color( 0xf0f0f0));
        main.setBorder(1);
        main.setCell(table, 0, 0);

        div.setPosition(HTMLStyleSheet.POSITION_ABSOLUTE);
        div.setTop( 30);
        div.setVisible(false);
        div.setZ(1);

        iAddComponent( div);

        taskPopups.put( tmpID, div);

        return div;
    }

    /**
     * Adjusts the time so that it fits within the displayed days and times
     * @param dt
     * @param nearest
     * @return the value
     */
    protected Date adjustTaskTime( Date dt, int nearest)
    {


        //Double day = null;
        //Double time = null;

        // Determine
        Date dy = makeDateStartOfDay( dt);
        int days = (int)DateUtil.dateDiff( displayStartDate, dy, "Days");

        GregorianCalendar gc = getCalendar();
        gc.setTime( dt);

        Date res = null;

        if( days >= 0)
        {
            if( days < totalDays)
            {
                int hrs = gc.get( GregorianCalendar.HOUR_OF_DAY);

                if( hrs >= timeSegmentStartHour)
                {
                    if( hrs > timeSegmentEndHour)
                    {
                        if( nearest == NEAREST_NEXT)
                        {
                            if( days < totalDays)
                            {
                                gc.add( GregorianCalendar.DAY_OF_YEAR, 1);
                                gc.set( GregorianCalendar.HOUR_OF_DAY, timeSegmentStartHour);
                                gc.set( GregorianCalendar.MINUTE, 0);
                                gc.set( GregorianCalendar.SECOND, 0);
                                res = gc.getTime();
                            }
                        }
                        else if( nearest == NEAREST_PREVIOUS)
                        {
                            gc.set( GregorianCalendar.HOUR_OF_DAY, timeSegmentEndHour);
                            gc.set( GregorianCalendar.MINUTE, 0);
                            gc.set( GregorianCalendar.SECOND, 0);
                            res = gc.getTime();
                        }
                    }
                    else
                    {
                        res = gc.getTime();
                    }
                }
                else
                {
                    if( nearest == NEAREST_NEXT)
                    {
                        gc.set( GregorianCalendar.HOUR_OF_DAY, timeSegmentStartHour);
                        gc.set( GregorianCalendar.MINUTE, 0);
                        gc.set( GregorianCalendar.SECOND, 0);
                        res = gc.getTime();
                    }
                    else if( nearest == NEAREST_PREVIOUS)
                    {
                        if( days > 0)
                        {
                            gc.add( GregorianCalendar.DAY_OF_YEAR, -1);
                            gc.set( GregorianCalendar.HOUR_OF_DAY, timeSegmentEndHour);
                            gc.set( GregorianCalendar.MINUTE, 0);
                            gc.add( GregorianCalendar.MINUTE, -1);
                            gc.set( GregorianCalendar.SECOND, 0);
                            res = gc.getTime();
                        }
                    }
                }
            }
            else
            {
                if( nearest == NEAREST_PREVIOUS)
                {
                    gc.setTime( displayEndDate);
                    gc.set( GregorianCalendar.HOUR_OF_DAY, timeSegmentEndHour);
                    gc.set( GregorianCalendar.MINUTE, 0);
                    gc.set( GregorianCalendar.SECOND, 0);
                    gc.add( GregorianCalendar.MINUTE, -1);
                    res = gc.getTime();
                }
            }

        }
        else
        {
            if( nearest == NEAREST_NEXT)
            {
                gc.setTime( displayStartDate);
                gc.set( GregorianCalendar.HOUR_OF_DAY, timeSegmentStartHour);
                gc.set( GregorianCalendar.MINUTE, 0);
                gc.set( GregorianCalendar.SECOND, 0);
                res = gc.getTime();
            }
        }

        return res;
    }

    /**
     *
     * @param dt
     * @return the value
     */
    protected Integer getRelativeColumn( Date dt)
    {


        // Find major col

        Date dy = makeDateStartOfDay( dt);
        int days = (int)DateUtil.dateDiff( displayStartDate, dy, "Days");


        // Find Minor Col

        GregorianCalendar gc = getCalendar();
        gc.setTime( dt);
        int hrs = gc.get( GregorianCalendar.HOUR_OF_DAY);
        int mins = gc.get( GregorianCalendar.MINUTE);
        int totalMins = (hrs*60 ) + mins;
        int minsOffset = totalMins - (timeSegmentStartHour*60);

        int minorCol;
        minorCol = ( days * columnsPerDay);
        minorCol += (int)( Math.floor((double)minsOffset / (minsPerTimeSegment/(double)BREAKS_PER_TIME_SEGMENT)));


        return minorCol;
    }

    /**
     *
     */
    protected void setUp()
    {
        // Determine range of dates to shown on screen if not already set by user
        if( displayStartDate == null ||
            displayEndDate == null)
        {
            Date minTaskStartDate = null,
                 maxTaskEndDate = null;

            // Add tasks to schedule
            Enumeration tasksEnum = tasks.keys();

            while( tasksEnum.hasMoreElements())
            {
                String taskId = (String)tasksEnum.nextElement();

                SchedTask task = (SchedTask)tasks.get( taskId);

            //    String t = DateUtil.makeSybaseDate( task.getStartDateTime());
            //    String e = DateUtil.makeSybaseDate( task.getEndDateTime());
                // Maintain range of all tasks
                if( minTaskStartDate == null || task.getStartDateTime().getTime() < minTaskStartDate.getTime())
                {
                    minTaskStartDate = task.getStartDateTime();
                }

                if( maxTaskEndDate == null || task.getEndDateTime().getTime() > maxTaskEndDate.getTime())
                {
                    maxTaskEndDate = task.getEndDateTime();
                }
            }

            if( displayStartDate == null)
            {
                displayStartDate = makeDateStartOfDay( minTaskStartDate);
            }

            if( displayEndDate == null)
            {
                displayEndDate = makeDateStartOfDay( maxTaskEndDate);
            }
        }

        if( displayStartDate == null ||
            displayEndDate == null)
        {
            return;
        }

        // Allow only 2 months to be shown;

        Date maxDate;
        maxDate = DateUtil.dateAdd( displayStartDate, "Days", 60);
        if( displayEndDate.after( maxDate))
        {
            displayEndDate = maxDate;
        }


        // Determine break up of columns to represent time segments within each day

        if( useWorkingHours == true)
        {
            timeSegmentStartHour = 7;
            timeSegmentEndHour = 19;
        }
        else
        {
            timeSegmentStartHour = 0;
            timeSegmentEndHour = 24;
        }

        int hrsDisplayedPerDay = timeSegmentEndHour - timeSegmentStartHour;
        if( compressFmt == true)
        {
            minsPerTimeSegment = (hrsDisplayedPerDay / 4) * 60; // seperate into four segments
        }
        else
        {
            minsPerTimeSegment = 60; // 1 hour for each segement
        }
        timeSegmentsPerDay = (hrsDisplayedPerDay / (minsPerTimeSegment / 60));

        columnsPerDay = timeSegmentsPerDay * BREAKS_PER_TIME_SEGMENT;

        totalDays = (int)DateUtil.dateDiff( displayStartDate, displayEndDate, "Days");
        totalDays++;

        //dataColOffset = 1;
        dataRowOffset = 3;


    }


    private Date makeDateStartOfDay( Date dt)
    {
        GregorianCalendar gc = getCalendar();
        gc.setTime( dt);
        gc.set(Calendar.HOUR_OF_DAY, 0);
        gc.set(Calendar.MINUTE, 0);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);

        return gc.getTime();
    }

    private GregorianCalendar getCalendar()
    {
        return (GregorianCalendar)calendar.clone();
    }


    /**
     *
     */
    /**
     *
     */
    protected Date      displayStartDate,
                        displayEndDate;
    /**
     *
     */
    protected Hashtable resources;
    /**
     *
     */
    protected Hashtable tasks;
    /**
     *
     */
    protected Hashtable taskPopups;

    private   int       timeSegmentsPerDay,
                        minsPerTimeSegment,
                        timeSegmentStartHour,
                        timeSegmentEndHour,
                        columnsPerDay;

    private static final int  BREAKS_PER_TIME_SEGMENT = 6;

    private   boolean   useWorkingHours;
    private   boolean   compressFmt;

    int                 totalDays;
    private final       TimeZone timeZone;
    private final            GregorianCalendar calendar;


//    private   int       dataColOffset = 1;
    private   int       dataRowOffset = 2;

    private   static final Color[] taskColors = { new Color( 0, 0, 255),
                                            new Color( 125, 0 ,255),
                                            new Color( 255, 0, 255),
                                            new Color( 255, 0, 125),
                                            new Color( 255, 0, 0)
                                          };
    private   static final int NEAREST_NEXT = 1;
    private   static final int NEAREST_PREVIOUS = 2;

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.html.HTMLSchedule");//#LOGGER-NOPMD
}
