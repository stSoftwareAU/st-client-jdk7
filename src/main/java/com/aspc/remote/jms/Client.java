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
package com.aspc.remote.jms;

import com.aspc.remote.application.Shutdown;
import com.aspc.remote.application.ShutdownListener;
import com.aspc.remote.jms.internal.JMSCloser;
import com.aspc.remote.jms.internal.JMSConnector;
import com.aspc.remote.util.misc.*;
import com.aspc.remote.util.net.NetUtil;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.management.JMSConnectionStatsImpl;
import org.apache.commons.logging.Log;


/**
 *  MT Thread safe JMS client.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       29 April 2005
 */
public abstract class Client implements ExceptionListener,ShutdownListener//, TransportStatusEventListener
{
    /** the max inactivity duration */
    public static final int MAX_INACTIVITY_DURATION;

    /** the default max inactivity duration */
    private static final int DEFAULT_MAX_INACTIVITY_DURATION=15 * 60 * 1000;

    /** the property to control max inactivity duration */
    public static final String PROPERTY_MAX_INACTIVITY_DURATION="MAX_INACTIVITY_DURATION";

    /**
     * Environment Variable to turn JMS off.
     */
    public static final String DISABLE_JMS="JMS";

    /**
     * The maximum time we will wait for a JMS message to be sent.
     */
    public static final long JMS_TIMEOUT;

    /**
     * Environment variable to control The maximum time we will wait for a JMS message to be sent.
     */
    public static final String ENV_JMS_TIMEOUT="JMS_TIMEOUT";

    /**
     * Environment variable to control the max transfer time in milliseconds
     */
    public static final String ENV_JMS_MAX_LIVE_MS="JMS_MAX_LIVE_MS";

    /**
     * The pending count that we will send out an error
     */
    public static final String ENV_JMS_PENDING_COUNT="JMS_PENDING_COUNT";
    /**
     * The time that this message was sent.
     */
    public static final String ATTRIBUTE_SENT_MS="sent_ms";

    /**
     * The transaction for this message.
     */
    public static final String ATTRIBUTE_TRANS_NR="trans_nr";

    /**
     * The transaction milliseconds for this message.
     */
    public static final String ATTRIBUTE_TRANS_MS="trans_ms";

    private static final long MAX_LIVE_MS;

    private long lastPendingWarningMS;
    private static final long JMS_PENDING_COUNT;
    /**
     * the last time we connected to the JMS server
     */
    private Date lastConnected;

    /**
     * current connection
     */
    private TopicConnection currentConnection;

    /**
     * current publisher
     */
    private TopicPublisher currentPublisher;

    private String cacheID;

    /**
     * current session
     */
    private TopicSession currentSession;

    /**
     * current subscriber
     */
    private TopicSubscriber currentSubscriber;

    /** Initial context for the JMS */
    private String requiredJmsContext;

    /** The URL of the JMS */
    private String requiredJmsProviderURL;

    /**
     * A spin lock on the JMS.
     */
    private final SyncBlock lockJMS = new SyncBlock( "JMS block", 300 );

    private boolean jmsServerDown=false;

    private int connectionCount;

    /**
     * How many messages have been sent ?
     */
    private long totalSent;

    /**
     * The logger for the master db
     */
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.jms.Client");//#LOGGER-NOPMD

    /**
     * The context factory to use
     *
     * @return The context factory
     */
    public abstract String getContextFactory();

    /**
     * get the provider URL to use.
     *
     * @return the provider URL.
     */
    public abstract String getProviderUrl();

    /**
     * reload the context from the database or where ever.
     *
     * @throws Exception A serious error.
     */
    protected void refreshContext() throws Exception
    {

    }

    /**
     * Do we have a JMS
     *
     * @return TRUE if we are using a JMS
     */
    public boolean isEnabled()
    {
        return StringUtilities.isBlank( requiredJmsProviderURL) == false;
    }

