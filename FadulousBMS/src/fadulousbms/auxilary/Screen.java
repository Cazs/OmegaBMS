/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.auxilary;

import fadulousbms.managers.QuoteManager;
import fadulousbms.managers.ScreenManager;
import fadulousbms.model.Screens;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 *
 * @author ghost
 */
public abstract class Screen
{
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
    private BorderPane loading_pane;

    public Screen()
    {
        //loading_pane.setVisible(false);
        //loading_bpane.setVisible(false);
        //shpLoad.setFill(Color.RED);
        //System.err.println(main_pane==null);
    }

    public abstract void refreshView();

    public abstract void refreshModel();

    public void refreshStatusBar(String msg)
    {
        try
        {
            boolean ping = RemoteComms.pingServer();
            Platform.runLater(() ->
            {
                shpServerStatus.setStroke(Color.TRANSPARENT);
                if(ping)
                    shpServerStatus.setFill(Color.LIME);
                else shpServerStatus.setFill(Color.RED);
                lblOutput.setText(msg);
            });
        } catch (IOException e)
        {
            if(Globals.DEBUG_ERRORS.getValue().equalsIgnoreCase("on"))
                IO.log(getClass().getName(), IO.TAG_ERROR, "could not refresh status bar: "+e.getMessage());
            Platform.runLater(() ->
            {
                shpServerStatus.setFill(Color.RED);
                lblOutput.setText(msg);
            });
        }
    }

    @FXML
    public void showLogin()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.LOGIN.getScreen(),getClass().getResource("../views/"+Screens.LOGIN.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.LOGIN.getScreen());
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
            if(ScreenManager.getInstance().loadScreen(Screens.HOME.getScreen(),getClass().getResource("../views/"+Screens.HOME.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.HOME.getScreen());
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
            if(ScreenManager.getInstance().loadScreen(Screens.CREATE_ACCOUNT.getScreen(),getClass().getResource("../views/"+Screens.CREATE_ACCOUNT.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.CREATE_ACCOUNT.getScreen());
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
            if(ScreenManager.getInstance().loadScreen(Screens.NEW_QUOTE.getScreen(),getClass().getResource("../views/"+Screens.NEW_QUOTE.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.NEW_QUOTE.getScreen());
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
            if(ScreenManager.getInstance().loadScreen(Screens.NEW_GENERIC_QUOTE.getScreen(),getClass().getResource("../views/"+Screens.NEW_GENERIC_QUOTE.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.NEW_GENERIC_QUOTE.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load generic quote creation screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void comingSoon()
    {
        IO.logAndAlert("Coming Soon", "This feature is currently being implemented.", IO.TAG_INFO);
    }

    public ImageView getProfileImageView()
    {
        return this.img_profile;
    }

    public Label getUserNameLabel()
    {
        return this.user_name;
    }

    public BorderPane getLoadingPane()
    {
        return this.loading_pane;
    }

    @FXML
    public void previousScreen()
    {
        try
        {
            ScreenManager.getInstance().setPreviousScreen();
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }
}
