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
import com.aspc.remote.database.DataBase;
import com.aspc.remote.jdbc.Executor;
import com.aspc.remote.jdbc.SoapResultSet;
import com.aspc.remote.task.TaskHandler;
import com.aspc.remote.task.TaskManager;
import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.DocumentUtil;
import com.aspc.remote.util.misc.StringUtilities;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *  Updates an external database with changes made to stSoftware
 *  data in real time.
 *  The handler implements the handleTask method which will be called by the TaskManager
 *  whenever a transaction is processed containing changes to classes that we are interested in
 *  <br>
 *  <i>THREAD MODE: NON-MUTABLE</i>
 *
 *
 *  @author      Jason McGrath
 *  @since       23 September 2005
 */
public class ExtDBSyncHandler implements TaskHandler
{
    /** ACID mode */
    public static final String MODE_ACID="ACID";
    /** AUTO mode */
    public static final String MODE_AUTO="AUTO";
    private static final String DELETE_ACTION = "D";
    private static final String DEFAULT_FORMAT = "d MMM yyyy";
    private static final String COLUMN_CLASS="dbclass";
    private static final String COLUMN_ROW="row_uid";

    private HashMap<String, String>parentClassMap;

    /**
     * begin the task and process any parameters
     * @param xml the parameters
     * @param manager the task manager
     * @param executor the server connection
     * @throws Exception a serious problem
     */
    @Override
    public void beginTask( final @Nonnull TaskManager manager, final @Nonnull Executor executor, final @Nonnull String xml ) throws Exception
    {
    }

    private void loadParentMap( final HashMap<String, String>tmpClassMap, String subClassName, String parentClassName)
    {
        String subKey=subClassName.toLowerCase();
        String parentKey=parentClassName.toLowerCase();

        if( StringUtilities.isBlank(parentKey) == false)
        {
            tmpClassMap.put(subKey, parentKey);
        }
    }

    /**
     *
     * @param executor
     * @throws Exception a serious problem.
     */
    protected void initParentClasses( final @Nonnull Executor executor) throws Exception
    {
        if( parentClassMap== null)
        {
            HashMap<String, String>tmpMap=HashMapFactory.create();
            SoapResultSet rs = executor.fetch("SELECT name, parent_gid.name,template.name FROM DBClass");

            while( rs.next())
            {
                loadParentMap( tmpMap, rs.getString( 1), rs.getString( 2));
                loadParentMap( tmpMap, rs.getString( 1), rs.getString( 3));
            }

            rs = executor.fetch("SELECT name, parent_gid.name FROM DBTemplate");

            while( rs.next())
            {
                loadParentMap( tmpMap, rs.getString( 1), rs.getString( 2));
            }

            parentClassMap=tmpMap;
        }

    }

    /**
     * Is this transaction interesting ?
     * @param executor
     * @param transID
     * @param td
     * @param rowID
     * @return the value
     */
    @CheckReturnValue
    protected boolean isInteresting( final Executor executor, final long transID, ExtDBSyncTableDef td, long rowID)
    {
        return true;
    }

    /**
     * analyze all the transactions.
     * @param executor
     * @param transID
     * @param transRecord
     * @return the value
     * @throws Exception a serious problem.
     */
    protected TransactionAnalysis analyseTransactions( final Executor executor, final long transID, final SoapResultSet transRecord) throws Exception
    {
        TransactionAnalysis ta;
        ta = new TransactionAnalysis();

        while( transRecord.next())
        {
            String className = transRecord.getString( COLUMN_CLASS);

            long rowID = transRecord.getLong( COLUMN_ROW);

            ExtDBSyncTableDef[] tableDefs = getTableDefinitions( className);

            for (ExtDBSyncTableDef def : tableDefs) {
                if( isInteresting( executor, transID, def, rowID))
                {
                    ta.add( def.getSrcName(), rowID);
                    break;
                }
            }
        }
        ta.complete();

        transRecord.rewind();

        return ta;
    }

