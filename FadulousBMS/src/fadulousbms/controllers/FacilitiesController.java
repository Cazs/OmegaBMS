package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.Screen;
import fadulousbms.managers.*;
import fadulousbms.model.Employee;
import fadulousbms.model.Screens;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ghost on 2017/02/02.
 */
public class FacilitiesController extends Screen implements Initializable
{
    private ScreenManager screen_mgr;
    @FXML
    private final ImageView img_profile = new ImageView();
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
        if (SessionManager.getInstance().getActive() != null)
        {
            if (!SessionManager.getInstance().getActive().isExpired())
            {
                AssetManager.getInstance().initialize(screen_mgr);
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
        /*screen_mgr.addEventHandler(EventType.ROOT, eventHandler);
        container.prefWidthProperty().bind(screen_mgr.widthProperty());
        container.prefHeightProperty().bind(screen_mgr.heightProperty());*/
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

    @FXML
    public void productionClick()
    {
        try
        {
            screen_mgr.loadScreen(Screens.OPERATIONS.getScreen(),
                    getClass().getResource("../views/" + Screens.OPERATIONS.getScreen()));
            screen_mgr.setScreen(Screens.OPERATIONS.getScreen());
        } catch (IOException ex)
        {
            Logger.getLogger(OperationsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void facilitiesClick()
    {
        try
        {
            screen_mgr.loadScreen(Screens.OPERATIONS_FACILITIES.getScreen(),
                    getClass().getResource("../views/" + Screens.OPERATIONS_FACILITIES.getScreen()));
            screen_mgr.setScreen(Screens.OPERATIONS_FACILITIES.getScreen());
        } catch (IOException ex)
        {
            Logger.getLogger(OperationsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void salesClick()
    {
        try
        {
            screen_mgr.loadScreen(Screens.OPERATIONS_SALES.getScreen(),
                    getClass().getResource("../views/" + Screens.OPERATIONS_SALES.getScreen()));
            screen_mgr.setScreen(Screens.OPERATIONS_SALES.getScreen());
        } catch (IOException ex)
        {
            Logger.getLogger(OperationsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Asset click event handlers
    @FXML
    public void assetsClick()
    {
        AssetManager.getInstance().newWindow();
    }
}
