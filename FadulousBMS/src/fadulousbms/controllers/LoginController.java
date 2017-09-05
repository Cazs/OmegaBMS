/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Screen;
import fadulousbms.managers.ScreenManager;
import fadulousbms.auxilary.Session;
import fadulousbms.managers.SessionManager;
import fadulousbms.exceptions.LoginException;
import fadulousbms.model.Employee;
import fadulousbms.model.Screens;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * views Controller class
 *
 * @author ghost
 */
public class LoginController implements Initializable, Screen 
{
    @FXML
    private TextField txtUsr = new TextField();
    @FXML
    private TextField txtPwd = new TextField();
    private ScreenManager screen_mgr;
    @FXML
    private ImageView img_profile;
    @FXML
    private final Label user_name = new Label();

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
        //TODO
        txtUsr.setText("ghost");
        txtPwd.setText("12345678");

        try
        {
            BufferedImage bufferedImage;
            bufferedImage = ImageIO.read(new File("images/profile.png"));
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            img_profile.setImage(image);

            /*for(int i=0;i<30;i++)
                news_feed_tiles.getChildren().add(createTile());*/
        }catch (IOException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
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
    public void createAccount()
    {
        screen_mgr.setScreen(Screens.CREATE_ACCOUNT.getScreen());
    }

    @FXML
    public void resetPassword(){screen_mgr.setScreen(Screens.RESET_PWD.getScreen());}

    @FXML
    public void login()
    {
        try 
        {
            String usr = txtUsr.getText(), pwd=txtPwd.getText();
            if(usr!=null && pwd!=null)
            {
                try
                {
                    Session session = RemoteComms.auth(usr, pwd);
                    SessionManager ssn_mgr = SessionManager.getInstance();
                    ssn_mgr.addSession(session);

                    //screen_mgr.getScreen(Screens.LOGIN.getScreen()).getParent()=null;
                    //Stage stage = (Stage)txtUsr.getScene().getWindow();
                    screen_mgr.removeScreen(screen_mgr.getScreen(Screens.LOGIN.getScreen()));
                    screen_mgr.setScreen(Screens.HOME.getScreen());
                    //stage.close();
                }catch(ConnectException ex)
                {
                    JOptionPane.showMessageDialog(null, ex.getMessage() + ", \nis the server up? are you connected to the network?", "Login failure", JOptionPane.ERROR_MESSAGE);
                    IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage() + ", \nis the server up? are you connected to the network?");
                } catch (LoginException ex) 
                {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Login failure", JOptionPane.ERROR_MESSAGE);
                    IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
                    //Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                JOptionPane.showMessageDialog(null, "Invalid entry.", "Login failure", JOptionPane.ERROR_MESSAGE);
                IO.log(getClass().getName(), IO.TAG_ERROR, "invalid input.");
            }
        } catch (IOException ex) 
        {
            IO.logAndAlert(getClass().getName(), ex.getMessage(), IO.TAG_ERROR);
            //Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
