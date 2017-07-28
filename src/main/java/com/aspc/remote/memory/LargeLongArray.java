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

import java.util.ArrayList;
import java.util.Arrays;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Handle very large long arrays. 
 * <br>
 * <i>THREAD MODE: SINGLE THREADED</i>
 *
 * @author Nigel Leck
 *
 * @since 21 Nov 2015
 */
@SuppressWarnings("AssertWithSideEffects")
public class LargeLongArray
{
    private static final boolean ASSERT;
        
    private long[] lastSegment;
    private long lastOffset;
//    private boolean blankValueUsed;
//    private final long blankValue; 
    private int nextAppendPosition;
    
    private final Object sanity;
    private long data[][];
    private long sizeCache=-1;
    private final Validator validator;
    private final int segmentSize;
    private final boolean inputShared;
    private boolean shallowCopied;
    private boolean fullCopied;
    private final boolean outputShared;

    /**
     * The factory to create a new Large Long Array.
     * @param data the data.
     * @return the factory
     */
    @CheckReturnValue @Nonnull
    public static Builder factory(final long data[][])
    {
        return new Builder(data);
    }
    
    /**
     * The factory to create a new Large Long Array.
     * @param rows the data.
     * @return the factory
     */
    @CheckReturnValue @Nonnull
    public static Builder factory(final long rows[])
    {
        if( rows == null)
        {
            long data[][]={};
            return new Builder(data);
        }
        else
        {
            long data[][]={rows};
            return new Builder(data);            
        }
    }
    
    public static Builder factory()
    {
        long data[][]={};
        
        return new Builder(data);        
    }
    
    private LargeLongArray(
        final long data[][], 
        final Validator validator, 
        final int segmentSize, 
        final boolean inputShared, 
        final boolean outputShared,
        final Object sanity
    )
    {
        this.data=data;

        this.validator=validator;
        this.segmentSize=segmentSize;
        this.inputShared=inputShared;
        this.outputShared=outputShared;
        this.sanity=sanity;
//        this.blankValue=blankValue;
        
        this.nextAppendPosition=-1;
    }
    
    private static class Validator{
        private final boolean checkIfZero;
        private final boolean checkIfUnique;
        private final boolean assertIfZero;
        private final boolean assertIfUnique;
        
        Validator( final boolean checkIfZero, final boolean checkIfUnique, final boolean assertIfZero, final boolean assertIfUnique)
        {
            this.checkIfZero=checkIfZero;
            this.checkIfUnique=checkIfUnique;
            this.assertIfUnique=assertIfUnique;
            this.assertIfZero=assertIfZero;
        }
        
        void validateSorted( final long sortedData[][])
        {
            long last=0;
            long scanPos=0;
            for( long rows[]:sortedData)
            {
                for( long row: rows)
                {
                    if( scanPos>0)
                    {
                        if( row == last)
                        {
                            if( assertIfUnique || checkIfUnique)
                            {
                                String msg= "Duplicate value " + row + " found at " + scanPos;
                                assert assertIfUnique==false: msg;
                                throw new IllegalArgumentException(msg);
                            }
                        }
                        
                        assert row>=last: "unsorted rows at " + scanPos +" " + last + "->" + row;                                
                    }
                    if(row == 0)
                    {
                        if( assertIfZero || checkIfZero)
                        {
                            String msg="Zero value found at " + scanPos;
                            assert assertIfZero==false: msg;
                            throw new IllegalArgumentException( msg);
                        }
                    }                    

                    scanPos++;

                    last=row;
                }
            }
        }
    }
    
    /**
     * record when a new array is created.
     */
    public static interface SanityArrayCounter
    {
        void sanityNewArray();
    }
    
    /**
     * The long array builder. 
     */
    public static final class Builder
    {
        private final long data[][];
        private boolean checkIfZero;
        private boolean checkIfUnique;
        private boolean assertIfZero;
        private boolean assertIfUnique;
        private static final int DEFAULT_SEGMENT_SIZE=16*1024*1024/64;
        private int segmentSize=DEFAULT_SEGMENT_SIZE;
        private boolean inputShared=true;
        private boolean outputShared=true;
        private Object sanity;

