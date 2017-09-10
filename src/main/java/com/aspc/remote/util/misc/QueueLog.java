/**
 *  STS Remote library
 *
 *  Copyright (C) 2006  stSoftware Pty Ltd
 *
 *  stSoftware.com.au
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
package com.aspc.remote.util.misc;
//SERVER-START
import com.aspc.remote.memory.MemoryHandler;
import com.aspc.remote.memory.MemoryManager;
//SERVER-END
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;

/**
 *  Queued logger
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 * @author  Nigel Leck
 * @since 27 June 2012
 */
@SuppressWarnings({"empty-statement", "CallToThreadDumpStack"})
public final class QueueLog implements Log
{
    private static final ConcurrentHashMap<Log, QueueLog>LOGS_MAP=new ConcurrentHashMap<>();
    public static final int QUEUE_LIMIT;
    private static final long MAX_BLOCK;
    private static final String ENVIRONMENT_QUEUE_LOG_LIMIT="QUEUE_LOG_LIMIT";
    private static final String ENVIRONMENT_QUEUE_LOG_MAX_BLOCK="QUEUE_LOG_MAX_BLOCK";
    private static final BlockingQueue<LogMessage> QUEUE;
    private static final Lock QUEUE_LOCK = new ReentrantLock();
    private static final Condition EMPTY_QUEUE  = QUEUE_LOCK.newCondition();
    private static final AtomicBoolean LOW_MEMORY=new AtomicBoolean();
    
    private static final LinkedHashMap<String,PatternMask> MASK_LIST  = new LinkedHashMap();
    private static PatternMask masks[]={};
    private static final Thread RUNNER;

    public final Log wrappedLog;

    private QueueLog( final Log log)
    {
        wrappedLog=log;
    }

    /**
     * The number of log mask patterns.
     * @return The count.
     */
    @Nonnegative
    public static int getLogMaskCount()
    {
        return masks.length;
    }
    
    /**
     * Flush the remaining messages
     * @param timeToWaitMS the number of milliseconds to wait
     */
    @SuppressWarnings({"SleepWhileInLoop", "AccessingNonPublicFieldOfAnotherObject"})
    public static void flush( final long timeToWaitMS)
    {
        long start=System.currentTimeMillis();
        while( QUEUE.isEmpty() == false)
        {
            long now = System.currentTimeMillis();
            if( now > start + timeToWaitMS)
            {
                @SuppressWarnings("UseOfSystemOutOrSystemErr")
                PrintStream out=System.out;
                out.println( "LOST " + QUEUE.size() + " MESSAGES");
                break;
            }
            QUEUE_LOCK.lock();
            try
            {
                EMPTY_QUEUE.await(100, TimeUnit.MILLISECONDS);
            }
            catch( InterruptedException ie)
            {

            }
            finally
            {
                QUEUE_LOCK.unlock();
            }
        }
    }

    /**
     * find the queue log.
     * @param log the log that should be wrapped.
     * @return the queue log.
     */
    @CheckReturnValue
    public static QueueLog find( final Log log)
    {
        if( log instanceof QueueLog)
        {
            return (QueueLog)log;
        }

        QueueLog ql=LOGS_MAP.get( log);

        if( ql == null)
        {
            ql = new QueueLog( log);

            QueueLog ql2 = LOGS_MAP.putIfAbsent(log, ql);

            if( ql2 != null) return ql2;
        }

        return ql;
    }

    /* {@inheritDoc} */
    @Override @CheckReturnValue
    public boolean isDebugEnabled()
    {
        return wrappedLog.isDebugEnabled();
    }

    /* {@inheritDoc} */
    @Override @CheckReturnValue
    public boolean isErrorEnabled()
    {
        return wrappedLog.isErrorEnabled();
    }

    /* {@inheritDoc} */
    @Override @CheckReturnValue
    public boolean isFatalEnabled()
    {
        return wrappedLog.isFatalEnabled();
    }

    /* {@inheritDoc} */
    @Override @CheckReturnValue
    public boolean isInfoEnabled()
    {
        return wrappedLog.isInfoEnabled();
    }

    /* {@inheritDoc} */
    @Override @CheckReturnValue
    public boolean isTraceEnabled()
    {
        return false;
    }

