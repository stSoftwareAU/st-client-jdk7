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

import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import java.sql.*;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;

/**
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *  @author      Jason McGrath
 *  @since       March 4, 2002, 5:15 pm
 */
public final class ColumnType
{
    public final static String STD_INTEGER="INTEGER";
    public final static String STD_INT="INT";
    public final static String STD_BIGINT="BIGINT";
    public final static String STD_FLOAT="FLOAT";
    public final static String STD_DOUBLE="DOUBLE";
    public final static String STD_CHAR="CHAR";
    public final static String STD_CLOB="CLOB";
    public final static String STD_LONGVARCHAR="LONGVARCHAR";
    public final static String STD_NUMERIC="NUMERIC";
    public final static String STD_VARCHAR="VARCHAR";
    public final static String STD_REAL="REAL";
    
    /**
     *
     * @param dataBase the database
     * @param typeName the type name
     * @param type the type
     * @throws SQLException a serious problem
     * @return the data type
     */
    @CheckReturnValue @Nonnull
    public static ColumnType findType( final DataBase dataBase, final String typeName, final int type) throws SQLException
    {
        HashMap<String, ColumnType> types;
        types = getTypeList( dataBase);

        ColumnType t;

        t = types.get( typeName.toLowerCase());

        if( t != null) return t;

        t = types.get( Integer.toString(type));

        if (t == null)
        {
            throw new SQLException( "Invalid type: " + type);
        }

        return t;
    }

    /**
     *
     * @param dataBase the database
     * @param oTypeName the type
     * @throws Exception a serious problem
     * @return the data type
     */
    @CheckReturnValue @Nonnull
    public static ColumnType findType( DataBase dataBase, String oTypeName) throws Exception
    {
        String typeName = oTypeName;

        HashMap<String, ColumnType> types;
        types = getTypeList( dataBase);

        String dbType = dataBase.getType();
        if( dbType.equals( DataBase.TYPE_POSTGRESQL))
        {
            if( typeName.equalsIgnoreCase( "OID"))
            {
                typeName = "INT8";
            }
        }
        else if( dbType.equals( DataBase.TYPE_HSQLDB))
        {
            if( typeName.equalsIgnoreCase( "LONGVARCHAR"))
            {
                typeName = "TEXT";
            }
        }
        else if( dataBase.getType().equals( "MYSQL"))
        {
            if( typeName.equalsIgnoreCase( "LONG"))
            {
                typeName = "BIGINT";
            }
            else if( typeName.equalsIgnoreCase( "LONGLONG"))
            {
                typeName = "BIGINT";
            }
            else if ( typeName.equalsIgnoreCase("SHORT"))
            {
                typeName = "SMALLINT";
            }
        }

        ColumnType t;

        t = types.get( typeName.toLowerCase());

        if (t == null)
        {
            throw new SQLException( "Invalid type: " + typeName);
        }

        return t;
    }

