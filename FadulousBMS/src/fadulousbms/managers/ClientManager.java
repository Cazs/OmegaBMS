package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.Validators;
import fadulousbms.controllers.HomescreenController;
import fadulousbms.controllers.OperationsController;
import fadulousbms.model.CustomTableViewControls;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.model.BusinessObject;
import fadulousbms.model.Client;
import fadulousbms.model.Job;
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
 * Created by ghost on 2017/01/11.
 */
public class ClientManager implements BusinessObjectManager
{
    private Client[] clients;
    private TableView tblClients;
    private Gson gson;
    private static ClientManager clientManager = new ClientManager();
    public static final String TAG = "ClientManager";

    private ClientManager()
    {
    }

    public static ClientManager getInstance()
    {
        return clientManager;
    }

    public Client[] getClients(){return clients;}

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
            if(smgr.getActive()!=null)
            {
                if(!smgr.getActive().isExpired())
                {
                    gson  = new GsonBuilder().create();
                    ArrayList<AbstractMap.SimpleEntry<String,String>> headers = new ArrayList<>();
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));

                    String clients_json = RemoteComms.sendGetRequest("/api/clients", headers);
                    clients = gson.fromJson(clients_json, Client[].class);
                }else{
                    JOptionPane.showMessageDialog(null, "Active session has expired.", "Session Expired", JOptionPane.ERROR_MESSAGE);
                }
            }else{
                JOptionPane.showMessageDialog(null, "No active sessions.", "Session Expired", JOptionPane.ERROR_MESSAGE);
            }
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
                stage.setTitle(Globals.APP_NAME.getValue() + " - Clients");
                stage.setMinWidth(320);
                stage.setMinHeight(340);
                //stage.setAlwaysOnTop(true);

                tblClients = new TableView();
                tblClients.setEditable(true);

                TableColumn<Job, String> client_id = new TableColumn<>("Client ID");
                client_id.setMinWidth(100);
                client_id.setCellValueFactory(new PropertyValueFactory<>("short_id"));

                TableColumn<BusinessObject, String> client_name = new TableColumn("Client name");
                CustomTableViewControls.makeEditableTableColumn(client_name, TextFieldTableCell.forTableColumn(), 215, "client_name", "/api/client");
                /*job_description.setMinWidth(130);
                job_description.setCellValueFactory(new PropertyValueFactory<>("job_description"));*/


                TableColumn<BusinessObject, String> client_physical_address = new TableColumn("Physical address");
                CustomTableViewControls.makeEditableTableColumn(client_physical_address, TextFieldTableCell.forTableColumn(), 215, "physical_address", "/api/client");
                //client_name.setMinWidth(100);
                //client_name.setCellValueFactory(new PropertyValueFactory<>("client_id"));

                TableColumn<BusinessObject, String> client_postal_address = new TableColumn("Postal Address");
                CustomTableViewControls.makeEditableTableColumn(client_postal_address, TextFieldTableCell.forTableColumn(), 100, "postal_address", "/api/client");

                TableColumn<BusinessObject, String> client_tel = new TableColumn("Tel Number");
                CustomTableViewControls.makeEditableTableColumn(client_tel, TextFieldTableCell.forTableColumn(), 100, "tel", "/api/client");

                TableColumn<BusinessObject, String> client_fax = new TableColumn("Fax Number");
                CustomTableViewControls.makeEditableTableColumn(client_fax, TextFieldTableCell.forTableColumn(), 100, "fax", "/api/client");

                TableColumn client_active = new TableColumn("Active");
                //supplier_active.setCellFactory(CheckBoxTableCell.forTableColumn(supplier_active));
                CustomTableViewControls.makeCheckboxedTableColumn(client_active, CheckBoxTableCell.forTableColumn(client_active), 60, "active", "/api/client");

                TableColumn<BusinessObject, Long> client_date_partnered = new TableColumn("Date partnered");
                CustomTableViewControls.makeDatePickerTableColumn(client_date_partnered, "date_partnered", "/api/client");

                TableColumn<BusinessObject, String> client_website = new TableColumn("Website");
                CustomTableViewControls.makeEditableTableColumn(client_website, TextFieldTableCell.forTableColumn(), 100, "website", "/api/client");

                TableColumn<BusinessObject, String> client_other = new TableColumn("Other");
                CustomTableViewControls.makeEditableTableColumn(client_other, TextFieldTableCell.forTableColumn(), 100, "other", "/api/client");


                ObservableList<Client> lst_clients = FXCollections.observableArrayList();
                lst_clients.addAll(clients);

                tblClients.setItems(lst_clients);
                tblClients.getColumns().addAll(client_id, client_name, client_physical_address,
                        client_postal_address, client_tel, client_fax, client_active,
                        client_date_partnered, client_website, client_other);

                MenuBar menu_bar = new MenuBar();
                Menu file = new Menu("File");
                Menu edit = new Menu("Edit");

                MenuItem new_client = new MenuItem("New client");
                new_client.setOnAction(event -> handleNewClient(stage));
                MenuItem save = new MenuItem("Save");
                MenuItem print = new MenuItem("Print");

                file.getItems().addAll(new_client, save, print);

                menu_bar.getMenus().addAll(file, edit);

                BorderPane border_pane = new BorderPane();
                border_pane.setTop(menu_bar);
                border_pane.setCenter(tblClients);

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

    public void handleNewClient(Stage parentStage)
    {
        parentStage.setAlwaysOnTop(false);
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Add New Client");
        stage.setMinWidth(320);
        stage.setMinHeight(350);
        //stage.setHeight(700);
        //stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(1);

        final TextField txt_client_name = new TextField();
        txt_client_name.setMinWidth(200);
        txt_client_name.setMaxWidth(Double.MAX_VALUE);
        HBox client_name = CustomTableViewControls.getLabelledNode("Client name", 200, txt_client_name);

        final TextArea txt_physical_address = new TextArea();
        txt_physical_address.setMinWidth(200);
        txt_physical_address.setMaxWidth(Double.MAX_VALUE);
        HBox physical_address = CustomTableViewControls.getLabelledNode("Client physical address", 200, txt_physical_address);

        final TextArea txt_postal_address = new TextArea();
        txt_postal_address.setMinWidth(200);
        txt_postal_address.setMaxWidth(Double.MAX_VALUE);
        HBox postal_address = CustomTableViewControls.getLabelledNode("Client postal address", 200, txt_postal_address);

        final TextField txt_tel = new TextField();
        txt_tel.setMinWidth(200);
        txt_tel.setMaxWidth(Double.MAX_VALUE);
        HBox tel = CustomTableViewControls.getLabelledNode("Client tel number", 200, txt_tel);

        final CheckBox chbx_active = new CheckBox();
        chbx_active.setMinWidth(200);
        chbx_active.setMaxWidth(Double.MAX_VALUE);
        HBox active = CustomTableViewControls.getLabelledNode("Active", 200, chbx_active);

        final DatePicker dpk_date_partnered = new DatePicker();
        dpk_date_partnered.setMinWidth(200);
        dpk_date_partnered.setMaxWidth(Double.MAX_VALUE);
        HBox date_partnered = CustomTableViewControls.getLabelledNode("Date partnered", 200, dpk_date_partnered);

        final TextField txt_website = new TextField();
        txt_website.setMinWidth(200);
        txt_website.setMaxWidth(Double.MAX_VALUE);
        HBox website = CustomTableViewControls.getLabelledNode("Client website", 200, txt_website);

        final TextArea txt_other = new TextArea();
        txt_other.setMinWidth(200);
        txt_other.setMaxWidth(Double.MAX_VALUE);
        HBox other = CustomTableViewControls.getLabelledNode("Other", 200, txt_other);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Submit", event ->
        {
            String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            if(!Validators.isValidNode(txt_client_name, txt_client_name.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_physical_address, txt_physical_address.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_postal_address, txt_postal_address.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_tel, txt_tel.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_website, txt_website.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(dpk_date_partnered, dpk_date_partnered.getValue()==null?"":dpk_date_partnered.getValue().toString(), 4, date_regex))
                return;
            if(!Validators.isValidNode(txt_website, txt_website.getText(), 1, ".+"))
                return;

            String str_client_name = txt_client_name.getText();
            String str_physical_address = txt_physical_address.getText();
            String str_postal_address = txt_postal_address.getText();
            String str_tel = txt_tel.getText();
            String str_website = txt_website.getText();
            long date_partnered_in_sec = dpk_date_partnered.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            String str_other = txt_other.getText();
            boolean is_active = chbx_active.selectedProperty().get();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("client_name", str_client_name));
            params.add(new AbstractMap.SimpleEntry<>("physical_address", str_physical_address));
            params.add(new AbstractMap.SimpleEntry<>("postal_address", str_postal_address));
            params.add(new AbstractMap.SimpleEntry<>("tel", str_tel));
            params.add(new AbstractMap.SimpleEntry<>("website", str_website));
            params.add(new AbstractMap.SimpleEntry<>("date_partnered", String.valueOf(date_partnered_in_sec)));
            params.add(new AbstractMap.SimpleEntry<>("other", str_other));
            params.add(new AbstractMap.SimpleEntry<>("active", String.valueOf(is_active)));

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

                HttpURLConnection connection = RemoteComms.postData("/api/client/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        JOptionPane.showMessageDialog(null, "Successfully added new client!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }else
                    {
                        JOptionPane.showMessageDialog(null, connection.getResponseCode(), "Error", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (IOException e)
            {
                IO.log(TAG, IO.TAG_ERROR, e.getMessage());
            }
        });

        //populate clients combobox

        //Add form controls vertically on the stage
        vbox.getChildren().add(client_name);
        vbox.getChildren().add(physical_address);
        vbox.getChildren().add(postal_address);
        vbox.getChildren().add(tel);
        vbox.getChildren().add(website);
        vbox.getChildren().add(date_partnered);
        vbox.getChildren().add(other);
        vbox.getChildren().add(active);
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
