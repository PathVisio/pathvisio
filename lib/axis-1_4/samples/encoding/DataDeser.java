package samples.encoding;

import org.apache.axis.Constants;
import org.apache.axis.encoding.DeserializationContext;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.DeserializerImpl;
import org.apache.axis.encoding.FieldTarget;
import org.apache.axis.message.SOAPHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.util.Hashtable;

public class DataDeser extends DeserializerImpl
{
    public static final String STRINGMEMBER = "stringMember";
    public static final String FLOATMEMBER = "floatMember";
    public static final String DATAMEMBER = "dataMember";
    public static final QName myTypeQName = new QName("typeNS", "Data");
    
    private Hashtable typesByMemberName = new Hashtable();  
    
    public DataDeser()
    {
        typesByMemberName.put(STRINGMEMBER, Constants.XSD_STRING);
        typesByMemberName.put(FLOATMEMBER, Constants.XSD_FLOAT);
        typesByMemberName.put(DATAMEMBER, myTypeQName);
        value = new Data();
    }
    
    /** DESERIALIZER STUFF - event handlers
     */

    /**
     * This method is invoked when an element start tag is encountered.
     * @param namespace is the namespace of the element
     * @param localName is the name of the element
     * @param prefix is the element's prefix
     * @param attributes are the attributes on the element...used to get the type
     * @param context is the DeserializationContext
     */
    public SOAPHandler onStartChild(String namespace,
                                    String localName,
                                    String prefix,
                                    Attributes attributes,
                                    DeserializationContext context)
        throws SAXException
    {
        QName typeQName = (QName)typesByMemberName.get(localName);
        if (typeQName == null)
            throw new SAXException("Invalid element in Data struct - " + localName);
        
        // These can come in either order.
        Deserializer dSer = context.getDeserializerForType(typeQName);
        try {
            dSer.registerValueTarget(new FieldTarget(value, localName));
        } catch (NoSuchFieldException e) {
            throw new SAXException(e);
        }
        
        if (dSer == null)
            throw new SAXException("No deserializer for a " + typeQName + "???");
        
        return (SOAPHandler)dSer;
    }
}
