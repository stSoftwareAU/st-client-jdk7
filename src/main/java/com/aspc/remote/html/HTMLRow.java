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

import com.aspc.remote.html.internal.CellData;
import com.aspc.remote.html.internal.ColumnData;
import com.aspc.remote.html.internal.HandlesSingleClick;
import com.aspc.remote.html.scripts.HTMLEvent;
import com.aspc.remote.html.scripts.HTMLMouseEvent;
import com.aspc.remote.html.style.HTMLStyleSheet;
import com.aspc.remote.html.style.InlineStyleSheet;
import com.aspc.remote.util.misc.StringUtilities;
import java.awt.Color;
import java.util.*;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 *  HTMLRow
 *
 * <i>THREAD MODE: SINGLE-THREADED HTML generator component</i>
 *  @author      Nigel Leck
 *  @since       January 12, 1999, 9:22 PM
 */
public final class HTMLRow extends HTMLContainer
{
    /**
     *
     * @param headerRow if true then &lt;TH&gt; will be generated
     */
    public void setHeaderRow(boolean headerRow)
    {
        this.headerRow = headerRow;
    }

    /**
     * Gets the color when the mouse over the row.
     *
     * @return color the highlight color
     */
    public Color getHighlightColor()
    {
        return highlightColor;
    }

    /**
     * Sets the color when the mouse over the row.
     *
     * @param color the highlight color
     */
    public void setHighlightColor( int color)
    {
        highlightColor = new Color( color);
    }

    /**
     * Sets the color when the mouse over the row.
     *
     * @param color the highlight color
     */
    public void setHighlightColor( Color color)
    {
        highlightColor = color;
    }

    /**
     *
     * Change the cursor when mouse over this row
     * @param cursor
     */
    public void setCursor( String cursor)
    {
        setStyleProperty( InlineStyleSheet.STYLE_CURSOR, cursor);
    }

    /**
     * at compile time scan for HTMLAnchors and cancel the bubble. This is for
     * single click editing
     *
     * @param flag TRUE to cancel the bubble.
     */
    public void cancelSingleClickBubbleForAnchors( boolean flag)
    {
        cancelSingleClickBubbleForAnchors = flag;
    }

    /**
     *
     * @return the value
     */
    public int getColumns()
    {
        return this.getComponentCount();
    }

    /**
     *
     * @return the value
     */
    public String getToolTip()
    {
        return toolTip;
    }

    /**
     *
     * @param toolTip
     */
    public void setToolTip( String toolTip)
    {
        this.toolTip = toolTip;
    }

    /**
     *
     * @param parent
     */
    @Override
    public void setParent( HTMLComponent parent)
    {
        super.setParent( parent);


        if( parent != null && (parent instanceof HTMLTable ) == false)
        {
            throw new RuntimeException( "you must add rows to tables");
        }

        table = (HTMLTable)parent;
    }

    /**
     *
     * @param color
     */
    public void setBackGround( Color color)
    {
        bgColor = color;
    }

    /**
     *
     * @return the value
     */
    public Color getBackGround()
    {
        return bgColor;
    }

    /**
     *
     * @param alignment
     * @param col the column
     */
    public void setCellAlignment(final String alignment, final @Nonnegative int col)
    {
        assert col >=0: "must not be negative: " + col;
        assert alignment==null || "".equals( alignment) || "LEFT".equalsIgnoreCase(alignment) || "RIGHT".equalsIgnoreCase(alignment) || "CENTER".equalsIgnoreCase(alignment): "Invalid alignment: " + alignment;
        CellData cd;

        cd = findCellData( col, true);

        cd.alignment = alignment;
    }

    /**
     *
     * @param ss
     * @param col the column
     */
    public void setCellStyle(HTMLStyleSheet ss, int col)
    {
        CellData cd;

        cd = findCellData( col, true);

        cd.style = ss;
    }

    /**
     *
     * @param height
     * @param col the column
     */
    public void setCellHeight(String height, int col)
    {
        CellData cd;

        cd = findCellData( col, true);

        cd.height = height;
    }

    /**
     *
     * @param noWrap
     * @param col the column
     */
    public void setCellNoWrap( boolean noWrap, int col)
    {
        CellData cd;

        cd = findCellData( col, true);

        cd.setNoWrap(noWrap);
    }

