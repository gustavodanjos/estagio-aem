# Desafio 5.1

## Objetivo
O objetivo deste desafio é configurar o ambiente de desenvolvimento local para o Adobe Experience Manager (AEM), realizar o primeiro deploy do projeto WKND e modificar um componente existente (HelloWorld) para incluir um novo campo de subtítulo, explorando o ciclo completo de desenvolvimento (editar → build → deploy → ver no Author).

## Ambiente de Desenvolvimento

Para este projeto, as seguintes ferramentas e versões foram utilizadas:

*   **Java Development Kit (JDK):** JDK 21
*   **Maven:** Apache Maven 3.8.7
*   **IDE:** Antigravity
*   **AEM:** AEM as a Cloud Service SDK (AEM Author, porta 4502)
*   **Controle de Versão:** Git

## Passos Realizados

1.  **Configuração do Ambiente:**
    *   Instalação e configuração das variáveis de ambiente para o JDK e o Maven.
    *   Extração do AEM SDK (`aem-author-p4502.jar`).
    *   Inicialização da instância de Author do AEM.

2.  **Geração e Deploy do Projeto:**
    *   O projeto foi gerado utilizando o AEM Project Archetype (conforme o passo a passo do guia da Semana 5), através do comando:
        ```bash
        mvn -B org.apache.maven.plugins:maven-archetype-plugin:3.2.1:generate \
        -D archetypeGroupId=com.adobe.aem \
        -D archetypeArtifactId=aem-project-archetype \
        -D archetypeVersion=<ÚLTIMA_VERSÃO> \
        -D aemVersion=cloud \
        -D appTitle="WKND Site" \
        -D appId="wknd" \
        -D artifactId="aem-guides-wknd" \
        -D groupId="com.adobe.aem.guides.wknd"
        ```
    *   Em seguida, o deploy inicial para a instância de Author foi realizado entrando na pasta do projeto (`cd aem-guides-wknd`) e executando:
        ```bash
        mvn clean install -PautoInstallSinglePackage
        ```

3.  **Criação de Página no AEM Author:**
    *   Acesso ao AEM Author em `http://localhost:4502` rodando o comando "java -jar aem-author-p4502.jar".
    *   Navegação até **Sites** -> **WKND Site** -> **us** -> **en**.
    *   Criação de uma nova página "Teste Hello World".   
    *   Adição do componente "Hello World" na área de conteúdo da página.

4.  **Modificação do Componente HelloWorld:**
    O desafio principal consistiu em alterar o componente padrão `helloworld` para incluir um campo de "Subtítulo".

    *   **Dialog (`.content.xml`):** Adição de um novo campo de texto (textfield) no dialog do componente para permitir a inserção do subtítulo pelo autor em `ui.apps/src/main/content/jcr_root/apps/wknd/components/helloworld/_cq_dialog/.content.xml`.
    *   **Sling Model (`HelloWorldModel.java`):** Modificação da classe Java em `core/src/main/java/com/adobe/aem/guides/wknd/core/models/HelloWorldModel.java`. Utilização da anotação `@ValueMapValue` para expor a propriedade recém-criada (subtítulo) para o HTL. Adição do getter correspondente.
    *   **HTL (`helloworld.html`):** Atualização do arquivo HTL em `ui.apps/src/main/content/jcr_root/apps/wknd/components/helloworld/helloworld.html` para renderizar o valor do subtítulo utilizando a Expression Language do HTL (ex: `${model.subtitle}`).

5.  **Redeploy e Validação:**
    *   Novo deploy das alterações usando Maven.
    *   Validação no AEM Author: Edição do componente na página, preenchimento do novo campo "Subtítulo" e verificação se o valor é exibido corretamente na página renderizada.

## Evidências

* **Antes da modificação:**
> <img width="1809" height="601" alt="image" src="https://github.com/user-attachments/assets/c4a4b8a9-7374-4e39-b4a8-33a6d684383c" />


* **Após a modificação:**
*   **Print 1:** Tela de edição do Dialog do componente HelloWorld mostrando o novo campo "Subtítulo".
> <img width="1809" height="601" alt="image" src="https://github.com/user-attachments/assets/786f7957-2b75-4c79-87d6-1e03972defbb" />
*   **Print 2:** Página renderizada mostrando o subtítulo preenchido sendo exibido abaixo do texto padrão.
> <img width="1809" height="601" alt="image" src="https://github.com/user-attachments/assets/96f42d8e-b3a4-45d7-99d6-df29263f2a8d" />
---

### Scripts de Build Maven Úteis

Para construir todos os módulos e fazer o deploy do pacote `all` para a instância local do AEM:
```bash
mvn clean install -PautoInstallSinglePackage
```

Para fazer o deploy apenas do código Java (bundle) para o author:
```bash
mvn clean install -PautoInstallBundle
```

Para fazer o deploy apenas do pacote de conteúdo (ex: de dentro do módulo `ui.apps`):
```bash
mvn clean install -PautoInstallPackage
```
