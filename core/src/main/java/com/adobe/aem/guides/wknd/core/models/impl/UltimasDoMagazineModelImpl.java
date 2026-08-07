package com.adobe.aem.guides.wknd.core.models.impl;

import com.adobe.aem.guides.wknd.core.models.UltimasDoMagazineModel;
import com.adobe.aem.guides.wknd.core.models.dto.MagazineArticle;
import com.adobe.aem.guides.wknd.core.models.dto.OmnichannelCard;
import com.adobe.aem.guides.wknd.core.models.dto.AdventureCard;
import com.adobe.cq.dam.cfm.ContentFragment;
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

@Model(
        adaptables = { SlingHttpServletRequest.class, Resource.class },
        adapters = { UltimasDoMagazineModel.class, ComponentExporter.class },
        resourceType = UltimasDoMagazineModelImpl.RESOURCE_TYPE,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@Exporter(
        name = ExporterConstants.SLING_MODEL_EXPORTER_NAME,
        extensions = ExporterConstants.SLING_MODEL_EXTENSION,
        selector = "ultimas"
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

    @ValueMapValue
    @Default(values = "/content/dam/wknd")
    private String adventuresPath;

    @ValueMapValue
    @Default(intValues = 2)
    private int maxAdventures;

    private List<OmnichannelCard> articles = new ArrayList<>();

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

        // Buscar Aventuras (Content Fragments)
        if (!isBlank(adventuresPath) && maxAdventures > 0) {
            Map<String, String> advPredicateMap = createAdventurePredicateMap();
            Query advQuery = queryBuilder.createQuery(PredicateGroup.create(advPredicateMap), session);
            SearchResult advResult = advQuery.getResult();
            for (Hit hit : advResult.getHits()) {
                try {
                    Resource hitResource = hit.getResource();
                    if (hitResource != null) {
                        // Resiliência: buscar diretamente no nó master caso a API ContentFragment falhe
                        Resource masterNode = hitResource.getChild("jcr:content/data/master");
                        if (masterNode != null) {
                            ValueMap vm = masterNode.getValueMap();
                            String title = vm.get("titulo", String.class);
                            if (isBlank(title)) title = hitResource.getName();
                            
                            String description = vm.get("descricao", String.class);
                            if (description == null) description = "";
                            
                            String difficulty = vm.get("dificuldade", String.class);
                            if (difficulty == null) difficulty = "";
                            
                            Double price = 0.0;
                            Object priceObj = vm.get("preco");
                            if (priceObj != null) {
                                try {
                                    price = Double.parseDouble(priceObj.toString());
                                } catch (NumberFormatException e) {
                                    // ignore
                                }
                            }
                            
                            String imagePath = vm.get("imagem", String.class);
                            if (imagePath == null) imagePath = "";
                            
                            String path = "#"; // Não tenta abrir Content Fragment como página html
                            
                            String guideName = "Guia WKND";
                            String instructorPath = vm.get("instrutor", String.class);
                            if (instructorPath != null && resourceResolver != null) {
                                Resource guideResource = resourceResolver.getResource(instructorPath);
                                if (guideResource != null) {
                                    Resource guideMaster = guideResource.getChild("jcr:content/data/master");
                                    if (guideMaster != null) {
                                        String nome = guideMaster.getValueMap().get("nome", String.class);
                                        if (nome != null && !nome.isEmpty()) guideName = nome;
                                    }
                                }
                            }
                            
                            // Obter ultima modificacao do asset
                            String lastModifiedStr = "";
                            Resource jcrContent = hitResource.getChild("jcr:content");
                            if (jcrContent != null) {
                                Calendar lastMod = jcrContent.getValueMap().get("jcr:lastModified", Calendar.class);
                                if (lastMod != null) {
                                    lastModifiedStr = new SimpleDateFormat("dd/MM/yyyy").format(lastMod.getTime());
                                }
                            }
                            
                            AdventureCard adv = new AdventureCard(title, path, imagePath, description, lastModifiedStr, price, difficulty, guideName);
                            articles.add(adv);
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Erro ao processar hit de Aventura", e);
                }
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

    private Map<String, String> createAdventurePredicateMap() {
        String searchPath = isBlank(adventuresPath) ? "/content/dam/wknd" : adventuresPath.trim();
        Map<String, String> map = new HashMap<>();
        map.put("path", searchPath);
        map.put("type", "dam:Asset");
        map.put("property", "jcr:content/contentFragment");
        map.put("property.operation", "exists");
        map.put("orderby", "@jcr:content/jcr:lastModified");
        map.put("orderby.sort", "desc");
        map.put("p.limit", String.valueOf(maxAdventures > 0 ? maxAdventures : 2));
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

        String img = getFileReferenceFromChild(contentRes, "image");
        if (!isBlank(img)) {
            return img;
        }
        img = getFileReferenceFromChild(contentRes, "cq:featuredImage");
        if (!isBlank(img)) {
            return img;
        }

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


    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    public List<OmnichannelCard> getArticles() {
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