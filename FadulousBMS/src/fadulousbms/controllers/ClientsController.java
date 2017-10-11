/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.Screen;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class ClientsController extends Screen implements Initializable
{
    @FXML
    private TableView<Client>    tblClients;
    @FXML
    private TableColumn     colClientId,colClientName,colClientPhysicalAddress,
                            colClientPostalAddress,colClientTel,colClientFax,colClientActive,
                            colClientDatePartnered,colClientWebsite,colClientOther,colAction;

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading clients view..");

        colClientId.setMinWidth(100);
        colClientId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        CustomTableViewControls.makeEditableTableColumn(colClientName, TextFieldTableCell.forTableColumn(), 215, "client_name", "/api/client");
        CustomTableViewControls.makeEditableTableColumn(colClientPhysicalAddress, TextFieldTableCell.forTableColumn(), 215, "physical_address", "/api/client");
        CustomTableViewControls.makeEditableTableColumn(colClientPostalAddress, TextFieldTableCell.forTableColumn(), 215, "postal_address", "/api/client");
        CustomTableViewControls.makeEditableTableColumn(colClientTel, TextFieldTableCell.forTableColumn(), 215, "tel", "/api/client");
        CustomTableViewControls.makeEditableTableColumn(colClientFax, TextFieldTableCell.forTableColumn(), 215, "fax", "/api/client");
        CustomTableViewControls.makeCheckboxedTableColumn(colClientActive, null, 80, "active", "/api/client");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colClientDatePartnered, "date_partnered", "/api/client");
        CustomTableViewControls.makeEditableTableColumn(colClientWebsite, TextFieldTableCell.forTableColumn(), 215, "website", "/api/client");
        CustomTableViewControls.makeEditableTableColumn(colClientOther, TextFieldTableCell.forTableColumn(), 215, "other", "/api/client");

        ObservableList<Client> lst_clients = FXCollections.observableArrayList();
        lst_clients.addAll(ClientManager.getInstance().getClients());
        tblClients.setItems(lst_clients);

        final ScreenManager screenManager = ScreenManager.getInstance();
        Callback<TableColumn<Client, String>, TableCell<Client, String>> cellFactory
                =
                new Callback<TableColumn<Client, String>, TableCell<Client, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Client, String> param)
                    {
                        final TableCell<Client, String> cell = new TableCell<Client, String>()
                        {
                            final Button btnView = new Button("View");
                            final Button btnRemove = new Button("Delete");

                            @Override
                            public void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                btnView.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnView.getStyleClass().add("btnApply");
                                btnView.setMinWidth(100);
                                btnView.setMinHeight(35);
                                HBox.setHgrow(btnView, Priority.ALWAYS);

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
                                    HBox hBox = new HBox(btnView, btnRemove);
                                    Client client = getTableView().getItems().get(getIndex());

                                    btnView.setOnAction(event ->
                                    {
                                        //System.out.println("Successfully added material quote number " + quoteItem.getItem_number());
                                        ClientManager.getInstance().setSelected(client);
                                        screenManager.setScreen(Screens.VIEW_JOB.getScreen());
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        //Quote quote = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(client);
                                        getTableView().refresh();
                                        //TODO: remove from server
                                        //IO.log(getClass().getName(), IO.TAG_INFO, "successfully removed quote: " + quote.get_id());
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

        colAction.setCellValueFactory(new PropertyValueFactory<>(""));
        colAction.setCellFactory(cellFactory);

        tblClients.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                ClientManager.getInstance().setSelected(tblClients.getSelectionModel().getSelectedItem()));
    }

    @Override
    public void refreshModel()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading clients data model..");
        ClientManager.getInstance().initialize();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        new Thread(() ->
        {
            refreshModel();
            Platform.runLater(() -> refreshView());
        }).start();
    }

    @FXML
    public void createClientClick()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.NEW_CLIENT.getScreen(),getClass().getResource("../views/"+Screens.NEW_CLIENT.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.NEW_CLIENT.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load client creation screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }
}
