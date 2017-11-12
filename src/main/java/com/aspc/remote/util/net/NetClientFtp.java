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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.apache.commons.net.io.Util;

/**
 * Implements the ftp protocol for NetClient
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 * @author      luke
 * @since       February 21, 2006
 */
public class NetClientFtp implements NetClient, CopyStreamListener
{
    private FTPClient ftp;
    private String rootDir; //#NOPMD
    private ProgressListener monitor;
    private boolean deadConnection;
    
    /**
     * Checks if the file specified by the given arguments physically exists
     * @return boolean true if it exists
     * @param path the file path
     * @throws Exception a serious problem
     */
    @Override
    public boolean exists( final String path ) throws Exception
    {
        boolean found = false;
        
        if( ftp != null && ftp.isConnected() )
        {
            String[] tokens;
            if( path.contains("/") )
            {
                tokens = path.split("/");
            }
            else
            {
                tokens = new String[]{path};
            }
            
            // check each element of the path
            String tmpPath = "";
            for (String token : tokens) 
            {
                // dodge empty tokens
                if (token.length() == 0) 
                {
                    continue;
                }
                FTPFile[] ls;
                if( tmpPath.length() > 0 )
                {
                    ls = ftp.listFiles( tmpPath );
                }
                else
                {
                    ls = ftp.listFiles();
                }
                found = contains(ls, token);
                if (found) {
                    tmpPath += token + "/";
                } else {
                    break;
                }
            }
        }
        
        return found;
    }
    
    /**
     * Fetches the file specified by the given arguments
     * @param path the path of the file
     * @param target the target file
     * @throws Exception a serious problem
     */
    @Override
    public void fetch( final String path, final File target ) throws Exception
    {
        // validate input
        if( StringUtilities.isBlank( path ) )
        {
            deadConnection=true;
            throw new Exception( "fetch 'path' is blank" );
        }
        
        if( target == null )
        {
            deadConnection=true;
            throw new Exception( "fetch 'target' file is null" );
        }
        
        if( path.startsWith( "/" ) )
        {
            deadConnection=true;
            throw new Exception( "fetch 'path' must be relative: "+path );
        }
        
        // try to fetch
        if( ftp != null && ftp.isConnected() )
        {
            FileOutputStream fos  = null;
            InputStream is = null;
            
            try
            {
                fos = new FileOutputStream( target );

                if( monitor != null )
                {
                    // ftp.completePendingCommand must be called in finally block
                    // after closing the streams, some ftp servers will hang otherwise
                    is = ftp.retrieveFileStream( path );
                    Util.copyStream( is, fos, ftp.getBufferSize(), CopyStreamEvent.UNKNOWN_STREAM_SIZE, this );
                }
                else
                {
                    if( ftp.retrieveFile( path, fos ) == false )
                    {
                        deadConnection=true;
                        throw new Exception( "ftp fetch failed for " + path + " reply: " + ftp.getReplyString() );
                    }
                }
            }
            finally
            {
                if( fos != null )
                {
                    try
                    {
                        fos.close();
                    }
                    catch( IOException e )
                    {
                        deadConnection=true;                        
                        // ensure that ftp.completePendingCommand still gets called
                        LOGGER.warn( "could not close output stream",e );
                    }
                }
                
                if( is != null )
                {
                    try
                    {
                        is.close();
                    }
                    catch( IOException e )
                    {
                        deadConnection=true;                        
                        // enusre that ftp.completePendingCommand still get called
                        LOGGER.warn( "could not close input stream",e);
                    }
                }
                
                if( monitor != null )
                {
                    try
                    {
                        ftp.completePendingCommand();
                    }
                    catch( IOException ioe )
                    {
                        deadConnection=true;
                        // the connection might have been closed prematurely
                        LOGGER.warn( "could not complete ftp command reply: " + ftp.getReplyString(),ioe );
                    }
                }
            }
        }
        else
        {
            deadConnection=true;
            throw new Exception( "ftp is not connected, must call make before fetch" );
        }
    }
    
