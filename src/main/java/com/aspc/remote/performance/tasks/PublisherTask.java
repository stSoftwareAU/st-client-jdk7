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

import javax.jms.*;

import com.aspc.remote.performance.BenchMarkClient;
import com.aspc.remote.util.misc.CLogger;

import com.aspc.remote.util.misc.TimeUtil;
import org.apache.commons.logging.Log;

/**
 * This is a task handles publishing of messages
 * 
 * <I>THREAD MODE : SINGLE-THREADED </I>
 * 
 * @author sr83034
 * @since 29 September 2006
 */
public class PublisherTask extends JmsTask
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.performance.tasks.PublisherTask");//#LOGGER-NOPMD

    private String              message = null;

    /**
     * Publish JMS messages
     * @param name The name of the task
     * @param bmClient the benchmark client
     */
    public PublisherTask( String name, BenchMarkClient bmClient )
    {
        super( name, bmClient );
    }

    /**
     * Create a message
     */
    private void createMessage()
    {
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < getMessageSize() * 1024; i++ )
        {
            sb.append( (char)(32 + (126 - 32) * Math.random()) );
        }

        this.message = sb.toString();
    }

    /**
     * This function publishes
     * @throws Exception a serious problem
     */
    @Override
    protected void process() throws Exception
    {
        try
        {
            /**
             * This will initialize the connection and the sessions etc.
             */
            createConnectionsEtc();

            /**
             * If all is well - create sample message
             */
            createMessage();

            /**
             * Create a publisher and send messages
             */
            TopicPublisher topicPublisher = topicSession.createPublisher( topic );

            long lastTime = System.currentTimeMillis();
            long startTime = lastTime;
            for( int i = 0; i < getMessageCount(); i++ )
            {
                TextMessage tmpMessage = topicSession.createTextMessage();
                tmpMessage.setStringProperty( CLIENT_ID, getName() );
                tmpMessage.setStringProperty( MESSAGE_ID, String.valueOf( i ) );
                tmpMessage.setText( this.message );
                topicPublisher.publish( tmpMessage );
                long now = System.currentTimeMillis();

                if( now > lastTime + 10000)
                {
                    LOGGER.info( getName() + " " + i + " ! in " + TimeUtil.getDiff(startTime) );
                    lastTime = now;
                }

                /**
                 * Sleep
                 */
                if( getReceiveDelay() > 0 )
                {
                    Thread.sleep( getReceiveDelay() );
                }
            }
        }
        finally
        {
            closeConnectionsEtc();
        }
    }
}
