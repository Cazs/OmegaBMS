package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Validators;
import fadulousbms.managers.ResourceManager;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.Resource;
import fadulousbms.model.ResourceType;
import fadulousbms.model.Screens;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class NewResourceController extends ScreenController implements Initializable
{
    private boolean itemsModified;
    @FXML
    private TextField txtName,txtDescription,txtSerial,txtValue,txtUnit,txtQuantity;
    @FXML
    private DatePicker dateAcquired,dateExhausted;
    @FXML
    private ComboBox<ResourceType> cbxResourceType;

    @Override
    public void refreshView()
    {
        if(ResourceManager.getInstance().getResource_types()!=null)
            cbxResourceType.setItems(FXCollections.observableArrayList(ResourceManager.getInstance().getResource_types().values()));
    }

    @Override
    public void refreshModel()
    {
        ResourceManager.getInstance().loadDataFromServer();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        new Thread(() ->
        {
            refreshModel();
            Platform.runLater(() -> refreshView());
        }).start();
    }

    @FXML
    public void createResource()
    {
        if(SessionManager.getInstance().getActive()==null)
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActive().isExpired())
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return;
        }
        if(!Validators.isValidNode(txtName, txtName.getText(), 1, ".+"))
        {
            txtName.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtDescription, txtDescription.getText(), 1, ".+"))
        {
            txtDescription.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtSerial, txtSerial.getText(), 1, ".+"))
        {
            txtSerial.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtValue, txtValue.getText(), 1, ".+"))
        {
            txtValue.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(cbxResourceType.getSelectionModel().getSelectedItem()==null)
        {
            //cbxResourceType.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            IO.logAndAlert("Validation Error", "Please select a valid resource type.", IO.TAG_ERROR);
            return;
        }
        if(dateAcquired.getValue()==null)
        {
            //dateAcquired.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            IO.logAndAlert("Validation Error", "Please choose a valid acquisition date.", IO.TAG_ERROR);
            return;
        }
        //TODO: date must be present/past not future?
        /*if(dateAcquired.getValue())
        {
            //dateAcquired.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            IO.logAndAlert("Validation Error", "Please choose a valid acquisition date.", IO.TAG_ERROR);
            return;
        }*/
        if(!Validators.isValidNode(txtQuantity, txtQuantity.getText(), 1, ".+"))
        {
            txtQuantity.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtUnit, txtUnit.getText(), 1, ".+"))
        {
            txtUnit.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }

        //prepare supplier parameters
        Resource resource = new Resource();
        resource.setResource_name(txtName.getText());
        resource.setResource_description(txtDescription.getText());
        resource.setResource_serial(txtSerial.getText());
        resource.setResource_value(Double.valueOf(txtValue.getText()));
        resource.setResource_type(cbxResourceType.getSelectionModel().getSelectedItem().get_id());
        resource.setUnit(txtUnit.getText());
        resource.setQuantity(Long.valueOf(txtQuantity.getText()));
        resource.setDate_acquired(dateAcquired.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        if(dateExhausted.getValue()!=null)
            resource.setDate_exhausted(dateExhausted.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        //if(str_extra!=null)
        //    quote.setExtra(str_extra);

        try
        {
            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));

            //create new supplier on database
            HttpURLConnection connection = RemoteComms.postData("/api/resource/add", resource.asUTFEncodedString(), headers);
            if(connection!=null)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());
                    IO.log(getClass().getName(), IO.TAG_INFO, "created resource["+response+"].");

                    if(response==null)
                    {
                        IO.logAndAlert("New Resource Creation Failure", "Invalid response.", IO.TAG_ERROR);
                        return;
                    }
                    if(response.isEmpty())
                    {
                        IO.logAndAlert("New Resource Creation Failure", "Invalid response.", IO.TAG_ERROR);
                        return;
                    }
                    ResourceManager.getInstance().setSelected(resource);
                    IO.logAndAlert("New Resource Creation Success", "Successfully created a new resource.", IO.TAG_INFO);
                    itemsModified = false;
                }else
                {
                    //Get error message
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                }
                if(connection!=null)
                    connection.disconnect();
            } else IO.logAndAlert("New Resource Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }
    }

    @FXML
    public void previousScreen()
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
                        if(screenManager.loadScreen(Screens.OPERATIONS.getScreen(),getClass().getResource("../views/"+Screens.OPERATIONS.getScreen())))
                        {
                            //Platform.runLater(() ->
                            screenManager.setScreen(Screens.OPERATIONS.getScreen());
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load operations screen.");
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
    public void newResourceType()
    {
        ResourceManager.getInstance().newResourceTypeWindow(param ->
        {
            new Thread(() ->
            {
                refreshModel();
                Platform.runLater(() -> refreshView());
            }).start();
            return null;
        });
    }
}
