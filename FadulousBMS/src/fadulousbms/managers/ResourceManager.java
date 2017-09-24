package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import fadulousbms.auxilary.*;
import fadulousbms.controllers.HomescreenController;
import fadulousbms.controllers.OperationsController;
import fadulousbms.model.*;
import fadulousbms.model.Error;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ghost on 2017/01/13.
 */
public class ResourceManager extends BusinessObjectManager
{
    private Resource[] resources;
    private Resource selected;
    private TableView tblResources;
    private Gson gson;
    private static ResourceManager resource_manager = new ResourceManager();

    //public static final String[] RESOURCE_TYPES = {"VEHICLE", "EQUIPMENT"};
    private ResourceType[] resource_types;
    public static final String TAG = "ResourceManager";
    public static final String ROOT_PATH = "cache/resources/";
    public String filename = "";
    private long timestamp;

    private ResourceManager()
    {
    }

    public static ResourceManager getInstance()
    {
        return resource_manager;
    }

    public Resource[] getResources()
    {
        return resources;
    }

    public ResourceType[] getResourceTypes()
    {
        return resource_types;
    }

    public void setSelected(Resource resource)
    {
        this.selected=resource;
    }

    public Resource getSelected()
    {
        return this.selected;
    }

    public ResourceType[] getResource_types()
    {
        return resource_types;
    }

    @Override
    public void initialize(ScreenManager screenManager)
    {
        loadDataFromServer();
    }

