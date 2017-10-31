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
import java.net.URL;
import java.time.DateTimeException;
import java.util.*;

/**
 * views Controller class
 *
 * @author ghost
 */
public class ViewQuoteController extends QuoteController
{
    @Override
    public void refreshView()
    {
        if(EmployeeManager.getInstance().getEmployees()==null)
        {
            IO.logAndAlert(getClass().getName(), "no employees were found in the database.", IO.TAG_ERROR);
            return;
        }
        if( ClientManager.getInstance().getClients()==null)
        {
            IO.logAndAlert(getClass().getName(), "no clients were found in the database.", IO.TAG_ERROR);
            return;
        }

        Employee[] employees = new Employee[EmployeeManager.getInstance().getEmployees().values().toArray().length];
        EmployeeManager.getInstance().getEmployees().values().toArray(employees);

        tblSaleReps.getItems().clear();
        tblQuoteItems.getItems().clear();

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
                                txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.computeQuoteTotal(tblQuoteItems.getItems())));
                                tblQuoteItems.refresh();
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
                                txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.computeQuoteTotal(tblQuoteItems.getItems())));
                                tblQuoteItems.refresh();
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
                                txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.computeQuoteTotal(tblQuoteItems.getItems())));
                                tblQuoteItems.refresh();
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

        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

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
        cbxClients.setItems(FXCollections.observableArrayList(ClientManager.getInstance().getClients().values()));
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
        cbxContactPerson.setItems(FXCollections.observableArrayList(employees));
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
            if(selected.getContact_person()==null)
            {
                IO.logAndAlert("View Quote Error", "Selected quote's contact person attribute is null.", IO.TAG_ERROR);
                return;
            }
            if(selected.getClient()==null)
            {
                IO.logAndAlert("View Quote Error", "Selected quote's client attribute is null.", IO.TAG_ERROR);
                return;
            }
            cbxClients.setValue(selected.getClient());
            cbxContactPerson.setValue(selected.getContact_person());
            txtCell.setText(selected.getContact_person().getCell());
            txtTel.setText(selected.getContact_person().getTel());
            txtEmail.setText(selected.getContact_person().getEmail());
            txtFax.setText(cbxClients.getValue().getFax());
            txtQuoteId.setText(selected.get_id());
            txtSite.setText(selected.getSitename());
            txtRequest.setText(selected.getRequest());
            txtVat.setText(String.valueOf(selected.getVat()));

            try
            {
                //String date = LocalDate.parse(new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(selected.getDate_generated()*1000))).toString();
                String date = new Date(selected.getDate_generated() * 1000).toString();
                txtDateGenerated.setText(date);
            } catch (DateTimeException e)
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }

            if (selected.getResources() != null)
                tblQuoteItems.setItems(FXCollections.observableArrayList(selected.getResources()));
            else IO.log(getClass().getName(), IO.TAG_WARN, "quote [" + selected.get_id() + "] has no resources.");
            if (selected.getRepresentatives() != null)
                tblSaleReps.setItems(FXCollections.observableArrayList(selected.getRepresentatives()));
            else IO.log(getClass().getName(), IO.TAG_WARN, "quote [" + selected
                    .get_id() + "] has no representatives.");

            /** Store additional cost cols in a HashMap -  used a map
             * To ensure that only a single instance of all additional
             * Cost columns are stored.
             **/
            //HashMap<String, TableColumn> map = new HashMap<>();
            //search for matching column for each additional cost
            for (QuoteItem item : tblQuoteItems.getItems())
            {
                if (item.getAdditional_costs() != null)
                {
                    for (String str_cost : item.getAdditional_costs().split(";"))
                    {
                        String[] arr = str_cost.split("=");
                        if (arr != null)
                        {
                            if (arr.length > 1)
                            {
                                TableColumn col = new TableColumn(arr[0]);

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
                                                if (getIndex() >= 0 && getIndex() < tblQuoteItems.getItems().size())
                                                {
                                                    QuoteItem quoteItem = tblQuoteItems.getItems().get(getIndex());
                                                    //update QuoteItem object on TextField commit
                                                    txt.setOnKeyPressed(event ->
                                                    {
                                                        if (event.getCode() == KeyCode.ENTER)
                                                        {
                                                            if (quoteItem != null)
                                                            {
                                                                String new_cost = col.getText()
                                                                        .toLowerCase() + "=" + txt.getText();
                                                                String old_add_costs = "";
                                                                if (quoteItem.getAdditional_costs() != null)
                                                                    old_add_costs = quoteItem.getAdditional_costs()
                                                                            .toLowerCase();
                                                                String new_add_costs = "";
                                                                int old_var_index = old_add_costs
                                                                        .indexOf(col.getText().toLowerCase());
                                                                //if(old_add_costs==null)
                                                                //if(old_add_costs.isEmpty())
                                                                if (old_var_index < 0)
                                                                {
                                                                    if (old_add_costs.isEmpty())
                                                                    {
                                                                        //pair DNE & no additional costs exist - append pair
                                                                        //** key-value pair is the first and only pair - add it w/o semi-colon
                                                                        new_add_costs += new_cost;
                                                                    }
                                                                    else
                                                                    {
                                                                        //pair DNE but other additional costs exist - append pair then add the rest of the pairs
                                                                        new_add_costs += new_cost + ";" + old_add_costs;//.substring(old_add_costs.indexOf(';')-1)
                                                                    }
                                                                }
                                                                else if (old_var_index == 0)
                                                                {
                                                                    //** key-value pair exists and is first pair.
                                                                    new_add_costs += new_cost;
                                                                    if (old_add_costs
                                                                            .indexOf(';') > 0)//if there are other pairs append them
                                                                        new_add_costs += ";" + old_add_costs
                                                                                .substring(old_add_costs
                                                                                        .indexOf(';') + 1);
                                                                }
                                                                else
                                                                {
                                                                    //** key-value pair is not first - append to additional costs.
                                                                    //copy additional costs before current cost
                                                                    new_add_costs = old_add_costs
                                                                            .substring(0, old_var_index - 1);
                                                                    //append current cost
                                                                    new_add_costs += ";" + new_cost;
                                                                    //append additional costs after current cost
                                                                    int i = old_add_costs.substring(old_var_index)
                                                                            .indexOf(';');
                                                                    new_add_costs += ";" + old_add_costs
                                                                            .substring(i + 1);
                                                                }
                                                                IO.log(getClass()
                                                                        .getName(), IO.TAG_INFO, "committed additional costs for quote item [#" +
                                                                        quoteItem
                                                                                .getItem_number() + "]:: " + new_add_costs);
                                                                quoteItem.setAdditional_costs(new_add_costs);

                                                                //RemoteComms.updateBusinessObjectOnServer(quote_item, "/api/quote/resource", column.getText());
                                                                //IO.logAndAlert("Success", "Successfully updated property '" + column.getText() + "' for quote item #" + quote_item.getItem_number(), IO.TAG_INFO);
                                                            }
                                                        }
                                                    });

                                                    //render the cell
                                                    if (!empty)// && item!=null
                                                    {
                                                        if (quoteItem.getAdditional_costs() == null)
                                                            txt.setText("0.0");
                                                        else if (quoteItem.getAdditional_costs().length() <= 0)
                                                            txt.setText("0.0");
                                                        else if (quoteItem.getAdditional_costs().length() > 0)
                                                        {
                                                            if (quoteItem.getAdditional_costs() != null)
                                                            {
                                                                for (String str_cost : quoteItem.getAdditional_costs().split(";"))
                                                                {
                                                                    String[] arr = str_cost.split("=");
                                                                    if (arr != null)
                                                                    {
                                                                        if (arr.length > 1)
                                                                        {
                                                                            if (arr[0].toLowerCase().equals(col.getText().toLowerCase()))
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
                                                    } else setGraphic(null);
                                                    getTableView().refresh();
                                                } //else IO.log("Quote Materials Table", IO.TAG_ERROR, "index out of bounds [" + getIndex() + "]");
                                            }
                                        };
                                    }
                                });
                                //if column absent from map, add it
                                if(colsMap.get(arr[0].toLowerCase())==null)
                                {
                                    col.setPrefWidth(80);
                                    tblQuoteItems.getColumns().add(col);
                                    colsMap.put(arr[0].toLowerCase(), col);
                                }
                            }
                        }
                    }
                }
            }
            tblQuoteItems.refresh();
        }else IO.logAndAlert("View Quote Error", "Selected quote is invalid.", IO.TAG_ERROR);

        txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(selected.getTotal()));
    }
}