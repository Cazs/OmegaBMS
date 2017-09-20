/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Native;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 *
 * @author ghost
 */
public class Job implements BusinessObject, Serializable
{
    private String _id;
    //private String job_name;
    //private String job_description;
    //private String client_id;
    private long planned_start_date;
    private long date_logged;
    private long date_assigned;
    private long date_started;
    private long date_completed;
    private long job_number;
    private String invoice_id;
    private String quote_id;
    private boolean marked;
    private Quote quote;
    private Employee[] assigned_employees;
    private FileMetadata[] safety_catalogue;
    //private Client client;//Client requesting the Job
    //private Comment[] employee_comments;
    //private Comment[] clients_comments;
    //private Invoice invoice;
    //private Resource[] resources_used;
    
    /*public Job(String _id, String job_name, String job_description, String client_id, long date_logged, long date_assigned, long date_started, long date_completed, String invoice_id, boolean job_completed)
    {
        this._id = _id;
        this.job_name = job_name;//new SimpleStringProperty(job_name);
        this.job_description = job_description;
        this.client_id = client_id;
        this.date_logged = date_logged;
        this.date_assigned = date_assigned;
        this.date_started = date_started;
        this.date_completed = date_completed;
        this.invoice_id = invoice_id;
        this.job_completed = job_completed;
    }*/

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

    //public StringProperty planned_start_dateProperty(){return new SimpleStringProperty(String.valueOf(planned_start_date));}

    public long getPlanned_start_date() {return planned_start_date;}

    public void setPlanned_start_date(long planned_start_date) {this.planned_start_date = planned_start_date;}

    public long getDate_logged() 
    {
        return date_logged;
    }

    public void setDate_logged(long date_logged) 
    {
        this.date_logged = date_logged;
    }
    
    /*public StringProperty date_loggedProperty()
    {
        return new SimpleStringProperty(String.valueOf(date_logged));
    }*/

    public StringProperty job_numberProperty()
    {
        return new SimpleStringProperty(String.valueOf(job_number));
    }

    public long getJob_number()
    {
        return job_number;
    }

    public void setJob_number(long job_number)
    {
        this.job_number = job_number;
    }

    public Employee[] getAssigned_employees()
    {
        return assigned_employees;
    }

    public void setAssigned_employees(Employee[] employees)
    {
        this.assigned_employees=employees;
    }

    public void setAssigned_employees(ArrayList<Employee> reps)
    {
        this.assigned_employees = new Employee[reps.size()];
        for(int i=0;i<reps.size();i++)
        {
            this.assigned_employees[i] = reps.get(i);
        }
    }

    public StringProperty assigned_employeesProperty()
    {
        String s="";
        for(Employee e: assigned_employees)
            s += e.getFirstname() + " " + e.getLastname() + ",";
        return new SimpleStringProperty(s.substring(0,s.length()-1));
    }

    public FileMetadata[] getSafety_catalogue()
    {
        return safety_catalogue;
    }

    public void setSafety_catalogue(FileMetadata[] safety_catalogue)
    {
        this.safety_catalogue=safety_catalogue;
    }

    public StringProperty safety_catalogueProperty()
    {
        String s="";
        for(FileMetadata file: safety_catalogue)
            s += file.getIndex() + " : " + file.getLabel() + ",";
        return new SimpleStringProperty(s.substring(0,s.length()-1));
    }


    public long getDate_assigned() 
    {
        return date_assigned;
    }

    public void setDate_assigned(long date_assigned) 
    {
        this.date_assigned = date_assigned;
    }
    
    /*public StringProperty date_assignedProperty()
    {
        return new SimpleStringProperty(String.valueOf(date_assigned));
    }*/

    public long getDate_started() 
    {
        return date_started;
    }

    public void setDate_started(long date_started) 
    {
        this.date_started = date_started;
    }
    
    /*public StringProperty date_startedProperty()
    {
        return new SimpleStringProperty(String.valueOf(date_started));
    }*/

    public long getDate_completed() 
    {
        return date_completed;
    }

    public void setDate_completed(long date_completed) 
    {
        this.date_completed = date_completed;
    }
    
    /*public StringProperty date_completedProperty()
    {
        return new SimpleStringProperty(String.valueOf(date_completed));
    }*/

    public boolean isJob_completed()
    {
        return (date_completed>0);
    }

    /*public void setJob_completed(boolean executed)
    {
        this.job_completed = executed;
    }
    
    public StringProperty job_completedProperty()
    {
        return new SimpleStringProperty(String.valueOf(job_completed));
    }*/

    public StringProperty quote_idProperty()
    {
        return new SimpleStringProperty(quote_id);
    }

    public String getQuote_id()
    {
        return quote_id;
    }

    public void setQuote_id(String quote_id)
    {
        this.quote_id = quote_id;
    }

    public Quote getQuote()
    {
        return quote;
    }

    public void setQuote(Quote quote)
    {
        this.quote = quote;
    }

    public StringProperty invoice_idProperty()
    {
        return new SimpleStringProperty(invoice_id);
    }

    public String getInvoice_id() 
    {
        return invoice_id;
    }

    public void setInvoice_id(String invoice_id) 
    {
        this.invoice_id = invoice_id;
    }

