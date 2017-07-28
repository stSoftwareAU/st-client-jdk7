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

import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.w3c.dom.Element;

/**
 *  benchmark item
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author Nigel Leck
 *  @since       7 April 2001
 */
public abstract class Item 
{
    /**
     * error status
     */
    public static final String STATUS_ERROR="Error";
    /**
     * slow status
     */
    public static final String STATUS_SLOW="Slow";
    /**
     * fast status
     */
    public static final String STATUS_FAST="Fast";
    /**
     * everything is OK
     */
    public static final String STATUS_OK="OK";

    /** the max GC time */
    public static final int GC_MAX_TIME=3*60*1000;    

    /**
     *
     * @param name
     */
    public Item(final String name) 
    {
        this.name = name;
        loop = 1;
    }
    
    /**
     * The name of this client.
     *
     * <pre>
     *   &lt;CLIENT <b>name ="my client"</b> clone="5" wait="15 secs" loop="5" depends="setup, load cache">
     * </pre>
     * @return the value
     */
    public String getName() 
    {
        return name;
    }
    
    /**
     *
     * @param duration
     * @throws Exception a serious problem
     */
    public void setLoopTime( final String duration) throws Exception 
    {
        loop_ms = TimeUtil.convertDurationToMs( duration);
    }
    
    /**
     *
     * @param problem
     */
    public void setError( final Exception problem)
    {
        error = problem;
    }
    
    /**
     *
     * @param duration
     * @throws Exception a serious problem
     */
    public void setPauseTime( final String duration) throws Exception 
    {
        pause_ms = TimeUtil.convertDurationToMs( duration);
    }
    
    /**
     *
     * @param duration
     * @throws Exception a serious problem
     */
    public void setWaitTime( final String duration) throws Exception 
    {
        wait_ms = TimeUtil.convertDurationToMs( duration);
    }
    
    /**
     *
     * @param duration
     * @throws Exception a serious problem
     */
    public void setStaggerTime( final String duration) throws Exception 
    {
        stagger_ms = TimeUtil.convertDurationToMs( duration);
    }
    
    /**
     *
     * @param duration
     * @throws Exception a serious problem
     */
    public void setMaxTime( final String duration) throws Exception
    {
        max_acceptable_ms = TimeUtil.convertDurationToMs( duration);
    }

    /**
     * This sets the value of serverReset if "reset" specify in the client/task tag
     * @param resetFlag
     * @throws Exception a serious problem
     */
    public void setResetFlag( final String resetFlag) throws Exception
    {
        if(resetFlag.trim().equalsIgnoreCase("Y") || resetFlag.trim().equalsIgnoreCase("true"))
        {
            serverReset =true;
        }
    }

    /**
     * Set the time to sleep after the reset
     * @param duration
     * @throws Exception a serious problem
     */
    public void setResetSleep( final String duration) throws Exception
    {
        serverResetSleepMS = TimeUtil.convertDurationToMs( duration);
        if( maxAcceptableFirst != 0)
        {
           serverReset = true;
        }
    }

    /**
     * set the validation pattern
     * @param validatePatternString the pattern
     * @throws Exception Exception A serious problem
     */
    public void setValidatePattern( final String validatePatternString ) throws Exception
    {
        if( ! StringUtilities.isBlank( validatePatternString ) )
        {
            validatePattern = Pattern.compile( validatePatternString);
        }
        else
        {
            validatePattern=null;
        }
    }

     /**
     * set the validation pattern
     * @param validatePatternString the pattern
     * @throws Exception Exception A serious problem
     */
    public void setMatchResult( final String matchResultString ) throws Exception
    {
        if( ! StringUtilities.isBlank( matchResultString ) )
        {
            matchResult = matchResultString;
        }
        else
        {
            matchResult=null;
        }
    }

     /**
     * set the validation pattern
     * @param validatePatternString the pattern
     * @throws Exception Exception A serious problem
     */
    public void setTaskFilter( final String taskFilterString ) throws Exception
    {
        if( ! StringUtilities.isBlank( taskFilterString ) )
        {
            taskFilter = taskFilterString;
        }
        else
        {
            taskFilter=null;
        }
    }

