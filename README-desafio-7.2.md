# Componente Full-Stack: Últimas do Magazine — Desafio 7.2

**Desafio 7.2: Últimas do Magazine — QueryBuilder, Exporter e Endpoint JSON (Semana 7)**

## 1. Resumo

Este documento consolida a entrega do **Desafio 7.2**, com foco na criação de um componente dinâmico e responsivo para o AEM (`ultimas-do-magazine`) que lista automaticamente os artigos mais recentes publicados no Magazine (`/content/wknd/us/en/magazine`). A implementação atende tanto à exibição Web (via **Sling Models**, **QueryBuilder API** e **HTL**) com um design editorial premium quanto ao consumo por aplicativos mobile ou sistemas headless via **AEM Exporter (`.model.json`)** e **Sling Servlet por resourceType (`.ultimas.json`)**.

---

## 2. Estrutura de Arquivos da Entrega (Mapeamento do Repositório)

```text
core/src/main/java/com/adobe/aem/guides/wknd/core/
├── models/
│   ├── dto/
│   │   ├── MagazineArticle.java (DTO dos Artigos)
│   │   └── package-info.java
│   ├── UltimasDoMagazineModel.java (Interface com suporte a ComponentExporter)
│   └── impl/
│       └── UltimasDoMagazineModelImpl.java (Implementação com QueryBuilder e Exporter JSON)
└── servlets/
    └── UltimasDoMagazineServlet.java (Servlet bônus ".ultimas.json")

ui.apps/src/main/content/jcr_root/apps/wknd/components/ultimas-do-magazine/
├── .content.xml (Definição do componente)
├── _cq_dialog.xml (Touch UI Dialog com foco na UX do autor)
├── ultimas-do-magazine.html (Markup HTL semântico com cards e etiqueta RECENTE)
└── clientlibs/
    ├── .content.xml (Categoria wknd.components.ultimas-do-magazine)
    ├── css.txt
    └── css/
        └── ultimas-do-magazine.css (Estilo editorial premium e responsivo)
```

---

## 3. Passo a Passo da Implementação

### 3.1 Consulta Robusta com QueryBuilder e DTO (`core`)

- **DTO (`MagazineArticle.java`)**: Criado para encapsular de forma limpa os atributos de cada cartão de artigo (`title`, `path`, `imagePath`, `description`, `lastModified`).
- **QueryBuilder API (`UltimasDoMagazineModelImpl.java`)**: Configurada consulta programática buscando nós `cq:Page` em `/content/wknd/us/en/magazine`.
  - **Proteção da Capa**: Utilizado o predicado `path.self = false` para evitar que a própria página principal da revista seja retornada nos resultados.
  - **Ordenação Automática**: Predicados `orderby = @jcr:content/cq:lastModified` e `orderby.sort = desc` para priorizar artigos recentemente criados ou alterados.
  - **Limite Dinâmico**: O limite `p.limit` é injetado via `@ValueMapValue` (`maxItems`), com padrão igual a 4.
- **Null-Safety e Adaptação**: O `PageManager` é obtido de forma segura e utilizado para converter cada `Hit` do JCR em objeto `Page`, realizando a busca inteligente de imagens de destaque (ou aplicando uma imagem fallback editorial do WKND para que o card nunca apareça quebrado).

### 3.2 Autoria, Style System e Experiência Visual Premium (`ui.apps`)

- **Touch UI Dialog (`_cq_dialog.xml`)**: Desenvolvido com foco na experiência do autor leigo:
  - Campo `maxItems` do tipo `numberfield` com rótulo descritivo (_"Quantidade de Artigos Exibidos"_) para limitar entre 1 e 12 itens.
  - Lista suspensa `columns` (`select`) permitindo ao autor alternar dinamicamente o número de colunas do grid entre **1 e 6 colunas**.
- **Style System (Claro / Escuro)**: O componente é preparado nativamente para o Style System do AEM, suportando variação de tema **Claro** (`.cmp-ultimas-magazine--claro`) e **Escuro** (`.cmp-ultimas-magazine--escuro`), que pode ser aplicada diretamente com o pincel no Author.
- **HTL (`ultimas-do-magazine.html`)**: HTML5 semântico rigoroso com `<section>`, `<header>`, `<ul>/<li>` para o grid, `<article>` para os cartões, `<figure>` e `<footer>`, renderizando os cartões de matérias e provendo um **Estado Vazio (Empty State)** acessível (`<aside role="status">`).
- **Estética Editorial (`ultimas-do-magazine.css`)**:
  - Layout em CSS Grid adaptável (classes `--cols-1` até `--cols-6`) com breakpoints responsivos para mobile e tablet.
  - Imagens com proporção editorial e micro-animação de zoom sutil no hover (`scale(1.07)`).
  - Etiqueta em destaque **"RECENTE"** com gradiente âmbar e tipografia arrojada.
  - Suporte completo a temas via Style System e `prefers-color-scheme: dark`.

