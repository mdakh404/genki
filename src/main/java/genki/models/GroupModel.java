package genki.models;

import com.mongodb.client.model.Updates;
import genki.utils.DBConnection;
import genki.utils.UserSession;
import genki.utils.AddGroupResult;
import genki.utils.AddGroupStatus;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.model.Filters;
import com.mongodb.MongoException;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.List;

public class GroupModel {

       private static final DBConnection groupModelDBConnection = DBConnection.getInstance("genki_testing");
       private static final Logger logger = Logger.getLogger(GroupModel.class.getName());

    /**
     * Méthode originale maintenue pour compatibilité (sans photo)
     */
    public static AddGroupResult addGroup(String groupName, String groupDescription, 
                                          boolean isPublic, String groupAdmin) {
        // Appeler la nouvelle méthode avec une photo par défaut
        return addGroupWithPhoto(groupName, groupDescription, isPublic, groupAdmin, 
                                "genki/img/group-default.png");
    }


    public static AddGroupResult addGroupWithPhoto(String groupName, String groupDescription, 
                                                   boolean isPublic, String groupAdmin, 
                                                   String photoUrl) {
        logger.log(Level.INFO, "Adding group " + groupName + " with photo: " + photoUrl);

        if (groupAdmin == null) {
            logger.log(Level.WARNING, "UserSession was revoked.");
            return new AddGroupResult(AddGroupStatus.GROUP_ADD_FAILURE);
        }

        // Valider le chemin de la photo
        if (photoUrl == null || photoUrl.trim().isEmpty()) {
            photoUrl = "genki/img/groupe_picture.jpg";
            logger.log(Level.INFO, "No photo provided, using default");
        }

        try {
            MongoCollection<Document> groupsCollection = groupModelDBConnection.getCollection("groups");
            MongoCollection<Document> usersCollection = groupModelDBConnection.getCollection("users");

            // Récupérer le document de l'admin
            Document adminUserDoc = usersCollection.find(
                Filters.eq("username", groupAdmin)
            ).first();

            if (adminUserDoc == null) {
                logger.log(Level.WARNING, "Admin user not found: " + groupAdmin);
                return new AddGroupResult(AddGroupStatus.GROUP_ADD_FAILURE);
            }

            // Créer la liste des membres (commence avec l'admin)
            List<String> usersIDs = new ArrayList<>();
            usersIDs.add(adminUserDoc.getObjectId("_id").toHexString());

            // Créer le document du groupe avec le champ photo_url
            Document newGroupDoc = new Document("group_name", groupName)
                .append("description", groupDescription)
                .append("is_public", isPublic)
                .append("profile_picture", photoUrl)  // STOCKAGE DE LA PHOTO
                .append("group_admin", groupAdmin)
                .append("users", usersIDs)
                .append("created_at", new java.util.Date());  // Date de création

            // Insérer le groupe dans la base de données
            InsertOneResult insertGroupResult = groupsCollection.insertOne(newGroupDoc);

            if (insertGroupResult.wasAcknowledged()) {
                logger.log(Level.INFO, "Successfully added group " + groupName);

                // Récupérer l'ID du nouveau groupe
                ObjectId nvGroupId = newGroupDoc.getObjectId("_id");
                
                // Créer l'objet Group Java
                Group nvGroup = new Group(
                    nvGroupId.toHexString(),
                    groupName,
                    groupDescription,
                    isPublic,
                    photoUrl,  // PHOTO URL
                    groupAdmin
                );
                
                // Ajouter tous les membres (pour l'instant juste l'admin)
                for (String userId : usersIDs) {
                    nvGroup.addUser(userId);
                }

                // Ajouter le groupe à la liste des groupes de l'admin
                usersCollection.updateOne(
                    Filters.eq("_id", adminUserDoc.getObjectId("_id")),
                    Updates.addToSet("groups", nvGroupId.toHexString())
                );

                // Ajouter le groupe à la session utilisateur
                UserSession.addGroup(nvGroup);
                
                logger.log(Level.INFO, "Group added to UserSession: " + nvGroup);
                
                return new AddGroupResult(AddGroupStatus.GROUP_ADD_SUCCESS);
            } else {
                logger.log(Level.WARNING, "Failed to add group " + groupName);
                return new AddGroupResult(AddGroupStatus.GROUP_ADD_FAILURE);
            }

        } catch (MongoException ex) {
            logger.log(Level.WARNING, "DB Error while adding group", ex);
            return new AddGroupResult(AddGroupStatus.DB_ERROR);
        } catch (NullPointerException ex) {
            logger.log(Level.WARNING, "Null pointer exception", ex);
            return new AddGroupResult(AddGroupStatus.DB_ERROR);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Unexpected error while adding group", ex);
            return new AddGroupResult(AddGroupStatus.GROUP_ADD_FAILURE);
        }
    }