     /**
     * set this task is excluded
     * @param excludedFlag the flag to set
     *
     */
    public void setExcluded( final boolean excludedFlag )
    {
        exclude = excludedFlag;
    }

    /**
     * return excluded flag
     * @return the value
     */
    public boolean isExcluded()
    {
        return exclude;
    }

    /**
     * set the validation formula
     * @param validateFormulaString the formula
     * @throws Exception Exception A serious problem
     *
    public void setValidateFormula( final String validateFormulaString ) throws Exception
    {
        if( ! StringUtilities.isBlank( validateFormulaString ) )
        {
            validateFormula = new ValidateFormula( validateFormulaString);
        }
        else
        {
            validateFormula=null;
        }
    }*/

    /**
     * Set the  value of Maximum first  time 
     * @param duration
     * @throws Exception a serious problem
     */    
    public void setMaxFirst( final String duration) throws Exception 
    {
        maxAcceptableFirst = TimeUtil.convertDurationToMs( duration);
        if( maxAcceptableFirst != 0)
        {
           serverReset = true;
        }
    }

    /**
     * get the max acceptable first
     * @return the max acceptable first
     */
    protected long getMaxAcceptableFirst()
    {
        return maxAcceptableFirst;
    }
    /**
     * Set the  value of Minimum first  time 
     * @param duration
     * @throws Exception a serious problem
     */
    
    public void setMinFirst( final String duration) throws Exception 
    {
        minAcceptableFirst = TimeUtil.convertDurationToMs( duration);
        if( minAcceptableFirst != 0)
        {
           serverReset = true;
        }
    }
    
    /**
     *  Set the  value of Maximum Mean  time 
     * @param duration
     * @throws Exception a serious problem
     */
    
    public void setMaxMean( final String duration) throws Exception 
    {
          maxAcceptableMean = TimeUtil.convertDurationToMs( duration);
    }
    
    /**
     * Set the  value of Minimum Mean time 
     * @param duration
     * @throws Exception a serious problem
     */
    
    public void setMinMean( final String duration) throws Exception 
    {
         minAcceptableMean = TimeUtil.convertDurationToMs( duration);
    }
    
    /**
     * Set the  value of Maximum Mode  time 
     * @param duration
     * @throws Exception a serious problem
     */
    
    public void setMaxMode( final String duration) throws Exception
    {
          maxAcceptableMode = TimeUtil.convertDurationToMs( duration);
    }
    
    /**
     * Set the  value of Minimum Mode  time 
     * @param duration
     * @throws Exception a serious problem
     */
    
    public void setMinMode( final String duration) throws Exception 
    {
          minAcceptableMode = TimeUtil.convertDurationToMs( duration);
    }
    
    /**
     * Set the  value of Maximum Median time 
     * @param duration
     * @throws Exception a serious problem
     */
    
    public void setMaxMedian( final String duration) throws Exception 
    {
        maxAcceptableMedian = TimeUtil.convertDurationToMs( duration);
    }
    
    /**
     * Set the  value of Minimum Median time
     * @param duration
     * @throws Exception a serious problem
     */
    
    public void setMinMedian( final String duration) throws Exception 
    {
        minAcceptableMedian = TimeUtil.convertDurationToMs( duration); 
    }
    
    /**
     *
     * @param duration
     * @throws Exception a serious problem
     */
    public void setLoopFrequency( final String duration) throws Exception 
    {
        loop_frequency_ms = TimeUtil.convertDurationToMs( duration);
    }
    
    /**
     *
     * @param loop
     */
    public void setLoop( final int loop) 
    {
        this.loop = loop;
    }
    /**
     * @return Returns the job.
     */
    public boolean isJob() 
    {
        return job;
    }
    /**
     * @param job The job to set.
     */
    public void setJob(final boolean job) 
    {
        this.job = job;
    }
    
    /**
     *
     * @return the value
     */
    public int getLoop( ) 
    {
        return loop;
    }
    
    /**
     *
     * @return the value
     */
    public int getActualLoop() 
    {
        return actualLoop;
    }

