package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import fadulousbms.auxilary.*;
import fadulousbms.model.*;
import fadulousbms.model.Error;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
 * Created by ghost on 2017/01/13.
 */
public class PurchaseOrderManager extends BusinessObjectManager
{
    private PurchaseOrder[] purchaseOrders;
    private PurchaseOrder selected;
    private Gson gson;
    private static PurchaseOrderManager po_manager = new PurchaseOrderManager();
    public static final String TAG = "PurchaseOrderManager";
    public static final String ROOT_PATH = "cache/purchase_orders/";
    public String filename = "";
    private long timestamp;

    private PurchaseOrderManager()
    {
    }

    public static PurchaseOrderManager getInstance()
    {
        return po_manager;
    }

    public PurchaseOrder[] getPurchaseOrders()
    {
        return purchaseOrders;
    }

    public void setSelected(PurchaseOrder purchaseOrder)
    {
        this.selected=purchaseOrder;
    }

    public PurchaseOrder getSelected()
    {
        return this.selected;
    }

    @Override
    public void initialize()
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
                    String timestamp_json = RemoteComms.sendGetRequest("/api/timestamp/purchaseorders_timestamp", headers);
                    Counters cntr_timestamp = gson.fromJson(timestamp_json, Counters.class);
                    if(cntr_timestamp!=null)
                    {
                        timestamp = cntr_timestamp.getCount();
                        filename = "purchase_order_"+timestamp+".dat";
                        IO.log(this.getClass().getName(), IO.TAG_INFO, "Server Timestamp: "+timestamp);
                    }else {
                        IO.logAndAlert(this.getClass().getName(), "could not get valid timestamp", IO.TAG_ERROR);
                        return;
                    }

                    if(!isSerialized(ROOT_PATH+filename))
                    {
                        String purchaseorders_json = RemoteComms.sendGetRequest("/api/purchaseorders", headers);
                        purchaseOrders = gson.fromJson(purchaseorders_json, PurchaseOrder[].class);

                        IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of purchase orders.");

                        this.serialize(ROOT_PATH+filename, purchaseOrders);
                    }else{
                        IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object ["+ROOT_PATH+filename+"] on local disk is already up-to-date.");
                        purchaseOrders = (PurchaseOrder[]) this.deserialize(ROOT_PATH+filename);
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
}
