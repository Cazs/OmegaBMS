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
    //private String quote_id;
    private String job_id;
    private String creator;
    private Employee creator_employee;
    //private Quote quote;
    private Job job;
    private long date_generated;
    private String extra;
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

    private StringProperty creatorProperty(){return new SimpleStringProperty(creator);}

    public String getCreator()
    {
        return creator;
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
                case "extra":
                    extra = String.valueOf(val);
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
            case "job_id":
                return job_id;
            case "date_generated":
                return date_generated;
            case "creator":
                return creator;
            case "extra":
                return extra;
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
            /*result.append(URLEncoder.encode("quote_id","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(quote_id), "UTF-8"));*/
            result.append("&" + URLEncoder.encode("job_id","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(job_id), "UTF-8"));
            if(date_generated>0)
                result.append("&" + URLEncoder.encode("date_generated","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_generated), "UTF-8"));
            result.append("&" + URLEncoder.encode("creator","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(creator), "UTF-8"));
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
