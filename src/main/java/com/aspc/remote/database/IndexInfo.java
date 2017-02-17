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
package com.aspc.remote.database;

import com.aspc.remote.util.misc.CLogger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.apache.commons.logging.Log;


/**
 *  Index Information
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       10 December 2009
 */
public final class IndexInfo
{
    /** the index name */
    public final String name;
    /** the index type */
    public final short type;
    /** the columns */
    private IndexColumnInfo[] columns;
    private final ArrayList<IndexColumnInfo> loadingColumns=new ArrayList<>();
    private boolean completeFg;

    /** 
     * is unique ?
     * Not unreliable in MYSQL. 
     * 
     * http://bugs.mysql.com/bug.php?id=8812
     */
    public final boolean unique;

    /**
     * The index info
     * @param name the name
     * @param type the type
     * @param unique is unique
     */
    public IndexInfo( final String name, final short type, final boolean unique)
    {
        this.name = name;
        this.type=type;
        this.unique=unique;
    }

    /**
     * Add the column
     * @param name the name
     * @param seq the sequence
     * @param order the order
     */
    public void addColumn( final String name, final int seq, final IndexColumnInfo.ORDER order)
    {
        if( completeFg) throw new AssertionError( "object completed");
        IndexColumnInfo info = new IndexColumnInfo(name, seq, order);

        loadingColumns.add(info);
    }

    /**
     * completed
     */
    public void complete()
    {
        completeFg = true;
        Collections.sort(
            loadingColumns,
            new Comparator<IndexColumnInfo>()
            {
                @Override
                public int compare(IndexColumnInfo o1, IndexColumnInfo o2)
                {
                    return o1.seq - o2.seq;
                }

            }
        );
        columns=new IndexColumnInfo[loadingColumns.size()];
        loadingColumns.toArray( columns);
    }

    /**
     * get the columns
     * @return the columns
     */
    public IndexColumnInfo[] getColumns()
    {
        if( completeFg==false) throw new AssertionError( "not completed");
        return columns.clone();
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.IndexInfo");//#LOGGER-NOPMD
}
