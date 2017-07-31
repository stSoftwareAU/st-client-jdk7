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
package com.aspc.remote.rest;

import com.aspc.remote.rest.errors.ReSTException;
import com.aspc.remote.rest.internal.Trace;
import com.aspc.remote.util.misc.DocumentException;
import com.aspc.remote.util.misc.DocumentUtil;

import com.aspc.remote.util.misc.FileUtil;
import com.aspc.remote.util.misc.StringUtilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.json.JSONObject;
import org.w3c.dom.Document;

/**
 *  ReST call response. 
 * 
 *  <i>THREAD MODE: readonly</i>
 *
 *  @author      Lei Gao
 *  @since       Jan 9, 2014
 */
public final class Response
{
    private final File file;
    private final String data;
    public final @Nullable String redirection;
    public final Trace trace;
    public final String mimeType;
    public final String cacheControl;
    public final Status status;
    public final long lastModified;
    
    public static Builder builder(final @Nonnull Status status, final @Nonnull String mimeType, final @Nonnull File file)
    {
        if( file ==null) throw new IllegalArgumentException( "data must not be null");
        return new Builder( status, mimeType, file,null);
    }
        
    public static Builder builder(final @Nonnull Status status, final @Nonnull String mimeType, final @Nonnull String data)
    {
        if( data ==null) throw new IllegalArgumentException( "data must not be null");
        return new Builder( status, mimeType,null, data);
    }
    
    
    public static Builder builder(final @Nonnull Status status, final @Nonnull ContentType contentType, final @Nonnull String data)
    {
        if( data ==null) throw new IllegalArgumentException( "data must not be null");
        if( contentType==null)  throw new IllegalArgumentException( "content type must not be null");
        
        return new Builder( status, contentType.mimeType,null, data);
    }
    
    public static class Builder
    {
        private final File file;
        private final String data;
        private Trace trace=Trace.UNKNOWN;
        private final String mimeType;
        private String cacheControl="no-cache";
        private final Status status;
        private String redirection;
        
        private Builder( 
            final @Nonnull Status status, 
            final @Nonnull String mimeType,
            final @Nonnull File file,
            final @Nullable String data
        )
        {
            if( status==null) throw new IllegalArgumentException("status is mandatory");
//            if( file, final @Nullable String data==null) throw new IllegalArgumentException("file is mandatory");
            
            if( StringUtilities.isBlank(mimeType)) throw new IllegalArgumentException("mimeType is mandatory");
            this.status=status;
            this.file=file;
            this.data=data;
            this.mimeType=mimeType;
        }
        
        /**
         * Set the trace for this call.
         * @param trace the trace of this call.
         * @return this
         */
        public @Nonnull Builder setTrace( final @Nonnull Trace trace)
        {
            if( trace==null) throw new IllegalArgumentException("trace must not be null");
            this.trace=trace;
            
            return this;
        }

        public @Nonnull Builder setRedirection( final @Nullable String redirection)
        {
            this.redirection=redirection;
            return this;
        }
        
        /**
         * Set the cache control for this call.
         * @param cacheControl the cache control.
         * @return this
         */
        public @Nonnull Builder setCacheControl( final @Nonnull String cacheControl)
        {
            if( StringUtilities.isBlank(cacheControl)){
                this.cacheControl="no-cache";
            }
            else
            {
                this.cacheControl=cacheControl;
            }
            
            return this;
        }
        
        public @Nonnull Response make() throws IllegalArgumentException
        {            
            return new Response( file, data, trace, mimeType, status, cacheControl,redirection);
        }
    }

    private Response(
        final @Nullable File file, 
        final @Nullable String data, 
        final @Nonnull Trace trace, 
        final @Nonnull String mimetype, 
        final @Nonnull Status status,
        final @Nonnull String cacheControl,
        final @Nullable String redirection
    )
    {
        this.data=data;
        this.file=file;
        assert data != null || file != null: "mandatory data or file";
        this.trace=trace;
        if( trace==null) throw new IllegalArgumentException("trace is mandatory");
        this.mimeType=mimetype;
        if( StringUtilities.isBlank(mimeType)) throw new IllegalArgumentException("mimeType is mandatory");
        this.status=status; 
        if( status==null) throw new IllegalArgumentException("status is mandatory");
        if( file == null)
        {
            this.lastModified=System.currentTimeMillis();
        }
        else
        {
            this.lastModified=file.lastModified();
        }
        
        if( cacheControl==null) throw new IllegalArgumentException("cacheControl is mandatory");
        
        this.cacheControl=cacheControl;
        this.redirection=redirection;
        
    }
    
