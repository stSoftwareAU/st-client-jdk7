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

import com.aspc.remote.soap.Client;
import com.aspc.remote.soap.LoginContext;
import com.aspc.remote.util.misc.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.apache.commons.logging.Log;

/**
 *  App
 *
 * @author Nigel Leck
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @since       7 April 2001
 */
public class BenchMarkClient extends Item implements Runnable
{
    /**
     * Constructor for BenchMarkClient
     * Creates a client that can be used by tasks to make soap requests
     * 
     * @param connectionUrl - url identifying the user details and database to connect to
     * @param loginContext - details about the users physical environment
     * @param name - a name to uniquely identify the client
     * @throws Exception - an error has occurred instantiating the clients
     */
    public BenchMarkClient( String name,  String connectionUrl, LoginContext loginContext) throws Exception
    {
        super( name);
        this.connectionUrl = connectionUrl;
        this.loginContext = loginContext;
        rClient = createRemoteClient( connectionUrl, loginContext);
        
        taskList = new ArrayList();
        t = new Thread( this);
        t.setDaemon( true);
        
    }
    
    /**
     * Retrieves the remote client that can be used for making soap requests
     * @return the remote client
     */
    public Client getRemoteClient()
    {
        return rClient;
    }
    
    /**
     * This function creates the client to the server
     * specified by remote url passed
     * as a parameter.
     * @param remoteURL The remote url
     * @param loginContext The login context
     * @return client - the soap client
     * @throws Exception - the client could connect
     */
    public static final Client createRemoteClient( final String remoteURL, final LoginContext loginContext ) throws Exception
    {
        Client client = new Client( remoteURL);
        client.setByPassDiscover( true);
        String login = client.currentTransport().getDefaultLogin();
        String passwd = client.currentTransport().getDefaultPassword();
        String layer = client.currentTransport().getDefaultLayer();
        
        try
        {
            client.login( 
                login,
                passwd,
                layer,
                loginContext
            );
        }
        catch( Exception e)
        {
            LOGGER.error( "could not login", e);
            
            throw e;
        }
        
        return client;
    }

    /**
     * The name of the other clients that this client will wait until finished before starting.
     * 
     * <pre>
     *   &lt;CLIENT name ="my client" clone="5" wait="15 secs" loop="5" <b>depends="setup, load cache"</b>>
     * </pre>
     * @param depends - the name of the dependent item
     */
    public void setDepends( String depends)
    {
        this.depends = depends;
    }

    /**
     * The name of the other clients that this client will wait until finished before starting.
     * 
     * <pre>
     *   &lt;CLIENT name ="my client" clone="5" wait="15 secs" loop="5" <b>depends="setup, load cache"</b>>
     * </pre>
     * @return the name of the dependent item
     */
    public String getDepends( )
    {
        return depends;
    }

    
    /**
     * Adds another client as a dependent of this client
     * @param depend - the benchmark client
     */
    public void iAddDepend( BenchMarkClient depend)
    {
        if( dependList == null) dependList = new ArrayList();

        dependList.add( depend);
    }

    
   /**
     * The url that this client is connected to.
     * @return - the url
     */
    public String getConnectionUrl()
    {
        return connectionUrl;
    }
      
   /**
     * Login context containing details of the physical environment of the user
     * @return the login context
     */
    public LoginContext getLoginContext()
    {
        return loginContext;
    }
    
    /**
     * Adds a child task to this client
     * @param task - the task to add
     */
    public void addTask( Task task)
    {
        String taskName = task.getName();
        if(taskFilter != null)
        {
            if(includeTask(taskFilter, taskName))
            {
                taskList.add( task);
            }
            else
            {
                task.setExcluded(true);
            }
        }
        else
        {
            taskList.add( task);
        }
        
    }
    
