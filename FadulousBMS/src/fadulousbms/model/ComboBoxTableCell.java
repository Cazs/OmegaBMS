package fadulousbms.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.BusinessObject;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringJoiner;

/**
 * Created by ghost on 2017/01/09.
 */

public class ComboBoxTableCell extends TableCell<BusinessObject, String>
{
    private ComboBox<String> comboBox;
    private String property, label_property, api_method;
    private BusinessObject[] business_objects;
    private boolean multi_type_items = false;
    public static final String TAG = "ComboBoxTableCell";
    //private BusinessObject bo_selected;

    public ComboBoxTableCell(BusinessObject[] business_objects, String property, String label_property, String api_method)
    {
        super();
        this.property = property;
        this.api_method = api_method;
        this.business_objects=business_objects;
        this.label_property = label_property;

        String[] combobox_items;
        if(business_objects==null)
        {
            IO.log(TAG, IO.TAG_ERROR, "business objects array for the combo box cannot be null!");
            return;
        }
        if(business_objects.length<=0)
        {
            IO.log(TAG, IO.TAG_ERROR, "business objects array for the combo box cannot be empty!");
            return;
        }

        combobox_items = new String[business_objects.length];

        for(int i=0;i<business_objects.length;i++)
            combobox_items[i] = (String) business_objects[i].get(label_property);

        comboBox = new ComboBox<>(FXCollections.observableArrayList(combobox_items));
        HBox.setHgrow(comboBox, Priority.ALWAYS);

        comboBox.valueProperty().addListener((observable, oldValue, newValue) ->
        {
            int selected_pos = comboBox.selectionModelProperty().get().getSelectedIndex();
            if(selected_pos<business_objects.length)
            {
                updateItem(business_objects[selected_pos].get_id(), false);
                if(oldValue!=null)
                    commitEdit(business_objects[selected_pos].get_id());
                //IO.log(TAG, IO.TAG_INFO, "selected: " + business_objects[selected_pos]);
            }
            else IO.log(TAG, IO.TAG_ERROR, "selection index out of bounds.");
        });
    }

    public ComboBoxTableCell(BusinessObject[] business_objects, String property, String label_properties, String api_method, boolean multi_type_items)
    {
        super();
        this.property = property;
        this.api_method = api_method;
        this.business_objects=business_objects;
        this.label_property = label_properties;
        this.multi_type_items = multi_type_items;

        String[] combobox_items;
        if(business_objects==null)
        {
            IO.log(TAG, IO.TAG_ERROR, "business objects array for the combo box cannot be null!");
            return;
        }
        if(business_objects.length<=0)
        {
            IO.log(TAG, IO.TAG_ERROR, "business objects array for the combo box cannot be empty!");
            return;
        }

        combobox_items = new String[business_objects.length];
        String[] properties = label_properties.split("\\|");
        for(int i=0;i<business_objects.length;i++)
        {
            if(multi_type_items)
            {
                String prop_val = getBusinessObjectProperty(properties, business_objects[i]);
                if (prop_val!=null)
                {
                    combobox_items[i] = prop_val;
                    IO.log(TAG, IO.TAG_INFO, String.format("set combo box array item #%s to '%s'.", i, prop_val));
                    //break;
                }else
                {
                    IO.log(TAG, IO.TAG_WARN, String.format("property '%s' on object of type '%s' is null.", label_property, business_objects[i].getClass().getName()));
                }
            }else{//Single type combo box items
                combobox_items[i] = (String) business_objects[i].get(label_property);
            }
        }

        comboBox = new ComboBox<>(FXCollections.observableArrayList(combobox_items));
        HBox.setHgrow(comboBox, Priority.ALWAYS);

        IO.log(TAG, IO.TAG_INFO, "set array to combo box.");

        comboBox.valueProperty().addListener((observable, oldValue, newValue) ->
        {
            int selected_pos = comboBox.selectionModelProperty().get().getSelectedIndex();
            if(selected_pos>=0 && selected_pos<business_objects.length)
            {
                updateItem(business_objects[selected_pos].get_id(), business_objects[selected_pos].get_id()==null);
                if(oldValue!=null)
                    commitEdit(business_objects[selected_pos].get_id());
                //IO.log(TAG, IO.TAG_INFO, "selected: " + business_objects[selected_pos]);
            }else IO.log(TAG, IO.TAG_ERROR, "index out of bounds.");
        });
    }