    /**
     *
     * @param dataBase the database
     * @param typeName the type
     * @param oSize the size
     * @param oScale the scale
     * @param nullable is nullable
     * @param defaultValue default value
     * @throws Exception a serious problem
     * @return the param
     */
    @CheckReturnValue @Nonnull
    public static String getCreateParam(
        final DataBase dataBase,
        final String typeName,
        final String oSize,
        final String oScale,
        final String nullable,
        final String defaultValue
    ) throws Exception
    {
        String sqlDefault = defaultValue;
        String size = oSize;
        String scale = oScale;
        String stdTypeName = typeName;
        StringBuilder sb = new StringBuilder("");

        stdTypeName = stdTypeName.toUpperCase();

        if( stdTypeName.equals("BPCHAR") || stdTypeName.equalsIgnoreCase("CHARACTER"))
        {
            stdTypeName="CHAR";
        }

        String dbType = dataBase.getType();
        // Convert standard types to another if it is not supported by this database
        if( dbType.equals(DataBase.TYPE_SYBASE))
        {
            switch (stdTypeName) {
                case "BIGINT":
                    stdTypeName = getStdTypeName( Types.NUMERIC);
                    size = "20";
                    break;
                case STD_DOUBLE:
                    stdTypeName = STD_FLOAT;
                    size = "8";
                    break;
                case "CLOB":
                    stdTypeName = "TEXT";
                    size = "-1";
                    break;
                default:
                    break;
            }
        }       
        else if(dbType.equals(DataBase.TYPE_HSQLDB))
        {
            if( stdTypeName.equals( "NUMERIC") && size != null && size.equals( "20"))
            {
                stdTypeName = getStdTypeName( Types.BIGINT);
                size = "-1";
            }
            else if( stdTypeName.equals( "INT8")||stdTypeName.equals( "LONG"))
            {
                stdTypeName = getStdTypeName( Types.BIGINT);
                size = "-1";
            }
            else if( stdTypeName.equals( "INT4"))
            {
                stdTypeName = getStdTypeName( Types.INTEGER);
                size = "-1";
            }
            else if( stdTypeName.equals( "INT2"))
            {
                stdTypeName = getStdTypeName( Types.INTEGER);
                size = "-1";
            }
            else if( stdTypeName.equals( "FLOAT8"))
            {
                stdTypeName = getStdTypeName( Types.DOUBLE);
                size = "-1";
            }
            else if( stdTypeName.equals( "NUMBER"))
            {
                stdTypeName = getStdTypeName( Types.DOUBLE);
                size = "-1";
            }
            else if( stdTypeName.equals( "FLOAT4"))
            {
                stdTypeName = getStdTypeName( Types.FLOAT);
                size = "-1";
            }
            else if( stdTypeName.equals( "TEXT")||stdTypeName.equals( "LONGVARCHAR"))
            {
                stdTypeName = "CLOB";
                //size = "-1";
            }
            else if( stdTypeName.equals( "VARCHAR2"))
            {
                stdTypeName = "VARCHAR";
            }
            //else if( stdTypeName.equals( "LONGVARCHAR"))
            //{
            //    stdTypeName = "CLOB";
            //    size = "-1";
            //}
            // hsqldb does not accept default values
            sqlDefault = null;
        }
        else if( dbType.equals(DataBase.TYPE_POSTGRESQL))
        {
            if( stdTypeName.equals( "NUMERIC") && size != null && size.equals( "20"))
            {
                stdTypeName = getStdTypeName( Types.BIGINT);
                size = "-1";
            }
            else if( stdTypeName.equals( "BIGSERIAL"))
            {
                stdTypeName = getStdTypeName( Types.BIGINT);
            }
            else if( stdTypeName.equals( STD_DOUBLE))
            {
                stdTypeName = "FLOAT8";
                size = "-1";
            }
            else if( stdTypeName.equals( STD_FLOAT))
            {
                stdTypeName = "FLOAT8";
                size = "-1";
            }
            else if( stdTypeName.equals( "VARCHAR"))
            {
                if( size != null && size.length() > 4)// more than 10,000 convert to TEXT
                {
                    stdTypeName = "TEXT";
                    size = "-1";
                }
            }
            else if( stdTypeName.equals( "CLOB"))
            {
                stdTypeName = "TEXT";
                size = "-1";
            }
            else if( stdTypeName.equals( "LONGVARCHAR"))
            {
                stdTypeName = "TEXT";
                size = "-1";
            }
            else if( stdTypeName.equals( "LONGVARBINARY"))
            {
                stdTypeName = "TEXT";
                size = "-1";
            }
            else if( stdTypeName.equals( "BIGINT"))
            {
                stdTypeName = "INT8";
            }
            else if( stdTypeName.equals( STD_INTEGER) || stdTypeName.equals(STD_INT))
            {
                stdTypeName = "INT4";
            }
        }
        else if( dbType.equals("PERVASIVE"))
        {
            if( stdTypeName.equals( "VARCHAR") && size.equals( "255"))
            {
                stdTypeName = getStdTypeName( Types.CHAR);
                size = "255";
            }
            else if( stdTypeName.equals( "BIGINT"))
            {
                stdTypeName = getStdTypeName( Types.INTEGER);
                size = "8";
            }
        }
        else if( dbType.startsWith(DataBase.TYPE_MSSQL))
        {
            if( stdTypeName.equals( STD_FLOAT))
            {
                stdTypeName = getStdTypeName( Types.DOUBLE);
            }
            else if (stdTypeName.equals("VARCHAR") && (Integer.parseInt(size) > 8000))
            {
                size = "255";
            }
            else if( stdTypeName.equals( "LONGVARCHAR"))
            {
                //stdTypeName = getStdTypeName( Types.CLOB);
                stdTypeName = "TEXT";
            }
            else if( stdTypeName.equals( "INT2"))
            {
                stdTypeName = getStdTypeName( Types.INTEGER);
                size = "-1";
            }
            else if( stdTypeName.equals( "INT4"))
            {
                stdTypeName = getStdTypeName( Types.INTEGER);
                size = "-1";
            }
            else if( stdTypeName.equals( "INT8"))
            {
                stdTypeName = getStdTypeName( Types.BIGINT);
                size = "-1";
            }
            else if( stdTypeName.equals( "FLOAT8"))
            {
                stdTypeName = getStdTypeName( Types.DOUBLE);
                size = "-1";
            }
            else if( stdTypeName.equals( "FLOAT4"))
            {
                stdTypeName = getStdTypeName( Types.FLOAT);
                size = "-1";
            }
        }
        else if( dbType.equals("MYSQL"))
        {
            if( stdTypeName.equals( "VARCHAR") && StringUtilities.isBlank( size) )
            {
                stdTypeName = getStdTypeName( Types.VARCHAR);
                size = "255";
            }
            else if (stdTypeName.equals("VARCHAR") && (Integer.parseInt(size) > 21845))
            {
                size = "255";
            }
            else if( stdTypeName.equals( "LONGVARCHAR"))
            {
                stdTypeName = "TEXT";
                size = "-1";
            }
            else if( stdTypeName.equals( "LONGVARBINARY"))
            {
                stdTypeName = "TEXT";
                size = "-1";
            }
            else if (stdTypeName.equals( "NUMERIC") && size != null && size.equals( "20"))
            {
                stdTypeName = getStdTypeName( Types.BIGINT);
                size = "-1";
            }
            else if (stdTypeName.equals( "NUMERIC"))
            {
                if( StringUtilities.isBlank( size) == false)
                {
                    if( Integer.parseInt( size) > 64)
                    {
                        size = "64";
                    }
                }

                if(StringUtilities.isBlank( scale) == false)
                {
                    if( Integer.parseInt( scale) > 30)
                    {
                        scale = "30";
                    }
                }
            }
        }
        else if( dbType.equals(DataBase.TYPE_ORACLE))
        {
            switch (stdTypeName) {
                case STD_INTEGER:
                    return "INT " +  nullable;
                    //stdTypeName = getStdTypeName( Types.NUMERIC);
                    //size = "12";
                case "VARCHAR":
                    return stdTypeName + "(" + size + ") " + nullable;
                    //stdTypeName = getStdTypeName( Types.NUMERIC);
                    //size = "12";
                case "BIGINT":
                    return TableUtil.find(dataBase).getLongTypeName()+ " " + nullable;
                    //stdTypeName = getStdTypeName( Types.NUMERIC);
                    //size = "20";
                case "NUMERIC":
                    return stdTypeName + "(" + size + ") " + nullable;
                case "TIMESTAMP":
                    return "VARCHAR(255) " + nullable;
                case "BINARY":
                    // Size needs to be verifed. For now, set to match
                    // sybase BINARY size
                    return "RAW(255) " + nullable;
                default:
                    break;
            }
        }

        // Try and find a type with a localName matching the standard type
        ColumnType t;
        HashMap<String, ColumnType> types;
        types = getTypeList( dataBase);

        t = types.get( stdTypeName.toLowerCase());

        // If none found then search through all local types and find a match
        // on standard type
        // TODO : more accurate comparison can be made if more information supplied
        // such as autoincrement support, precision and so on

        if( t == null)
        {
            for(ColumnType tm: types.values())
            {
                if( tm.getName().contains("IDENTITY")) continue;
                String stdName=tm.getStdName();
                if( stdName.equalsIgnoreCase( stdTypeName))
                {
                    t = tm;
                    break;
                }
            }
        }

        if (t == null)
        {
            throw new SQLException( "Invalid type: " + stdTypeName);
        }

        // Build create statement field params
        if( t.doesAcceptsLength())
        {
            if( StringUtilities.isBlank( size) == false)
            {
                if( Integer.parseInt( size) > 0)
                {
                    if (sb.length() != 0)
                    {
                        sb.append(',');
                    }
                    sb.append( size);
                }
            }
        }

        if( t.doesAcceptsScale())
        {
            if( StringUtilities.isBlank( scale) == false)
            {
                if( Integer.parseInt( scale) > 0)
                {
                    if (sb.length() != 0)
                    {
                        sb.append(',');
                    }

                    sb.append( scale);
                }
            }
        }


        StringBuilder typeDef = new StringBuilder();

        typeDef.append( t.getName());

        if (sb.length() > 0)
        {
            typeDef.append( "(");
            typeDef.append( sb);
            typeDef.append( ")");
        }

        typeDef.append( " ");
        if( StringUtilities.isBlank(nullable))
        {
            String type = dataBase.getType();
            if( type.equals(DataBase.TYPE_DERBY) == false)
            {
                typeDef.append( "NULL");
            }
        }
        else
        {
            if(dataBase.getType().equals(DataBase.TYPE_ORACLE) &&
                    t.getStdName().equals("VARCHAR"))
            {
                typeDef.append( "NULL");
            }
            else
            {
                typeDef.append( nullable);
            }
        }

        if( StringUtilities.isBlank(sqlDefault) == false)
        {
            typeDef.append(" DEFAULT ");
            typeDef.append(sqlDefault);
        }

        return typeDef.toString();
    }