    /**
     * Processes the next supplied transaction by identifying the classes changed and updating
     * the external database based on the field mappings.
     * see TaskHandler.handleTask for more information
     *
     * @param executor an Executor object for running sql
     * @param transid the transaction id for this notification
     * @param transRecord the transaction record for this transaction id
     * @throws Exception A serious error
     */
    @Override
    public void handleTask( final @Nonnull Executor executor, final @Nonnull String transid, final @Nonnull SoapResultSet transRecord ) throws Exception
    {
        HashMap<DataBase, CSQL> acidDataBase=HashMapFactory.create();
        try
        {
            initParentClasses(executor);
            ArrayList updateList = new ArrayList();

            TransactionAnalysis ta = analyseTransactions( executor, Long.parseLong(transid), transRecord);

            while( transRecord.next())
            {
                String className = transRecord.getString( COLUMN_CLASS);
                String gkey = transRecord.getString( "global_key");
                String action = transRecord.getString( "action");
                long rowID = transRecord.getLong( COLUMN_ROW);

                if( ta.interesting( rowID) == false) continue;

                LOGGER.debug( "Processing transaction:" + transid + " key:" + gkey);

                ExtDBSyncTableDef[] tableDefs = getTableDefinitions( className);

                for (ExtDBSyncTableDef tableDef : tableDefs) {
                    DataBase destDb = tableDef.getDataBase();
                    String mode = tableDef.getMode();

                    if( MODE_ACID.equals( mode))
                    {
                        if( acidDataBase.containsKey(destDb) == false)
                        {
                            CSQL sql =new CSQL( destDb);
                            sql.beginTransaction();
                            acidDataBase.put(destDb, sql);
                        }
                    }

                    ExtDBSyncUpdate updateItem = null;
                    
                    // Retrieve list of changes
                    HashMap valMap = getFldValueMap( executor,  destDb, transid, rowID, tableDef, action, ta);

                    if( valMap != null && ! valMap.isEmpty())
                    {
                        updateItem = getUpdateHandler( tableDef, transid, action, valMap, executor );
                    }

                    if ( updateItem != null)
                    {
                        updateList.add( updateItem);
                    }
                }
            }

            // Order list of updates based on dependencies
            ExtDBSyncUpdate[] sortedList = new ExtDBSyncUpdate[updateList.size()];
            updateList.toArray( sortedList);
            doSort( sortedList);

            for (ExtDBSyncUpdate updateItem : sortedList) {
                LOGGER.debug( "Processing Entry:" + updateItem.getTableDef().getSrcName() + " key:" + updateItem.getSrcKeyValue());

                updateItem.doUpdate();
            }

            for( CSQL sql: acidDataBase.values())
            {
                sql.commit();
            }

            acidDataBase.clear();
        }
        catch( Exception e)
        {
            LOGGER.error( "Error processing transaction " + transid, e);
            throw e;
        }
        finally
        {
            for( CSQL sql: acidDataBase.values())
            {
                sql.rollback();
            }
        }
    }

    /**
     * Sort the list of updates so that the updates are done in the correct order based on
     * dependencies, i.e If an Invoice is linked to a Person then the Person must be created
     * first in the target database before the Invoice is.  However if both objects are being deleted
     * then the Invoice must be deleted first.
     * We can't use the standard java sort as we need to ensure that every object is compared
     * to the each other.
     * @param sortedList List of update objects that need to be sorted
     */
    @SuppressWarnings("AssignmentToForLoopParameter")
    public void doSort(ExtDBSyncUpdate[] sortedList)
    {
        ExtDBSyncUpdate tmp;
        for( int x=0; x<sortedList.length;x++)
        {
            for( int y=x+1; y<sortedList.length;y++)
            {
                if( sortedList[x] != sortedList[y])
                {
                    if( isDependent( sortedList[x], sortedList[y]))
                    {
                        if( sortedList[x].getAction().equals(DELETE_ACTION) == false)
                        {
                            tmp = sortedList[y];
                            sortedList[y] = sortedList[x];
                            sortedList[x] = tmp;
                            x=0;
                        }
                    }
                }
            }
        }
    }