    /* {@inheritDoc} */
    @Override @CheckReturnValue
    public boolean isWarnEnabled()
    {
        return wrappedLog.isWarnEnabled();
    }

    /**
     * schedule a log event in a separate thread so that we do not block
     *
     * @param logger the logger
     * @param level the level (debug, info, warn, error or fatal)
     * @param obj the message to log
     * @param cause the cause
     */
    private void schedule( final String level, final @Nullable Object obj, final @Nullable Throwable cause)
    {
        String message = obj != null ? obj.toString() : "";

        Thread currentThread = Thread.currentThread();
        final String name = currentThread.getName();
        LogMessage msg = new LogMessage(
            wrappedLog,
            name,
            message,
            level,
            cause
        );

        if(LOW_MEMORY.get())
        {
            QUEUE_LOCK.lock();
            try
            {
                if( QUEUE.isEmpty()==false)
                {
                    EMPTY_QUEUE.await(60, TimeUnit.SECONDS);
                }
            }
            catch( InterruptedException ie)
            {

            }
            finally
            {
                QUEUE_LOCK.unlock();
            }
            
            msg.write();
            //SERVER-START
            LOW_MEMORY.set(MemoryManager.isFull());
            //SERVER-END
        }
        else if( QUEUE_LIMIT >0)
        {
            long start=System.currentTimeMillis();
            while( true)
            {
                try
                {
                    QUEUE.add(msg);
                    return;
                }
                catch( IllegalStateException e)
                {
                    if( ThreadUtil.isAliveOrStarting(RUNNER))
                    {
                        QUEUE_LOCK.lock();
                        try
                        {
                            if( QUEUE.size() > 0)
                            {
                                EMPTY_QUEUE.await(1000, TimeUnit.MILLISECONDS);
                            }
                        }
                        catch( InterruptedException ie)
                        {
                            /**
                             * Do not log as an interrupted thread can't do I/O so the messages isn't excepted 
                             */
                            currentThread.interrupt();
                            break;
                        }
                        finally
                        {
                            QUEUE_LOCK.unlock();
                        }
                    }
                    else
                    {
                        msg.write();
                        return;
                    }
                }
                
                if(MAX_BLOCK> 0)
                {
                    long now = System.currentTimeMillis();
                    if( now > start + MAX_BLOCK)
                    {
                        @SuppressWarnings("UseOfSystemOutOrSystemErr")
                        PrintStream out=System.out;
                        out.println( "Thread '" + name + "' LOST MESSAGE after " + TimeUtil.getDiff(start, now));
                        out.println( message); 
                        break;
                    }
                }
            }
        }
        else
        {            
            msg.write();
        }
    }

    /* {@inheritDoc} */
    @Override
    public void trace(final Object o)
    {
    }

    /* {@inheritDoc} */
    @Override
    public void trace(final Object o, final Throwable thrwbl)
    {
    }

    /* {@inheritDoc} */
    @Override
    public void debug(Object o)
    {
        if( wrappedLog.isDebugEnabled())
        {
            schedule( CLogger.LEVEL_DEBUG, o, null);
        }
    }

    /* {@inheritDoc} */
    @Override
    public void debug(final Object o, final Throwable thrwbl)
    {
        if( wrappedLog.isDebugEnabled())
        {
            schedule( CLogger.LEVEL_DEBUG, o, thrwbl);
        }
    }

    /* {@inheritDoc} */
    @Override
    public void info(final Object o)
    {
        if( wrappedLog.isInfoEnabled())
        {
            schedule( CLogger.LEVEL_INFO, o, null);
        }
    }

    /* {@inheritDoc} */
    @Override
    public void info(final Object o, final Throwable thrwbl)
    {
        if( wrappedLog.isInfoEnabled())
        {
            schedule( CLogger.LEVEL_INFO, o, thrwbl);
        }
    }

    /* {@inheritDoc} */
    @Override
    public void warn(final Object o)
    {
        if( wrappedLog.isWarnEnabled())
        {
            schedule( CLogger.LEVEL_WARN, o, null);
        }
    }

    /* {@inheritDoc} */
    @Override
    public void warn(final Object o, final Throwable thrwbl)
    {
        if( wrappedLog.isWarnEnabled())
        {
            schedule( CLogger.LEVEL_WARN, o, thrwbl);
        }
    }

