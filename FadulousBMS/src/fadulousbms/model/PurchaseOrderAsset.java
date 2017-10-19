package fadulousbms.model;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.AssetManager;
import fadulousbms.managers.ResourceManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;

/**
 * Created by ghost on 2017/01/21.
 */
public class PurchaseOrderAsset extends PurchaseOrderItem
{
    public static final String TAG = "PurchaseOrderAsset";

    public String getItem_name()
    {
        return getItem().getAsset_name();
    }

    public String getItem_description()
    {
        return getItem().getAsset_description();
    }

    public String getUnit()
    {
        return getItem().getUnit();
    }

    public double getCostValue()
    {
        return getItem().getAsset_value();
    }

    public Asset getItem()
    {
        AssetManager.getInstance().loadDataFromServer();
        if (AssetManager.getInstance().getAssets() != null)
        {
            Asset asset = AssetManager.getInstance().getAssets().get(getItem_id());
            if (asset != null)
                return asset;
            else IO.log(TAG, IO.TAG_ERROR, "key returns null po asset object.");
        }
        else IO.log(TAG, IO.TAG_ERROR, "no assets found on database.");
        return null;
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/purchaseorder/asset";
    }
}