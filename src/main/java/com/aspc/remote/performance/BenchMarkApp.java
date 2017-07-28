/*
 *  Copyright (c) 2004 ASP Converters pty ltd
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.performance;

import com.aspc.remote.performance.internal.SSLUtilities;
import com.aspc.remote.performance.tasks.JmsTask;
import com.aspc.remote.performance.tasks.ScreenLoadTask;
import com.aspc.remote.application.AppCmdLine;
import com.aspc.remote.html.HTMLPage;
import com.aspc.remote.html.HTMLTable;
import com.aspc.remote.html.HTMLText;
import com.aspc.remote.jdbc.SoapResultSet;
import com.aspc.remote.soap.Client;
import com.aspc.remote.soap.Constants;
import com.aspc.remote.soap.LoginContext;
import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.util.misc.*;
import com.aspc.remote.util.net.NetUrl;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message.RecipientType;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.w3c.dom.*;

/**
 *  BenchMarkApp
 *
 * <br>
 * <i>THREAD MODE: SINGLETON command line application SINGLE-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       7 April 2001
 */
public class BenchMarkApp extends AppCmdLine
{
    private ZipOutputStream zipOutputStream;
    private File zipFile;

    /**
     * add extra command line options
     * @param options the options
     */
    @Override
    protected void addExtraOptions( final Options options)
    {
        super.addExtraOptions( options);
        options.addOption( "e", true, "Email list" );
        options.addOption( "f", true, "config file" );

        Option option;

        option = new Option( "t", true, "timezone name");
        option.setArgName( "timezone name");
        option.setOptionalArg(true);
        options.addOption(  option);

        option = new Option( "o", true, "timezone offset");
        option.setArgName( "timezone offset");
        option.setOptionalArg(true);
        options.addOption(  option);

        option = new Option( "l", true, "language");
        option.setArgName( "language");
        option.setOptionalArg(true);
        options.addOption(  option);
    }

    /**
     * Handles arguments.
     *
     * TODO: change to handleCommandLine
     *
     * @param p The properties object
     * @throws Exception In case of error
     */
    @Override
    @SuppressWarnings("deprecation")
    public void handleArgs( Properties p ) throws Exception
    {
        remoteURL = p.getProperty( "-R");
        emailList = p.getProperty( "-e");
        xmlFile = p.getProperty( "-f");
        timeZoneName = p.getProperty("-t");
        timeZoneOffset = p.getProperty("-o");
        language = p.getProperty("-l");

        if(
            StringUtilities.isBlank( remoteURL) ||
            StringUtilities.isBlank( xmlFile) ||
            StringUtilities.isBlank( emailList)
        )
        {
            throw new Exception( "Missing mandatory parameter" );
        }

        /**
         * The path name of the XML file is used to get
         * the data files
         */
        setLocation( xmlFile );

        super.handleArgs( p);
    }

    /**
     * This function simply checks for a valid location and sets it
     * @param _xmlFileName file name
     * @throws Exception Incase of invalid location
     */
    protected void setLocation( final String _xmlFileName ) throws Exception
    {
        if( StringUtilities.isBlank(_xmlFileName) == false )
        {
            File file = new File( _xmlFileName );
            if( file.exists() )
            {
                if(file.getParent() != null)
                {
                    this.location = file.getParent() + "/";
                }
                else
                {
                    this.location = "";
                }
            }
            else
            {
                throw new Exception( "Invalid file specified - "
                        + _xmlFileName );
            }
        }
    }

    /**
     * This function returns the location string
     * @return String
     */
    protected String getLocation()
    {
        return( this.location );
    }

    /**
     * get the start time
     * @return the start time
     */
    public static long getStartTm()
    {
        return startMS;
    }

    /**
     * Loads XML document containing configuration for the benchmark run and processes each item
     */
    @Override
    public void process()
    {
        String temp = System.getProperty( CProperties.PROPERTY_DISABLE);
        temp = Constants.DISABLE_MOVE_TO + "," + temp;
        System.setProperty( CProperties.PROPERTY_DISABLE, temp);

        try
        {
            HTMLPage page;

            try
            {
                loadDoc( xmlFile);
                if( titleFound == false)
                {
                    LOGGER.warn( "Require TITLE tag");
                }

                makeDependancies( );

                startClients( );

                waitClients( );

                page = generate( );

                if( page == null)
                {
                    return;
                }

                Client client = createRemoteClient();

                for (Object attachmentList1 : attachmentList) 
                {
                    Attachment attachment = (Attachment) attachmentList1;
                    attachment.process( client);
                }
            }
            catch( Exception e)
            {
                LOGGER.fatal( "Error running benchmark", e);
                page = new HTMLPage( );
                page.setTitle( "Error: " + title);

                page.addText( "The following error occurred while checking tasks\n");
                page.addText( e.toString());
            }

            try
            {
                sendMail( emailList, page.getTitle(), page.generate());
            }
            catch( SendFailedException sfe )
            {
                if( GraphicsEnvironment.isHeadless() )
                {
                    throw sfe;
                }
                else
                {
                    LOGGER.info( "sending email failed, attempt to open in browser" );
                    File tmp = File.createTempFile( "benchmark", ".html", FileUtil.makeQuarantineDirectory() );
                    try
                    (FileWriter fw = new FileWriter( tmp )) {
                        fw.write( page.generate() );
                    }
                    FileUtil.openFileInDefaultApplication( tmp );
                }
            }
        }
        catch( Exception e)
        {
            LOGGER.fatal( "A error", e);
        }
    }

    private void makeDependancies( ) throws Exception
    {
        for (Object clientList2 : clientList) 
        {
            BenchMarkClient t = (BenchMarkClient) clientList2;
            String depends = t.getDepends();
            if( StringUtilities.isBlank( depends))
            {
                continue;
            }
            StringTokenizer st= new StringTokenizer( depends, ",");
            while( st.hasMoreTokens())
            {
                String name = st.nextToken().trim();

                if( StringUtilities.isBlank( name))
                {
                    continue;
                }

                boolean found = false;
                
                for (Object clientList1 : clientList) 
                {
                    BenchMarkClient dt = (BenchMarkClient) clientList1;
                    if( dt == t)
                    {
                        continue;
                    }
                    String dName = dt.getName();
                    if( StringUtilities.isLike( name, dName))
                    {
                        t.iAddDepend( dt);
                        found = true;
                    }
                }

                if( found == false)
                {
                    throw new Exception( "Couldn't find client '" + name + "'");
                }
            }
        }
    }

