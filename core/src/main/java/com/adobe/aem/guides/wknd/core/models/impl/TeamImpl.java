package com.adobe.aem.guides.wknd.core.models.impl;

import com.adobe.aem.guides.wknd.core.models.Team;
import com.adobe.aem.guides.wknd.core.models.TeamMember;
import com.adobe.aem.guides.wknd.core.services.ExibicaoEquipeService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Model(
    adaptables = {SlingHttpServletRequest.class, Resource.class},
    adapters = Team.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TeamImpl implements Team {

    private static final Logger LOG = LoggerFactory.getLogger(TeamImpl.class);

    @ValueMapValue(name = "title")
    @Default(values = "Nossa Equipe")
    private String title;

    @ChildResource(name = "members")
    private List<TeamMember> members;

    @OSGiService
    private ExibicaoEquipeService exibicaoEquipeService;

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public List<TeamMember> getMembers() {
        if (members == null || members.isEmpty()) {
            return Collections.emptyList();
        }

        int limite = exibicaoEquipeService != null ? exibicaoEquipeService.getMaxMembros() : 3;
        LOG.debug("Total de membros no JCR: {}, Limite configurado via OSGi: {}", members.size(), limite);

        if (limite <= 0 || limite >= members.size()) {
            return new ArrayList<>(members);
        }

        return new ArrayList<>(members.subList(0, limite));
    }

    @Override
    public boolean isEmpty() {
        return getMembers().isEmpty();
    }
}
