package genki.models;

import java.util.ArrayList;

public class Group {

    private String groupId;
    private String groupName;
    private String description;
    private boolean isPublic;
    private String profilePictureUrl;
    private String groupAdmin;
    private ArrayList<String> listUsers = new ArrayList<>();

    // Constructeur principal
    public Group(String groupId, String groupName, String description, boolean isPublic,
                 String profilePictureUrl, String groupAdmin) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.description = description;
        this.isPublic = isPublic;
        this.profilePictureUrl = profilePictureUrl;
        this.groupAdmin = groupAdmin;
    }

    // Constructeur par défaut (utile pour la création progressive)
    public Group() {
        this.listUsers = new ArrayList<>();
    }

    // Getters
    public String getGroupId() { 
        return this.groupId; 
    }

    public String getGroupName() { 
        return this.groupName; 
    }

    public String getDescription() { 
        return this.description;
    }

    public boolean isPublic() { 
        return this.isPublic; 
    }

    public String getGroupProfilePicture() { 
        return this.profilePictureUrl; 
    }

    public String getGroupAdmin() { 
        return this.groupAdmin; 
    }

    public ArrayList<String> getListUsers() { 
        return this.listUsers; 
    }

    // Setters (ajoutés pour plus de flexibilité)
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public void setGroupAdmin(String groupAdmin) {
        this.groupAdmin = groupAdmin;
    }

    public void setListUsers(ArrayList<String> listUsers) {
        this.listUsers = listUsers != null ? listUsers : new ArrayList<>();
    }

    // Méthodes utilitaires
    public void addUser(String userId) {
        if (userId != null && !listUsers.contains(userId)) {
            listUsers.add(userId);
        }
    }

    public void removeUser(String userId) {
        listUsers.remove(userId);
    }

    public boolean hasUser(String userId) {
        return listUsers.contains(userId);
    }

    public int getUserCount() {
        return listUsers.size();
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupId='" + groupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", description='" + description + '\'' +
                ", isPublic=" + isPublic +
                ", profilePictureUrl='" + profilePictureUrl + '\'' +
                ", groupAdmin='" + groupAdmin + '\'' +
                ", memberCount=" + listUsers.size() +
                '}';
    }
}