    /**
     * Send the XML message to the message server.
     *
     * If the message server is not connected yet then connect.
     *
     * If will fail to send the message because of illegal state then close the
     * connection and try again as the message server was probably been restarted
     * @param message The message text to send.
     * @throws Exception A serious message
     */
    public void send( final String message) throws Exception
    {
        long startMS = System.currentTimeMillis();
//        long pendingCount = 0;

        lockJMS.take();
        try
        {
            for( int loop = 0; loop < 2; loop++)
            {
                if( isConnected() == false)
                {
                    startJMS( requiredJmsContext, requiredJmsProviderURL);
                }

                if( currentConnection == null) return;

                try
                {
                    TopicSession session = currentSession;
                    TopicPublisher publisher = currentPublisher;

                    if( session != null)
                    {
                        TextMessage tm;
                        tm = session.createTextMessage( message);

                        prePublish(tm);
                        publisher.publish( tm);
//                        if( publisher instanceof ActiveMQTopicPublisher)
//                        {
//                            ActiveMQTopicPublisher tp = (ActiveMQTopicPublisher)publisher;
//
//                            JMSProducerStatsImpl ps = tp.getProducerStats();
//                            if( ps != null)
//                            {
//                                CountStatisticImpl cs = ps.getPendingMessageCount();
//                                pendingCount = cs.getCount();
//                            }
//                        }
                        totalSent++;
                        postPublish( tm);

                        logSentMessage( tm, startMS);
                    }
                }
                catch( javax.jms.JMSException e)
                {
                    LOGGER.error("Trying to Reconnect to JMS: ", e);

                    shutdownJMS();
                    jmsServerDown = true;

                    if( loop == 0) continue;

                    throw e;
                }
                break;
            }
        }
        finally
        {
            lockJMS.release();
        }
//
//        if( pendingCount > JMS_PENDING_COUNT && lastPendingWarningMS < startMS + 60000 )
//        {
//            lastPendingWarningMS = startMS;
//            LOGGER.error( "too many JMS Pending messages (" + pendingCount + ")");
//        }
    }

    /**
     * We have reconnected to the JMS server ( may have lost some messages).
     */
    @SuppressWarnings("NoopMethodInAbstractClass")
    protected void reconnectedJMS()
    {

    }

    /**
     * About to send a message
     *
     * @param msg The message we are about to send
     */
    @SuppressWarnings("NoopMethodInAbstractClass")
    protected void prePublish( Message msg)
    {

    }

    /**
     * We have sent a message
     *
     * @param msg The message that was sent.
     */
    @SuppressWarnings("NoopMethodInAbstractClass")
    protected void postPublish( Message msg)
    {

    }

    /**
     * Connect to the JMS server
     *
     * @throws Exception A serious problem.
     */
    public void connect() throws Exception
    {
        try
        {
            refreshContext();

            requiredJmsContext = getContextFactory();

            requiredJmsProviderURL = getProviderUrl();

            startJMS( requiredJmsContext, requiredJmsProviderURL );
        }
        catch( Exception e)
        {
            LOGGER.fatal( "Failed to start JMS", e);
        }
    }

    /**
     * Handle a JMS exception
     *
     * @param jMSException The exception
     */
    @Override
    public void onException(javax.jms.JMSException jMSException)
    {
        LOGGER.error( "JMS Exception", jMSException);
    }

    /**
     * The stats
     * @return the stats
     */
    public JMSConnectionStatsImpl getConnectionStats()
    {
        JMSConnectionStatsImpl stats = null;

        if( currentConnection instanceof ActiveMQConnection)
        {
            stats = ((ActiveMQConnection)currentConnection).getConnectionStats();
        }

        return stats;
    }

    /**
     * when did we last connect ?
     * @return the last connection time
     */
    public Date getLastConnected()
    {
        return lastConnected;
    }

    /**
     * How many times have we connected ?
     * @return the count
     */
    public int getConnectionCount()
    {
        return connectionCount;
    }

    /**
     * How many messages have been sent ?
     *
     * @return the count
     */
    public long getTotalSentCount()
    {
        return totalSent;
    }

    /**
     * Are we currently connected.
     *
     * @return ture if connected
     */
    public boolean isConnected()
    {
       if( currentConnection != null)
       {
           return !jmsServerDown;
       }

       return false;
    }

    /**
     * The message listener
     *
     * @return the listener.
     */
    protected MessageListener getMessageListener()
    {
        return null;
    }

