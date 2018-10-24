package application.controllers;

import application.models.CSSManager;
import application.models.NameModelManager;

/**Interface for controller classes, has one method called initialise
 * which should be called by a controller after it has been constructed
 */
public interface Controller {

    /**Every controller class in this program should have an initialise method which sets up the name models
     * stored by the controller as well as the theme the scene should display
     * @param nameManager reference to an instance of NameModelManager
     * @param cssManager reference to an instance of CSSManager
     */
    public void initialise(NameModelManager nameManager, CSSManager cssManager);
}
