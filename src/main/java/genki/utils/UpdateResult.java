package genki.utils;

public class UpdateResult {

    private final UpdateStatus status;

    public UpdateResult(UpdateStatus status) {
        this.status = status;
    }

    public UpdateStatus getStatus() {
        return this.status;
    }
}
