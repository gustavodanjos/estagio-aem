package com.adobe.aem.guides.wknd.core.servlets;

import com.adobe.aem.guides.wknd.core.models.UltimasDoMagazineModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collections;

@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes = "wknd/components/ultimas-do-magazine",
        selectors = "ultimas",
        methods = HttpConstants.METHOD_GET,
        extensions = "json"
)
@ServiceDescription("Servlet Bônus para Últimas do Magazine JSON")
public class UltimasDoMagazineServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(UltimasDoMagazineServlet.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected void doGet(final SlingHttpServletRequest req, final SlingHttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            UltimasDoMagazineModel model = req.adaptTo(UltimasDoMagazineModel.class);
            if (model != null && model.getArticles() != null) {
                OBJECT_MAPPER.writeValue(resp.getWriter(), model.getArticles());
            } else {
                OBJECT_MAPPER.writeValue(resp.getWriter(), Collections.emptyList());
            }
        } catch (Exception e) {
            LOG.error("Erro ao serializar resposta JSON em UltimasDoMagazineServlet", e);
            resp.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            OBJECT_MAPPER.writeValue(resp.getWriter(), Collections.singletonMap("error", "Erro ao obter artigos do Magazine"));
        }
    }
}
