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
package com.aspc.remote.util.misc;

import com.aspc.remote.database.CSQL;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.FileAppender;

/**
 *  Utilities to access standard log files
 *
 * @author Nigel Leck
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @since       20 September 2006
 */
public final class LogUtil
{
    /**
     * default file read buffer size
     */
    public static final int    BLOCK_SIZE = 102400;
    
    /**
     * type LOG
     */
    public static final String TYPE_LOG         = "LOG";
    
    /**
     * type ERROR
     */
    public static final String TYPE_ERROR       = "ERROR";
    
    /**
     * type GC
     */
    public static final String TYPE_GC          = "GC";
    
    /**
     * type SQL
     */
    public static final String TYPE_SQL          = "SQL";
        
    private static final String PROPERTY_LOG           = "LOGFILENAME";
    private static final String PROPERTY_ERROR         = "ERRLOGFILENAME";
    private static final String PROPERTY_GC            = "GCLOGFILENAME";
    private static final String PROPERTY_ADDITIONAL    = "ADDITIONAL_LOGS";
    
    /** Default constructor
     */
    private LogUtil()
    {
        
    }
    
    /**
     * Loads contents of file starting at supplied offset into a string buffer
     * @param fileName - filename of file to read
     * @param fileBuffer - string buffer to load contents into
     * @param offset - position to start reading from.  If negative then position is based on offset from end of file
     * @throws Exception - file could not be accessed
     * @return new file position after read
     */
    public static long loadFileBuffer(final String fileName, StringBuilder fileBuffer, final long offset) throws Exception
    {
        return loadFileBuffer( fileName, fileBuffer, offset, BLOCK_SIZE);
    }
        
    /**
     * 
     * Loads contents of file starting at supplied offset into a string buffer
     * @param fileName - filename of file to read
     * @param fileBuffer - string buffer to load contents into
     * @param offset - position to start reading from.  If negative then position is based on offset from end of file
     * @param bufferSize - number of bytes to read from file
     * @throws Exception - file could not be accessed
     * @return new file position after read
     */
    public static long loadFileBuffer(final String fileName, final StringBuilder fileBuffer, final long offset, final int bufferSize) throws Exception
    {
        long newOffset = offset;
        
        if( fileName != null)
        {
            RandomAccessFile reader = null;

            try
            {
                try
                {
                    reader = new RandomAccessFile( fileName, "r");
                }
                catch( FileNotFoundException nf)
                {
                    LOGGER.warn( "Can't find log file " + fileName, nf);
                }
                
                if( reader != null)
                {
                    long fileLen = reader.length();

                    long firstPos,
                         lastPos;
                    
                    if( offset >= 0)
                    {
                        firstPos = offset;
                    }
                    else
                    {
                        firstPos = fileLen -1 + offset;
                        if( firstPos < 0)
                        {
                            firstPos = 0;
                        }
                    }
                    newOffset = firstPos;
                    lastPos = firstPos + bufferSize;
                    if( lastPos >= fileLen) lastPos = fileLen -1;
                    if( lastPos < 0) lastPos = 0;
                    if( firstPos + bufferSize >= fileLen) firstPos = fileLen -bufferSize;
                    if( firstPos < 0) firstPos=0;
                    
                    reader.seek( firstPos);

                    int tempSize = (int)(lastPos - firstPos);                        
                    
                    byte data[] = new byte[ tempSize];
                    int cnt = reader.read( data);
                    if( cnt > 0)
                    {      
                        String s = new String( data, 0, cnt);
                        fileBuffer.append(s);
                        newOffset = reader.getFilePointer();
                    }
                }
            }
            finally
            {
                try
                {
                    if( reader != null) reader.close();
                }
                catch( IOException ioe)
                {
                    LOGGER.debug("problem", ioe); // tried our best
                }
            }
        }
        return newOffset;
    }
    
