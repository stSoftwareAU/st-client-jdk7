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
package com.aspc.remote.util.net;

import com.aspc.remote.util.misc.StringUtilities;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;


/**
 *  Soap transport.
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *  @author      Jason McGrath
 *  @since       25 September 2006, 12:02
 */
public class URLParser
{
    private final String protocol;
    /** Default login id */
    private final String userName;
    /** Default Password */
    private final String password;
     /** Host name */
    private final String hostname;
     /** URI */
    private final String uri;
    private final String port;

    /**
     * The default constructor to parse a supplied url
     * @param theURL the URL to connect to
     */
    public URLParser(final String theURL)
    {
        String s = theURL;

        int pos = s.indexOf("://");

        if( pos != -1)
        {
            protocol=s.substring(0, pos);

            s = s.substring( pos + 3);
        }
        else
        {
            protocol="UNKNOWN";
        }

        String tempUserName=null;
        String tempPassword=null;
        String tempHostName=null;
        String tempPort=null;
        String tempURI;
        StringBuilder buffer = new StringBuilder();
        // check for user
        
        for( int i =0; i < s.length();i++)
        {
            char c = s.charAt(i);

            if( c == '%' && i + 2 < s.length())
            {
                char c1 = (char) s.charAt(i + 1);
                char c2 = (char) s.charAt(i + 2);

                int b;
                b = Character.digit(c1, 16);
                b *= 16;
                b += Character.digit(c2, 16);

                buffer.append((char)b);

                i +=2;
            }
            else if( c == '+')
            {
                buffer.append( " ");
            }
            else
            {
                if( c == ':' && tempUserName == null)
                {
                    tempUserName = buffer.toString();

                    buffer = new StringBuilder();
                }
                else if( c == '@' && tempPassword == null)
                {
                    if( tempUserName == null)
                    {
                        tempUserName = buffer.toString();
                        tempPassword="";
                    }
                    else if( tempPassword == null)
                    {
                        tempPassword = buffer.toString();
                    }
                    buffer = new StringBuilder();
                }
                else if( c == ':' && tempPort == null)
                {
                    tempHostName = buffer.toString();

                    buffer = new StringBuilder();
                }
                else if( c == '/' && tempHostName == null)
                {
                    if( tempUserName == null)
                    {
                        tempUserName="guest";
                        tempPassword="";
                    }
                    tempHostName = buffer.toString();
                    tempPort="";
                    buffer = new StringBuilder();
                }
                else if( c == '/' && tempPort == null)
                {
                    tempPort = buffer.toString();

                    buffer = new StringBuilder();
                }
                else
                {
                    buffer.append(c);
                }
            }
        }

        if( tempHostName == null)
        {
            tempHostName = buffer.toString();
            tempURI = "";
            tempPort="";
        }
        else if( tempPort == null)
        {
            tempPort =buffer.toString();
            tempURI="";
        }
        else
        {
            tempURI = buffer.toString();
        }

        if( tempUserName == null)
        {
            tempUserName = "guest";
        }

        if( tempPassword == null)
        {
            tempPassword = "";
        }

        userName = tempUserName;
        password = tempPassword;


        uri = tempURI;
        hostname=tempHostName;
        port=tempPort;
    }


    /**
     * to string
     * @return the string
     */
    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        if( protocol.equalsIgnoreCase("unknown") == false)
        {
            buffer.append(protocol);
            buffer.append("://");
        }
        String tempUserName = userName;
        tempUserName = StringUtilities.encode(tempUserName);
        buffer.append(tempUserName);
        buffer.append(":");
        buffer.append(password);
        buffer.append("@");
        buffer.append(hostname);
        buffer.append(":");
        buffer.append(port);
        buffer.append("/");
        String tempURI=uri;
        tempURI = StringUtilities.encode(tempURI);
        buffer.append(tempURI);

        return buffer.toString();
    }

    /**
     * The protocol.
     * @return the host
     */
    public String getProtocol()
    {
        return protocol;
    }

    /**
     * The port.
     * @return the host
     */
    public String getPort()
    {
        return port;
    }

    /**
     * The host.
     * @return the host
     */
    public String getHostName()
    {
        return hostname;
    }

    /**
     * The login.
     * @return The login
     */
    public String getUserName()
    {
        return userName;
    }
     
    /**
     * The password
     * @return the password.
     */
    public String getPassword()
    {
        return password;
    }
     
    /**
     * The URI
     * @return the layer
     */
    public String getURI()
    {
        return uri;
    }    
}
