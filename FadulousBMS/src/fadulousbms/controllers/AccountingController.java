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
public class AccountingController extends Screen implements Initializable
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
    public void purchasesClick()
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
                        if(screenManager.loadScreen(Screens.PURCHASES.getScreen(),getClass().getResource("../views/"+Screens.PURCHASES.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.PURCHASES.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load purchases screen.");
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
    public void invoicesClick()
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
                        if(screenManager.loadScreen(Screens.INVOICES.getScreen(),getClass().getResource("../views/"+Screens.INVOICES.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.INVOICES.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load invoices screen.");
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