    /**
     * Finds the system log file name for the supplier log file type
     * @param type - the type of log file to retrieve name for
     * @throws Exception - invalid type
     * @return filename of log file
     */
    public static String getLogFileName( final String type) throws Exception
    {
        if( type.equals( TYPE_LOG))
        {            
            String fileName = CProperties.getProperty( PROPERTY_LOG);
            
            if( StringUtilities.isBlank(fileName))
            {                
                fileName = getLog4JFileName(LOGGER );
            }
            
            return fileName;
        }
        else if( type.equals(TYPE_ERROR))
        {
            String fileName;
            fileName = CProperties.getProperty(PROPERTY_ERROR);
            return fileName;
        }
        else if( type.equals(TYPE_GC))
        {
            String fileName;
            fileName = CProperties.getProperty(PROPERTY_GC);
            return fileName;
        }
        else if( type.equals(TYPE_SQL))
        {
            String fileName;
            fileName = getLog4JFileName(CSQL.LOGGER_TIMINGS_SQL );
            return fileName;
        }
        else
        {
            throw new Exception( "LogUtil.findLogFile('" + type + "') - Unknown type");
        }
    }    
    
   /**
     * Retrieves a comma separated list of Strings representing the prefixes
     * used to retrieve additional log properties.
     * for e.g if MYLOG,MYBIGLOG is the value then the following properties 
     * are expected: 
     * MYLOG.title="MyLog"
     * MYLOG.filename="c:\logs\mylog.log"
     * MYBIGLOG.title="MyBigLog"
     * MYBIGLOG.filename="c:\logs\mybiglog.log"
     *
     * @return array of prefixes
     */
    public static String[] getAdditionalLogPrefixes( )
    {
        String additionalLogProps=CProperties.getProperty( PROPERTY_ADDITIONAL);
        if( StringUtilities.isBlank(additionalLogProps) )
        {
            return null;
        }
        
        StringTokenizer st = new StringTokenizer( additionalLogProps, ",");
        ArrayList list = new ArrayList();

        while( st.hasMoreTokens())
        {
            String prefix = st.nextToken().trim();

            if( StringUtilities.isBlank( getAdditionalLogTitle( prefix)) == false &&
                StringUtilities.isBlank( getAdditionalLogFilename( prefix)) == false)
            {
                list.add( prefix);
            }
            else
            {
                LOGGER.error( "Could not find properties for log prefix " + prefix);
            }

            
        }
        
        String prefixes[] = new String[list.size()];

        list.toArray( prefixes);

        return prefixes;
    }
    
   /**
     * Retrieves the title of a log file based on the prefix provided
     * for e.g if MYLOG is the prefix then the title will be located in the property
     * MYLOG.title="MyLog"
     *
    * @param prefix
    * @return title
     */
    public static String getAdditionalLogTitle( String prefix)
    {
        String title = CProperties.getProperty( prefix + ".title");        
        return title;    
    }
    
    
   /**
     * Retrieves the filename of a log file based on the prefix provided
     * for e.g if MYLOG is the prefix then the title will be located in the property
     * MYLOG.filename="c:\logs\mylog.log"
     *
    * @param prefix
    * @return filename
     */
    public static String getAdditionalLogFilename( String prefix)
    {
        String filename = CProperties.getProperty( prefix + ".filename");        
        return filename;    
    }
    
     /**
     * 
     * @param logger 
     * @return the value
     */
    public static String getLog4JFileName( final Object logger)
    {
        String fileName = null;
        
        Category logCategory = null;
                
        if( logger instanceof Log4JLogger)                
        {
            logCategory = ((Log4JLogger)logger).getLogger();
        }
        else if( logger instanceof QueueLog)
        {
            Log wrappedLog= ((QueueLog)logger).wrappedLog;
            if( wrappedLog instanceof Log4JLogger)
            {
                logCategory = ((Log4JLogger)wrappedLog).getLogger();
            }
        }
        
        while( StringUtilities.isBlank(fileName) && logCategory != null)
        {            
            Enumeration enumeration = logCategory.getAllAppenders(); 

            while( fileName == null && enumeration.hasMoreElements())
            {
                Appender appender = (Appender)enumeration.nextElement();

                if( appender instanceof FileAppender)
                {
                    FileAppender fa = (FileAppender)appender;

                    fileName = fa.getFile();
                }
            }

            logCategory = logCategory.getParent();
        }
        
        return fileName;
    }
        
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.LogUtil");//#LOGGER-NOPMD

}
