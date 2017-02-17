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

import com.aspc.remote.html.internal.ColumnData;
import com.aspc.remote.html.scripts.HTMLEvent;
import com.aspc.remote.html.style.HTMLStyleSheet;
import com.aspc.remote.html.style.InlineStyleSheet;
import com.aspc.remote.html.style.InternalStyleSheet;
import com.aspc.remote.html.theme.HTMLTheme;
import com.aspc.remote.util.misc.StringUtilities;
import java.awt.Color;
import java.util.ArrayList;
import javax.annotation.Nullable;

/**
 *  HTMLTable
 *
 * <i>THREAD MODE: SINGLE-THREADED HTML generator component</i>
 *  @author      Nigel Leck
 *  @since       June 19, 1999, 9:22 PM
 */
public class HTMLTable extends HTMLContainer
{
    private final int version;
    
    /**
     * a table.
     */
    public HTMLTable()
    {
        this( 1);
    }    
    
    /**
     *
     * @param version the version
     */
    public HTMLTable(final int version)
    {
        this.version=version;
        if(version > 1)
        {
            appendClassName("table");
        }
        columns = new ArrayList<>();
        if( version>1)
        {
            cellSpacing = -1;
            cellPadding = -1;            
        }
        else
        {
            cellSpacing = 0;
            cellPadding = 0;
        }
        headerCellSpacing = -1;
        headerCellPadding = -1;
        scrollWidth = "";
        scrollHeight = "";
        selectedRow = -1;
        headerBorder = -1;
    }
    