    /**
     *
     * @param type the type
     * @throws SQLException a serious problem
     * @return the type
     */
    @CheckReturnValue @Nonnull
    public static String getStdTypeName( final int type) throws SQLException
    {
        switch( type)
        {
            case Types.BIGINT: return STD_BIGINT;
            case Types.BINARY: return "BINARY";
            case Types.BIT: return "BIT";
            case Types.BLOB: return "BLOB";
            case Types.OTHER: return "BLOB";
            case Types.CHAR: return STD_CHAR;
            case Types.CLOB: return STD_CLOB;
            case Types.DATE: return "DATE";
            case Types.DECIMAL: return "DECIMAL";
            case Types.DOUBLE: return  STD_DOUBLE;
            case Types.FLOAT: return  STD_FLOAT;
            case Types.INTEGER: return  STD_INTEGER;
            case Types.LONGVARCHAR: return  STD_LONGVARCHAR;
            case Types.LONGVARBINARY: return  "LONGVARBINARY";
            case Types.NUMERIC: return  "NUMERIC";
            case Types.REAL: return  STD_REAL;
            case Types.SMALLINT: return  "SMALLINT";
            case Types.TIME: return  "TIME";
            case Types.TIMESTAMP: return  "TIMESTAMP";
            case Types.TINYINT: return  "TINYINT";
            case Types.VARBINARY: return  "VARBINARY";
            case Types.VARCHAR: return  STD_VARCHAR;
            default:
                throw new SQLException( "Unknown type " + type);
        }
    }

