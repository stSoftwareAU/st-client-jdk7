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
package com.aspc.remote.util.net;

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.FileUtil;
import com.aspc.remote.util.misc.StringUtilities;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;


/**
 * Implements the file protocol for NetClient
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 * @author      Jason McGrath
 * @since       February 21, 2006
 */
public class NetClientFile implements NetClient
{
    
    /**
     * Checks if the file specified by the given arguments physically exists
     * @param path the file path
     * @return boolean true if it exists
     * @throws Exception a serious problem
     */
    @Override
    public boolean exists( final String path ) throws Exception
    {
        File rawFile = new File( makePathAbsolute( path));
                
        boolean found = rawFile.exists();               
        
        return found;
    }
    
    /**
     * Fetches the file specified by the given arguments
     * 
     * @param path the path of the file
     * @param target the target file
     * @throws Exception a serious problem
     */
    @Override
    public void fetch( final String path, final File target ) throws Exception
    {                
        File rawFile = new File( makePathAbsolute( path));
        if( rawFile.equals( target))
        {
            return;
        }
        
        File dir = target.getParentFile();

        FileUtil.mkdirs(dir);
        FileUtil.copy( rawFile, target);
    }
    
    /**
     * Removes the physical file specified by the given arguments
     * @param path the file to remove
     * @throws Exception a serious problem
     */
    @Override
    public void remove( final String path ) throws Exception
    {
        File targetFile = new File( makePathAbsolute( path));
        
        if( targetFile.exists() && targetFile.isFile())
        {
            FileUtil.deleteAll(targetFile);
        }
    }
    
    /**
     * Renames the physical file specified by the given arguments
     * @param from the current path of the file
     * @param to the new path of the file
     * @throws Exception a serious problem
     */
    @Override
    public void rename( final String from, final String to ) throws Exception
    {
        // creates directories if they do not exist
        String tmp = FileUtil.getBaseFromPath( makePathAbsolute( to ) );
        if( !exists( tmp ) )
        {
            changeDirectory( tmp, true );
        }

        File fromFile = new File( makePathAbsolute( from));
        File toFile = new File( makePathAbsolute(to));
        
        if( fromFile.renameTo( toFile ) == false )
        {
            throw new Exception( "could not rename file " + fromFile.getAbsolutePath() + "->" + toFile.getAbsolutePath() );
        }

    }
    
    /**
     * Sends the file specified by the given arguments to the given location
     * @param rawFile the file to send
     * @param path the path to send the file to
     * @throws Exception a serious problem
     */
    @Override
    public void send( final File rawFile, final String path ) throws Exception
    {
        String absPath = makePathAbsolute( path);
        changeDirectory( getBaseFromPath(absPath), true );
            
        File targetFile = new File( absPath);
        if( ! targetFile.equals( rawFile ) )
        {
            FileUtil.copy( rawFile, targetFile);
        }
    }
        /**
     * Gets the directory portion of a file path, if there is one.
     *
     * @param path the file path
     * @return String the directory portion
     */
    public static String getBaseFromPath( final String path )
    {
        String base = "";
        
        int position = path.lastIndexOf( File.separatorChar );
        if( position != -1 )
        {
            base = path.substring( 0, position );
        }
        
        return base;
    }
    
    /**
     * Changes the base directory
     * @param create create the directory if it doesn't exist
     * @param path the directories to create
     * @return boolean true if the path could be changed
     */
    @Override
    public boolean changeDirectory( final String path, final boolean create ) 
    {
        boolean changed = false;
        File pathFile = null;
        
        if( ! StringUtilities.isBlank( path ) )
        {
            String tmp = normalize( path );
                        
            if( tmp.length() > 0 )
            {
                pathFile = new File( tmp );
                changed = pathFile.exists();
            }
            else
            {
                changed = true;
            }
        }
        
        if( !changed && create )
        {
            try
            {
                FileUtil.mkdirs(pathFile);
                changed = true;
            }
            catch( IOException io)
            {
                LOGGER.warn( "change directory " + path, io);
            }
        }        
        
        return changed;
    }
    
    /**
     * Gets the current working directory
     * @return String the current working directory
     * @throws Exception a serious problem
     */
    @Override
    public String getDirectory() throws Exception
    {        
        return originalPath;
    }
    