    private boolean isDependent( ExtDBSyncUpdate update1, ExtDBSyncUpdate update2 )
    {
        boolean result = false;
        String[] fieldNames = update1.getTableDef().getFieldNames();
        String dependentName = update2.getTableDef().getSrcName();

        for (String fieldName : fieldNames) {
            ExtDBSyncFieldDef fieldDef = update1.getTableDef().getFieldDef(fieldName);
            String[] dependentClasses = fieldDef.getSrcDependentClasses();
            if( update1.getTableDef().getDataBase().getTypeKey().equals( update2.getTableDef().getDataBase().getTypeKey())==false)
            {
                return result;
            }
            if (dependentClasses != null) {
                for (String dependentClasse : dependentClasses) {
                    if (StringUtilities.isBlank(dependentClasse) == false && dependentClasse.equalsIgnoreCase(dependentName)) {
                        String destKey = update2.getSrcKeyValue();
                        String srcVal = (String)update1.getValMap().get( fieldDef.getDestName());

                        if( StringUtilities.isBlank( srcVal) == false &&
                                srcVal.equalsIgnoreCase( destKey))
                        {
                            result =  true;
                        }
                    }
                }
            }
        }
        return result;
    }


    /**
     * Formats the supplied value so that it is compatible with the type of field being
     * updated
     * @param db - database to be updated
     * @param fieldDef - field definition for external database
     * @param inValue - Value to be converted
     * @throws Exception A serious error
     * @return Formatted string
     */
    public String encodeValue(DataBase db, ExtDBSyncFieldDef fieldDef, String inValue) throws Exception
    {


        String value=inValue;
        if( StringUtilities.isBlank( value))
        {
            if( fieldDef.isString())
            {
                return "''";
            }
            else
            {
                return "NULL";
            }
        }

        String dateFormat = fieldDef.getDateFormat();
        if(  dateFormat != null && ! StringUtilities.isBlank(value))
        {
            SimpleDateFormat df1 = new SimpleDateFormat( DEFAULT_FORMAT);//NOPMD
            Date date = df1.parse(value);

            SimpleDateFormat df2 = new SimpleDateFormat( dateFormat);//NOPMD
            value = df2.format( date);
        }

        if( fieldDef.isString())
        {
            if (db.hasSQLEscape())
            {
                value = StringUtilities.replace(value, "\\", "\\\\");
            }

            value = StringUtilities.replace(value, "'", "''");

            value = "'" + value + "'";
        }

        return value;

    }

    /**
     * Loads the table/field mappings from the supplied file
     * @param fileName - name of file to be loaded
     * @throws Exception A serious error
     */
    public void configure( String fileName) throws Exception
    {
        File file = new File( fileName);

        if( file.exists() == false)
        {
            throw new Exception( "missing configuration file '" + fileName + "'");
        }

        loadDoc( file);

    }

