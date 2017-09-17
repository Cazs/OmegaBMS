package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.model.*;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by ghost on 2017/01/21.
 */
public class QuoteManager extends BusinessObjectManager
{
    private Quote[] quotes= null;
    private BusinessObject[] genders=null, domains=null;
    private Gson gson;
    private static QuoteManager quote_manager = new QuoteManager();
    private ScreenManager screenManager = null;
    private Quote selected_quote;
    private boolean fromGeneric = false;
    private long timestamp;
    public static final String ROOT_PATH = "cache/quotes/";
    public String filename = "";

    private QuoteManager()
    {
    }

    public static QuoteManager getInstance()
    {
        return quote_manager;
    }

    @Override
    public void initialize(ScreenManager screenManager)
    {
        this.screenManager = screenManager;
        //init genders
        BusinessObject male = new Gender();
        male.set_id("male");
        male.parse("gender", "male");

        BusinessObject female = new Gender();
        female.set_id("female");
        female.parse("gender", "female");

        genders = new BusinessObject[]{male, female};

        //init domains
        BusinessObject internal = new Domain();
        internal.set_id("true");
        internal.parse("domain", "internal");

        BusinessObject external = new Domain();
        external.set_id("false");
        external.parse("domain", "external");

        domains = new BusinessObject[]{internal, external};

        loadDataFromServer();

        /*organisations = new BusinessObject[clients.length + suppliers.length + 3];
        BusinessObject lbl_clients = new Client();
        lbl_clients.parse("client_name", "________________________Clients________________________");

        BusinessObject lbl_internal = new Client();
        lbl_internal.parse("client_name", "INTERNAL");
        lbl_internal.set_id("INTERNAL");

        BusinessObject lbl_suppliers = new Supplier();
        lbl_suppliers.parse("supplier_name", "________________________Suppliers________________________");

        //Prepare the list of BusinessObjects to be added to the combo boxes.
        organisations[0] = lbl_internal;
        organisations[1] = lbl_clients;
        int cursor = 1;
        for(int i=0;i<clients.length;i++)
            organisations[++cursor]=clients[i];
        organisations[++cursor] = lbl_suppliers;
        for(int i=0;i<suppliers.length;i++)
            organisations[++cursor]=suppliers[i];*/
    }

    public void setFromGeneric(boolean b) {fromGeneric=b;}

    public boolean fromGeneric(){return fromGeneric;}

    public Quote[] getQuotes()
    {
        return quotes;
    }

    public void setSelectedQuote(Quote quote)
    {
        if(quote!=null)
        {
            this.selected_quote = quote;
            IO.log(getClass().getName(), IO.TAG_INFO, "set selected quote to: " + selected_quote);
        }else IO.log(getClass().getName(), IO.TAG_ERROR, "quote to be set as selected is null.");
    }

    public void setSelectedQuote(String quote_id)
    {
        for(Quote quote : quotes)
        {
            if(quote.get_id().equals(quote_id))
            {
                setSelectedQuote(quote);
                break;
            }
        }
    }

    public Quote getSelectedQuote()
    {
        /*if(selected_quote>-1)
            return quotes[selected_quote];
        else return null;*/
        return selected_quote;
    }

    public void nullifySelected()
    {
        this.selected_quote=null;
    }

