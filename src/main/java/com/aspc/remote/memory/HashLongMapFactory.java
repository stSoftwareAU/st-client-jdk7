/*
 *  Copyright (c) 2002-2004 ASP Converters pty ltd
 *
 *  www.aspconverters.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.memory;

import com.aspc.developer.ThreadCop;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *  Factory for creating hash long map
 *  <br>
 *  <i>THREAD MODE: SINGLETON</i>
 *
 *
 *  @author      Nigel Leck
 *  @since       29 March 2002
 */
public final class HashLongMapFactory
{
    private static final int DEFAULT_VERSION=8;
    private static final AtomicInteger PROGRAM_VERSION=new AtomicInteger();
    private static final AtomicInteger ENVIROMENT_VERSION=new AtomicInteger();
    private static final AtomicInteger DATABASE_VERSION=new AtomicInteger();
    private static final Constructor DEFAULT_CONSTRUCTOR[]=new Constructor[1];
    private static final Constructor INIT_CONSTRUCTOR[]=new Constructor[1];
    private static final String PROPERTY_HASH_LONG_MAP_VERSION="HASH_LONG_MAP_VERSION";
    private static final Object DEFAULT_INITS[]=new Object[0];
    public static final float LOAD_FACTOR=0.75f;

    private HashLongMapFactory()
    {

    }

    /**
     * create a new hash long map
     * @return the new map
     */
    public static HashLongMap create()
    {
        try
        {
            return (HashLongMap)DEFAULT_CONSTRUCTOR[0].newInstance(DEFAULT_INITS);
        }
        catch( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            throw new RuntimeException( "could not create HashLongMap", e);
        }
    };

    /**
     * create a new hash long map
     * @param expectedItems store at least this number of items
     * @return the new map
     */
    public static HashLongMap create(final int expectedItems)
    {
        try
        {
            int initialCapacity = (int)((double)expectedItems/LOAD_FACTOR + 1);
            if( initialCapacity < 11 ) initialCapacity=11;
            Object params[]={initialCapacity};
            HashLongMap lm= (HashLongMap)INIT_CONSTRUCTOR[0].newInstance(params);

            assert ThreadCop.monitor(lm, ThreadCop.MODE.ACCESS_ONLY_BY_CREATING_THREAD);
            return lm;
        }
        catch( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            throw new RuntimeException( "could not create HashLongMap", e);
        }
    };

    /**
     * set the version
     * @param version the version
     * @return the previous version
     */
    public static int setProgramVersion( final int version)
    {
        int previousVersion = PROGRAM_VERSION.getAndSet(version);

        correctVersion();

        return previousVersion;
    }

    /**
     * set the version
     * @param version the version
     * @return the previous version
     */
    public static int setDataBaseVersion( final int version)
    {
        int previousVersion = DATABASE_VERSION.getAndSet(version);

        correctVersion();

        return previousVersion;
    }

    private static void correctVersion( )
    {
        int version = PROGRAM_VERSION.get();

        if( version < 1)
        {
            version = ENVIROMENT_VERSION.get();

            if( version < 1)
            {
                version = DATABASE_VERSION.get();

                if( version < 1)
                {
                    version = DEFAULT_VERSION;
                }
            }
        }
        try
        {
            Class tmpClass;

            switch( version)
            {
                case 8:
                    tmpClass = Class.forName("com.aspc.remote.memory.impl.HashLongMapV8");
                    break;
                case 7:
                    tmpClass = Class.forName("com.aspc.remote.memory.impl.HashLongMapV7");
                    break;
                case 6:
                    tmpClass = Class.forName("com.aspc.remote.memory.impl.HashLongMapV6");
                    break;
                case Integer.MAX_VALUE:
                    tmpClass = Class.forName("com.aspc.remote.memory.impl.HashLongMapCompare");
                    break;
                default:
                    throw new IllegalArgumentException( "version must be 6,7 or 8 was: " + version);
            }

            Class defaultTypes[] = new Class[0];
            DEFAULT_CONSTRUCTOR[0] = tmpClass.getDeclaredConstructor(defaultTypes);
            Class intTypes[] = new Class[]{Integer.TYPE};
            INIT_CONSTRUCTOR[0] = tmpClass.getDeclaredConstructor(intTypes);
        }
        catch( ClassNotFoundException | IllegalArgumentException | NoSuchMethodException | SecurityException e)
        {
            throw new Error( "couldn't find the implementation", e);
        }
    }

    static
    {
        String temp;

        temp = System.getProperty( PROPERTY_HASH_LONG_MAP_VERSION, "0");

        ENVIROMENT_VERSION.set(Integer.parseInt(temp));

        correctVersion();
    }
}