    /**
     * Loads the table/field mappings from the supplied file
     *
     * @param file - file to be loaded
     * @throws Exception A serious error
     */
    public void loadDoc( File file) throws Exception
    {
        /**
         * load the properties
         */
        Document doc = DocumentUtil.loadDocument( file);

        DocumentUtil.normaliseDocument( doc);

        NodeList connections = doc.getElementsByTagName(TAG_DATABASE);
        String name = "default";
        String type = "";
        String url = "";
        String user = "";
        String pass = "";
        String mode = "";
        for( int i = 0; i < connections.getLength(); i++)
        {
            Element databaseElement = (Element)connections.item( i);

            if( databaseElement.hasAttribute( ATT_DATABASE_NAME))
            {
                name = databaseElement.getAttribute( ATT_DATABASE_NAME);
            }
            if( databaseElement.hasAttribute( ATT_DATABASE_TYPE))
            {
                type = databaseElement.getAttribute( ATT_DATABASE_TYPE);
            }
            if( databaseElement.hasAttribute( ATT_DATABASE_URL))
            {
                url = databaseElement.getAttribute( ATT_DATABASE_URL);
            }
            if( databaseElement.hasAttribute( ATT_DATABASE_USER))
            {
                user = databaseElement.getAttribute( ATT_DATABASE_USER);
            }
            if( databaseElement.hasAttribute( ATT_DATABASE_PASS))
            {
                pass = databaseElement.getAttribute( ATT_DATABASE_PASS);
            }
            if( databaseElement.hasAttribute( ATT_DATABASE_MODE))
            {
                mode = databaseElement.getAttribute( ATT_DATABASE_MODE);
            }
            addDataBase( name, type, url, user, pass, mode);
        }

        NodeList tmpTables = doc.getElementsByTagName( TAG_TABLE);

        for( int i = 0; i < tmpTables.getLength(); i++)
        {
            String srcTableName = "";
            String destTableName = "";
            String databaseName = "default";
            String updateHandlerName = "";
            String filter = "";

            Element tablesElement = (Element)tmpTables.item( i);

            if( tablesElement.hasAttribute( ATT_TABLE_SRC_NAME))
            {
                srcTableName = tablesElement.getAttribute( ATT_TABLE_SRC_NAME);
            }
            if( tablesElement.hasAttribute( ATT_TABLE_DEST_NAME))
            {
                destTableName = tablesElement.getAttribute( ATT_TABLE_DEST_NAME);
            }

            if( tablesElement.hasAttribute( ATT_TABLE_DEST_DATABASE_NAME))
            {
                databaseName = tablesElement.getAttribute( ATT_TABLE_DEST_DATABASE_NAME);
            }

            if( tablesElement.hasAttribute( ATT_TABLE_UPDATE_HANDLER_NAME))
            {
                updateHandlerName = tablesElement.getAttribute( ATT_TABLE_UPDATE_HANDLER_NAME);
            }

            if( tablesElement.hasAttribute( ATT_TABLE_FILTER))
            {
                filter = tablesElement.getAttribute( ATT_TABLE_FILTER);
            }

            ExtDBSyncTableDef tableDef = addTable( srcTableName, destTableName, databaseName, updateHandlerName, filter);

            NodeList fields = tablesElement.getElementsByTagName( TAG_FIELD);

            for( int f = 0; f < fields.getLength(); f++)
            {
                Element fieldsElement = (Element)fields.item( f);

                String srcFieldValue = "";
                String srcFieldPath = "";
                String destFieldName = "";
                String linkedClass = "";
                String dateFormat = null;
                boolean isKey = false,
                        isString = true;

                if( fieldsElement.hasAttribute( ATT_FIELD_SRC_VALUE))
                {
                    srcFieldValue = fieldsElement.getAttribute( ATT_FIELD_SRC_VALUE);
                }
                if( fieldsElement.hasAttribute( ATT_FIELD_SRC_PATH))
                {
                    srcFieldPath = fieldsElement.getAttribute( ATT_FIELD_SRC_PATH);
                }
                if( fieldsElement.hasAttribute( ATT_FIELD_DEST_NAME))
                {
                    destFieldName = fieldsElement.getAttribute( ATT_FIELD_DEST_NAME);
                }
                if( fieldsElement.hasAttribute( ATT_FIELD_IS_KEY))
                {
                    String tmp = fieldsElement.getAttribute( ATT_FIELD_IS_KEY);
                    if( "true".equalsIgnoreCase(tmp))
                    {
                        isKey = true;
                    }
                }
                if( fieldsElement.hasAttribute( ATT_FIELD_IS_STRING))
                {
                    String tmp = fieldsElement.getAttribute( ATT_FIELD_IS_STRING);
                    if( "false".equalsIgnoreCase(tmp))
                    {
                        isString = false;
                    }
                }
                if( fieldsElement.hasAttribute( ATT_FIELD_SRC_LNK_TABLE))
                {
                    String tmp = fieldsElement.getAttribute( ATT_FIELD_SRC_LNK_TABLE);
                    linkedClass = tmp;
                }
                if( fieldsElement.hasAttribute( ATT_FIELD_FORMAT))
                {
                    dateFormat = fieldsElement.getAttribute( ATT_FIELD_FORMAT);
                }

                ExtDBSyncFieldValidation[] l=ExtDBSyncFieldValidation.createValidationList(fieldsElement);
                tableDef.addField( srcFieldValue, srcFieldPath, destFieldName, isKey, isString, linkedClass, dateFormat, l);

            }
         }
    }

    /**
     * Retrieves the database for the supplied database name
     * @param name - name of database to be retrieved
     * @return - Database if one is found otherwise null
     */
    protected DataBase getDataBase( final String name)
    {
        DataBase db = (DataBase)databases.get( name);
        return db;
    }

    /**
     *
     * @param name the database
     * @return the mode
     */
    protected String getMode( final String name)
    {
        return transactionMode.get(name);
    }

