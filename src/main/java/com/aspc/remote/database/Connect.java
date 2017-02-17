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
package com.aspc.remote.database;

import com.aspc.remote.application.*;
import com.aspc.remote.util.misc.*;

/**
 *  Connect
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *
 *  @author      Nigel Leck
 *  @since       12 May 1998
 */
public class Connect
{
    private final CFrame master;
    private CLogin theLogin;//NOPMD

    /**
     * 
     * @param master the master
     */
    public Connect( CFrame master)
    {
        this.master = master;
    }

    /**
     *
     */
    public void start()
    {
        theLogin = new CLogin(master, false);

        CUtilities.center( theLogin, master);
        theLogin.show();
    }
}
