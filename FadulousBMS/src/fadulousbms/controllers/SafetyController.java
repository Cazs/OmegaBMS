/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.Screen;
import fadulousbms.managers.*;
import fadulousbms.model.Employee;
import fadulousbms.model.Screens;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * views Controller class
 *
 * @author ghost
 */
public class SafetyController implements Initializable, Screen
{
    private ScreenManager screen_mgr;
    @FXML
    private ImageView img_profile;// = new ImageView();
    @FXML
    private Label user_name = new Label();

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
        try
        {
            //Set default profile photo
            BufferedImage bufferedImage;
            bufferedImage = ImageIO.read(new File("images/profile.png"));
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            img_profile.setImage(image);

            //Set current logged in employee
            Employee e = SessionManager.getInstance().getActiveEmployee();
            if(e!=null)
                user_name.setText(e.getFirstname() + " " + e.getLastname());

        }catch (IOException ex)
        {
            Logger.getLogger(HomescreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void safetyClick()
    {
        if (SessionManager.getInstance().getActive() != null)
        {
            if (!SessionManager.getInstance().getActive().isExpired())
            {
                /*try
                {
                    screen_mgr.loadScreen(Screens.SAFETY.getScreen(),
                            HomescreenController.class.getResource("../views/" + Screens.SAFETY.getScreen()));
                    screen_mgr.setScreen(Screens.SAFETY.getScreen());
                } catch (IOException ex)
                {
                    Logger.getLogger(HomescreenController.class.getName()).log(Level.SEVERE, null, ex);
                }*/
                SafetyManager.getInstance().initialize(screen_mgr);
                SafetyManager.getInstance().newWindow();
            }else{
                JOptionPane.showMessageDialog(null, "No active sessions!", "Session expired", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }else{
            JOptionPane.showMessageDialog(null, "No active sessions!", "Session expired", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    public void riskClick()
    {
        if (SessionManager.getInstance().getActive() != null)
        {
            if (!SessionManager.getInstance().getActive().isExpired())
            {
                RiskAssessmentManager.getInstance().initialize(screen_mgr);
                RiskAssessmentManager.getInstance().newWindow();
            }else{
                JOptionPane.showMessageDialog(null, "No active sessions!", "Session expired", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }else{
            JOptionPane.showMessageDialog(null, "No active sessions!", "Session expired", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    public void ohsClick()
    {
        if (SessionManager.getInstance().getActive() != null)
        {
            if (!SessionManager.getInstance().getActive().isExpired())
            {
                OHSManager.getInstance().initialize(screen_mgr);
                OHSManager.getInstance().newWindow();
            }else{
                JOptionPane.showMessageDialog(null, "No active sessions!", "Session expired", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }else{
            JOptionPane.showMessageDialog(null, "No active sessions!", "Session expired", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    public void appointmentClick()
    {
        if (SessionManager.getInstance().getActive() != null)
        {
            if (!SessionManager.getInstance().getActive().isExpired())
            {
                AppointmentManager.getInstance().initialize(screen_mgr);
                AppointmentManager.getInstance().newWindow();
            }else{
                JOptionPane.showMessageDialog(null, "No active sessions!", "Session expired", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }else{
            JOptionPane.showMessageDialog(null, "No active sessions!", "Session expired", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    public void showScanWindow()
    {
        /*Morena morena = new Morena();
        try
        {
            Manager manager= Manager.getInstance();
            morena.simpleScan();
            manager.close();
        } catch (Exception e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }*/
        //Scan scan = new Scan();

    }

    public void inspectionClick()
    {
        if (SessionManager.getInstance().getActive() != null)
        {
            if (!SessionManager.getInstance().getActive().isExpired())
            {
                InspectionManager.getInstance().initialize(screen_mgr);
                InspectionManager.getInstance().newWindow();
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
}
