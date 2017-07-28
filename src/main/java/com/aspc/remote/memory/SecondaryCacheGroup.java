/*
 *  Copyright (c) 1999-2004 ASP Converters pty ltd
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.memory;


/**
 *  The secondary cache group. The cache will be cleared at the group and child level. 
 *
 *  <br>
 *  <i>THREAD MODE: READONLY memory management</i>
 *
 * @author Nigel Leck
 * @since 12 April 2011
 */
public interface SecondaryCacheGroup
{
    /**
     * 
     * @return the group key
     */
    Object getSecondaryCacheGroupKey();
            
}
