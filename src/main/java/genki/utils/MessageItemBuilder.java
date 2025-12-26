package genki.utils;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

/**
 * Helper to build message UI nodes (received / sent)
 */
public class MessageItemBuilder {

    public static HBox createReceivedMessage(String profileImageUrl, String senderName, String messageText) {
        return createReceivedMessage(profileImageUrl, senderName, messageText, "");
    }

    public static HBox createReceivedMessage(String profileImageUrl, String senderName, String messageText, String timestamp) {
        HBox container = new HBox(10);
        container.getStyleClass().addAll("message-box", "received-message");
        container.setAlignment(Pos.TOP_LEFT);

        ImageView avatar = buildAvatar(profileImageUrl);
        avatar.getStyleClass().add("avatar");

        VBox content = new VBox(4);
        Label name = new Label(senderName);
        name.getStyleClass().addAll("sender-name");

        Label msg = new Label(messageText);
        msg.setWrapText(true);
        msg.setMaxWidth(600);
        msg.getStyleClass().addAll("message-text", "received-bubble");

        // Add timestamp
        HBox messageTimeBox = new HBox(8);
        messageTimeBox.setAlignment(Pos.BOTTOM_LEFT);
        Label timeLabel = new Label(timestamp);
        timeLabel.getStyleClass().add("message-time");
        timeLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 11px; -fx-padding: 2 0 0 0;");
        messageTimeBox.getChildren().add(timeLabel);

        content.getChildren().addAll(name, msg, messageTimeBox);

        container.getChildren().addAll(avatar, content);
        return container;
    }

    public static HBox createSentMessage(String profileImageUrl, String senderName, String messageText) {
        return createSentMessage(profileImageUrl, senderName, messageText, "");
    }

    public static HBox createSentMessage(String profileImageUrl, String senderName, String messageText, String timestamp) {
        HBox container = new HBox(10);
        container.getStyleClass().addAll("message-box", "sent-message");
        container.setAlignment(Pos.TOP_RIGHT);

        ImageView avatar = buildAvatar(UserSession.getImageUrl());
        avatar.getStyleClass().add("avatar");

        VBox content = new VBox(4);
        Label name = new Label("You");
        name.getStyleClass().addAll("sender-name", "sent-name");

        Label msg = new Label(messageText);
        msg.setWrapText(true);
        msg.setMaxWidth(600);
        msg.getStyleClass().addAll("message-text", "sent-bubble");

        // Add timestamp
        HBox messageTimeBox = new HBox(8);
        messageTimeBox.setAlignment(Pos.BOTTOM_RIGHT);
        Label timeLabel = new Label(timestamp);
        timeLabel.getStyleClass().add("message-time");
        timeLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 11px; -fx-padding: 2 0 0 0;");
        messageTimeBox.getChildren().add(timeLabel);

        content.setAlignment(Pos.TOP_RIGHT);
        content.getChildren().addAll(name, msg, messageTimeBox);

        // For sent messages, order is content then avatar
        container.getChildren().addAll(content, avatar);
        return container;
    }

    private static ImageView buildAvatar(String profileImageUrl) {
        ImageView avatar = new ImageView();
        avatar.setFitHeight(40);
        avatar.setFitWidth(40);
        avatar.setPreserveRatio(false); // Always fill the 40x40 area
        try {
            String imageUrl = profileImageUrl;
            if (profileImageUrl != null && !profileImageUrl.startsWith("http") && !profileImageUrl.startsWith("file:")) {
                var res = MessageItemBuilder.class.getResource("/" + profileImageUrl);
                if (res != null) imageUrl = res.toExternalForm();
            }
            // Load at 160x160 for better clarity when displaying at 40x40
            if (imageUrl != null) avatar.setImage(new Image(imageUrl, 160, 160, false, true));
        } catch (Exception e) {
            System.err.println("Error loading avatar image: " + e.getMessage());
        }
        // Center the clip for a 40x40 image
        Circle clip = new Circle(20, 20, 20);
        avatar.setClip(clip);
        avatar.getStyleClass().add("avatar");
        return avatar;
    }
}
