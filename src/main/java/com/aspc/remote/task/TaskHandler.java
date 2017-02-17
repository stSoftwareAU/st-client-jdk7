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
package com.aspc.remote.task;

import com.aspc.remote.jdbc.Executor;
import com.aspc.remote.jdbc.SoapResultSet;
import javax.annotation.Nonnull;

/**
 * TaskHandler defines a method that is called by a SyncManager to process a task.
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 * @author luke
 * @since 28 September 2005
 */
public interface TaskHandler
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
     *
     * @throws Exception a serious problem
     */
    void handleTask( final @Nonnull Executor executor, final @Nonnull String transid, final @Nonnull SoapResultSet transRecord ) throws Exception;

    /**
     * begin the task and process any parameters
     * @param manager the task manager
     * @param executor the server connection
     * @param parameters parameters for starting the task
     * @throws Exception a serious problem
     */
    void beginTask( final @Nonnull TaskManager manager, final @Nonnull Executor executor, final @Nonnull String parameters ) throws Exception;
}
