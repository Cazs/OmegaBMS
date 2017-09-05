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
public class OperationsController implements Initializable, Screen
{
    @FXML
    private ImageView img_profile;
    @FXML
    private Label user_name;
    @FXML
    private BorderPane rootContainer;
    private ScreenManager screen_mgr;
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
                JobManager.getInstance().initialize(screen_mgr);
                SupplierManager.getInstance().initialize(screen_mgr);
                ClientManager.getInstance().initialize(screen_mgr);
                ResourceManager.getInstance().initialize(screen_mgr);
                SaleManager.getInstance().initialize(screen_mgr);

                /*try
                {*/
                    //Set default profile photo
                    if(HomescreenController.defaultProfileImage!=null)
                    {
                        Image image = SwingFXUtils.toFXImage(HomescreenController.defaultProfileImage, null);
                        img_profile.setImage(image);
                    }else IO.log(TAG, "default profile image is null.", IO.TAG_ERROR);

                    //Set current logged in employee
                    /*Employee e = SessionManager.getInstance().getActiveEmployee();
                    if (e != null)
                        user_name.setText(e.toString());*/
                /*}catch (IOException ex)
                {
                    IO.log(OperationsController.class.getName(), IO.TAG_ERROR, ex.getMessage());
                }*/
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
    public void setParent(ScreenManager mgr)
    {
        screen_mgr = mgr;
        /*screen_mgr.addEventHandler(EventType.ROOT, eventHandler);
        container.prefWidthProperty().bind(screen_mgr.widthProperty());
        container.prefHeightProperty().bind(screen_mgr.heightProperty());*/
    }

    @Override
    public void refresh()
    {
        Employee e = SessionManager.getInstance().getActiveEmployee();
        if(e!=null)
            user_name.setText(e.toString());
        else IO.log(getClass().getName(), IO.TAG_ERROR, "No active sessions.");
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
                    user_name.setText(e.getFirstname() + " " + e.getLastname());
            });
            
        } catch (IOException ex) 
        {
            Logger.getLogger(HomescreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    public void productionClick()
    {
        try 
        {
            screen_mgr.loadScreen(Screens.OPERATIONS_PRODUCTION.getScreen(),
                    getClass().getResource("../views/" + Screens.OPERATIONS_PRODUCTION.getScreen()));
            screen_mgr.setScreen(Screens.OPERATIONS_PRODUCTION.getScreen());
        } catch (IOException ex) 
        {
            Logger.getLogger(OperationsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    public void facilitiesClick()
    {
        try
        {
            screen_mgr.loadScreen(Screens.OPERATIONS_FACILITIES.getScreen(),
                    getClass().getResource("../views/" + Screens.OPERATIONS_FACILITIES.getScreen()));
            screen_mgr.setScreen(Screens.OPERATIONS_FACILITIES.getScreen());
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
            screen_mgr.loadScreen(Screens.OPERATIONS_SALES.getScreen(),
                    getClass().getResource("../views/" + Screens.OPERATIONS_SALES.getScreen()));
            screen_mgr.setScreen(Screens.OPERATIONS_SALES.getScreen());
        } catch (IOException ex)
        {
            Logger.getLogger(OperationsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Sale click event handlers
    @FXML
    public void quotesClick()
    {
        //for(Node node: rootContainer.getChildren())
        //    node.setOpacity(0.1);
        //node.setStyle("-fx-fill: rgba(0,255,0,0.5)");
        //rootContainer.setStyle("-fx-stroke: rgba(255,0,0,1)");
        //new Thread(() ->
        screen_mgr.setScreen("loading.fxml");
        screen_mgr.setScreen(Screens.QUOTES.getScreen());//).start();
    }

    @FXML
    public void invoicesClick()
    {
        InvoiceManager.getInstance().newWindow();
    }

    @FXML
    public void showSalesClick()
    {
        screen_mgr.setScreen(Screens.SALES.getScreen());
    }

    //Production click event handlers
    public void suppliersClick()
    {
        SupplierManager.getInstance().newWindow();
    }

    public void jobsClick()
    {
        screen_mgr.setScreen(Screens.JOBS.getScreen());
    }

    public void clientsClick()
    {
        ClientManager.getInstance().newWindow();
    }

    public void resourcesClick(){ResourceManager.getInstance().newWindow();}
}
