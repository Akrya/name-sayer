package application.models;

import application.controllers.NamesSelectorController;

public class ControllerManager {

    //singleton class that holds a reference to NameSelectorController, reference to controller is passed to CustomPlayMode so it can load the selected names

    private static ControllerManager _instance = null;

    private NamesSelectorController _controller;

    public static ControllerManager getInstance(){
        if (_instance == null){
            _instance = new ControllerManager();
        }
        return _instance;
    }

    private ControllerManager(){ }

    public void setController(NamesSelectorController controller){
        _controller = controller;
    }

    public NamesSelectorController getController(){
        return _controller;
    }


}
