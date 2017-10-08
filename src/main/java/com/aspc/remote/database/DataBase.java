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
import com.aspc.remote.application.Shutdown;
import com.aspc.remote.database.internal.wrapper.WrapperAssertError;
import com.aspc.remote.database.internal.wrapper.WrapperConnection;
import com.aspc.remote.database.internal.wrapper.WrapperStatement;
import com.aspc.remote.util.links.*;
import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.util.misc.*;
import com.aspc.remote.util.net.NetUtil;
import com.aspc.remote.util.timer.Lap;
import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;

/**
 *  DataBase is the holder of the database connection.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       19 May 1997
 */
@SuppressWarnings({"NestedAssignment", "AssertWithSideEffects"})
public class DataBase
{
    /** the raw driver class name */
    public static final String PROPERTY_DRIVER="DRIVER";

    /** SQL read timeout */
    public static final String SQL_READ_TIMEOUT="SQL_READ_TIMEOUT";
    /**
     * Derby database
     */
    public static final String TYPE_DERBY="DERBY";

    /**
     * Sybase database
     */
    public static final String TYPE_SYBASE="SYBASE";

    /**
     * HSQLDB database
     */
    public static final String TYPE_HSQLDB="HSQLDB";

    /**
     * Postgres database
     */
    public static final String TYPE_POSTGRESQL="POSTGRESQL";

    /**
     * Oracle database.
     */
    public static final String TYPE_ORACLE="ORACLE";

    /**
     * MySQL database
     */
    public static final String TYPE_MYSQL="MYSQL";

    /** Microsoft SQL Server */
    public static final String TYPE_MSSQL="MSSQL";

    /** Microsoft SQL Server using the Microsoft JDBC Driver */
    public static final String TYPE_MSSQL_NATIVE="MSSQL_N";

    private boolean         visible;

    private final String    type;
    private final Map<String, String>    connectionProps;
    private final java.sql.Driver driver;
    private final String    url;
    private String shortUrl;
    private final String    userId;
    private final String    typeKey;
    private final String    password;
    private String createTableSuffix;
    private String createIndexSuffix;
    private int dbMajorVersion;
    private int dbMinorVersion;
    private boolean shuttingDown;
    private int driverMajorVersion;
    private int driverMinorVersion;
    private int maxStatementLength;
    private static DataBase currentDB;//MT CHECKED
    
    private static final String DERBY_EMBEDDED="embedded:";
    private static final boolean ASSERT_ENABLE;
    private static final LinkGroup LINK_GROUP=new LinkGroup( "DATABASE");

    /**
     * Protection modes.
     */
    public static enum Protection{
        /**
         * No protection provided.
         */
        NONE,
        /*
         * the connection is readonly, no changes to the database will be allowed. 
         */
        READONLY, 
        /*
         * When a SELECT is performed, the connection will be marked as readonly.
         */
        SELECT_READONLY_BY_DEFAULT
    };
    /**
     * Protection mode
     */
    public final Protection protection;
    
    /**
     * create a new database
     *
     * @param userId the user id
     * @param password the password to use when connecting.
     * @param inType the database type
     * @param inUrl the connection URL
     * @throws Exception a serious problem
     * @deprecated specify the protection constructor.
     */    
    public DataBase(
        final String userId,
        final String password,
        final String inType,
        final String inUrl
    ) throws Exception
    {
        this(userId,password,inType,inUrl,Protection.NONE);
    }  
    
