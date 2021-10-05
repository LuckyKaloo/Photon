package application.Controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;

public class CustomDialog implements Initializable {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Label information;
    @FXML
    private ImageView imageView;
    @FXML
    private Label dialogType;

    public enum DialogType {ERROR, INFO}

    private final StringProperty css = new SimpleStringProperty();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        anchorPane.styleProperty().bind(css);
    }

    public void setType(DialogType type) {
        String fileName = switch (type) {
            case ERROR -> "error.png";
            case INFO -> "info.png";
        };

        dialogType.setText(switch (type) {
            case ERROR -> "Error";
            case INFO -> "Info";
        });

        try {
            Image image = new Image(new FileInputStream("src/application/Resources/images/" + fileName));
            imageView.setImage(image);
        } catch (FileNotFoundException ignored) {}
    }

    public void setInformation(String text) {
        information.setText(text);
    }

    @FXML
    private void exit() {
        Main.exitDialog();
    }

    public void setCss(String css) {
        this.css.setValue(css);
    }
}
