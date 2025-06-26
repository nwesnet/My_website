package nwes.mywebsite;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "links")
public class Link {
    @Id
    private String id;

    private String resource;
    private String link;
    @Column(name = "date_added")
    private LocalDateTime dateAdded;
    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    private String deleted;
    private String sync;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String ownerUsername;
    // Getters and Setters
    // User
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    // Owner username
    public String getOwnerUsername() {
        return ownerUsername;
    }
    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }
    // IdForItems
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    // Resource
    public String getResource() {
        return resource;
    }
    public void setResource(String resource) {
        this.resource = resource;
    }
    // Link url
    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }
    // Date added
    public LocalDateTime getDateAdded() {
        return dateAdded;
    }
    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded = dateAdded;
    }
    // Last modified date
    public LocalDateTime getLastModified() {
        return lastModified;
    }
    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
    // Deleted status
    public String getDeleted() {
        return deleted;
    }
    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }
    // Sync status
    public String getSync() {
        return sync;
    }
    public void setSync(String sync) {
        this.sync = sync;
    }


}
