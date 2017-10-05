/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.Screen;
import fadulousbms.managers.ResourceManager;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
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
public class ResourcesController extends Screen implements Initializable
{
    @FXML
    private TableView<Resource> tblResources;
    @FXML
    private TableColumn colId,colName,colSerial,colType,colDescription,colValue,colAccount,colUnit,
                        colQuantity,colDateAcquired,colDateExhausted,colOther,colAction;

    @Override
    public void refresh()
    {
        ResourceManager.getInstance().initialize(this.getScreenManager());

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

        colId.setMinWidth(100);
        colId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        CustomTableViewControls.makeEditableTableColumn(colName, TextFieldTableCell.forTableColumn(), 215, "resource_name", "/api/resource");
        CustomTableViewControls.makeEditableTableColumn(colSerial, TextFieldTableCell.forTableColumn(), 80, "resource_serial", "/api/resource");
        CustomTableViewControls.makeComboBoxTableColumn(colType, ResourceManager.getInstance().getResource_types(), "resource_type", "type_name", "/api/resource", 120);
        CustomTableViewControls.makeEditableTableColumn(colDescription, TextFieldTableCell.forTableColumn(), 215, "resource_description", "/api/resource");
        CustomTableViewControls.makeEditableTableColumn(colValue, TextFieldTableCell.forTableColumn(), 80, "resource_value", "/api/resource");
        CustomTableViewControls.makeEditableTableColumn(colAccount, TextFieldTableCell.forTableColumn(), 80, "account", "/api/resource");
        CustomTableViewControls.makeEditableTableColumn(colUnit, TextFieldTableCell.forTableColumn(), 50, "unit", "/api/resource");
        CustomTableViewControls.makeEditableTableColumn(colQuantity, TextFieldTableCell.forTableColumn(), 50, "quantity", "/api/resource");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateAcquired, "date_acquired", "/api/resource");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateExhausted, "date_exhausted", "/api/resource");
        CustomTableViewControls.makeEditableTableColumn(colOther, TextFieldTableCell.forTableColumn(), 215, "extra", "/api/resource");

        ObservableList<Resource> lst_resources = FXCollections.observableArrayList();
        lst_resources.addAll(ResourceManager.getInstance().getResources());
        tblResources.setItems(lst_resources);

        final ScreenManager screenManager = this.getScreenManager();
        Callback<TableColumn<Resource, String>, TableCell<Resource, String>> cellFactory
                =
                new Callback<TableColumn<Resource, String>, TableCell<Resource, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Resource, String> param)
                    {
                        final TableCell<Resource, String> cell = new TableCell<Resource, String>()
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
                                    Resource resource = getTableView().getItems().get(getIndex());

                                    btnView.setOnAction(event ->
                                    {
                                        //System.out.println("Successfully added material quote number " + quoteItem.getItem_number());
                                        ResourceManager.getInstance().setSelected(resource);
                                        //screenManager.setScreen(Screens.VIEW_JOB.getScreen());
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        //Quote quote = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(resource);
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

        tblResources.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                ResourceManager.getInstance().setSelected(tblResources.getSelectionModel().getSelectedItem()));
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
    public void createResourceClick()
    {
        final ScreenManager screenManager = this.getScreenManager();
        this.getScreenManager().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.NEW_RESOURCE.getScreen(),getClass().getResource("../views/"+Screens.NEW_RESOURCE.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.NEW_RESOURCE.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load resource creation screen.");
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