    /**
     *
     * @param type the type
     * @param value the value
     * @param col the column
     */
    public void setCellStyleProperty( String type, String value, int col)
    {
       CellData cd;

        cd = findCellData( col, true);

        cd.setStyleProperty( type, value);
    }

    /**
     *
     * @param bgcolor
     * @param col the column
     */
    public void setCellBackGroundColor(Color bgcolor, int col)
    {
        CellData cd;

        cd = findCellData( col, true);

        cd.bgcolor = bgcolor;
    }

    /**
     *
     * @param col the column
     * @return the value
     */
    public Color getCellBackGroundColor( final int col)
    {
        CellData cd;

        cd = findCellData( col, true);

        return cd.bgcolor;
    }

    /**
     *
     * @param url
     * @param col the column
     */
    public void setCellBackGroundImage(String url, int col)
    {
        CellData cd;

        cd = findCellData( col, true);

        cd.imageURL = url;
    }

    /**
     *
     * @param span
     * @param col the column
     */
    public void setCellColSpan(int span, int col)
    {
        CellData cd;

        cd = findCellData( col, true);

        cd.span = span;
    }

    /**
     *
     * @param width
     * @param col the column
     */
    public void setCellWidth(String width, int col)
    {
        CellData cd;

        cd = findCellData( col, true);

        cd.width = width;
    }

    /**
     *
     * @param alignment
     * @param col the column
     */
    public void setCellVerticalAlignment(String alignment, int col)
    {
        CellData cd;

        cd = findCellData( col, true);
        if(table.getVersion() > 1)
        {
            cd.setStyleProperty("vertical-align", alignment);
        }

        cd.vAlignment = alignment;
        cd.setStyleProperty("vertical-align", alignment);
    }

    /**
     *
     * @return the value
     */
    public String getRowHeight()
    {
        return rowHeight;
    }

    /**
     *
     * @param height
     */
    public void setRowHeight(String height)
    {
        rowHeight = height;
    }


    /**
     *
     * @param span
     * @param col the column
     */
    public void setCellRowSpan(int span, int col)
    {
        CellData cd;

        cd = findCellData( col, true);

        cd.rowSpan = span;
    }

    /**
     *
     * Return the rowspan of the cell
     * @param col column
     * @return the value
     */
    public int getCellRowSpan(int col)
    {
         CellData cd;

         cd = findCellData( col, false);
         if(cd != null)
         {
             if (cd.rowSpan != null)
             {
                 return cd.rowSpan;
             }
         }
         return 1;
    }


    /**
     *
     * @param tip
     * @param col the column
     */
    public void setCellToolTip(final String tip, int col)
    {
        assert StringUtilities.assertIllegalCharactersHTML(tip);
        CellData cd;

        cd = findCellData( col, true);

        cd.toolTip = tip;
    }

    /**
     * Sets the ID for a particular cell. This may be useful for
     * drag and drop extra.
     * @param ID
     * @param col the column
     */
    public void setCellID(String ID, int col)
    {
        CellData cd;

        cd = findCellData( col, true);

        cd.id = ID;
    }

    /**
     * Stores data of any type against the cell.
     * This can be used to assign additional information to the cell
     * that is usefull in the generation of the table
     * @param data
     * @param col the column
     */
    public void setCellTag( Object data, int col)
    {
        CellData cd;

        cd = findCellData( col, true);

        cd.cellTag = data;
    }

    /**
     *
     * @param col the column
     * @return the value
     */
    public Object getCellTag( int col)
    {
        CellData cd;

        cd = findCellData( col, false);
        if( cd == null)
        {
            return cd;
        }

        return cd.cellTag;
    }

    /**
     *
     * @param event the event
     * @param script
     */
    public void addEvent(HTMLEvent event, String script)
    {
        iAddEvent( event, script);
    }

    /**
     *
     * @param event the event
     * @param col the column
     */
    public void addCellEvent(HTMLEvent event, int col)
    {
        CellData cd;

        cd = findCellData( col, true);

        if( cd.events == null)
        {
            cd.events = new Vector();//NOPMD
        }

        cd.events.addElement( event);
    }

