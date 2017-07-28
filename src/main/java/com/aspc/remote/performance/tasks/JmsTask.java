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

import com.aspc.remote.database.InvalidDataException;
import java.lang.reflect.*;
import java.util.Hashtable;
import javax.jms.*;
import javax.naming.*;

import org.apache.commons.logging.Log;

import com.aspc.remote.performance.BenchMarkClient;
import com.aspc.remote.performance.Task;
import com.aspc.remote.jms.Client;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.misc.TimeUtil;
import org.apache.activemq.ActiveMQConnection;

/**
 * This class is a base class to do any JMS related activities
 *
 * <I>THREAD MODE : SINGLE-THREADED </I>
 *
 * @author sr83034
 * @since 29 September 2006
 */
public class JmsTask extends Task
{
    /**
     *
     */
    public static final String       CLIENT_ID                  = "CLIENT_ID";
    /**
     *
     */
    public static final String       MESSAGE_ID                 = "MESSAGE_ID";
    /**
     *
     */
    public static final String       SYSTEM_MESSAGE             = "SYSTEM_MESSAGE";
    /**
     *
     */
    public static final String       SYSTEM_MESSAGE_SHUTDOWN    = "SHUTDOWN";

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.performance.tasks.JmsTask");//#LOGGER-NOPMD

    private long                    delayMessageList[];
    private String                   contextFactory             = null;
    private String                   providerUrl                = null;
    private String                   connectionFactory          = "TopicConnectionFactory";
    private String                   destination                = null;
    private long                     messageCount               = 1;
    private int                      messageSize                = 1;
    private long                     delay                      = 0;
    private String                   domain                     = "Domain1";
    private String                   securityPrincipal          = "Administrator";
    private String                   securityCredentials        = "Administrator";
    private boolean                  useJndi                    = false;
    private String                   hostName                   = "localhost";
    private int                      portNumber                 = 1212;
    private String                   connectionFactoryClassName = null;
    private String                   destinationClassName       = null;
    /**
     *
     */
    protected Context                jndiContext                = null;
    /**
     *
     */
    protected TopicConnectionFactory topicConnectionFactory     = null;
    /**
     *
     */
    protected TopicConnection        topicConnection            = null;
    /**
     *
     */
    protected TopicSession           topicSession               = null;
    /**
     *
     */
    protected Topic                  topic                      = null;
    /**
     *
     */
    protected String                 listenDuration;

    /**
     * @param name
     * @param bmClient
     */
    public JmsTask( String name, BenchMarkClient bmClient )
    {
        super( name, bmClient );
        max_acceptable_ms = 120 * 60 * 1000;
    }

    /**
     * @return Returns the destination.
     */
    public String getDestination()
    {
        return destination;
    }

    /**
     * @param destination The destination to set.
     */
    public void setDestination( final String destination )
    {
        this.destination = destination;
    }

    /**
     *
     * @param duration
     */
    public void setListenDuration( final String duration)
    {
        listenDuration=duration;
    }

    /**
     * @return Returns the messageCount.
     */
    public long getMessageCount()
    {
        return messageCount;
    }

    /**
     * @param messageCount
     *            The messageCount to set.
     */
    public void setMessageCount( long messageCount )
    {
        this.messageCount = messageCount;
    }

    /**
     * @param messageCount
     *            The messageCount to set.
     */
    public void setMessageCount( String messageCount )
    {
        this.messageCount = Long.parseLong( messageCount );
    }

    /**
     * @return Returns the providerUrl.
     */
    public String getProviderUrl()
    {
        return providerUrl;
    }

    /**
     * @param providerUrl
     *            The providerUrl to set.
     */
    public void setProviderUrl( String providerUrl )
    {
        this.providerUrl = providerUrl;
    }

    /**
     * This is a implementation of abstract function
     * @throws Exception Exception A serious problem
     */
    @Override
    protected void process() throws Exception
    {
        try
        {
            createConnectionsEtc();
        }
        finally
        {
            closeConnectionsEtc();
        }
    }

    /**
     *
     */
    protected void closeConnectionsEtc()
    {
        /**
         * Close the connections only on exception
         */
        if( this.topicSession != null )
        {
            try
            {
                this.topicSession.close();
            }
            catch( JMSException e )
            {
                LOGGER.error( "Error occurred closing session", e );
            }
        }
        if( this.topicConnection != null )
        {
            try
            {
                this.topicConnection.close();
            }
            catch( JMSException e )
            {
                LOGGER.error( "Error occurred closing connection", e );
            }
        }
    }

