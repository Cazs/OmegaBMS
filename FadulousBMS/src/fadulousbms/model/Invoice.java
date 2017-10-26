/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.model;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.ClientManager;
import fadulousbms.managers.EmployeeManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 *
 * @author ghost
 */
public class Invoice implements BusinessObject, Serializable
{
    private String _id;
    //private String quote_id;
    private String job_id;
    private String creator;
    private Employee creator_employee;
    //private Quote quote;
    private Job job;
    private long date_generated;
    private String account;
    private String extra;
    private boolean marked;
    private double receivable;

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

    public long getDate_generated()
    {
        return date_generated;
    }

    public void setDate_generated(long date_generated)
    {
        this.date_generated = date_generated;
    }

    public StringProperty accountProperty(){return new SimpleStringProperty(account);}

    public String getAccount()
    {
        return account;
    }

    public void setAccount(String account)
    {
        this.account = account;
    }

    public StringProperty receivableProperty(){return new SimpleStringProperty(String.valueOf(receivable));}

    public double getReceivable()
    {
        return receivable;
    }

    public void setReceivable(double receivable)
    {
        this.receivable = receivable;
    }

    /*private StringProperty quote_idProperty(){return new SimpleStringProperty(quote_id);}

    public String getQuote_id()
    {
        return quote_id;
    }

    public void setQuote_id(String quote_id)
    {
        this.quote_id = quote_id;
    }*/

    private StringProperty job_idProperty(){return new SimpleStringProperty(job_id);}

    public String getJob_id()
    {
        return job_id;
    }

    public void setJob_id(String job_id)
    {
        this.job_id = job_id;
    }

    public StringProperty invoice_numberProperty()
    {
        return new SimpleStringProperty(_id);//TODO: fix this!
    }

    public double getTotal()
    {
        if(job==null)
        {
            IO.logAndAlert("Error " + getClass().getName(), "Job object is not set", IO.TAG_ERROR);
            return 0;
        }
        if(job.getQuote()==null)
        {
            IO.logAndAlert("Error " + getClass().getName(), "Job Quote object is not set", IO.TAG_ERROR);
            return 0;
        }

        return job.getQuote().getTotal();
    }

    private StringProperty totalProperty()
    {
        if(job==null)
        {
            IO.logAndAlert("Error " + getClass().getName(), "Job object is not set", IO.TAG_ERROR);
            return new SimpleStringProperty("N/A");
        }
        if(job.getQuote()==null)
        {
            IO.logAndAlert("Error " + getClass().getName(), "Job Quote object is not set", IO.TAG_ERROR);
            return new SimpleStringProperty("N/A");
        }

        return job.getQuote().totalProperty();
    }

    public long getJob_number()
    {
        if(job==null)
        {
            IO.logAndAlert("Error " + getClass().getName(), "Job object is not set", IO.TAG_ERROR);
            return 0;
        }
        return job.getJob_number();
    }

    public StringProperty job_numberProperty()
    {
        if(job==null)
        {
            IO.logAndAlert("Error " + getClass().getName(), "Job object is not set", IO.TAG_ERROR);
            return new SimpleStringProperty("N/A");
        }
        return new SimpleStringProperty(String.valueOf(job.getJob_number()));
    }

    public String getClient()
    {
        return  clientProperty().get();
    }

    private StringProperty clientProperty()
    {
        if(job==null)
        {
            IO.logAndAlert("Error " + getClass().getName(), "Job object is not set", IO.TAG_ERROR);
            return new SimpleStringProperty("N/A");
        }
        if(job.getQuote()==null)
        {
            IO.logAndAlert("Error " + getClass().getName(), "Job->Quote object is not set", IO.TAG_ERROR);
            return new SimpleStringProperty("N/A");
        }
        if(job.getQuote().getClient().getClient_name()==null)
        {
            IO.logAndAlert("Error " + getClass().getName(), "Job->Quote->Client object is not set", IO.TAG_ERROR);
            return new SimpleStringProperty("N/A");
        }
        if(ClientManager.getInstance().getClients()==null)
        {
            IO.logAndAlert("Error " + getClass().getName(), "Job->Client object is not set", IO.TAG_ERROR);
            return new SimpleStringProperty("N/A");
        }

        for(Client client : ClientManager.getInstance().getClients().values())
            if(client.get_id().equals(job.getQuote().getClient_id()))
                return new SimpleStringProperty(job.getQuote().getClient().getClient_name());

        return new SimpleStringProperty("N/A");
    }

    private StringProperty creatorProperty()
    {
        if(EmployeeManager.getInstance().getEmployees()==null)
        {
            IO.logAndAlert("Error " + getClass().getName(), "No employees were found in the database.", IO.TAG_ERROR);
            return new SimpleStringProperty(creator);
        }

        Employee[] employees = new Employee[EmployeeManager.getInstance().getEmployees().size()];
        EmployeeManager.getInstance().getEmployees().values().toArray(employees);

        for(Employee employee : employees)
            if(employee.getUsr().equals(creator))
                return new SimpleStringProperty(employee.toString());
        return new SimpleStringProperty(creator);
    }

    public String getCreator()
    {
        return creatorProperty().get();
    }

    public void setCreator(String creator)
    {
        this.creator = creator;
    }

    public Employee getCreator_employee()
    {
        return creator_employee;
    }

    public void setCreator_employee(Employee creator_employee)
    {
        this.creator_employee = creator_employee;
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

    /*public Quote getQuote()
    {
        return quote;
    }

    public void setQuote(Quote quote)
    {
        this.quote = quote;
    }*/

    public Job getJob()
    {
        return job;
    }

    public void setJob(Job job)
    {
        this.job = job;
    }

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "date_generated":
                    date_generated = Long.parseLong(String.valueOf(val));
                    break;
                case "job_id":
                    job_id = String.valueOf(val);
                    break;
                case "creator":
                    creator = String.valueOf(val);
                    break;
                case "account":
                    account = String.valueOf(val);
                    break;
                case "receivable":
                    receivable = Double.valueOf(String.valueOf(val));
                    break;
                case "extra":
                    extra = String.valueOf(val);
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "unknown Invoice attribute '" + var + "'.");
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
            case "job_id":
                return job_id;
            case "date_generated":
                return date_generated;
            case "creator":
                return creator;
            case "account":
                return account;
            case "receivable":
                return receivable;
            case "extra":
                return extra;
            default:
                IO.log(getClass().getName(), IO.TAG_ERROR, "unknown Invoice attribute '" + var + "'.");
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
            /*result.append(URLEncoder.encode("quote_id","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(quote_id), "UTF-8"));*/
            result.append("&" + URLEncoder.encode("job_id","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(job_id), "UTF-8"));
            if(date_generated>0)
                result.append("&" + URLEncoder.encode("date_generated","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_generated), "UTF-8"));
            result.append("&" + URLEncoder.encode("creator","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(creator), "UTF-8"));
            result.append("&" + URLEncoder.encode("account","UTF-8") + "="
                    + URLEncoder.encode(account, "UTF-8"));
            result.append("&" + URLEncoder.encode("receivable","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(receivable), "UTF-8"));
            if(extra!=null)
                result.append(URLEncoder.encode("extra","UTF-8") + "="
                        + URLEncoder.encode(extra, "UTF-8") + "&");
            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }
}