    private void startClients()
    {
        startMS = System.currentTimeMillis();

        for (Object clientList1 : clientList) 
        {
            BenchMarkClient r = (BenchMarkClient) clientList1;
            r.start();
        }
    }

    private void waitClients( ) throws Exception
    {
        for (Object clientList1 : clientList) {
            BenchMarkClient r = (BenchMarkClient) clientList1;
            r.join();
        }
    }

    private void loadDoc( final String fileName) throws Exception
    {
        fileList.add( fileName);

        Document doc;
        doc = DocumentUtil.makeDocument( FileUtil.readFile( fileName));
        Element rootElement = doc.getDocumentElement();
        if( rootElement != null)
        {
            String disable = rootElement.getAttribute(ATT_DISABLE);

            if( StringUtilities.isBlank(disable) == false)
            {
                String temp = System.getProperty( CProperties.PROPERTY_DISABLE);
                temp = disable + "," + temp;
                System.setProperty( CProperties.PROPERTY_DISABLE, temp);
            }
        }

        NodeList imports = doc.getElementsByTagName( "IMPORT");

        String [] importMandatory = {ATT_FILE};
        String [] importOptional = {};
        for( int i = 0; i < imports.getLength(); i++)
        {
            Element element = (Element)imports.item( i);

            checkAttributes( element, importMandatory, importOptional);

            String file = getLocation() + element.getAttribute( ATT_FILE);

            loadDoc( file);
        }

        addClients( doc);

        loadVaryList( doc);
        loadReportCmds( doc);
        loadAttachments( doc);
        loadTitle( doc);

        currentScript=doc;
    }

    private void addClients( final Document doc) throws Exception
    {
        String list[][]={
            {NODE_CLIENT, "com.aspc.remote.performance.BenchMarkClient"},
            {NODE_WEB_CLIENT, "com.aspc.remote.performance.BenchMarkWebClient"}
        };

        for (String[] list1 : list) 
        {
            NodeList clients = doc.getElementsByTagName(list1[0]);
            for (int i = 0; i < clients.getLength(); i++) {
                Element te = (Element)clients.item( i);
                int clones = 1;
                if( te.hasAttribute( ATT_CLONE))
                {
                    clones = Integer.parseInt(te.getAttribute( ATT_CLONE));
                }
                for (int j = 0; j < clones; j++) {
                    String ts = "";
                    if( clones > 1)
                    {
                        ts = ":" + (j + 1);
                    }
                    BenchMarkClient client = createClient(te, ts, j, list1[1]);
                    clientList.add( client);
                    addToHashMap(te,client);
                }
            }
        }
   }

    /**
     * This function is for adding client/task list to the map
     * @param element The XML element which is a item client or task
     */
    private void addToHashMap(final Element element, final Item item)
    {
        List list = (ArrayList)itemMap.get(element);

        if( list == null)
        {
            list = new ArrayList();
            itemMap.put(element,list);
        }

        list.add(item);
    }

    private void loadVaryList( final Document doc) throws Exception
    {
        NodeList list = doc.getElementsByTagName( "LIST");
        String [] listMandatory = {ATT_NAME};
        String [] listOptional = {ATT_FILE};

        for( int i = 0; i < list.getLength(); i++)
        {
            Element element = (Element)list.item( i);
            checkAttributes( element, listMandatory, listOptional);

            String name = element.getAttribute( ATT_NAME);
            String data = "";

            if( element.hasAttribute( ATT_FILE))
            {
                StringBuilder buffer = new StringBuilder();

                try
                (FileReader fr = new FileReader( getLocation() + element.getAttribute(ATT_FILE) )) {
                    while( true)
                    {
                        char array[] = new char[1024];
                        int len = fr.read( array);

                        if( len <= 0)
                        {
                            break;
                        }

                        buffer.append( array,0, len);
                    }
                }

                data = buffer.toString();
            }
            else
            {
                Node firstNode = element.getFirstChild();

                if( firstNode != null)
                {
                    data = firstNode.getNodeValue();
                }
            }

            VaryList.create( name, data);
        }
    }

    private void loadTitle( final Document doc) throws Exception
    {
        NodeList list = doc.getElementsByTagName( "TITLE");

        String [] titleMandatory = {ATT_NAME};
        String [] titleOptional = {};

        for( int i = 0; i < list.getLength(); i++)
        {
            Element element = (Element)list.item( i);
            checkAttributes( element, titleMandatory, titleOptional);
            String tempTitle;
            tempTitle = element.getAttribute( ATT_NAME).trim();

            if( StringUtilities.isBlank( title) == false)
            {
                title = tempTitle;
                if( titleFound )
                {
                    LOGGER.warn( "Duplicate TITLE '" + title + "'");
                }
                else
                {
                    titleFound = true;
                }
            }
            else
            {
                LOGGER.warn( "Blank TITLE");
            }
        }

        if( CProperties.isDisabled(Task.DISABLE_TASK_RESET))
        {
            title = "[CACHE ONLY] " + title;
        }

        if( title.contains("${HOST}"))
        {
            NetUrl nu = new NetUrl( remoteURL);

            String host = nu.getHost();
            host = host.replace( "http://", "");
            host = host.replace( "https://", "");

            title = title.replace( "${HOST}", host);
        }
    }

    private void loadReportCmds( final Document doc) throws Exception
    {
        NodeList list = doc.getElementsByTagName( "REPORT");

        reportCmds ="";
        String [] reportMandatory = {};
        String [] reportOptional = {};

        for( int i = 0; i < list.getLength(); i++)
        {
            Element element = (Element)list.item( i);
            checkAttributes( element, reportMandatory, reportOptional);

            Node node = element.getFirstChild();

            if( node != null)
            {
                String data = node.getNodeValue();

                if( StringUtilities.isBlank( reportCmds) == false)
                {
                    reportCmds += ";";
                }
                reportCmds += data;
            }
        }
    }

