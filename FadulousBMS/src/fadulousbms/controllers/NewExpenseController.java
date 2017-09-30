/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Screen;
import fadulousbms.auxilary.Validators;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZoneId;
import java.util.*;

/**
 * views Controller class
 *
 * @author ghost
 */
public class NewExpenseController extends Screen implements Initializable
{
    private boolean itemsModified;
    private Date date_generated;
    @FXML
    private TextField txtTitle,txtDescription,txtValue,txtOther,txtAccount;
    @FXML
    private ComboBox<Supplier> cbxSupplier;
    @FXML
    private DatePicker dateLogged;

    @Override
    public void refresh()
    {
        SupplierManager.getInstance().initialize(this.getScreenManager());
        if(SupplierManager.getInstance().getSuppliers()!=null)
            cbxSupplier.setItems(FXCollections.observableArrayList(SupplierManager.getInstance().getSuppliers()));
        else IO.logAndAlert("No Suppliers Error", "No suppliers were found in the database.", IO.TAG_ERROR);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
    }

    @FXML
    public void createExpense()
    {
        if(SessionManager.getInstance().getActive()==null)
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActive().isExpired())
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return;
        }
        if(!Validators.isValidNode(txtTitle, txtTitle.getText(), 1, ".+"))
        {
            txtTitle.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtDescription, txtDescription.getText(), 1, ".+"))
        {
            txtDescription.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtValue, txtValue.getText(), 1, ".+"))
        {
            txtValue.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtAccount, txtAccount.getText(), 1, ".+"))
        {
            txtAccount.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(cbxSupplier.getValue()==null)
        {
            IO.logAndAlert("Error", "Please choose a valid supplier.", IO.TAG_ERROR);
            return;
        }
        if(dateLogged.getValue()==null)
        {
            IO.logAndAlert("Error", "Please choose a valid purchase date.", IO.TAG_ERROR);
            return;
        }
        if(dateLogged.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond()<=0)
        {
            IO.logAndAlert("Error", "Please choose a valid purchase date.", IO.TAG_ERROR);
            return;
        }

        //prepare expense parameters
        Expense expense = new Expense();
        expense.setExpense_title(txtTitle.getText());
        expense.setExpense_description(txtDescription.getText());
        expense.setExpense_value(Double.parseDouble(txtValue.getText()));
        expense.setCreator(SessionManager.getInstance().getActive().getUsername());
        expense.setAccount(txtAccount.getText());
        expense.setSupplier_obj(cbxSupplier.getValue());
        expense.setDate_logged(dateLogged.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        if(txtOther.getText()!=null)
            expense.setOther(txtOther.getText());

        try
        {
            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));

            //create new quote on database
            HttpURLConnection connection = RemoteComms.postData("/api/expense/add", expense.asUTFEncodedString(), headers);
            if(connection!=null)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());
                    IO.log(getClass().getName(), IO.TAG_INFO, "created expense["+response+"].");

                    if(response==null)
                    {
                        IO.logAndAlert("New Expense Error", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }
                    if(response.isEmpty())
                    {
                        IO.logAndAlert("New Expense Error", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }
                    IO.logAndAlert("New Expense Creation Success", "Successfully created a new expense.", IO.TAG_INFO);
                    itemsModified = false;
                }else
                {
                    //Get error message
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                }
                if(connection!=null)
                    connection.disconnect();
            }else IO.logAndAlert("New Expense Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }
    }
}