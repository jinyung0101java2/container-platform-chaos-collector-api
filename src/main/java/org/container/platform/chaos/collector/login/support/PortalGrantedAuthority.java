package org.container.platform.chaos.collector.login.support;

import org.container.platform.chaos.collector.common.Constants;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

/**
 * PortalGrantedAuthority 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 */
public class PortalGrantedAuthority implements GrantedAuthority {
    private static final long serialVersionUID = 21301223123L;
    private final String id;
    private final String type;
    private final String role;
    private final String token;
    private final String url;
    private final String parentId;

    public PortalGrantedAuthority(String id) {
        Assert.hasText(id, "A granted authority id is required");
        this.id = id;
        this.type = Constants.ContextType.CLUSTER.name();
        this.role = Constants.EMPTY_STRING;
        this.token = Constants.EMPTY_STRING;
        this.url = Constants.EMPTY_STRING;
        this.parentId = Constants.EMPTY_STRING;
    }
    public PortalGrantedAuthority(String id, String type, String role) {
        Assert.hasText(type, "A granted authority type is required");
        Assert.hasText(id, "A granted authority id is required");
        Assert.hasText(role, "A granted authority role is required");
        this.id = id;
        this.type = type;
        this.role = role;
        this.token = Constants.EMPTY_STRING;
        this.url = Constants.EMPTY_STRING;
        this.parentId = Constants.EMPTY_STRING;
    }
    public String getAuthority() {
        return this.role;
    }

    public String getToken() {
        return this.token;
    }

    public String geturl() {
        return this.url;
    }

    public boolean equals(String id, String type) {
        return this.id.equals(id) && this.type.equals(type);
    }

    public boolean equals(String id, String parentId, String type) {
        return this.id.equals(id) && this.parentId.equals(parentId) && this.type.equals(type);
    }

    public int hashCode() {
        return this.role.hashCode();
    }

    public String toString() {
        return "id: " + this.id
                + ", role: " + this.role
                + ", type: " + this.type
                + ", token: " + this.token
                + ", url: " + this.url
                + ", parentId: " + this.parentId ;
    }
}
