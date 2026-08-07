package com.adobe.aem.guides.wknd.core.models;

import com.adobe.aem.guides.wknd.core.models.dto.OmnichannelCard;
import com.adobe.cq.export.json.ComponentExporter;
import org.osgi.annotation.versioning.ProviderType;

import java.util.List;

@ProviderType
public interface UltimasDoMagazineModel extends ComponentExporter {

    List<OmnichannelCard> getArticles();

    int getMaxItems();

    int getColumns();

    boolean isEmpty();
}
