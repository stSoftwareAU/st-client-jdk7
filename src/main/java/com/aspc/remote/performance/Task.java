/*
 *  Copyright (c) 2004 ASP Converters pty ltd
 *
 *  www.aspconverters.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.performance;

import com.aspc.remote.util.misc.*;
import java.util.StringTokenizer;
import java.io.*;
import org.apache.commons.logging.Log;


/**
 *  Benchmark Task
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author Nigel Leck
 *  @since       7 April 2001
 */
public abstract class Task extends Item
{
    /** disable task reset */
    public static final String DISABLE_TASK_RESET="TASK_RESET";
    private String location = "";
    private String preFileName = null;
    private String postFileName = null;
    
    /**
     * 
     * @param name 
     * @param bmClient 
     */
    public Task(String name, BenchMarkClient bmClient)
    {
        super( name);

        this.bmClient = bmClient;

        max_acceptable_ms = 15 * 60 * 1000;
    }

    /**
     * 
     * @param data 
     */
    public void setData( final String data)
    {
        this.data = data;
    }
  
    /**
     * 
     * @param summary 
     */
    public void setSummary( String summary)
    {
        this.summary = summary;
    }

    /**
     * 
     * @return the value
     */
    public String getSummary( )
    {
        return summary;
    }

    /**
     * 
     * @param flag 
     */
    public void setMarkLog( String flag)
    {
        if( flag.toUpperCase().startsWith( "Y"))
        {
            this.markLog = true;
        }
        else
        {
            this.markLog = false;
        }
    }

    /**
     * This function sets file name
     * which contain the instructions
     * that are processed before the task 
     * is started
     * @param preFileName 
     */
    public void setPreFileName( String preFileName )
    {
        this.preFileName = getLocation() + preFileName;
    }

    /**
     * This function sets file name
     * which contain the instructions
     * that are processed after the task 
     * is completed
     * @param postFileName 
     */
    public void setPostFileName( final String postFileName )
    {
        this.postFileName = getLocation() + postFileName;
    }
    
    /**
     * This function returns the pre file name
     * @return String 
     */
    public String getPreFileName()
    {
        return preFileName;
    }

    /**
     * This function returns the post file name
     * @return String 
     */
    public String getPostFileName()
    {
        return postFileName;
    }
    
    /**
     * reset the server before starting any task ;
     * @throws Exception Exception A serious problem
     */
    @Override
    public void resetServer() throws Exception
    {
        if( CProperties.isDisabled(DISABLE_TASK_RESET) == false)
        {
            bmClient.getRemoteClient().execute(
               "MEMORY CLEAR 10;\n" +
               "MEMORY GC;"
            );

            if( serverResetSleepMS > 0)
            {
                LOGGER.info( getName() + " sleeping for " + TimeUtil.getDiff(0, serverResetSleepMS) + " after reset");
                Thread.sleep(serverResetSleepMS);
            }
            bmClient.getRemoteClient().execute(
               "LOG ECHO 'ping'"
            );
        }
    }
    
    
    /**
     * 
     * @throws Exception Exception A serious problem
     */
    @Override
    protected void preProcess() throws Exception
    {
        if( markLog)
        {
            bmClient.getRemoteClient().execute(
                "LOG WRITE '\n----- START: " + getName() + " -----\n';\n" +
                "LOG LOGGER timings.sql WRITE '\n----- START: " + getName() + " -----\n';"
            );
        }
    }


    /**
     * 
     * @param varying 
     */
    public void setVarying( String varying)
    {
        this.varying = varying;
    }

    /**
     * 
     * @return the value
     */
    public String getVarying( )
    {
        return varying;
    }

    /**
     * 
     * @param varyingMethod 
     */
    public void setVaryingMethod( String varyingMethod)
    {
        this.varyingMethod = varyingMethod;
    }

    /**
     * 
     * @return the value
     */
    public String getVaryingMethod( )
    {
        return varyingMethod;
    }

    /**
     * 
     * @param value the value
     * @return the value
     */
    public String getVaryListValue(String value)
    {
        return value;
    }

    /**
     * 
     * @param dataFile 
     */
    public void  setDataFileName( final String dataFile )
    {
        this.dataFile = getLocation() + dataFile;        
    }
    
    /**
     * 
     * @param xmlFile 
     * @throws Exception Exception A serious problem
     * @return the value
     */
    protected String readFile( String xmlFile ) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        BufferedReader in = new BufferedReader( new FileReader( xmlFile ) );
        String line;

        try
        {
            while( ( line = in.readLine() ) != null )//NOPMD
            {
                sb.append( line );
            }
        }
        finally
        {
            in.close();
        }
        
        return sb.toString();
    }

    /**
     * 
     * @param commandTemplate 
     * @throws Exception Exception A serious problem
     * @return the value
     */
    public String editCommand( String commandTemplate) throws Exception
    {
        String command=commandTemplate;
        
        if( StringUtilities.isBlank( varying) == false)
        {
            StringTokenizer st= new StringTokenizer( varying, ",");

            while( st.hasMoreTokens())
            {
                String name = st.nextToken().trim();

                String value;

                if (getVaryingMethod() != null)
                {
                    value = getVaryListValue(VaryList.nextValue( name, (int)(Math.random()*100000d)));
                }
                else
                {
                    value = getVaryListValue(VaryList.nextValue( name));
                }

                command = StringUtilities.replace( command, "%" + name + "%", value);
            }
        }

        command = StringUtilities.replace( command, "%CLIENT_NAME%", bmClient.getName());

        if( dataFile != null)
        {
            if( cacheDataFile == null)
            {
                cacheDataFile = readFile( dataFile);
            }
            command = StringUtilities.replace( command, "%DATA%", cacheDataFile);
        }
        
        return command.trim();
    }

    /**
     * 
     * @return the value
     */
    public String getData()
    {
        if( data == null) return "";

        return data;
    }
    
    /**
     * 
     * @throws Exception Exception A serious problem
     */
    @Override
    protected abstract void process() throws Exception;
    
    /**
     *
     */
    protected BenchMarkClient bmClient;
    private boolean markLog;

    private String  varying;
    private String  varyingMethod;
    /**
     *
     */
    protected String  summary;
    private String dataFile;    
    private String cacheDataFile;
    /**
     *
     */
    protected String resetFlag;
    /**
     * @return Returns the location.
     */
    public String getLocation()
    {
        return location;
    }
    /**
     * @param location The location to set.
     */
    public void setLocation( String location )
    {
        this.location = location;
    }
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.performance.Task");//#LOGGER-NOPMD
}