    /**
     * Adds a new Database to the list of databases
     * @param name - Name used to retrieve the database
     * @param type - type of database, i.e. POSTGRESQL
     * @param url - server/database url
     * @param user - user name
     * @param pass - password of user
     * @param mode the mode ACID
     * @throws Exception A serious error
     */
    public void addDataBase( String name, String type, String url, String user, String pass, String mode ) throws Exception
    {
        DataBase db = new DataBase(user, pass, type, url, DataBase.Protection.NONE);
        db.connect();

        addDataBase( name, db, mode);
    }


    /**
     * Adds a new Database to the list of databases
     * @param name - Name used to retrieve the database
     * @param db - database to be added
     * @param mode the mode ACID
     * @throws Exception A serious error
     */
    public void addDataBase( final String name, final DataBase db, final String mode ) throws Exception
    {
        databases.put( name, db);
        String tmpMode=mode;
        if (StringUtilities.isBlank( tmpMode))
        {
            tmpMode = MODE_AUTO;
        }

        if( tmpMode.equals(MODE_ACID) == false && tmpMode.equals(MODE_AUTO) == false)
        {
            throw new Exception( "invalid mode " + tmpMode);
        }
        transactionMode.put(name, tmpMode);
    }


        /**
     * Adds a new table definition to the definition list
     * @param srcName - name of class in source database
     * @param destName - name of table in destination database
     * @param databaseName - name of destination database that this table is found in
     * @return - new table definition object
     */
    public ExtDBSyncTableDef addTable( String srcName,
                                       String destName,
                                       String databaseName)
    {
        return addTable( srcName, destName, databaseName, null, null);
    }

    /**
     * Adds a new table definition to the definition list
     *
     * @return - new table definition object
     * @param filter - filter for the class
     * @param srcClassName - name of class in source database
     * @param destName - name of table in destination database
     * @param databaseName - name of destination database that this table is found in
     * @param updateHandler - name of handler class that updates will be performed through
     */
    public ExtDBSyncTableDef addTable( String srcClassName,
                                       String destName,
                                       String databaseName,
                                       String updateHandler,
                                       String filter)
    {


        ExtDBSyncTableDef tableDef = new ExtDBSyncTableDef( srcClassName,
                                                            destName,
                                                            getDataBase(databaseName),
                                                            getMode(databaseName),
                                                            updateHandler,
                                                            filter);

        String srcClassKey = srcClassName.toLowerCase();

        // Add new table def to a list of table defs for source name
        ArrayList t = (ArrayList)classHandlerMap.get( srcClassKey);
        if( t == null)
        {
            t = new ArrayList();
            classHandlerMap.put( srcClassKey, t);
        }
        t.add( tableDef);

        return tableDef;
    }

    /**
     * Returns a list of classes from the source database based on the
     * mapping definitions
     * @return - list of classes
     */
    public String[] getSrcClasses()
    {
        ArrayList list = new ArrayList();
        Iterator en = classHandlerMap.keySet().iterator();
        while( en.hasNext())
        {
            list.add( (String)en.next());
        }
        String[] res = new String[ list.size()];
        list.toArray(res);
        return res;
    }

    /**
     * Returns a list of table definitions that have the supplied source class name
     * @param srcClassName - name of class in source database
     * @return - array of table definitions
     */
    public ExtDBSyncTableDef[] getTableDefinitions( final String srcClassName)
    {
        String srcClassKey=srcClassName.toLowerCase();
        ArrayList<ExtDBSyncTableDef> tmpList = new ArrayList<>();

        ArrayList t = (ArrayList)classHandlerMap.get( srcClassKey);
        if( t != null)
        {
            tmpList.addAll(t);
        }

        if (parentClassMap != null)
        {
            String parentClassName = parentClassMap.get(srcClassKey);
            if (parentClassName != null)
            {
                ExtDBSyncTableDef[] subList = getTableDefinitions(parentClassName);
                tmpList.addAll(Arrays.asList(subList));
            }
        }

        ExtDBSyncTableDef[] list = new ExtDBSyncTableDef[tmpList.size()];
        tmpList.toArray( list);

        return list;
    }

