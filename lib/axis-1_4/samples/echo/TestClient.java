/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package samples.echo ;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Stub;
import org.apache.axis.types.HexBinary;
import org.apache.axis.types.NegativeInteger;
import org.apache.axis.types.NonNegativeInteger;
import org.apache.axis.types.NonPositiveInteger;
import org.apache.axis.types.NormalizedString;
import org.apache.axis.types.PositiveInteger;
import org.apache.axis.types.Token;
import org.apache.axis.types.UnsignedByte;
import org.apache.axis.types.UnsignedInt;
import org.apache.axis.types.UnsignedLong;
import org.apache.axis.types.UnsignedShort;
import org.apache.axis.utils.JavaUtils;
import org.apache.axis.utils.Options;

import javax.xml.rpc.holders.FloatHolder;
import javax.xml.rpc.holders.IntHolder;
import javax.xml.rpc.holders.StringHolder;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;


/**
 * Test Client for the echo interop service.  See the main entrypoint
 * for more details on usage.
 *
 * @author Sam Ruby <rubys@us.ibm.com>
 * Modified to use WSDL2Java generated stubs and artifacts by
 * @author Rich Scheuerle <scheu@us.ibm.com>
 */
public abstract class TestClient {

    private static InteropTestPortType binding = null;

    /**
     * When testMode is true, we throw exceptions up to the caller
     * instead of recording them and continuing.
     */
    private boolean testMode = false;

    public TestClient() {
    }

    /**
     * Constructor which sets testMode
     */
    public TestClient(boolean testMode) {
        this.testMode = testMode;
    }

    /**
     * Determine if two objects are equal.  Handles nulls and recursively
     * verifies arrays are equal.  Accepts dates within a tolerance of
     * 999 milliseconds.
     */
    protected boolean equals(Object obj1, Object obj2) {
       if (obj1 == null || obj2 == null) return (obj1 == obj2);
       if (obj1.equals(obj2)) return true;

       // For comparison purposes, get the array of bytes representing
        // the HexBinary object.
       if (obj1 instanceof HexBinary) {
           obj1 = ((HexBinary) obj1).getBytes();
       }
       if (obj2 instanceof HexBinary) {
           obj2 = ((HexBinary) obj2).getBytes();
       }

       if (obj1 instanceof Calendar && obj2 instanceof Calendar) {
           if (Math.abs(((Calendar)obj1).getTime().getTime() - ((Calendar)obj2).getTime().getTime()) < 1000) {
               return true;
           }
       }

       if ((obj1 instanceof Map) && (obj2 instanceof Map)) {
           Map map1 = (Map)obj1;
           Map map2 = (Map)obj2;
           Set keys1 = map1.keySet();
           Set keys2 = map2.keySet();
           if (!(keys1.equals(keys2))) return false;

           // Check map1 is a subset of map2.
           Iterator i = keys1.iterator();
           while (i.hasNext()) {
               Object key = i.next();
               if (!equals(map1.get(key), map2.get(key)))
                   return false;
           }

           // Check map2 is a subset of map1.
           Iterator j = keys2.iterator();
           while (j.hasNext()) {
               Object key = j.next();
               if (!equals(map1.get(key), map2.get(key)))
                   return false;
           }
           return true;
       }

       if (obj1 instanceof List)
         obj1 = JavaUtils.convert(obj1, Object[].class);
       if (obj2 instanceof List)
         obj2 = JavaUtils.convert(obj2, Object[].class);

       if (!obj2.getClass().isArray()) return false;
       if (!obj1.getClass().isArray()) return false;
       if (Array.getLength(obj1) != Array.getLength(obj2)) return false;
       for (int i=0; i<Array.getLength(obj1); i++)
           if (!equals(Array.get(obj1,i),Array.get(obj2,i))) return false;
       return true;
    }

    /**
     * Set up the call object.
     */
    public void setURL(String url)
        throws AxisFault
    {
        try {
            binding = new InteropTestServiceLocator().
                getecho(new java.net.URL(url));

            // safety first
            ((InteropTestSoapBindingStub)binding).setTimeout(60000);
            ((InteropTestSoapBindingStub)binding).setMaintainSession(true);
        } catch (Exception exp) {
            throw AxisFault.makeFault(exp);
        }
    }

    void setUser(String user) {
        ((Stub)binding).setUsername(user);
    }

    void setPassword(String password) {
        ((Stub)binding).setPassword(password);        
    }

    /**
     * Execute all tests.
     */
    public void executeAll() throws Exception {
        execute2A();
        execute2B();
        executeAxisXSD();
    }

