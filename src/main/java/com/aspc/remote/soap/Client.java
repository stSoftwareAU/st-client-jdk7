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
package com.aspc.remote.soap;

import com.aspc.remote.database.NotFoundException;
import com.aspc.remote.database.SessionLoggedOutException;
import com.aspc.remote.jdbc.Executor;
import com.aspc.remote.jdbc.SoapResultSet;
import com.aspc.remote.soap.http.WebTransport;
import com.aspc.remote.soap.internal.SoapCallException;
import com.aspc.remote.soap.internal.SoapTransport;
import com.aspc.remote.util.crypto.CryptoUtil;
import com.aspc.remote.util.misc.*;
import com.aspc.remote.util.net.NetUrl;
import com.aspc.remote.util.net.NetUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import org.apache.commons.logging.Log;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *  Remote client allows remote data access to the server.
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED a client may only be accessed by one thread at a time.</i>
 *
 *  @author      Nigel Leck
 *  @since       29 November 2003
 */
public class Client implements Executor
{
    /**
     * The current version of the soap client
     */
    public static final String VERSION="$Revision: 1.133 $";//NOSYNC

    /**
     * Create a new SOAP client
     *
     * @param host The URL or list of URLs
     */
    public Client( final String host)
    {
        if( StringUtilities.isBlank( host)) throw new IllegalArgumentException("no host URL given");
        if( host.indexOf( ',') != -1)
        {
            StringTokenizer st= new StringTokenizer( host, ",");

            while( st.hasMoreTokens())
            {
                WebTransport ht = new WebTransport( st.nextToken());
                addTransport( ht);
            }
        }
        else
        {
            addTransport( new WebTransport( host));
        }
    }

    /**
     * Create a new SOAP client
     *
     * @param transport The internal transport
     */
    public Client( final SoapTransport transport)
    {
        addTransport( transport);
    }

    /**
     * by pass the discover phase
     * @param flag true if we should not call DISCOVER
     */
    public void setByPassDiscover( final boolean flag)
    {
        byPassDiscover=flag;
    }

    /**
     * add an additional transport
     *
     * @param transport The additional transport
     */
    public final void addTransport( final SoapTransport transport)
    {
        transports.add( transport);
    }

    /**
     * Which transport are we using ?
     *
     * @return The current transport.
     */
    public SoapTransport currentTransport()
    {
        if( current == null)
        {
            List tempTransportsList = getTransportList();
            int size = tempTransportsList.size();
            int pos = 0;

            /*
             * If the DISCOVER command returns URL list then
             * use the same sequence of URL. Otherwise it works
             * the same way as before.
             */
            if (isServerSequenceEnabled==false)
            {
                if( size != 1)
                {
                    /**
                     * a random number >= 0.0 and < 1.0
                     */
                    double random = Math.random();
                    pos = (int)( random * size);
                }
            }
            current = (SoapTransport)tempTransportsList.get( pos);

            // If we are changing the current transport then we should lose our state.
            stateID = null;
        }

        return current;
    }

    /**
     * Move to the next transport
     *
     * @return The next transport
     */
    public SoapTransport nextTransport()
    {
        if( current == null)
        {
            return currentTransport();
        }

        SoapTransport tempTransport = current;

        List tempTransportsList = getTransportList();

        int pos = tempTransportsList.indexOf( current);

        pos++;

        if( pos >= tempTransportsList.size())
        {
            pos = 0;
        }

        current = (SoapTransport)tempTransportsList.get( pos);

        if( current != tempTransport)
        {
            // only if we change the transport should we lose our state.
            stateID = null;
        }

        return current;
    }

    /**
     * The number of transports avaliable.
     *
     * @return The total number of transports.
     */
    public int getTransportCount()
    {
        List tempTransportList = getTransportList();

        if( tempTransportList == null)
        {
            return 0;
        }

        return tempTransportList.size();
    }

    /**
     * The current host.
     *
     * @return The host URI
     */
    public String getHost()
    {
        return currentTransport().getHost();
    }

    /**
     * discover commands
     * @param userId User Id
     * @param password Password
     *
     * @throws Exception a serious problem
     *
     */
    private void configTransports(final String userId, final String password) throws Exception
    {
        /*
         * If the current transport is not webtransport then
         * we don't have to do SSL login
         */
        if((currentTransport() instanceof WebTransport )==false)
        {
            return;
        }
        SoapResultSet rs;

        /*
         * if we requested that we by pass the discover phase then just return.
         */
        if( byPassDiscover)
        {
            return;
        }

        /*
         * Reset the flag so that getTransportList method will return the appropriate
         * list.
         */
        isLoginSuccessful = false;

        /*
         * Check is there any system variable set for by-pass of discover & moveTo.
         * need to call processForByPassUrl before calling byPassUrlEnabled method.
         * Whenever the by-pass is required the client app need to be restarted with
         * the system arg. This is purely for the developers use only just to by-pass
         * discover call and traffic communication.
         */
        processForByPassUrl(userId, password);

        if (isUrlByPassEnabled())
        {
            return;
        }

        /*
         * Reset the current transport to avoid using the previous transport if any.
         * Scenario, if the client app is already loged in and try to login again
         * then it will use the previous transport instead of using the user
         * configured URL.
         */
        this.current = null;
        this.discoverTransports.clear();

        rs = fetch( "DISCOVER SOAP");

        //IF WORKED
        if( rs.next())
        {
            isServerSequenceEnabled = true;
            int count =0;
            //THEN all HA functions are under server control
            do
            {
                //configure a transport
                String tempUrlStr = rs.getString("server_url");
                if (! StringUtilities.isBlank(tempUrlStr))
                {
                    count++;
                    addDiscoverTransport(tempUrlStr, userId, password);
                }
            }
            while( rs.next() );

            //Discover should return atleast one URL
            if (count==0)
            {
                StringBuilder buffer = new StringBuilder();
                buffer.append("***********************************************\n");
                buffer.append("********* DISCOVER return'd ").append(count).append(" records. ********\n");
                buffer.append("***********************************************\n");
                LOGGER.warn( buffer.toString());
            }
            else
            {
                LOGGER.info("DISCOVER return'd "+count +" urls.");
            }

            /*
             * reseting to null since the login should be using http/https protocol
             * returned by DISCOVER
             */
            this.current = null;
            this.sessionID = null;
        }
        else
        {
            //WORKS as is.
            LOGGER.info("Discover didn't return any URL, so use the existing transport.");
        }
    }

    /**
     * The database layer to connect to.
     *
     * @return The database layer
     */
    public String getLayer()
    {
        if( currentLayer == null)
        {
            return currentTransport().getDefaultLayer();
        }
        return currentLayer;
    }

    /**
     * The timezone of this executor
     * @return the timezone.
     */
    @Override
    public TimeZone getTimeZone()
    {
        if(currentTz == null)
        {
            currentTz = TimeZone.getDefault();
        }
        return currentTz;
    }

