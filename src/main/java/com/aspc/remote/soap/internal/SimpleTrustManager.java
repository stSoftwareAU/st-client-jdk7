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
package com.aspc.remote.soap.internal;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.apache.commons.logging.Log;

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.DateUtil;
import javax.net.ssl.X509TrustManager;

/**
 *  SimpleTrustManager
 *  
 *  Hook for Axis sender, allowing unsigned server certs
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  
 *  @author      Suren Potharaju
 *  @since       February 23, 1:50 PM
 */
public class SimpleTrustManager implements X509TrustManager
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.soap.internal.SimpleTrustManager");//#LOGGER-NOPMD
    
    /**
     * Do not need to implement this method.
     * @return always null
     */
    @Override
    public X509Certificate[] getAcceptedIssuers()//NOPMD
    {
        return null;
    }
    
    /**
     * Do not need to implement this method.
     * @param chain the chain of certificates 
     * @param authType the authentication type  
     * @throws java.security.cert.CertificateException not valid
     */
    @Override
    public void checkServerTrusted( final X509Certificate[] chain, final String authType ) throws CertificateException
    {
        isServerTrusted(chain);
    }
    
    /**
     * Do not need to implement this method.
     * @param chain the chain of certificates 
     * @param authType the authentication type  
     * @throws java.security.cert.CertificateException not valid
     */
    @Override
    public void checkClientTrusted( final X509Certificate[] chain, final String authType ) throws CertificateException
    {
        // No client validation
    }
    
    /**
     *  This is the method which gets called while validating
     *  server certificate. We can check the expiry of the 
     *  certificate
     *  
     * 
     * @param cert the chain of certificates
     * @return true if trusted
     */
    public boolean isServerTrusted(final X509Certificate [] cert )
    {
        
        for (X509Certificate tt : cert) {
            try
            {
                Date dt = DateUtil.getToday(DateUtil.GMT_ZONE);
                tt.checkValidity(dt);                
            }
            catch(Exception ex)
            {
                LOGGER.debug("SSL Certificate Expired: "+ex.getMessage());
                LOGGER.debug("SSL Certificate Info: " +tt.toString());
            }
        }
        
        return true;
    }
    
    /**
     * Do not need to implement this method as we are not
     * interested client side
     * @param cert the chain of certificates
     * @return always TRUE
     */
    public boolean isClientTrusted( final X509Certificate [] cert )
    {
        return true;
    }
}