    public StringProperty job_descriptionProperty()
    {
        if(quote!=null)
            return new SimpleStringProperty(quote.getRequest());
        else return new SimpleStringProperty("n/a");
    }

    public StringProperty client_nameProperty()
    {
        if(quote!=null)
            if(quote.getClient()!=null)
                return new SimpleStringProperty(quote.getClient().getClient_name());
            else return new SimpleStringProperty("n/a");
        else return new SimpleStringProperty("n/a");
    }

    public StringProperty sitenameProperty()
    {
        if(quote!=null)
            return new SimpleStringProperty(quote.getSitename());
        else return new SimpleStringProperty("n/a");
    }

    public StringProperty contact_personProperty()
    {
        if(quote!=null)
            if(quote.getContactPerson()!=null)
                return new SimpleStringProperty(quote.getContactPerson().toString());
            else return new SimpleStringProperty("n/a");
        else return new SimpleStringProperty("n/a");
    }

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "quote_id":
                    quote_id = (String)val;
                    break;
                case "planned_start_date":
                    planned_start_date = Long.parseLong(String.valueOf(val));
                    break;
                case "date_logged":
                    date_logged = Long.parseLong(String.valueOf(val));
                    break;
                case "date_assigned":
                    date_assigned = Long.parseLong(String.valueOf(val));
                    break;
                case "date_started":
                    date_started = Long.parseLong(String.valueOf(val));
                    break;
                case "date_completed":
                    date_completed = Long.parseLong(String.valueOf(val));
                    break;
                case "job_number":
                    job_number = Long.parseLong(String.valueOf(val));
                    break;
                case "invoice_id":
                    invoice_id = (String)val;
                    break;
                case "assigned_employees":
                    if(val!=null)
                        assigned_employees = (Employee[]) val;
                    else IO.log(getClass().getName(), IO.TAG_WARN, "value to be casted to Employee[] is null.");
                    break;
                case "safety_catalogue":
                    if(val!=null)
                        safety_catalogue = (FileMetadata[]) val;
                    else IO.log(getClass().getName(), IO.TAG_WARN, "value to be casted to FileMetadata[] is null.");
                    break;
                default:
                    System.err.println("Unknown Job attribute '" + var + "'.");
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
            case "quote_id":
                return quote_id;
            case "planned_start_date":
                return planned_start_date;
            case "date_logged":
                return date_logged;
            case "date_assigned":
                return date_assigned;
            case "date_started":
                return date_started;
            case "date_completed":
                return date_completed;
            case "invoice_id":
                return invoice_id;
            case "assigned_employees":
                return assigned_employees;
            case "safety_catalogue":
                return safety_catalogue;
            case "job_number":
                return job_number;
            default:
                System.err.println("Unknown Job attribute '" + var + "'.");
                return null;
        }
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/job";
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            /*result.append(URLEncoder.encode("job_number","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(job_number), "UTF-8"));*/
            result.append(URLEncoder.encode("quote_id","UTF-8") + "="
                    + URLEncoder.encode(quote_id, "UTF-8"));
            if(date_logged>0)
                result.append(URLEncoder.encode("date_logged","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_logged), "UTF-8"));
            result.append("&" + URLEncoder.encode("date_assigned","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(date_assigned), "UTF-8"));
            result.append("&" + URLEncoder.encode("planned_start_date","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(planned_start_date), "UTF-8"));
            result.append("&" + URLEncoder.encode("date_started","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(date_started), "UTF-8"));
            result.append("&" + URLEncoder.encode("date_completed","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(date_completed), "UTF-8"));
            if(invoice_id!=null)
                result.append("&" + URLEncoder.encode("invoice_id","UTF-8") + "="
                        + URLEncoder.encode(invoice_id, "UTF-8"));
            /*if(assigned_employees!=null)
                result.append("&" + URLEncoder.encode("assigned_employees","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(assigned_employees.length), "UTF-8"));
            if(safety_catalogue!=null)
                result.append("&" + URLEncoder.encode("safety_catalogue","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(safety_catalogue.length), "UTF-8"));*/

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
        return "Job #"+job_number;
    }
    /*public Employee[] getAssignedEmployees() 
    {
        return assigned_employees;
    }

    public void setAssignedEmployees(Employee[] assigned_employees) 
    {
        this.assigned_employees = assigned_employees;
    }

    public Client getClient() 
    {
        return client;
    }

    public void setClient(Client client) 
    {
        this.client = client;
    }

    public Person getClientRep() 
    {
        return client_rep;
    }

    public void setClientRep(Person client_rep) 
    {
        this.client_rep = client_rep;
    }
    
    public Comment[] getEmployeeComments() 
    {
        return employee_comments;
    }

    public void setEmployeeComments(Comment[] employee_comments) 
    {
        this.employee_comments = employee_comments;
    }

    public Comment[] getClientsComments() 
    {
        return clients_comments;
    }

    public void setClientsComments(Comment[] clients_comments) 
    {
        this.clients_comments = clients_comments;
    }

    public Invoice getInvoice() 
    {
        return invoice;
    }

    public void setInvoice(Invoice invoice) 
    {
        this.invoice = invoice;
    }

    public Resource[] getResourcesUsed() 
    {
        return resources_used;
    }

    public void setResourcesUsed(Resource[] resources_used) 
    {
        this.resources_used = resources_used;
    }*/
}
