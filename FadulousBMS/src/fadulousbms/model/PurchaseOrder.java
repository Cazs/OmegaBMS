package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ghost on 2017/01/21.
 */
public class PurchaseOrder implements BusinessObject, Serializable
{
    private String _id;
    private int number;
    private String supplier_id;
    private String contact_person_id;
    private double vat;
    private long date_logged;
    private String creator;
    private String account;
    private int status;
    private Employee creator_employee;
    private boolean marked;
    private String extra;
    public static final String TAG = "PurchaseOrder";
    public PurchaseOrderItem[] resources;

    public StringProperty idProperty(){return new SimpleStringProperty(_id);}

    /**
     * Function to get identifier of PurchaseOrder object.
     * @return PurchaseOrder identifier.
     */
    @Override
    public String get_id()
    {
        return _id;
    }

    /**
     * Method to assign identifier to this object.
     * @param _id identifier to be assigned to this object.
     */
    public void set_id(String _id)
    {
        this._id = _id;
    }


    /**
     * Function to get a shortened identifier of this object.
     * @return The shortened identifier.
     */
    public StringProperty short_idProperty(){return new SimpleStringProperty(_id.substring(0, 8));}

    @Override
    public String getShort_id()
    {
        return _id.substring(0, 8);
    }

    @Override
    public boolean isMarked()
    {
        return marked;
    }

    @Override
    public void setMarked(boolean marked){this.marked=marked;}

    private StringProperty numberProperty(){return new SimpleStringProperty(String.valueOf(number));}

    public String getNumber()
    {
        return String.valueOf(number);
    }

    public int getNumberValue()
    {
        return number;
    }

    public void setNumber(int number)
    {
        this.number = number;
    }

    private StringProperty vatProperty(){return new SimpleStringProperty(String.valueOf(vat));}

    public String getVat()
    {
        return String.valueOf(vat);
    }

    public double getVatVal()
    {
        return vat;
    }

    public void setVat(double vat)
    {
        this.vat= vat;
    }

    public long getDate_logged()
    {
        return date_logged;
    }

    public void setDate_logged(long date_logged)
    {
        this.date_logged = date_logged;
    }

    public String getSupplier_id()
    {
        return supplier_id;
    }

    public void setSupplier_id(String supplier_id)
    {
        this.supplier_id = supplier_id;
    }

    public String getContact_person_id()
    {
        return contact_person_id;
    }

    public void setContact_person_id(String contact_person_id)
    {
        this.contact_person_id = contact_person_id;
    }

    public PurchaseOrderItem[] getResources()
    {
        return resources;
    }

    public void setResources(PurchaseOrderItem[] resources)
    {
        this.resources = resources;
    }

    public StringProperty creatorProperty()
    {
        if(creator_employee==null)
            return new SimpleStringProperty(String.valueOf(creator));
        else return new SimpleStringProperty(String.valueOf(creator_employee.toString()));
    }

    public String getCreator()
    {
        if(creator_employee==null)
            return creator;
        else return creator_employee.toString();
    }

    public String getCreatorID(){return this.creator;}

    public void setCreator(String creator)
    {
        this.creator = creator;
    }

    public Employee getCreatorEmployee()
    {
        return this.creator_employee;
    }

    public void setCreator(Employee creator_employee)
    {
        this.creator_employee = creator_employee;
        if(creator_employee!=null)
            setCreator(creator_employee.getUsr());
    }

    private StringProperty accountProperty(){return new SimpleStringProperty(account);}

    public String getAccount()
    {
        return account;
    }

    public void setAccount(String account)
    {
        this.account = account;
    }

    private StringProperty statusProperty(){return new SimpleStringProperty(String.valueOf(status));}

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    private StringProperty extraProperty(){return new SimpleStringProperty(extra);}

    public String getExtra()
    {
        return extra;
    }

    public void setExtra(String extra)
    {
        this.extra = extra;
    }

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "number":
                    number = Integer.valueOf((String)val);
                    break;
                case "supplier_id":
                    supplier_id = String.valueOf(val);
                    break;
                case "vat":
                    vat = Double.valueOf((String)val);
                    break;
                case "date_logged":
                    date_logged = Long.valueOf((String)val);
                    break;
                case "status":
                    status = Integer.valueOf((String)val);
                    break;
                case "creator":
                    creator = (String)val;
                    break;
                case "extra":
                    extra = String.valueOf(val);
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "Unknown PurchaseOrder attribute '" + var + "'.");
                    break;
            }
        }catch (NumberFormatException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @Override
    public Object get(String var)
    {
        switch (var.toLowerCase())
        {
            case "_id":
                return _id;
            case "number":
                return number;
            case "supplier_id":
                return supplier_id;
            case "vat":
                return vat;
            case "date_logged":
                return date_logged;
            case "creator":
                return creator;
            case "status":
                return status;
            case "extra":
                return extra;
            default:
                System.err.println("Unknown PurchaseOrder attribute '" + var + "'.");
                return null;
        }
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/purchaseorder";
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("number","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(number), "UTF-8"));
            result.append("&" + URLEncoder.encode("supplier_id","UTF-8") + "="
                    + URLEncoder.encode(supplier_id, "UTF-8"));
            result.append("&" + URLEncoder.encode("contact_person_id","UTF-8") + "="
                    + URLEncoder.encode(contact_person_id, "UTF-8"));
            result.append("&" + URLEncoder.encode("vat","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(vat), "UTF-8"));
            result.append("&" + URLEncoder.encode("status","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(status), "UTF-8"));
            result.append("&" + URLEncoder.encode("account","UTF-8") + "="
                    + URLEncoder.encode(account, "UTF-8"));
            if(date_logged>0)
                result.append("&" + URLEncoder.encode("date_logged","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_logged), "UTF-8"));
            result.append("&" + URLEncoder.encode("creator","UTF-8") + "="
                    + URLEncoder.encode(creator, "UTF-8"));
            if(extra!=null)
                if(!extra.isEmpty())
                    result.append("&" + URLEncoder.encode("extra","UTF-8") + "="
                            + URLEncoder.encode(extra, "UTF-8"));
            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(TAG, IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }
}