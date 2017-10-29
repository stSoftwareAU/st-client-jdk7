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
package com.aspc.remote.database;

import com.aspc.developer.ThreadCop;
import com.aspc.developer.ThreadCop.MODE;
import com.aspc.remote.database.internal.ResultsLoader;
import com.aspc.remote.database.internal.TableUtilRunner;
import com.aspc.remote.util.links.LinkManager;
import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.memory.MemoryHandler;
import com.aspc.remote.memory.MemoryManager;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.CProperties;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.timer.Lap;
import java.sql.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;

/**
 *  CreateTables
 *
 *  NB: Sybase has a limit of 30 characters for an index or table name.
 *
 *  TODO: If an key table doesn't exist then we should make sure that when we create the key table the index version
 *        string is cleared. If I drop the index table it should gracefully recreate the table and reindex the
 *        fields.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       February 13, 2001, 3:20 PM
 */
public final class TableUtil
{
    @SuppressWarnings({"PublicInnerClass", "PackageVisibleInnerClass"})
    enum INDEX_TYPE
    {
        UNIQUE,
        PRIMARY_KEY;
    };

    /**
     * The upper case function ( different for each database)
     */
    public static final String FUNCTION_UPPER="UPPER";

    /**
     * The length case function ( different for each database)
     */
    public static final String FUNCTION_LENGTH="LENGTH";

    /**
     * null
     */
    public static final String FIELD_MODIFIER_NULL="NULL";

    /**
     * The maximum clauses for this database type.
     *
     * @return The number of clauses.
     */
    @CheckReturnValue
    public int maxClauses()
    {
        return 200;
    }

    /**
     * Find the table utils for this database.
     *
     * @param dBase The database.
     * @return The table utils.
     */
    @CheckReturnValue @Nonnull
    public static TableUtil find( final @Nonnull DataBase dBase)
    {
        TableUtil tu;

        tu = DATABASE_MAP.get( dBase);

        if( tu == null)
        {
            tu = new TableUtil( dBase);

            TableUtil tu2 = DATABASE_MAP.putIfAbsent(dBase, tu);
            
            if( tu2 != null) 
            {
                tu=tu2;
            }
        }

        return tu;
    }

    /**
     * Clears all cached information about this database.
     */
    public synchronized void clearCache()
    {
        LOGGER.debug( "clearCache() for database '" + dBase.getUrl() + "'");
        holderAllTables = null;
        holderAllProcedures=null;
        holdIndexes.clear();
        cacheColumns.clear();
    }

    /**
     * Clears all cached information about ALL databases.
     */
    public static void clearCacheAll()
    {
        DATABASE_MAP.keySet().stream().forEach((dBase) -> {
            find( dBase).clearCache();
        });
    }

    /**
     * Does this procedure exist ?
     * @param procedure the name
     * @return true if exists
     * @throws SQLException a serious problem
     */
    @CheckReturnValue
    public boolean doesProcedureExist(final @Nonnull String procedure) throws SQLException
    {
        HashMap<String, String> procedures = getProcedures();

        return procedures.containsKey(procedure.toLowerCase());
    }

    /**
     * Does the table exist ?
     *
     * @param tableName The table to check
     *
     * @throws java.sql.SQLException A SQL exception
     * @return true if the table exists.
     */
    @CheckReturnValue
    public boolean doesTableExist(final @Nonnull String tableName) throws SQLException
    {
        ConcurrentHashMap tables = getTables();

        boolean flag = tables.get( tableName.toLowerCase()) != null;

        return flag;
    }

    /**
     * get the column type.
     *
     * @param tableName the table name
     * @param columnName the column name
     *
     * @throws java.sql.SQLException A SQL exception
     *
     * @return the data type
     */
    @CheckReturnValue @Nonnull
    public ColumnType getColumnDataType(
        final @Nonnull String tableName,
        final @Nonnull String columnName
    ) throws SQLException
    {
        ColumnInfo column = getColumn( tableName, columnName);

        try
        {
            return ColumnType.findType(dBase, column.typeName, column.type);
        }
        catch( SQLException sqlE)
        {
            throw new SQLException( "could not get column type for " + tableName + "." + columnName, sqlE);
        }
    }

    /**
     * get the column type.
     *
     * @param tableName the table name
     * @param columnName the column name
     *
     * @throws java.sql.SQLException A SQL exception
     *
     * @return the type
     */
    @CheckReturnValue
    public int getColumnType(
        final @Nonnull String tableName,
        final @Nonnull String columnName
    ) throws SQLException
    {
        ColumnInfo column = getColumn( tableName, columnName);

        return column.type;
    }

    /**
     * get the column.
     *
     * @param tableName the table name
     * @param columnName the column name
     *
     * @throws java.sql.SQLException A SQL exception
     *
     * @return the column
     */
    @CheckReturnValue @Nonnull
    public ColumnInfo getColumn(
        final @Nonnull String tableName,
        final @Nonnull String columnName
    ) throws SQLException
    {
        ColumnInfo list[] = getColumns( tableName);

        for( ColumnInfo c: list)
        {
            if( c.name.equalsIgnoreCase(columnName)) return c;
        }

        throw new SQLException( "no column " + tableName + "." + columnName);
    }

    /**
     * get the column.
     *
     * @param tableName the table name
     * @param columnName the column name
     *
     * @throws java.sql.SQLException A SQL exception
     *
     * @return the column
     */
    @CheckReturnValue @Nullable
    public ColumnInfo fetchColumn(
        final @Nonnull String tableName,
        final @Nonnull String columnName
    ) throws SQLException
    {
        ColumnInfo list[] = getColumns( tableName);

        for( ColumnInfo c: list)
        {
            if( c.name.equalsIgnoreCase(columnName)) return c;
        }

        return null;
    }

    /**
     * get the column type.
     *
     * @param tableName the table name
     * @param columnName the column name
     *
     * @throws java.sql.SQLException A SQL exception
     *
     * @return the type
     */
    @CheckReturnValue @Nonnull
    public String getColumnStdTypeName(
        final @Nonnull String tableName,
        final @Nonnull String columnName
    ) throws SQLException
    {
        ColumnInfo ci = getColumn( tableName, columnName);

        try
        {
            return ColumnType.getStdTypeName(ci.type);
        }
        catch( SQLException sqlE)
        {
            throw new SQLException( "could not get column standard type for " + tableName + "." + columnName, sqlE);
        }
    }

    /**
     * get the column type.
     *
     * @param tableName the table name
     * @param columnName the column name
     *
     * @throws java.sql.SQLException A SQL exception
     *
     * @return the type
     */
    @CheckReturnValue
    public boolean isColumnNullable(
        final @Nonnull String tableName,
        final @Nonnull String columnName
    ) throws SQLException
    {
        ColumnInfo ci = getColumn( tableName, columnName);

        return ci.nullable;
    }

