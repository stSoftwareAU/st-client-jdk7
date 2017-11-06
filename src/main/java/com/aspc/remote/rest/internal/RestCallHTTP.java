package com.aspc.remote.rest.internal;

import com.aspc.remote.rest.ContentType;
import com.aspc.remote.rest.DispositionType;
import com.aspc.remote.rest.Method;
import com.aspc.remote.rest.Response;
import com.aspc.remote.rest.Status;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.FileUtil;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.net.NetUrl;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.util.Properties;
import java.util.Random;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLProtocolException;
import org.apache.commons.logging.Log;

/**
 * HTTP ReST call 
 * 
 * @author      Lei Gao
 * @since       20 March 2015
 */
public class RestCallHTTP extends RestCall
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.rest.internal.RestCallHTTP");//#LOGGER-NOPMD
    private static final String LINE_FEED = "\r\n";
    
    private static final String TWO_HYPHENS = "--";
    
    /**
     * The ReST call. 
     * 
     * @param method the method
     * @param url the URL. 
     * @param auth any authorization.
     * @param agent the agent string to use.
     * @param propertiesFile the properties file
     * @param body the file contains request body
     * @param timeout the timeout in milliseconds ( if any) 
     * @param disableGZIP true to NOT accept GZIP encoding, default is false
     * @param friend the friend
     * @param contentType the content type
     * @param dispositionType the disposition type
     */    
    public RestCallHTTP(
        final @Nonnull Method method,             
        final URL url, 
        final ReSTAuthorizationInterface auth, 
        final String agent, 
        final File propertiesFile,
        final File body,
        final int timeout,
        final boolean disableGZIP,
        final Friend friend,
        final @Nullable ContentType contentType,
        final @Nullable DispositionType dispositionType
    ) {
        super(method, url, auth, agent, propertiesFile, body, timeout, disableGZIP, friend,contentType,dispositionType);
    }

    @Override @CheckReturnValue @Nonnull
    protected Response doCall() throws Exception
    {
        if( auth instanceof ReSTCallInterface)
        {
            return ((ReSTCallInterface)auth).doCall(this);
        }
        else
        {
            if( propertiesFile == null)
            {
                return nonCachedCall();
            }
            else
            {
                return cachedCall();
            }
        }
    }
    
    @CheckReturnValue @Nonnull
    private Response cachedCall() throws Exception 
    {
        InputStream in=null;

        OutputStream out=null;
        File tmpFile;
        File tmpPropertiesFile;
        File dir = propertiesFile.getParentFile();
        FileUtil.mkdirs(dir);
        tmpFile = File.createTempFile("fetch", ".tmp", dir);
        tmpPropertiesFile = File.createTempFile("fetch", ".properties", dir);    
        
        HttpURLConnection c =null;
        try
        {           
            c = (HttpURLConnection)url.openConnection();
            /*
             * TODO: c.setIfModifiedSince(timeout);
             */
            if( timeoutMS>0)
            {
                c.setReadTimeout(timeoutMS);
            }
            c.setRequestMethod(method.name());
            NetUrl.relaxSSLConnection(c);
            c.setDoOutput(false);
            if( auth != null)
            {
                auth.setRequestProperty( c);
            }

            if( agent!=null)
            {
                c.setRequestProperty("User-Agent", agent);
            }
            Properties p = new Properties();
            if( propertiesFile.exists())
            {
                p.load(new StringReader(( FileUtil.readFile(propertiesFile))));
            }            
            File previousResultsFile=null;
            String ifNoneMatch=p.getProperty(RestTransport.ETAG);
            if( StringUtilities.notBlank(ifNoneMatch))
            {
                String sha1=p.getProperty(RestTransport.RESULTS_SHA1);
                if( StringUtilities.notBlank(sha1))
                {
                    String tmpStatus = p.getProperty(RestTransport.STATUS);
                    if( tmpStatus.startsWith("2") || tmpStatus.equals(Integer.toString(Status.C304_NOT_MODIFIED.code)))
                    {
                        String tmpFileList = p.getProperty(RestTransport.FILE_LIST);
                        if( StringUtilities.notBlank(tmpFileList))
                        {
                            String fileList[]=tmpFileList.split(",");
                            String fn=fileList[fileList.length -1];
                            
                            File cacheFile=new File( dir, fn);
                            if( cacheFile.exists())
                            {
                                String tmpCS=new String( StringUtilities.encodeBase64( FileUtil.generateSHA1(cacheFile)));
                                if( tmpCS.equals(sha1))
                                {
                                    previousResultsFile=cacheFile;
                                    c.setRequestProperty(RestTransport.IF_NONE_MATCH, ifNoneMatch);
                                }
                            }
                        }
                    }
                }
            }
            if(disableGZIP == false)
            {
                c.setRequestProperty("Accept-Encoding", "gzip");
            }
            out=new FileOutputStream(tmpFile);

            int statusCode=c.getResponseCode();
            in=c.getErrorStream();
            if( in == null)
            {
                in=c.getInputStream();
            }
            String mimeType=c.getContentType();
            
            byte array[]=new byte[10 * 1024];

            while(true)
            {
                int read = in.read(array);
                if( read == -1) break;

                out.write(array, 0, read);
            }
            out.close();
            out=null;
            in.close();
            in=null;

            String transferEncoding=c.getContentEncoding();
            Trace trace;
            if( "gzip".equalsIgnoreCase(transferEncoding))
            {                
                File gzipFile = File.createTempFile("fetch", ".gzip", dir);
                try
                {
                    FileUtil.replaceTargetWithTempFile(tmpFile, gzipFile);

                    FileUtil.decompressFile(gzipFile, tmpFile);
                }
                finally
                {
                    FileUtil.deleteAll(gzipFile);
                }

                trace=Trace.FETCHED_GZIP;
            }
            else
            {
                trace=Trace.FETCHED_UNCOMPRESSED;
            }
            
            Status status=Status.find(statusCode);
            String redirection=null;
            switch( status)
            {
                case C304_NOT_MODIFIED:
                    if( previousResultsFile!=null)
                    {
                        return Response.builder(status,p.getProperty(RestTransport.MIME_TYPE), previousResultsFile).setTrace(Trace.CACHED).make();
                    }
                    break;
                    
                case C301_REDIRECT_MOVED_PERMANENTLY:
                case C302_REDIRECT_FOUND:
                case C303_REDIRECT_SEE_OTHER:
                    redirection=c.getHeaderField(HEADER_LOCATION);
                    p.setProperty(HEADER_LOCATION, redirection);
            }
                   
            String tmpCS=new String( StringUtilities.encodeBase64( FileUtil.generateSHA1(tmpFile)));
            String ext=".unknown";
            if( StringUtilities.notBlank(mimeType))
            {
                ext="." + FileUtil.requiredExtension(mimeType, "data");
            }
            
            File cacheFile=new File( tmpFile.getParentFile(), tmpCS.replace("/", "_").replace("=", "") + ext);
                 
            String cacheControl=c.getHeaderField(RestTransport.CACHE_CONTROL);
            if( StringUtilities.notBlank(cacheControl))
            {
                p.setProperty(RestTransport.CACHE_CONTROL, cacheControl);
            }
            else
            {
                p.remove(RestTransport.CACHE_CONTROL);
            }
            
            String eTag=c.getHeaderField(RestTransport.ETAG);
            if( StringUtilities.notBlank(eTag))
            {
                p.setProperty(RestTransport.ETAG, eTag);
            }
            else
            {
                p.remove(RestTransport.ETAG);
            }
            
            if( StringUtilities.isBlank(mimeType))
            {
                mimeType= ContentType.TEXT_HTML.mimeType;
            }
            
            p.setProperty( RestTransport.MIME_TYPE, mimeType);
            
            p.setProperty( RestTransport.STATUS, Integer.toString(statusCode));

            p.setProperty(RestTransport.RESULTS_SHA1, tmpCS);
            long lastModified= c.getHeaderFieldDate("Last-Modified", 0);
            if( lastModified > 0)
            {
                cacheFile.setLastModified(lastModified);
            }
            mantainFileHistory(p, cacheFile);

            try (FileWriter fw = new FileWriter(tmpPropertiesFile)) {
                p.store(fw, "");
            }
            FileUtil.replaceTargetWithTempFile(tmpFile, cacheFile);
            cacheFile.setReadOnly();
            FileUtil.replaceTargetWithTempFile(tmpPropertiesFile, propertiesFile);
            
            return Response.builder(status,mimeType, cacheFile).setRedirection(redirection).setTrace(trace).make();
        }
        catch( Exception e)
        {
            LOGGER.warn( StringUtilities.stripPasswordFromURL(url.toString()), e);
            try (PrintWriter pw = new PrintWriter(tmpFile)) {
                pw.println(e.getMessage());
                
                e.printStackTrace(pw);
            }
            
            Properties p = new Properties();
            if( propertiesFile.exists())
            {
                p.load(new StringReader(( FileUtil.readFile(propertiesFile))));
            }
            p.setProperty( RestTransport.MIME_TYPE, ContentType.TEXT_PLAIN.mimeType);
            Status status=Status.C500_SERVER_INTERNAL_ERROR;
            if( e instanceof UnknownHostException)
            {
                status=Status.C503_SERVICE_UNAVAILABLE;
            }
            else if( e instanceof SSLProtocolException)
            {
                status=Status.C526_SSL_INVALID_CERTIFICATE;
            }
            else if( e instanceof SSLException)
            {
                status=Status.C525_SSL_HANDSHAKE_FAILED;
            }
            else if( e instanceof CertificateException)
            {
                status=Status.C526_SSL_INVALID_CERTIFICATE;
            }
            else if( e instanceof SocketTimeoutException)
            {
                status=Status.C598_TIMED_OUT_SERVER_NETWORK_READ;
            }

            p.setProperty( RestTransport.STATUS, Integer.toString(status.code));
            String tmpCS=new String( StringUtilities.encodeBase64( FileUtil.generateSHA1(tmpFile)));
            p.setProperty(RestTransport.RESULTS_SHA1, tmpCS);
            File cacheFile=new File( tmpFile.getParentFile(), tmpCS.replace("/", "_").replace("=", "") + ".error");
            FileUtil.replaceTargetWithTempFile(tmpFile, cacheFile);
            mantainFileHistory(p, cacheFile);
            
            try (FileWriter fw = new FileWriter(tmpPropertiesFile)) {
                p.store(fw, "");
            }
            FileUtil.replaceTargetWithTempFile(tmpPropertiesFile, propertiesFile);
            throw e;
        }
        finally
        {
            try
            {
                if( in != null) in.close();
            }
            catch( IOException io)
            {
                LOGGER.warn( "Could not close IN", io);
            }
            try
            {
                if( out != null) out.close();
            }
            catch( IOException io)
            {
                LOGGER.warn( "Could not close OUT", io);
            }

            if( c != null){
                c.disconnect();
            }

            FileUtil.deleteAll(tmpFile);
            FileUtil.deleteAll(tmpPropertiesFile);
        }
    }
    
    @CheckReturnValue @Nonnull
    private Response nonCachedCall() throws Exception 
    {
        InputStream in=null;

        HttpURLConnection c =null;
        try
        {
            URL tmpURL=url;
            byte[] postData=null;
            if( body==null)
            {
                String temp=url.toString();
                int pos=temp.indexOf("?");
                if( pos != -1)
                {
                    tmpURL=new URL( temp.substring(0, pos));
                    postData = temp.substring(pos + 1).getBytes( StandardCharsets.UTF_8 );
                }
            }
            c = (HttpURLConnection)tmpURL.openConnection();
            /*
             * TODO: c.setIfModifiedSince(timeout);
             */
            if( timeoutMS>0)
            {
                c.setReadTimeout(timeoutMS);
            }
            c.setRequestMethod(method.name());
//            c.setDefaultUseCaches(false);
            NetUrl.relaxSSLConnection(c);
            
            if( DispositionType.ATTACHMENT.equals(dispositionType))
            {
                if( contentType!=null)
                {
                    c.setRequestProperty("Content-Type", contentType.mimeType);
                }
                c.setRequestProperty("Content-Length", Long.toString(body.length()));
                String md5=new String( StringUtilities.encodeBase64(FileUtil.generateMD5(body)));
                c.setRequestProperty("Content-MD5", md5);
            }

            if( auth != null)
            {
                auth.setRequestProperty( c);
            }

            if( agent!=null)
            {
                c.setRequestProperty("User-Agent", agent);
            }
            if(disableGZIP == false)
            {
                c.setRequestProperty("Accept-Encoding", "gzip");
            }
            c.setUseCaches(false);
            if(body != null)
            {           
                if( DispositionType.ATTACHMENT.equals(dispositionType))
                {
                    c.setDoOutput(true);

                    try(OutputStream os = c.getOutputStream();FileInputStream fin=new FileInputStream( body))
                    {
                        byte array[] = new byte[10240];
                        while ( true )
                        {
                            int len = fin.read( array );

                            if ( len <= 0 )
                            {
                                break;
                            }
                            os.write( array, 0, len );
                        }
                        
                        os.flush();
                        os.close();
                    }
                }
                else
                {
                    byte randomBytes[]=new byte[30];
                    Random r = new Random();
                    r.nextBytes(randomBytes);
                    String boundary="===stsFormBoundary"+ new String( StringUtilities.encodeBase64(randomBytes));
                    c.setRequestProperty( "Content-Type", "multipart/form-data;boundary=" + boundary);
                    c.setDoOutput(true);

                    try(OutputStream os = c.getOutputStream())
                    {
                        PrintWriter pw=new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8),true);
                        String fileName=body.getName().replace("\"", "_").replace("\\", "_");
                        pw.append(TWO_HYPHENS).append(boundary).append(LINE_FEED);
                        pw.append("Content-Disposition: form-data; name=\"file\";filename=\"" + fileName + "\"").append( LINE_FEED);
                        pw.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
                        pw.append("Content-Length: ").append(Long.toString(body.length())).append(LINE_FEED);
                        
                        ContentType tmpContentType=contentType;
                        if( tmpContentType==null)
                        {
                            tmpContentType=ContentType.APPLICATION_OCTET_STREAM;
                        }
                        pw.append("Content-Type:").append(tmpContentType.mimeType).append( LINE_FEED);
                        pw.append(LINE_FEED);
                        pw.flush();

                        try (FileInputStream fin = new FileInputStream( body ))
                        {
                            byte array[] = new byte[10240];
                            while ( true )
                            {
                                int len = fin.read( array );

                                if ( len <= 0 )
                                {
                                    break;
                                }
                                os.write( array, 0, len );
                            }
                        }

                        os.flush();

                        pw.append(LINE_FEED);
                        pw.append(TWO_HYPHENS).append(boundary).append(TWO_HYPHENS).append(LINE_FEED);
                        pw.flush();
                        
                        os.flush();
                        os.close();
                    }
                }
            }
            else
            {
                if( postData!=null)
                {
                    c.setDoOutput(true);

                    c.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
                    c.setRequestProperty( "charset", StandardCharsets.UTF_8.name());
                    c.setRequestProperty( "Content-Length", Integer.toString( postData.length ));
                    c.setUseCaches( false );
                    try( DataOutputStream os = new DataOutputStream( c.getOutputStream())) {
                       os.write( postData );
                    }
                }
                else
                {
                    c.setDoOutput(false);                    
                }
            }
            
            ByteArrayOutputStream out=new ByteArrayOutputStream(); 
           
            int statusCode=c.getResponseCode();
            in=c.getErrorStream();
            if( in == null)
            {
                in=c.getInputStream();
            }
            String mimeType=c.getContentType();
            byte array[]=new byte[10 * 1024];

            while(true)
            {
                int read = in.read(array);
                if( read == -1) break;

                out.write(array, 0, read);
            }
            
            in.close();
            in=null;
            String transferEncoding=c.getContentEncoding();
            Trace trace;
            String data;
            if( "gzip".equalsIgnoreCase(transferEncoding))
            {                
                data =StringUtilities.decompress(out.toByteArray());
                trace=Trace.FETCHED_GZIP;
            }
            else
            {
                trace=Trace.FETCHED_UNCOMPRESSED;
                data=new String( out.toByteArray(), StandardCharsets.UTF_8 );
            }
            String redirection=null;
            Status status = Status.find(statusCode);
            switch( status)
            {
                case C301_REDIRECT_MOVED_PERMANENTLY:
                case C302_REDIRECT_FOUND:
                case C303_REDIRECT_SEE_OTHER:
                    redirection=c.getHeaderField(HEADER_LOCATION);
            }
            
            /**
             * AWS S3 sends back zero length response with no content type.
             */
            if( mimeType==null && data.isEmpty())
            {
                mimeType=ContentType.TEXT_PLAIN.mimeType;
            }
            assert mimeType!=null;
            return Response.builder(status,mimeType, data).setRedirection(redirection).setTrace(trace).make();
            //return Response.builder(Status.find(status),mimeType, data).setTrace(trace).make();
//            return new Response(data, trace, mimeType, Status.find(status));
        }
        catch( Exception e)
        {
            LOGGER.warn( StringUtilities.stripPasswordFromURL(url.toString()), e);
            throw e;
        }
        finally
        {
            try
            {
                if( in != null) in.close();
                
                if( c != null) c.disconnect();
            }
            catch( IOException io)
            {
                LOGGER.warn( "Could not close", io);
            }
        }
    }
}
