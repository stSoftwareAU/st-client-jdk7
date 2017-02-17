/**
 *  STS Remote library
 *
 *  Copyright (C) 2006  stSoftware Pty Ltd
 *
 *  www.stsoftware.com.au
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

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.CProperties;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.net.NetUrl;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;
import org.w3c.dom.Document;

/**
 *  Soap transport.
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *
 *  @author      Nigel Leck
 *  @since       3 December 2001, 12:02
 */
public abstract class SoapTransport
{
    /**
     * Environment variable to control the compression of SOAP requests
     */
    public static final String ENV_SOAP_COMPRESSED="SOAP_COMPRESSED";

    /** 
     * SOAP POST in compressed format
     */
    public static final boolean SOAP_COMPRESSED;

    /** disable compression */
    protected boolean disabledCompression;

     /** Host name */ 
    private final String host;
    
    /** Default login id */
    protected String defaultLogin;//NOPMD
    
    /** Default Password */
    protected String      defaultPassword = "";
    
    /** Default Layer */
    private String      defaultLayer = "";//NOPMD    

    /**
     * The logger for the master db
     */
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.soap.internal.SoapTransport");//#LOGGER-NOPMD

    /**
     * The soap transport layer
     * @param url the URL to connect to
     */
    public SoapTransport(final String url)
    {
        NetUrl rUrl = new NetUrl( url);
        
        host = rUrl.getHost();
        defaultLogin = rUrl.getLogin();
        defaultPassword =rUrl.getPassword();
        defaultLayer = rUrl.getLayer();       
    }

    /**
     *
     * @return true if compressed enabled.
     */
    public boolean isCompressEnabled()
    {
        if( SOAP_COMPRESSED)
        {
            return disabledCompression != true;
        }

        return false;
    }

    /**
     * disable compress
     */
    public void disableCompress()
    {
        disabledCompression=true;
    }

    /**
     * The host.
     * @return the host
     */
    public String getHost()
    {
        return host;
    }

    /**
     * The host.
     * @return The host
     */
    public String getDefaultLogin()
    {
        return defaultLogin;
    }
     
    /**
     * The default password
     * @return the password.
     */
    public String getDefaultPassword()
    {
        return defaultPassword;
    }
     
    /**
     * The default layer
     * @return the layer
     */
    public String getDefaultLayer()
    {
        return defaultLayer;
    }

    /**
     * send the envelope.
     * 
     * <?xml version="1.0" encoding="UTF-8"?>
     * <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
     *  <soapenv:Header>
     *   <SessionInfo soapenv:actor="http://schemas.xmlsoap.org/soap/actor/next" soapenv:mustUnderstand="0" xmlns="">
     *    <SESSION_SIGNATURE xmlns="">self_test</SESSION_SIGNATURE>
     *    <SESSION_TZ xmlns="">Australia/Sydney</SESSION_TZ>
     *   </SessionInfo>
     * </soapenv:Header>
     *  <soapenv:Body>
     *   <execute xmlns="">
     *    <sql>DISCOVER SSO</sql>
     *   </execute>
     *   <messageID xmlns="">1</messageID>
     *  </soapenv:Body>
     * </soapenv:Envelope>
     *
     * @param envelope the envelope to send
     * @param relativePath the path to send it to
     * @return The result document
     * @throws Exception A serious problem
     */
    public abstract Document sendEnvelope( final Document envelope, String relativePath) throws Exception;

    /**
     * debug info
     * @return connection details
     */
    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        StringBuilder buffer = new StringBuilder("transport ");
        
        buffer.append( defaultLogin != null ? defaultLogin : "guest");
        buffer.append( "@");
        if( host != null)
        {
            String temp = StringUtilities.replace( host, "https://", "");
            temp = StringUtilities.replace( temp, "http://", "");
            buffer.append( temp);
        }

        buffer.append( "/");
        buffer.append( defaultLayer != null ? defaultLayer : "");
        
        return  buffer.toString(); 
    }

    static
    {
        String temp;
        temp = CProperties.getProperty( ENV_SOAP_COMPRESSED, "");
        temp = temp.toLowerCase();
        
        SOAP_COMPRESSED = !temp.startsWith( "n") && !temp.startsWith( "f");
    }    
}