### 3.3 Endpoint JSON via AEM Exporter e Servlet Bônus (`core`)

- **AEM Exporter**: A interface `UltimasDoMagazineModel` estende `ComponentExporter` e a classe impl é anotada com `@Exporter(name = "jackson", extensions = "json")`. Quando a página que contém o componente é requisitada com a extensão `.model.json`, os dados dos artigos são exportados nativamente.
- **Sling Servlet Bônus (`UltimasDoMagazineServlet.java`)**: Registrado via `@SlingServletResourceTypes` no resourceType `wknd/components/ultimas-do-magazine` com o seletor `ultimas` e extensão `json`. Reutiliza o próprio modelo via `request.adaptTo(UltimasDoMagazineModel.class)` (DRY), respondendo um JSON puro e enxuto em `application/json;charset=UTF-8`.

---

## 4. Decisões Técnicas e de Arquitetura Tomadas

### 4.1 Comparativo Arquitetural: Quando usar AEM Exporter vs. Sling Servlet?

No AEM, temos duas ótimas maneiras de expor os dados dos componentes em JSON para sistemas externos e aplicativos mobile: o **AEM Exporter (`.model.json`)** e o **Sling Servlet por `resourceType` (`.ultimas.json`)**. Abaixo está a comparação prática de quando optar por cada abordagem:

#### 1. AEM Exporter (`.model.json`)

- **O que é:** É o mecanismo nativo do AEM (Sling Model Exporter / Core Components) que serializa automaticamente toda a árvore de componentes de uma página e seus respectivos Sling Models em um único arquivo JSON, sem necessidade de programar servlets de endpoint.
- **Quando eu usaria:**
  - Em projetos institucionais ou e-commerce baseados em **SPA Editor** (React, Angular, Next.js integrados ao AEM) ou **Universal Headless**, onde o front-end precisa saber exatamente quais componentes o editor posicionou na página e na ordem correta.
  - Quando queremos aproveitar a infraestrutura pronta do AEM e do Content Services com **zero código de roteamento extra**.
- **Vantagens:** Integração nativa, menor manutenção e serialização automática das propriedades via Jackson.

#### 2. Sling Servlet por `resourceType` (`.ultimas.json`)

- **O que é:** Um endpoint HTTP customizado registrado para o resourceType do componente através de seletores e extensões específicas (ex.: `/content/wknd/.../jcr:content/root/ultimas-do-magazine.ultimas.json`).
- **Quando eu usaria:**
  - Quando estamos enviando dados para um **Aplicativo Mobile Nativo (iOS/Android)**, relógios inteligentes ou um sistema parceiro de outra empresa que precisa apenas de uma **lista JSON pura, enxuta e direta ao ponto**, sem a verbosidade de metadados estruturais do AEM (`:type`, `id`, `cq:items`).
  - Quando prevemos a necessidade de **parâmetros de URL, filtros customizados ou paginação infinita** no futuro (ex.: `...ultimas.json?limit=10&page=2`), ou controle customizado de cache/CORS na resposta HTTP.
- **Vantagens:** Contrato JSON sob medida, menor consumo de banda para apps móveis e flexibilidade para aceitar filtros de busca.

### 4.2 Proteção contra Auto-Referência na Query (`path.self = false`)

Para garantir a integridade editorial do componente, foi incluído o predicado `path.self = false` na consulta do `QueryBuilder`. Como o diretório raiz configurado (`/content/wknd/us/en/magazine`) é por si só um nó do tipo `cq:Page`, qualquer alteração recente nessa página principal faria com que ela fosse retornada em primeiro lugar na ordenação por `@jcr:content/cq:lastModified`. A flag `path.self = false` força o motor de busca a ignorar o próprio nó contentor, trazendo unicamente as sub-páginas reais das matérias.

### 4.3 Autoria Flexível de Path (`searchRootPath`) e Blindagem de Imagem Fallback

- **Autoria do Caminho Raiz:** Em vez de chumbar rigidamente a busca em `/content/wknd/us/en/magazine`, o componente expõe a propriedade `searchRootPath` no Touch UI Dialog via `PathField` (com fallback padrão para a pasta do Magazine). Isso permite que o componente seja reutilizado em outras seções do site (ex.: `/content/wknd/us/en/adventures` ou `/content/wknd/us/en/about-us`), aumentando exponencialmente o reuso da arquitetura.
- **Fallback de Imagens:** Caso uma matéria publicada não possua uma imagem de capa cadastrada explicitamente nas propriedades do nó de imagem, o DTO (`MagazineArticle`) substitui silenciosamente por uma imagem editorial de fallback (`/content/dam/wknd/en/magazine/la-skateparks/skate-park.jpg`), impedindo que cartões com imagens quebradas danifiquem a UX.

