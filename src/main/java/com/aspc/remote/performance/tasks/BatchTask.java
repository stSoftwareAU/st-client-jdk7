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
package com.aspc.remote.performance.tasks;

import org.apache.commons.logging.Log;
import com.aspc.remote.performance.BenchMarkClient;
import com.aspc.remote.performance.Task;
import com.aspc.remote.performance.ValidateResultException;
import com.aspc.remote.jdbc.SoapResultSet;
import com.aspc.remote.soap.Client;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.FileUtil;
import com.aspc.remote.util.misc.StringUtilities;
import java.sql.SQLException;

/**
 * App
 * 
 * @author Nigel Leck
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 * @since 7 April 2001
 */
public class BatchTask extends Task
{
    /**
     * 
     * @param name 
     * @param bmClient 
     */
    public BatchTask( String name, BenchMarkClient bmClient )
    {
        super( name, bmClient );
    }

    /**
     * 
     * @throws Exception a serious problem
     */
    @Override
    protected void process() throws Exception
    {
        /**
         * All the instructions (pre, post and the command) 
         * within this task are executed as one single transaction 
         */
        StringBuilder commandBuffer = new StringBuilder();
        
        String fn = getPreFileName();
        
        commandBuffer.append( FileUtil.readFile(  fn) );

        commandBuffer.append( getData().trim() );
        
        fn = getPostFileName();
        
        commandBuffer.append( FileUtil.readFile( fn ) );

        boolean needtToCancel = false;
        String command = "";
        if( isJob() )
        {
            command = editCommand( commandBuffer.toString() );
            if( getCancelMS() > 0 )
            {
                needtToCancel = true;
            }
        }
        else
        {
            command = super.editCommand( commandBuffer.toString() );
        }

        Client c = bmClient.getRemoteClient();
        long jobStartMS = System.currentTimeMillis();

        SoapResultSet r = c.fetch( command );
        validateResult( r );
        r.rewind();

        if( r.getColumnName( 1 ).equals( "result" ) )
        {
            LOGGER.info( "Got the result: " );

            if( needtToCancel )
            {
                LOGGER.info( "Cannot Cancel: Job Ran in sync mode " );
            }
            return;
        }

        String jobID = null;
        if( r.next() )
        {
            jobID = r.getString( 1 );
            LOGGER.info( "Job Id: " + jobID );
        }

        if( jobID != null )
        {
            String jobstatus = "WT";
            SoapResultSet jobstatus_result = null;
            boolean cancelled = false;
            while( "RN".equalsIgnoreCase( jobstatus )
                    || "WT".equalsIgnoreCase( jobstatus ) )
            {
                try
                {
                    jobstatus_result = c.fetch( "JOB STATUS " + jobID + " BLOCK 30000");
                    //jobstatus_result.rewind();
                    if( jobstatus_result.next() )
                    {
                        jobstatus = jobstatus_result.getString( 1 );
                    }
                }
                catch( Exception e)
                {
                    LOGGER.warn("problem checking status", e);
                    Thread.sleep(1000);
                }
                
               // LOGGER.info( "Job Id: " + jobID + " Job Status: " + jobstatus );
                if( needtToCancel && ( cancelled == false ) )
                {
                    long jobMS = System.currentTimeMillis();
                    long jobElapsedMS = jobMS - jobStartMS;
                    if( jobElapsedMS < 0 )
                        jobElapsedMS = 0;
                    doJobCancel( c, jobID, jobElapsedMS );
                    cancelled = true;
                }
            }
            bmClient.getRemoteClient().fetch(
                    "JOB DATA " + jobID );
            if( "OK".equalsIgnoreCase( jobstatus ) )
            {
                LOGGER.info( "Got the result: " );
                return;
            }
            else if( "ER".equalsIgnoreCase( jobstatus ) )
            {
                LOGGER.info( jobstatus_result.formatOutput() );
                return;
            }
        }
    }

    /**
     * 
     * @param valueTemplate 
     * @return the value
     */
    @Override
    public String getVaryListValue( String valueTemplate )
    {
        String value = valueTemplate;
        
        try
        {
            if( value.startsWith( "%f" ) )
            {
                value = readFile( getLocation() + value.substring( 2 ) );
            }
        }
        catch( Exception exp )
        {
            LOGGER.error( "error", exp);
        }
        return value;
    }

    private void doJobCancel( Client c, String job_id, long jobElapsedMS )
            throws Exception
    {

        if( jobElapsedMS > getCancelMS() )
        {
            c.fetch( "JOB CANCEL " + job_id );
            LOGGER.info( "Job Cancelled: " + job_id );
        }
    }

    /**
     * 
     * @param command 
     * @throws Exception a serious problem
     * @return the value
     */
    @Override
    public String editCommand( final String command ) throws Exception
    {
        String cmd = ( super.editCommand( command ) ).trim();
        if( cmd.startsWith( BATCH_COMMAND ) )
        {
            cmd = BATCH_JOB + " { " + cmd + " }";
        }

        return cmd;
    }

    /**
     *  validating the batch result.
     * @param srs
     * @throws ValidateResultException
     * @throws SQLException if a database-access error occurs.
     * @throws Exception a serious problem.
     */
    private void validateResult( final SoapResultSet srs ) throws ValidateResultException, SQLException, Exception//NOPMD
    {
        if( matchResult == null) return;

        if( srs == null )
        {
            throw new ValidateResultException( "no results to validate");
        }

        if( srs.next())
        {
            String value;
            
            if( StringUtilities.isBlank(summary))
            {
                value = srs.getString(1);
            }
            else
            {
                value = srs.getString(summary);
            }


            if( StringUtilities.isLike(matchResult, value) == false)
            {
                 throw new ValidateResultException( value + " doesn't match " + matchResult);
            }
        }
    }

    private static final String                        BATCH_COMMAND = "BATCH RETRIEVAL";

    private static final String                        BATCH_JOB     = "BATCHJOB";

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.performance.tasks.BatchTask");//#LOGGER-NOPMD
}
