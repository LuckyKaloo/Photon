package application.Controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class About implements Initializable {
    @FXML
    private AnchorPane anchorPane;

    private final StringProperty css = new SimpleStringProperty();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        anchorPane.styleProperty().bind(css);
    }

    @FXML
    private void exit() {
        Main.exitAbout();
    }

    public void setCss(String css) {
        this.css.setValue(css);
    }
}
