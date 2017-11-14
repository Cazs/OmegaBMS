package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.Employee;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;

import java.net.URL;
import java.util.ResourceBundle;

public class NavController extends ScreenController implements Initializable
{
    @FXML
    private Label lblScreen;

    @Override
    public void refreshView()
    {
        if (SessionManager.getInstance().getActive() != null)
        {
            if (!SessionManager.getInstance().getActive().isExpired())
            {
                //Render default profile photo
                if(HomescreenController.defaultProfileImage!=null)
                {
                    Image image = SwingFXUtils.toFXImage(HomescreenController.defaultProfileImage, null);
                    super.getProfileImageView().setImage(image);
                }else IO.log(getClass().getName(), "default profile image is null.", IO.TAG_ERROR);
                //Render user name
                Employee e = SessionManager.getInstance().getActiveEmployee();
                if(e!=null)
                    this.getUserNameLabel().setText(e.toString());
                else IO.log(getClass().getName(), IO.TAG_ERROR, "No active sessions.");
                //Render current screen name
                lblScreen.setText(ScreenManager.getInstance().peekScreenControllers().getKey());
            }else
            {
                IO.logAndAlert("Session expired","No active sessions!", IO.TAG_ERROR);
                return;
            }
        } else {
            IO.logAndAlert("Session expired","No active sessions!", IO.TAG_ERROR);
            return;
        }
    }

    @Override
    public void refreshModel()
    {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        ScreenManager.getInstance().setLblScreenName(lblScreen);
        refreshView();
    }
}
