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
import java.io.*;
import java.net.ConnectException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Set;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * views Controller class
 *
 * @author ghost
 */
public class SettingsController implements Initializable, Screen 
{
    @FXML
    private TextField txtIP = new TextField();
    @FXML
    private TextField txtPort = new TextField();
    private ScreenManager screen_mgr;
    @FXML
    private ImageView img_profile, img_logo;
    @FXML
    private final Label user_name = new Label();

    @Override
    public void refresh()
    {
        txtIP.setText("127.0.0.1");
        txtPort.setText("9000");
        RemoteComms.host = "http://127.0.0.1:9000";

        try
        {
            BufferedImage bufferedImage;
            bufferedImage = ImageIO.read(new File("images/profile.png"));
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            img_profile.setImage(image);

            if(SessionManager.getInstance().getActive()!=null)
            {
                if(!SessionManager.getInstance().getActive().isExpired())
                {
                    ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie",
                            SessionManager.getInstance().getActive().getSessionId()));

                    byte[] file = RemoteComms.sendFileRequest("logo", headers);
                    ByteArrayInputStream bis = new ByteArrayInputStream(file);
                    BufferedImage buff_img = ImageIO.read(bis);
                    Image img = SwingFXUtils.toFXImage(buff_img, null);

                    img_logo.setImage(img);
                }else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
            }else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
            /*for(int i=0;i<30;i++)
                news_feed_tiles.getChildren().add(createTile());*/
        }catch (IOException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
        }

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

    }    

    @FXML
    public void changeLogo()
    {
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                try
                {
                    FileChooser fileChooser = new FileChooser();
                    File f = fileChooser.showOpenDialog(txtIP.getScene().getWindow());
                    if (f != null)
                    {
                        if (f.exists())
                        {
                            FileInputStream in = new FileInputStream(f);
                            byte[] buffer = new byte[(int) f.length()];
                            in.read(buffer, 0, buffer.length);
                            in.close();

                            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                            headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));
                            headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "image/" + f.getName().split("\\.")[1]));
                            headers.add(new AbstractMap.SimpleEntry<>("Filetype", f.getName().split("\\.")[1]));
                            RemoteComms.uploadFile("/api/upload/logo", headers, buffer);
                            System.out.println("\n File size: " + buffer.length + " bytes.");
                        } else
                        {
                            IO.logAndAlert(getClass().getName(), "File not found.", IO.TAG_ERROR);
                        }
                    } else
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, "File object is null.");
                    }
                } catch (FileNotFoundException e)
                {
                    IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
                } catch (IOException e)
                {
                    IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
                }
            }else{
                IO.logAndAlert(getClass().getName(), "Active session has expired.", IO.TAG_ERROR);
            }
        }else{
            IO.logAndAlert(getClass().getName(), "Active session is invalid.", IO.TAG_ERROR);
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
    public void applySettings()
    {
        if(txtPort.getText()!=null && txtIP.getText()!=null)
        {
            //RemoteComms.host = "http://" + txtIP.getText() + ":" + txtPort.getText() + "/";
            RemoteComms.setHost("http://" + txtIP.getText() + ":" + txtPort.getText());
            IO.logAndAlert(getClass().getName(), "successfully updated system configuration.", IO.TAG_INFO);
        } else IO.logAndAlert(SettingsController.class.getName(), "Empty entries are not allowed for required fields.", IO.TAG_ERROR);
    }
    
}
