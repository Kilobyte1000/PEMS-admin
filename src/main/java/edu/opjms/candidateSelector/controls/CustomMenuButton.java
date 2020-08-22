package edu.opjms.candidateSelector.controls;

import edu.opjms.candidateSelector.util.HouseIndex;
import javafx.scene.input.MouseEvent;

public class CustomMenuButton extends javafx.scene.control.Button {

    private HouseIndex houseIndex;

    public CustomMenuButton() {
        super();
        this.setOnMouseMoved(this::MouseTrackGradient);
        this.setOnMouseExited(mouseEvent -> this.setStyle("-fx-background-color: transparent"));
    }

    private HouseIndex setUpEnum() {
        try {
            houseIndex = HouseIndex.valueOf(getText().toUpperCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return houseIndex;
    }

    public HouseIndex getHouseIndex() {
        return (houseIndex == null) ? setUpEnum() : houseIndex;
    }

    private void MouseTrackGradient(MouseEvent mouseEvent) {
        var mousePosition = mouseEvent.getX() / this.getWidth();
        this.setStyle("-fx-font-size: 19;-fx-background-color: linear-gradient(to right, rgba(255,255,255," + (0.6 * (1 - mousePosition) + 0.05)
                + ") 0%, rgba(255,255,255,0.6) " + mousePosition * 100
                + "%, rgba(255,255,255," + (0.6 * mousePosition + 0.05)
                + ") 100%)");
    }
}