    /**
     *
     * @param list
     */
    @Override
    protected void makeListOfEvents( List list)
    {
        super.makeListOfEvents( list);

        for(
            int col = 0;
            items != null && col < table.getMaxColumn();
            col++
        )
        {
            CellData cellData;

            cellData = findCellData( col, false);

            if( cellData != null)
            {
                if( cellData.events != null)
                {
                    for( int i = 0; i < cellData.events.size(); i++)
                    {
                        list.add(cellData.events.elementAt( i));
                    }
                }
            }
        }
    }


    private CellData findCellData( int col, boolean create)
    {
        if( CDList == null )
        {
            if( create == false)
            {
                return null;
            }

            CDList = new Vector();//NOPMD
        }

        CellData cd = null;

        if( col < CDList.size())
        {
            cd = (CellData)CDList.elementAt( col);
        }

        if( cd == null && create == true)
        {
            cd = new CellData();
            if( col >= CDList.size())
            {
                CDList.setSize( col + 1);
            }

            CDList.setElementAt( cd, col);
        }

        return cd;
    }

    /**
     *  Replaces the cell at the specified position.
     *  If the col is passed the end of the row then empty text components are
     *  added to fill in the empty columns.
     * @param cell
     * @param col the column
     */
    public void setCell( HTMLComponent cell, int col)
    {
        checkIsContainer();
        while( col >= items.size())
        {
            items.add( null);
        }

        items.set(col, cell);
        cell.setParent( this);
    }


    /**
     *  Inserts a new cell into a row forcing all existing cells after specified col to
     *  be moved one position to the right
     * @param cell
     * @param col the column
     */
    public void insertCell( HTMLComponent cell, int col)
    {
        checkIsContainer();
        if( col < items.size())
        {
            items.set(col, cell);
        }

        setCell( cell, col);
    }

    /**
     * get the ID of this component
     *
     * @return the ID
     */
    @Override
    public String getId()
    {
        return id;
    }

    /**
     * set the ID of this component.
     *
     * @param id The id of the component
     */
    public void setId( String id)
    {
        iSetId(id);
    }