    private static void put( final HashMap<String, ColumnType> tmpTypes, final ColumnType t)
    {
        tmpTypes.put( t.getName().toLowerCase(), t);
        String id=Integer.toString(t.getType());

        if( tmpTypes.containsKey(id) == false)
        {
            tmpTypes.put( id, t);
        }
    }
    
    @CheckReturnValue @Nonnull
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static HashMap<String, ColumnType> getTypeList( final DataBase dataBase) throws SQLException
    {
        HashMap<String, ColumnType> types = DB_TYPES.get( dataBase.getType());
        if (types != null)//NOPMD
        {
            return types;
        }

        HashMap<String, ColumnType> tmpTypes = HashMapFactory.create();

        // Load some defaults
        ColumnType t = new ColumnType( dataBase, Types.NUMERIC, "NUMERICN", "", "", null);

        put( tmpTypes, t);
        t = new ColumnType( dataBase, Types.OTHER, "BLOB", "", "", null);

        put( tmpTypes, t);
        t = new ColumnType( dataBase, Types.CLOB, "CLOB", "", "", null);

        put( tmpTypes, t);
        //tmpTypes.put(t.get, types)

        t = new ColumnType( dataBase, Types.TIMESTAMP, "DATETIME", "", "", null);

        put( tmpTypes, t);

        String precis = "PRECIS";
        String dbType = dataBase.getType();

        if(
            dbType.startsWith( DataBase.TYPE_MSSQL) ||
            dbType.equals(DataBase.TYPE_ORACLE)
        )
        {
            precis = "PRECISION";
        }

        t = new ColumnType( dataBase, Types.DOUBLE, "DOUBLE " + precis, "", "", null);

        put( tmpTypes, t);

        Connection connection = null;//NOPMD
        ResultSet rs = null;
        try
        {
            try
            {
                connection = dataBase.checkOutConnection();
            }
            catch( Exception e)
            {
                LOGGER.warn( "could not check out", e);
                throw new SQLException( "could not check out");
            }
            DatabaseMetaData dbMeta;
            dbMeta = connection.getMetaData();

            rs = dbMeta.getTypeInfo();

            while (rs.next())
            {
                int type = rs.getInt( "DATA_TYPE");
                if( type == 1111) continue;
                if( type>2000) continue;
                String name = rs.getString( "TYPE_NAME");
                name = name.toUpperCase();
                String prefix = rs.getString( "LITERAL_PREFIX");
                String suffix = rs.getString( "LITERAL_SUFFIX");
                String createParams = rs.getString( "CREATE_PARAMS");
                //String localTypeName=rs.getString("LOCAL_TYPE_NAME");
//LOGGER.info( "type:" + type + ", name: " + name + ", prefix: " + prefix+ ", suffix: " + suffix+ ", createParams: " + createParams + ", LOCAL_TYPE_NAME:" + localTypeName);
                try
                {
                    if (name.equals("OID"))
                    {
                        continue;
                    }
                    getStdTypeName( type); // do we support this type
                    t = new ColumnType( dataBase, type, name, prefix, suffix, createParams);

                    put( tmpTypes, t);

                    //LOGGER.info("DataType:- name:" + name + " type:" + type + " stdName:" + stdName + " createP:" + createParams );
                }
                catch(SQLException e)
                {
                    LOGGER.debug("DataType is not supported. name:" + name + " type:" + type);//, e );
                }
            }
        }
        finally
        {
            try
            {
                if( rs != null) rs.close();
            }
            catch( SQLException e)
            {
                LOGGER.error( "oops", e);
            }
            dataBase.checkInConnection( connection);
        }

        DB_TYPES.put( dataBase.getType(), tmpTypes);

        return tmpTypes;
    }

