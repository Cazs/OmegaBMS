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
    private Stack<AbstractMap.SimpleEntry<String, Node>> screens = new Stack<>();
    private Stack<AbstractMap.SimpleEntry<String, Screen>> controllers = new Stack<>();
    private Screen focused;
    private String focused_id;
    private String previous_id;
    private Node loading_screen;
    private Screen loading_screen_ctrl;
    private static ScreenManager screenManager = new ScreenManager();

    private ScreenManager()
    {
        super();
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/loading.fxml"));
            loading_screen = loader.load();
            loading_screen_ctrl = loader.getController();
            loading_screen_ctrl.setParent(this);
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    public static ScreenManager getInstance()
    {
        return screenManager;
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

    public Screen peekScreenControllers()
    {
        if(controllers!=null)
            return controllers.peek().getValue();
        else return null;
    }

    public Node peekScreens()
    {
        if(screens!=null)
            return screens.peek().getValue();
        else return null;
    }

    public void setPreviousScreen() throws IOException
    {
        if(screens.size()>1)
        {
            screens.pop().getValue();
            controllers.pop().getValue();
            setScreen("previous");
        }
    }

    /**
     * Method to add a Screen object to ScreenManager
     * @param id Screen identifier
     */
    public void setScreen(final String id)
    {
        if(getChildren().setAll(new Node[]{}))//remove all screens
        {
            Node screen = null;
            if(screens.peek()!=null)
                        screen = screens.peek().getValue();
            if(screen!=null)
            {
                Screen controller = null;
                //update UI of current view
                if(controllers.peek()!=null)
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
