package nwes.mywebsite;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
public class Card {
    @Id
    private String id;



    private String resource;
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private String ownerName;
    private String cardPin;
    private String cardNetwork;
    private String cardType;
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
    // Card number
    public String getCardNumber() {
        return cardNumber;
    }
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    // Expiry date
    public String getExpiryDate() {
        return expiryDate;
    }
    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
    // CVV
    public String getCvv() {
        return cvv;
    }
    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
    // Owner name
    public String getOwnerName() {
        return ownerName;
    }
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    // Pin code
    public String getCardPin() {
        return cardPin;
    }
    public void setCardPin(String cardPin) {
        this.cardPin = cardPin;
    }
    // Card network type
    public String getCardNetwork() {
        return cardNetwork;
    }
    public void setCardNetwork(String cardNetwork) {
        this.cardNetwork = cardNetwork;
    }
    // Card type
    public String getCardType() {
        return cardType;
    }
    public void setCardType(String cardType) {
        this.cardType = cardType;
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

