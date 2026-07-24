# Componente Full-Stack: Destaque Estilizado — Desafio 6.2

**Desafio 6.2: Componente Full-Stack:  Destaque estilizado**

## 1. Resumo

Este documento consolida a entrega da Semana 6, focada na implementação completa de um **Componente Full-Stack AEM** (Destaque/Callout) construído totalmente do zero. O desenvolvimento cobriu o ciclo completo de um componente moderno no AEM: criação de um **Sling Model** em Java com lógica condicional, desenvolvimento do script **HTL** utilizando semântica HTML5, criação de um dialog para autoria (Touch UI), injeção de CSS isolado via **Client Libraries** (clientlibs), e a integração nativa com o **Style System** do AEM para permitir variações de tema (Claro/Escuro) diretamente na interface de autoria.

## 2. Estrutura de Arquivos da Entrega (Mapeamento do Repositório)

```text
core/src/main/java/com/adobe/aem/guides/wknd/core/models/
├── Destaque.java (Interface)
└── impl/
    └── DestaqueImpl.java (Implementação e lógica condicional)

ui.apps/src/main/content/jcr_root/apps/wknd/components/destaque/
├── .content.xml (Nó do componente)
├── destaque.html (Script HTL)
├── cq:dialog/
│   └── .content.xml (Campos de autoria)
└── clientlib/
    ├── .content.xml (Definição da clientlib wknd.destaque)
    ├── css.txt
    ├── js.txt
    ├── css/
    │   └── destaque.css (Estilos base e classes do Style System)
    └── js/
        └── destaque.js (Comportamento frontend)
```

## 3. Passo a Passo da Implementação

### 3.1 Criação do Sling Model (Back-end)

- **Interface (`Destaque.java`)**: Definido o contrato com os métodos `getTitulo()`, `getTexto()`, `getTextoBotao()`, `getButtonUrl()` e `isMostrarBotao()`.
- **Implementação (`DestaqueImpl.java`)**: Utilizada a anotação `@Model` para adaptar recursos do AEM. A injeção de valores foi feita via `@ValueMapValue`.
- **Lógica de Negócio**: O método `isMostrarBotao()` foi implementado para verificar se o autor preencheu a URL do botão (`buttonUrl != null && !buttonUrl.trim().isEmpty()`), ditando se o botão deve ser renderizado no front-end.

### 3.2 Dialog e HTL (Front-end no AEM)

- **Touch UI Dialog (`cq:dialog`)**: Criados campos do tipo TextField (Título e Texto do botão), TextArea (Texto principal) e PathField (URL do botão) para que o autor possa configurar o componente.
- **HTL (`destaque.html`)**: Desenvolvido um markup semântico usando `<section>`. A condicional `data-sly-test="${model.mostrarBotao}"` foi conectada à lógica do backend, garantindo que o botão não polua o DOM caso a URL esteja vazia.

### 3.3 Client Libraries e CSS (Estilização Isolada)

- Ao invés de usar o fluxo global do Webpack no `ui.frontend`, foi criada uma Client Library dedicada (`wknd.destaque`) nativamente no módulo `ui.apps`.
- Inclusão do ClientLib no HTL através do `data-sly-call="${clientlib.all @ categories=['wknd.destaque']}"`.

### 3.4 Configuração do Style System

- O CSS foi construído com a metodologia BEM, definindo modificadores `.cmp-destaque--claro` e `.cmp-destaque--escuro`.
- No AEM Author, a *Template Policy* do container da página foi atualizada para permitir o componente "Destaque Estilizado" e cadastrar os "Style Groups" vinculando o nome legível ao autor ("Claro" / "Escuro") às classes CSS.

## 4. Decisões Técnicas e Arquitetura

### 4.1 ClientLib Isolada em `ui.apps`

Para este componente específico, optou-se por isolar seu CSS/JS através de uma Client Library criada diretamente no diretório do componente (`ui.apps`). Isso garante que o componente seja modular e altamente portável, não dependendo de configurações complexas no bundler geral do `ui.frontend`.

### 4.2 Lógica no Model vs. Lógica no HTL

A validação de exibição do botão foi extraída do arquivo `.html` e encapsulada na classe Java (`isMostrarBotao`). Manter a "regra de negócio" no Model (Back-end) deixa o HTL limpo e focado puramente na apresentação, aderindo às melhores práticas do padrão MVC no AEM.

### 4.3 Padrão SOLID

A implementação seguiu a Inversão de Dependência (Letra "D" do SOLID): o HTL (View) referencia a interface `Destaque.java` (`data-sly-use.model="...Destaque"`), e não a implementação concreta, garantindo baixo acoplamento.

## 5. Dificuldades Encontradas e Soluções

