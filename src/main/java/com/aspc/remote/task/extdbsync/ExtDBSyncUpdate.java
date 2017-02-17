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
package com.aspc.remote.task.extdbsync;

import com.aspc.remote.database.CSQL;
import com.aspc.remote.database.TableUtil;
import com.aspc.remote.jdbc.Executor;
import com.aspc.remote.util.misc.CLogger;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.logging.Log;

/**
 *  Updates an external database with changes made to stSoftware
 *  data in real time
 *
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *  @author      Jason McGrath
 *  @since       23 September 2005
 */
public class ExtDBSyncUpdate
{

    /**
     * constructor
     * @param tableDef - Table and field definitions
     */
    public ExtDBSyncUpdate(  final ExtDBSyncTableDef tableDef )
    {
        this.tableDef = tableDef;
        this.executor = null;
    }

     /**
     * Sets the data to be used by updates
     * @param transId - Id of transaction
     * @param action - Action to be performed i.e D for delete
     * @param valMap - Field/value mapping
     */
    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public void setData( final String transId,
                         final String action,
                         final HashMap valMap)
    {
        this.transId = transId;
        this.action = action;
        this.valMap = valMap;
    }

    /**
     * the action to be performed
     * @return the name
     */
    public String getAction()
    {
        return action;
    }

    /**
     * the definition of the table
     * @return the name
     */
    public ExtDBSyncTableDef getTableDef()
    {
        return tableDef;
    }

    /**
     * the transaction id that the update was triggered from
     * @return the name
     */
    public String getTransId()
    {
        return transId;
    }

    /**
     * the value of the src key
     * @return the name
     */
    public String getSrcKeyValue()
    {
        String value= null;
        ExtDBSyncFieldDef keyField = tableDef.getKeyFieldDef();
        if( keyField != null)
        {
            value = (String)valMap.get( keyField.getDestName());
        }
        return value;
    }


    /**
     * the table of values
     * @return the name
     */
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public HashMap getValMap ()
    {
        return valMap;
    }

    /**
     * Updates the destination database based on the action
     * @throws Exception A serious error
     */
    public void doUpdate() throws Exception
    {
        if( action.equalsIgnoreCase( "D") == false)
        {
            processUpdate( tableDef, valMap);
        }
        else
        {
            processDelete( tableDef, valMap);
        }
    }

    /**
     * Performs an insert or updated based on whether the data already exists in the destination database or not;
     * @param tableDef - Table and field definitions
     * @param valMap - Field/value mapping
     * @throws Exception A serious error
     */
    protected void processUpdate( final ExtDBSyncTableDef tableDef, final HashMap valMap) throws Exception
    {
        // Check if destination does not already exist

        if( checkDestExists( tableDef, valMap) == false)
        {
            doInsert( tableDef, valMap);
        }
        else
        {
            doUpdate( tableDef, valMap);
        }
    }

    /**
     * Deletes a record from the destination database
     * @param tableDef - Table and field definitions
     * @param valMap - Field/value mapping
     * @throws Exception A serious error
     */
    protected void processDelete( final ExtDBSyncTableDef tableDef, final HashMap valMap) throws Exception
    {

        ExtDBSyncFieldDef keyField = tableDef.getKeyFieldDef();
        if( keyField == null)
        {
            return;
        }
        String fieldName = keyField.getDestName();
        String srcKeyValue = (String)valMap.get( fieldName);

        String sql = "DELETE FROM " + tableDef.getDestName() + " WHERE " + keyField.getDestName() + " = " + srcKeyValue;

        CSQL csql = new CSQL( tableDef.getDataBase());
        csql.execute( sql);
        LOGGER.debug( sql);
    }

