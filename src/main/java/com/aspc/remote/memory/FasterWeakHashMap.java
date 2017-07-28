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
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 *  This is a hacked version of HashMap for longs only keys. It prevents us from having to
 *  create objects for keys.
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       29 March 2002
 */
public class FasterWeakHashMap extends WeakHashMap
{
    /**
     * Constructs a new, empty map with the specified initial capacity
     * and default load factor, which is <tt>0.75</tt>.
     *
     * @param initialCapacity the initial capacity of the HashMap.
     */
    public FasterWeakHashMap(int initialCapacity)
    {
        super( initialCapacity);
    }
    
    /**
     * call super and clear cache
     * @param key teh key to put
     * @param value the value to be place.
     * @return the removed value.
     */
    @Override
    public Object put(Object key, Object value) 
    {
        Object previous;
        previous = super.put( key, value);
        
        // We must always clear as the keys are held weak reference which 
        // is per object the put is done by equals()
        cacheKeyReferences=null;
        
        return previous;
    }
    
    /**
     * call super and clear cache
     * @param key the key to remove
     * @return the removed value.
     */
    @Override
    public Object remove(Object key) 
    {
        Object previous;
        previous = super.remove( key);

        // if we found one then clear        
        if( previous != null)
        {
            cacheKeyReferences=null;
        }
        
        return previous;
    }

    /**
     * call super and clear cache
     */
    @Override
    public void clear() 
    {
        super.clear();
        cacheKeyReferences=null;

    }

    /**
     * list of keys and weak references.
     * @return the weak reference keys
     */
    public WeakReference[] listKeyReferences()
    {
        if( cacheKeyReferences == null )
        {
            Object keys[] =keySet().toArray();
            
            WeakReference[] tempReferences = new WeakReference[keys.length];
            
            for( int i = 0; i < keys.length; i++)
            {
                tempReferences[i] = new WeakReference( keys[i]);                               
            }
            
            cacheKeyReferences = tempReferences;
        }
        
        return cacheKeyReferences;
    }
    
    private WeakReference[] cacheKeyReferences;
}