    private String makeFastError()
    {
        int location = 0;
        int worstPercentage = 0;
        long worstMS = 0;

        if( CProperties.isDisabled(Task.DISABLE_TASK_RESET) == false)
        {
            if(minAcceptableFirst >firstTime && firstTime != 0)
            {
                int percentage=(int)(((minAcceptableFirst-firstTime)*100)/firstTime);

                if( percentage > worstPercentage)
                {
                    worstMS = minAcceptableFirst;
                    worstPercentage = percentage;
                    location = 0;
                }
            }

            long mode = getModeMS();
            if(minAcceptableMode >mode && mode != 0)
            {
                int percentage=(int)(((minAcceptableMode-mode)*100)/mode);

                if( percentage > worstPercentage)
                {
                    worstMS = minAcceptableMode;
                    worstPercentage = percentage;
                    location = 1;
                }
            }
        }
        
        long mean = getMeanMS();
        
        if(minAcceptableMean >mean && mean != 0)
        {
            int percentage=(int)(((minAcceptableMean-mean)*100)/mean);

            if( percentage > worstPercentage)
            {
                worstMS = minAcceptableMean;
                worstPercentage = percentage;
                location = 2;
            }
        }
 
        long median = getMedianMS();
        
        if(minAcceptableMedian >median && median != 0)
        {
            int percentage=(int)(((minAcceptableMedian-median)*100)/median);

            if( percentage > worstPercentage)
            {
                worstMS = minAcceptableMedian;
                worstPercentage = percentage;
                location = 3;
            }
        }
 
        if(worstPercentage > 0) 
        {
            return "Min "+ attributes[location] +" of "+TimeUtil.getDiff(0,worstMS) +
                    " surpassed by "+ worstPercentage  + "%";
        }
        return null;
    }
    
    private String makeSlowError()
    {
        int location = 0;
        int worstPercentage = 0;
        long worstMS = 0;
        if(maxAcceptableFirst <firstTime && maxAcceptableFirst != 0)
        {
            int percentage=(int)(((firstTime - maxAcceptableFirst)*100)/maxAcceptableFirst);

            if( percentage > worstPercentage)
            {
                worstMS = maxAcceptableFirst;
                worstPercentage = percentage;
                location = 0;
            }
        }
        
        long mode = getModeMS();
        
        if(maxAcceptableMode < mode && maxAcceptableMode != 0)
        {
            int percentage=(int)(((mode - maxAcceptableMode)*100)/maxAcceptableMode);

            if( percentage > worstPercentage)
            {
                worstMS = maxAcceptableMode;
                worstPercentage = percentage;
                location = 1;
            }
        }
 
        long mean = getMeanMS();
        if(maxAcceptableMean < mean && maxAcceptableMean != 0)
        {
            int percentage=(int)(((mean - maxAcceptableMean)*100)/maxAcceptableMean);

            if( percentage > worstPercentage)
            {
                worstMS = maxAcceptableMean;
                worstPercentage = percentage;
                location = 2;
            }
        }
 
        long median = getMedianMS();
        if(maxAcceptableMedian < median && maxAcceptableMedian != 0)
        {
            int percentage=(int)((( median- maxAcceptableMedian)*100)/maxAcceptableMedian);

            if( percentage > worstPercentage)
            {
                worstMS = maxAcceptableMedian;
                worstPercentage = percentage;
                location = 3;
            }
        }
 
        if(worstPercentage > 0) 
        {
            return "Max "+ attributes[location] +" of "+TimeUtil.getDiff(0,worstMS) +
                    " exceeded by "+ worstPercentage  + "%";
        }
        
        return null;
    }
    
    /**
     * return the error based on the status and cause of status 
     * @return the value
     */
    public String getError() 
    {

        if( error != null)
        {
            String msg = error.getMessage();

            if( StringUtilities.isBlank(msg) == false) return msg;

            return error.toString();
        }
        
        String temp;
        
        temp = makeSlowError();
        
        if( StringUtilities.isBlank( temp) == false) return temp;

        if( loop_frequency_ms != 0)
        {
            if( max > loop_frequency_ms) 
            {
                return "Loop frequency: " + TimeUtil.getDiff( start,start+loop_frequency_ms);
            }
        }

        temp = makeFastError();
        
        if( StringUtilities.isBlank( temp) == false) return temp;

        return "";
    }
    