    private void loadAttachments( final Document doc) throws Exception
    {
        NodeList list = doc.getElementsByTagName( "ATTACHMENT");

        String [] attachmentMandatory = {ATT_NAME, ATT_JAVA_CLASS};
        String [] attachmentOptional = {};

        for( int i = 0; i < list.getLength(); i++)
        {
            Element element = (Element)list.item( i);
            checkAttributes( element, attachmentMandatory, attachmentOptional);
            String data = "";

            Node node = element.getFirstChild();

            if( node != null)
            {
                data = node.getNodeValue();
            }

            String name = element.getAttribute( ATT_NAME);

            String javaClass = element.getAttribute( ATT_JAVA_CLASS);

            Class attachmentClass = Class.forName( javaClass);

            Class constructorSignature[] = {
                name.getClass()
            };

            Constructor cons = attachmentClass.getConstructor(constructorSignature);

            Object objs[] = {name};

            Attachment attachment = (Attachment)cons.newInstance(objs);

            attachment.setData( data);

            attachmentList.add( attachment);
        }
    }

    private void checkAttributes(
        final Element element,
        final String mandatory[],
        final String optional[]
    ) throws Exception
    {
        if( mandatory != null)
        {
            for (String mandatory1 : mandatory) 
            {
                if (element.hasAttribute(mandatory1) == false) {
                    throw new Exception(element.getNodeName() + " mandatory attribute '" + mandatory1 + "' missing");
                }
            }
        }

        if( optional != null)
        {
            NamedNodeMap map = element.getAttributes();

            for( int i = 0; i < map.getLength(); i++)
            {
                Node node = map.item( i);

                String name = node.getNodeName();

                boolean found = false;

                if( mandatory != null)
                {
                    for( int j = 0; found == false && j < mandatory.length; j++)
                    {
                        if( name.equals( mandatory[j]))
                        {
                            found = true;
                        }
                    }
                }

                for( int j = 0; found == false && j < optional.length; j++)
                {
                    if( name.equals( optional[j]))
                    {
                        found = true;
                    }
                }

                if( found == false)
                {
                    LOGGER.debug( element.getNodeName() + " unknown attribute '" + name + "'");
                }
            }
        }
    }

    private BenchMarkClient createClient(
        final Element element,
        final String suffix,
        final int seq,
        final String defaultJavaClass
    ) throws Exception
    {
        String name = element.getAttribute( ATT_NAME) + suffix;

        String [] mandatory = {ATT_NAME};
        String [] optional = {
            ATT_CLONE,
            "depends",
            "loop",
            "wait",
            "stagger",
            "pause",
            "max_tm",
            ATT_CLONE_FREQUENCY,
            ATT_LOOP_FREQUENCY,
            "remote_url",

            ATT_MIN_FIRST,
            ATT_MAX_FIRST,
            ATT_MIN_MODE,
            ATT_MAX_MODE,
            ATT_MIN_MEDIAN,
            ATT_MAX_MEDIAN,
            ATT_MIN_MEAN,
            ATT_MAX_MEAN,
            ATT_RESET,
            ATT_VALIDATE,
            ATT_VALIDATE_FORMULA
        };

        checkAttributes( element, mandatory, optional);

        /**
         * Is the client is to be connected to a different
         * server other than the one specified in command line
         * then this is the place where it does so
         */
        String url = this.remoteURL;
        if( element.hasAttribute( "remote_url" ) )
        {
            url = element.getAttribute( "remote_url" ).trim();
        }

        String tzName = this.timeZoneName;
        if( element.hasAttribute( "timeZoneName" ) )
        {
            tzName = element.getAttribute( "timeZoneName" ).trim();
        }

        String tzOffset = this.timeZoneOffset;
        if( element.hasAttribute( "timeZoneOffset" ) )
        {
            tzOffset = element.getAttribute( "timeZoneOffset" ).trim();
        }

        String tmpLanguage = this.language;
        if( element.hasAttribute( "language" ) )
        {
            tmpLanguage = element.getAttribute( "language" ).trim();
        }

        LoginContext loginContext = new LoginContext("", tzOffset, tzName, null, tmpLanguage, null,null, false,null, false, null, -1, -1);

        String javaClass = (StringUtilities.isBlank(defaultJavaClass))?
            "com.aspc.remote.performance.BenchMarkClient":defaultJavaClass;

        if( element.hasAttribute( ATT_JAVA_CLASS))
        {
            javaClass = element.getAttribute( ATT_JAVA_CLASS);
        }

        Class clientClass = Class.forName( javaClass);

        Class constructorSignature[] = {
            name.getClass(),
            url.getClass(),
            loginContext.getClass()
        };

        Constructor cons = clientClass.getConstructor(constructorSignature);

        Object objs[] = {name, url, loginContext};

        BenchMarkClient client;
        try
        {
            client = (BenchMarkClient)cons.newInstance(objs);
        }
        catch( InvocationTargetException ite)
        {
            Throwable t = ite.getTargetException();

            if( t instanceof Exception)
            {
                throw (Exception)t;
            }
            else
            {
                throw new Exception( "could not create a client", t);//NOPMD
            }
        }

        client.setItemAttributes( element);

        if( element.hasAttribute( "depends"))
        {
            client.setDepends( element.getAttribute( "depends"));
        }

        if( element.hasAttribute( ATT_CLONE_FREQUENCY))
        {
            client.setCloneFrequency( element.getAttribute( ATT_CLONE_FREQUENCY), seq);
        }

        if( element.hasAttribute(ATT_RESET))
        {
            if(element.getAttribute(ATT_RESET).trim().equalsIgnoreCase("Y") ||
                    element.getAttribute(ATT_RESET).trim().equalsIgnoreCase("true"))
            {
                client.getRemoteClient().execute(
                    "MEMORY CLEAR 10;\n" +
                        "MEMORY GC;"
                        );
            }
        }
        addTasks(element, client);
        return client;
    }


    private void addTasks(final Element element, final BenchMarkClient client) throws Exception
    {
        String list[][]={
            {"TASK", "com.aspc.remote.performance.tasks.CommandTask"},
            {"FILE", "com.aspc.remote.performance.tasks.FileTask"}
        };

        for (String[] list1 : list) 
        {
            NodeList tasks = element.getElementsByTagName(list1[0]);
            for (int i = 0; i < tasks.getLength(); i++) {
                Element te = (Element)tasks.item( i);
                int clones = 1;
                if( te.hasAttribute( ATT_CLONE))
                {
                    clones = Integer.parseInt(te.getAttribute( ATT_CLONE));
                }
                for (int j = 0; j < clones; j++) {
                    String ts = "";
                    if( clones > 1)
                    {
                        ts = ":" + (j + 1);
                    }
                    Task task = createTask(client, te, ts, list1[1]);
                    client.addTask( task);
                    this.addToHashMap(te,task);
                }
            }
        }
    }

