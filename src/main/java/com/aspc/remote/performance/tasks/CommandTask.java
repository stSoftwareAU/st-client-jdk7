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

import com.aspc.remote.jdbc.SoapResultSet;
import com.aspc.remote.performance.BenchMarkClient;
import com.aspc.remote.performance.Task;
import com.aspc.remote.performance.ValidateResultException;
import com.aspc.remote.soap.Client;
import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.DocumentUtil;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.misc.TimeUtil;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.regex.Matcher;
import org.apache.commons.logging.Log;
import org.w3c.dom.Document;

/**
 *  command task
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author Nigel Leck
 *  @since       7 April 2001
 */
public class CommandTask extends Task
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.performance.tasks.CommandTask");//#LOGGER-NOPMD

    /**
     *
     * @param name
     * @param bmClient
     */
    public CommandTask(String name, BenchMarkClient bmClient)
    {
        super( name, bmClient);
    }

    @SuppressWarnings("SleepWhileInLoop")
    private SoapResultSet runCommand( final String command) throws Exception
    {
        Client client = bmClient.getRemoteClient();
        if( isJob())
        {
            long cancelMS = getCancelMS();

            if( cancelMS <= 0)
            {
                long maxFirst = getMaxAcceptableFirst();

                if( maxFirst > 0)
                {
                    cancelMS = GC_MAX_TIME + maxFirst + 60000;
                }
            }

            StringBuilder buffer = new StringBuilder("JOB ");

            if( cancelMS > 0)
            {
                buffer.append( "MAX_RUN_TIME '");
                buffer.append( cancelMS);
                buffer.append( " MS' ");
            }


            buffer.append( "EXECUTE\n{\n");
            buffer.append( command);
            buffer.append("\n}");
            SoapResultSet r = client.fetch(buffer.toString());
            r.next();

            String jobID = r.getString( "job_id");
            long jobStart = System.currentTimeMillis();
            while( true)
            {
                try
                {
                    r = client.fetch( "JOB STATUS " + jobID + " BLOCK 60000");
                }
                catch( SocketException se)
                {
                    LOGGER.info( "socket issue", se);
                    Thread.sleep( 60000);
                }
                String jobStatus;

                r.next();

                jobStatus = r.getString( 1);

                if(
                    "RN".equalsIgnoreCase( jobStatus ) ||
                    "WT".equalsIgnoreCase( jobStatus )
                )
                {
                    LOGGER.info( "running " + jobID + " for " + TimeUtil.getDiff(jobStart));
                }
                else if( "OK".equalsIgnoreCase( jobStatus ))
                {
                    break;
                }
                else
                {
                    String message = r.getString( "message");
                    throw new Exception(
                        "job " + jobID + " returned " + jobStatus +
                        " after " + TimeUtil.getDiff(jobStart) + " message: " +message
                    );
                }
            }

            r = client.fetch("JOB DATA " + jobID);
            r.next();

            String edata = r.getString( "result");

            String encoding = r.getString( "encoding");

            String tmp = StringUtilities.decompress( edata, encoding);

            Document doc = DocumentUtil.makeDocument( tmp);

            SoapResultSet rs = new SoapResultSet( doc, 0, client);
            return rs;
        }
        else
        {
            return client.fetch( command);
        }
    }

    /**
     *
     * @throws Exception a serious problem
     */
    @Override
    protected void process() throws Exception
    {
        String command = getData();

        command = editCommand( command);

        if(StringUtilities.isBlank( summary))
        {
            try (SoapResultSet srs = runCommand( command)) {
                validateResult( srs );
            }
        }
        else
        {
            SoapResultSet r = runCommand( command);
            SoapResultSet prv = r;
            while( r != null)
            {
                validateResult( r );
                int columns = r.getColumnCount();
                for( int col = 1; col <= columns; col++)
                {
                    String colName = r.getColumnName( col);
                    if ( summary.equalsIgnoreCase(colName))
                    {
                        while( r.next())
                        {
                            addCount(r.getString( col));
                        }
                    }
                }
                prv = r;
                r = r.nextResultSet();
            }
            prv.close();
        }
    }

    private HashMap map = null;
    private void addCount(String key)
    {
        if(map == null)
        {
            map = HashMapFactory.create();
        }
        double[] d = (double[]) map.get(key);
        if(d == null)
        {
            map.put(key, new double[]{1.0});
        }
        else
        {
            d[0]++;
        }
    }

    private void validateResult( final SoapResultSet srs ) throws ValidateResultException, SQLException, Exception//NOPMD
    {
        if( validatePattern == null ) return;

        if( srs == null )
        {
            throw new ValidateResultException( "no results to validate");
        }

        while( srs.next())
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

            if( validatePattern != null)
            {
                Matcher m = validatePattern.matcher(value);
                if( m.matches() == false)
                {
                    throw new ValidateResultException( value + " doesn't match " + validatePattern);
                }
            }

           /* if( validateFormula != null)
            {
                Double d = null;

                if( StringUtilities.isBlank( value) == false)
                {
                    d = new Double( value);
                }
                ValidateQuery q = new ValidateQuery( d);
                StringBuilder info=new StringBuilder();
                double calculatedValue = validateFormula.calculate(q, info, null);

                if( CUtilities.isZero(calculatedValue))
                {
                    throw new ValidateResultException( info + " is zero" );
                }
            }*/
        }
    }
}
