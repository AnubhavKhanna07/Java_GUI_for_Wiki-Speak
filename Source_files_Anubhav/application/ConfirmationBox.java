package application;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConfirmationBox {
	
	private static boolean decision;
	
	public static boolean confirmNow(String title, String message) {
		Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinWidth(250);
        Label label = new Label(message);

        //Create two buttons
        Button yesButton = new Button("Yes");
        Button noButton = new Button("No");

        //Clicking will set answer and close window
        yesButton.setOnAction(e -> {
            decision = true;
            window.close();
        });
        noButton.setOnAction(e -> {
            decision = false;
            window.close();
        });

        VBox vbox = new VBox(10);
        HBox hbox = new HBox(10);

        //Add buttons
        hbox.getChildren().addAll(yesButton, noButton);
        hbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(label, hbox);
        vbox.setAlignment(Pos.CENTER);
        Scene scene = new Scene(vbox);
        window.setScene(scene);
        window.showAndWait();

        //Make sure to return answer
        return decision;
    }
	}