        private Builder( final long data[][])
        {
            this.data=data;
        }

        @Override
        public String toString() {
            return "Builder{" + ", checkIfZero=" + checkIfZero + ", checkIfUnique=" + checkIfUnique + ", assertIfZero=" + assertIfZero + ", assertIfUnique=" + assertIfUnique + ", segmentSize=" + segmentSize + ", inputShared=" + inputShared + ", outputShared=" + outputShared + ", sanity=" + sanity + '}';
        }
        
        /* 
         * Validate that the array doesn't contain zeros.
         * @param on turns this feature on/off
         * @return this
         */
        public Builder validateNonZero( final boolean on)
        {
            checkIfZero=on;
            return this;
        }

//        /* 
//         * The value to use to mark a blank value.
//        
//         * @param the value to use for blank
//         * @return this
//         */
//        public Builder setBlankValue( final long blankValue)
//        {
//            this.blankValue=blankValue;
//            return this;
//        }
        
        /**
         * Set the segment size to use when repacking.
         * @param size the segment size.
         * @return this
         */
        public Builder setSegmentSize( final @Nonnegative int size)
        {
            if( size<=0) throw new IllegalArgumentException("segment size must be greater than zero: " + size);
            segmentSize=size;
            
            return this;
        }
        
        /**
         * Adjust the segment size to be no larger than the capacity.
         * @param capacity the expected number of records.
         * @return this
         */
        public Builder setExpectedCapacity( final @Nonnegative int capacity)
        {
            if( capacity<0) throw new IllegalArgumentException("capacity must be non negative: " + capacity);
            segmentSize=Math.max(Math.min(capacity, DEFAULT_SEGMENT_SIZE), 1024);
            
            return this;
        }        
        /**
         * Set the sanity call back object.
         * @param sanity the call back object.
         * @return this
         */
        public Builder setSanity( final Object sanity)
        {
            this.sanity=sanity;
            
            return this;
        }
        
        /**
         * Validate ( on sort) that the array contains unique values. 
         * <i>ONLY if assert is enabled.</i>
         * @param on turns this feature on/off
         * @return this
         */
        public Builder validateUnique( final boolean on)
        {
            checkIfUnique=on;
            return this;
        }
        
        /* 
         * Validate that the array doesn't contain zeros.
         * <i>ONLY if assert is enabled.</i>
         * @param on turns this feature on/off
         * @return this
         */
        public Builder assertNonZero( final boolean on)
        {
            assertIfZero=on;
            return this;
        }

        /**
         * Validate ( on sort) that the array contains unique values.
         * @param on turns this feature on/off
         * @return this
         */
        public Builder assertUnique( final boolean on)
        {
            assertIfUnique=on;
            return this;
        }
        
        /**
         * Is the input data shared ( clone before changing) (DEFAULT on)
         * @param on turns this feature on/off
         * @return this
         */
        public Builder setInputShared( final boolean on)
        {
            inputShared=on;
            return this;
        }
                
        /**
         * Is the output arrays shared ( clone on pack)
         * @param on turns this feature on/off (DEFAULT on)
         * @return this
         */
        public Builder setOutputShared( final boolean on)
        {
            outputShared=on;
            return this;
        }
        
        /**
         * Create the LargeLongArray
         * @return the new Large Long Array.
         */
        public LargeLongArray build()
        {
            Validator validator =null;
            if( 
                checkIfUnique || 
                checkIfZero || (
                    ASSERT &&
                    (
                        assertIfUnique||
                        assertIfZero
                    )
                )
            )
            {
                validator=new Validator(checkIfZero, checkIfUnique, assertIfZero, assertIfUnique);
            }
            return new LargeLongArray( 
                data, 
                validator, 
                segmentSize, 
                inputShared, 
                outputShared, 
                sanity//,
//                blankValue
            );
        }
    }
    
