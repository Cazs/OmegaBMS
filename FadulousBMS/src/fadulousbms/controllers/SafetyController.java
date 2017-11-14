/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.*;
import fadulousbms.model.Employee;
import fadulousbms.model.Screens;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class SafetyController extends ScreenController implements Initializable
{
    @Override
    public void refreshView()
    {
    }

    @Override
    public void refreshModel()
    {
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
    }

    public void safetyClick()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.SAFETY_FILES.getScreen(),getClass().getResource("../views/"+Screens.SAFETY_FILES.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.SAFETY_FILES.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load safety files screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    public void riskClick()
    {
    }

    public void ohsClick()
    {
    }

    public void appointmentClick()
    {
    }

    public void showScanWindow()
    {
        /*Morena morena = new Morena();
        try
        {
            Manager manager= Manager.getInstance();
            morena.simpleScan();
            manager.close();
        } catch (Exception e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }*/
        //Scan scan = new Scan();

    }

    public void inspectionClick()
    {
    }
}