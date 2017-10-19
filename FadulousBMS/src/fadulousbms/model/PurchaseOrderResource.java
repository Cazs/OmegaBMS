package fadulousbms.model;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.AssetManager;
import fadulousbms.managers.ResourceManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ghost on 2017/01/21.
 */
public class PurchaseOrderResource extends PurchaseOrderItem
{
    public static final String TAG = "PurchaseOrderResource";

    public String getItem_name()
    {
        return getItem().getResource_name();
    }

    public String getItem_description()
    {
        return getItem().getResource_description();
    }

    public String getUnit()
    {
        return getItem().getUnit();
    }

    public double getCostValue()
    {
        return getItem().getResource_value();
    }

    public Resource getItem()
    {
        ResourceManager.getInstance().loadDataFromServer();
        if (ResourceManager.getInstance().getResources() != null)
        {
            Resource resource = ResourceManager.getInstance().getResources().get(getItem_id());
            if (resource != null)
            {
                return resource;
            }
            else IO.log(TAG, IO.TAG_ERROR, "key returns null po resource object.");
        }
        else IO.log(TAG, IO.TAG_ERROR, "no resources on database.");
        return null;
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/purchaseorder/item";
    }
}