    /**
     * Sort the passed in data. The data must be unique and non zero.
     * A new array is created ONLY if needed. 
     * @return the sorted data.
     */
    @Nonnull @CheckReturnValue
    public long[][] sort( )
    {
        trimAppendData();
        boolean alreadySorted=true;
        long last=Long.MIN_VALUE;
        
        for( long rows[]:data)
        {
            for( long row: rows)
            {
                if( row < last)
                {
                    alreadySorted=false;
                    break;
                }
                
                last=row;
            }
            if( alreadySorted==false)break;
        }
        
        long sortedData[][];
        if( alreadySorted)
        {
            makePrivate(outputShared);
            sortedData=data;
        }
        else
        {
            long raw[][]=repack(true);
            boolean repackSorted=true;
            for( int j=0;j<raw.length;j++)
            {
                long rows[]=raw[j];
                Arrays.sort(rows);
                if( j>0)
                {
                    long previousRows[]=raw[j-1];
                    long currentRows[]=rows;
                    if( previousRows[0]>currentRows[0])
                    {
                        raw[j-1]=currentRows;
                        raw[j]=previousRows;
                        
                        currentRows=previousRows;
                        previousRows=rows;
                    }
                    
                    if( repackSorted && currentRows[0]<previousRows[previousRows.length -1])
                    {
                        repackSorted=false;
                    }
                }
            }
            if( repackSorted==false)
            {
                factory(raw)
                    .setInputShared(false)
                    .build()
                    .quickSort( 0, size()-1);
            }            
           
            sortedData=raw;
        }
        
        if( validator != null)
        {
            validator.validateSorted(sortedData);
        }
    
        return sortedData;
    }
    
    public void append( final long value)
    {
        if( validator!=null)
        {
            if( value==0)
            {
                assert validator.assertIfZero==false: "zero value";
                if( validator.checkIfZero)
                {
                    throw new IllegalArgumentException("zero value");
                }
            }
            
            if( validator.assertIfUnique||validator.checkIfUnique)
            {
                long size=size();
                if( size>0)
                {
                    long previousValue=get( size-1);
                    if( previousValue>=value)
                    {
                        String msg="non unique and assending value " + previousValue + "->" + value;
                        assert validator.assertIfUnique==false:msg;
                        throw new IllegalArgumentException(msg);
                    }
                }
            }
        }
        makePrivate(false);
        long segment[];

        if( nextAppendPosition!=-1)
        {
            segment=data[data.length-1];
        }
        else
        {
            segment=makeAppendSegment();
            
            long currentSegment[]=null;
            
            if( data.length>0)
            {
                currentSegment=data[data.length-1];
            }
            
            if( currentSegment!=null && currentSegment.length< segmentSize/2)
            {
                System.arraycopy(currentSegment, 0, segment, 0, currentSegment.length);
                nextAppendPosition=currentSegment.length;
                data[data.length-1]=segment;
            }
            else
            {
                long tmpData[][]=new long[data.length + 1][];
                System.arraycopy(data, 0, tmpData, 0, data.length);
                tmpData[data.length]=segment;
                data=tmpData;
            }
        }
        
        segment[nextAppendPosition]=value;
        nextAppendPosition+=1;
        if( nextAppendPosition>=segment.length)
        {
            nextAppendPosition=-1;
        }
        lastSegment=null;
        lastOffset=-1;
        //Correct the cache the value if needed.
        if( sizeCache!=-1)
        {
            sizeCache+=1;
        }
    }
    
    private long[] makeAppendSegment()
    {
        long[] segment=new long[segmentSize];
        if( sanity instanceof SanityArrayCounter) ((SanityArrayCounter)sanity).sanityNewArray();
//        blankValueUsed=true;
//        Arrays.fill(segment, blankValue);
        nextAppendPosition=0;
        return segment;
    }
    
