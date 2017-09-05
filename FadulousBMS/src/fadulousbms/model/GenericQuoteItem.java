package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ghost on 2017/01/21.
 */
public class GenericQuoteItem implements BusinessObject
{
    private String _id;
    private int item_number;
    private String quote_id;
    private String equipment_name;
    private String equipment_description;
    private String unit;
    private int quantity;
    private double value;
    private double rate;
    private double labour;
    private double markup;
    private String extra;
    private boolean marked;
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

    private StringProperty equipment_descriptionProperty(){return new SimpleStringProperty(equipment_description);}

    public String getEquipment_description()
    {
        return equipment_description;
    }

    public void setEquipment_description(String equipment_description)
    {
        this.equipment_description = equipment_description;
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

    private StringProperty equipment_nameProperty(){return new SimpleStringProperty(equipment_name);}

    public String getEquipment_name()
    {
        return equipment_name;
    }

    public void setEquipment_name(String equipment_name)
    {
        this.equipment_name = equipment_name;
    }

    private StringProperty unitProperty(){return new SimpleStringProperty(unit);}

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
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

    public String getRate()
    {
        return String.valueOf(rate);
    }

    public double getRateValue()
    {
        return rate;
    }

    public void setRate(double rate)
    {
        this.rate = rate;
    }

    private StringProperty labourProperty(){return new SimpleStringProperty(String.valueOf(labour));}

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

    private StringProperty valueProperty(){return new SimpleStringProperty(String.valueOf(value));}

    public String getValue()
    {
        return String.valueOf(value);
    }

    public double getVal()
    {
        return value;
    }

    public void setValue(double value)
    {
        this.value = value;
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

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "item_number":
                    item_number = Integer.valueOf((String)val);
                    break;
                case "quote_id":
                    quote_id = String.valueOf(val);
                    break;
                case "equipment_name":
                    equipment_name = String.valueOf(val);
                    break;
                case "equipment_description":
                    equipment_description = (String)val;
                    break;
                case "unit":
                    unit = (String)val;
                    break;
                case "quantity":
                    quantity = Integer.valueOf((String)val);
                    break;
                case "value":
                    value = Double.parseDouble((String)val);
                    break;
                case "rate":
                    rate = Double.valueOf((String)val);
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
                    IO.log(getClass().getName(), IO.TAG_ERROR, "Unknown GenericQuoteItem attribute '" + var + "'.");
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
                return quote_id;
            case "item_number":
                return item_number;
            case "equipment_name":
                return equipment_name;
            case "equipment_description":
                return equipment_description;
            case "unit":
                return unit;
            case "quantity":
                return quantity;
            case "value":
                return value;
            case "rate":
                return rate;
            case "labour":
                return labour;
            case "markup":
                return markup;
            case "extra":
                return extra;
            default:
                System.err.println("Unknown GenericQuoteItem attribute '" + var + "'.");
                return null;
        }
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/quote/generic/resource";
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("quote_id","UTF-8") + "="
                    + URLEncoder.encode(quote_id, "UTF-8"));
            result.append("&" + URLEncoder.encode("item_number","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(item_number), "UTF-8"));
            result.append("&" + URLEncoder.encode("equipment_name","UTF-8") + "="
                    + URLEncoder.encode(equipment_name, "UTF-8"));
            result.append("&" + URLEncoder.encode("equipment_description","UTF-8") + "="
                    + URLEncoder.encode(equipment_description, "UTF-8"));
            result.append("&" + URLEncoder.encode("unit","UTF-8") + "="
                    + URLEncoder.encode(unit, "UTF-8"));
            result.append("&" + URLEncoder.encode("quantity","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(quantity), "UTF-8"));
            result.append("&" + URLEncoder.encode("value","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(value), "UTF-8"));
            result.append("&" + URLEncoder.encode("rate","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(rate), "UTF-8"));
            result.append("&" + URLEncoder.encode("labour","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(labour), "UTF-8"));
            result.append("&" + URLEncoder.encode("markup","UTF-8") + "="
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
}