    /*public ComboBoxTableCell(String[] items, String property, String api_method)
    {
        this.property = property;
        this.api_method = api_method;

        comboBox = new ComboBox<>(FXCollections.observableArrayList(items));

        comboBox.getSelectionModel().selectedItemProperty().addListener((ov, oldValue, newValue) ->
        {
            if(!isEmpty())
            {
                commitEdit(comboBox.getValue());
                updateItem(comboBox.getValue(), isEmpty());
            }else{
                System.err.println("Selected BusinessObject is empty!");
            }
        });
    }*/

    /**
     * Function to get the matching values to a list of BusinessObject attributes.
     * @param properties list of attributes to be retrieved.
     * @param business_object BusinessObject to retrieve the values from.
     * @return String with all the attribute values separated by a space.
     */
    public String getBusinessObjectProperty(String[] properties, BusinessObject business_object)
    {
        String prop_val = "";
        for (String label_property : properties)
            prop_val += business_object.get(label_property) + " ";
        if (prop_val != null)
            return prop_val.substring(0,prop_val.length()-1);//return the chained String - without the last space.
        return null;
    }

    @Override
    public void commitEdit(String selected_id)
    {
        super.commitEdit(selected_id);
        if(selected_id!=null)
        {
            if (getTableRow().getItem() instanceof BusinessObject)
            {
                BusinessObject bo = (BusinessObject) getTableRow().getItem();
                bo.parse(property, selected_id);
                if (bo != null)
                {
                    RemoteComms.updateBusinessObjectOnServer(bo, api_method, property);
                    IO.log(TAG, IO.TAG_INFO, "updated business object: " + bo);
                } else
                {
                    IO.log(TAG, IO.TAG_ERROR, "row business object is null.");
                }
            } else IO.log(TAG, IO.TAG_ERROR, String.format("unknown row object: " + getTableRow().getItem()));
        }else IO.log(TAG, IO.TAG_ERROR, "selected id is null.");
    }