    public int getVersion()
    {
        return version;
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
     * @param toolTip
     */
    public void setToolTip( String toolTip)
    {
        this.toolTip = toolTip;
    }

    /**
     *
     * @param row the row to use
     */
    public void setSelectedRow( int row)
    {
        selectedRow = row;
    }

    /**
     *
     * @param rules
     */
    public void setRules( String rules)
    {
        this.rules = rules;
    }

    /**
     *
     * @param pixels
     */
    public void setBorder( int pixels)
    {
        this.pixels = pixels;
    }

    /**
     *
     * @return the value
     */
    public int getBorder()
    {
        return pixels;
    }

    /**
     *
     * @param color
     */
    public void setBackGroundColor( Color color)
    {
        bgColor = color;
    }

    /**
     *
     * @param image
     */
    public void setBackGroundImage( String image)
    {
        bgImage = image;
    }

    /**
     *
     * @param color
     */
    public void setBorderColor( Color color)
    {
        borderColor = color;
    }

    /**
     *
     * @param on
     * @deprecated 
     */
    public void setHighlightOddRow( boolean on)
    {
        highlightOddRow = on;
    }

    /**
     *
     * @param flag
     */
    public void setHeaderAsFirstRow( final boolean flag)
    {
        setHeaderAsFirstRow( (flag == true ? 1 : 0));
    }

    /**
     *
     * @param rows
     */
    public void setHeaderAsFirstRow( final int rows)
    {
        headerAsFirstRow = rows;
    }

    /**
     *
     * @return the value
     */
    public int getHeaderRows( )
    {
        return headerAsFirstRow;
    }

    /**
     *
     * @param alignment
     */
    public void setTableAlignment( String alignment)
    {
        tableAlignment = alignment;
    }

    /**
     *
     * @param pixels
     */
    public void setCellSpacing( int pixels)
    {
        cellSpacing = pixels;
    }

    /**
     *
     * @return the value
     */
    public int getCellSpacing( )
    {
        return cellSpacing;
    }

    /**
     *
     * @param pixels
     */
    public void setCellPadding( int pixels)
    {
        cellPadding = pixels;
    }

    /**
     *
     * @return the value
     */
    public int getCellPadding( )
    {
        return cellPadding;
    }

    /**
     *
     * @param pixels
     */
    public void setHeaderCellSpacing( int pixels)
    {
        headerCellSpacing = pixels;
    }

    /**
     *
     * @param pixels
     */
    public void setHeaderBorder( int pixels)
    {
        headerBorder = pixels;
    }

    /**
     *
     * @param pixels
     */
    public void setHeaderCellPadding( int pixels)
    {
        headerCellPadding = pixels;
    }

    /**
     *
     * @param w
     */
    public void setWidth( String w)
    {
        width = w;
    }

    /**
     *
     * @return the value
     */
    public String getWidth( )
    {
        return width;
    }

    /**
     *
     * @param h
     */
    public void setHeight( String h)
    {
        height = h;
    }

    /**
     *
     * @param w
     */
    public void setScrollWidth( String w)
    {
        scrollWidth = w.trim();
    }

    /**
     *
     * @param h
     */
    public void setScrollHeight( String h)
    {
        scrollHeight = h.trim();
    }

    /**
     *
     * @param fg
     */
    public void setFixedHeaders( boolean fg)
    {
        fixedHeaders = fg;
    }

    /**
     *
     * @param fg
     */
    public void setCalculateWidth( boolean fg)
    {
        calculateWidth = fg;
    }

    /**
     *
     * @param caption
     */
    public void setCaption( HTMLText caption)
    {
        this.caption = caption;
        caption.setParent(this);
    }

    /**
     *
     * @param caption
     */
    public void setCaption( String caption)
    {
        setCaption( new HTMLText( caption));
    }

    /**
     *
     * @param text
     */
    public void setHeader( String text)
    {
        setHeader( new HTMLText( text));
    }

    /**
     *
     * @param header
     */
    public void setHeader( HTMLText header)
    {
        this.header = header;
    }

    /**
     *
     * @return the value
     */
    public HTMLComponent getHeader( )
    {
        return this.header;
    }

    /**
     *
     * @param row the row to use
     * @param col the column
     * @return the value
     */
    @Nullable
    public HTMLComponent getCell( int row, int col)
    {
        expandToFit(row);
        HTMLRow htmlRow;

        htmlRow = (HTMLRow)getComponent(row);
        if( htmlRow==null) return null;
        HTMLComponent component;

        component = htmlRow.getComponent( col);

        return component;
    }

    /**
     *
     * @return the value
     */
    public int getRows()
    {
         return getComponentCount();
    }

    /**
     *
     * @param row the row to use
     * @return the value
     */
    public HTMLRow getRow( int row)
    {
        return (HTMLRow)getComponent(row);
    }

    /**
     *
     * @param type the type
     * @param value the value
     * @param row the row to use
     */
    public void setRowStyleProperty( String type, String value, int row)
    {
       expandToFit(row);
       HTMLRow htmlRow;

       htmlRow = (HTMLRow)getComponent( row);
       assert htmlRow!=null;
       htmlRow.setStyleProperty( type, value);
    }

    /**
     *
     * @param type the type
     * @param value the value
     * @param row the row to use
     * @param col the column
     */
    public void setCellStyleProperty( String type, String value, int row, int col)
    {
       expandToFitWidth( col);
       expandToFit(row);
       HTMLRow htmlRow;

       htmlRow = (HTMLRow)getComponent( row);
       assert htmlRow!=null;
       htmlRow.setCellStyleProperty( type, value, col);
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
     *
     * @param bgcolor
     * @param row the row to use
     * @param col the column
     */
    public void setCellBackGroundColor( final Color bgcolor, final int row, final int col)
    {
        expandToFitWidth( col);
        expandToFit(row);
        HTMLRow htmlRow;

        htmlRow = (HTMLRow)getComponent( row);
        assert htmlRow!=null;
        htmlRow.setCellBackGroundColor(bgcolor, col);
    }

    /**
     *
     * @param bgcolor
     * @param row the row to use
     */
    public void setRowBackGroundColor( Color bgcolor, int row)
    {
        expandToFit(row);
        HTMLRow htmlRow;

        htmlRow = (HTMLRow)getComponent( row);
        assert htmlRow!=null;
        htmlRow.setBackGround(bgcolor);
    }

    /**
     * Set odd row color
     * @param c color
     */
    public void setOddRowColor(Color c)
    {
        oddRowColor = c;
    }

    /**
     *
     * @param url
     * @param row the row to use
     * @param col the column
     */
    public void setCellBackGroundImage( String url, int row, int col)
    {
        expandToFitWidth( col);
        expandToFit(row);
        HTMLRow htmlRow;

        htmlRow = (HTMLRow)getComponent( row);
        assert htmlRow!=null;

        htmlRow.setCellBackGroundImage(url, col);
    }

    /**
     * Sets the ID of a cell
     * @param ID
     * @param row the row to use
     * @param col the column
     */
    public void setCellID( String ID, int row, int col)
    {
        expandToFitWidth( col);
        expandToFit(row);
        HTMLRow htmlRow;

        htmlRow = (HTMLRow)getComponent( row);
        assert htmlRow!=null;
        htmlRow.setCellID(ID, col);
    }

    /**
     * Adds an event to a cell.
     * @param event the event
     * @param row the row to use
     * @param col the column
     */
    public void addCellEvent(HTMLEvent event, int row, int col)
    {
        expandToFitWidth( col);
        expandToFit(row);
        HTMLRow htmlRow;

        htmlRow = (HTMLRow)getComponent( row);
        assert htmlRow!=null;
        htmlRow.addCellEvent(event, col);
    }

    /**
     * Adds an event to the table.
     * @param event the event
     */
    public void addEvent(HTMLEvent event)
    {
        iAddEvent(event, "");
    }

    /**
     *
     * @param alignment
     * @param row the row to use
     * @param col the column
     */
    public void setCellAlignment( String alignment, int row, int col)
    {
        expandToFitWidth( col);
        expandToFit(row);
        HTMLRow htmlRow;

        htmlRow = (HTMLRow)getComponent( row);
        assert htmlRow!=null;
        htmlRow.setCellAlignment(alignment, col);
    }

    /**
     *
     * @param ss
     * @param row the row to use
     * @param col the column
     */
    public void setCellStyle( HTMLStyleSheet ss, int row, int col)
    {
        expandToFitWidth( col);
        expandToFit(row);
        HTMLRow htmlRow;

        htmlRow = (HTMLRow)getComponent( row);
        assert htmlRow!=null;

        htmlRow.setCellStyle(ss, col);
    }

    /**
     *
     * @param noWrap
     * @param row the row to use
     * @param col the column
     */
    public void setCellNoWrap( boolean noWrap, int row, int col)
    {
        expandToFitWidth( col);
        expandToFit(row);
        HTMLRow htmlRow;

        htmlRow = (HTMLRow)getComponent( row);
        assert htmlRow!=null;

        htmlRow.setCellNoWrap( noWrap, col);
    }

    /**
     *
     * @param span
     * @param row the row to use
     * @param col the column
     */
    public void setCellColSpan( int span, int row, int col)
    {
        expandToFitWidth( col);
        expandToFit(row);
        HTMLRow htmlRow;

        htmlRow = (HTMLRow)getComponent( row);
        assert htmlRow!=null;

        htmlRow.setCellColSpan(span, col);
    }

    /**
     *
     * @param width
     * @param row the row to use
     * @param col the column
     */
    public void setCellWidth( String width, int row, int col)
    {
        expandToFitWidth( col);
        expandToFit(row);
        HTMLRow htmlRow;

        htmlRow = (HTMLRow)getComponent( row);
        assert htmlRow!=null;

        htmlRow.setCellWidth(width, col);
    }

    /**
     *
     * @param height
     * @param row the row to use
     * @param col the column
     */
    public void setCellHeight( String height, int row, int col)
    {
        expandToFitWidth( col);
        expandToFit(row);
        HTMLRow htmlRow;

        htmlRow = (HTMLRow)getComponent( row);
        assert htmlRow!=null;

        htmlRow.setCellHeight(height, col);
    }

    /**
     *
     * @param alignment
     * @param row the row to use
     * @param col the column
     */
    public void setCellVerticalAlignment( String alignment, int row, int col)
    {
        expandToFitWidth( col);
        expandToFit(row);
        HTMLRow htmlRow;

        htmlRow = (HTMLRow)getComponent( row);
        assert htmlRow!=null;

        htmlRow.setCellVerticalAlignment (alignment, col);
    }

    /**
     *
     * @param height
     * @param row the row to use
     */
    public void setRowHeight( String height, int row)
    {
        expandToFit(row);
        HTMLRow htmlRow;

        htmlRow = (HTMLRow)getComponent( row);
        assert htmlRow!=null;

        htmlRow.setRowHeight(height);
    }

    /**
     *
     * @param span
     * @param row the row to use
     * @param col the column
     */
    public void setCellRowSpan( int span, int row, int col)
    {
        expandToFitWidth( col);
        expandToFit( row);
        HTMLRow htmlRow;

        htmlRow = (HTMLRow)getComponent( row);
        assert htmlRow!=null;

        htmlRow.setCellRowSpan(span, col);
    }

    /**
     *
     * @param tip
     * @param row the row to use
     * @param col the column
     */
    public void setCellToolTip( String tip, int row, int col)
    {
        expandToFitWidth( col);
        expandToFit(row);
        HTMLRow htmlRow;

        htmlRow = (HTMLRow)getComponent( row);
        assert htmlRow!=null;

        htmlRow.setCellToolTip(tip, col);
    }

    /**
     *
     * @param text
     * @param row the row to use
     * @param col the column
     */
    public void setCell( String text, int row, int col)
    {
        expandToFitWidth( col);
        expandToFit(row);
        HTMLText htmlText;

        htmlText = new HTMLText( text);

        setCell( htmlText, row, col);

    }

    /**
     *
     * @param o
     * @param row the row to use
     * @param col the column
     */
    public void setCellTag( Object o, int row, int col)
    {
        expandToFitWidth( col);
        expandToFit(row);
        HTMLRow htmlRow;

        htmlRow = (HTMLRow)getComponent( row);
        assert htmlRow!=null;

        htmlRow.setCellTag( o, col);
    }

    /**
     *
     * @param row the row to use
     * @param col the column
     * @return the value
     */
    public Object getCellTag( int row, int col)
    {
        HTMLRow htmlRow;

        htmlRow = (HTMLRow)getComponent( row);
        if( htmlRow == null)
        {
            return null;
        }

        return htmlRow.getCellTag( col);
    }

    /**
     *
     * @param text
     * @param row the row to use
     * @param col the column
     */
    public void insertCellInRow( String text, int row, int col)
    {
        HTMLText htmlText;

        htmlText = new HTMLText( text);

        insertCellInRow( htmlText, row, col);

    }


    /**
     *
     * @param cell
     * @param row the row to use
     * @param col the column
     */
    public void insertCellInRow( HTMLComponent cell, int row, int col)
    {
        expandToFit(row);

        HTMLRow htmlRow;
        htmlRow = (HTMLRow)iGetComponent( row);

        if( htmlRow==null) throw new IllegalArgumentException( "row " + row + " is out of range");
        
        htmlRow.insertCell( cell, col);

        if( htmlRow.iGetComponentCount() < maxColumn)
        {
            expandToFitWidth(htmlRow.iGetComponentCount());
        }
    }

    private void expandToFitWidth( int col)
    {
        if( col > maxColumn)
        {
            maxColumn = col;
        }
    }

    /**
     *
     * @return the value
     */
    public int getMaxColumn()
    {
        return maxColumn;
    }

    private void expandToFit( int row)
    {
        while( row >= iGetComponentCount())
        {
            addComponent( new HTMLRow( ));
        }
    }

    /**
     *
     * @param cell
     * @param row the row to use
     * @param col the column
     */
    public void setCell( HTMLComponent cell, int row, int col)
    {
        if( cell != null)
        {
            expandToFitWidth( col);
            expandToFit(row);
            HTMLRow htmlRow;
            htmlRow = (HTMLRow)iGetComponent( row);
            if( htmlRow == null) throw new IllegalArgumentException( "row: " + row + " out of range");
            htmlRow.setCell( cell, col);
        }
    }

    /**
     *
     * @param htmlRow
     */
    @Override
    public void addComponent( HTMLComponent htmlRow)
    {
        if( htmlRow instanceof HTMLRow)
        {
            expandToFitWidth( ((HTMLRow)htmlRow).getColumns()-1);
            super.addComponent(htmlRow);
        }
        else
        {
            throw new RuntimeException( "Can only add rows to tables");
        }
    }

    /**
     *
     * @param col the column
     * @param flag
     */
    public void setColumnHidden( int col, boolean flag)
    {
        expandToFitWidth( col);
        ColumnData cd;

        cd = findColumnData( col);

        cd.setHidden( flag);
    }

    /**
     *
     * @param col the column
     * @param width
     */
    public void setColumnWidth( int col, String width)
    {
        expandToFitWidth( col);
        ColumnData cd;

        cd = findColumnData( col);

        cd.setWidth( width);
    }

    /**
     *
     * @param col the column
     * @param toolTip
     */
    public void setColumnToolTip( int col, String toolTip)
    {
        expandToFitWidth( col);
        ColumnData cd;

        cd = findColumnData( col);

        cd.setToolTip( toolTip);
    }

    /**
     *
     * @param col the column
     * @param alignment
     */
    public void setColumnAlignment( int col, String alignment)
    {
        expandToFitWidth( col);
        ColumnData cd;

        cd = findColumnData( col);

        cd.setAlignment( alignment);
    }

    /**
     * TOP,BOTTOM or MIDDLE
     * @param col the column
     * @param verticalAlignment
     */
    public void setColumnVerticalAlignment( int col, String verticalAlignment)
    {
        expandToFitWidth( col);
        ColumnData cd;

        cd = findColumnData( col);

        cd.setVerticalAlignment( verticalAlignment);
    }


    /**
     *
     * @param col the column
     * @return the value
     */
    public ColumnData findColumnData( int col)
    {
        while( col >= columns.size())
        {
            columns.add( new ColumnData());
        }

        ColumnData cd;

        cd = columns.get(col);

        assert cd != null: "missing column " + col;

        return cd;
    }

    /**
     *
     */
    public void makeRowIDs()
    {
        int rows;
        rows = getComponentCount();

        for( int r = 0; r < rows; r++)
        {
            HTMLRow row;

            row = (HTMLRow)getComponent(r);
            assert row!=null;

            String tmpID = row.getId();

            if( StringUtilities.isBlank( tmpID))
            {
                String tID = getId();

                if( StringUtilities.isBlank(tID))
                {
                    HTMLPage page = getParentPage();

                    tID = "T";
                    if( page != null)
                    {
                        for( int loop = 0; StringUtilities.isBlank(page.getFlag( "TABLE:" + tID)) == false; loop++)
                        {
                            tID = "T"+ loop;
                        }

                        page.putFlag( "TABLE:" + tID, "used");
                    }

                }

                tmpID = tID + "_" + r;

                row.setId( tmpID);
            }
        }
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
        if( fixedHeadMainTable != null)
        {
            fixedHeadMainTable.iGenerate( browser, buffer);
        }
        else
        {
            if( getComponentCount() == 0) return; // no point producing a table if there are no rows

            buffer.append("<table");

            iGenerateAttributes(browser, buffer);

            if (rules != null)
            {
                buffer.append( " RULES=");
                buffer.append( rules);
            }

            /*if( bgImage != null)
            {
                buffer.append( " BACKGROUND='");
                buffer.append( bgImage);
                buffer.append( "'");
            }*/

            buffer.append(">\n");

            HTMLTheme theme = getTheme();

            if( caption != null)
            {
                InlineStyleSheet ss = new InlineStyleSheet();

                ss.setColour(
                    "color",
                    theme.getDefaultColor(
                        HTMLTheme.DKEY_TABLE_CAPTION_FONT_COLOR
                    )
                );
                ss.addElement(
                    InternalStyleSheet.STYLE_FONT_SIZE,
                    "" + theme.getDefaultInt(
                        HTMLTheme.DKEY_TABLE_CAPTION_SIZE
                    ) + "px"
                );

                caption.setBold(true);

                buffer.append( "<CAPTION ");
                ss.iGenerate( browser, buffer);
                buffer.append( ">");
                caption.iGenerate(browser, buffer);
                buffer.append( "</CAPTION>");
            }

            if( version < 2)
            {
                for( int r = 0; r < headerAsFirstRow; r++)
                {
                    HTMLRow row;

                    row = (HTMLRow)getComponent(r);
                    if( row == null) continue;
                    if( row.getBackGround() == null)
                    {
                        row.setBackGround(
                            theme.getDefaultColor(
                                HTMLTheme.DKEY_TABLE_HEAD_BG
                            )
                        );
                    }

                    row.getMutableTheme().setDefault(
                        HTMLTheme.DKEY_FONT_COLOR,
                        theme.getDefaultColor(
                            HTMLTheme.DKEY_TABLE_HEAD_FONT_COLOR
                        )
                    );

                    row.getMutableTheme().setDefault(
                        HTMLTheme.DKEY_FONT_BOLD,
                        "TRUE"
                    );

                    if( pixels < 1) pixels = 1;
                }
            }
            
            if( selectedRow != -1)
            {
                HTMLRow row;

                row = (HTMLRow)getComponent(selectedRow);
                assert row!=null;

                row.setBackGround(
                    getTheme().getDefaultColor(
                        HTMLTheme.DKEY_TABLE_HIGHLIGHT_ROW_COLOR
                    )
                );
            }

            if( header != null)
            {
                buffer.append("\n<TH>");
                header.iGenerate(browser, buffer);
                buffer.append("</TH>\n");
            }

            if( highlightOddRow == true)
            {
                int rows;
                rows = getComponentCount();

                for( int r = headerAsFirstRow; r < rows; r++)
                {
                    if( (r - headerAsFirstRow) % 2 != 0)
                    {
                        HTMLRow row;

                        row = (HTMLRow)getComponent(r);
                        assert row!=null;

                        if (row.getBackGround() == null)
                        {
                            if (oddRowColor == null)
                            {
                                row.appendClassName("sts-odd-row");
                                //row.setBackGround(
                                //    getTheme().getDefaultColor(
                                //       HTMLTheme.DKEY_TABLE_ODD_ROW_COLOR
                                //   )
                                //);
                            } else
                            {
                                row.setBackGround(oddRowColor);
                            }
                        }
                    }
                }
            }
            
            if( version>1 && headerAsFirstRow>0)
            {
                if( items != null)
                {
                    boolean endHead=false;
                    buffer.append("<thead>");
                    for( int i = 0; i < items.size(); i++)
                    {
                        HTMLComponent c;

                        c = (HTMLComponent)items.get(i);
                        
                        if( c instanceof HTMLRow )
                        {
                            HTMLRow r=(HTMLRow)c;
                            if( i < headerAsFirstRow)
                            {
                                r.setHeaderRow(true);
                            }
                            else
                            {
                                r.setHeaderRow(false);
                            }
                        }
                        if( i == headerAsFirstRow)
                        {
                            endHead=true;
                            buffer.append("</thead>\n");
                            buffer.append("<tbody>\n");
                        }

                        iGenerateComponent(c, browser, i, buffer);
                    }                    
                    
                    if( endHead==false)
                    {
                        buffer.append("</thead>\n");
                    }
                    else
                    {
                        buffer.append("</tbody>\n");
                    }
                }
            }
            else
            {
                super.iGenerate(browser, buffer);
            }

            buffer.append("</table>\n");
        }
    }

    /**
     *
     * @param browser
     * @param buffer
     */
    @Override
    protected void iGenerateAttributes(final ClientBrowser browser, final StringBuilder buffer)
    {
        if( fixedHeadMainTable != null)
        {
            return;
        }
        
        if( calculateWidth )
        {
            int totalWidth = 0;
            for( ColumnData cd: columns)
            {
                String w;

                w = cd.getWidth();

                if(
                    w != null &&
                    w.length() != 0 &&
                    w.indexOf('%') == -1
                )
                {
                    totalWidth += Integer.parseInt(w);
                }
            }
            width = "" + totalWidth;
        }

        if( width != null && width.trim().length() > 0)
        {
            buffer.append( " width=");
            buffer.append( width);
        }
        
        if(bgColor != null)
        {
            setStyleProperty("background-color", makeColorID(bgColor) + " !important");
        }
            
        if(version > 1)
        {
            if(pixels > 0)
            {
                appendClassName("table-bordered");
            }
            else
            {
                appendClassName("table-no-border");
            }
            
            if(width != null && width.trim().length() > 0)
            {
                String temp = width;
                try
                {
                    int i = Integer.parseInt(temp);
                    temp += "px";
                }
                catch(NumberFormatException e)
                {
                    //ignore
                }
                setStyleProperty("width", temp + " !important");
            }
        }

        super.iGenerateAttributes(browser, buffer);

        if( version < 2 || pixels > 0)
        {
            buffer.append( " border=");
            buffer.append( pixels);
        }
        
        if( pixels > 0)
        {
            if( borderColor == null && version < 2)
            {
                borderColor = getTheme().getDefaultColor(
                    HTMLTheme.DKEY_TABLE_BORDER_COLOR
                );
            }

            if (borderColor != null)
            {
                int c;
                c = borderColor.getRGB() & 0xffffff;
                String  t;

                t = "000000" + Integer.toHexString(c);

                t = t.substring(t.length() - 6);

                buffer.append( " BORDERCOLOR=#");
                buffer.append( t);

            }
        }

        if( tableAlignment != null)
        {
            buffer.append( " ALIGN=");
            buffer.append( tableAlignment);
        }

        if( cellSpacing != -1)
        {
            buffer.append( " cellspacing=");
            buffer.append( cellSpacing);
        }
        
        if( cellPadding != -1)
        {
            buffer.append( " cellpadding=");
            buffer.append( cellPadding);
        }
    }

    /**
     * This is the spot to put all your generation
     * of the HTML components. Do not put it in iGenerate()
     * @param browser
     */
    @Override
    protected void compile( final ClientBrowser browser)
    {
        if( StringUtilities.notBlank(bgImage))
        {
            String tmpImage=bgImage;
            if( tmpImage.contains("&amp;") == false)
            {
                tmpImage=tmpImage.replace("&", "&amp;");
            }
            else
            {
                assert false: "should not be encoded at this stage: " + tmpImage;
            }
            
            setStyleProperty("background-image", "url('" + tmpImage + "')");
        }
        
        if(
            fixedHeaders == false ||
            getHeaderRows() < 1
        )
        {
            super.compile( browser);
            return;
        }


        if( height != null)
        {
     //       if( browser.isBrowserIE() == false)
     //       {
                setStyleProperty("height", height);
     //       }
           // buffer.append( " HEIGHT=");
           // buffer.append( height);
        }

        HTMLPage   page;

        page = getParentPage();

        // Create ids to use for components
        String headerDivId = HTMLUtilities.makeValidName(getId()+ "_HD");
        String headerSubDivId = HTMLUtilities.makeValidName(getId()+ "_HD_SUB");
        String headerTableId = HTMLUtilities.makeValidName(getId() + "_TAB_HS") ;
        String internalDivId = HTMLUtilities.makeValidName(getId() + "_INT") ;
        String internalSubDivId = HTMLUtilities.makeValidName(getId() + "_INT_SUB") ;
        String internalTableId = HTMLUtilities.makeValidName(getId() +"_TAB_INT") ;

        // Create maintable to be returned which will contain the heading and
        // internal table
        fixedHeadMainTable = new HTMLTable();
        fixedHeadMainTable.setParent( page);
        fixedHeadMainTable.setWidth( getScrollWidth());

        // Create headers div and populate with headers
        HTMLDiv headerDiv = new HTMLDiv( headerDivId);
        headerDiv.setWidth( getScrollWidth());
        headerDiv.setOverflow( "hidden");

        // Create div to hold headings which we will allow control of width and
        // will move when scrolling
        HTMLDiv headerSubDiv = new HTMLDiv( headerSubDivId);
        headerSubDiv.setWidth( "10");
        headerSubDiv.setPosition( "relative");
        headerDiv.addComponent( headerSubDiv);

        // Create a HTML table to hold headings
        HTMLTable headerTable = new HTMLTable();
        headerTable.setId( headerTableId);
        headerTable.setWidth( "100%");
        headerTable.setHeaderAsFirstRow( getHeaderRows());


        // Set attributes such as border, etc if user has specified
        // otherwise let onload script sync these with internal table
        StringBuilder syncAttributes = new StringBuilder(200);

        if( headerBorder == -1)
        {
            syncAttributes.append("headingsTab.border = internalTab.border;\n");
        }
        else
        {
            headerTable.setBorder( headerBorder);
        }

        if( headerCellPadding == -1)
        {
            syncAttributes.append("headingsTab.cellPadding = internalTab.cellPadding;\n");
        }
        else
        {
            headerTable.setCellPadding( headerCellPadding);
        }

        if( headerCellSpacing == -1)
        {
            syncAttributes.append("headingsTab.cellSpacing = internalTab.cellSpacing;\n");
        }
        else
        {
            headerTable.setCellSpacing( headerCellSpacing);
        }

        // Move header rows from main table
        int copyRw=0;
        for( int hdR = 0; hdR < getHeaderRows(); hdR++)
        {
            headerTable.addComponent( getRow(copyRw));
            copyRw++;
        }

        // Add heading table to div
        headerSubDiv.addComponent( headerTable);


        // Create div with scrollbars
        HTMLDiv internalDiv = new HTMLDiv( internalDivId);
        // Set div to be width and height based on user settings
        // default to 400X100 if none supplied
        internalDiv.setWidth( getScrollWidth());
        internalDiv.setHeight( getScrollHeight());
        internalDiv.setOverflow( "auto");

        // Add script to scroll heading division when internal table division is
        // scrolled by the user
        internalDiv.addSrollingSync( headerSubDiv.getId(), HTMLDiv.SCROLLSYNC_METHOD_HORIZONTAL);

        // Create sub div to hold internal table so that we can control its width
        // This handles cases where the internal column widths are smaller then
        // corresponding heading column width
        HTMLDiv internalSubDiv = new HTMLDiv( internalSubDivId);
        if( width != null)
        {
            internalSubDiv.setWidth( width);
        }
        else
        {
            internalSubDiv.setWidth( "");
        }
        internalSubDiv.setPosition( "relative");
        internalDiv.addComponent( internalSubDiv);

        // Call plugin to return internal table
        HTMLTable internalTable =  new HTMLTable();
        internalTable.copyAttributes( this);
        internalTable.setId( internalTableId);
        internalTable.setWidth( "100%");
        internalTable.setHeaderAsFirstRow( 0);
        internalTable.setFixedHeaders( false);

        // Add internal table to division component
        internalSubDiv.addComponent( internalTable);

        // Move rest of rows from main table to internal table
        for( ; copyRw < getRows(); copyRw++)
        {
            internalTable.addComponent( getRow(copyRw));
        }

        int internalTableColumns = internalTable.getMaxColumn();
        int headerColumns = headerTable.getMaxColumn();

        // Create additional columns in header if more columns in internal table then
        // heading table

        if( internalTableColumns > headerColumns)
        {
            headerTable.setCell( "", 0, internalTableColumns);
        }
        else if( internalTableColumns < headerColumns)
        {
            internalTable.setCell( "", 0, headerColumns);
        }

        // Add heading div to first row of main table
        fixedHeadMainTable.setCell( headerDiv, 0, 0);

        // Add script to sync columns widths when page loads
        fixedHeadMainTable.setCell( internalDiv, 1, 0);

        String funcName = "doLoad_" + HTMLUtilities.makeValidName(getId());

        internalTableColumns = internalTable.getMaxColumn()+1;
        String onLoadScript = "function " + funcName + "()\n" +
                              "{\n" +
                                 "var internalDiv = findElement( '" + internalDivId + "');\n" +
                                 "var internalSubDiv = findElement( '" + internalSubDivId + "');\n" +
                                 "var headingsDiv = findElement( '" + headerDivId + "');\n" +
                                 "var headingsSubDiv = findElement( '" + headerSubDivId + "');\n" +
                                 "var internalTab = findElement('" + internalTableId + "');\n" +
                                 "var headingsTab = findElement('" + headerTableId + "');\n" +

                                 // new code by Venkat to clear cut-off issue in TimeSeries Tab
                                 "headingsDiv.style.width = internalDiv.offsetWidth + 'px';\n"+

                                 syncAttributes.toString() +
                                 "var internalCells = getTableColumns(internalTab);\n" +
                                 "var headingsCells = getTableColumns(headingsTab);\n" +
                                 "var cellWidths = new Array();\n" +
                                 "var totalCellWidths = 0;\n" +
                                 "var currWidth;\n" +
                                 "for( var x=0; x<" + internalTableColumns + "; x++)\n" +
                                 "{\n" +
                                 "    if( headingsCells[x].offsetWidth >= internalCells[x].offsetWidth)\n" +
                                 "    {\n" +
                                 "       currWidth = headingsCells[x].offsetWidth;\n" +
                                 "    }\n" +
                                 "    else\n" +
                                 "    {\n" +
                                 "       currWidth = internalCells[x].offsetWidth;\n" +
                                 "    }\n" +
                                 "    cellWidths[x] = currWidth;\n" +
                                 "    totalCellWidths += currWidth;\n" +
                                 "}\n" +
                                 "headingsSubDiv.style.width = totalCellWidths+(headingsTab.border*2);\n" +
                                 "internalSubDiv.style.width = totalCellWidths+(internalTab.border*2);\n" +
                                 "for( var x=0; x<" + internalTableColumns + "; x++)\n" +
                                 "{\n" +
                                 "    if( headingsCells[x].offsetWidth > cellWidths[x]);\n" +
                                 "    {\n" +
                                 "       headingsCells[x].width = removeBordersFromWidth( headingsCells[x], cellWidths[x], headingsTab);\n" +
                                 "    }\n" +
                                 "    if( internalCells[x].offsetWidth > cellWidths[x]);\n" +
                                 "    {\n" +
                                 "       internalCells[x].width = removeBordersFromWidth( internalCells[x], cellWidths[x], internalTab);\n" +
                                 "    }\n" +
                                 "}\n" +
                                 "for( var x=0; x<" + internalTableColumns + "; x++)\n" +
                                 "{\n" +
                                 "    if( headingsCells[x].offsetWidth != cellWidths[x]);\n" +
                                 "    {\n" +
                                 "       headingsCells[x].width = removeBordersFromWidth( headingsCells[x], cellWidths[x], headingsTab);\n" +
                                 "    }\n" +
                                 "    if( internalCells[x].offsetWidth != cellWidths[x]);\n" +
                                 "    {\n" +
                                 "       internalCells[x].width = removeBordersFromWidth( internalCells[x], cellWidths[x], internalTab);\n" +
                                 "    }\n" +
                                 "}\n" +
                                 "}";
        fixedHeadMainTable.addOnLoadScript( funcName + "_onload", "javascript:" + funcName + "();");
        fixedHeadMainTable.addJavaScript( funcName, onLoadScript);

        String adjustWidthFunction = "function removeBordersFromWidth( cell, width, table)\n" +
                                     "{\n" +
                                     "  var newwidth = width;\n" +

                                     "  if( cell != null)\n" +
                                     "  {\n" +
                                     "     if( cell.style.borderLeftWidth != null)\n" +
                                     "     {\n" +
                                     "        var i = cell.style.borderLeftWidth.indexOf('px');\n" +
                                     "        newwidth -= cell.style.borderLeftWidth.substring(0,i); \n" +
                                     "     }\n" +
                                     "     if( cell.style.borderRightWidth != null)\n" +
                                     "     { \n" +
                                     "         var i = cell.style.borderRightWidth.indexOf('px');\n" +
                                     "         newwidth -= cell.style.borderRightWidth.substring(0,i);\n" +
                                     "     }\n" +
                                     "     if( table.border != null)\n" +
                                     "     {\n" +
                                     "         newwidth -= table.border * 2;\n" +
                                     "     }\n" +
                                     "     if( table.cellPadding != null)\n" +
                                     "     {\n" +
                                     "         newwidth -= table.cellPadding * 2;\n" +
                                     "     }\n" +
                                     "  }\n" +
                                     "  return newwidth;\n" +
                                     "}";
       fixedHeadMainTable.addJavaScript( "removeBordersFromWidth", adjustWidthFunction);
       String getChildCell = "function getTableColumns( table)\n" +
                                     "{\n" +
                                     " var tBody = table.tBodies[0]; \n" +
                                     " var trs = tBody.childNodes;\n" +
                                     " var colList = new Array();\n" +
                                     " var pos=0;\n" +
                                     " for (var r=0; r<trs.length; r++) {\n" +
                                     "  if( trs[r].tagName == 'TR')\n" +
                                     "  {\n" +
                                     "      var tds = trs[r].childNodes;\n" +
                                     "      for (var c=0; c<tds.length; c++) {\n" +
                                     "        if( tds[c].tagName == 'TD')\n" +
                                     "        {\n" +
                                     "          colList[pos] = tds[c];\n" +
                                     "          pos++;\n" +
                                     "        }\n" +
                                     "     }\n" +
                                     "     return colList;\n" +
                                     "   }\n" +
                                     " }\n" +
                                     "}";
       fixedHeadMainTable.addJavaScript( "getTableColumns", getChildCell);
       fixedHeadMainTable.compile( browser);


    }

    /**
     *
     * @param table
     */
    protected void copyAttributes( HTMLTable table)
    {
        pixels = table.getBorder();
        selectedRow = table.selectedRow;
        maxColumn = table.maxColumn;
        cellSpacing = table.cellSpacing;
        cellPadding = table.cellPadding;
        calculateWidth = table.calculateWidth;
        highlightOddRow = table.highlightOddRow;
        width = table.width;
        height = table.height;
        tableAlignment = table.tableAlignment;
        rules= table.rules;
        bgImage = table.bgImage;
        borderColor = table.borderColor;
        columns = (ArrayList<ColumnData>)table.columns.clone();
        super.copyAttributes( table);
    }

    /**
     *
     * @return the value
     */
    protected String getScrollWidth()
    {
        String sw = "800";
        if( StringUtilities.isBlank( scrollWidth ) == false)
        {
            sw = scrollWidth;
        }
        else if( StringUtilities.isBlank( width ) == false)
        {
            sw = width;
        }
        return sw;
    }

    /**
     *
     * @return the value
     */
    protected String getScrollHeight()
    {
        String sh = "100";
        if( StringUtilities.isBlank( scrollHeight ) == false)
        {
            sh = scrollHeight;
        }
        else if( StringUtilities.isBlank( height ) == false)
        {
            sh = height;
        }
        return sh;
    }


    /**
     * @param row the row to use
     * @param col the column
     * @return bgcolor
     */
    public Color getCellBackGroundColor(final int row, final int col)
    {
        HTMLRow htmlRow;

        htmlRow = (HTMLRow)getComponent( row);
        if (htmlRow != null)
        {
            return htmlRow.getCellBackGroundColor(col);
        }
        return null;
    }


    protected int         pixels,
                        selectedRow,
                        maxColumn,
                        cellSpacing,
                        headerAsFirstRow,
                        cellPadding,
                        headerCellSpacing,
                        headerCellPadding,
                        headerBorder;

    protected boolean     calculateWidth;
    /**
     * @deprecated 
     */
    protected boolean     highlightOddRow;
    protected boolean     fixedHeaders;

    private HTMLText    caption,
                        header;

    protected ArrayList<ColumnData> columns;

    protected String      width,
                        height,
                        tableAlignment,
                        rules,
                        scrollWidth,
                        scrollHeight;

    protected String      bgImage;
    protected Color       borderColor,
                          oddRowColor;

    private HTMLTable   fixedHeadMainTable;
}