    /**
     * Test custom mapping of xsd types not standardized:  xsd:token and
     * xsd:normalizedString.
     */
    public void executeAxisXSD() throws Exception {
        Object output = null;

        // Test xsd:token
        Token tInput = new Token("abccdefg");
        try {
            output = binding.echoToken(tInput);
            verify("echoToken", tInput, output);
        } catch (Exception e) {
            if (!testMode) {
                verify("echoToken", tInput, e);
            } else {
                throw e;
            }
        }

        // Test xsd:normalizedString
        NormalizedString nsInput = new NormalizedString("abccdefg");
        try {
            output = binding.echoNormalizedString(nsInput);
            verify("echoNormalizedString", nsInput, output);
        } catch (Exception e) {
            if (!testMode) {
                verify("echoNormalizedString", nsInput, e);
            } else {
                throw e;
            }
        }

        // Test xsd:unsignedLong
        UnsignedLong ulInput = new UnsignedLong(100);
        try {
            output = binding.echoUnsignedLong(ulInput);
            verify("echoUnsignedLong", ulInput, output);
        } catch (Exception e) {
            if (!testMode) {
                verify("echoUnsignedLong", ulInput, e);
            } else {
                throw e;
            }
        }

        // Test xsd:unsignedInt
        UnsignedInt uiInput = new UnsignedInt(101);
        try {
            output = binding.echoUnsignedInt(uiInput);
            verify("echoUnsignedInt", uiInput, output);
        } catch (Exception e) {
            if (!testMode) {
                verify("echoUnsignedInt", uiInput, e);
            } else {
                throw e;
            }
        }

        // Test xsd:unsignedShort
        UnsignedShort usInput = new UnsignedShort(102);
        try {
            output = binding.echoUnsignedShort(usInput);
            verify("echoUnsignedShort", usInput, output);
        } catch (Exception e) {
            if (!testMode) {
                verify("echoUnsignedShort", usInput, e);
            } else {
                throw e;
            }
        }

        // Test xsd:unsignedByte
        UnsignedByte ubInput = new UnsignedByte(103);
        try {
            output = binding.echoUnsignedByte(ubInput);
            verify("echoUnsignedByte", ubInput, output);
        } catch (Exception e) {
            if (!testMode) {
                verify("echoUnsignedByte", ubInput, e);
            } else {
                throw e;
            }
        }

        // Test xsd:nonNegativeInteger
        NonNegativeInteger nniInput = new NonNegativeInteger("12345678901234567890");
        try {
            output = binding.echoNonNegativeInteger(nniInput);
            verify("echoNonNegativeInteger", nniInput, output);
        } catch (Exception e) {
            if (!testMode) {
                verify("echoNonNegativeInteger", nniInput, e);
            } else {
                throw e;
            }
        }

        // Test xsd:positiveInteger
        PositiveInteger piInput = new PositiveInteger("12345678901234567890");
        try {
            output = binding.echoPositiveInteger(piInput);
            verify("echoPositiveInteger", piInput, output);
        } catch (Exception e) {
            if (!testMode) {
                verify("echoPositiveInteger", piInput, e);
            } else {
                throw e;
            }
        }

        // Test xsd:nonPositiveInteger
        NonPositiveInteger npiInput = new NonPositiveInteger("-12345678901234567890");
        try {
            output = binding.echoNonPositiveInteger(npiInput);
            verify("echoNonPositiveInteger", npiInput, output);
        } catch (Exception e) {
            if (!testMode) {
                verify("echoNonPositiveInteger", npiInput, e);
            } else {
                throw e;
            }
        }
        
        // Test xsd:negativeInteger
        NegativeInteger niInput = new NegativeInteger("-12345678901234567890");
        try {
            output = binding.echoNegativeInteger(niInput);
            verify("echoNegativeInteger", niInput, output);
        } catch (Exception e) {
            if (!testMode) {
                verify("echoNegativeInteger", niInput, e);
            } else {
                throw e;
            }
        }

    }