    /**
     * It returns the status of client/task based on performance
     * @return status of current task
     */
    public String getStatus() 
    {        
        if( error != null) return STATUS_ERROR;
        String temp;
        
        temp = makeSlowError();
        
        if( StringUtilities.isBlank( temp) == false) return STATUS_SLOW;

        if( loop_frequency_ms != 0) 
        {
            if( max > loop_frequency_ms) 
            {
                return STATUS_SLOW;
            }
        }
        
        temp = makeFastError();
        
        if( StringUtilities.isBlank( temp) == false) return STATUS_FAST;

        return STATUS_OK;
    }
    
    /**
     *
     * @return the value
     */
    public String getEndTm() 
    {
        if( end == 0)
        {
            return TimeUtil.getDiff( BenchMarkApp.getStartTm());
        } 
        else 
        {
            return TimeUtil.getDiff( BenchMarkApp.getStartTm(), end);
        }
    }
    
    /**
     * 
     * @return time in milliseconds
     */
    public String getEndTmMS() 
    {
        long temp = end;
        
        if( temp == 0) 
        {
            temp = System.currentTimeMillis();
        }
        
        return "" + ( temp - BenchMarkApp.getStartTm());
    }
    
    /**
     *
     * @return the value
     *
     */
    public String getStartTm()
    {
        return TimeUtil.getDiff( BenchMarkApp.getStartTm(), start);
    }
    
    /**
     * 
     * @return time in milliseconds
     */
    
    public String getStartTmMS() 
    {
        return "" + ( start - BenchMarkApp.getStartTm());
    }
    
    /**
     * This function for only  calculation of mean
     * and initialise actual mean
     * @return the mean time in MS
     */
    public long getMeanMS()
    {
        
        if(dataTime == null || dataTime.length == 0)
        {
            return 0;
        }

        long tmpMean=0;
        for(int j=0;j<dataTime.length;j++) 
        {
            tmpMean+=dataTime[j];
        }
        long mean;
        mean=tmpMean/dataTime.length;
        
        return mean;
    }
    
    /**
     * This function for calculation of MODE
     * and initialise the actual Mode
     * @return calculated mode
     */
    public long getModeMS()
    {        
        int  actCount=0;
        long  actElement=0;
        int i=1;
        while( dataTime != null && i < dataTime.length)
        {
            int count=0;
            
            if(dataTime[i-1] != dataTime[i])
            {
                i++;
            } 
            else if(i < dataTime.length) 
            {
                while(i < dataTime.length && dataTime[i-1]==dataTime[i])
                {
                    count++;
                    i++;
                    
                    if(i > dataTime.length)
                    {
                        break;
                    }
                }
                if(i < dataTime.length && count > actCount)
                {
                    actCount=count;
                    actElement=dataTime[i];
                    
                }
            }
        }
        
        if(dataTime != null && dataTime.length >= 1 && actElement==0)
        {
            actElement=dataTime[0];
        }
        
        return actElement;
    }

    /**
     * This function for calculation of MODE
     * and initialise the actual Mode
     * @return calculated mode
     */
    public String getMode() 
    {
        long mode = getModeMS();
        
        return TimeUtil.getDiff( 0, mode);
    }
    
    /**
     * This is for calculation of median
     * and initialise actual median
     * @return calculated Median
     */
    public String getMedianDuration() 
    {
        long median = getMedianMS();

        return TimeUtil.getDiff( 0, median);
    }
    
    /**
     * This is for calculation of median
     * and initialise actual median
     * @return calculated Median
     */
    public long getMedianMS() 
    {
        long median = 0;
        if(dataTime != null && dataTime.length>0)
        {
            if(dataTime.length == 1 )
            {
                median=dataTime[0];
            }
            else if(dataTime.length % 2 == 0 )
            {
                int pos = dataTime.length/2 - 1;
                median=(dataTime[pos]+dataTime[pos+1])/2;
            }
            else
            {
                median=dataTime[dataTime.length / 2];
            }
        }
        
        return median;
    }
    
   /**
    *
    * @return the  value of actual first time
    */
    public String getFirst() 
    {
        if( realFirstTime== 0) return "00";
        
        return TimeUtil.getDiff( 0, realFirstTime);
    }
 
