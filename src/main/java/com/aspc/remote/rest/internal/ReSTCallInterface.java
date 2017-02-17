package com.aspc.remote.rest.internal;

import com.aspc.remote.rest.Response;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 *
 *  @author      Lei Gao
 *  @version     $Revision: 1.1 $
 *  @since       06 May 2016 
 */
public interface ReSTCallInterface
{
    @Nonnull @CheckReturnValue
    public Response doCall( @Nonnull final RestCall call) throws Exception;
}
