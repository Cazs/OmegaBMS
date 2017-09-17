package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.Counters;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.model.Employee;
import fadulousbms.model.GenericQuote;
import fadulousbms.model.GenericQuoteItem;
import fadulousbms.model.Quote;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by ghost on 2017/09/15.
 */
public class GenericQuoteManager extends BusinessObjectManager
{
    private GenericQuote[] generic_quotes= null;
    private static GenericQuoteManager generic_quote_manager = new GenericQuoteManager();
    private ScreenManager screenManager = null;
    private GenericQuote selected_generic_quote;
    private Gson gson;
    private long timestamp;
    public static final String ROOT_PATH = "cache/quotes/";
    public String filename = "";

    public static GenericQuoteManager getInstance()
    {
        return generic_quote_manager;
    }

    @Override
    public void initialize(ScreenManager screenManager)
    {
        loadDataFromServer();
    }

    public void loadDataFromServer()
    {
        //generic_quotes = null;
        try
        {
            SessionManager smgr = SessionManager.getInstance();
            if (smgr.getActive() != null)
            {
                if (!smgr.getActive().isExpired())
                {
                    EmployeeManager.getInstance().initialize(screenManager);
                    gson = new GsonBuilder().create();
                    ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));
                    //Get Timestamp
                    String quotes_timestamp_json = RemoteComms.sendGetRequest("/api/timestamp/generic_quotes_timestamp", headers);
                    Counters quotes_timestamp = gson.fromJson(quotes_timestamp_json, Counters.class);
                    if (quotes_timestamp != null)
                    {
                        timestamp = quotes_timestamp.getCount();
                        filename = "generic_quotes_"+timestamp+".dat";
                        IO.log(GenericQuoteManager.getInstance().getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + quotes_timestamp.getCount());
                    } else {
                        IO.logAndAlert(this.getClass().getName(), "could not get valid timestamp", IO.TAG_ERROR);
                        return;
                    }

                    if (!this.isSerialized(ROOT_PATH+filename))
                    {
                        //Load Generic Quotes
                        String generic_quotes_json = RemoteComms.sendGetRequest("/api/quotes/generic", headers);
                        generic_quotes = gson.fromJson(generic_quotes_json, GenericQuote[].class);
                        //Load Generic Quotes Resources/Items
                        if (generic_quotes != null)
                        {
                            for (GenericQuote quote : generic_quotes)
                            {
                                //Get quote resources
                                String generic_quote_res_json = RemoteComms.sendGetRequest("/api/quote/generic/resources/" + quote.get_id(), headers);
                                GenericQuoteItem[] generic_quote_res = gson.fromJson(generic_quote_res_json, GenericQuoteItem[].class);
                                quote.setResources(generic_quote_res);

                                //Set quote creator
                                for (Employee employee : EmployeeManager.getInstance().getEmployees())
                                {
                                    if (employee.getUsr().equals(quote.getCreator()))
                                    {
                                        quote.setCreator(employee);
                                        break;
                                    }
                                }
                            }
                            IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of generic quotes.");
                            this.serialize(ROOT_PATH+filename, generic_quotes);
                        }else{
                            IO.log(getClass().getName(), IO.TAG_ERROR, "generic quotes object is null.");
                            IO.showMessage("No generic quotes", "no generic quotes found in database.", IO.TAG_ERROR);
                        }
                    }else{
                        IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object ["+ROOT_PATH+filename+"] on local disk is already up-to-date.");
                        generic_quotes = (GenericQuote[]) this.deserialize(ROOT_PATH+filename);
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

    public GenericQuote[] getGenericQuotes()
    {
        return generic_quotes;
    }

    public void setSelectedGenericQuote(GenericQuote quote)
    {
        if(quote!=null)
        {
            this.selected_generic_quote = quote;
            IO.log(getClass().getName(), IO.TAG_INFO, "set selected generic quote to: " + selected_generic_quote);
        }else IO.log(getClass().getName(), IO.TAG_ERROR, "generic quote to be set as selected is null.");
    }

    public void setSelectedGenericQuote(String quote_id)
    {
        for(GenericQuote quote : generic_quotes)
        {
            if(quote.get_id().equals(quote_id))
            {
                setSelectedGenericQuote(quote);
                break;
            }
        }
    }

    public void nullifySelected()
    {
        this.selected_generic_quote =null;
    }

    public GenericQuote getSelectedGenericQuote()
    {
        /*if(selected_quote>-1)
            return quotes[selected_quote];
        else return null;*/
        return selected_generic_quote;
    }

    public void updateGenericQuote(GenericQuote quote, ObservableList<GenericQuoteItem> quoteItems)
    {
        if(quoteItems==null)
        {
            IO.logAndAlert("Invalid Generic Quote", "Quote items list is null.", IO.TAG_ERROR);
            return;
        }
        if(quoteItems.size()<=0)
        {
            IO.logAndAlert("Invalid Generic Quote", "Generic quote has no items", IO.TAG_ERROR);
            return;
        }

        GenericQuoteItem[] items = new GenericQuoteItem[quoteItems.size()];
        quoteItems.toArray(items);

        updateGenericQuote(quote, items);
    }

    public boolean updateGenericQuote(GenericQuote quote, GenericQuoteItem[] quoteItems)
    {
        if (SessionManager.getInstance().getActive() == null)
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return false;
        }
        if (SessionManager.getInstance().getActive().isExpired())
        {
            IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
            return false;
        }

        if(quoteItems==null)
        {
            IO.logAndAlert("Invalid Generic Quote", "Generic quote items list is null.", IO.TAG_ERROR);
            return false;
        }
        if(quoteItems.length<=0)
        {
            IO.logAndAlert("Invalid Generic Quote", "Generic quote has no items", IO.TAG_ERROR);
            return false;
        }

        if(quote!=null)
        {
            //prepare generic quote parameters
            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
                //update quote on database
                HttpURLConnection connection = RemoteComms.postData("/api/quote/generic/update/"+quote.get_id(), quote.asUTFEncodedString(), headers);
                if (connection != null)
                {
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                    {
                        String response = IO.readStream(connection.getInputStream());
                        IO.log(getClass().getName(), IO.TAG_INFO, "updated generic quote[" + quote.get_id() + "]. Updating generic quote resources.");

                        if (response == null)
                        {
                            IO.logAndAlert("Generic Quote Update Error", "Invalid server response.", IO.TAG_ERROR);
                            return false;
                        }
                        if (response.isEmpty())
                        {
                            IO.logAndAlert("Generic Quote Update Error", "Invalid server response: " + response, IO.TAG_ERROR);
                            return false;
                        }
                        updateGenericQuoteItems(quote.get_id(), quoteItems, headers);
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
        return true;
    }

    public void updateGenericQuoteItems(String quote_id, GenericQuoteItem[] quoteItems, ArrayList headers) throws IOException
    {
        if(quote_id==null || quoteItems==null || headers == null)
            return;

        /* Update/Create QuoteItems on database */
        for (GenericQuoteItem quoteItem : quoteItems)
        {
            if (quoteItem != null)
            {
                /*
                    if GenericQuoteItem has an ID then it's been already
                    added to the database - then update it, else create new record on db.
                 */
                if (quoteItem.get_id() != null)
                {
                    //update quote_item
                    if(!updateGenericQuoteItem(quoteItem, headers))
                        IO.logAndAlert("Quote Manager", "could not update GenericQuoteItem:\n"+quoteItem, IO.TAG_ERROR);
                } else
                {
                    //new generic_quote_resource
                    quoteItem.setQuote_id(quote_id);
                    if(!createGenericQuoteItem(quote_id, quoteItem, headers))
                        IO.logAndAlert("Quote Manager", "could not create GenericQuoteItem:\n"+quoteItem, IO.TAG_ERROR);
                }
            } else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid[null] quote_item.");
        }
    }

    public boolean updateGenericQuoteItem(GenericQuoteItem quoteItem, ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        if(quoteItem!=null)
        {
            IO.log(getClass().getName(), IO.TAG_INFO, "attempting to update generic_quote_resource["+quoteItem.get_id()+"] for generic quote[" + quoteItem.getQuote_id() + "].");
            HttpURLConnection connection = RemoteComms.postData("/api/quote/generic/resource/update/" + quoteItem.get_id(), quoteItem.asUTFEncodedString(), headers);
            if (connection != null)
            {
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    IO.log(getClass().getName(), IO.TAG_INFO, "successfully updated generic_quote_resource[" + quoteItem.get_id() + "] for generic quote[" + quoteItem.getQuote_id() + "].");
                    //Close connection
                    if (connection != null)
                        connection.disconnect();
                    return true;
                } else
                {
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.log(getClass().getName(),IO.TAG_ERROR,"Error " + String.valueOf(connection.getResponseCode()) + ":" + msg);
                }
            } else IO.logAndAlert("Generic Quote Item Update Failure", "Could not connect to server.", IO.TAG_ERROR);
            //Close connection
            if (connection != null)
                connection.disconnect();
        }else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid[null] generic_quote_item.");
        return false;
    }

    public boolean createGenericQuoteItem(String quote_id, ArrayList params,ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "attempting to create new generic_quote_item for generic quote[" + quote_id + "].");
        HttpURLConnection connection = RemoteComms.postData("/api/quote/generic/resource/add/"+quote_id, params, headers);

        if (connection != null)
        {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                IO.log(getClass().getName(), IO.TAG_INFO, "successfully added a new generic_quote_item for generic quote["+quote_id+"].");
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
        }else IO.logAndAlert("Generic Quote Item Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        //Close connection
        if (connection != null)
            connection.disconnect();
        return false;
    }

    public boolean createGenericQuoteItem(String quote_id, GenericQuoteItem item, ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "attempting to create new generic_quote_item for generic quote[" + quote_id + "].");
        HttpURLConnection connection = RemoteComms.postData("/api/quote/generic/resource/add", item.asUTFEncodedString(), headers);

        if (connection != null)
        {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                IO.log(getClass().getName(), IO.TAG_INFO, "successfully added a new quote_item for generic quote["+quote_id+"].");
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
        }else IO.logAndAlert("New Generic Quote Item Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        //Close connection
        if (connection != null)
            connection.disconnect();
        return false;
    }
}