    /**
     *
     * @return the value
     */
    public String getMinDurationMS() 
    {
        if( min == 0) return "";
        
        return "" + min;
    }
    
    /**
     *
     * @return the value
     */
    public String getMinDuration() 
    {
        if( min == 0) 
        {
            return "";
        }
        else 
        {
            return TimeUtil.getDiff( start, start + min);
        }
    }
    
    /**
     *
     * @return the value
     */
    public String getMaxDurationMS() 
    {
        if( max == 0) 
        {
            return "";
        } 
        else 
        {
            return "" + max;
        }
        
    }
    /**
     *
     * @return the value
     */
    public String getMaxDuration() 
    {
        if( max == 0) 
        {
            return "";
        }
        else 
        {
            return TimeUtil.getDiff( start, start + max);
        }
    }
    
    /**
     *
     * @return the value
     */
    public String getMeanDuration()
    {
        long mean = getMeanMS();
        
        return TimeUtil.getDiff( 0, mean);
    }
    
    /**
     */
    public long getCancelMS() 
    {
        return cancel_ms;
    }
    /**
     *
     * @param cancel_str
     * @throws Exception a serious problem
     */
    public void setCancelMS(String cancel_str) throws Exception 
    {
        cancel_ms = TimeUtil.convertDurationToMs(cancel_str);
    }
    
    /**
     * @param actualLoop The actualLoop to set.
     */
    public void setActualLoop(int actualLoop) 
    {
        this.actualLoop = actualLoop;
    }
    
    /**
     *
     * @throws Exception a serious problem
     */
    protected void executeWait() throws Exception 
    {
        Thread.sleep( wait_ms);
        
        if( stagger_ms != 0) 
        {
            long sleep_ms = (long)((double)stagger_ms * Math.random());
            
            Thread.sleep( sleep_ms);
        }
    }
    
    /**
     *
     * @throws Exception a serious problem
     */
    protected void resetServer() throws Exception 
    {
        
    }
    
    /**
     *
     * @throws Exception a serious problem
     */
    protected void preProcess() throws Exception 
    {
        
    }
    
