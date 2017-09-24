/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.*;
import fadulousbms.managers.SafetyManager;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.Employee;
import fadulousbms.model.Screens;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * views Controller class
 *
 * @author ghost
 */
public class HomescreenController extends Screen implements Initializable
{
    @FXML
    private Button btnCreateAccount;

    private ColorAdjust colorAdjust = new ColorAdjust();

    @Override
    public void refresh()
    {
        //this.getLoadingPane().setVisible(true);

        Employee e = SessionManager.getInstance().getActiveEmployee();
        if(e!=null)
        {
            this.getUserNameLabel().setText(e.getFirstname() + " " + e.getLastname());
            if(e.getAccessLevel() >= AccessLevels.ADMIN.getLevel())
            {
                IO.log(getClass().getName(), IO.TAG_INFO, "enabling account creation button.");
                btnCreateAccount.setDisable(false);
            }
        }
        else IO.log(getClass().getName(), IO.TAG_ERROR, "No active sessions.");

        //this.getLoadingPane().setVisible(false);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        //try
        {
            //defaultProfileImage = ImageIO.read(new File("images/profile.png"));
            Image image = SwingFXUtils.toFXImage(defaultProfileImage, null);
            this.getProfileImageView().setImage(image);
            //img_profile.setImage(new Image("dist/profile.png"));
            colorAdjust.setBrightness(0.0);

            refresh();
        }/*catch (IOException ex)
        {
            Logger.getLogger(HomescreenController.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }
    
    private Rectangle createTile()
    {
        Rectangle rectangle = new Rectangle(160, 100);
        Random rand = new Random();
        double r = rand.nextDouble();
        double g = rand.nextDouble();
        double b = rand.nextDouble();
        rectangle.setStroke(Color.WHITE);
        rectangle.setFill(new Color(r,g,b,0.5));

        return rectangle;
    }
    
    public void operationsClick()
    {
        try
        {
            if(this.getScreenManager().loadScreen(Screens.OPERATIONS.getScreen(),getClass().getResource("../views/"+Screens.OPERATIONS.getScreen())))
                this.getScreenManager().setScreen(Screens.OPERATIONS.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load operations screen.");
        } catch (IOException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
        }
    }

    public void safetyClick()
    {
        if (SessionManager.getInstance().getActive() != null)
        {
            if (!SessionManager.getInstance().getActive().isExpired())
            {
                try
                {
                    this.getScreenManager().loadScreen(Screens.SAFETY.getScreen(),
                            HomescreenController.class.getResource("../views/" + Screens.SAFETY.getScreen()));
                    this.getScreenManager().setScreen(Screens.SAFETY.getScreen());
                } catch (IOException ex)
                {
                    Logger.getLogger(HomescreenController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                JOptionPane.showMessageDialog(null, "No active sessions!", "Session expired", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }else{
            JOptionPane.showMessageDialog(null, "No active sessions!", "Session expired", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }
    
    public void operationsMouseEnter()
    {
        KeyValue start_kv = new KeyValue(colorAdjust.brightnessProperty(), 
                                         colorAdjust.brightnessProperty().getValue(), 
                                         Interpolator.LINEAR);
        KeyValue end_kv =   new KeyValue(colorAdjust.brightnessProperty(), 
                                         1, 
                                         Interpolator.LINEAR);
        
        KeyFrame start_frame = new KeyFrame(Duration.millis(0), start_kv);
        KeyFrame end_frame = new KeyFrame(  Duration.millis(100), end_kv);
        
        Timeline fadeInTimeline = new Timeline(start_frame, end_frame);
        
        fadeInTimeline.setCycleCount(1);
        fadeInTimeline.setAutoReverse(false);
        fadeInTimeline.play();
        //System.out.println("Operations enter");
    }
    
    public void operationsMouseLeave()
    {
        KeyValue start_kv = new KeyValue(colorAdjust.brightnessProperty(), 
                                         colorAdjust.brightnessProperty().getValue(), 
                                         Interpolator.LINEAR);
        KeyValue end_kv =   new KeyValue(colorAdjust.brightnessProperty(), 
                                         0, 
                                         Interpolator.LINEAR);
        
        KeyFrame start_frame = new KeyFrame(Duration.millis(0), start_kv);
        KeyFrame end_frame = new KeyFrame(Duration.millis(100), end_kv);
        
        Timeline fadeOutTimeline = new Timeline(start_frame, end_frame);
        
        fadeOutTimeline.setCycleCount(1);
        fadeOutTimeline.setAutoReverse(false);
        fadeOutTimeline.play();
    }

    @FXML
    public void hrClick()
    {
        try
        {
            if(this.getScreenManager().loadScreen(Screens.HR.getScreen(),getClass().getResource("../views/"+Screens.HR.getScreen())))
                this.getScreenManager().setScreen(Screens.HR.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load human resources screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void accountingClick()
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
                        if(screenManager.loadScreen(Screens.ACCOUNTING.getScreen(),getClass().getResource("../views/"+Screens.ACCOUNTING.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.ACCOUNTING.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load accounting screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    @FXML
    public void facilitiesClick()
    {
        try
        {
            if(this.getScreenManager().loadScreen(Screens.FACILITIES.getScreen(),getClass().getResource("../views/"+Screens.FACILITIES.getScreen())))
                this.getScreenManager().setScreen(Screens.FACILITIES.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load facilities screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void showSettings()
    {
        try
        {
            if(this.getScreenManager().loadScreen(Screens.SETTINGS.getScreen(),getClass().getResource("../views/"+Screens.SETTINGS.getScreen())))
                this.getScreenManager().setScreen(Screens.SETTINGS.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load settings screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }
}