    /**
     * remove a value
     * @param value to remove
     * @return true if found
     */
    public boolean remove( final long value)
    {
        trimAppendData();
        makePrivate(false);
        boolean found=false;
        for( int r=0;r<data.length;r++)
        {
            long rows[]=data[r];
            
            for( int startPos=0;startPos<rows.length;startPos++)
            {
                long tmpValue=rows[startPos];
                if( tmpValue==value)
                {
                    int endPos=startPos+1;
                    for( ;endPos<rows.length;endPos++)
                    {
                        long nextValue=rows[endPos];
                        if( nextValue!=value)
                        {
                            break;
                        }
                    }
                    long replaceRows[];
                    int tmpLen=rows.length-(endPos-startPos);
                    if( tmpLen==0)
                    {
                        long emptyRows[]={};
                        replaceRows=emptyRows;
                    }
                    else
                    {
                        replaceRows=new long[tmpLen];
                        if( sanity instanceof SanityArrayCounter) ((SanityArrayCounter)sanity).sanityNewArray();
                        System.arraycopy(rows, 0, replaceRows, 0, startPos);

                        System.arraycopy(rows, endPos, replaceRows, startPos, rows.length-endPos);

                    }
                    data[r]=replaceRows;
                    found=true;
//                    if( validator!=null)
//                    {
//                        if( validator.checkIfUnique||validator.assertIfUnique)
//                        {
//                            break;
//                        }
//                    }
                    r--;
                    break;
                }
                else if( validator!=null )
                {
                    if( validator.checkIfUnique||validator.assertIfUnique)
                    {
                        if( tmpValue>value)
                        {
                            break;
                        }
                    }
                }
            }
        }
        if( found)
        {
            sizeCache=-1;
//            nextAppendPosition=-1;
        }
        return found;
    }
    
    /**
     * The size of this long array.
     * @return the size.
     */
    @CheckReturnValue @Nonnegative
    public long size()
    {
        if( sizeCache==-1)
        {
            long counter=0;
            for( long rows[]:data)
            {
                counter+=rows.length;
            }
            if( nextAppendPosition!=-1)
            {
                long[]appendSegment=data[data.length-1];
                counter-=appendSegment.length-nextAppendPosition;
            }
            sizeCache=counter;
        }
        return sizeCache;
    }
    
    /**
     * Repack this array. This will always result in NEW array being created UNLESS outputShared is marked as OFF in the builder.
     * @return the new array
     */
    @Nonnull @CheckReturnValue
    public long[][] repack()
    {
        return repack(outputShared);
    }
    
    private void trimAppendData()
    {
        if( nextAppendPosition!=-1)
        {
            int lastDataPosition=data.length-1;
            long[] rows=data[lastDataPosition];
            long tmpRows[]=new long[nextAppendPosition];
            System.arraycopy(rows, 0, tmpRows, 0, tmpRows.length);
            data[lastDataPosition]=tmpRows;
            nextAppendPosition=-1;
            lastSegment=null;
        }
                
    }
    private long[][] repack(final boolean forceCopy)
    {
        trimAppendData();
        if( forceCopy==false)
        {
            return data;
        }
        ArrayList<long[]>list=new ArrayList<>();
       
        for (long[] rows : data) {
            
            int offset=0;
            if( list.isEmpty()==false)
            {
                int repackRow=list.size()-1;
                long repackRows[]=list.get(repackRow);
                if( repackRows.length < segmentSize)
                {                    
                    int len=repackRows.length + rows.length;
                    if( len> segmentSize)
                    {
                        len=segmentSize;
                    }
                    long tmp[]=new long[len];
                    if( sanity instanceof SanityArrayCounter) ((SanityArrayCounter)sanity).sanityNewArray();
                    System.arraycopy(repackRows, 0, tmp, 0, repackRows.length);
                    offset=len-repackRows.length;
                    System.arraycopy(rows, 0, tmp, repackRows.length, offset);      
                    list.set(repackRow, tmp);
                }
            }
            
            while( offset<rows.length)
            {
                int len=rows.length-offset;
                if( len> segmentSize)
                {
                    len=segmentSize;
                }
                long tmp[]=new long[len];
                if( sanity instanceof SanityArrayCounter) ((SanityArrayCounter)sanity).sanityNewArray();
                System.arraycopy(rows, offset, tmp, 0, len);      
                list.add(tmp);
                offset+=len;
            }
        }
        
        long raw[][]=new long[list.size()][];
        list.toArray(raw);
        return raw;
    }
    
