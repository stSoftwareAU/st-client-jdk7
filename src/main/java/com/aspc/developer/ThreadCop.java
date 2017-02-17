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
package com.aspc.developer;

import com.aspc.developer.errors.*;
import com.aspc.remote.database.NullValueException;
import com.aspc.remote.util.misc.CLogger;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.Stack;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.apache.commons.logging.Log;

/**
 *  Thread Cop is designed to make sure that an object is called in the correct manner, not that the object handles the call correctly.
 *
 *  If an object is single threaded then it is only called by one thread.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED singleton</i>
 *
 *  @author      Nigel Leck
 *  @since       9 November 2009
 */
public final class ThreadCop
{
    /** the access type */
    @SuppressWarnings("PublicInnerClass")
    public enum ACCESS
    {
        /** read access */
        READ,
        /** modify access */
        MODIFY;
    }

    /** the thread modes */
    @SuppressWarnings("PublicInnerClass")
    public enum MODE
    {
        /** readonly access by MANY threads is allowed but to only readonly methods */
        READONLY,
        /** This object is only accessed by the thread that created it */
        ACCESS_ONLY_BY_CREATING_THREAD,
        /** the access to this thread is controlled externally */
        EXTERNAL_SYNCHRONIZED,
        /** Disabled TEST CASE ONLY */
        DISABLED,
    }

    private static final AtomicLong ERROR_COUNT=new AtomicLong();
    private static final WeakHashMap<Object, Brief> MONITORED_OBJECTS=new WeakHashMap<>();
    private static final ThreadCopError LAST_ERROR_HANDLE[]=new ThreadCopError[1];
    private static final ConcurrentHashMap CAUSE_ERROR=new ConcurrentHashMap();
    private static final ConcurrentHashMap<String, Long> PAUSE_LOCATIONS=new ConcurrentHashMap();

    private static final AtomicBoolean THROWING_ERROR=new AtomicBoolean();
    private static final ReentrantReadWriteLock MONITOR_LOCK=new ReentrantReadWriteLock();
    private static final Log LOGGER = CLogger.getLog( "com.aspc.developer.ThreadCop");//#LOGGER-NOPMD
    private ThreadCop()
    {
    }

    /**
     * check that this a flow control exception
     * @param t
     * @return the value
     */
    public static boolean checkFlowControlException( Throwable t)
    {
        if( t instanceof NullValueException )
        {
            return true;
        }
        
        throw new AssertionError( "not a flow control exception: " + t.toString());
    }
    
    /**
     * clear all pauses.
     * 
     * @return always TRUE
     */
    public static boolean clearLocations( )
    {
        PAUSE_LOCATIONS.clear();
        CAUSE_ERROR.clear();
        
        return true;
    }    
    
    /**
     * add pause to a location
     * @param position this position
     * @param timeToPause now long to pause
     * @return always TRUE
     */
    public static boolean addPauseAt( final String position, long timeToPause)
    {
        if( timeToPause > 0)
        {
            PAUSE_LOCATIONS.put(position, timeToPause);
        }
        else
        {
            PAUSE_LOCATIONS.remove(position);
        }
       
        return true;
    }    
    
    /**
     * pause at a location
     * @param position this position
     * @return always TRUE
     */
    public static boolean pauseAt( final String position)
    {
        Long pauseTime=PAUSE_LOCATIONS.get( position);
        if( pauseTime != null)
        {
            try
            {
                Thread.sleep(pauseTime);
            } 
            catch (InterruptedException ex)
            {
                
            }
        }
        return true;
    }
    
    /**
     * cause an exception at
     * @param position this position
     */
    public static void causeExceptionAt( final String position)
    {
        CAUSE_ERROR.put(position, "");
    }

    /**
     * cause an exception at
     * @param position this position
     * @return true
     */
    public static boolean checkExceptionAt( final String position)
    {
        if( CAUSE_ERROR.remove(position) != null)
        {
            throw new RuntimeException( "problem as requested at " + position);
        }

        return true;
    }

    /**
     * The total error count
     * @return the count
     */
    public static long errorCount()
    {
        return ERROR_COUNT.get();
    }

    /**
     * The last error that occurred.
     * @return the last error.
     */
    public static ThreadCopError lastError()
    {
        return LAST_ERROR_HANDLE[0];
    }

    /**
     * record the last error that occurred.
     * @param lastError the last error.
     */
    public static void recordLastError( final ThreadCopError lastError)
    {
        LAST_ERROR_HANDLE[0]=lastError;
    }

    private static Object realTarget( final Object target)
    {
        if( target instanceof MonitorTC)
        {
            return ((MonitorTC)target).target();
        }

        return target;
    }

