/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Validators;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class NewPurchaseOrderController extends OperationsController implements Initializable
{
    @FXML
    private TableView<PurchaseOrderItem>    tblPurchaseOrderItems;
    @FXML
    private TableColumn     colId,colItemNumber,colName,colDescription,colQuantity,colValue,colDiscount,colUnit,colCreator,colAction;
    @FXML
    private TextField txtVat,txtAccount;
    @FXML
    private ComboBox<Supplier> cbxSuppliers;
    @FXML
    private ComboBox<Employee> cbxContactPerson;

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading new purchase order view..");
        if(SupplierManager.getInstance().getSuppliers()==null)
        {
            IO.logAndAlert(getClass().getName(), "no suppliers found in the database.", IO.TAG_ERROR);
            return;
        }
        if(EmployeeManager.getInstance().getEmployees()==null)
        {
            IO.logAndAlert(getClass().getName(), "no employees found in the database.", IO.TAG_ERROR);
            return;
        }
        Employee[] employees = new Employee[EmployeeManager.getInstance().getEmployees().size()];
        EmployeeManager.getInstance().getEmployees().values().toArray(employees);
        Supplier[] suppliers = new Supplier[SupplierManager.getInstance().getSuppliers().size()];
        SupplierManager.getInstance().getSuppliers().values().toArray(suppliers);

        //setup suppliers combo box
        cbxSuppliers.setItems(FXCollections.observableArrayList(suppliers));

        //setup employees combo box
        cbxContactPerson.setItems(FXCollections.observableArrayList(employees));

        //set up PurchaseOrderItems table
        colId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        colItemNumber.setCellValueFactory(new PropertyValueFactory<>("item_number"));
        colName.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("item_name", "item_name", null));
        colDescription.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("item_description", "item_description", null));
        colValue.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("cost", "cost", null));
        colUnit.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("unit", "unit", null));
        colQuantity.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("quantity", "quantity", null));
        colDiscount.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("discount", "discount", null));

        Callback<TableColumn<PurchaseOrder, String>, TableCell<PurchaseOrder, String>> cellFactory
                =
                new Callback<TableColumn<PurchaseOrder, String>, TableCell<PurchaseOrder, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<PurchaseOrder, String> param)
                    {
                        final TableCell<PurchaseOrder, String> cell = new TableCell<PurchaseOrder, String>()
                        {
                            final Button btnView = new Button("View");
                            final Button btnPDF = new Button("PDF");
                            final Button btnRemove = new Button("Delete");

                            @Override
                            public void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                btnView.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnView.getStyleClass().add("btnApply");
                                btnView.setMinWidth(100);
                                btnView.setMinHeight(35);
                                HBox.setHgrow(btnView, Priority.ALWAYS);

                                btnPDF.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnPDF.getStyleClass().add("btnApply");
                                btnPDF.setMinWidth(100);
                                btnPDF.setMinHeight(35);
                                HBox.setHgrow(btnPDF, Priority.ALWAYS);

                                btnRemove.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnRemove.getStyleClass().add("btnBack");
                                btnRemove.setMinWidth(100);
                                btnRemove.setMinHeight(35);
                                HBox.setHgrow(btnRemove, Priority.ALWAYS);

                                if (empty)
                                {
                                    setGraphic(null);
                                    setText(null);
                                } else
                                {
                                    HBox hBox = new HBox(btnView, btnPDF, btnRemove);

                                    btnView.setOnAction(event ->
                                    {
                                        PurchaseOrder purchaseOrder = getTableView().getItems().get(getIndex());
                                        if(purchaseOrder==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "PurchaseOrder object is not set", IO.TAG_ERROR);
                                            return;
                                        }

                                        /*ScreenManager.getInstance().showLoadingScreen(param ->
                                        {
                                            new Thread(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    PurchaseOrderManager.getInstance().setSelected(purchaseOrder);
                                                    try
                                                    {
                                                        if(ScreenManager.getInstance().loadScreen(Screens.VIEW_PURCHASE_ORDER.getScreen(),getClass().getResource("../views/"+Screens.VIEW_PURCHASE_ORDER.getScreen())))
                                                        {
                                                            Platform.runLater(() -> ScreenManager.getInstance().setScreen(Screens.VIEW_PURCHASE_ORDER.getScreen()));
                                                        }
                                                        else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load purchase order viewer screen.");
                                                    } catch (IOException e)
                                                    {
                                                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                                    }
                                                }
                                            }).start();
                                            return null;
                                        });*/
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        PurchaseOrder purchaseOrder = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(purchaseOrder);
                                        getTableView().refresh();
                                        //TODO: remove from server
                                        IO.log(getClass().getName(), IO.TAG_INFO, "successfully removed purchase order: " + purchaseOrder.get_id());
                                    });

                                    btnPDF.setOnAction(event ->
                                    {
                                        /*PurchaseOrder purchaseOrder = getTableView().getItems().get(getIndex());
                                        try
                                        {
                                            PDF.createPurchaseOrderPdf(purchaseOrder);
                                        } catch (IOException ex)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
                                        }*/
                                    });

                                    hBox.setFillHeight(true);
                                    HBox.setHgrow(hBox, Priority.ALWAYS);
                                    hBox.setSpacing(5);
                                    setGraphic(hBox);
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }
                };
        colAction.setCellValueFactory(new PropertyValueFactory<>(""));
        colAction.setCellFactory(cellFactory);

        //tblPurchaseOrderItems.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
        //        PurchaseOrderManager.getInstance().setSelected(tblPurchaseOrderItems.getSelectionModel().getSelectedItem()));

        //tblPurchaseOrderItems.setItems(FXCollections.observableArrayList(PurchaseOrderManager.getInstance().getPurchaseOrders()));
    }

    @Override
    public void refreshModel()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading purchase order data model..");

        EmployeeManager.getInstance().loadDataFromServer();
        ResourceManager.getInstance().loadDataFromServer();
        AssetManager.getInstance().loadDataFromServer();
        SupplierManager.getInstance().loadDataFromServer();
        PurchaseOrderManager.getInstance().loadDataFromServer();
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
    public void newResourcePurchaseOrderItem()
    {
        if (ResourceManager.getInstance() != null)
        {
            if (ResourceManager.getInstance().getResources() != null)
            {
                if (ResourceManager.getInstance().getResources().size() > 0)
                {
                    ComboBox<Resource> resourceComboBox = new ComboBox<>();
                    resourceComboBox.setMinWidth(120);
                    resourceComboBox.setItems(FXCollections.observableArrayList(ResourceManager.getInstance().getResources().values()));
                    HBox.setHgrow(resourceComboBox, Priority.ALWAYS);

                    Button btnAdd = new Button("Add");
                    btnAdd.setMinWidth(80);
                    btnAdd.setMinHeight(40);
                    btnAdd.setDefaultButton(true);
                    btnAdd.getStyleClass().add("btnAdd");
                    btnAdd.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    Button btnNewMaterial = new Button("New Material");
                    btnNewMaterial.setMinWidth(80);
                    btnNewMaterial.setMinHeight(40);
                    btnNewMaterial.setDefaultButton(true);
                    btnNewMaterial.getStyleClass().add("btnAdd");
                    btnNewMaterial.getStylesheets()
                            .add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    Button btnCancel = new Button("Close");
                    btnCancel.setMinWidth(80);
                    btnCancel.setMinHeight(40);
                    btnCancel.getStyleClass().add("btnBack");
                    btnCancel.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    HBox hBox = new HBox(new Label("Resource: "), resourceComboBox);
                    HBox.setHgrow(hBox, Priority.ALWAYS);
                    hBox.setSpacing(20);

                    HBox hBoxButtons = new HBox(btnAdd, btnNewMaterial, btnCancel);
                    hBoxButtons.setHgrow(btnAdd, Priority.ALWAYS);
                    hBoxButtons.setHgrow(btnCancel, Priority.ALWAYS);
                    hBoxButtons.setSpacing(20);

                    VBox vBox = new VBox(hBox, hBoxButtons);
                    VBox.setVgrow(vBox, Priority.ALWAYS);
                    vBox.setSpacing(20);
                    HBox.setHgrow(vBox, Priority.ALWAYS);
                    vBox.setFillWidth(true);

                    Stage stage = new Stage();
                    stage.setMaxWidth(300);
                    stage.setTitle("Resource Purchase Order");
                    stage.setScene(new Scene(vBox));
                    stage.setAlwaysOnTop(true);
                    stage.show();


                    btnAdd.setOnAction(event ->
                    {
                        if (resourceComboBox.getValue() != null)
                        {
                            PurchaseOrderResource purchaseOrderResource = new PurchaseOrderResource();
                            purchaseOrderResource.setItem_number(tblPurchaseOrderItems.getItems().size());
                            purchaseOrderResource.setItem(resourceComboBox.getValue());
                            purchaseOrderResource.setItem_id(resourceComboBox.getValue().get_id());
                            purchaseOrderResource.setQuantity(1);
                            purchaseOrderResource.setDiscount(0);
                            tblPurchaseOrderItems.getItems().add(purchaseOrderResource);
                            tblPurchaseOrderItems.refresh();

                            //itemsModified = true;

                            //computeQuoteTotal();

                        }
                        else IO.logAndAlert("Purchase Order Item Addition", "Invalid item selected.", IO.TAG_ERROR);
                    });

                    btnNewMaterial.setOnAction(event ->
                            ResourceManager.getInstance().newResourceWindow(param ->
                            {
                                new Thread(() ->
                                {
                                    refreshModel();
                                    Platform.runLater(() -> refreshView());
                                }).start();
                                return null;
                            }));

                    btnCancel.setOnAction(event ->
                            stage.close());
                    return;
                }
            }
        }
        IO.logAndAlert("Add Purchase Order Item", "No resources were found in the database, please add some resources first and try again.", IO.TAG_ERROR);
    }

    @FXML
    public void newAssetPurchaseOrderItem()
    {
        if (AssetManager.getInstance() != null)
        {
            if (AssetManager.getInstance().getAssets() != null)
            {
                if (AssetManager.getInstance().getAssets().size() > 0)
                {
                    ComboBox<Asset> assetComboBox = new ComboBox<>();
                    assetComboBox.setMinWidth(120);
                    assetComboBox.setItems(FXCollections.observableArrayList(AssetManager.getInstance().getAssets().values()));
                    HBox.setHgrow(assetComboBox, Priority.ALWAYS);

                    Button btnAdd = new Button("Add");
                    btnAdd.setMinWidth(80);
                    btnAdd.setMinHeight(40);
                    btnAdd.setDefaultButton(true);
                    btnAdd.getStyleClass().add("btnAdd");
                    btnAdd.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    Button btnNew = new Button("New Asset");
                    btnNew.setMinWidth(80);
                    btnNew.setMinHeight(40);
                    btnNew.setDefaultButton(true);
                    btnNew.getStyleClass().add("btnAdd");
                    btnNew.getStylesheets()
                            .add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    Button btnCancel = new Button("Close");
                    btnCancel.setMinWidth(80);
                    btnCancel.setMinHeight(40);
                    btnCancel.getStyleClass().add("btnBack");
                    btnCancel.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    HBox hBox = new HBox(new Label("Asset: "), assetComboBox);
                    HBox.setHgrow(hBox, Priority.ALWAYS);
                    hBox.setSpacing(20);

                    HBox hBoxButtons = new HBox(btnAdd, btnNew, btnCancel);
                    hBoxButtons.setHgrow(btnAdd, Priority.ALWAYS);
                    hBoxButtons.setHgrow(btnCancel, Priority.ALWAYS);
                    hBoxButtons.setSpacing(20);

                    VBox vBox = new VBox(hBox, hBoxButtons);
                    VBox.setVgrow(vBox, Priority.ALWAYS);
                    vBox.setSpacing(20);
                    HBox.setHgrow(vBox, Priority.ALWAYS);
                    vBox.setFillWidth(true);

                    Stage stage = new Stage();
                    stage.setMaxWidth(300);
                    stage.setTitle("Asset Purchase Order");
                    stage.setScene(new Scene(vBox));
                    stage.setAlwaysOnTop(true);
                    stage.show();


                    btnAdd.setOnAction(event ->
                    {
                        if (assetComboBox.getValue() != null)
                        {
                            PurchaseOrderAsset purchaseOrderAsset = new PurchaseOrderAsset();
                            purchaseOrderAsset.setItem_number(tblPurchaseOrderItems.getItems().size());
                            purchaseOrderAsset.setItem(assetComboBox.getValue());
                            purchaseOrderAsset.setItem_id(assetComboBox.getValue().get_id());
                            purchaseOrderAsset.setQuantity(1);
                            purchaseOrderAsset.setDiscount(0);
                            tblPurchaseOrderItems.getItems().add(purchaseOrderAsset);
                            tblPurchaseOrderItems.refresh();

                            //itemsModified = true;

                            //computeQuoteTotal();

                        }
                        else IO.logAndAlert("Purchase Order Item Addition", "Invalid item selected.", IO.TAG_ERROR);
                    });

                    btnNew.setOnAction(event ->
                            ResourceManager.getInstance().newResourceWindow(param ->
                            {
                                new Thread(() ->
                                {
                                    refreshModel();
                                    Platform.runLater(() -> refreshView());
                                }).start();
                                return null;
                            }));

                    btnCancel.setOnAction(event ->
                            stage.close());
                    return;
                }
            }
        }
        IO.logAndAlert("Asset Purchase Order", "No assets were found in the database, please add some assets first and try again.", IO.TAG_ERROR);
    }

    @FXML
    public void createPurchaseOrder()
    {
        String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

        cbxSuppliers.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
        if(cbxSuppliers.getValue()==null)
        {
            cbxSuppliers.getStyleClass().remove("form-control-default");
            cbxSuppliers.getStyleClass().add("control-input-error");
            return;
        }else{
            cbxSuppliers.getStyleClass().remove("control-input-error");
            cbxSuppliers.getStyleClass().add("form-control-default");
        }

        cbxContactPerson.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
        if(cbxContactPerson.getValue()==null)
        {
            cbxContactPerson.getStyleClass().remove("form-control-default");
            cbxContactPerson.getStyleClass().add("control-input-error");
            return;
        }else{
            cbxContactPerson.getStyleClass().remove("control-input-error");
            cbxContactPerson.getStyleClass().add("form-control-default");
        }

        if(!Validators.isValidNode(txtVat, txtVat.getText(), 1, ".+"))
        {
            txtVat.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtAccount, txtAccount.getText(), 1, ".+"))
        {
            txtAccount.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }

        List<PurchaseOrderItem> purchaseOrderItems = tblPurchaseOrderItems.getItems();

        if(purchaseOrderItems==null)
        {
            IO.logAndAlert("Invalid Purchase Order", "PurchaseOrder items list is null.", IO.TAG_ERROR);
            return;
        }
        if(purchaseOrderItems.size()<=0)
        {
            IO.logAndAlert("Invalid Purchase Order", "PurchaseOrder has no materials", IO.TAG_ERROR);
            return;
        }

        String str_supplier = null;
        String str_contact = null;
        String str_vat = "";
        String str_account = "";
        try
        {
            str_supplier = cbxSuppliers.getValue().get_id();
            str_contact = cbxContactPerson.getValue().getUsr();
            str_vat = txtVat.getText();
            str_account = txtAccount.getText();
        }catch (NumberFormatException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
            return;
        }

        //prepare PurchaseOrder attributes
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setSupplier_id(str_supplier);
        purchaseOrder.setContact_person_id(str_contact);
        purchaseOrder.setStatus(0);
        purchaseOrder.setAccount(str_account);
        purchaseOrder.setVat(Double.parseDouble(str_vat));
        purchaseOrder.setCreator(SessionManager.getInstance().getActive().getUsername());

        PurchaseOrderItem[] items = new PurchaseOrderItem[purchaseOrderItems.size()];
        purchaseOrderItems.toArray(items);
        purchaseOrder.setItems(items);

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

            //create new purchase order on database
            HttpURLConnection connection = RemoteComms.postData("/api/purchaseorder/add", purchaseOrder.asUTFEncodedString(), headers);
            if(connection!=null)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());
                    IO.log(getClass().getName(), IO.TAG_INFO, "created purchaseorder ["+response+"]. Adding resources to purchaseorder.");

                    if(response==null)
                    {
                        IO.logAndAlert("New Purchase Order Item", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }
                    if(response.isEmpty())
                    {
                        IO.logAndAlert("New Purchase Order Item", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }

                    //Close connection
                    if(connection!=null)
                        connection.disconnect();
                    /* Add Purchase Order Resources to Purchase Order on database */

                    boolean added_all_po_items = true;
                    for(PurchaseOrderItem purchaseOrderItem: tblPurchaseOrderItems.getItems())
                    {
                        //prepare parameters for purchase order asset.
                        purchaseOrderItem.setPurchase_order_id(response);

                        if(purchaseOrderItem instanceof PurchaseOrderAsset)
                            connection = RemoteComms.postData("/api/purchaseorder/asset/add", purchaseOrderItem.asUTFEncodedString(), headers);
                        else if(purchaseOrderItem instanceof PurchaseOrderResource)
                            connection = RemoteComms.postData("/api/purchaseorder/item/add", purchaseOrderItem.asUTFEncodedString(), headers);
                        else IO.logAndAlert("Purchase Order Item Creation Error", "unknown purchase order item type ["+purchaseOrderItem+"].", IO.TAG_ERROR);

                        if (connection != null)
                        {
                            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                            {
                                IO.log(getClass().getName(), IO.TAG_INFO, "Successfully added a new purchase order["+response+"] item.");
                            } else
                            {
                                added_all_po_items = false;
                                //Get error message
                                String msg = IO.readStream(connection.getErrorStream());
                                IO.logAndAlert("Error " + String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                            }
                        }else IO.logAndAlert("New Purchase Order Item Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
                    }
                    if(added_all_po_items)
                    {
                        //System.out.println("po_id: "+response);
                        /*purchaseOrder.set_id(response);
                        PurchaseOrderItem[] arr_items = new PurchaseOrderItem[purchaseOrderItems.size()];
                        purchaseOrderItems.toArray(arr_items);
                        purchaseOrder.setItems(arr_items);
                        PurchaseOrderManager.getInstance().loadDataFromServer();
                        PurchaseOrderManager.getInstance().setSelected(purchaseOrder);
                        //tblPurchaseOrderItems.setItems(FXCollections.observableArrayList(PurchaseOrderManager.getInstance().getSelected().getItems()));*/

                        IO.logAndAlert("New Purchase Order Creation Success", "Successfully created a new Purchase Order.", IO.TAG_INFO);

                        PurchaseOrderManager.getInstance().loadDataFromServer();
                        PurchaseOrderManager.getInstance().setSelected(PurchaseOrderManager.getInstance().getPurchaseOrders().get(response));

                        ScreenManager.getInstance().showLoadingScreen(param ->
                        {
                            new Thread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    try
                                    {
                                        if(ScreenManager.getInstance().loadScreen(Screens.VIEW_PURCHASE_ORDER.getScreen(),getClass().getResource("../views/"+Screens.VIEW_PURCHASE_ORDER.getScreen())))
                                        {
                                            Platform.runLater(() -> ScreenManager.getInstance().setScreen(Screens.VIEW_PURCHASE_ORDER.getScreen()));
                                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load purchase order viewer screen.");
                                    } catch (IOException e)
                                    {
                                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                    }
                                }
                            }).start();
                            return null;
                        });
                        //itemsModified = false;
                    } else IO.logAndAlert("New Purchase Order Creation Failure", "Could not add items to Purchase Order.", IO.TAG_ERROR);
                }else
                {
                    //Get error message
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                }
                if(connection!=null)
                    connection.disconnect();
            }else IO.logAndAlert("New Purchase Order Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }
    }

    @FXML
    public void previousScreen()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        screenManager.showLoadingScreen(param ->
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
}