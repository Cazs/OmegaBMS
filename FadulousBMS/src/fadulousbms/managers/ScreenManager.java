/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.managers;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.Screen;
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
import javafx.util.Duration;

/**
 *
 * @author ghost
 */
public class ScreenManager extends StackPane
{
    private HashMap<String, Node> screens = new HashMap<>();
    private HashMap<String, Screen> controllers = new HashMap<>();
    private Screen focused;
    private String focused_id;
    
    public ScreenManager()
    {
        super();
        //this.setWidth(600);
        //this.setHeight(480);
        //this.stage = stage;
    }

    /**
     * Method to add a Screen object to array of screens in memory.
     * @param id Screen identifier.
     * @param screen Screen/Node object to be added to memory.
     * @return true if successfully added Screen, false otherwise.
     */
    public boolean addScreen(String id, Node screen)
    {
        screens.put(id, screen);
        return true;
    }

    /**
     * Method to get a Screen that has been loaded to memory.
     * @param id Screen identifier.
     * @return Screen[Node] object from memory.
     */
    public Node getScreen(String id)
    {
        return screens.get(id);
    }

    /**
     * Method to load a single Screen into memory.
     * @param id Screen identifier
     * @param path Path to the FXML view for the Screen.
     * @return true if successfully added new screen, false otherwise.
     * @throws IOException
     */
    public boolean  loadScreen(String id, URL path) throws IOException
    {
        //String path = filename;
        FXMLLoader loader = new FXMLLoader(path);
        //URL url = new URL(path);
        //System.out.println(">>"+ScreenManager.class.getResource(path)+"<<");
        Parent screen = loader.load();
        
        Screen screen_controller = loader.getController();
        screen_controller.setParent(this);
        controllers.put(id, screen_controller);
        
        return addScreen(id, screen);
    }

    /**
     * Method to load array of Screens to memory.
     * @param filenames Paths to the FXML views for the Screens.
     * @throws IOException
     */
    public void  loadScreens(String[] filenames) throws IOException
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
    }
    

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
            Node screen = screens.get(id);

            if(screen!=null)
            {
                //update UI of current view
                Screen controller = controllers.get(id);
                if(controller!=null)
                {
                    focused_id = id;
                    focused = controller;
                    //screen.setOpacity(1);
                    focused.refresh();//refresh the screen every time it's loaded
                }
                getChildren().add(screen);

                final DoubleProperty opacity =  opacityProperty();
                Timeline fade = new Timeline(new KeyFrame(Duration.ONE, new KeyValue(opacity, 0.0)),
                        new KeyFrame(Duration.millis(20),new KeyValue(opacity, 1.0)));
                fade.play();
            }else{
                IO.logAndAlert(getClass().getName(), "Screen not loaded to memory.", IO.TAG_ERROR);
            }
        }else{
            IO.logAndAlert(getClass().getName(), "Could not remove StackPane children.", IO.TAG_ERROR);
        }
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
