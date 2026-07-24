package com.adobe.aem.guides.wknd.core.models.impl;

import com.adobe.aem.guides.wknd.core.models.Destaque;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
        adaptables = Resource.class,
        adapters = Destaque.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class DestaqueImpl implements Destaque {

    @ValueMapValue
    private String title;

    @ValueMapValue
    private String text;

    @ValueMapValue
    private String buttonText;

    @ValueMapValue
    private String buttonUrl;

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getButtonText() {
        return buttonText;
    }

    @Override
    public String getButtonUrl() {
        return buttonUrl;
    }

    @Override
    public boolean isMostrarBotao() {
        return buttonUrl != null && !buttonUrl.trim().isEmpty();
    }
}
