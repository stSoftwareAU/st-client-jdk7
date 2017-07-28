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
package com.aspc.developer.wrapper;

import com.aspc.developer.MonitorTC;
import com.aspc.developer.ThreadCop;
import java.util.Iterator;

/**
 *  Thread cop wrapper.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED singleton</i>
 *
 *  @param <E>
 * @author      Nigel Leck
 *  @since       11 September 2012
 */
public final class IteratorTC<E> implements Iterator<E>
{
    private final Iterator<E>s;
    private final MonitorTC monitor;

    /** {@inheritDoc}
     * @param s
     * @param monitor
     */
    public IteratorTC( Iterator<E>s, MonitorTC monitor)
    {
        this.s=s;
        this.monitor=monitor;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext()
    {
        monitor.enter( ThreadCop.ACCESS.READ);
        try
        {
            return s.hasNext();
        }
        finally
        {
            monitor.leave();
        }
    }

    /** {@inheritDoc} */
    @Override
    public E next()
    {
        monitor.enter( ThreadCop.ACCESS.READ);
        try
        {
            return s.next();
        }
        finally
        {
            monitor.leave();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void remove()
    {
        monitor.enter( ThreadCop.ACCESS.MODIFY);
        try
        {
            s.remove();
        }
        finally
        {
            monitor.leave();
        }
    }
}
