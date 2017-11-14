/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.managers.*;
import fadulousbms.auxilary.Session;
import fadulousbms.exceptions.LoginException;
import fadulousbms.model.Employee;
import fadulousbms.model.Screens;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * views Controller class
 *
 * @author ghost
 */
public class LoginController extends ScreenController implements Initializable
{
    @FXML
    private TextField txtUsr;
    @FXML
    private TextField txtPwd;

    @Override
    public void refreshView()
    {
        //TODO
        txtUsr.setText("ghost");
        txtPwd.setText("12345678");
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
        refreshView();
    }

    @FXML
    public void resetPassword()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.RESET_PWD.getScreen(),getClass().getResource("../views/"+Screens.RESET_PWD.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.RESET_PWD.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load password reset screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void login()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String usr = txtUsr.getText(), pwd=txtPwd.getText();
                        if(usr!=null && pwd!=null)
                        {
                            Session session = RemoteComms.auth(usr, pwd);
                            SessionManager ssn_mgr = SessionManager.getInstance();
                            ssn_mgr.addSession(session);

                            //load data to memory
                            EmployeeManager.getInstance().loadDataFromServer();
                            //JobManager.getInstance().loadDataFromServer();
                            //ClientManager.getInstance().loadDataFromServer();
                            //SupplierManager.getInstance().loadDataFromServer();
                            //ResourceManager.getInstance().loadDataFromServer();
                            //QuoteManager.getInstance().loadDataFromServer();
                            //QuoteManager.getInstance().initialize(screenManager);

                            /*IO.log(getClass().getName(), IO.TAG_INFO,
                                    "operations loaded? " + (screenManager.loadScreen(Screens.OPERATIONS.getScreen(),
                                                            getClass().getResource("../views/"+Screens.OPERATIONS.getScreen()))==true));
                            IO.log(getClass().getName(), IO.TAG_INFO,
                                    "quotes loaded? " + (screenManager.loadScreen(Screens.QUOTES.getScreen(),
                                            getClass().getResource("../views/"+Screens.QUOTES.getScreen()))==true));
                            screenManager.peekScreenControllers().refresh();*/

                            //System.out.println(screenManager.loadScreen(Screens.HOME.getScreen(), getClass().getResource("../views/" + Screens.HOME.getScreen())));
                            if (screenManager.loadScreen(Screens.HOME.getScreen(), getClass().getResource("../views/" + Screens.HOME.getScreen())))
                            {
                                /*Thread t = new Thread(() ->
                                {
                                    JobManager.getInstance().loadDataFromServer();
                                    ClientManager.getInstance().loadDataFromServer();
                                    SupplierManager.getInstance().loadDataFromServer();
                                    ResourceManager.getInstance().loadDataFromServer();
                                    QuoteManager.getInstance().loadDataFromServer();
                                    QuoteManager.getInstance().initialize(screenManager);
                                });
                                t.start();*/
                                //Platform.runLater(() ->
                                screenManager.setScreen(Screens.HOME.getScreen());
                            } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load home screen.");
                        }else{
                            JOptionPane.showMessageDialog(null, "Invalid entry.", "Login failure", JOptionPane.ERROR_MESSAGE);
                            IO.log(getClass().getName(), IO.TAG_ERROR, "invalid input.");
                        }
                    }catch(ConnectException ex)
                    {
                        JOptionPane.showMessageDialog(null, ex.getMessage() + ", \nis the server up? are you connected to the network?", "Login failure", JOptionPane.ERROR_MESSAGE);
                        IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage() + ", \nis the server up? are you connected to the network?");
                    } catch (LoginException ex)
                    {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Login failure", JOptionPane.ERROR_MESSAGE);
                        IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }
}
