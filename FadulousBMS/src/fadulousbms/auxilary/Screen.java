/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.auxilary;

import fadulousbms.managers.QuoteManager;
import fadulousbms.managers.ScreenManager;
import fadulousbms.model.Screens;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 *
 * @author ghost
 */
public abstract class Screen
{
    private ScreenManager screen_mgr;
    @FXML
    private ImageView img_profile;
    @FXML
    private Label user_name;
    public static BufferedImage defaultProfileImage;
    @FXML
    private Circle shpServerStatus;
    @FXML
    private Label lblOutput;
    @FXML
    private Pane pane;
    @FXML
    private BorderPane bpane;

    public Screen()
    {
        pane.setVisible(false);
        bpane.setVisible(false);
    }

    public void setParent(ScreenManager mgr)
    {
        this.screen_mgr = mgr;
    }

    public abstract void refresh();

    public void refreshStatusBar(String msg)
    {
        try
        {
            shpServerStatus.setStroke(Color.TRANSPARENT);
            if(RemoteComms.pingServer())
                shpServerStatus.setFill(Color.LIME);
            else shpServerStatus.setFill(Color.RED);
        } catch (IOException e)
        {
            if(Globals.DEBUG_ERRORS.getValue().equalsIgnoreCase("on"))
                System.err.println("Could not refresh status bar: "+e.getMessage());
            shpServerStatus.setFill(Color.RED);
        }
        lblOutput.setText(msg);
    }

    @FXML
    public void showLogin()
    {
        try
        {
            if(this.getScreenManager().loadScreen(Screens.LOGIN.getScreen(),getClass().getResource("../views/"+Screens.LOGIN.getScreen())))
                this.getScreenManager().setScreen(Screens.LOGIN.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load login screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void showMain()
    {
        try
        {
            if(this.getScreenManager().loadScreen(Screens.HOME.getScreen(),getClass().getResource("../views/"+Screens.HOME.getScreen())))
                this.getScreenManager().setScreen(Screens.HOME.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load home screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void createAccount()
    {
        try
        {
            if(this.getScreenManager().loadScreen(Screens.CREATE_ACCOUNT.getScreen(),getClass().getResource("../views/"+Screens.CREATE_ACCOUNT.getScreen())))
                this.getScreenManager().setScreen(Screens.CREATE_ACCOUNT.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load account creation screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void newQuote()
    {
        QuoteManager.getInstance().setFromGeneric(false);
        QuoteManager.getInstance().nullifySelected();
        try
        {
            if(this.getScreenManager().loadScreen(Screens.NEW_QUOTE.getScreen(),getClass().getResource("../views/"+Screens.NEW_QUOTE.getScreen())))
                this.getScreenManager().setScreen(Screens.NEW_QUOTE.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load new quotes screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void newGenericQuote()
    {
        QuoteManager.getInstance().setFromGeneric(false);
        QuoteManager.getInstance().nullifySelected();
        try
        {
            if(this.getScreenManager().loadScreen(Screens.NEW_GENERIC_QUOTE.getScreen(),getClass().getResource("../views/"+Screens.NEW_GENERIC_QUOTE.getScreen())))
                this.getScreenManager().setScreen(Screens.NEW_GENERIC_QUOTE.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load generic quote creation screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    public ScreenManager getScreenManager()
    {
        return  this.screen_mgr;
    }

    public ImageView getProfileImageView()
    {
        return this.img_profile;
    }

    public Label getUserNameLabel()
    {
        return this.user_name;
    }

    @FXML
    public void previousScreen()
    {
        try
        {
            screen_mgr.setPreviousScreen();
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }
}
