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
package com.aspc.remote.rest.internal;

/**
 *  ReST call response. 
 * 
 *  <i>THREAD MODE: readonly</i>
 *
 *  @author      Lei Gao
 *  @since       Jan 9, 2014
 */
public enum Trace
{
    UNKNOWN,
    CACHED,
    STALE,
    FETCHED_UNCOMPRESSED,
    FETCHED_GZIP,
    PREFETCH;
    //ERROR;    
    
    public boolean isFetched()
    {
        return this == FETCHED_GZIP || this == FETCHED_UNCOMPRESSED;
    }
}
