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

/**
 * Utility class to dynamically create conversation item UI components
 */
public class ConversationItemBuilder {

    private static final Logger logger = Logger.getLogger(ConversationItemBuilder.class.getName());

    /**
     * Create a conversation item HBox with all necessary styling and data
     * @param profileImageUrl URL or path to the profile image
     * @param contactName Name of the contact
     * @param lastMessage Last message text
     * @param time Time of the last message
     * @param unreadCount Number of unread messages
     * @param isOnline Whether the contact is online
     * @return HBox representing the conversation item
     */
    public static HBox createConversationItem(String profileImageUrl, String contactName, 
                                             String lastMessage, String time, int unreadCount, 
                                             boolean isOnline) {
        
        // Main container HBox
        HBox mainContainer = new HBox();
        mainContainer.setAlignment(Pos.CENTER_LEFT);
        mainContainer.setSpacing(12.0);
        mainContainer.getStyleClass().add("conversation-item");
        
        // Profile image with online indicator
        StackPane profileContainer = createProfileContainer(profileImageUrl, isOnline);
        
        // Contact name and last message
        VBox messageInfo = createMessageInfo(contactName, lastMessage);
        HBox.setHgrow(messageInfo, javafx.scene.layout.Priority.ALWAYS);
        
        // Time and unread badge
        VBox rightInfo = createRightInfo(time, unreadCount);
        
        mainContainer.getChildren().addAll(profileContainer, messageInfo, rightInfo);
        return mainContainer;
    }


    /**
     * Create a conversation item HBox with all necessary styling and data
     * @param profileImageUrl URL or path to the group image
     * @param groupName Name of the group
     * @param lastMessage Last message text
     * @param time Time of the last message
     * @param unreadCount Number of unread messages
     * @return HBox representing the conversation item
     */
    public static HBox createGroupConversationItem(String profileImageUrl, String groupName,
                                              String lastMessage, String time, int unreadCount) {

        // Main container HBox
        HBox mainGroupContainer = new HBox();
        mainGroupContainer.setAlignment(Pos.CENTER_LEFT);
        mainGroupContainer.setSpacing(12.0);
        mainGroupContainer.getStyleClass().add("conversation-item");

        // Profile image with online indicator
        StackPane groupContainer = createGroupContainer(profileImageUrl);

        // Contact name and last message
        VBox messageInfo = createMessageInfo(groupName, lastMessage);
        HBox.setHgrow(messageInfo, javafx.scene.layout.Priority.ALWAYS);

        // Time and unread badge
        VBox rightInfo = createRightInfo(time, unreadCount);

        mainGroupContainer.getChildren().addAll(groupContainer, messageInfo, rightInfo);
        return mainGroupContainer;
    }


    /**
     * Create profile image container with online status indicator
     */
    private static StackPane createProfileContainer(String profileImageUrl, boolean isOnline) {
        StackPane profileContainer = new StackPane();
        
        // Profile image
        ImageView profileImage = new ImageView();
        profileImage.setFitHeight(45.0);
        profileImage.setFitWidth(45.0);
        profileImage.setPreserveRatio(true);
        profileImage.setPickOnBounds(true);
        
        // Load image
        try {
            profileImage.setImage(new Image(profileImageUrl != null ? profileImageUrl : "@../img/user-default.png"));
        } catch (Exception e) {
            System.err.println("Error loading profile image: " + e.getMessage());
        }
        
        // Circular clip
        Circle clip = new Circle(22.5, 22.5, 22.5);
        profileImage.setClip(clip);
        
        // Online status indicator
        Circle statusCircle = new Circle(6.0);
        statusCircle.setFill(isOnline ? Color.web("#4ade80") : Color.web("#9ca3af"));
        statusCircle.setStroke(Color.web("#1e2936"));
        statusCircle.setStrokeWidth(2.0);
        StackPane.setAlignment(statusCircle, Pos.BOTTOM_RIGHT);
        
        profileContainer.getChildren().addAll(profileImage, statusCircle);
        return profileContainer;
    }

    /**
     * Create group image container
     */

    private static StackPane createGroupContainer(String profileImageUrl) {

        StackPane groupContainer = new StackPane();

        // group image
        ImageView groupImage = new ImageView();
        groupImage.setFitHeight(45.0);
        groupImage.setFitWidth(45.0);
        groupImage.setPreserveRatio(true);
        groupImage.setPickOnBounds(true);

        // Load image
        try {
            groupImage.setImage(new Image(profileImageUrl != null ? profileImageUrl : "@../img/group-default.png"));
        } catch (Exception e) {
            logger.info("Error loading group image: " + e.getMessage());
        }

        // Circular clip
        Circle groupClip = new Circle(22.5, 22.5, 22.5);
        groupImage.setClip(groupClip);

        groupContainer.getChildren().addAll(groupImage);
        return groupContainer;
    }
    
    /**
     * Create message info VBox (contact name and last message)
     */
    private static VBox createMessageInfo(String contactName, String lastMessage) {
        VBox messageInfo = new VBox();
        messageInfo.setSpacing(4.0);
        
        // Contact name label
        Label nameLabel = new Label(contactName);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Last message label
        Label messageLabel = new Label(lastMessage);
        messageLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px;");
        messageLabel.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        
        messageInfo.getChildren().addAll(nameLabel, messageLabel);
        return messageInfo;
    }
    
    /**
     * Create right info VBox (time and unread badge)
     */
    private static VBox createRightInfo(String time, int unreadCount) {
        VBox rightInfo = new VBox();
        rightInfo.setAlignment(Pos.TOP_RIGHT);
        rightInfo.setSpacing(6.0);
        
        // Time label
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
        
        // Unread badge
        StackPane badge = new StackPane();
        badge.setStyle("-fx-background-color: #4a5fff; -fx-background-radius: 10;");
        badge.setMinWidth(20);
        badge.setMinHeight(20);
        
        Label unreadLabel = new Label(String.valueOf(unreadCount));
        unreadLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;");
        badge.getChildren().add(unreadLabel);
        
        // Only show badge if there are unread messages
        if (unreadCount > 0) {
            rightInfo.getChildren().addAll(timeLabel, badge);
        } else {
            rightInfo.getChildren().add(timeLabel);
        }
        
        return rightInfo;
    }
}