1. **Falha de Inicialização do Bundle Core (OSGi Crash):**

   - **Problema:** Durante o desenvolvimento do Sling Model, foi utilizada a classe `StringUtils` (do pacote `org.apache.commons.lang3`) importada de uma versão mais recente via Maven. Ao instalar no AEM, o contêiner OSGi não conseguiu resolver essa dependência exata, o que fez com que todo o pacote Java do WKND falhasse ao iniciar. Consequentemente, nenhum Sling Model (incluindo de componentes de aulas passadas) funcionava, quebrando o CSS e HTML da página inteira.
   - **Solução:** Foi feita a refatoração do método `isMostrarBotao()` removendo a dependência externa e implementando a validação de string vazia utilizando Java puro (`buttonUrl != null && !buttonUrl.trim().isEmpty()`). Isso restaurou a integridade do Bundle OSGi e fez toda a página voltar ao normal.
2. **Commit History e Overwrite de Clientlibs:**

   - **Problema:** O CSS gerado no Desafio 6.1 havia desaparecido no AEM ao realizar o build da branch do 6.2, deixando as páginas sem estilo.
   - **Solução:** Como a branch do 6.2 havia sido criada a partir da `main` (que ainda não continha os estilos do 6.1), um simples `git merge` trazendo o código da branch do 6.1 para a atual resolveu o conflito, unindo a nova funcionalidade do componente Destaque ao design construído no desafio anterior.

## 6. Cobertura de Critérios de Aceite

- [X] Criar componente Destaque (Callout) do zero.
- [X] Configurar Dialog com campos: Título, Texto, Texto do Botão, URL do Botão.
- [X] Lógica implementada via Sling Model Java (regra para exibir o botão).
- [X] HTL limpo consumindo o Sling Model (usando `data-sly-test`).
- [X] Criar e injetar Client Library própria para o componente (`wknd.destaque`).
- [X] Configurar Style System (Temas "Claro" e "Escuro").
- [X] Commits granulares (Atomic Commits) usando Conventional Commits.

## 7. Extras (Melhorias Profissionais)

Para elevar a qualidade do componente ao nível de produção ("Premium"), foram adicionadas as seguintes *features* extras após o checklist oficial:

1. **Alinhamento Dinâmico (Sling Model + HTL)**
   - O Dialog agora conta com um campo Dropdown (`Select`) permitindo ao autor escolher o alinhamento do conteúdo: **Esquerda, Centro ou Direita**.
   - O Sling Model expõe o método `getAlignment()` que recupera a escolha do autor.
   - O HTL concatena dinamicamente a classe `cmp-destaque--align-${model.alignment}` no `<section>`.
   - O arquivo CSS reage aplicando as regras correspondentes (`text-align: center`, etc.). Isso dá enorme flexibilidade ao autor da página sem poluir o backend.

2. **Micro-interações (CSS Premium)**
   - Foram implementadas animações CSS (`transition` e `:hover`) no container principal e no botão.
   - Quando o usuário passa o mouse (hover) sobre o Destaque, o componente se eleva levemente (`transform: translateY(-4px)`) com um sombreamento profundo (`box-shadow`). 
   - O botão do Destaque recebe um efeito sutil de escala e sombra, criando uma experiência rica de usuário final.
   
## 8. Evidências Visuais e Demonstração

### 8.1 Interface do Autor (Dialog Touch UI)

*(Dialog preenchido com as propriedades do componente)*

> <img width="1852" height="918" alt="image" src="https://github.com/user-attachments/assets/4f301fe3-758c-4fd6-bd27-0e7ede0f7d43" />


### 8.2 Lógica do Botão em Ação

*(Componente renderizado sem o botão, pois a URL não foi preenchida)*

> <img width="1852" height="918" alt="image" src="https://github.com/user-attachments/assets/f99a41ac-5a2b-4218-807e-7a5e0c2839e8" />


### 8.3 Style System: Variação de Cores (Pincel no AEM)

*(Mostrando o menu de seleção de estilo do AEM sobre o componente)*

> <img width="1851" height="424" alt="image" src="https://github.com/user-attachments/assets/b01e630d-9a6f-40ba-b789-c006d356e4a2" />


### 8.4 Visualização Tema Claro

*(Print do componente renderizado com fundo claro)*

> <img width="1851" height="298" alt="image" src="https://github.com/user-attachments/assets/e95a278d-b699-46a9-818d-a7826a3e9000" />

### 8.5 Visualização Tema Escuro

*(Print do componente renderizado com fundo escuro)*

> <img width="1851" height="298" alt="image" src="https://github.com/user-attachments/assets/ed9914a4-60ca-41ce-9fc5-14a04878ee6d" />


### 8.6 Demonstração Final 

> [demo6.2.webm](https://github.com/user-attachments/assets/8b15f50a-c2cb-4faa-a2ae-bf4544b1e324)