    private ColumnType(DataBase dataBase, int type, String name, String prefix, String suffix, String createParams) throws SQLException
    {
        this.dataBase = dataBase;
        this.type = type;
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
        this.createParams = createParams;

        parseParams();
    }

    /**
     *
     * @return the name
     */
    @CheckReturnValue @Nonnull
    public String getName()
    {
        return name;
    }

    /**
     *
     * @return the type
     */
    @CheckReturnValue @Nonnull
    public int getType()
    {
        return type;
    }

    /**
     *
     * @throws Exception a serious problem
     * @return the name
     */
    @CheckReturnValue @Nonnull
    public String getStdName() throws Exception
    {
        return getStdTypeName( type);
    }

    /**
     * @param data the data that will be written
     * @return the prefix
     */
    @CheckReturnValue @Nonnull
    public String getTypePrefix( final String data)
    {
        if( data != null && data.contains("\\"))
        {
            return escapePrefix;
        }
        else
        {
            return prefix;
        }
    }

    /**
     *
     * @return the suffix
     */
    @CheckReturnValue @Nonnull
    public String getTypeSuffix()
    {
        return suffix;
    }

    /**
     *
     * @return the length
     */
    @CheckReturnValue
    public boolean doesAcceptsLength()
    {
        return acceptsLength;
    }