    /**
     * The current the client ID
     * @return the client ID
     */
    @Nonnull @CheckReturnValue
    public String getCurrentClientID()
    {
        Connection tempConnection = currentConnection;

        if( tempConnection != null)
        {
            try
            {
                return tempConnection.getClientID();
            }
            catch( JMSException jmsException)
            {
                return "ERROR: " + jmsException.getMessage();
            }
        }

        return "UNKNOWN";
    }

    @SuppressWarnings("SleepWhileInLoop")
    private String makeClientID()
    {
        if( cacheID != null) return cacheID;

        StringBuilder buffer=new StringBuilder();

        String userName= CProperties.getProperty( "user.name", "unknown");

        String shortName = CUtilities.makeAppShortName();

        buffer.append( userName);
        buffer.append( "-");
        buffer.append( shortName);
        buffer.append( "@");
        buffer.append( NetUtil.LOCAL_HOST_NAME);
        buffer.append( "#");

        try
        {
            try
            (ServerSocket ss = new ServerSocket(0)) {

                buffer.append( Integer.toHexString( ss.getLocalPort()));
                /*
                 * make sure that no other client on  this machine can make a client id until the
                 * system clock has changed.
                 */
                long now = System.currentTimeMillis();
                buffer.append( Long.toHexString( now));

                while( System.currentTimeMillis() == now)
                {
                     Thread.sleep(1);
                }
            }
        }
        catch( IOException | InterruptedException e)
        {
            LOGGER.warn( "could use a server socket to generate a unique id", e);
            long r = (long) (Math.random() * Long.MAX_VALUE);
            buffer.append( r);
        }

        cacheID= buffer.toString();
        return cacheID;
    }

