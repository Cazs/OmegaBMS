/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms;

import com.sun.javafx.application.LauncherImpl;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.Screen;
import fadulousbms.auxilary.Session;
import fadulousbms.controllers.HomescreenController;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.Screens;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jdk.nashorn.internal.objects.Global;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ghost
 */
public class FadulousBMS extends Application 
{
    //GridDisplay grid;
    //public static final String[] SCREENS = {"Homescreen.fxml","Operations.fxml"};
    
    /*public class GridDisplay
    {
        private static final double ELEMENT_SIZE = 100;
        private static final double GAP = ELEMENT_SIZE / 10;

        private TilePane tilePane = new TilePane();
        private Group display = new Group(tilePane);
        private int nRows;
        private int nCols;

        public GridDisplay(int nRows, int nCols) 
        {
            tilePane.setStyle("-fx-background-color: rgba(255, 215, 0, 0.1);");
            tilePane.setHgap(GAP);
            tilePane.setVgap(GAP);
            setColumns(nCols);
            setRows(nRows);
        }

        public void setColumns(int newColumns) {
            nCols = newColumns;
            tilePane.setPrefColumns(nCols);
            createElements();
        }

        public void setRows(int newRows) {
            nRows = newRows;
            tilePane.setPrefRows(nRows);
            createElements();
        }

        public Group getDisplay() {
            return display;
        }

        private void createElements() {
            tilePane.getChildren().clear();
            for (int i = 0; i < nCols; i++) {
                for (int j = 0; j < nRows; j++) {
                    tilePane.getChildren().add(createElement());
                }
            }
        }

        private Rectangle createElement() {
            Rectangle rectangle = new Rectangle(ELEMENT_SIZE, ELEMENT_SIZE);
            rectangle.setStroke(Color.ORANGE);
            rectangle.setFill(Color.STEELBLUE);

            return rectangle;
        }

    }*/
    
    @Override
    public void start(Stage stage) throws Exception 
    {
        stage.setOnCloseRequest(event ->
        {
            int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?");
            if(result==JOptionPane.OK_OPTION)
            {
                stage.close();
                System.exit(0);
            }
            else  event.consume();
        });
        //grid = new GridDisplay(2, 4);
        ScreenManager screen_mgr = new ScreenManager();
        IO.getInstance().init(screen_mgr);


        try
        {
            Screen.defaultProfileImage = ImageIO.read(new File("images/profile.png"));
        }catch (IOException ex)
        {
            IO.log(getClass().getName(), ex.getMessage(), IO.TAG_ERROR);
        }
        //screen_mgr.loadScreen("loading.fxml",getClass().getResource("views/loading.fxml"));
        //screen_mgr.loadScreen(Screens.LOGIN.getScreen(),getClass().getResource("views/"+Screens.LOGIN.getScreen()));
        //screen_mgr.loadScreen(Screens.SETTINGS.getScreen(),getClass().getResource("views/"+Screens.SETTINGS.getScreen()));
        //screen_mgr.loadScreen(Screens.CREATE_ACCOUNT.getScreen(),getClass().getResource("views/"+Screens.CREATE_ACCOUNT.getScreen()));
        //screen_mgr.loadScreen(Screens.RESET_PWD.getScreen(),getClass().getResource("views/"+Screens.RESET_PWD.getScreen()));
        //screen_mgr.loadScreen(Screens.NEW_QUOTE.getScreen(),getClass().getResource("views/"+Screens.NEW_QUOTE.getScreen()));
        //screen_mgr.loadScreen(Screens.QUOTES.getScreen(),getClass().getResource("views/"+Screens.QUOTES.getScreen()));
        //screen_mgr.loadScreen(Screens.VIEW_QUOTE.getScreen(),getClass().getResource("views/"+Screens.VIEW_QUOTE.getScreen()));
        //screen_mgr.loadScreen(Screens.JOBS.getScreen(),getClass().getResource("views/"+Screens.JOBS.getScreen()));
        //screen_mgr.loadScreen(Screens.VIEW_JOB.getScreen(),getClass().getResource("views/"+Screens.VIEW_JOB.getScreen()));
        //screen_mgr.loadScreen(Screens.NEW_GENERIC_QUOTE.getScreen(),getClass().getResource("views/"+Screens.NEW_GENERIC_QUOTE.getScreen()));
        //screen_mgr.loadScreen(Screens.VIEW_GENERIC_QUOTE.getScreen(),getClass().getResource("views/"+Screens.VIEW_GENERIC_QUOTE.getScreen()));
        //screen_mgr.loadScreen(Screens.CLIENTS.getScreen(),getClass().getResource("views/"+Screens.CLIENTS.getScreen()));
        //screen_mgr.loadScreen(Screens.SUPPLIERS.getScreen(),getClass().getResource("views/"+Screens.SUPPLIERS.getScreen()));
        //screen_mgr.loadScreen(Screens.RESOURCES.getScreen(),getClass().getResource("views/"+Screens.RESOURCES.getScreen()));
        //screen_mgr.loadScreen(Screens.GENERIC_QUOTES.getScreen(),getClass().getResource("views/"+Screens.GENERIC_QUOTES.getScreen()));
        //screen_mgr.loadScreen(Screens.REJECTED_QUOTES.getScreen(),getClass().getResource("views/"+Screens.REJECTED_QUOTES.getScreen()));

        //screen_mgr.loadScreen("loading.fxml", getClass().getResource("views/loading.fxml"));
        if(screen_mgr.loadScreen(Screens.LOGIN.getScreen(),getClass().getResource("views/"+Screens.LOGIN.getScreen())))
        {
            screen_mgr.setScreen(Screens.LOGIN.getScreen());
            HBox root = new HBox();
            HBox.setHgrow(screen_mgr, Priority.ALWAYS);

            root.getChildren().addAll(screen_mgr);

            Scene scene = new Scene(root);
            stage.setTitle(Globals.APP_NAME.getValue());
            stage.setScene(scene);

            stage.setMinHeight(600);
            stage.setHeight(700);
            stage.setMinWidth(600);

            if(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth()>=1200)
                stage.setWidth(900);
            stage.show();
        }else
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, "screens were not successfully loaded.");
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        LauncherImpl.launchApplication(FadulousBMS.class, BMSPreloader.class, args);
    }
    
}