    /**
     * The column size
     *
     * @param tableName The table name
     * @param columnName The column name
     *
     * @throws java.sql.SQLException A SQL exception
     *
     * @return the column size.
     */
    @CheckReturnValue
    public int getColumnSize(
        final @Nonnull String tableName,
        final @Nonnull String columnName
    ) throws SQLException
    {
        ColumnInfo ci = getColumn( tableName, columnName);

        return ci.size;
    }

    /**
     * The column scale
     *
     * @param tableName The table name
     * @param columnName The column name
     *
     * @throws java.sql.SQLException A SQL exception
     *
     * @return the column scale.
     */
    @CheckReturnValue
    public int getColumnScale(
        final @Nonnull String tableName,
        final @Nonnull String columnName
    ) throws SQLException
    {
        ColumnInfo ci = getColumn( tableName, columnName);

        return ci.scale;
    }

    /**
     * Does the column exist ?
     *
     * @param tableName The table name
     * @param columnName The column name
     *
     * @throws java.sql.SQLException A SQL exception
     *
     * @return true if the column exist.
     */
    @CheckReturnValue
    public boolean doesColumnExist(
        final @Nonnull String tableName,
        final @Nonnull String columnName
    ) throws SQLException
    {
        ColumnInfo ci = fetchColumn( tableName, columnName);

        return ci != null;
    }

    /**
     * fetch the index info
     * @param tableName the table name
     * @param indexName the index name
     * @return null if the index doesn't exist
     * @throws SQLException a problem
     */
    @CheckReturnValue @Nullable
    public IndexInfo fetchIndexInfo(final @Nonnull String tableName, final @Nonnull String indexName) throws SQLException
    {
        IndexInfo list[]= getIndexes(tableName);

        for( IndexInfo info: list)
        {
            if( indexName.equalsIgnoreCase(info.name)) return info;
        }

        return null;
    }

    /**
     * Does the column exist ?
     *
     * @return true if the column exist.
     * @param tableName The table name
     * @throws java.sql.SQLException A SQL exception
     */
    @SuppressWarnings({"NestedSynchronizedStatement", "Convert2Lambda"})
    @CheckReturnValue @Nonnull
    public synchronized ColumnInfo[] getColumns(
        final @Nonnull String tableName
    ) throws SQLException
    {
        if( doesTableExist( tableName) == false )
        {
            return new ColumnInfo[0];
        }

        String key = tableName.toLowerCase();

        ColumnInfo[][] handle = (ColumnInfo[][])cacheColumns.get( key);

        if( handle == null)
        {
            handle = new ColumnInfo[1][];

            cacheColumns.put( key, handle);
        }

        ColumnInfo[] columns = handle[0];

        if( columns == null)
        {
            final ArrayList<ColumnInfo> temp = new ArrayList();
            CSQL sql =new CSQL( dBase);

            sql.setLoader( new ResultsLoader() {

                @Override
                public int loadResults(final Statement stmt) throws Exception
                {
                    ResultSet resultSet;

                    resultSet = stmt.getResultSet();

                    ResultSetMetaData metaData;
                    metaData    = resultSet.getMetaData();
                    int colCount;
                    colCount    = metaData.getColumnCount();

                    for( int i = 1; i <= colCount; i++)
                    {
                        String cName;
                        cName = metaData.getColumnName( i);
                        ColumnInfo ci = new ColumnInfo(
                            cName,
                            metaData.getColumnTypeName( i),
                            metaData.getColumnType( i),
                            metaData.getColumnDisplaySize(i),
                            metaData.getPrecision(i),
                            metaData.isNullable( i),
                            metaData.getScale( i)
                        );

                        temp.add( ci );
                    }
                    return 0;
                }
            });

            sql.perform("SELECT * FROM " + tableName + " WHERE 0=1");

            columns=new ColumnInfo[temp.size()];
            temp.toArray( columns);

            // prevent reorder
            synchronized( handle)
            {
                handle[0]=columns;
            }
        }

        return columns;
    }

    /**
     * Is a table index unique
     *
     * @param tableName the table name
     * @param indexName the index name
     *
     * @throws java.sql.SQLException A SQL exception
     *
     * @return true if the index exists
     * <B>Note unreliable in MYSQL. </B>
     * 
     * http://bugs.mysql.com/bug.php?id=8812
     *
    public synchronized boolean ZisTableIndexUnique(
        final String tableName,
        final String indexName
    ) throws SQLException
    {
        IndexInfo[] indexes = getIndexes( tableName);

        for( IndexInfo info: indexes)
        {
            if( info.name.equalsIgnoreCase(indexName))
            {
                return info.unique;
            }
        }

        return false;
    }*/

