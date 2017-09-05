/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.swing.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * views Controller class
 *
 * @author ghost
 */
public class NewQuoteController implements Initializable, Screen
{
    private ScreenManager screen_mgr;
    private boolean itemsModified;
    private Date date_generated;

    @FXML
    private TableView<QuoteItem> tblQuoteItems, tblGenericQuoteItems;
    @FXML
    private TableView<Employee> tblSaleReps;
    @FXML
    private TableColumn colFirstname,colLastname,colCell,colEmail,colTel,colGender,colActive,colAction;
    @FXML
    private TableColumn colMarkup,colQuantity,colLabour;
    @FXML
    private ComboBox<Client> cbxClients;
    @FXML
    private ComboBox<Employee> cbxContactPerson;
    @FXML
    private TextField txtCell,txtTel,txtTotal,txtQuoteId,txtFax,txtEmail,txtSite,txtDateGenerated,txtExtra;
    @FXML
    private TextArea txtRequest;

    @Override
    public void refresh()
    {
        QuoteManager.getInstance().initialize(screen_mgr);
        ResourceManager.getInstance().initialize(screen_mgr);

        tblSaleReps.getItems().clear();
        tblQuoteItems.getItems().clear();

        Callback callback = param ->
        {
            if(param!=null)
            {
                BusinessObject obj = (BusinessObject)param;
                String value = String.valueOf(obj.get("value"));
                String quantity = String.valueOf(obj.get("quantity"));
                String strMarkup = String.valueOf(obj.get("markup"));
                String strLabour = String.valueOf(obj.get("labour"));

                try
                {
                    double item_value = Double.parseDouble(value);
                    double qty = Double.parseDouble(quantity);
                    double markup = Double.parseDouble(strMarkup);
                    double labour = Double.parseDouble(strLabour);

                    double rate = (item_value * (markup / 100)) + item_value + labour;//rate per item

                    obj.parse("rate", String.valueOf(rate));//set new rate

                    computeQuoteTotal();
                    return obj;
                } catch (NumberFormatException e)
                {
                    IO.logAndAlert("NumberFormatException", e.getMessage(), IO.TAG_ERROR);//Could not convert value to number.
                }
            }else IO.logAndAlert("Quote Item Error","Invalid object, double check your data.", IO.TAG_ERROR);
            return null;
        };

        //Setup Quote Items table
        colMarkup.setCellValueFactory(new PropertyValueFactory<>("markup"));
        colMarkup.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("markup", "markup", callback));

        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQuantity.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("quantity", "quantity", callback));

        colLabour.setCellValueFactory(new PropertyValueFactory<>("labour"));
        colLabour.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("labour", "labour", callback));

        //Setup Sale Reps table
        colFirstname.setCellValueFactory(new PropertyValueFactory<>("firstname"));
        colLastname.setCellValueFactory(new PropertyValueFactory<>("lastname"));
        colCell.setCellValueFactory(new PropertyValueFactory<>("cell"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("tel"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        date_generated = new Date();
        txtDateGenerated.setText(date_generated.toString());

        cbxClients.setCellFactory(new Callback<ListView<Client>, ListCell<Client>>()
        {
            @Override public ListCell<Client> call(ListView<Client> p)
            {
                return new ListCell<Client>()
                {
                    @Override
                    protected void updateItem(Client item, boolean empty)
                    {
                        super.updateItem(item, empty);

                        if (item == null || empty)
                        {
                            setGraphic(null);
                        } else{
                            setText(item.getClient_name());
                        }
                    }
                };
            }
        });
        cbxClients.setButtonCell(null);
        cbxClients.setItems(FXCollections.observableArrayList(ClientManager.getInstance().getClients()));
        cbxClients.setOnAction(event ->
        {
            if(cbxClients.getValue()!=null)
            {
                txtFax.setText(cbxClients.getValue().getFax());
                itemsModified=true;
            }
            //else IO.logAndAlert("Invalid Client", "Client company selected is invalid.", IO.TAG_ERROR);
        });

        cbxContactPerson.setCellFactory(new Callback<ListView<Employee>, ListCell<Employee>>()
        {
            @Override public ListCell<Employee> call(ListView<Employee> p)
            {
                return new ListCell<Employee>()
                {
                    @Override
                    protected void updateItem(Employee item, boolean empty)
                    {
                        super.updateItem(item, empty);

                        if (item == null || empty)
                        {
                            setGraphic(null);
                        } else{
                            setText(item.getFirstname() + " " + item.getLastname());
                        }
                    }
                };
            }
        });
        cbxContactPerson.setButtonCell(null);
        cbxContactPerson.setItems(FXCollections.observableArrayList(EmployeeManager.getInstance().getEmployees()));
        cbxContactPerson.setOnAction(event ->
        {
            Employee employee = cbxContactPerson.getValue();
            if(employee!=null)
            {
                txtCell.setText(employee.getCell());
                txtTel.setText(employee.getTel());
                txtEmail.setText(employee.getEmail());
                itemsModified=true;
            }//else IO.logAndAlert("Invalid Employee", "Selected contact person is invalid", IO.TAG_ERROR);
        });

        //Populate fields
        if(QuoteManager.getInstance().fromGeneric())
        {
            GenericQuote quote = QuoteManager.getInstance().getSelectedGenericQuote();
            if(quote!=null)
            {
                txtRequest.setText(quote.getRequest());
                txtSite.setText(quote.getSitename());
            }
        }else{
            Quote selected = QuoteManager.getInstance().getSelectedQuote();
            if (selected != null)
            {
                cbxClients.setValue(selected.getClient());
                cbxContactPerson.setValue(selected.getContactPerson());

                if (selected.getResources() != null)
                    tblQuoteItems.setItems(FXCollections.observableArrayList(selected.getResources()));
                else IO.log(getClass().getName(), IO.TAG_WARN, "quote [" + selected.get_id() + "] has no resources.");
                if (selected.getRepresentatives() != null)
                    tblSaleReps.setItems(FXCollections.observableArrayList(selected.getRepresentatives()));
                else
                    IO.log(getClass().getName(), IO.TAG_WARN, "quote [" + selected.get_id() + "] has no representatives.");
            }
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        //colAction.setCellFactory(new ButtonTableCellFactory<>());

        colAction.setCellValueFactory(new PropertyValueFactory<>(""));

        Callback<TableColumn<QuoteItem, String>, TableCell<QuoteItem, String>> cellFactory
                =
                new Callback<TableColumn<QuoteItem, String>, TableCell<QuoteItem, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<QuoteItem, String> param)
                    {
                        final TableCell<QuoteItem, String> cell = new TableCell<QuoteItem, String>()
                        {
                            final Button btnAdd = new Button("Add materials");
                            final Button btnRemove = new Button("Remove item");

                            @Override
                            public void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                btnAdd.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnAdd.getStyleClass().add("btnApply");
                                btnAdd.setMinWidth(100);
                                btnAdd.setMinHeight(35);
                                HBox.setHgrow(btnAdd, Priority.ALWAYS);

                                btnRemove.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnRemove.getStyleClass().add("btnBack");
                                btnRemove.setMinWidth(100);
                                btnRemove.setMinHeight(35);
                                HBox.setHgrow(btnRemove, Priority.ALWAYS);

                                if (empty)
                                {
                                    setGraphic(null);
                                    setText(null);
                                } else
                                {
                                    HBox hBox = new HBox(btnAdd, btnRemove);

                                    btnAdd.setOnAction(event ->
                                    {
                                        QuoteItem quoteItem = getTableView().getItems().get(getIndex());
                                        addQuoteItemAdditionalMaterial(quoteItem);
                                        IO.log(getClass().getName(), IO.TAG_INFO, "Successfully added material quote number " + quoteItem.getItem_number());
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        QuoteItem quoteItem = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(quoteItem);
                                        getTableView().refresh();
                                        computeQuoteTotal();
                                        IO.log(getClass().getName(), IO.TAG_INFO, "successfully removed quote item " + quoteItem.getItem_number());
                                    });

                                    hBox.setFillHeight(true);
                                    HBox.setHgrow(hBox, Priority.ALWAYS);
                                    hBox.setSpacing(5);
                                    setGraphic(hBox);
                                    setText(null);
                                }
                            }
                        };
                    return cell;
                    }
                };

        colAction.setCellFactory(cellFactory);
    }

    private void computeQuoteTotal()
    {
        //compute total
        double total=0;
        for(QuoteItem item: tblQuoteItems.getItems())
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
                        }else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid Quote Item additional cost.");
                    }
                }
            }
            //add Quote Item rate*quantity to total
            total += item.getRateValue() * item.getQuantityValue();
        }
        txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(total));
    }

    public void addQuoteItemAdditionalMaterial(QuoteItem quoteItem)
    {
        Stage stage = new Stage();
        stage.setAlwaysOnTop(true);

        TextField txtName = new TextField();
        TextField txtCost = new TextField();

        Button btnAdd = new Button("Add");
        btnAdd.setMinWidth(140);
        btnAdd.setOnAction(event ->
        {
            //validate cost name
            if(txtName.getText()!=null)
            {
                if (txtName.getText().isEmpty())
                {
                    IO.logAndAlert("New Additional Cost Error", "Please enter a valid cost name.", IO.TAG_ERROR);
                    return;
                }
            }else{
                IO.logAndAlert("New Additional Cost Error", "Please enter a valid cost name.", IO.TAG_ERROR);
                return;
            }
            //validate cost value
            if(txtCost.getText()!=null)
            {
                if (txtCost.getText().isEmpty())
                {
                    IO.logAndAlert("New Additional Cost Error", "Please enter a valid cost value.", IO.TAG_ERROR);
                    return;
                }
            }else{
                IO.logAndAlert("New Additional Cost Error", "Please enter a valid cost value.", IO.TAG_ERROR);
                return;
            }

            String new_cost = txtName.getText()+"="+txtCost.getText();
            if(quoteItem.getAdditional_costs().isEmpty())
                quoteItem.setAdditional_costs(new_cost);
            else quoteItem.setAdditional_costs(quoteItem.getAdditional_costs()+";"+new_cost);

            computeQuoteTotal();
        });

        HBox row1 = new HBox(new Label("Material name"), txtName);
        HBox row2 = new HBox(new Label("Material value"), txtCost);

        stage.setTitle("Extra Costs For Quote Item #"+quoteItem.getItem_numberValue());
        stage.setScene(new Scene(new VBox(row1, row2, btnAdd)));
        stage.show();
    }

    @FXML
    public void newQuoteItem()
    {
        if(ResourceManager.getInstance()!=null)
        {
            if(ResourceManager.getInstance().getResources()!=null)
            {
                if(ResourceManager.getInstance().getResources().length>0)
                {
                    ComboBox<Resource> resourceComboBox = new ComboBox<>();
                    resourceComboBox.setMinWidth(120);
                    resourceComboBox.setItems(FXCollections.observableArrayList(ResourceManager.getInstance().getResources()));
                    HBox.setHgrow(resourceComboBox, Priority.ALWAYS);

                    Button btnAdd = new Button("Add");
                    btnAdd.setMinWidth(80);
                    btnAdd.setMinHeight(40);
                    btnAdd.setDefaultButton(true);
                    btnAdd.getStyleClass().add("btnApply");
                    btnAdd.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    Button btnCancel = new Button("Close");
                    btnCancel.setMinWidth(80);
                    btnCancel.setMinHeight(40);
                    btnCancel.getStyleClass().add("btnBack");
                    btnCancel.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    HBox hBox = new HBox(new Label("Resource: "), resourceComboBox);
                    HBox.setHgrow(hBox, Priority.ALWAYS);
                    hBox.setSpacing(20);

                    HBox hBoxButtons = new HBox(btnAdd, btnCancel);
                    hBoxButtons.setHgrow(btnAdd, Priority.ALWAYS);
                    hBoxButtons.setHgrow(btnCancel, Priority.ALWAYS);
                    hBoxButtons.setSpacing(20);

                    VBox vBox = new VBox(hBox, hBoxButtons);
                    VBox.setVgrow(vBox, Priority.ALWAYS);
                    vBox.setSpacing(20);
                    HBox.setHgrow(vBox, Priority.ALWAYS);
                    vBox.setFillWidth(true);

                    Stage stage = new Stage();
                    stage.setMaxWidth(300);
                    stage.setTitle("New Quote Resource");
                    stage.setScene(new Scene(vBox));
                    stage.setAlwaysOnTop(true);
                    stage.show();

                    btnAdd.setOnAction(event ->
                    {
                        if(resourceComboBox.getValue()!=null)
                        {
                            QuoteItem quoteItem = new QuoteItem();

                            quoteItem.setItem_number(tblQuoteItems.getItems().size());
                            quoteItem.setEquipment_description(resourceComboBox.getValue().getResource_description());
                            quoteItem.setUnit(resourceComboBox.getValue().getUnit());
                            quoteItem.setQuantity(1);
                            quoteItem.setRate(resourceComboBox.getValue().getResource_value());
                            quoteItem.setLabour(0);
                            quoteItem.setMarkup(0);
                            quoteItem.setValue(resourceComboBox.getValue().getResource_value());
                            quoteItem.setResource(resourceComboBox.getValue());
                            quoteItem.setEquipment_name(resourceComboBox.getValue().getResource_name());

                            tblQuoteItems.getItems().add(quoteItem);
                            tblQuoteItems.refresh();

                            itemsModified = true;

                            computeQuoteTotal();

                        } else IO.logAndAlert("New Quote Resource", "Invalid resource selected.", IO.TAG_ERROR);
                    });

                    btnCancel.setOnAction(event ->
                            stage.close());
                    return;
                }
            }
        }
        IO.logAndAlert("New Quote Resource", "No resources were found in the database, please add some resources first and try again.",IO.TAG_ERROR);
    }

    @FXML
    public void newSaleConsultant()
    {
        if(QuoteManager.getInstance()!=null)
        {
            if(EmployeeManager.getInstance().getEmployees()!=null)
            {
                if(EmployeeManager.getInstance().getEmployees().length>0)
                {
                    ComboBox<Employee> employeeComboBox = new ComboBox<>();
                    employeeComboBox.setMinWidth(120);
                    employeeComboBox.setItems(FXCollections.observableArrayList(EmployeeManager.getInstance().getEmployees()));
                    HBox.setHgrow(employeeComboBox, Priority.ALWAYS);

                    Button btnAdd = new Button("Add");
                    btnAdd.setMinWidth(80);
                    btnAdd.setMinHeight(40);
                    btnAdd.setDefaultButton(true);
                    btnAdd.getStyleClass().add("btnApply");
                    btnAdd.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    Button btnCancel = new Button("Close");
                    btnCancel.setMinWidth(80);
                    btnCancel.setMinHeight(40);
                    btnCancel.getStyleClass().add("btnBack");
                    btnCancel.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    HBox hBox = new HBox(new Label("Employee: "), employeeComboBox);
                    HBox.setHgrow(hBox, Priority.ALWAYS);
                    hBox.setSpacing(20);

                    HBox hBoxButtons = new HBox(btnAdd, btnCancel);
                    hBoxButtons.setHgrow(btnAdd, Priority.ALWAYS);
                    hBoxButtons.setHgrow(btnCancel, Priority.ALWAYS);
                    hBoxButtons.setSpacing(20);

                    VBox vBox = new VBox(hBox, hBoxButtons);
                    VBox.setVgrow(vBox, Priority.ALWAYS);
                    vBox.setSpacing(20);
                    HBox.setHgrow(vBox, Priority.ALWAYS);
                    vBox.setFillWidth(true);

                    Stage stage = new Stage();
                    stage.setTitle("Add Quote Representative");
                    stage.setScene(new Scene(vBox));
                    stage.setAlwaysOnTop(true);
                    stage.show();

                    btnAdd.setOnAction(event ->
                    {
                        if(employeeComboBox.getValue()!=null)
                        {
                            tblSaleReps.getItems().add(employeeComboBox.getValue());
                            itemsModified=true;
                        }
                        else IO.logAndAlert("Add Quote Representative", "Invalid employee selected.", IO.TAG_ERROR);
                    });

                    btnCancel.setOnAction(event ->
                        stage.close());
                    return;
                }
            }
        }
        IO.logAndAlert("New Sale Consultant", "No employees were found in the database, please add an employee first and try again.",IO.TAG_ERROR);
    }

    @FXML
    public void apply()
    {
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                //Update Quote if already been added
                if(txtQuoteId.getText()!=null)
                {
                    if(!txtQuoteId.getText().isEmpty())
                    {
                        updateQuote();
                        return;
                    }
                }
                //else create new Quote
                createQuote();
            }else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public void updateQuote()
    {
        cbxClients.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
        if(cbxClients.getValue()==null)
        {
            cbxClients.getStyleClass().remove("form-control-default");
            cbxClients.getStyleClass().add("control-input-error");
            return;
        }else{
            cbxClients.getStyleClass().remove("control-input-error");
            cbxClients.getStyleClass().add("form-control-default");
        }

        cbxContactPerson.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
        if(cbxContactPerson.getValue()==null)
        {
            cbxContactPerson.getStyleClass().remove("form-control-default");
            cbxContactPerson.getStyleClass().add("control-input-error");
            return;
        }else{
            cbxContactPerson.getStyleClass().remove("control-input-error");
            cbxContactPerson.getStyleClass().add("form-control-default");
        }

        if(!Validators.isValidNode(txtCell, txtCell.getText(), 1, ".+"))
        {
            txtCell.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtTel, txtTel.getText(), 1, ".+"))
        {
            txtTel.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtEmail, txtEmail.getText(), 1, ".+"))
        {
            txtEmail.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtSite, txtSite.getText(), 1, ".+"))
        {
            txtSite.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }

        String str_site = txtSite.getText();
        //String str_extra = txtExtra.getText();


        Quote selected = QuoteManager.getInstance().getSelectedQuote();
        if(selected!=null)
        {
            selected.setClient_id(cbxClients.getValue().get_id());
            selected.setContact_person_id(cbxContactPerson.getValue().get_id());
            selected.setSitename(str_site);
            selected.setRequest(txtRequest.getText());
            /*if (str_extra != null)
                selected.parse("extra", str_extra);*/
            //QuoteManager.getInstance().updateQuote(selected, ((QuoteItem[]) tblQuoteItems.getItems().toArray()), ((Employee[])tblSaleReps.getItems().toArray()));
            QuoteManager.getInstance().updateQuote(selected, tblQuoteItems.getItems(), tblSaleReps.getItems());

            refresh();
            //tblQuoteItems.refresh();
            //tblSaleReps.refresh();
        }
    }

    public void createQuote()
    {
        String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

        cbxClients.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
        if(cbxClients.getValue()==null)
        {
            cbxClients.getStyleClass().remove("form-control-default");
            cbxClients.getStyleClass().add("control-input-error");
            return;
        }else{
            cbxClients.getStyleClass().remove("control-input-error");
            cbxClients.getStyleClass().add("form-control-default");
        }

        cbxContactPerson.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
        if(cbxContactPerson.getValue()==null)
        {
            cbxContactPerson.getStyleClass().remove("form-control-default");
            cbxContactPerson.getStyleClass().add("control-input-error");
            return;
        }else{
            cbxContactPerson.getStyleClass().remove("control-input-error");
            cbxContactPerson.getStyleClass().add("form-control-default");
        }

        if(!Validators.isValidNode(txtCell, txtCell.getText(), 1, ".+"))
        {
            txtCell.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtTel, txtTel.getText(), 1, ".+"))
        {
            txtTel.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        /*if(!Validators.isValidNode(txtFax, txtFax.getText(), 1, ".+"))
        {
            txtFax.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }*/
        if(!Validators.isValidNode(txtEmail, txtEmail.getText(), 1, ".+"))
        {
            txtEmail.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtSite, txtSite.getText(), 1, ".+"))
        {
            txtSite.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtRequest, txtRequest.getText(), 1, ".+"))
        {
            txtRequest.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }

        List<QuoteItem> quoteItems = tblQuoteItems.getItems();

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

        List<Employee> quoteReps = tblSaleReps.getItems();
        if(quoteReps==null)
        {
            IO.logAndAlert("Invalid Quote", "Quote has no representatives.", IO.TAG_ERROR);
            return;
        }
        if(quoteReps.size()<=0)
        {
            IO.logAndAlert("Invalid Quote", "Quote has no representatives", IO.TAG_ERROR);
            return;
        }

        String str_company = cbxClients.getValue().get_id();
        String str_contact = cbxContactPerson.getValue().get_id();
        String str_site = txtSite.getText();
        long date_generated_in_sec = (date_generated.getTime()/1000);
        String str_extra = null;//txtExtra.getText();

        //prepare quote attributes
        Quote quote = new Quote();
        quote.setClient_id(str_company);
        quote.setContact_person_id(str_contact);
        quote.setDate_generated(date_generated_in_sec);
        quote.setSitename(str_site);
        quote.setRequest(txtRequest.getText());
        quote.setStatus(0);
        quote.setCreator(SessionManager.getInstance().getActive().getUser());
        quote.setRevision(1.0);
        QuoteItem[] items = new QuoteItem[quoteItems.size()];
        quoteItems.toArray(items);
        quote.setResources(items);
        Employee[] reps = new Employee[quoteReps.size()];
        quoteReps.toArray(reps);
        quote.setRepresentatives(reps);
        if(str_extra!=null)
            quote.setExtra(str_extra);

        try
        {
            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
            if(SessionManager.getInstance().getActive()!=null)
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
            else
            {
                IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
                return;
            }

            //create new quote on database
            HttpURLConnection connection = RemoteComms.postData("/api/quote/add", quote.asUTFEncodedString(), headers);
            if(connection!=null)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());
                    IO.log(getClass().getName(), IO.TAG_INFO, "created quote["+response+"]. Adding representatives and resources to quote.");

                    if(response==null)
                    {
                        IO.logAndAlert("New Quote Resource", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }
                    if(response.isEmpty())
                    {
                        IO.logAndAlert("New Quote Resource", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }
                    txtQuoteId.setText(response);

                    /* Add Quote Representatives/Employees to Quote on database*/
                    if(connection!=null)
                        connection.disconnect();

                    boolean added_all_quote_reps = true;
                    for(Employee employee : tblSaleReps.getItems())
                    {
                        //prepare parameters for quote resources.
                        ArrayList params = new ArrayList<>();
                        params.add(new AbstractMap.SimpleEntry<>("quote_id", response));
                        params.add(new AbstractMap.SimpleEntry<>("usr", employee.getUsr()));
                        added_all_quote_reps = QuoteManager.getInstance().createQuoteRep(response, params, headers);
                    }
                    if(!added_all_quote_reps)
                        IO.logAndAlert("New Quote Representative Creation Failure", "Could not add representatives to quote, however, the quote["+response+"] has been created.", IO.TAG_INFO);


                    /* Add Quote Resources to Quote on database */
                    if(connection!=null)
                        connection.disconnect();

                    boolean added_all_quote_items = true;
                    for(QuoteItem quoteItem : tblQuoteItems.getItems())
                    {
                        //prepare parameters for quote resources.
                        ArrayList params = new ArrayList<>();
                        params.add(new AbstractMap.SimpleEntry<>("quote_id", response));
                        params.add(new AbstractMap.SimpleEntry<>("item_number", quoteItem.getItem_number()));
                        params.add(new AbstractMap.SimpleEntry<>("resource_id", quoteItem.getResource().get_id()));
                        params.add(new AbstractMap.SimpleEntry<>("markup", quoteItem.getMarkup()));
                        params.add(new AbstractMap.SimpleEntry<>("labour", quoteItem.getLabour()));
                        params.add(new AbstractMap.SimpleEntry<>("quantity", quoteItem.getQuantity()));
                        params.add(new AbstractMap.SimpleEntry<>("additional_costs", quoteItem.getAdditional_costs()));
                        added_all_quote_items = QuoteManager.getInstance().createQuoteItem(response, params, headers);

                        /*connection = RemoteComms.postData("/api/quote/resource/add/"+response, params, headers);
                        if (connection != null)
                        {
                            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                            {
                                IO.log(getClass().getName(), IO.TAG_INFO, "Successfully added a new quote["+response+"] item.");
                            } else
                            {
                                added_all_quote_items = false;
                                //Get error message
                                String msg = IO.readStream(connection.getErrorStream());
                                IO.logAndAlert("Error " + String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                            }
                        }else IO.logAndAlert("New Quote Item Creation Failure", "Could not connect to server.", IO.TAG_ERROR);*/
                    }
                    if(added_all_quote_items && added_all_quote_reps)
                    {
                        //set selected quote
                        //quote.set_id(response);
                        QuoteManager.getInstance().loadDataFromServer();
                        QuoteManager.getInstance().setSelectedQuote(response);
                        tblQuoteItems.setItems(FXCollections.observableArrayList(QuoteManager.getInstance().getSelectedQuote().getResources()));

                        //QuoteManager.getInstance().setSelectedQuote(quote);
                        IO.logAndAlert("New Quote Creation Success", "Successfully created a new quote.", IO.TAG_INFO);
                        itemsModified = false;
                    } else IO.logAndAlert("New Quote Creation Failure", "Could not add items to quote.", IO.TAG_ERROR);
                }else
                {
                    //Get error message
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                }
                if(connection!=null)
                    connection.disconnect();
            }else IO.logAndAlert("New Quote Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }
    }

    @FXML
    public void createSale()
    {
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                //Update Quote if already been added
                if(txtQuoteId.getText()!=null)
                {
                    if(!txtQuoteId.getText().isEmpty())
                    {
                        cbxClients.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
                        if(cbxClients.getValue()==null)
                        {
                            cbxClients.getStyleClass().remove("form-control-default");
                            cbxClients.getStyleClass().add("control-input-error");
                            return;
                        }else{
                            cbxClients.getStyleClass().remove("control-input-error");
                            cbxClients.getStyleClass().add("form-control-default");
                        }

                        cbxContactPerson.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
                        if(cbxContactPerson.getValue()==null)
                        {
                            cbxContactPerson.getStyleClass().remove("form-control-default");
                            cbxContactPerson.getStyleClass().add("control-input-error");
                            return;
                        }else{
                            cbxContactPerson.getStyleClass().remove("control-input-error");
                            cbxContactPerson.getStyleClass().add("form-control-default");
                        }
                        if(!Validators.isValidNode(txtSite, txtSite.getText(), 1, ".+"))
                        {
                            txtSite.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
                            return;
                        }
                        //
                        List<QuoteItem> quoteItems = tblQuoteItems.getItems();

                        if(quoteItems==null)
                        {
                            IO.logAndAlert("Cannot Create Sale", "Can't create sale because this quote items list is null.", IO.TAG_ERROR);
                            return;
                        }
                        if(quoteItems.size()<=0)
                        {
                            IO.logAndAlert("Cannot Create Sale", "Can't create sale because this quote has no items/resources", IO.TAG_ERROR);
                            return;
                        }

                        List<Employee> quoteReps = tblSaleReps.getItems();
                        if(quoteReps==null)
                        {
                            IO.logAndAlert("Cannot Create Sale", "Can't create sale because this quote has no representatives.", IO.TAG_ERROR);
                            return;
                        }
                        if(quoteReps.size()<=0)
                        {
                            IO.logAndAlert("Cannot Create Sale", "Can't create sale because this quote has no representatives", IO.TAG_ERROR);
                            return;
                        }
                        if(QuoteManager.getInstance().getSelectedQuote()!=null)
                            SaleManager.getInstance().createNewSale(QuoteManager.getInstance().getSelectedQuote().get_id());
                        else IO.logAndAlert("Cannot Create Sale", "Cannot create sale because the selected quote is invalid.", IO.TAG_ERROR);
                    } else IO.logAndAlert("Cannot Create Sale", "Cannot create sale from this quote because the quote data is invalid.", IO.TAG_ERROR);
                } else IO.logAndAlert("Cannot Create Sale", "Cannot create sale from this quote because the quote data is invalid.", IO.TAG_ERROR);
            }else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    @FXML
    public void previousScreen()
    {
        if(itemsModified)
        {
            int response = JOptionPane.showConfirmDialog(null, "You have unsaved changes to the quote's items, would you like to save them?");

            if(response == JOptionPane.OK_OPTION)
            {
                apply();
            }else{
                screen_mgr.setScreen(Screens.QUOTES.getScreen());
            }
        }else{
            screen_mgr.setScreen(Screens.QUOTES.getScreen());
        }
    }

    @Override
    public void setParent(ScreenManager mgr) 
    {
        screen_mgr = mgr;
    }

    @FXML
    public void showMain()
    {
        screen_mgr.setScreen(Screens.HOME.getScreen());
    }

    class ComboBoxTableCell extends TableCell<BusinessObject, String>
    {
        private ComboBox<String> comboBox;
        private String property, label_property, api_method;
        private BusinessObject[] business_objects;
        public static final String TAG = "ComboBoxTableCell";

        public ComboBoxTableCell(BusinessObject[] business_objects, String property, String label_properties)
        {
            super();
            this.property = property;
            this.api_method = api_method;
            this.business_objects=business_objects;
            this.label_property = label_properties;

            String[] combobox_items;
            if(business_objects==null)
            {
                IO.log(TAG, IO.TAG_ERROR, "business objects array for the combo box cannot be null!");
                return;
            }
            if(business_objects.length<=0)
            {
                IO.log(TAG, IO.TAG_ERROR, "business objects array for the combo box cannot be empty!");
                return;
            }

            combobox_items = new String[business_objects.length];
            String[] properties = label_properties.split("\\|");
            for(int i=0;i<business_objects.length;i++)
            {
                String prop_val = getBusinessObjectProperty(properties, business_objects[i]);
                if (prop_val!=null)
                {
                    combobox_items[i] = prop_val;
                    IO.log(TAG, IO.TAG_INFO, String.format("set combo box array item #%s to '%s'.", i, prop_val));
                    //break;
                }else
                {
                    IO.log(TAG, IO.TAG_WARN, String.format("property '%s' on object of type '%s' is null.", label_property, business_objects[i].getClass().getName()));
                }
            }

            comboBox = new ComboBox<>(FXCollections.observableArrayList(combobox_items));
            HBox.setHgrow(comboBox, Priority.ALWAYS);

            IO.log(TAG, IO.TAG_INFO, "set array to combo box.");

            comboBox.valueProperty().addListener((observable, oldValue, newValue) ->
            {
                int selected_pos = comboBox.selectionModelProperty().get().getSelectedIndex();
                if(selected_pos>=0 && selected_pos<business_objects.length)
                {
                    commitEdit(business_objects[selected_pos].get_id());
                    updateItem(business_objects[selected_pos].get_id(), business_objects[selected_pos].get_id()==null);
                    //IO.log(TAG, IO.TAG_INFO, "selected: " + business_objects[selected_pos]);
                }else IO.log(TAG, IO.TAG_ERROR, "index out of bounds.");
            });
        }

        /**
         * Function to get the matching values to a list of BusinessObject attributes.
         * @param properties list of attributes to be retrieved.
         * @param business_object BusinessObject to retrieve the values from.
         * @return String with all the attribute values separated by a space.
         */
        public String getBusinessObjectProperty(String[] properties, BusinessObject business_object)
        {
            String prop_val = "";
            for (String label_property : properties)
                prop_val += business_object.get(label_property) + " ";
            if (prop_val != null)
                return prop_val.substring(0,prop_val.length()-1);//return the chained String - without the last space.
            return null;
        }

        @Override
        public void commitEdit(String selected_id)
        {
            super.commitEdit(selected_id);
            if(selected_id!=null)
            {
                if(!selected_id.isEmpty())
                {
                    if (getTableRow().getItem() instanceof BusinessObject)
                    {
                        int selected_pos = comboBox.selectionModelProperty().get().getSelectedIndex();
                        if(selected_pos>=0 && selected_pos<business_objects.length)
                        {
                            System.out.println("\n\ngetIndex():" + getIndex() + " getTableRow().getIndex():" + getTableRow().getIndex() + "\n\n");
                            //getTableRow().setItem(comboBox.get);
                            System.out.println("Current: " + getTableRow().getItem());
                            for(BusinessObject bo: business_objects)
                            {
                                if(bo.get_id().equals(selected_id))
                                {
                                    System.out.println("New: " + bo);
                                    System.out.println("TableView item count: "+getTableView().getItems().size());
                                    //getTableView().getItems().set(getIndex(), bo);
                                    List items = getTableView().getItems();
                                    items.set(getIndex(), bo);
                                    getTableView().setItems(FXCollections.observableArrayList(items));
                                    return;
                                }
                            }
                            //getTableView().getItems()
                            /*for (Employee employee : QuoteManager.getInstance().getEmployees())
                            {
                                if (employee.get_id().equals(selected_id))
                                {
                                    getTableView().getItems().set(selected_pos, employee);
                                    return;
                                }
                            }*/
                        }else IO.log(TAG, IO.TAG_ERROR, "index out of bounds.");
                        IO.logAndAlert(TAG, "NewQuoteController.ComboBoxTableCell> Selected Employee was not found in Employee list.", IO.TAG_ERROR);
                        /*
                        bo.parse(property, selected_id);
                        if (bo != null)
                        {
                            RemoteComms.updateBusinessObjectOnServer(bo, api_method, property);

                        } else
                        {
                            IO.log(TAG, IO.TAG_ERROR, "row business object is null.");
                        }*/
                    } else IO.log(TAG, IO.TAG_ERROR, String.format("unknown row object: " + getTableRow().getItem()));
                } else IO.log(TAG, IO.TAG_ERROR, String.format("selected_id is empty"));
            }else IO.log(TAG, IO.TAG_ERROR, "selected_id is null.");
        }

        @Override
        protected void updateItem(String selected_id, boolean empty)
        {
            super.updateItem(selected_id, empty);
            //comboBox.setValue(prop_val);
            if(!empty && selected_id!=null)
            {
                setGraphic(comboBox);
                /*update_counter++;
                if(updated_ids.putIfAbsent(selected_id, "")==null)
                {
                    setGraphic(comboBox);
                    System.out.println("Added new ID.");
                }else System.err.println("ID was present in HashMap already, thus no setGraphic().");
                /*if(update_counter<getTableView().getItems().size())
                {

                }*/
                //System.out.println("\n-->id:"+selected_id+"\tupdate count:"+update_counter+"<--\n");

                //IO.log(TAG, IO.TAG_INFO, String.format("updated selected item to [%s] on combo box.", selected_id));
            }
            //IO.log(TAG, IO.TAG_INFO, String.format("set property value of '%s' on combo box.", prop_val));
            /*BusinessObject tbl_row_businessObject;
            if(selected_id==null)
            {
                if (getTableRow() != null)
                {
                    if (getTableRow().getItem() instanceof BusinessObject)
                    {
                        tbl_row_businessObject = (BusinessObject) getTableRow().getItem();
                        if (tbl_row_businessObject != null)
                        {
                            if(SessionManager.getInstance().getActive()!=null)
                            {
                                String url = tbl_row_businessObject.apiEndpoint() + "/" + tbl_row_businessObject.get_id();
                                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                                headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
                                try
                                {
                                    String obj_json = RemoteComms.sendGetRequest(url, headers);

                                    System.out.println(obj_json);
                                    if (obj_json != null)
                                    {
                                        Gson gson = new GsonBuilder().create();
                                        if(!obj_json.toString().equals("") && !obj_json.toString().equals("[]") &&
                                                !obj_json.toString().equals("{}") && obj_json.toString().contains("{"))
                                        {
                                            BusinessObject obj = gson.fromJson(obj_json, tbl_row_businessObject.getClass());

                                            for (BusinessObject combo_item : business_objects)
                                            {
                                                if (combo_item.get_id() != null)
                                                {
                                                    if (combo_item.get_id().equals(obj.get(property)))
                                                    {
                                                        String prop_val;
                                                    /*
                                                        If the combo items are of multiple data types then
                                                        Get the appropriate label for each data type.
                                                        Labels are passed through the label_property property -
                                                        If there are multi-types the labels are separated by the pipe (|) symbol.
                                                     *
                                                        String[] properties = label_property.split("\\|");
                                                        prop_val = getBusinessObjectProperty(properties, combo_item);

                                                        //if a valid label was found on the object then the combo box value and graphic are set
                                                        if (prop_val != null)
                                                        {
                                                            comboBox.setValue(prop_val);
                                                            setGraphic(comboBox);
                                                            IO.log(TAG, IO.TAG_INFO, String.format("set property value of '%s' on combo box.", prop_val));
                                                            break;
                                                        } else
                                                        {
                                                            IO.log(TAG, IO.TAG_WARN, String.format("property '%s' on object of type '%s' is null.", label_property, combo_item.getClass().getName()));
                                                        }
                                                    }
                                                } else
                                                {
                                                    IO.log(TAG, IO.TAG_WARN, "combo box item id is null.");
                                                }
                                            }
                                        }else{
                                            IO.log(getClass().getName(), IO.TAG_ERROR, "invalid JSON object ["+tbl_row_businessObject.get_id()+" type "+tbl_row_businessObject.getClass().getName()+"]\n" + obj_json);
                                        }
                                    } else
                                    {
                                        IO.log(TAG, IO.TAG_ERROR, "JSON data from server is null.");
                                    }
                                } catch (IOException e)
                                {
                                    IO.log(TAG, IO.TAG_ERROR, e.getMessage());
                                }
                            } else
                            {
                                IO.log(TAG, IO.TAG_ERROR, "no active sessions.");
                            }
                        } else
                        {
                            IO.log(TAG, IO.TAG_ERROR, "row object is null.");
                        }
                    } else
                    {
                        IO.log(TAG, IO.TAG_ERROR, "unknown row object: " + getTableRow().getItem());
                    }
                } else
                {
                    IO.log(TAG, IO.TAG_ERROR, "row is null.");
                }
            }else{
                if(business_objects!=null)
                {
                    for (BusinessObject combo_item : business_objects)
                    {
                        if (combo_item.get_id() != null)
                        {
                            if (combo_item.get_id().equals(selected_id))
                            {
                                String prop_val;
                            /*
                                If the combo items are of multiple data types then
                                Get the appropriate label for each data type.
                                Labels are passed through the label_property property -
                                If there are multi-types the labels are separated by the pipe (|) symbol.
                             *
                                String[] properties = label_property.split("\\|");
                                prop_val = getBusinessObjectProperty(properties, combo_item);

                                //if a valid label was found on the object then the combo box value and graphic are set
                                if (prop_val != null)
                                {
                                    comboBox.setValue(prop_val);
                                    setGraphic(comboBox);
                                    IO.log(TAG, IO.TAG_INFO, String.format("set property value of '%s' on combo box.", prop_val));
                                    break;
                                } else
                                {
                                    IO.log(TAG, IO.TAG_WARN, String.format("property '%s' on object of type '%s' is null.", label_property, combo_item.getClass().getName()));
                                }
                            }
                        } else
                        {
                            IO.log(TAG, IO.TAG_WARN, "combo box item id is null.");
                        }
                    }
                }else{
                    IO.log(TAG, IO.TAG_WARN, (getTableView().getItems().size()>0?"business objects of type " + getTableView().getItems().get(0).getClass().getName():"business objects of " + selected_id) + " are NULL.");
                }
            }*/
        /*if(selected_id!=null)
        {
            if(Globals.DEBUG_INFO.getValue().toLowerCase().equals("on"))
                System.out.println(String.format("ComboBox> info: selected id is '%s'.", selected_id));

            for (BusinessObject bo : business_objects)
            {
                if (bo.get_id().equals(selected_id))
                {
                    comboBox.setValue((String) bo.get(label_property));
                    if(Globals.DEBUG_INFO.getValue().toLowerCase().equals("on"))
                        System.out.println(String.format("ComboBox> info: selected '%s'.", (String) bo.get(label_property)));
                }
            }
            setGraphic(comboBox);
        } else
            if(Globals.DEBUG_ERRORS.getValue().toLowerCase().equals("on"))
                System.err.println("ComboBox> error: selected id is null, ignoring.");*/

        }

        @Override
        public void startEdit()
        {
            super.startEdit();
        }
    }
}
