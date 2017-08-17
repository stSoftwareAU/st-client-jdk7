package com.aspc.remote.rest.internal;

import com.aspc.remote.rest.ContentType;
import com.aspc.remote.rest.DispositionType;
import com.aspc.remote.rest.Method;
import java.io.File;
import java.net.URL;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 *  @author      Lei Gao
 *  @since       19 March 2015 
 */
public abstract interface RestTransport
{
    String MIME_TYPE="mimeType";
    String RESULTS_SHA1="SHA1";
    String CACHE_CONTROL="cache-control";
    String IF_NONE_MATCH="If-None-Match";
    String ETAG="ETag";
    
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
        final Friend friend,
        final @Nullable ContentType contentType,
        final @Nullable DispositionType dispositionType
    );
}
