/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.*;
import fadulousbms.managers.*;
import fadulousbms.model.*;
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
public class QuotesController implements Initializable, Screen
{
    @FXML
    private ImageView img_profile;
    @FXML
    private Label user_name;
    private ScreenManager   screen_mgr;
    @FXML
    private TableView<Quote>    tblQuotes;
    @FXML
    private TableView<GenericQuote>    tblGenericQuotes;
    @FXML
    private TableColumn     colId, colClient, colSitename, colRequest, colContactPerson, colTotal,
                            colDateGenerated, colStatus, colCreator, colRevision,
                            colExtra,colAction;
    @FXML
    private TableColumn     colGenericQuoteNumber, colGenericQuoteClient, colGenericQuoteSitename, colGenericQuoteRequest,
                            colEmail, colTel, colCell, colGenericQuoteContact, colGenericQuoteDate, colGenericQuoteStatus, colGenericQuoteCreator,
                            colGenericQuoteExtra,colGenericQuoteAction;

    @Override
    public void refresh()
    {
        //Set Employee name
        Employee e = SessionManager.getInstance().getActiveEmployee();
        if(e!=null)
            user_name.setText(e.toString());
        else IO.log(getClass().getName(), IO.TAG_ERROR, "No active sessions.");
        //Set default profile photo
        if(HomescreenController.defaultProfileImage!=null)
        {
            Image image = SwingFXUtils.toFXImage(HomescreenController.defaultProfileImage, null);
            img_profile.setImage(image);
        }else IO.log(getClass().getName(), "default profile image is null.", IO.TAG_ERROR);

        QuoteManager.getInstance().initialize(screen_mgr);

        colId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        CustomTableViewControls.makeComboBoxTableColumn(colClient, ClientManager.getInstance().getClients(), "client_id", "client_name", "/api/quote", 180);
        CustomTableViewControls.makeComboBoxTableColumn(colContactPerson, EmployeeManager.getInstance().getEmployees(), "contact_person_id", "firstname|lastname", "/api/quote", 160, true);
        CustomTableViewControls.makeDatePickerTableColumn(colDateGenerated, "date_generated", "/api/quote");
        CustomTableViewControls.makeEditableTableColumn(colRequest, TextFieldTableCell.forTableColumn(), 100, "request", "/api/quote");
        CustomTableViewControls.makeEditableTableColumn(colSitename, TextFieldTableCell.forTableColumn(), 100, "sitename", "/api/quote");
        CustomTableViewControls.makeDynamicToggleButtonTableColumn(colStatus,100, "status", new String[]{"0","PENDING","1","SALE"}, false,"/api/quote");
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creator"));
        colRevision.setCellValueFactory(new PropertyValueFactory<>("revision"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        CustomTableViewControls.makeEditableTableColumn(colExtra, TextFieldTableCell.forTableColumn(), 100, "extra", "/api/quote");
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
                                        //System.out.println("Successfully added material quote number " + quoteItem.getItem_number());
                                        QuoteManager.getInstance().setSelectedQuote(getTableView().getItems().get(getIndex()));
                                        screen_mgr.setScreen(Screens.VIEW_QUOTE.getScreen());
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

        //Set up pending quotes table
        colGenericQuoteNumber.setCellValueFactory(new PropertyValueFactory<>("_id"));
        //CustomTableViewControls.makeComboBoxTableColumn(colGenericQuoteClient, ClientManager.getInstance().getClients(), "client", "client_name", "/api/quote/generic", 180);
        CustomTableViewControls.makeEditableTableColumn(colGenericQuoteClient, TextFieldTableCell.forTableColumn(), 100, "client", "/api/quote/generic");
        //CustomTableViewControls.makeComboBoxTableColumn(colGenericQuoteContact, EmployeeManager.getInstance().getEmployees(), "contact_person", "firstname|lastname", "/api/quote/generic", 160, true);
        CustomTableViewControls.makeEditableTableColumn(colGenericQuoteContact, TextFieldTableCell.forTableColumn(), 100, "contact_person", "/api/quote/generic");
        CustomTableViewControls.makeEditableTableColumn(colEmail, TextFieldTableCell.forTableColumn(), 100, "email", "/api/quote/generic");
        CustomTableViewControls.makeEditableTableColumn(colTel, TextFieldTableCell.forTableColumn(), 100, "tel", "/api/quote/generic");
        CustomTableViewControls.makeEditableTableColumn(colCell, TextFieldTableCell.forTableColumn(), 100, "cell", "/api/quote/generic");
        CustomTableViewControls.makeDatePickerTableColumn(colGenericQuoteDate, "date_generated", "/api/quote/generic");
        CustomTableViewControls.makeEditableTableColumn(colGenericQuoteRequest, TextFieldTableCell.forTableColumn(), 100, "request", "/api/quote/generic");
        CustomTableViewControls.makeEditableTableColumn(colGenericQuoteSitename, TextFieldTableCell.forTableColumn(), 100, "sitename", "/api/quote/generic");
        CustomTableViewControls.makeDynamicToggleButtonTableColumn(colGenericQuoteStatus,100, "status", new String[]{"0","Pending","1","Accepted"}, false,"/api/quote/generic");
        colGenericQuoteCreator.setCellValueFactory(new PropertyValueFactory<>("creator"));
        CustomTableViewControls.makeEditableTableColumn(colGenericQuoteExtra, TextFieldTableCell.forTableColumn(), 100, "extra", "/api/quote/generic");
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
                            final Button btnQuote = new Button("New Quote");
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

                                btnQuote.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnQuote.getStyleClass().add("btnApply");
                                btnQuote.setMinWidth(100);
                                btnQuote.setMinHeight(35);
                                HBox.setHgrow(btnQuote, Priority.ALWAYS);

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
                                    HBox hBox = new HBox(btnView, btnQuote, btnRemove);

                                    btnView.setOnAction(event ->
                                    {
                                        GenericQuote quote = getTableView().getItems().get(getIndex());
                                        QuoteManager.getInstance().setSelectedGenericQuote(quote);
                                        screen_mgr.setScreen(Screens.VIEW_GENERIC_QUOTE.getScreen());
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        GenericQuote quote = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(quote);
                                        getTableView().refresh();
                                        //TODO: remove from server
                                        IO.log(getClass().getName(), IO.TAG_INFO, "successfully removed quote: " + quote.get_id());
                                    });

                                    btnQuote.setOnAction(event -> {
                                        QuoteManager.getInstance().setFromGeneric(true);
                                        GenericQuote quote = getTableView().getItems().get(getIndex());
                                        QuoteManager.getInstance().setSelectedGenericQuote(quote);
                                        screen_mgr.setScreen(Screens.NEW_QUOTE.getScreen());
                                        /*try
                                        {
                                            PDF.createQuotePdf(quote);
                                        } catch (IOException ex)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
                                        }*/
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

        colGenericQuoteAction.setCellValueFactory(new PropertyValueFactory<>(""));
        colGenericQuoteAction.setCellFactory(colGenericCellFactory);

        tblQuotes.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                QuoteManager.getInstance().setSelectedQuote(tblQuotes.getSelectionModel().getSelectedItem()));
        tblGenericQuotes.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                QuoteManager.getInstance().setSelectedGenericQuote(tblGenericQuotes.getSelectionModel().getSelectedItem()));


        tblQuotes.setItems(FXCollections.observableArrayList(QuoteManager.getInstance().getQuotes()));
        tblGenericQuotes.setItems(FXCollections.observableArrayList(QuoteManager.getInstance().getGenericQuotes()));
        /*try
        {
            BufferedImage bufferedImage;
            bufferedImage = ImageIO.read(new File("images/profile.png"));
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            img_profile.setImage(image);

            if(SessionManager.getInstance().getActive()!=null)
            {
                if(!SessionManager.getInstance().getActive().isExpired())
                {
                    ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie",
                            SessionManager.getInstance().getActive().getSessionId()));

                    byte[] file = RemoteComms.sendFileRequest("logo", headers);
                    ByteArrayInputStream bis = new ByteArrayInputStream(file);
                    BufferedImage buff_img = ImageIO.read(bis);
                    Image img = SwingFXUtils.toFXImage(buff_img, null);

                    img_logo.setImage(img);
                }else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
            }else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
            /*for(int i=0;i<30;i++)
                news_feed_tiles.getChildren().add(createTile());*
        }catch (IOException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
        }

        Employee e = SessionManager.getInstance().getActiveEmployee();
        if(e!=null)
            user_name.setText(e.getFirstname() + " " + e.getLastname());
        else IO.log(getClass().getName(), IO.TAG_ERROR, "No active sessions.");*/
    }

    @FXML
    public void showMain()
    {
        screen_mgr.setScreen(Screens.HOME.getScreen());
    }

    @FXML
    public void showLogin()
    {
        try
        {
            Stage stage = new Stage();
            stage.setTitle("Login to BMS Engine");
            stage.setMinWidth(320);
            stage.setMinHeight(280);
            //stage.setAlwaysOnTop(true);

            ScreenManager login_screen_mgr = new ScreenManager();
            login_screen_mgr.loadScreen(Screens.LOGIN.getScreen(), getClass().getResource("../views/"+Screens.LOGIN.getScreen()));
            login_screen_mgr.setScreen(Screens.LOGIN.getScreen());

            Group root = new Group();
            root.getChildren().add(login_screen_mgr);
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.show();
            stage.centerOnScreen();
            stage.setResizable(false);

            //When the login screen is being dismissed set the user's first and last name
            stage.setOnHiding(event ->
            {
                Employee e = SessionManager.getInstance().getActiveEmployee();
                if(e!=null)
                    user_name.setText(e.toString());
            });

        } catch (IOException ex)
        {
            Logger.getLogger(HomescreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        QuoteManager.getInstance().setFromGeneric(false);
        QuoteManager.getInstance().nullifySelected();
        screen_mgr.setScreen(Screens.NEW_QUOTE.getScreen());
    }

    @FXML
    public void newGenericQuote()
    {
        QuoteManager.getInstance().nullifySelected();
        screen_mgr.setScreen(Screens.NEW_GENERIC_QUOTE.getScreen());
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
}
