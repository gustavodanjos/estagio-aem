package com.adobe.aem.guides.wknd.core.models;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface TeamMember {

    String getName();

    String getRole();

    String getPhoto();

    boolean hasPhoto();

    boolean hasRole();

    default boolean getHasPhoto() {
        return hasPhoto();
    }

    default boolean getHasRole() {
        return hasRole();
    }

    String getInitial();
}
