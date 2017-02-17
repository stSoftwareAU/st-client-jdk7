/*
 *  Copyright (c) 2000-2004 ASP Converters pty ltd
 *
 *  www.aspconverters.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.memory.selftest;

import com.aspc.remote.memory.MemoryManager;
import com.aspc.remote.memory.MemoryManager.Collector;
import com.aspc.remote.util.misc.*;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.commons.logging.Log;

/**
 * Check we handle G1GC
 *
 *  @author      Nigel Leck
 *  @since       21 Oct 2015
 */
public class TestG1 extends TestCase
{
    /**
     * Constructor for the test unit.
     * @param testName The name of the test unit
     */
    public TestG1(String testName)
    {
        super(testName);
    }

    /**
     * The main for the program
     *
     * @param args The command line arguments
     */
    public static void main(String[] args)
    {
        //parseArgs( args);
        TestRunner.run(suite());
    }

    /**
     * Creates the test suite.
     *
     * @return The test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestG1.class);
        return suite;
    }

    /**
     * Detect the type.
     * @throws Exception a serious problem
     */
    public void testDetectType() throws Exception
    {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> inputArguments = runtimeMXBean.getInputArguments();

        for( String argument: inputArguments)
        {
            LOGGER.info(argument);
        }

//        MemoryMXBean mBean=ManagementFactory.getMemoryMXBean();

        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();

        boolean g1Used=false;
        for( GarbageCollectorMXBean garbageCollectorMXBean:garbageCollectorMXBeans)
        {
            String names[] = garbageCollectorMXBean.getMemoryPoolNames();
            for( String name: names)
            {
                LOGGER.info("GC: " + name);
                if( name.equalsIgnoreCase(MemoryManager.Collector.G1.oldGenerationPoolName))
                {
                    g1Used=true;
                }
            }
        }
        assert MemoryManager.COLLECTOR != Collector.PS;
        if( g1Used)
        {
            assertEquals("Should be G1", Collector.G1, MemoryManager.COLLECTOR );
        }
        else
        {
            assertEquals("Should be CMS", Collector.CMS, MemoryManager.COLLECTOR );
        }
        String details=MemoryManager.getDetails();
        LOGGER.info(details);
    }

    /**
     * Check the G1 defaults.
     * @throws Exception a serious problem
     */
    public void testDefaultsG1() throws Exception
    {
        if( MemoryManager.COLLECTOR != Collector.G1) return;

        long totalMemory = MemoryManager.getTotalMemory();
        long expectedThreshold = totalMemory/100L * Collector.G1.defaultInitiatingOccupancyFaction - ( totalMemory/100L * Collector.G1.defaultResercePercent);

        long threshold = MemoryManager.calculatedTenuredThreshold();
        assertEquals( "Initating occupancy fraction default", expectedThreshold, threshold);

        MemoryManager.checkZone();
        long requiredThreadhold = MemoryManager.calculatedTenuredThreshold();
        long collectionUsageThreshold=MemoryManager.getCollectionUsageThreshold();

        assertEquals( "Collection Usage Threshold", requiredThreadhold, collectionUsageThreshold);
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.selftest.TestG1");//#LOGGER-NOPMD
}
