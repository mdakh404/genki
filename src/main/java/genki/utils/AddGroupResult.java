package genki.utils;

public class AddGroupResult {

     private AddGroupStatus status;

     public AddGroupResult(AddGroupStatus status) {
         this.status = status;
     }

     public AddGroupStatus getResult() {
         return this.status;
     }
}