    @SuppressWarnings("empty-statement")
    private void startJMS( final String jmsContext, final String jmsProviderURL) throws Exception
    {
        Shutdown.addListener( this);
        String clientID = makeClientID();

        lockJMS.take();
        try
        {
            closeJMS( currentPublisher, currentSubscriber,  currentSession,  currentConnection);

            /**
             * try to prevent half open connections
             */
            currentPublisher = null;
            currentSubscriber = null;
            currentSession = null;
            currentConnection = null;

            boolean required;

            if(
                StringUtilities.isBlank( jmsProviderURL) == false &&
                CProperties.isDisabled( DISABLE_JMS) == false
            )
            {
                required = true;

                LOGGER.info( "Start JMS " + clientID + "{" + jmsContext + ", " + jmsProviderURL + "}");
                connectionCount++;
                lastConnected= new Date();

                /**
                 * hold the JMS connections incase we can't completely open it.
                 */
                TopicSession    tempSession = null;
                TopicConnection tempConnection = null;
                TopicPublisher  tempPublisher = null;
                TopicSubscriber tempSubscriber = null;

                try
                {
                    Hashtable props = new Hashtable();

                    props.put(
                        Context.INITIAL_CONTEXT_FACTORY,
                        jmsContext
                    );

                    props.put(
                        Context.PROVIDER_URL,
                        jmsProviderURL
                    );

                    Context context = new InitialContext(props);

                    // lookup the connection factory from the context
                    TopicConnectionFactory factory = null;

                    try
                    {
                        factory = (TopicConnectionFactory)context.lookup(
                            "TopicConnectionFactory"
                        );
                    }
                    catch(NamingException e)
                    {
                        LOGGER.warn( "could not create topic factory", e);
                    }

                    // if we can't find the factory then throw an exception
                    if (factory == null)
                    {
                        throw new Exception("Failed to locate connection factory");
                    }

                    if( factory instanceof ActiveMQConnectionFactory )
                    {
                        ActiveMQConnectionFactory af = (ActiveMQConnectionFactory)factory;
                        StringBuilder buffer= new StringBuilder( jmsProviderURL);
                        if( buffer.indexOf( "maxInactivityDuration") == -1)
                        {
                            if( buffer.indexOf("?")==-1)
                            {
                                buffer.append("?");
                            }
                            else
                            {
                                buffer.append("&");
                            }
                            buffer.append("wireFormat.maxInactivityDuration=").append( MAX_INACTIVITY_DURATION);
                        }
                        af.setBrokerURL(buffer.toString());
                    }

                    tempConnection = factory.createTopicConnection();

                    if( tempConnection instanceof ActiveMQConnection)
                    {
                        ActiveMQConnection ac = (ActiveMQConnection)tempConnection;
                        ac.setCloseTimeout( (int)JMS_TIMEOUT);

                        ac.setSendTimeout((int)JMS_TIMEOUT);

                        ac.setUseCompression( true);

                        /*
                         * http://devzone.logicblaze.com/site/apache-activemq-performance-tuning-guide.html
                         *
                         * Avoiding message copy
                         *
                         * If you know you are not going to reuse the Message object after sending then disable copyMessageOnSend flag on the ActiveMQConnection (via Java code or the connection URI)
                         */
                        ac.setCopyMessageOnSend( false);
                    }

                    tempConnection.setClientID( clientID);

                    tempSession = tempConnection.createTopicSession(
                        false,
                        Session.AUTO_ACKNOWLEDGE
                    );

                    tempConnection.setExceptionListener( this );

                    Topic topic = null;
                    try
                    {
                        topic = (Topic)context.lookup( "jms/topic/ASPC:MASTERDB");

                    }
                    catch(NamingException e)
                    {
                        LOGGER.warn( "could not create topic", e);
                    }

                    if (topic == null)
                    {
                         //Should find this since it is openJMS
                        topic = tempSession.createTopic( "ASPC:MASTERDB");
                    }

                    if (topic == null)
                    {
                        throw new Exception("Failed to Locate/Create Topic ASPC:MASTERDB");
                    }

                    tempPublisher = tempSession.createPublisher( topic );
                    if( MAX_LIVE_MS != -1)
                    {
                        tempPublisher.setTimeToLive(MAX_LIVE_MS);
                    }

                    tempPublisher.setDeliveryMode( DeliveryMode.NON_PERSISTENT);
                    MessageListener listener = getMessageListener();

                    if( listener != null)
                    {
                        tempSubscriber = tempSession.createSubscriber( topic, null, true);

                        try
                        {
                            tempSubscriber.setMessageListener(listener);
                        }
                        catch (javax.jms.JMSException e)
                        {
                            LOGGER.fatal("could not set up listener", e);

                            throw e;
                        }
                    }

                    tempConnection.start();

                    /**
                     * ALL OK so we'll set the globals.
                     */
                    currentPublisher        = tempPublisher;
                    currentSubscriber       = tempSubscriber;
                    currentSession          = tempSession;

                    /**
                     * Once we complete this line we say that it's all OK
                     */
                    currentConnection       = tempConnection;
                    jmsServerDown=false;
                }
                finally
                {
                    /**
                     * We must of had an error of some sort.
                     */
                    if( currentConnection == null )
                    {
                        if( required)
                        {
                            connectionFailed();
                        }

                        closeJMS( tempPublisher,    tempSubscriber, tempSession,    tempConnection);
                    }
                }
            }
        }
        finally
        {
            lockJMS.release();
        }
    }

    /**
     * the connection failed
     */
    protected void connectionFailed()
    {
        JMSConnector.retry( this);
    }

    /**
     * close the JMS connection.
     *
     * Don't kick of the thread if nothing to close.
     */
    private void closeJMS( TopicPublisher publisher,  TopicSubscriber subscriber, TopicSession session, TopicConnection connection)
    {
        JMSConnector.remove( this);

        /*
         * Don't start a thread if there is nothing to close.
         */
        if(
            publisher == null &&
            subscriber == null &&
            session == null &&
            connection == null
        )
        {
            return;
        }

        lockJMS.take();
        try
        {
            JMSCloser closer = new JMSCloser( publisher, subscriber, session, connection);

            closer.check();
        }
        finally
        {
            lockJMS.release();
        }
    }

    /**
     * the application is shutting down.
     */
    @Override
    public final void shutdown( )
    {
        LOGGER.info( "closing JMS on application shutdown");
        shutdownJMS();
    }

    /**
     * shutdown the JMS connections
     */
    public final void shutdownJMS( )
    {
        JMSConnector.remove( this);

        lockJMS.take();
        try
        {
            if(
                currentPublisher != null   ||
                currentSubscriber != null  ||
                currentSession != null     ||
                currentConnection != null
            )
            {
                closeJMS( currentPublisher, currentSubscriber, currentSession, currentConnection);
            }

            currentPublisher=null;
            currentSubscriber = null;
            currentSession = null;
            currentConnection = null;
        }
        finally
        {
            lockJMS.release();
        }
    }