    /**
     * Recursive quick sort logic
     *
     * @param array input array
     * @param startIdx start index of the array
     * @param endIdx end index of the array
     */
    private void quickSort(final long lowIdx, final long highIdx) {
     
        if( lowIdx >= highIdx) return;
        
        long idx = partition(lowIdx, highIdx);

        // Recursively call quicksort with left part of the partitioned array
        if (lowIdx < idx - 1) {
            quickSort(lowIdx, idx - 1);
        }

        // Recursively call quick sort with right part of the partitioned array
        if (highIdx > idx) {
            quickSort(idx, highIdx);
        }
    }
    
    /**
     * Get the value at this position.
     * @param pos the position
     * @return the value. 
     */
    @CheckReturnValue
    public long get( final @Nonnegative long pos)
    {
        if( pos >= size() || pos <0)
        {
            String msg="can not get position " + pos;
//            assert false: msg;
            throw new IllegalArgumentException( msg);
        }

        if( lastSegment!=null && lastSegment.length + lastOffset >pos && pos >= lastOffset )
        {
            assert lastOffset!=-1;
            int segmentPos=(int)( pos - lastOffset);
            return lastSegment[segmentPos];
        }

        long offset=0;
        for( long rows[]: data)
        {
            if( rows.length + offset >pos)
            {
                lastSegment=rows;
                lastOffset=offset;
                int segmentPos=(int)( pos - offset);
                return rows[segmentPos];
            }
            offset+=rows.length;
        }
        String msg="can not get position " + pos;
        assert false: msg;
        throw new IllegalArgumentException( msg);
    }
    
    private void makePrivate(final boolean fullCopyRequired)
    {
        if( inputShared)
        {
            if( fullCopyRequired)
            {
                if( fullCopied==false)
                {
                    data=repack(true);
                    fullCopied=true;
                    lastOffset=-1;
                    lastSegment=null;
                }
            }
            else{
                if( shallowCopied==false && fullCopied==false)
                {
                    if( data.length ==1 && data[0].length>segmentSize)
                    {
                        data=repack(true);
                    }
                    else
                    {
                        data=data.clone();
                    }
                    lastOffset=-1;
                    lastSegment=null;
                    shallowCopied=true;
                }
            }
        }
    }
    
    public int replace( final long from, final long to)
    {
        //if( value == blankValue) throw new IllegalArgumentException( "can not set magic blank value of: " + value + "@" + pos);
        if( validator!=null)
        {
            if( to==0)
            {
                String msg="replace( "+from + ",0)-> zero value";
                assert validator.assertIfZero==false: msg;
                if( validator.checkIfZero)
                {
                    throw new IllegalArgumentException(msg);
                }
            }
        }
        
        makePrivate(true);

        int count=0;

        if( validator!=null && (validator.assertIfUnique||validator.checkIfUnique))
        {
            long lastValue=Long.MIN_VALUE;
            long offset=0;
            for( long rows[]: data)
            {
                for( int pos=0;pos<rows.length;pos++)
                {
                    
                    long value=rows[pos];
                    long changedValue;
                    if( value==from)
                    {
                        changedValue=to;
                        count++;
                    }
                    else
                    {
                        changedValue=value;
                    }
                    
                    if( lastValue>=changedValue)
                    {
                        String msg="replace( "+from + "," + to+ ") @" + offset + " non unique and assending value " + offset + "," + changedValue;
                        assert validator.assertIfUnique==false:msg;
                        throw new IllegalArgumentException(msg);
                    }
                    rows[pos]=changedValue;
                    offset++;
                    lastValue=changedValue;
                }
            }
        }
        else
        {
            for( long rows[]: data)
            {
                for( int pos=0;pos<rows.length;pos++)
                {
                    long value=rows[pos];
                    if( value==from)
                    {
                        count++;
                        rows[pos]=to;
                    }
                }
            }
        }

        return count;
    }
    
