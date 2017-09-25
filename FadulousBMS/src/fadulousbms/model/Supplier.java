package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ghost on 2017/01/03.
 */
public class Supplier implements BusinessObject, Serializable
{
    private String _id;
    private String supplier_name;
    private String physical_address;
    private String postal_address;
    private String tel;
    private String fax;
    private String speciality;
    private boolean active;
    private long date_partnered;
    private String website;
    private String contact_email;
    private String other;
    private boolean marked;

    public StringProperty idProperty(){return new SimpleStringProperty(_id);}

    @Override
    public String get_id()
    {
        return _id;
    }

    public void set_id(String _id)
    {
        this._id = _id;
    }

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

    public StringProperty supplier_nameProperty(){return new SimpleStringProperty(supplier_name);}

    public String getSupplier_name()
    {
        return supplier_name;
    }

    public void setSupplier_name(String supplier_name)
    {
        this.supplier_name = supplier_name;
    }

    public StringProperty physical_addressProperty(){return new SimpleStringProperty(physical_address);}

    public String getPhysical_address()
    {
        return physical_address;
    }

    public void setPhysical_address(String physical_address)
    {
        this.physical_address = physical_address;
    }

    public StringProperty postal_addressProperty(){return new SimpleStringProperty(postal_address);}

    public String getPostal_address()
    {
        return postal_address;
    }

    public void setPostal_address(String postal_address)
    {
        this.postal_address = postal_address;
    }

    public StringProperty telProperty(){return new SimpleStringProperty(tel);}

    public String getTel()
    {
        return tel;
    }

    public void setTel(String tel)
    {
        this.tel = tel;
    }

    public StringProperty faxProperty(){return new SimpleStringProperty(fax);}

    public String getFax()
    {
        return fax;
    }

    public void setFax(String fax)
    {
        this.fax = fax;
    }

    public StringProperty specialityProperty(){return new SimpleStringProperty(speciality);}

    public String getSpeciality()
    {
        return speciality;
    }

    public void setSpeciality(String speciality)
    {
        this.speciality = speciality;
    }

    public StringProperty activeProperty(){return new SimpleStringProperty(String.valueOf(active));}

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    //public StringProperty date_partneredProperty(){return new SimpleStringProperty(String.valueOf(date_partnered));}

    public long getDate_partnered()
    {
        return date_partnered;
    }

    public void setDate_partnered(long date_partnered)
    {
        this.date_partnered = date_partnered;
    }

    public StringProperty websiteProperty(){return new SimpleStringProperty(website);}

    public String getWebsite()
    {
        return website;
    }

    public void setWebsite(String website)
    {
        this.website = website;
    }

    public StringProperty contact_emailProperty(){return new SimpleStringProperty(contact_email);}

    public String getContact_email()
    {
        return contact_email;
    }

    public void setContact_email(String contact_email)
    {
        this.contact_email = contact_email;
    }

    public StringProperty otherProperty(){return new SimpleStringProperty(other);}

    public String getOther()
    {
        return other;
    }

    public void setOther(String other)
    {
        this.other = other;
    }

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "supplier_name":
                    supplier_name = (String)val;
                    break;
                case "physical_address":
                    physical_address = (String)val;
                    break;
                case "postal_address":
                    postal_address = (String)val;
                    break;
                case "tel":
                    tel = (String)val;
                    break;
                case "fax":
                    fax = (String)val;
                    break;
                case "speciality":
                    speciality = (String)val;
                    break;
                case "active":
                    active = Boolean.parseBoolean(String.valueOf(val));
                    break;
                case "date_partnered":
                    date_partnered = Long.parseLong(String.valueOf(val));
                    break;
                case "website":
                    website = (String)val;
                    break;
                case "contact_email":
                    contact_email = (String)val;
                    break;
                case "other":
                    other = (String)val;
                    break;
                default:
                    System.err.println("Unknown Supplier attribute '" + var + "'.");
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
            case "supplier_name":
                return supplier_name;
            case "physical_address":
                return physical_address;
            case "postal_address":
                return postal_address;
            case "tel":
                return tel;
            case "fax":
                return fax;
            case "speciality":
                return speciality;
            case "active":
                return active;
            case "date_partnered":
                return date_partnered;
            case "website":
                return website;
            case "contact_email":
                return contact_email;
            case "other":
                return other;
            default:
                System.err.println("Unknown Supplier attribute '" + var + "'.");
                return null;
        }
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/supplier";
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("supplier_name","UTF-8") + "="
                    + URLEncoder.encode(supplier_name, "UTF-8") + "&");
            result.append(URLEncoder.encode("physical_address","UTF-8") + "="
                    + URLEncoder.encode(physical_address, "UTF-8") + "&");
            result.append(URLEncoder.encode("postal_address","UTF-8") + "="
                    + URLEncoder.encode(postal_address, "UTF-8") + "&");
            result.append(URLEncoder.encode("tel","UTF-8") + "="
                    + URLEncoder.encode(tel, "UTF-8") + "&");
            result.append(URLEncoder.encode("fax","UTF-8") + "="
                    + URLEncoder.encode(fax, "UTF-8") + "&");
            result.append(URLEncoder.encode("speciality","UTF-8") + "="
                    + URLEncoder.encode(speciality, "UTF-8") + "&");
            result.append(URLEncoder.encode("active","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(active), "UTF-8") + "&");
            result.append(URLEncoder.encode("date_partnered","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(date_partnered), "UTF-8") + "&");
            result.append(URLEncoder.encode("website","UTF-8") + "="
                    + URLEncoder.encode(website, "UTF-8") + "&");
            result.append(URLEncoder.encode("contact_email","UTF-8") + "="
                    + URLEncoder.encode(contact_email, "UTF-8") + "&");
            if(other!=null)
                result.append(URLEncoder.encode("other","UTF-8") + "="
                        + URLEncoder.encode(other, "UTF-8"));

            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }

    @Override
    public String toString()
    {
        return supplier_name;
    }
}