    /* {@inheritDoc} */
    @Override
    public void error(final Object o)
    {
        if( wrappedLog.isErrorEnabled())
        {
            schedule( CLogger.LEVEL_ERROR, o, null);
        }
    }

    /* {@inheritDoc} */
    @Override
    public void error(final Object o, final Throwable thrwbl)
    {
        if( wrappedLog.isErrorEnabled())
        {
            schedule( CLogger.LEVEL_ERROR, o, thrwbl);
        }
    }

    /* {@inheritDoc} */
    @Override
    public void fatal(final Object o)
    {
        if( wrappedLog.isFatalEnabled())
        {
            schedule( CLogger.LEVEL_FATAL, o, null);
        }
    }

    /* {@inheritDoc} */
    @Override
    public void fatal(final Object o, final Throwable thrwbl)
    {
        if( wrappedLog.isFatalEnabled())
        {
            schedule( CLogger.LEVEL_FATAL, o, thrwbl);
        }
    }

    private static class LogMessage
    {
        private final Log log;
        private final String name;
        private final String msg;
        private final String level;
        private final Throwable cause;

        LogMessage(
            final Log log,
            final String name,
            final String msg,
            final String level,
            final Throwable cause
        )
        {
            this.log=log;
            this.name=name;
            this.msg=msg;
            this.level=level;
            this.cause=cause;
        }

        void write()
        {
            Thread t= Thread.currentThread();
            String orginalName=t.getName();
            try
            {
                String tmpMsg = maskLogMessage(msg);

                t.setName(name);

                if( level.equalsIgnoreCase(CLogger.LEVEL_DEBUG))
                {
                    log.debug(tmpMsg, cause);
                }
                else if( level.equalsIgnoreCase(CLogger.LEVEL_INFO))
                {
                    log.info(tmpMsg, cause);
                }
                else if( level.equalsIgnoreCase(CLogger.LEVEL_WARN))
                {
                    log.warn(tmpMsg, cause);
                }
                else if( level.equalsIgnoreCase(CLogger.LEVEL_ERROR))
                {
                    log.error(tmpMsg, cause);
                }
                else
                {
                    log.fatal(tmpMsg, cause);
                }
            }
            finally
            {
                t.setName(orginalName);
            }
        }
    }

    private static class LogRunner implements Runnable
    {
        @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch", "CallToPrintStackTrace"})
        @Override
        public void run()
        {
            while( true)
            {
                try
                {
                    LogMessage lm = QUEUE.poll();
                    if( lm == null)
                    {
                        QUEUE_LOCK.lock();
                        try
                        {
                            EMPTY_QUEUE.signalAll();
                        }
                        finally
                        {
                            QUEUE_LOCK.unlock();
                        }
                        lm = QUEUE.take();
                    }

                    lm.write();
                }
                catch( InterruptedException ie)
                {
                    // Applet finished.
                    break;
                }
                catch( Throwable t)
                {
                    t.printStackTrace();
                }
            }
        }
    }

    static
    {
        String tmp=System.getProperty(ENVIRONMENT_QUEUE_LOG_LIMIT, "2048");
        QUEUE_LIMIT=Integer.parseInt(tmp);
        tmp=System.getProperty(ENVIRONMENT_QUEUE_LOG_MAX_BLOCK, "60000");
        MAX_BLOCK=Long.parseLong(tmp);
        QUEUE=new LinkedBlockingQueue<>( QUEUE_LIMIT > 0 ? QUEUE_LIMIT:1);
        RUNNER=new Thread(new LogRunner(), "log runner - IDLE");
        RUNNER.setPriority( Thread.MIN_PRIORITY);
        RUNNER.setDaemon(true);

        RUNNER.start();
        //SERVER-START
        MemoryManager.register(new MemoryHandler() {
            @Override
            public MemoryHandler.Cost getCost() {
                return MemoryHandler.Cost.HIGH;
            }

            @Override
            public long freeMemory(double percentage) {
                return 0;
            }

            @Override
            public long tidyUp() {
                return 0;
            }

            @Override
            public long queuedFreeMemory(double percentage) {
                LOW_MEMORY.set(MemoryManager.isFull());
                return 0;
            }

            @Override
            public long panicFreeMemory() {
                LOW_MEMORY.set(MemoryManager.isFull());
                return 0;
            }

            @Override
            public long getEstimatedSize() {
                return QUEUE.size()*1024;
            }

            @Override
            public long getLastAccessed() {
                return System.currentTimeMillis();
            }
        });
        //SERVER-END
    }

