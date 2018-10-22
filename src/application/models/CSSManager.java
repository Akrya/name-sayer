package application.models;

import javax.swing.text.html.CSS;

public class CSSManager {

    public String cssTheme;
    public Boolean isLight;


    public CSSManager(){
        cssTheme = "/application/views/FXStyler.css";
        isLight = false;
    }

    public void switchDark(){
        cssTheme = "/application/views/FXStyler.css";
    }

    public void switchLight(){
        cssTheme = "/application/views/FXstylerLight.css";
    }




}



