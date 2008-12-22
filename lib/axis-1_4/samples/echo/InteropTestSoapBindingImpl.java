/**
 * InteropTestSoapBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis Wsdl2java emitter.
 *
 * And then it was hand modified to echo
 * the arguments back to the caller.
 */

package samples.echo;

import org.apache.axis.MessageContext;

public class InteropTestSoapBindingImpl implements samples.echo.InteropTestPortType {
    public java.lang.String echoString(java.lang.String inputString) throws java.rmi.RemoteException {
        MessageContext.getCurrentContext().setProperty(echoHeaderStringHandler.ECHOHEADER_STRING_ID, "header text");
        return inputString;
    }

    public java.lang.String[] echoStringArray(java.lang.String[] inputStringArray) throws java.rmi.RemoteException {
        return inputStringArray;
    }

    public int echoInteger(int inputInteger) throws java.rmi.RemoteException {
        return inputInteger;
    }

    public int[] echoIntegerArray(int[] inputIntegerArray) throws java.rmi.RemoteException {
        return inputIntegerArray;
    }

    public float echoFloat(float inputFloat) throws java.rmi.RemoteException {
        return inputFloat;
    }

    public float[] echoFloatArray(float[] inputFloatArray) throws java.rmi.RemoteException {
        return inputFloatArray;
    }

    public samples.echo.SOAPStruct echoStruct(samples.echo.SOAPStruct inputStruct) throws java.rmi.RemoteException {
        return inputStruct;
    }

    public samples.echo.SOAPStruct[] echoStructArray(samples.echo.SOAPStruct[] inputStructArray) throws java.rmi.RemoteException {
        return inputStructArray;
    }

    public void echoVoid() throws java.rmi.RemoteException {
    }

    public byte[] echoBase64(byte[] inputBase64) throws java.rmi.RemoteException {
        return inputBase64;
    }

    public java.util.Calendar echoDate(java.util.Calendar inputDate) throws java.rmi.RemoteException {
        return inputDate;
    }

    public byte[] echoHexBinary(byte[] inputHexBinary) throws java.rmi.RemoteException {
        return inputHexBinary;
    }

    public java.math.BigDecimal echoDecimal(java.math.BigDecimal inputDecimal) throws java.rmi.RemoteException {
        return inputDecimal;
    }

    public boolean echoBoolean(boolean inputBoolean) throws java.rmi.RemoteException {
        return inputBoolean;
    }

    public void echoStructAsSimpleTypes(samples.echo.SOAPStruct inputStruct, javax.xml.rpc.holders.StringHolder outputString, javax.xml.rpc.holders.IntHolder outputInteger, javax.xml.rpc.holders.FloatHolder outputFloat) throws java.rmi.RemoteException {
        outputString.value = inputStruct.getVarString() ;
        outputInteger.value = inputStruct.getVarInt() ;
        outputFloat.value = inputStruct.getVarFloat() ;
    }

    public samples.echo.SOAPStruct echoSimpleTypesAsStruct(java.lang.String inputString, int inputInteger, float inputFloat) throws java.rmi.RemoteException {
        samples.echo.SOAPStruct s = new samples.echo.SOAPStruct();
        s.setVarInt(inputInteger);
        s.setVarString(inputString);
        s.setVarFloat(inputFloat);
        return s;
    }

    public java.lang.String[][] echo2DStringArray(java.lang.String[][] input2DStringArray) throws java.rmi.RemoteException {
        return input2DStringArray;
    }

    public samples.echo.SOAPStructStruct echoNestedStruct(samples.echo.SOAPStructStruct inputStruct) throws java.rmi.RemoteException {
        return inputStruct;
    }

    public samples.echo.SOAPArrayStruct echoNestedArray(samples.echo.SOAPArrayStruct inputStruct) throws java.rmi.RemoteException {
        return inputStruct;
    }

    /**
     * This method accepts a Map and echoes it back to the client.
     */
    public java.util.HashMap echoMap(java.util.HashMap input) {
        return input;
    }

    /**
     * This method accepts an array of Maps and echoes it back to the client.
     */
    public java.util.HashMap [] echoMapArray(java.util.HashMap[] input) {
        return input;
    }

    /**
     * This method accepts a Token (xsd:token) and echoes it back to the client.
     */
    public org.apache.axis.types.Token echoToken(org.apache.axis.types.Token input) throws java.rmi.RemoteException {
        return input;
    }

    /**
     * This method accepts a NormalizedString (xsd:normalizedString) and echoes
     * it back to the client.
     */
    public org.apache.axis.types.NormalizedString echoNormalizedString(org.apache.axis.types.NormalizedString input) throws java.rmi.RemoteException {
        return input;
    }

        /**
         * This method accepts a UnsignedLong (xsd:unsignedLong) and echoes
         * it back to the client.
         */
    public org.apache.axis.types.UnsignedLong echoUnsignedLong(org.apache.axis.types.UnsignedLong input) throws java.rmi.RemoteException {
            return input;
        }

        /**
         * This method accepts a UnsignedInt (xsd:unsignedInt) and echoes
         * it back to the client.
         */
    public org.apache.axis.types.UnsignedInt echoUnsignedInt(org.apache.axis.types.UnsignedInt input) throws java.rmi.RemoteException {
            return input;
        }

        /**
         * This method accepts a UnsignedShort (xsd:unsignedShort) and echoes
         * it back to the client.
         */
    public org.apache.axis.types.UnsignedShort echoUnsignedShort(org.apache.axis.types.UnsignedShort input) throws java.rmi.RemoteException {
            return input;
        }

        /**
         * This method accepts a UnsignedByte (xsd:unsignedByte) and echoes
         * it back to the client.
         */
    public org.apache.axis.types.UnsignedByte echoUnsignedByte(org.apache.axis.types.UnsignedByte input) throws java.rmi.RemoteException {
            return input;
        }

    /**
     * This method accepts a NonNegativeInteger (xsd:nonNegativeInteger) and echoes
     * it back to the client.
     */
    public org.apache.axis.types.NonNegativeInteger echoNonNegativeInteger(org.apache.axis.types.NonNegativeInteger input) throws java.rmi.RemoteException {
            return input;
        }
        
    /**
     * This method accepts a PositiveInteger (xsd:positiveInteger) and echoes
     * it back to the client.
     */
    public org.apache.axis.types.PositiveInteger echoPositiveInteger(org.apache.axis.types.PositiveInteger input) throws java.rmi.RemoteException {
            return input;
        }

    /**
     * This method accepts a NonPositiveInteger (xsd:nonPositiveInteger) and echoes
     * it back to the client.
     */
    public org.apache.axis.types.NonPositiveInteger echoNonPositiveInteger(org.apache.axis.types.NonPositiveInteger input) throws java.rmi.RemoteException {
            return input;
        }

    /**
     * This method accepts a NegativeInteger (xsd:negativeInteger) and echoes
     * it back to the client.
     */
    public org.apache.axis.types.NegativeInteger echoNegativeInteger(org.apache.axis.types.NegativeInteger input) throws java.rmi.RemoteException {
            return input;
        }


}
