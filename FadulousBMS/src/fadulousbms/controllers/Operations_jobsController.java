/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Screen;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.Employee;
import fadulousbms.model.Job;
import java.io.IOException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import javax.swing.JOptionPane;

/**
 * views Controller class
 *
 * @author ghost
 */
public class Operations_jobsController implements Initializable, Screen
{
    private ScreenManager screen_mgr;
    @FXML
    private ImageView img_profile;
    @FXML
    private Label user_name;
    @FXML
    private TableView tblJobs;// = new TableView();
    @FXML
    private BorderPane table_container = new BorderPane();
    @FXML
    private TableColumn<Job, String> job_name;

    @Override
    public void refresh()
    {
        Employee e = SessionManager.getInstance().getActiveEmployee();
        if(e!=null)
            user_name.setText(e.getFirstname() + " " + e.getLastname());
        else IO.log(getClass().getName(), IO.TAG_ERROR, "No active sessions.");
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        Button btn = new Button("A button!");
        BorderPane.setAlignment(btn, Pos.CENTER);
        table_container.setStyle("-fx-background-color: red;");
        table_container = new BorderPane(btn);
        //table_container.getChildren().add(btn);
        try 
        {
            SessionManager smgr = SessionManager.getInstance();
            if(smgr.getActive()!=null)
            {
                ArrayList<AbstractMap.SimpleEntry<String,String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));
                String jobs_json = RemoteComms.sendGetRequest("/api/jobs", headers);
                Gson gson = new GsonBuilder().create();
                Job[] jobs = gson.fromJson(jobs_json, Job[].class);
                
                //TableColumn<Job, String> job_name = new TableColumn<>("Job name");
                job_name.setCellValueFactory(new PropertyValueFactory<>("job_name"));
                //job_name.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList<String, String>>);
                /*TableColumn job_description = new TableColumn("Job description");
                TableColumn client_name = new TableColumn("Client");
                TableColumn date_logged = new TableColumn("Date logged");
                TableColumn date_assigned = new TableColumn("Date assigned");
                TableColumn date_started = new TableColumn("Date started");
                TableColumn date_ended = new TableColumn("Date completed");
                TableColumn invoice_id = new TableColumn("Invoice ID");
                TableColumn job_completed = new TableColumn("Completed?");*/
                
                ObservableList<Job> lst_jobs = FXCollections.observableArrayList(jobs);
                //lst_jobs.add(jobs[0]);
                //tblJobs.setItems(lst_jobs);
                tblJobs.getItems().setAll(lst_jobs);
                /*tblJobs.getColumns().addAll(job_name, job_description, client_name, date_logged, 
                        date_assigned, date_started, date_ended, invoice_id, job_completed);*/
                
                
                //System.out.println("Job: " + jobs[0].getName());
            }else{
                JOptionPane.showMessageDialog(null, "No active sessions.", "Session Expired", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) 
        {
            Logger.getLogger(Operations_jobsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    

    @FXML
    private void showMain(MouseEvent event) {
    }

    @FXML
    private void showLogin(MouseEvent event) {
    }

    @FXML
    private void productionClick(MouseEvent event) {
    }

    @FXML
    private void salesClick(MouseEvent event) {
    }

    @FXML
    private void facilitiesClick(MouseEvent event) {
    }

    @Override
    public void setParent(ScreenManager mgr) 
    {
        this.screen_mgr = mgr;
    }
    
}
