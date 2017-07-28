/*
 *  Copyright (c) 2001-2004 ASP Converters Pty Ltd.
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
*/
package com.aspc.developer;

import com.aspc.developer.*;

/**
 *  Thread Cop is designed to make sure that an object is called in the correct manner, not that the object handles the call correctly.
 *
 *  If an object is single threaded then it is only called by one thread.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED singleton</i>
 *
 * @author      Nigel Leck
 *  @since       11 September 2012
 */
public interface MonitorTC
{
    Object target();

    void enter( final ThreadCop.ACCESS access);

    void leave( );
}
