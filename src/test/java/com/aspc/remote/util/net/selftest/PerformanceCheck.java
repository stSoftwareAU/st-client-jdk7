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
package com.aspc.remote.util.net.selftest;

import org.apache.commons.logging.Log;
import com.aspc.remote.memory.internal.MemoryUtil;
import com.aspc.remote.application.AppCmdLine;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.FileUtil;
import com.aspc.remote.util.misc.NumUtil;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.misc.TimeUtil;
import java.io.File;
import java.io.RandomAccessFile;
import com.aspc.remote.util.net.*;
import java.util.Properties;
import org.apache.commons.cli.Options;

/**
 * check the performance of transferring files to different locations.
 *
 * separate check for send and fetch
 * small medium and large file size
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED application</i>
 *
 * @author Nigel Leck
 * @since 29 September 2006
 */
public class PerformanceCheck extends AppCmdLine
{
    private static final long FILE_SIZE[]={100, 1000, 10000};
    
    private static final int LOOP=6;
    
    private String urls;
    private static String testFilePath = null;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.net.selftest.PerformanceCheck");//#LOGGER-NOPMD

    /**
     * create a new PerformanceCheck for the comma separate list of location URLs
     */
    public PerformanceCheck()
    {
    }
    
    /**
     * add extra command line options
     * @param options the options
     */
    @Override
    protected void addExtraOptions( final Options options)
    {
        super.addExtraOptions( options);
        options.addOption( "u", true, "The URLs" );
        options.addOption( "f", true, "test file path" );
    }
    

    /**
     * handle the args
     * @param p the args
     * @throws Exception a serious problem
     */
    @Override
    public void handleArgs(Properties p) throws Exception
    {
        super.handleArgs(p);
        urls = p.getProperty("-u", "ftp://guest:guest@localhost");
        testFilePath = p.getProperty("-f");
    }

    /**
     * Check
     */
    @Override
    public void process()
    {
        try
        {
            String result = check( urls);
            LOGGER.info( result);
        }
        catch( Throwable e)
        {
            LOGGER.error( "checking performance", e);
        }
    }
    
    /**
     * performs a check for each given URL, then returns a formatted message.
     * @return String the formatted check result
     * @param locationURLs the urls to check
     * @throws Exception a serious problem
     */
    public static String check(final String locationURLs) throws Exception
    {
        File[] files = null;
        
        if (testFilePath != null && !testFilePath.equals(""))
        {
            files = new File[1];
            files[0] = new File(testFilePath);
        }
        else
        {
            files = new File[FILE_SIZE.length];
            
            for( int i = 0; i < FILE_SIZE.length; i++)
            {
                files[i] = createTestFile(FILE_SIZE[i]);
            }
        }
        
        String result = "";
        String[] list = locationURLs.split(",");

        for (String list1 : list) //test loop
        {
            String tmp = StringUtilities.stripPasswordFromURL(list1);
            result += tmp;
//            long firstStart = System.currentTimeMillis();
            NetUtil.sendData(files[0], list1, files[0].getName());
            //result += " (Connection " + TimeUtil.getDiff( firstStart) +")\n";
            result += "\n";
            for (File file : files) {
                long size = file.length();
                String fileName = "file size "+ NumUtil.convertMemoryToHumanReadable( size);
                result += "\t" + fileName + "\n";
                long minSend=Long.MAX_VALUE;
                long maxSend=Long.MIN_VALUE;
                long avgSend=0L;
                long minFetch=Long.MAX_VALUE;
                long maxFetch=Long.MIN_VALUE;
                long avgFetch=0L;
                for (int k = 0; k < LOOP; k++) {
//                    LOGGER.info( ".");
                    long start;
                    long stop;
                    long time;
                    start = System.currentTimeMillis();
                    NetUtil.sendData(file, list1, file.getName());
                    stop = System.currentTimeMillis();
                    time = stop - start;
                    if( time < minSend ) minSend = time;
                    if( time > maxSend ) maxSend = time;
                    avgSend += time;
                    // Fetch
                    start = System.currentTimeMillis();
                    File receivedFile;
                    receivedFile = NetUtil.fetchData(list1, file.getName(), false, null);
                    stop = System.currentTimeMillis();
                    if( receivedFile.length() != size)
                    {
                        throw new Exception( "Failed to recieve " + file);
                    }
                    receivedFile.delete();
                    time = stop - start;
                    if( time < minFetch ) minFetch = time;
                    if( time > maxFetch ) maxFetch = time;
                    avgFetch += time;
                    NetUtil.removeData(list1, file.getName());
                }
//                LOGGER.info( "");
                String msg;
                msg = "Send :: Min "+TimeUtil.getDiff(0L,minSend)+" Max "+TimeUtil.getDiff(0L,maxSend)
                        +" Avg "+TimeUtil.getDiff(0L,(avgSend/LOOP));
                LOGGER.info( fileName + " " + msg);
                result += "\t\t" + msg +"\n";
                msg = "Fetch :: Min "+TimeUtil.getDiff(0L,minFetch)+" Max "+TimeUtil.getDiff(0L,maxFetch)
                        +" Avg "+TimeUtil.getDiff(0L,(avgFetch/LOOP));
                LOGGER.info( fileName + " " + msg);
                result += "\t\t" + msg +"\n";
            }
/*
            for( int j = 0; j < files.length; j++ )
            {
            String fileName = "file size "+(files[j].length()/1024)+"K";
            result += "\t" + fileName + "\n";
            long min=99999L;
            long max=0L;
            long avg=0L;
            NetUtil.sendData( files[j], list[i], files[j].getName() );
            File f = null;
            for( int k = 0; k < rep; k++ )
            {
            start = System.currentTimeMillis();
            f = NetUtil.fetchData( list[i], files[j].getName(), false );
            stop = System.currentTimeMillis();
            f.delete();
            stop -= start;
            if( stop < min ) min = stop;
            if( stop > max ) max = stop;
            avg += stop;
            }
            NetUtil.removeData( list[i], files[j].getName() );           
            result += "\t\tFetch :: Min "+TimeUtil.getDiff(0L,min)+" Max "+TimeUtil.getDiff(0L,max)
            +" Avg "+TimeUtil.getDiff(0L,(avg/rep))+"\n";
            }
             */
            result += "\n";
        }
        
        if (testFilePath == null || testFilePath.equals(""))
        {
            for (File file : files) { //clean up
                file.delete();
            }
        }
        
        return result;
    }
    
    private static File createTestFile( final long size ) throws Exception
    {
        File tmp = File.createTempFile("perf"+size+"K",null, FileUtil.makeQuarantineDirectory());
        try (RandomAccessFile raf = new RandomAccessFile( tmp, "rws" )) {
            raf.setLength(size*1024);
        }
        return tmp;
    }
    
    /**
     * The main for the program
     * @param argv the parameters
     */
    public static void main(String argv[])
    {
        new PerformanceCheck().execute(argv);
    }
}