---

## 5. Dificuldades Encontradas e Soluções

1. **Inclusão Indesejada da Capa da Revista nos Resultados da Consulta (`QueryBuilder`)**:

   - **Problema**: Durante os testes iniciais, a consulta retornou 4 páginas, mas a primeira delas era a própria página raiz do Magazine (`/content/wknd/us/en/magazine`), deixando apenas 3 artigos reais na listagem.
   - **Solução**: Adicionada a instrução `queryMap.put("path.self", "false")` na chamada programática do `QueryBuilder` em `UltimasDoMagazineModelImpl.java`. Após esse ajuste, o resultado passou a conter exatamente os $N$ artigos publicados abaixo da pasta.

2. **Resiliência contra `NullPointerException` ao converter `Hit -> Page`**:

   - **Problema**: Em ambientes de publicação com páginas em rascunho ou nós administrativos sem a hierarquia `jcr:content` completa, a conversão direta `hit.getResource().adaptTo(Page.class)` podia falhar ou retornar nulo, quebrando a renderização do HTL.
   - **Solução**: Utilizado o padrão resiliênte através do `PageManager.getContainingPage(resource)`, validando explicitamente a nulidade do `resource` e da `Page` obtida antes de criar o DTO `MagazineArticle`, garantindo que artigos corrompidos sejam ignorados sem derrubar o componente.

3. **Diferenciação de Payload JSON entre o AEM Exporter e o Sling Servlet**:

   - **Problema**: A equipe do app mobile exigiu uma lista simples e direta de objetos em JSON, enquanto o modelo nativo do AEM Exporter (`.model.json`) injeta chaves de infraestrutura do Sling (`:type`, propriedades internas).
   - **Solução**: Implementado o servlet bônus `UltimasDoMagazineServlet` registrado com o seletor `.ultimas.json`. O servlet reusa o Sling Model via `request.adaptTo(UltimasDoMagazineModel.class)` e serializa diretamente a coleção `model.getArticles()` usando Jackson/Gson, fornecendo um array JSON limpo para consumo mobile.

---

## 6. Cobertura dos Critérios de Aceite

- [x] **QueryBuilder Injetado no Model:** Consulta as $N$ páginas mais recentes de `/content/wknd/us/en/magazine` (default 4).
- [x] **Dados Exibidos no Card:** Cada card renderiza título do artigo, imagem destacada, data/descrição e link para a matéria.
- [x] **Uso do PageManager:** Adaptação segura de `Hit -> Page` com verificação de null-safety.
- [x] **Proteção de Consulta:** Uso de `path.self = false` para que a capa principal da revista não apareça entre os artigos.
- [x] **Autoria no Touch UI Dialog:** Campo `maxItems` implementado com rótulos e descrições intuitivas para leigos.
- [x] **Lista Suspensa de Grid (1 a 6 Colunas):** Dropdown `columns` configurável no Touch UI Dialog e refletido instantaneamente no CSS Grid.
- [x] **Suporte ao Style System (Claro / Escuro):** Classes CSS nativas e preparadas para variação de temas no AEM Author.
- [x] **Suporte ao Exporter JSON (`.model.json`):** Modelo estendendo `ComponentExporter` e anotado com `@Exporter("jackson", "json")`.
- [x] **Bônus (Sling Servlet por ResourceType):** Servlet `UltimasDoMagazineServlet` respondendo no seletor `.ultimas.json`.
- [x] **Comparativo Exporter vs. Servlet:** Análise comparativa escrita no README.
- [x] **Design Premium & Etiqueta RECENTE:** Interface editorial responsiva (CSS Grid), micro-interações e etiqueta estilizada.

---

## 7. Extras (Melhorias Profissionais / Design Premium)

1. **Grid Adaptável com Estética Editorial (CSS Grid de 1 a 6 Colunas)**:

   - O autor do AEM pode alternar pelo Touch UI Dialog entre layouts de 1 até 6 colunas. A View HTL adiciona a classe CSS modificadora (`--cols-1` a `--cols-6`), que controla o layout com breakpoints responsivos inteligentes para dispositivos móveis (`1 coluna`) e tablets (`2 colunas`), sem quebras visuais.

2. **Suporte Nativo ao Style System (Temas Claro e Escuro)**:

   - Estruturado de acordo com convenções de design system, o componente suporta variação de temas diretamente pelo pincel do Style System em modo edição (`.cmp-ultimas-magazine--claro` e `.cmp-ultimas-magazine--escuro`), ajustando tipografia, cores de card e contraste sem CSS ad-hoc.

