/*
 *  Copyright (c) 2000-2006 ASP Converters pty ltd
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.performance.tasks;

/**
 * This is a implementation of message listener
 * 
 * <I>THREAD MODE : SINGLE-THREADED</I>
 * 
 * @author sr83034
 * @since 29 September 2006
 */
import javax.jms.*;

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.TimeUtil;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.logging.Log;

/**
 *
 * @author nigel
 */
public class TextListener implements MessageListener
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.performance.tasks.TextListener");//#LOGGER-NOPMD

    private long                delay    = 0;
    private final boolean             shutdown[] = {false};
    private long expectedMessageCount;
    private Exception error;
    private long listenDuration;
    private final HashSet uniqueCheck=new HashSet();
    private final long startTime;
    private long lastTime;
    private final AtomicLong receiveCount=new AtomicLong();
    private long delayList[];

    /**
     *
     */
    public TextListener()
    {
        startTime = System.currentTimeMillis();
    }

    /**
     *
     * @param delayList
     */
    public void setDelayList( long delayList[])
    {
        this.delayList=delayList;
    }

    /**
     *
     * @param time
     */
    public void setListenDuration( long time)
    {
        listenDuration=time;
    }

    /**
     *
     * @param count
     */
    public void setMessageCount( long count)
    {
        expectedMessageCount=count;
    }

    /**
     * Checks if received shutdown
     * 
     * @throws Exception a serious problem.
     */
    public void waitToComplete() throws Exception
    {
        while( true)
        {
            if( error != null)
            {
                throw error;
            }
            
            if( listenDuration >0)
            {
                long now = System.currentTimeMillis();
                long endTime = startTime + listenDuration;
                if( endTime < now)
                {
                    throw new Exception( receiveCount + " messages exceed max listen time " + TimeUtil.getDiff(startTime, now));
                }
            }

            if( expectedMessageCount >0)
            {
                if( expectedMessageCount > receiveCount.get())
                {
                    Thread.sleep(100);
                    continue;
                }
                else
                {
                    return;
                }
            }

            synchronized( shutdown)
            {
                boolean flag = shutdown[0];
                if( flag == false)
                {
                    shutdown.wait( 5000);
                }
                else
                {
                    break;
                }
            }
        }
    }

    /**
     * Casts the message to a TextMessage and displays its text.
     * 
     * 
     * @param _message 
     */
    @Override
    public void onMessage( final Message _message )
    {
        if( _message instanceof TextMessage )
        {
            receiveCount.incrementAndGet();
            long now = System.currentTimeMillis();

            if( now > lastTime + 10000)
            {
                LOGGER.info( "received " + receiveCount + " ! in " + TimeUtil.getDiff(startTime) );
                lastTime = now;
            }
            try
            {
                String clientId = _message
                        .getStringProperty( JmsTask.CLIENT_ID );
                String messageId = _message
                        .getStringProperty( JmsTask.MESSAGE_ID );
                String key = clientId + ":" + messageId;
                if( uniqueCheck.contains(key))
                {
                    throw new Exception( "message already received " + key);
                }

                if( uniqueCheck.size() > 10000) uniqueCheck.clear();

                uniqueCheck.add(key);

                LOGGER.debug( "received - " + clientId + ":"
                        + messageId );

                /**
                 * If the message is a system message then unlatch..
                 * 
                 * This will stop the subscriber
                 */
                if( _message.propertyExists( JmsTask.SYSTEM_MESSAGE ) )
                {
                    String sysMessage = _message.getStringProperty(JmsTask.SYSTEM_MESSAGE);
                    if( sysMessage
                            .equalsIgnoreCase( JmsTask.SYSTEM_MESSAGE_SHUTDOWN ) )
                    {
                        LOGGER.debug( "System message is a shutdown" );
                        synchronized( shutdown)
                        {
                            shutdown[0] = true;
                            shutdown.notifyAll();
                        }
                    }
                }

                if( delay > 0)
                {
                    boolean doDelay=false;

                    if( delayList == null || delayList.length==0)
                    {
                        doDelay=true;
                    }
                    else
                    {
                        for( long messageCounter : delayList)
                        {
                            if( messageCounter == receiveCount.get())
                            {
                                doDelay=true;
                                break;
                            }
                        }
                    }

                    if( doDelay)
                    {
                        /**
                         * Sleep
                         */
                        Thread.sleep( delay );
                    }
                }
            }
            catch( Exception e )
            {
                error = e;
            }
        }
    }

    /**
     * @return Returns the receiveDelay.
     */
    public long getDelay()
    {
        return delay;
    }

    /**
     * 
     * 
     * @param delay 
     */
    public void setDelay( long delay )
    {
        this.delay = delay;
    }
}