    /**
     * 🔥 NOUVELLE MÉTHODE : Mettre à jour la photo d'un groupe existant
     * 
     * @param groupId ID du groupe
     * @param newPhotoUrl Nouveau chemin de la photo
     * @return true si la mise à jour réussit, false sinon
     */
    public static boolean updateGroupPhoto(String groupId, String newPhotoUrl) {
        logger.log(Level.INFO, "Updating photo for group: " + groupId);

        if (newPhotoUrl == null || newPhotoUrl.trim().isEmpty()) {
            logger.log(Level.WARNING, "Invalid photo URL provided");
            return false;
        }

        try {
            MongoCollection<Document> groupsCollection = groupModelDBConnection.getCollection("groups");

            // Mettre à jour le champ profile_picture
            var updateResult = groupsCollection.updateOne(
                Filters.eq("_id", new ObjectId(groupId)),
                Updates.set("profile_picture", newPhotoUrl)
            );

            if (updateResult.getModifiedCount() > 0) {
                logger.log(Level.INFO, "Successfully updated group photo");
                
                // Mettre à jour dans UserSession si le groupe est chargé
                ArrayList<Group> userGroups = UserSession.getGroups();
                if (userGroups != null) {
                    for (Group group : userGroups) {
                        if (group.getGroupId().equals(groupId)) {
                            group.setProfilePictureUrl(newPhotoUrl);
                            logger.log(Level.INFO, "Updated photo in UserSession");
                            break;
                        }
                    }
                }
                
                return true;
            } else {
                logger.log(Level.WARNING, "No group found with ID: " + groupId);
                return false;
            }

        } catch (MongoException ex) {
            logger.log(Level.WARNING, "DB Error while updating group photo", ex);
            return false;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Unexpected error while updating group photo", ex);
            return false;
        }
    }

    /**
     * NOUVELLE MÉTHODE : Récupérer un groupe par son ID
     * 
     * @param groupId ID du groupe
     * @return Objet Group ou null si non trouvé
     */
    public static Group getGroupById(String groupId) {
        try {
            MongoCollection<Document> groupsCollection = groupModelDBConnection.getCollection("groups");

            Document groupDoc = groupsCollection.find(
                Filters.eq("_id", new ObjectId(groupId))
            ).first();

            if (groupDoc != null) {
                Group group = new Group(
                    groupDoc.getObjectId("_id").toHexString(),
                    groupDoc.getString("group_name"),
                    groupDoc.getString("description"),
                    groupDoc.getBoolean("is_public", true),
                    groupDoc.getString("profile_picture"),  // 🔥 RÉCUPÉRATION DE LA PHOTO
                    groupDoc.getString("group_admin")
                );

                // Charger les membres
                List<String> users = groupDoc.getList("users", String.class);
                if (users != null) {
                    for (String userId : users) {
                        group.addUser(userId);
                    }
                }

                return group;
            }

            return null;

        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error fetching group by ID", ex);
            return null;
        }
    }

    /**
     * Récupérer tous les noms de groupes
     */
    public ArrayList<String> getGroupNames() {
        ArrayList<String> groupNames = new ArrayList<>();

        try {
            MongoCollection<Document> groupsCollection = groupModelDBConnection.getCollection("groups");

            groupsCollection.find().forEach(groupDoc -> {
                groupNames.add(groupDoc.getString("group_name"));
            });

            return groupNames;

        } catch (MongoException ex) {
            logger.warning("DB Error while getting group names: " + ex);
            return new ArrayList<>();
        }
    }

    /**
     *  NOUVELLE MÉTHODE : Récupérer tous les groupes publics avec leurs photos
     */
    public static ArrayList<Group> getAllPublicGroups() {
        ArrayList<Group> publicGroups = new ArrayList<>();

        try {
            MongoCollection<Document> groupsCollection = groupModelDBConnection.getCollection("groups");

            groupsCollection.find(Filters.eq("is_public", true)).forEach(groupDoc -> {
                Group group = new Group(
                    groupDoc.getObjectId("_id").toHexString(),
                    groupDoc.getString("group_name"),
                    groupDoc.getString("description"),
                    groupDoc.getBoolean("is_public", true),
                    groupDoc.getString("profile_picture"),
                    groupDoc.getString("group_admin")
                );

                List<String> users = groupDoc.getList("users", String.class);
                if (users != null) {
                    for (String userId : users) {
                        group.addUser(userId);
                    }
                }

                publicGroups.add(group);
            });

            logger.log(Level.INFO, "Loaded " + publicGroups.size() + " public groups");
            return publicGroups;

        } catch (MongoException ex) {
            logger.log(Level.WARNING, "DB Error while getting public groups", ex);
            return new ArrayList<>();
        }
    }
}