    /**
     * Gets the protocol prefix that this client implements
     * @return String the protocol type
     */
    @Override
    public String getType()
    {
        return PREFIX_FILE;
    }
    
    /**
     * Checks that the client is able to connect and login.
     * expects the url to be in the format username:password@server
     *
     * @param url the url to check
     * @return boolean true if the client can connect, false otherwise
     */
    @Override
    public boolean canConnect( final String url )
    {
        boolean connectable = false;
        
        try
        {            
            exists( url);     
            connectable = true;
        }
        catch( Exception failed )
        {
            LOGGER.warn( url+" is not connectable", failed );
        }
        
        
        return connectable;
    }
    
    /**
     * Activates the client, also used by pooling
     */
    @Override
    public void activate()
    {
        // clear the progress listener
        monitor = null;
    }
    
    /**
     * Destroys the client, also used by pooling
     */
    @Override
    public void destroy()
    {
    }
    
    /**
     * Makes a new client, also used by pooling. Expects the url to be in the
     * following format://base_path
     * @param url the client url
     * @throws Exception a serious problem
     */
    @Override
    public void make( final String url ) throws Exception
    {        
        originalPath = url;
        basePath = url;
        
        if( basePath.startsWith( PREFIX_FILE ) )
        {
            basePath = basePath.substring( PREFIX_FILE.length() );
        }
        
        if( basePath.length() > 0 )
        {
            basePath = normalize(basePath);
            
            if( basePath.endsWith(File.separator))
            {
                basePath = basePath.substring( 0, basePath.length()-1);
            }          

            if( !changeDirectory( basePath, true ) )
            {
                throw new Exception( "directory "+basePath+ " does not exist" );
            }
        }        
    }
    
    /**
     * Validates a client, also used by pooling
     * @return boolean true if the client is valid, false otherwise
     */
    @Override
    public boolean validate()
    {
        return true;
    }
    
    private String makePathAbsolute( String path) throws Exception
    {
        String result;
        String tmpPath = path;
        
        if( basePath == null)
        {
            throw new Exception( "Client not initialised: No Base Path");
        }
        
        if( tmpPath.startsWith( PREFIX_FILE ) )
        {
            tmpPath = tmpPath.substring( PREFIX_FILE.length() );
        }
        
        tmpPath = normalize(tmpPath);
        
        
        if( tmpPath.startsWith( basePath) &&
                ( tmpPath.length() <= basePath.length() ||
                tmpPath.substring(basePath.length(), basePath.length() + 1).equals( File.separator))
                )
        {
            result =  tmpPath;
        }
        else
        {
            result = basePath + "/" + tmpPath;
        }
        
        return result;
    }
    
    private String normalize( final String path)
    {
        if( StringUtilities.isBlank( path))
        {
            return "";
        }                
        
        String tmp = path;
        if( tmp.startsWith( PREFIX_FILE ) )
        {
            tmp = tmp.substring( PREFIX_FILE.length() );
        }
        
        File fl = new File( tmp);
        return fl.getPath();
    }
    
    /**
     * Sets the progress listener. The progress listener will be cleared by a call
     * to activate.
     * @param monitor the progress listener
     */
    @Override @CheckReturnValue @Nullable
    public ProgressListener setProgressListener( final @Nullable ProgressListener monitor )
    {
        ProgressListener previous=this.monitor;
        this.monitor = monitor;
        return previous;
    }
    
    private String basePath,
                   originalPath;
    
    private ProgressListener monitor;//NOPMD
    
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.net.NetClientFile");//#LOGGER-NOPMD

    /**
     * Names of files in allocation
     * @return file names
     * @throws Exception io problem
     */
    @Override
    public String[] retrieveFileList( ) throws Exception
    {
        String[] fileList;
        
        File file = new File( basePath);
        File files[] = file.listFiles();
        
        ArrayList fileNames = new ArrayList();
        
        for (File file1 : files) 
        {
            if (file1.isDirectory() == false) 
            {
                fileNames.add(file1.getName());
            }
        }
        
        fileList = (String[]) fileNames.toArray(new String[fileNames.size()]);
        
        return fileList;
    }


     /**
     * Get the underlying connection object
     * @return the null as there is no underlying connection object
     */
    @Override
    public Object getUnderlyingConnectionObject()
    {
        return null;
    }    
}
