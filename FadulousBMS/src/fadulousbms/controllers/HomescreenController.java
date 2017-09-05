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
public class HomescreenController implements Initializable, Screen
{
    //@FXML
    //TilePane news_feed_tiles; //= new TilePane();
    @FXML
    private BorderPane btnOperations;
    private ScreenManager screen_mgr;
    @FXML
    private ImageView img_profile;// = new ImageView();
    @FXML
    private Label user_name;
    @FXML
    private Button btnCreateAccount;
    public static BufferedImage defaultProfileImage;

    private ColorAdjust colorAdjust = new ColorAdjust();

    @Override
    public void refresh()
    {
        Employee e = SessionManager.getInstance().getActiveEmployee();
        if(e!=null)
        {
            user_name.setText(e.getFirstname() + " " + e.getLastname());
            if(e.getAccessLevel() >= AccessLevels.ADMIN.getLevel())
            {
                IO.log(getClass().getName(), IO.TAG_INFO, "enabling account creation button.");
                btnCreateAccount.setDisable(false);
            }
        }
        else IO.log(getClass().getName(), IO.TAG_ERROR, "No active sessions.");
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        try
        {
            defaultProfileImage = ImageIO.read(new File("images/profile.png"));
            Image image = SwingFXUtils.toFXImage(defaultProfileImage, null);
            img_profile.setImage(image);
            //img_profile.setImage(new Image("dist/profile.png"));
            colorAdjust.setBrightness(0.0);
            //btnOperations.setEffect(colorAdjust);

            /*for(int i=0;i<30;i++)
                news_feed_tiles.getChildren().add(createTile());*/
            //Set current logged in employee
            refresh();
        }catch (IOException ex)
        {
            Logger.getLogger(HomescreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        if (SessionManager.getInstance().getActive() != null)
        {
            if (!SessionManager.getInstance().getActive().isExpired())
            {
                try
                {
                    screen_mgr.loadScreen(Screens.OPERATIONS.getScreen(),
                            HomescreenController.class.getResource("../views/" + Screens.OPERATIONS.getScreen()));
                    screen_mgr.setScreen(Screens.OPERATIONS.getScreen());
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

    public void safetyClick()
    {
        if (SessionManager.getInstance().getActive() != null)
        {
            if (!SessionManager.getInstance().getActive().isExpired())
            {
                try
                {
                    screen_mgr.loadScreen(Screens.SAFETY.getScreen(),
                            HomescreenController.class.getResource("../views/" + Screens.SAFETY.getScreen()));
                    screen_mgr.setScreen(Screens.SAFETY.getScreen());
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

    @Override
    public void setParent(ScreenManager mgr) 
    {
        screen_mgr = mgr;
    }

    public void showLogin()
    {
        /*try
        {
            Stage stage = new Stage();
            //stage.setAlwaysOnTop(true);
            stage.setTitle("Login to BMS Engine");
            stage.setMinWidth(320);
            stage.setMinHeight(280);
            stage.setAlwaysOnTop(true);
            
            ScreenManager login_screen_mgr = new ScreenManager();
            
            login_screen_mgr.loadScreen(Screens.LOGIN.getScreen(), getClass().getResource("../../views/"+Screens.LOGIN.getScreen()));
            */
            screen_mgr.setScreen(Screens.LOGIN.getScreen());
            
            /*Group root = new Group();
            root.getChildren().add(screen_mgr);
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
            });*/
            
        /*} catch (IOException ex) 
        {
            Logger.getLogger(HomescreenController.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    public void showRegistrationForm()
    {
        screen_mgr.setScreen(Screens.CREATE_ACCOUNT.getScreen());
        /*if (SessionManager.getInstance().getActive() != null)
        {
            if (!SessionManager.getInstance().getActive().isExpired())
            {
                try
                {
                    screen_mgr.loadScreen(Screens.CREATE_ACCOUNT.getScreen(),
                            HomescreenController.class.getResource("../views/" + Screens.CREATE_ACCOUNT.getScreen()));
                    screen_mgr.setScreen(Screens.CREATE_ACCOUNT.getScreen());
                } catch (IOException ex)
                {
                    IO.logAndAlert(getClass().getName(), ex.getMessage(), IO.TAG_ERROR);
                }
            }else{
                JOptionPane.showMessageDialog(null, "No active sessions!", "Session expired", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }else{
            JOptionPane.showMessageDialog(null, "No active sessions!", "Session expired", JOptionPane.ERROR_MESSAGE);
            return;
        }*/
    }
    @FXML
    public void showSettings()
    {
        screen_mgr.setScreen(Screens.SETTINGS.getScreen());
    }
}
