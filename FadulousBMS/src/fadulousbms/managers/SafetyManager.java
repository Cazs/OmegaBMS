package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import fadulousbms.auxilary.*;
import fadulousbms.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.print.PrintException;
import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ghost on 2017/02/24.
 */
public class SafetyManager extends BusinessObjectManager
{
    private TableView tblSafety;
    private static FileMetadata[] documents;
    private static SafetyManager safety_manager = new SafetyManager();
    public static final String TAG = "SafetyManager";

    public static SafetyManager getInstance()
    {
        return safety_manager;
    }

    @Override
    public void initialize()
    {
        loadDataFromServer();
    }

    private static void loadDataFromServer()
    {
        try
        {
            //Validate session - also done on server-side don't worry ;)
            SessionManager smgr = SessionManager.getInstance();
            if (smgr.getActive() != null)
            {
                if (!smgr.getActive().isExpired())
                {
                    //Create & init Gson builder object
                    Gson gson = new GsonBuilder().create();
                    //Prepare headers
                    ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));

                    //Request index
                    String resources_json = RemoteComms.sendGetRequest("/api/safety/indices", headers);
                    //Convert to local model - FileMetadata[]
                    documents = gson.fromJson(resources_json, FileMetadata[].class);

                    //System.out.println("\n\n>>>>>Documents successfully loaded, size: " + documents.length + "<<<<<\n\n");

                    //Sort array in ascending order
                    if(documents!=null)
                        if(documents.length>0)
                            FileMetadata.quickSort(documents, 0, documents.length-1);
                        else IO.log("No documents found", IO.TAG_ERROR, "No safety documents were found in the database.");
                    else IO.log("No documents found", IO.TAG_ERROR, "No safety documents were found in the database.");

                } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
            } else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
        }catch (JsonSyntaxException ex)
        {
            IO.logAndAlert(TAG, ex.getMessage(), IO.TAG_ERROR);
        }catch (MalformedURLException ex)
        {
            IO.logAndAlert(TAG, ex.getMessage(), IO.TAG_ERROR);
        }catch (IOException ex)
        {
            IO.logAndAlert(TAG, ex.getMessage(), IO.TAG_ERROR);
        }
    }

    public FileMetadata[] getDocuments()
    {
        return documents;
    }

    private void swap(int pos, int min_max)
    {
        FileMetadata temp = documents[pos];

        if(min_max==1)//max
        {
            //Shift all to right of pos one position left
            for (int i = pos + 1; i < documents.length; i++)
                if (i - 1 >= 0)
                    documents[i - 1] = documents[i];

            documents[documents.length - 1] = temp;
        }else{//min
            //Shift all to left of pos one position right
            for (int i = pos - 1; i >= 0; i--)
                //if (i + 1 < documents.length)
                    documents[i + 1] = documents[i];

            documents[0] = temp;
        }
    }

    public void newWindow()
    {
        listSafetyDocuments(documents, null);
    }

    public static void listSafetyDocuments(FileMetadata[] docs, String job_id)
    {
        //Validate session - also done on server-side don't worry ;)
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                Stage stage = new Stage();
                stage.setTitle(Globals.APP_NAME.getValue() + " - Safety Documents");
                stage.setMinWidth(530);
                stage.setMinHeight(340);
                //stage.setAlwaysOnTop(true);

                TableView tblSafety = new TableView();
                tblSafety.setEditable(true);

                //Some Safety columns
                TableColumn<BusinessObject, String> index = new TableColumn("Index");
                CustomTableViewControls.makeEditableTableColumn(index, TextFieldTableCell.forTableColumn(), 80, "index", "/api/safety/index");

                TableColumn<BusinessObject, String> label = new TableColumn("Label");
                CustomTableViewControls.makeEditableTableColumn(label, TextFieldTableCell.forTableColumn(), 250, "label", "/api/safety/index");

                TableColumn<BusinessObject, String> document = new TableColumn("Document Path");
                CustomTableViewControls.makeEditableTableColumn(document, TextFieldTableCell.forTableColumn(), 250, "pdf_path", "/api/safety/index");

                TableColumn<BusinessObject, HBox> action = new TableColumn("Quick Action");
                CustomTableViewControls.makeActionTableColumn(action, 270, "pdf_path", "/api/safety/index");

                TableColumn<BusinessObject, GridPane> required = new TableColumn("Required?");
                CustomTableViewControls.makeToggleButtonTableColumn(required, null,100, "required", "/api/safety/index");

                TableColumn<BusinessObject, String> logo_options = new TableColumn("Logo Options");
                CustomTableViewControls.makeEditableTableColumn(logo_options, TextFieldTableCell.forTableColumn(), 250, "logo_options", "/api/safety/index");

                //TableColumn<BusinessObject, String> type = new TableColumn("Type");
                //CustomTableViewControls.makeEditableTableColumn(type, TextFieldTableCellOld.forTableColumn(), 250, "type", "/api/safety/index");

                TableColumn<BusinessObject, GridPane> type = new TableColumn("Type");
                CustomTableViewControls.makeToggleButtonTypeTableColumn(type, null,80, "type", "/api/safety/index");

                TableColumn<BusinessObject, GridPane> mark = new TableColumn("Select");
                CustomTableViewControls.makeCheckboxedTableColumn(mark, null,100, "marked", "/api/safety/index");

                ObservableList<FileMetadata> lst_safety = FXCollections.observableArrayList();
                lst_safety.addAll(docs);

                tblSafety.setItems(lst_safety);
                tblSafety.getColumns().addAll(index, label, document, action, required, logo_options, type, mark);

                //Menu bar
                MenuBar menu_bar = new MenuBar();
                Menu file = new Menu("File");
                Menu print = new Menu("Print");
                Menu view = new Menu("View");
                Menu options = new Menu("Options");

                //Menu - Options
                MenuItem add_job_cat = new MenuItem("Add to job catalogue");
                add_job_cat.setOnAction(event -> addToJobCatalogue(stage, tblSafety));

                options.getItems().add(add_job_cat);

                //Menu - File
                MenuItem new_item = new MenuItem("New safety document reference");
                new_item.setOnAction(event -> newSafetyDocumentReference(stage));
                MenuItem upload = new MenuItem("Upload");
                upload.setOnAction(event->
                {
                    try
                    {
                        FileChooser fileChooser = new FileChooser();
                        File f = fileChooser.showOpenDialog(stage);
                        if(f!=null)
                        {
                            if(f.exists())
                            {
                                FileInputStream in = new FileInputStream(f);
                                byte[] buffer = new byte[(int) f.length()];
                                in.read(buffer, 0, buffer.length);
                                in.close();

                                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                                headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));
                                headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/pdf"));
                                headers.add(new AbstractMap.SimpleEntry<>("Filename", f.getName()));
                                RemoteComms.uploadFile("/api/upload", headers, buffer);
                                IO.log(TAG, "File size: " + buffer.length + " bytes.", IO.TAG_INFO);
                            }else{
                                IO.logAndAlert(TAG, "File not found.", IO.TAG_ERROR);
                            }
                        }else{
                            IO.log(TAG, "File object is null.", IO.TAG_ERROR);
                        }
                    } catch (FileNotFoundException e)
                    {
                        IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                    } catch (IOException e)
                    {
                        IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                    }
                });
                MenuItem scan = new MenuItem("Scan");
                scan.setOnAction(event->
                {
                    try
                    {
                        Sane.scan();
                    } catch (IOException e)
                    {
                        IO.logAndAlert(Sane.class.getName(), e.getMessage(), IO.TAG_ERROR);
                    }
                });

                MenuItem exit = new MenuItem("Close Window");
                exit.setOnAction(event -> stage.close());

                //Menu - View
                MenuItem generate_index = new MenuItem("Generate/View Index Page");
                generate_index.setOnAction(event -> IO.viewIndexPage("Safety Documents Index", docs, "bin/safety_index.pdf"));

                //Menu - Print
                MenuItem print_index = new MenuItem("Print Index Page");
                print_index.setOnAction(event -> IO.printIndexPage("bin/safety_index.pdf"));

                MenuItem print_selected = new MenuItem("Print Marked Documents");
                print_selected.setOnAction(event -> IO.printSelectedDocuments(docs));

                MenuItem print_all = new MenuItem("Print All Documents");
                print_all.setOnAction(event -> IO.printAllDocuments(docs));

                file.getItems().addAll(new_item, scan, upload, exit);
                view.getItems().add(generate_index);
                print.getItems().addAll(print_index, print_selected, print_all);

                menu_bar.getMenus().addAll(file, view, print, options);

                BorderPane border_pane = new BorderPane();
                border_pane.setTop(menu_bar);
                border_pane.setCenter(tblSafety);

                stage.onHidingProperty().addListener((observable, oldValue, newValue) ->
                        loadDataFromServer());

                Scene scene = new Scene(border_pane);
                stage.setScene(scene);
                stage.show();
                stage.centerOnScreen();
                stage.setResizable(true);
            }else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public static void addToJobCatalogue(Stage parentStage, TableView tblSafety)
    {
        int selected = tblSafety.getSelectionModel().selectedIndexProperty().get();
        if(selected<0 || selected>documents.length)
        {
            IO.log(TAG, IO.TAG_ERROR, "addToJobCatalogue> selected safety document index out of bounds.");
            return;
        }
        FileMetadata selected_doc = documents[selected];

        parentStage.setAlwaysOnTop(false);
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Add safety doc to job catalogue");
        stage.setMinWidth(320);
        stage.setMinHeight(200);
        //stage.setAlwaysOnTop(true);

        JobManager jobManager = JobManager.getInstance();
        jobManager.loadDataFromServer();
        Collection<Job> jobs_arr = jobManager.getJobs().values();
        Job[] jobs = new Job[jobs_arr.size()];
        jobs_arr.toArray(jobs);


        VBox vbox = new VBox(10);

        final TextField txt_safety_doc = new TextField();
        txt_safety_doc.setMinWidth(200);
        txt_safety_doc.setMaxWidth(Double.MAX_VALUE);
        txt_safety_doc.setText(selected_doc.getIndex() + ":" + selected_doc.getLabel());
        txt_safety_doc.setEditable(false);
        HBox safety_doc = CustomTableViewControls.getLabelledNode("Safety document: ", 200, txt_safety_doc);

        final ComboBox<Job> cbx_job_number = new ComboBox<>();
        cbx_job_number.setCellFactory(new Callback<ListView<Job>, ListCell<Job>>()
        {
            @Override
            public ListCell<Job> call(ListView<Job> lst_jobs)
            {
                return new ListCell<Job>()
                {
                    @Override
                    protected void updateItem(Job job, boolean empty)
                    {
                        super.updateItem(job, empty);
                        if(job!=null && !empty)
                        {
                            setText(job.get_id());
                        }else{
                            setText("");
                        }
                    }
                };
            }
        });
        cbx_job_number.setButtonCell(new ListCell<Job>()
        {
            @Override
            protected void updateItem(Job job, boolean empty)
            {
                super.updateItem(job, empty);
                if(job!=null && !empty)
                {
                    setText(job.get_id());
                }else{
                    setText("");
                }
            }
        });
        cbx_job_number.setItems(FXCollections.observableArrayList(jobs));
        cbx_job_number.setMinWidth(200);
        cbx_job_number.setMaxWidth(Double.MAX_VALUE);
        HBox job = CustomTableViewControls.getLabelledNode("Job Number", 200, cbx_job_number);


        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Submit", event ->
        {
            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("safety_id", selected_doc.get_id()));
            params.add(new AbstractMap.SimpleEntry<>("job_id", cbx_job_number.getSelectionModel().getSelectedItem().get_id()));

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

                HttpURLConnection connection = RemoteComms.postData("/api/job/safetycatalogue/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        JOptionPane.showMessageDialog(null, "Successfully added safety document to job catalogue!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }else{
                        String msg = IO.readStream(connection.getErrorStream());
                        JOptionPane.showMessageDialog(null, msg, "Error " + connection.getResponseCode(), JOptionPane.ERROR_MESSAGE);
                    }
                    connection.disconnect();
                }
            } catch (IOException e)
            {
                IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
            }
        });

        //Add form controls vertically on the scene
        vbox.getChildren().add(safety_doc);
        vbox.getChildren().add(job);
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

    public static void newSafetyDocumentReference(Stage parentStage)
    {
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Create New Safety Document Reference");
        stage.setMinWidth(380);
        stage.setMinHeight(200);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.centerOnScreen();

        VBox vbox = new VBox(10);

        final TextField txt_index = new TextField();
        txt_index.setMinWidth(200);
        txt_index.setMaxWidth(Double.MAX_VALUE);
        HBox index = CustomTableViewControls.getLabelledNode("Index", 200, txt_index);

        final TextField txt_label = new TextField();
        txt_label.setMinWidth(200);
        txt_label.setMaxWidth(Double.MAX_VALUE);
        HBox label = CustomTableViewControls.getLabelledNode("Label", 200, txt_label);

        final TextField txt_path = new TextField();
        txt_path.setMinWidth(200);
        txt_path.setMaxWidth(Double.MAX_VALUE);
        HBox path = CustomTableViewControls.getLabelledNode("Document path", 200, txt_path);
        //TODO: File picker

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Submit", event ->
        {
            if(!Validators.isValidNode(txt_index, txt_index.getText(), 1, "(\\d+|\\d+\\.\\d+)"))
            {
                IO.logAndAlert("Error", "Please make sure that the index number is valid.", IO.TAG_ERROR);
                return;
            }

            if(!Validators.isValidNode(txt_label, txt_label.getText(), 1, ".+"))
            {
                IO.logAndAlert("Error", "Please make sure that the label field is not empty.", IO.TAG_ERROR);
                return;
            }

            if(!Validators.isValidNode(txt_path, txt_path.getText(), 1, ".+"))
            {
                IO.logAndAlert("Error", "Please make sure that the file path field is not empty.", IO.TAG_ERROR);
                return;
            }


            String str_index = txt_index.getText();
            String str_label = txt_label.getText();
            String str_path = txt_path.getText();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("index", str_index));
            params.add(new AbstractMap.SimpleEntry<>("label", str_label));
            params.add(new AbstractMap.SimpleEntry<>("pdf_path", String.valueOf(str_path)));

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                if(SessionManager.getInstance().getActive()!=null)
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
                else
                {
                    IO.logAndAlert("Session expired", "No active sessions.", IO.TAG_ERROR);
                    return;
                }

                HttpURLConnection connection = RemoteComms.postData("/api/safety/index/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Success", "Successfully added new safety document reference!", IO.TAG_INFO);
                    }else{
                        String msg = IO.readStream(connection.getErrorStream());
                        IO.logAndAlert("Error " + connection.getResponseCode(), msg, IO.TAG_ERROR);
                    }
                    connection.disconnect();
                }
            } catch (IOException e)
            {
                IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
            }
        });

        //Add form controls vertically on the scene
        vbox.getChildren().add(index);
        vbox.getChildren().add(label);
        vbox.getChildren().add(path);
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
    }
}