    /**
     * create a new database
     *
     * @param userId the user id
     * @param password the password to use when connecting.
     * @param inType the database type
     * @param inUrl the connection URL
     * @param protection the protection mode.
     * @throws Exception a serious problem
     */
    public DataBase(
        final String userId,
        final String password,
        final @Nonnull String inType,
        final @Nonnull String inUrl,
        final @Nonnull Protection protection
    ) throws Exception
    {
        if( protection==null) throw new IllegalArgumentException("protection mode is mandatory");
        this.protection=protection;
        
        if( StringUtilities.isBlank(inType) )
        {
            throw new Exception( "Database type must be specified");
        }

        if( StringUtilities.isBlank(inUrl) )
        {
            throw new Exception( "Database URL must be specified");
        }
        this.userId = userId;
        this.password = password;

        String inProps = null;
        String tmpURL=inUrl;
        int pos = tmpURL.indexOf(",");
        if( pos != -1)
        {
            tmpURL = inUrl.substring(0, pos);
            inProps=inUrl.substring(pos + 1);
        }
        HashMap<String, String> tmpProperties=HashMapFactory.create();
        if( StringUtilities.isBlank(inProps) == false)
        {
            String listProps[][] = StringUtilities.splitAttributes(inProps);
            for (String[] tmpProps : listProps) 
            {
                if( tmpProps.length>1)
                {
                    tmpProperties.put(tmpProps[0], tmpProps[1]);
                }
            }
        }
        String dbProperties=CProperties.getProperty(inType + "_PROPERTIES");
        if( StringUtilities.isBlank(dbProperties) == false)
        {
            String listProps[][] = StringUtilities.splitAttributes(dbProperties);
            for (String[] tmpProps : listProps) 
            {
                if( tmpProps.length>1)
                {
                    tmpProperties.put(tmpProps[0], tmpProps[1]);
                }
            }
        }
        
        assert ThreadCop.monitor(tmpProperties, ThreadCop.MODE.READONLY);
        this.connectionProps=Collections.unmodifiableMap(tmpProperties);

        visible     = true;
        type        = inType.toUpperCase().trim();
        url         = tmpURL;

        typeKey = "DB-" + userId + "@" + type + "." + url;

        String driverClassName = null;
        if( type.equals(TYPE_SYBASE))
        {
            driverClassName ="com.sybase.jdbc2.jdbc.SybDriver";
        }
        else if( type.equals( TYPE_POSTGRESQL)==true)
        {
            driverClassName ="org.postgresql.Driver";
        }
        else if( type.equals(TYPE_HSQLDB)==true)
        {
            driverClassName ="org.hsqldb.jdbcDriver";
        }
        else if( type.equals( "ODBC" ) == true )
        {
            driverClassName ="sun.jdbc.odbc.JdbcOdbcDriver";
        }
        else if( type.equals( "SOAP" ) == true )
        {
            driverClassName ="com.aspc.remote.jdbc.Driver";
        }
        else if( type.equals( TYPE_MYSQL ) == true )
        {
            driverClassName ="com.mysql.jdbc.Driver";
        }
//        else if( type.equals( "DB2" ) == true )
//        {
//            driverClassName ="com.ibm.db2.jcc.DB2Driver";
//        }
//        else if( type.startsWith( TYPE_MSSQL_NATIVE ) == true )
//        {
//            driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
//        }
        else if( type.startsWith( TYPE_MSSQL ) == true )
        {
            driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        }
        else if( type.equals( TYPE_DERBY ) == true )
        {
            if( url.startsWith( DERBY_EMBEDDED))
            {
                driverClassName = "org.apache.derby.jdbc.EmbeddedDriver";
            }
            //TODO: The server version
        }
        else if(type.equals(TYPE_ORACLE) == true)
        {
            driverClassName = "oracle.jdbc.driver.OracleDriver";
        }
        else
        {
            throw new Exception( "unknown type '" + type + "'");
        }

        if( connectionProps.containsKey(PROPERTY_DRIVER))
        {
            driverClassName = connectionProps.get( PROPERTY_DRIVER);
        }

        driver = (java.sql.Driver)Class.forName( driverClassName ).newInstance();
        DriverManager.registerDriver( driver );
    }

    @CheckReturnValue
    public int getMaxPreparedStatements()
    {
       // if( type.equals(TYPE_HSQLDB))
      //  {
      //      return 10;
      //  }
      //  else
      //  {
            return 1000;
       // }
    }

    /**
     * set the create table suffix
     * @param suffix the suffix
     * @return this
     */
    @Nonnull
    public DataBase setCreateTableSuffix( final @Nullable String suffix)
    {
        createTableSuffix=suffix;
        return this;
    }

    /**
     * get the create table suffix
     * @return the suffix
     */
    @CheckReturnValue @Nullable
    public String getCreateTableSuffix( )
    {
        return createTableSuffix;
    }

    /**
     * set the create index suffix
     * @param suffix the suffix
     * @return this
     */
    @Nonnull
    public DataBase setCreateIndexSuffix( final @Nullable String suffix)
    {
        createIndexSuffix=suffix;
        return this;
    }

    /**
     * get the create index suffix
     * @return the suffix
     */
    @CheckReturnValue @Nullable
    public String getCreateIndexSuffix( )
    {
        return createIndexSuffix;
    }

    /**
     * Version supported
     * @param major the major data base version
     * @param minor the minor data base version
     * @return true if a supported version.
     */
    @CheckReturnValue
    public boolean checkVersion( final int major, final int minor)
    {
        if( major == dbMajorVersion)
        {
            if( minor <= dbMinorVersion)//MT WARN: luckily the version number will not always change
            {
                return true;
            }
        }
        else if( major < dbMajorVersion)
        {
            return true;
        }

        return false;
    }

