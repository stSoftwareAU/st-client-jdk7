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
import com.aspc.remote.util.misc.CProperties;
import com.aspc.remote.util.misc.FileUtil;
import com.aspc.remote.util.misc.StringUtilities;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jcraft.jsch.ProxySOCKS5;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;


/**
 * Implements NeClient for the SFTP protocol
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 * @author      luke
 * @since       February 23, 2006
 */
public class NetClientSftp implements NetClient, SftpProgressMonitor
{
    private ChannelSftp sftp;
    private Session session;
    private String rootDir;
    private ProgressListener monitor;
    private long progress;
    private boolean deadConnection;
    private static final int DEFAULT_CONNECTION_TIMEOUT_MS=120000;
    private static final int CONNECTION_TIMEOUT_MS;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.net.NetClientSftp");//#LOGGER-NOPMD
    private static final String SFTP_CONNECTION_TIMEOUT_MS = "SFTP_CONNECTION_TIMEOUT_MS";

    static
    {
        String timeout;
        timeout = CProperties.getProperty(SFTP_CONNECTION_TIMEOUT_MS);
        int intTimeout = DEFAULT_CONNECTION_TIMEOUT_MS;
        if(StringUtilities.notBlank(timeout))
        {
            try
            {
                intTimeout = Integer.parseInt(timeout);
            }
            catch(NumberFormatException e)
            {
                LOGGER.warn("Invalid SFTP_CONNECTION_TIMEOUT_MS value " + timeout + ", default CONNECTION_TIMEOUT_MS(" + DEFAULT_CONNECTION_TIMEOUT_MS + ") is used", e);
            }
        }
        CONNECTION_TIMEOUT_MS = intTimeout;
    }
    
    /**
     * Checks if the file specified by the given arguments physically exists.
     * @param path the file to check
     * @return boolean true if it exists
     * @throws Exception in case of error
     */
    @Override
    public boolean exists( final String path ) throws Exception
    {
        if( sftp == null || sftp.isConnected()==false )
        {
            deadConnection=true;
            throw new Exception( "not connected");
        }
        
        boolean found = false;

        try
        {
            String[] tokens;
            if( path.contains("/")  )
            {
                tokens = path.split( "/" );
            }
            else
            {
                tokens = new String[]{path};
            }
            
            // use a temporary path, do not change directories!
            String tmpPath = "";
            
            for (String dir : tokens) {
                // dodge empty tokens
                if( dir.length() == 0 )
                {
                    continue;
                }
                
                // not before empty token check, and not after name check!
                found = false;
                List<ChannelSftp.LsEntry> ls;
                if( StringUtilities.isBlank(tmpPath))
                {
                    ls = sftp.ls( "." );
                }
                else
                {
                    ls = sftp.ls( tmpPath );
                }

                Iterator it = ls.iterator();
                while( it.hasNext() )
                {
                    String entry = ((LsEntry)it.next()).getFilename();
                    if( entry.equals( dir ) )
                    {
                        found = true;
                        break;
                    }
                }
                
                if( found )
                {
                    tmpPath += dir+"/";
                }
                else
                {
                    break;
                }
            }        
        }
        catch( Throwable t)
        {
            deadConnection=true;
            LOGGER.warn( "problem", t);
            throw CLogger.rethrowException(t);
        }
        return found;
    }
    
    /**
     * Fetches the file specified by the given arguments
     * @param path the path of the file
     * @param target the target file
     * @throws Exception in case of error
     */
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    @Override
    public void fetch( final String path, final File target ) throws Exception
    {
        // validate the input
        if( StringUtilities.isBlank( path ) )
        {
            throw new Exception( "blank 'path' in call to fetch" );
        }
        
        if( target == null )
        {
            throw new Exception( "null 'target' file in call to fetch" );
        }
        
        if( path.startsWith( "/" ) )
        {
            throw new Exception( "'path' must be relative: "+path );
        }
        
        // now try to fetch
        if( sftp != null && sftp.isConnected() )
        {            
            try
            (FileOutputStream fos = new FileOutputStream( target )) {
                
                if( monitor != null )
                {
                    sftp.get( path, fos, this );
                }
                else
                {
                    sftp.get( path, fos );
                }
            }
            catch( Exception e )
            {
                deadConnection=true;
                LOGGER.info( "sftp fetch failed for "+path, e );
                String msg = "path: "+path
                        +" pwd: "+sftp.pwd();
                throw new Exception( msg, e);
            }
        }
        else
        {
            deadConnection=true;
            throw new Exception( "sftp is not connected, must call make before fetch" );
        }
    }
    
