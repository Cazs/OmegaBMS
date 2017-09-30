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
import fadulousbms.managers.SessionManager;
import fadulousbms.managers.SupplierManager;
import fadulousbms.model.Expense;
import fadulousbms.model.Revenue;
import fadulousbms.model.Supplier;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class NewRevenueController extends Screen implements Initializable
{
    private boolean itemsModified;
    private Date date_generated;
    @FXML
    private TextField txtTitle,txtDescription,txtValue,txtOther,txtAccount;
    @FXML
    private DatePicker dateLogged;

    @Override
    public void refresh()
    {
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
    }

    @FXML
    public void createRevenue()
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
        if(dateLogged.getValue()==null)
        {
            IO.logAndAlert("Error", "Please choose a valid date.", IO.TAG_ERROR);
            return;
        }
        if(dateLogged.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond()<=0)
        {
            IO.logAndAlert("Error", "Please choose a valid date.", IO.TAG_ERROR);
            return;
        }
        //TODO: future/past date validation

        //prepare revenue parameters
        Revenue revenue = new Revenue();
        revenue.setRevenue_title(txtTitle.getText());
        revenue.setRevenue_description(txtDescription.getText());
        revenue.setRevenue_value(Double.parseDouble(txtValue.getText()));
        revenue.setCreator(SessionManager.getInstance().getActive().getUsername());
        revenue.setAccount(txtAccount.getText());
        revenue.setDate_logged(dateLogged.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        if(txtOther.getText()!=null)
            revenue.setOther(txtOther.getText());

        try
        {
            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));

            //create new quote on database
            HttpURLConnection connection = RemoteComms.postData("/api/revenue/add", revenue.asUTFEncodedString(), headers);
            if(connection!=null)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());
                    IO.log(getClass().getName(), IO.TAG_INFO, "created revenue["+response+"].");

                    if(response==null)
                    {
                        IO.logAndAlert("New Revenue Error", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }
                    if(response.isEmpty())
                    {
                        IO.logAndAlert("New Revenue Error", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }
                    IO.logAndAlert("New Revenue Creation Success", "Successfully logged additional revenue.", IO.TAG_INFO);
                    itemsModified = false;
                }else
                {
                    //Get error message
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                }
                if(connection!=null)
                    connection.disconnect();
            }else IO.logAndAlert("New Revenue Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }
    }
}