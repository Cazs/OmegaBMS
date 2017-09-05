package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.Screen;
import fadulousbms.managers.*;
import fadulousbms.model.Employee;
import fadulousbms.model.Quote;
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ghost on 2017/01/18.
 */
public class SalesControllerOld implements Initializable, Screen
{
    @FXML
    private ImageView img_profile;
    @FXML
    private Label user_name;

    private ScreenManager screen_mgr;

    @Override
    public void refresh()
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
            if (e != null)
                user_name.setText(e.getFirstname() + " " + e.getLastname());
        }catch (IOException ex)
        {
            IO.log(OperationsController.class.getName(), IO.TAG_ERROR, ex.getMessage());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
    }

    @Override
    public void setParent(ScreenManager mgr)
    {
        screen_mgr = mgr;

        SaleManager.getInstance().initialize(screen_mgr);
        QuoteManager.getInstance().initialize(screen_mgr);
        InvoiceManager.getInstance().initialize(screen_mgr);
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

    @FXML
    public void quotesClick()
    {
        //QuoteManager.getInstance().newWindow();
        screen_mgr.setScreen(Screens.QUOTES.getScreen());
    }

    @FXML
    public void invoicesClick()
    {
        InvoiceManager.getInstance().newWindow();
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

    //Sales click event handlers
    @FXML
    public void showSalesClick()
    {
        SaleManager.getInstance().newWindow();
    }
}
