package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by ghost on 2017/01/27.
 */
public class InvoiceManager extends BusinessObjectManager
{
    private Invoice[] invoices= null;
    private BusinessObject[] genders=null, domains=null;
    private static InvoiceManager invoice_manager = new InvoiceManager();
    private ScreenManager screenManager = null;
    private Job selected_invoice;
    private Gson gson;
    public static final String ROOT_PATH = "cache/invoices/";
    public String filename = "";
    private long timestamp;
    private static final String TAG = "InvoiceManager";

    private InvoiceManager()
    {
    }

    public static InvoiceManager getInstance()
    {
        return invoice_manager;
    }

    @Override
    public void initialize(ScreenManager screenManager)
    {
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
    }

    public void loadDataFromServer()
    {
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
                    String timestamp_json = RemoteComms.sendGetRequest("/api/timestamp/invoices_timestamp", headers);
                    Counters cntr_timestamp = gson.fromJson(timestamp_json, Counters.class);
                    if(cntr_timestamp!=null)
                    {
                        timestamp = cntr_timestamp.getCount();
                        filename = "invoices_"+timestamp+".dat";
                        IO.log(this.getClass().getName(), IO.TAG_INFO, "Server Timestamp: "+timestamp);
                    }else {
                        IO.logAndAlert(this.getClass().getName(), "could not get valid timestamp", IO.TAG_ERROR);
                        return;
                    }

                    if(!isSerialized(ROOT_PATH+filename))
                    {
                        String invoices_json = RemoteComms.sendGetRequest("/api/invoices", headers);
                        invoices = gson.fromJson(invoices_json, Invoice[].class);

                        IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of invoices.");
                        this.serialize(ROOT_PATH+filename, invoices);
                    }else{
                        IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object ["+ROOT_PATH+filename+"] on local disk is already up-to-date.");
                        invoices = (Invoice[]) this.deserialize(ROOT_PATH+filename);
                    }
                }else{
                    JOptionPane.showMessageDialog(null, "Active session has expired.", "Session Expired", JOptionPane.ERROR_MESSAGE);
                }
            }else{
                JOptionPane.showMessageDialog(null, "No active sessions.", "Session Expired", JOptionPane.ERROR_MESSAGE);
            }
        } catch (MalformedURLException ex)
        {
            IO.log(TAG, IO.TAG_ERROR, ex.getMessage());
        } catch (ClassNotFoundException ex)
        {
            IO.log(TAG, IO.TAG_ERROR, ex.getMessage());
        } catch (IOException ex)
        {
            IO.log(TAG, IO.TAG_ERROR, ex.getMessage());
        }
    }

    public void generateInvoice(Job job) throws IOException
    {
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                gson  = new GsonBuilder().create();
                ArrayList<AbstractMap.SimpleEntry<String,String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));

                Invoice invoice = new Invoice();
                invoice.setCreator(smgr.getActiveEmployee().getUsr());
                invoice.setJob_id(job.get_id());

                HttpURLConnection response = RemoteComms.postData("/api/invoice/add", invoice.asUTFEncodedString(), headers);
                if(response!=null)
                {
                    if(response.getResponseCode()==HttpURLConnection.HTTP_OK)
                        IO.logAndAlert("Success", IO.readStream(response.getInputStream()), IO.TAG_INFO);
                    else IO.logAndAlert("Error", IO.readStream(response.getErrorStream()), IO.TAG_ERROR);
                }else IO.logAndAlert("Error", "Response object is null.", IO.TAG_ERROR);
            }else{
                JOptionPane.showMessageDialog(null, "Active session has expired.", "Session Expired", JOptionPane.ERROR_MESSAGE);
            }
        }else{
            JOptionPane.showMessageDialog(null, "No active sessions.", "Session Expired", JOptionPane.ERROR_MESSAGE);
        }
    }
    /*public void newWindow()
    {
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                Stage stage = new Stage();
                stage.setTitle(Globals.APP_NAME.getValue() + " - Invoices");
                stage.setMinWidth(320);
                stage.setMinHeight(340);
                //stage.setAlwaysOnTop(true);

                tblInvoices = new TableView();
                tblInvoices.setEditable(true);

                TableColumn<BusinessObject, String> invoice_id = new TableColumn<>("Invoice ID");
                invoice_id.setMinWidth(100);
                invoice_id.setCellValueFactory(new PropertyValueFactory<>("_id"));

                TableColumn<BusinessObject, String> invoice_description = new TableColumn("Invoice description");
                CustomTableViewControls.makeEditableTableColumn(invoice_description, TextFieldTableCell.forTableColumn(), 215, "invoice_description", "/api/invoice");

                TableColumn<BusinessObject, String> invoice_issuer = new TableColumn("Invoice issuer");
                CustomTableViewControls.makeComboBoxTableColumn(invoice_issuer, organisations, "issuer_org_id", label_properties, "/api/invoice", 220, true);

                TableColumn<BusinessObject, String> invoice_receiver = new TableColumn("Invoice receiver");
                CustomTableViewControls.makeComboBoxTableColumn(invoice_receiver, organisations, "receiver_org_id", label_properties, "/api/invoice", 220, true);

                TableColumn<BusinessObject, String> total_value = new TableColumn("Total cost(incl. VAT)");
                total_value.setMinWidth(100);
                total_value.setCellValueFactory(new PropertyValueFactory<>("total_value"));
                //CustomTableViewControls.makeEditableTableColumn(total_value, TextFieldTableCellOld.forTableColumn(), 80, "total_value", "/api/invoice");

                TableColumn<BusinessObject, String> ex_total_value = new TableColumn("Total cost(excl. VAT)");
                ex_total_value.setMinWidth(100);
                ex_total_value.setCellValueFactory(new PropertyValueFactory<>("ex_total_value"));
                //CustomTableViewControls.makeEditableTableColumn(ex_total_value, TextFieldTableCellOld.forTableColumn(), 80, "ex_total_value", "/api/invoice");

                TableColumn<BusinessObject, String> labour = new TableColumn("Labour");
                CustomTableViewControls.makeEditableTableColumn(labour, TextFieldTableCell.forTableColumn(), 80, "labour", "/api/invoice");

                TableColumn<BusinessObject, String> tax = new TableColumn("Tax");
                CustomTableViewControls.makeEditableTableColumn(tax, TextFieldTableCell.forTableColumn(), 80, "tax", "/api/invoice");

                TableColumn<BusinessObject, Long> request_date = new TableColumn("Request date");
                CustomTableViewControls.makeDatePickerTableColumn(request_date, "request_date", "/api/invoice");

                TableColumn<BusinessObject, Long> date_generated = new TableColumn("Date generated");
                CustomTableViewControls.makeDatePickerTableColumn(date_generated, "date_generated", "/api/invoice");

                TableColumn<BusinessObject, String> extra = new TableColumn("Extra");
                CustomTableViewControls.makeEditableTableColumn(extra, TextFieldTableCell.forTableColumn(), 80, "extra", "/api/invoice");

                //compute total values
                if(invoices!=null)
                {
                    for (Invoice invoice : invoices)
                    {
                        try
                        {
                            double total_inc_vat = 0, total_exc_vat = Double.valueOf(invoice.getLabour());
                            if (invoice.getResources() != null)
                            {
                                for (Resource resource : invoice.getResources())
                                {
                                    total_exc_vat += resource.getResource_value();//actual value
                                    total_exc_vat += resource.getResource_value()*(resource.getMarkup()/100);//with markup
                                }
                            } else
                            {
                                IO.log(TAG, IO.TAG_WARN, String.format("invoice '%s' has no resources.", invoice.get_id()));
                            }
                            invoice.setEx_total_value(total_exc_vat);

                            double vat = Double.parseDouble(invoice.getTax());
                            total_inc_vat = total_exc_vat*(vat/100) + total_exc_vat;
                            invoice.setTotal_value(total_inc_vat);
                            IO.log(TAG, IO.TAG_INFO, String.format("set totals for invoice '%s'.", invoice.get_id()));
                        }catch (NumberFormatException e)
                        {
                            IO.log(TAG, IO.TAG_ERROR, e.getMessage());
                        }
                    }
                }else IO.log(TAG, IO.TAG_ERROR, "no invoices in database.");
                ObservableList<Invoice> lst_invoices = FXCollections.observableArrayList();
                lst_invoices.addAll(invoices);

                tblInvoices.setItems(lst_invoices);
                tblInvoices.getColumns().addAll(invoice_id, invoice_description, invoice_issuer, invoice_receiver,
                        total_value, ex_total_value, labour, tax, request_date, date_generated, extra);

                MenuBar menu_bar = new MenuBar();

                Menu file = new Menu("File");
                MenuItem new_invoice = new MenuItem("New invoice");
                MenuItem save = new MenuItem("Save");
                MenuItem print = new MenuItem("Print");
                file.getItems().addAll(new_invoice, save, print);

                Menu edit = new Menu("Edit");

                Menu invoice_options = new Menu("Invoice options");
                MenuItem invoice_resources = new MenuItem("View invoice resources");
                MenuItem invoice_reps = new MenuItem("View invoice representatives");
                invoice_options.getItems().addAll(invoice_resources, invoice_reps);

                //menu item handlers
                new_invoice.setOnAction(event -> handleNewInvoice(stage));
                invoice_resources.setOnAction(event -> invoiceResources());
                invoice_reps.setOnAction(event -> invoiceReps());

                menu_bar.getMenus().addAll(file, edit, invoice_options);

                BorderPane border_pane = new BorderPane();
                border_pane.setTop(menu_bar);
                border_pane.setCenter(tblInvoices);

                stage.setOnCloseRequest(event ->
                {
                    System.out.println("Reloading local data.");
                    loadDataFromServer();

                    stage.close();
                });

                Scene scene = new Scene(border_pane);
                stage.setScene(scene);
                stage.show();
                stage.centerOnScreen();
                stage.setResizable(true);
            }else{
                JOptionPane.showMessageDialog(null, "Active session has expired.", "Session Expired", JOptionPane.ERROR_MESSAGE);
            }
        }else{
            JOptionPane.showMessageDialog(null, "No active sessions.", "Session Expired", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void handleNewInvoice(Stage parentStage)
    {
        parentStage.setAlwaysOnTop(false);
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Add new invoice");
        stage.setMinWidth(320);
        stage.setMinHeight(350);
        //stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(10);

        final TextField txt_invoice_description = new TextField();
        txt_invoice_description.setMinWidth(200);
        txt_invoice_description.setMaxWidth(Double.MAX_VALUE);
        HBox invoice_description = CustomTableViewControls.getLabelledNode("Invoice description", 200, txt_invoice_description);

        //Issuer combo box
        final ComboBox<BusinessObject> cbx_issuer_org_id = new ComboBox<>();
        cbx_issuer_org_id.setCellFactory(new Callback<ListView<BusinessObject>, ListCell<BusinessObject>>()
        {
            @Override
            public ListCell<BusinessObject> call(ListView<BusinessObject> lst_items)
            {
                return new ListCell<BusinessObject>()
                {
                    @Override
                    protected void updateItem(BusinessObject businessObject, boolean empty)
                    {
                        super.updateItem(businessObject, empty);
                        if(businessObject!=null)
                        {
                            String[] properties  = label_properties.split("\\|");
                            for (String label_property : properties)
                            {
                                String prop_val = (String) businessObject.get(label_property);
                                if (prop_val != null)
                                {
                                    setText(prop_val);
                                    System.out.println("Set label property: " + label_property + " to value: " + prop_val);
                                    //break;
                                } else
                                {
                                    if (Globals.DEBUG_WARNINGS.getValue().toLowerCase().equals("on"))
                                        System.out.println(String.format("ComboBox> warning: property '%s' on object of type '%s' is null.", label_property, businessObject.getClass().getName()));
                                }
                            }
                        }else{
                            setText("");
                            if (Globals.DEBUG_WARNINGS.getValue().toLowerCase().equals("on"))
                                System.out.println("ComboBox> warning: business object is null.");
                        }
                    }
                };
            }
        });
        cbx_issuer_org_id.setButtonCell(new ListCell<BusinessObject>()
        {
            @Override
            protected void updateItem(BusinessObject businessObject, boolean empty)
            {
                super.updateItem(businessObject, empty);
                if(businessObject!=null)
                {
                    String[] properties  = label_properties.split("\\|");
                    for (String label_property : properties)
                    {
                        String prop_val = (String) businessObject.get(label_property);
                        if (prop_val != null)
                        {
                            setText(prop_val);
                            break;
                        } else
                        {
                            if (Globals.DEBUG_WARNINGS.getValue().toLowerCase().equals("on"))
                                System.out.println(String.format("ComboBox> warning: property '%s' on object of type '%s' is null.", label_property, businessObject.getClass().getName()));
                        }
                    }
                }else{
                    setText("");
                    if (Globals.DEBUG_WARNINGS.getValue().toLowerCase().equals("on"))
                        System.out.println("ComboBox> warning: business object is null.");
                }
            }
        });
        cbx_issuer_org_id.setItems(FXCollections.observableArrayList(organisations));
        cbx_issuer_org_id.setMinWidth(200);
        cbx_issuer_org_id.setMaxWidth(Double.MAX_VALUE);
        HBox issuer_org_id = CustomTableViewControls.getLabelledNode("Issuer", 200, cbx_issuer_org_id);

        //Receiver combo box
        final ComboBox<BusinessObject> cbx_receiver_org_id = new ComboBox<>();
        cbx_receiver_org_id.setCellFactory(new Callback<ListView<BusinessObject>, ListCell<BusinessObject>>()
        {
            @Override
            public ListCell<BusinessObject> call(ListView<BusinessObject> lst_items)
            {
                return new ListCell<BusinessObject>()
                {
                    @Override
                    protected void updateItem(BusinessObject businessObject, boolean empty)
                    {
                        super.updateItem(businessObject, empty);
                        if(businessObject!=null)
                        {
                            String[] properties  = label_properties.split("\\|");
                            for (String label_property : properties)
                            {
                                String prop_val = (String) businessObject.get(label_property);
                                if (prop_val != null)
                                {
                                    setText(prop_val);
                                    break;
                                } else
                                {
                                    if (Globals.DEBUG_WARNINGS.getValue().toLowerCase().equals("on"))
                                        System.out.println(String.format("ComboBox> warning: property '%s' on object of type '%s' is null.", label_property, businessObject.getClass().getName()));
                                }
                            }
                        }else{
                            setText("");
                            if (Globals.DEBUG_WARNINGS.getValue().toLowerCase().equals("on"))
                                System.out.println("ComboBox> warning: business object is null.");
                        }
                    }
                };
            }
        });
        cbx_receiver_org_id.setButtonCell(new ListCell<BusinessObject>()
        {
            @Override
            protected void updateItem(BusinessObject businessObject, boolean empty)
            {
                super.updateItem(businessObject, empty);
                if(businessObject!=null)
                {
                    String[] properties  = label_properties.split("\\|");
                    for (String label_property : properties)
                    {
                        String prop_val = (String) businessObject.get(label_property);
                        if (prop_val != null)
                        {
                            setText(prop_val);
                            break;
                        } else
                        {
                            if (Globals.DEBUG_WARNINGS.getValue().toLowerCase().equals("on"))
                                System.out.println(String.format("ComboBox> warning: property '%s' on object of type '%s' is null.", label_property, businessObject.getClass().getName()));
                        }
                    }
                }else{
                    setText("");
                    if (Globals.DEBUG_WARNINGS.getValue().toLowerCase().equals("on"))
                        System.out.println("ComboBox> warning: business object is null.");
                }
            }
        });
        cbx_receiver_org_id.setItems(FXCollections.observableArrayList(organisations));
        cbx_receiver_org_id.setMinWidth(200);
        cbx_receiver_org_id.setMaxWidth(Double.MAX_VALUE);
        HBox receiver_org_id = CustomTableViewControls.getLabelledNode("Receiver", 200, cbx_receiver_org_id);

        final TextField txt_labour = new TextField();
        txt_labour.setMinWidth(200);
        txt_labour.setMaxWidth(Double.MAX_VALUE);
        HBox labour = CustomTableViewControls.getLabelledNode("Labour cost", 200, txt_labour);

        final TextField txt_tax = new TextField();
        txt_tax.setMinWidth(200);
        txt_tax.setMaxWidth(Double.MAX_VALUE);
        HBox tax = CustomTableViewControls.getLabelledNode("Tax", 200, txt_tax);

        DatePicker dpk_request_date = new DatePicker();
        dpk_request_date.setMinWidth(200);
        dpk_request_date.setMaxWidth(Double.MAX_VALUE);
        HBox request_date = CustomTableViewControls.getLabelledNode("Request Date", 200, dpk_request_date);

        DatePicker dpk_date_generated = new DatePicker();
        dpk_date_generated.setMinWidth(200);
        dpk_date_generated.setMaxWidth(Double.MAX_VALUE);
        HBox date_generated = CustomTableViewControls.getLabelledNode("Date generated", 200, dpk_date_generated);
        HBox submit;

        final TextField txt_extra = new TextField();
        txt_extra.setMinWidth(200);
        txt_extra.setMaxWidth(Double.MAX_VALUE);
        HBox extra = CustomTableViewControls.getLabelledNode("Extra", 200, txt_extra);

        submit = CustomTableViewControls.getSpacedButton("Submit", event ->
        {
            String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            if(!Validators.isValidNode(txt_invoice_description, txt_invoice_description.getText(), 1, ".+"))
                return;
            if(cbx_issuer_org_id.getValue()!=null)
            {
                if (!Validators.isValidNode(cbx_issuer_org_id, cbx_issuer_org_id.getValue().get_id() == null ? "" : cbx_issuer_org_id.getValue().get_id(), 1, ".+"))
                    return;
            }
            else
            {
                Validators.isValidNode(cbx_issuer_org_id, "", 1, ".+");
                return;
            }

            if(cbx_receiver_org_id.getValue()!=null)
            {
                if (!Validators.isValidNode(cbx_receiver_org_id, cbx_receiver_org_id.getValue().get_id() == null ? "" : cbx_receiver_org_id.getValue().get_id(), 1, ".+"))
                    return;
            }
            else
            {
                Validators.isValidNode(cbx_receiver_org_id, "", 1, ".+");
                return;
            }

            if(!Validators.isValidNode(txt_labour, txt_labour.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_tax, txt_tax.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(dpk_request_date, dpk_request_date.getValue()==null?"":dpk_request_date.getValue().toString(), 4, date_regex))
                return;
            if(!Validators.isValidNode(dpk_date_generated, dpk_date_generated.getValue()==null?"":dpk_date_generated.getValue().toString(), 4, date_regex))
                return;

            long date_requested_in_sec=0, date_generated_in_sec=0;
            String str_invoice_description = txt_invoice_description.getText();
            String str_issuer_id = cbx_issuer_org_id.getValue().get_id();
            String str_receiver_id = cbx_receiver_org_id.getValue().get_id();
            String str_labour = txt_labour.getText();
            String str_tax = txt_tax.getText();
            if(dpk_request_date.getValue()!=null)
                date_requested_in_sec = dpk_request_date.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            if(dpk_date_generated.getValue()!=null)
                date_generated_in_sec = dpk_date_generated.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            String str_extra = txt_extra.getText();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            //Mandatory
            params.add(new AbstractMap.SimpleEntry<>("invoice_description", str_invoice_description));
            params.add(new AbstractMap.SimpleEntry<>("issuer_org_id", str_issuer_id));
            params.add(new AbstractMap.SimpleEntry<>("receiver_org_id", str_receiver_id));
            params.add(new AbstractMap.SimpleEntry<>("labour", str_labour));
            params.add(new AbstractMap.SimpleEntry<>("tax", str_tax));
            params.add(new AbstractMap.SimpleEntry<>("request_date", String.valueOf(date_requested_in_sec)));
            params.add(new AbstractMap.SimpleEntry<>("date_generated", String.valueOf(date_generated_in_sec)));
            //Optional
            if(str_extra!=null)
                params.add(new AbstractMap.SimpleEntry<>("extra", str_extra));

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

                HttpURLConnection connection = RemoteComms.postData("/api/invoice/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        JOptionPane.showMessageDialog(null, "Successfully added new invoice!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
        vbox.getChildren().add(invoice_description);
        vbox.getChildren().add(issuer_org_id);
        vbox.getChildren().add(receiver_org_id);
        vbox.getChildren().add(labour);
        vbox.getChildren().add(tax);
        vbox.getChildren().add(request_date);
        vbox.getChildren().add(date_generated);
        vbox.getChildren().add(extra);
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
    }*/
}