    /**
     * Execute the 2A tests
     */
    public void execute2A() throws Exception {
        // execute the tests
        Object output = null;

        {
            String input = "abccdefg";
            try {
                output = binding.echoString(input);
                verify("echoString", input, output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoString", input, e);
                } else {
                    throw e;
                }
            }
        }

        {
            String[] input = new String[] {"abc", "def"};
            try {
                output = binding.echoStringArray(input);
                verify("echoStringArray", input, output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoStringArray", input, e);
                } else {
                    throw e;
                }
            }
        }

        {
            Integer input = new Integer(42);
            try {
                output = new Integer( binding.echoInteger(input.intValue()));
                verify("echoInteger", input, output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoInteger", input, e);
                } else {
                    throw e;
                }
            }
        }

        {
            int[] input = new int[] {42};
            try {
                output = binding.echoIntegerArray(input);
                verify("echoIntegerArray", input, output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoIntegerArray", input, e);
                } else {
                    throw e;
                }
            }
        }

        {
            Float input = new Float(3.7F);
            try {
                output = new Float(binding.echoFloat(input.floatValue()));
                verify("echoFloat", input, output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoFloat", input, e);
                } else {
                    throw e;
                }
            }
        }

        {
            float[] input = new float[] {3.7F, 7F};
            try {
                output = binding.echoFloatArray(input);
                verify("echoFloatArray", input, output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoFloatArray", input, e);
                } else {
                    throw e;
                }
            }
        }

        {
            SOAPStruct input = new SOAPStruct();
            input.setVarInt(5);
            input.setVarString("Hello");
            input.setVarFloat(103F);
            try {
                output = binding.echoStruct(input);
                verify("echoStruct", input, output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoStruct", input, e);
                } else {
                    throw e;
                }
            }
        }

        {
            SOAPStruct[] input = new SOAPStruct[] {
                new SOAPStruct(),
                new SOAPStruct(),
                new SOAPStruct()};
            input[0].setVarInt(1);
            input[0].setVarString("one");
            input[0].setVarFloat(1.1F);
            input[1].setVarInt(2);
            input[1].setVarString("two");
            input[1].setVarFloat(2.2F);
            input[2].setVarInt(3);
            input[2].setVarString("three");
            input[2].setVarFloat(3.3F);

            try {
                output = binding.echoStructArray(input);
                verify("echoStructArray", input, output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoStructArray", input, e);
                } else {
                    throw e;
                }
            }
        }

        {
            try {
                binding.echoVoid();
                verify("echoVoid", null, null);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoVoid", null, e);
                } else {
                    throw e;
                }
            }
        }

        {
            byte[] input = "Base64".getBytes();
            try {
                output = binding.echoBase64(input);
                verify("echoBase64", input, output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoBase64", input, e);
                } else {
                    throw e;
                }
            }
        }

        {
            HexBinary input = new HexBinary("3344");
            try {
                output = binding.echoHexBinary(input.getBytes());
                verify("echoHexBinary", input, output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoHexBinary", input, e);
                } else {
                    throw e;
                }
            }
        }
        Calendar inputDate = Calendar.getInstance();
        inputDate.setTimeZone(TimeZone.getTimeZone("GMT"));
        inputDate.setTime(new Date());
        {
            try {
                output = binding.echoDate(inputDate);
                verify("echoDate", inputDate, output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoDate", inputDate, e);
                } else {
                    throw e;
                }
            }
        }

        {
            BigDecimal input = new BigDecimal("3.14159");
            try {
                output = binding.echoDecimal(input);
                verify("echoDecimal", input, output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoDecimal", input, e);
                } else {
                    throw e;
                }
            }
        }

        {
            Boolean input = Boolean.TRUE;
            try {
                output = new Boolean( binding.echoBoolean(input.booleanValue()));
                verify("echoBoolean", input, output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoBoolean", input, e);
                } else {
                    throw e;
                }
            }
        }

        HashMap map = new HashMap();
        map.put(new Integer(5), "String Value");
        map.put("String Key", inputDate);
        {
            HashMap input = map;
            try {
                output = binding.echoMap(input);
                verify("echoMap", input, output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoMap", input, e);
                } else {
                    throw e;
                }
            }
        }

