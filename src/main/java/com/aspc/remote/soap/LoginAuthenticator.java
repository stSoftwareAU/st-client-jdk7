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

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Login context.
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 * @author alex
 * @since 29 September 2006
 */
public class LoginAuthenticator extends Authenticator
{
    public static void set( final String user, final String passwd)
    {
        AUTHENTICATOR_USER.set(user);
        AUTHENTICATOR_PASSWORD.set(passwd);
    }

    public static void clear( )
    {
        AUTHENTICATOR_PASSWORD.remove();
        AUTHENTICATOR_USER.remove();
    }
    
    /** the thread password. */
    private static final ThreadLocal<String> AUTHENTICATOR_PASSWORD = new ThreadLocal<>();
    /** the user */
    private static final ThreadLocal<String> AUTHENTICATOR_USER = new ThreadLocal<>();

    /** {@inheritDoc }
     * @return  */
    @Override
    public PasswordAuthentication getPasswordAuthentication()
    {
        String pw = AUTHENTICATOR_PASSWORD.get();
        String user = AUTHENTICATOR_USER.get();

        return new PasswordAuthentication(user, pw.toCharArray());
    }
}
