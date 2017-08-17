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

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.misc.TimeUtil;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Calculate the AWS authorization header. 
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED test case</i>
 *
 * @author Nigel Leck
 * @since 12 August 2017
 */
public final class AWSReSTAuthorization implements ReSTAuthorizationInterface
{
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    public final String accessKeyID;
    private final String secretAccessKey;

    public AWSReSTAuthorization(final String accessKeyID, final String secretAccessKey) {
        this.accessKeyID=accessKeyID;
        this.secretAccessKey=secretAccessKey;
    }

    @Override
    public ReSTAuthorizationInterface setRequestProperty(URLConnection c) {
        HttpURLConnection http=(HttpURLConnection)c;
        String method = http.getRequestMethod();

        String headerDateString =http.getRequestProperty("Date");// "";//http.getHeaderField("Date");
        if( StringUtilities.isBlank(headerDateString))
        {
            headerDateString = TimeUtil.format("E, d MMM yyyy H:mm:ss +0000", new Date(), null);
            http.setRequestProperty("Date", headerDateString);
        }
        String stringToSign;
        stringToSign=method +"\n";
        
        String md5=c.getRequestProperty("Content-MD5");
        if( StringUtilities.notBlank(md5))
        {
            stringToSign+=md5 +"\n";
        }
        else
        {
            stringToSign+="\n";
        }
        String contentType=c.getRequestProperty("Content-Type");
        if( StringUtilities.notBlank(contentType))
        {
            stringToSign+=contentType +"\n";
        }
        else
        {
            stringToSign+="\n";
        }
        stringToSign+=headerDateString +"\n";
        
        
        Map<String, List<String>> requestProperties = c.getRequestProperties();
        
        ArrayList<String> awsList=new ArrayList();
        for( String key: requestProperties.keySet())
        {
            String name=key.toLowerCase();
            
            if( name.startsWith("x-amz-"))
            {
                String value=name+":";
                List<String> valueList = requestProperties.get(key);
                boolean started=false;
                for( String tmp: valueList)
                {
                    if( started) value+=",";
                    started=true;
                    value+=tmp;
                }
                
                awsList.add(value);
            }
        }
        
        Collections.sort(awsList);
        for( String value: awsList)
        {
            stringToSign+=value +"\n";
        }
        String host = http.getURL().getHost();
        if( host.contains("amazonaws.com"))
        {
            stringToSign+="/" + host.substring(0, host.indexOf("."));
        }
        else
        {
            stringToSign+="/" + host;
        }
        
        stringToSign+=http.getURL().getPath();

        String signature=calculateRFC2104HMAC(stringToSign,secretAccessKey );

        String auth = "AWS" + " " + accessKeyID + ":" + signature;

        c.setRequestProperty(ReSTUtil.HEADER_AUTHORIZATION, auth);
        
        return this;
    }

    @Nonnull @CheckReturnValue
    private String calculateRFC2104HMAC(final @Nonnull String data, final @Nonnull String key)
    {
        try{
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            return new String(StringUtilities.encodeBase64(mac.doFinal(data.getBytes())));
        }
        catch( IllegalStateException | InvalidKeyException | NoSuchAlgorithmException e)
        {
            throw CLogger.rethrowRuntimeExcepton(e);
        }
    }
    @Override @Nonnull @CheckReturnValue
    public String checkSumAdler32(final @Nonnull String url) {
        return StringUtilities.checkSumAdler32(url + accessKeyID);
    }

    @Override @Nonnull @CheckReturnValue
    public String toShortString() {
        return accessKeyID;
    }
}
