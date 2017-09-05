package fadulousbms.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ghost on 2017/02/01.
 */
public class Asset implements BusinessObject
{
    private boolean marked;
    private String _id;
    private String asset_name;
    private String asset_description;
    private String asset_type;
    private double asset_value;
    private long date_acquired;
    private long date_exhausted;
    private String other;

    public StringProperty idProperty(){return new SimpleStringProperty(_id);}

    @Override
    public String get_id()
    {
        return _id;
    }

    public void set_id(String _id)
    {
        this._id = _id;
    }

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

    public StringProperty asset_nameProperty(){return new SimpleStringProperty(asset_name);}

    public String getAsset_name()
    {
        return asset_name;
    }

    public void setAsset_name(String asset_name)
    {
        this.asset_name = asset_name;
    }

    public StringProperty asset_descriptionProperty(){return new SimpleStringProperty(asset_description);}

    public String getAsset_description()
    {
        return asset_description;
    }

    public void setAsset_description(String asset_description)
    {
        this.asset_description = asset_description;
    }

    public StringProperty asset_typeProperty(){return new SimpleStringProperty(asset_type);}

    public String getAsset_type()
    {
        return asset_type;
    }

    public void setAsset_type(String asset_type)
    {
        this.asset_type = asset_type;
    }

    public StringProperty asset_valueProperty(){return new SimpleStringProperty(String.valueOf(asset_value));}

    public double getAsset_value()
    {
        return asset_value;
    }

    public void setAsset_value(double asset_value)
    {
        this.asset_value = asset_value;
    }

    //public StringProperty date_acquiredProperty(){return new SimpleStringProperty(String.valueOf(date_acquired));}

    public long getDate_acquired()
    {
        return date_acquired;
    }

    public void setDate_acquired(long date_acquired)
    {
        this.date_acquired = date_acquired;
    }

    //public StringProperty date_exhaustedProperty(){return new SimpleStringProperty(String.valueOf(date_exhausted));}

    public long getDate_exhausted()
    {
        return date_exhausted;
    }

    public void setDate_exhausted(long date_exhausted)
    {
        this.date_exhausted = date_exhausted;
    }

    public StringProperty otherProperty(){return new SimpleStringProperty(String.valueOf(other));}

    public String getOther()
    {
        return other;
    }

    public void setOther(String other)
    {
        this.other = other;
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("asset_name","UTF-8") + "="
                    + URLEncoder.encode(asset_name, "UTF-8") + "&");
            result.append(URLEncoder.encode("asset_type","UTF-8") + "="
                    + URLEncoder.encode(asset_type, "UTF-8") + "&");
            result.append(URLEncoder.encode("asset_description","UTF-8") + "="
                    + URLEncoder.encode(asset_description, "UTF-8") + "&");
            result.append(URLEncoder.encode("asset_value","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(asset_value), "UTF-8") + "&");
            result.append(URLEncoder.encode("date_acquired","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(date_acquired), "UTF-8") + "&");
            result.append(URLEncoder.encode("date_exhausted","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(date_exhausted), "UTF-8") + "&");
            if(other!=null)
                result.append(URLEncoder.encode("other","UTF-8") + "="
                        + URLEncoder.encode(other, "UTF-8") + "&");

            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            System.err.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void parse(String var, Object val)
    {
        switch (var.toLowerCase())
        {
            case "asset_name":
                asset_name = (String)val;
                break;
            case "asset_description":
                asset_description = (String)val;
                break;
            case "asset_type":
                asset_type = (String)val;
                break;
            case "asset_value":
                asset_value = Double.parseDouble(String.valueOf(val));
                break;
            case "date_acquired":
                date_acquired = Long.parseLong(String.valueOf(val));
                break;
            case "date_exhausted":
                date_exhausted = Long.parseLong(String.valueOf(val));
                break;
            case "other":
                other = (String)val;
                break;
            default:
                System.err.println("Unknown Asset attribute '" + var + "'.");
                break;
        }
    }

    @Override
    public Object get(String var)
    {
        switch (var.toLowerCase())
        {
            case "asset_name":
                return asset_name;
            case "asset_type":
                return asset_type;
            case "asset_value":
                return asset_value;
            case "asset_description":
                return asset_description;
            case "date_acquired":
                return date_acquired;
            case "date_exhausted":
                return date_exhausted;
            case "other":
                return other;
            default:
                System.err.println("Unknown Asset attribute '" + var + "'.");
                return null;
        }
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/asset";
    }
}
