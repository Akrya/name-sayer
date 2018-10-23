package application.models;

public class CSSManager {

    public String cssTheme;
    public Boolean isLight;


    public CSSManager(){
        cssTheme = "/application/views/DarkTheme.css";
        isLight = false;
    }

    public void switchDark(){
        cssTheme = "/application/views/DarkTheme.css";
    }

    public void switchLight(){
        cssTheme = "/application/views/LightTheme.css";
    }




}



