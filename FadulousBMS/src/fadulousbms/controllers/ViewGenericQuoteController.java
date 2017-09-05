/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Screen;
import fadulousbms.auxilary.Validators;
import fadulousbms.managers.QuoteManager;
import fadulousbms.managers.ResourceManager;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
public class ViewGenericQuoteController implements Initializable, Screen
{
    private ScreenManager screen_mgr;
    private boolean itemsModified;
    private Date date_generated;

    @FXML
    private TableView<GenericQuoteItem> tblGenericQuoteItems;
    @FXML
    private TableColumn colEquipmentName,colDescription,
                        colUnit,colQuantity,colRate,colMarkup,
                        colValue,colLabour,colAction;
    @FXML
    private TextField txtCompany,txtContact,txtCell,txtTel,txtFax,
                        txtEmail,txtSite,txtExtra;
    @FXML
    private TextArea txtRequest;
    @FXML
    private ToggleButton btnStatus;

    @Override
    public void refresh()
    {
        QuoteManager.getInstance().initialize(screen_mgr);

        GenericQuote quote = QuoteManager.getInstance().getSelectedGenericQuote();
        if(quote!=null)
        {
            //populate fields
            txtCompany.setText(quote.getClient());
            txtContact.setText(quote.getContact_person());
            txtCell.setText(quote.getCell());
            txtEmail.setText(quote.getEmail());
            txtTel.setText(quote.getTel());
            txtRequest.setText(quote.getRequest());
            txtSite.setText(quote.getSitename());
            if(quote.getStatus()==0)
            {
                btnStatus.setSelected(false);
                btnStatus.setText("PENDING");
            }else{
                btnStatus.setSelected(true);
                btnStatus.setText("ACCEPTED");
            }
            tblGenericQuoteItems.setItems(FXCollections.observableArrayList(quote.getResources()));
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        CustomTableViewControls.createEditableTableColumn(colEquipmentName, javafx.scene.control.cell.TextFieldTableCell.forTableColumn(), 100, "equipment_name", "/api/quote/generic/resource");
        CustomTableViewControls.createEditableTableColumn(colDescription, javafx.scene.control.cell.TextFieldTableCell.forTableColumn(), 100, "equipment_description", "/api/quote/generic/resource");
        CustomTableViewControls.createEditableTableColumn(colUnit, javafx.scene.control.cell.TextFieldTableCell.forTableColumn(), 100, "unit", "/api/quote/generic/resource");
        CustomTableViewControls.createEditableTableColumn(colQuantity, javafx.scene.control.cell.TextFieldTableCell.forTableColumn(), 100, "quantity", "/api/quote/generic/resource");
        CustomTableViewControls.createEditableTableColumn(colRate, javafx.scene.control.cell.TextFieldTableCell.forTableColumn(), 100, "rate", "/api/quote/generic/resource");
        CustomTableViewControls.createEditableTableColumn(colLabour, javafx.scene.control.cell.TextFieldTableCell.forTableColumn(), 100, "labour", "/api/quote/generic/resource");
        CustomTableViewControls.createEditableTableColumn(colValue, javafx.scene.control.cell.TextFieldTableCell.forTableColumn(), 100, "value", "/api/quote/generic/resource");
        CustomTableViewControls.createEditableTableColumn(colMarkup, javafx.scene.control.cell.TextFieldTableCell.forTableColumn(), 100, "markup", "/api/quote/generic/resource");

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
                            final Button btnRemove = new Button("Remove item");

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
                                    HBox hBox = new HBox(btnRemove);

                                    btnRemove.setOnAction(event ->
                                    {
                                        QuoteItem quoteItem = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(quoteItem);
                                        getTableView().refresh();
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

    @FXML
    public void previousScreen()
    {
        if(itemsModified)
        {
            int response = JOptionPane.showConfirmDialog(null, "You have unsaved changes to the quote's items, would you like to save them?");

            if(response == JOptionPane.OK_OPTION)
            {
                updateGenericQuote();
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

    @FXML
    public void updateGenericQuote()
    {
        if(SessionManager.getInstance().getActive()==null)
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActive().isExpired())
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return;
        }
        if(!Validators.isValidNode(txtCompany, txtCompany.getText(), 1, ".+"))
        {
            txtCompany.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtContact, txtContact.getText(), 1, ".+"))
        {
            txtContact.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
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

        List<GenericQuoteItem> quoteItems = tblGenericQuoteItems.getItems();

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

        //prepare quote parameters
        GenericQuote quote = QuoteManager.getInstance().getSelectedGenericQuote();//new GenericQuote();
        if(quote==null)
        {
            IO.logAndAlert("Generic Quote Error", "Please select a valid generic quote.", IO.TAG_ERROR);
            return;
        }
        quote.setClient(txtCompany.getText());
        quote.setContact_person(txtContact.getText());
        quote.setSitename(txtSite.getText());
        quote.setRequest(txtRequest.getText());
        quote.setEmail(txtEmail.getText());
        quote.setCell(txtCell.getText());
        quote.setTel(txtTel.getText());
        //quote.setStatus(0);
        //quote.setCreator(SessionManager.getInstance().getActive().getUser());
        GenericQuoteItem[] items = new GenericQuoteItem[quoteItems.size()];
        quoteItems.toArray(items);
        quote.setResources(items);

        //if(str_extra!=null)
        //    quote.setExtra(str_extra);

        try
        {
            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));

            //update generic quote on database
            HttpURLConnection connection = RemoteComms.postData("/api/quote/generic/update/" + quote.get_id(), quote.asUTFEncodedString(), headers);
            if(connection!=null)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());
                    IO.log(getClass().getName(), IO.TAG_INFO, "updated generic quote["+response+"]. Adding resources to quote.");

                    if(response==null)
                    {
                        IO.logAndAlert("Generic Quote Resource Error", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }
                    if(response.isEmpty())
                    {
                        IO.logAndAlert("Generic Quote Resource Error", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }

                    /* Add GenericQuote Resources to GenericQuote on database */
                    if(connection!=null)
                        connection.disconnect();

                    if(QuoteManager.getInstance().updateGenericQuote(quote, items))
                    {
                        //TODO:set selected quote
                        //QuoteManager.getInstance().loadDataFromServer();
                        //QuoteManager.getInstance().setSelectedQuote(response);
                        //tblQuoteItems.setItems(FXCollections.observableArrayList(QuoteManager.getInstance().getSelectedQuote().getResources()));

                        //QuoteManager.getInstance().setSelectedQuote(quote);
                        IO.logAndAlert("Generic Quote Update Success", "Successfully created a new generic quote.", IO.TAG_INFO);
                        itemsModified = false;
                    } else IO.logAndAlert("Generic Quote Update Error", "Could not add items to generic quote.", IO.TAG_ERROR);
                }else
                {
                    //Get error message
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                }
                if(connection!=null)
                    connection.disconnect();
            }else IO.logAndAlert("Generic Quote Update Error", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }
    }

    @FXML
    public void changeStatus()
    {
        //TODO: allow authorized user to change status
        if(btnStatus.isSelected())
            btnStatus.setText("ACCEPTED");
        else btnStatus.setText("PENDING");
    }

    @FXML
    public void newGenericQuoteItem()
    {
        if(ResourceManager.getInstance()!=null)
        {
            if(ResourceManager.getInstance().getResources()!=null)
            {
                if(ResourceManager.getInstance().getResources().length>0)
                {
                    GenericQuoteItem quoteItem = new GenericQuoteItem();

                    quoteItem.setItem_number(tblGenericQuoteItems.getItems().size());
                    quoteItem.setEquipment_name("empty");
                    quoteItem.setEquipment_description("empty");
                    quoteItem.setUnit("empty");
                    quoteItem.setQuantity(1);
                    quoteItem.setValue(0);
                    quoteItem.setRate(0);
                    quoteItem.setLabour(0);
                    quoteItem.setMarkup(0);

                    tblGenericQuoteItems.getItems().add(quoteItem);
                    tblGenericQuoteItems.refresh();

                    itemsModified = true;
                }
            }
        }
    }
}
