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

import com.aspc.remote.application.AppCmdLine;
import com.aspc.remote.html.HTMLDiv;
import com.aspc.remote.html.HTMLPage;
import com.aspc.remote.html.HTMLTable;
import com.aspc.remote.html.HTMLText;
import com.aspc.remote.html.scripts.ScriptLink;

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.NumUtil;
import com.aspc.remote.util.misc.QueueLog;
import com.aspc.remote.util.misc.StringUtilities;
import com.google.gwt.user.client.Random;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Scan the GC logs.
 *
 * @author Nigel Leck
 * @since 21 Oct 2015
 */
public class ScanGCLog  extends AppCmdLine
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.selftest.ScanGCLog");//#LOGGER-NOPMD
    private String inFileList;

    private long beginTime;
    private long endTime;
    private long timeStep;
    private File dir;
    @Override
    public void handleCommandLine(final CommandLine line) throws Exception {
        super.handleCommandLine(line);

        inFileList=line.getOptionValue('i');
        if( StringUtilities.isBlank(inFileList))
        {
            throw new IllegalArgumentException("file list is mandatory" );
        }
        String tmpOutFile=line.getOptionValue('o');
        if( StringUtilities.isBlank(tmpOutFile))
        {
            throw new IllegalArgumentException("out dir is mandatory" );
        }
        dir=new File( tmpOutFile);
        if( dir.exists() == false || dir.isDirectory() == false)
        {
            throw new IOException("Directory doesn't exist: " + dir );
        }
        
        beginTime=Long.parseLong(line.getOptionValue('b', "0"));
        endTime=Long.parseLong(line.getOptionValue('e', Integer.toString(3 * 60 * 60)));
        timeStep=Long.parseLong(line.getOptionValue('s', Integer.toString(5 * 60)));

    }

    @Override
    protected void addExtraOptions(final Options options) {
        super.addExtraOptions(options);
        Option in = new Option( "i", true, "in file");

        options.addOption(  in);
        Option out = new Option( "o", true, "output directory");

        options.addOption(  out);
        Option begin = new Option( "b", true, "begin time");

        options.addOption(  begin);
        
        Option end = new Option( "e", true, "end time");

        options.addOption(  end);
        Option step = new Option( "s", true, "step time");

        options.addOption(  step);
    }


    @Override
    public void process() throws Exception {
//        HashMap<String, String> cells=new HashMap();
        ArrayList<HashMap<String, Cell>>columns=new ArrayList();
        ArrayList<String>titles=new ArrayList();
        for( String fn: inFileList.split(",") )
        {
            StringBuilder title=new StringBuilder();
            HashMap<String, Cell> rows=loadColumn( fn, title);
            columns.add(rows);
            titles.add(title.toString());
        }
        File outFile=new File( dir, "gc.csv");
        try
        (FileWriter fw = new FileWriter( outFile)) {
            for (String title : titles) {
                fw.append("\tAccumulate");
            }
            for (String title : titles) {
                fw.append("\tMax");
            }
            fw.append("\n");
            for( String title: titles)
            {
                fw.append("\t");
                fw.append(title);
            }
            for( String title: titles)
            {
                fw.append("\t");
                fw.append(title);
            }
            fw.append("\n");
            DecimalFormat df=new DecimalFormat( "0.00");
            long time= 0;
            while(true)
            {
                if( time>=beginTime){
                if( time>endTime) break;

                fw.append(Long.toString(time));
                for( int c=0;c <columns.size();c++)
                {
                    fw.append("\t");
                    HashMap<String, Cell>cells=columns.get( c);
                    Cell cell=find(cells, time);
                    fw.append(df.format(cell.accumulate));
                }
                for( int c=0;c <columns.size();c++)
                {
                    fw.append("\t");
                    HashMap<String, Cell>cells=columns.get( c);
                    Cell cell=find(cells, time);
                    fw.append(df.format(cell.max));
                }
                fw.append("\n");
                }
                time+=timeStep;
            }
        }
        generateHTML(titles, columns);
    }
    private ArrayList<String>makeColors( final ArrayList<HashMap<String, Cell>>columns)
    {
        DecimalFormat df=new DecimalFormat("00000000.000");
        ArrayList<String>orderedList=new ArrayList<>();
        ArrayList<String>colors=new ArrayList<>();
        for( int c=0;c <columns.size();c++)
        {
            double total=0;
            HashMap<String, Cell>cells=columns.get( c);
            for(long time=beginTime;time<=endTime;time+=timeStep)
            {
                if( time>endTime) break;
                Cell cell=find(cells, time);
                total+=cell.accumulate;
            }
            String tmp=df.format(total) + "#" + c;
            orderedList.add(tmp);
        }        
        
        Collections.sort(orderedList);
        
        String tmp=orderedList.remove(0);
        addColor( tmp, Color.BLUE, colors);
        
        if( orderedList.isEmpty()==false)
        {
            tmp=orderedList.remove(orderedList.size()-1);
            addColor( tmp, Color.RED, colors);
        }
        if( orderedList.isEmpty()==false)
        {
            tmp=orderedList.remove(0);
            addColor( tmp, Color.GREEN, colors);
        }
        if( orderedList.isEmpty()==false)
        {
            tmp=orderedList.remove(orderedList.size()-1);
            addColor( tmp, Color.ORANGE, colors);
        }
        if( orderedList.isEmpty()==false)
        {
            tmp=orderedList.remove(orderedList.size()-1);
            addColor( tmp, Color.YELLOW, colors);
        }

        while( orderedList.isEmpty()==false)
        {
            tmp=orderedList.remove(0);
            addColor( tmp, new Color( Random.nextInt()), colors);
        }
        return colors;
    }
    
    private void addColor( String key, Color color, ArrayList<String>colors)
    {
        int pos = Integer.parseInt(key.substring(key.indexOf("#") + 1));
        
        for( int s=colors.size()-1; pos > s;s++)
        {
            colors.add("");
        }
        
        int c;
        c = color.getRGB() & 0xffffff;

        String  t;

        t = "000000" + Integer.toHexString(c);

        t = "#" + t.substring(t.length() - 6);
        colors.set(pos, t);
    }
    private void generateHTML(final ArrayList<String>titles, final ArrayList<HashMap<String, Cell>>columns) throws IOException
    {
        HTMLPage p=new HTMLPage( 2);
        p.addJavaScriptLink("https://www.google.com/jsapi", ScriptLink.LoadType.BLOCKING);

        ArrayList<String> colorList=makeColors( columns);
        double maxMS=0;
        for(long time=beginTime;time<=endTime;time+=timeStep)
        {
            if( time>endTime) break;
            for( int c=0;c <columns.size();c++)
            {
                HashMap<String, Cell>cells=columns.get( c);
                Cell cell=find(cells, time);
                if( cell.accumulate>maxMS)
                {
                    maxMS=cell.accumulate;
                }
            }
        }

        int maxSeconds=0;
        while( maxSeconds< maxMS)
        {
            maxSeconds+=5;
        }
        
        JSONArray cols=new JSONArray();
        cols.put("Time");

        HTMLTable args=new HTMLTable();

        args.setHeaderAsFirstRow(true);
        args.setHighlightOddRow(true);
        int col=0;
        for( String fn: inFileList.split(",") )
        {
            int pos=fn.lastIndexOf("/");
            int pos2=fn.lastIndexOf(".");
            String alais=fn.substring(pos + 1, pos2);
            cols.put( "Max: " + alais);
            cols.put( alais);
            HTMLText ht=new HTMLText( alais);
            Color color=new Color( Integer.parseInt(colorList.get(col).substring(1),16));
            ht.setColor(color);
            args.setCell(ht, 0, col);

            String title=titles.get(col);
            int row=0;
            for( String arg:title.split(" "))
            {
                ht=new HTMLText( arg);
                ht.setColor(color);
                args.setCell(ht, row + 1, col);
                row++;
            }
            col++;
        }
        JSONArray data=new JSONArray();
        data.put(cols);
        JSONObject vAxes=new JSONObject();      
        for( int c=0;c <columns.size();c++)
        {
            JSONObject v=new JSONObject();
            JSONObject vSize=new JSONObject();
            vSize.put("max", maxSeconds);
            vSize.put("min", 0);
            v.put("viewWindow", vSize);
            if( c!=0)
            {
                JSONObject fontSize=new JSONObject();

                fontSize.put("fontSize", 0);
                v.put("textStyle", fontSize);
                 
            }
            vAxes.put(Integer.toString(c), v);
        }
        JSONObject series=new JSONObject();
        int targetIndex=0;
        int seriesIndex=0;
        for(long time=beginTime;time<=endTime;time+=timeStep)
        {
            if( time>endTime) break;
            String tmp=Long.toString(time);
            JSONArray values=new JSONArray();
            values.put(tmp);
            for( int c=0;c <columns.size();c++)
            {
                HashMap<String, Cell>cells=columns.get( c);
                Cell cell=find(cells, time);
                JSONObject target=new JSONObject();
                target.put("targetAxisIndex", targetIndex);                
                series.put(Integer.toString(seriesIndex), target);
                values.put(cell.max);
                seriesIndex++;
//                JSONObject target2=new JSONObject();
                //series.put(Integer.toString(seriesIndex), target);
                values.put(cell.accumulate-cell.max);
                //seriesIndex++;
            }

            targetIndex++;
            data.put(values);
//            if( targetIndex>3) break;
//            time+=TIME_STEP;
        }
        
//        "vAxes": {
//1: {viewWindow: {max: 220, min: 0}, textStyle: {fontSize: 0}},
//2: {viewWindow: {max: 220, min: 0}, textStyle: {fontSize: 0}},
//3: {viewWindow: {max: 220, min: 0}, textStyle: {fontSize: 0}}
//}
        JSONObject legend=new JSONObject();
//        legend.put("position", "bottom");
        legend.put("position", "none");
//        legend.put("maxLines", 1);

        JSONObject options=new JSONObject();
        options.append("title", "GC stop the world");
        options.put("legend", legend);
        JSONArray colors=new JSONArray();
        for( String c: colorList)
        {
            colors.put(c);
        }
        
        options.put("colors", colors);
        options.put("isStacked", true);
//        "isStacked": true,
                
        options.put("series", series);
        options.put("vAxes", vAxes);

        p.addJavaScript(
            "      // Load the Visualization API and the piechart package.\n" +
            "      google.load('visualization', '1.1', {'packages':['corechart', 'bar']});\n" +
            "\n" +
            "      // Set a callback to run when the Google Visualization API is loaded.\n" +
            "      google.setOnLoadCallback(drawChart);\n" +
            "\n" +
            "      // Callback that creates and populates a data table,\n" +
            "      // instantiates the pie chart, passes in the data and\n" +
            "      // draws it.\n" +
            "      function drawChart() {\n" +
            "\n" +
            "        // Set chart options\n" +
            "        var options = " + options.toString(2) + ";\n" +
//                    "'legend': {'position':'bottom'}," +
//            "                       'width':400,\n" +
//            "                       'height':300};\n" +
//            "};\n" +
                    ""+
            "        // Create the data table.\n" +
            "        var data = new google.visualization.arrayToDataTable(" + data.toString(2) +");\n" +
//            "        data.addColumn('number', 'Time');\n" +
//            "        data.addColumn('number', 'Slices');\n" +
//            "        data.addRows([\n" +
//            "          ['Mushrooms', 3],\n" +
//            "          ['Onions', 1],\n" +
//            "          ['Olives', 1],\n" +
//            "          ['Zucchini', 1],\n" +
//            "          ['Pepperoni', 2]\n" +
//            "        ]);\n" +
            "\n" +

            "        // Instantiate and draw our chart, passing in some options.\n" +
//            "        var chart = new google.visualization.ColumnChart(document.getElementById('chart_div'));\n" +
//            "        chart.draw(data, options);\n"+
            "    var chart = new google.charts.Bar(document.getElementById('chart_div'));\n" +
            "    chart.draw(data, google.charts.Bar.convertOptions(options));"
                    + "\n};"
        );
        HTMLDiv div=new HTMLDiv( "chart_div", true);
        p.addComponent(div);

        p.addText("\n");
        HTMLDiv center=new HTMLDiv( "center_div", true);
        center.addComponent(args);
        center.setStyleProperty("margin", "0 auto");
        center.setStyleProperty("width", "600px");
        p.addComponent(center);
        div.setHeight("700px");
        File outFile=new File( dir, "gc.html");
        try
        (FileWriter fw = new FileWriter( outFile)) {
            String html=p.generate();
            fw.write(html);
        }


    }

    private String correctArg( final String arg)
    {
        int pos=arg.indexOf("=");
        if( pos == -1) return arg;

        String name=arg.substring(0, pos);
        String size=arg.substring(pos + 1);
        if( size.matches("[0-9]+"))
        {
            if( name.contains("Size") && name.contains("StringTableSize")==false)
            {
                long byteSize=Long.parseLong(size);
                return name + "=" + NumUtil.convertMemoryToHumanReadable(byteSize).replace(",", "");
            }
        }

        return arg;
    }

    /**
     * : -XX:-BytecodeVerificationLocal
     * -XX:-BytecodeVerificationRemote -XX:+CMSClassUnloadingEnabled -XX:CMSInitiatingOccupancyFraction=60 -XX:+CMSParallelRemarkEnabled
     * -XX:+CMSPrecleanRefLists1 -XX:+CMSPrecleanRefLists2 -XX:+CMSScavengeBeforeRemark -
     * XX:-ClassUnloading -XX:ConcGCThreads=8 -XX:+ExplicitGCInvokesConcurrent -XX:InitialHeapSize=34359738368
     * -XX:LoopUnrollLimit=0 -XX:+ManagementServer -XX:MarkStackSize=4194304 -XX:MarkStackSizeMax=541065216 -XX:MaxHeapSize=34359738368 -XX:MaxNewSize=536870912
     * -XX:MaxPermSize=268435456 -XX:NewSize=536870912 -XX:OldPLABSize=16 -XX:-OmitStackTraceInFastThrow -XX:ParGCDesiredObjsFromOverflowList=20480
     * -XX:ParallelGCThreads=8 -XX:PermSize=268435456 -XX:PrintCMSStatistics=2  -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDetails -XX:+PrintGCTimeStamps
     * -XX:+PrintHeapAtGC -XX:StackShadowPages=20 -XX:SurvivorRatio=2 -XX:ThreadStackSize=8192 -XX:+UseCMSInitiatingOccupancyOnly -XX:+UseConcMarkSweepGC
     * -XX:+UseParNewGC : -XX:AutoBoxCacheMax=40000 -XX:-BytecodeVerificationLocal -XX:-BytecodeVerificationRemote -XX:+ExplicitGCInvokesConcurrent
     * -XX:G1ReservePercent=20 -XX:InitialHeapSize=34359738368 -XX:+ManagementServer -XX:MaxGCPauseMillis=2000 -XX:MaxHeapSize=34359738368
     * -XX:MaxPermSize=268435456 -XX:-OmitStackTraceInFastThrow -XX:ParallelGCThreads=18 -XX:+ParallelRefProcEnabled -XX:PermSize=268435456 -XX:+PrintAdaptiveSizePolicy
     * -XX:+PrintGC -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -XX:ThreadStackSize=8192 -XX:+UseG1GC
     * @param line
     * @return
     */
    private String makeTitle(final String line)
    {
        int pos =line.indexOf(":");
        String tmpLine=line.substring(pos + 1);

        ArrayList<String>args=new ArrayList();
        String removeList[]={
            "-XX:\\-BytecodeVerificationLocal",
            "-XX:\\-BytecodeVerificationRemote",
            "-XX:\\+CMSClassUnloadingEnabled",
            "-XX:-OmitStackTraceInFastThrow",
            "-XX:\\+Print[a-zA-Z]+",
            "-XX:PrintCMSStatistics=2",
//            "-XX:\\+PrintGCApplicationStoppedTime",
//            "-XX:\\+PrintGCDetails",
//            "-XX:\\+PrintGCTimeStamps",
            "-XX:\\+PrintHeapAtGC",
            "-XX:\\+ManagementServer",
            "-XX:\\+PrintAdaptiveSizePolicy",
            "-XX:ThreadStackSize=[0-9]+",
            "-XX:InitialHeapSize=[0-9]+",
            "-XX:\\+ExplicitGCInvokesConcurrent"
        };

        for( String arg: tmpLine.split(" "))
        {
            boolean found=false;
            String tmpArg=arg.trim();
            for( String remove: removeList)
            {
                if( tmpArg.matches(remove))
                {
                    found=true;
                    break;
                }
            }

            if( found==false)
            {
                args.add(correctArg(tmpArg));
            }
        }
        ArrayList<String>first=new ArrayList();
        ArrayList<String>middle=new ArrayList();
        ArrayList<String>last=new ArrayList();
        for( String arg: args)
        {
            if( arg.contains("UseG1GC")||arg.contains("UseConcMarkSweepGC"))
            {
                first.add(arg);
            }
            else if( arg.contains("MaxHeapSize"))
            {
                first.add(arg);
            }
            else
            {
                last.add(arg);
            }
        }
        args.clear();
        Collections.sort(first);
        Collections.sort(middle);
        Collections.sort(last);
        args.addAll(first);
        args.addAll(middle);
        args.addAll(last);

        StringBuilder sb=new StringBuilder();
        args.stream().forEach((arg) -> {
            if( sb.length()!=0) sb.append( " ");
            sb.append(arg);
        });

        return sb.toString().replace("  ",  " ").trim();
    }

    private HashMap<String, Cell> loadColumn( final String fn, StringBuilder title) throws FileNotFoundException, IOException
    {
        HashMap<String, Cell>cells=new HashMap();
//        ArrayList<String> rows=new ArrayList();
        BufferedReader r=new BufferedReader( new FileReader( fn));

        while( true)
        {
            String line = r.readLine();
            if( line == null) break;

            if( line.startsWith("CommandLine flags:"))
            {
                title.append(makeTitle(line));
            }
            else if( line.contains("application threads were stopped:"))
            {
                int pos = line.indexOf(":");
                double t;
                try{
                    t=Double.parseDouble(line.substring(0, pos));
                }
                catch( NumberFormatException nf)
                {
                    LOGGER.info(line, nf);
                    continue;
                }
                Cell cell=find( cells, (long)t);

                if( t> endTime) break;

                pos=line.indexOf("stopped:");
                int pos2=line.indexOf( " seconds");
                if( pos2==-1)break;
                try
                {
                    double blockage=Double.parseDouble(line.substring(pos + 9, pos2).trim());
                    cell.accumulate+=blockage;
                    if( blockage>cell.max)
                    {
                        cell.max=blockage;
                    }
                }
                catch( NumberFormatException nf)
                {
                    LOGGER.info( line, nf);
                }
            }
        }

        return cells;
    }

    private Cell find( HashMap<String, Cell>cells, long time)
    {
        String key=Long.toHexString(time/timeStep);
        Cell cell=cells.get(key);
        if( cell == null)
        {
            cell=new Cell();
            cells.put(key, cell);
        }

        return cell;
    }
    /**
     * The main for the program
     *
     * @param args The command line arguments
     * @throws Exception a serious problem.
     */
    public static void main (String[] args) throws Exception
    {
        new ScanGCLog( ).execute(args);
        QueueLog.flush(1000);
    }

    private class Cell
    {
        double accumulate;
        double max;
    }
}
