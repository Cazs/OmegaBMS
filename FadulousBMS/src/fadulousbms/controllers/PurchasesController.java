package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.Screen;
import fadulousbms.managers.AssetManager;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.Employee;
import fadulousbms.model.Screens;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by ghost on 2017/02/02.
 */
public class PurchasesController extends Screen implements Initializable
{
    @Override
    public void refresh()
    {
        Employee e = SessionManager.getInstance().getActiveEmployee();
        if(e!=null)
            this.getUserNameLabel().setText(e.getFirstname() + " " + e.getLastname());
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
            AssetManager.getInstance().initialize(this.getScreenManager());
            defaultProfileImage = ImageIO.read(new File("images/profile.png"));
            Image image = SwingFXUtils.toFXImage(defaultProfileImage, null);
            this.getProfileImageView().setImage(image);
        }catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void stockClick()
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
                        if(screenManager.loadScreen(Screens.RESOURCES.getScreen(),getClass().getResource("../views/"+Screens.RESOURCES.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.RESOURCES.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load resources screen.");
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
    public void assetsClick()
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
                        if(screenManager.loadScreen(Screens.ASSETS.getScreen(),getClass().getResource("../views/"+Screens.ASSETS.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.ASSETS.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load assets screen.");
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
    public void otherClick()
    {
    }
}