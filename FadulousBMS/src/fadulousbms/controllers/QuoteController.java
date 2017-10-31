/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.*;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.DateTimeException;
import java.util.*;

/**
 * views Controller class
 *
 * @author ghost
 */
public abstract class QuoteController extends Screen implements Initializable
{
    protected boolean itemsModified;

    @FXML
    protected TableView<QuoteItem> tblQuoteItems;
    @FXML
    protected TableView<Employee> tblSaleReps;
    @FXML
    protected TableColumn colFirstname,colLastname,colCell,colEmail,colTel,colGender,
                        colActive,colTotal,colAction,colEmployeeAction;
    @FXML
    protected TableColumn colMarkup,colQuantity,colLabour;
    @FXML
    protected ComboBox<Client> cbxClients;
    @FXML
    protected ComboBox<Employee> cbxContactPerson;
    @FXML
    protected TextField txtCell,txtTel,txtTotal,txtQuoteId,txtFax,txtEmail,txtSite,txtVat,txtDateGenerated,txtExtra;
    @FXML
    protected TextArea txtRequest;
    protected HashMap<String, TableColumn> colsMap = new HashMap<>();

    @Override
    public abstract void refreshView();


    @Override
    public void refreshModel()
    {
        EmployeeManager.getInstance().initialize();
        ResourceManager.getInstance().initialize();
        ClientManager.getInstance().initialize();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        colAction.setCellFactory(new ButtonTableCellFactory<>());
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
                                btnAdd.getStyleClass().add("btnAdd");
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
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        //TODO: deal with server side
                                        QuoteItem quoteItem = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(quoteItem);
                                        getTableView().refresh();
                                        //txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.computeQuoteTotal(QuoteManager.getInstance().getSelectedQuote())));
                                        txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.getInstance().getSelectedQuote().getTotal()));
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

        Callback<TableColumn<Employee, String>, TableCell<Employee, String>> actionColCellFactory
                =
                new Callback<TableColumn<Employee, String>, TableCell<Employee, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Employee, String> param)
                    {
                        final TableCell<Employee, String> cell = new TableCell<Employee, String>()
                        {
                            final Button btnRemove = new Button("Remove");

                            @Override
                            public void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);

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
                                    btnRemove.setOnAction(event ->
                                    {
                                        Employee employee = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(employee);
                                        getTableView().refresh();
                                        //TODO: remove from server
                                        System.out.println("Successfully removed sale representative: " + employee.toString());
                                    });
                                    setGraphic(btnRemove);
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }
                };

        colEmployeeAction.setMinWidth(120);
        colEmployeeAction.setCellFactory(actionColCellFactory);
    }

    public void addQuoteItemAdditionalMaterial(QuoteItem quoteItem)
    {
        Stage stage = new Stage();
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);

        TextField txtName = new TextField();
        txtName.setMinWidth(150);
        Label lblName = new Label("Material name: ");
        lblName.setMinWidth(150);

        TextField txtCost = new TextField();
        txtCost.setMinWidth(150);
        Label lblCost = new Label("Material value: ");
        lblCost.setMinWidth(150);

        TextField txtMarkup = new TextField();
        txtMarkup.setMinWidth(150);
        Label lblMarkup = new Label("Markup [%]: ");
        lblMarkup.setMinWidth(150);

        Button btnAdd = new Button("Add");
        btnAdd.getStyleClass().add("btnAdd");
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
            if(quoteItem.getAdditional_costs()==null)
                quoteItem.setAdditional_costs(new_cost);
            else if(quoteItem.getAdditional_costs().isEmpty())
                quoteItem.setAdditional_costs(new_cost);
            else
            {
                //if additional cost exists already, update its value
                if(quoteItem.getAdditional_costs().toLowerCase().contains(txtName.getText().toLowerCase()))
                {
                    String old_add_costs = quoteItem.getAdditional_costs().toLowerCase();
                    String new_add_costs="";
                    int old_var_index = old_add_costs.indexOf(txtName.getText().toLowerCase());
                    if(old_var_index==0)
                    {
                        //key-value pair is first add it w/o semi-colon
                        new_add_costs += new_cost;
                    }else
                    {
                        new_add_costs = old_add_costs.substring(0, old_var_index);
                        new_add_costs += ";" + new_cost;
                    }
                    quoteItem.setAdditional_costs(new_add_costs);
                } else quoteItem.setAdditional_costs(quoteItem.getAdditional_costs()+";"+new_cost);
            }

            //RemoteComms.updateBusinessObjectOnServer(quoteItem, "/api/quote/resource", txtName.getText());

            TableColumn<QuoteItem, String> col = new TableColumn(txtName.getText());
            col.setPrefWidth(80);
            col.setCellFactory(new Callback<TableColumn<QuoteItem, String>, TableCell<QuoteItem, String>>()
            {
                @Override
                public TableCell<QuoteItem, String> call(TableColumn<QuoteItem, String> param)
                {
                    return new TableCell<QuoteItem, String>()
                    {
                        final TextField txt = new TextField("0.0");

                        @Override
                        protected void updateItem(String item, boolean empty)
                        {
                            super.updateItem(item, empty);

                            //update QuoteItem object
                            txt.textProperty().addListener((observable, oldValue, newValue) ->
                            {
                                if(txt.isFocused())
                                {
                                    QuoteItem quote_item = getTableView().getItems().get(getIndex());
                                    String add_costs = quote_item.getAdditional_costs();
                                    if(add_costs.contains(col.getText()+"="+oldValue))
                                        add_costs = add_costs.replace(col.getText()+"="+oldValue,col.getText()+"="+newValue);
                                    else add_costs=add_costs.length()>0?add_costs+";"+col.getText()+"="+newValue:col.getText()+"="+newValue;
                                    //System.out.println("Committed text: "+add_costs+", old: " + oldValue + ", new: " + newValue);
                                    quote_item.setAdditional_costs(add_costs);
                                    RemoteComms.updateBusinessObjectOnServer(quote_item, "/api/quote/resource", col.getText());
                                    setText("R "+newValue);
                                    setGraphic(null);
                                    getTableView().refresh();
                                }
                            });

                            //render the cell
                            if(!empty)
                            {
                                //txt.setText("0.0");
                                QuoteItem quote_item = getTableView().getItems().get(getIndex());
                                if(quote_item.getAdditional_costs()!=null)
                                {
                                    for(String str_cost: quote_item.getAdditional_costs().split(";"))
                                    {
                                        String[] arr = str_cost.split("=");
                                        if(arr!=null)
                                            if(arr.length>1)
                                                if(arr[0].toLowerCase().equals(col.getText().toLowerCase()))
                                                {
                                                    txt.setText(arr[1]);
                                                    break;
                                                }
                                    }
                                }
                                txt.setPrefWidth(50);
                                setGraphic(txt);
                                getTableView().refresh();
                            }else setGraphic(null);
                        }
                    };
                }
            });
            /*boolean found=false;
            for(TableColumn c: tblQuoteItems.getColumns())
                if(col.getText().toLowerCase().equals(c.getText().toLowerCase()))
                {
                    found=true;
                    break;
                }
            if(!found)
                tblQuoteItems.getColumns().add(col);
            tblQuoteItems.refresh();*/
            tblQuoteItems.getColumns().add(col);
            tblQuoteItems.refresh();

            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.getInstance().getSelectedQuote().getTotal()));
        });

        HBox row1 = new HBox(lblName, txtName);
        HBox row2 = new HBox(lblCost, txtCost);
        HBox row3 = new HBox(lblMarkup, txtMarkup);
        row1.setSpacing(20);
        row2.setSpacing(20);
        row3.setSpacing(20);
        //HBox row3 = new HBox(new Label("Markup"), txtMarkup);

        stage.setTitle("Extra Costs For Quote Item #"+quoteItem.getItem_numberValue());
        stage.setScene(new Scene(new VBox(row1, row2, row3, btnAdd)));
        stage.show();
    }

    @FXML
    public void newQuoteItem()
    {
        if(ResourceManager.getInstance()!=null)
        {
            if(ResourceManager.getInstance().getResources()!=null)
            {
                if(ResourceManager.getInstance().getResources().size()>0)
                {
                    ComboBox<Resource> resourceComboBox = new ComboBox<>();
                    resourceComboBox.setMinWidth(120);
                    resourceComboBox.setItems(FXCollections.observableArrayList(ResourceManager.getInstance().getResources().values()));
                    HBox.setHgrow(resourceComboBox, Priority.ALWAYS);

                    Button btnAdd = new Button("Add");
                    btnAdd.setMinWidth(80);
                    btnAdd.setMinHeight(40);
                    btnAdd.setDefaultButton(true);
                    btnAdd.getStyleClass().add("btnApply");
                    btnAdd.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    Button btnNewMaterial = new Button("New Material");
                    btnNewMaterial.setMinWidth(80);
                    btnNewMaterial.setMinHeight(40);
                    btnNewMaterial.setDefaultButton(true);
                    btnNewMaterial.getStyleClass().add("btnAdd");
                    btnNewMaterial.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    Button btnCancel = new Button("Close");
                    btnCancel.setMinWidth(80);
                    btnCancel.setMinHeight(40);
                    btnCancel.getStyleClass().add("btnBack");
                    btnCancel.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    HBox hBox = new HBox(new Label("Resource: "), resourceComboBox);
                    HBox.setHgrow(hBox, Priority.ALWAYS);
                    hBox.setSpacing(20);

                    HBox hBoxButtons = new HBox(btnAdd, btnNewMaterial, btnCancel);
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
                            quoteItem.setQuantity(1);
                            quoteItem.setLabour(0);
                            quoteItem.setMarkup(0);
                            quoteItem.setResource_id(resourceComboBox.getValue().get_id());
                            //quoteItem.setEquipment_description(resourceComboBox.getValue().getResource_description());
                            //quoteItem.setUnit(resourceComboBox.getValue().getUnit());
                            //quoteItem.setRate(resourceComboBox.getValue().getResource_value());
                            //quoteItem.setValue(resourceComboBox.getValue().getResource_value());
                            //quoteItem.setResource(resourceComboBox.getValue());
                            //quoteItem.setEquipment_name(resourceComboBox.getValue().getResource_name());

                            tblQuoteItems.getItems().add(quoteItem);
                            tblQuoteItems.refresh();

                            itemsModified = true;

                            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " +
                                    String.valueOf(QuoteManager.computeQuoteTotal(QuoteManager.getInstance().getSelectedQuote())));

                        } else IO.logAndAlert("New Quote Resource", "Invalid resource selected.", IO.TAG_ERROR);
                    });

                    /*btnNewMaterial.setOnAction(event ->
                            ResourceManager.getInstance().newResourceWindow(param ->
                            {
                                new Thread(() ->
                                {
                                    refreshModel();
                                    Platform.runLater(() -> refreshView());
                                }).start();
                                return null;
                            }));*/

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
                if(EmployeeManager.getInstance().getEmployees().size()>0)
                {
                    Employee[] employees = new Employee[EmployeeManager.getInstance().getEmployees().size()];
                    EmployeeManager.getInstance().getEmployees().values().toArray(employees);

                    ComboBox<Employee> employeeComboBox = new ComboBox<>();
                    employeeComboBox.setMinWidth(120);
                    employeeComboBox.setItems(FXCollections.observableArrayList(employees));
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

    public void createQuote()
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
        if(!Validators.isValidNode(txtVat, txtVat.getText(), 1, ".+"))
        {
            txtVat.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
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
            IO.logAndAlert("Invalid Quote", "Quote has no materials", IO.TAG_ERROR);
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
        String str_contact = cbxContactPerson.getValue().getUsr();
        String str_site = txtSite.getText();
        String str_vat = txtVat.getText();
        String str_extra = null;//txtExtra.getText();

        //prepare quote attributes
        Quote quote = new Quote();
        quote.setClient_id(str_company);
        quote.setContact_person_id(str_contact);
        quote.setSitename(str_site);
        quote.setRequest(txtRequest.getText());
        quote.setStatus(0);
        quote.setVat(Double.parseDouble(str_vat));
        quote.setCreator(SessionManager.getInstance().getActive().getUsername());
        quote.setRevision(1.0);
        //QuoteItem[] items = new QuoteItem[quoteItems.size()];
        //quoteItems.toArray(items);
        //quote.setResources(items);
        //Employee[] reps = new Employee[quoteReps.size()];
        //quoteReps.toArray(reps);
        //quote.setRepresentatives(reps);
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
                        IO.logAndAlert("New Quote Creation Error", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }
                    if(response.isEmpty())
                    {
                        IO.logAndAlert("New Quote Creation Error", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }
                    txtQuoteId.setText(response);

                    //Close connection
                    if(connection!=null)
                        connection.disconnect();
                    /* Add Quote Representatives/Employees to Quote on database*/

                    boolean added_all_quote_reps = true;
                    for(Employee employee : quoteReps)
                    {
                        //prepare parameters for quote resources.
                        ArrayList params = new ArrayList<>();
                        params.add(new AbstractMap.SimpleEntry<>("quote_id", response));
                        params.add(new AbstractMap.SimpleEntry<>("usr", employee.getUsr()));
                        added_all_quote_reps = QuoteManager.getInstance().createQuoteRep(response, params, headers);
                    }
                    if(!added_all_quote_reps)
                        IO.logAndAlert("New Quote Representative Creation Failure", "Could not add representatives to quote, however, the quote["+response+"] has been created.", IO.TAG_INFO);


                    //Close connection
                    if(connection!=null)
                        connection.disconnect();
                    /* Add Quote Resources to Quote on database */

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
                        //added_all_quote_items = QuoteManager.getInstance().createQuoteItem(response, params, headers);

                        quoteItem.setQuote_id(response);

                        connection = RemoteComms.postData("/api/quote/resource/add", quoteItem.asUTFEncodedString(), headers);
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
                        }else IO.logAndAlert("New Quote Item Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
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
                        ScreenManager.getInstance().showLoadingScreen(param ->
                        {
                            new Thread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    try
                                    {
                                        if(ScreenManager.getInstance().loadScreen(Screens.VIEW_QUOTE.getScreen(),getClass().getResource("../views/"+Screens.VIEW_QUOTE.getScreen())))
                                        {
                                            Platform.runLater(() -> ScreenManager.getInstance().setScreen(Screens.VIEW_QUOTE.getScreen()));
                                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load quote viewer screen.");
                                    } catch (IOException e)
                                    {
                                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                    }
                                }
                            }).start();
                            return null;
                        });
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
        if(!Validators.isValidNode(txtVat, txtVat.getText(), 1, ".+"))
        {
            txtVat.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
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

        String str_site = txtSite.getText();
        String str_vat = txtVat.getText();
        //String str_extra = txtExtra.getText();


        Quote selected = QuoteManager.getInstance().getSelectedQuote();
        if(selected!=null)
        {
            selected.setClient_id(cbxClients.getValue().get_id());
            selected.setContact_person_id(cbxContactPerson.getValue().getUsr());
            selected.setVat(Double.parseDouble(str_vat));
            selected.setSitename(str_site);
            selected.setRequest(txtRequest.getText());
            /*if (str_extra != null)
                selected.parse("extra", str_extra);*/
            //QuoteManager.getInstance().updateQuote(selected, ((QuoteItem[]) tblQuoteItems.getItems().toArray()), ((Employee[])tblSaleReps.getItems().toArray()));
            QuoteManager.getInstance().updateQuote(selected, tblQuoteItems.getItems(), tblSaleReps.getItems());

            refreshView();
            //tblQuoteItems.refresh();
            //tblSaleReps.refresh();
        }
    }

    @FXML
    public void createJob()
    {
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                Quote selected = QuoteManager.getInstance().getSelectedQuote();
                if(selected!=null)
                {
                    Job job = new Job();
                    job.setQuote_id(selected.get_id());
                    /*if(JobManager.getInstance().getJobs()!=null)
                        job.setJob_number(JobManager.getInstance().getJobs().length);
                    else job.setJob_number(0);*/
                    String new_job_id = JobManager.getInstance().createNewJob(job);
                    if(new_job_id!=null)
                    {
                        IO.logAndAlert("Success", "Successfully created a new job.", IO.TAG_INFO);
                        JobManager.getInstance().loadDataFromServer();
                        if(JobManager.getInstance().getJobs()!=null)
                        {
                            JobManager.getInstance().setSelectedJob(JobManager.getInstance().getJobs().get(new_job_id));
                            ScreenManager.getInstance().showLoadingScreen(param ->
                            {
                                new Thread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        try
                                        {
                                            if (ScreenManager.getInstance()
                                                    .loadScreen(Screens.VIEW_JOB.getScreen(), getClass()
                                                            .getResource("../views/" + Screens.VIEW_JOB.getScreen())))
                                            {
                                                Platform.runLater(() -> ScreenManager.getInstance()
                                                        .setScreen(Screens.VIEW_JOB.getScreen()));
                                            }
                                            else IO.log(getClass()
                                                    .getName(), IO.TAG_ERROR, "could not load job viewer screen.");
                                        } catch (IOException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        }
                                    }
                                }).start();
                                return null;
                            });
                        } else IO.logAndAlert("Error", "Could not find any jobs in the database.", IO.TAG_INFO);
                    }else IO.logAndAlert("Error", "Could not successfully create a new job.", IO.TAG_INFO);
                }else IO.logAndAlert("Cannot Create Job", "Cannot create job because the selected quote is invalid.", IO.TAG_ERROR);
            }else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    @FXML
    public void newClient()
    {
        ClientManager.getInstance().newClientWindow(param ->
        {
            new Thread(() ->
            {
                refreshModel();
                Platform.runLater(() -> refreshView());
            }).start();
            return null;
        });
    }

    @FXML
    public void newEmployee()
    {
        EmployeeManager.getInstance().newEmployeeWindow(param ->
        {
            new Thread(() ->
            {
                refreshModel();
                Platform.runLater(() -> refreshView());
            }).start();
            return null;
        });
    }

    @FXML
    public void createPDF()
    {
        try
        {
            PDF.createQuotePdf(QuoteManager.getInstance().getSelectedQuote());
        } catch (IOException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
        }
    }

    @FXML
    public void previousScreen()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.OPERATIONS.getScreen(),getClass().getResource("../views/"+Screens.OPERATIONS.getScreen())))
                        {
                            //Platform.runLater(() ->
                            screenManager.setScreen(Screens.OPERATIONS.getScreen());
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load operations screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }
}