    /**
     * Removes the physical file specified by the given arguments.
     * @param path the file to remove
     * @throws Exception in case of error
     */
    @Override
    public void remove( final String path ) throws Exception
    {
        if( sftp != null && sftp.isConnected() )
        {
            try
            {
                sftp.rm( path );
            }
            catch( Exception e )
            {
                LOGGER.warn( "could not remove " + path, e);
            }
        }
        else
        {
            deadConnection=true;
            throw new Exception( "sftp is not connected, must call make before remove" );
        }
    }
    
    /**
     * Renames the physical file specified by the given arguments
     * @param from the current path of the file
     * @param to the new path of the file
     * @throws Exception in case of error
     */
    @Override
    public void rename( final String from, final String to ) throws Exception
    {
        // first validate the input
        if( StringUtilities.isBlank( from ) )
        {
            throw new Exception( "rename 'from' path must not be blank" );
        }
        
        if( StringUtilities.isBlank( to ) )
        {
            throw new Exception( "rename 'to' path must not be blank" );
        }
        
        if( from.startsWith( "/" ) )
        {
            throw new Exception( "rename 'from' path must be relative: "+from );
        }
        
        if( to.startsWith( "/" ) )
        {
            throw new Exception( "rename 'to' path must be relative: "+to );
        }
        
        // now attempt to rename
        if( sftp != null && sftp.isConnected() )
        {            
            String base = FileUtil.getBaseFromPath( to );
            
            if( ! changeDirectory( base, true ) )
            {
                deadConnection=true;
                throw new IOException( "could not create directories for: "+to );
            }
            
            try
            {
                sftp.cd( rootDir );
                
                sftp.rename( from, to );
            }
            catch( Exception e )
            {
                deadConnection=true;
                LOGGER.info( "NetClientSftp: failed to rename the file from "+from+" to "+to, e );
                throw e;
            }
        }
        else
        {
            deadConnection=true;
            throw new Exception( "sftp is not connected, must call make before rename" );
        }
    }
    