    /**
     *
     * @return the value
     */
    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        return "HTMLRow: " + id;
    }

    /**
     *
     * @param type the type
     * @param value the value
     */
    @Override
    public void setStyleProperty( String type, String value)
    {
       super.setStyleProperty( type, value);
    }

    /**
     * I'll make the table a square.
     * @param browser
     */
    @Override
    protected void compile( ClientBrowser browser)
    {
        int skipped = 0;

        for(
            int col = 0;
            col < table.getMaxColumn();
            col++
        )
        {
            CellData cellData;

            cellData = findCellData( col, false);

            if( cellData != null)
            {
                if( cellData.span != null)
                {
                    skipped += cellData.span - 1;
                }
            }
        }

        int s = table.getMaxColumn() - skipped + 1;

        if( items == null)
        {
            items = new ArrayList();//NOPMD
        }

        while( items.size() < s)
        {
            items.add(null);
        }

        if(cancelSingleClickBubbleForAnchors)
        {
            for (HTMLComponent item : items) 
            {
                HTMLComponent cell = item;
                cancelBubbleOnComponent( cell);
            }
        }

        if( highlightColor != null)
        {
            if( StringUtilities.isBlank( id))
            {
                table.makeRowIDs();
            }

            HTMLMouseEvent me;

            me= new HTMLMouseEvent( HTMLMouseEvent.onMouseOverEvent, "setBGc( '" + id + "','" + makeColorID( highlightColor) + "')");

            addEvent( me, null);

            me= new HTMLMouseEvent( HTMLMouseEvent.onMouseOutEvent, "setBGc( '" + id + "',null)");

            addEvent( me, null);
        }

        super.compile(browser);
    }

    private boolean cancelBubbleOnComponent( HTMLComponent c)
    {
        if( c == null) return false;

        boolean changed = false;

        if( c instanceof HandlesSingleClick)
        {
            HandlesSingleClick a = (HandlesSingleClick)c;

            a.cancelClickBubble();
            changed = true;
        }
        else if( c.items != null)
        {
            for (HTMLComponent item : c.items) 
            {
                HTMLComponent cell = item;
                if( cancelBubbleOnComponent( cell))
                {
                    changed = true;
                }
            }
        }

        if( changed)
        {
            c.touch();
        }

        return changed;
    }

    /**
     * generate the raw HTML for this component.
     *
     * @param browser The target browser
     * @param buffer The generate HTML
     */
    @Override
    protected void iGenerate( final ClientBrowser browser, final StringBuilder buffer)
    {
        //TODO work on BS tables
        
      //  if( getComponentCount() == 0) return; // no point producing a row if there is not cell data

        buffer.append("<tr");
        iGenerateAttributes(browser, buffer);

        buffer.append( ">\n");

        super.iGenerate(browser, buffer);

        if( needsEndTags( browser))
        {
            buffer.append("</tr>\n");
        }
    }

    /**
     *
     * @param theItem
     * @param browser
     * @param col the column
     * @param buffer
     */
    @Override
    protected void iGenerateComponent(
        HTMLComponent theItem,
        ClientBrowser browser,
        int col,
        StringBuilder buffer
    )
    {
        ColumnData cd;
        cd = table.findColumnData( col);

        CellData cellData;

        cellData = findCellData(
            col,
            false
        );

        // If you set the Cell tool tip then
        // that takes presedence over others.
        String holdTip;

        holdTip = toolTip;
        if( cellData != null)
        {
            if( cellData.toolTip != null)
            {
                toolTip = null;
            }
        }

        if( headerRow)
        {
            buffer.append( "<th");
        }
        else
        {
            buffer.append( "<td");            
        }

        /**************** List Events *****************/
        if( cellData != null && cellData.events != null)
        {
            Hashtable tmpTable = new Hashtable();

            for( int i = 0; i < cellData.events.size();i++)
            {
                HTMLEvent event;

                event = (HTMLEvent)cellData.events.elementAt(i);
                String  call,
                        key;

                key = event.getName(browser).trim().toUpperCase();
                call = (String)tmpTable.get(key);

                if( call == null)
                {
                    call = event.getName(browser) + "=\"" + event.getCall();
                }
                else
                {
                    call += ";" + event.getCall();
                }

                tmpTable.put( key, call);
            }

            Enumeration e;
            e = tmpTable.elements();
            while( e.hasMoreElements())
            {
                buffer.append( " ");
                buffer.append( e.nextElement());
                buffer.append( "\"" );
            }
        }

        /************* End of list events *************/
        toolTip = holdTip;

        String cellAlignment = null;
        Color cellbgColor = null;

        boolean spanCol = false;
        boolean heightDone = false;

        StringBuilder styleBuffer=null;
        if( cellData != null)
        {
            if( cellData.style != null)
            {
                //buffer.append( " ");
                //buffer.append( cellData.style);
                styleBuffer=new StringBuilder( );
                styleBuffer.append(cellData.style);
            }

            if( cellData.styleProperty != null)
            {
                Object keys[] = cellData.styleProperty.keySet().toArray();
                if( keys.length > 0)
                {
                    if( styleBuffer == null)
                    {
                        styleBuffer=new StringBuilder( );
                    }
           //         else
           //         {
          //              LOGGER.info(styleBuffer);
          //          }
                    for (Object key : keys) 
                    {
                        String prop = (String) key;
                        String val = (String)cellData.styleProperty.get(prop);
                        styleBuffer.append(prop );
                        styleBuffer.append( ":");
                        styleBuffer.append( val);
                        styleBuffer.append( ";");
                    }
                    // buffer.append( "\"");
                    //iGenerateAttributes(browser, buffer);
                }
            }

            if( cellData.height != null)
            {
                heightDone = true;
                buffer.append( " height=\"");
                buffer.append( cellData.height);
                buffer.append( "\"");
            }

            if( cellData.id != null)
            {
                String tmpID;
                tmpID = cellData.id.toUpperCase();

                buffer.append( " id=");
                buffer.append( tmpID);
            }


            if( cellData.getNoWrap() != null && cellData.getNoWrap() == true)
            {
                buffer.append( " nowrap");
            }

            if( cellData.imageURL != null)
            {
                if( styleBuffer == null)
                {
                    styleBuffer=new StringBuilder( );
                }

                styleBuffer.append( "background-image: url('" ).append(cellData.imageURL.replace("&", "&amp;")).append("');");
            }

            cellAlignment = cellData.alignment;
            cellbgColor = cellData.bgcolor;

            if( cellData.span != null && cellData.span > 1)
            {
                spanCol = true;
                buffer.append( " colspan=");
                buffer.append( cellData.span);
            }

            if( cellData.rowSpan != null && cellData.rowSpan > 1)
            {
                buffer.append( " rowspan=");
                buffer.append( cellData.rowSpan);
            }
        }

        if(
            heightDone == false &&
            rowHeight != null &&
            rowHeight.equals( "") == false
        )
        {
            buffer.append( " height=\"");
            buffer.append( rowHeight);
            buffer.append( "\"");
        }

        boolean cellWidthSet;
        cellWidthSet = false;
        boolean cellVAlignmentSet;
        cellVAlignmentSet = false;

        if (cellData != null)
        {
            if (cellData.width != null)
            {
                buffer.append( " width=");
                buffer.append( cellData.width );
                cellWidthSet = true;
            }

            if (cellData.vAlignment != null)
            {
                buffer.append( " valign=");
                buffer.append( cellData.vAlignment );
                cellVAlignmentSet = true;
            }
        }

        if(
            spanCol == false &&
            cellWidthSet == false &&
            cd.getWidth().equals( "") == false
        )
        {
            buffer.append( " width=");
            if( cd.isHidden())
            {
                buffer.append( "\"0%\"");
            }
            else
            {
                buffer.append( "\"");
                buffer.append( cd.getWidth());
                buffer.append( "\"");
            }
        }

        if( cd.isHidden())
        {
            if( styleBuffer == null)
            {
                styleBuffer=new StringBuilder( );
            }
       //     else
       //     {
          //      if(styleBuffer.toString().trim().endsWith(";") ==false)
         //       {
        //            styleBuffer.append(";");
         //       }
        //    }
            styleBuffer.append( "border-left: 0; border-right: 0;");
        }

        if( cellAlignment == null)
        {
            cellAlignment = cd.getAlignment();
        }

        if( cellAlignment.equals( "") == false)
        {
            buffer.append( " align=");
            buffer.append( cellAlignment);
        }

        if( cellbgColor == null)
        {
            cellbgColor = cd.getBackGroundColor();
        }

        if( cellbgColor != null)
        {
            buffer.append( " bgcolor=\"#");
            int cc;
            cc = cellbgColor.getRGB() & 0xffffff;
            String  t;

            t = "000000" + Integer.toHexString(cc);

            t = t.substring(t.length() - 6);

            buffer.append( t);
            buffer.append( "\"");
        }

        if(
            cellVAlignmentSet == false &&
            cd.getVerticalAlignment().equals( "") == false
        )
        {
            buffer.append( " valign=");
            buffer.append( cd.getVerticalAlignment());
        }

        String cellTip = null;

        if( cellData != null)
        {
            cellTip = cellData.toolTip;
        }

        if( cellTip == null)
        {
            cellTip = cd.getToolTip();
        }

        if(StringUtilities.isBlank(cellTip) == false)
        {
            String tmpTip=cellTip;
            if( tmpTip.contains("\r"))
            {
                tmpTip=tmpTip.replace("\n\r", "\n");
                tmpTip=tmpTip.replace("\r", "");
            }
            buffer.append( " title=\"");
            buffer.append( StringUtilities.encodeHTML(tmpTip));
            buffer.append( "\"");
        }

        if( styleBuffer != null)
        {                        
            buffer.append( " style=\"");
            buffer.append(styleBuffer);
            buffer.append("\"");
        }
        
        if( theItem != null)
        {
            buffer.append( ">\n");
        }
        else
        {
            buffer.append( ">");
        }


        int orgLength = buffer.length();

        if( theItem != null)
        {
            theItem.iGenerate(browser, buffer);
        }

        if( buffer.length() == orgLength)
        {
            buffer.append( "&nbsp;");
        }

        if( needsEndTags( browser))
        {            
            if( headerRow)
            {
                buffer.append( "</th>");
            }
            else
            {
                buffer.append( "</td>");            
            }
        }
    }

    private boolean needsEndTags( final ClientBrowser browser)
    {
        return !browser.isBrowserNETSCAPE() || browser.getBrowserVersion() < 5;
    }

    private Color highlightColor;

    private boolean     headerRow;

    private boolean     cancelSingleClickBubbleForAnchors;//NOPMD

    private String      rowHeight;
    private Vector      CDList;
    private HTMLTable   table;
}
