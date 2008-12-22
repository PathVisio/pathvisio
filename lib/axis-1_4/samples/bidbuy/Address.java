package samples.bidbuy;

public class Address {

    // constructors

    public Address() {};

    public Address(String name, String address, String city, String state,
                   String zipCode)
    {
         this.name=name;
         this.address=address;
         this.city=city;
         this.state=state;
         this.zipCode=zipCode;
    }
    
    // properties

    private String name;
    public String getName() { return name; }
    public void setName(String value) { name=value; }

    private String address;
    public String getAddress() { return address; }
    public void setAddress(String value) { address=value; }

    private String city;
    public String getCity() { return city; }
    public void setCity(String value) { city=value; }

    private String state;
    public String getState() { return state; }
    public void setState(String value) { state=value; }

    private String zipCode;
    public String getZipCode() { return zipCode; }
    public void setZipCode(String value) { zipCode=value; }
}
