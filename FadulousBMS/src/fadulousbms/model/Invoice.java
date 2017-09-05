/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 *
 * @author ghost
 */
public class Invoice implements BusinessObject
{
    private String _id;
    private String invoice_description;
    private String issuer_org_id;
    private String receiver_org_id;
    private String labour;
    private String tax;
    transient private double total_value;
    transient private double ex_total_value;
    private long request_date;
    private long date_generated;
    private String extra;
    private Resource[] resources;
    private Employee[] representatives;
    private boolean marked;
    public static final String TAG = "Invoice";

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
    public boolean isMarked() { return marked;}

    @Override
    public void setMarked(boolean marked){this.marked=marked;}


    private StringProperty invoice_descriptionProperty(){return new SimpleStringProperty(invoice_description);}

    public String getInvoice_description()
    {
        return invoice_description;
    }

    public void setInvoice_description(String invoice_description)
    {
        this.invoice_description = invoice_description;
    }

    private StringProperty issuer_org_idProperty(){return new SimpleStringProperty(issuer_org_id);}

    public String getIssuer_org_id()
    {
        return issuer_org_id;
    }

    public void setIssuer_org_id(String issued_by_org)
    {
        this.issuer_org_id = issued_by_org;
    }

    private StringProperty receiver_org_idProperty(){return new SimpleStringProperty(receiver_org_id);}

    public String getReceiver_org_id()
    {
        return receiver_org_id;
    }

    public void setReceiver_org_id(String recv_by_org)
    {
        this.receiver_org_id = recv_by_org;
    }

    private StringProperty labourProperty(){return new SimpleStringProperty(String.valueOf(labour));}

    public String getLabour()
    {
        return labour;
    }

    public void setLabour(String labour)
    {
        this.labour = labour;
    }

    private StringProperty taxProperty(){return new SimpleStringProperty(String.valueOf(tax));}

    public String getTax()
    {
        return tax;
    }

    public void setTax(String tax)
    {
        this.tax = tax;
    }

    public long getRequest_date()
    {
        return request_date;
    }

    public void setRequest_date(long request_date)
    {
        this.request_date = request_date;
    }

    public long getDate_generated()
    {
        return date_generated;
    }

    public void setDate_generated(long date_generated)
    {
        this.date_generated = date_generated;
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

    private StringProperty total_valueProperty(){return new SimpleStringProperty(String.valueOf(total_value));}

    public double getTotal_value(){return this.total_value;}

    public void setTotal_value(double total_value){this.total_value=total_value;}

    private StringProperty ex_total_valueProperty(){return new SimpleStringProperty(String.valueOf(ex_total_value));}

    public double getEx_total_value(){return this.ex_total_value;}

    public void setEx_total_value(double ex_total_value){this.ex_total_value=ex_total_value;}

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "invoice_description":
                    invoice_description = (String) val;
                    break;
                case "issuer_org_id":
                    issuer_org_id = (String) val;
                    break;
                case "receiver_org_id":
                    receiver_org_id = (String) val;
                    break;
                case "labour":
                    labour = String.valueOf(val);
                    break;
                case "tax":
                    tax = String.valueOf(val);
                    break;
                case "request_date":
                    request_date = Long.parseLong(String.valueOf(val));
                    break;
                case "date_generated":
                    date_generated = Long.parseLong(String.valueOf(val));
                    break;
                case "extra":
                    extra = String.valueOf(val);
                    break;
                case "total_value":
                    total_value = Double.parseDouble((String) val);
                    break;
                case "ex_total_value":
                    ex_total_value = Double.parseDouble((String) val);
                    break;
                default:
                    System.err.println("Unknown Invoice attribute '" + var + "'.");
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
            case "short_id":
                return getShort_id();
            case "invoice_description":
                return invoice_description;
            case "issuer_org_id":
                return issuer_org_id;
            case "receiver_org_id":
                return receiver_org_id;
            case "labour":
                return labour;
            case "tax":
                return tax;
            case "request_date":
                return request_date;
            case "date_generated":
                return date_generated;
            case "extra":
                return extra;
            case "total_value":
                return total_value;
            case "ex_total_value":
                return ex_total_value;
            default:
                System.err.println("Unknown Invoice attribute '" + var + "'.");
                return null;
        }
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/invoice";
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("invoice_description","UTF-8") + "="
                    + URLEncoder.encode(invoice_description, "UTF-8") + "&");
            result.append(URLEncoder.encode("receiver_org_id","UTF-8") + "="
                    + URLEncoder.encode(receiver_org_id, "UTF-8") + "&");
            result.append(URLEncoder.encode("issuer_org_id","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(issuer_org_id), "UTF-8") + "&");
            result.append(URLEncoder.encode("labour","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(labour), "UTF-8") + "&");
            result.append(URLEncoder.encode("tax","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(tax), "UTF-8") + "&");
            result.append(URLEncoder.encode("request_date","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(request_date), "UTF-8") + "&");
            result.append(URLEncoder.encode("date_generated","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(date_generated), "UTF-8") + "&");
            if(extra!=null)
                result.append(URLEncoder.encode("extra","UTF-8") + "="
                        + URLEncoder.encode(extra, "UTF-8") + "&");
            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(TAG, IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }

    public Resource[] getResources()
    {
        return resources;
    }

    public void setResources(Resource[] resources)
    {
        this.resources=resources;
    }

    public void setResources(ArrayList<Resource> resources)
    {
        this.resources = new Resource[resources.size()];
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
}
