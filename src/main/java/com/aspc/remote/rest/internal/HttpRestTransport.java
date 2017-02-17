package com.aspc.remote.rest.internal;

import com.aspc.remote.rest.Method;
import com.aspc.remote.util.misc.CLogger;
import java.io.File;
import java.net.URL;
import javax.annotation.Nonnegative;
import org.apache.commons.logging.Log;

/**
 *
 *  @author      Lei Gao
 *  @since       19 March 2015
 */
public class HttpRestTransport implements RestTransport
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.rest.internal.HttpRestTransport");//#LOGGER-NOPMD

    @Override
    public String getRootFolderName()
    {
        return "remote/";
    }

    @Override
    public RestCall makeRestCall(
        final Method method,
        final URL url,
        final ReSTAuthorizationInterface auth,
        final String agent,
        final File propertiesFile,
        final File body,
        final @Nonnegative int timeout,
        final boolean disableGZIP,
        final Friend friend) {
        assert timeout >= 0: "timeout must be non negative " + timeout;
        return new RestCallHTTP(method, url, auth, agent, propertiesFile, body, timeout, disableGZIP, friend);
    }
}
