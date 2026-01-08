package genki.utils;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.logging.Logger;

public class ConversationItemBuilder {

    private static final Logger logger = Logger.getLogger(ConversationItemBuilder.class.getName());

    public static HBox createConversationItem(String profileImageUrl, String contactName,
                                             String lastMessage, String time, int unreadCount,
                                             boolean isOnline) {

        HBox mainContainer = new HBox();
        mainContainer.setAlignment(Pos.CENTER_LEFT);
        mainContainer.setSpacing(12.0);
        mainContainer.getStyleClass().add("conversation-item");

        StackPane profileContainer = createProfileContainer(profileImageUrl, isOnline);

        VBox messageInfo = createMessageInfo(contactName, lastMessage);
        HBox.setHgrow(messageInfo, javafx.scene.layout.Priority.ALWAYS);

        VBox rightInfo = createRightInfo(time, unreadCount);

        mainContainer.getChildren().addAll(profileContainer, messageInfo, rightInfo);
        return mainContainer;
    }

    public static HBox createGroupConversationItem(String groupImageUrl, String groupName,
                                              String lastMessage, String time, int unreadCount) {

        HBox mainGroupContainer = new HBox();
        mainGroupContainer.setAlignment(Pos.CENTER_LEFT);
        mainGroupContainer.setSpacing(12.0);
        mainGroupContainer.getStyleClass().add("conversation-item");

        StackPane groupContainer = createGroupContainer(groupImageUrl);

        VBox messageInfo = createMessageInfo(groupName, lastMessage);
        HBox.setHgrow(messageInfo, javafx.scene.layout.Priority.ALWAYS);

        VBox rightInfo = createRightInfo(time, unreadCount);

        mainGroupContainer.getChildren().addAll(groupContainer, messageInfo, rightInfo);
        return mainGroupContainer;
    }

    private static StackPane createProfileContainer(String profileImageUrl, boolean isOnline) {
        StackPane profileContainer = new StackPane();

        ImageView profileImage = new ImageView();
        profileImage.setFitHeight(45.0);
        profileImage.setFitWidth(45.0);
        profileImage.setPreserveRatio(false);
        profileImage.setPickOnBounds(true);

        try {
            String imageUrl = profileImageUrl;
            if (profileImageUrl != null && !profileImageUrl.startsWith("http") && !profileImageUrl.startsWith("file:")) {
                java.io.File file = new java.io.File(profileImageUrl);
                if (file.exists()) {
                    imageUrl = file.toURI().toURL().toExternalForm();
                } else {
                    var res = ConversationItemBuilder.class.getResource("/" + profileImageUrl);
                    if (res != null) imageUrl = res.toExternalForm();
                    else imageUrl = ConversationItemBuilder.class.getResource("/genki/img/user-default.png").toExternalForm();
                }
            }
            if (imageUrl != null) profileImage.setImage(new Image(imageUrl, 180, 180, false, true));
        } catch (Exception e) {
            System.err.println("Error loading profile image: " + e.getMessage());
        }

        Circle clip = new Circle(22.5, 22.5, 22.5);
        profileImage.setClip(clip);

        Circle statusCircle = new Circle(6.0);
        statusCircle.setFill(isOnline ? Color.web("#4ade80") : Color.web("#9ca3af"));
        statusCircle.setStroke(Color.web("#1e2936"));
        statusCircle.setStrokeWidth(2.0);
        StackPane.setAlignment(statusCircle, Pos.BOTTOM_RIGHT);

        profileContainer.getChildren().addAll(profileImage, statusCircle);
        return profileContainer;
    }

    private static StackPane createGroupContainer(String groupImageUrl) {

        StackPane groupContainer = new StackPane();

        ImageView groupImage = new ImageView();
        groupImage.setFitHeight(45.0);
        groupImage.setFitWidth(45.0);
        groupImage.setPreserveRatio(true);
        groupImage.setPickOnBounds(true);

        try {
            groupImage.setImage(
                    (groupImageUrl != null && !groupImageUrl.isEmpty())
                            ? new Image(groupImageUrl, true)
                            : new Image(ConversationItemBuilder.class.getResource("/genki/img/group-default.png").toExternalForm())
            );
        } catch (Exception e) {
            logger.info("Error loading group image: " + e.getMessage());
        }

        Circle groupClip = new Circle(22.5, 22.5, 22.5);
        groupImage.setClip(groupClip);

        groupContainer.getChildren().addAll(groupImage);
        return groupContainer;
    }

    private static VBox createMessageInfo(String contactName, String lastMessage) {
        VBox messageInfo = new VBox();
        messageInfo.setSpacing(4.0);

        Label nameLabel = new Label(contactName);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label messageLabel = new Label(lastMessage);
        messageLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px;");
        messageLabel.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        messageInfo.getChildren().addAll(nameLabel, messageLabel);
        return messageInfo;
    }

    private static VBox createRightInfo(String time, int unreadCount) {
        VBox rightInfo = new VBox();
        rightInfo.setAlignment(Pos.TOP_RIGHT);
        rightInfo.setSpacing(6.0);

        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");

        StackPane badge = new StackPane();
        badge.setStyle("-fx-background-color: #4a5fff; -fx-background-radius: 10;");
        badge.setMinWidth(20);
        badge.setMinHeight(20);

        Label unreadLabel = new Label(String.valueOf(unreadCount));
        unreadLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;");
        badge.getChildren().add(unreadLabel);

        if (unreadCount > 0) {
            rightInfo.getChildren().addAll(timeLabel, badge);
        } else {
            rightInfo.getChildren().add(timeLabel);
        }

        return rightInfo;
    }
}