    /**
     * The login to connect with
     *
     * @return The database layer
     */
    @Override
    public String getUserName()
    {
        return login;
    }

    /**
     * Login using the defaults
     *
     * @throws Exception Failed to login.
     */
    public void login( ) throws Exception
    {
        SoapTransport transport = currentTransport();

        login(
            transport.getDefaultLogin(),
            transport.getDefaultPassword(),
            transport.getDefaultLayer()
        );
    }

    /**
     * Login using the defaults.
     *
     * @param login The user id
     * @param password The password.
     * @param layer The layer to login to.
     * @param loginContext The context.
     * @throws Exception Failed to login.
     */
    public void login(final String login, final String password, final String layer, final LoginContext loginContext) throws Exception
    {
        //Configure the transports using DISCOVER command before login
        configTransports(login, password);

        LoginContext tempContext = loginContext;

        if( tempContext == null)
        {
            TimeZone tz = TimeZone.getDefault();

            tempContext = new LoginContext(
                "" + tz.getRawOffset(),
                tz.getID(),
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                false,
                null,
                -1,
                -1
            );
        }

        for( int loop = 1; true; loop++)
        {
            try
            {
                singleLogin(login, password, layer, tempContext);
                isLoginSuccessful = true;

                return;
            }
            catch( Exception e)
            {
                String msg = e.getMessage();
                if( msg != null && msg.startsWith("unsupported content-encoding of"))
                {
                    SoapTransport trans = currentTransport();
                    if(trans.isCompressEnabled())
                    {
                        trans.disableCompress();
                        LOGGER.warn( "disabling compression");
                        continue;
                    }
                }
                /**
                 * If we have typed the wrong password or login then
                 * don't move to a new server.
                 */
                if( isPasswordException( e))
                {
                    throw e;
                }
                else if( loop < getTransportCount())
                {
                    LOGGER.warn( "Failed to log onto " + current + " trying next one", e);
                    // move to the next one.
                    nextTransport();
                }
                else
                {
                    throw e;
                }
            }
        }
    }

    /**
     * Login using the defaults                                                 <BR/>
     *
     * @param login The user id
     * @param password The password.
     * @param layer The layer to login to.
     * @param loginContext The context.
     * @throws Exception Failed to login.
     */
    private void singleLogin(final String login, final String password, final String layer, final LoginContext loginContext) throws Exception
    {
        logout();
//        if( StringUtilities.isBlank(layer) == false)
//        {
            currentLayer=layer;
//        }

        String iisAuthorization="";
        String iisEncryptionCode="";

        String protectedURL="";
        try
        {
            SoapResultSet rs = fetch( "DISCOVER SSO");

            //IF WORKED
            if( rs.next())
            {
                protectedURL = rs.getString( "protected_url");
            }
        }
        catch( Exception e)
        {
            LOGGER.warn( "could not detect SSO ", e);
        }

        if( StringUtilities.isBlank(protectedURL) == false)
        {
            DefaultHttpClient httpClient = new DefaultHttpClient();

            UsernamePasswordCredentials creds = new UsernamePasswordCredentials(login, password);
            AuthScope scope=new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, "Basic");
            httpClient.getCredentialsProvider().setCredentials(scope, creds);
            URL url = new URL( protectedURL);
            HttpHost targetHost = new HttpHost( url.getHost(), url.getPort(), url.getProtocol());
            AuthCache authCache = new BasicAuthCache();
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(targetHost, basicAuth);

            BasicHttpContext localContext = new BasicHttpContext();
            localContext.setAttribute("http.auth.auth-cache", authCache);

            HttpGet httpget = new HttpGet( url.toURI());

            HttpResponse response = httpClient.execute(targetHost, httpget, localContext);

            if( response.getStatusLine().getStatusCode() == 200)
            {
                InputStream in = response.getEntity().getContent();

                StringBuilder sb;
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(in))) 
                {
                    sb = new StringBuilder();
                    while( true)
                    {
                        String line = rd.readLine();
                        
                        if( line == null) break;
                        
                        if( sb.length() > 0 ) sb.append("\n");
                        
                        sb.append(line);
                    }
                }
               // LOGGER.info(sb);
                Document doc = DocumentUtil.makeDocument(sb.toString());
                NodeList list = doc.getElementsByTagName(Constants.AUTHENTICATOR_SECRET);