    /**
     * The current cause of status for  this client
     * @return - the cause of status if status is not OK
     */
    @Override
    public String getError() 
    {        
        Task worstTask = worstTask();

        if( worstTask != null) 
        {            
            return "due to Task \""+ worstTask.getName()+" \""; 
        }
        
        return super.getError();         
    }
    
    private Task worstTask() 
    {        
        Task worstTask = null;
        if(taskList.size() > 0) 
        {
            String worstStatus = Item.STATUS_OK;
            
            for (Object taskList1 : taskList) {
                Task task = (Task) taskList1;
                String status = task.getStatus();
                if( status.equals( Item.STATUS_OK)) continue;
                if( status.equals( worstStatus)) continue;
                if( worstStatus.equals( Item.STATUS_ERROR)) continue;// we have the worst one posible
                if( worstStatus.equals( Item.STATUS_SLOW) && status.equals( Item.STATUS_FAST)) continue;/// slow is worst than fast
                worstStatus = status;
                worstTask = task;
            }
        }
        
        return worstTask;
     }

    /**
     * The current status of this client
     * @return - the status which can be one of "Wait", "Run", "Error", "Slow" or "OK"
     */
    @Override
    public String getStatus()
    {
        if( t == null) return "Wait";

        if( ThreadUtil.isAliveOrStarting(t)) return "Run";
        Task worstTask = worstTask();

        if( worstTask != null) 
        {            
            return worstTask.getStatus(); 
        }

        return super.getStatus();
    }

    /**
     * Returns an array of child tasks
     * @return - the task list
     */
    public Task[] listTask()
    {
        Task list[] = new Task[ taskList.size()];

        taskList.toArray( list);

        return list;
    }

    /**
     * Starts this client
     */
    @Override
    public void run()
    {
        execute();
    }

    /**
     * Executes the associated tasks
     * @throws Exception - a error has occurred processing one of the tasks
     */
    @Override
    public void process() throws Exception
    {
        for (Object taskList1 : taskList) {
            Task task = (Task) taskList1;
            try
            {
                task.execute();
            }
            catch( Exception e)
            {
                task.setError( e);
                LOGGER.warn( task.getName(), e);
            }
        }
    }    
    
    /**
     * Wait for clients that this client depends on to finish.
     * @throws Exception - an error occurred when waiting for other clients to finish
     */
    @Override
    protected void executeWait() throws Exception
    {
        super.executeWait();

        if( dependList != null)
        {
            for (Object dependList1 : dependList) {
                BenchMarkClient bmc = (BenchMarkClient) dependList1;
                bmc.join();
            }
        }

        Thread.sleep( cloneDelay);
    }

    /**
     * Sets the delay between processes when processing this client multiple times
     * @param frequency - the delay between processes in the format of a duration, i.e "5 secs"
     * @param seq - the sequence of the client relative to other clients
     * @throws Exception - An error occurred converting the duration to milliseconds
     */
    public void setCloneFrequency( String frequency, int seq) throws Exception
    {
        cloneDelay = TimeUtil.convertDurationToMs( frequency) * seq;
    }

    /**
     * Starts this client
     */    
    public void start()
    {
        t.setName( getName());
        t.start();
    }

    /**
     * Waits for the thread to finish
     * @throws Exception - problem occurred when joining current thread
     */
    public void join() throws Exception
    {
        t.join();
    }

    /**
     * 
     * @param taskfilters
     * @param task
     * @return the value
     */
    private boolean includeTask(String taskfilters, String task)
    {        
        StringTokenizer st = new StringTokenizer(taskfilters, ",");
        while(st.hasMoreTokens())
        {
            String filter = st.nextToken();
            if(StringUtilities.isLike(filter, task))
            {
                return true;
            }
        }
        return false;
    }

    private long cloneDelay;

    private String  depends;
    
    private ArrayList   dependList;
    private final ArrayList   taskList;
    private final Thread  t;
    private final Client  rClient;
    private final String connectionUrl;
    private final LoginContext loginContext;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.performance.BenchMarkClient");//#LOGGER-NOPMD
}
