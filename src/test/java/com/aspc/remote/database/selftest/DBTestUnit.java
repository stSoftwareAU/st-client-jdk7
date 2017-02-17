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
package com.aspc.remote.database.selftest;

import com.aspc.developer.ThreadCop;
import com.aspc.remote.database.DataBase;
import com.aspc.remote.memory.MemoryManager;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.CProperties;
import com.aspc.remote.util.misc.FileUtil;
import com.aspc.remote.util.misc.QueueLog;
import com.aspc.remote.util.misc.ThreadPool;
import java.io.File;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;

/**
 *  VirtualDBTestUnit is a JUnit based test...
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED test unit</i>
 *
 *  @author      Nigel Leck
 *  @since       27 July 2007
 */
@SuppressWarnings("AssertWithSideEffects")
public abstract class DBTestUnit extends TestCase
{
    /**
     * environment variable to hide known errors.
     */
    public static final String HIDE_KNOWN_ERRORS="HIDE_KNOWN_ERRORS";

    private ThreadKiller killer;
    public static final boolean ASSERT;
    /**
     * The test was run from the command line and we should shutdown the JMS connection
     */
    protected static boolean runFromCommandLine;

    /**
     * Creates new VirtualDBTestUnit
     * @param name The name of the test.
     */
    protected DBTestUnit(final String name) 
    {
        super( name);
        try
        {
            /**
             * https://code.google.com/p/google-web-toolkit/issues/detail?id=8422
             */
            Class.forName("java.awt.EventQueue");
        }
        catch( ClassNotFoundException ncf)
        {
            LOGGER.error("could not load class", ncf);
        }
    }

    /**
     * Should we hide known errors.
     * @return true if we should ignore errors
     */
    public static boolean hideKnownErrors()
    {
        return "YES".equalsIgnoreCase(System.getProperty(HIDE_KNOWN_ERRORS));
    }

