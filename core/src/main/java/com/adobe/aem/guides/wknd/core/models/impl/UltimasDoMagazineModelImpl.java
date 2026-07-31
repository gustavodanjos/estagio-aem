package com.adobe.aem.guides.wknd.core.models.impl;

import com.adobe.aem.guides.wknd.core.models.UltimasDoMagazineModel;
import com.adobe.aem.guides.wknd.core.models.dto.MagazineArticle;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.jcr.Session;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implementação do Sling Model do componente Últimas do Magazine.
 * Suporta exportação para JSON nativo via AEM Exporter (Jackson).
 */
@Model(
        adaptables = { SlingHttpServletRequest.class, Resource.class },
        adapters = { UltimasDoMagazineModel.class, ComponentExporter.class },
        resourceType = UltimasDoMagazineModelImpl.RESOURCE_TYPE,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@Exporter(
        name = ExporterConstants.SLING_MODEL_EXPORTER_NAME,
        extensions = ExporterConstants.SLING_MODEL_EXTENSION
)
public class UltimasDoMagazineModelImpl implements UltimasDoMagazineModel {

    private static final Logger LOG = LoggerFactory.getLogger(UltimasDoMagazineModelImpl.class);

    public static final String RESOURCE_TYPE = "wknd/components/ultimas-do-magazine";
    private static final String DEFAULT_ROOT_PATH = "/content/wknd/us/en";

    @OSGiService
    private QueryBuilder queryBuilder;

    @SlingObject
    private ResourceResolver resourceResolver;

    @ValueMapValue
    private String rootPath;

    @ValueMapValue
    @Default(intValues = 4)
    private int maxItems;

    @ValueMapValue
    @Default(intValues = 4)
    private int columns;

    private List<MagazineArticle> articles = new ArrayList<>();

    @PostConstruct
    protected void init() {
        LOG.debug("Iniciando carregamento do UltimasDoMagazineModelImpl (maxItems = {})", maxItems);
        if (queryBuilder == null || resourceResolver == null) {
            LOG.warn("QueryBuilder ou ResourceResolver indisponíveis. Lista de artigos ficará vazia.");
            return;
        }

        PageManager pm = getPageManagerSafe();
        if (pm == null) {
            LOG.warn("PageManager indisponível.");
            return;
        }

        Session session = resourceResolver.adaptTo(Session.class);
        if (session == null) {
            LOG.warn("Sessão JCR não pôde ser obtida do ResourceResolver.");
            return;
        }

        Map<String, String> predicateMap = createPredicateMap();
        Query query = queryBuilder.createQuery(PredicateGroup.create(predicateMap), session);
        SearchResult result = query.getResult();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        for (Hit hit : result.getHits()) {
            try {
                Resource hitResource = hit.getResource();
                if (hitResource != null) {
                    Page page = pm.getContainingPage(hitResource);
                    if (page != null) {
                        String title = getArticleTitle(page);
                        String path = page.getPath() + ".html";
                        String imagePath = extractImagePath(page);
                        String description = isBlank(page.getDescription())
                                ? "Artigo recente do Magazine WKND."
                                : page.getDescription();

                        String lastModifiedStr = "";
                        Calendar lastMod = page.getLastModified();
                        if (lastMod != null) {
                            lastModifiedStr = dateFormat.format(lastMod.getTime());
                        }

                        MagazineArticle article = new MagazineArticle(title, path, imagePath, description, lastModifiedStr);
                        articles.add(article);
                    }
                }
            } catch (Exception e) {
                LOG.error("Erro ao processar hit da query de Últimas do Magazine", e);
            }
        }
    }

    private PageManager getPageManagerSafe() {
        if (resourceResolver != null) {
            return resourceResolver.adaptTo(PageManager.class);
        }
        return null;
    }

    private Map<String, String> createPredicateMap() {
        String searchPath = isBlank(rootPath) ? DEFAULT_ROOT_PATH : rootPath.trim();
        Map<String, String> map = new HashMap<>();
        map.put("path", searchPath);
        map.put("path.self", "false");
        map.put("type", "cq:Page");
        map.put("orderby", "@jcr:content/cq:lastModified");
        map.put("orderby.sort", "desc");
        map.put("p.limit", String.valueOf(maxItems > 0 ? maxItems : 4));
        return map;
    }

    private String getArticleTitle(Page page) {
        String title = page.getNavigationTitle();
        if (isBlank(title)) {
            title = page.getTitle();
        }
        if (isBlank(title)) {
            title = page.getName();
        }
        return title;
    }

    private String extractImagePath(Page page) {
        Resource contentRes = page.getContentResource();
        if (contentRes == null) {
            return "";
        }

        // 1. Verificar subnó "image" ou "cq:featuredImage"
        String img = getFileReferenceFromChild(contentRes, "image");
        if (!isBlank(img)) {
            return img;
        }
        img = getFileReferenceFromChild(contentRes, "cq:featuredImage");
        if (!isBlank(img)) {
            return img;
        }

        // 2. Buscar recursivamente por qualquer propriedade fileReference dentro da página
        img = findFirstFileReference(contentRes);
        if (!isBlank(img)) {
            return img;
        }

        return "";
    }

    private String getFileReferenceFromChild(Resource parent, String childName) {
        Resource child = parent.getChild(childName);
        if (child != null) {
            ValueMap vm = child.getValueMap();
            String fileRef = vm.get("fileReference", String.class);
            if (!isBlank(fileRef)) {
                return fileRef;
            }
        }
        return null;
    }

    private String findFirstFileReference(Resource resource) {
        ValueMap vm = resource.getValueMap();
        String fileRef = vm.get("fileReference", String.class);
        if (!isBlank(fileRef)) {
            return fileRef;
        }
        Iterator<Resource> children = resource.listChildren();
        while (children.hasNext()) {
            Resource child = children.next();
            String found = findFirstFileReference(child);
            if (!isBlank(found)) {
                return found;
            }
        }
        return null;
    }

    /**
     * Substitui StringUtils.isBlank(String) do commons-lang3,
     * evitando dependência externa não resolvida no OSGi.
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    public List<MagazineArticle> getArticles() {
        return Collections.unmodifiableList(articles);
    }

    @Override
    public int getMaxItems() {
        return maxItems;
    }

    @Override
    public int getColumns() {
        return (columns >= 1 && columns <= 6) ? columns : 4;
    }

    @Override
    public boolean isEmpty() {
        return articles.isEmpty();
    }

    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}