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
package com.aspc.remote.application.table;

import org.apache.commons.logging.Log;
import javax.swing.table.*;
import com.aspc.remote.application.*;
import java.util.*;
import com.aspc.remote.util.misc.*;
import javax.swing.*;
import java.awt.event.*;

/**
 *  CTable
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       June 18, 1998, 1:42 PM
 */
public class CTable extends JTable
{
    //Constructors
    /**
     *
     */
    public CTable()
    {
        this(
            new CTableModel("","Col1|Col2|Col3")
        );
    }

    /**
     * 
     * @param model 
     */
    public CTable(CTableModel model)
    {
        super();
        setModel( model);//NOPMD
    }

    // Public methods
    /**
     * 
     * @param model 
     */
    @Override
    public void setModel( TableModel model)
    {
        super.setModel( model);
        createSortListener();
    }

    /**
     * 
     * @return the value
     */
    public String getPrefs()
    {
        int c;
        c = getColumnCount();
        StringBuilder buffer = new StringBuilder();

        for( int i = 0; i < c; i++)
        {
            TableColumn aColumn = getColumnModel().getColumn( i);

             buffer.append(
                getColumnName( i) + ",width=" +aColumn.getWidth() + "\t"
             );
        }

        return buffer.toString();
    }

    /**
     *
     */
    public void clear()
    {
        CApp.swingInvokeAndWait(
            new Runnable()
            {
                @Override
                public void run()
                {
                    CTableModel model;
                    model = (CTableModel)getModel();
                    while( model.getRowCount() > 0)
                    {
                        model.deleteRow(0);
                    }
                }
            }
        );
    }

    /**
     * 
     * @param s 
     */
    public void setPrefs(String s)
    {
        try
        {
            StringTokenizer st = new StringTokenizer( s, "\t");

            while( st.hasMoreTokens())
            {
                String t = st.nextToken();

                StringTokenizer vt = new StringTokenizer( t, ",");

                String n, k;

                n = vt.nextToken();

                while( vt.hasMoreTokens())
                {
                    StringTokenizer et = new StringTokenizer( vt.nextToken(), "=");
                    k = et.nextToken();
                    //v = et.nextToken();

                    int c;
                    c = getColumnCount();
                    boolean found = false;
                    for( int i = 0; i < c && found == false; i++)
                    {
                        if( getColumnName( i).equals( n))
                        {
                            found = true;
                            if( k.equals( "width"))
                            {
                                ;//TableColumn aColumn = getColumnModel().getColumn( i);
                                //aColumn.setPreferredWidth( Integer.parseInt( v));
                            }
                            else
                            {
                                LOGGER.info( "Unknown command '" + k + "'");
                            }
                        }
                    }
                    if( found == false)
                    {
                        LOGGER.info( "Couldn't find '" + n + "'");
                    }
                }
            }
        }
        catch( Throwable t)
        {
            LOGGER.info("table", t);
        }
    }

    private void createSortListener()
    {
        setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                TableColumnModel columnModel = getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = convertColumnIndexToModel(viewColumn);
                if(e.getClickCount() == 1 && column != -1)
                {
                    //LOGGER.info("Sorting ...");
                    int shiftPressed = e.getModifiers()&InputEvent.SHIFT_MASK;
                    int order;

                    order = TableSortCompare.ASCENDING;

                    if( shiftPressed != 0)
                    {
                        order = TableSortCompare.DESCENDING;
                    }
                    CTableModel model;

                    model = (CTableModel)getModel();
                    model.sort(column, order);
                }
             }
        };

        JTableHeader th = getTableHeader();
        if( th != null)
        {
            th.addMouseListener(listMouseListener);
        }
    }
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.application.table.CTable");//#LOGGER-NOPMD
}