    /**
     *
     * @param element
     * @throws Exception a serious problem
     */
   protected void setItemAttributes(Element element) throws Exception 
   {
        if( element.hasAttribute( "loop"))
        {
            try 
            {
                int tmpLoop = Integer.parseInt(element.getAttribute( "loop"));
                
                setLoop( tmpLoop);
            } 
            catch( NumberFormatException e) 
            {
                setLoopTime( element.getAttribute( "loop"));
            }
        }
        
        if( element.hasAttribute( "wait"))
        {
            setWaitTime( element.getAttribute( "wait"));
        }        
        
        if( element.hasAttribute( "stagger"))
        {
            setStaggerTime( element.getAttribute( "stagger"));
        }
        
        if( element.hasAttribute( "pause"))
        {
            setPauseTime( element.getAttribute( "pause"));
        }

        if( element.hasAttribute( BenchMarkApp.ATT_RESET ))
        {
            setResetFlag( element.getAttribute( BenchMarkApp.ATT_RESET ));
        }

        if( element.hasAttribute( BenchMarkApp.ATT_RESET ))
        {
            setResetSleep( element.getAttribute( BenchMarkApp.ATT_RESET_SLEEP ));
        }

        if( element.hasAttribute( BenchMarkApp.ATT_VALIDATE ) )
        {
            setValidatePattern( element.getAttribute( BenchMarkApp.ATT_VALIDATE ) );
        }

        if( element.hasAttribute( BenchMarkApp.ATT_IS_LIKE ) )
        {
            setMatchResult( element.getAttribute( BenchMarkApp.ATT_IS_LIKE ) );
        }

        if( element.hasAttribute( BenchMarkApp.ATT_TASK_FILTER ) )
        {
            setTaskFilter( element.getAttribute( BenchMarkApp.ATT_TASK_FILTER ) );
        }

       // if( element.hasAttribute( BenchMarkApp.ATT_VALIDATE_FORMULA ) )
       // {
       //     setValidateFormula( element.getAttribute( BenchMarkApp.ATT_VALIDATE_FORMULA ) );
       // }

        if( element.hasAttribute( BenchMarkApp.ATT_UNTIL_ISSET ) )
        {
            setUntilIsSet( element.getAttribute( BenchMarkApp.ATT_UNTIL_ISSET ) );
        }

        if( element.hasAttribute( BenchMarkApp.ATT_ON_ERROR_SET ) )
        {
            setOnErrorProperty( element.getAttribute( BenchMarkApp.ATT_ON_ERROR_SET ) );
        }

        if( element.hasAttribute( "max_tm"))
        {
            setMaxTime( element.getAttribute( "max_tm"));
        }        
        
        if( element.hasAttribute( BenchMarkApp.ATT_MIN_FIRST))  
        {
            setMinFirst( element.getAttribute(BenchMarkApp.ATT_MIN_FIRST));  
        }
        
        if( element.hasAttribute(BenchMarkApp.ATT_MAX_FIRST))     
        {
            setMaxFirst( element.getAttribute( BenchMarkApp.ATT_MAX_FIRST));  
        }
        if( element.hasAttribute(BenchMarkApp.ATT_MIN_MEAN))                  
        {
            setMinMean( element.getAttribute( BenchMarkApp.ATT_MIN_MEAN));  
        }
        
        if( element.hasAttribute(BenchMarkApp.ATT_MAX_MEAN))              
        {
            setMaxMean( element.getAttribute( BenchMarkApp.ATT_MAX_MEAN));
        }
        if( element.hasAttribute(BenchMarkApp.ATT_MIN_MODE))                
        {
            setMinMode( element.getAttribute( BenchMarkApp.ATT_MIN_MODE));  
        }
        
        if( element.hasAttribute(BenchMarkApp.ATT_MAX_MODE))            
        {
            setMaxMode( element.getAttribute(BenchMarkApp.ATT_MAX_MODE));      
        }
        if( element.hasAttribute(BenchMarkApp.ATT_MIN_MEDIAN))            
        {
            setMinMedian( element.getAttribute( BenchMarkApp.ATT_MIN_MEDIAN));  
        }
        
        if( element.hasAttribute(BenchMarkApp.ATT_MAX_MEDIAN))               
        {
            setMaxMedian( element.getAttribute( BenchMarkApp.ATT_MAX_MEDIAN));  
        }
        
        if( element.hasAttribute( BenchMarkApp.ATT_LOOP_FREQUENCY))
        {
            setLoopFrequency( element.getAttribute( BenchMarkApp.ATT_LOOP_FREQUENCY));
        }
        
        if( element.hasAttribute( "job")) 
        {
            if ("Y".equalsIgnoreCase(element.getAttribute( "job")) || "true".equalsIgnoreCase(element.getAttribute( "job")))
            {
                //LOGGER.info("Setting Job");
                setJob(true);
                //in case of this is a job if you want to cancel after some time
                if( element.hasAttribute( "jobcancel"))
                {
                    setCancelMS( element.getAttribute( "jobcancel"));
                }
            }
        }
    }

   private void setUntilIsSet( final String untilIsSet)
   {
       this.untilIsSet=untilIsSet;
   }

   private void setOnErrorProperty( final String onErrorProperty)
   {
       this.onErrorProperty=onErrorProperty;
   }

