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


    public Group(String groupId, String groupName, String description, boolean isPublic,
                 String profilePictureUrl, String groupAdmin) {

        this.groupId= groupId;
           this.groupName = groupName;
           this.description = description;
           this.isPublic = isPublic;
           this.profilePictureUrl = profilePictureUrl;
           this.groupAdmin = groupAdmin;
    }

    public String getGroupId() { return this.groupId; }

    public String getGroupName() { return this.groupName; }

    public String getDescription() { return this.description;}

    public boolean isPublic() { return this.isPublic; }

    public String getGroupProfilePicture() { return this.profilePictureUrl; }

    public String getGroupAdmin() { return this.groupAdmin; }

    public ArrayList<String> getListUsers() { return this.listUsers; }

    public void addUser(String username) {
        listUsers.add(username);
    }




}
