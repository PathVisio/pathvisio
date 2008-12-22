package samples.encoding;

public class Data
{
    public String stringMember;
    public Float floatMember;
    public Data dataMember;
    
    public String toString()
    {
        return getStringVal("", this);
    }
    
    public String getStringVal(String indent, Data topLevel)
    {
        String ret = "\n" + indent + "Data:\n";
        ret +=       indent + " str[" + stringMember + "]\n";
        ret +=       indent + " float[" + floatMember + "]\n";
        ret +=       indent + " data[";
        
        if (dataMember != null) {
            if (dataMember == topLevel) {
                ret += " top level";
            } else
                ret += dataMember.getStringVal(indent + "  ", topLevel) + "\n" + indent;
        } else
            ret += " null";
        
        ret += " ]";
        return ret;
    }
}