    /**
     * monitor the object
     * @param target the target object
     * @param mode the access mode
     * @return always true
     */
    public static boolean pushMonitor( final Object target, final MODE mode)
    {
        if( target == null) return true;

        Object rt=realTarget( target);
        MONITOR_LOCK.writeLock().lock();
        try
        {
            MODE previousMode = currentMonitor(rt);
            iMonitor(rt, mode);

            Brief brief;

            brief = MONITORED_OBJECTS.get(rt);

            if( brief.modeStack == null)
            {
                brief.modeStack=new Stack<>();
            }
            brief.modeStack.push(previousMode);
        }
        finally
        {
            MONITOR_LOCK.writeLock().unlock();
        }

        return true;
    }

    /**
     * monitor the object
     * @param target the target object
     * @return always true
     */
    public static boolean popMonitor( final Object target)
    {
        Object rt=realTarget( target);
        MONITOR_LOCK.writeLock().lock();

        try
        {
            MODE mode;

            Brief brief;

            brief = MONITORED_OBJECTS.get(rt);
            if( brief != null && brief.modeStack != null)
            {
                mode = brief.modeStack.pop();
                iMonitor(rt, mode);
            }
        }
        finally
        {
            MONITOR_LOCK.writeLock().unlock();
        }

        return true;
    }

    /**
     * monitor the object
     * @param target the target object
     * @return always true
     */
    public static MODE currentMonitor( final Object target)
    {
        Object rt=realTarget( target);

        MONITOR_LOCK.readLock().lock();
        try
        {
            Brief previousBrief;

            previousBrief = MONITORED_OBJECTS.get(rt);

            if( previousBrief != null)
            {
                return previousBrief.mode;
            }
            else
            {
                return MODE.DISABLED;
            }
        }
        finally
        {
            MONITOR_LOCK.readLock().unlock();
        }
    }

    /**
     * monitor the object
     * @param target the target object
     * @param mode the access mode
     * @return always true
     */
    public static boolean monitor( final Object target, final MODE mode)
    {
        WriteLock writeLock = MONITOR_LOCK.writeLock();
        writeLock.lock();
        try
        {
            iMonitor(target, mode);
        }
        finally
        {
            writeLock.unlock();
        }

        return true;
    }

    private static void iMonitor( final Object target, final MODE mode)
    {
        Object rt=realTarget( target);

        if( rt instanceof AbstractMap || rt instanceof AbstractList)
        {
           // LOGGER.info( rt + "#" + rt.hashCode() + " type unsupported");
            return;
        }
        Brief previousBrief;

        previousBrief = MONITORED_OBJECTS.get(rt);

        if( previousBrief != null)
        {
            if( previousBrief.mode==MODE.DISABLED)
            {
                return;
            }

            long threadID=Thread.currentThread().getId();
            if( previousBrief.ownerThreadID != threadID)
            {
                if( previousBrief.stackCount > 0 && mode != MODE.DISABLED)
                {
                    throw new ThreadMonitorChangedByNonOwnerError( rt + "#" + rt.hashCode() + " is monitored by " + previousBrief.ownerThreadID + " already\n" + CLogger.stackDump());
                }
                previousBrief.ownerThreadID=threadID;
            }

            previousBrief.mode=mode;
        }
        else
        {
            Brief brief =new Brief(mode);

            MONITORED_OBJECTS.put(rt, brief);
        }
    }

    /**
     * read access
     * @param target the target object
     * @return always true
     */
    public static boolean read( final Object target)
    {
        return access( target, ACCESS.READ);
    }

    /**
     * modify access
     * @param target the target object
     * @return always true
     */
    public static boolean modify( final Object target)
    {
        return access( target, ACCESS.MODIFY);
    }

    /**
     * modify access
     * @param target the target object
     * @return always true
     */
    public static boolean access( final Object target)
    {
        return access( target, ACCESS.MODIFY);
    }

    /**
     * check the thread access of the object
     * @param target the target object
     * @param access the type of access
     * @return always true
     */
    public static boolean access( final Object target, final ACCESS access)
    {
        Object rt=realTarget( target);

        MONITOR_LOCK.readLock().lock();
        try
        {
            Brief brief;
            brief = MONITORED_OBJECTS.get(rt);
            if( brief != null)
            {
                if( null != brief.mode)
                switch (brief.mode) {
                    case ACCESS_ONLY_BY_CREATING_THREAD:
                        if( brief.ownerThreadID != Thread.currentThread().getId())
                        {
                            if( THROWING_ERROR.getAndSet(true) == false)
                            {
                                try
                                {
                                    throw new SingleThreadObjectAccessedByNonCreatingThreadError( rt + "#" + rt.hashCode() + " was created by " + brief.ownerThreadID + "\n" + CLogger.stackDump());
                                }
                                finally
                                {
                                    THROWING_ERROR.set(false);
                                }
                            }
                        }   break;
                    case EXTERNAL_SYNCHRONIZED:
                        if( brief.currentThread != null && brief.currentThread != Thread.currentThread())
                        {
                            throw new ConcurrentAccessError(
                                    rt + "#" + rt.hashCode() +
                                            " prevented from " + Thread.currentThread() +
                                            " as it's currently being accessed by " +
                                            brief.currentThread + "\n" +
                                            CLogger.stackDump()
                            );
                        }   break;
                    case READONLY:
                        if( access != ACCESS.READ)
                        {
                            throw new ReadOnlyObjectModifiedThreadError( rt + "#" + rt.hashCode() + " which is readonly is changed\n" + CLogger.stackDump());
                        }   break;
                    default:
                        break;
                }
            }
        }
        finally
        {
            MONITOR_LOCK.readLock().unlock();
        }

        return true;
    }

