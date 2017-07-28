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
package com.aspc.remote.soap.http;

import org.w3c.dom.Document;
import com.aspc.remote.soap.internal.SoapTransport;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.DocumentUtil;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.net.NetUrl;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import org.apache.commons.logging.Log;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *  HTTP Transport for SOAP calls
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *
 *  @author      Nigel Leck
 *  @since       3 December 2001, 12:02
 */
public class WebTransport extends SoapTransport
{
    /** Log handler */
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.soap.http.WebTransport");//#LOGGER-NOPMD
    
    /** holder to place the header xml */
    private String headerXml = "";
    /** Static field for moveTo tag */
    private static final String MOVE_TO = "moveTo";    
    private boolean invalidSSL=false;
    public static final ThreadLocal<Boolean> BY_PASS_INVALID_CERT=new ThreadLocal()
    {
        @Override
        protected Object initialValue() 
        {
            return Boolean.FALSE;
        }
    };
            
    
    /**
     * The raw transport layer.
     * @param host The host name
     */
    public WebTransport(String host)
    {
       super( host);
    }

    /**
     * Constructor to set the host, userId and password.
     * @param host Hostname
     * @param userId User Id
     * @param password Password
     */
    public WebTransport(final String host, final String userId, final String password)
    {
        super(host);
        this.defaultLogin = userId;
        this.defaultPassword = password;
    }

    private boolean initizalized;
    @SuppressWarnings("SleepWhileInLoop")
    private void init() throws Exception
    {
        String host=getHost();
        int pos = host.indexOf("://");
        if( pos!=-1)
        {
            host=host.substring(pos+3);
        }
        for( int loop=0; loop<3;loop++){
            try{
                InetAddress.getAllByName(host);
                break;
            }
            catch( UnknownHostException uhe)
            {
                LOGGER.warn( "could not resolve: " + host, uhe);
                Thread.sleep((int)(200 * Math.random()));
            }
        }
        initizalized=true;
    }
    /**
     * Send the SOAP envelope
     * @param envelope The envelope to send
     * @param relativePath Where to send it
     * @throws Exception A serious problem
     * @return The result document.
     */
    @Override
    @SuppressWarnings("null")
    public Document sendEnvelope( final Document envelope, final String relativePath) throws Exception
    {
        if( initizalized==false)
        {
            init();
        }
        String data = DocumentUtil.docToString(envelope);
        
        URL url = new URL( getHost() + relativePath);
        HttpURLConnection conn=null;
        try
        {
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            byte[] bytes = StringUtilities.compressToBytes(data);
            conn.setRequestProperty("Content-Length", "" +bytes.length);
            conn.setRequestProperty("Content-Type", "text/xml");
            conn.setRequestProperty("Content-Encoding", "gzip");
            conn.setRequestProperty("SoapAction", ""); 
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches (false);
            conn.setConnectTimeout(TIMEOUT);
            
            OutputStream out=null;
            if( invalidSSL == false)
            {
                try
                {
                    out = conn.getOutputStream();
                }
                catch( SSLHandshakeException sslEx)
                {
                    if( BY_PASS_INVALID_CERT.get())
                    {
                        if( conn instanceof HttpsURLConnection)
                        {
                            invalidSSL=true;
                            LOGGER.warn(getHost() + " SSL warning", sslEx);
                        }
                        else
                        {
                            throw sslEx;
                        }
                    }
                    else
                    {
                        throw sslEx;
                    }
                }                
            }
            
            if( invalidSSL)
            {
                NetUrl.relaxSSLConnection( conn);
                out = conn.getOutputStream();
            }

            out.write(bytes);

            int responseCode = conn.getResponseCode();
            if( responseCode != 200)
            {
                throw new Exception( conn.getResponseMessage());
            }
            conn.connect();

            ByteArrayOutputStream bOut=new ByteArrayOutputStream();
            InputStream in=conn.getInputStream();

            byte array[]=new byte[2048];

            while( true)
            {
                int count = in.read(array);

                if( count == -1) break;

                bOut.write(array, 0, count);
            }
            String xml;
            String encoding=conn.getHeaderField("Content-Encoding");
            if( "gzip".equals(encoding))
            {
                xml= StringUtilities.decompress(bOut.toByteArray());
            }
            else
            {
                xml=new String( bOut.toByteArray(), StandardCharsets.UTF_8);
            }

            Document result = DocumentUtil.makeDocument(xml, DocumentUtil.PARSER.TOLERANT);

            headerXml = "";
            Element header = (Element)result.getElementsByTagName("Header").item(0);

            if( header != null)
            {
                StringWriter sw=new StringWriter();
                DocumentUtil.writeNode(header, sw);
                headerXml=sw.toString();
            }
            return result;
        }
        finally
        {
            if( conn != null) conn.disconnect();
        }
    }
    private static final int TIMEOUT = 120000;

    /**
     * If the moveTo tag in the header has any URL, then
     * it will be passed to the client.
     * @return List of URL
     */
    public List getMoveToUrlList ()
    {
        List moveToUrlList = new ArrayList();

        /**
         * Check header is not empty to avoid Premature end of file
         * Exception.
         */
        if( StringUtilities.isBlank( this.headerXml ) == false )
        {
            /*
             *      If incase the header is going to have multiple URLs then the following can
             *      be used.
             *
             */
            Document docTemp;

            try
            {
                docTemp = DocumentUtil.makeDocument( this.headerXml );
            }
            catch( Exception e )
            {
                LOGGER.error("Error while prasing the headerXml",e);
                return moveToUrlList;
            }

            NodeList headerList = docTemp.getElementsByTagName( MOVE_TO);
            int len = headerList.getLength();
            for( int i = 0; i < len; i++ )
            {
                Element h = ( Element )headerList.item( i );
                for( Node child = h.getFirstChild(); child != null; child = child
                        .getNextSibling() )
                {
                    //child.getNodeName() should give you "URL"
                    if( child.getNodeType() == Node.ELEMENT_NODE )
                    {
                        String urlStr = child.getFirstChild().getNodeValue();
                        if (StringUtilities.isBlank(urlStr)==false)
                        {
                            moveToUrlList.add(urlStr);
                        }
                    }//ELEMENT NODE check
                }//url for loop
            }//moveTo forloop
        }//header if loop
        else
        {
            LOGGER.info("******* Soap Header is empty. Unable to get the moveTo URL *******");
        }
        return moveToUrlList;
    }
}
