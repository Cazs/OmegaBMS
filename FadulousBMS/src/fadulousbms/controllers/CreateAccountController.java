/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.*;
import fadulousbms.exceptions.LoginException;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.Employee;
import fadulousbms.model.Screens;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class CreateAccountController implements Initializable, Screen
{
    private ScreenManager screen_mgr;
    @FXML
    private ImageView img_profile;
    @FXML
    private final Label user_name = new Label();
    @FXML
    private TextField txtUsername;// = new TextField();

    @FXML
    private TextField txtPassword;// = new TextField();

    @FXML
    private TextField txtFirstname;// = new TextField();

    @FXML
    private TextField txtLastname;// = new TextField();

    @FXML
    private ComboBox cbxSex, cbxAccessLevel;// = new ComboBox();

    @FXML
    private TextField txtEmail;// = new TextField();

    @FXML
    private TextField txtTelephone;// = new TextField();

    @FXML
    private TextField txtCellphone;// = new TextField();

    @FXML
    private TextArea txtOther;// = new TextArea();

    private String[] access_levels = {"NONE", "NORMAL", "ADMIN", "SUPER"};

    @Override
    public void refresh()
    {
        Employee e = SessionManager.getInstance().getActiveEmployee();
        if(e!=null)
            user_name.setText(e.getFirstname() + " " + e.getLastname());
        else IO.log(getClass().getName(), IO.TAG_ERROR, "No active sessions.");

        cbxAccessLevel.setItems(FXCollections.observableArrayList(access_levels));
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        try
        {
            BufferedImage bufferedImage;
            bufferedImage = ImageIO.read(new File("images/profile.png"));
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            img_profile.setImage(image);
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

    public void showLogin()
    {
        /*try
        {
            Stage stage = new Stage();
            //stage.setAlwaysOnTop(true);
            stage.setTitle("Login to BMS Engine");
            stage.setMinWidth(320);
            stage.setMinHeight(280);
            stage.setAlwaysOnTop(true);

            ScreenManager login_screen_mgr = new ScreenManager();

            login_screen_mgr.loadScreen(Screens.LOGIN.getScreen(), getClass().getResource("../../views/"+Screens.LOGIN.getScreen()));
            */
        screen_mgr.setScreen(Screens.LOGIN.getScreen());

            /*Group root = new Group();
            root.getChildren().add(screen_mgr);
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
            });*/

        /*} catch (IOException ex)
        {
            Logger.getLogger(HomescreenController.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    @FXML
    public void showMain()
    {
        screen_mgr.setScreen(Screens.HOME.getScreen());
    }

    public void createAccount() throws IOException
    {
        int sex_index = cbxSex.getSelectionModel().selectedIndexProperty().get();

        if(!validateFormField(txtUsername, "Invalid Username", "please enter a valid username", "^.*(?=.{5,}).*"))
            return;
        if(!validateFormField(txtPassword, "Invalid Password", "please enter a valid password", "^.*(?=.{8,}).*"))//(?=[a-zA-Z])(?=.*[0-9])(?=.*[@#!$%^&*-+=])
            return;
        if(!validateFormField(txtFirstname, "Invalid Firstname", "please enter a valid first name", "^.*(?=.{1,}).*"))
            return;
        if(!validateFormField(txtLastname, "Invalid Lastname", "please enter a valid last name", "^.*(?=.{1,}).*"))
            return;

        if(sex_index<0)
        {
            cbxSex.getStyleClass().remove("form-control-default");
            cbxSex.getStyleClass().add("control-input-error");
        }else{
            cbxSex.getStyleClass().remove("control-input-error");
            cbxSex.getStyleClass().add("form-control-default");
        }
        if(!validateFormField(txtEmail, "Invalid Email", "please enter a valid email address", "^.*(?=.{5,})(?=(.*@.*\\.)).*"))
            return;
        if(!validateFormField(txtTelephone, "Invalid Telephone Number", "please enter a valid telephone number", "^.*(?=.{10,}).*"))
            return;
        if(!validateFormField(txtCellphone, "Invalid Cellphone Number", "please enter a valid cellphone number", "^.*(?=.{10,}).*"))
            return;

        //all valid, send data to server
        int access_level_index = cbxAccessLevel.getSelectionModel().getSelectedIndex();
        if(access_level_index>=0)
        {
            if(access_levels[access_level_index].toLowerCase().equals("super"))
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
                params.add(new AbstractMap.SimpleEntry<>("usr", txtUsername.getText()));
                params.add(new AbstractMap.SimpleEntry<>("pwd", txtPassword.getText()));
                params.add(new AbstractMap.SimpleEntry<>("access_level", "3"));
                params.add(new AbstractMap.SimpleEntry<>("firstname", txtFirstname.getText()));
                params.add(new AbstractMap.SimpleEntry<>("lastname", txtLastname.getText()));
                params.add(new AbstractMap.SimpleEntry<>("gender", cbxSex.getItems().get(sex_index).toString()));
                params.add(new AbstractMap.SimpleEntry<>("email", txtEmail.getText()));
                params.add(new AbstractMap.SimpleEntry<>("tel", txtTelephone.getText()));
                params.add(new AbstractMap.SimpleEntry<>("cell", txtCellphone.getText()));
                if(txtOther.getText()!=null)
                    params.add(new AbstractMap.SimpleEntry<>("other", txtOther.getText()));

                HttpURLConnection connection = RemoteComms.postData("/api/employee/add", params, null);
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    IO.logAndAlert("Account Creation Success", IO.readStream(connection.getInputStream()), IO.TAG_INFO);
                }else IO.logAndAlert("Account Creation Failure", IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);

                connection.disconnect();
            }
        }
    }

    private boolean validateFormField(TextField txt, String errTitle, String errMsg, String regex)
    {
        if(!Validators.isValidNode(txt, txt.getText(), regex))
        {
            IO.logAndAlert(errTitle, errMsg, IO.TAG_ERROR);
            return false;
        }
        return true;
    }
}
