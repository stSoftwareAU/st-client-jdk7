/*
 *  Copyright (c) 2002-2004 ASP Converters pty ltd
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.memory;

import com.aspc.remote.memory.internal.InterfaceSizeOf;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *  This is a hacked version of HashMap for longs only keys. It prevents us from having to
 *  create objects for keys.
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *  <I> MEMORY USAGE: 144 bytes per Object </I>
 * 
 *  @author      Nigel Leck
 *  @param <T> the type of object stored
 *  @since       29 March 2002
 */
public interface HashLongMap<T> extends InterfaceSizeOf, Cloneable
{
    /** the state of the multiple rows */
    enum State
    {
        UNIQUE_SORTED,
        UNIQUE,
        UNKNOWN
    };

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    @Nonnegative @CheckReturnValue
    public int size();

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    @CheckReturnValue
    public boolean isEmpty();

    /**
     * clone the memory handler group.
     * @throws CloneNotSupportedException doesn't happen.
     * @return the new group.
     */
    @CheckReturnValue @Nonnull
    public Object clone() throws CloneNotSupportedException;//NOPMD

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value.
     */
    @CheckReturnValue
    public boolean containsValue(@Nonnull Object value);

    /** 
     * create an array of the first 200 odd keys
     *
     * @return the array
     */
    @CheckReturnValue @Nonnull
    public long[] briefKeyArray();
    
    /** 
     * create an array of the keys
     * 
     * 
     * @return the array
     */
    @CheckReturnValue @Nonnull
    public long[] getKeyArray();
    
    /**
     * create an array of arrays for the keys. 
     * @return the data array.
     */
    public long[][] getKeyData();
    
    /**
     * Is the key array sorted ? 
     * @return true if sorted
     */
    @CheckReturnValue
    public boolean isKeyArraySorted();
    
    /** 
     * create an array of the keys
     * 
     * SORTED array. 
     * 
     * @return the array
     */
    @CheckReturnValue @Nonnull
    public long[] getSortedKeyArray();
    
    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     *
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     * @param key key whose presence in this Map is to be tested.
     */
    @CheckReturnValue
    public boolean containsKey(long key);

    /**
     * Returns the value to which this map maps the specified key.  Returns
     * <tt>null</tt> if the map contains no mapping for this key.  A return
     * value of <tt>null</tt> does not <i>necessarily</i> indicate that the
     * map contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to <tt>null</tt>.  The <tt>containsKey</tt>
     * operation may be used to distinguish these two cases.
     *
     * @return the value to which this map maps the specified key.
     * @param key key whose associated value is to be returned.
     */
    @CheckReturnValue
    public T get(long key);

    /** 
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     * 
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.  A <tt>null</tt> return can
     *         also indicate that the HashMap previously associated
     *         <tt>null</tt> with the specified key.
     * @param orginal The object to place into the table.
     * @param key key with which the specified value is to be associated.
     */
    @Nullable
    public T put(final long key, final T orginal);

    /**
     * put multiple rows.
     * @param rows the rows
     * @param value the value
     * @param stateOfRows are these rows known to be sorted ? MAYBE null if you don't know
     */
    public void putMultiRows( final @Nonnull long rows[], final @Nullable T value, final @Nonnull State stateOfRows);
    
    /**
     * Removes the mapping for this key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.  A <tt>null</tt> return can
     *         also indicate that the map previously associated <tt>null</tt>
     *         with the specified key.
     */
    @Nullable
    public T remove(long key);

    /**
     * Removes all mappings from this map.
     */
    public void clear();

    /** {@inheritDoc} */
    @Override @CheckReturnValue
    public long sizeOf();

    /**
     * is this hash map initialized.
     * @return true if initialized.
     */
    @CheckReturnValue
    public boolean isInitialized();
}
