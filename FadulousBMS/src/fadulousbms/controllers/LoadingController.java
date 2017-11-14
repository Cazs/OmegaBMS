/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.Employee;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class LoadingController extends ScreenController implements Initializable
{
    @FXML
    private Label lblLoading;

    @Override
    public void refreshView()
    {
        Employee e = SessionManager.getInstance().getActiveEmployee();
        if(e!=null)
            this.getUserNameLabel().setText(e.toString());
        else IO.log(getClass().getName(), IO.TAG_ERROR, "No active sessions.");
        //Set default profile photo
        if(ScreenController.defaultProfileImage!=null)
        {
            Image image = SwingFXUtils.toFXImage(ScreenController.defaultProfileImage, null);
            this.getProfileImageView().setImage(image);
        }else IO.log("LoadingController", "default profile image is null.", IO.TAG_ERROR);
    }

    @Override
    public void refreshModel()
    {

    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        Thread t = new Thread(() ->
        {
            while(true)
            {
                Platform.runLater(() ->
                {
                    if(lblLoading.getText().length()>14)
                        lblLoading.setText("Loading");
                    lblLoading.setText(lblLoading.getText()+".");
                });
                try
                {
                    Thread.sleep(100);
                } catch (InterruptedException e)
                {
                    IO.log("LoadingScreenController", IO.TAG_ERROR, e.getMessage());
                }
            }
        });
        t.start();
        //Set default profile photo
        if(ScreenController.defaultProfileImage!=null)
        {
            Image image = SwingFXUtils.toFXImage(ScreenController.defaultProfileImage, null);
            this.getProfileImageView().setImage(image);
        }else IO.log("LoadingController", "default profile image is null.", IO.TAG_ERROR);
    }
}