    /**
     * The are entering the target object
     *
     * @param target the target object
     * @return always true
     */
    public static boolean enter( final Object target)
    {
        return enter( target, ACCESS.MODIFY);
    }

    /**
     * The are entering the target object
     *
     * @param target the target object
     * @param access the type of access
     * @return always true
     */
    public static boolean enter( final Object target, final ACCESS access)
    {
        Object rt=realTarget( target);

        MONITOR_LOCK.readLock().lock();
        try
        {
            Brief brief;
            brief = MONITORED_OBJECTS.get(rt);
            if( brief != null)
            {
                if( brief.mode == MODE.DISABLED) return true;

                if( brief.mode == MODE.READONLY)
                {
                    if( access != ACCESS.READ)
                    {
                        throw new ReadOnlyObjectModifiedThreadError( rt + "#" + rt.hashCode() + " which is readonly is changed\n" + CLogger.stackDump());
                    }
                    return true;
                }

                if( brief.mode == MODE.ACCESS_ONLY_BY_CREATING_THREAD)
                {
                    if( brief.ownerThreadID != Thread.currentThread().getId())
                    {
                        throw new SingleThreadObjectAccessedByNonCreatingThreadError( rt + "#" + rt.hashCode() + " was created by " + brief.ownerThreadID + "\n" + CLogger.stackDump());
                    }
                }

                Thread bt=brief.currentThread;
                if( bt  == null)
                {
                    brief.currentThread=Thread.currentThread();
                }
                else if( bt != Thread.currentThread())
                {
                    throw new ConcurrentAccessError( rt + "#" + rt.hashCode() + " is currently being accessed by " + bt.getName() + "\n" + CLogger.stackDump());
                }

                brief.stackCount++;
            }
        }
        finally
        {
            MONITOR_LOCK.readLock().unlock();
        }

        return true;
    }

    /**
     * The are leaving the target object
     *
     * @param target the target object
     * @return always true
     */
    public static boolean leave( final Object target)
    {
        Object rt=realTarget( target);

        MONITOR_LOCK.readLock().lock();

        try
        {
            Brief brief;
            brief = MONITORED_OBJECTS.get(rt);
            if( brief != null)
            {
                if( brief.mode == MODE.DISABLED) return true;

                if( brief.mode == MODE.READONLY)
                {
                    return true;
                }

                if( brief.mode == MODE.ACCESS_ONLY_BY_CREATING_THREAD)
                {
                    if( brief.ownerThreadID != Thread.currentThread().getId())
                    {
                        throw new SingleThreadObjectAccessedByNonCreatingThreadError( rt + "#" + rt.hashCode() + " was created by " + brief.ownerThreadID + "\n" + CLogger.stackDump());
                    }
                }

                if( brief.currentThread  == null)
                {
                    throw new NeverEnteredError( rt + "#" + rt.hashCode()+ " was never entered");
                }
                else if( brief.currentThread != Thread.currentThread())
                {
                    throw new ConcurrentAccessError( rt + "#" + rt.hashCode() + " is currently being accessed by " + brief.currentThread + "\n" + CLogger.stackDump());
                }

                brief.stackCount--;

                if( brief.stackCount == 0)
                {
                    brief.currentThread=null;
                }
            }
        }
        finally
        {
            MONITOR_LOCK.readLock().unlock();
        }

        return true;
    }

    /**
     * increment the error count
     * @return the current count
     */
    public static long incrementErrorCount()
    {
        return ERROR_COUNT.incrementAndGet();
    }

    /**
     * How should this object be handled.
     */
    private static class Brief
    {
        long ownerThreadID = Thread.currentThread().getId();
        MODE mode;
        Thread currentThread;
        int stackCount;
        Stack<MODE> modeStack;

        Brief( final MODE mode)
        {
            this.mode = mode;
        }
    }
}