                if( list.getLength() > 0)
                {
                    Element secretElement = (Element)list.item(0);

                    if(secretElement != null)
                    {
                        iisAuthorization=secretElement.getTextContent();
                        iisEncryptionCode=secretElement.getAttribute("code");
                    }
                }
            }
        }
        Document envelope = createEnvelope();
        Element body = (Element)envelope.getElementsByTagName(Constants.SOAP_BODY).item(0);

        Element function = envelope.createElement(Constants.FUNCTION_LOGIN);
        body.appendChild(function);
        
        if( StringUtilities.isBlank(iisAuthorization))
        {
            Element username = envelope.createElement(Constants.LOGIN_PARAMETER_USERNAME);
            username.setTextContent(login);
            function.appendChild(username);
            
            Element passwordElement = envelope.createElement(Constants.LOGIN_PARAMETER_PASSWORD);
            passwordElement.setTextContent(password);
            function.appendChild(passwordElement);
        }
        else
        {         
            Element authorizationElement = envelope.createElement(Constants.LOGIN_AUTHORIZATION);
            authorizationElement.setTextContent(iisAuthorization);
            function.appendChild(authorizationElement);

            Element authorizationCodeElement = envelope.createElement(Constants.AUTHENTICATOR_SECRET_CODE);
            authorizationCodeElement.setTextContent(iisEncryptionCode);
            function.appendChild(authorizationCodeElement);
        }

        if( StringUtilities.isBlank(layer) == false)
        {
            Element layerElement = envelope.createElement(Constants.LOGIN_PARAMETER_SIGNATURE);
            layerElement.setTextContent(layer);
            function.appendChild(layerElement);
        }

        Element tzNameElement = envelope.createElement(Constants.LOGIN_PARAMETER_TIME_ZONE_NAME);
        tzNameElement.setTextContent(loginContext.getClientTimeZoneName());
        function.appendChild(tzNameElement);
        
        Element tzElement = envelope.createElement(Constants.LOGIN_PARAMETER_TIME_ZONE);
        tzElement.setTextContent(loginContext.getClientTimeZoneOffset());
        function.appendChild(tzElement);
        
        String language = loginContext.getLanguage();
        if( StringUtilities.isBlank( language))
        {
            language = System.getProperty("user.language");
        }
        
        Element languageElement = envelope.createElement(Constants.LOGIN_PARAMETER_LANGUAGE);
        languageElement.setTextContent(language);
        function.appendChild(languageElement);
        
        Element javaVersionElement = envelope.createElement(Constants.LOGIN_PARAMETER_JAVA_VERSION);
        javaVersionElement.setTextContent(System.getProperty("java.version"));
        function.appendChild(javaVersionElement);

        Element appNameElement = envelope.createElement(Constants.LOGIN_PARAMETER_APP_NAME);
        appNameElement.setTextContent(CUtilities.makeAppShortName());
        function.appendChild(appNameElement);

        Element versionElement = envelope.createElement(Constants.LOGIN_PARAMETER_SOAP_CLIENT);
        versionElement.setTextContent(VERSION);
        function.appendChild(versionElement);

        Document doc;
        doc = sendEnvelope( envelope, "/soap/action/dataSource");

        Document result = getResult( doc, "Result");

        sessionID = DocumentUtil.getNodeText(result, Constants.ELM_SESSION);

        if (StringUtilities.isBlank(sessionID)==false )
        {
            processMoveTo( login, password );
        }

        this.login = login;
        this.passwd = password;

        if( StringUtilities.isBlank( layer))
        {
            currentLayer = DocumentUtil.getNodeText(result, Constants.ELM_LAYER);
        }
        else
        {
            currentLayer = layer;
        }
    }

    /**
     * If the login soap call returns the moveTo URL in the header
     * then use that URL for the subsequent communication.
     * @param login login id
     * @param password password
     * @throws Exception If the moveTo is not available.
     */
    private void processMoveTo( final String login, final String password ) throws Exception
    {
        if( CProperties.isDisabled( Constants.DISABLE_MOVE_TO))
        {
            return;
        }

        if( this.current instanceof WebTransport )
        {
            /*
             * If the by-pass is not enabled then get moveToUrl. Otherwise
             * use the URL set in the by-pass
             */
            if (isUrlByPassEnabled()==false)
            {
                WebTransport tempTran = ( WebTransport )this.current;
                List moveToList = tempTran.getMoveToUrlList();
                /*
                 * If the moveToList is empty then throw exception
                 */
                if( moveToList != null && moveToList.size() > 0 )
                {
                    // Reset the login
                    LOGGER.info( "Login successful. Now switching to moveTo url" );
                    this.current = null;

                    addMoveToUrlTransport( moveToList, login, password );
                }
            }
        }//instanceof if loop
    }

    /**
     * Login using the values passed.
     *
     * @param login The user id
     * @param password The password.
     * @param layer The layer to login to.
     * @throws Exception Failed to login.
     */
    public void login( final String login, final String password, final String layer) throws Exception
    {
        login(
            login,
            password,
            layer,
            null
        );
    }

    /**
     * Logout.
     *
     * @throws Exception A serious problem
     */
    public void logout() throws Exception
    {
        try
        {
            if( StringUtilities.isBlank( sessionID))
            {
                return;
            }

            Document envelope = createEnvelope();

            Element body = (Element)envelope.getElementsByTagName(Constants.SOAP_BODY).item(0);

            Element function = envelope.createElement("logout");
            body.appendChild(function);

            sendEnvelope( envelope, "/soap/action/dataSource");
        }
        finally
        {
            sessionID = null;
            login = null;
        }
    }

    /**
     * Is the client currently stateless ?
     *
     * @return true if stateless.
     */
    @Override
    public boolean isStateless()
    {
       return StringUtilities.isBlank( stateID);
    }

    /**
     * Return details of this client
     *
     * @return the client details
     */
    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        String tempLogin = login;
        String tempLayer = currentLayer;

        SoapTransport transport = currentTransport();
        if( StringUtilities.isBlank( tempLogin))
        {
            tempLogin = transport.getDefaultLogin();
        }

        if( StringUtilities.isBlank( tempLayer))
        {
            tempLayer = transport.getDefaultLayer();
        }

        String temp = tempLogin + "@" + transport.getHost() + "/" + tempLayer;

        return temp;
    }

    /**
     * Updates a file with a new version
     * @param fileKey the global key of the file
     * @param newVersion the new version of the file
     * @throws Exception update failure
     */
    public void updateFile( final String fileKey, final File newVersion ) throws Exception
    {
        submitFile( fileKey, newVersion, TYPE_UPDATE, null );
    }

    /**
     * Updates a file with a new version
     * @param fileKey the global key of the file
     * @param newVersion the new version of the file
     * @param location the location to update
     * @throws Exception update failure
     */
    public void updateFile( final String fileKey, final File newVersion, final String location ) throws Exception
    {
        submitFile( fileKey, newVersion, TYPE_UPDATE, location );
    }

    /**
     * Checks in a file
     * @param fileName the name of the file
     * @param file the file to check in
     * @return String the file key
     * @throws Exception check in failure
     */
    public String checkInFile( final String fileName, final File file ) throws Exception
    {
        return submitFile( fileName, file, TYPE_CHECKIN, null );
    }

    /**
     * Checks in a file
     * @param fileName the name of the file
     * @param file the file to check in
     * @param location the location to create the file on
     * @return String the file key
     * @throws Exception check in failure
     */
    public String checkInFile( final String fileName, final File file, final String location ) throws Exception
    {
        return submitFile( fileName, file, TYPE_CHECKIN, location );
    }

    /**
     * Submit a file.
     *
     * @param fileName The target file name.
     * @param sourceFile The file to check in.
     * @param type check in or update
     * @param location location
     * @return String the global key of the file
     * @throws Exception A serious problem
     */
    private String submitFile( final String fileName, final File sourceFile, final String type, final String location ) throws Exception
    {
        if( sourceFile.exists() == false)
        {
            throw new Exception( "missing source file");
        }

        boolean checkin = type.equals( TYPE_CHECKIN );
        if( !checkin && type.equals( TYPE_UPDATE ) == false )
        {
            throw new Exception( "unknown submit type" );
        }

        String size = ""+sourceFile.length();
        byte[] raw = FileUtil.generateCheckSum( sourceFile );

        /* compress the file */
        File deflate = File.createTempFile( "deflate", "tmp", sourceFile.getParentFile());
        FileUtil.compressFile( sourceFile, deflate );

        byte[] fkey = CryptoUtil.generateKeyAES();

        /* aes init vector */
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes( iv );

        /* encrypt the file */
        File encrypt = File.createTempFile( "encrypt", "tmp", deflate.getParentFile() );
        CryptoUtil.encryptFile( deflate, encrypt, fkey, iv );

        try
        {
            /* checksum */
            byte[] chk = FileUtil.generateCheckSum( encrypt );

            SoapResultSet srs;

            String cmd = "file ";

            if( checkin )
            {
                cmd += " create "+fileName;
            }
            else
            {
                cmd += " update "+fileName; // actually file key
            }

            if( !StringUtilities.isBlank( location ) )
            {
                cmd += " location "+location;
            }

            srs = fetch( cmd );

            String key;
            String tmpName;
            if( srs.next() )
            {
                key = srs.getString( "file_key" );
                tmpName = srs.getString( "temporary_name" );
                assert tmpName!=null;
            }
            else
            {
                throw new Exception( "file create failed." );
            }

            srs = srs.nextResultSet();
            assert srs!=null;
            String url;
            String code=null;
            while( srs.next() )
            {
                // try to upload file.
                url = srs.getString( "url" );
                assert url!=null;
                code = srs.getString( "code" );
                try
                {
                    NetUtil.sendData( encrypt, url, tmpName );
                    break; // file was uploaded
                }
                catch( Exception e )
                {
                    LOGGER.warn( "soap client:", e );
                    fetch( "log_filelocation_error '"+e.getMessage()+"' location "+code+" file "+key );

                    if( srs.isLast() )
                    {
                        throw e;
                    }
                 //   else
                //    {
                 //       continue; // try next URL
                 //   }
                }
            }

            String sCipher = new String( StringUtilities.encodeBase64(fkey), "ascii" );
            String sInit = new String( StringUtilities.encodeBase64(iv), "ascii" );
            String sRaw = new String( StringUtilities.encodeBase64(raw), "ascii" );
            String checksum = new String( StringUtilities.encodeBase64(chk), "ascii" );
            String locationParameter;

            locationParameter = " LOCATION " + code + " MIME " +
                    FileUtil.adjustMimeType(FileUtil.MIME_APPLICATION_OCTET_STREAM, sourceFile, null);

            fetch(
                "FILE submit "+key+" gzip " +
                " cipher "+sCipher+" ivec "+sInit +
                " checksum "+checksum+" raw_checksum "+sRaw+" filesize "+size +
                locationParameter
            );
            return key;
        }
        finally
        {
            if( deflate.exists()) deflate.delete();
            if( encrypt != null) encrypt.delete();
        }
    }

    /**
     * gets and locks a file from the server.
     *
     * @param fileName The actual file to check out.
     * @throws Exception A serious problem
     * @return The file.
     */
    public File checkOutFile( final String fileName) throws Exception
    {
        return fetchFile( fileName, TYPE_CHECKOUT, null );
    }

    /**
     * Gets and locks a file from the server
     * @param fileName the name of the file to checkout
     * @param location the location to get the file from
     * @return File the file
     * @throws Exception failure to get file or lock
     */
    public File checkOutFile( final String fileName, final String location ) throws Exception
    {
        return fetchFile( fileName, TYPE_CHECKOUT, location );
    }

    /**
     * gets a file from the server for READ access.
     *
     * @param fileName The actual file name
     * @throws Exception A serious problem
     * @return The raw file.
     */
    public File readFile( final String fileName) throws Exception
    {
        return fetchFile( fileName, TYPE_READ, null );
    }

    /**
     * Gets a file from the server for READ access, using the specified location
     * @param fileName the name of the file to get
     * @param location the location to get the file from
     * @return File the file
     * @throws Exception a failure to get the file
     */
    public File readFile( final String fileName, final String location ) throws Exception
    {
        return fetchFile( fileName, TYPE_READ, location );
    }

    /**
     * fetch a SQL statement as a record set.
     *
     * @param sql The SQL to execute
     * @throws Exception A serious problem
     * @return The record set
     */
    @Override
    public SoapResultSet fetch( final String sql) throws Exception
    {
        Document result;

        result = execute( sql);

        SoapResultSet rs = new SoapResultSet( result, 0, this);

        return rs;
    }

    /**
     * execute the commands on a
     * @param sql the commands
     * @return the result document.
     * @throws Exception a serious problem
     */
    public Document farm( final String sql) throws Exception
    {
        Document envelope = createEnvelope();

        Element body = (Element)envelope.getElementsByTagName(Constants.SOAP_BODY).item(0);

        Element farmElement = envelope.createElement(Constants.ELM_FARM);
        body.appendChild(farmElement);

        boolean invalidXMLChar = StringUtilities.checkXML(sql) == false;
        
        if( invalidXMLChar)
        {
            String value = StringUtilities.encodeUTF8base64( sql);
            Element esqlElement = envelope.createElement(Constants.ELM_ESQL);
            farmElement.appendChild(esqlElement);
            esqlElement.setTextContent( value);
        }
        else
        {
            Element sqlElement = envelope.createElement(Constants.ELM_SQL);
            farmElement.appendChild(sqlElement);
            sqlElement.setTextContent( sql);
        }

        /*
         * unique id which will be used to join the commands on the server side.
         */
        Element messageIdElement = envelope.createElement(Constants.POST_PARAMETER_MESSAGE_ID);
        body.appendChild(messageIdElement);

        messageIdElement.setTextContent( Long.toString( MESSAGE_ID.incrementAndGet()));

        Document doc;
        doc = sendEnvelope( envelope, "/soap/action/commander");

        Document result = getResult( doc, "result");

        NodeList headerList = doc.getElementsByTagName( Constants.SOAP_HEADER);

        int len = headerList.getLength();

        for( int i = 0; i < len; i++)
        {
            Element h = (Element)headerList.item( i);

            NodeList idList = h.getElementsByTagName( "ns1:TxId");

            if( idList.getLength() > 0)
            {
                Element txIdElement = (Element)idList.item( 0);

                Element idElement = (Element)txIdElement.getElementsByTagName( "id").item(0);
                stateID = idElement.getNodeValue();

                if( stateID == null)
                {
                    Node textNode = idElement.getFirstChild();

                    if( textNode != null)
                    {
                        stateID = textNode.getNodeValue();
                    }
                }
            }
        }

        return result;
    }

    /**
     * Execute a SQL statement. <BR>
     *
     * if an error occurs and we have multiple connections check that
     * the current server is OK by sending a very simple command. If that
     * fails then we will declare the server dead and move onto the next server.
     * If we aren't currently in state and the simplest of commands have failed then
     * we will retry the command without notifying the user                                                                             <br>
     *                                                                                                                                  <br>
     * This is a SINGLE THREADED class but... as people are accidently calling it from multiple threads we should protect ourselves by
     * adding a synchronized.
     *
     * @param sql The SQL/XML statement
     * @throws Exception A serious problem
     * @return The XML result document
     */
    @Override
    public synchronized Document execute( final String sql) throws Exception
    {
        boolean repeatable = isStateless();

        /*
         * Added for loop to re-try with all the available
         * transport if there is any exception.
         */
        for( int loop = 1; true; loop++)
        {
            try
            {
                return iExecute( sql);
            }
            catch( SessionLoggedOutException sloe)
            {
                if( repeatable == false)
                {
                    throw sloe;
                }

                login(login, passwd, currentLayer);
            }
            catch( Exception e )
            {
                LOGGER.warn( sql, e);
                boolean switchedServer = false;

                /*
                 * If there are more transports available then
                 * re-try with the next available transport.
                 */
                int transportCount = getTransportCount();
                if( loop < transportCount)
                {
                    try
                    {
                        iExecute( "SLEEP 0");
                    }
                    catch( Exception e2 )
                    {
                        LOGGER.warn(
                            "Server '" +
                            current.getHost() +
                            "' is not responding so switching to the next available transport."
                        );
                        // change servers and try with again.
                        nextTransport();
                        switchedServer = true;
                    }// catch block
                }// getTransportCount check

                /*
                 * If we aren't currently in state and we have moved to a new server because the
                 * simplist of all command SLEEP 0 failed then we can repeat the request without notifing the calling
                 * program but in general we must throw the exception back to the calling program.
                 */
                if( switchedServer && repeatable)
                {
                    continue;
                }

                throw e;
            }//Catch block
        }//for loop
    }
    
    
    private Document iExecute( final String sql) throws Exception
    {
        Document envelope = createEnvelope();

        Element body = (Element)envelope.getElementsByTagName(Constants.SOAP_BODY).item(0);
        Element executeElement = envelope.createElement(Constants.ELM_EXECUTE);
        body.appendChild(executeElement);

        boolean invalidXMLChar = StringUtilities.checkXML(sql) == false;       

        if( invalidXMLChar)
        {
            String value = StringUtilities.encodeUTF8base64( sql);
            Element esqlElement = envelope.createElement(Constants.ELM_ESQL);
            executeElement.appendChild(esqlElement);
            esqlElement.setTextContent( value);
        }
        else
        {
            Element sqlElement = envelope.createElement(Constants.ELM_SQL);
            executeElement.appendChild(sqlElement);
            sqlElement.setTextContent( sql);
        }

        /*
         * unique id which will be used to join the commands on the server side.
         */
        Element messageIdElement = envelope.createElement(Constants.POST_PARAMETER_MESSAGE_ID);
        body.appendChild(messageIdElement);
        messageIdElement.setTextContent( Long.toString( MESSAGE_ID.incrementAndGet()));

        Document doc;
        doc = sendEnvelope( envelope, "/soap/action/commander");

        Document result = getResult( doc, "result");

        NodeList headerList = doc.getElementsByTagName( Constants.SOAP_HEADER);

        int len = headerList.getLength();

        for( int i = 0; i < len; i++)
        {
            Element h = (Element)headerList.item( i);

            NodeList idList = h.getElementsByTagName( "ns1:TxId");

            if( idList.getLength() > 0)
            {
                Element txIdElement = (Element)idList.item( 0);

                Element idElement = (Element)txIdElement.getElementsByTagName( "id").item(0);
                stateID = idElement.getNodeValue();

                if( stateID == null)
                {
                    Node textNode = idElement.getFirstChild();

                    if( textNode != null)
                    {
                        stateID = textNode.getNodeValue();
                    }
                }
            }
        }

        return result;
    }

    /**
     * &lt;?xml version="1.0" encoding="UTF-8"?&lt;
     * &lt;soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"&lt;
     *  &lt;soapenv:Header&lt;
     *   &lt;SessionInfo soapenv:actor="http://schemas.xmlsoap.org/soap/actor/next" soapenv:mustUnderstand="0" xmlns=""&lt;
     *    &lt;SESSION_SIGNATURE xmlns=""&lt;self_test&lt;/SESSION_SIGNATURE&lt;
     *    &lt;SESSION_TZ xmlns=""&lt;Australia/Sydney&lt;/SESSION_TZ&lt;
     *   &lt;/SessionInfo&lt;
     * &lt;/soapenv:Header&lt;
     *  &lt;soapenv:Body&lt;
     *   &lt;execute xmlns=""&lt;
     *    &lt;sql&lt;DISCOVER SSO&lt;/sql&lt;
     *   &lt;/execute&lt;
     *   &lt;messageID xmlns=""&lt;1&lt;/messageID&lt;
     *  &lt;/soapenv:Body&lt;
     * &lt;/soapenv:Envelope&lt;
     *
     * 
     * @return the value
     * @throws Exception a serious problem.
     */
    public Document createEnvelope() throws Exception
    {
        Document doc=DocumentUtil.newDocument();
        Element elementEnvelope = doc.createElement("soapenv:Envelope");
        doc.appendChild(elementEnvelope);
        
        //elementEnvelope.setAttribute( "xmlns:SOAPZ-ENV", "http://schemas.xmlsoap.org/soap/envelope/");
        elementEnvelope.setAttribute("xmlns:soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
        elementEnvelope.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
        elementEnvelope.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        
        Element headerElement = doc.createElement(Constants.SOAP_HEADER);
        elementEnvelope.appendChild(headerElement);
        
        Element bodyElement = doc.createElement(Constants.SOAP_BODY);
        elementEnvelope.appendChild(bodyElement);
         
        return doc;
    }

    
    /**
     * Add the session header.
     * &lt;?xml version="1.0" encoding="UTF-8"?&lt;
     * &lt;soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"&lt;
     *  &lt;soapenv:Header&lt;
     *   &lt;SessionInfo soapenv:actor="http://schemas.xmlsoap.org/soap/actor/next" soapenv:mustUnderstand="0" xmlns=""&lt;
     *    &lt;SESSION_SIGNATURE xmlns=""&lt;self_test&lt;/SESSION_SIGNATURE&lt;
     *    &lt;SESSION_TZ xmlns=""&lt;Australia/Sydney&lt;/SESSION_TZ&lt;
     *   &lt;/SessionInfo&lt;
     * &lt;/soapenv:Header&lt;
     *  &lt;soapenv:Body&lt;
     *   &lt;execute xmlns=""&lt;
     *    &lt;sql&lt;DISCOVER SSO&lt;/sql&lt;
     *   &lt;/execute&lt;
     *   &lt;messageID xmlns=""&lt;1&lt;/messageID&lt;
     *  &lt;/soapenv:Body&lt;
     * &lt;/soapenv:Envelope&lt;
     *
     * @param envelope The envelope to add the session header to.
     * @throws Exception A serious problem
     */
    protected final void addSessionHeader( final Document envelope) throws Exception
    {
        Element headerElement=(Element)envelope.getElementsByTagName( Constants.SOAP_HEADER).item(0);
        
        if( headerElement == null)
        {
            headerElement = envelope.createElement(Constants.SOAP_HEADER);
            envelope.getDocumentElement().appendChild(headerElement);
        }
        Element sessionInfoElement = envelope.createElement(Constants.SOAPENV_HEADER_SESSION_INFO);
        headerElement.appendChild(sessionInfoElement);
        sessionInfoElement.setAttribute("soapenv:actor", "http://schemas.xmlsoap.org/soap/actor/next");
        sessionInfoElement.setAttribute("soapenv:mustUnderstand", "0");
        
        Element sessionSignatureElement = envelope.createElement(Constants.SOAPENV_HEADER_SESSION_TAG_LAYER);
        sessionInfoElement.appendChild(sessionSignatureElement);
        sessionSignatureElement.setTextContent(getLayer());
        
        Element sessionTimeZoneElement = envelope.createElement(Constants.SOAPENV_HEADER_SESSION_TAG_TZ);
        sessionInfoElement.appendChild(sessionTimeZoneElement);
        sessionTimeZoneElement.setTextContent(getTimeZone().getID());
        
        if( StringUtilities.notBlank( sessionID))
        {
            Element sessionUsernameElement = envelope.createElement(Constants.SOAPENV_HEADER_SESSION_TAG_USERNAME);
            sessionInfoElement.appendChild(sessionUsernameElement);
            sessionUsernameElement.setTextContent(login);

            Element sessionIdElement = envelope.createElement(Constants.SOAPENV_HEADER_SESSION_TAG_ID);
            sessionInfoElement.appendChild(sessionIdElement);
            sessionIdElement.setTextContent(sessionID);
        }
    }

    /**
     * Convert a XML document into a record set.
     *
     * @param doc The XML document
     * @param tag The tag to fetch
     * @throws Exception A serious problem
     * @return The result set
     */
    protected Document getResult( final Document doc, final String tag) throws Exception
    {
        /*
         * &lt;soapenv:Envelope
         *      soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"
         *      xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
         * &lt;
         *     &lt;soapenv:Body&lt;
         *        &lt;soapenv:Fault&lt;
         *            &lt;faultcode&lt;com.aspc.remote.database.NotFoundException&lt;/faultcode&lt;
         *            &lt;faultstring&lt;com.aspc.remote.database.NotFoundException: Not Found&lt;/faultstring&lt;
         *        &lt;/soapenv:Fault&lt;
         *     &lt;/soapenv:Body&lt;
         * &lt;/soapenv:Envelope&lt;
         */
        NodeList faultList = doc.getElementsByTagName( "soapenv:Fault");

        Node faultNode = null;
        
        if( faultList.getLength()>0)
        {
            faultNode=faultList.item( 0);
        }

        if( faultNode != null)
        {
            /*
             * Ok we have a problem.
             */
            Node child = faultNode.getFirstChild();
            String  code = null,
                    reason = null;

            while( child != null && ( code == null || reason == null))
            {
                String name = child.getNodeName();

                if( "faultcode".equalsIgnoreCase(name))
                {
                    code = child.getNodeValue();
                    if( code == null)
                    {
                        Node sub = child.getFirstChild();

                        if( sub != null)
                        {
                            code = sub.getNodeValue();
                        }
                    }
                }
                else if( "faultstring".equalsIgnoreCase( name))
                {
                    reason = child.getNodeValue();
                    if( reason == null)
                    {
                        Node sub = child.getFirstChild();

                        if( sub != null)
                        {
                            reason = sub.getNodeValue();
                        }
                    }
                }

                child = child.getNextSibling();
            }

            handleFault( code, reason);
        }
        stateID = null;
        NodeList nl = doc.getElementsByTagName( tag);

        Node node = nl.item( 0);

        String value = "";
        if( node != null)
        {
            Node child = node.getFirstChild();

            if( child != null)
            {
                value = child.getNodeValue();
            }
        }

        if( value == null)
        {
            throw new Exception( "no result in " + DocumentUtil.docToString( doc));
        }
        Document result = DocumentUtil.makeDocument( value);
        DocumentUtil.normaliseDocument(result);

        return result;
    }

    /**
     * Handle a fault.
     *
     * @param code The code
     * @param reason The reason
     * @throws Exception A serious problem
     */
    protected void handleFault( final String code, final String reason) throws Exception
    {
        switch (code) 
        {
            case "com.aspc.remote.database.NotFoundException":
                throw new NotFoundException( reason);
            case "com.aspc.remote.database.SessionLoggedOutException":
                throw new SessionLoggedOutException( reason);
            default:
                throw new SoapCallException( code,  reason);
        }
    }

    /**
     * send the envelope.
     * 
     * &lt;?xml version="1.0" encoding="UTF-8"?>
     * &lt;soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
     *  &lt;soapenv:Header>
     *   &lt;SessionInfo soapenv:actor="http://schemas.xmlsoap.org/soap/actor/next" soapenv:mustUnderstand="0" xmlns="">
     *    &lt;SESSION_SIGNATURE xmlns="">self_test&lt;/SESSION_SIGNATURE>
     *    &lt;SESSION_TZ xmlns="">Australia/Sydney&lt;/SESSION_TZ>
     *   &lt;/SessionInfo>
     * &lt;/soapenv:Header>
     *  &lt;soapenv:Body>
     *   &lt;execute xmlns="">
     *    &lt;sql>DISCOVER SSO&lt;/sql>
     *   &lt;/execute>
     *   &lt;messageID xmlns="">1&lt;/messageID>
     *  &lt;/soapenv:Body>
     * &lt;/soapenv:Envelope>
     *
     * @param envelope The envelope
     * @param relativePath The path to send it to
     * @throws Exception A serious problem
     * @return The XML document.
     */
    public final Document sendEnvelope( final Document envelope, final String relativePath) throws Exception
    {
        addSessionHeader( envelope);

        SoapTransport transport = currentTransport();

        return transport.sendEnvelope( envelope, relativePath);
    }

    /**
     * Uses the SOAP FILE commands to download a file.
     * @param fileName the path of the file to fetch
     * @param type checkout or readonly
     * @param location the location to use
     * @return File the downloaded file
     * @throws Exception failure to download the file
     */
    @SuppressWarnings("empty-statement")
    private File fetchFile( final String fileName, final String type, final String location ) throws Exception
    {
        boolean checkout = type.equals( TYPE_CHECKOUT );
        boolean gotLock = false;

        if( !checkout && !type.equals( TYPE_READ ) )
        {
            throw new Exception( "Unsupported fetch type: "+type );
        }

        String tempName = StringUtilities.replace( fileName, "'", "\\'");
        SoapResultSet srs = fetch( "FILE INFO PATH '" + tempName + "' columns isGzip" );
        String key = "";
        String gzip;
        if( srs.next() )
        {
            key = srs.getString( "key" );
            gzip = srs.getString( "isGzip" );
        }
        else
        {
            throw new Exception( "file '" + tempName + "' not found" );
        }

        StringBuilder fetchCmd = new StringBuilder( 50);
        fetchCmd.append("FILE FETCH ").append( key);

        if( checkout )
        {
            fetchCmd.append( " LOCKFILE " );
        }

        if( StringUtilities.isBlank( location ) == false )
        {
            fetchCmd.append(" LOCATION ").append( location);
        }

        srs = fetch( fetchCmd.toString() );

        String fname;
        String chksum;
        String cipher;
        String init;
        String raw;
        if( srs.next() )
        {
            if( checkout )
            {
                String lockMode = srs.getString( "lock_mode" );
                assert lockMode!=null;
                gotLock = lockMode.equals( "LOCKED" );
                if( !gotLock )
                {
                    throw new Exception( "Could not obtain a lock on the file "+fileName );
                }
            }
            fname = srs.getString( "file_name" );
            assert fname!=null;
            chksum = srs.getString( "checksum" );
            cipher = srs.getString( "cipher_key" );
            init = srs.getString( "init_vector" );
            raw = srs.getString( "raw_checksum" );
        }
        else
        {
            throw new Exception( "file fetch "+key+" returned no records" );
        }

        srs = srs.nextResultSet();
        assert srs!=null;
        File file = null;
        String url;
        while( srs.next() )
        {
            url = srs.getString( "url" );
            assert url!=null;
            String store = srs.getString( "code" );
            try
            {
                if( cacheDirectory == null || StringUtilities.isBlank( cacheDirectory) )
                {
                    file = NetUtil.fetchData( url, fname, false, chksum );
                }
                else
                {
                    File cacheFile;
                    cacheFile = new File( cacheDirectory+fname+"_" + Long.toHexString(System.currentTimeMillis()) );

                    NetUtil.retrieveData( url, fname, cacheFile, false, chksum);
                }

                break; // we have the file, no more attempts neccessary
            }
            catch( Exception e )
            {
                // log_filelocation_error
                fetch("log_filelocation_error '"+e.getMessage()+"' location "+store+" file "+key);

                if( srs.isLast() )
                {
                    if( gotLock )
                    {
                        execute( "FILE UNLOCK "+key );
                        gotLock = false;
                    }
                    throw e;
                }
                else
                {
                    ; // ignore the error and try the next file store.
                }
            }
        }

        if( file == null )
        {
            if( gotLock )
            {
                execute( "FILE UNLOCK "+key );
            }
            throw new Exception( "unable to fetch file" );
        }

        /* Decrypt the file if cipher key and init vector are present */
        if( cipher!=null && init!=null && StringUtilities.isBlank( cipher ) == false && StringUtilities.isBlank( init ) == false )
        {
            try
            {
                byte[] c = StringUtilities.decodeBase64( cipher.getBytes("ascii") );
                byte[] i = StringUtilities.decodeBase64( init.getBytes("ascii") );
                File tmp = File.createTempFile( "decrypt", "tmp", FileUtil.makeQuarantineDirectory());
                CryptoUtil.decryptFile( file, tmp, c, i );
                if( tmp.exists() )
                {
                    file = tmp;
                }
            }
            catch( Exception e )
            {
                if( gotLock )
                {
                    execute( "FILE UNLOCK "+key );
                    gotLock = false;
                }
                throw e;
            }
        }

        /* decompress the file if gzip flag is set */
        if( gzip!=null && StringUtilities.isBlank( gzip ) == false && gzip.equals( "true" ) )
        {
            try
            {
                File tmp = File.createTempFile( "inflate", "tmp", FileUtil.makeQuarantineDirectory());
                FileUtil.decompressFile( file, tmp );
                if( tmp.exists() )
                {
                    if( StringUtilities.isBlank( raw ) == false )
                    {
                        FileUtil.validate( tmp, raw, -1 );
                    }
                    file = tmp;
                }
            }
            catch( Exception e )
            {
                if( gotLock )
                {
                    execute( "FILE UNLOCK "+key );
                }
                throw e;
            }
        }

        return file;
    }
    /**
     * Returns session ID.
     * @return session ID
     */
    public String getSessionID()
    {
        return sessionID;
    }

    /**
     * The cache directory is used by the file fetch operations. If this is not set
     * the client will use the default cache directory.
     * @return String the cache directory, null if not set
     */
    public String getCacheDiretcory()
    {
        return cacheDirectory;
    }

    /**
     * Sets the cache directory to be used during fetch operations. Setting the
     * cache directory to null will cause the client to use the default system
     * cache directory.
     * @param path the path of the cache directory, can be null
     */
    public void setCacheDirectory( final String path )
    {
        cacheDirectory = path;
    }

    /**
     * Add an additional transport. This is  an extension
     * for SSL project.
     *
     * @param transportUrl The additional transport
     * @param userId user id
     * @param password password
     */
    public final void addDiscoverTransport( final String transportUrl, final String userId, final String password)
    {
        /*
         * Since the transport URL has the protocol, cannot append the userId & password
         * in the URL itself like how it used to work (abc@123:localhost:8080/citi). So created 3 param constructor.
         */
        WebTransport tempObj = new WebTransport(transportUrl, userId, password);
      //  if( tempObj != null )
     //   {
            discoverTransports.add( tempObj);
     //   }
     //   else
      //  {
      //      LOGGER.error("Error while creating WebTransport for URL:"+transportUrl);
      //  }
    }

    /**
     * Add move to URL transport.
     * @param transportUrl URL
     * @param userId user id
     * @param password password
     */
    public final void addMoveToUrlTransport( final List transportUrl, final String userId, final String password)
    {
        int size = (transportUrl==null)?0:transportUrl.size();
        for (int i=0; i<size; i++)
        {
            @SuppressWarnings("null")
            String tempUrl = (String)transportUrl.get(i);
            LOGGER.info("moveTo URL "+(i+1)+". "+tempUrl);
            WebTransport tempObj = new WebTransport(tempUrl, userId, password);
          //  if( tempObj != null )
       //     {
                moveToUrlTransports.add( tempObj);
         //   }
         //   else
         //   {
         //       LOGGER.error("Error at addMoveToUrlTransport(...) while creating WebTransport for URL:"+transportUrl);
         //   }
        }//for loop
    }

    /**
     * To return the list of transport. If there is no
     * transport returned from the server then it returns
     * the initial configured list of transport.
     *
     * @return list of transport
     */
    private List getTransportList()
    {
        /*
         * If the URL by-pass is enabled then use the URL mentioned
         * in the system property. which basically means by-pass
         * discover and moveTo calls.
         */
        if (isUrlByPassEnabled())
        {
            if (isLoginSuccessful)
            {
                LOGGER.info("^^^ By-Pass enabled, returning By-Pass Http Transport ^^^");
                return Collections.unmodifiableList(this.byPassTrafficTransportList);
            }
            else
            {
                if( this.byPassLoginTransportList == null
                        || this.byPassLoginTransportList.isEmpty() )
                {
                    LOGGER.info("^^^ By-Pass enabled, doesn't have HTTPS configured so returning Http Transport to Login ^^^");
                    return Collections.unmodifiableList(byPassTrafficTransportList);
                }
                else
                {
                    LOGGER.info("^^^ By-Pass enabled, returning By-Pass Https Transport to Login ^^^");
                    return Collections.unmodifiableList(byPassLoginTransportList);
                }
            }
        }

        /**
         * After login we shouldn't be using the discover transports.
         * Should always use moveToUrl. If incase the moveToUrl is empty then
         * probably we can return the original transport set by the client
         * application
         */
        if (isLoginSuccessful)
        {
            if (moveToUrlTransports.isEmpty())
            {
                LOGGER.info("*** MoveToUrl transport is empty, so returning original transports. ***");
                return Collections.unmodifiableList(transports);
            }
            else
            {
                return Collections.unmodifiableList(moveToUrlTransports);
            }
        }
        /**
         * If the discoverTransports is empty then use
         * the original list(s) of URL set in the
         * configured.
         */
        if (this.discoverTransports == null || this.discoverTransports.isEmpty())
        {
            return Collections.unmodifiableList(transports);
        }
        else
        {
            return Collections.unmodifiableList(discoverTransports);
        }
    }

    /**
     * set the layer
     * @param layer the signature
     */
    public void setLayer( final String layer)
    {
        currentLayer = layer;
    }

    /**
     * set the time zone
     * @param tz the time zone
     */
    public void setTimeZone(final TimeZone tz)
    {
        currentTz = tz;
    }

    /**
     * Method checks the system property for URL by-pass. If
     * any thing set then set the URL's to the member variable
     * and return true. This method will be called only when it is
     * for WebTransport
     *
     * @param userId user ID
     * @param password password
     *
     * @throws Exception If the HTTPS URL is set and HTTP is not set
     */
    private void processForByPassUrl(final String userId, final String password) throws Exception
    {
        String httpUrl = System.getProperty( BY_PASS_TRAFFIC );
        String httpsUrl = System.getProperty( BY_PASS_LOGIN );
        /*
         * If both are empty then use the regular process.
         */
        if( StringUtilities.isBlank( httpsUrl )
                && StringUtilities.isBlank( httpUrl ) )
        {
            this.byPassUrlEnabled = false;
        }
        else
        {
            /*
             * If the https(LOGIN) is not empty then http(TRAFFIC) is mandatory.
             */
            if( StringUtilities.isBlank( httpsUrl ) == false
                    && StringUtilities.isBlank( httpUrl ) )
            {
                throw new Exception(
                        BY_PASS_TRAFFIC
                                + " Tag is missing or value is empty. Traffic By-pass URL is Mandatory." );
            }

            if (StringUtilities.isBlank(httpsUrl)==false)
            {
                if (httpsUrl.startsWith(NetUrl.HTTP_PROTOCOL))
                {
                    throw new Exception("'"
                                    + BY_PASS_LOGIN
                                    + "' protocol should be 'https' not http. "
                                    + "If SSL login is not required then set '"
                                    + BY_PASS_TRAFFIC + "' param alone." );
                }

                this.byPassLoginTransportList = new ArrayList();
                WebTransport tempHttpsTransport = new WebTransport( httpsUrl,
                        userId, password );
                this.byPassLoginTransportList.add( tempHttpsTransport );
            }

            if (httpUrl.startsWith(NetUrl.HTTPS_PROTOCOL))
            {
                throw new Exception ("Please check, '"+BY_PASS_TRAFFIC + "' protocol should NOT be 'https'.");
            }

            this.byPassTrafficTransportList = new ArrayList();
            WebTransport tempHttpTransport = new WebTransport(httpUrl, userId, password);
            this.byPassTrafficTransportList.add(tempHttpTransport);

            this.byPassUrlEnabled = true;

            //reset the current transport so that the login will use the
            //system specified one.
            this.current=null;
        }
    }

    /**
     * Returns the flag if the URL by-pass is set or not.
     * @return True if enabled.
     */
    public boolean isUrlByPassEnabled()
    {
        return this.byPassUrlEnabled;
    }

   /**
     * To check the exception is critical for the application.
     * If critical (ex: Authentication Exception ) then it is crucial for the application.
     * @param e Exception object
     * @return true if the exception is crucial
     */
    private boolean isPasswordException( final Exception e)
    {
        boolean toProceed = false;
        /*
         * Check for following Authentication exceptions :
         * InvalidPasswordException
         * ExpiredPasswordException
         * NotFoundException
         * TooManyUsersException
         * LoginException
         *
         * above exceptions are classified as crucial
         */
        if( e instanceof NotFoundException )
        {
            toProceed = true;
        }
        else if (e instanceof SoapCallException)
        {
            SoapCallException callException = (SoapCallException)e;

            String faultCode = callException.getCode();

            if( CRITICAL_ERRORS.contains( faultCode ) )
            {
                toProceed = true;
            }
        }

        return toProceed;
    }

    /**
     * Critical Error list
     */
    private static final List   CRITICAL_ERRORS            = new ArrayList();

    /**
     * Initialize the critical error list.
     */
    static
    {
        CRITICAL_ERRORS.add( "com.lecklogic.Database.NotFoundException");//It will handle the NotFoundException from ssb/../LoginPlugin
        CRITICAL_ERRORS.add( "com.aspc.DBObj.Contact.InvalidPasswordException" );
        CRITICAL_ERRORS.add( "com.aspc.DBObj.Contact.LoginException" );
        CRITICAL_ERRORS.add( "com.aspc.DBObj.Errors.TooManyUsersException" );
        CRITICAL_ERRORS.add( Constants.FAULT_CODE_IMMEDIATE_PASSWORD_CHANGE );
    }

    /**
     * the message count which will be joined on the server side.
     */
    private static final AtomicLong MESSAGE_ID=new AtomicLong();

    /**
     * Static field for traffic bypass value
     */
    public static final String BY_PASS_TRAFFIC = "bypassTraffic";
    /**
     * Static field for login bypass value
     */
    public static final String BY_PASS_LOGIN = "bypassLogin";

    /**
     * Flag to indicate to by-pass Discover URL and moveTo URL.
     */
    private boolean byPassUrlEnabled = false;
    /**
     * Holder for the Login by-pass URL
     */
    private List byPassLoginTransportList = null;
    /**
     * Holder for Traffic by-pass URL
     */
    private List byPassTrafficTransportList = null;

    /**
     * Flag to indicate that login is successful, so we can switch to
     * use the moveToUrl list for rest of the communication.
     */
    private boolean isLoginSuccessful = false;

    /**
     * Flag to indicate the sequence of server URL usage, when the current transport fails.
     */
    private boolean isServerSequenceEnabled = false;

    /**
     * List of transport returned by Discover command
     */
    private final ArrayList discoverTransports = new ArrayList();

    /**
     * List of transport returned by moveToUrl header.
     */
    private final ArrayList moveToUrlTransports = new ArrayList();

    private boolean byPassDiscover;

    //private SOAPHeaderElement cacheHeader;
    private String sessionID;
    private String stateID;
    private String login;//NOPMD
    private String passwd;
    private final ArrayList transports = new ArrayList();
    private SoapTransport current;
    private String currentLayer;
    private TimeZone currentTz;

    private String cacheDirectory;

    private static final String TYPE_CHECKOUT = "CHECKOUT";
    private static final String TYPE_READ = "READ";
    private static final String TYPE_UPDATE = "UPDATE";
    private static final String TYPE_CHECKIN = "CHECKIN";    

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.soap.Client");//#LOGGER-NOPMD
}
