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
package com.aspc.remote.application.table;

import org.apache.commons.logging.Log;
import javax.swing.table.*;
import java.util.*;
import com.aspc.remote.util.misc.*;

/**
 *  CTableModel
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       June 19, 1998, 4:43 PM
 */
public class CTableModel extends AbstractTableModel
{
    int rowCount;

    Hashtable data;
    /**
     *
     */
    protected boolean extendable;
    private Vector columns;

    /**
     * 
     * @param mode 
     * @param cols 
     */
    public CTableModel(String mode, String cols)
    {
        data = new Hashtable();
        rowCount = 0;
        setMode( mode);//NOPMD
        setCols( cols);//NOPMD
    }

    // These methods always need to be implemented.
    /**
     * 
     * @return the value
     */
    @Override
    public int getColumnCount()
    {
        if( columns == null) return -1;

        return columns.size();
    }

    // The default implementations of these methods in
    // AbstractTableModel would work, but we can refine them.
    /**
     * 
     * @param column 
     * @return the value
     */
    @Override
    public String getColumnName(int column)
    {
        if( column >= columns.size()) return null;
        
        Column col;

        col = (Column)columns.elementAt(column);

        return col.name;
    }

    /**
     * 
     * @param row the row to use
     * @param column 
     * @return the value
     */
    @Override
    public boolean isCellEditable(int row, int column)
    {
        Column col;

        col = (Column)columns.elementAt(column);

        return col.editable;
    }


    /**
     * 
     * @param cols 
     */
    public void setCols( String cols)
    {
        StringTokenizer st = new StringTokenizer( cols, "|", true);
        columns = new Vector();//NOPMD

        for( int i = 0; st.hasMoreTokens(); i++)//NOPMD
        {
            String t;
            t = st.nextToken();
            
            if( t.equals( "|"))
            {
                t = "";
            }
            else
            {
                if( st.hasMoreTokens())
                {
                    st.nextToken();
                }
            }
            String n = "",
                   v = "";
 
            StringTokenizer st2 = new StringTokenizer( t, "=");

            if( st2.hasMoreTokens())
            {
                n = st2.nextToken();
                if( st2.hasMoreTokens())
                {
                    v = st2.nextToken();
                }
            }
            
            Column col = new Column();

            col.name = n;
            col.editable = true;
            if( v.contains("k"))
            {
                col.key = true;
            }

            if( v.contains("o"))
            {
                col.editable = false;
            }

            columns.addElement(col);
        }
    }

    /**
     * 
     * @param mode 
     */
    public void setMode( String mode)
    {
        extendable = false;

        if( mode.contains("EXTEND"))
        {
            extendable = true;
        }
    }

    /**
     * 
     * @param c 
     * @return the value
     */
    @Override
    public Class getColumnClass(int c)
    {
        Object o;

        o = getValueAt(0, c);
        if( o != null)
        {
            o.getClass();
        }

        return super.getColumnClass( c);
    }

    /**
     * 
     * @return the value
     */
    @Override
    public int getRowCount()
    {
        if( extendable == true ) return rowCount + 1;

        return rowCount;
    }

    /**
     * 
     * @param row the row to use
     * @param col the column
     * @return the value
     */
    protected String makeKey( int row, int col)
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append( row);
        buffer.append( "-");
        buffer.append( col);

        return buffer.toString();
    }

    /**
     * 
     * @param row the row to use
     * @param col the column
     * @return the value
     */
    @Override
    public Object getValueAt(int row, int col)
    {
        Object o;

        o = data.get( makeKey( row, col));

        return o;
    }

    /**
     * 
     * @param row the row to use
     */
    public synchronized void deleteRow( int row)
    {
        if( row > rowCount)
        {
            throw new RuntimeException( "Can't delete row " + row );
        }

        // Shuffel items back one.
        for( int r = row; r < rowCount; r++)
        {
            for( int i = 0; i < getColumnCount(); i++)
            {
                setValueAt( getValueAt( r + 1, i), row, i);
            }
        }

        rowCount--;

        if( rowCount < 0) rowCount = 0;

        fireTableRowsDeleted(row, row);
    }
    
    /**
     * 
     * @param col the column
     * @param order 
     */
    public synchronized void sort( int col, int order)
    {
        Vector list;
        list = new Vector();//NOPMD

        for( int r = 0; r < rowCount; r++)
        {
            Object o;
            o = getValueAt( r, col);
            TableSortData sortData;
            sortData = new TableSortData( o, r);

            list.addElement( sortData);
        }

        TableSortCompare sortCompare = new TableSortCompare( order);

        Collections.sort( list, sortCompare);

        Hashtable table = new Hashtable();

        for( int r = 0; r < list.size(); r++)
        {
            for( int i = 0; i < getColumnCount(); i++)
            {
                TableSortData sd;
                sd = (TableSortData)list.elementAt( r);

                Object o;

                o = getValueAt( sd.row, i);

                if( o != null)
                {
                    String key;
                    key = makeKey( r, i);

                    table.put( key, o);
                }
            }
        }

        data = table;

        fireTableRowsUpdated(0, getRowCount());
    }

    /**
     * 
     * @param aValue 
     * @param row the row to use
     * @param col the column
     */
    @Override
    public void setValueAt(Object aValue, int row, int col)
    {
        String key;

        if( row >= rowCount)
        {
            if( row > rowCount + 1 )
            {
                throw new RuntimeException(
                    "Can only extend table one row at a time"
                );
            }
            rowCount = row + 1;

            fireTableRowsInserted(rowCount - 1, rowCount);
        }

        Column column;

        column = (Column)columns.elementAt(col);

        if( column.key == true)
        {
            if( StringUtilities.isBlank(aValue))
            {
                deleteRow( row);
                return;
            }
        }

        key = makeKey( row, col);

        if( aValue == null)
        {
            data.remove( key);
        }
        else
        {
            data.put( key, aValue);
        }
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.application.table.CTableModel");//#LOGGER-NOPMD
}

class Column
{
    boolean key,
            editable;

    String name,
           mode;
}
