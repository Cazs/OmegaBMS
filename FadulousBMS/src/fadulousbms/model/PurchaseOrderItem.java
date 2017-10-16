package fadulousbms.model;

import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ghost on 2017/01/21.
 */
public class PurchaseOrderItem implements BusinessObject, Serializable
{
    private String _id;
    private int item_number;
    private String purchase_order_id;
    private String item_id;
    private int quantity;
    private double discount;
    private long date_logged;
    private BusinessObject item;
    private boolean marked;
    private String extra;
    public static final String TAG = "PurchaseOrderItem";

    public StringProperty idProperty(){return new SimpleStringProperty(_id);}

    /**
     * Function to get identifier of Quote object.
     * @return Quote identifier.
     */
    @Override
    public String get_id()
    {
        return _id;
    }

    /**
     * Method to assign identifier to this object.
     * @param _id identifier to be assigned to this object.
     */
    public void set_id(String _id)
    {
        this._id = _id;
    }


    /**
     * Function to get a shortened identifier of this object.
     * @return The shortened identifier.
     */
    public StringProperty short_idProperty(){return new SimpleStringProperty(_id.substring(0, 8));}

    @Override
    public String getShort_id()
    {
        return _id.substring(0, 8);
    }

    @Override
    public boolean isMarked()
    {
        return marked;
    }

    @Override
    public void setMarked(boolean marked){this.marked=marked;}

    private StringProperty item_numberProperty(){return new SimpleStringProperty(String.valueOf(item_number));}

    public String getItem_number()
    {
        return String.valueOf(item_number);
    }

    public int getItem_numberValue()
    {
        return item_number;
    }

    public void setItem_number(int item_number)
    {
        this.item_number = item_number;
    }

    private StringProperty purchase_order_idProperty(){return new SimpleStringProperty(purchase_order_id);}

    public String getPurchase_order_id()
    {
        return purchase_order_id;
    }

    public void setPurchase_order_id(String purchase_order_id)
    {
        this.purchase_order_id = purchase_order_id;
    }

    private StringProperty item_idProperty(){return new SimpleStringProperty(item_id);}

    public String getItem_id()
    {
        return item_id;
    }

    public void setItem_id(String item_id)
    {
        this.item_id = item_id;
    }

    public long getDate_logged()
    {
        return date_logged;
    }

    public void setDate_logged(long date_logged)
    {
        this.date_logged = date_logged;
    }

    private StringProperty item_nameProperty()
    {
        return new SimpleStringProperty(getItem_name());
    }

    public String getItem_name()
    {
        if(item!=null)
        {
            if(item instanceof Resource)
            {
                return ((Resource)item).getResource_name();
            }else if(item instanceof Asset)
            {
                return ((Asset)item).getAsset_name();
            }else IO.log(TAG, IO.TAG_ERROR, "unknown PurchaseOrderItem item type.");
        }else IO.log(TAG, IO.TAG_ERROR, "PurchaseOrderItem item is not set.");
        return "N/A";
    }

    private StringProperty item_descriptionProperty(){return new SimpleStringProperty(getItem_description());}

    public String getItem_description()
    {
        if(item!=null)
        {
            if(item instanceof Resource)
            {
                return ((Resource)item).getResource_description();
            }else if(item instanceof Asset)
            {
                return ((Asset)item).getAsset_description();
            }else IO.log(TAG, IO.TAG_ERROR, "unknown PurchaseOrderItem item type.");
        }else IO.log(TAG, IO.TAG_ERROR, "PurchaseOrderItem item is not set.");
        return "N/A";
    }

    private StringProperty unitProperty(){return new SimpleStringProperty(getUnit());}

    public String getUnit()
    {
        if(item!=null)
        {
            if(item instanceof Resource)
            {
                return ((Resource)item).getUnit();
            }else if(item instanceof Asset)
            {
                return ((Asset)item).getUnit();
            }else IO.log(TAG, IO.TAG_ERROR, "unknown PurchaseOrderItem item type.");
        }else IO.log(TAG, IO.TAG_ERROR, "PurchaseOrderItem item is not set.");
        return "N/A";
    }

    private StringProperty quantityProperty(){return new SimpleStringProperty(getQuantity());}

    public String getQuantity()
    {
        return String.valueOf(quantity);
    }

    public int getQuantityValue()
    {
        return quantity;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    private StringProperty costProperty(){return new SimpleStringProperty(getCost());}

    public String getCost()
    {
        return String.valueOf(getCostValue());
    }

    public double getCostValue()
    {
        if(item!=null)
        {
            if(item instanceof Resource)
            {
                return ((Resource)item).getResource_value();
            }else if(item instanceof Asset)
            {
                return ((Asset)item).getAsset_value();
            }else IO.log(TAG, IO.TAG_ERROR, "unknown PurchaseOrderItem item type.");
        }else IO.log(TAG, IO.TAG_ERROR, "PurchaseOrderItem item is not set.");
        return 0;
    }

    private StringProperty discountProperty(){return new SimpleStringProperty(String.valueOf(discount));}

    public String getDiscount(){return String.valueOf(this.discount);}

    public double getDiscountValue(){return this.discount;}

    public void setDiscount(double discount){this.discount=discount;}

    private StringProperty extraProperty(){return new SimpleStringProperty(extra);}

    public String getExtra()
    {
        return extra;
    }

    public void setExtra(String extra)
    {
        this.extra = extra;
    }

    public BusinessObject getResource()
    {
        return item;
    }

    public void setResource(BusinessObject item)
    {
        this.item=item;
    }

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "purchase_order_id":
                    purchase_order_id = String.valueOf(val);
                    break;
                case "item_id":
                    item_id = String.valueOf(val);
                    break;
                case "item_number":
                    item_number = Integer.valueOf((String)val);
                    break;
                case "quantity":
                    quantity = Integer.valueOf((String)val);
                    break;
                case "discount":
                    discount = Double.parseDouble((String) val);
                    break;
                case "extra":
                    extra = String.valueOf(val);
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "Unknown PurchaseOrderItem attribute '" + var + "'.");
                    break;
            }
        }catch (NumberFormatException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @Override
    public Object get(String var)
    {
        switch (var.toLowerCase())
        {
            case "_id":
                return _id;
            case "quote_id":
                return purchase_order_id;
            case "item_id":
                return item_id;
            case "item_number":
                return item_number;
            case "quantity":
                return quantity;
            case "discount":
                return discount;
            case "extra":
                return extra;
            default:
                System.err.println("Unknown PurchaseOrderItem attribute '" + var + "'.");
                return null;
        }
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/purchaseorder/item";
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("purchase_order_id","UTF-8") + "="
                    + URLEncoder.encode(purchase_order_id, "UTF-8"));
            result.append("&" + URLEncoder.encode("item_id","UTF-8") + "="
                    + URLEncoder.encode(item_id, "UTF-8"));
            result.append("&" + URLEncoder.encode("item_number","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(item_number), "UTF-8"));
            result.append("&" + URLEncoder.encode("quantity","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(quantity), "UTF-8"));
            result.append("&" + URLEncoder.encode("discount","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(discount), "UTF-8"));
            if(date_logged>0)
                result.append("&" + URLEncoder.encode("date_logged","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_logged), "UTF-8"));
            if(extra!=null)
                if(!extra.isEmpty())
                    result.append("&" + URLEncoder.encode("extra","UTF-8") + "="
                            + URLEncoder.encode(extra, "UTF-8"));
            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(TAG, IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }
}