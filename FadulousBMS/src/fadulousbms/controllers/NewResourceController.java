package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Screen;
import fadulousbms.auxilary.Validators;
import fadulousbms.managers.ResourceManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.managers.SupplierManager;
import fadulousbms.model.Resource;
import fadulousbms.model.ResourceType;
import fadulousbms.model.Supplier;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;

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
public class NewResourceController extends Screen implements Initializable
{
    private boolean itemsModified;
    @FXML
    private TextField txtName,txtDescription,txtSerial,txtValue,txtAccount,txtUnit,txtQuantity;
    @FXML
    private DatePicker dateAcquired,dateExhausted;
    @FXML
    private ComboBox<ResourceType> cbxResourceType;

    @Override
    public void refresh()
    {
        ResourceManager.getInstance().loadDataFromServer();
        /*cbxResourceType.setCellFactory(new Callback<ListView<ResourceType>, ListCell<ResourceType>>()
        {
            @Override
            public ListCell<ResourceType> call(ListView<ResourceType> lst_resource_types)
            {
                return new ListCell<ResourceType>()
                {
                    @Override
                    protected void updateItem(ResourceType resource_type, boolean empty)
                    {
                        super.updateItem(resource_type, empty);
                        if(resource_type!=null && !empty)
                        {
                            setText(resource_type.getType_name());
                        }else{
                            setText("");
                        }
                    }
                };
            }
        });
        cbxResourceType.setButtonCell(new ListCell<ResourceType>()
        {
            @Override
            protected void updateItem(ResourceType resource_type, boolean empty)
            {
                super.updateItem(resource_type, empty);
                if(resource_type!=null && !empty)
                {
                    setText(resource_type.getType_name());
                }else{
                    setText("");
                }
            }
        });*/
        cbxResourceType.setItems(FXCollections.observableArrayList(ResourceManager.getInstance().getResource_types()));
        //cbxResourceType.setItems(FXCollections.observableArrayList(new String[]{"vehicle","equipment"}));
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
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
        if(!Validators.isValidNode(txtAccount, txtAccount.getText(), 1, ".+"))
        {
            txtAccount.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
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
        resource.setAccount(txtAccount.getText());
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
}