    /**
     *
     * @return the scale
     */
    @CheckReturnValue
    public boolean doesAcceptsScale()
    {
        return acceptsScale;
    }

    /**
     *
     * @throws SQLException a serious problem
     */
    private void parseParams() throws SQLException
    {
        if( prefix == null) prefix = "";
        if( escapePrefix == null) escapePrefix = "";
        if( suffix == null) suffix = "";
        String dbType = dataBase.getType();
        if(
            dbType.equals( DataBase.TYPE_POSTGRESQL)    ||
            dbType.equals( DataBase.TYPE_SYBASE)        ||
            dbType.equals( DataBase.TYPE_HSQLDB)        ||
            dbType.equals( DataBase.TYPE_MYSQL)         ||
            dbType.equals( DataBase.TYPE_MSSQL)         ||  //Adding MSSQL Support
            dbType.equals(DataBase.TYPE_ORACLE)
        )
        {
            // suffix / prefix
            switch(type)
            {
                case Types.CHAR:
                case Types.CLOB:
                case Types.DATE:
                case Types.BINARY:
                case Types.LONGVARCHAR:
                case Types.LONGVARBINARY:
                case Types.TIME:
                case Types.TIMESTAMP:
                case Types.VARCHAR:
                    prefix = "'";
                    if(dbType.equals( DataBase.TYPE_POSTGRESQL))
                    {
                        if( dataBase.checkVersion(8, 2))
                        {
                            escapePrefix="E'";
                        }
                        else
                        {
                            escapePrefix="'";
                        }
                    }
                    else
                    {
                        escapePrefix="'";
                    }
                    suffix = "'";
                    break;
                default:
                    prefix = "";
                    suffix = "";
            }

            // length required
            switch(type)
            {
                case Types.NUMERIC:
                case Types.DECIMAL:
                case Types.VARCHAR:
                case Types.CHAR:
                    acceptsLength = true;
                    break;
                default:
            }

            if(
                dbType.equals( DataBase.TYPE_POSTGRESQL) &&
                (
                    //name.equalsIgnoreCase( "bpchar") ||
                    name.equalsIgnoreCase( "text")
                )
            )
            {
                acceptsLength = false;
            }

            // scale required
            switch(type)
            {
                case Types.NUMERIC:
                case Types.DECIMAL:
                    acceptsScale = true;
                    break;
                default:
            }
        }
        else
        {
            if( createParams != null)
            {
                StringTokenizer st = new StringTokenizer( createParams, ",");
                while( st.hasMoreTokens())
                {
                    String tk = st.nextToken();
                    if(
                        tk.equalsIgnoreCase( "length") ||
                        tk.equalsIgnoreCase( "max length") ||
                        tk.equalsIgnoreCase( "precision")
                    )
                    {
                        acceptsLength = true;
                    }
                    else if( tk.equalsIgnoreCase( "scale"))
                    {
                        acceptsScale = true;
                    }
                }
            }
        }
    }

    /**
     * the string
     * @return string
     */
    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        String name1 = null;
        try
        {
            name1 = this.getStdName();
        }
        catch(Exception e)
        {
             LOGGER.warn( "getStdName() for " + this.name, e);
        }

        return "name=" + this.getName() + ", stdName=" + name1;
    }

    /** the type */
    public static final ConcurrentHashMap<String, HashMap<String, ColumnType>> DB_TYPES=new ConcurrentHashMap();

    private final int   type;

    private final String      name;
    private String escapePrefix="",
                        prefix,
                        suffix;
    private final String createParams;

    private final DataBase    dataBase;

    private boolean     acceptsLength,
                        acceptsScale;

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.ColumnType");//#LOGGER-NOPMD
}
