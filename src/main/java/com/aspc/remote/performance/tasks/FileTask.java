/*
 *  Copyright (c) 2004 ASP Converters pty ltd
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.performance.tasks;

import com.aspc.remote.performance.BenchMarkClient;
import com.aspc.remote.performance.Task;
import com.aspc.remote.soap.Client;
import com.aspc.remote.util.misc.CProperties;
import com.aspc.remote.util.misc.FileUtil;
import com.aspc.remote.util.misc.StringUtilities;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import org.w3c.dom.Element;

/**
 *  File task.
 *
 * @author Nigel Leck
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @since       February 17, 2006 06:03:17
 */
public class FileTask extends Task
{
    private String type,
            fileKeyPropName, fsVolumeName,targetName,location,cachePath;
    private long mFileSize;
    
    private static final String FILE_NAME="stresstest.txt"; 
    private static final long DEFAULT_FILE_SIZE=1000; 
    
    /**
     * Default constructor.
     * @param name The name
     * @param bmClient The benchmark client
     */
    public FileTask(String name, BenchMarkClient bmClient)
    {
        super( name, bmClient);
        mFileSize = DEFAULT_FILE_SIZE;
    }
   
    
    /**
     * Set item attributes.
     * @param element The xml element to fill out from
     * @throws Exception If something went wrong
     */
    @Override
    protected void setItemAttributes(Element element) throws Exception
    {
        super.setItemAttributes(element);
        
        if( element.hasAttribute( "type"))
        {
            type = element.getAttribute( "type");
        } 
        if( element.hasAttribute( "fs_volume_name"))
        {
            fsVolumeName = element.getAttribute( "fs_volume_name");
        } 
        if( element.hasAttribute( "file_target"))
        {
            targetName = element.getAttribute( "file_target");
        }
        if( element.hasAttribute( "file_key_prop_name"))
        {
            fileKeyPropName = element.getAttribute( "file_key_prop_name");
        } 
        if( element.hasAttribute( "location"))
        {
            location = element.getAttribute( "location");
        }
        if( element.hasAttribute( "cache_path"))
        {
            cachePath = element.getAttribute( "cache_path");
        }
        if( element.hasAttribute( "file_size"))
        {
            try 
            {
                mFileSize = Long.parseLong(element.getAttribute( "file_size"));
            }
            catch(Exception ex)
            {
                mFileSize = DEFAULT_FILE_SIZE;
            }
        }
    }
    
    /**
     * Processes the task.
     * @throws Exception If something went wrong
     */
    @Override
    protected void process() throws Exception
    {
        
        if("CLEANUP".equalsIgnoreCase(type))
        {
            doCleanup();
        }
        if("CREATE".equalsIgnoreCase(type))
        {
            doCreate();
        }
        else if("UPDATE".equalsIgnoreCase(type))
        {
            doUpdate();
        }
        else if("READ".equalsIgnoreCase(type))
        {
            doRead();
        }
        else if(CProperties.PROPERTY_DISABLE.equalsIgnoreCase(type))
        {
            throw new Exception("Type " + CProperties.PROPERTY_DISABLE + " is not supported any more");
        }
        else if("ENABLE".equalsIgnoreCase(type))
        {
            throw new Exception("Type ENABLE is not supported any more");
        }
    }
    /**
     * Performs cleanup.
     * @throws Exception If something went wrong
     */
    private void doCleanup() throws Exception
    {   
        Client client = bmClient.getRemoteClient();
        client.execute( "delete from dbfile where name = "+ targetName );
        client.execute( "commit" );
        
        File file = new File(FILE_NAME);
        FileUtil.deleteAll(file);
    }
    
    /**
     * Performs create.
     * @throws Exception If something went wrong
     */
    private void doCreate() throws Exception
    {   
        File file = getStressTestFile(FILE_NAME, mFileSize);
        String tmpKey = null;
        Client client = bmClient.getRemoteClient();
        tmpKey = client.checkInFile(targetName, file, location);
        client.execute("commit");
        if(! StringUtilities.isBlank(fileKeyPropName))
        {
            System.setProperty(fileKeyPropName, tmpKey);
        }
    }
    /**
     * Performs submit.
     * @throws Exception If something went wrong
     */
    private void doUpdate() throws Exception
    {
        if(StringUtilities.isBlank(fileKeyPropName))
        {
            throw new Exception("required parameter 'file key' is missing");
        }
        String key = System.getProperty(fileKeyPropName);
        if(StringUtilities.isBlank(key))
        {
            throw new Exception("required parameter 'file key' is missing");
        }
        File file = getStressTestFile(FILE_NAME, mFileSize);
        
        Client client = bmClient.getRemoteClient();
        client.updateFile(key, file);
        client.execute("commit");
    }
    /**
     * Performs fetch.
     * @throws Exception If something went wrong
     */
    private void doRead() throws Exception
    {
        Client client = bmClient.getRemoteClient();
        if(! StringUtilities.isBlank(cachePath))
        {
            client.setCacheDirectory(cachePath);
        }
        
        client.readFile(targetName, location);
    }
    
    /**
     * Creates test file.
     * @param fName The file name
     * @param fileSize The file size
     *
     * @throws Exception A serious problem
     * @return new file
     */
    private File getStressTestFile(String fName, long fileSize) throws Exception
    {
        File file = new File(fName);
        if(! file.exists())
        {
            try
            (Writer output = new BufferedWriter( new FileWriter(file) )) {
                for (long l=0;l<fileSize;l++)
                {
                    output.write( "A" );
                }
            }
            if ( ! file.exists())
            {
                throw new Exception("Raw file has not been generated.");
            }
        }
        return file;
    }
}
