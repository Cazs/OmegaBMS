package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by ghost on 2017/01/21.
 */
public class GenericQuote implements BusinessObject, Serializable
{
    private String _id;
    private String client;
    private String contact_person;
    private String email;
    private String tel;
    private String cell;
    private String sitename;
    private String request;
    private long date_generated;
    private String creator;
    private int status;
    private String extra;
    private Employee creator_employee;
    private GenericQuoteItem[] resources;
    private boolean marked;
    public static final String TAG = "GenericQuote";

    public StringProperty idProperty(){return new SimpleStringProperty(_id);}

    /**
     * Function to get identifier of Quote object.
     * @return Quote identifier.
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

    private StringProperty clientProperty(){return new SimpleStringProperty(client);}

    public String getClient()
    {
        return client;
    }

    public void setClient(String client)
    {
        this.client = client;
    }

    private StringProperty contact_personProperty(){return new SimpleStringProperty(contact_person);}

    public String getContact_person()
    {
        return contact_person;
    }

    public void setContact_person(String contact_person)
    {
        this.contact_person = contact_person;
    }

    private StringProperty sitenameProperty(){return new SimpleStringProperty(sitename);}

    public String getSitename()
    {
        return sitename;
    }

    public void setSitename(String sitename)
    {
        this.sitename = sitename;
    }

    private StringProperty requestProperty(){return new SimpleStringProperty(request);}

    public String getRequest()
    {
        return request;
    }

    public void setRequest(String request)
    {
        this.request = request;
    }

    public long getDate_generated()
    {
        return date_generated;
    }

    public void setDate_generated(long date_generated)
    {
        this.date_generated = date_generated;
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

    private StringProperty emailProperty(){return new SimpleStringProperty(email);}

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    private StringProperty telProperty(){return new SimpleStringProperty(tel);}

    public String getTel()
    {
        return tel;
    }

    public void setTel(String tel)
    {
        this.tel = tel;
    }

    private StringProperty cellProperty(){return new SimpleStringProperty(cell);}

    public String getCell()
    {
        return cell;
    }

    public void setCell(String cell)
    {
        this.cell = cell;
    }

    private StringProperty creatorProperty()
    {
        if(creator_employee==null)
            return new SimpleStringProperty(String.valueOf(creator));
        else return new SimpleStringProperty(String.valueOf(creator_employee.getFirstname()+" "+creator_employee.getLastname()));
    }

    public String getCreator()
    {
        if(creator_employee==null)
            return creator;
        else return creator_employee.getFirstname()+" "+creator_employee.getLastname();
    }

    public String getCreatorID(){return this.creator;}

    private StringProperty extraProperty(){return new SimpleStringProperty(extra);}

    public String getExtra()
    {
        return extra;
    }

    public void setExtra(String extra)
    {
        this.extra = extra;
    }

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

    public GenericQuoteItem[] getResources()
    {
        return resources;
    }

    public void setResources(GenericQuoteItem[] resources)
    {
        this.resources=resources;
    }

    public void setResources(ArrayList<GenericQuoteItem> resources)
    {
        this.resources = new GenericQuoteItem[resources.size()];
        for(int i=0;i<resources.size();i++)
        {
            this.resources[i] = resources.get(i);
        }
    }

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "client":
                    client = (String)val;
                    break;
                case "contact_person":
                    contact_person = (String)val;
                    break;
                case "tel":
                    tel = (String)val;
                    break;
                case "cell":
                    cell = (String)val;
                    break;
                case "email":
                    email = (String)val;
                    break;
                case "sitename":
                    sitename = String.valueOf(val);
                    break;
                case "request":
                    request = String.valueOf(val);
                    break;
                case "date_generated":
                    date_generated = Long.parseLong(String.valueOf(val));
                    break;
                case "status":
                    status = Integer.parseInt(String.valueOf(val));
                    break;
                case "creator":
                    creator = String.valueOf(val);
                    break;
                case "extra":
                    extra = String.valueOf(val);
                    break;
                default:
                    System.err.println("Unknown GenericQuote attribute '" + var + "'.");
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
            case "client":
                return client;
            case "contact_person":
                return contact_person;
            case "tel":
                return tel;
            case "cell":
                return cell;
            case "email":
                return email;
            case "sitename":
                return sitename;
            case "request":
                return request;
            case "status":
                return status;
            case "creator":
                return creator;
            case "extra":
                return extra;
            default:
                System.err.println("Unknown GenericQuote attribute '" + var + "'.");
                return null;
        }
    }

    public SimpleStringProperty quoteProperty(){
        if(this!=null)
            if(this.creator_employee!=null)
            {
                String quote_number = this.creator_employee.getFirstname() + "-"
                        + this.creator_employee.getInitials() + this.get_id().substring(0,8)
                        + " UNOFFICIAL";
                return new SimpleStringProperty(quote_number);
            }else return new SimpleStringProperty(this.contact_person);
        else return new SimpleStringProperty("N/A");
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/quote";
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("client","UTF-8") + "="
                    + URLEncoder.encode(client, "UTF-8"));
            result.append("&" + URLEncoder.encode("contact_person","UTF-8") + "="
                    + URLEncoder.encode(contact_person, "UTF-8"));
            result.append("&" + URLEncoder.encode("email","UTF-8") + "="
                    + URLEncoder.encode(email, "UTF-8"));
            result.append("&" + URLEncoder.encode("tel","UTF-8") + "="
                    + URLEncoder.encode(tel, "UTF-8"));
            result.append("&" + URLEncoder.encode("cell","UTF-8") + "="
                    + URLEncoder.encode(cell, "UTF-8"));
            result.append("&" + URLEncoder.encode("sitename","UTF-8") + "="
                    + URLEncoder.encode(sitename, "UTF-8"));
            result.append("&" + URLEncoder.encode("request","UTF-8") + "="
                    + URLEncoder.encode(request, "UTF-8"));
            result.append("&" + URLEncoder.encode("status","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(status), "UTF-8"));
            result.append("&" + URLEncoder.encode("creator","UTF-8") + "="
                    + URLEncoder.encode(creator, "UTF-8"));
            if(date_generated>0)
                result.append("&" + URLEncoder.encode("date_generated","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_generated), "UTF-8"));
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