    private Task createTask(
        final BenchMarkClient client,
        final Element element,
        final String suffix,
        final String defaultJavaClass
    ) throws Exception
    {
        String [] mandatory = {ATT_NAME};
        String [] optional = {
            ATT_JAVA_CLASS,
            "varying",
            "varying_method",
            "summary",
            "loop",
            "wait",
            "stagger",
            "pause",
            "max_tm",
            ATT_LOOP_FREQUENCY,
            "pre_file",
            "post_file",
            "job",
            "jobcancel",
            ATT_MARK_LOG,
            ATT_DATA_FILE,

            ATT_MIN_FIRST,
            ATT_MAX_FIRST,
            ATT_MIN_MODE,
            ATT_MAX_MODE,
            ATT_MIN_MEDIAN,
            ATT_MAX_MEDIAN,
            ATT_MIN_MEAN,
            ATT_MAX_MEAN,
            ATT_MAX_CONTENT,
            ATT_RESET,
            ATT_VALIDATE,
            ATT_VALIDATE_FORMULA
        };

        checkAttributes( element, mandatory, optional);

        String name = element.getAttribute( ATT_NAME) + suffix;

        String javaClass = (StringUtilities.isBlank(defaultJavaClass))?
            "com.aspc.remote.performance.tasks.CommandTask":defaultJavaClass;

        if( element.hasAttribute( ATT_JAVA_CLASS))
        {
            javaClass = element.getAttribute( ATT_JAVA_CLASS);
        }

        Class taskClass = Class.forName( javaClass);

        Class constructorSignature[] = {
            name.getClass(),
            BenchMarkClient.class
        };

        Constructor cons = taskClass.getConstructor(constructorSignature);

        Object objs[] = {name, client};

        Task task = (Task)cons.newInstance(objs);
        if( task instanceof JmsTask )
        {
            handleJmsRelatedAttributes( ( JmsTask )task, element );
        }

        /**
         * Set the location
         */
        task.setLocation( getLocation() );

        /**
         * Set the pre file, post file
         * and the data within the tasks.
         * How to handle the instructions within
         * are determined within the task itself
         */
        if( element.hasAttribute( "pre_file" ) )
        {
            task.setPreFileName( element.getAttribute( "pre_file" ) );
        }

        if ( element.hasAttribute( "post_file" ) )
        {
            task.setPostFileName( element.getAttribute( "post_file" ) );
        }

        if ( element.hasAttribute( "data_file" ) )
        {
            task.setDataFileName( element.getAttribute( "data_file" ) );
        }

        if( element.hasChildNodes() )
        {
            StringBuilder buff = new StringBuilder();
            NodeList nodes = element.getChildNodes();
            Node n;
            for( int i = 0; i < nodes.getLength(); i++ )
            {
                n = nodes.item( i );
                if( n.getNodeType() == Node.TEXT_NODE || n.getNodeType() == Node.CDATA_SECTION_NODE )
                {
                    buff.append( n.getNodeValue() );
                }
            }
            task.setData( buff.toString() );
        }

        task.setItemAttributes(element);

        if( element.hasAttribute( "varying"))
        {
            task.setVarying( element.getAttribute( "varying"));
        }

        if( element.hasAttribute( ATT_MARK_LOG))
        {
            task.setMarkLog( element.getAttribute( ATT_MARK_LOG));
        }

        if( element.hasAttribute( "varying_method"))
        {
            task.setVaryingMethod( element.getAttribute( "varying_method"));
        }
        if( element.hasAttribute( "summary"))
        {
            task.setSummary( element.getAttribute( "summary"));
        }

        return task;
    }

    private void handleJmsRelatedAttributes( JmsTask _jmsTask, Element _element ) throws Exception
    {
        /**
         * Required attributes if the task is a JMS Task
         */
        String[][] jmsRequired = {

            /* Topic of Queue */
            { "destination", "setDestination" }
        };
        String[][] jmsOptional = {
                /* If JNDI is to be used to get connection factory
                 */
            { "use_jndi", "setUseJndi" },

            /* Class name of initial context factory */
            { "context_factory", "setContextFactory" },

            /* Url of the JMS provider */
            { "provider_url", "setProviderUrl" },

            /* Connection factory identifier  */
            { "connection_factory", "setConnectionFactory" },

                /*
                 * This is used by publishing task to
                 * control the number of messages published
                 *
                 * Default is 1
                 */
            { "message_count", "setMessageCount" },

                /*
                 * This is number of seconds that the
                 * subscriber and/or publisher
                 * will wait before receiving and/or sending
                 * the next message
                 *
                 * Default is 0 milliseconds
                 */
            { "receive_delay", "setReceiveDelay" },
                /*
                 * The delay list
                 */
            { "delay_message", "setReceiveDelayList" },

            /*
             * total time to listen
             */
            { "listen_duration", "setListenDuration" },

                /*
                 * This is the size of the message in KB that is published.
                 * Default is 1K
                 */
            { "message_size", "setMessageSize" },

                /*
                 * This is domain name used by sonic mq
                 * Default is Domain1
                 */
            { "domain", "setDomain" },

                /*
                 * This is security principal required for sonic mq
                 * Default is Administrator
                 */
            { "security_principal", "setSecurityPrincipal" },

                /*
                 * This is security credentials required for sonic mq
                 * Default is Administrator
                 */
            { "security_credentials", "setSecurityCredentials" },

                /*
                 * JMS server hostname. Used if jndi is not to be used
                 * Default is localhost
                 */
            { "host_name", "setHostName" },

                /*
                 * JMS server port number. Used if jndi is not to be used
                 * Default is 1212
                 */
            { "port_number", "setPortNumber" },

                /*
                 * Providers Connection Factory Class Name. Used if jndi is not to be used
                 * Default is null
                 */
            { "connection_factory_class_name", "setConnectionFactoryClassName" },

                /*
                 * Providers Destination Class Name. Used if jndi is not to be used
                 * Default is null
                 */
            { "destination_class_name", "setDestinationClassName" }
        };

        /**
         * Validate attributes
         */
        checkAttributes(
            _element,
            getElements( jmsRequired, 0 ),
            getElements( jmsOptional, 0 )
        );

        /**
         * Everything is good - now start setting it
         */
        setAttributes( _jmsTask, _element, jmsRequired, jmsOptional );
    }

