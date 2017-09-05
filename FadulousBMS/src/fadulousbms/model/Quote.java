package fadulousbms.model;

import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by ghost on 2017/01/21.
 */
public class Quote implements BusinessObject
{
    private String _id;
    private String client_id;
    private String contact_person_id;
    private String sitename;
    private String request;
    private long date_generated;
    private String creator;
    private double revision;
    private String extra;
    private int status;
    private Client client;
    private Employee contact_person;
    private Employee creator_employee;
    private QuoteItem[] resources;
    private Employee[] representatives;
    public static double VAT = 14.0;

    private boolean marked;
    public static final String TAG = "Quote";

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

    private StringProperty client_idProperty(){return new SimpleStringProperty(client_id);}

    public String getClient_id()
    {
        return client_id;
    }

    public void setClient_id(String client_id)
    {
        this.client_id = client_id;
    }

    private StringProperty contact_person_idProperty(){return new SimpleStringProperty(contact_person_id);}

    public String getContact_person_id()
    {
        return contact_person_id;
    }

    public void setContact_person_id(String contact_person_id)
    {
        this.contact_person_id = contact_person_id;
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
    }

    private StringProperty revisionProperty(){return new SimpleStringProperty(String.valueOf(revision));}

    public double getRevision()
    {
        return revision;
    }

    public void setRevision(double revision)
    {
        this.revision = revision;
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

    public SimpleStringProperty totalProperty(){return new SimpleStringProperty(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(getTotal()));}

    public double getTotal()
    {
        //Compute total
        double total=0;
        if(this.getResources()!=null)
        {
            for (QuoteItem item : this.getResources())
            {
                //compute additional costs for each Quote Item
                if (item.getAdditional_costs() != null)
                {
                    if (!item.getAdditional_costs().isEmpty())
                    {
                        String[] costs = item.getAdditional_costs().split(";");
                        for (String str_cost : costs)
                        {
                            if (str_cost.contains("="))
                            {
                                double cost = Double.parseDouble(str_cost.split("=")[1]);
                                total += cost;
                            } else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid Quote Item additional cost.");
                        }
                    }
                }
                //add Quote Item rate*quantity to total
                total += item.getRateValue() * item.getQuantityValue() + item.getLabourCost();
            }
        }
        return total;
    }

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "client_id":
                    client_id = (String)val;
                    break;
                case "contact_person_id":
                    contact_person_id = (String)val;
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
                case "revision":
                    revision = Integer.parseInt(String.valueOf(val));
                    break;
                case "extra":
                    extra = String.valueOf(val);
                    break;
                default:
                    System.err.println("Unknown Quote attribute '" + var + "'.");
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
            case "client_id":
                return client_id;
            case "contact_person_id":
                return contact_person_id;
            case "sitename":
                return sitename;
            case "request":
                return request;
            case "status":
                return status;
            case "creator":
                return creator;
            case "revision":
                return revision;
            case "extra":
                return extra;
            default:
                System.err.println("Unknown Quote attribute '" + var + "'.");
                return null;
        }
    }

    public QuoteItem[] getResources()
    {
        return resources;
    }

    public void setResources(QuoteItem[] resources)
    {
        this.resources=resources;
    }

    public void setResources(ArrayList<QuoteItem> resources)
    {
        this.resources = new QuoteItem[resources.size()];
        for(int i=0;i<resources.size();i++)
        {
            this.resources[i] = resources.get(i);
        }
    }

    public Employee[] getRepresentatives()
    {
        return representatives;
    }

    public void setRepresentatives(Employee[] representatives)
    {
        this.representatives=representatives;
    }

    public void setRepresentatives(ArrayList<Employee> reps)
    {
        this.representatives = new Employee[reps.size()];
        for(int i=0;i<reps.size();i++)
        {
            this.representatives[i] = reps.get(i);
        }
    }

    public Client getClient()
    {
        return client;
    }

    public void setClient(Client client)
    {
        this.client = client;
    }

    public Employee getContactPerson()
    {
        return contact_person;
    }

    public void setContactPerson(Employee contact_person)
    {
        this.contact_person = contact_person;
    }

    public SimpleStringProperty quoteProperty(){
        if(this!=null)
            if(this.getContactPerson()!=null)
            {
                String quote_number = this.getContactPerson().getFirstname() + "-"
                        + this.getContactPerson().getInitials() + this.get_id().substring(0,8)
                        + " REV" + String.valueOf(this.getRevision()).substring(0,3);
                return new SimpleStringProperty(quote_number);
            }else return new SimpleStringProperty(this.getContact_person_id());
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
            result.append(URLEncoder.encode("client_id","UTF-8") + "="
                    + URLEncoder.encode(client_id, "UTF-8") + "&");
            result.append(URLEncoder.encode("contact_person_id","UTF-8") + "="
                    + URLEncoder.encode(contact_person_id, "UTF-8") + "&");
            result.append(URLEncoder.encode("date_generated","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(date_generated), "UTF-8"));
            result.append("&" + URLEncoder.encode("sitename","UTF-8") + "="
                    + URLEncoder.encode(sitename, "UTF-8"));
            result.append("&" + URLEncoder.encode("request","UTF-8") + "="
                    + URLEncoder.encode(request, "UTF-8"));
            result.append("&" + URLEncoder.encode("status","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(status), "UTF-8"));
            result.append("&" + URLEncoder.encode("creator","UTF-8") + "="
                    + URLEncoder.encode(creator, "UTF-8"));
            result.append("&" + URLEncoder.encode("revision","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(revision), "UTF-8"));
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