    /**
     * to string
     * @return the string
     */
    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        return getTypeKey();
    }

    /**
     * close the database connections
     */
    public void close()
    {
        try
        {
            LinkManager.close( getTypeKey());
        }
        catch( Exception e)
        {
            LOGGER.warn( "could not close " + this, e);
        }
    }

    /**
     * Returns the native expression of a long value for this database type.
     *
     * Note: This is very important for POSTGRES as it will not use the indexes
     *       for a long unless you cast it.
     * @param buffer the buffer of the SQL statement
     * @param l the long value
     * @return the appended long
     */
    @Nonnull
    public StringBuilder appendSQLString( final @Nonnull StringBuilder buffer,  final long l)
    {
        if( shuttingDown) throw new IllegalStateException("database is shutting down");
        if( type.equals( TYPE_POSTGRESQL))
        {
            buffer.append( l);

            buffer.append( "::bigint");
        }
        else
        {
            buffer.append( l);
        }

        return buffer;
    }

    /**
     *
     * @return true if this database supports comments
     */
    @CheckReturnValue
    public boolean isCommentsSupported()
    {
        return !type.equals( TYPE_DERBY);
    }

    /**
     *
     * @return the URL
     */
    @CheckReturnValue @Nonnull
    public String getUrl()
    {
        return url;
    }

    /**
     *
     * @return the short URL
     */
    @CheckReturnValue @Nonnull
    public String getShortUrl()
    {
        if( shortUrl == null)
        {
            if( url.length() < 60)
            {
                shortUrl = url;
            }
            else
            {
                int hostPos = url.indexOf("HOST=");
                int portPos = url.indexOf("PORT=");

                if( hostPos != -1 && portPos != -1)
                {
                    int hostEndPos = url.indexOf(')', hostPos);
                    int portEndPos = url.indexOf(')', portPos);
                    if( hostEndPos != -1 && portEndPos != -1)
                    {
                        shortUrl=url.substring(hostPos + 5, hostEndPos) + ":" + url.substring(portPos +5, portEndPos);
                    }
                    else
                    {
                        shortUrl = url;
                    }
                }
                else
                {
                    shortUrl = url;
                }
            }
        }

        return shortUrl;
    }
    
    /**
     *
     * @return the database Type
     */
    @CheckReturnValue @Nonnull
    public String getType()
    {
        return type;
    }

    /**
     *
     * @return true if connected
     */
    @CheckReturnValue
    public boolean isConnected()
    {
        if( shuttingDown) throw new IllegalStateException("database is shutting down");
        return LinkManager.hasType( getTypeKey());
    }

    /**
     * Some databases can not be shared by multiple JVM.
     * 
     * @return false if the database can only be accessed by one process. 
     */
    @CheckReturnValue
    public boolean supportsMultipleJVMs()
    {
        if( TYPE_HSQLDB.equals(type))
        {
            return false;
        }
        
        return true; 
    }
    /**
     *
     * @param inType the database type
     * @throws Exception a serious problem
     * @return the default database name
     */
    @CheckReturnValue @Nonnull
    public static String getMasterDBName( final @Nullable String inType) throws Exception
    {
        if( inType == null )
        {
            throw new Exception( "Database type must be specified");
        }

        String type  = inType.toUpperCase().trim();

        if( type.equals(TYPE_SYBASE)==true)
        {
            return "";
        }
        else if( type.equals(TYPE_POSTGRESQL)==true)
        {
            return "template1";
        }
        else if( type.equals( TYPE_MYSQL ) == true )
        {
            return "mysql";
        }
        else if( type.equals( TYPE_MSSQL ) == true )
        {
            return "MSSQL";
        }
//        else if( type.equals( "MSSQL5" ) == true )
//        {
//            return "tempdb";
//        }
//        else if( type.equals( TYPE_MSSQL_NATIVE ) == true )
//        {
//            return TYPE_MSSQL_NATIVE;
//        }
        else
        {
            throw new Exception( "No default database name found for type '" + type + "'");
        }
    }

    /**
     *
     * @return the user
     */
    @CheckReturnValue @Nonnull
    public String getUser()
    {
        return userId;
    }

    /**
     *
     * @return the password
     */
    @CheckReturnValue @Nonnull
    public String getPassword()
    {
        return password;
    }

    /**
     *
     * @param visible true if visible
     * @return this
     */
    @Nonnull
    public DataBase setVisible( final boolean visible)
    {
        this.visible = visible;//MT WARN: Inconsistent synchronization
        return this;
    }

    /**
     *
     * @return this
     */
    @Nonnull
    public DataBase setCurrent()
    {
        if( shuttingDown) throw new IllegalStateException("database is shutting down");
        currentDB = this;
        return this;
    }

    /**
     *
     * @return the KEY
     */
    @CheckReturnValue @Nonnull
    public String getTypeKey()
    {
        return typeKey;
    }

    /**
     * The maximum statement length
     * @return the length
     */
    @CheckReturnValue
    public int getMaxStatementLength()
    {
        return maxStatementLength;
    }

    /**
     * make a new connection.
     *
     * @throws Exception a serious problem
     */
    @SuppressWarnings({"UseSpecificCatch", "BroadCatchBlock", "TooBroadCatch"})
    public synchronized void connect( ) throws Exception
    {
        if( shuttingDown) throw new IllegalStateException("database is shutting down");
        Lap start=new Lap();

        String jdbcURL = "";
        Connection connection;//NOPMD

        Properties props = new Properties();

        try
        {
            props.put( "user", userId );
            props.put( "password", password );

            String env = CProperties.findProperty("DATABASE_" + type+ ".PROPERTIES");
            String nameValueList[] = env.split(",");

            for( String nameValue: nameValueList)
            {
                int pos=nameValue.indexOf('=');
                if( pos!=-1)
                {
                    String name=nameValue.substring(0, pos);
                    String value=nameValue.substring(pos + 1);

                    props.put(name, value);
                }
            }

            if( type.equals( TYPE_SYBASE ))
            {
                props.put( "CURSOR_ROWS", "10" );
                jdbcURL = "jdbc:sybase:Tds:" + url;

                String hostName      =  NetUtil.LOCAL_HOST_NAME;
                props.put( "HOSTNAME", hostName ); // char(10)

                String applicationName= CUtilities.makeAppShortName();
                if( applicationName.length() > 16)
                {
                    applicationName = applicationName.substring( 0, 15);
                }

                props.put( "APPLICATIONNAME", applicationName); // char(16)
                String processName     = "";

                props.put( "HOSTPROC", processName ); // char(8)
            }
            else if( type.equals( "SERVER" ) )
            {
                jdbcURL = type + ":" + url;
            }
            else if( type.equals( "SOAP" ) )
            {
                jdbcURL = type + ":" + url;
            }
            else if( type.equals( "DBOBJECT" ) )
            {
                jdbcURL = type + ":" + url;
            }
            else if( type.equals( "ODBC" ) == true )
            {
                jdbcURL = "jdbc:odbc:" + url;
            }
            else if( type.equals( TYPE_POSTGRESQL ) == true )
            {
                jdbcURL = "jdbc:postgresql://" + url;
               // props.put("loglevel", "1");
                //props.put("logUnclosedConnections", "true");
            }
            else if( type.equals( TYPE_HSQLDB ) == true )
            {
                props.put("check_props", "true");
                props.put("sql.enforce_refs", "true");
                props.put("sql.enforce_size", "true");
                props.put("hsqldb.default_table_type", "CACHED");
                /** http://hsqldb.org/doc/2.0/guide/dbproperties-chapt.html */
                props.put("hsqldb.tx", "MVCC");
                props.put("sql.enforce_names", "true");
                
                /*
                 * turned off 8 April 2015
                 * http://hsqldb.org/doc/guide/management-chapt.html
                 */
                //props.put( "hsqldb.log_size", "0");
                
                /*
                 * disable nio 12 April 2015
                 */
                props.put( "hsqldb.nio_data_file", "false");

                jdbcURL = "jdbc:hsqldb:" + url;
            }
            else if( type.equals( TYPE_MYSQL ) == true )
            {
                /**
                 * MySQl connection:
                 * (1) jdbc:mysql://localhost/
                 * (2) jdbc:mysql://localhost/mysql
                 */
                if(url.contains("/"))
                {
                    jdbcURL = "jdbc:mysql://" + url;
                }
                else
                {
                    jdbcURL = "jdbc:mysql://" + url + "/";
                }
            }
            else if( type.equals( TYPE_MSSQL ) == true )
            {
                if(url.startsWith("jdbc:sqlserver")==false)
                {
                    jdbcURL = "jdbc:sqlserver://"+url.replace("/", ";databaseName=");
                }
                else
                {
                    jdbcURL = url;
                }
            }
            else if( type.equals( TYPE_DERBY ) == true )
            {
                String temp;

                if( url.startsWith( DERBY_EMBEDDED))
                {
                    temp = url.substring( DERBY_EMBEDDED.length()) ;

                    if( temp.indexOf( ';') == -1)
                    {
                        File dir = new File( temp);

                        if( dir.exists() == false)
                        {
                            temp += ";create=true";
                        }
                    }
                }
                else
                {
                    temp = url;
                }

                jdbcURL = "jdbc:derby:" + temp;
            }
            else if(type.equals(TYPE_ORACLE))
            {
                props.put("RECV_BUF_SIZE", "100000");
                props.put("SEND_BUF_SIZE", "100000");
                //props.put("CURSOR_SHARING","FORCE");
                props.put("defaultRowPrefetch", "1000");
                String value = System.getProperty(SQL_READ_TIMEOUT, Integer.toString( CSQL.DEFAULT_QUERY_TIMEOUT_SECONDS * 1000 + 60000));// Set to one minute more than the default query timeout
                props.put ("oracle.jdbc.ReadTimeout", value);
                //props.put( "oracle.net.ns.SQLnetDef.TCP_CONNTIMEOUT_STR","3000");
                jdbcURL = "jdbc:oracle:thin:@" + url;
                String applicationName= CUtilities.makeAppShortName();
                if( applicationName.length()>48)// max 48 chars
                {
                    applicationName=applicationName.substring(applicationName.length()-48);
                }
                props.put("v$session.program", applicationName);
            }
            else
            {
                jdbcURL = type + url;
            }

            if( visible == true)
            {
                currentDB = this;
            }

            props.putAll(connectionProps);
            DriverManager.setLoginTimeout( 60);
            if( driver.acceptsURL(jdbcURL) ==false)
            {
                throw new SQLException( displayDriverInfo() + " does not accept " + StringUtilities.stripPasswordFromURL(jdbcURL));
            }
            connection = driver.connect( jdbcURL, props );
            if( connection == null)
            {
                throw new SQLException( displayDriverInfo() + " did not return a connection");
            }
            
            connection.setAutoCommit( true);
            assert connection.getAutoCommit();
            if( protection ==Protection.READONLY)
            {
                connection.setReadOnly(true);
            }
            connection.setTransactionIsolation( Connection.TRANSACTION_READ_COMMITTED);

            String key = getTypeKey();

            if( LinkManager.hasType( key) == false)
            {
                LinkType lt;
                lt = new LinkType( key);
                lt.setGroup(LINK_GROUP);

                int max = 0;
                /**
                 * Maximum connections to open:
                 *  is the maximum specified in the System property MAX_DBCONNECTIONS if specified or
                 *  the maximum from the database meta data if valid or
                 *  just put in an number out of the air say 100
                 */
                try
                {
                    DatabaseMetaData md = connection.getMetaData();

                    max = md.getMaxConnections();

                    if( type.equals(TYPE_SYBASE) == false)
                    {
                        dbMajorVersion = md.getDatabaseMajorVersion();
                        dbMinorVersion = md.getDatabaseMinorVersion();
                    }
                    driverMajorVersion = md.getDriverMajorVersion();
                    driverMinorVersion = md.getDriverMinorVersion();

                    if( type.equals(TYPE_ORACLE) )
                    {
                        String str = System.getProperty("DATABASE." + type + ".MAX_REUSE");
                        if( StringUtilities.isBlank(str) == false)
                        {
                            lt.setMaximumReuse(Integer.parseInt(str));
                        }
                    }

                    maxStatementLength = md.getMaxStatementLength();
                    if( maxStatementLength <= 0)
                    {
                        maxStatementLength = 1024 * 1024;
                    }
                }
                catch( Exception e)
                {
                    LOGGER.warn( "Can't get the max connections for the database " + this, e);
                }

                validateVersion();

                if( max < 1 || max > 100) max = 100;

                max = Integer.parseInt( CProperties.getProperty("MAX_DBCONNECTIONS", "" + max));

                lt.setMaximumReserve(20);

                lt.setMaximumConnections( max);
                lt.setMinimumConnections(0);
                LinkManager.addType(lt);
            }

            checkName(connection);

            if( ASSERT_ENABLE || type.equals( TYPE_HSQLDB ) )
            {
                WrapperConnection c = new WrapperConnection( connection);
                checkConnection( c);
                connection=c;
            }

            LinkType tmpType;
            tmpType = LinkManager.getType( key);
            LinkConnection lc;
            lc = new LinkConnection( tmpType, connection);

            if( type.equals(TYPE_HSQLDB) == false)
            {
                lc.setMaxIdle(5 * 60);
            }

            LinkManager.addConnection(key, lc);

            CSQL.recordTime("CONNECTION", start, connection, null, 0, this);
        }
        catch( Exception e)
        {
            LOGGER.error( jdbcURL, e);
            String msg=StringUtilities.safeMessage(e.getMessage() + " thePars:"+jdbcURL +" props:"+props);
            throw new SQLException(msg, e);
        }
    }

    /**
     * Display the driver info ( class name and jar file). 
     * @return the information. 
     */
    @CheckReturnValue @Nonnull
    public String displayDriverInfo()
    {
        Class dClass=driver.getClass();
        URL location = dClass.getResource('/'+dClass.getName().replace('.', '/')+".class");
        String tmpJarFile = location.toString();
        int pos = tmpJarFile.indexOf("!");
        if( pos!=-1)
        {
            tmpJarFile=tmpJarFile.substring(0, pos);
        }
        String strip[][]={
            {"jar:file:",""}, 
            {System.getProperty("user.home"),"~"}
        };
        for( String pair[]:strip)
        {
            String name=pair[0];
            if( tmpJarFile.startsWith(name))
            {
                tmpJarFile=tmpJarFile.substring(name.length());
                tmpJarFile=pair[1] + tmpJarFile;
            }
        }
        
        return dClass.getName() + " in " + tmpJarFile;        
    }
    @SuppressWarnings("ConvertToStringSwitch")
    private void validateVersion()
    {
        int requiredDatabaseMajorVersion = 0;
        int requiredDatabaseMinorVersion = 0;

        int requiredDriverMajorVersion = 0;
        int requiredDriverMinorVersion = 0;

        if( type.equals( TYPE_POSTGRESQL))
        {
            requiredDatabaseMajorVersion=8;
            requiredDatabaseMinorVersion=1;

            requiredDriverMajorVersion=8;
            requiredDriverMinorVersion=0;
        }
        else if( type.equals( TYPE_HSQLDB))
        {
            requiredDatabaseMajorVersion=1;
            requiredDatabaseMinorVersion=8;

            requiredDriverMajorVersion=1;
            requiredDriverMinorVersion=8;
        }
        else if( type.equals( TYPE_ORACLE))
        {
            requiredDatabaseMajorVersion=10;
            requiredDatabaseMinorVersion=1;

            requiredDriverMajorVersion=11;
            requiredDriverMinorVersion=1;
        }

        if(
            dbMajorVersion < requiredDatabaseMajorVersion ||
            (
                dbMajorVersion == requiredDatabaseMajorVersion &&
                dbMinorVersion < requiredDatabaseMinorVersion
            )
        )
        {
            throw new DataBaseVersionError(
                "wrong database version " + dbMajorVersion + "." + dbMinorVersion +
                " ( required " + requiredDatabaseMajorVersion + "." + requiredDatabaseMinorVersion + ")"
            );
        }

        if(
            driverMajorVersion < requiredDriverMajorVersion ||
            (
                driverMajorVersion == requiredDriverMajorVersion &&
                driverMinorVersion < requiredDriverMinorVersion
            )
        )
        {
            throw new DataBaseVersionError(
                "wrong driver version " + driverMajorVersion + "." + driverMinorVersion +
                " ( required " + requiredDriverMajorVersion + "." + requiredDriverMinorVersion + ")"
            );
        }
    }
    
    /**
     * Returns which separator should be used for this database. eg, SYABSE is blank but others are ;
     * @return the separator for the statements
     */
    @CheckReturnValue @Nonnull
    public String getBatchSeperator()
    {
        String seperator = ";";
        String dbType = getType();
        if( dbType.equals( TYPE_SYBASE) )
        {
            seperator = "";
        }

        return seperator;
    }

    /**
     * The escape string.
     * @return true if we need to escape sqls.
     */
    @SuppressWarnings("ConvertToStringSwitch")
    @CheckReturnValue
    public boolean hasSQLEscape()
    {
        String tmpType = getType();
        if(tmpType.equals( TYPE_POSTGRESQL))
        {
            if( dbMajorVersion < 9)
            {
                return true;
            }
            if( dbMajorVersion == 9 && dbMinorVersion < 1)
            {
                return true;
            }
        }
        else if(tmpType.equals( TYPE_MYSQL))
        {
            return true;
        }

        return false;
    }

    /**
     * SQL encoding for a string
     * @param javaString the required encoding
     * @return the SQL value
     */
    public @CheckReturnValue String encodeString( final @Nonnull String javaString)
    {
//        assert javaString.matches("[\t\r\n -~]*"): "Invalid SQL value: " + StringUtilities.encode(javaString);
        String sqlString=javaString;
        if( javaString.contains("'") || javaString.contains("\\"))
        {
            if( getType().equalsIgnoreCase(DataBase.TYPE_POSTGRESQL))
            {
                String temp = javaString.replace( "\\", "\\\\");

                temp = temp.replace( "'", "\\'");

                return "E'" + temp + "'";
            }

            sqlString = javaString.replace( "'", "''");

            if( hasSQLEscape())
            {
                sqlString = sqlString.replace( "\\", "\\\\");
            }
        }

        return "'" + sqlString + "'";
    }

    /**
     *
     * @return the current database.
     */
    @CheckReturnValue @Nullable
    public static DataBase getCurrent()
    {
        return currentDB;
    }

    /**
     * Returns which separator should be used for this database. eg, SYABSE is blank but others are ;
     * @return true if this database supports batch statements
     */
    @CheckReturnValue
    public boolean getSupportsBatchStatements()
    {
        return !(
            type.equals( TYPE_DERBY)    ||
            type.equals( TYPE_ORACLE)   ||
            type.equals( TYPE_MYSQL)
        );
    }

    /**
     *
     * @return true if this database supports batch inserts
     */
    @CheckReturnValue
    public boolean getSupportsBatchInsertValues()
    {
        return type.equals( TYPE_MYSQL);
    }

    /**
     * does this database support renaming of tables ?
     * @return true if we can rename tables.
     */
    @CheckReturnValue
    public boolean getSupportsTableRenames()
    {
        String tmpType = getType();
        return !tmpType.equals( TYPE_DERBY);
    }

    /**
     * does this database support renaming of indexes ?
     * @return true if we can rename indexes.
     */
    @CheckReturnValue 
    public boolean getSupportsIndexRenames()
    {
        String tmpType = getType();
        return !(tmpType.equals( TYPE_MYSQL)|| tmpType.equals( TYPE_SYBASE));
    }

    /**
     *
     * @param connection the connection to check in
     */
    public void checkInConnection( final @Nullable Connection connection)
    {
        assert checkConnection( connection);
        LinkManager.checkInClient( connection);
    }
    
    private boolean checkConnection( final @Nullable Connection connection)
    {
        if( connection == null) return true;

        try
        {
            if( connection.getAutoCommit() ==false)
            {
                throw new WrapperAssertError( connection + " checked in with auto commit OFF" );
            }

            if( connection.isClosed() )
            {
                throw new WrapperAssertError( connection + " is closed" );
            }
            
            if( protection == Protection.SELECT_READONLY_BY_DEFAULT)
            {
                if(connection.isReadOnly())
                {
                    throw new WrapperAssertError( connection + " is readonly" );                    
                }
            }
            else if( protection == Protection.READONLY)
            {
                if(connection.isReadOnly()==false)
                {
                    throw new WrapperAssertError( connection + " is NOT readonly" );                    
                }                
            }

            if( connection instanceof WrapperConnection)
            {
                WrapperConnection wc = (WrapperConnection)connection;
                WrapperStatement list[] = wc.listOpenStatements();

                if( list.length > 0)
                {
                    @SuppressWarnings({"ThrowableInstanceNotThrown", "ThrowableInstanceNeverThrown"})
                    Exception openedByException = new Exception( "still open connect (see stack)");
                    openedByException.setStackTrace(list[0].openedByStackTrace());

                    throw new WrapperAssertError( connection + " has " + list.length + " open connections", openedByException );
                }
            }
        }
        catch( SQLException sqlException )
        {
            throw new WrapperAssertError( sqlException.toString() );
        }

        return true;
    }
    
    /**
     *  Synchronized so that it puts a limit onto the number of connections
     *  Hopefully if two threads require a connection by the time one has
     *  connected another connection may be freed.
     *
     *  The procedure loops until it finds a live connection. If the connection
     *  is not valid then remove it from the LinkManager.
     * @throws Exception a serious problem
     * @return the connection
     */
    @CheckReturnValue @Nonnull
    public synchronized Connection checkOutConnection() throws Exception
    {
        if( shuttingDown)
        {
            throw new DataBaseError( this + " shutting down");
        }
        
        if( Thread.currentThread().isInterrupted())
        {
            throw new DataBaseError( "Thread has been interrupted");
        }

        if( isConnected() == false)
        {
            connect( );
        }

        Connection connection = null;//NOPMD

        long startTime = 0;
        String key = getTypeKey();

        while( true)
        {
            try
            {
                connection = (Connection)LinkManager.checkOutClient( key);
            }
            catch( NoLinksException nl)
            {
                // don't care
            }

            if( connection != null)
            {
                break;
            }

            int count = LinkManager.countConnections( key);
            int max = LinkManager.getMaxConnections( key);
            if( count >= max)
            {
                if( startTime == 0)
                {
                    startTime = System.currentTimeMillis();
                }
                else
                {
                    if( System.currentTimeMillis() - startTime > 60 * 1000)
                    {
                        throw new Exception(
                            "Waited " + TimeUtil.getDiff( startTime) + " for a free connection to " + key
                        );
                    }
                }

                CLogger.schedule(
                  LOGGER,
                  "info",
                  key + " too many connections " + count + "/" + max + " waited: " + TimeUtil.getDiff( startTime),
                  null
                );

                wait( 1000);
            }
            else
            {
                CLogger.schedule(
                    LOGGER,
                    "info",
                    "Connecting " + key,
                    null
                );
                connect( );
            }
        }
        assert connection!=null: "must have a connection";
        assert protection!=Protection.SELECT_READONLY_BY_DEFAULT || connection.isReadOnly()==false: connection + " must be writtable";
        assert protection!=Protection.READONLY || connection.isReadOnly(): connection + " must be readonly";
        
        return connection;
    }

    /**
     * Retrieves the major version of the database
     * @throws Exception a serious problem
     * @return version
     */
    @CheckReturnValue 
    public int getMajorVersion() throws Exception
    {
        return dbMajorVersion;
    }

    /**
     * Retrieves the minor version of the database
     * @throws Exception a serious problem
     * @return version
     */
    @CheckReturnValue
    public int getMinorVersion() throws Exception
    {
        return dbMinorVersion;
    }

    /**
     *
     */
    @SuppressWarnings({ "BroadCatchBlock", "TooBroadCatch", "ConvertToStringSwitch", "SleepWhileHoldingLock", "UseSpecificCatch"})
    public synchronized void shutDown()
    {
        if( currentDB==null) currentDB=null;
        // If already shutdown then exit
        if( type.equals(TYPE_HSQLDB))
        {
            
            Lap lap=new Lap();
            
            String shutdownCommand="SHUTDOWN COMPACT";
            Connection conn=null;
            try
            {
                LOGGER.info( "Shutdown db: " + getTypeKey());
                conn = checkOutConnection();
                
                shuttingDown=true;
                Thread.sleep(200);
                SQLWarning warn;
                try (Statement s = conn.createStatement()) {
                    s.execute(shutdownCommand);
                    warn = s.getWarnings();
                }
                if( warn!=null)
                {
                    LOGGER.warn( shutdownCommand, warn);
                }
            }
            catch( Throwable t)
            {
                CSQL.recordTime(shutdownCommand, lap, conn, t, 0, this);
                LOGGER.error( "Could not shutdown database " + getTypeKey(), t);
            }
            finally
            {
                checkInConnection(conn);
            }
        }
        else if( type.equals( TYPE_DERBY))
        {
            /*
             *  In embedded mode, an application should shut down Derby.
             *  If the application fails to shut down Derby explicitly,
             *  the Derby does not perform a checkpoint when the JVM shuts down, which means
             *  that the next connection will be slower.
             *  Explicitly shutting down Derby with the URL is preferred.
             *  This style of shutdown will always throw an "exception".
             */
            if (url.startsWith( DERBY_EMBEDDED))
            {
                try
                {
                    DriverManager.getConnection("jdbc:derby:;shutdown=true").close();
                }
                catch (SQLException se)
                {
                    // This expected and normal.
                }
            }
        }

        // closes all connections
        LinkManager.killType( getTypeKey());
    }

    /**
     * Sybase will connect to the default database without warning if the name of the database is wrong
     */
    private void checkName(final @Nonnull Connection conn) throws Exception
    {
        if( type.equals( DataBase.TYPE_SYBASE ))
        {
            Statement stmt;//NOPMD

            stmt = conn.createStatement();

            stmt.execute( "SELECT db_name()");

            ResultSet rs;//NOPMD
            rs = stmt.getResultSet();

            if( rs.next() == false)
            {
                throw new Exception( "Failed to get database name");
            }

            String name;
            name = rs.getString( 1);

            String expectedName = url;

            int pos = expectedName.indexOf( '/');

            expectedName = expectedName.substring( pos + 1);

            if( expectedName.equalsIgnoreCase( name) == false)
            {
                try
                {
                    conn.close();
                    LOGGER.info("Closing unexpected connections name = '" + name + "'");

                }
                catch (SQLException igore)
                {
                    // don't care.
                }

                throw new Exception(
                    "Expected to connect to database '" + expectedName + "' but instead connected to '" + name + "'"
                );
            }
        }
    }

    static
    {
        LINK_GROUP.setMaximumConnections(100);
        boolean enabled = false;
        assert enabled = true;

        ASSERT_ENABLE=enabled;
        Shutdown.init();
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.DataBase");//#LOGGER-NOPMD
}
