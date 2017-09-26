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
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class ExpensesController extends Screen implements Initializable
{
    @FXML
    private TableView<Expense>    tblExpenses;
    @FXML
    private TableColumn     colId,colTitle,colDescription,colValue,colSupplier,
                            colDateLogged,colCreator,colAccount,colOther,colAction;

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

        ExpenseManager.getInstance().initialize(this.getScreenManager());
        SupplierManager.getInstance().initialize(this.getScreenManager());

        //Set up expenses table
        colId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        CustomTableViewControls.makeEditableTableColumn(colTitle, TextFieldTableCell.forTableColumn(), 100, "expense_title", "/api/expense");
        CustomTableViewControls.makeEditableTableColumn(colDescription, TextFieldTableCell.forTableColumn(), 100, "expense_description", "/api/expense");
        CustomTableViewControls.makeEditableTableColumn(colValue, TextFieldTableCell.forTableColumn(), 100, "expense_value", "/api/expense");
        CustomTableViewControls.makeComboBoxTableColumn(colSupplier, SupplierManager.getInstance().getSuppliers(), "supplier", "supplier_name", "/api/expense", 160);
        CustomTableViewControls.makeDatePickerTableColumn(colDateLogged, "date_logged", "/api/expense");
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creator"));
        CustomTableViewControls.makeEditableTableColumn(colAccount, TextFieldTableCell.forTableColumn(), 100, "account", "/api/expense");
        CustomTableViewControls.makeEditableTableColumn(colOther, TextFieldTableCell.forTableColumn(), 100, "extra", "/api/expense");

        final ScreenManager screenManager = this.getScreenManager();
        Callback colGenericCellFactory
                =
                new Callback<TableColumn<Expense, String>, TableCell<Expense, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Expense, String> param)
                    {
                        final TableCell<Expense, String> cell = new TableCell<Expense, String>()
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
                                        /*Expense expense = getTableView().getItems().get(getIndex());
                                        ExpenseManager.getInstance().setSelected(expense);
                                        try
                                        {
                                            if(screenManager.loadScreen(Screens.VIEW_EXPENSE.getScreen(),getClass().getResource("../views/"+Screens.VIEW_EXPENSE.getScreen())))
                                                screenManager.setScreen(Screens.VIEW_EXPENSE.getScreen());
                                            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load expense viewer screen.");
                                        } catch (IOException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        }*/
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        Expense expense = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(expense);
                                        getTableView().refresh();
                                        //TODO: remove from server
                                        IO.log(getClass().getName(), IO.TAG_INFO, "successfully removed expense: " + expense.get_id());
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
        colAction.setCellFactory(colGenericCellFactory);

        tblExpenses.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                ExpenseManager.getInstance().setSelected(tblExpenses.getSelectionModel().getSelectedItem()));
        tblExpenses.setItems(FXCollections.observableArrayList(ExpenseManager.getInstance().getExpenses()));
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
    }

    @FXML
    public void newExpenseClick()
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
                        if(screenManager.loadScreen(Screens.NEW_EXPENSE.getScreen(),getClass().getResource("../views/"+Screens.NEW_EXPENSE.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.NEW_EXPENSE.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load expense creation screen.");
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
