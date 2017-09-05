/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.Screen;
import fadulousbms.managers.*;
import fadulousbms.model.CustomTableViewControls;
import fadulousbms.model.Sale;
import fadulousbms.model.Screens;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class SalesController implements Initializable, Screen
{
    private ScreenManager   screen_mgr;
    @FXML
    private TableView<Sale> tblSales;
    @FXML
    private TableColumn     colId,colClient,colSitename,colContactPerson,colTotal,
                            colDateGenerated,colQuote,colCreator,colAction;

    @Override
    public void refresh()
    {
        QuoteManager.getInstance().initialize(screen_mgr);
        SaleManager.getInstance().initialize(screen_mgr);

        colId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("client"));
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creator"));
        colSitename.setCellValueFactory(new PropertyValueFactory<>("sitename"));
        colQuote.setCellValueFactory(new PropertyValueFactory<>("quote"));
        colContactPerson.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        CustomTableViewControls.makeDatePickerTableColumn(colDateGenerated, "date_logged", false);

        //CustomTableViewControls.makeEditableTableColumn(colExtra, TextFieldTableCell.forTableColumn(), 100, "extra", "/api/quote");

        Callback<TableColumn<Sale, String>, TableCell<Sale, String>> cellFactory
                =
                new Callback<TableColumn<Sale, String>, TableCell<Sale, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Sale, String> param)
                    {
                        final TableCell<Sale, String> cell = new TableCell<Sale, String>()
                        {
                            final Button btnView = new Button("View Quote");
                            final Button btnRemove = new Button("Remove item");

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

                                    btnView.setOnAction(event ->
                                    {
                                        //System.out.println("Successfully added material quote number " + quoteItem.getItem_number());
                                        if(getIndex()>=0)
                                            QuoteManager.getInstance().setSelectedQuote(tblSales.getItems().get(getIndex()).getQuote());
                                        screen_mgr.setScreen(Screens.VIEW_QUOTE.getScreen());
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        Sale sale = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(sale);
                                        getTableView().refresh();
                                        //TODO: show confirmation dialog and remove from server
                                        IO.log(getClass().getName(), IO.TAG_INFO, "successfully removed sale: " + sale.get_id());
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

        tblSales.setItems(FXCollections.observableArrayList(SaleManager.getInstance().getSales()));

        tblSales.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
        {
            if(newValue.intValue()>=0)
                QuoteManager.getInstance().setSelectedQuote(tblSales.getItems().get(newValue.intValue()).getQuote());
        });
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        /*colAction.setCellFactory(new ButtonTableCellFactory<>());

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

        colAction.setCellFactory(cellFactory);*/
    }

    @FXML
    public void newQuote()
    {
        screen_mgr.setScreen(Screens.NEW_QUOTE.getScreen());
    }

    @FXML
    public void previousScreen()
    {
        screen_mgr.setScreen(Screens.OPERATIONS_SALES.getScreen());
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
}
