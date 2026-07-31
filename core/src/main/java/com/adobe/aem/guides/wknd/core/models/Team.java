package com.adobe.aem.guides.wknd.core.models;

import org.osgi.annotation.versioning.ProviderType;
import java.util.List;

@ProviderType
public interface Team {

    String getTitle();

    List<TeamMember> getMembers();

    boolean isEmpty();
}
