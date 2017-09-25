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
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * views Controller class
 *
 * @author ghost
 */
public class QuotesController extends Screen implements Initializable
{
    @FXML
    private TableView<Quote>    tblQuotes;
    @FXML
    private TableColumn     colId, colClient, colSitename, colRequest, colContactPerson, colTotal,
                            colDateGenerated, colStatus, colCreator, colRevision,
                            colExtra,colAction;

    @Override
    public void refresh()
    {
        //Set Employee name
        Employee e = SessionManager.getInstance().getActiveEmployee();
        if(e!=null)
            this.getUserNameLabel().setText(e.toString());
        else IO.log(getClass().getName(), IO.TAG_ERROR, "No active sessions.");
        //Set default profile photo
        if(HomescreenController.defaultProfileImage!=null)
        {
            Image image = SwingFXUtils.toFXImage(HomescreenController.defaultProfileImage, null);
            this.getProfileImageView().setImage(image);
        }else IO.log(getClass().getName(), "default profile image is null.", IO.TAG_ERROR);

        EmployeeManager.getInstance().loadDataFromServer();
        ClientManager.getInstance().loadDataFromServer();
        QuoteManager.getInstance().loadDataFromServer();

        colId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        CustomTableViewControls.makeComboBoxTableColumn(colClient, ClientManager.getInstance().getClients(), "client_id", "client_name", "/api/quote", 180);
        CustomTableViewControls.makeComboBoxTableColumn(colContactPerson, EmployeeManager.getInstance().getEmployees(), "contact_person_id", "firstname|lastname", "/api/quote", 160, true);
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateGenerated, "date_generated", "/api/quote");
        CustomTableViewControls.makeEditableTableColumn(colRequest, TextFieldTableCell.forTableColumn(), 100, "request", "/api/quote");
        CustomTableViewControls.makeEditableTableColumn(colSitename, TextFieldTableCell.forTableColumn(), 100, "sitename", "/api/quote");
        CustomTableViewControls.makeDynamicToggleButtonTableColumn(colStatus,100, "status", new String[]{"0","PENDING","1","SALE"}, false,"/api/quote");
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creator"));
        colRevision.setCellValueFactory(new PropertyValueFactory<>("revision"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        CustomTableViewControls.makeEditableTableColumn(colExtra, TextFieldTableCell.forTableColumn(), 100, "extra", "/api/quote");

        final ScreenManager screenManager = this.getScreenManager();
        Callback<TableColumn<Quote, String>, TableCell<Quote, String>> cellFactory
                =
                new Callback<TableColumn<Quote, String>, TableCell<Quote, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Quote, String> param)
                    {
                        final TableCell<Quote, String> cell = new TableCell<Quote, String>()
                        {
                            final Button btnView = new Button("View");
                            final Button btnPDF = new Button("PDF");
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

                                btnPDF.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnPDF.getStyleClass().add("btnApply");
                                btnPDF.setMinWidth(100);
                                btnPDF.setMinHeight(35);
                                HBox.setHgrow(btnPDF, Priority.ALWAYS);

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
                                    HBox hBox = new HBox(btnView, btnPDF, btnRemove);

                                    btnView.setOnAction(event ->
                                    {
                                        Quote quote = getTableView().getItems().get(getIndex());
                                        if(quote==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Quote object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        screenManager.showLoadingScreen(param ->
                                        {
                                            new Thread(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    QuoteManager.getInstance().setSelectedQuote(quote);
                                                    try
                                                    {
                                                        if(screenManager.loadScreen(Screens.VIEW_QUOTE.getScreen(),getClass().getResource("../views/"+Screens.VIEW_QUOTE.getScreen())))
                                                        {
                                                            Platform.runLater(() -> screenManager.setScreen(Screens.VIEW_QUOTE.getScreen()));
                                                        }
                                                        else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load quotes viewer screen.");
                                                    } catch (IOException e)
                                                    {
                                                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                                    }
                                                }
                                            }).start();
                                            return null;
                                        });
                                        //System.out.println("Successfully added material quote number " + quoteItem.getItem_number());
                                        /*QuoteManager.getInstance().setSelectedQuote(getTableView().getItems().get(getIndex()));
                                        try
                                        {
                                            if(screenManager.loadScreen(Screens.VIEW_QUOTE.getScreen(),getClass().getResource("../views/"+Screens.VIEW_QUOTE.getScreen())))
                                                screenManager.setScreen(Screens.VIEW_QUOTE.getScreen());
                                            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load quote viewing screen.");
                                        } catch (IOException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        }*/
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        Quote quote = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(quote);
                                        getTableView().refresh();
                                        //TODO: remove from server
                                        IO.log(getClass().getName(), IO.TAG_INFO, "successfully removed quote: " + quote.get_id());
                                    });

                                    btnPDF.setOnAction(event -> {
                                        Quote quote = getTableView().getItems().get(getIndex());
                                        try
                                        {
                                            PDF.createQuotePdf(quote);
                                        } catch (IOException ex)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
                                        }
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

        tblQuotes.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                QuoteManager.getInstance().setSelectedQuote(tblQuotes.getSelectionModel().getSelectedItem()));

        tblQuotes.setItems(FXCollections.observableArrayList(QuoteManager.getInstance().getQuotes()));
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
    }
}