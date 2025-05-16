package nwes.mywebsite;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("user_settings")
public class UserSettings {
    @Id
    private String id;

    private String username;
    private boolean doubleConfirmation;
    private boolean storeLogs;
    private String theme;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isDoubleConfirmation() {
        return doubleConfirmation;
    }

    public void setDoubleConfirmation(boolean doubleConfirmation) {
        this.doubleConfirmation = doubleConfirmation;
    }

    public boolean isStoreLogs() {
        return storeLogs;
    }

    public void setStoreLogs(boolean storeLogs) {
        this.storeLogs = storeLogs;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