    /**
     * This function simply returns the specified
     * element of double dimensional array as an single
     * dimensional array
     * @param String[][] _darray
     * @return String[]
     */
    private String[] getElements( final String[][] _darray, final int _elementIndex )
    {
        String[] ret = new String[_darray.length];

        for( int i=0; i<_darray.length; i++ )
        {
            ret[i] = _darray[i][_elementIndex];
        }

        return( ret );
    }

    /**
     * This function sets the attributes from the element
     * @param JmsTask _jmsTask
     * @param Element _element
     * @param String _required
     * @param String _optional
     * @throws Exception Exception A serious problem
     */
    private void setAttributes( JmsTask _jmsTask,
        final Element _element,
        final String _required[][],
        final String _optional[][]
    ) throws Exception
    {
        for (String[] _required1 : _required) 
        {
            /**
             * Get the value of the attribute
             */
            String value = _element.getAttribute(_required1[0]);
            /**
             * Get the function
             */
            String function = _required1[1];
            /**
             * Invoke the function which will set the value
             */
            invokeFunction( _jmsTask, function, value );
        }

        for (String[] _optional1 : _optional) 
        {
            if (_element.hasAttribute(_optional1[0])) {
                /**
                 * Get the value of the attribute
                 */
                String value = _element.getAttribute(_optional1[0]);
                /**
                 * Get the function
                 */
                String function = _optional1[1];
                /**
                 * Invoke the function which will set the value
                 */
                invokeFunction( _jmsTask, function, value );
            }
        }
    }

    /**
     * This function invokes the function in
     * specified class with the
     * specified value
     * @param Class _class
     * @param String _function
     * @param String _value
     * @throws Exception Exception A serious problem
     */
    private void invokeFunction(
        final JmsTask _class,
        final String _function,
        final String _value
    ) throws Exception
    {
        Class c = _class.getClass();
        Class parameterTypes[] ={ String.class };

        Method m = c.getMethod( _function, parameterTypes );
        Object objargs[] ={ _value };

        try
        {
            m.invoke( _class, objargs );
        }
        catch( InvocationTargetException ite)
        {
            LOGGER.warn( "class: " + _class + " function: " + _function + " value: " + _value, ite);

            throw ite;
        }
    }

    private HTMLPage generate() throws Exception
    {
        HTMLPage page = new HTMLPage( );
        page.setTitle( title );

        HTMLTable mainTable = new HTMLTable();
        mainTable.setBorder(1);
        mainTable.setWidth( "90%");
        mainTable.setAlignment( "center");

        HTMLTable detailsTable = new HTMLTable();
        mainTable.setCell( detailsTable, 0, 0);
        detailsTable.setCell( "Start Time:",      0, 0);
        detailsTable.setCell( TimeUtil.format("dd MMM yyyy HH:mm:ss@z", new Date( startMS), TimeZone.getDefault() ),      0, 1);
        detailsTable.setCell( "Time Taken:",      1, 0);
        detailsTable.setCell( TimeUtil.getDiff(startMS),      1, 1);

        HTMLTable table = new HTMLTable();
        mainTable.setCell( table, 2, 0);

        page.addComponent( mainTable);

        table.setHeaderAsFirstRow( true);
        table.setHighlightOddRow( true);
        table.setWidth( "100%");
        table.setCell( "Client",      0, 0);
        clientCSV.append( "\"Client\",");

        table.setCell( "Status",    0, 1);
        clientCSV.append( "\"Status\",");

        table.setCell( "Loop",      0, 2);
        clientCSV.append( "\"Loop\",");

        table.setCell( "Min",       0, 3);
        clientCSV.append( "\"Min\",");

        table.setCell( "Max",       0, 4);
        clientCSV.append( "\"Max\",");

        table.setCell( "Avg",       0, 5);
        clientCSV.append( "\"Avg\",");

        //table.setCell( "First",      0, 6);
        //clientCSV.append( "\"First\",");

        table.setCell( "Mode",       0, 6);
        clientCSV.append( "\"Mode\",");

        table.setCell( "Median",      0, 7);
        clientCSV.append( "\"Median\",");

        table.setCell( "Remarks",     0, 8);
        clientCSV.append( "\"Remarks\"");

        clientCSV.append( "\n");

        int row = 1;

        for (Object clientList1 : clientList) 
        {
            BenchMarkClient r = (BenchMarkClient) clientList1;
            HTMLText name = new HTMLText( r.getName());
            clientCSV.append("\"").append(r.getName()).append( STR_SLASH_COMMA);
            name.setNoWrap( true);
            table.setCell( name, row, 0);
            String temp = r.getStatus();
            clientCSV.append("\"").append(temp).append( STR_SLASH_COMMA);
            HTMLText status = new HTMLText( temp);
            if( temp.equalsIgnoreCase( "OK") == false)
            {
                if( temp.equalsIgnoreCase( "FAST") ==true)
                {
                    status.setColor( Color.GREEN);
                }

                else if( temp.equalsIgnoreCase( "SLOW") ==true)
                {
                    status.setColor( Color.ORANGE);
                }
                else
                {
                    status.setColor( Color.RED);
                }
            }
            table.setCell( status, row, 1);
            temp = "" + r.getActualLoop();
            table.setCell( temp, row, 2);
            clientCSV.append("\"").append(temp).append( STR_SLASH_COMMA);
            temp = r.getMinDuration();
            HTMLText min = new HTMLText( temp);
            min.setNoWrap( true);
            table.setCell( min, row, 3);
            clientCSV.append("\"").append(r.getMinDurationMS()).append( STR_SLASH_COMMA);
            temp = r.getMaxDuration();
            HTMLText max = new HTMLText( temp);
            max.setNoWrap( true);
            table.setCell( max, row, 4);
            clientCSV.append("\"").append(r.getMaxDurationMS()).append( STR_SLASH_COMMA);
            temp = r.getMeanDuration();
            HTMLText avg = new HTMLText( temp);
            avg.setNoWrap( true);
            table.setCell( avg, row, 5);
            StringBuilder append = clientCSV.append("\"").append(r.getMeanMS()).append( STR_SLASH_COMMA);
            /*
            temp = r.getFirst();
            HTMLText waitTm = new HTMLText( temp);
            waitTm.setNoWrap( true);
            table.setCell( waitTm, row, 6);
            clientCSV.append( "\"" +r.getFirst() + STR_SLASH_COMMA);
             **/
            temp = r.getMode();
            HTMLText runTm = new HTMLText( temp);
            runTm.setNoWrap( true);
            table.setCell( runTm, row, 6);
            clientCSV.append("\"").append(r.getMode()).append( STR_SLASH_COMMA);
            temp = r.getMedianDuration();
            HTMLText median = new HTMLText( temp);
            median.setNoWrap( true);
            table.setCell( median, row,7);
            clientCSV.append("\"").append(temp).append( STR_SLASH_COMMA);
            temp = r.getError();
            table.setCell( temp, row , 8);
            clientCSV.append("\"").append(temp).append( "\"\n");
            HTMLTable taskTable = createTaskTable( r);
            page.addText( "\n");
            page.addComponent( taskTable);
            row++;
        }

        doReport( page);

        return page;
    }

