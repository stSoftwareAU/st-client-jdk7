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
import java.io.File;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;

/**
 *  NetUtil
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       August 9, 1999, 5:21 PM
 */
public final class NetUtil
{
    /**
     * The maximum number of connections per user/server
     */
    public static final String NETCLIENT_MAX_ACTIVE="NETCLIENT_MAX_ACTIVE";

    /** Time in milliseconds to block for */
    public static final String NETCLIENT_MAX_WAIT_MS="NETCLIENT_MAX_WAIT_MS";

    /** Time in milliseconds to check idle connections */
    public static final String NETCLIENT_TEST_IDLE_MS="NETCLIENT_TEST_IDLE";

    /** the number of re-attempts on error */
    public static final AtomicInteger RETRY_ATTEMPTS=new AtomicInteger(2);
    
    public static final String KNOWN_INVALID_PASSWORD = "********";
    
    /** The default cache period to use when fetching files */
    public static final ThreadLocal<String>CACHE_PERIOD=new ThreadLocal<String>(){
        @Override
        protected String initialValue() {
            return ""; 
        }        
    };
    
    /**
     * Local host name
     */
    public static final String LOCAL_HOST_NAME;

    public static final ThreadLocal<Boolean> REPAIR_MODE= new ThreadLocal<Boolean>(){
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE; 
        }            
    };
    
    /**
     * Send the raw file to the locations listed.
     * @param rawFile The raw  file to send
     * @param URLs The list of URLs
     * @param path The path to store the file.
     * @throws Exception A serious problem
     */
    public static void sendData( final @Nonnull File rawFile, final @Nonnull String URLs, final @Nonnull String path) throws Exception
    {
        StringTokenizer st= new StringTokenizer( URLs, ",");

        boolean success = false;
        Exception lastE=null;
        while( st.hasMoreTokens())
        {
            String url = st.nextToken().trim();
            NetClient client = null;
            try
            {
                client = borrowClient( url);
                client.send( rawFile, path);
                success = true;
            }
            catch( Exception e)
            {
                String msg="Could not send " + rawFile + "->" + StringUtilities.stripPasswordFromURL(url) + "/" + path;
                LOGGER.warn(msg, e);

                lastE=new Exception( msg, e);
            }
            finally
            {
                returnClient( client);
            }
        }
        
        if( success==false )
        {
            throw lastE;
        }
    }

    /**
     * remove the data from the locations specified.
     *
     * @param URLs The URLs for the file to be removed from.
     * @param path The path of the file to be removed.
     * @throws Exception A serious problem
     */
    public static void removeData( final @Nonnull String URLs, final @Nonnull String path) throws Exception
    {
        StringTokenizer st= new StringTokenizer( URLs, ",");

        Exception lastE=null;
        while( st.hasMoreTokens())
        {
            String url = st.nextToken().trim();
            NetClient client = null;
            try
            {
                client = borrowClient( url);
                client.remove( path);
            }
            catch( Exception e)
            {
                lastE=e;
                LOGGER.warn("Could not remove " + StringUtilities.stripPasswordFromURL(url) + "/" + path, e);
            }
            finally
            {
                returnClient( client);
            }
        }
        
        if( lastE != null)
        {
            throw lastE;
        }
    }

    /**
     * fetch the file from the following location
     * @return The file
     * @param checksum the check sum
     * @param URLs The locations to search
     * @param path The file path
     * @param haMode If in HA mode then if a file is missing we will sync between locations.
     * @throws Exception A serious problem
     */
    @CheckReturnValue @Nonnull
    public static File fetchData(
        final @Nonnull String URLs,
        final @Nonnull String path,
        final boolean haMode,
        final @Nullable String checksum
    ) throws Exception
    {
        File cacheFile = FileUtil.makeCacheFile( URLs, path );

        if( cacheFile.exists() )
        {
            if( StringUtilities.isBlank( checksum)== false )
            {
                if( ! FileUtil.isValid( cacheFile, checksum, -1 ) )
                {
                    LOGGER.warn( "Refetch of '" + path + "' from " + StringUtilities.stripPasswordFromURLs(URLs) + " as checksum '" + checksum + "' didn't match");
                    retrieveData( URLs, path, cacheFile, haMode, checksum);
                }
            }
        }
        else
        {
            retrieveData( URLs, path, cacheFile, haMode, checksum);
        }
        assert cacheFile!=null;
        return cacheFile;
    }

   /**
     * Retrieve a file from a list of locations.
     *
     * No checks are done to determine if file already exists at the destination location
     *
     * @param targetFile the target file to receive to
     * @param checksum the checksum
     * @param URLs The locations to search
     * @param path The file path
     * @param haMode If in HA ( High Availability) mode then we ensure that volumes that have multiple urls
     *               have the file on all urls if it is found on one and not the others.  This should not
     *               happen as files are placed at all locations in a volume when the file is uploaded.
     * @throws Exception A serious problem
     */
    public static void retrieveData( final @Nonnull String URLs, final @Nonnull String path, final @Nonnull File targetFile, final boolean haMode, final @Nullable String checksum) throws Exception
    {
        File tmpFile = File.createTempFile( targetFile.getName(), ".tmp", targetFile.getParentFile() );
        StringTokenizer fileStores= new StringTokenizer( URLs, "|");
        while( fileStores.hasMoreTokens())
        {
            String volume = fileStores.nextToken().trim();

            LOGGER.info( "Trying... " + StringUtilities.stripPasswordFromURL( volume));

            try
            {
                String urlList[]=volume.split(",");
                
                ArrayList<String> list = null;
                Exception lastE=null;
                for( String url: urlList)
                {                    
                    NetClient client = null;
                    ProgressListener previousListener=null;
                    try
                    {
                        client = borrowClient( url);

                        if( monitor != null )
                        {
                            previousListener=client.setProgressListener( monitor );
                        }

                        client.fetch(path, tmpFile);

                        // verify the file that we recieved.
                        if( StringUtilities.isBlank( checksum ) == false )
                        {
                            if( FileUtil.isValid( tmpFile, checksum, -1 ) == false )
                            {
                                tmpFile.delete();
                                throw new Exception( "checksum failed on '" + path + "' from " + StringUtilities.stripPasswordFromURL(url) + " as checksum '" + checksum + "' didn't match");
                            }
                        }

                        if( list != null)
                        {
                            for (String temp : list) 
                            {
                                try
                                {
                                    sendData( tmpFile, temp, path);
                                }
                                catch( Exception e)
                                {
                                    LOGGER.warn( "HA re-sync of file " + tmpFile + " -> " + StringUtilities.stripPasswordFromURL(temp) , e);
                                }
                            }
                        }

                        if( REPAIR_MODE.get())
                        {
                            for( String tmpURL: urlList)
                            {           
                                if( tmpURL.equals(url)) continue;
                                NetClient tmpClient = null;
                                File checkFile = File.createTempFile( targetFile.getName(), ".check", targetFile.getParentFile() );
                                try
                                {
                                    tmpClient = borrowClient( tmpURL);                                    
                                    
                                    tmpClient.fetch(path, checkFile);

                                    // verify the file that we recieved.
                                    if( StringUtilities.isBlank( checksum ) == false )
                                    {
                                        if( FileUtil.isValid( checkFile, checksum, -1 ) == false )
                                        {
                                            checkFile.delete();
                                            sendData( tmpFile, tmpURL, path);
                                        }
                                    }
                                }
                                catch( Exception e)
                                {
                                    sendData( tmpFile, tmpURL, path);
                                }
                                finally
                                {
                                    returnClient(tmpClient);
                                    checkFile.delete();
                                }
                            }
                        }                                
                        FileUtil.replaceTargetWithTempFile(tmpFile, targetFile);

                        return;
                    }
                    catch( Exception e)
                    {
                        lastE=e;
                        
                        LOGGER.warn( "failed to fetch file from HA alternative " + StringUtilities.stripPasswordFromURL(url), e);
                        if( haMode)
                        {
                            if( list == null)
                            {
                                list = new ArrayList();
                            }

                            list.add( url);
                        }
                    }
                    finally
                    {
                        if( client != null)
                        {
                            client.setProgressListener( previousListener );
                            returnClient(client);
                        }
                    }
                }
                
                if( lastE != null)
                {
                    throw lastE;
                }
            }
            catch( Exception e)
            {
                if( fileStores.hasMoreTokens() == false)
                {
                    throw e;
                }
                LOGGER.warn( "failed to fetch file from URL " + StringUtilities.stripPasswordFromURL(volume), e);
            }
        }
        throw new Exception( "No valid URLs to fetch file");
    }

    /**
     * rename the original file to the new name/location
     *
     * @param URLs the locations to rename the file at
     * @param originalPath the path of the file to be renamed
     * @param newPath the path to rename to
     * @throws Exception a serious problem
     */
    public static void renameData(final @Nonnull String URLs, final @Nonnull String originalPath, final @Nonnull String newPath ) throws Exception
    {
        StringTokenizer st = new StringTokenizer( URLs, "," );

        boolean success = false;

        while( st.hasMoreTokens() ) // for each URL
        {
            String url = st.nextToken().trim(); // remove leading and trailing whitespace
            NetClient client = null;
            try
            {
                client = borrowClient( url);
                client.rename(originalPath, newPath);

                success = true; // rename operation success.
            }
            catch ( Exception e )
            {
                LOGGER.warn("could not renameData", e);
                if( client != null)
                {
                    boolean newExists = client.exists( newPath );
                    LOGGER.info( "destination file exists: "+newExists );
                    boolean originalExists = client.exists( originalPath );
                    LOGGER.info( "source file exists: "+originalExists );
                    if( newExists && originalExists )
                    {
                        boolean renamedNewPath = false;
                        String postfix = "."+System.currentTimeMillis()+".rename.tmp";
                        try
                        {
                            // rename newPath to newPath.something
                            LOGGER.info( "moving the destination file" );
                            client.rename( newPath, newPath+postfix );
                            // set flag
                            renamedNewPath = true;
                            // rename originalPath to newPath
                            LOGGER.info( "trying the rename again" );
                            client.rename( originalPath, newPath );
                            // set success to true
                            success = true;
                            // clean newPath.something
                            LOGGER.info( "cleanup "+newPath+postfix );
                            client.remove( newPath+postfix );
                        }
                        catch( Exception ex )
                        {
                            if( renamedNewPath && ! success )
                            {
                                // then put it back how it was
                                LOGGER.info( "second rename attempt failed" );
                                LOGGER.info( "restoring destination file" );
                                client.rename( newPath+postfix, newPath );
                            }
                        }
                    }
                }

                if( st.hasMoreTokens() == false && success == false )
                {
                    LOGGER.info("originalPath: " + originalPath + ", newPath: " + newPath );
                    throw e;
                }
            }
            finally
            {
                returnClient(client);
            }
        }
    }

    /**
     * Checks if the file denoted by the given uri physically exists
     * @return boolean true if the file exists
     * @param path the file path
     * @param uri the full uri to the file
     * @throws Exception failure to perform check
     */
    @CheckReturnValue
    public static boolean exists( final @Nonnull String uri, final @Nonnull String path ) throws Exception
    {
        boolean found = false;

        NetClient client = null;

        try
        {
            client = borrowClient( uri);
            found = client.exists(path);
        }
        finally
        {
            returnClient(client);
        }
        return found;
    }


    /**
     * Checks that we can connect to the server specified in the uri
     *
     * @param uri the full uri to the file
     * @return boolean true if the server can be reached
     * @throws Exception failure to perform check
     */
    @CheckReturnValue
    public static boolean canConnect( final @Nonnull String uri) throws Exception
    {

        boolean connected = false;

        NetClient client = null;

        try
        {
            client = borrowClient( uri);
            connected = client.canConnect(uri);
        }
        finally
        {
            returnClient(client);
        }
        return connected;
    }

    /**
     * Borrow a Net client
     * @return The Net Client
     * @param url The URL to connect to
     * @throws Exception could not create connection
     */
    @CheckReturnValue
    public static NetClient borrowClient( final @Nonnull String url) throws Exception
    {
        int maxAttempts=RETRY_ATTEMPTS.get();
        for( int attempt=0; true; attempt++)
        {
            try
            {
                NetClient client;

                client = (NetClient)CLIENT_POOL.borrowObject( url);

                return client;
            }
            catch( NoRouteToHostException noRoute)
            {
                LOGGER.warn( "The host is is unknown " + StringUtilities.stripPasswordFromURL(url), noRoute);
                throw noRoute;
            }
            catch( Exception e)
            {
                if( attempt< maxAttempts)
                {
                    LOGGER.warn( (attempt + 1) + " of " + maxAttempts + " retrying " + StringUtilities.stripPasswordFromURL(url), e);
                }
                else
                {
                    throw e;
                }
            }
        }
    }
    
    /**
     * The number of clients that are currently borrowed. 
     * @return The count of borrowed clients ( checked out but not back in yet)
     */
    @CheckReturnValue
    public static int borrowedClientCount()
    {
        return CLIENT_POOL.borrowedObjectsCount();
    }
    
    /**
     * number of clients in the pool
     * @return int number
     * @throws Exception doesn't support exception
     */
    @CheckReturnValue
    public static int clientPoolSize() throws Exception
    {
        return CLIENT_POOL.size();
    }

    /**
     * return the Net client to the pool
     * @param client The borrowed client
     * @throws Exception client not borrowed.
     */
    public static void returnClient( final @Nullable NetClient client) throws Exception
    {
        if( client == null)
        {
            return;
        }

        CLIENT_POOL.returnObject( client);
    }

    /**
     *
     * @return the max wait time.
     */
    @CheckReturnValue
    public static int getMaxWaitTime()
    {
        int clientMaxWait = 120 * 1000;

        try
        {
            String temp = CProperties.getProperty(NETCLIENT_MAX_WAIT_MS);
            if( StringUtilities.isBlank(temp) == false)
            {
                clientMaxWait = Integer.parseInt(temp);
            }
        }
        catch( NumberFormatException nf)
        {
            LOGGER.warn( "Couldn't set " + NETCLIENT_MAX_WAIT_MS, nf);
        }

        return clientMaxWait;
    }

    /**
     * Invalidate this Net client
     * @param client The borrowed Net client
     * @throws Exception A serious problem
     */
    public static void invalidateClient( final @Nullable NetClient client) throws Exception
    {
        CLIENT_POOL.invalidateObject( client);
    }

    /**
     * set the progress listener
     * @param listener the listener
     */
    public static void setProgressListener( final @Nullable ProgressListener listener )
    {
        monitor = listener;
    }

    /**
     * Retrieve a list of file names from a url passed
     * @param url the url
     * @throws Exception A serious problem
     * @return the file list
     */
    @CheckReturnValue @Nonnull
    public static String[] retrieveFileList( final @Nonnull String url) throws Exception
    {
        String fileNames[];

        NetClient client = null;
        try
        {
            client = borrowClient( url);
            fileNames = client.retrieveFileList();
        }
        finally
        {
            returnClient( client );
        }

        return fileNames;

    }

    private NetUtil()
    {
    }

    private static ProgressListener monitor = null;//MT CHECKED

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.net.NetUtil");//#LOGGER-NOPMD

    /**
     * Pool of NetClient connections
     */
    private static final NetClientPool CLIENT_POOL;

    static
    {
        CLIENT_POOL= new com.aspc.remote.util.net.internal.ServerClientPoolImpl();//#CLEINT_POOL
        String temp;
        try
        {
            temp = InetAddress.getLocalHost().getHostName();
        }
        catch( UnknownHostException u)
        {
            temp="unkwown";
        }

        LOCAL_HOST_NAME=temp;
    }
}
