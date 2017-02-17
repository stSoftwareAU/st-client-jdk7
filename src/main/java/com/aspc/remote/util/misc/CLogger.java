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
package com.aspc.remote.util.misc;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  This is the static logger
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 * @author  Jonathan Lee
 * @since 29 September 2006
 */
@SuppressWarnings({"empty-statement", "CallToThreadDumpStack", "CallToPrintStackTrace"})
public final class CLogger
{
    /**
     * debug level
     */
    public static final String LEVEL_DEBUG="debug";

    /**
     * info level
     */
    public static final String LEVEL_INFO="info";

    /**
     * warning level
     */
    public static final String LEVEL_WARN="warn";

    /**
     * error level
     */
    public static final String LEVEL_ERROR="error";

    /**
     * fatal level
     */
    public static final String LEVEL_FATAL="fatal";

    public static final boolean DISABLE_QUEUE_LOG;
    public static final String ENV_DISABLE_QUEUE_LOG="DISABLE_QUEUE_LOG";

    private CLogger()
    {

    }

    /** The System property that contains the file name of the logging properties */
    public static final String LOG_PROPERTIES = "LOG_PROPERTIES";

    /**
     * log a fatal message via a background thread
     * @param logger the logger to use
     * @param message the message
     * @param cause the cause
     */
    public static void fatal( final @Nonnull Log logger, final @Nonnull String message, final @Nullable Throwable cause)
    {      
        QueueLog.find(logger).fatal(message, cause);
    }

    /**
     * log a fatal message via a background thread
     * @param logger the logger to use
     * @param message the message
     */
    public static void fatal( final @Nonnull Log logger, final @Nonnull String message)
    {
        fatal(logger, message, null);
    }

    /**
     * log a error message via a background thread
     * @param logger the logger to use
     * @param message the message
     * @param cause the cause
     */
    public static void error( final @Nonnull Log logger, final @Nonnull String message, final @Nullable Throwable cause)
    {       
        QueueLog.find(logger).error(message, cause);
    }

    /**
     * log a error message via a background thread
     * @param logger the logger to use
     * @param message the message
     */
    public static void error( final @Nonnull Log logger, final @Nonnull String message)
    {
        error(logger, message, null);
    }

    /**
     * log a warning message via a background thread
     * @param logger the logger to use
     * @param message the message
     * @param cause the cause
     */
    public static void warn( final @Nonnull Log logger, final @Nonnull String message, final @Nullable Throwable cause)
    {       
        QueueLog.find(logger).warn(message, cause);
    }

    /**
     * log a warning message via a background thread
     * @param logger the logger to use
     * @param message the message
     */
    public static void warn( final @Nonnull Log logger, final @Nonnull String message)
    {       
        warn(logger, message, null);
    }

    /**
     * log a info message via a background thread
     * @param logger the logger to use
     * @param message the message
     * @param cause the cause
     */
    public static void info( final @Nonnull Log logger, final @Nonnull String message, final @Nullable Throwable cause)
    {
        QueueLog.find(logger).info( message, cause);
    }

    /**
     * log a info message via a background thread
     * @param logger the logger to use
     * @param message the message
     */
    public static void info( final @Nonnull Log logger, final @Nonnull String message)
    {
        info( logger, message, null);
    }

    /**
     * log a debug message via a background thread
     * @param logger the logger to use
     * @param message the message
     * @param cause the cause
     */
    public static void debug( final @Nonnull Log logger, final @Nonnull String message, final @Nullable Throwable cause)
    {
        QueueLog.find(logger).debug(message, cause);
    }

    /**
     * log a debug message via a background thread
     * @param logger the logger to use
     * @param message the message
     */
    public static void debug( final @Nonnull Log logger, final @Nonnull String message)
    {
        debug(logger, message, null);
    }

    /**
     * schedule a log event in a separate thread so that we do not block
     *
     * @param logger the logger
     * @param level the level (debug, info, warn, error or fatal)
     * @param message the message to log
     * @param cause the cause
     */
    public static void schedule( final @Nonnull Log logger, final @Nullable String level, final @Nonnull String message, final @Nullable Throwable cause)
    {
        QueueLog ql = QueueLog.find(logger);

        if( LEVEL_DEBUG.equalsIgnoreCase(level))
        {
            ql.debug(message, cause);
        }
        else if( LEVEL_INFO.equalsIgnoreCase(level))
        {
            ql.info(message, cause);
        }
        else if( LEVEL_WARN.equalsIgnoreCase(level))
        {
            ql.warn(message, cause);
        }
        else if( LEVEL_ERROR.equalsIgnoreCase(level))
        {
            ql.error(message, cause);
        }
        else
        {
            ql.fatal(message, cause);
        }
    }

