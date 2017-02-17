/*
 *  Copyright (c) 2000-2006 ASP Converters pty ltd
 *
 *  www.aspconverters.com.au
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

import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.misc.TimeUtil;
import org.apache.commons.logging.Log;

/**
 * This class handles subscription
 * 
 * <I>THREAD MODE : SINGLE-THREADED </I>
 * 
 * @author sr83034
 * @since 29 September 2006
 */
public class SubscriberTask extends JmsTask
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.performance.tasks.SubscriberTask");//#LOGGER-NOPMD

    /**
     * @param name
     * @param bmClient
     */
    public SubscriberTask( String name, BenchMarkClient bmClient )
    {
        super( name, bmClient );
    }

    /**
     * This function subscribes
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
             * Create suibscriber and set the listener
             */
            TopicSubscriber topicSubscriber = this.topicSession.createSubscriber( topic );
            TextListener topicListener = new TextListener();
            long delay = getReceiveDelay();
            topicListener.setDelay( delay );
            long delayList[] = getReceiveDelayList();
            topicListener.setDelayList( delayList );
            topicListener.setMessageCount( getMessageCount());
            if( StringUtilities.isBlank( listenDuration)==false)
            {
                topicListener.setListenDuration(TimeUtil.convertDurationToMs(listenDuration));
                
            }
            topicSubscriber.setMessageListener( topicListener );

            /**
             * start the connection
             */
            this.topicConnection.start();

            LOGGER.debug( "Subscriber started !" );
 
            /**
             * Wait until we receive a shutdown
             */
             topicListener.waitToComplete();
             
        }
        finally
        {
            closeConnectionsEtc();
        }

        LOGGER.debug( "Subscriber stopped !" );
    }
}