    /**
     *
     * @throws Exception Exception A serious problem
     */
    @SuppressWarnings("empty-statement")
    protected void createConnectionsEtc() throws Exception
    {
        if( isUseJndi() )
        {
            /**
             * Create jndi context
             */
            Hashtable props = new Hashtable();
            props.put( Context.INITIAL_CONTEXT_FACTORY, getContextFactory() );
            props.put( Context.PROVIDER_URL, getProviderUrl() );

            /**
             * If the provider is SonicMQ then set the provider specific
             * properties
             */
            if( getContextFactory().equals(
                    "com.sonicsw.jndi.mfcontext.MFContextFactory" ) )
            {
                props.put( "com.sonicsw.jndi.mfcontext.domain", getDomain() );
                props.put( Context.SECURITY_PRINCIPAL, getSecurityPrincipal() );
                props.put( Context.SECURITY_CREDENTIALS,
                        getSecurityCredentials() );
            }

            this.jndiContext = new InitialContext( props );

            /**
             * Look up connection factory and topic. If either does not exist,
             * exit.
             */
            this.topicConnectionFactory = ( TopicConnectionFactory )this.jndiContext
                    .lookup( getConnectionFactory() );

            /**
             * Certain providers like ActiveMQ and SonicMQ require broker URL to
             * be set explicitly in order to connect to a remote server
             */
            if( getContextFactory().endsWith( "ActiveMQInitialContextFactory" ) )
            {
                setBrokerURL();
            }
        }
        else
        {
            this.topicConnectionFactory = ( TopicConnectionFactory )createObject( getConnectionFactoryClassName() );
        }

        /*
         * Create connection. Create session from connection; false means
         * session is not transacted.
         */
        this.topicConnection = this.topicConnectionFactory.createTopicConnection();
        // this.topicConnection = ( ActiveMQConnection )this.topicConnectionFactory.createTopicConnection();
        // TransportChannel transportChannel = ( ( ActiveMQConnection )this.topicConnection ).getTransportChannel();
        // transportChannel.addTransportStatusEventListener( new JmsServerStatusListener() );

        this.topicConnection.setExceptionListener( new JmsExceptionListener() );

        if( topicConnection instanceof ActiveMQConnection)
        {
            ActiveMQConnection ac = (ActiveMQConnection)topicConnection;
            ac.setCloseTimeout( (int)15 * 60 * 1000);
            //ac.setSendConnectionInfoTimeout( (int)JMS_TIMEOUT);
            ac.setSendTimeout((int)15 * 60 * 1000);

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
        /**
         * Create topic session.
         */
        this.topicSession = this.topicConnection.createTopicSession( false,
                Session.AUTO_ACKNOWLEDGE );

        if( isUseJndi() )
        {
            while( topic == null)
            {
                try
                {
                    this.topic = ( Topic )jndiContext
                        .lookup( getDestination() );
                }
                catch( NameNotFoundException nf)
                {
                    ;// cool.
                }
                catch( NamingException ex )
                {
                    LOGGER.error( "Error occurred finding topic will create a new topic",ex );
                    Thread.sleep((long) (1000 * Math.random()));
                }
                if( this.topic == null )
                {
                    this.topic = this.topicSession.createTopic( getDestination() );
                    LOGGER.error( "New topic '" + getDestination() + "' created" );
                }
            }
        }
        else
        {
            this.topic = ( Topic )createObject( getDestinationClassName() );
        }
    }

    /**
     * This function simply sets broker url method within the connection factory
     */
    private void setBrokerURL()
    {
        /**
         * This is needed for ActiveMQ and SonicMQ to work correctly in case of
         * remote server
         */
        try
        {
            Class c = this.topicConnectionFactory.getClass();
            Class parameterTypes[] = { String.class };

            Method m = c.getMethod( "setBrokerURL", parameterTypes );
            StringBuilder buffer= new StringBuilder( getProviderUrl());
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
                buffer.append( "wireFormat.maxInactivityDuration=" + Client.MAX_INACTIVITY_DURATION);
            }
            Object objargs[] = { buffer.toString() };

            m.invoke( topicConnectionFactory, objargs );
        }
        catch( Exception e )
        {
            LOGGER.error( "This simply indicates that "
                    + "the 'setBrokerURL' method does not existe.", e );
        }
    }

    /**
     * @return Returns the contextFactory.
     */
    public String getContextFactory()
    {
        return contextFactory;
    }

    /**
     * @param contextFactory
     *            The contextFactory to set.
     */
    public void setContextFactory( String contextFactory )
    {
        useJndi = true;
        this.contextFactory = contextFactory;
    }

    /**
     * @return Returns the jndiContext.
     */
    public Context getJndiContext()
    {
        return jndiContext;
    }

    /**
     * @param jndiContext
     * The jndiContext to set.
     */
    protected void setJndiContext( Context jndiContext )
    {
        this.jndiContext = jndiContext;
    }

    /**
     * @return Returns the delay.
     */
    public long getReceiveDelay()
    {
        return this.delay;
    }

