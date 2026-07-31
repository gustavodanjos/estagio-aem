package com.adobe.aem.guides.wknd.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "WKND - Serviço de Exibição da Equipe",
    description = "Configuração do serviço OSGi responsável por controlar a quantidade de membros da equipe exibidos no componente."
)
public @interface ExibicaoEquipeServiceConfiguration {

    @AttributeDefinition(
        name = "Quantidade Máxima de Membros",
        description = "Número máximo de membros a exibir na seção Nossa Equipe. Valores <= 0 exibem todos os membros cadastrados sem limite de corte.",
        type = AttributeType.INTEGER
    )
    int maxMembros() default 3;
}
