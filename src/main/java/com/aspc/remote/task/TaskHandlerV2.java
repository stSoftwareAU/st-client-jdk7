package com.aspc.remote.task;

import com.aspc.remote.jdbc.Executor;
import com.aspc.remote.jdbc.SoapResultSet;

/**
 *
 *  @author      Lei Gao
 *  @version     $Revision: 1.1 $
 *  @since       23 February 2016 
 */
public interface TaskHandlerV2 extends TaskHandler
{
    /**
     * Handles a task notification.
     * @param executor an Executor object for running sql
     * @param transid the transaction id for this notification
     * @param transRecord the transaction record for this trnasaction id
     *    +--------+--------+--------+----------------+----------+------+-----------+
     *    |trans_id|layer_id|dbclass |global_key      |row_uid   |action|action_name|
     *    +--------+--------+--------+----------------+----------+------+-----------+
     *    |   82115|       1|DBFolder|4295260035~765@1|4295260035|D     |Delete     |
     *    |   82115|       1|DBFolder|4295260038~765@1|4295260038|D     |Delete     |
     *    +--------+--------+--------+----------------+----------+------+-----------+  
     * @param trans_ms transaction time in ms
     * @throws Exception a serious problem
     */
    void handleTaskV2( final Executor executor, final String transid, final SoapResultSet transRecord, final long trans_ms ) throws Exception;

    /**
     * pre handle a task
     * @param transid
     * @param trans_ms
     * @return false to stop this task and all afterward tasks
     * @throws Exception
     */
    boolean preHandleTask( final String transid, final long trans_ms ) throws Exception;
}
