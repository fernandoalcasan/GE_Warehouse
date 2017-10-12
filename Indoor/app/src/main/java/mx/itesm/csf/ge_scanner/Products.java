package mx.itesm.csf.ge_scanner;

/**
 * Created by danflovier on 16/09/2017.
 */


// Class of Products
public class Products {
    private int id;
    private String ean, product, id_lift_truck, section, status;

    // Empty Constructor
    public Products() {}

    // Parameterized constructor for receiving the data of the products.
    public Products(int id, String ean, String product, String id_lift_truck, String status, String section) {
        this.id = id;
        this.ean = ean;
        this.product = product;
        this.id_lift_truck = id_lift_truck;
        this.status = status;
        this.section = section;
    }

    // GET method to provide access to the value a variable holds
    public int getID(){ return id; }
    public String getEAN() {
        return ean;
    }
    public String getProduct() {
        return product;
    }
    public String getID_LiftTruck(){ return id_lift_truck;}
    public String getSection(){ return section; }
    public String getStatus(){ return status; }

    // SET method to assign values to the variables
    public void setID(int id){ this.id = id;}
    public void setEAN(String ean) {
        this.ean = ean;
    }
    public void setProduct(String product) {
        this.product = product;
    }
    public void setID_LiftTruck(String id_lift_truck){ this.id_lift_truck = id_lift_truck;}
    public void setSection(String section){ this.section = section; }
    public void setStatus(String status){ this.status = status; }
}


