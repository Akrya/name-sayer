package application.models;

import application.uiControllers.NamesSelectorController;

public class NameSelectorSingleton {

    //singleton class that holds a reference to NameSelectorController, reference to controller is passed to CustomPlayMode so it can load the selected names

    private static NameSelectorSingleton _instance = null;

    private NamesSelectorController _controller;

    public static NameSelectorSingleton getInstance(){
        if (_instance == null){
            _instance = new NameSelectorSingleton();
        }
        return _instance;
    }

    private NameSelectorSingleton(){ }

    public void setController(NamesSelectorController controller){
        _controller = controller;
    }

    public NamesSelectorController getController(){
        return _controller;
    }


}
