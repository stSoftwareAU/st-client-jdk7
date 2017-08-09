package com.aspc.remote.rest.internal;

import com.aspc.remote.rest.Method;
import com.aspc.remote.rest.Response;
import com.aspc.remote.util.misc.FileUtil;
import com.aspc.remote.util.misc.StringUtilities;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Callable;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 *  @author      Lei Gao
 *  @since       20 March 2015 
 */
public abstract class RestCall implements Callable<Response>
{
    public ReSTTask fjt;

    protected final File propertiesFile;
    protected final File body;
    protected final Method method;
    protected final URL url;
    protected final String agent;
    protected final ReSTAuthorizationInterface auth;
    public final int timeoutMS;
    protected final Friend friend;
    protected final boolean disableGZIP;
    
    /**
     * The ReST call. 
     * 
     * @param method the method
     * @param url the URL. 
     * @param auth any authorization.
     * @param agent the agent string to use.
     * @param propertiesFile the properties file
     * @param body the call body
     * @param timeoutMS the timeout in milliseconds ( if any) 
     * @param disableGZIP true to NOT accept GZIP encoding, default is false
     * @param friend the friend
     */
    public RestCall(
        final @Nonnull Method method, 
        final @Nonnull URL url, 
        final @Nullable ReSTAuthorizationInterface auth, 
        final @Nullable String agent,
        final @Nullable File propertiesFile,
        final @Nullable File body,
        final @Nonnegative int timeoutMS,
        final boolean disableGZIP,
        final @Nullable Friend friend
    )
    {
        if( method == null)
        {
            throw new IllegalArgumentException( "method is mandatory");
        }
        this.method=method;
        this.url = url;
        if( url == null)
        {
            throw new IllegalArgumentException( "url is mandatory");
        }
        this.auth=auth;
        this.agent=agent;
        this.propertiesFile = propertiesFile;
        this.body = body;
        this.timeoutMS=timeoutMS;
        this.disableGZIP = disableGZIP;
        this.friend=friend;
        if( friend == null)
        {
            throw new IllegalArgumentException( "ReST friend is mandatory");
        }
    }

    @CheckReturnValue @Nonnull
    public String getURL()
    {
        return url.toString();
    }
    
    @CheckReturnValue @Nonnull
    public Method getMethod()
    {
        return method;
    }
    
    public File getBody()
    {
        return body;
    }
    
    @Override @CheckReturnValue @Nonnull
    public String toString() {
        String tmpMethod="";
        if( method!=Method.GET)
        {
            tmpMethod=method + "->";
        }
        
        String tmpAuth="";
        if( auth!=null)
        {
            tmpAuth=", auth=" + auth ;
        }
        return "RestCall{" + tmpMethod + StringUtilities.stripPasswordFromURL(url.toString()) + tmpAuth + '}';
    }

    /**
     * Make the call 
     * @return the response 
     * @throws Exception a serious problem.
     */
    @Override @CheckReturnValue @Nonnull
    public Response call() throws Exception
    {
        Thread ct=Thread.currentThread();
        String tn=ct.getName();
        try{
            ct.setName(method.label + ": " + url);
            return doCall();
        }
        finally{
            ct.setName(tn);
            if( friend != null && propertiesFile != null) 
            {
                friend.cleanUp(propertiesFile, this);
            }
        }        
    }
    
    /**
     * Make the call 
     * @return the response 
     * @throws Exception a serious problem.
     */
    @CheckReturnValue @Nonnull 
    protected abstract Response doCall() throws Exception;
        
    protected void mantainFileHistory( final @Nonnull Properties p, final @Nonnull File cacheFile) throws IOException
    {
        String historyList[]=p.getProperty( RestTransport.FILE_LIST, "").split( ",");

        String lastFileName=null;
        for( String oldFile: historyList)
        {
            if( StringUtilities.isBlank(oldFile) || oldFile.equals(cacheFile.getName())) continue;

            File checkFile=new File( cacheFile.getParentFile(), oldFile);

            if( checkFile.exists())
            {
                if( lastFileName!=null)
                {
                    File removeFile=new File( cacheFile.getParentFile(), lastFileName);

                    FileUtil.deleteAll(removeFile);                                                 
                }
                lastFileName=checkFile.getName();
            }
        }
        StringBuilder historyBuilder=new StringBuilder();
        if( lastFileName != null)
        {
            historyBuilder.append(lastFileName);
            historyBuilder.append(",");
        }

        historyBuilder.append(cacheFile.getName());
        p.setProperty( RestTransport.FILE_LIST, historyBuilder.toString());
    }
}
