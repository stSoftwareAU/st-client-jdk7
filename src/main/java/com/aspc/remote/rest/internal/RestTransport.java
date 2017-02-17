package com.aspc.remote.rest.internal;

import com.aspc.remote.rest.Method;
import java.io.File;
import java.net.URL;
import javax.annotation.Nonnull;

/**
 *
 *  @author      Lei Gao
 *  @since       19 March 2015 
 */
public abstract interface RestTransport
{
    String MIME_TYPE="mimeType";
    String CHECKSUM="checksum";
    String CACHE_CONTROL="cache-control";
    String STATUS="status";
//    String TRANSFER_ENCODING="transfer_encoding";
    String FILE_LIST="file_list";

    String getRootFolderName();
    
    RestCall makeRestCall(
        final @Nonnull Method method,    
        final @Nonnull URL url, 
        final ReSTAuthorizationInterface auth, 
        final String agent, 
        final File propertiesFile,
        final File body,
        final int timeout,
        final boolean disableGZIP,
        final Friend friend
    );
}
