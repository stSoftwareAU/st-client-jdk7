package com.aspc.remote.rest;

import com.aspc.remote.util.misc.StringUtilities;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 *
 *  @author      Lei Gao
 *  @since       13 May 2015 
 */
public class ContentType
{
    public final static ContentType TEXT_PLAIN=new ContentType( "text/plain");
    public final static ContentType TEXT_HTML=new ContentType( "text/html");
    public final static ContentType TEXT_CSS=new ContentType( "text/css");
    
    public final static ContentType TEXT_CSV=new ContentType( "text/csv");
    public final static ContentType APPLICATION_XML=new ContentType( "application/xml");
    public final static ContentType APPLICATION_JSON=new ContentType( "application/json");
    public final static ContentType APPLICATION_JAVASCRIPT=new ContentType( "application/javascript");
    
    public final String mimeType;

    public ContentType(final @Nonnull String mimeType)
    {
        if( mimeType == null) throw new IllegalArgumentException( "mime type is mandatory");
        if( mimeType.toLowerCase().matches(".*charset *=.*")) throw new IllegalArgumentException( "mime type must not contain charset: " + mimeType);
        assert mimeType.contains("/"): "invalid mime type: " + mimeType;

        if( mimeType.contains("/"))
        {
            this.mimeType=mimeType;
        }
        else
        {            
            this.mimeType = "application/" + mimeType.toLowerCase().trim();
        }
    }

    /**
     * The mime type and UTF-8 character set. 
     * @return the mine type & character set. 
     */
    @CheckReturnValue @Nonnull
    public String getContentTypeUTF8()
    {
        return mimeType + ";charset=UTF-8";
    }
    
    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        return mimeType;
    }
    
    @CheckReturnValue
    public boolean matches( final @Nonnull String accept)
    {
        String tmpAccept=accept.toLowerCase().trim();
        int pos = tmpAccept.indexOf(";");
        if( pos != -1)
        {
            tmpAccept=tmpAccept.substring(0, pos).trim();
        }
        if( tmpAccept.contains("/")==false)
        {
            tmpAccept="*/" +tmpAccept;
        }
        
        String tmpMimeType=mimeType.toLowerCase();
        int pos2 = tmpMimeType.indexOf(";");
        if( pos2 != -1)
        {
            tmpMimeType=tmpMimeType.substring(0, pos2).trim();
        }
        if( StringUtilities.isLike(tmpAccept, tmpMimeType))
        {
            return true;
        }
        return false;
    }
}