    private void doReport( final HTMLPage page)
    {
        try
        {
            if( StringUtilities.isBlank( reportCmds))
            {
                /* nothing to see here... */
                return;
            }

            Client client = createRemoteClient();

            SoapResultSet r = client.fetch( reportCmds);

            for( int rc = 0; r != null; rc++)
            {
                if( rc > 0)
                {
                    page.addText( "\n\n");
                }
                else
                {
                    page.addText( "\n");
                }

                HTMLTable table = new HTMLTable();
                table.setBorder(1);
                table.setWidth( "90%");
                table.setAlignment( "center");
                String tmpTitle = r.getTitle(reportCmds);

                titleList.add( tmpTitle);
                StringBuilder buffer = new StringBuilder( );

                reportList.add( buffer);
                table.setCaption( tmpTitle);
                page.addComponent( table);

                table.setHeaderAsFirstRow( true);
                table.setHighlightOddRow( true);

                int columns = r.getColumnCount();
                for( int col = 1; col <= columns; col++)
                {
                    String name = r.getColumnName( col);
                    table.setCell( name, 0, col -1);
                    buffer.append("\"").append(name).append( STR_SLASH_COMMA);
                }

                for( int row = 1; r.next(); row++)
                {
                    buffer.append( "\n");

                    for( int col = 1; col <= columns; col++)
                    {
                        String value = r.getString( col);
                        table.setCell( value, row, col -1);
                        buffer.append("\"").append(value).append( STR_SLASH_COMMA);
                    }
                }

                r = r.nextResultSet();
            }

        }
        catch( Exception e)
        {
            HTMLText text = new HTMLText( e.toString());
            text.setColor( Color.RED);

            page.addText( "\n");
            page.addComponent( text);
        }
    }

    private boolean firstTask = true;

    private HTMLTable createTaskTable( final BenchMarkClient client) throws Exception
    {
        HTMLTable mainTable = new HTMLTable();
        mainTable.setBorder(1);
        mainTable.setWidth( "90%");
        mainTable.setAlignment( "center");
        mainTable.setCaption( "Client:" + client.getName());

        HTMLTable table = new HTMLTable();
        mainTable.setCell( table, 0, 0);

        table.setHeaderAsFirstRow( true);
        table.setHighlightOddRow( true);
        table.setWidth( "100%");
        if( firstTask)
        {
            itemCSV.append( "\"Client\",");
        }

        table.setCell( "Task",      0, 0);
        if( firstTask)
        {
            itemCSV.append( "\"Task\",");
        }

        table.setCell( "Status",    0, 1);
        if( firstTask)
        {
            itemCSV.append( "\"Status\",");
        }

        table.setCell( "Loop",      0, 2);
        if( firstTask)
        {
            itemCSV.append( "\"Loop\",");
        }

        table.setCell( "Min",       0, 3);
        if( firstTask)
        {
            itemCSV.append( "\"Min\",");
        }

        table.setCell( "Max",       0, 4);
        if( firstTask)
        {
            itemCSV.append( "\"Max\",");
        }

        table.setCell( "Avg",       0, 5);
        if( firstTask)
        {
            itemCSV.append( "\"Avg\",");
        }

        /*
         table.setCell( "First",      0, 6);
        if( firstTask)
        {
            itemCSV.append( "\"First\",");
        }
        */

        table.setCell( "Mode",       0,6);
        if( firstTask)
        {
            itemCSV.append( "\"Mode\",");
        }

        table.setCell( "Median",      0, 7);
        if( firstTask)
        {
            itemCSV.append( "\"Median\",");
        }

        table.setCell( "Remarks",     0, 8);
        if( firstTask)
        {
            itemCSV.append( "\"Remarks\"");
        }

        itemCSV.append( "\n");
        firstTask = false;

        Task list[] = client.listTask();
        int row = 1;
        for (Task list1 : list) 
        {
            itemCSV.append("\"").append(client.getName()).append( STR_SLASH_COMMA);
            Task r = list1;
            HTMLText name = new HTMLText( r.getName());
            itemCSV.append("\"").append(r.getName()).append( STR_SLASH_COMMA);
            name.setNoWrap( true);
            table.setCell( name, row, 0);
            String statusCode = r.getStatus();
            itemCSV.append("\"").append(statusCode).append( STR_SLASH_COMMA);
            HTMLText status = new HTMLText( statusCode);
            if( statusCode.equalsIgnoreCase( "OK") == false)
            {
                if( statusCode.equalsIgnoreCase( "FAST") ==true)
                {
                    status.setColor( Color.GREEN);
                }

                else if( statusCode.equalsIgnoreCase( "SLOW") ==true)
                {
                    status.setColor( Color.ORANGE);
                }
                else
                {
                    status.setColor( Color.RED);
                }
            }
            table.setCell( status, row, 1);
            table.setCell( "" + r.getActualLoop(), row, 2);
            itemCSV.append("\"").append(r.getActualLoop()).append( STR_SLASH_COMMA);
            HTMLText min = new HTMLText( r.getMinDuration());
            itemCSV.append("\"").append(r.getMinDurationMS()).append( STR_SLASH_COMMA);
            min.setNoWrap( true);
            table.setCell( min, row, 3);
            HTMLText max = new HTMLText( r.getMaxDuration());
            itemCSV.append("\"").append(r.getMaxDurationMS()).append( STR_SLASH_COMMA);
            max.setNoWrap( true);
            table.setCell( max, row, 4);
            HTMLText avg = new HTMLText( r.getMeanDuration());
            itemCSV.append("\"").append(r.getMeanMS()).append( STR_SLASH_COMMA);
            avg.setNoWrap( true);
            table.setCell( avg, row, 5);
            String temp;
            temp = r.getMode();
            HTMLText runTm = new HTMLText( temp);
            runTm.setNoWrap( true);
            table.setCell( runTm, row, 6);
            itemCSV.append("\"").append(r.getMode()).append( STR_SLASH_COMMA);
            temp = r.getMedianDuration();
            HTMLText median = new HTMLText( temp);
            median.setNoWrap( true);
            table.setCell( median, row,7);
            itemCSV.append("\"").append(temp).append( STR_SLASH_COMMA);
            temp = r.getError();
            if( statusCode.equals( "OK") && StringUtilities.isBlank( temp) == false)
            {
                LOGGER.info( "What the " + statusCode + ":" + temp);
                statusCode = r.getStatus();
                temp = r.getError();
                LOGGER.info( "part 2 " + statusCode + ":" + temp);
            }
            table.setCell( temp, row, 8);
            itemCSV.append("\"").append(temp).append( "\"\n");
            row++;
        }

        return mainTable;
    }

