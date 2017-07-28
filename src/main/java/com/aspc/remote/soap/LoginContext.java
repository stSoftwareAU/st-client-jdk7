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
package com.aspc.remote.soap;

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import java.util.TimeZone;
import org.apache.commons.logging.Log;

/**
 * Login context.
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 * @author alex
 * @since 29 September 2006
 */
public class LoginContext
{
    /**
     * Client time zone name.
     */
    public String clientTimeZoneName;

    /**
     * Client time zone offset.
     */
    public String clientTimeZoneOffset;

    /**
     * JDK version of client browser.
     */
    public String javaVersion;

    /**
     * client language.
     */
    public String language;

    /**
     * client authorization.
     */
    public final String authorization;

    /**
     * client request type.
     */
    public final String method;
    /** userName */
    public final String userName;
    /**
     * remember me.
     */
    public final boolean rememberMe;

    /** SSO authorized */
    public final boolean ssoAuthorized;

    /** magic number */
    public final String magicNumber;

    /**
     * client session type.
     */
    public String sessionType;

    private String reason;
    public final int screenWidth;
    public final int screenHeight;
    //private String authorizationUserName;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.soap.LoginContext");//#LOGGER-NOPMD

    /**
     * Creates a new instance of LoginContext.
     *
     * @param clientTimeZoneOffset time zone offset
     * @param clientTimeZoneName time zone name
     * @param javaVersion jdk version
     * @param language user language
     * @param authorization the authorization
     * @param method the request type
     * @param rememberMe remember me
     * @param userName user name
     * @param authorized is authorized
     * @param sessionType the type
     * @param magicNumber magic number
     * @param screenWidth the width
     * @param screenHeight  the screen height
     */
    public LoginContext(
        final String userName,
        final String clientTimeZoneOffset,
        final String clientTimeZoneName,
        final String javaVersion,
        final String language,
        final String authorization,
        final String method,
        final boolean rememberMe,
        final String sessionType,
        final boolean authorized,
        final String magicNumber,
        final int screenWidth,
        final int screenHeight
    )
    {
        this.userName = userName;
        this.clientTimeZoneOffset = clientTimeZoneOffset;
        this.clientTimeZoneName = clientTimeZoneName;
        this.javaVersion = javaVersion;
        this.language = language;
        this.authorization = authorization;
        this.ssoAuthorized = authorized;
        this.method=method;
        this.rememberMe=rememberMe;
        this.sessionType=sessionType;
        this.magicNumber=magicNumber;
        this.screenHeight=screenHeight;
        this.screenWidth=screenWidth;
    }

    /**
     * default context
     */
    public LoginContext(    )
    {
        this.userName = "";
        this.clientTimeZoneOffset = "";
        this.clientTimeZoneName = "";
        this.javaVersion = "";
        this.language = "";
        this.authorization = "";
        this.ssoAuthorized = false;
        this.method="";
        this.rememberMe=false;
        this.sessionType="";
        this.magicNumber="";
        this.screenHeight=-1;
        this.screenWidth=-1;
    }

    /**
     * Returns client time zone name.
     *
     * @return client time zone name.
     */
    public String getClientTimeZoneName()
    {
        if( StringUtilities.isBlank(clientTimeZoneName)== false)
        {
            return clientTimeZoneName;
        }

        return TimeZone.getDefault().getID();
    }

    /**
     *
     * @return the reason
     */
    public String getReason()
    {
        return reason != null ? reason:"";
    }

    /**
     *
     * @param reason the reason
     */
    public void setReason(final String reason)
    {
        this.reason=reason;
    }

    /**
     * Returns client time zone name.
     *
     * @return client time zone name.
     */
    public String getClientTimeZoneRaw()
    {
        if( StringUtilities.isBlank( clientTimeZoneName))
        {
            return clientTimeZoneOffset;
        }
        else
        {
            return clientTimeZoneName;
        }
    }


    /**
     * Sets session type.
     *
     * @param type the type.
     */
    public void setSessionType(final String type)
    {
        this.sessionType = type;
    }

    /**
     * Sets client time zone name.
     *
     * @param clientTimeZoneName client time zone name.
     */
    public void setClientTimeZoneName(String clientTimeZoneName)
    {
        this.clientTimeZoneName = clientTimeZoneName;
    }

    /**
     * Returns client time zone offset.
     *
     * @return client time zone offset.
     */
    public String getClientTimeZoneOffset()
    {
        if( StringUtilities.isBlank(clientTimeZoneName)== false)
        {
            return clientTimeZoneName;
        }

        return TimeZone.getDefault().getID();
    }

    /**
     * Sets client time zone offset.
     *
     * @param clientTimeZoneOffset client time zone offset.
     */
    public void setClientTimeZoneOffset(String clientTimeZoneOffset)
    {
        this.clientTimeZoneOffset = clientTimeZoneOffset;
    }

    /**
     * Returns java version.
     *
     * @return java version.
     */
    public String getJavaVersion()
    {
        return javaVersion;
    }

    /**
     * Sets java version.
     *
     * @param javaVersion JDK version.
     */
    public void setJavaVersion(String javaVersion)
    {
        this.javaVersion = javaVersion;
    }

    /**
     * Returns language.
     *
     * @return language.
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     * Sets language.
     *
     * @param language client language
     */
    public void setLanguage(String language)
    {
        this.language = language;
    }

    /**
     * Returns sessionType.
     *
     * @return sessionType.
     */
    public String getSessionType()
    {
        return sessionType;
    }

}