    /**
     * Does an index of this name exist on this table ?
     *
     * @param tableName the table name
     * @param indexName the index name
     *
     * @throws java.sql.SQLException A SQL exception
     *
     * @return true if the index exists
     */
    @CheckReturnValue
    public boolean doesTableIndexExist(
        final @Nonnull String tableName,
        final @Nonnull String indexName
    ) throws SQLException
    {
        IndexInfo[] indexes = getIndexes( tableName);

        for( IndexInfo info: indexes)
        {
            if( info.name.equalsIgnoreCase(indexName))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the database for this table utilities.
     *
     * @return the database.
     */
    @CheckReturnValue @Nonnull
    public DataBase getDataBase()
    {
        return dBase;
    }

    /**
     *
     * @param column the column
     * @param value the default value
     * @return the null clause
     */
    @CheckReturnValue @Nonnull
    public String convertNull( final @Nonnull String column, final @Nonnull String value)
    {
        String type = dBase.getType();
        switch (type) 
        {
            case DataBase.TYPE_SYBASE:
                return "isnull(convert(varchar(255)," + column + "),'" + value + "')";
            case DataBase.TYPE_POSTGRESQL:
                return "COALESCE(" + column + ",'" + value + "')";
            case DataBase.TYPE_HSQLDB:
                return "ifnull(" + column + ",'" + value + "')";
            case DataBase.TYPE_ORACLE:
                return "nvl(" + column + ", '" + value + "')";
            case DataBase.TYPE_MYSQL:
                return "COALESCE(" + column + ", '" + value + "')";
            case DataBase.TYPE_MSSQL:
                return "COALESCE(" + column + ", '" + value + "')";
            default:
                throw new RuntimeException( "can't convert to Null for database type " + type);
        }
    }


    /**
     * column IS NOT NULL or and not blank ( only white space)
     *
     * @param column the column
     * @return the null clause
     */
    @CheckReturnValue @Nonnull
    public String notBlankClause( final @Nonnull String column)
    {
        String type = dBase.getType();
        switch (type) 
        {
            case DataBase.TYPE_SYBASE:
                return "isnull(convert(varchar(255)," + column + "),'') != ''";
            case DataBase.TYPE_POSTGRESQL:
                return "COALESCE(" + column + ",'')!=''";
            case DataBase.TYPE_HSQLDB:
                return "ifnull(" + column + ",'') != ''";
            case DataBase.TYPE_ORACLE:
                return "trim(" + column + ") IS NOT NULL";
            case DataBase.TYPE_MYSQL:
                return "COALESCE(" + column + ", '') != ''";
            case DataBase.TYPE_MSSQL:
                return "COALESCE(" + column + ", '') != ''";
            default:
                throw new RuntimeException( "can't convert to Null for database type " + type);
        }
    }

    /**
     * column IS NULL or blank ( only white space)
     *
     * @param column A text/varchar column
     * @return the null clause
     */
    @CheckReturnValue @Nonnull
    public String isBlankClause( final @Nonnull String column)
    {
        String type = dBase.getType();
        switch (type) 
        {
            case DataBase.TYPE_SYBASE:
                return "isnull(convert(varchar(255)," + column + "),'') = ''";
            case DataBase.TYPE_POSTGRESQL:
                return "COALESCE(trim(" + column + "),'')=''";
            case DataBase.TYPE_HSQLDB:
                return "LTRIM(ifnull(" + column + ",'')) = ''";
            case DataBase.TYPE_ORACLE:
                return "nvl(length(trim(" + column + ")),0)=0";
            case DataBase.TYPE_MYSQL:
                return "COALESCE(" + column + ", '') = ''";
            case DataBase.TYPE_MSSQL:
                return "COALESCE(" + column + ", '') = ''";
            default:
                throw new RuntimeException( "can't convert to Null for database type " + type);
        }
    }

    /**
     * ISNULL function
     * @param column the column
     * @param value the default value
     * @return the null clause
     */
    @CheckReturnValue @Nonnull
    public String convertNull( final @Nonnull String column, final int value)
    {
        String type = dBase.getType();
        switch (type) 
        {
            case DataBase.TYPE_SYBASE:
                return "isnull(" + column + "," + value + ")";
            case DataBase.TYPE_POSTGRESQL:
                return "COALESCE(" + column + "," + value + ")";
            case DataBase.TYPE_HSQLDB:
                return "ifnull(" + column + "," + value + ")";
            case DataBase.TYPE_ORACLE:
                return "nvl(" + column + ", " + value + ")";
            case DataBase.TYPE_MYSQL:
                return "COALESCE(" + column + ", " + value + ")";
            case DataBase.TYPE_MSSQL:
                return "COALESCE(" + column + ", " + value + ")";
            default:
                throw new RuntimeException( "can't convert to Null for database type " + type);
        }
    }

    /**
     * truncate a column value
     *
     * @param column the column
     * @return the truncate function
     */
    @CheckReturnValue @Nonnull
    public String mathTruncate( final @Nonnull String column)
    {
        String type = dBase.getType();
        switch (type) 
        {
            case DataBase.TYPE_POSTGRESQL:
            case DataBase.TYPE_ORACLE:
                return "trunc(" + column + ")";
            case DataBase.TYPE_SYBASE:
                return "floor(" + column + ")";
            case DataBase.TYPE_MSSQL:
                return "round(" + column + ",0,1)";
            default:
                return "truncate(" + column + ",0)";
        }
    }

    /**
     *
     * @param column the column
     * @return the long convert function
     */
    @CheckReturnValue @Nonnull
    public String convertLong( final @Nonnull String column)
    {
        String type = dBase.getType();
        switch (type) 
        {
            case DataBase.TYPE_SYBASE:
                return "convert(" + longTypeName + ", " + column + ")";
            case DataBase.TYPE_POSTGRESQL:
            case DataBase.TYPE_ORACLE:
                return "to_number(" + column + ", '9999999999999999999')";
            case DataBase.TYPE_HSQLDB:
                return "convert( " + column + "," + longTypeName + ")";
            case DataBase.TYPE_MYSQL:
                return "cast(" + column + " AS UNSIGNED)";
            case DataBase.TYPE_MSSQL:
                return "cast(" + column + " AS bigint)";
            default:
                throw new RuntimeException( "can't convert to long for database type " + type);
        }
    }

    /**
     * Get the function for this database.
     *
     * @param name our function name
     *
     * @return the sql function name.
     */
    @CheckReturnValue @Nonnull
    public String getSQLFunction( final @Nonnull String name)
    {
        String res;

        res = name;

        if( name.equalsIgnoreCase( FUNCTION_LENGTH))
        {
            String type=dBase.getType();
            if( type.equals( DataBase.TYPE_MSSQL))// || type.equals( DataBase.TYPE_MSSQL_NATIVE))
            {
                res = "LEN";
            }
        }

        return res;
    }

    /**
     * Get the field modifier for this database.
     *
     * @param modifier the modifier
     *
     * @return the sql modifier name.
     */
    @CheckReturnValue @Nonnull
    public String getFieldModifier( final @Nonnull String modifier)
    {
        String res;

        res = modifier;

        if( modifier.equalsIgnoreCase( FIELD_MODIFIER_NULL))
        {
            if( dBase.getType().equals(DataBase.TYPE_DERBY))
            {
                res = "";
            }
        }

        return res;
    }

    /**
     * List All tables names. We don't return the ConcurrentHashMap as it
     * caused too many problems with other programs creating an enumeration
     * of the tables which then changes in the background.
     *
     * @throws java.sql.SQLException A SQL Exception
     * @return A list table.
     */
    @CheckReturnValue @Nonnull
    public synchronized String[] listTables() throws SQLException
    {
        ConcurrentHashMap table = getTables();

        String list[] = new String[ table.size()];

        Enumeration keys = table.keys();

        for(int i =0; keys.hasMoreElements(); i++)
        {
            list[i] = (String)keys.nextElement();
        }

        return list;
    }

    @CheckReturnValue @Nonnull
    private ConcurrentHashMap getTables() throws SQLException
    {
        ConcurrentHashMap holder[] = holderAllTables;

        if( holder == null)
        {
            holder = new ConcurrentHashMap[1];
            synchronized( this)
            {
                holderAllTables = holder;
            }
        }

        ConcurrentHashMap tables = holder[0];

        if( tables == null)
        {
            tables = new ConcurrentHashMap();

            Connection connection = null;//NOPMD
            ResultSet r = null;
            try
            {
                Lap start=new Lap();

                try
                {
                    connection = dBase.checkOutConnection();
                }
                catch( Exception e)
                {
                    if( e instanceof SQLException ) throw (SQLException)e;//NOPMD

                    throw new SQLException( e.toString());
                }

                String usertables[] = {"TABLE"};

                DatabaseMetaData dbMeta;

                dbMeta = connection.getMetaData();
                String dbType =dBase.getType();

                String schema=null;
                if( dbType.equals( DataBase.TYPE_ORACLE))
                {
                    schema=dBase.getUser().toUpperCase();
                }
                String catalog=connection.getCatalog();
                r = dbMeta.getTables( catalog, schema, "%", usertables);

                while(r.next())
                {
                    String name;

                    name = r.getString("table_name").trim().toLowerCase();
                    tables.put( name, "");
                }
                r.close();
                r=null;
                CSQL.recordTime( "getTables( null, " + schema + ",%,{TABLE})", start, connection, null, tables.size(), dBase);

                synchronized( this)
                {
                    holder[0] = tables;
                }

                dBase.checkInConnection( connection);
                connection=null;
            }
            finally
            {
                try
                {
                    if( r != null) r.close();
                }
                catch( SQLException e)
                {
                    LOGGER.warn( "closing result set", e);
                }

                LinkManager.killClient( connection);
            }
        }

        return tables;
    }

    @CheckReturnValue @Nonnull
    private HashMap<String,String> getProcedures() throws SQLException
    {
        HashMap<String,String> holder[] = holderAllProcedures;

        if( holder == null)
        {
            holder = new HashMap[1];
            synchronized( this)
            {
                holderAllProcedures = holder;
            }
        }

        HashMap<String,String> procedures = holder[0];

        if( procedures == null)
        {
            procedures = HashMapFactory.create();

            Connection connection = null;//NOPMD
            ResultSet r = null;
            try
            {
                Lap start=new Lap();

                try
                {
                    connection = dBase.checkOutConnection();
                }
                catch( Exception e)
                {
                    if( e instanceof SQLException ) throw (SQLException)e;//NOPMD

                    throw new SQLException( e.toString());
                }

                DatabaseMetaData dbMeta;

                dbMeta = connection.getMetaData();
                String dbType =dBase.getType();

                String schema=null;
                if( dbType.equals( DataBase.TYPE_ORACLE))
                {
                    schema=dBase.getUser();
                }
                String catalog=connection.getCatalog();
                r = dbMeta.getProcedures( catalog, schema, "%");

                while(r.next())
                {
                    String name;

                    name = r.getString("procedure_name").trim().toLowerCase();
                    procedures.put( name, "");
                }
                r.close();
                r=null;
                CSQL.recordTime( "getProcedures( null, " + schema + ",%)", start, connection, null, procedures.size(), dBase);

                assert ThreadCop.monitor(procedures, MODE.READONLY);

                synchronized( this)
                {
                    holder[0] = procedures;
                }

                dBase.checkInConnection( connection);
                connection=null;
            }
            finally
            {
                try
                {
                    if( r != null) r.close();
                }
                catch( SQLException e)
                {
                    LOGGER.warn( "closing result set", e);
                }

                LinkManager.killClient( connection);
            }
        }

        return procedures;
    }

    /**
     * List All tables names. We don't return the ConcurrentHashMap as it
     * caused too many problems with other programs creating an enumeration
     * of the tables which then changes in the background.
     *
     * @param tableName The table to check
     * @throws java.sql.SQLException A SQL exception
     *
     * @return The list of indexes for the table.
     */
    @CheckReturnValue @Nonnull
    public synchronized IndexInfo[] getIndexes(final @Nonnull String tableName) throws SQLException
    {
        String tableKey = tableName.toLowerCase();
        IndexInfo[] indexes = holdIndexes.get( tableKey);

        if( indexes == null)
        {
            ArrayList<IndexInfo> list=new ArrayList<>();

            if( doesTableExist( tableKey))
            {
                Connection connection = null;//NOPMD
                ResultSet indexies = null;

                try
                {
                    Lap start=new Lap();

                    try
                    {
                        connection = dBase.checkOutConnection();
                    }
                    catch( Exception e)
                    {
                        if( e instanceof SQLException ) throw (SQLException)e;//NOPMD

                        throw new SQLException( e.toString());
                    }

                    String searchTableName = tableKey;
                    String dbType =dBase.getType();
                    String schema=null;
                    switch (dbType) 
                    {
                        case DataBase.TYPE_HSQLDB:
                            searchTableName = tableKey.toUpperCase();
                            break;
                        case DataBase.TYPE_ORACLE:
                            schema=dBase.getUser();
                            searchTableName = tableKey.toUpperCase();
                            break;
                    }

                    DatabaseMetaData dbMeta;

                    dbMeta = connection.getMetaData();
                    String catalog=connection.getCatalog();
                    indexies = dbMeta.getIndexInfo(
                        catalog,               // catalog,
                        schema,             // schema,
                        searchTableName,    // table
                        false,              // unique,
                        true                // approximate
                    );

                    HashMap<String, IndexInfo>  map = HashMapFactory.create();
                    while( indexies.next())
                    {
                        String  iName;

                        iName = indexies.getString( "INDEX_NAME");
                        if( iName == null)
                        {
                            // Sybase return some indexes which are null maybe the table key ?
                            continue;
                        }

                        IndexInfo info = map.get(iName);
                        if( info == null)
                        {
                            short type = indexies.getShort( "TYPE");

                            boolean unique = !indexies.getBoolean("NON_UNIQUE");

                            info=new IndexInfo( iName, type, unique );
                            list.add(info);
                            map.put(iName, info);
                        }

                        String columnName = indexies.getString("COLUMN_NAME");
                        IndexColumnInfo.ORDER order;
                        if( "D".equalsIgnoreCase( indexies.getString("ASC_OR_DESC")))
                        {
                            order = IndexColumnInfo.ORDER.DESCENDING;
                        }
                        else
                        {
                            order = IndexColumnInfo.ORDER.ASSENDING;
                        }
                        int seq = indexies.getInt("ORDINAL_POSITION");

                        info.addColumn(columnName, seq, order);
                    }

                    indexies.close();
                    indexies = null;

                    CSQL.recordTime( "getIndexInfo( null," + schema + ","+ searchTableName +",false,true)", start, connection, null, list.size(), dBase);

                    dBase.checkInConnection( connection);
                    connection = null;
                }
                catch( SQLException e)
                {
                    LOGGER.warn( "could not get indexes for " + tableName, e);
                    throw e;
                }
                finally
                {
                    if( indexies != null) indexies.close();

                    if( connection != null)
                    {
                        LinkManager.killClient( connection);
                        clearCache();
                    }
                }
            }

            indexes = new IndexInfo[list.size()];
            list.toArray(indexes);
            for( IndexInfo info: indexes)
            {
                info.complete();
            }

            holdIndexes.put( tableKey, indexes);
        }

        return indexes.clone();
    }

    /**
     * Add a column to a table.
     *
     * @param pTableName The table to add the column.
     * @param pColumnName The column to add.
     * @param type The column type.
     *
     * @param nullable the field is nullable
     * @throws java.sql.SQLException A SQL exception
     */
    public void alterColumn( final @Nonnull String pTableName, final @Nonnull String pColumnName, final @Nonnull String type, boolean nullable) throws SQLException
    {
        // ALL tables/columns within this system will be lower case ( some databases don't support mix cases)
        String tableName = pTableName.toLowerCase();
        String columnName = pColumnName.toLowerCase();

        if( doesTableExist( tableName) == false)
        {
            throw new SQLException( "Can't add column '" + tableName + "." + columnName + "' as table doesn't exists");
        }

        if( doesColumnExist( tableName, columnName) == false)
        {
            throw new SQLException( "Can't alter column '" + tableName + "." + columnName + "' as column DOESN'T exists");
        }

        StringBuilder buffer = new StringBuilder( "ALTER TABLE ");

        buffer.append( tableName);
        buffer.append( " MODIFY ");

        if( dBase.getType().equals( "HSQLDB"))
        {
            buffer.append( " COLUMN ");
        }

        buffer.append( columnName);
        buffer.append( " ");
        buffer.append( type);

        if( nullable)
        {
            String fieldMod = getFieldModifier( TableUtil.FIELD_MODIFIER_NULL);
            if( StringUtilities.isBlank( fieldMod) == false)
            {
                buffer.append( " ");
                buffer.append( fieldMod);
            }
        }

        boolean clean = false;

        try
        {
            TableUtilRunner.perform(
                dBase,
                buffer.toString()
            );
            clean = true;
        }
        finally
        {
            if( clean)
            {
                synchronized( this)
                {
                    // Clear columns cache.
                    cacheColumns.remove( tableName);
                    holdIndexes.remove( tableName);
                }
            }
            else
            {
                clearCache();
            }
        }
    }

    /**
     * Add a column to a table.
     *
     * @param pTableName The table to add the column.
     * @param pColumnName The column to add.
     * @param type The column type.
     *
     * @throws java.sql.SQLException A SQL exception
     */
    public void addColumn( final @Nonnull String pTableName, final @Nonnull String pColumnName, final @Nonnull String type) throws SQLException
    {
        // ALL tables/columns within this system will be lower case ( some databases don't support mix cases)
        String tableName = pTableName.toLowerCase();
        String columnName = pColumnName.toLowerCase();

        if( doesTableExist( tableName) == false)
        {
            throw new SQLException( "Can't add column '" + tableName + "." + columnName + "' as table doesn't exists");
        }

        if( doesColumnExist( tableName, columnName))
        {
            throw new SQLException( "Can't add column '" + tableName + "." + columnName + "' as column already exists");
        }

        StringBuilder buffer = new StringBuilder( "ALTER TABLE ");

        buffer.append( tableName);
        buffer.append( " ADD ");

        if( dBase.getType().equals( "HSQLDB"))
        {
            buffer.append( " COLUMN ");
        }

        buffer.append( columnName);
        buffer.append( " ");
        buffer.append( type);
        String fieldMod = getFieldModifier( TableUtil.FIELD_MODIFIER_NULL);
        if( StringUtilities.isBlank( fieldMod) == false)
        {
            buffer.append( " ");
            buffer.append( fieldMod);
        }
        boolean clean = false;

        try
        {
            TableUtilRunner.perform(
                dBase,
                buffer.toString()
            );
            clean = true;
        }
        finally
        {
            if( clean)
            {
                synchronized( this)
                {
                    // Clear columns cache.
                    cacheColumns.remove( tableName);
                }
            }
            else
            {
                clearCache();
            }
        }
    }

    /**
     * Update stats
     *
     * @param tableName The table name
     * @throws Exception A serious problem
     */
    public void updateStats( final @Nonnull String tableName) throws Exception
    {
        // ALL tables/columns within this system will be lower case ( some databases don't support mix cases)
        String name = tableName.toLowerCase();

        if( doesTableExist( name) == false)
        {
            throw new Exception( "Can't update stats '" + name + "' as table doesn't exist");
        }


        StringBuilder buffer = null;

        if( dBase.getType().equals( DataBase.TYPE_SYBASE))
        {
            buffer = new StringBuilder( "UPDATE STATISTICS ");

            buffer.append( tableName);
        }

        if( buffer != null)
        {
            TableUtilRunner.perform(
                dBase,
                buffer.toString()
            );
        }
    }

    /**
     * Rename a table.
     * @param oldTableName The old table.
     * @param newTableName The new table
     * @throws java.sql.SQLException A SQL exception
     */
    public synchronized void renameTable( final @Nonnull String oldTableName, final @Nonnull String newTableName) throws SQLException
    {
        String oldName = oldTableName.toLowerCase();
        String newName = newTableName.toLowerCase();

        if( doesTableExist( oldName) == false)
        {
            throw new SQLException( "Can't rename table '" + oldName + "' -> '" + newName + "' as source table doesn't exist {" + dBase.getTypeKey() + "}");
        }

        if( doesTableExist( newName))
        {
            throw new SQLException( "Can't rename table '" + oldName + "' -> '" + newName + "' as target table exists {" + dBase.getTypeKey() + "}");
        }

        StringBuilder buffer = new StringBuilder();

        if(
            dBase.getType().equals( DataBase.TYPE_SYBASE)
        )
        {
            buffer.append( "sp_rename ");
            buffer.append( oldName);
            buffer.append( ",");
            buffer.append( newName);
        }else if(
            dBase.getType().startsWith( DataBase.TYPE_MSSQL)
        )
        {
            buffer.append( "sp_rename ");
            buffer.append( "'");
            buffer.append( oldName);
            buffer.append( "'");
            buffer.append( ",");
            buffer.append( "'");
            buffer.append( newName);
            buffer.append( "'");
        }
        else
        {
            buffer.append( "ALTER TABLE ");
            buffer.append( oldName);
            buffer.append( " RENAME TO ");
            buffer.append( newName);
        }

        boolean clean = false;
        try
        {
            TableUtilRunner.perform(
                dBase,
                buffer.toString()
            );

            getTables().remove( oldName);
            cacheColumns.remove( oldName);

            holdIndexes.remove( oldName);

            getTables().put( newName, "");
            cacheColumns.remove( newName);
            holdIndexes.remove( newName);
            clean=true;
        }
        finally
        {
            if( clean == false) clearCache();
        }
    }


    /**
     * Drop a column.
     * @param table The table to drop the column from
     * @param column The column to drop
     * @throws SQLException A serious problem
     */
    public synchronized void dropColumn( final @Nonnull String table, final @Nonnull String column) throws SQLException
    {
        // ALL tables/columns within this system will be lower case ( some databases don't support mix cases)
        String tableName = table.toLowerCase();
        String columnName = column.toLowerCase();

        if( doesTableExist( tableName) == false)
        {
            throw new SQLException( "Can't drop column '" + tableName + "." + columnName + "' as table doesn't exist");
        }

        if( doesColumnExist( tableName, columnName) == false)
        {
            throw new SQLException( "Can't drop column '" + tableName + "." + columnName + "' as column doesn't exist");
        }

        boolean clean = false;
        try
        {
            // Postgres can't drop columns
            if( dBase.getType().equals( "POSTGRESQL"))
            {
                iDropColumnPostgres( tableName, columnName);
            }
            else
            {
                StringBuilder buffer = new StringBuilder( "ALTER TABLE ");

                buffer.append( tableName);
                buffer.append( " DROP ");

                buffer.append( columnName);

                TableUtilRunner.perform(
                    dBase,
                    buffer.toString()
                );
            }

            // Clear columns cache.
            cacheColumns.remove( tableName);
            holdIndexes.remove( tableName);
            clean=true;
        }
        finally
        {
            if( !clean ) clearCache();
        }
    }

    /**
     * Rename a column
     *
     * @param tableName The table to rename the column.
     * @param oldName The old column name
     * @param newName The new column name
     * @param def The def
     *
     * @throws Exception A serious problem
     */
    public void renameColumn( final @Nonnull String tableName, final @Nonnull String oldName, final @Nonnull String newName, final @Nonnull String def) throws Exception
    {
        boolean clean = false;
        try
        {
            // ALL tables/columns within this system will be lower case ( some databases don't support mix cases)
            String tableKey = tableName.toLowerCase();
            String oldKey   = oldName.toLowerCase();
            String newKey   = newName.toLowerCase();

            if( doesTableExist( tableKey) == false)
            {
                throw new Exception( "Can't rename column '" + tableKey + "." + oldKey + "' as table doesn't exist");
            }

            if( doesColumnExist( tableKey, oldKey) == false)
            {
                throw new Exception( "Can't rename column '" + tableKey + "." + oldKey + "' as column doesn't exist");
            }

            if( doesColumnExist( tableKey, newKey))
            {
                throw new Exception( "Can't rename column '" + tableKey + "." + oldKey + "' -> '" + newKey + "' as target column exist");
            }

            if(
                dBase.getType().equals( DataBase.TYPE_SYBASE) ||
                dBase.getType().startsWith( DataBase.TYPE_MSSQL)
            )
            {
                StringBuilder buffer = new StringBuilder( "sp_rename '");

                buffer.append( tableKey);
                buffer.append( ".");

                buffer.append( oldKey);
                buffer.append( "', ");

                buffer.append( newKey);

                TableUtilRunner.perform(
                    dBase,
                    buffer.toString()
                );
            }
            else if( dBase.getType().equals( DataBase.TYPE_MYSQL))
            {
                StringBuilder buffer = new StringBuilder( "ALTER TABLE ");

                buffer.append( tableKey);
                buffer.append( " CHANGE ");

                buffer.append( oldKey);
                buffer.append( " ");
                buffer.append( newKey);
                buffer.append( " ");
                buffer.append( def);

                TableUtilRunner.perform(
                    dBase,
                    buffer.toString()
                );
            }
            else
            {
                StringBuilder buffer = new StringBuilder( "ALTER TABLE ");

                buffer.append( tableKey);
                buffer.append( " RENAME ");

                buffer.append( oldKey);
                buffer.append( " TO ");

                buffer.append( newKey);

                TableUtilRunner.perform(
                    dBase,
                    buffer.toString()
                );
            }

            // Clear columns cache.
            synchronized( this)
            {
                cacheColumns.remove( tableKey);
                holdIndexes.remove( tableKey);
            }

            clean=true;
        }
        finally
        {
            if( !clean) clearCache();
        }
    }

    
    /**
     * create a table
     * @param fieldList The field list
     * @param tableName The table to create.
     * @param primaryKey the primary key
     * @throws java.sql.SQLException a SQL exception
     */
    public void createTable( final @Nonnull String tableName, final @Nonnull String fieldList, final @Nullable String primaryKey) throws SQLException
    {
        iCreateTable(tableName, fieldList, primaryKey, 3);
    }
    
    /**
     * create a table
     * @param fieldList The field list
     * @param tableName The table to create.
     * @param primaryKey the primary key
     * @throws java.sql.SQLException a SQL exception
     */
    private void iCreateTable( final @Nonnull String tableName, final @Nonnull String fieldList, final @Nullable String primaryKey, final int retryCount) throws SQLException
    {
        if( doesTableExist( tableName))
        {
            LOGGER.info( "table already existed " + tableName);
            return;
        }
        boolean clean = false;
        StringBuilder sb=new StringBuilder(300);
        sb.append(createTableCmd);
        sb.append(tableName);
        sb.append(" ( \n");
        sb.append(fieldList.trim());

        String indexName;
        if( primaryKey!=null && StringUtilities.isBlank(primaryKey) == false)
        {
            sb.append( ",\n");
            if( dBase.getType().equals(DataBase.TYPE_SYBASE))
            {
                throw new SQLException( "can't name primary keys for Sybase on " + tableName);
            }

            sb.append( "CONSTRAINT ");
            indexName =tableName.trim().toUpperCase() + "_PK" ;
            sb.append( indexName);

            sb.append( " PRIMARY KEY(");
            sb.append(primaryKey.trim());
            sb.append( ")");
        }
        sb.append("\n)");

        if( dBase.getType().equals(DataBase.TYPE_ORACLE))
        {
            if( StringUtilities.isBlank(primaryKey) == false)
            {
                sb.append( "\nORGANIZATION INDEX\n");
            }
        }

        sb.append(createTableSuffix);

        try
        {
            TableUtilRunner.perform(
                dBase,
                sb.toString()
            );

            createdTable( tableName);

            clean = true;
        }
        catch( SQLException sqlE)
        {            
            if( retryCount > 0 && Thread.currentThread().isInterrupted() ==false)
            {
                LOGGER.warn( "retrying... " + sb, sqlE);
                try
                {
                    Thread.sleep((long) (1000 * Math.random()));
                }
                catch( InterruptedException ie)
                {
                    throw sqlE;
                }
                clearCache();
                
                iCreateTable(tableName, fieldList, primaryKey, retryCount - 1);
            }
            else
            {
                throw sqlE;
            }
        }
        finally
        {
            if( ! clean ) clearCache();
        }
    }

    /**
     * create an index on a table.
     *
     * @param tableName The table name
     * @param logicalIndexName the index name
     * @param unique is it unique ?
     * @param columns The columns
     *
     * @throws java.sql.SQLException A SQL exception
     */
    public void createIndex(
        final @Nonnull String tableName,
        final @Nonnull String logicalIndexName,
        final boolean unique,
        final @Nonnull String columns
    ) throws SQLException
    {
        if( doesTableExist( tableName) == false) return;

        String indexName = logicalIndexName;

        IndexInfo indexInfo = fetchIndexInfo( tableName, indexName);

        if( indexInfo != null && unique != indexInfo.unique)
        {
            /** My SQL does return unique correctly. */
            if( dBase.getType().equals(DataBase.TYPE_MYSQL) == false)
            {
                LOGGER.info( dBase.getShortUrl() + " INDEX " + tableName + "." + indexName + " EXPECTED UNIQUE=" + unique + " but was " + indexInfo.unique);
                indexInfo = null;
                dropIndex(tableName, indexName);
            }
        }

        if( indexInfo != null )
        {
            String checkedColumns = checkedIndex.get(indexName);
            if( checkedColumns == null || checkedColumns.equals( columns) == false)
            {
                IndexColumnInfo[] indexColumns = indexInfo.getColumns();

                String tmpColumns[] =columns.split(",");

                if( tmpColumns.length != indexColumns.length)
                {
                    LOGGER.info( dBase.getShortUrl() + " INDEX " + tableName + "." + indexName + " EXPECTED " + tmpColumns.length + " columns but was " + indexColumns.length);
                    indexInfo = null;
                    dropIndex(tableName, indexName);
                }
                else
                {
                    for( int c = 0; c < tmpColumns.length; c++)
                    {
                        String name = tmpColumns[c].trim();

                        int pos = name.indexOf(' ');

                        if( pos != -1)
                        {
                            name = name.substring(0, pos).trim();
                        }

                        if( name.equalsIgnoreCase( indexColumns[c].name) == false)
                        {
                            LOGGER.info(
                                dBase.getShortUrl() + " INDEX " + tableName + "." + indexName +
                                " EXPECTED " + name + " at position " + indexColumns[c].seq + " but was " + indexColumns[c].name
                            );
                            indexInfo = null;
                            dropIndex(tableName, indexName);
                            break;
                        }
                    }
                }

                if( indexInfo != null)
                {
                    checkedIndex.put(indexName, columns);
                }
            }
        }

        if( indexInfo == null)
        {
            StringBuilder buffer = new StringBuilder( "CREATE ");

            if( unique )
            {
                buffer.append( "UNIQUE ");
                String type = dBase.getType();

                if(
                    type.equals( DataBase.TYPE_SYBASE) ||
                    type.startsWith( DataBase.TYPE_MSSQL)
                )
                {
                    buffer.append( "CLUSTERED ");
                }
            }

            buffer.append( "INDEX ");
            buffer.append( indexName);
            buffer.append( " ON ");
            buffer.append( tableName);
            buffer.append( "(");

            buffer.append( columns);
            buffer.append( ")");
            if( StringUtilities.isBlank(createIndexSuffix) == false)
            {
                buffer.append( " ");
                buffer.append( createIndexSuffix.trim());
            }

            try
            {
                TableUtilRunner.perform(
                    dBase,
                    buffer.toString()
                );
                LOGGER.debug("create index, sql:"+ buffer.toString());
                checkedIndex.put(indexName, columns);
                synchronized( this )
                {
                    holdIndexes.remove( tableName.toLowerCase());
                }
            }
            catch( SQLException e)
            {
                if( e instanceof SQLException) throw e;//NOPMD
                throw new SQLException( e.toString());
            }
            finally
            {
                // just clear it, reselect later
                String tableKey = tableName.toLowerCase();

                holdIndexes.remove( tableKey);
            }
        }
    }

    /**
     * Drop a table
     *
     * @param tableName The table name to drop
     * @throws Exception A serious problem
     */
    public void dropTable( final String tableName) throws Exception
    {
        String tmpName = tableName.toLowerCase();
        if( doesTableExist(tmpName))
        {
            String statement;

            statement = "DROP TABLE " + tmpName;

            String type = dBase.getType();

            if(
                type.equals( DataBase.TYPE_POSTGRESQL) &&
                "7.1".equals( CProperties.getProperty( "POSTGRESQL_VERSION")) == false
            )
            {
                statement += " CASCADE";
            }

            if( type.equals( DataBase.TYPE_ORACLE))
            {
                statement += " PURGE";
            }

            boolean clean = false;
            try
            {
                TableUtilRunner.perform(
                    dBase,
                    statement
                );

                synchronized( this)
                {
                    if( holderAllTables != null)
                    {
                        ConcurrentHashMap tables= holderAllTables[0];
                        if( tables != null)
                        {
                            tables.remove(tmpName);
                        }
                    }

                    HashMap tmpTable;

                    tmpTable = holdIndexes;
                    if( tmpTable != null) tmpTable.remove( tmpName);

                    HashMap map = cacheColumns;
                    if( map != null) map.remove( tmpName);
                }
                clean = true;
            }
            finally
            {
                if( !clean ) clearCache();
            }
        }
    }

    /**
     * Drop temporary tables (starts with zz)
     *
     * @throws Exception A serious problem
     */
    public void dropZZTables() throws Exception
    {

        String tables[] = listTables();
        for (String table : tables) 
        {
            if (table.startsWith("zz_")) {
                dropTable(table);
            }
        }

    }

    /**
     * drop all the indexes for a table
     *
     * @param tableName The table name
     * @throws Exception A serious problem
     */
    public void dropAllIndexes( final @Nonnull String tableName) throws Exception
    {
        if( doesTableExist( tableName))
        {
            IndexInfo indexes[] = getIndexes( tableName);

            for(IndexInfo info: indexes)
            {
                dropIndex( tableName, info.name);
            }
        }
    }

    /**
     * Drop an index for a table.
     *
     * @param tableName The table name to drop the index from.
     * @param indexName The index to drop
     * @throws java.sql.SQLException A SQL exception
     */
    public void dropIndex( final @Nonnull String tableName, final @Nonnull String indexName) throws SQLException
    {
        if( doesTableIndexExist(tableName, indexName))
        {
            checkedIndex.remove(indexName);

            String statement;

            String type = dBase.getType();
            if(
                type.equals( DataBase.TYPE_SYBASE) ||
                type.startsWith( DataBase.TYPE_MSSQL)
            )
            {
                statement = "DROP INDEX " + tableName.toLowerCase() + "." + indexName.toLowerCase();
            }
            else if( type.equals( DataBase.TYPE_MYSQL))
            {
                statement = "DROP INDEX " + indexName.toLowerCase() + " ON " + tableName.toLowerCase();
            }
            else
            {
                statement = "DROP INDEX " + indexName.toLowerCase();
            }

            boolean clean = false;
            try
            {
                holdIndexes.remove( tableName.toLowerCase());
                TableUtilRunner.perform(
                    dBase,
                    statement
                );

                holdIndexes.remove( tableName.toLowerCase());
                clean = true;
            }
            finally
            {
                if( !clean ) clearCache();
            }
        }
    }

    /**
     * Rename an index for a table.
     *
     * @param tableName The table name to drop the index from.
     * @param oldIndexName The old index name
     * @param newIndexName The new index name
     */
    public void renameIndex(final @Nonnull String tableName, final @Nonnull String oldIndexName, final @Nonnull String newIndexName)
    {
        String statement;
//            String statement1 = "";

        String type = dBase.getType();
        if(
            type.equals( DataBase.TYPE_SYBASE)
        )
        {
            statement = "ALTER INDEX " + oldIndexName.toLowerCase() + " ON " + tableName.toLowerCase() +
                    " RENAME TO " + newIndexName.toLowerCase();
        }
        else if(
            type.startsWith( DataBase.TYPE_MSSQL)
        )
        {
            //statement  = "ALTER INDEX " + oldIndexName.toLowerCase() + " ON " + tableName.toLowerCase();
            statement = "SP_RENAME '" + tableName.toLowerCase() +"." + oldIndexName.toLowerCase() + "','" + newIndexName.toLowerCase() + "','INDEX'";
        }
        else if( type.equals( DataBase.TYPE_MYSQL))
        {
            LOGGER.info("Cannot alter index name for MSACCESS or MYSQL");
            return;
        }
        else // POSTGRES...
        {
            statement = "ALTER INDEX " + oldIndexName + " RENAME TO " + newIndexName;
        }

        try
        {
            TableUtilRunner.perform(
                  dBase,
                  statement
            );
        }
        catch (SQLException e)
        {
            String msg="Renaming index " + oldIndexName;
            LOGGER.warn(msg, e);
            
            assert false: msg;
        }
    }

    /**
     * get the text type.
     *
     * @return The text type.
     */
    @CheckReturnValue
    public String getTextTypeName()
    {
        return textTypeName;
    }

    /**
     * get the long type.
     *
     * @return The long type.
     */
    @CheckReturnValue
    public String getLongTypeName()
    {
        return longTypeName;
    }

    /**
     * get the double type.
     *
     * @return The double type.
     */
    @CheckReturnValue
    public String getDoubleTypeName()
    {
        return float8TypeName;
    }

    @SuppressWarnings("empty-statement")
    private TableUtil( final @Nonnull DataBase dBase)
    {
        this.dBase = dBase;

        String tempLongTypeName = "BIGINT";
        String tempDoubleTypeName = "FLOAT(8)";
        String tempTextTypeName = "TEXT";
        String tempCreateTableCmd = "CREATE TABLE ";
        String tempCreateTableEndCmd = "";

        String dbType = dBase.getType();
        switch (dbType)
        {
            case DataBase.TYPE_SYBASE:
                tempLongTypeName = "NUMERIC( 20)";
                break;
            case DataBase.TYPE_ORACLE:
                tempLongTypeName = "NUMERIC( 20)";
                tempTextTypeName = "CLOB";
                break;
            case "HSQLDB":
                tempTextTypeName = "LONGVARCHAR";
                tempCreateTableCmd = "CREATE CACHED TABLE ";
                break;
            case DataBase.TYPE_MYSQL:
                tempCreateTableEndCmd = "ENGINE = InnoDB";
                try
                {
                    int major = dBase.getMajorVersion();
                    int minor = dBase.getMinorVersion();
                    if( major > 0)
                    {
                        if( major < 4 || (major == 4 && minor == 0))
                        {
                            tempCreateTableEndCmd = "TYPE = InnoDB";
                        }
                    }
                }
                catch( Exception e)
                {
                    ;
                }   
                break;
            case DataBase.TYPE_DERBY:
                tempTextTypeName = "CLOB";
                break;
        }

        String tempCreateTableSuffix=dBase.getCreateTableSuffix();
        if( StringUtilities.isBlank(tempCreateTableSuffix) == false)
        {
            tempCreateTableEndCmd += " " + tempCreateTableSuffix;
        }
        String tempCreateIndexSuffix="";

        String temp=dBase.getCreateIndexSuffix();
        if( StringUtilities.isBlank(temp) == false)
        {
            tempCreateIndexSuffix += " " + temp;
        }

        longTypeName =      tempLongTypeName;
        float8TypeName =    tempDoubleTypeName;
        textTypeName =      tempTextTypeName;
        createTableCmd =    tempCreateTableCmd;
        createTableSuffix = tempCreateTableEndCmd;
        createIndexSuffix=  tempCreateIndexSuffix;

        assert ThreadCop.monitor(cacheColumns, MODE.EXTERNAL_SYNCHRONIZED);
    }

    /**
     * If a table is created then clear what was known about the table.
     * @param tableName the table is created.
     * @throws java.sql.SQLException A SQL exception
     */
    public void createdTable( final @Nonnull String tableName) throws SQLException
    {
        String tableKey = tableName.toLowerCase();

        synchronized( this)
        {
            getTables().put( tableKey, "");
            cacheColumns.remove( tableKey);
        }
    }

    private void iDropColumnPostgres( final @Nonnull String tableName, final @Nonnull String columnName) throws SQLException
    {
        String tempName;

        for( int i = 0; true; i++)
        {
            tempName = "zz_drop_column";

            if( i != 0) tempName += "_" + i;

            if( doesTableExist( tempName) == false) break;
        }

        StringBuilder buffer = new StringBuilder( "SELECT ");

        CSQL sql = new CSQL( dBase);

        sql.perform("SELECT * FROM " + tableName + " WHERE 0=1");

        boolean found = false;

        for( int c = 1; c <= sql.getColumnCount(); c++)
        {
            String name = sql.getColumnName(c).toLowerCase();

            if( name.equals( columnName)) continue;
            if( found) buffer.append( ",");
            found = true;
            buffer.append( name);
        }

        buffer.append( "\nINTO ");
        buffer.append( tempName);
        buffer.append( "\nFROM ");
        buffer.append( tableName);
        TableUtilRunner.perform(
            dBase,
            buffer.toString()
        );
        createdTable( tempName);

        String oldTable;

        for( int i = 0; true; i++)
        {
            oldTable = "z" + tableName;

            if( i != 0) oldTable += "_" + i;

            if( doesTableExist( oldTable) == false) break;
        }

        renameTable( tableName, oldTable);
        renameTable( tempName, tableName);
        String tableKey=tableName.toLowerCase();
        IndexInfo indexes[] = getIndexes( oldTable);

        for( IndexInfo info: indexes)
        {
            dropIndex( oldTable, info.name);
        }

        synchronized( this)
        {
            cacheColumns.remove( tableKey);
            holdIndexes.remove( tableKey);
        }
    }

    private final String longTypeName;
    private final String float8TypeName;
    private final String textTypeName;
    private final String createTableCmd;
    private final String createTableSuffix;
    private final String createIndexSuffix;

    private final DataBase dBase;

    /** Holds the tableUtil for each dataBase.*/
    private static final ConcurrentHashMap<DataBase,TableUtil>    DATABASE_MAP      = new ConcurrentHashMap();

    private HashMap<String, String>                    holderAllProcedures[];
    private ConcurrentHashMap                          holderAllTables[]; // this holds the full list of tables
    private final HashMap<String, IndexInfo[]>         holdIndexes = new  HashMap <>();
    private final HashMap                              cacheColumns = HashMapFactory.create();
    private final ConcurrentHashMap<String, String>    checkedIndex=new ConcurrentHashMap<>();
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.TableUtil");//#LOGGER-NOPMD

    private static final MemoryHandler MEMORY_HANDLER=new MemoryHandler() {
        @Override
        public MemoryHandler.Cost getCost() {
            return MemoryHandler.Cost.MEDIUM_HIGH;
        }

        @Override
        public long freeMemory(double percentage) {
            return queuedFreeMemory(percentage);
        }

        @Override
        public long tidyUp() {
            return 0;
        }

        @Override
        public long queuedFreeMemory(double percentage) {
            if( percentage>0)
            {
                long estimate=getEstimatedSize();
                DATABASE_MAP.clear();
                
                return estimate;
            }
            
            return 0;
        }

        @Override
        public long panicFreeMemory() {
            return queuedFreeMemory( 1);
        }

        @Override
        public long getEstimatedSize() {
            return DATABASE_MAP.size() * 1024 * 1024;
        }

        @Override
        public long getLastAccessed() {
            return System.currentTimeMillis();
        }
    };
    
    static{
        MemoryManager.register(MEMORY_HANDLER);
    }
}
