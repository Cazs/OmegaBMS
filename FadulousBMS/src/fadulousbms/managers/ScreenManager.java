/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.managers;

import java.io.IOException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Stack;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.Screen;
import fadulousbms.model.Screens;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

/**
 *
 * @author ghost
 */
public class ScreenManager extends StackPane
{
    //private HashMap<String, Node> screens = new HashMap<>();
    private Stack<AbstractMap.SimpleEntry<String, Node>> screens = new Stack<>();
    //private HashMap<String, Screen> controllers = new HashMap<>();
    private Stack<AbstractMap.SimpleEntry<String, Screen>> controllers = new Stack<>();
    private Screen focused;
    private String focused_id;
    private String previous_id;
    private Node loading_screen;
    private Screen loading_screen_ctrl;
    
    public ScreenManager()
    {
        super();
        try
        {
            //loadScreen("loading.fxml", getClass().getResource("../views/loading.fxml"));
            //loading_screen = screens.get("loading.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/loading.fxml"));
            loading_screen = loader.load();
            loading_screen_ctrl = loader.getController();
            loading_screen_ctrl.setParent(this);
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
        /*try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/loading.fxml"));
            loading_screen = loader.load();

            loading_screen_ctrl = loader.getController();
            loading_screen_ctrl.setParent(this);
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }*/
    }

    /**
     * Method to add a Screen object to array of screens in memory.
     * @param id Screen identifier.
     * @param screen Screen/Node object to be added to memory.
     * @return true if successfully added Screen, false otherwise.
     */
    /*public boolean addScreen(String id, Node screen)
    {
        screens.putIfAbsent(id, screen);
        return true;
    }*/

    /**
     * Method to get a Screen that has been loaded to memory.
     * @param id Screen identifier.
     * @return Screen[Node] object from memory.
     */
    /*public Node getScreen(String id)
    {
        return screens.get(id);
    }*/

    /**
     * Method to load a single Screen into memory.
     * @param id Screen identifier
     * @param path Path to the FXML view for the Screen.
     * @return true if successfully added new screen, false otherwise.
     * @throws IOException
     */
    public boolean  loadScreen(String id, URL path) throws IOException
    {
        /*try
        {
            loadScreen("loading.fxml", getClass().getResource("../views/loading.fxml"));
            loading_screen = screens.get("loading.fxml");
            getChildren().setAll(new Node[]{});
            getChildren().add(loading_screen);
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }*/
        //setScreen("loading.fxml");

        /*if(focused!=null)
        {
            if(focused.getLoadingPane()!=null)
            {
                focused.getLoadingPane().setVisible(true);
                focused.refresh();
                if(focused_id!=null)
                    screens.get(focused_id).setVisible(true);
                setScreen("loading.fxml");
                System.err.println("showing loading screen");
            }
        }*/
        //if(loadScreen("loading.fxml",getClass().getResource("../views/loading.fxml")))
        //addScreen("loading.fxml", loading_screen);
        //setScreen("loading.fxml");
        //else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load loading screen.");

        //Backup previous screen and its controller
        //Screen prev_screen_ctrl = controllers.get(previous_id);
        //Node prev_screen = screens.get(previous_id);

        //remove other screens
        //controllers = new HashMap<>();
        //screens = new HashMap<>();
        //controllers = new Stack<>();
        //screens = new Stack<>();

        //controllers.putIfAbsent(previous_id, prev_screen_ctrl);
        //screens.putIfAbsent(previous_id, prev_screen);

        //controllers.putIfAbsent(id, screen_controller);
        //return addScreen(id, screen);
        FXMLLoader loader = new FXMLLoader(path);
        Parent screen = loader.load();

        Screen screen_controller = loader.getController();
        screen_controller.setParent(this);

        controllers.push(new AbstractMap.SimpleEntry<>(id, screen_controller));
        screens.push(new AbstractMap.SimpleEntry<>(id, screen));

        return true;
    }

    public void setPreviousScreen() throws IOException
    {
        /*if(loadScreen(previous_id, getClass().getResource("../views/"+ previous_id)))
            setScreen(previous_id);
        else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load screen: " + previous_id);*/
        if(screens.size()>1)
        {
            screens.pop();
            controllers.pop();
            setScreen("previous");
        }
    }

    /**
     * Method to load array of Screens to memory.
     * @param filenames Paths to the FXML views for the Screens.
     * @throws IOException
     */
    /*public void  loadScreens(String[] filenames) throws IOException
    {
        for(String filename : filenames)
        {
            String path = "../views/"+filename;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            loader.setRoot(this);
            loader.setLocation(getClass().getResource(path));
            Parent screen = (Parent)loader.load();

            Screen screen_controller = (Screen)loader.getController();
            screen_controller.setParent(this);
            addScreen(filename, screen);
        }
    }*/
    

    /**
     * Method to add a Screen object to ScreenManager
     * @param id Screen identifier
     */
    public void setScreen(final String id)
    {
        if(focused_id!=null)
        {
            /*Node screen = screens.get(focused_id);
            System.out.println("\n instanceof> "+(screen instanceof BorderPane)+"\n");
            if(screen instanceof Pane)
            {
                screen.setStyle("-fx-background-color: rgba(0, 100, 100, 0.5); -fx-background-radius: 10;");
                for(Node node : ((Pane)screen).getChildren())
                {
                    node.setOpacity(0.2);
                    node.setStyle("-fx-background-color: rgba(0, 100, 100, 0.5); -fx-background-radius: 10;");
                }
            }*/
            /*for(Node node : getChildren())
                node.setOpacity(0.1);*/
            //final DoubleProperty opacity =  opacityProperty();
            //Timeline fade = new Timeline(new KeyFrame(Duration.ONE, new KeyValue(opacity, 0.0)),
            //        new KeyFrame(Duration.millis(20),new KeyValue(opacity, 1.0)));
            //fade.setOnFinished(event -> );
            //fade.play();
            //screen.getScene().getRoot().setOpacity(0.2);
            //if(focused!=null)
            //    focused.refresh();
            //System.out.println("lowered opacity of current screen");
        }

        if(getChildren().setAll(new Node[]{}))//remove all screens
        {
            //getChildren().add(screens.get("loading.fxml"));
            Node screen = null;
            /*if(id.equals("loading.fxml"))
            {
                System.out.println("showing loading screen");
                screen = loading_screen;
            } else */if(screens.peek()!=null)
                        screen = screens.peek().getValue();//screens.get(id);

            if(screen!=null)
            {
                Screen controller = null;
                //update UI of current view
                /*if(id.equals("loading.fxml"))
                    controller = loading_screen_ctrl;
                else */if(controllers.peek()!=null)
                        controller = controllers.peek().getValue();

                if(controller!=null)
                {
                    if(focused_id!=null)
                    {
                        if (!focused_id.equals(previous_id))
                        {
                            previous_id = focused_id;
                            IO.log(getClass().getName(), IO.TAG_INFO, "set previous screen to: " + previous_id);
                        }
                    }

                    focused_id = id;
                    focused = controller;
                    //screen.setOpacity(1);
                    focused.refreshStatusBar("Welcome back" + (SessionManager.getInstance().getActiveEmployee()!=null?" "+SessionManager.getInstance().getActiveEmployee()+"!":"!"));
                    focused.refresh();//refresh the screen every time it's loaded
                }
                getChildren().add(screen);

                final DoubleProperty opacity =  opacityProperty();
                Timeline fade = new Timeline(new KeyFrame(Duration.ONE, new KeyValue(opacity, 0.0)),
                        new KeyFrame(Duration.millis(20),new KeyValue(opacity, 1.0)));
                fade.play();
            }else{
                IO.logAndAlert(getClass().getName(), "Screen ["+id+"] not loaded to memory.", IO.TAG_ERROR);
            }
        }else{
            IO.logAndAlert(getClass().getName(), "Could not remove StackPane children.", IO.TAG_ERROR);
        }
    }

    public void showLoadingScreen(Callback callback)
    {
        if(getChildren().setAll(new Node[]{}))//remove all screens
        {
            loading_screen_ctrl.refreshStatusBar("Loading data, please wait...");
            loading_screen_ctrl.refresh();
            getChildren().add(loading_screen);
            /*if(focused!=null)
            {
                focused.getLoadingPane().setVisible(true);
                //System.out.println(focused.getLoadingPane()==null);
            }*/
            callback.call(null);
        }
    }

    public Screen getFocused()
    {
        return this.focused;
    }

    public String getFocused_id()
    {
        return this.focused_id;
    }
    
    public Node removeScreen(Node screen)
    {
        //screens
        for(Node n: getChildren())
        {
            if(n==screen)
            {
                if(getChildren().remove(n))
                    return n;
                else
                    return null;
            }
        }
        return null;
    }
}