    /**
     * Set the value at this position. 
     * @param pos the position
     * @param value the value to set
     * @return the previous value.
     */
    public long set( final @Nonnegative long pos, final long value)
    {
        if( pos<0)throw new IllegalArgumentException("position must be non negative: " + pos);
        //if( value == blankValue) throw new IllegalArgumentException( "can not set magic blank value of: " + value + "@" + pos);
        if( validator!=null)
        {
            if( value==0)
            {
                String msg="set( "+pos + ")-> zero value";
                assert validator.assertIfZero==false: msg;
                if( validator.checkIfZero)
                {
                    throw new IllegalArgumentException(msg);
                }
            }
            
            if( validator.assertIfUnique||validator.checkIfUnique)
            {
                
                if( pos>0)
                {
                    long previousValue=get( pos-1);
                    if( previousValue>=value)
                    {
                        String msg="set( "+pos + ") non unique and assending value " + previousValue + "," + value;
                        assert validator.assertIfUnique==false:msg;
                        throw new IllegalArgumentException(msg);
                    }
                }
                if( pos < size()-1)
                {
                    long nextValue=get( pos+1);
                    if( nextValue<=value)
                    {
                        String msg="set( "+pos + ")-> non unique and decending value " + value + "," + nextValue;
                        assert validator.assertIfUnique==false:msg;
                        throw new IllegalArgumentException(msg);
                    }
                }                
            }
        }
        
        makePrivate(true);
        if( lastSegment!=null && lastSegment.length + lastOffset >pos && pos >= lastOffset )
        {
            int segmentPos=(int)( pos - lastOffset);
            long previousValue= lastSegment[segmentPos];
            lastSegment[segmentPos]=value;
            return previousValue;
        }
        
        long offset=0;
        for( long rows[]: data)
        {
            if( rows.length + offset >pos)
            {
                lastSegment=rows;
                lastOffset=offset;
                int segmentPos=(int)( pos - offset);
                long previousValue=rows[segmentPos];
                rows[segmentPos]=value;
                return previousValue;
            }
            offset+=rows.length;
        }

        String msg="can not set position " + pos;
//        assert false: msg;
        throw new IllegalArgumentException( msg);
    }
    
    /**
     * Divides array from pivot, left side contains elements less than
     * Pivot while right side contains elements greater than pivot.
     *
     * @param tmpData array to partitioned
     * @param lowIdx lower bound of the array
     * @param highIdx upper bound of the array
     * @return the partition index
     */
    private long partition(long lowIdx, long highIdx) {
        long middleIdx=lowIdx + ( highIdx-lowIdx)/2;

        long pivot = get( middleIdx); // taking first element as pivot

        while (lowIdx <= highIdx) {
            //searching number which is greater than pivot, bottom up
            long leftValue;
            while (true) {
                leftValue=get(lowIdx);
                if( leftValue>= pivot)break;
                lowIdx++;
            }
            //searching number which is less than pivot, top down
            long rightValue;
            while ( true) {
                rightValue=get(highIdx);
                if( rightValue<=pivot) break;
                highIdx--;
            }

            // swap the values
            if (lowIdx <= highIdx) {
                
                set( highIdx, leftValue);
                set( lowIdx, rightValue);

                //increment left index and decrement right index
                lowIdx++;
                highIdx--;
            }
        }
        return lowIdx;
    }
    
    static
    {
        boolean flag=false;
        assert flag=true;
        ASSERT=flag;
    }
}
