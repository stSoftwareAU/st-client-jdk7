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
package com.aspc.remote.rest.internal;

import com.aspc.remote.util.misc.StringUtilities;
import java.net.URLConnection;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *  ReST client which will cache requests for a given period. 
 *  
 *  <h3>Optimizations</h3>
 *  <ul>
 *      <li> Requests that are for data that we have an existing result less than the cache period will return immediately. 
 *      <li> Requests that are for data that are half the age of the cache period will result in a "pre-fetch" request being made in the background.
 *      <li> Multiple requests for the same URL will result in only one request to the web server. All requests will get the same result when returned.
 *  </ul>
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Lei Gao
 *  @since       Jan 9, 2014
 */
public final class ReSTAuthorization implements ReSTAuthorizationInterface
{
    private final String user;
    private final String passwd;
    private final String domain;
    private final String token;
    
    public ReSTAuthorization(final @Nonnull String token)
    {
        if( StringUtilities.isBlank(token)) throw new IllegalArgumentException("authorization token is mandatory");
        this.token=token;
        this.user="unknown";
        this.passwd=null;
        this.domain=null;
    }

    public ReSTAuthorization(final @Nonnull String user, final @Nonnull String passwd, final @Nullable String domain)
    {
        if( StringUtilities.isBlank(user)) throw new IllegalArgumentException("authorization user must NOT be blank");
        if( passwd==null)
        {
            throw new IllegalArgumentException("authorization password is mandatory");
        }
        this.user=user;
        this.passwd=passwd;
        this.domain=domain;
        this.token=null;
    }
    
    @Override @Nonnull
    public ReSTAuthorization setRequestProperty( final @Nonnull URLConnection c)
    {
        String auth;
        if( token ==null)
        {
            String userpass = (domain != null ? domain + "\\": "") + user + ":" + passwd;
            auth = "Basic " + StringUtilities.encodeBase64(userpass);
        }
        else
        {
            auth = "Token " + token;
        }
        c.setRequestProperty(ReSTUtil.HEADER_AUTHORIZATION, auth);
        
        return this;
    }
    
    @Override @CheckReturnValue @Nonnull
    public String checkSumAdler32( final @Nonnull String url)
    {
        if( token!=null)
        {
            return StringUtilities.checkSumAdler32(token + ":" + url);            
        }
        else
        {
            String tmp=(domain != null ? domain + "\\": "") +user +":" + url +"#"+ passwd;

            return StringUtilities.checkSumAdler32(tmp);
        }
    }
    
    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        if( token != null)
        {
            int first=0;
            if( token.length()>12)
            {
                first=4;
            }
            int last = token.length()-4;
            
            if( last<0) last = 0;
            
            StringBuilder sb=new StringBuilder();
            for( int i=first +1;i<last;i++) {
                sb.append("*");
            }
            String tempToken=token.substring(0, first) + sb.toString() + token.substring(last);
            return "token: " + tempToken;
        }
        else
        {
            return (domain != null ? domain + "\\": "") + user + ":xxxx";
        }
    }

    @Override @Nonnull
    public String toShortString()
    {
        return user;
    }
}
