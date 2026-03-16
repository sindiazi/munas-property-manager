package com.example.rentalmanager.user.domain.aggregate;

import com.example.rentalmanager.shared.domain.AggregateRoot;
import com.example.rentalmanager.user.domain.event.UserRegisteredEvent;
import com.example.rentalmanager.user.domain.event.UserRoleChangedEvent;
import com.example.rentalmanager.user.domain.valueobject.UserId;
import com.example.rentalmanager.user.domain.valueobject.UserRole;

import java.time.Instant;
import java.util.UUID;

public class User extends AggregateRoot<UserId> {

    private final UserId id;
    private final String username;
    private String email;
    private String passwordHash;
    private UserRole role;
    private boolean active;
    private final Instant createdAt;

    @Override public UserId  getId()           { return id; }
    public String            getUsername()     { return username; }
    public String            getEmail()        { return email; }
    public String            getPasswordHash() { return passwordHash; }
    public UserRole          getRole()         { return role; }
    public boolean           isActive()        { return active; }
    public Instant           getCreatedAt()    { return createdAt; }

    /** Reconstitution constructor. */
    public User(UserId id, String username, String email, String passwordHash,
                UserRole role, boolean active, Instant createdAt) {
        this.id           = id;
        this.username     = username;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.active       = active;
        this.createdAt    = createdAt;
    }

    // ── Factory ────────────────────────────────────────────────────────────

    public static User create(String username, String email, String passwordHash, UserRole role) {
        var id   = UserId.generate();
        var user = new User(id, username, email, passwordHash, role, true, Instant.now());
        user.registerEvent(new UserRegisteredEvent(UUID.randomUUID(), Instant.now(), id, username, role));
        return user;
    }

    // ── Behaviour ──────────────────────────────────────────────────────────

    public void changeRole(UserRole newRole) {
        var previous = this.role;
        this.role = newRole;
        registerEvent(new UserRoleChangedEvent(UUID.randomUUID(), Instant.now(), id, previous, newRole));
    }

    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}