    private void sendMail(
        String toEmail,
        String subject,
        String htmlText
    ) throws Exception
    {
        Properties props = new Properties();

        Session session;

        String hst = CProperties.getProperty( "mail.smtp.host");

        if( hst != null)
        {
            props.put( "mail.smtp.host", hst);
        }

        session = Session.getInstance( props, null);

        MimeMessage msg;
        msg = new MimeMessage( session);

        StringTokenizer st=new StringTokenizer( toEmail, ",;");

        while( st.hasMoreTokens())
        {
            String email = st.nextToken();
            msg.addRecipient(RecipientType.TO, new InternetAddress( email));
        }
        msg.setSubject( subject);
        msg.setFrom( new InternetAddress( "benmark@stsoftware.com.au" ) );//#NOSYNC

        MimeMultipart mp = new MimeMultipart();

        MimeMultipart altMp = new MimeMultipart( "alternative");
        String txt = "Please view the attached file.";

        MimeBodyPart htmlPt = new MimeBodyPart();
        htmlPt.setContent( htmlText, "text/html");

        MimeBodyPart txtPt = new MimeBodyPart();

        txtPt.setContent( txt, "text/plain");

        altMp.addBodyPart( txtPt);
        altMp.addBodyPart( htmlPt);

        // The following allows us to add a multi part as a body element
        // We need to do this so that we can include additional attachments
        MimeBodyPart altBody = new MimeBodyPart();
        altBody.setContent( altMp);

        mp.addBodyPart( altBody);

        String str = title == null ? subject : title;

        String prefix = str.replace(' ', '_');

        addFile( htmlText, prefix, "html");
        addFile( clientCSV.toString(), prefix + "_client","csv");
        addFile( itemCSV.toString(), prefix + "_item","csv");

        for (Object clientList1 : clientList) 
        {
            BenchMarkClient t = (BenchMarkClient) clientList1;
            if( t instanceof BenchMarkWebClient)
            {
                Task list[] = t.listTask();

                for (Task task : list) 
                {
                    if( task instanceof ScreenLoadTask)
                    {
                        ScreenLoadTask screen = (ScreenLoadTask)task;

                        String page = screen.getLastPage();

                        if( StringUtilities.isBlank( page) == false)
                        {
                            addFile( page, screen.getName(), "html");
                        }
                    }
                }
            }
        }
        for( int i = 0; i < reportList.size(); i++)
        {
            String temp = (String) titleList.get( i);

            addFile( (StringBuilder)reportList.get( i), prefix + "_" + temp , "csv");
        }

        addXMLFiles( );

        for (Object attachmentList1 : attachmentList) 
        {
            Attachment attachment = (Attachment) attachmentList1;
            String name = attachment.getName();
            addFile(attachment.getContent(),name);
        }

        msg.setContent( mp);

        createScript(mp);// for attaching new script to the mail

        addZip( mp);
        Transport.send( msg);

        LOGGER.info( "Mail '" + subject + "' to:" + toEmail);
    }

    /**
     * This function is for adjust the timing of first/median for fast processes
     * and regenerate the script after adjust the time.
     * @param Mimemultipart mp.
     *
     */
    private void createScript(final MimeMultipart mp) throws Exception //NOPMD
    {
        Iterator elements =itemMap.keySet().iterator();

        while(elements.hasNext())
        {
            Element element=(Element)elements.next();

            ArrayList items=(ArrayList)itemMap.get( element);

            long    maxFirstTime=   Long.MIN_VALUE,
                    maxMedian=      Long.MIN_VALUE,
                    minFirstTime=   Long.MAX_VALUE,
                    minMedian=      Long.MAX_VALUE;

            boolean foundError = false;

            for (Object item1 : items) 
            {
                Item item = (Item) item1;
                if(item.isExcluded())
                {
                    continue; //no need to look for statistics as it is not ran.
                }
                String status = item.getStatus();
                // I do not want to adjust times if the element is slow.
                if(
                        status.equals( Item.STATUS_ERROR) ||
                        status.equals( Item.STATUS_SLOW)
                        )
                {
                    foundError = true;
                }
                if(maxFirstTime < item.firstTime)
                {
                    maxFirstTime=item.firstTime;
                }
                long median = item.getMedianMS();
                if(maxMedian < median)
                {
                    maxMedian=median;
                }
                if(minFirstTime >= item.firstTime)
                {
                    minFirstTime=item.firstTime;
                }
                if(minMedian >= median)
                {
                    minMedian=median;
                }
            }

            if( foundError ) continue;

            element.setAttribute( ATT_MAX_FIRST,TimeUtil.getDiff(0,(long) (maxFirstTime * 1.1)));

            element.setAttribute(ATT_MAX_MEDIAN,TimeUtil.getDiff(0, (long) (maxMedian * 1.1)));

            element.setAttribute( ATT_MIN_FIRST,TimeUtil.getDiff(0,(long) (minFirstTime * 0.9)));

            element.setAttribute( ATT_MIN_MEDIAN,TimeUtil.getDiff(0,(long) (minMedian * 0.9)));

        } // while end for clients

        if( currentScript != null)
        {
            String xmlScript=DocumentUtil.docToString(currentScript);

            String fileName = xmlFile.replace( '\\', '/');

            int pos = fileName.lastIndexOf( "/");

            if( pos != -1)
            {
                fileName = fileName.substring( pos + 1);
            }

            addFile( xmlScript,"adjusted_"+fileName);
        }
    }

    private void addXMLFiles( ) throws Exception
    {
        for (Object fileList1 : fileList) 
        {
            String file = (String) fileList1;
            String name = file.replace( '\\', '/');
            int pos = name.lastIndexOf( "/");
            if( pos != -1)
            {
                name = name.substring( pos + 1);
            }
            addFile( FileUtil.readFile( file), name );
        }
    }

