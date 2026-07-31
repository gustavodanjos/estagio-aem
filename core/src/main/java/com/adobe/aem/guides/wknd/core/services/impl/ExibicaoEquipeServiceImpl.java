package com.adobe.aem.guides.wknd.core.services.impl;

import com.adobe.aem.guides.wknd.core.services.ExibicaoEquipeService;
import com.adobe.aem.guides.wknd.core.services.ExibicaoEquipeServiceConfiguration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = ExibicaoEquipeService.class, immediate = true)
@Designate(ocd = ExibicaoEquipeServiceConfiguration.class)
public class ExibicaoEquipeServiceImpl implements ExibicaoEquipeService {

    private static final Logger LOG = LoggerFactory.getLogger(ExibicaoEquipeServiceImpl.class);

    private int maxMembros;

    @Activate
    @Modified
    protected void activate(final ExibicaoEquipeServiceConfiguration config) {
        this.maxMembros = config.maxMembros();
        LOG.info("ExibicaoEquipeServiceImpl configurado com maxMembros = {}", this.maxMembros);
    }

    @Override
    public int getMaxMembros() {
        return maxMembros;
    }
}
