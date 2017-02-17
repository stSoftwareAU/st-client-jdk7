package com.aspc.remote.util.misc.selftest;

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.JsonPath;
import com.aspc.remote.util.misc.PathNotFoundException;
import java.util.Date;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 *  @author      Lei Gao
 *  @since       13 August 2015 
 */
public class TestJsonPath extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.selftest.TestJsonPath");//#LOGGER-NOPMD
    /**
     * Creates new VirtualDBTestUnit
     *
     * @param name the name of the test
     */
    public TestJsonPath(String name)
    {
        super( name);
    }

    /**
     * Entry point to run this test standalone
     * @param args the arguments */
    public static void main(String[] args)
    {
        Test test = suite();
//        test=TestSuite.createTest(TestJsonPath.class, "testFunctions");
        TestRunner.run(test);
    }

    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestJsonPath.class);
        return suite;
    }    

    public void testInvalidName() throws Exception
    {
        JSONObject json=new JSONObject( 
            "{\n" +
            "  \"results\": [\n" +
            "    {\n" +
            "      \"_date\": \"2016-05-23T06:23:02.594Z\",\n" +
            "      \"_action\": \"I\",\n" +
            "      \"_class\": \"Person\",\n" +
            "      \"_row_uid\": \"429496729617\",\n" +
            "      \"_global_key\": \"17@100~12@1\",\n" +
            "      \"_href\": \"/ReST/v5/class/Person/17@100~12@1^17@100\",\n" +
            "      \"_details\": \"/v1/transaction/details/100/17/429496729617\",\n" +
            "      \"_head\": \"/v1/transaction/head/100/17\",\n" +
            "      \"contactId\": \"17\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"_date\": \"2016-05-23T06:23:02.636Z\",\n" +
            "      \"_action\": \"U\",\n" +
            "      \"_class\": \"Person\",\n" +
            "      \"_row_uid\": \"429496729618\",\n" +
            "      \"_global_key\": \"18@100~12@1\",\n" +
            "      \"_href\": \"/ReST/v5/class/Person/18@100~12@1^18@100\",\n" +
            "      \"_details\": \"/v1/transaction/details/100/18/429496729618\",\n" +
            "      \"_head\": \"/v1/transaction/head/100/18\",\n" +
            "      \"contactId\": \"18\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"since\": 1463984582637\n" +
            "}"
        );
                
        String values[]={
            "$.results[1].abc",
            "$.noitem"
        };
                        
        for(String jsonPath : values)
        {
            try
            {
                String actualValue=JsonPath.read(json, jsonPath).toString();
                fail("JsonPath: " + jsonPath + " should be invalid was:" + actualValue);
            }
            catch(PathNotFoundException e)
            {
                // Expected.
            }
        }
        
    }
    public void testFunctions() throws Exception
    {
        JSONObject json=new JSONObject( 
            "{\n" +
            "  \"results\": [\n" +
            "    {\n" +
            "      \"_date\": \"2016-05-23T06:23:02.594Z\",\n" +
            "      \"_action\": \"I\",\n" +
            "      \"_class\": \"Person\",\n" +
            "      \"_row_uid\": \"429496729617\",\n" +
            "      \"_global_key\": \"17@100~12@1\",\n" +
            "      \"_href\": \"/ReST/v5/class/Person/17@100~12@1^17@100\",\n" +
            "      \"_details\": \"/v1/transaction/details/100/17/429496729617\",\n" +
            "      \"_head\": \"/v1/transaction/head/100/17\",\n" +
            "      \"contactId\": \"17\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"_date\": \"2016-05-23T06:23:02.636Z\",\n" +
            "      \"_action\": \"U\",\n" +
            "      \"_class\": \"Person\",\n" +
            "      \"_row_uid\": \"429496729618\",\n" +
            "      \"_global_key\": \"18@100~12@1\",\n" +
            "      \"_href\": \"/ReST/v5/class/Person/18@100~12@1^18@100\",\n" +
            "      \"_details\": \"/v1/transaction/details/100/18/429496729618\",\n" +
            "      \"_head\": \"/v1/transaction/head/100/18\",\n" +
            "      \"contactId\": \"18\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"since\": 1463984582637\n" +
            "}"
        );
                
        String values[][]={
            {"$.results.length()", "2"}
        };
                        
        for(String[] s : values)
        {
            String jsonPath=s[0];
            String expectedValue=s[1];
            
            try
            {
                String actualValue=JsonPath.read(json, jsonPath).toString();
                assertEquals("JsonPath: " + jsonPath + " value is incorrect", expectedValue, actualValue);
            }
            catch(Exception e)
            {
                fail("JsonPath " + jsonPath + " failed. " + e.getMessage());
            }
        }
    }
    
    public void testInvalidPath() throws Exception
    {
        String[] invalid = {
            "$.",
            "a",
            "$a.b",
            "$a[1].b",
            "$[a].b",
            "$.[1].b",
            "$.a[s].b",
            "$.a[].b",
            "$.a."
        };
        for(String s : invalid)
        {
            try
            {
                JsonPath.checkPath(s);
                fail(s + " should be invalid json path");
            }
            catch(UnsupportedOperationException e)
            {
                //expected
            }
        }
    }

    public void testValidPath() throws Exception
    {
        String[] valid = {
            "$.a",
            "$[0].a",
            "$.b[1]",
            "$",
            "$[0]"
        };
        for(String s : valid)
        {
            try
            {
                JsonPath.checkPath(s);
            }
            catch(UnsupportedOperationException e)
            {
                fail(s + " should be valid json path");
            }
        }
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testJsonPath() throws Exception
    {
        JSONObject json = new JSONObject();
        json.put("a", "aa");
        json.append("b", "bb0");
        json.append("c", "cc0");
        json.append("c", "cc1");
        JSONObject jsonD = new JSONObject();
        json.put("d", jsonD);
        jsonD.put("d1", "dd1");
        jsonD.append("d2", "dd20");
        jsonD.append("d2", "dd21");
        JSONObject jsonE = new JSONObject();
        jsonE.put("e", "ee");
        jsonD.append("d3", jsonE);
        JSONObject jsonF = new JSONObject();
        jsonF.put("f", "ff");
        jsonD.append("d3", jsonF);
        json.put("x", true);
        json.put("y", 1.2);
        json.put("z", new Date(1439210400000L));//2015.08.10 22:40:00 AEST
        
        LOGGER.info(json.toString(2));
        
        String[][] values = {
            {"{\"a\":\"aa\",\"b\":[\"bb0\"],\"c\":[\"cc0\",\"cc1\"],\"d\":{\"d1\":\"dd1\",\"d2\":[\"dd20\",\"dd21\"],\"d3\":[{\"e\":\"ee\"},{\"f\":\"ff\"}]},\"x\":true,\"y\":1.2,\"z\":\"Mon Aug 10 22:40:00 AEST 2015\"}", "$"},
            {"aa", "$.a"},
            {"[\"bb0\"]", "$.b"},
            {"bb0", "$.b[0]"},
            {"cc0", "$.c[0]"},
            {"cc1", "$.c[1]"},
            {"dd1", "$.d.d1"},
            {"dd20", "$.d.d2[0]"},
            {"dd21", "$.d.d2[1]"},
            {"ee", "$.d.d3[0].e"},
            {"ff", "$.d.d3[1].f"},
            {"{\"d1\":\"dd1\",\"d2\":[\"dd20\",\"dd21\"],\"d3\":[{\"e\":\"ee\"},{\"f\":\"ff\"}]}", "$.d"},
            {"true", "$.x"},
            {"1.2", "$.y"},
            {"Mon Aug 10 22:40:00 AEST 2015", "$.z"},
        };
        
        for(String[] s : values)
        {
            try
            {
                assertEquals("JsonPath: " + s[1] + " value is incorrect", s[0], JsonPath.read(json, s[1]).toString());
            }
            catch(Exception e)
            {
                fail("JsonPath " + s[1] + " failed. " + e.getMessage());
            }
        }
        
        String[] invalidPaths = {
            "$[0]",
            "$.b[2]",
            "$.d.d3[5]"//,
//            "$.d.d3[0].f"
        };
        for(String s : invalidPaths)
        {
            try
            {
                JsonPath.read(json, s);
                fail(s + " should get exception here");
            }
            catch(PathNotFoundException | UnsupportedOperationException e)
            {
                //expected
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testJsonArrayPath() throws Exception
    {
        JSONArray ja = new JSONArray();
        JSONObject json = new JSONObject();
        ja.put(json);
        json.put("a", "aa");
        json.append("b", "bb0");
        json.append("c", "cc0");
        json.append("c", "cc1");
        JSONObject jsonD = new JSONObject();
        json.put("d", jsonD);
        jsonD.put("d1", "dd1");
        jsonD.append("d2", "dd20");
        jsonD.append("d2", "dd21");
        JSONObject jsonE = new JSONObject();
        jsonE.put("e", "ee");
        jsonD.append("d3", jsonE);
        JSONObject jsonF = new JSONObject();
        jsonF.put("f", "ff");
        jsonD.append("d3", jsonF);
        JSONObject json2 = new JSONObject();
        json2.put("z", "zz");
        ja.put(json2);
        
        LOGGER.info(ja.toString(2));
        
        String[][] values = {
            {"[{\"a\":\"aa\",\"b\":[\"bb0\"],\"c\":[\"cc0\",\"cc1\"],\"d\":{\"d1\":\"dd1\",\"d2\":[\"dd20\",\"dd21\"],\"d3\":[{\"e\":\"ee\"},{\"f\":\"ff\"}]}},{\"z\":\"zz\"}]", "$"},
            {"{\"a\":\"aa\",\"b\":[\"bb0\"],\"c\":[\"cc0\",\"cc1\"],\"d\":{\"d1\":\"dd1\",\"d2\":[\"dd20\",\"dd21\"],\"d3\":[{\"e\":\"ee\"},{\"f\":\"ff\"}]}}", "$[0]"},
            {"{\"z\":\"zz\"}", "$[1]"},
            {"aa", "$[0].a"},
            {"[\"bb0\"]", "$[0].b"},
            {"bb0", "$[0].b[0]"},
            {"cc0", "$[0].c[0]"},
            {"cc1", "$[0].c[1]"},
            {"dd1", "$[0].d.d1"},
            {"dd20", "$[0].d.d2[0]"},
            {"dd21", "$[0].d.d2[1]"},
            {"ee", "$[0].d.d3[0].e"},
            {"ff", "$[0].d.d3[1].f"},
            {"{\"d1\":\"dd1\",\"d2\":[\"dd20\",\"dd21\"],\"d3\":[{\"e\":\"ee\"},{\"f\":\"ff\"}]}", "$[0].d"},
            {"zz", "$[1].z"}
        };
        
        for(String[] s : values)
        {
            try
            {
                assertEquals("JsonPath: " + s[1] + " value is incorrect", s[0], JsonPath.read(ja, s[1]).toString());
            }
            catch(Exception e)
            {
                fail("JsonPath " + s[1] + " failed. " + e.getMessage());
            }
        }
        
        String[] invalidPaths = {
            "$[5]",
            "$[0].d.d3[5]",
            "$[1].d",
            "$."
        };
        for(String s : invalidPaths)
        {
            try
            {
                JsonPath.read(ja, s);
                fail("should get exception here");
            }
            catch(PathNotFoundException | UnsupportedOperationException e)
            {
                //expected
            }
        }
    }
}