    /**
     *  Inserts a record into the destination database
     * @param tableDef - Table and field definitions
     * @param valMap - Field/value mapping
     * @throws Exception A serious error
     */
    protected void doInsert( final ExtDBSyncTableDef tableDef, final HashMap valMap) throws Exception//NOPMD
    {

        StringBuilder sb = new StringBuilder(50);
        sb.append("INSERT INTO ");
        sb.append(tableDef.getDestName().toLowerCase());
        sb.append("(");
        String name;

         Iterator en = valMap.keySet().iterator();
        for( int i=0; en.hasNext(); i++)
        {
            name = (String)en.next();
            if (i != 0)
                sb.append(",");
            sb.append(name.toLowerCase());
        }
        sb.append(") VALUES (");

        en = valMap.keySet().iterator();
        for( int i=0; en.hasNext(); i++)
        {
            name = (String)en.next();
            if (i != 0)
                sb.append(",");

            String value = (String)valMap.get( name);
            sb.append(value);
        }
        sb.append(") ");


        CSQL csql = new CSQL( tableDef.getDataBase());
        csql.execute( sb.toString());
        LOGGER.debug( sb.toString());
    }


    /**
     *  Updates a record in the destination database
     * @param tableDef - Table and field definitions
     * @param valMap - Field/value mapping
     * @throws Exception A serious error
     */
    protected void doUpdate( final ExtDBSyncTableDef tableDef, final HashMap valMap) throws Exception
    {
        ExtDBSyncFieldDef keyField = tableDef.getKeyFieldDef();
        if( keyField == null)
        {
            return;
        }
        String keyFld = keyField.getDestName();
        String keyValue = (String)valMap.get( keyFld);
        String tableName = tableDef.getDestName();

        StringBuilder sb = new StringBuilder(50);
        sb.append("UPDATE ");
        sb.append(tableName.toLowerCase());
        sb.append(" SET ");

        Iterator en = valMap.keySet().iterator();
        for( int i=0; en.hasNext(); i++)
        {
            String name = (String)en.next();
            if (i != 0)
                sb.append(",");

            String value = (String)valMap.get( name);

            sb.append(name.toLowerCase());
            sb.append( " = ");

            if( "''".equals( value))
            {
                TableUtil tu = TableUtil.find( tableDef.getDataBase());

                if( tu.isColumnNullable( tableName, name))
                {
                    value = "NULL";
                }
            }
            sb.append(value);

        }

        sb.append(" WHERE ");
        sb.append( keyFld);
        sb.append( " = ");
        sb.append( keyValue);

        CSQL csql = new CSQL( tableDef.getDataBase());
        csql.execute( sb.toString());
        LOGGER.debug( sb.toString());
    }

    /**
     * Determines if a record already exists in the destination database
     * @param tableDef - database to be updated
     * @param valMap - name of destination table to be updated
     * @throws Exception A serious error
     * @return - true if there is an entry in the destination database that matches supplied field value
     */
    public boolean checkDestExists( final ExtDBSyncTableDef tableDef, final HashMap valMap) throws Exception
    {
        ExtDBSyncFieldDef keyField = tableDef.getKeyFieldDef();
        if( keyField == null)
        {
            return false;
        }
        String fieldName = keyField.getDestName();
        String fieldValue = (String)valMap.get( fieldName);

        String sql = "SELECT " + fieldName + " FROM " + tableDef.getDestName() + " WHERE " + fieldName + " = " + fieldValue;
        CSQL csql = new CSQL( tableDef.getDataBase());
        csql.perform(sql);
        return csql.next();
    }

    /**
     * The st executor (connection)
     * @return Executor a connection
     */
    public Executor getExecutor()
    {
        return executor;
    }

    /**
     * Set the st executor (connection)
     * @param executor -  a connection
     */
    public void setExecutor( final Executor executor)
    {
        this.executor = executor;
    }

    private final ExtDBSyncTableDef tableDef;
    private Executor executor;
    private String transId;
    private String action;
    private HashMap valMap;

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.task.extdbsync.ExtDBSyncUpdate");//#LOGGER-NOPMD
}