    /**
     * Get value map.
     * @param executor executor
     * @param db the database
     * @param transid transaction id
     * @param rowid RowId for object
     * @param tableDef tableDef
     * @param action action performed on the object
     * @param ta the analysis
     * @return map
     * @throws Exception A serious error
     */
    protected HashMap getFldValueMap(
        Executor executor,
        DataBase db,
        String transid,
        long rowid,
        ExtDBSyncTableDef tableDef,
        String action,
        TransactionAnalysis ta
    )  throws Exception
    {
        HashMap sqlValueMap = null;

        String[] fieldNames = tableDef.getFieldNames();
        String filter = tableDef.getFilter();

        StringBuilder sb = new StringBuilder(50);
        sb.append( "SELECT ");

        boolean first = true;
        HashSet<String> pathSet=new HashSet();

        for( String fldName: fieldNames)
        {
            ExtDBSyncFieldDef fieldDef = tableDef.getFieldDef(fldName);

            if ( StringUtilities.isBlank(fieldDef.getSrcValue()))
            {
                String p=fieldDef.getSrcPath();
                if( pathSet.add(p))
                {
                    if (!first)
                    {
                        sb.append(",");
                    }
                    sb.append(p);
                    first = false;
                }
                ExtDBSyncFieldValidation[] listValidations = fieldDef.listValidations();

                for( ExtDBSyncFieldValidation v: listValidations)
                {
                    if( v.filterField != null)
                    {
                        if( pathSet.add(v.filterField))
                        {
                            if (!first)
                            {
                                sb.append(",");
                            }
                            sb.append(v.filterField);
                            first = false;
                        }
                    }
                }
            }
        }

        sb.append( " FROM ");
        String srcClass=tableDef.getSrcName();
        sb.append( srcClass);

        StringBuilder criteria = new StringBuilder();

        if( StringUtilities.isBlank( filter) != true)
        {
            sb.append( " WHERE ");
            sb.append( filter);
            criteria.append( "AND ROW_IS ");
        }
        else
        {
            criteria.append( "WHERE ROW_IS ");
        }

        criteria.append( rowid);

        if( ta != null) ta.appendHint( srcClass, criteria);

        if( transid != null)
        {
            criteria.append( " TRANSACTION ");

            if( DELETE_ACTION.equalsIgnoreCase(action))
            {
                criteria.append( "BEFORE ");
            }
            criteria.append( transid);
        }

        sb.append( " ");
        sb.append( criteria);

        ResultSet rs=null;

        try
        {
            rs = executor.fetch( sb.toString());

            if( rs.next())
            {
                sqlValueMap = HashMapFactory.create();
                HashMap<String, String> filterMap = HashMapFactory.create();
                //HashMap<String, String> valueMap = HashMapFactory.create();
                for( String fldName: fieldNames)
                {
                    ExtDBSyncFieldDef fieldDef = tableDef.getFieldDef(fldName);
                    String temp;
                    if (fieldDef.getSrcValue() == null || fieldDef.getSrcValue().equals(""))
                    {
                        temp = rs.getString(fieldDef.getSrcPath());
                    }
                    else
                    {
                        temp = fieldDef.getSrcValue();
                    }
                    ExtDBSyncFieldValidation[] listValidations = fieldDef.listValidations();

                    for( ExtDBSyncFieldValidation v: listValidations)
                    {
                        if( v.filterField != null)
                        {
                            filterMap.put(v.filterField.toLowerCase(), rs.getString(v.filterField));
                        }
                    }
                    filterMap.put(fldName.toLowerCase(), temp);
                    temp = encodeValue( db, fieldDef, temp);
                    sqlValueMap.put( fieldDef.getDestName(), temp);
                }

                for( String fldName: fieldNames)
                {
                    ExtDBSyncFieldDef fieldDef = tableDef.getFieldDef(fldName);

                    ExtDBSyncFieldValidation[] listValidations = fieldDef.listValidations();

                    for( ExtDBSyncFieldValidation v: listValidations)
                    {
                        v.validate( filterMap.get( fldName.toLowerCase()), filterMap);
                    }
                }
            }
            else
            {
                LOGGER.error( "No objects found matching conditions. " + sb);
            }
        }
        catch( Exception e)
        {
            LOGGER.error( "Could not retrieve transaction values. Class:" + tableDef.getSrcName() + " Condition:" + criteria.toString(), e);
            throw e;
        }
        finally
        {
            if( rs != null)
            {
                try
                {
                    rs.close();
                }
                catch( SQLException se)
                {
                    LOGGER.error( "Could not close resultset when retrieving transaction values.");
                }
            }
        }

        return sqlValueMap;
    }

