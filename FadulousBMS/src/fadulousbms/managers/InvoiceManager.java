package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Validators;
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
    private Client[] clients = null;
    private Supplier[] suppliers = null;
    private Employee[] employees = null;
    private String label_properties = "client_name|supplier_name";
    private BusinessObject[] organisations=null, genders=null, domains=null;
    private TableView tblInvoices, tblInvoiceResources, tblInvoiceRepresentatives;
    private Gson gson;
    private static final String TAG = "InvoiceManager";
    private static InvoiceManager invoice_manager = new InvoiceManager();
    private Resource[] resources;
    public static ResourceType[] resource_types;

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

        organisations = new BusinessObject[clients.length + suppliers.length + 3];
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
            organisations[++cursor]=suppliers[i];
    }

    public void loadDataFromServer()
    {
        invoices = null;
        clients = null;
        suppliers = null;
        resource_types = null;

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

                    String invoices_json = RemoteComms.sendGetRequest("/api/invoices", headers);
                    invoices = gson.fromJson(invoices_json, Invoice[].class);

                    String clients_json = RemoteComms.sendGetRequest("/api/clients", headers);
                    clients = gson.fromJson(clients_json, Client[].class);

                    String suppliers_json = RemoteComms.sendGetRequest("/api/suppliers", headers);
                    suppliers = gson.fromJson(suppliers_json, Supplier[].class);

                    String employees_json = RemoteComms.sendGetRequest("/api/employees", headers);
                    employees = gson.fromJson(employees_json, Employee[].class);

                    String resources_json = RemoteComms.sendGetRequest("/api/resources", headers);
                    resources = gson.fromJson(resources_json, Resource[].class);

                    String resource_types_json = RemoteComms.sendGetRequest("/api/resource/types", headers);
                    resource_types = gson.fromJson(resource_types_json, ResourceType[].class);

                    //Load invoice resources
                    ArrayList<InvoiceResource> invoiceResourceIds;
                    ArrayList<Resource> invoiceResources;
                    if(invoices!=null)
                    {
                        if(invoices.length>0)
                        {
                            for (Invoice invoice : invoices)
                            {
                                invoiceResourceIds = new ArrayList<>();

                                String invoice_resource_ids_json = RemoteComms.sendGetRequest("/api/invoice/resources/" + invoice.get_id(), headers);
                                if(invoice_resource_ids_json!=null)
                                {
                                    if (!invoice_resource_ids_json.equals("[]"))
                                    {
                                        InvoiceResource[] invoice_resources = gson.fromJson(invoice_resource_ids_json, InvoiceResource[].class);
                                        for(InvoiceResource qr : invoice_resources)
                                            invoiceResourceIds.add(qr);

                                        invoiceResources = new ArrayList<>();
                                        for (InvoiceResource invoice_resource : invoiceResourceIds)
                                        {
                                            String invoice_resources_json = RemoteComms.sendGetRequest("/api/resource/" + invoice_resource.get("resource_id"), headers);
                                            if (!invoice_resources_json.equals("[]"))//if the resource exists add it to the list of the invoice's resources.
                                            {
                                                invoiceResources.add(gson.fromJson(invoice_resources_json, Resource.class));
                                                IO.log(TAG, IO.TAG_INFO, String.format("added resource '%s' for invoice '%s'.", invoice_resource.get("resource_id"), invoice_resource.get("invoice_id")));
                                            }
                                            else
                                                IO.log(TAG, IO.TAG_ERROR, String.format("resource '%s does not exist!", invoice_resource.get("resource_id")));
                                        }
                                        invoice.setResources(invoiceResources);
                                    } else
                                        IO.log(TAG, IO.TAG_WARN, String.format("invoice '%s does not have any resources.", invoice.get_id()));
                                }else
                                    IO.log(TAG, IO.TAG_WARN, String.format("invoice '%s does not have any resources.", invoice.get_id()));
                            }
                        }else{
                            IO.log(TAG, IO.TAG_WARN, "no invoices found in database.");
                        }
                    }else{
                        IO.log(TAG, IO.TAG_WARN, "invoices object is null.");
                    }

                    //Load invoice representatives
                    ArrayList<InvoiceRep> invoiceRepIds;
                    ArrayList<Employee> invoiceReps;
                    if(invoices!=null)
                    {
                        if(invoices.length>0)
                        {
                            for (Invoice invoice : invoices)
                            {
                                invoiceRepIds = new ArrayList<>();

                                String invoice_rep_ids_json = RemoteComms.sendGetRequest("/api/invoice/reps/" + invoice.get_id(), headers);
                                if(invoice_rep_ids_json!=null)
                                {
                                    if (!invoice_rep_ids_json.equals("[]"))
                                    {
                                        InvoiceRep[] invoice_reps = gson.fromJson(invoice_rep_ids_json, InvoiceRep[].class);
                                        for(InvoiceRep qr : invoice_reps)
                                            invoiceRepIds.add(qr);

                                        invoiceReps = new ArrayList<>();
                                        for (InvoiceRep invoice_rep : invoice_reps)
                                        {
                                            String invoice_reps_json = RemoteComms.sendGetRequest("/api/employee/" + invoice_rep.get("usr"), headers);
                                            if (!invoice_reps_json.equals("[]") && !invoice_reps_json.equals("null"))//if the resource exists add it to the list of the invoice's resources.
                                            {
                                                invoiceReps.add(gson.fromJson(invoice_reps_json, Employee.class));
                                                IO.log(TAG, IO.TAG_INFO, String.format("added rep '%s' for invoice '%s'.", invoice_rep.get("usr"), invoice_rep.get("invoice_id")));
                                            }
                                            else
                                                IO.log(TAG, IO.TAG_ERROR, String.format("employee '%s' does not exist!", invoice_rep.get("usr")));
                                        }
                                        invoice.setRepresentatives(invoiceReps);
                                        IO.log(TAG, IO.TAG_INFO, String.format("set reps for invoice '%s'.", invoice.get_id()));
                                    } else
                                        IO.log(TAG, IO.TAG_WARN, String.format("invoice '%s does not have any representatives.", invoice.get_id()));
                                }else
                                    IO.log(TAG, IO.TAG_WARN, String.format("invoice '%s does not have any representatives.", invoice.get_id()));
                            }
                        }else{
                            IO.log(TAG, IO.TAG_WARN, "no invoices found in database.");
                        }
                    }else{
                        IO.log(TAG, IO.TAG_WARN, "invoices object is null.");
                    }
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

    public void newWindow()
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

    public void invoiceResources()
    {
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                Stage stage = new Stage();
                stage.setTitle(Globals.APP_NAME.getValue() + " - invoice resources");
                stage.setMinWidth(320);
                stage.setMinHeight(340);
                //stage.setAlwaysOnTop(true);

                tblInvoiceResources = new TableView();
                tblInvoiceResources.setEditable(true);

                TableColumn<BusinessObject, String> invoice_description = new TableColumn("Resource");
                CustomTableViewControls.makeEditableTableColumn(invoice_description, TextFieldTableCell.forTableColumn(), 80, "resource_name", "/api/invoice/resource");

                TableColumn<BusinessObject, String> resource_type = new TableColumn("Resource type");
                CustomTableViewControls.makeComboBoxTableColumn(resource_type, resource_types, "resource_type", "type_name", "/api/resource", 100);

                TableColumn<BusinessObject, String> resource_description = new TableColumn("Resource description");
                CustomTableViewControls.makeEditableTableColumn(resource_description, TextFieldTableCell.forTableColumn(), 80, "resource_description", "/api/invoice/resource");

                TableColumn<BusinessObject, String> resource_value = new TableColumn("Resource value[before markup]");
                CustomTableViewControls.makeEditableTableColumn(resource_value, TextFieldTableCell.forTableColumn(), 80, "resource_value", "/api/invoice/resource");

                TableColumn<BusinessObject, String> markup = new TableColumn("Markup");
                CustomTableViewControls.makeEditableTableColumn(markup, TextFieldTableCell.forTableColumn(), 80, "markup", "/api/resource");

                TableColumn<BusinessObject, Long> date_acquired = new TableColumn("Date acquired");
                CustomTableViewControls.makeDatePickerTableColumn(date_acquired, "date_acquired", "/api/invoice/resource");

                TableColumn<BusinessObject, Long> date_exhausted = new TableColumn("Date exhausted");
                CustomTableViewControls.makeDatePickerTableColumn(date_exhausted, "date_exhausted", "/api/invoice/resource");

                TableColumn<BusinessObject, String> other = new TableColumn("Other");
                CustomTableViewControls.makeEditableTableColumn(other, TextFieldTableCell.forTableColumn(), 80, "other", "/api/invoice/resource");

                MenuBar menu_bar = new MenuBar();
                Menu file = new Menu("File");
                Menu edit = new Menu("Edit");

                MenuItem new_resource = new MenuItem("New resource");
                new_resource.setOnAction(event -> handleNewInvoiceResource(stage));
                MenuItem save = new MenuItem("Save");
                MenuItem print = new MenuItem("Print");


                ObservableList<Resource> lst_invoice_resources = FXCollections.observableArrayList();
                int selected_index = tblInvoices.getSelectionModel().selectedIndexProperty().get();
                //Invoice selected_invoice = (Invoice) tblInvoices.selectionModelProperty().get();
                if(invoices!=null)
                {
                    if (selected_index >= 0 && selected_index < invoices.length)
                    {
                        if(invoices[selected_index].get("issuer_org_id")!=null)
                        {
                            for (BusinessObject businessObject : organisations)
                            {
                                if(businessObject.get_id()!=null)
                                {
                                    if (businessObject.get_id().equals(invoices[selected_index].get("issuer_org_id")))
                                    {
                                        if (label_properties.split("\\|").length > 1)
                                        {
                                            String name = (String) businessObject.get(label_properties.split("\\|")[0]);
                                            if (name == null)
                                                name = (String) businessObject.get(label_properties.split("\\|")[1]);
                                            if (name == null)
                                                IO.log(TAG, IO.TAG_ERROR, "neither of the label_properties were found in object!");
                                            else
                                            {
                                                new_resource.setText("New resource for invoice issued by " + name);
                                                IO.log(TAG, IO.TAG_INFO, String.format("set invoice context to [invoice issued by] '%s'", name));
                                            }
                                        } else
                                        {
                                            IO.log(TAG, IO.TAG_ERROR, "label_properties split array index out of bounds!");
                                        }
                                    }
                                }else IO.log(TAG, IO.TAG_WARN, "business object id is null.");
                            }
                        }else{
                            IO.log(TAG, IO.TAG_ERROR, String.format("issuer_org_id of selected invoice '%s' is null.", invoices[selected_index].get_id()));
                        }
                        if(invoices[selected_index].getResources()!=null)
                            lst_invoice_resources.addAll(invoices[selected_index].getResources());
                        else IO.log(TAG, IO.TAG_ERROR, String.format("invoice '%s' has no resources.", invoices[selected_index].get_id()));
                    }
                    else IO.log(TAG, IO.TAG_ERROR, "invoice array index out of bounds: " + selected_index);
                }else IO.log(TAG, IO.TAG_ERROR, "invoices array is null!");

                tblInvoiceResources.setItems(lst_invoice_resources);
                tblInvoiceResources.getColumns().addAll(invoice_description, resource_type, resource_description, resource_value,
                        markup, date_acquired, date_exhausted, other);


                file.getItems().addAll(new_resource, save, print);

                menu_bar.getMenus().addAll(file, edit);

                BorderPane border_pane = new BorderPane();
                border_pane.setTop(menu_bar);
                border_pane.setCenter(tblInvoiceResources);

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

    public void handleNewInvoiceResource(Stage parentStage)
    {
        if(tblInvoices==null)
        {
            IO.log(TAG, IO.TAG_ERROR, "invoices table is null!");
            return;
        }

        if(resources==null)
        {
            IO.log(TAG, IO.TAG_ERROR, "no resources were found in the database.");
            return;
        }

        int selected_index = tblInvoices.getSelectionModel().selectedIndexProperty().get();

        if(invoices!=null)
        {
            if (selected_index < 0 || selected_index >= invoices.length)
            {
                IO.log(TAG, IO.TAG_ERROR, "invoices array index is out of bounds");
                return;
            }
        }else{
            IO.log(TAG, IO.TAG_ERROR, "invoices array is null!");
            return;
        }

        parentStage.setAlwaysOnTop(false);
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Add new invoice resource");
        stage.setMinWidth(320);

        stage.setMinHeight(120);
        //stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(10);

        final TextField txt_invoice_issuer = new TextField();
        txt_invoice_issuer.setMinWidth(200);
        txt_invoice_issuer.setMaxWidth(Double.MAX_VALUE);
        txt_invoice_issuer.setEditable(false);
        HBox invoice_issuer = CustomTableViewControls.getLabelledNode("Invoice issuer", 200, txt_invoice_issuer);

        //resource combo box
        final ComboBox<Resource> cbx_resource_id = new ComboBox<>();
        cbx_resource_id.setCellFactory(new Callback<ListView<Resource>, ListCell<Resource>>()
        {
            @Override
            public ListCell<Resource> call(ListView<Resource> lst_resources)
            {
                return new ListCell<Resource>()
                {
                    @Override
                    protected void updateItem(Resource resource, boolean empty)
                    {
                        super.updateItem(resource, empty);
                        if(resource!=null && !empty)
                        {
                            setText(resource.getResource_name());
                        }else{
                            setText("");
                        }
                    }
                };
            }
        });
        cbx_resource_id.setButtonCell(new ListCell<Resource>()
        {
            @Override
            protected void updateItem(Resource resource, boolean empty)
            {
                super.updateItem(resource, empty);
                if(resource!=null && !empty)
                {
                    setText(resource.getResource_name());
                }else{
                    setText("");
                }
            }
        });
        //set invoice issuer
        String issuer_id = (String)invoices[selected_index].get("issuer_org_id");
        if(issuer_id!=null)
            txt_invoice_issuer.setText(issuer_id);
        else{
            IO.log(TAG, IO.TAG_ERROR, "issuer id is null.");
            return;
        }

        cbx_resource_id.setItems(FXCollections.observableArrayList(resources));
        cbx_resource_id.setMinWidth(200);
        cbx_resource_id.setMaxWidth(Double.MAX_VALUE);
        HBox resource_id = CustomTableViewControls.getLabelledNode("Resource", 200, cbx_resource_id);


        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Submit", event ->
        {
            if(cbx_resource_id.getValue()!=null)
            {
                if (!Validators.isValidNode(cbx_resource_id, cbx_resource_id.getValue().get_id() == null ? "" : cbx_resource_id.getValue().get_id(), 1, ".+"))
                    return;
            }
            else
            {
                Validators.isValidNode(cbx_resource_id, "", 1, ".+");
                return;
            }

            String str_resource_id = cbx_resource_id.getValue().get_id();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            //Mandatory
            params.add(new AbstractMap.SimpleEntry<>("invoice_id", invoices[selected_index].get_id()));
            params.add(new AbstractMap.SimpleEntry<>("resource_id", str_resource_id));

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

                HttpURLConnection connection = RemoteComms.postData("/api/invoice/resource/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        JOptionPane.showMessageDialog(null, "Successfully added new invoice resource!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
        vbox.getChildren().add(invoice_issuer);
        vbox.getChildren().add(resource_id);

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

    public void invoiceReps()
    {
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                Stage stage = new Stage();
                stage.setTitle(Globals.APP_NAME.getValue() + " - invoice representatives");
                stage.setMinWidth(320);
                stage.setMinHeight(340);
                //stage.setAlwaysOnTop(true);

                tblInvoiceRepresentatives = new TableView();
                tblInvoiceRepresentatives.setEditable(true);

                TableColumn<BusinessObject, String> resource_id = new TableColumn<>("Employee ID");
                resource_id.setMinWidth(100);
                resource_id.setCellValueFactory(new PropertyValueFactory<>("short_id"));

                TableColumn<BusinessObject, String> firstname = new TableColumn("First name");
                CustomTableViewControls.makeEditableTableColumn(firstname, TextFieldTableCell.forTableColumn(), 80, "firstname", "/api/employee");

                TableColumn<BusinessObject, String> lastname = new TableColumn("Last name");
                CustomTableViewControls.makeEditableTableColumn(lastname, TextFieldTableCell.forTableColumn(), 80, "lastname", "/api/employee");

                //TableColumn<BusinessObject, String> access_level = new TableColumn("Access level");
                //CustomTableViewControls.makeEditableTableColumn(access_level, TextFieldTableCellOld.forTableColumn(), 80, "access_level", "/api/employee");

                TableColumn<BusinessObject, String> gender = new TableColumn("Gender");
                CustomTableViewControls.makeComboBoxTableColumn(gender, genders, "gender", "gender", "/api/employee", 80);

                TableColumn<BusinessObject, String> email_address = new TableColumn("eMail address");
                CustomTableViewControls.makeEditableTableColumn(email_address, TextFieldTableCell.forTableColumn(), 80, "email", "/api/employee");

                TableColumn<BusinessObject, Long> date_joined = new TableColumn("Date joined");
                CustomTableViewControls.makeDatePickerTableColumn(date_joined, "date_joined", "/api/employee");

                TableColumn<BusinessObject, String> tel = new TableColumn("Tel. number");
                CustomTableViewControls.makeEditableTableColumn(tel, TextFieldTableCell.forTableColumn(), 80, "tel", "/api/employee");

                TableColumn<BusinessObject, String> cell = new TableColumn("Cell number");
                CustomTableViewControls.makeEditableTableColumn(cell, TextFieldTableCell.forTableColumn(), 80, "cell", "/api/employee");

                TableColumn<BusinessObject, String> domain = new TableColumn("Domain");
                CustomTableViewControls.makeComboBoxTableColumn(domain, domains, "active", "domain", "/api/employee", 80);
                //CustomTableViewControls.makeEditableTableColumn(other, TextFieldTableCellOld.forTableColumn(), 80, "other", "/api/invoice/resource");

                TableColumn<BusinessObject, String> other = new TableColumn("Other");
                CustomTableViewControls.makeEditableTableColumn(other, TextFieldTableCell.forTableColumn(), 80, "other", "/api/employee");

                MenuBar menu_bar = new MenuBar();
                Menu file = new Menu("File");
                Menu edit = new Menu("Edit");

                MenuItem new_resource = new MenuItem("New representative");
                new_resource.setOnAction(event -> handleNewInvoiceRep(stage));
                MenuItem save = new MenuItem("Save");
                MenuItem print = new MenuItem("Print");


                ObservableList<Employee> lst_invoice_reps = FXCollections.observableArrayList();
                int selected_index = tblInvoices.getSelectionModel().selectedIndexProperty().get();
                //Invoice selected_invoice = (Invoice) tblInvoices.selectionModelProperty().get();
                //make fancy "New representative" label - not really necessary though
                if(invoices!=null)
                {
                    if (selected_index >= 0 && selected_index < invoices.length)
                    {
                        if(invoices[selected_index].get("issuer_org_id")!=null)
                        {
                            for (BusinessObject businessObject : organisations)
                            {
                                if(businessObject.get_id()!=null)
                                {
                                    if (businessObject.get_id().equals(invoices[selected_index].get("issuer_org_id")))
                                    {
                                        if (label_properties.split("\\|").length > 1)
                                        {
                                            String name = (String) businessObject.get(label_properties.split("\\|")[0]);
                                            if (name == null)
                                                name = (String) businessObject.get(label_properties.split("\\|")[1]);
                                            if (name == null)
                                                IO.log(TAG, IO.TAG_ERROR, "neither of the label_properties were found in object!");
                                            else
                                            {
                                                new_resource.setText("New representative for invoice issued by " + name);
                                                IO.log(TAG, IO.TAG_INFO, String.format("set invoice [representative] context to [invoice issued by] '%s'", name));
                                            }
                                        } else
                                        {
                                            IO.log(TAG, IO.TAG_ERROR, "label_properties split array index out of bounds!");
                                        }
                                    }
                                }else IO.log(TAG, IO.TAG_WARN, "business object id is null.");
                            }
                        }else{
                            IO.log(TAG, IO.TAG_ERROR, String.format("issuer_org_id of selected invoice '%s' is null.", invoices[selected_index].get_id()));
                        }
                        if(invoices[selected_index].getRepresentatives()!=null)
                        {
                            lst_invoice_reps.addAll(invoices[selected_index].getRepresentatives());
                            IO.log(TAG, IO.TAG_INFO, String.format("added invoice '%s' representatives.", invoices[selected_index].get_id()));
                        }
                        else IO.log(TAG, IO.TAG_ERROR, String.format("invoice '%s' has no representatives.", invoices[selected_index].get_id()));
                    }
                    else IO.log(TAG, IO.TAG_ERROR, "invoice array index out of bounds: " + selected_index);
                }else IO.log(TAG, IO.TAG_ERROR, "invoices array is null!");

                tblInvoiceRepresentatives.setItems(lst_invoice_reps);
                tblInvoiceRepresentatives.getColumns().addAll(firstname, lastname, gender,
                        email_address, date_joined, tel, cell, domain, other);


                file.getItems().addAll(new_resource, save, print);

                menu_bar.getMenus().addAll(file, edit);

                BorderPane border_pane = new BorderPane();
                border_pane.setTop(menu_bar);
                border_pane.setCenter(tblInvoiceRepresentatives);

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

    public void handleNewInvoiceRep(Stage parentStage)
    {
        if(tblInvoices==null)
        {
            IO.log(TAG, IO.TAG_ERROR, "invoices table is null!");
            return;
        }

        if(employees==null)
        {
            IO.log(TAG, IO.TAG_ERROR, "no employees were found in the database.");
            return;
        }

        int selected_index = tblInvoices.getSelectionModel().selectedIndexProperty().get();

        if(invoices!=null)
        {
            if (selected_index < 0 || selected_index >= invoices.length)
            {
                IO.log(TAG, IO.TAG_ERROR, "invoices array index is out of bounds");
                return;
            }
        }else{
            IO.log(TAG, IO.TAG_ERROR, "invoices array is null!");
            return;
        }

        parentStage.setAlwaysOnTop(false);
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Add new invoice representative");
        stage.setMinWidth(320);
        stage.setMinHeight(120);
        //stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(10);

        final TextField txt_invoice_issuer = new TextField();
        txt_invoice_issuer.setMinWidth(200);
        txt_invoice_issuer.setMaxWidth(Double.MAX_VALUE);
        txt_invoice_issuer.setEditable(false);
        HBox invoice_issuer = CustomTableViewControls.getLabelledNode("Invoice issuer", 200, txt_invoice_issuer);

        //resource combo box
        final ComboBox<Employee> cbx_employee = new ComboBox<>();
        cbx_employee.setCellFactory(new Callback<ListView<Employee>, ListCell<Employee>>()
        {
            @Override
            public ListCell<Employee> call(ListView<Employee> lst_reps)
            {
                return new ListCell<Employee>()
                {
                    @Override
                    protected void updateItem(Employee employee, boolean empty)
                    {
                        super.updateItem(employee, empty);
                        if(employee!=null && !empty)
                        {
                            setText(employee.getFirstname() + " " + employee.getLastname());
                        }else{
                            setText("");
                        }
                    }
                };
            }
        });
        cbx_employee.setButtonCell(new ListCell<Employee>()
        {
            @Override
            protected void updateItem(Employee employee, boolean empty)
            {
                super.updateItem(employee, empty);
                if(employee!=null && !empty)
                {
                    setText(employee.getFirstname() + " " + employee.getLastname());
                }else{
                    setText("");
                }
            }
        });
        //set invoice issuer
        String issuer_id = (String)invoices[selected_index].get("issuer_org_id");
        if(issuer_id!=null)
            txt_invoice_issuer.setText(issuer_id);
        else{
            IO.log(TAG, IO.TAG_ERROR, "issuer id is null.");
            return;
        }

        cbx_employee.setItems(FXCollections.observableArrayList(employees));
        cbx_employee.setMinWidth(200);
        cbx_employee.setMaxWidth(Double.MAX_VALUE);
        HBox employee = CustomTableViewControls.getLabelledNode("Employee", 200, cbx_employee);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Submit", event ->
        {
            if(cbx_employee.getValue()!=null)
            {
                if (!Validators.isValidNode(cbx_employee, cbx_employee.getValue().get_id() == null ? "" : cbx_employee.getValue().get_id(), 1, ".+"))
                    return;
            }
            else
            {
                Validators.isValidNode(cbx_employee, "", 1, ".+");
                return;
            }

            String str_employee_usr = (String)cbx_employee.getValue().get("usr");

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            //Mandatory
            params.add(new AbstractMap.SimpleEntry<>("invoice_id", invoices[selected_index].get_id()));
            params.add(new AbstractMap.SimpleEntry<>("usr", str_employee_usr));

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

                HttpURLConnection connection = RemoteComms.postData("/api/invoice/rep/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        JOptionPane.showMessageDialog(null, "Successfully added new invoice representative!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
        vbox.getChildren().add(invoice_issuer);
        vbox.getChildren().add(employee);

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
    }
}
