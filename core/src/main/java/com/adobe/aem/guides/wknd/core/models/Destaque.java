package com.adobe.aem.guides.wknd.core.models;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface Destaque {

    String getTitle();

    String getText();

    String getButtonText();

    String getButtonUrl();

    boolean isMostrarBotao();

    String getAlignment();
}
