package application.models;

import application.controllers.NamesSelectorController;

/**Singleton class that holds a reference to NameSelectorController, reference to controller is passed to CustomPlayMode so it can load the selected names
 */
public class ControllerManager {

    private static ControllerManager _instance = null;

    private NamesSelectorController _controller;

    /** Method called when the manager is needed, ensures only one instance of the manager is created
     * @return
     */
    public static ControllerManager getInstance(){
        if (_instance == null){
            _instance = new ControllerManager();
        }
        return _instance;
    }

    /**Private constructor to ensure only one instance of the class is instantiated
     */
    private ControllerManager(){ }

    /** Method called when NamesSelectorController is constructed, it is passed to this class, the practice controller will use this
     * class to access the stored controller
     * @param controller NamesSelectorController instance
     */
    public void setController(NamesSelectorController controller){
        _controller = controller;
    }

    /** Method called by practice mode controller to get the NamesSelectorController instance  so it can load in the practice list
     * @return NamesSelectorController instance
     */
    public NamesSelectorController getController(){
        return _controller;
    }


}
