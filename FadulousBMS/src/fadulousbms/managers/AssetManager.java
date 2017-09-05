package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Validators;
import fadulousbms.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

/**
 * Created by ghost on 2017/02/01.
 */
public class AssetManager implements BusinessObjectManager
{
    private Asset[] assets;
    private TableView tblAssets;
    private Gson gson;
    private static AssetManager asset_manager = new AssetManager();
    public static AssetType[] asset_types;
    public static final String TAG = "AssetManager";

    private AssetManager()
    {
    }

    public static AssetManager getInstance()
    {
        return asset_manager;
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

                    String assets_json = RemoteComms.sendGetRequest("/api/assets", headers);
                    assets = gson.fromJson(assets_json, Asset[].class);

                    String asset_types_json = RemoteComms.sendGetRequest("/api/asset/types", headers);
                    asset_types = gson.fromJson(asset_types_json, AssetType[].class);
                } else
                {
                    JOptionPane.showMessageDialog(null, "Active session has expired.", "Session Expired", JOptionPane.ERROR_MESSAGE);
                }
            } else
            {
                JOptionPane.showMessageDialog(null, "No active sessions.", "Session Expired", JOptionPane.ERROR_MESSAGE);
            }
        }catch (JsonSyntaxException ex)
        {
            IO.log(TAG, IO.TAG_ERROR, ex.getMessage());
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }catch (MalformedURLException ex)
        {
            IO.log(TAG, IO.TAG_ERROR, ex.getMessage());
        }catch (IOException ex)
        {
            IO.log(TAG, IO.TAG_ERROR, ex.getMessage());
        }
    }

    @Override
    public void newWindow()
    {
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                Stage stage = new Stage();
                stage.setTitle(Globals.APP_NAME.getValue() + " - Assets");
                stage.setMinWidth(320);
                stage.setMinHeight(340);
                //stage.setAlwaysOnTop(true);

                tblAssets = new TableView();
                tblAssets.setEditable(true);

                TableColumn<BusinessObject, String> asset_id = new TableColumn<>("Asset ID");
                asset_id.setMinWidth(80);
                asset_id.setCellValueFactory(new PropertyValueFactory<>("short_id"));

                TableColumn<BusinessObject, String> asset_name = new TableColumn("Asset name");
                CustomTableViewControls.makeEditableTableColumn(asset_name, TextFieldTableCell.forTableColumn(), 80, "asset_name", "/api/asset");

                TableColumn<BusinessObject, String> asset_description = new TableColumn("Asset description");
                CustomTableViewControls.makeEditableTableColumn(asset_description, TextFieldTableCell.forTableColumn(), 80, "asset_description", "/api/asset");

                TableColumn<BusinessObject, String> asset_type = new TableColumn("Asset type");
                CustomTableViewControls.makeComboBoxTableColumn(asset_type, asset_types, "asset_type", "type_name", "/api/asset", 100);

                TableColumn<BusinessObject, String> asset_value = new TableColumn("Asset value");
                CustomTableViewControls.makeEditableTableColumn(asset_value, TextFieldTableCell.forTableColumn(), 80, "asset_value", "/api/asset");

                TableColumn<BusinessObject, Long> date_acquired = new TableColumn("Date acquired");
                CustomTableViewControls.makeDatePickerTableColumn(date_acquired, "date_acquired", "/api/asset");

                TableColumn<BusinessObject, Long> date_exhausted = new TableColumn("Date exhausted");
                CustomTableViewControls.makeDatePickerTableColumn(date_exhausted, "date_exhausted", "/api/asset");

                TableColumn<BusinessObject, String> other = new TableColumn("Other");
                CustomTableViewControls.makeEditableTableColumn(other, TextFieldTableCell.forTableColumn(), 80, "other", "/api/asset");

                ObservableList<Asset> lst_assets = FXCollections.observableArrayList();
                lst_assets.addAll(assets);

                tblAssets.setItems(lst_assets);
                tblAssets.getColumns().addAll(asset_id, asset_name, asset_description, asset_type, asset_value,
                        date_acquired, date_exhausted, other);

                MenuBar menu_bar = new MenuBar();
                Menu file = new Menu("File");
                Menu edit = new Menu("Edit");

                MenuItem new_resource = new MenuItem("New asset");
                MenuItem new_resource_type = new MenuItem("New asset type");
                new_resource.setOnAction(event -> handleNewAsset(stage));
                new_resource_type.setOnAction(event -> handleNewAssetType(stage));
                MenuItem save = new MenuItem("Save");
                MenuItem print = new MenuItem("Print");

                file.getItems().addAll(new_resource, new_resource_type, save, print);

                menu_bar.getMenus().addAll(file, edit);

                BorderPane border_pane = new BorderPane();
                border_pane.setTop(menu_bar);
                border_pane.setCenter(tblAssets);

                stage.onHidingProperty().addListener((observable, oldValue, newValue) ->
                        loadDataFromServer());

                Scene scene = new Scene(border_pane);
                stage.setScene(scene);
                stage.show();
                stage.centerOnScreen();
                stage.setResizable(true);
            }else{
                JOptionPane.showMessageDialog(null, "Active session has expired.", "Session Expired", JOptionPane.ERROR_MESSAGE);
            }
        }else{
            JOptionPane.showMessageDialog(null, "No active sessions.", "Session Expired", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void handleNewAsset(Stage parentStage)
    {
        parentStage.setAlwaysOnTop(false);
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Add New Asset");
        stage.setMinWidth(320);
        stage.setMinHeight(350);
        //stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(10);

        final TextField txt_asset_name = new TextField();
        txt_asset_name.setMinWidth(200);
        txt_asset_name.setMaxWidth(Double.MAX_VALUE);
        HBox asset_name = CustomTableViewControls.getLabelledNode("Asset name", 200, txt_asset_name);

        final TextField txt_asset_description = new TextField();
        txt_asset_description.setMinWidth(200);
        txt_asset_description.setMaxWidth(Double.MAX_VALUE);
        HBox asset_description = CustomTableViewControls.getLabelledNode("Asset description", 200, txt_asset_description);

        final ComboBox<AssetType> cbx_asset_type = new ComboBox<>();
        cbx_asset_type.setCellFactory(new Callback<ListView<AssetType>, ListCell<AssetType>>()
        {
            @Override
            public ListCell<AssetType> call(ListView<AssetType> lst_asset_types)
            {
                return new ListCell<AssetType>()
                {
                    @Override
                    protected void updateItem(AssetType asset_type, boolean empty)
                    {
                        super.updateItem(asset_type, empty);
                        if(asset_type!=null && !empty)
                        {
                            setText(asset_type.getType_name());
                        }else{
                            setText("");
                        }
                    }
                };
            }
        });
        cbx_asset_type.setButtonCell(new ListCell<AssetType>()
        {
            @Override
            protected void updateItem(AssetType asset_type, boolean empty)
            {
                super.updateItem(asset_type, empty);
                if(asset_type!=null && !empty)
                {
                    setText(asset_type.getType_name());
                }else{
                    setText("");
                }
            }
        });
        cbx_asset_type.setItems(FXCollections.observableArrayList(asset_types));
        cbx_asset_type.setMinWidth(200);
        cbx_asset_type.setMaxWidth(Double.MAX_VALUE);
        HBox asset_type = CustomTableViewControls.getLabelledNode("Asset type", 200, cbx_asset_type);

        final TextField txt_asset_value = new TextField();
        txt_asset_value.setMinWidth(200);
        txt_asset_value.setMaxWidth(Double.MAX_VALUE);
        HBox asset_value = CustomTableViewControls.getLabelledNode("Value", 200, txt_asset_value);

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

            if(!Validators.isValidNode(txt_asset_name, txt_asset_name.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_asset_description, txt_asset_description.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(cbx_asset_type, cbx_asset_type.getValue()==null?"":cbx_asset_type.getValue().get_id(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_asset_value, txt_asset_value.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(dpk_date_acquired, dpk_date_acquired.getValue()==null?"":dpk_date_acquired.getValue().toString(), 4, date_regex))
                return;
            /*if(!Validators.isValidNode(dpk_date_exhausted, dpk_date_exhausted.getValue()==null?"":dpk_date_exhausted.getValue().toString(), 4, date_regex))
                return;
            if(!Validators.isValidNode(txt_other, txt_other.getText(), 1, ".+"))
                return;*/

            long date_acquired_in_sec, date_exhausted_in_sec=0;
            String str_asset_name = txt_asset_name.getText();
            String str_asset_description = txt_asset_description.getText();
            String str_asset_type = cbx_asset_type.getValue().get_id();
            String str_asset_value = txt_asset_value.getText();
            date_acquired_in_sec = dpk_date_acquired.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            if(dpk_date_exhausted.getValue()!=null)
                date_exhausted_in_sec = dpk_date_exhausted.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            String str_other = txt_other.getText();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("asset_name", str_asset_name));
            params.add(new AbstractMap.SimpleEntry<>("asset_description", str_asset_description));
            params.add(new AbstractMap.SimpleEntry<>("asset_type", str_asset_type));
            params.add(new AbstractMap.SimpleEntry<>("asset_value", str_asset_value));
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
                    JOptionPane.showMessageDialog(null, "No active sessions.", "Session expired", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                HttpURLConnection connection = RemoteComms.postData("/api/asset/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        JOptionPane.showMessageDialog(null, "Successfully added new asset!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
        vbox.getChildren().add(asset_name);
        vbox.getChildren().add(asset_description);
        vbox.getChildren().add(asset_type);
        vbox.getChildren().add(asset_value);
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

    public void handleNewAssetType(Stage parentStage)
    {
        parentStage.setAlwaysOnTop(false);
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Add New Asset Type");
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
        HBox type_description = CustomTableViewControls.getLabelledNode("Asset type description", 200, txt_type_description);

        final TextField txt_other = new TextField();
        txt_other.setMinWidth(200);
        txt_other.setMaxWidth(Double.MAX_VALUE);
        HBox other = CustomTableViewControls.getLabelledNode("Other", 200, txt_other);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Submit", event ->
        {
            if(!Validators.isValidNode(txt_type_name, txt_type_name.getText(), 1, "\\w+"))
            {
                JOptionPane.showMessageDialog(null, "Please make sure that the asset type name doesn't have any spaces.", "Error", JOptionPane.ERROR_MESSAGE);
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

                HttpURLConnection connection = RemoteComms.postData("/api/asset/type/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        JOptionPane.showMessageDialog(null, "Successfully added new asset type!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
