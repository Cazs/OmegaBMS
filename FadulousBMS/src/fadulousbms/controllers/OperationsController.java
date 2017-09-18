/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.*;
import fadulousbms.managers.*;
import fadulousbms.model.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * views Controller class
 *
 * @author ghost
 */
public class OperationsController extends Screen implements Initializable
{
    public static final String TAG="OperationsController";

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        if (SessionManager.getInstance().getActive() != null)
        {
            if (!SessionManager.getInstance().getActive().isExpired())
            {
                    //Set default profile photo
                    if(HomescreenController.defaultProfileImage!=null)
                    {
                        Image image = SwingFXUtils.toFXImage(HomescreenController.defaultProfileImage, null);
                        this.getProfileImageView().setImage(image);
                    }else IO.log(TAG, "default profile image is null.", IO.TAG_ERROR);
            }else{
                JOptionPane.showMessageDialog(null, "No active sessions!", "Session expired", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }else{
            JOptionPane.showMessageDialog(null, "No active sessions!", "Session expired", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    @Override
    public void refresh()
    {
        Employee e = SessionManager.getInstance().getActiveEmployee();
        if(e!=null)
            this.getUserNameLabel().setText(e.toString());
        else IO.log(getClass().getName(), IO.TAG_ERROR, "No active sessions.");
    }

    @FXML
    public void productionClick()
    {
        try 
        {
            this.getScreenManager().loadScreen(Screens.OPERATIONS_PRODUCTION.getScreen(),
                    getClass().getResource("../views/" + Screens.OPERATIONS_PRODUCTION.getScreen()));
            this.getScreenManager().setScreen(Screens.OPERATIONS_PRODUCTION.getScreen());
        } catch (IOException ex) 
        {
            Logger.getLogger(OperationsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void salesClick()
    {
        try
        {
            this.getScreenManager().loadScreen(Screens.OPERATIONS_SALES.getScreen(),
                    getClass().getResource("../views/" + Screens.OPERATIONS_SALES.getScreen()));
            this.getScreenManager().setScreen(Screens.OPERATIONS_SALES.getScreen());
        } catch (IOException ex)
        {
            Logger.getLogger(OperationsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void resourcesClick()
    {
        try
        {
            if(this.getScreenManager().loadScreen(Screens.RESOURCES.getScreen(),getClass().getResource("../views/"+Screens.RESOURCES.getScreen())))
                this.getScreenManager().setScreen(Screens.RESOURCES.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load resources screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    //Sales event handlers
    @FXML
    public void quotesClick()
    {
        //this.getScreenManager().setScreen("loading.fxml");
        try
        {
            if(this.getScreenManager().loadScreen(Screens.QUOTES.getScreen(),getClass().getResource("../views/"+Screens.QUOTES.getScreen())))
                this.getScreenManager().setScreen(Screens.QUOTES.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load quotes screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void pendingQuotesClick()
    {
        try
        {
            if(this.getScreenManager().loadScreen(Screens.GENERIC_QUOTES.getScreen(),getClass().getResource("../views/"+Screens.GENERIC_QUOTES.getScreen())))
                this.getScreenManager().setScreen(Screens.GENERIC_QUOTES.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load generic quotes screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void rejectedQuotesClick()
    {
        try
        {
            if(this.getScreenManager().loadScreen(Screens.REJECTED_QUOTES.getScreen(),getClass().getResource("../views/"+Screens.REJECTED_QUOTES.getScreen())))
                this.getScreenManager().setScreen(Screens.REJECTED_QUOTES.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load rejected quotes screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    //Production event handlers
    @FXML
    public void suppliersClick()
    {
        try
        {
            if(this.getScreenManager().loadScreen(Screens.SUPPLIERS.getScreen(),getClass().getResource("../views/"+Screens.SUPPLIERS.getScreen())))
                this.getScreenManager().setScreen(Screens.SUPPLIERS.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load suppliers screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void jobsClick()
    {
        try
        {
            if(this.getScreenManager().loadScreen(Screens.JOBS.getScreen(),getClass().getResource("../views/"+Screens.JOBS.getScreen())))
                this.getScreenManager().setScreen(Screens.JOBS.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load jobs screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void clientsClick()
    {
        try
        {
            if(this.getScreenManager().loadScreen(Screens.CLIENTS.getScreen(),getClass().getResource("../views/"+Screens.CLIENTS.getScreen())))
                this.getScreenManager().setScreen(Screens.CLIENTS.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load clients screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }
}