    /**
     * Removes the physical file specified by the given arguments
     * @param path the file to remove
     * @throws Exception a serious problem
     */
    @Override
    public void remove( final String path ) throws Exception
    {
        if( ftp != null && ftp.isConnected() )
        {
            ftp.deleteFile( path );
        }
        else
        {
            deadConnection=true;
            throw new Exception( "ftp is not connected, must call make before remove" );
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
        // validate input
        if( StringUtilities.isBlank( from ) )
        {
            deadConnection=true;
            throw new Exception( "rename 'from' is blank" );
        }
        
        if( StringUtilities.isBlank( to ) )
        {
            deadConnection=true;
            throw new Exception( "rename 'to' is blank" );
        }
        
        if( from.startsWith( "/" ) )
        {
            deadConnection=true;
            throw new Exception( "rename 'from' must be relative: "+from );
        }
        
        if( to.startsWith( "/" ) )
        {
            deadConnection=true;
            throw new Exception( "rename 'to' must be relative: "+to );
        }
        
        // try to rename
        if( ftp != null && ftp.isConnected() )
        {
            // creates directories if they do not exist
            String base = FileUtil.getBaseFromPath( to );
            
            if( ! changeDirectory( base, true ) )
            {
                deadConnection=true;
                throw new IOException( "NetClientFtp: unable to create directory "+base + " reply: " + ftp.getReplyString() );
            }
            
            ftp.changeWorkingDirectory( rootDir );
            
            if( ftp.rename( from, to ) == false )
            {
                deadConnection=true;
                throw new Exception( "NetClientFtp: failed to rename the file from "+from+" to "+to + " reply: " + ftp.getReplyString());
            }
        }
        else
        {
            deadConnection=true;
            throw new Exception( "ftp is not connected, must call make before rename" );
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
        // validate input
        if( rawFile == null || ! rawFile.exists() )
        {
            deadConnection=true;
            throw new Exception( "file to send does not exist" );
        }
        
        if( StringUtilities.isBlank( path ) )
        {
            deadConnection=true;
            throw new Exception( "send 'path' is blank" );
        }
        
        if( path.startsWith( "/" ) )
        {
            deadConnection=true;
            throw new Exception( "send 'path' must be relative: "+path );
        }
        
        // try to send the file
        if( ftp != null && ftp.isConnected() )
        {            
            try
            (FileInputStream fis = new FileInputStream( rawFile )) {
                
                String base = FileUtil.getBaseFromPath( path );
                
                if( ! StringUtilities.isBlank( base ) && ! changeDirectory( base, true ) )
                {
                    deadConnection=true;
                    throw new Exception( "could not change or create directory: :"+base );
                }
                
                String name = FileUtil.getName( path );
                if( ! ftp.storeFile( name, fis ) )
                {
                    deadConnection=true;
                    throw new Exception( "NetClientFtp: connected, but file send failed for "+path + " reply: " + ftp.getReplyString());
                }
            }
        }
        else
        {
            deadConnection=true;
            throw new Exception( "ftp is not connected, must call make before send" );
        }
    }
    
    /**
     * Changes the working directory to the given directory.                                    <BR>
     *                                                                                          <BR>
     * 
     * When FTPing from NY-> Syd each request takes 300ms due to network latency.
     * 
     * To change directory to /docs/20/20/1/1200 we were saying does /docs exist ? cd /docs, 
     * does 20 exist ? cd 20, does 20 exits ? cd 20, does 1 exits ? cd 1, 
     * does 1200 exits ? cd 1200 == 3,000ms
     * 
     * Now we say cd /docs/20/20/1/1200 == 300ms if that fails we try to cd to each directory 
     * in turn and if it fails we check if it exists and create if not so ~= 1,500ms worst case
     * @return boolean true if directory changed false otherwise
     * @param path the directory path
     * @param create should we create the directory if it doesn't exist
     */
    @Override
    public boolean changeDirectory( final String path, final boolean create )
    {
        boolean changed = false;
        
        if( ftp != null && ftp.isConnected() )
        {
            try
            {
                changed = ftp.changeWorkingDirectory( path );
                
                if( ! changed && create )
                {
                    String[] lst = path.split( "/" );
                    for( int i = 0; i < lst.length; i++ )
                    {
                        // skip empty tokens
                        if( lst[i].length() > 0 )
                        {
                            String dir = lst[i];
                            
                            /**
                             * For all but the last directory try to cd 
                             * to the directory before we check that it exits as most of the base
                             * directory will exits. we know that the last one ( unless we have 
                             * a race condition ) doesn't exits so don't try it before checking 
                             * for it's existence.
                             */
                            if( i < lst.length -1 )
                            {
                                changed = ftp.changeWorkingDirectory( dir );
                            
                                if( changed ) continue;
                            }
                            
                            FTPFile[] ls = ftp.listFiles();
                            
                            if( ! contains( ls, dir ) )
                            {
                                ftp.makeDirectory( dir );
                            }
                            
                            changed = ftp.changeWorkingDirectory( dir );
                            
                            if( ! changed ) break;
                        }
                    }
                }
            }
            catch( IOException e )
            {
                deadConnection=true;
                LOGGER.warn( "NetClientFtp: connected, but unable to change directory to "+path + " reply: " + ftp.getReplyString(),e );

                changed = false;
            }
        }
        else
        {
            deadConnection=true;
            LOGGER.warn( "NetClientFtp: unable to change directory, not connected" );
        }
        
        return changed;
    }
    
    /**
     * Checks if the given list of strings contains the search string
     *
     * @param list the list of strings to search in
     * @param search the string to search for
     * @return boolean true if the list contains the string at least once, false otherwise
     */
    public static boolean contains( final FTPFile[] list, final String search )
    {
        boolean found = false;
        
        if( list != null && search != null )
        {
            for( int i = 0; i < list.length && !found; i++ )
            {
                FTPFile file = list[i];
                if( file == null) continue;
                String name = file.getName();
                
                found = search.equals( name );
            }
        }
        
        return found;
    }
    
    /**
     * Gets the current working directory
     * @return String the current working directory
     * @throws Exception a serious problem
     */
    @Override
    public String getDirectory() throws Exception
    {
        String directory = null;
        
        if( ftp != null && ftp.isConnected() )
        {
            directory = ftp.printWorkingDirectory();
        }
        else
        {
            deadConnection=true;
            throw new Exception( "ftp is not connected, must call make before getDirectory" );
        }
        
        return directory;
    }
    
    /**
     * Gets the protocol prefix that this client implements
     * @return String the protocol type
     */
    @Override
    public String getType()
    {
        return PREFIX_FTP;
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
        
        if( ftp != null && ftp.isConnected() )
        {
            connectable = true;
        }
        else
        {
            try
            {
                make( url );
                connectable = true;
            }
            catch( Exception failed )
            {
                deadConnection=true;
                LOGGER.warn( StringUtilities.stripPasswordFromURL(url) +" is not connectable", failed );
            }
        }
        
        return connectable;
    }
    
    /**
     * Activates the client, also used by pooling
     */
    @Override
    public void activate()
    {
        if( ftp != null && ftp.isConnected() )
        {
            // clear the progress monitor
            monitor = null;
            
            try
            {
                if( !ftp.setFileType( FTP.BINARY_FILE_TYPE ) )
                {
                    deadConnection=true;
                    throw new Exception( "NetClientFtp: could not set binary transfer mode reply: " + ftp.getReplyString() );
                }
                
                ftp.enterLocalPassiveMode();
            }
            catch( Exception e )
            {
                deadConnection=true;
                // We don't want a stack trace because this is fairly normal
                String msg = "NetClientFtp: could not activate "+this + " reply: " + ftp.getReplyString() + ": " + e.toString();
                LOGGER.warn(  msg);
            }
        }
    }
    
    /**
     * Destroys the client, also used by pooling
     */
    @SuppressWarnings("empty-statement")
    @Override
    public void destroy()
    {
        if( ftp != null && ftp.isConnected() )
        {
            try
            {
                ftp.logout();
            }
            catch( IOException ignore )
            {
                // dont care
            }
            
            try
            {
                ftp.disconnect();
            }
            catch( IOException ignore )
            {
                // dont care
            }
        }
    }
    
    /**
     * Makes a new client, also used by pooling. Expects the url to be in the
     * following format: username:password@server/baseDir
     * @param url the client url
     * @throws Exception a serious problem
     */
    @Override
    public void make( final String url ) throws Exception
    {
        // make sure we are not already connected
        destroy();

        URLParser parser = new URLParser(url);
        
        String userName = parser.getUserName();
        String password = parser.getPassword();
        String server = parser.getHostName();
        
        String baseDir = parser.getURI();
        
        ftp = new FTPClient();
        //ftp.setDefaultTimeout( NetUtil.getMaxWaitTime());
        ftp.connect( server );

        if( !ftp.login( userName, password ) )
        {
            deadConnection=true;
            throw new Exception( "NetClientFtp: connected to server but unable to login for "+ StringUtilities.stripPasswordFromURL(url)  );
        }

        if( baseDir.length() > 0 )
        {
            if( !changeDirectory( baseDir, true ) )
            {
                deadConnection=true;
                throw new IOException( "could not change to base directory: "+baseDir  + " reply: " + ftp.getReplyString() );
            }
        }
        
        rootDir = ftp.printWorkingDirectory();
        
        if( !ftp.setFileType( FTP.BINARY_FILE_TYPE ) )
        {
            deadConnection=true;
            throw new Exception( "NetClientFtp: connected and logged in, unable to set binary transfer "+ StringUtilities.stripPasswordFromURL(url) + " reply: " + ftp.getReplyString() );
        }
        
        ftp.enterLocalPassiveMode();
    }
    
    /**
     * Validates a client, also used by pooling
     * @return boolean true if the client is valid, false otherwise
     */
    @Override
    public boolean validate()
    {
        if( deadConnection ) return false;
        
        boolean valid = false;
        
        if( ftp != null && ftp.isConnected() )
        {
            valid = true;
        }
        
        return valid;
    }
    
    /**
     * Sets the progress listener. The progress monitor will be cleared by a call to
     * activate.
     * @param monitor the progress listener
     * @return previous monitor
     */
    @Override @CheckReturnValue @Nullable
    public ProgressListener setProgressListener( final @Nullable ProgressListener monitor ) 
    {
        ProgressListener previous=this.monitor;
        this.monitor = monitor;
        return previous;
    }
    
    /**
     * CopyStreamListener interface
     * @param cse the copy stream event
     */
    @SuppressWarnings("empty-statement")
    @Override
    public void bytesTransferred( CopyStreamEvent cse )
    {
        ; // not implemented.
    }
    
    /**
     * CopyStreamListener interface. used by fetch when there is a ProgressListener
     * @param count the current bytes sent
     * @param total the total bytes transferred
     * @param size the size of the stream
     */
    @Override
    public void bytesTransferred( long total, int count, long size )
    {
        if( monitor != null )
        {
            monitor.update( (int) ((total / size) * 100) );
        }
    }
    
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.net.NetClientFtp");//#LOGGER-NOPMD

    /**
     * returns the names of files in a location
     * @return file names
     * @throws Exception serious problem
     */
    @Override
    public String[] retrieveFileList() throws Exception
    {
        String[] fileList;
        
        if( ftp != null && ftp.isConnected() )
        {            
            fileList = ftp.listNames();            
        }
        else
        {
            fileList = null;
        }
        return fileList;
    }
    
    /**
     * Set ftp connection time out
     * @param timeout Timeout in milliseconds
     * @throws Exception while setting the timeout 
     */
    public void setConnectionTimeOut( final int timeout ) throws Exception
    {
        if ( ftp != null  && ftp.isConnected() && timeout > 0) 
        {
            ftp.setSoTimeout(timeout);
        }
    }

    /**
     * Get the underlying connection object
     * @return the underlying connection object
     */
    @Override
    public Object getUnderlyingConnectionObject()
    {
        return ftp;
    }
    
    @Override
    public void make(String url, String SOCKSProxyURL, String keyPath, int serverPort) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