    /**
     * This method return troublesome strings that are used for SQL injection.
     *
     * http://www.unicode.org/faq/utf_bom.html
     *
     * @return return these troublesome strings
     */
    public static String[] getSQLInjectionStrings()
    {
        char c[]= { 0x20ac};

        StringBuilder backslash = new StringBuilder();
        for( int i=0;i < 2*256;i++)
        {
            backslash.append("\\");
        }
        String euroSymbol = new String( c);//NOPMD
        return new String[] {
            
            "\"&amp;%00<!--\\'';你好", // Always try this one even if done manually. 
            "\u0000", //Null
            "\u0001",
            "\u0002\u0003\u0004\u0005\u0006\u0007\u0008",
            "" + (char)9 + (char)10 + (char)11 + (char)12 + (char)13, 
            "<?xml version=\"1.0\" ?><!DOCTYPE TINFOIL [<!%",
            "1%401%7e1385%401()\"&%1'-;<tinfoil_xss_c08d9955148bc0199789922ca232a77b69003980/>'",
            "${VAR}",
            "${-1}",
            "XML\u2013",
            "a\\b?c%d*e:f|g\\\"h<i>j.k l你m",
            /**
             * http://www.unixwiz.net/techtips/sql-injection.html
             */
            "\\'; DROP TABLE users; --",
            "'';",
            "\\'",
            "\\''; \\'';:Contact-Delete",
            "'\\''; \\'';",
            "\u00F0\u009F\u00BF\u00B1\u00F0\u00AF\u00BF\u00B2\u00F0\u00BF\u00BF\u00B3\u00F1\u008F\u00BF\u00B4\u00F1\u009F\u00BF\u00B5\u00F1\u00AF\u00BF\u00B6\u00F4\u008F\u00BF\u00B7",
            "\u00EF\u00BB\u00BF\u00FF\u00FE\u00FF\u00FF\u00FE\u00FF\u2060",
            "\u001E\u0100",
            "\u001E\u00f0\u00f1\u00f2\u00f3\u00f4\u00f5\u00f6\u00f7\u00f8\u00f9\u00fa\u00fb\u00fc\u00fd\u00fe\u00ff",
            "\uE000",
            "\uD7FF",
            "\ufffe",
            euroSymbol,
            euroSymbol + "123",
            "&" + euroSymbol + "123",
            "';alert(String.fromCharCode(88,83,83))//';alert(String.fromCharCode(88,83,83))//alert(String.fromCharCode(88,83,83))//;" +
               "alert(String.fromCharCode(88,83,83))//--></SCRIPT>\">'><SCRIPT>alert(String.fromCharCode(88,83,83))</SCRIPT>",
            "' or 1=1;--",
            "{? = CALL addJdbcExampleTrade (1, 'john', 32, '2004-10-22') }",
            "{call ...}",
            "{?= call ...}",
            "{fn ...}",
            "{oj ...}",
            "{d ...}",
            "{t ...}",
            "{ts ...}",
            "©¡¢£¤¥¦§¨ª¬®°º»¼½¾¿ ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏ ÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞß àáâãäåæçèéêëìíîï ðñòóôõö÷øùúûüýþÿ",
            "abc'$",
            "'''''\"\"\"\"$$$$\\\\\\'\\$$",
            "$$$$$",
            "$global.id$",
            "\\$",
            "",
            "====",
            "+++",
            "#",
            "#23;",
            "#abc;",
            "#abc;#23;########;#",
            "RT @ClimateGroup - RT @EurActiv: #23;Solar #23;recession signals end of \\''Wild West\\'' gold rush http://t.co/GHFL9g2p #23;EU #23;renewable #23;energy",
            backslash.toString(),
            "Bob&Sons",
            "Mr 5%3",
            "My &amp; name",
            "hacker \'; games",
            "&#20;",
            "you+me",
            "-- \n;DELETE FROM Login;",
            "\"\"",
            "/*comment */",
            "SELECT /*!32302 1/0, */ 1 FROM tablename",
            "ID: 10; DROP TABLE members /*",
            "SELECT /*!32302 1/0, *\\/ 1 FROM tablename",
            "admin' --",
            "admin' #",
            "admin'/*",
            "' or 1=1 or ''='",
            "' or 1=1--",
            "' or 1=1#",
            "' or 1=1/*",
            "') or '1'='1--",
            "') or ('1'='1--",
            "\" or 1=1--",
            "or 1=1--",
            "' or 1=1 or ' '= '",
            ">]]></Description>",
            "<Description xmlns=\"\"><![CDATA[",
            "</Resource>",
            "<!-->]]>&lt;![CDATA[",
            "'or 1=1 or ''='",
            "PETA: Seaworld\'s Use of Whales Violates the 13th Amendment.",
            "PETA: Seaworld\\'s Use of Whales Violates the 13th Amendment.",
            "PETA: Seaworld''s Use of Whales Violates the 13th Amendment.",
            "PETA: Seaworld\\''s Use of Whales Violates the 13th Amendment.",
            "'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''"+
            "'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''"+
            "'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''",
            "INSERT INTO st_person ( code,name,notes) Values (\n'XYZ','Nigel','\uD7FF')",

            /** https://www.owasp.org/index.php/XSS_Filter_Evasion_Cheat_Sheet */
            "<",
            "%3C",
            "&lt",
            "&lt;",
            "&LT",
            "&LT;",
            "&#60",
            "&#060",
            "&#0060",
            "&#00060",
            "&#000060",
            "&#0000060",
            "&#60;",
            "&#060;",
            "&#0060;",
            "&#00060;",
            "&#000060;",
            "&#0000060;",
            "&#x3c",
            "&#x03c",
            "&#x003c",
            "&#x0003c",
            "&#x00003c",
            "&#x000003c",
            "&#x3c;",
            "&#x03c;",
            "&#x003c;",
            "&#x0003c;",
            "&#x00003c;",
            "&#x000003c;",
            "&#X3c",
            "&#X03c",
            "&#X003c",
            "&#X0003c",
            "&#X00003c",
            "&#X000003c",
            "&#X3c;",
            "&#X03c;",
            "&#X003c;",
            "&#X0003c;",
            "&#X00003c;",
            "&#X000003c;",
            "\\x3c",
            "\\x3C",
            "\\u003c",
            "\\u003C",

            /**
             * http://ferruh.mavituna.com/sql-injection-cheatsheet-oku/
             */
            "DROP sampletable;--",
            "DROP sampletable;#",
            "admin'--",
            "DROP/*comment*/sampletable",
            "DR/**/OP/*bypass blacklisting*/sampletable",
            "SELECT/*avoid-spaces*/password/**/FROM/**/Members ",
            "ID: /*!32302 10*/",
            "SELECT IF(1=1,'true','false')",
            "IF (1=1) SELECT 'true' ELSE SELECT 'false'",
            "CHAR(0x66)",
            "0x5045",
            "0x50 + 0x45",
            "SELECT login + '-' + password FROM members",
            "SELECT login || '-' || password FROM members",
            "SELECT CONCAT(login, password) FROM members",
            "0x457578 ",
            "SELECT CONCAT('0x',HEX('c:\\boot.ini')) ",
            "SELECT CONCAT(CHAR(75),CHAR(76),CHAR(77)) (M) ",
            "SELECT CHAR(75)+CHAR(76)+CHAR(77) (S) ",
            "SELECT LOAD_FILE(0x633A5C626F6F742E696E69) (M) ",
            "SELECT ASCII('a') ",
            "SELECT CHAR(64)",
            "' UNION SELECT 1, 'anotheruser', 'doesnt matter', 1--",
            "SELECT header FROM news UNION ALL SELECT name COLLATE SQL_Latin1_General_Cp1254_CS_AS FROM members ",
            "1234 ' AND 1=0 UNION ALL SELECT 'admin', '81dc9bdb52d04dc20036dbd8313ed055",
            "' HAVING 1=1 -- ",
            "' GROUP BY table.columnfromerror1 HAVING 1=1 --",
            "ORDER BY 1-- ",
            "' union select sum(columntofind) from users--",
            "SELECT * FROM Table1 WHERE id = -1 UNION ALL SELECT null, null, NULL, NULL, convert(image,1), null, null,NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULl, NULL-- ",
            "declare @o int ",
            "EXEC master.dbo.xp_cmdshell 'cmd.exe dir c:' ",
            "xp_regaddmultistring ",
            "xp_regdeletekey ",
            "xp_regdeletevalue ",
            "SELECT * FROM master..sysprocesses /*WHERE spid=@@SPID*/ ",
            "DECLARE @result int; EXEC @result = xp_cmdshell 'dir *.exe';IF (@result = 0) SELECT 0 ELSE SELECT 1/0",
            "WAITFOR DELAY '0:0:10'--",
            "IF (SELECT * FROM login) BENCHMARK(1000000,MD5(1))",
            "SELECT pg_sleep(10); ",
            "product.asp?id=5-1",
            "MD5() ",
            "SHA1() ",
            "PASSWORD()",
            "ENCODE()",
            "COMPRESS() ",
            "ROW_COUNT()",
            "SCHEMA()",
            "VERSION() ",
            "@@version",
        };
    }