    public void loadDataFromServer()
    {
        try
        {
            SessionManager smgr = SessionManager.getInstance();
            if (smgr.getActive() != null)
            {
                if (!smgr.getActive().isExpired())
                {
                    gson = new GsonBuilder().create();
                    ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));

                    //Get Timestamp
                    String timestamp_json = RemoteComms.sendGetRequest("/api/timestamp/resources_timestamp", headers);
                    Counters cntr_timestamp = gson.fromJson(timestamp_json, Counters.class);
                    if(cntr_timestamp!=null)
                    {
                        timestamp = cntr_timestamp.getCount();
                        filename = "resources_"+timestamp+".dat";
                        IO.log(this.getClass().getName(), IO.TAG_INFO, "Server Timestamp: "+timestamp);
                    }else {
                        IO.logAndAlert(this.getClass().getName(), "could not get valid timestamp", IO.TAG_ERROR);
                        return;
                    }

                    if(!isSerialized(ROOT_PATH+filename))
                    {
                        String resources_json = RemoteComms.sendGetRequest("/api/resources", headers);
                        resources = gson.fromJson(resources_json, Resource[].class);

                        String resource_types_json = RemoteComms.sendGetRequest("/api/resource/types", headers);
                        resource_types = gson.fromJson(resource_types_json, ResourceType[].class);

                        IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of clients.");

                        this.serialize(ROOT_PATH+filename, resources);
                        this.serialize(ROOT_PATH+"resource_types.dat", resource_types);
                    }else{
                        IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object ["+ROOT_PATH+filename+"] on local disk is already up-to-date.");
                        resources = (Resource[]) this.deserialize(ROOT_PATH+filename);
                        resource_types = (ResourceType[]) this.deserialize(ROOT_PATH+"resource_types.dat");
                    }
                } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
            } else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
        }catch (JsonSyntaxException ex)
        {
            IO.log(TAG, IO.TAG_ERROR, ex.getMessage());
        }catch (MalformedURLException ex)
        {
            IO.log(TAG, IO.TAG_ERROR, ex.getMessage());
        } catch (ClassNotFoundException ex)
        {
            IO.log(TAG, IO.TAG_ERROR, ex.getMessage());
        }catch (IOException ex)
        {
            IO.log(TAG, IO.TAG_ERROR, ex.getMessage());
        }
    }

    public void handleNewResource(Stage parentStage)
    {
        parentStage.setAlwaysOnTop(false);
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Add New Resource");
        stage.setMinWidth(320);
        stage.setMinHeight(350);
        //stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(10);

        final TextField txt_resource_name = new TextField();
        txt_resource_name.setMinWidth(200);
        txt_resource_name.setMaxWidth(Double.MAX_VALUE);
        HBox resource_name = CustomTableViewControls.getLabelledNode("Resource name", 200, txt_resource_name);

        final TextField txt_resource_description = new TextField();
        txt_resource_description.setMinWidth(200);
        txt_resource_description.setMaxWidth(Double.MAX_VALUE);
        HBox resource_description = CustomTableViewControls.getLabelledNode("Resource description", 200, txt_resource_description);

        final TextField txt_resource_serial = new TextField();
        txt_resource_serial.setMinWidth(200);
        txt_resource_serial.setMaxWidth(Double.MAX_VALUE);
        HBox resource_serial = CustomTableViewControls.getLabelledNode("Resource serial", 200, txt_resource_serial);

        final ComboBox<ResourceType> cbx_resource_type = new ComboBox<>();
        cbx_resource_type.setCellFactory(new Callback<ListView<ResourceType>, ListCell<ResourceType>>()
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
        cbx_resource_type.setButtonCell(new ListCell<ResourceType>()
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
        });
        cbx_resource_type.setItems(FXCollections.observableArrayList(resource_types));
        cbx_resource_type.setMinWidth(200);
        cbx_resource_type.setMaxWidth(Double.MAX_VALUE);
        HBox resource_type = CustomTableViewControls.getLabelledNode("Resource type", 200, cbx_resource_type);

        final TextField txt_resource_value = new TextField();
        txt_resource_value.setMinWidth(200);
        txt_resource_value.setMaxWidth(Double.MAX_VALUE);
        HBox resource_value = CustomTableViewControls.getLabelledNode("Value", 200, txt_resource_value);

        final TextField txt_markup = new TextField();
        txt_markup.setMinWidth(200);
        txt_markup.setMaxWidth(Double.MAX_VALUE);
        HBox markup = CustomTableViewControls.getLabelledNode("Markup", 200, txt_markup);

        final TextField txt_labour = new TextField();
        txt_labour.setMinWidth(200);
        txt_labour.setMaxWidth(Double.MAX_VALUE);
        HBox labour = CustomTableViewControls.getLabelledNode("Labour", 200, txt_labour);

        final TextField txt_quantity = new TextField();
        txt_quantity.setMinWidth(200);
        txt_quantity.setMaxWidth(Double.MAX_VALUE);
        HBox quantity = CustomTableViewControls.getLabelledNode("Quantity", 200, txt_quantity);

        final TextField txt_unit = new TextField();
        txt_unit.setMinWidth(200);
        txt_unit.setMaxWidth(Double.MAX_VALUE);
        HBox unit = CustomTableViewControls.getLabelledNode("Unit", 200, txt_unit);

        DatePicker dpk_date_acquired = new DatePicker();
        dpk_date_acquired.setMinWidth(200);
        dpk_date_acquired.setMaxWidth(Double.MAX_VALUE);
        HBox date_acquired = CustomTableViewControls.getLabelledNode("Date acquired", 200, dpk_date_acquired);

        DatePicker dpk_date_exhausted = new DatePicker();
        dpk_date_exhausted.setMinWidth(200);
        dpk_date_exhausted.setMaxWidth(Double.MAX_VALUE);
        HBox date_exhausted = CustomTableViewControls.getLabelledNode("Date exhausted", 200, dpk_date_exhausted);


        final TextField txt_other = new TextField();
        txt_other.setMinWidth(200);
        txt_other.setMaxWidth(Double.MAX_VALUE);
        HBox other = CustomTableViewControls.getLabelledNode("Other", 200, txt_other);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Submit", event ->
        {
            String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            if(!Validators.isValidNode(txt_resource_name, txt_resource_name.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_resource_description, txt_resource_description.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_resource_serial, txt_resource_serial.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(cbx_resource_type, cbx_resource_type.getValue()==null?"":cbx_resource_type.getValue().get_id(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_resource_value, txt_resource_value.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_markup, txt_markup.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_labour, txt_labour.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_quantity, txt_quantity.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_unit, txt_unit.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(dpk_date_acquired, dpk_date_acquired.getValue()==null?"":dpk_date_acquired.getValue().toString(), 4, date_regex))
                return;
            /*if(!Validators.isValidNode(dpk_date_exhausted, dpk_date_exhausted.getValue()==null?"":dpk_date_exhausted.getValue().toString(), 4, date_regex))
                return;
            if(!Validators.isValidNode(txt_other, txt_other.getText(), 1, ".+"))
                return;*/

            long date_acquired_in_sec, date_exhausted_in_sec=0;
            String str_resource_name = txt_resource_name.getText();
            String str_resource_description = txt_resource_description.getText();
            String str_resource_serial = txt_resource_serial.getText();
            String str_resource_type = cbx_resource_type.getValue().get_id();
            String str_resource_value = txt_resource_value.getText();
            String str_markup = txt_markup.getText();
            String str_labour = txt_labour.getText();
            String str_quantity = txt_markup.getText();
            String str_unit = txt_markup.getText();
            date_acquired_in_sec = dpk_date_acquired.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            if(dpk_date_exhausted.getValue()!=null)
                date_exhausted_in_sec = dpk_date_exhausted.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            String str_other = txt_other.getText();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("resource_name", str_resource_name));
            params.add(new AbstractMap.SimpleEntry<>("resource_description", str_resource_description));
            params.add(new AbstractMap.SimpleEntry<>("resource_serial", str_resource_serial));
            params.add(new AbstractMap.SimpleEntry<>("resource_type", str_resource_type));
            params.add(new AbstractMap.SimpleEntry<>("resource_value", str_resource_value));
            params.add(new AbstractMap.SimpleEntry<>("markup", str_markup));
            params.add(new AbstractMap.SimpleEntry<>("labour", str_labour));
            params.add(new AbstractMap.SimpleEntry<>("quantity", str_quantity));
            params.add(new AbstractMap.SimpleEntry<>("unit", str_unit));
            params.add(new AbstractMap.SimpleEntry<>("date_acquired", String.valueOf(date_acquired_in_sec)));
            if(date_exhausted_in_sec>0)
                params.add(new AbstractMap.SimpleEntry<>("date_exhausted", String.valueOf(date_exhausted_in_sec)));
            params.add(new AbstractMap.SimpleEntry<>("other", str_other));

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                if(SessionManager.getInstance().getActive()!=null)
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
                else
                {
                    IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
                    return;
                }

                HttpURLConnection connection = RemoteComms.postData("/api/resource/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Success", "Successfully added a new resource!", IO.TAG_INFO);
                    }else
                    {
                        //Get error message
                        String msg = IO.readStream(connection.getErrorStream());
                        Gson gson = new GsonBuilder().create();
                        Error error = gson.fromJson(msg, Error.class);
                        IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), error.getError(), IO.TAG_ERROR);
                    }
                }
            } catch (IOException e)
            {
                IO.log(TAG, IO.TAG_ERROR, e.getMessage());
            }
        });

        //Add form controls vertically on the scene
        vbox.getChildren().add(resource_name);
        vbox.getChildren().add(resource_description);
        vbox.getChildren().add(resource_serial);
        vbox.getChildren().add(resource_type);
        vbox.getChildren().add(resource_value);
        vbox.getChildren().add(markup);
        vbox.getChildren().add(labour);
        vbox.getChildren().add(quantity);
        vbox.getChildren().add(unit);
        vbox.getChildren().add(date_acquired);
        vbox.getChildren().add(date_exhausted);
        vbox.getChildren().add(other);
        vbox.getChildren().add(submit);

        //Setup scene and display
        Scene scene = new Scene(vbox);
        File fCss = new File("src/fadulousbms/styles/home.css");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

        stage.onHidingProperty().addListener((observable, oldValue, newValue) ->
                loadDataFromServer());

        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
        stage.setResizable(true);
    }

    public void handleNewResourceType(Stage parentStage)
    {
        parentStage.setAlwaysOnTop(false);
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Add New Resource Type");
        stage.setMinWidth(320);
        stage.setMinHeight(200);
        //stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(10);

        final TextField txt_type_name = new TextField();
        txt_type_name.setMinWidth(200);
        txt_type_name.setMaxWidth(Double.MAX_VALUE);
        HBox type_name = CustomTableViewControls.getLabelledNode("Type name", 200, txt_type_name);

        final TextField txt_type_description = new TextField();
        txt_type_description.setMinWidth(200);
        txt_type_description.setMaxWidth(Double.MAX_VALUE);
        HBox type_description = CustomTableViewControls.getLabelledNode("Resource type description", 200, txt_type_description);

        final TextField txt_other = new TextField();
        txt_other.setMinWidth(200);
        txt_other.setMaxWidth(Double.MAX_VALUE);
        HBox other = CustomTableViewControls.getLabelledNode("Other", 200, txt_other);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Submit", event ->
        {
            if(!Validators.isValidNode(txt_type_name, txt_type_name.getText(), 1, "\\w+"))
            {
                JOptionPane.showMessageDialog(null, "Please make sure that the resource type name doesn't have any spaces.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String str_type_name = txt_type_name.getText();
            String str_type_description = txt_type_description.getText();
            String str_type_other = txt_other.getText();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("type_name", str_type_name));
            params.add(new AbstractMap.SimpleEntry<>("type_description", str_type_description));
            params.add(new AbstractMap.SimpleEntry<>("other", String.valueOf(str_type_other)));

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                if(SessionManager.getInstance().getActive()!=null)
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
                else
                {
                    JOptionPane.showMessageDialog(null, "No active sessions.", "Session expired", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                HttpURLConnection connection = RemoteComms.postData("/api/resource/type/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        JOptionPane.showMessageDialog(null, "Successfully added new resource type!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }else{
                        JOptionPane.showMessageDialog(null, connection.getResponseCode(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (IOException e)
            {
                IO.log(TAG, IO.TAG_ERROR, e.getMessage());
            }
        });

        //Add form controls vertically on the scene
        vbox.getChildren().add(type_name);
        vbox.getChildren().add(type_description);
        vbox.getChildren().add(other);
        vbox.getChildren().add(submit);

        //Setup scene and display stage
        Scene scene = new Scene(vbox);
        File fCss = new File("src/fadulousbms/styles/home.css");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

        stage.onHidingProperty().addListener((observable, oldValue, newValue) ->
                loadDataFromServer());

        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
        stage.setResizable(true);
    }
}