    /**
     * Mask log message
     * @param msg the message
     * @return the value
     */
    public static String maskLogMessageV2(final String msg)
    {
        if( masks.length==0 || StringUtilities.isBlank(msg))
        {
            return msg;
        }

        String tmpMsg = msg;

        for( PatternMask patternMask: masks)
        {
            StringBuilder sb=new StringBuilder( tmpMsg.length());

            for( String line: tmpMsg.split("[\n\r]+"))
            {
                if( sb.length()>0)
                {
                    sb.append("\n");
                }
//                StringBuilder result=null;
                int index = 0;

                Matcher matcher = patternMask.pattern.matcher(line);

                while (matcher.find())
                {
//                    if( result == null)
//                    {
//                         result= new StringBuilder();
//                    }
                    int groupNumber = patternMask.groupNumber;

                    if (groupNumber > 0)
                    {
                        int start = matcher.start(groupNumber);
                        int end = matcher.end(groupNumber);
                        sb.append(line.substring(index, start ) );
                        sb.append(patternMask.mask);
                        index = end;
                    }
                    else
                    {
                        int start = matcher.start();
                        int end = matcher.end();
                        sb.append(line.substring(index, start ) );
                        sb.append(patternMask.mask);
                        index = end;
                    }
                }
//                if( result!=null)
//                {
                    sb.append(line.substring(index));
//                    line = result.toString();
//                }
            }
            tmpMsg=sb.toString();
        }
        
        return tmpMsg;
    }

    /**
     * Mask log message
     * @param msg the message
     * @return the value
     */
    public static String maskLogMessage(final String msg)
    {
        String tmpMsg = msg;

        for( PatternMask patternMask: masks)
        {
            StringBuilder result=null;
            int index = 0;

            Matcher matcher = patternMask.pattern.matcher(tmpMsg);

            while (matcher.find())
            {
                if( result == null)
                {
                     result= new StringBuilder();
                }
                int groupNumber = patternMask.groupNumber;

                if (groupNumber > 0)
                {
                    int start = matcher.start(groupNumber);
                    int end = matcher.end(groupNumber);
                    result.append(tmpMsg.substring(index, start ) );
                    result.append(patternMask.mask);
                    index = end;
                }
                else
                {
                    int start = matcher.start();
                    int end = matcher.end();
                    result.append(tmpMsg.substring(index, start ) );
                    result.append(patternMask.mask);
                    index = end;
                }
            }
            if( result!=null)
            {
                result.append(tmpMsg.substring(index));
                tmpMsg = result.toString();
            }
        }
        return tmpMsg;
    }

    /**
     * Add log patterns
     * @param strPattern regex pattern
     * @param mask mask
     * @param groupNumber group number
     */
    public static void addPatternMask(final String strPattern,final String mask,final Integer groupNumber)
    {
        synchronized( MASK_LIST)
        {
            if (MASK_LIST.containsKey(strPattern) == false)
            {

                PatternMask patternMask = new PatternMask(strPattern,mask,groupNumber);
                                
                MASK_LIST.put(strPattern, patternMask);
                PatternMask t[]=new PatternMask[MASK_LIST.size()];
                MASK_LIST.values().toArray(t);
                masks=t;
            }
        }
    }

    private static class PatternMask
    {
        public final String mask;
        public final int groupNumber;
        public final Pattern pattern;

        PatternMask(final String patternStr, final String mask,final Integer groupNumber)
        {
            this.pattern = Pattern.compile(patternStr);
            if (StringUtilities.isBlank(mask))
            {
                this.mask = "********";
            }
            else
            {
                this.mask = mask;
            }
            if (groupNumber == null)
            {
                this.groupNumber = 0;
            }
            else
            {
                int value = groupNumber;
                assert value >= 0: "group must NOT be negative " + value;
                this.groupNumber = groupNumber;
            }
        }

        @Override @CheckReturnValue @Nonnull
        public String toString()
        {
            return "PatternMask{" + "patternr=" + pattern.pattern() + ", mask=" + mask + ", groupNumber=" + groupNumber + '}';
        }
    }
}
