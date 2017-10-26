package fadulousbms.model;

import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.managers.ResourceManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/21.
 */
public class QuoteItem implements BusinessObject, Serializable
{
    private String _id;
    private int item_number;
    private int quantity;
    private double labour;
    private double markup;
    private String additional_costs;
    private boolean marked;
    private String quote_id;
    private String resource_id;
    private String extra;
    public static final String TAG = "QuoteItem";

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

    private StringProperty quote_idProperty(){return new SimpleStringProperty(quote_id);}

    public String getQuote_id()
    {
        return quote_id;
    }

    public void setQuote_id(String quote_id)
    {
        this.quote_id = quote_id;
    }

    private StringProperty resource_idProperty(){return new SimpleStringProperty(resource_id);}

    public String getResource_id()
    {
        return resource_id;
    }

    public void setResource_id(String resource_id)
    {
        this.resource_id = resource_id;
    }

    private StringProperty equipment_nameProperty(){return new SimpleStringProperty(getEquipment_name());}

    public String getEquipment_name()
    {
        Resource resource = getResource();
        if(resource!=null)
            return resource.getResource_name();
        return "N/A";
    }

    private StringProperty equipment_descriptionProperty(){return new SimpleStringProperty(getEquipment_description());}

    public String getEquipment_description()
    {
        Resource resource = getResource();
        if(resource!=null)
            return resource.getResource_description();
        return "N/A";
    }

    private StringProperty additional_costsProperty(){return new SimpleStringProperty(additional_costs);}

    public String getAdditional_costs()
    {
        return additional_costs;
    }

    public void setAdditional_costs(String additional_costs)
    {
        this.additional_costs = additional_costs;
    }

    private StringProperty unitProperty(){return new SimpleStringProperty(getUnit());}

    public String getUnit()
    {
        Resource resource = getResource();
        if(resource!=null)
            return resource.getUnit();
        return "N/A";
    }

    private StringProperty quantityProperty(){return new SimpleStringProperty(String.valueOf(quantity));}

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

    private StringProperty labourProperty() {return new SimpleStringProperty(String.valueOf(labour));}

    public String getLabour()
    {
        return String.valueOf(labour);
    }

    public double getLabourCost()
    {
        return labour;
    }

    public void setLabour(double labour)
    {
        this.labour = labour;
    }

    private StringProperty valueProperty(){return new SimpleStringProperty(String.valueOf(getValue()));}

    public String getValue()
    {
        return String.valueOf(getCost());
    }

    public double getCost()
    {
        Resource resource = getResource();
        if(resource!=null)
            return resource.getResource_value();
        return 0;
    }

    private StringProperty markupProperty(){return new SimpleStringProperty(String.valueOf(markup));}

    public String getMarkup(){return String.valueOf(this.markup);}

    public double getMarkupValue(){return this.markup;}

    public void setMarkup(double markup){this.markup=markup;}

    private StringProperty extraProperty(){return new SimpleStringProperty(extra);}

    public String getExtra()
    {
        return extra;
    }

    public void setExtra(String extra)
    {
        this.extra = extra;
    }

    public Resource getResource()
    {
        ResourceManager.getInstance().loadDataFromServer();
        HashMap<String, Resource> resources = ResourceManager.getInstance().getResources();
        if(resources!=null)
            return resources.get(getResource_id());
        return null;
    }

    public StringProperty rateProperty()
    {
        return new SimpleStringProperty(Globals.CURRENCY_SYMBOL.getValue() + " " + getRate());
    }

    public double getRate()
    {
        double marked_up = getCost() + getCost()*(markup/100);
        double total = marked_up;
        total += labour;

        //check additional costs
        if (additional_costs != null)
        {
            if (!additional_costs.isEmpty())
            {
                //compute additional costs for each Quote Item
                if(additional_costs.contains(";"))//check cost delimiter
                {
                    String[] costs = additional_costs.split(";");
                    for (String str_cost : costs)
                    {
                        if (str_cost.contains("="))
                        {
                            double cost = Double.parseDouble(str_cost.split("=")[1]);
                            total += cost;
                        }
                        else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid Quote Item additional cost.");
                    }
                }else if (additional_costs.contains("="))//if only one additional cost
                {
                    double cost = Double.parseDouble(additional_costs.split("=")[1]);
                    total += cost;
                }else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid Quote Item additional cost.");
            }
        }
        return total;
    }

    public StringProperty totalProperty()
    {
        return new SimpleStringProperty(Globals.CURRENCY_SYMBOL.getValue() + " " + getTotal());
    }

    public double getTotal()
    {
        return getRate()*getQuantityValue();
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/quote/resource";
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("quote_id","UTF-8") + "="
                    + URLEncoder.encode(quote_id, "UTF-8") + "&");
            result.append(URLEncoder.encode("resource_id","UTF-8") + "="
                    + URLEncoder.encode(resource_id, "UTF-8") + "&");
            result.append(URLEncoder.encode("item_number","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(item_number), "UTF-8") + "&");
            if(additional_costs!=null)
                result.append(URLEncoder.encode("additional_costs","UTF-8") + "="
                        + URLEncoder.encode(additional_costs, "UTF-8") + "&");
            result.append(URLEncoder.encode("quantity","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(quantity), "UTF-8") + "&");
            result.append(URLEncoder.encode("labour","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(labour), "UTF-8") + "&");
            result.append(URLEncoder.encode("markup","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(markup), "UTF-8"));
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

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "quote_id":
                    quote_id = String.valueOf(val);
                    break;
                case "resource_id":
                    resource_id = String.valueOf(val);
                    break;
                case "item_number":
                    item_number = Integer.valueOf((String)val);
                    break;
                case "additional_costs":
                    additional_costs = (String)val;
                    break;
                case "quantity":
                    quantity = Integer.valueOf((String)val);
                    break;
                case "labour":
                    labour = Double.valueOf((String)val);
                    break;
                case "markup":
                    markup = Double.parseDouble((String) val);
                    break;
                case "extra":
                    extra = String.valueOf(val);
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "Unknown QuoteItem attribute '" + var + "'.");
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
                return get_id();
            case "quote_id":
                return getQuote_id();
            case "resource_id":
                return getResource_id();
            case "item_number":
                return getItem_number();
            case "equipment_name":
                return getEquipment_name();
            case "equipment_description":
                return getEquipment_description();
            case "additional_costs":
                return getAdditional_costs();
            case "unit":
                return getUnit();
            case "quantity":
                return getQuantityValue();
            case "labour":
                return getLabourCost();
            case "value":
                return getCost();
            case "markup":
                return getMarkupValue();
            case "extra":
                return getExtra();
            default:
                IO.log(TAG, IO.TAG_ERROR, "Unknown QuoteItem attribute '" + var + "'.");
                return null;
        }
    }
}