   /**
    * execute the request
    */
   public void execute()
    {
        ArrayList list = new ArrayList();
        try
        {
            
            executeWait();
            
            preProcess();
            if(serverReset==true) 
            {
                resetServer();
            }
            
            if( start == 0) start = System.currentTimeMillis();
            long itemStart = System.currentTimeMillis();

            int retriedFirstCount = 0;

            for( int itemLoop = 0; itemLoop < loop || loop_ms != 0;itemLoop++, actualLoop++) 
            {
                if( StringUtilities.isBlank(untilIsSet) == false)
                {
                    String property = System.getProperty(untilIsSet);

                    if( StringUtilities.isBlank(property) == false)
                    {
                        break;
                    }
                }
                
                if( itemLoop > 0) Thread.sleep( pause_ms);
                
                long loopStart = System.currentTimeMillis();
                
                if( loop_ms != 0 && loopStart > itemStart + loop_ms) break;
                
                process();

                long loopEnd = System.currentTimeMillis();
                
                long diff = loopEnd-loopStart;
                if(itemLoop==0) 
                {
                    if( retriedFirstCount == 0)
                    {
                        realFirstTime=diff;
                    }

                    /*
                     * if we are checking the first max time retry three times so we
                     * don't miss fire.
                     */
                    if(
                        itemLoop + 1 < loop && 
                        maxAcceptableFirst > 0 &&
                        serverReset==true &&
                        retriedFirstCount < 2 && // only try twice times to get the first time.
                        diff > maxAcceptableFirst &&
                        (
                            diff < maxAcceptableFirst + GC_MAX_TIME || // did a GC occur ?
                            diff < cancel_ms     // Long archive jobs to be re-tried.
                        )
                    ) 
                    {
                        retriedFirstCount++;
                        itemLoop--;
                        resetServer();
                        continue;
                    }

                    // if we have retried because it was too long now it's too short don't mark as FAST
                    if( retriedFirstCount > 0 && diff < minAcceptableFirst)
                    {
                        LOGGER.warn( "Too slow (" + TimeUtil.getDiff(0, realFirstTime) + ") now too fast (" + TimeUtil.getDiff(0, diff) + ").. " + getName());
                        firstTime=(maxAcceptableFirst + minAcceptableFirst)/2;                                            
                    }
                    else
                    {
                        firstTime=diff;                    
                    }
                }
                total_ms += diff;
                
                list.add( diff);
                
                if( min == 0) min = diff;
                
                if( min > diff) min = diff;
                
                if( max < diff) max = diff;
                
                if( loop_frequency_ms > 0) 
                {
                    long padding = loop_frequency_ms - diff;
                    
                    if( padding > 0) 
                    {
                        Thread.sleep( padding);
                    }
                }
            }            
        } 
        catch( Throwable e) 
        {
            LOGGER.warn( name, e);
            error = e;

            if( StringUtilities.isBlank(onErrorProperty) == false)
            {
                String msg = e.getMessage();
                if( StringUtilities.isBlank(msg))
                {
                    msg = e.toString();
                }
                
                System.setProperty(onErrorProperty, msg);
            }
        }
        
        end = System.currentTimeMillis();
        
        long temp[] = new long[ list.size()];
        for( int i = 0; i < temp.length; i++)
        {
            Long tm = (Long)list.get( i);
            
            temp[i]=tm;
        }
        
        Arrays.sort( temp); 
        
        dataTime=temp;       
    }
    
    /**
     *
     * @throws Exception a serious problem
     */
    protected abstract void process() throws Exception;
        
    /**
     *
     */
    protected long  start,
            wait_ms,
            max_acceptable_ms,
            loop_frequency_ms,
            pause_ms,
            stagger_ms,
            loop_ms,
            total_ms,
            cancel_ms=-1,
            min,
            max,
            end;
    
    /**
     *
     */
    protected long
            firstTime,
            realFirstTime,
            
            minAcceptableMean,
            maxAcceptableMean,
            minAcceptableMode,
            maxAcceptableMode,
            minAcceptableMedian,
            maxAcceptableMedian,
            minAcceptableFirst,
            maxAcceptableFirst;
           
             
    private final String[] attributes = {
        "First time",
        "Mode",
        "Mean",
        "Median",
        "Time"
    };
    
    private int     loop,
                    actualLoop;
    
    private boolean job = false;
        
    private Throwable error;
    
    private final String  name;
    
    private long[] dataTime;

    /**
     * the data
     */
    protected String data = "";

    protected String untilIsSet;
    protected String onErrorProperty;

    /**
     * should reset the server.
     */
    protected boolean  serverReset=false;
    /** time to sleep after reset */
    protected long     serverResetSleepMS;

    /**
     * the pattern to validate
     */
    protected Pattern validatePattern;

    /**
     * the value to match results
     */
    protected String matchResult;

    /**
     * The pattern to match the task name to run
     */
    protected String taskFilter;

    /**
     * this task is not included to run. this flag is used to filter out in report.
     */
    protected boolean exclude = false;

    /**
     * the validate formula.
     */
   // protected ValidateFormula validateFormula;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.performance.Item");//#LOGGER-NOPMD
}
