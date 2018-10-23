package application.models;

/**Class holds reference to what theme is use currently
 */
public class CSSManager {

    public String cssTheme;
    public Boolean isLight;

    /**Constructor called when application begins running
     */
    public CSSManager(){
        cssTheme = "/application/views/DarkTheme.css";
        isLight = false;
    }

    /**Called when user toggles the screen from light theme to dark
     */
    public void switchDark(){
        cssTheme = "/application/views/DarkTheme.css";
    }

    /**Called when user toggles the screen from dark theme to light
     *
     */
    public void switchLight(){
        cssTheme = "/application/views/LightTheme.css";
    }




}



