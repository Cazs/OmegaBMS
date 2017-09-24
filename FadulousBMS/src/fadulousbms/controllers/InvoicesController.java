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
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
public class InvoicesController extends Screen implements Initializable
{
    @FXML
    private TableView<Invoice>    tblInvoices;
    @FXML
    private TableColumn     colInvoiceNum,colJobNum,colClient,colTotal,colDateGenerated,
                            colCreator,colExtra,colAction;

    @Override
    public void refresh()
    {
        //Set Employee name
        Employee e = SessionManager.getInstance().getActiveEmployee();
        if(e!=null)
            this.getUserNameLabel().setText(e.toString());
        else IO.log(getClass().getName(), IO.TAG_ERROR, "No active sessions.");
        //Set Employee profile photo
        //Set default profile photo
        if(HomescreenController.defaultProfileImage!=null)
        {
            Image image = SwingFXUtils.toFXImage(HomescreenController.defaultProfileImage, null);
            this.getProfileImageView().setImage(image);
        }else IO.log(getClass().getName(), "default profile image is null.", IO.TAG_ERROR);

        EmployeeManager.getInstance().loadDataFromServer();
        InvoiceManager.getInstance().loadDataFromServer();
        ClientManager.getInstance().loadDataFromServer();

        colInvoiceNum.setMinWidth(140);
        colInvoiceNum.setCellValueFactory(new PropertyValueFactory<>("invoice_number"));
        colJobNum.setMinWidth(120);
        colJobNum.setCellValueFactory(new PropertyValueFactory<>("job_number"));
        colClient.setMinWidth(100);
        colClient.setCellValueFactory(new PropertyValueFactory<>("client"));
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateGenerated, "date_generated", "/api/invoice");
        colCreator.setMinWidth(70);
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creator"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        CustomTableViewControls.makeJobManagerAction(colAction, 420, null);
        CustomTableViewControls.makeEditableTableColumn(colExtra, TextFieldTableCell.forTableColumn(), 80, "extra", "/api/invoice");

        ObservableList<Invoice> lst_invoices = FXCollections.observableArrayList();
        lst_invoices.addAll(InvoiceManager.getInstance().getInvoices());
        tblInvoices.setItems(lst_invoices);

        final ScreenManager screenManager = this.getScreenManager();
        Callback<TableColumn<Invoice, String>, TableCell<Invoice, String>> cellFactory
                =
                new Callback<TableColumn<Invoice, String>, TableCell<Invoice, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Invoice, String> param)
                    {
                        final TableCell<Invoice, String> cell = new TableCell<Invoice, String>()
                        {
                            final Button btnViewQuote = new Button("View Quote");
                            final Button btnViewJob = new Button("View Job");
                            final Button btnPDF = new Button("PDF");
                            final Button btnRemove = new Button("Delete");

                            @Override
                            public void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                btnViewQuote.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnViewQuote.getStyleClass().add("btnApply");
                                btnViewQuote.setMinWidth(100);
                                btnViewQuote.setMinHeight(35);
                                HBox.setHgrow(btnViewQuote, Priority.ALWAYS);

                                btnViewJob.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnViewJob.getStyleClass().add("btnApply");
                                btnViewJob.setMinWidth(100);
                                btnViewJob.setMinHeight(35);
                                HBox.setHgrow(btnViewJob, Priority.ALWAYS);

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
                                    HBox hBox = new HBox(btnViewQuote, btnViewJob, btnPDF, btnRemove);
                                    Invoice invoice = getTableView().getItems().get(getIndex());

                                    btnViewQuote.setOnAction(event ->
                                    {
                                        if(invoice.getJob()==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Job object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        if(invoice.getJob().getQuote()==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Job->Quote object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        QuoteManager.getInstance().setSelectedQuote(invoice.getJob().getQuote());
                                        try
                                        {
                                            if(screenManager.loadScreen(Screens.VIEW_QUOTE.getScreen(),getClass().getResource("../views/"+Screens.VIEW_QUOTE.getScreen())))
                                                screenManager.setScreen(Screens.VIEW_QUOTE.getScreen());
                                            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load quote viewing screen.");
                                        } catch (IOException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        }
                                    });

                                    btnViewJob.setOnAction(event ->
                                    {
                                        JobManager.getInstance().setSelectedJob(invoice.getJob());
                                        try
                                        {
                                            if(screenManager.loadScreen(Screens.VIEW_JOB.getScreen(),getClass().getResource("../views/"+Screens.VIEW_JOB.getScreen())))
                                                screenManager.setScreen(Screens.VIEW_JOB.getScreen());
                                            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load jobs viewer screen.");
                                        } catch (IOException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        }
                                        /*if(invoice.getJob()==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Job object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        JobManager.getInstance().loadDataFromServer();
                                        JobManager.getInstance().setSelectedJob(invoice.getJob());
                                        try
                                        {
                                            if(screenManager.loadScreen(Screens.VIEW_JOB.getScreen(),getClass().getResource("../views/"+Screens.VIEW_JOB.getScreen())))
                                                screenManager.setScreen(Screens.VIEW_JOB.getScreen());
                                            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load quote viewing screen.");
                                        } catch (IOException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                            e.printStackTrace();
                                        }*/
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        //197.242.144.30
                                        //Quote quote = getTableView().getItems().get(getIndex());
                                        //getTableView().getItems().remove(quote);
                                        //getTableView().refresh();
                                        //TODO: remove from server
                                        //IO.log(getClass().getName(), IO.TAG_INFO, "successfully removed quote: " + quote.get_id());
                                    });

                                    //TODO: btnPDF.setOnAction(event -> InvoiceManager.generatePDF(invoice));

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

        tblInvoices.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                InvoiceManager.getInstance().setSelected(tblInvoices.getSelectionModel().getSelectedItem()));
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
}