    /**
     * When this response was modified.
     * @return 
     */
    @CheckReturnValue
    public @Nonnull Date getLastModified()
    {
        return new Date(lastModified);
    }
    
    /**
     * Get the content as a String. 
     * 
     * @return the content.
     * @throws IOException problem reading. 
     */
    @CheckReturnValue @Nonnull
    public String getContentAsString() throws IOException
    {
        if( data != null)
        {
            return data;
        }
        
        if( file == null || file.exists() == false)
        {
            return "";
        }
        return FileUtil.readFile(file);
    }
 
    /**
     * Get the content as a XML. 
     * 
     * @return the content.
     * @throws IOException problem reading. 
     * @throws DocumentException invalid XML
     */
    @CheckReturnValue @Nonnull
    public Document getContentAsXML() throws IOException, DocumentException
    {
        String xml=getContentAsString();
        return DocumentUtil.makeDocument(xml);
    }
    
    /**
     * Get the content as a JSON. 
     * 
     * @return the content.
     * @throws IOException problem reading. 
     */
    @CheckReturnValue @Nonnull
    public JSONObject getContentAsJSON() throws IOException
    {
        String text=getContentAsString();
        if( text.startsWith("{"))
        {
            return new JSONObject( text);
        }
        else
        {
            throw new FileNotFoundException(text);
        }
    }
    
    /**
     * Get the content as a file. 
     * 
     * @return the content.
     * @throws IOException problem reading. 
     */
    @CheckReturnValue @Nonnull
    public File getContentAsFile() throws IOException
    {
        if( data != null)
        {
            String ext= FileUtil.requiredExtension(mimeType, ".data");
            File tmpFile=File.createTempFile("rest", ext, FileUtil.makeQuarantineDirectory());
            try (FileWriter fw = new FileWriter( tmpFile)) {
                fw.write(data);
            }
            
            String cs=new String(StringUtilities.encodeBase64(FileUtil.generateSHA1(tmpFile)));
            cs=cs.replace("=", "");
            File targetFile=new File( FileUtil.makeQuarantineDirectory(), "rest_" + cs + ext);
            FileUtil.replaceTargetWithTempFile(tmpFile, targetFile);
            
            return targetFile;
        }
        
        return file;
    }
    
    /**
     * Get a raw input stream of the content. 
     * @return the input stream. 
     * @throws IOException a problem reading. 
     */
    @CheckReturnValue @Nonnull
    public byte[] getContentAsByteArray() throws IOException
    {      
        if( data != null) 
        {
            return data.getBytes(StandardCharsets.UTF_8);            
        }
        
        long len=file.length();
        if( len >= Integer.MAX_VALUE)
        {
            throw new IOException( "too large for byte array " + len);
        }
        
        try(FileInputStream in=new FileInputStream( file))
        {
            byte[] a;
           
            a = new byte[(int)len];
            in.read(a);
        
            return a;
        }
    }    
    
    /**
     * http://www.restapitutorial.com/httpstatuscodes.html
     * 
     * 
     * @return the status name.
     * 
     * @throws FileNotFoundException 404
     * @throws com.aspc.remote.rest.errors.ReSTException unknown error.
     */
    @Nonnull
    public String checkStatus() throws FileNotFoundException, ReSTException
    {
//        switch( status)
//        {
//            case C301_REDIRECT_MOVED_PERMANENTLY:
//            case C302_REDIRECT_FOUND:
//            case C303_REDIRECT_SEE_OTHER:
//                throw new RedirectionRestException( status, redirection);
//        }

        try{
            return status.check();
        }
        catch( ReSTException re)
        {
            if( ContentType.TEXT_PLAIN.matches(mimeType) && StringUtilities.notBlank(data))
            {
                ReSTException re2=new ReSTException( re.status, data, re);
                throw re2;
            }
            else
            {
                throw re;
            }
        }
    }
}
