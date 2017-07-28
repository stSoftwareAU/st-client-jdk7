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

import javax.jms.TextMessage;
import javax.jms.TopicPublisher;

import com.aspc.remote.performance.BenchMarkClient;

/**
 * This class simply publishes a shutdown message
 * 
 * <I>THREAD MODE : SINGLE-THREADED </I>
 * 
 * @author sr83034
 * @since 29 September 2006
 */
public class ShutdownSubscriberTask extends PublisherTask
{
    /**
     * @param name
     * @param bmClient
     */
    public ShutdownSubscriberTask( String name, BenchMarkClient bmClient )
    {
        super( name, bmClient );
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
             * Create a shutdown message
             */
            TextMessage shutdownMessage = topicSession.createTextMessage( "SHUTDOWN");
            shutdownMessage.setStringProperty( SYSTEM_MESSAGE, SYSTEM_MESSAGE_SHUTDOWN );

            /**
             * Send the system message last
             */
            TopicPublisher topicPublisher = topicSession.createPublisher( topic );
            topicPublisher.publish( shutdownMessage );
        }
        finally
        {
            closeConnectionsEtc();
        }
    }
}
