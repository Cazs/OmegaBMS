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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.swing.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.Remote;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.*;

/**
 * views Controller class
 *
 * @author ghost
 */
public class ViewQuoteController extends Screen implements Initializable
{
    private boolean itemsModified;

    @FXML
    private TableView<QuoteItem> tblQuoteItems;
    @FXML
    private TableView<Employee> tblSaleReps;
    @FXML
    private TableColumn colFirstname,colLastname,colCell,colEmail,colTel,colGender,
                        colActive,colAction,colEmployeeAction;
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
    //private TableColumn<QuoteItem, String> col;

    @Override
    public void refresh()
    {
        ResourceManager.getInstance().initialize(this.getScreenManager());

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
        colMarkup.setCellFactory(param -> new TableCell()
        {
            final TextField txt = new TextField("0.0");

            @Override
            protected void updateItem(Object item, boolean empty)
            {
                super.updateItem(item, empty);
                if (getIndex() >= 0 && getIndex() < tblQuoteItems.getItems().size())
                {
                    QuoteItem quoteItem = tblQuoteItems.getItems().get(getIndex());
                    //update QuoteItem object on TextField commit
                    txt.setOnKeyPressed(event ->
                    {
                        if(event.getCode()== KeyCode.ENTER)
                        {
                            QuoteItem quote_item = (QuoteItem) getTableView().getItems().get(getIndex());
                            try
                            {
                                quote_item.setMarkup(Double.valueOf(txt.getText()));
                                RemoteComms.updateBusinessObjectOnServer(quote_item, "/api/quote/resource", "markup");
                            }catch (NumberFormatException e)
                            {
                                IO.logAndAlert("Error","Please enter a valid markup percentage.", IO.TAG_ERROR);
                                return;
                            }
                            IO.logAndAlert("Success","Successfully updated markup percentage property for quote item #" + quote_item.getItem_number(), IO.TAG_INFO);
                        }
                    });

                    if (!empty)
                    {
                        txt.setText(quoteItem.getMarkup());
                        setGraphic(txt);
                    } else setGraphic(null);
                    getTableView().refresh();
                }
            }
        });

        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        //colQuantity.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("quantity", "quantity", callback));
        colQuantity.setCellFactory(param -> new TableCell()
        {
            final TextField txt = new TextField("0.0");

            @Override
            protected void updateItem(Object item, boolean empty)
            {
                super.updateItem(item, empty);
                if (getIndex() >= 0 && getIndex() < tblQuoteItems.getItems().size())
                {
                    QuoteItem quoteItem = tblQuoteItems.getItems().get(getIndex());
                    //update QuoteItem object on TextField commit
                    txt.setOnKeyPressed(event ->
                    {
                        if(event.getCode()== KeyCode.ENTER)
                        {
                            QuoteItem quote_item = (QuoteItem) getTableView().getItems().get(getIndex());
                            try
                            {
                                quote_item.setQuantity(Integer.valueOf(txt.getText()));
                                RemoteComms.updateBusinessObjectOnServer(quote_item, "/api/quote/resource", "quantity");
                            }catch (NumberFormatException e)
                            {
                                IO.logAndAlert("Error","Please enter a valid quantity.", IO.TAG_ERROR);
                                return;
                            }
                            IO.logAndAlert("Success","Successfully updated item quantity property for quote item #" + quote_item.getItem_number(), IO.TAG_INFO);
                        }
                    });

                    if (!empty)
                    {
                        txt.setText(quoteItem.getQuantity());
                        setGraphic(txt);
                    } else setGraphic(null);
                    getTableView().refresh();
                }
            }
        });

        colLabour.setCellValueFactory(new PropertyValueFactory<>("labour"));
        //colLabour.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("labour", "labour", callback));
        colLabour.setCellFactory(param -> new TableCell()
        {
            final TextField txt = new TextField("0.0");

            @Override
            protected void updateItem(Object item, boolean empty)
            {
                super.updateItem(item, empty);
                if (getIndex() >= 0 && getIndex() < tblQuoteItems.getItems().size())
                {
                    QuoteItem quoteItem = tblQuoteItems.getItems().get(getIndex());
                    //update QuoteItem object on TextField commit
                    txt.setOnKeyPressed(event ->
                    {
                        if(event.getCode()== KeyCode.ENTER)
                        {
                            QuoteItem quote_item = (QuoteItem) getTableView().getItems().get(getIndex());
                            try
                            {
                                quote_item.setLabour(Double.valueOf(txt.getText()));
                                RemoteComms.updateBusinessObjectOnServer(quote_item, "/api/quote/resource", "labour");
                            }catch (NumberFormatException e)
                            {
                                IO.logAndAlert("Error","Please enter a valid labour cost.", IO.TAG_ERROR);
                                return;
                            }
                            IO.logAndAlert("Success","Successfully updated labour cost property for quote item #" + quote_item.getItem_number(), IO.TAG_INFO);
                        }
                    });
                    if (!empty)
                    {
                        txt.setText(quoteItem.getLabour());
                        setGraphic(txt);
                    } else setGraphic(null);
                    getTableView().refresh();
                }
            }
        });

        //Setup Sale Reps table
        colFirstname.setCellValueFactory(new PropertyValueFactory<>("firstname"));
        colLastname.setCellValueFactory(new PropertyValueFactory<>("lastname"));
        colCell.setCellValueFactory(new PropertyValueFactory<>("cell"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("tel"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

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
        Quote selected = QuoteManager.getInstance().getSelectedQuote();
        if(selected!=null)
        {
            cbxClients.setValue(selected.getClient());
            cbxContactPerson.setValue(selected.getContactPerson());
            txtCell.setText(selected.getContactPerson().getCell());
            txtTel.setText(selected.getContactPerson().getTel());
            txtEmail.setText(selected.getContactPerson().getEmail());
            txtFax.setText(cbxClients.getValue().getFax());
            txtQuoteId.setText(selected.get_id());
            txtSite.setText(selected.getSitename());
            txtRequest.setText(selected.getRequest());

            try
            {
                //String date = LocalDate.parse(new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(selected.getDate_generated()*1000))).toString();
                String date = new Date(selected.getDate_generated()*1000).toString();
                txtDateGenerated.setText(date);
            }catch (DateTimeException e)
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }

            if (selected.getResources() != null)
                tblQuoteItems.setItems(FXCollections.observableArrayList(selected.getResources()));
            else IO.log(getClass().getName(), IO.TAG_WARN, "quote [" + selected.get_id() + "] has no resources.");
            if (selected.getRepresentatives() != null)
                tblSaleReps.setItems(FXCollections.observableArrayList(selected.getRepresentatives()));
            else IO.log(getClass().getName(), IO.TAG_WARN, "quote [" + selected.get_id() + "] has no representatives.");

            /** Store additional cost cols in a HashMap -  used a map
             * To ensure that only a single instance of all additional
             * Cost columns are stored.
             **/
            HashMap<String, TableColumn> map = new HashMap<>();
            //for(TableColumn column: tblQuoteItems.getColumns())
            //    map.putIfAbsent(column.getText().toLowerCase(), column);
            //search for matching column for each additional cost
            for(QuoteItem item: tblQuoteItems.getItems())
            {
                /*if(item.getAdditional_costs()==null)
                {
                    IO.log(getClass().getName(), IO.TAG_INFO, "quote resource ["+item.get_id()+"] has no additional costs. [null]. skipping..");
                    continue;
                }
                if(item.getAdditional_costs().length()<=0)
                {
                    IO.log(getClass().getName(), IO.TAG_INFO, "quote resource ["+item.get_id()+"] has no additional costs. skipping..");
                    continue;
                }*/
                for(String str_cost: item.getAdditional_costs().split(";"))
                {
                    String[] arr = str_cost.split("=");
                    if (arr != null)
                    {
                        if(arr.length>1)
                        {
                            TableColumn col;
                            //if column absent from map, add it
                            if (map.get(arr[0].toLowerCase()) == null)
                            {
                                col = new TableColumn(arr[0]);
                                col.setPrefWidth(80);
                                map.putIfAbsent(arr[0].toLowerCase(), col);
                            } else col = map.get(arr[0].toLowerCase());
                        }
                    }
                }
            }
            //HashMap<String, TableColumn> cols_map = new HashMap<>();
            //for(TableColumn column: tblQuoteItems.getColumns())
            //    cols_map.putIfAbsent(column.getText().toLowerCase(), column);

            //tblQuoteItems.getColumns().clear();
            //for each additional cost column, check if its not already added to the table
            for(TableColumn column: map.values())
            {
                boolean found=false;
                for(TableColumn col: tblQuoteItems.getColumns())
                {
                    if (col.getText().toLowerCase().equals(column.getText().toLowerCase()))
                    {
                        found = true;
                        break;
                    }
                }
                if(!found)
                {
                    tblQuoteItems.getColumns().add(column);
                } else IO.log(getClass().getName(), IO.TAG_INFO, "TableColumn ["+column.getText()+"] has already been added to the TableView.");

                column.setCellFactory(new Callback<TableColumn<QuoteItem, String>, TableCell<QuoteItem, String>>()
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
                                    if (getIndex() >= 0 && getIndex() < tblQuoteItems.getItems().size())
                                    {
                                        //System.out.println(tblQuoteItems.getItems().get(getIndex()).getEquipment_name());
                                        QuoteItem quoteItem = tblQuoteItems.getItems().get(getIndex());
                                        //update QuoteItem object on TextField commit
                                        txt.setOnKeyPressed(event ->
                                        {
                                            if(event.getCode()== KeyCode.ENTER)
                                            {
                                                QuoteItem quote_item = getTableView().getItems().get(getIndex());
                                                if (quote_item != null)
                                                {
                                                    String new_cost = column.getText().toLowerCase() + "=" + txt.getText();
                                                    String old_add_costs="";
                                                    if(quote_item.getAdditional_costs()!=null)
                                                        old_add_costs = quote_item.getAdditional_costs().toLowerCase();
                                                    String new_add_costs = "";
                                                    int old_var_index = old_add_costs.indexOf(column.getText().toLowerCase());
                                                    //if(old_add_costs==null)
                                                    //if(old_add_costs.isEmpty())
                                                    if (old_var_index < 0)
                                                    {
                                                        if (old_add_costs.isEmpty())
                                                        {
                                                            //pair DNE & no additional costs exist - append pair
                                                            //** key-value pair is the first and only pair - add it w/o semi-colon
                                                            new_add_costs += new_cost;
                                                        } else
                                                        {
                                                            //pair DNE but other additional costs exist - append pair then add the rest of the pairs
                                                            new_add_costs += new_cost + ";" + old_add_costs;//.substring(old_add_costs.indexOf(';')-1)
                                                        }
                                                    } else if (old_var_index == 0)
                                                    {
                                                        //** key-value pair exists and is first pair.
                                                        new_add_costs += new_cost;
                                                        if (old_add_costs.indexOf(';') > 0)//if there are other pairs append them
                                                            new_add_costs += ";" + old_add_costs.substring(old_add_costs.indexOf(';') + 1);
                                                    } else
                                                    {
                                                        //** key-value pair is not first - append to additional costs.
                                                        //copy additional costs before current cost
                                                        new_add_costs = old_add_costs.substring(0, old_var_index - 1);
                                                        //append current cost
                                                        new_add_costs += ";" + new_cost;
                                                        //append additional costs after current cost
                                                        int i = old_add_costs.substring(old_var_index).indexOf(';');
                                                        new_add_costs += ";" + old_add_costs.substring(i + 1);
                                                    }
                                                    System.out.println("new additional costs for quote item [#" + quote_item.getItem_number() + "]:: " + new_add_costs);
                                                    quote_item.setAdditional_costs(new_add_costs);

                                                    /*String add_costs = quote_item.getAdditional_costs().toLowerCase();
                                                    if (add_costs.contains(column.getText().toLowerCase()))// + "=" + oldValue))
                                                        add_costs = add_costs.replace(column.getText() + "=" + oldValue, column.getText() + "=" + newValue);
                                                    else
                                                        add_costs = add_costs.length() > 0 ? add_costs + ";" + column.getText() + "=" + newValue : column.getText() + "=" + newValue;
                                                    quote_item.setAdditional_costs(add_costs)*/
                                                    //System.out.println("Committed text: "+add_costs+", old: " + oldValue + ", new: " + newValue);

                                                    RemoteComms.updateBusinessObjectOnServer(quote_item, "/api/quote/resource", column.getText());
                                                    IO.logAndAlert("Success", "Successfully updated property '" + column.getText() + "' for quote item #" + quote_item.getItem_number(), IO.TAG_INFO);
                                                }
                                            }
                                        });

                                        //render the cell
                                        if (!empty)// && item!=null
                                        {
                                            /*if(column.getText().toLowerCase().equals("markup"))
                                            {
                                                txt.setText(quoteItem.getMarkup());
                                            } else*/
                                            {
                                                if (quoteItem.getAdditional_costs() == null)
                                                {
                                                    txt.setText("0.0");
                                                } else if (quoteItem.getAdditional_costs().length() <= 0)
                                                {
                                                    txt.setText("0.0");
                                                } else if (quoteItem.getAdditional_costs().length() > 0)
                                                {
                                                    //QuoteItem quote_item = getTableView().getItems().get(getIndex());
                                                    if (quoteItem.getAdditional_costs() != null)
                                                    {
                                                        for (String str_cost : quoteItem.getAdditional_costs().split(";"))
                                                        {
                                                            String[] arr = str_cost.split("=");
                                                            if (arr != null)
                                                            {
                                                                if (arr.length > 1)
                                                                    if (arr[0].toLowerCase().equals(column.getText().toLowerCase()))
                                                                    {
                                                                        txt.setText(arr[1]);
                                                                        break;
                                                                    }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            txt.setPrefWidth(50);
                                            setGraphic(txt);
                                            getTableView().refresh();
                                        } else
                                        {
                                            setGraphic(null);
                                            getTableView().refresh();
                                        }
                                    } else
                                        IO.log("Quote Resources Table", IO.TAG_ERROR, "index out of bounds [" + getIndex() + "]");
                                }
                            };
                        }
                    });
            }

            /*for(TableColumn tc : tblQuoteItems.getColumns())
            {
                if(tc.getText().toLowerCase().equals("markup"))
                    tc.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("markup", "markup", callback));
            }*/

            /*for(TableColumn column: cols_map.values())
            {
                if(column.getText().toLowerCase().equals("markup"))
                {
                    System.out.println("set factory for markup");
                    column.setCellValueFactory(new PropertyValueFactory<>("markup"));
                    column.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("markup", "markup", callback));
                }
                tblQuoteItems.getColumns().add(column);
            }*/
            //Setup Quote Items table
            /*colMarkup.setCellValueFactory(new PropertyValueFactory<>("markup"));
            colMarkup.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("markup", "markup", callback));

            colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            colQuantity.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("quantity", "quantity", callback));

            colLabour.setCellValueFactory(new PropertyValueFactory<>("labour"));
            colLabour.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("labour", "labour", callback));*/
            //tblQuoteItems.getColumns().clear();
            tblQuoteItems.refresh();
        }else IO.logAndAlert("View Quote", "Selected Quote is invalid.", IO.TAG_ERROR);

        computeQuoteTotal();
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
                                        System.out.println("Successfully added material quote number " + quoteItem.getItem_number());
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        QuoteItem quoteItem = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(quoteItem);
                                        getTableView().refresh();
                                        computeQuoteTotal();
                                        System.out.println("Successfully removed quote item " + quoteItem.getItem_number());
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
            if(quoteItem.getAdditional_costs()==null)
                quoteItem.setAdditional_costs(new_cost);
            else if(quoteItem.getAdditional_costs().isEmpty())
                quoteItem.setAdditional_costs(new_cost);
            else
            {
                //if additional cost exists already, update its value
                if(quoteItem.getAdditional_costs().toLowerCase().contains(txtName.getText().toLowerCase()))
                {
                    System.out.println("item already has additional costs.");
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
            boolean found=false;
            for(TableColumn c: tblQuoteItems.getColumns())
                if(col.getText().toLowerCase().equals(c.getText().toLowerCase()))
                {
                    found=true;
                    break;
                }
            if(!found)
                tblQuoteItems.getColumns().add(col);
            tblQuoteItems.refresh();
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
                if(txtQuoteId.getText()!=null)
                {
                    if(!txtQuoteId.getText().isEmpty())
                    {
                        updateQuote();
                        return;
                    }
                }
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

    public void newJob()
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
                        {
                            createJob();
                        }else IO.logAndAlert("Cannot Create Sale", "Cannot create sale because the selected quote is invalid.", IO.TAG_ERROR);
                    }
                } else IO.logAndAlert("Invalid Quote", "Cannot create sale from this quote because the quote data is invalid.", IO.TAG_ERROR);
            }else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

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

                    if(JobManager.getInstance().createJob(job))
                        IO.logAndAlert("Success", "Successfully created a new job.", IO.TAG_INFO);
                    else IO.logAndAlert("Error", "Could not successfully create a new job.", IO.TAG_INFO);
                }else IO.logAndAlert("Cannot Create Job", "Cannot create job because the selected quote is invalid.", IO.TAG_ERROR);
            }else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
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
                    IO.log(TAG, IO.TAG_INFO, "selected: " + business_objects[selected_pos]);
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
