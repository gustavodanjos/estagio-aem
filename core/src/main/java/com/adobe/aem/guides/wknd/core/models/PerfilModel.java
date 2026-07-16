package com.adobe.aem.guides.wknd.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PerfilModel {

    @ValueMapValue
    private String fullName;

    @ValueMapValue
    private String jobTitle;

    @ValueMapValue
    private String bio;

    public String getFullName() {
        return fullName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getBio() {
        return bio;
    }
}