    /**
     * @param delay The delay to set.
     */
    public void setReceiveDelay( long delay )
    {
        this.delay = delay;
    }

    /**
     * @param delay
     *            The delay to set.
     * @throws InvalidDataException
     */
    public void setReceiveDelay( String delay ) throws InvalidDataException
    {
        this.delay = TimeUtil.convertDurationToMs( delay );
    }

    /**
     * @param delayList
     */
    public void setReceiveDelayList( String delayList )
    {
        if( StringUtilities.isBlank(delayList) == false)
        {
            String values[] = delayList.split(",");
            delayMessageList= new long[values.length];
            for( int i =0; i < delayMessageList.length;i++)
            {
                delayMessageList[i] = Long.parseLong(values[i].trim());
            }
        }
    }

    /**
     *
     * @return the value
     */
    public long[] getReceiveDelayList()
    {
        return delayMessageList;
    }

    /**
     * @return Returns the messageSize.
     */
    public int getMessageSize()
    {
        return messageSize;
    }

    /**
     * @param messageSize
     *            The messageSize to set in KB
     */
    public void setMessageSize( int messageSize )
    {
        this.messageSize = messageSize;
    }

    /**
     * @param messageSize
     *            The messageSize to set in KB
     */
    public void setMessageSize( String messageSize )
    {
        this.messageSize = Integer.parseInt( messageSize );
    }

    /**
     * @return Returns the connectionFactory.
     */
    public String getConnectionFactory()
    {
        return connectionFactory;
    }

    /**
     * @param connectionFactory
     *            The connectionFactory to set.
     */
    public void setConnectionFactory( String connectionFactory )
    {
        this.connectionFactory = connectionFactory;
    }

    /**
     * @return Returns the domain.
     */
    public String getDomain()
    {
        return domain;
    }

    /**
     * @param domain
     *            The domain to set.
     */
    public void setDomain( String domain )
    {
        this.domain = domain;
    }

    /**
     * @return Returns the securityCredentials.
     */
    public String getSecurityCredentials()
    {
        return securityCredentials;
    }

    /**
     * @param securityCredentials
     *            The securityCredentials to set.
     */
    public void setSecurityCredentials( String securityCredentials )
    {
        this.securityCredentials = securityCredentials;
    }

    /**
     * @return Returns the securityPrincipal.
     */
    public String getSecurityPrincipal()
    {
        return securityPrincipal;
    }

    /**
     * @param securityPrincipal
     *            The securityPrincipal to set.
     */
    public void setSecurityPrincipal( String securityPrincipal )
    {
        this.securityPrincipal = securityPrincipal;
    }

    /**
     * This function creates an instance of an object whose name is specified as
     * the argument via reflection.
     *
     * @param String
     *            _className. Name of the object that needs to be created
     * @return Object
     */
    private Object createObject( String _className )
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException
    {
        Object object = null;

        Class classDefinition = Class.forName( _className );
        object = classDefinition.newInstance();

        return ( object );
    }

    /**
     * @return Returns the hostName.
     */
    public String getHostName()
    {
        return hostName;
    }

    /**
     * @param hostName
     *            The hostName to set.
     */
    public void setHostName( String hostName )
    {
        this.hostName = hostName;
    }

    /**
     * @return Returns the portNumber.
     */
    public int getPortNumber()
    {
        return portNumber;
    }

    /**
     *
     *
     * @param portNumber
     */
    public void setPortNumber( int portNumber )
    {
        this.portNumber = portNumber;
    }

    /**
     *
     *
     * @param portNumber
     */
    public void setPortNumber( String portNumber )
    {
        setPortNumber( Integer.parseInt( portNumber ) );
    }

    /**
     * @return Returns the useJndi.
     */
    public boolean isUseJndi()
    {
        return useJndi;
    }

    /**
     *
     *
     * @param useJndi
     */
    public void setUseJndi( boolean useJndi )
    {
        this.useJndi = useJndi;
    }

    /**
     *
     *
     * @param useJndi
     */
    public void setUseJndi( String useJndi )
    {
        setUseJndi( "true".equalsIgnoreCase( useJndi ) );
    }

    /**
     * @return Returns the connectionFactoryClassName.
     */
    public String getConnectionFactoryClassName()
    {
        return connectionFactoryClassName;
    }

    /**
     * @param connectionFactoryClassName The connectionFactoryClassName to set.
     */
    public void setConnectionFactoryClassName( String connectionFactoryClassName )
    {
        this.connectionFactoryClassName = connectionFactoryClassName;
    }

    /**
     * @return Returns the destinationClassName.
     */
    public String getDestinationClassName()
    {
        return destinationClassName;
    }

    /**
     * @param destinationClassName The destinationClassName to set.
     */
    public void setDestinationClassName( String destinationClassName )
    {
        this.destinationClassName = destinationClassName;
    }
}
