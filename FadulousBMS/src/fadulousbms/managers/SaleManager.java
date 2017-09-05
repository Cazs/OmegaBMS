package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Validators;
import fadulousbms.controllers.HomescreenController;
import fadulousbms.controllers.OperationsController;
import fadulousbms.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ghost on 2017/01/18.
 */
public class SaleManager implements BusinessObjectManager
{
    private Sale[] sales= null;
    private Client[] clients = null;
    private Quote[] quotes= null;
    private Invoice[] invoices= null;
    private TableView tblSales;
    private Gson gson;
    private static SaleManager sale_manager = new SaleManager();
    public static final String TAG = "SaleManager";

    private SaleManager()
    {
    }

    public static SaleManager getInstance()
    {
        return sale_manager;
    }

    public Sale[] getSales(){return sales;}

    @Override
    public void initialize(ScreenManager screenManager)
    {
        loadDataFromServer();
    }

    public void loadDataFromServer()
    {
        sales = null;
        clients = null;
        invoices = null;
        quotes = null;
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

                    String sales_json = RemoteComms.sendGetRequest("/api/sales", headers);
                    sales = gson.fromJson(sales_json, Sale[].class);

                    QuoteManager.getInstance().loadDataFromServer();//refresh collection of quotes
                    ClientManager.getInstance().loadDataFromServer();//refresh collection of clients

                    for(Sale sale : sales)
                    {
                        //Set Quote for each Sale
                        for(Quote quote : QuoteManager.getInstance().getQuotes())
                        {
                            if(sale.getQuote_id().equals(quote.get_id()))
                            {
                                sale.setQuote(quote);
                                break;
                            }
                        }
                        //Set Creator for each Sale
                        for(Employee employee : EmployeeManager.getInstance().getEmployees())
                        {
                            if(employee.getUsr().equals(sale.getCreator()))
                            {
                                sale.setCreatorEmployee(employee);
                                break;
                            }
                        }
                    }

                    IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of sales.");
                }else{
                    JOptionPane.showMessageDialog(null, "Active session has expired.", "Session Expired", JOptionPane.ERROR_MESSAGE);
                }
            }else{
                JOptionPane.showMessageDialog(null, "No active sessions.", "Session Expired", JOptionPane.ERROR_MESSAGE);
            }
        }catch (MalformedURLException ex)
        {
            IO.log(TAG, IO.TAG_ERROR, ex.getMessage());
        }catch (IOException ex)
        {
            IO.log(TAG, IO.TAG_ERROR, ex.getMessage());
        }
    }

    @Override
    public void newWindow()
    {
    }

    public boolean createNewSale(String quote_id)
    {
        //prepare sale parameters
        ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
        //Mandatory
        params.add(new AbstractMap.SimpleEntry<>("quote_id", quote_id));
        params.add(new AbstractMap.SimpleEntry<>("creator", SessionManager.getInstance().getActive().getUser()));
        //params.add(new AbstractMap.SimpleEntry<>("revision", "1.0"));
        //Optional
        /*if(str_extra!=null)
            params.add(new AbstractMap.SimpleEntry<>("extra", str_extra));*/

        try
        {
            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));

            //create new sale on database
            HttpURLConnection connection = RemoteComms.postData("/api/sale/add", params, headers);
            if(connection!=null)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());
                    IO.log(getClass().getName(), IO.TAG_INFO, "successfully created a new sale: " + response);
                    //IO.logAndAlert("Sales Manager", "Successfully created a new sale.", IO.TAG_INFO);
                    if(connection!=null)
                        connection.disconnect();
                    return true;
                }else
                {
                    //Get error message
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                    if(connection!=null)
                        connection.disconnect();
                    return false;
                }
            }else IO.logAndAlert("Sale Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.logAndAlert("Sales Manager", e.getMessage(), IO.TAG_ERROR);
        }
        return false;
    }

    public void handleNewSale(Stage parentStage)
    {
        parentStage.setAlwaysOnTop(false);
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Add New Sale");
        stage.setMinWidth(320);
        stage.setMinHeight(220);
        //stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(10);

        final TextField txt_sale_description = new TextField();
        txt_sale_description.setMinWidth(200);
        txt_sale_description.setMaxWidth(Double.MAX_VALUE);
        HBox sale_description = CustomTableViewControls.getLabelledNode("Sale description", 200, txt_sale_description);

        final ComboBox<Client> cbx_client_id = new ComboBox<>();
        cbx_client_id.setCellFactory(new Callback<ListView<Client>, ListCell<Client>>()
        {
            @Override
            public ListCell<Client> call(ListView<Client> lst_clients)
            {
                return new ListCell<Client>()
                {
                    @Override
                    protected void updateItem(Client client, boolean empty)
                    {
                        super.updateItem(client, empty);
                        if(client!=null && !empty)
                        {
                            setText(client.getClient_name());
                        }else{
                            setText("");
                        }
                    }
                };
            }
        });
        cbx_client_id.setButtonCell(new ListCell<Client>()
        {
            @Override
            protected void updateItem(Client client, boolean empty)
            {
                super.updateItem(client, empty);
                if(client!=null && !empty)
                {
                    setText(client.getClient_name());
                }else
                {
                    setText("");
                }
            }
        });
        cbx_client_id.setItems(FXCollections.observableArrayList(clients));
        cbx_client_id.setMinWidth(200);
        cbx_client_id.setMaxWidth(Double.MAX_VALUE);
        HBox client_id = CustomTableViewControls.getLabelledNode("Client", 200, cbx_client_id);

        final ComboBox<Quote> cbx_quote_id = new ComboBox<>();
        cbx_quote_id.setCellFactory(new Callback<ListView<Quote>, ListCell<Quote>>()
        {
            @Override
            public ListCell<Quote> call(ListView<Quote> lst_quotes)
            {
                return new ListCell<Quote>()
                {
                    @Override
                    protected void updateItem(Quote quote, boolean empty)
                    {
                        super.updateItem(quote, empty);
                        if(quote!=null && !empty)
                        {
                            setText(quote.get_id());
                        }else{
                            setText("");
                        }
                    }
                };
            }
        });
        cbx_quote_id.setButtonCell(new ListCell<Quote>()
        {
            @Override
            protected void updateItem(Quote quote, boolean empty)
            {
                super.updateItem(quote, empty);
                if(quote!=null && !empty)
                {
                    setText(quote.get_id());
                }else
                {
                    setText("");
                }
            }
        });
        cbx_quote_id.setItems(FXCollections.observableArrayList(quotes));
        cbx_quote_id.setMinWidth(200);
        cbx_quote_id.setMaxWidth(Double.MAX_VALUE);
        HBox quote_id = CustomTableViewControls.getLabelledNode("Quote ID", 200, cbx_quote_id);

        final ComboBox<Invoice> cbx_invoice_id = new ComboBox<>();
        cbx_invoice_id.setCellFactory(new Callback<ListView<Invoice>, ListCell<Invoice>>()
        {
            @Override
            public ListCell<Invoice> call(ListView<Invoice> lst_invoices)
            {
                return new ListCell<Invoice>()
                {
                    @Override
                    protected void updateItem(Invoice invoice, boolean empty)
                    {
                        super.updateItem(invoice, empty);
                        if(invoice!=null && !empty)
                        {
                            setText(invoice.getShort_id());
                        }else{
                            setText("");
                        }
                    }
                };
            }
        });
        cbx_invoice_id.setButtonCell(new ListCell<Invoice>()
        {
            @Override
            protected void updateItem(Invoice invoice, boolean empty)
            {
                super.updateItem(invoice, empty);
                if(invoice!=null && !empty)
                {
                    setText(invoice.getShort_id());
                }else
                {
                    setText("");
                }
            }
        });
        cbx_invoice_id.setItems(FXCollections.observableArrayList(invoices));
        cbx_invoice_id.setMinWidth(200);
        cbx_invoice_id.setMaxWidth(Double.MAX_VALUE);
        HBox invoice_id = CustomTableViewControls.getLabelledNode("Invoice ID", 200, cbx_invoice_id);

        DatePicker dpk_date_logged = new DatePicker();
        dpk_date_logged.setMinWidth(200);
        dpk_date_logged.setMaxWidth(Double.MAX_VALUE);
        HBox date_logged = CustomTableViewControls.getLabelledNode("Date logged", 200, dpk_date_logged);

        /*final TextField txt_invoice_id = new TextField();
        txt_invoice_id.setMinWidth(200);
        txt_invoice_id.setMaxWidth(Double.MAX_VALUE);
        HBox invoice_id = getLabelledNode("Invoice", 200, txt_invoice_id);*/

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Submit", event ->
        {
            String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            if(!Validators.isValidNode(txt_sale_description, txt_sale_description.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(cbx_client_id, cbx_client_id.getValue()==null?"":cbx_client_id.getValue().get_id(), 1, ".+"))
                return;
            if(!Validators.isValidNode(cbx_quote_id, cbx_quote_id.getValue()==null?"":cbx_quote_id.getValue().get_id(), 1, ".+"))
                return;
            if(!Validators.isValidNode(cbx_invoice_id, cbx_invoice_id.getValue()==null?"":cbx_invoice_id.getValue().get_id(), 1, ".+"))
                return;
            if(!Validators.isValidNode(dpk_date_logged, dpk_date_logged.getValue()==null?"":dpk_date_logged.getValue().toString(), 4, date_regex))
                return;

            long date_logged_in_sec=0;
            String str_sale_description = txt_sale_description.getText();
            String str_client_id = cbx_client_id.getValue().get_id();
            String str_quote_id = cbx_quote_id.getValue().get_id();
            String str_invoice_id = cbx_invoice_id.getValue().get_id();
            if(dpk_date_logged.getValue()!=null)
                date_logged_in_sec = dpk_date_logged.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            //Mandatory
            params.add(new AbstractMap.SimpleEntry<>("sale_description", str_sale_description));
            params.add(new AbstractMap.SimpleEntry<>("client_id", str_client_id));
            params.add(new AbstractMap.SimpleEntry<>("date_logged", String.valueOf(date_logged_in_sec)));
            params.add(new AbstractMap.SimpleEntry<>("quote_id", String.valueOf(str_quote_id)));
            params.add(new AbstractMap.SimpleEntry<>("invoice_id", String.valueOf(str_invoice_id)));
            //Optional

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                if(SessionManager.getInstance().getActive()!=null)
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
                else
                {
                    JOptionPane.showMessageDialog(null, "No active sessions.", "Session expired", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                HttpURLConnection connection = RemoteComms.postData("/api/sale/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        JOptionPane.showMessageDialog(null, "Successfully added new sale!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }else{
                        JOptionPane.showMessageDialog(null, connection.getResponseCode(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (IOException e)
            {
                IO.log(TAG, IO.TAG_ERROR, e.getMessage());
            }
        });
        //Add form controls vertically on the scene
        vbox.getChildren().add(sale_description);
        vbox.getChildren().add(client_id);
        vbox.getChildren().add(quote_id);
        vbox.getChildren().add(invoice_id);
        vbox.getChildren().add(date_logged);
        vbox.getChildren().add(submit);

        //Setup scene and display
        Scene scene = new Scene(vbox);
        File fCss = new File("src/fadulousbms/styles/home.css");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

        stage.setOnCloseRequest(event ->
        {
            System.out.println("Reloading local data.");
            loadDataFromServer();
        });

        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
        stage.setResizable(true);
    }
}