    /**
     *
     * @throws Exception a serious problem
     */
    public static void connect() throws Exception
    {
        if( DataBase.getCurrent() == null)
        {
            if( sType == null)//NOPMD
            {
                sType = CProperties.getProperty("CONNECT_TYPE", "POSTGRESQL");
            }
            if( sURL == null)//NOPMD
            {
                sURL = CProperties.getProperty("DATASOURCE", "localhost/aspc_master");
            }
            if( sUser == null)//NOPMD
            {
                sUser = CProperties.getProperty("USER", "postgres");
            }
            if( sPassword == null)//NOPMD
            {
                sPassword = CProperties.getProperty("PASSWORD", "password");
            }

            if( DataBase.TYPE_HSQLDB.equals(sType))
            {
                File dbDir = new File( sURL).getParentFile();

                if( dbDir.exists() && dbDir.isDirectory())
                {
                    File list[] = dbDir.listFiles();

                    for( File file : list)
                    {
                        if( file.getName().endsWith(".lck"))
                        {
                            LOGGER.warn( "removing " + file);
                            FileUtil.deleteAll(file);
                        }
                    }
                }

                // Remove the array if using a private self test database.
                File dir = new File( FileUtil.getCachePath() + "/arrays/");
                if( dir.isDirectory())
                {
                    FileUtil.deleteAll(dir);
                }
            }

            DataBase db;
            db = new DataBase(sUser, sPassword, sType, sURL, DataBase.Protection.SELECT_READONLY_BY_DEFAULT);

            db.connect();
        }
    }

