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
package com.aspc.remote.html.internal;
import com.aspc.remote.html.HTMLComponent;

/**
 *  HTMLFormComponent
 *
 * <i>THREAD MODE: SINGLE-THREADED HTML generator component</i>
 *  @author      Nigel Leck
 *  @since       July 19, 2000, 10:49 PM
 */
public abstract class HTMLFormComponent extends HTMLComponent implements HTMLReadOnly
{
    /** field values style */
    public static final String STYLE_STS_FIELD="sts-field";

    public static final String STYLE_STS_FIELD_PRINTOUT="sts-field-printout";
    
    public static final String STYLE_STS_FIELD_IN_TABLE="sts-field-table";

    /**
     *
     * @return the value
     */
    public abstract String getName();
    /**
     *
     * @return the value
     */
    public abstract String getValue();
    /**
     *
     * @param value the value
     */
    public abstract void setValue( String value);
    /**
     *
     * @param call
     */
    public abstract void addOnChangeEvent( String call);
    /**
     *
     * @param call
     * @param script
     */
    public abstract void addOnChangeEvent( String call, String script);
}