    @Override
    protected void updateItem(String selected_id, boolean empty)
    {
        super.updateItem(selected_id, empty);
        BusinessObject tbl_row_businessObject;
        if(selected_id==null && !empty)
        {
            if (getTableRow() != null)
            {
                if (getTableRow().getItem() instanceof BusinessObject)
                {
                    tbl_row_businessObject = (BusinessObject) getTableRow().getItem();
                    if (tbl_row_businessObject != null)
                    {
                        if(SessionManager.getInstance().getActive()!=null)
                        {
                            String url = tbl_row_businessObject.apiEndpoint() + "/" + tbl_row_businessObject.get_id();
                            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
                            try
                            {
                                String obj_json = RemoteComms.sendGetRequest(url, headers);

                                //System.out.println(obj_json);
                                if (obj_json != null)
                                {
                                    Gson gson = new GsonBuilder().create();
                                    if(!obj_json.toString().equals("") && !obj_json.toString().equals("[]") &&
                                            !obj_json.toString().equals("{}") && obj_json.toString().contains("{"))
                                    {
                                        BusinessObject obj = gson.fromJson(obj_json, tbl_row_businessObject.getClass());

                                        for (BusinessObject combo_item : business_objects)
                                        {
                                            if (combo_item.get_id() != null)
                                            {
                                                if (combo_item.get_id().equals(obj.get(property)))
                                                {
                                                    String prop_val;
                                                    /*
                                                        If the combo items are of multiple data types then
                                                        Get the appropriate label for each data type.
                                                        Labels are passed through the label_property property -
                                                        If there are multi-types the labels are separated by the pipe (|) symbol.
                                                     */
                                                    if (multi_type_items)
                                                    {
                                                        String[] properties = label_property.split("\\|");
                                                        prop_val = getBusinessObjectProperty(properties, combo_item);
                                                    } else
                                                    {
                                                        prop_val = (String) combo_item.get(label_property);
                                                    }

                                                    //if a valid label was found on the object then the combo box value and graphic are set
                                                    if (prop_val != null)
                                                    {
                                                        comboBox.setValue(prop_val);
                                                        setGraphic(comboBox);
                                                        IO.log(TAG, IO.TAG_INFO, String.format("set property value of '%s' on combo box.", prop_val));
                                                        break;
                                                    } else
                                                    {
                                                        IO.log(TAG, IO.TAG_WARN, String.format("property '%s' on object of type '%s' is null.", label_property, combo_item.getClass().getName()));
                                                    }
                                                }
                                            } else
                                            {
                                                IO.log(TAG, IO.TAG_WARN, "combo box item id is null.");
                                            }
                                        }
                                    }else{
                                        IO.log(getClass().getName(), IO.TAG_ERROR, "invalid JSON object ["+tbl_row_businessObject.get_id()+" type "+tbl_row_businessObject.getClass().getName()+"]\n" + obj_json);
                                    }
                                } else
                                {
                                    IO.log(TAG, IO.TAG_ERROR, "JSON data from server is null.");
                                }
                            } catch (IOException e)
                            {
                                IO.log(TAG, IO.TAG_ERROR, e.getMessage());
                            }
                        } else
                        {
                            IO.log(TAG, IO.TAG_ERROR, "no active sessions.");
                        }
                    } else
                    {
                        IO.log(TAG, IO.TAG_ERROR, "row object is null.");
                    }
                } else
                {
                    IO.log(TAG, IO.TAG_ERROR, "unknown row object: " + getTableRow().getItem());
                }
            } else
            {
                IO.log(TAG, IO.TAG_ERROR, "row is null.");
            }
        }else{
            if(business_objects!=null)
            {
                for (BusinessObject combo_item : business_objects)
                {
                    if (combo_item.get_id() != null)
                    {
                        if (combo_item.get_id().equals(selected_id))
                        {
                            String prop_val;
                            /*
                                If the combo items are of multiple data types then
                                Get the appropriate label for each data type.
                                Labels are passed through the label_property property -
                                If there are multi-types the labels are separated by the pipe (|) symbol.
                             */
                            if (multi_type_items)
                            {
                                String[] properties = label_property.split("\\|");
                                prop_val = getBusinessObjectProperty(properties, combo_item);
                            } else
                            {
                                prop_val = (String) combo_item.get(label_property);
                            }

                            //if a valid label was found on the object then the combo box value and graphic are set
                            if (prop_val != null)
                            {
                                comboBox.setValue(prop_val);
                                setGraphic(comboBox);
                                IO.log(TAG, IO.TAG_INFO, String.format("set property value of '%s' on combo box.", prop_val));
                                break;
                            } else
                            {
                                IO.log(TAG, IO.TAG_WARN, String.format("property '%s' on object of type '%s' is null.", label_property, combo_item.getClass().getName()));
                            }
                        }
                    } else
                    {
                        IO.log(TAG, IO.TAG_WARN, "combo box item id is null.");
                    }
                }
            }else{
                IO.log(TAG, IO.TAG_WARN, (getTableView().getItems().size()>0?"business objects of type " + getTableView().getItems().get(0).getClass().getName():"business objects of " + selected_id) + " are NULL.");
            }
        }
        /*if(selected_id!=null)
        {
            if(Globals.DEBUG_INFO.getValue().toLowerCase().equals("on"))
                System.out.println(String.format("ComboBox> info: selected id is '%s'.", selected_id));

            for (BusinessObject bo : business_objects)
            {
                if (bo.get_id().equals(selected_id))
                {
                    comboBox.setValue((String) bo.get(label_property));
                    if(Globals.DEBUG_INFO.getValue().toLowerCase().equals("on"))
                        System.out.println(String.format("ComboBox> info: selected '%s'.", (String) bo.get(label_property)));
                }
            }
            setGraphic(comboBox);
        } else
            if(Globals.DEBUG_ERRORS.getValue().toLowerCase().equals("on"))
                System.err.println("ComboBox> error: selected id is null, ignoring.");*/
    }

    @Override
    public void startEdit()
    {
        super.startEdit();
    }
}