    /**
     * Sends the file specified by the given arguments to the given location
     * @param rawFile the file to send
     * @param path the path to send the file to
     * @throws Exception in case of error
     */
    @Override
    public void send( final @Nonnull File rawFile, final @Nonnull String path ) throws Exception
    {
        if( StringUtilities.isBlank( path ) )
        {
            throw new Exception( "send path cannot be blank" );
        }
        
        if( path.startsWith( "/" ) )
        {
            throw new Exception( "path must be relative: "+path );
        }
        
        if( ! rawFile.exists() )
        {
            throw new Exception( "cannot send a non-existant file: "+rawFile );
        }
        
        if( sftp != null && sftp.isConnected() )
        {            
            String base = FileUtil.getBaseFromPath( path );
            
            base = base.replace( "//", "/" );
            base = base.replace( "\\", "/" );
            
            try
            (FileInputStream fis = new FileInputStream( rawFile )) {
                
                if( ! StringUtilities.isBlank( base ) && ! changeDirectory( base, true ) )
                {
                    deadConnection=true;
                    throw new IOException( "could not create directory: "+base );
                }
                
                String name = FileUtil.getName( path );
                
                sftp.put( fis, name );
            }
            catch( Exception e )
            {
                deadConnection=true;
                LOGGER.info( "NetClientSftp: connected, but file send failed for "+path+" base: "+base, e );
                throw e;
            }
        }
        else
        {
            deadConnection=true;
            throw new Exception( "sftp is not connected, must call make before send" );
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
     * @param create should we create the directory if missing
     * @param path the directory path to change to
     */
    @Override @CheckReturnValue
    public boolean changeDirectory( final String path, final boolean create )
    {
        boolean changed = false;
        
        if( sftp != null && sftp.isConnected() )
        {
            try
            {
                String  fullPath,
                        basePath = null;
                
                String tempPath = path;
                if( ! StringUtilities.isBlank( rootDir ) && tempPath.startsWith( rootDir ) )
                {
                    tempPath = tempPath.substring( rootDir.length());
                    fullPath = rootDir + "/" + tempPath;
                    basePath = rootDir;
                }
                else if( path.startsWith("/") )
                {
                    basePath=rootDir;
                    fullPath = rootDir + path;
                }
                else
                {
                    fullPath = path;
                }

                fullPath = fullPath.replace( "//", "/");
                
                /** 
                 * Try to cd in one go first. 
                 */
                try
                {
                    sftp.cd( fullPath );
                    
                    changed = true;
                }
                catch( SftpException e )
                {
                    // We couldn't cd so if we are going to create the just cd to the base directory
                    if( create )
                    {
                        if( basePath != null)
                        {
                            sftp.cd( basePath);
                        }                        
                    }
                    else
                    {
                        throw e;
                    }
                }

                if( changed == false)
                {
                    String[] tokens = tempPath.split("/");
                    for (String token : tokens) {
                        if (token.length() > 0) {
                            try {
                                sftp.cd(token);
                            } catch (Exception e) {
                                if (create) {
                                    sftp.mkdir(token);
                                    sftp.cd(token);
                                } else {
                                    throw e;
                                }
                            }
                        }
                    }

                    changed = true;
                }
            }
            catch( Exception e )
            {
                deadConnection=true;
                LOGGER.warn( "NetClientSftp: connected, but unable to change directory to "+path, e );
            }
        }
        else
        {
            LOGGER.warn( "NetClientSftp: unable to change directory, not connected" );
        }
        
        return changed;
    }
    
    /**
     * Gets the current working directory.
     * @return String the current working directory
     * @throws Exception in case of error
     */
    @Override @CheckReturnValue @Nonnull 
    public String getDirectory() throws Exception
    {
        String directory = null;
        
        if( sftp != null && sftp.isConnected() )
        {
            directory = sftp.pwd();
        }
        else
        {
            throw new Exception( "sftp is not connected, must call make before getDirectory" );
        }
        
        return directory;
    }
    
    /**
     * Gets the protocol prefix that this client implements
     * @return String the protocol type
     */
    @Override @CheckReturnValue @Nonnull 
    public String getType()
    {
        return PREFIX_SFTP;
    }
    
    /**
     * Checks that the client is able to connect and login.
     * expects the url to be in the format username:password@server
     *
     * @param url the url to check
     * @return boolean true if the client can connect, false otherwise
     */
    @Override @CheckReturnValue
    public boolean canConnect( final String url )
    {
        boolean connectable = false;
        
        if( sftp != null && sftp.isConnected() )
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
                LOGGER.warn( StringUtilities.stripPasswordFromURL(url)+" is not connectable", failed );
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
        if( sftp != null && sftp.isConnected() )
        {
            // clear the progress listener
            monitor = null;
        }
    }
    
    /**
     * Destroys the client, also used by pooling
     */
    @Override
    public void destroy()
    {
        if( sftp !=null)
        {
            try{
                sftp.disconnect();
            }
            catch( Exception ignore )
            {
                // don't care
            }  
            sftp =null;
        }
        if( session != null )
        {            
            try
            {
                session.disconnect();
            }
            catch( Exception ignore )
            {
                // don't care
            }
            session=null;
        }
    }
    
    /**
     * Makes a new client, also used by pooling. Expects the url to be in the
     * following format: username:password@server
     * 
     * @param url the client url
     * @throws Exception in case of error
     */
    @Override
    public void make( final @Nonnull String url ) throws Exception
    {
        make(url, null);
    }
    /**
     * Makes a new client, also used by pooling. Expects the url to be in the
     * following format: username:password@server
     * 
     * @param url the client url
     * @param SOCKSProxyURL the SOCKS proxy url     *
     * @throws Exception in case of error
     */
    public void make( final @Nonnull String url, final @Nullable String SOCKSProxyURL ) throws Exception
    {
        // make sure we are not already connected
        destroy();
                      
        URLParser parser = new URLParser(url);

        String userName = parser.getUserName();
        String password = parser.getPassword();
        if( NetUtil.KNOWN_INVALID_PASSWORD.equals(password))
        {
            throw new Exception( "Known invalid password for: " + url);
        }
        String server = parser.getHostName();

        String baseDir = parser.getURI();
        
        JSch j = new JSch();
        
        Properties conf = new Properties();
        conf.put("StrictHostKeyChecking", "no");
//        conf.put("ConnectTimeout", "120");
        session = j.getSession(userName, server);
        session.setPassword(password);
        session.setConfig(conf);
        if (!StringUtilities.isBlank(SOCKSProxyURL))
        {
            assert SOCKSProxyURL!=null;
            String proxy_host=SOCKSProxyURL.substring(0, SOCKSProxyURL.indexOf(':'));
            int proxy_port=Integer.parseInt(SOCKSProxyURL.substring(SOCKSProxyURL.indexOf(':')+1));              
            session.setProxy(new ProxySOCKS5(proxy_host, proxy_port));        
        }
        session.connect(CONNECTION_TIMEOUT_MS);
        sftp = (ChannelSftp)session.openChannel("sftp");
        sftp.connect(CONNECTION_TIMEOUT_MS);
        
        if( baseDir.length() > 0 )
        {
            if( !changeDirectory( baseDir, true ) )
            {
                deadConnection=true;
                String msg;
                msg = "failed to change to base "+baseDir+" on server "+server;
                throw new IOException( msg );
            }
        }
        
        rootDir = sftp.pwd();
    }
    
    public void make( 
        final @Nonnull String url, 
        final @Nonnull byte[] prvkey, 
        final @Nullable byte[] pubkey, 
        final @Nullable byte[]  passPhrase,
        final @Nonnull String code,
        final @Nonnegative int port
    ) throws Exception
    {
        destroy();
                      
        URLParser parser = new URLParser(url);
        String userName = parser.getUserName();
        String server = parser.getHostName();

        String baseDir = parser.getURI();
        
        JSch j = new JSch();
        
        j.addIdentity(code, prvkey, pubkey, passPhrase);

        Properties conf = new Properties();
        conf.put("StrictHostKeyChecking", "no");        
        
        session = j.getSession(userName, server,port);
        session.setConfig(conf);
        session.connect(CONNECTION_TIMEOUT_MS);
        sftp = (ChannelSftp)session.openChannel("sftp");
        sftp.connect(CONNECTION_TIMEOUT_MS);
        
        if( baseDir.length() > 0 )
        {
            if( !changeDirectory( baseDir, true ) )
            {
                deadConnection=true;
                String msg;
                msg = "failed to change to base "+baseDir+" on server "+server;
                throw new IOException( msg );
            }
        }
        
        rootDir = sftp.pwd();
    }
    
    /**
     * Validates a client, also used by pooling
     * @return boolean true if the client is valid, false otherwise
     */
    @Override @CheckReturnValue
    public boolean validate()
    {
        if( deadConnection) return false;
        
        boolean valid = false;
        if( session!=null)
        {
            if( session.isConnected())
            {
                try{
                    session.sendKeepAliveMsg();
                    valid = true;
                }
                catch( Exception e)
                {
                    deadConnection=true;
                    LOGGER.info( "could not send keep alive", e);
                }
            }
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
     * Clears the progress state for SftpProgressMonitor
     */
    @Override
    public void end()
    {
        progress = 0;
    }
    
    /**
     * Updates the progress for SftpProgressMonitor
     * @param c the amount to update by
     * @return true
     */
    @Override
    public boolean count(long c) // need var for the subtotal
    {
        if( monitor != null )
        {
            monitor.update( (int)((c / progress)*100) );
        }
        return true;
    }
    
    /**
     * Prepares the SftpProgressMonitor
     * @param type the op type, GET or PUT
     * @param src the source path
     * @param dst the destination path
     * @param max the expected total
     */
    @Override
    public void init(final int type, final String src, final String dst, final long max)
    {
        progress = max;
    }
    
    /**
     * Return the names of files in a url
     * @throws Exception serious problem
     * @return file names
     */
    @Override @CheckReturnValue @Nullable
    public String[] retrieveFileList() throws Exception
    {
        String[] fileList;
        
        if( sftp != null && sftp.isConnected() )
        {            
            List<ChannelSftp.LsEntry> files = sftp.ls("*");
            List<String> list = new ArrayList<>();
            for (ChannelSftp.LsEntry file : files) 
            {
                list.add(file.getFilename());
            }
            fileList = list.toArray(new String[list.size()]);
        }
        else
        {
            fileList = null;
        }
        return fileList;       
    }

    /**
     * Get the underlying connection object
     * @return the underlying connection object
     */
    @Override  @CheckReturnValue @Nonnull
    public Object getUnderlyingConnectionObject()
    {
        return sftp;
    }
}