     /**
     * Get value map.
     * @param tableDef the table definition details
     * @param transId the transaction id
     * @param action action that was performed on object
     * @param valMap list of fieldname/val pairs
     * @param executor who should run it.
     * @return update handler for this table
     * @throws Exception in case of error
     */
    protected ExtDBSyncUpdate getUpdateHandler(
        final ExtDBSyncTableDef tableDef,
        final String transId,
        final String action,
        final HashMap valMap,
        final Executor executor
    ) throws Exception
    {
        String updateHndClassName= tableDef.getUpdateHandlerName();

        ExtDBSyncUpdate updateHandler = null;

        if( StringUtilities.isBlank( updateHndClassName) == true)
        {
            updateHandler = new ExtDBSyncUpdate( tableDef);
            updateHandler.setExecutor( executor);
        }
        else
        {
            Class aClass = null;

            try
            {
                aClass = Class.forName(updateHndClassName);
            }
            catch( ClassNotFoundException ce)
            {
                throw new Exception( "Update handler '" + updateHndClassName + "' does not exist.", ce);
            }
//
//            if( aClass != null)
//            {
                // Initialise parameters for class

                Class init[] = {ExtDBSyncTableDef.class};
                Object objs[] = { tableDef};

                // Construct new instance
                Constructor cons;

                try
                {
                    cons = aClass.getConstructor(init);
                    updateHandler = (ExtDBSyncUpdate)cons.newInstance(objs);
                }
                catch( NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
                {
                    throw new Exception( "Could construct class with " + updateHndClassName + "(ExtDBSyncTableDef tableDef).",  e);
                }
//            }
//            else{
//                
//            }
        }
        updateHandler.setData( transId, action, valMap);
        return updateHandler;
    }

    private final ConcurrentHashMap<String, ArrayList> classHandlerMap = new ConcurrentHashMap();//MT fixed: needs to be Concurrent hash map
    private final ConcurrentHashMap<String, DataBase> databases = new ConcurrentHashMap();//MT fixed: needs to be Concurrent hash map
    private final HashMap<String, String> transactionMode = HashMapFactory.create();

    private static final String TAG_TABLE="table";
    private static final String ATT_TABLE_SRC_NAME = "src_name";
    private static final String ATT_TABLE_DEST_NAME = "dest_name";
    private static final String ATT_TABLE_DEST_DATABASE_NAME = "database_name";
    private static final String ATT_TABLE_UPDATE_HANDLER_NAME = "update_handler_name";
    private static final String ATT_TABLE_FILTER = "filter";

    private static final String TAG_FIELD="field";
    private static final String ATT_FIELD_SRC_VALUE = "src_value";
    private static final String ATT_FIELD_SRC_PATH = "src_path";
    private static final String ATT_FIELD_SRC_LNK_TABLE = "src_link_table";
    private static final String ATT_FIELD_DEST_NAME = "dest_name";
    private static final String ATT_FIELD_IS_KEY = "is_key";
    private static final String ATT_FIELD_IS_STRING = "is_string";
    private static final String ATT_FIELD_FORMAT = "format";

    public static final String TAG_VALIDATE = "validate";
    public static final String ATT_VALIDATE_CODE = "code";
    public static final String ATT_VALIDATE_CONDITIONS = "conditions";
    public static final String ATT_VALIDATE_MESSAGE = "message";
    public static final String ATT_VALIDATE_PATTERN = "pattern";
    public static final String ATT_VALIDATE_ACTION = "action";
    public static final String ATT_VALIDATE_FILTER_FIELD = "filter_field";
    public static final String ATT_VALIDATE_FILTER_PATTERN = "filter_pattern";

    private static final String TAG_DATABASE="database";
    private static final String ATT_DATABASE_NAME = "name";
    private static final String ATT_DATABASE_TYPE = "type";
    private static final String ATT_DATABASE_URL = "url";
    private static final String ATT_DATABASE_USER = "username";
    private static final String ATT_DATABASE_PASS = "password";
    private static final String ATT_DATABASE_MODE = "mode";

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.task.extdbsync.ExtDBSyncHandler");//#LOGGER-NOPMD
}
