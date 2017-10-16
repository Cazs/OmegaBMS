/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.Screen;
import fadulousbms.managers.PurchaseOrderManager;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SupplierManager;
import fadulousbms.model.CustomTableViewControls;
import fadulousbms.model.PurchaseOrder;
import fadulousbms.model.Screens;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
public class PurchaseOrdersController extends Screen implements Initializable
{
    @FXML
    private TableView tblPurchaseOrders;
    @FXML
    private TableColumn colId,colPONumber,colSupplier,colDateLogged,colStatus,colCreator,colAccount,colVat,colTotal,colExtra,colAction;

    @Override
    public void refreshView()
    {
        colId.setMinWidth(100);
        colId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        colPONumber.setCellValueFactory(new PropertyValueFactory<>("number"));
        CustomTableViewControls.makeComboBoxTableColumn(colSupplier, SupplierManager.getInstance().getSuppliers(), "supplier_id", "supplier_name", "/api/purchaseorder", 120);
        CustomTableViewControls.makeEditableTableColumn(colAccount, TextFieldTableCell.forTableColumn(), 80, "account", "/api/purchaseorder");
        CustomTableViewControls.makeEditableTableColumn(colVat, TextFieldTableCell.forTableColumn(), 50, "vat", "/api/purchaseorder");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateLogged, "date_logged", "/api/purchaseorder");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creator"));
        CustomTableViewControls.makeEditableTableColumn(colExtra, TextFieldTableCell.forTableColumn(), 215, "extra", "/api/purchaseorder");

        ObservableList<PurchaseOrder> lst_po = FXCollections.observableArrayList();
        lst_po.addAll(PurchaseOrderManager.getInstance().getPurchaseOrders());
        tblPurchaseOrders.setItems(lst_po);

        Callback<TableColumn<PurchaseOrder, String>, TableCell<PurchaseOrder, String>> cellFactory
                =
                new Callback<TableColumn<PurchaseOrder, String>, TableCell<PurchaseOrder, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<PurchaseOrder, String> param)
                    {
                        final TableCell<PurchaseOrder, String> cell = new TableCell<PurchaseOrder, String>()
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
                                    PurchaseOrder po = getTableView().getItems().get(getIndex());

                                    btnView.setOnAction(event ->
                                    {
                                        //System.out.println("Successfully added material quote number " + quoteItem.getItem_number());
                                        PurchaseOrderManager.getInstance().setSelected(po);
                                        //screenManager.setScreen(Screens.VIEW_JOB.getScreen());
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        //Quote quote = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(po);
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

        tblPurchaseOrders.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                PurchaseOrderManager.getInstance().setSelected((PurchaseOrder) tblPurchaseOrders.getSelectionModel().getSelectedItem()));
    }

    @Override
    public void refreshModel()
    {
        PurchaseOrderManager.getInstance().initialize();
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
            if(PurchaseOrderManager.getInstance().getPurchaseOrders()!=null)
                Platform.runLater(() -> refreshView());
        }).start();
    }
}
