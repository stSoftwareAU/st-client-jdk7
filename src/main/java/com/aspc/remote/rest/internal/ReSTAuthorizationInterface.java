package com.aspc.remote.rest.internal;

import java.net.URLConnection;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 *
 *  @author      Lei Gao
 *  @version     $Revision: 1.1 $
 *  @since       06 May 2016 
 */
public interface ReSTAuthorizationInterface
{
    @Nonnull
    public ReSTAuthorizationInterface setRequestProperty( final @Nonnull URLConnection c);

    @CheckReturnValue @Nonnull
    public String checkSumAdler32( final @Nonnull String url);
    
    @Override @CheckReturnValue @Nonnull
    public String toString();
    
    @CheckReturnValue @Nonnull
    public String toShortString();
}
