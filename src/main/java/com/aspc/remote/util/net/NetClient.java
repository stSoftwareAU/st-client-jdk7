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
import java.io.File;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * NetClient represents a class for protocol transparent file operations.
 *
 * @author Nigel Leck
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 * @since       February 16, 2006
 */
public interface NetClient
{
    /**
     * Checks if the file specified by the given arguments physically exists
     * @return boolean true if it exists
     * @param path the path
     * @throws Exception a serious problem
     */
    @CheckReturnValue
    public boolean exists( final @Nonnull String path ) throws Exception;
    
    /**
     * Fetches the file specified by the given arguments
     * 
     * @param path the path of the file
     * @param target the target file
     * @throws Exception a serious problem
     */
    public void fetch( final @Nonnull String path, final @Nonnull File target ) throws Exception;
    
    /**
     * Removes the physical file specified by the given arguments
     * @param path the file to remove
     * @throws Exception a serious problem
     */
    public void remove( final @Nonnull String path ) throws Exception;
    
    /**
     * Renames the physical file specified by the given arguments
     * @param from the current path of the file
     * @param to the new path of the file
     * @throws Exception a serious problem
     */
    public void rename( final @Nonnull String from, final @Nonnull String to ) throws Exception;
    
    /**
     * Sends to the file specified by the given arguments to the given location
     * @param rawFile the file to send
     * @param path the path to send the file to
     * @throws Exception a serious problem
     */
    public void send( final @Nonnull File rawFile, final @Nonnull String path ) throws Exception;
    
    /**
     * Changes the working directory to the given directory
     * @return boolean true if directory changed false otherwise
     * @param path the path
     * @param create should we create the directory if missing ? 
     */
    @CheckReturnValue
    public boolean changeDirectory( final @Nonnull String path, final boolean create );
    
    
    /**
     * Gets the list of files in the path
     * @return String[] list of files
     * @throws Exception a serious problem
     */
    @CheckReturnValue @Nonnull
    public String[] retrieveFileList() throws Exception;
    
    
    /**
     * Gets the current working directory
     * @return String the current working directory
     * @throws Exception a serious problem
     */
    @CheckReturnValue @Nonnull
    public String getDirectory() throws Exception;
    
    /**
     * The protocol that this client is for
     * @return String the protocol type
     */
    @CheckReturnValue @Nonnull
    public String getType();
    
    /**
     * Checks that the client is able to connect
     * @param url the URL to try
     * @return boolean true if the client can connect, false otherwise
     */
    @CheckReturnValue
    public boolean canConnect( final @Nonnull String url );
    
    /**
     * Activates the client, also used by pooling
     */
    public void activate();
    
    /**
     * Destroys the client, also used by pooling
     */
    public void destroy();
    
    /**
     * Makes a new client, also used by pooling
     * @param url the client url
     * @throws Exception a serious problem
     */
    public void make( final @Nonnull String url ) throws Exception;
    
    /**
     * Validates a client, also used by pooling
     * @return boolean true if the client is valid, false otherwise
     */
    @CheckReturnValue
    public boolean validate();
    
    /**
     * Sets the progress listener to be used by the client for fetch operations.
     * A progress listener is not mandatory.
     * @param monitor the progress listener to use
     * @return the previous version
     */
    public @Nullable ProgressListener setProgressListener( final @Nullable ProgressListener monitor );

    /**
     * Get the underlying connection object
     * @return the underlying connection object
     */
    @CheckReturnValue @Nonnull
    public Object getUnderlyingConnectionObject();

    /**
     * FILE url
     */
    public static final String PREFIX_FILE="file://";

    /**
     * FTP url
     */
    public static final String PREFIX_FTP="ftp://";

    /**
     * SFTP url
     */
    public static final String PREFIX_SFTP="sftp://";
    
    
    /**
     * S3 url
     */
    public static final String PREFIX_S3="s3://";

}
