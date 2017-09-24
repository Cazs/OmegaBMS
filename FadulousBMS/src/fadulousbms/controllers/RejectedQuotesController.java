/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.Screen;
import fadulousbms.managers.GenericQuoteManager;
import fadulousbms.managers.QuoteManager;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.CustomTableViewControls;
import fadulousbms.model.Employee;
import fadulousbms.model.GenericQuote;
import fadulousbms.model.Screens;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class RejectedQuotesController extends Screen implements Initializable
{
    @FXML
    private TableView<GenericQuote>    tblRejectedQuotes;
    @FXML
    private TableColumn     colQuoteNumber, colQuoteClient, colQuoteSitename, colQuoteRequest,
                            colEmail, colTel, colCell, colQuoteContact, colQuoteDate, colQuoteStatus,
                            colQuoteCreator, colQuoteExtra,colQuoteAction;

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

        GenericQuoteManager.getInstance().initialize(this.getScreenManager());

        //Set up pending quotes table
        colQuoteNumber.setCellValueFactory(new PropertyValueFactory<>("_id"));
        //CustomTableViewControls.makeComboBoxTableColumn(colGenericQuoteClient, ClientManager.getInstance().getClients(), "client", "client_name", "/api/quote/generic", 180);
        CustomTableViewControls.makeEditableTableColumn(colQuoteClient, TextFieldTableCell.forTableColumn(), 100, "client", "/api/quote/generic");
        //CustomTableViewControls.makeComboBoxTableColumn(colGenericQuoteContact, EmployeeManager.getInstance().getEmployees(), "contact_person", "firstname|lastname", "/api/quote/generic", 160, true);
        CustomTableViewControls.makeEditableTableColumn(colQuoteContact, TextFieldTableCell.forTableColumn(), 100, "contact_person", "/api/quote/generic");
        CustomTableViewControls.makeEditableTableColumn(colEmail, TextFieldTableCell.forTableColumn(), 100, "email", "/api/quote/generic");
        CustomTableViewControls.makeEditableTableColumn(colTel, TextFieldTableCell.forTableColumn(), 100, "tel", "/api/quote/generic");
        CustomTableViewControls.makeEditableTableColumn(colCell, TextFieldTableCell.forTableColumn(), 100, "cell", "/api/quote/generic");
        CustomTableViewControls.makeDatePickerTableColumn(colQuoteDate, "date_generated", "/api/quote/generic");
        CustomTableViewControls.makeEditableTableColumn(colQuoteRequest, TextFieldTableCell.forTableColumn(), 100, "request", "/api/quote/generic");
        CustomTableViewControls.makeEditableTableColumn(colQuoteSitename, TextFieldTableCell.forTableColumn(), 100, "sitename", "/api/quote/generic");
        CustomTableViewControls.makeDynamicToggleButtonTableColumn(colQuoteStatus,100, "status", new String[]{"0","Pending","1","Rejected"}, true,"/api/quote/generic");
        colQuoteCreator.setCellValueFactory(new PropertyValueFactory<>("creator"));
        CustomTableViewControls.makeEditableTableColumn(colQuoteExtra, TextFieldTableCell.forTableColumn(), 100, "extra", "/api/quote/generic");

        final ScreenManager screenManager = this.getScreenManager();
        Callback colGenericCellFactory
                =
                new Callback<TableColumn<GenericQuote, String>, TableCell<GenericQuote, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<GenericQuote, String> param)
                    {
                        final TableCell<GenericQuote, String> cell = new TableCell<GenericQuote, String>()
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

                                    btnView.setOnAction(event ->
                                    {
                                        GenericQuote quote = getTableView().getItems().get(getIndex());
                                        GenericQuoteManager.getInstance().setSelectedGenericQuote(quote);
                                        try
                                        {
                                            if(screenManager.loadScreen(Screens.VIEW_GENERIC_QUOTE.getScreen(),getClass().getResource("../views/"+Screens.VIEW_GENERIC_QUOTE.getScreen())))
                                                screenManager.setScreen(Screens.VIEW_GENERIC_QUOTE.getScreen());
                                            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load rejected quotes viewer screen.");
                                        } catch (IOException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        }
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        GenericQuote quote = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(quote);
                                        getTableView().refresh();
                                        //TODO: remove from server
                                        IO.log(getClass().getName(), IO.TAG_INFO, "successfully removed quote: " + quote.get_id());
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

        colQuoteAction.setCellValueFactory(new PropertyValueFactory<>(""));
        colQuoteAction.setCellFactory(colGenericCellFactory);

        tblRejectedQuotes.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                GenericQuoteManager.getInstance().setSelectedGenericQuote(tblRejectedQuotes.getSelectionModel().getSelectedItem()));

        GenericQuote[] genericQuotes = GenericQuoteManager.getInstance().getGenericQuotes();
        ArrayList<GenericQuote> rejected_quotes = new ArrayList<>();
        for(GenericQuote genericQuote: genericQuotes)
            if(genericQuote.getStatus()==1)//if rejected
                rejected_quotes.add(genericQuote);
        tblRejectedQuotes.setItems(FXCollections.observableArrayList(rejected_quotes));
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
    }
}
