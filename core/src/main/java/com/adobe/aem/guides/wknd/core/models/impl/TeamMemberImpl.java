package com.adobe.aem.guides.wknd.core.models.impl;

import com.adobe.aem.guides.wknd.core.models.TeamMember;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
    adaptables = Resource.class,
    adapters = TeamMember.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TeamMemberImpl implements TeamMember {

    @ValueMapValue(name = "name")
    @Default(values = "Membro da Equipe")
    private String name;

    @ValueMapValue(name = "role")
    @Default(values = "")
    private String role;

    @ValueMapValue(name = "photo")
    @Default(values = "")
    private String photo;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getRole() {
        return role;
    }

    @Override
    public String getPhoto() {
        return photo;
    }

    @Override
    public boolean hasPhoto() {
        return isNotBlank(photo);
    }

    @Override
    public boolean hasRole() {
        return isNotBlank(role);
    }

    @Override
    public boolean getHasPhoto() {
        return hasPhoto();
    }

    @Override
    public boolean getHasRole() {
        return hasRole();
    }

    @Override
    public String getInitial() {
        return isNotBlank(name) ? name.substring(0, 1).toUpperCase() : "W";
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