    /**
     * log the sent message.
     * @param message the message
     * @param startMS when did we start sending the message.
     */
    public static void logSentMessage( final TextMessage message, final long startMS)
    {
        long endMS = System.currentTimeMillis();

        boolean warning = false;
        String slowMessage="";
        if( endMS - startMS > 20000)
        {
            slowMessage = "SLOW JMS: Took " + TimeUtil.getDiff( startMS) + " to send a message";
        }

        if( warning == false && LOGGER.isDebugEnabled() == false) return;

        String messageID="";

        StringBuilder buffer = new StringBuilder( 200);
        try
        {
            buffer.append( message.getText());
            messageID=message.getJMSMessageID();
        }
        catch( JMSException e)
        {
            buffer.append( e);
        }

        buffer.append( "\n<!--\n");
        buffer.append( Thread.currentThread().getName());
        buffer.append( "\n");
        buffer.append( messageID);
        buffer.append( "\n");
        buffer.append( TimeUtil.format( "dd MMM yyyy HH:mm:ss", new Date( startMS), TimeZone.getDefault()));
        buffer.append( "\nSent in: ");
        buffer.append( TimeUtil.getDiff( startMS));
        buffer.append( slowMessage);
        buffer.append( "\n-->\n");

        if( warning == false)
        {
            LOGGER.debug( buffer);
        }
        else
        {
            LOGGER.warn( buffer);
        }
    }

    static
    {
        String temp;
        temp = CProperties.getProperty( ENV_JMS_MAX_LIVE_MS);

        long tempMS = 20*60*1000;

        if( StringUtilities.isBlank( temp) == false)
        {
            try
            {
                tempMS = Long.parseLong( temp);
            }
            catch( Exception e)
            {
                LOGGER.error( "Invalid " + ENV_JMS_MAX_LIVE_MS + "='" + temp + "'", e );
            }
        }

        if( tempMS <= 0) tempMS = -1;// default to disabled.

        MAX_LIVE_MS=tempMS;

        LOGGER.info( "JMS MAX LIVE MS=" + MAX_LIVE_MS);

        temp = CProperties.getProperty( ENV_JMS_TIMEOUT);

        tempMS = 15 * 60 * 1000; // 15 minutes.

        if( StringUtilities.isBlank( temp) == false)
        {
            try
            {
                tempMS = Long.parseLong( temp);
            }
            catch( Exception e)
            {
                LOGGER.error( "Invalid " + ENV_JMS_TIMEOUT + "='" + temp + "'", e );
            }
        }


        JMS_TIMEOUT=tempMS;

        LOGGER.info( "JMS TIMEOUT=" + JMS_TIMEOUT);
        int tempPendingCount = 1000;
        temp = CProperties.getProperty( ENV_JMS_PENDING_COUNT);
        if( StringUtilities.isBlank( temp) == false)
        {
            try
            {
                tempPendingCount = Integer.parseInt( temp);
            }
            catch( Exception e)
            {
                LOGGER.error( "Invalid " + ENV_JMS_PENDING_COUNT + "='" + temp + "'", e );
            }
        }


        JMS_PENDING_COUNT=tempPendingCount;

        LOGGER.info( "JMS PENDING COUNT=" + JMS_PENDING_COUNT);

        int tmpMaxInactivityDuration=DEFAULT_MAX_INACTIVITY_DURATION;

        try
        {
            temp = System.getProperty( PROPERTY_MAX_INACTIVITY_DURATION, "" + tmpMaxInactivityDuration);
            tmpMaxInactivityDuration= Integer.parseInt(temp);

            if( tmpMaxInactivityDuration < 0 )
            {
                LOGGER.warn( PROPERTY_MAX_INACTIVITY_DURATION + " can't be less than to zero");
                tmpMaxInactivityDuration=0;
            }
        }
        finally
        {
            MAX_INACTIVITY_DURATION = tmpMaxInactivityDuration;
        }
    }
}
