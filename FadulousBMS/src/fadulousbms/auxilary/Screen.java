/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.auxilary;

import fadulousbms.managers.ScreenManager;

/**
 *
 * @author ghost
 */
public interface Screen 
{
    void setParent(ScreenManager mgr);
    void refresh();
}
