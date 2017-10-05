/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.Screen;
import fadulousbms.managers.ClientManager;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.managers.SupplierManager;
import fadulousbms.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * views Controller class
 *
 * @author ghost
 */
public class SuppliersController extends Screen implements Initializable
{
    @FXML
    private TableView<Supplier>    tblSuppliers;
    @FXML
    private TableColumn     colSupplierId,colSupplierName,colSupplierPhysicalAddress,colSupplierSpeciality,
                            colSupplierPostalAddress,colSupplierTel,colSupplierFax,colSupplierActive,
                            colSupplierDatePartnered,colSupplierWebsite,colSupplierOther,colAction;

    @Override
    public void refresh()
    {
        SupplierManager.getInstance().initialize(this.getScreenManager());

        //Set Employee name
        /*Employee e = SessionManager.getInstance().getActiveEmployee();
        if(e!=null)
            this.getUserNameLabel().setText(e.toString());
        else IO.log(getClass().getName(), IO.TAG_ERROR, "No active sessions.");
        //Set Employee profile photo
        //Set default profile photo
        if(HomescreenController.defaultProfileImage!=null)
        {
            Image image = SwingFXUtils.toFXImage(HomescreenController.defaultProfileImage, null);
            this.getProfileImageView().setImage(image);
        }else IO.log(getClass().getName(), "default profile image is null.", IO.TAG_ERROR);*/

        colSupplierId.setMinWidth(100);
        colSupplierId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        CustomTableViewControls.makeEditableTableColumn(colSupplierName, TextFieldTableCell.forTableColumn(), 215, "supplier_name", "/api/supplier");
        CustomTableViewControls.makeEditableTableColumn(colSupplierPhysicalAddress, TextFieldTableCell.forTableColumn(), 215, "physical_address", "/api/supplier");
        CustomTableViewControls.makeEditableTableColumn(colSupplierPostalAddress, TextFieldTableCell.forTableColumn(), 215, "postal_address", "/api/supplier");
        CustomTableViewControls.makeEditableTableColumn(colSupplierTel, TextFieldTableCell.forTableColumn(), 215, "tel", "/api/supplier");
        CustomTableViewControls.makeEditableTableColumn(colSupplierFax, TextFieldTableCell.forTableColumn(), 215, "fax", "/api/supplier");
        CustomTableViewControls.makeEditableTableColumn(colSupplierSpeciality, TextFieldTableCell.forTableColumn(), 215, "speciality", "/api/supplier");
        CustomTableViewControls.makeCheckboxedTableColumn(colSupplierActive, null, 80, "active", "/api/supplier");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colSupplierDatePartnered, "date_partnered", "/api/supplier");
        CustomTableViewControls.makeEditableTableColumn(colSupplierWebsite, TextFieldTableCell.forTableColumn(), 215, "website", "/api/supplier");
        CustomTableViewControls.makeEditableTableColumn(colSupplierOther, TextFieldTableCell.forTableColumn(), 215, "other", "/api/supplier");

        ObservableList<Supplier> lst_suppliers = FXCollections.observableArrayList();
        lst_suppliers.addAll(SupplierManager.getInstance().getSuppliers());
        tblSuppliers.setItems(lst_suppliers);

        final ScreenManager screenManager = this.getScreenManager();
        Callback<TableColumn<Supplier, String>, TableCell<Supplier, String>> cellFactory
                =
                new Callback<TableColumn<Supplier, String>, TableCell<Supplier, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Supplier, String> param)
                    {
                        final TableCell<Supplier, String> cell = new TableCell<Supplier, String>()
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
                                    Supplier supplier = getTableView().getItems().get(getIndex());

                                    btnView.setOnAction(event ->
                                    {
                                        //System.out.println("Successfully added material quote number " + quoteItem.getItem_number());
                                        SupplierManager.getInstance().setSelected(supplier);
                                        screenManager.setScreen(Screens.VIEW_JOB.getScreen());
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        //Quote quote = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(supplier);
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

        tblSuppliers.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                SupplierManager.getInstance().setSelected(tblSuppliers.getSelectionModel().getSelectedItem()));
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        refresh();
    }

    @FXML
    public void createSupplierClick()
    {
        try
        {
            if(this.getScreenManager().loadScreen(Screens.NEW_SUPPLIER.getScreen(),getClass().getResource("../views/"+Screens.NEW_SUPPLIER.getScreen())))
                this.getScreenManager().setScreen(Screens.NEW_SUPPLIER.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load supplier creation screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }
}