        HashMap map2 = new HashMap();
        map2.put("this is the second map", new Boolean(true));
        map2.put("test", new Float(411));
        {
            HashMap[] input = new HashMap [] {map, map2 };
            try {
                output = binding.echoMapArray(input);
                verify("echoMapArray", input, output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoMapArray", input, e);
                } else {
                    throw e;
                }
            }
        }
    }

    /**
     * Execute the 2B tests
     */
    public void execute2B() throws Exception {
        // execute the tests
        Object output = null;
        {
            SOAPStruct input = new SOAPStruct();
            input.setVarInt(5);
            input.setVarString("Hello");
            input.setVarFloat(103F);
            try {
                StringHolder outputString = new StringHolder();
                IntHolder outputInteger = new IntHolder();
                FloatHolder outputFloat = new FloatHolder();
                binding.echoStructAsSimpleTypes(input, outputString,
                                                 outputInteger, outputFloat);
                output = new SOAPStruct();
                ((SOAPStruct)output).setVarInt(outputInteger.value);
                ((SOAPStruct)output).setVarString(outputString.value);
                ((SOAPStruct)output).setVarFloat(outputFloat.value);
                verify("echoStructAsSimpleTypes",
                       input, output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoStructAsSimpleTypes", input, e);
                } else {
                    throw e;
                }
            }
        }

        {
            SOAPStruct input = new SOAPStruct();
            input.setVarInt(5);
            input.setVarString("Hello");
            input.setVarFloat(103F);
            try {
                output = binding.echoSimpleTypesAsStruct(
                   input.getVarString(), input.getVarInt(), input.getVarFloat());
                verify("echoSimpleTypesAsStruct",
                       input,
                       output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoSimpleTypesAsStruct", input, e);
                } else {
                    throw e;
                }
            }
        }

        {
            String[][] input = new String[2][2];
            input[0][0] = "00";
            input[0][1] = "01";
            input[1][0] = "10";
            input[1][1] = "11";
            try {
                output = binding.echo2DStringArray(input);
                verify("echo2DStringArray",
                       input,
                       output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echo2DStringArray", input, e);
                } else {
                    throw e;
                }
            }
        }

        {
            SOAPStruct inputS =new SOAPStruct();
            inputS.setVarInt(5);
            inputS.setVarString("Hello");
            inputS.setVarFloat(103F);
            SOAPStructStruct input = new SOAPStructStruct();
            input.setVarString("AXIS");
            input.setVarInt(1);
            input.setVarFloat(3F);
            input.setVarStruct(inputS);
            try {
                output = binding.echoNestedStruct(input);
                verify("echoNestedStruct", input, output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoNestedStruct", input, e);
                } else {
                    throw e;
                }
            }
        }
        {
            SOAPArrayStruct input = new SOAPArrayStruct();
            input.setVarString("AXIS");
            input.setVarInt(1);
            input.setVarFloat(3F);
            input.setVarArray(new String[] {"one", "two", "three"});
            try {
                output = binding.echoNestedArray(input);
                verify("echoNestedArray", input, output);
            } catch (Exception e) {
                if (!testMode) {
                    verify("echoNestedArray", input, e);
                } else {
                    throw e;
                }
            }
        }
    }

    /**
     * Verify that the object sent was, indeed, the one you got back.
     * Subclasses are sent to override this with their own output.
     */
    protected abstract void verify(String method, Object sent, Object gotBack);

    /**
     * Main entry point.  Tests a variety of echo methods and reports
     * on their results.
     *
     * Arguments are of the form:
     *   -h localhost -p 8080 -s /soap/servlet/rpcrouter
     * -h indicats the host
     */
    public static void main(String args[]) throws Exception {
        Options opts = new Options(args);

        boolean testPerformance = opts.isFlagSet('k') > 0;
        boolean allTests = opts.isFlagSet('A') > 0;
        boolean onlyB    = opts.isFlagSet('b') > 0;
        boolean testMode = opts.isFlagSet('t') > 0;

        // set up tests so that the results are sent to System.out
        TestClient client;

        if (testPerformance) {
            client = new TestClient(testMode) {
               public void verify(String method, Object sent, Object gotBack) {
               }
            };
        } else {
            client = new TestClient(testMode) {
            public void verify(String method, Object sent, Object gotBack) {
                String message;
                if (this.equals(sent, gotBack)) {
                    message = "OK";
                } else {
                    if (gotBack instanceof Exception) {
                        if (gotBack instanceof AxisFault) {
                            message = "Fault: " +
                                ((AxisFault)gotBack).getFaultString();
                        } else {
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            message = "Exception: ";
                            ((Exception)gotBack).printStackTrace(pw);
                            message += sw.getBuffer().toString();
                        }
                    } else {
                        message = "Fail:" + gotBack + " expected " + sent;
                    }
                }
                // Line up the output
                String tab = "";
                int l = method.length();
                while (l < 25) {
                    tab += " ";
                    l++;
                }
                System.out.println(method + tab + " " + message);
            }
        };
        }

        // set up the call object
        client.setURL(opts.getURL());
        client.setUser(opts.getUser());
        client.setPassword(opts.getPassword());

        if (testPerformance) {
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 10; i++) {
                if (allTests) {
                    client.executeAll();
                } else if (onlyB) {
                    client.execute2B();
                } else {
                    client.execute2A();
                }
            }
            long stopTime = System.currentTimeMillis();
            System.out.println("That took " + (stopTime - startTime) + " milliseconds");
        } else {
            if (allTests) {
                client.executeAll();
            } else if (onlyB) {
                client.execute2B();
            } else {
                client.execute2A();
            }
        }
    }
}
