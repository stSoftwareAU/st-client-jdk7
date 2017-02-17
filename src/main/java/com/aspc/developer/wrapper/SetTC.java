/*
 *  Copyright (c) 2001-2004 ASP Converters Pty Ltd.
 *
 *  www.aspconverters.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
*/
package com.aspc.developer.wrapper;

import com.aspc.developer.MonitorTC;
import com.aspc.developer.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

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
public final class SetTC<E> implements Set<E>
{
    private final Set<E>s;
    private final MonitorTC monitor;

    public SetTC( Set<E>s, MonitorTC monitor)
    {
        this.s=s;
        this.monitor=monitor;
    }

    /** {@inheritDoc} */
    @Override
    public int size()
    {
        monitor.enter( ThreadCop.ACCESS.READ);
        try
        {
            return s.size();
        }
        finally
        {
            monitor.leave();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty()
    {
        monitor.enter( ThreadCop.ACCESS.READ);
        try
        {
            return s.isEmpty();
        }
        finally
        {
            monitor.leave();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(Object o)
    {
        monitor.enter( ThreadCop.ACCESS.READ);
        try
        {
            return s.contains(o);
        }
        finally
        {
            monitor.leave();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<E> iterator()
    {
        monitor.enter( ThreadCop.ACCESS.READ);
        try
        {
            return new IteratorTC( s.iterator(), monitor);
        }
        finally
        {
            monitor.leave();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object[] toArray()
    {
        monitor.enter( ThreadCop.ACCESS.READ);
        try
        {
            return s.toArray();
        }
        finally
        {
            monitor.leave();
        }
    }

    /** {@inheritDoc}
     * @param <T>
     */
    @Override
    public <T> T[] toArray(T[] a)
    {
        monitor.enter( ThreadCop.ACCESS.READ);
        try
        {
            return s.toArray(a);
        }
        finally
        {
            monitor.leave();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean add(E e)
    {
        monitor.enter( ThreadCop.ACCESS.MODIFY);
        try
        {
            return s.add(e);
        }
        finally
        {
            monitor.leave();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean remove(Object o)
    {
        monitor.enter( ThreadCop.ACCESS.MODIFY);
        try
        {
            return s.remove(o);
        }
        finally
        {
            monitor.leave();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsAll(Collection<?> c)
    {
        monitor.enter( ThreadCop.ACCESS.READ);
        try
        {
            return s.containsAll(c);
        }
        finally
        {
            monitor.leave();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        monitor.enter( ThreadCop.ACCESS.MODIFY);
        try
        {
            return s.addAll(c);
        }
        finally
        {
            monitor.leave();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean retainAll(Collection<?> c)
    {
        monitor.enter( ThreadCop.ACCESS.MODIFY);
        try
        {
            return s.retainAll(c);
        }
        finally
        {
            monitor.leave();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeAll(Collection<?> c)
    {
        monitor.enter( ThreadCop.ACCESS.MODIFY);
        try
        {
            return s.retainAll(c);
        }
        finally
        {
            monitor.leave();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clear()
    {
        monitor.enter( ThreadCop.ACCESS.MODIFY);
        try
        {
            s.clear();
        }
        finally
        {
            monitor.leave();
        }
    }
}