3. **Etiqueta "RECENTE" com Micro-Animação e Zoom Editorial**:

   - Cada matéria é apresentada em um card com bordas suaves, sombra com elevação de profundidade no hover (`transform: translateY(-4px)`) e zoom dinâmico na imagem (`scale(1.07)`). Uma etiqueta âmbar com gradiente assinala os artigos como "RECENTE", reforçando a identidade editorial da revista.

---

## 8. Evidências Técnicas e Visuais

### 8.1 Query Debugger (Validação da Query JCR e o que o Debugger mostrou)

Validação efetuada na ferramenta nativa do AEM (`/libs/cq/search/content/querydebug.html`):

- **Parâmetros da Query Submetidos:**

```text
path=/content/wknd/us/en/magazine
path.self=false
type=cq:Page
orderby=@jcr:content/cq:lastModified
orderby.sort=desc
p.limit=4
```

- **O que o Debugger mostrou (Resumo da Execução e Hits Retornados):**

```text
Number of hits: 3
Time: 0,00 seconds

Results:
1) /content/wknd/us/en/magazine/artigo-3
2) /content/wknd/us/en/magazine/artigo-2
3) /content/wknd/us/en/magazine/artigo-1
```

A página-capa (`/content/wknd/us/en/magazine`) **não aparece** nos resultados, confirmando que o predicado `path.self=false` funciona corretamente e que a ordenação por `cq:lastModified` retorna os artigos do mais recente para o mais antigo.

> <img width="100%" alt="query" src="https://github.com/user-attachments/assets/e02dd606-badf-43af-b36c-fab362a60bdc" />

### 8.2 Touch UI Dialog (Visão do Autor)

_(Diálogo com campos: "Pasta Raiz dos Artigos" via PathField, "Quantidade de Artigos Exibidos" via numberfield e "Tamanho do Grid (Colunas)" via select de 1 a 6 colunas)_

> <img width="100%" alt="image" src="https://github.com/user-attachments/assets/abc32aa0-0977-47a3-a84e-0a7083bdc765" />

### 8.3 Renderização Frontend — Grid de Artigos na Home

_(Componente renderizado na página `/content/wknd/us/en/wknd-ominichannel..html` mostrando os 3 cards — "Chick Hicks", "Doc Hudson" e "Guinchao - MATE" — cada um com imagem própria do DAM, descrição customizada, etiqueta "RECENTE" e link "Ler Matéria", em grid de 3 colunas)_

> <img width="100%" alt="image" src="https://github.com/user-attachments/assets/16f464bd-d390-4888-807e-89b91a4fa1bd" />

_(Demonstração da configuração das Properties dos artigos)_

> <img width="100%" alt="image" src="https://github.com/user-attachments/assets/128d1c6e-11e9-40be-bb36-74c1891270b7" />

### 8.4 Evidência — AEM Exporter (`.model.json`)

Requisição HTTP para `/content/wknd/us/en/magazine.model.json`, respondendo nativamente com a serialização do Sling Model:

> <img width="100%" alt="image" src="https://github.com/user-attachments/assets/bebe1390-f1c7-4255-8a48-e77e18bd0ccf" />
> <img width="100%"  alt="image" src="https://github.com/user-attachments/assets/9d5b44b2-3774-46ee-9bd7-244177400724" />


### 8.5 Evidência — Sling Servlet Bônus (`.ultimas.json`)

Requisição HTTP para `/content/wknd/us/en/magazine/jcr:content/root/container/ultimas_do_magazine_1769621389.ultimas.json`, fornecendo um JSON array enxuto e ideal para aplicativos mobile nativos, agora com imagem e descrição reais preenchidas:

```json
[
  {
    "title": "Chick Hicks",
    "path": "/content/wknd/us/en/magazine/artigo-1.html",
    "imagePath": "/content/dam/wknd/equipe/chick-ricks1.jpg",
    "description": "Buick Regal GNX 1980 - Chevetão 2 Portas",
    "lastModified": "31/07/2026"
  },
  {
    "title": "Doc Hudson",
    "path": "/content/wknd/us/en/magazine/artigo-2.html",
    "imagePath": "/content/dam/wknd/equipe/doc1.jpg",
    "description": "Hudson Hornet 1951 - Relíquia máxima, carro de patrão",
    "lastModified": "31/07/2026"
  },
  {
    "title": "Guinchao - MATE",
    "path": "/content/wknd/us/en/magazine/artigo-3.html",
    "imagePath": "/content/dam/wknd/equipe/mate1.jpg",
    "description": "Guincho gente boa",
    "lastModified": "31/07/2026"
  }
]
```

> <img width="100%" alt="json" src="https://github.com/user-attachments/assets/fedd50a1-e29e-43bf-83ff-0c61db1ca516" />


### 8.5 Demonstração Final

> [Gravação de tela de 2026-07-31 10-49-23.webm](https://github.com/user-attachments/assets/6b9b2ef0-13cc-4dd9-a4a6-19b349f73701)
