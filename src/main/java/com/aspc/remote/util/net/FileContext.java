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
package com.aspc.remote.util.net;

/**
 * File Context holds all the values to retrieve a file.
 *
 * 
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 * @author      alex
 * @since       February 1, 2006
 */
public class FileContext
{
    /** Fetch name.*/
    public static final String NAME           ="FETCH_NAME";
    /** Cipher key.*/
    public static final String CIPHER_KEY     ="CIPHER_KEY";
    /** File key.*/
    public static final String FILE_KEY       ="FILE_KEY";
    /** Raw checksum.*/
    public static final String RAW_CHECKSUM   ="RAW_CHECKSUM";
    /** Chechsum.*/
    public static final String CHECKSUM       ="CHECKSUM";
    /** Init vector.*/
    public static final String INIT_VECTOR    ="INIT_VECTOR";
    /** File name.*/
    public static final String FILE_NAME      ="FILE_NAME";
    /** Location.*/
    public static final String LOCATION       ="LOCATION";
    /** Protocol.*/
    public static final String PROTOCOL       ="PROTOCOL";
    /** Compressed flag.*/
    public static final String COMPRESSED     ="COMPRESSED"; 
    /** File size.*/
    public static final String SIZE           ="SIZE";
    /** Applet version.*/ 
    public static final String JAR_APPLET_VERSION   ="1_57";
    /** Edit mode.*/
    public static final String EDIT_MODE   ="mode";
    
    /**
     * File context.
     */
    public FileContext()
    {
    } 
}
