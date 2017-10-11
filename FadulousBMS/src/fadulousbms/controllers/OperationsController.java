/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.*;
import fadulousbms.managers.*;
import fadulousbms.model.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * views Controller class
 *
 * @author ghost
 */
public class OperationsController extends Screen implements Initializable
{
    @FXML
    private TabPane tabs;
    @FXML
    private Tab clientTab;
    @FXML
    private TableView<Client>    tblClients;

    public static final String TAG="OperationsController";

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        if (SessionManager.getInstance().getActive() != null)
        {
            if (!SessionManager.getInstance().getActive().isExpired())
            {
                    //Set default profile photo
                    if(HomescreenController.defaultProfileImage!=null)
                    {
                        Image image = SwingFXUtils.toFXImage(HomescreenController.defaultProfileImage, null);
                        this.getProfileImageView().setImage(image);
                    }else IO.log(TAG, "default profile image is null.", IO.TAG_ERROR);
            }else{
                JOptionPane.showMessageDialog(null, "No active sessions!", "Session expired", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }else{
            JOptionPane.showMessageDialog(null, "No active sessions!", "Session expired", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    @Override
    public void refreshView()
    {
        Employee e = SessionManager.getInstance().getActiveEmployee();
        if(e!=null)
            this.getUserNameLabel().setText(e.toString());
        else IO.log(getClass().getName(), IO.TAG_ERROR, "No active sessions.");
    }

    @Override
    public void refreshModel()
    {
        //ClientManager.getInstance().initialize();
        //clientsController.refreshModel();
        //clientsController.refreshView();
    }

    @FXML
    public void newClientClick()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.NEW_CLIENT.getScreen(),getClass().getResource("../views/"+Screens.NEW_CLIENT.getScreen())))
                        {
                            //Platform.runLater(() ->
                            screenManager.setScreen(Screens.NEW_CLIENT.getScreen());
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load client creation screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    @FXML
    public void newSupplierClick()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.NEW_SUPPLIER.getScreen(),getClass().getResource("../views/"+Screens.NEW_SUPPLIER.getScreen())))
                        {
                            //Platform.runLater(() ->
                            screenManager.setScreen(Screens.NEW_SUPPLIER.getScreen());
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load supplier creation screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    @FXML
    public void newMaterialClick()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.NEW_RESOURCE.getScreen(),getClass().getResource("../views/"+Screens.NEW_RESOURCE.getScreen())))
                        {
                            //Platform.runLater(() ->
                            screenManager.setScreen(Screens.NEW_RESOURCE.getScreen());
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load resource creation screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    @FXML
    public void newQuoteClick()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.NEW_QUOTE.getScreen(),getClass().getResource("../views/"+Screens.NEW_QUOTE.getScreen())))
                        {
                            //Platform.runLater(() ->
                            screenManager.setScreen(Screens.NEW_QUOTE.getScreen());
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load quote creation screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    //nav
    @FXML
    public void productionClick()
    {
        try 
        {
            ScreenManager.getInstance().loadScreen(Screens.OPERATIONS_PRODUCTION.getScreen(),
                    getClass().getResource("../views/" + Screens.OPERATIONS_PRODUCTION.getScreen()));
            ScreenManager.getInstance().setScreen(Screens.OPERATIONS_PRODUCTION.getScreen());
        } catch (IOException ex) 
        {
            Logger.getLogger(OperationsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void salesClick()
    {
        try
        {
            ScreenManager.getInstance().loadScreen(Screens.OPERATIONS_SALES.getScreen(),
                    getClass().getResource("../views/" + Screens.OPERATIONS_SALES.getScreen()));
            ScreenManager.getInstance().setScreen(Screens.OPERATIONS_SALES.getScreen());
        } catch (IOException ex)
        {
            Logger.getLogger(OperationsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void resourcesClick()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.RESOURCES.getScreen(),getClass().getResource("../views/"+Screens.RESOURCES.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.RESOURCES.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load resources screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    //Sales event handlers
    @FXML
    public void quotesClick()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.QUOTES.getScreen(),getClass().getResource("../views/"+Screens.QUOTES.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.QUOTES.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load quotes screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    @FXML
    public void pendingQuotesClick()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.GENERIC_QUOTES.getScreen(),getClass().getResource("../views/"+Screens.GENERIC_QUOTES.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.GENERIC_QUOTES.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load generic quotes screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    @FXML
    public void rejectedQuotesClick()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.REJECTED_QUOTES.getScreen(),getClass().getResource("../views/"+Screens.REJECTED_QUOTES.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.REJECTED_QUOTES.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load rejected quotes screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    //Production event handlers
    @FXML
    public void suppliersClick()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.SUPPLIERS.getScreen(),getClass().getResource("../views/"+Screens.SUPPLIERS.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.SUPPLIERS.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load suppliers screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    @FXML
    public void jobsClick()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.JOBS.getScreen(),getClass().getResource("../views/"+Screens.JOBS.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.JOBS.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load jobs screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    @FXML
    public void clientsClick()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.CLIENTS.getScreen(),getClass().getResource("../views/"+Screens.CLIENTS.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.CLIENTS.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load clients screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }
}