    /**
     *
     * @param buffer
     */
    public static void requestDump(final @Nonnull Appendable buffer)
    {
        StringWriter w = new StringWriter();
        @SuppressWarnings({"ThrowableInstanceNotThrown", "ThrowableInstanceNeverThrown"})
        Throwable t = new Throwable();
        t.printStackTrace( new PrintWriter( w));
        String dump = w.toString();
        // Skip Throwable line
        int pos = dump.indexOf( '\n');
        // Skip requestDump link
        if( pos != -1)
        {
            pos = dump.indexOf( '\n', pos+1);
        }
        if( pos == -1)
        {
            pos = 0;
        }
        else
        {
            pos++;
        }
        try
        {
            buffer.append( dump.substring( pos));
        }
        catch( IOException io)
        {
            
        }
    }

    /**
     * dump the stack
     * @return the stack dump.
     */
    @CheckReturnValue @Nonnull
    public static String stackDump()
    {
        final StringBuilder buffer=new StringBuilder();
        
        try
        {
            Map<Thread, StackTraceElement[]> st = Thread.getAllStackTraces();
            for (Map.Entry<Thread, StackTraceElement[]> e : st.entrySet())
            {
                StackTraceElement[] el = e.getValue();
                Thread t = e.getKey();
                buffer.append("\"").
                        append(t.getName()).
                        append("\"" + " ").
                        append(t.isDaemon() ? "daemon" : "").
                        append(" prio=").
                        append(t.getPriority()).
                        append(" Thread id=").
                        append(t.getId()).append(" ").
                        append(t.getState()).
                        append("\n");

                for (StackTraceElement line : el)
                {
                    buffer.append("\tat ").append(line);
                    buffer.append("\n");
                }
                buffer.append("\n");
            }

        }
        catch (Exception e)
        {
             buffer.append("could not get stack traces: ").append(e.getMessage());
        }
        
        return buffer.toString();
    }

    /**
     * throws as a runtime exception
     * @param e the exception
     * @return never actually returns.
     */
    @Nonnull
    public static RuntimeException rethrowRuntimeExcepton( final @Nonnull Throwable e)
    {
        StackTraceElement stack[] = e.getStackTrace();

        try
        {
            if( e instanceof Error)
            {
                throw (Error)e;
            }
            else if( e instanceof RuntimeException )
            {
                throw (RuntimeException)e;
            }
            else
            {
                RuntimeException rt = new RuntimeException( "rethrow", e);
                rt.setStackTrace(stack);
                throw rt;
            }
        }
        finally
        {
            e.setStackTrace( stack);
        }
    }

    /**
     * throws as a runtime exception
     * @param e the exception
     * @return doesn't ever return. 
     * @throws Exception the exception
     */
    @Nonnull
    public static Exception rethrowException( final @Nonnull Throwable e) throws Exception
    {
        StackTraceElement stack[] = e.getStackTrace();

        try
        {
            if( e instanceof Error)
            {
                throw (Error)e;
            }
            else if( e instanceof Exception )
            {
                throw (Exception)e;
            }
            else
            {
                Exception e2 = new Exception( "rethrow", e);
                e2.setStackTrace(stack);
                throw e2;
            }
        }
        finally
        {
            e.setStackTrace( stack);
        }
    }

    /**
     * gets a Log4J logger
     * @param name path name.
     * @return logger
     */
    @CheckReturnValue @Nonnull
    public static Log getLog(final @Nonnull String name)
    {
        Log l = LogFactory.getLog(name);
        if( DISABLE_QUEUE_LOG || QueueLog.QUEUE_LIMIT <= 0)
        {
            return l;
        }

        return QueueLog.find(l);
    }

    static
    {
        String tmp=System.getProperty(ENV_DISABLE_QUEUE_LOG, "N");

        DISABLE_QUEUE_LOG = tmp.matches("(Y|t|T|y).*");

        try
        {
            Class configClass = Class.forName("com.aspc.remote.util.misc.CLoggerConfig");
            Method configMethod = configClass.getMethod("configure");
            configMethod.invoke(null);
        }
        catch(ClassNotFoundException | SecurityException ce)
        {
            ; // it may be Ok when the applet version is used
        }
        catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException me)
        {
            me.printStackTrace();//NOPMD
        }
    }
}