    private void addZip( final MimeMultipart mp) throws Exception
    {
        if( zipOutputStream == null ) return;
        zipOutputStream.close();
        zipOutputStream = null;
        MimeBodyPart mpf = new MimeBodyPart();
        FileDataSource fds = new FileDataSource( zipFile);

        mpf.setDataHandler( new DataHandler( fds));

        mpf.setFileName( "attachments.zip");

        mp.addBodyPart( mpf);
    }

    private void addFile( final Object data, final String name) throws Exception
    {
        String first=name;
        String extension = "";
        int pos = first.lastIndexOf(".");

        if( pos != -1)
        {
            first = name.substring(0, pos);
            extension = name.substring( pos + 1);
        }

        addFile( data, first, extension);
    }

    private void addFile( final Object data, final String name, final String extension) throws Exception
    {
        if( data == null) return;
        if( zipOutputStream == null)
        {
            zipFile = File.createTempFile( "attachment", ".zip", FileUtil.makeQuarantineDirectory());
            zipOutputStream = new ZipOutputStream( new FileOutputStream( zipFile));
        }

        String temp = name;

        String tmpTitle = "";
        for( int j = 0; j < temp.length(); j++)
        {
            char c = temp.charAt( j);

            if(
                c >= 'a' && c <= 'z' ||
                c >= 'A' && c <= 'Z' ||
                c >= '0' && c <= '9'
            )
            {
                tmpTitle += c;//NOPMD
            }
            else
            {
                tmpTitle += "_";//NOPMD
            }
        }

        while( tmpTitle.contains("__"))
        {
            tmpTitle = tmpTitle.replace( "__", "_");
        }

        try
        {
            ZipEntry ze = new ZipEntry( tmpTitle + "." + extension );
            zipOutputStream.putNextEntry( ze);
        }
        catch( ZipException ze)
        {
            // Ignore clones
            LOGGER.warn( "duplicate name", ze);
            return;
        }

        byte bytes[] = data.toString().getBytes("utf8");
        zipOutputStream.write( bytes, 0, bytes.length);
        zipOutputStream.closeEntry();
    }

    /**
     * This function creates the client to the server
     * specified by remote URL passed
     * as a command line parameter
     * @return Client
     * @throws Exception Exception A serious problem
     */
    private Client createRemoteClient() throws Exception
    {
        return( BenchMarkClient.createRemoteClient( this.remoteURL,
                new LoginContext("", this.timeZoneName, this.timeZoneOffset, null, this.language, null,null, false,null,false, null, -1, -1) ) );
    }

    /**
     * The main for the program
     * @param argv Parameters to control the processing of the benchmark application
     */
    public static void main(String argv[])
    {
        SSLUtilities.trustAllHostNames();
        SSLUtilities.trustAllCertificates();
        new BenchMarkApp().execute(argv);
    }

    private Document currentScript;
    private String location = "";
    private final StringBuilder clientCSV = new StringBuilder( 150);//NOPMD

    private final StringBuilder itemCSV = new StringBuilder( 150);//NOPMD

    private final ArrayList   reportList = new ArrayList();
    private final ArrayList   fileList = new ArrayList();
    private final ArrayList   clientList = new ArrayList();
    private final ArrayList   titleList = new ArrayList();
    private final ArrayList   attachmentList = new ArrayList();

    private String      remoteURL,
            xmlFile,
            emailList,
            reportCmds,
            timeZoneName,
            timeZoneOffset,
            language,
            title = "Bench Mark Results";

    private boolean titleFound;

    private static long startMS;
    /**
     *
     */
    public static final String ROOT_BENCHMARK="BENCHMARK";

    private static final String NODE_CLIENT="CLIENT";
    private static final String NODE_WEB_CLIENT="WEB_CLIENT";
    private static final String ATT_CLONE_FREQUENCY="clone_frequency";

    /**
     *BenchMark  Performance  measure Attributes name
     */
    public static final String ATT_MIN_FIRST =  "min_first";

    /**
     * until_isset stop after this property isset
     */
    public static final String ATT_UNTIL_ISSET =  "until_isset";

    /**
     * on_error_set when an error occurs set this property
     */
    public static final String ATT_ON_ERROR_SET =  "on_error_set";

    /**
     * disable feature
     */
    public static final String ATT_DISABLE =  "disable";
    /**
     *
     */
    public static final String ATT_MAX_FIRST =  "max_first";
    /**
     *
     */
    public static final String ATT_MIN_MODE =   "min_mode";
    /**
     *
     */
    public static final String ATT_MAX_MODE =   "max_mode";
    /**
     *
     */
    public static final String ATT_MIN_MEDIAN = "min_median";
    /**
     *
     */
    public static final String ATT_MAX_MEDIAN = "max_median";
    /**
     *
     */
    public static final String ATT_MIN_MEAN =   "min_mean";
    /**
     *
     */
    public static final String ATT_MAX_MEAN =   "max_mean";
    /**
     *
     */
    public static final String ATT_MAX_CONTENT =   "match_content";

    /**
     * should reset the server's memory before running the tests
     */
    public static final String ATT_RESET    =   "reset";

    /**
     * should sleep after reseting the memory
     */
    public static final String ATT_RESET_SLEEP    =   "reset_sleep";

    /**
     * the validation pattern
     */
    public static final String ATT_VALIDATE = "validate";

    /**
     * the result match string
     */
    public static final String ATT_IS_LIKE = "is_like";

    /**
     * To match the task to run
     */
    public static final String ATT_TASK_FILTER = "task_filter";

    /**
     * the validation formula
     */
    public static final String ATT_VALIDATE_FORMULA = "validate_formula";

    /**
     * The loop frequency attribute name
     */

    public  static final String ATT_LOOP_FREQUENCY = "loop_frequency";
    private static final String ATT_MARK_LOG = "mark_log";
    private static final String ATT_DATA_FILE="data_file";
    private static final String ATT_NAME="name";
    private static final String ATT_JAVA_CLASS="java_class";
    private static final String ATT_FILE="file";
    private static final String ATT_CLONE="clone";
    private static final String STR_SLASH_COMMA="\",";
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.performance.BenchMarkApp");//#LOGGER-NOPMD

    /**
     * Declaration for Recreation of report
     */
    private final HashMap itemMap=HashMapFactory.create();
}
