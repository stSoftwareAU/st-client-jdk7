/*
 *  Copyright (c) 2000-2004 ASP Converters pty ltd
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.memory.selftest;

import com.aspc.remote.application.AppCmdLine;

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.FileUtil;
import com.aspc.remote.util.misc.QueueLog;
import com.aspc.remote.util.misc.StringUtilities;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;

/**
 * Scan the GC logs.
 *
 * @author Nigel Leck
 * @since 21 Oct 2015
 */
public class MakeBM  extends AppCmdLine
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.selftest.MakeBM");//#LOGGER-NOPMD
    private File sourceFile;

    private File dir;
    @Override
    public void handleCommandLine(final CommandLine line) throws Exception {
        super.handleCommandLine(line);

        String tmp=line.getOptionValue('i');
        if( StringUtilities.isBlank(tmp))
        {
            throw new IllegalArgumentException("source file is mandatory" );
        }
        sourceFile=new File(tmp);
        if( sourceFile.exists()==false || sourceFile.isFile()==false)
        {
            throw new IOException("Source doesn't exist: " + sourceFile );
        }

        String tmpDir=line.getOptionValue('o');
        if( StringUtilities.isBlank(tmpDir))
        {
            throw new IllegalArgumentException("out dir is mandatory" );
        }
        dir=new File( tmpDir);
        if( dir.exists() == false || dir.isDirectory() == false)
        {
            throw new IOException("Directory doesn't exist: " + dir );
        }
    }

    @Override
    protected void addExtraOptions(final Options options) {
        super.addExtraOptions(options);
        Option in = new Option( "i", true, "source file");

        options.addOption(  in);
        Option out = new Option( "o", true, "output directory");

        options.addOption(  out);
    }


    @Override
    public void process() throws Exception {
        String cmds=FileUtil.readFile(sourceFile);
        String list[]=StringUtilities.splitCommands(cmds);

        List<String> a=Arrays.asList(list);
        Arrays.asList(list);
        try( FileWriter fw=new FileWriter( new File(dir, "stress.sql"))){
            fw.write("JOB EXECUTE CODE bm_block {\n");
            fw.write("  /* wait 3 hours for cache load to finish */\n");
            fw.write("  SLEEP 10800;\n");
            fw.write("};\n");
            fw.write(
                "SLEEP 5;\n"+
                "LOG WRITE 'Benchmark starting';\n"
            );
            final int MAX_CLIENTS=150;
            for( int client =1;client<=MAX_CLIENTS;client++)
            {
                ArrayList<String>cList=randomize(a);
                fw.write("JOB EXECUTE CODE c" + client + " WAIT bm_block {\n");
                for( String cmd: cList)
                {
                    fw.write(cmd.trim());
                    if( cmd.trim().endsWith(";")==false)
                    {
                        fw.write(";\n");
                    }
                    int dice=(int) (6 *Math.random()) + 1;
                    if( dice==6)
                    {
                        fw.write("SLEEP 1;\n");
                    }
                }
                fw.write("};\n");
            }
            fw.write("JOB EXECUTE CODE bm_end WAIT ");
            for( int client =1;client<=MAX_CLIENTS;client++)
            {
                if( client!=1) {
                    fw.write(",");
                }
                fw.write( "c" + client);
            }

            fw.write(
                " {\n"+
                "  LOG WRITE 'Benchmark complete';\n" +
                "};\n"
            );
        }
    }
    private ArrayList<String> randomize( final List<String> source)
    {
        ArrayList<String> tmpList=new ArrayList(source);

        ArrayList<String> targetList=new ArrayList();
        while( tmpList.isEmpty()==false)
        {
            int pos=0;
            if( tmpList.size()>1){
                pos=(int) ( tmpList.size() * Math.random());
            }
            String cmd=tmpList.remove(pos);
            targetList.add(cmd);
        }
        return targetList;
    }
    /**
     * The main for the program
     *
     * @param args The command line arguments
     * @throws Exception a serious problem.
     */
    public static void main (String[] args) throws Exception
    {
        new MakeBM( ).execute(args);
        QueueLog.flush(1000);
    }

}
