package fadulousbms.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ghost on 2017/02/01.
 */
public class AssetType  implements BusinessObject
{
    private boolean marked;
    private String _id;
    private String type_name;
    private String type_description;
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

    @Override
    public void parse(String var, Object val)
    {
        switch (var.toLowerCase())
        {
            case "type_name":
                type_name = (String)val;
                break;
            case "type_description":
                type_description = (String)val;
                break;
            case "other":
                other = (String)val;
                break;
            default:
                System.err.println("Unknown ResourceType attribute '" + var + "'.");
                break;
        }
    }

    @Override
    public Object get(String var)
    {
        switch (var.toLowerCase())
        {
            case "type_name":
                return type_name;
            case "type_description":
                return type_description;
            case "other":
                return other;
            default:
                System.err.println("Unknown ResourceType attribute '" + var + "'.");
                return null;
        }
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/resource/type";
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("type_name","UTF-8") + "="
                    + URLEncoder.encode(type_name, "UTF-8") + "&");
            result.append(URLEncoder.encode("type_description","UTF-8") + "="
                    + URLEncoder.encode(type_description, "UTF-8") + "&");
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

    public StringProperty type_nameProperty(){return new SimpleStringProperty(type_name);}

    public String getType_name()
    {
        return type_name;
    }

    public void setType_name(String type_name)
    {
        this.type_name = type_name;
    }

    public StringProperty type_descriptionProperty(){return new SimpleStringProperty(type_description);}

    public String getType_description()
    {
        return type_description;
    }

    public void setType_description(String type_description)
    {
        this.type_description = type_description;
    }

    public StringProperty otherProperty(){return new SimpleStringProperty(other);}

    public String getOther()
    {
        return other;
    }

    public void setOther(String other)
    {
        this.other = other;
    }
}
