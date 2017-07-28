/*
 *  Copyright (c) 2001-2004 ASP Converters pty ltd
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.memory.internal;

import com.aspc.remote.util.misc.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;

/**
 *  MemoryUtil
 *
 *  <br>
 *  <i>THREAD MODE: SINGLETON MULTI-THREADED memory management</i>
 *
 *  @author      Nigel Leck
 *  @since       12 May 2002
 */
public final class MemoryUtil
{
    /**
     * 
     * @param text the text to parse
     * @return the size
     */
    @CheckReturnValue @Nonnegative
    public static long parseSize( final String text)
    {
        String temp = text.trim().toUpperCase();
        temp = StringUtilities.replace( temp, ",", "");
        temp = temp.replace( " ", "");

        long multiple = 1;

        if( temp.endsWith("G"))
        {
            multiple = 1024 * 1024 * 1024;
            temp = temp.substring(0, temp.length() -1);
        }
        else if( temp.endsWith("M"))
        {
            multiple = 1024 * 1024;
            temp = temp.substring(0, temp.length() -1);
        }
        else if( temp.endsWith("K"))
        {
            multiple = 1024;
            temp = temp.substring(0, temp.length() -1);
        }
        else if( temp.endsWith("B"))
        {
            multiple = 1;
            temp = temp.substring(0, temp.length() -1);
        }

        double d = Double.parseDouble( temp);
        long size =(long)( d * multiple);

        return size;
    }
    
    /**
     * How much space is used by this object ?
     *
     * @param obj the object
     * @return The number of bytes
     */
    @CheckReturnValue @Nonnegative
    public static long estMemoryUsed( final Object obj)
    {
        if( obj == null) return 0;

        if( obj instanceof String)
        {
            String s = (String)obj;

            return s.length() * 2 + 10;
        }
        else if( obj instanceof Double )
        {
            return 16;
        }
        else if( obj instanceof Integer)
        {
            return 4;
        }
        else if( obj instanceof Long)
        {
            return 8;
        }
        else
        {
            String cn=obj.getClass().getName();
            if( cn.contains("DBData"))
            {
                return 1000;
            }
            else
            {
                return 50;
            }
        }
    }

    /**
     * The size of a pointer
     *
     * @return the size in bytes
     */
    @CheckReturnValue @Nonnegative
    public static int sizeOfPointer()
    {
        return SZ_REF;
    }

    /**
     * How much space is used ?
     *
     * @param b the element
     *
     * @return the size in bytes
     */
    @CheckReturnValue @Nonnegative
    public static int sizeof(boolean b)
    {
        return 1;
    }

    /**
     * How much space is used ?
     *
     * @param b the element
     *
     * @return the size in bytes
     */
    @CheckReturnValue @Nonnegative
    public static int sizeof(byte b)
    {
        return 1;
    }

    /**
     * How much space is used ?
     *
     * @param c the element
     *
     * @return the size in bytes
     */
    @CheckReturnValue @Nonnegative
    public static int sizeof(char c)
    {
        return 2;
    }

    /**
     * How much space is used ?
     *
     * @param s the element
     *
     * @return the size in bytes
     */
    @CheckReturnValue @Nonnegative
    public static int sizeof(short s)
    {
        return 2;
    }

    /**
     * How much space is used ?
     *
     * @param i the element
     *
     * @return the size in bytes
     */
    @CheckReturnValue @Nonnegative
    public static int sizeof(int i)
    {
        return 4;
    }

    /**
     * How much space is used ?
     *
     * @param l the element
     *
     * @return the size in bytes
     */
    @CheckReturnValue @Nonnegative
    public static int sizeof(long l)
    {
        return 8;
    }

    /**
     * How much space is used ?
     *
     * @param f the element
     *
     * @return the size in bytes
     */
    @CheckReturnValue @Nonnegative
    public static int sizeof(float f)
    {
        return 4;
    }

    /**
     * How much space is used ?
     *
     * @param d the element
     *
     * @return the size in bytes
     */
    @CheckReturnValue @Nonnegative
    public static int sizeof(double d)
    {
        return 8;
    }

    /**
     * How much space is used ?
     *
     * @param c the element
     *
     * @return the size in bytes
     */
    @CheckReturnValue @Nonnegative
    public static int instanceSize(final Class c)
    {
        Integer size = KNOWN_SIZES.get(c);

        if( size == null)
        {
            Field flds[] = c.getDeclaredFields();
            int sz = 24;// size of object in bytes

            for (Field f: flds)
            {
                if ( (f.getModifiers() & Modifier.STATIC) != 0) continue;

                sz += primativeSize(f.getType());
            }

            if (c.getSuperclass() != null)
            {
                sz += instanceSize(c.getSuperclass());
            }

            Class cv[] = c.getInterfaces();
            for (Class cv1 : cv) 
            {
                sz += instanceSize(cv1);
            }
            size = sz;
            KNOWN_SIZES.put(c, size);
        }

        return size;
    }

    /**
     * How much space is used ?
     *
     * @param obj the element
     *
     * @return the size in bytes
     */
    @CheckReturnValue @Nonnegative
    public static long sizeOf(final Object obj)
    {
        if (obj == null) return 0;

        if( obj instanceof InterfaceSizeOf)
        {
            InterfaceSizeOf sizeOf = (InterfaceSizeOf)obj;

            return sizeOf.sizeOf();
        }
        Class c = obj.getClass();

        if (c.isArray())
        {
            return arraySize(obj, c);
        }
        else
        {
            return instanceSize(c);
        }
    }

    private MemoryUtil()
    {
    }

    @CheckReturnValue @Nonnegative
    private static int primativeSize( final Class t)
    {
        if (t == Boolean.TYPE)
        {
            return 1;
        }
        else if (t == Byte.TYPE)
        {
            return 1;
        }
        else if (t == Character.TYPE)
        {
            return 2;
        }
        else if (t == Short.TYPE)
        {
            return 2;
        }
        else if (t == Integer.TYPE)
        {
            return 4;
        }
        else if (t == Long.TYPE)
        {
            return 8;
        }
        else if (t == Float.TYPE)
        {
            return 4;
        }
        else if (t == Double.TYPE)
        {
            return 8;
        }
        else if (t == Void.TYPE)
        {
            return 0;
        }
        else
        {
            return SZ_REF;
        }
    }

    @CheckReturnValue
    private static int arraySize(final Object obj, final Class c)
    {
        Class ct = c.getComponentType();
        int len = Array.getLength(obj);

        if (ct.isPrimitive())
        {
            return len * primativeSize(ct) + SZ_REF;
        }
        else
        {
            int sz = 0;
            for (int i = 0; i < len; i++)
            {
                sz += SZ_REF;
                Object obj2 = Array.get(obj, i);
                if (obj2 == null)
                    continue;
                Class c2 = obj2.getClass();
                if (!c2.isArray())
                {
                    sz += instanceSize( c2);
                }
                else
                {
                    sz += arraySize(obj2, c2);
                }
            }
            return sz;
        }
    }

    private static final int SZ_REF;
    private static final ConcurrentHashMap<Class, Integer> KNOWN_SIZES=new ConcurrentHashMap<>();
    static
    {
        String temp = System.getProperty("sun.arch.data.model", "");

        if("32".equals( temp))
        {
            SZ_REF=4;
        }
        else
        {
            SZ_REF=8;
        }
    }
}
