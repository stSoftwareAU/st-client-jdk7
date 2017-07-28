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
package com.aspc.remote.jdbc.formatter;

import com.aspc.remote.html.HTMLPage;
import com.aspc.remote.html.HTMLTable;
import com.aspc.remote.html.HTMLText;
import com.aspc.remote.jdbc.SoapResultSet;
import com.aspc.remote.util.misc.CLogger;
import java.awt.Color;
import javax.annotation.*;
import org.apache.commons.logging.Log;

/**
 *   SoapResultSet Formatter
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED Servlet</i>
 *
 *  @author      Ronit
 *  @since       05 Jan 2011
 */
public final class PagedResultSetFormatter
{
    /**
     * Format the given result set to given page
     *
     * @param r A SoapResultSet
     * @param page A HTMLPage
     * @param startRow the start row
     * @param pageSize the page size
     * @throws Exception A serious problem occurs
     */
    @SuppressWarnings("null")
    public static void make(final @Nonnull SoapResultSet r, final HTMLPage page, final int startRow, final int pageSize) throws Exception
    {
        HTMLTable table = new HTMLTable();

        HTMLTable main = new HTMLTable();
        main.setCell( table, 0,0);
        main.setBorder(1);
        main.setWidth("100%");

        table.setHeaderAsFirstRow( true);
        table.setHighlightOddRow( true);
        table.setWidth("100%");

        int columns = r.getColumnCount();
        for( int col = 1; col <= columns; col++)
        {
            table.setColumnVerticalAlignment( col - 1, "top");
            table.setCell( r.getColumnName( col), 0, col -1);
            //set left border and grey color.
            if(col>1)
            {
                table.setCellStyleProperty("border-left", "1px solid #adaa9c", 0, col-1);
            }
        }
        boolean hasFormatHTML =false;
//        assert r!=null: "result set should not be null";
        if( r != null)
        {
            r.setCurrentRow(startRow);
            int row;
            for( row = 1; r.next() && row <= pageSize; row++)
            {
                Object[] att = r.getRowAttributeNames();

                if(att != null)
                {
                    for (Object att1 : att) {
                        String key = (String) att1;
                        if(key.equalsIgnoreCase("format"))
                        {
                            String value = r.getRowAttributevValue(key);
                            if(value.equalsIgnoreCase("HTML"))
                            {
                                HTMLText text =new HTMLText(r.getString(1), false);

                                HTMLTable formatTable = new HTMLTable();
                                formatTable.setCell(text,0,0);
                                formatTable.setCellAlignment( "Center", 0, 0);
                                formatTable.setWidth( "80%");

                                page.addComponent(formatTable);
                                hasFormatHTML = true;
                                break;
                            }
                        }
                        if (key.equalsIgnoreCase("style")) {
                            String value = r.getRowAttributevValue(key);
                            String[] styles = value.split(";");
                            for (String style : styles) {
                                String[] properties = style.split(":");
                                table.setRowStyleProperty(properties[0], properties[1], row);
                            }
                        }
                    }
                }

                if(hasFormatHTML)
                {
                    break;
                }

                for( int col = 1; col <= columns; col++)
                {
                    HTMLText t = new HTMLText( r.getString( col));
                    t.setFont( "courier");
                    t.setIndent( true);

                    att = r.getAttributeNames(col);
                    if(att != null)
                    {
                        for (Object att1 : att) {
                            String key = (String) att1;
                            if(key.equalsIgnoreCase("format"))
                            {
                                String value = r.getAttributeValue(col, key);
                                if(value.equalsIgnoreCase("HTML"))
                                {
                                    t.setText(r.getString(col), false);
                                }
                            }
                            if (key.equalsIgnoreCase("style")) {
                                String value = r.getAttributeValue(col, key);
                                String[] styles = value.split(";");
                                for (String style : styles) {
                                    String[] properties = style.split(":");
                                    table.setCellStyleProperty(properties[0], properties[1], row, col-1);
                                }
                            }
                            if(key.equalsIgnoreCase("tooltip"))
                            {
                                String value = r.getAttributeValue(col,key);
                                table.setCellToolTip(value, row, col-1);
                            }
                        }
                    }

                    table.setCell( t, row, col -1);
                    if(col>1)
                    {
                        table.setCellStyleProperty("border-left", "1px solid #adaa9c", row, col-1);
                    }
                }
            }

            if (row > 1)
            {
                page.putFlag("hasResults", "true");
                HTMLText count = new HTMLText("Displaying Rerocrds # " + startRow + " - " + (startRow + row-2));
                count.setBold(true);
                count.setColor(Color.BLUE);
                page.addComponent(count);

                if (row <= pageSize)
                {
                    page.putFlag("isLastPage", "true");
                }
                else
                {
                    page.putFlag("isLastPage", "flase");
                }

                if(hasFormatHTML == false)
                {
                    page.addComponent( main);
                }
            }
            else if (row ==1)
            {
                HTMLText noRecords = new HTMLText("No records have been found. Please change your search criteria and try again. ");
                noRecords.setBold(true);
                noRecords.setColor(Color.RED);
                page.addComponent(noRecords);
                page.putFlag("hasResults", "false");
            }
        }
    }


    /** Creates a new instance of ResultSetFormatter */
    private PagedResultSetFormatter()
    {
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.jdbc.formatter.PagedResultSetFormatter");//#LOGGER-NOPMD
}