    /**
     *
     * @throws Exception a serious problem
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        /* Just clear any interruptions */        
        if( Thread.interrupted())
        {
            LOGGER.warn( "was interrupted at startup");
        }
        
        String testName = getName();
        Thread thread;
        thread = Thread.currentThread();

        threadName = thread.getName();
        thread.setName( testName);

        String timeoutStr = CProperties.getProperty("TEST_TIMEOUT", "1800000");

        long timeout = Long.parseLong( timeoutStr);

        if( timeout > 0)
        {
            killer = new ThreadKiller( getName(), Thread.currentThread(), timeout);
        }

        orgDisable = CProperties.getProperty( CProperties.PROPERTY_DISABLE, "");

        System.setProperty( CProperties.PROPERTY_DISABLE, "JMS,INDEXER," + orgDisable);

        connect();

        startThreadErrorCount = ThreadCop.errorCount();
        
        if( ASSERT==false )
        {
            fail( "Assert needs to be enabled");
        }
    }

    /**
     *
     * @param args the arguments
     */
    public static void parseArgs( final String args[])
    {
        runFromCommandLine=true;
        for (String arg : args) 
        {
            if (arg.startsWith("-U")) 
            {
                sUser = arg.substring(2).trim();
            } 
            else if (arg.startsWith("-T")) 
            {
                sType = arg.substring(2).trim();
            } else if (arg.startsWith("-R")) 
            {
                System.setProperty("DOC_ROOT", arg.substring(2).trim());
            } 
            else if (arg.startsWith("-D")) 
            {
                sURL = arg.substring(2).trim();
            } 
            else if (arg.startsWith("-P")) 
            {
                sPassword = arg.substring(2).trim();
            }
        }
    }

    /**
     * run the test
     * @throws java.lang.Throwable a serious problem
     */
    @Override
    protected void runTest() throws Throwable
    {
        super.runTest();

        if( expectThreadErrorFg == false)
        {
            long endThreadErrorCount = ThreadCop.errorCount();

            if( endThreadErrorCount != startThreadErrorCount)
            {
                throw ThreadCop.lastError();
            }
        }
        
        if( expectMemoryErrorFg==false)
        {
            MemoryManager.lastError();
        }
    }

    /**
     * Shut down the JMS connections
     * @throws Exception A serious error.
     */
    @Override
    protected void tearDown() throws Exception
    {
        Thread thread = Thread.currentThread();
        try
        {
            Thread.interrupted(); // clear the interrupt flag.
            if( killer != null) killer.cancel();

            super.tearDown();

            QueueLog.flush(60000);
            if( orgDisable != null)
            {
                System.setProperty( CProperties.PROPERTY_DISABLE, orgDisable);
            }
        }
        finally
        {
            if( threadName != null) thread.setName( threadName);
        }
        ThreadPool.purify();
    }

    /**
     * expect a thread error
     */
    protected void expectThreadError()
    {
        expectThreadErrorFg=true;
    }
    
    /**
     * expect a thread error
     */
    protected void expectMemoryError()
    {
        expectMemoryErrorFg=true;
    }
    
    private boolean expectThreadErrorFg;
    private boolean expectMemoryErrorFg;
    private long startThreadErrorCount;
    private String threadName;
    private String orgDisable;

    /**
     * the url
     */
    protected static String sURL;
    /** the type */
    protected static String sType;
    /** the user */
    protected static String sUser;
    /** the password */
    protected static String sPassword;

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.selftest.DBTestUnit");//#LOGGER-NOPMD
    static
    {
        boolean flag=false;
        assert flag=true;
        ASSERT=flag;
    }
}