    public void loadDataFromServer()
    {
        //quotes = null;
        try
        {
            SessionManager smgr = SessionManager.getInstance();
            if(smgr.getActive()!=null)
            {
                if(!smgr.getActive().isExpired())
                {
                    gson  = new GsonBuilder().create();
                    ArrayList<AbstractMap.SimpleEntry<String,String>> headers = new ArrayList<>();
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));
                    //Get Timestamp
                    String quotes_timestamp_json = RemoteComms.sendGetRequest("/api/timestamp/quotes_timestamp", headers);
                    Counters quotes_timestamp = gson.fromJson(quotes_timestamp_json, Counters.class);
                    if(quotes_timestamp!=null)
                    {
                        timestamp = quotes_timestamp.getCount();
                        filename = "quotes_"+timestamp+".dat";
                        IO.log(QuoteManager.getInstance().getClass().getName(), IO.TAG_INFO, "Server Timestamp: "+quotes_timestamp.getCount());
                    } else {
                        IO.logAndAlert(this.getClass().getName(), "could not get valid timestamp", IO.TAG_ERROR);
                        return;
                    }

                    if(!isSerialized(ROOT_PATH+filename))
                    {
                        //Load Quotes
                        String quotes_json = RemoteComms.sendGetRequest("/api/quotes", headers);
                        quotes = gson.fromJson(quotes_json, Quote[].class);

                        ClientManager.getInstance().loadDataFromServer();
                        SupplierManager.getInstance().loadDataFromServer();
                        EmployeeManager.getInstance().loadDataFromServer();
                        ResourceManager.getInstance().loadDataFromServer();

                        if(quotes!=null)
                        {
                            if(quotes.length>0)
                            {
                                for (Quote quote : quotes)
                                {
                                    //Set Quote creator
                                    for (Employee employee : EmployeeManager.getInstance().getEmployees())
                                    {
                                        if (employee.getUsr().equals(quote.getCreator()))
                                        {
                                            quote.setCreator(employee);
                                            break;
                                        }
                                    }
                                    //Load Quote Resources
                                    String quote_item_ids_json = RemoteComms.sendGetRequest("/api/quote/resources/" + quote.get_id(), headers);
                                    if (quote_item_ids_json != null)
                                    {
                                        if (!quote_item_ids_json.equals("[]"))
                                        {
                                            QuoteItem[] quote_items = gson.fromJson(quote_item_ids_json, QuoteItem[].class);
                                            //double total=0;//compute quote total
                                            for (QuoteItem item : quote_items)
                                            {
                                                //Load QuoteItem Resources
                                                String quote_resources_json = RemoteComms.sendGetRequest("/api/resource/" + item.getResource_id(), headers);
                                                if (!quote_resources_json.equals("[]"))//if the resource exists add it to the list of the quote's resources.
                                                {
                                                    Resource resource = gson.fromJson(quote_resources_json, Resource.class);
                                                    item.setResource(resource);
                                                    item.setValue(resource.getResource_value());
                                                    item.setEquipment_name(resource.getResource_name());
                                                    item.setEquipment_description(resource.getResource_description());
                                                    item.setUnit(resource.getUnit());
                                                    double rate = (resource.getResource_value() * (item.getMarkupValue() / 100) +
                                                            resource.getResource_value()) + item.getLabourCost();
                                                    item.setRate(rate);
                                                    IO.log(getClass().getName(), IO.TAG_INFO, String.format("added Resource [%s] for QuoteItem [%s].", resource.get_id(), item.get_id()));
                                                } else
                                                    IO.log(getClass().getName(), IO.TAG_ERROR, String.format("resource '%s does not exist!", item.getResource_id()));
                                            }
                                            quote.setResources(quote_items);
                                        } else
                                            IO.log(getClass().getName(), IO.TAG_WARN, String.format("quote '%s does not have any resources.", quote.get_id()));
                                    } else
                                        IO.log(getClass().getName(), IO.TAG_WARN, String.format("quote '%s does not have any resources.", quote.get_id()));

                                    //Load Quote Representatives
                                    ArrayList<QuoteRep> quoteRepIds = new ArrayList<>();
                                    ArrayList<Employee> quoteReps;

                                    //get IDs from server
                                    String quote_rep_ids_json = RemoteComms.sendGetRequest("/api/quote/reps/" + quote.get_id(), headers);
                                    if (quote_rep_ids_json != null)
                                    {
                                        if (!quote_rep_ids_json.equals("[]"))
                                        {
                                            QuoteRep[] quote_reps = gson.fromJson(quote_rep_ids_json, QuoteRep[].class);
                                            for (QuoteRep qr : quote_reps)
                                                quoteRepIds.add(qr);

                                            //load actual Employee objects from server
                                            quoteReps = new ArrayList<>();
                                            for (QuoteRep quote_rep : quote_reps)
                                            {
                                                //send request for Employee objects in JSON format
                                                String quote_reps_json = RemoteComms.sendGetRequest("/api/employee/" + quote_rep.get("usr"), headers);
                                                if (!quote_reps_json.equals("[]") && !quote_reps_json.equals("null"))//if the resource exists add it to the list of the quote's resources.
                                                {
                                                    quoteReps.add(gson.fromJson(quote_reps_json, Employee.class));
                                                    IO.log(getClass().getName(), IO.TAG_INFO, String.format("added rep '%s' for quote '%s'.", quote_rep.get("usr"), quote_rep.get("quote_id")));
                                                } else IO.log(getClass().getName(), IO.TAG_ERROR, String.format("employee '%s' does not exist!", quote_rep.get("usr")));
                                            }
                                            quote.setRepresentatives(quoteReps);
                                            IO.log(getClass().getName(), IO.TAG_INFO, String.format("set reps for quote '%s'.", quote.get_id()));
                                        } else
                                            IO.log(getClass().getName(), IO.TAG_WARN, String.format("quote '%s does not have any representatives.", quote.get_id()));
                                    } else
                                        IO.log(getClass().getName(), IO.TAG_WARN, String.format("quote '%s does not have any representatives.", quote.get_id()));

                                    //Set Quote Client object
                                    for (Client client : ClientManager.getInstance().getClients())
                                    {
                                        if (client.get_id().equals(quote.getClient_id()))
                                        {
                                            quote.setClient(client);
                                            break;
                                        }
                                    }
                                    //Set Quote contact person[Employee] object
                                    for (Employee employee : EmployeeManager.getInstance().getEmployees())
                                    {
                                        if (employee.get_id().equals(quote.getContact_person_id()))
                                        {
                                            quote.setContactPerson(employee);
                                            break;
                                        }
                                    }
                                    //Update selected quote data
                                    if(selected_quote!=null)
                                    {
                                        if (quote.get_id().equals(selected_quote.get_id()))
                                            selected_quote = quote;
                                    }
                                }
                                IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of quotes.");
                                this.serialize(ROOT_PATH+filename, quotes);
                            }else{
                                IO.log(getClass().getName(), IO.TAG_ERROR, "no quotes found in database.");
                                IO.showMessage("No quotes", "no quotes found in database.", IO.TAG_ERROR);
                            }
                        }else{
                            IO.log(getClass().getName(), IO.TAG_ERROR, "quotes object is null.");
                            IO.showMessage("No quotes", "no quotes found in database.", IO.TAG_ERROR);
                        }
                    } else{
                        IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object ["+ROOT_PATH+filename+"] on local disk is already up-to-date.");
                        quotes = (Quote[]) this.deserialize(ROOT_PATH+filename);
                    }
                }else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
            }else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
        }catch (MalformedURLException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
            IO.showMessage("URL Error", ex.getMessage(), IO.TAG_ERROR);
        }catch (ClassNotFoundException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            IO.showMessage("ClassNotFoundException", e.getMessage(), IO.TAG_ERROR);
        }catch (IOException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
            IO.showMessage("I/O Error", ex.getMessage(), IO.TAG_ERROR);
        }
    }

    public static double computeQuoteTotal(Quote quote)
    {
        //compute total
        double total=0;
        for(QuoteItem item:  quote.getResources())
        {
            //compute additional costs for each Quote Item
            if(item.getAdditional_costs()!=null)
            {
                if(!item.getAdditional_costs().isEmpty())
                {
                    String[] costs = item.getAdditional_costs().split(";");
                    for(String str_cost:costs)
                    {
                        if(str_cost.contains("="))
                        {
                            double cost = Double.parseDouble(str_cost.split("=")[1]);
                            total+=cost;
                        }else IO.log("Quote Manager", IO.TAG_ERROR, "invalid Quote Item additional cost.");
                    }
                }
            }
            //add Quote Item rate*quantity to total
            total += item.getRateValue() * item.getQuantityValue();
        }
        return total;
    }

    public void updateQuote(Quote quote, ObservableList<QuoteItem> quoteItems, ObservableList<Employee> quoteReps)
    {
        if(quoteItems==null)
        {
            IO.logAndAlert("Invalid Quote", "Quote items list is null.", IO.TAG_ERROR);
            return;
        }
        if(quoteItems.size()<=0)
        {
            IO.logAndAlert("Invalid Quote", "Quote has no items", IO.TAG_ERROR);
            return;
        }

        if(quoteReps==null)
        {
            IO.logAndAlert("Invalid Quote", "Quote representatives list is null.", IO.TAG_ERROR);
            return;
        }
        if(quoteReps.size()<=0)
        {
            IO.logAndAlert("Invalid Quote", "Quote has no representatives", IO.TAG_ERROR);
            return;
        }

        QuoteItem[] items = new QuoteItem[quoteItems.size()];
        quoteItems.toArray(items);

        Employee[] employees = new Employee[quoteReps.size()];
        quoteReps.toArray(employees);

        updateQuote(quote, items, employees);
    }

    public void updateQuote(Quote quote, QuoteItem[] quoteItems, Employee[] quoteReps)
    {
        if (SessionManager.getInstance().getActive() == null)
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return;
        }
        if (SessionManager.getInstance().getActive().isExpired())
        {
            IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
            return;
        }

        if(quoteItems==null)
        {
            IO.logAndAlert("Invalid Quote", "Quote items list is null.", IO.TAG_ERROR);
            return;
        }
        if(quoteItems.length<=0)
        {
            IO.logAndAlert("Invalid Quote", "Quote has no items", IO.TAG_ERROR);
            return;
        }

        if(quoteReps==null)
        {
            IO.logAndAlert("Invalid Quote", "Quote representatives list is null.", IO.TAG_ERROR);
            return;
        }
        if(quoteReps.length<=0)
        {
            IO.logAndAlert("Invalid Quote", "Quote has no representatives", IO.TAG_ERROR);
            return;
        }

        //Quote selected = getSelectedQuote();
        if(quote!=null)
        {
            //prepare quote parameters
            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
                //update quote on database
                HttpURLConnection connection = RemoteComms.postData("/api/quote/update/"+quote.get_id(), quote.asUTFEncodedString(), headers);
                if (connection != null)
                {
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                    {
                        String response = IO.readStream(connection.getInputStream());
                        IO.log(getClass().getName(), IO.TAG_INFO, "updated quote[" + quote.get_id() + "]. Adding representatives and resources to quote.");

                        if (response == null)
                        {
                            IO.logAndAlert("Quote Update", "Invalid server response.", IO.TAG_ERROR);
                            return;
                        }
                        if (response.isEmpty())
                        {
                            IO.logAndAlert("Quote Update", "Invalid server response: " + response, IO.TAG_ERROR);
                            return;
                        }

                        boolean updated_all_quote_items = updateQuoteItems(quote.get_id(), quoteItems, headers);
                        boolean updated_all_quote_reps = updateQuoteReps(quote, quoteReps, headers);
                        //boolean updated_all_quote_reps = true;

                        if (updated_all_quote_items && updated_all_quote_reps)
                        {
                            IO.logAndAlert("Quote Manager","successfully updated quote[" + quote.get_id() + "].", IO.TAG_INFO);
                        } else {
                            if(!updated_all_quote_items)
                                IO.logAndAlert("Quote Update Failure", "Could not update all Quote Items for Quote["+quote.get_id()+"].", IO.TAG_INFO);
                            if(!updated_all_quote_reps)
                                IO.logAndAlert("Quote Update Failure", "Could not update all Quote Representatives for Quote["+quote.get_id()+"].", IO.TAG_INFO);
                        }
                    } else
                    {
                        //Get error message
                        String msg = IO.readStream(connection.getErrorStream());
                        IO.logAndAlert("Error " + String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                    }
                    //Close connection
                    if (connection != null)
                        connection.disconnect();
                } else IO.logAndAlert("Quote Update Failure", "Could not connect to server.", IO.TAG_ERROR);
            } catch (IOException e)
            {
                IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
            }
        }else IO.logAndAlert("Update Quote","Selected Quote is invalid.", IO.TAG_ERROR);
    }

    public boolean updateQuoteItems(String quote_id, QuoteItem[] quoteItems, ArrayList headers) throws IOException
    {
        if(quote_id==null || quoteItems==null || headers == null)
            return false;

        boolean all_successful = true;
        /* Update/Create QuoteItems on database */
        for (QuoteItem quoteItem : quoteItems)
        {
            if (quoteItem != null)
            {
                /*
                    if QuoteItem has an ID then it's been already
                    added to the database - then update it, else create new record on db.
                 */
                if (quoteItem.get_id() != null)
                {
                    //update quote_item
                    all_successful = updateQuoteItem(quoteItem, headers);
                } else
                {
                    //new quote_item
                    //prepare parameters for quote_item.
                    ArrayList params = new ArrayList<>();
                    params.add(new AbstractMap.SimpleEntry<>("item_number", quoteItem.getItem_number()));
                    params.add(new AbstractMap.SimpleEntry<>("resource_id", quoteItem.getResource().get_id()));
                    params.add(new AbstractMap.SimpleEntry<>("quote_id", quote_id));
                    params.add(new AbstractMap.SimpleEntry<>("markup", quoteItem.getMarkup()));
                    params.add(new AbstractMap.SimpleEntry<>("labour", quoteItem.getLabour()));
                    params.add(new AbstractMap.SimpleEntry<>("quantity", quoteItem.getQuantity()));
                    params.add(new AbstractMap.SimpleEntry<>("additional_costs", quoteItem.getAdditional_costs()));

                    all_successful = createQuoteItem(quote_id, params, headers);
                }
            } else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid[null] quote_item.");
        }
        return all_successful;
    }

    public boolean createQuoteItem(String quote_id, ArrayList params,ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "attempting to create new quote_item for quote[" + quote_id + "].");
        HttpURLConnection connection = RemoteComms.postData("/api/quote/resource/add", params, headers);

        if (connection != null)
        {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                IO.log(getClass().getName(), IO.TAG_INFO, "successfully added a new quote_item for quote["+quote_id+"].");
                //loadDataFromServer();//refresh data set
                //Close connection
                if (connection != null)
                    connection.disconnect();
                return true;
            } else
            {
                //Get error message
                String msg = IO.readStream(connection.getErrorStream());
                IO.logAndAlert("Error " + String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
            }
        }else IO.logAndAlert("New Quote Item Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        //Close connection
        if (connection != null)
            connection.disconnect();
        return false;
    }

    public boolean createQuoteItem(String quote_id, QuoteItem item, ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "attempting to create new quote_item for quote[" + quote_id + "].");
        HttpURLConnection connection = RemoteComms.postData("/api/quote/resource/add", item.asUTFEncodedString(), headers);

        if (connection != null)
        {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                IO.log(getClass().getName(), IO.TAG_INFO, "successfully added a new quote_item for quote["+quote_id+"].");
                //loadDataFromServer();//refresh data set
                //Close connection
                if (connection != null)
                    connection.disconnect();
                return true;
            } else
            {
                //Get error message
                String msg = IO.readStream(connection.getErrorStream());
                IO.logAndAlert("Error " + String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
            }
        }else IO.logAndAlert("New Quote Item Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        //Close connection
        if (connection != null)
            connection.disconnect();
        return false;
    }

    public boolean updateQuoteItem(QuoteItem quoteItem, ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        if(quoteItem!=null)
        {
            IO.log(getClass().getName(), IO.TAG_INFO, "attempting to update quote_item["+quoteItem.get_id()+"] for quote[" + quoteItem.getQuote_id() + "].");
            HttpURLConnection connection = RemoteComms.postData("/api/quote/resource/update/" + quoteItem.get_id(), quoteItem.asUTFEncodedString(), headers);
            if (connection != null)
            {
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    IO.log(getClass().getName(), IO.TAG_INFO, "successfully updated quote_item[" + quoteItem.get_id() + "] for quote[" + quoteItem.getQuote_id() + "].");
                    //Close connection
                    if (connection != null)
                        connection.disconnect();
                    return true;
                } else
                {
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.log(getClass().getName(),IO.TAG_ERROR,"Error " + String.valueOf(connection.getResponseCode()) + ":" + msg);
                }
            } else IO.logAndAlert("Quote Item Update Failure", "Could not connect to server.", IO.TAG_ERROR);
            //Close connection
            if (connection != null)
                connection.disconnect();
        }else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid[null] quote_item.");
        return false;
    }

    public boolean updateQuoteReps(Quote quote, Employee[] reps, ArrayList headers) throws IOException
    {
        if(quote==null || reps==null || headers == null)
            return false;

        boolean all_successful = true;
        /* Update/Create Quote representatives on database */
        for (Employee rep : reps)
        {
            if (rep != null)
            {
                if (rep.get_id() != null)
                {
                    //check if employee already in list of quote reps
                    boolean found=false;
                    if(quote.getRepresentatives()!=null)
                    {
                        for (Employee employee : quote.getRepresentatives())
                        {
                            if (employee.get_id().equals(rep.get_id()))
                            {
                                found = true;
                                break;
                            }
                        }
                    }
                    if(!found)
                    {
                        //new quote rep
                        //prepare parameters for quote_item.
                        ArrayList params = new ArrayList<>();
                        params.add(new AbstractMap.SimpleEntry<>("quote_id", quote.get_id()));
                        params.add(new AbstractMap.SimpleEntry<>("usr", rep.getUsr()));

                        all_successful = createQuoteRep(quote.get_id(), params, headers);
                    }else IO.log(getClass().getName(), IO.TAG_INFO, "quote representatives are up to date.");
                }
            } else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid[null] quote_item.");
        }
        return all_successful;
    }

    public boolean createQuoteRep(String quote_id, ArrayList params,ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "attempting to create new quote_rep for quote[" + quote_id + "].");
        HttpURLConnection connection = RemoteComms.postData("/api/quote/rep/add/"+quote_id, params, headers);
        if (connection != null)
        {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                IO.log(getClass().getName(), IO.TAG_INFO, "successfully added a new quote_rep for quote["+quote_id+"].");
                //loadDataFromServer();//refresh data set
                //Close connection
                if (connection != null)
                    connection.disconnect();
                return true;
            } else
            {
                //Get error message
                String msg = IO.readStream(connection.getErrorStream());
                IO.logAndAlert("Error " + String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
            }
        }else IO.logAndAlert("New Quote Representative Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        //Close connection
        if (connection != null)
            connection.disconnect();
        return false;
    }
}
