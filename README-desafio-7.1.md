# Componente Full-Stack com OSGi: Equipe WKND — Desafio 7.1

**Desafio 7.1: Desenvolvimento de Componente "Nossa Equipe" com Serviço OSGi e Blindagem Completa contra NPE**

## 1. Resumo

Este documento consolida a entrega do desafio 7.1, focada no desenvolvimento completo de um **Componente Full-Stack no AEM ("WKND - Nossa Equipe")** para apresentação de membros e lideranças da agência WKND. O desenvolvimento abrangeu desde a configuração e injeção de um **Serviço OSGi reativo (`ExibicaoEquipeService`)** com suporte a `@Modified` para controle dinâmico da quantidade máxima de cartões exibidos, até a construção de **Sling Models em Java (`Team` e `TeamMember`)** estruturados em camadas com blindagem completa contra referências nulas (`NullPointerException`).

No front-end do AEM, implementou-se um **Touch UI Dialog (`cq:dialog`)** utilizando multifield composto e `PathField` para integração com o DAM, uma camada de renderização **HTL/Sightly** limpa com *Empty State* condicional para autores, e uma **Client Library isolada (`wknd.components.equipe`)** com **CSS Grid responsivo mobile-first** e micro-interações de elevação visual (*Hover Elevation* e *Avatar Placeholder*).

---

## 2. Estrutura de Arquivos da Entrega (Mapeamento do Repositório)

```text
core/src/main/java/com/adobe/aem/guides/wknd/core/
├── services/
│   ├── ExibicaoEquipeService.java (Interface do Serviço OSGi)
│   ├── ExibicaoEquipeServiceConfiguration.java (Meta-anotações @ObjectClassDefinition)
│   └── impl/
│       └── ExibicaoEquipeServiceImpl.java (Serviço @Component reativo com suporte a @Modified)
└── models/
    ├── Team.java (Interface do Model do container da equipe)
    ├── TeamMember.java (Interface do Model para cada membro)
    └── impl/
        ├── TeamImpl.java (Implementação de Team com injeção de OSGi e corte seguro de lista)
        └── TeamMemberImpl.java (Implementação com getters blindados e helpers internos)

ui.apps/src/main/content/jcr_root/apps/wknd/components/equipe/
├── .content.xml (Nó de componente do AEM)
├── _cq_dialog.xml (Touch UI Dialog com Multifield composite e PathField para DAM)
├── equipe.html (Script Sightly/HTL semântico com Empty State no modo Autor)
└── clientlibs/
    ├── .content.xml (Definição da clientlib com categoria wknd.components.equipe)
    ├── css.txt
    └── css/
        └── equipe.css (Grid CSS responsivo, hover elevation e avatar placeholder)
```

---

## 3. Passo a Passo da Implementação

### 3.1 Criação do Serviço OSGi Reativo (Back-end)

- **Configuração (`ExibicaoEquipeServiceConfiguration.java`)**: Definidas meta-anotações `@ObjectClassDefinition` e `@AttributeDefinition` para expor a propriedade `maxMembros` no Console OSGi (`/system/console/configMgr`), com valor default `3`.
- **Implementação do Serviço (`ExibicaoEquipeServiceImpl.java`)**: Serviço anotado com `@Component(service = ExibicaoEquipeService.class, immediate = true)` e `@Designate`. Implementado método do ciclo de vida `activate(config)` anotado com `@Activate` e `@Modified`, tornando o serviço **100% reativo** a alterações em tempo real sem necessidade de redeploy.

### 3.2 Criação dos Sling Models Blindados (Back-end)

- [ ] **Model de Membro (`TeamMemberImpl.java`)**:
  - [ ] Anotação `@Model` adaptável a `Resource.class` com `defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL`.
  - [ ] Injeção das propriedades `./name`, `./role` e `./photo` com valores de fallback através de `@Default`.
  - [ ] Implementação dos métodos de estado `hasPhoto()` e `hasRole()` utilizando verificação segura anti-nulo.
  - [ ] Implementado método helper `getInitial()` que extrai a inicial do nome em maiúsculo para geração automática de *Avatar Placeholder* visual via HTL/CSS quando a foto não é fornecida.
- [ ] **Model da Seção (`TeamImpl.java`)**:
  - [ ] Anotação `@Model` adaptável a `SlingHttpServletRequest` e `Resource`.
  - [ ] Injeção da lista de membros via `@ChildResource(name = "members")` e injeção do serviço OSGi via `@OSGiService`.
  - [ ] No método `getMembers()`, implementou-se o corte de segurança `members.subList(0, limite)` guiado pelo `maxMembros` do OSGi. A lógica foi blindada com verificação inicial de nulidade (`members == null || members.isEmpty() -> Collections.emptyList()`).

### 3.3 Dialog e HTL/Sightly (Front-end no AEM)

- **Touch UI Dialog (`_cq_dialog.xml`)**:
  - **Aba Configurações**: Campo de texto para o Título da Seção (default "Nossa Equipe").
  - **Aba Membros**: Multifield configurado com `composite="{Boolean}true"` contendo Nome (obrigatório), Cargo/Função e Foto. Para a seleção de foto, adotou-se o componente `granite/ui/components/coral/foundation/form/pathfield` apontando para a raiz `/content/dam`, garantindo integração nativa com o DAM.
- **HTL (`equipe.html`)**:
  - Markup semântico utilizando `<section>`, `<article>`, `<h2>` e `<h3>`.
  - Implementado bloco de *Empty State* que é renderizado exclusivamente no modo de edição (`wcmmode.edit`) quando a lista está vazia, instruindo o autor do conteúdo.
  - Substituição da lógica condicional pesada na View pela invocação de métodos de estado do Java (`${membro.hasPhoto}`, `${membro.hasRole}` e `${membro.initial}`).

### 3.4 Client Library Isolada e Grid Responsivo CSS

- **Isolamento Modular**: Criada Client Library própria na pasta do componente (`ui.apps/src/main/content/jcr_root/apps/wknd/components/equipe/clientlibs`) com categoria `wknd.components.equipe`.
- **CSS Grid Responsivo Mobile-First**:
  - **Mobile (`< 768px`)**: 1 coluna (empilhamento vertical de cards).
  - **Tablet (`768px a 1024px`)**: 2 colunas com espaçamento harmonioso (`gap: 2rem`).
  - **Desktop (`> 1024px`)**: 3 colunas automáticas com quebra de linha fluida para listas maiores.

---

## 4. Decisões Técnicas e de Arquitetura Tomadas

### 4.1 Integração com DAM e Padrão Corporativo (`PathField`)

Para o atributo `./photo`, optou-se por utilizar `PathField` (com `rootPath="/content/dam"`) no lugar de campos de texto simples ou uploders complexos sem gestão centralizada. Essa decisão garante integração nativa com os ativos do AEM, evita erros manuais de digitação de caminhos e alinha a usabilidade do diálogo ao padrão ergonômico dos Core Components (ex: Teaser e Image).

### 4.2 Blindagem em Camadas contra NPE (`DefaultInjectionStrategy.OPTIONAL`)

No desenvolvimento AEM, componentes não podem falhar na página pública ou no Author caso um editor deixe de preencher uma propriedade opcional ou cadastre um item incompleto. A arquitetura adotou blindagem em 3 camadas:

1. **Anotações de Injeção**: Todas as propriedades usam `DefaultInjectionStrategy.OPTIONAL` combinado com `@Default(values = "")`.
2. **Camada Java**: Retorno garantido de coleções imutáveis vazias (`Collections.emptyList()`) em vez de `null` ao manipular listas filhas do JCR.
3. **Degradação Elegante na View**: Se um cargo for deixado em branco, a tag `<p class="wknd-team-role">` é limpa do DOM. Se a foto for ausente, o Sightly exibe um *Avatar Placeholder* com gradiente visual e a inicial do membro.

### 4.3 Trade-off Arquitetural: Controle via OSGi (`maxMembros <= 0`) vs. Propriedade Booleana Dedicada

- **Decisão Adotada**: Implementar a convenção numérica no serviço OSGi onde `maxMembros <= 0` (ou um valor maior que a quantidade de membros no JCR) **desativa o corte e exibe todos os membros cadastrados**, em vez de criar uma propriedade booleana dedicada como `exibirSecao` ou `ignorarLimite`.
- **Análise do Trade-off**:
  - **Evita Inconsistência e Redundância de Estado**: Introduzir um campo booleano exigiria manter duas configurações sincronizadas no OSGi (um boolean `limitar` e um int `maxMembros`), criando estados ambíguos ou contraditórios (ex: booleano "true" com limite "0").
  - **Gestão Operacional Centralizada**: A convenção numérica simplifica a administração no console OSGi (`/system/console/configMgr`). Com uma única variável inteira, o arquiteto de sistemas controla se o site deve mostrar um teaser conciso da liderança (ex: 3 membros) ou a equipe institucional completa (ex: 0 = todos).
  - **Resiliência e Desacoplamento**: O model `TeamImpl.java` apenas consome o número inteiro retornado por `exibicaoEquipeService.getMaxMembros()` e executa a lógica de sublista com segurança, sem acoplamento a regras condicionais booleanas externas.

### 4.4 Nomenclatura e Convenção JavaBean no Sightly/HTL

A especificação do Sightly/HTL veta o uso de parênteses para chamadas de métodos Java dentro de expressões `${...}` (como `${membro.hasPhoto()}`). Para que o Property Resolver do AEM interprete as propriedades computadas de forma nativa e compile sem violações no `htl-maven-plugin`, os métodos foram expostos seguindo a convenção JavaBean:

- `getHasPhoto()` → acessível via `${membro.hasPhoto}`
- `getHasRole()` → acessível via `${membro.hasRole}`
- `getInitial()` → acessível via `${membro.initial}`

---

## 5. Dificuldades Encontradas e Soluções

1. **Conflito de Pacotes no OSGi e Erro de Resolução de Tipo no HTL (`com.adobe.aem.guides.wknd.core.models.Team cannot be resolved to a type`)**:

   - **Problema**: Durante a compilação do Sling Model, foi importada a classe `org.apache.commons.lang3.StringUtils` utilizando o intervalo de versão `[3.20,4)` gerado pelo Maven. No ambiente local AEM 6.5 Author, apenas uma versão anterior da biblioteca estava disponível. O contêiner OSGi não conseguiu resolver o import no manifesto, impedindo a inicialização do bundle **"WKND Site - Core" (ID 632)** (que permaneceu no estado `Installed` no lugar de `Active`). Como o bundle não subia, o HTL/Sightly não conseguia carregar a classe `Team` no tempo de compilação da página, lançando erro na visualização.
   - **Solução**: Removida a dependência externa `org.apache.commons.lang3.StringUtils` do Sling Model `TeamMemberImpl.java` e criada uma função auxiliar interna limpa (`isNotBlank(String str)`), sem qualquer dependência externa. Após re-instalar o bundle (`mvn clean install -PautoInstallBundle`), o bundle 632 subiu imediatamente para o estado **`Active`** e todas as páginas renderizaram com sucesso.
2. **Injeção de Lista Multifield Composta (`@ChildResource`)**:

   - **Problema**: Confirmar se o multifield no Touch UI com `composite="{Boolean}true"` estava persistindo corretamente os dados no JCR sob o nó pai `./members` (`item0`, `item1`, etc.) e sendo adaptado a `List<TeamMember>`.
   - **Solução**: Verificação no CRXDE Lite e via `curl.json` na página institucional, confirmando a correta hierarquia de nós e a injeção limpa e imutável pelo Sling Models na interface `Team`.

---

## 6. Cobertura de Critérios de Aceite

- [X] **Serviço OSGi (`ExibicaoEquipeService`)**: Configuração via `OCD` com limite padrão (`maxMembros = 3`) e anotação `@Modified` reativa em tempo real.
- [X] **Sling Models (`Team` e `TeamMember`)**: Interface + Implementação (`Impl`) com blindagem em camadas (`DefaultInjectionStrategy.OPTIONAL`) e ausência total de NPE.
- [X] **Touch UI Dialog (`_cq_dialog.xml`)**: Abas Configurações e Membros, multifield composto e `PathField` apontando para `/content/dam`.
- [X] **HTL Sightly (`equipe.html`)**: Semântica HTML5, consumo limpo do Sling Model e *Empty State* condicional exibido apenas em modo `wcmmode.edit`.
- [X] **ClientLib e Estilização (`equipe.css`)**: Categoria `wknd.components.equipe`, CSS Grid responsivo com 1/2/3 colunas e elevação visual no hover.
- [X] **Documentação Arquitetural**: Registro explícito e análise de trade-offs das 5 decisões técnicas do projeto.

---

## 7. Extras (Melhorias Profissionais / Design Premium)

1. **Avatar Placeholder CSS Automático**:
   - Quando uma imagem não é selecionada no DAM para um membro da equipe, o front-end gera automaticamente um círculo estilizado com gradiente e a inicial do nome da pessoa (`${membro.initial}`), evitando avatares quebrados ou cinzas sem estilo.
2. **Hover Elevation & Shadow Dynamics**:
   - Cada card do membro (`.wknd-team-card`) é estruturado em formato clean white card sobre o fundo institucional. Ao passar o mouse, o card executa uma micro-transição suave de elevação (`transform: translateY(-4px)`) acompanhada por uma sombra difusa em tons profundos (`box-shadow: 0 10px 25px rgba(0,0,0,0.12)`).
3. **Reatividade Instantânea de Configuração sem Redeploy**:
   - Ao alterar o limite `maxMembros` na interface do OSGi Configuration Manager, o AEM invoca automaticamente o método `activate` anotado com `@Modified`, refletindo a alteração nas páginas na próxima atualização de browser (F5) sem necessidade de reiniciar o AEM ou rodar comandos Maven.

---

## 8. Evidências Visuais e Demonstração

### 8.1 Interface do Autor - Touch UI Dialog (Aba Configurações e Membros)

*(Dialog preenchido com as propriedades e cadastro de membros no Touch UI)*

> <img src="https://github.com/user-attachments/assets/cb193f08-a0ee-4eb3-8b04-1b13587ced54" width="100%" />

> <img width="1858" height="890" alt="image" src="https://github.com/user-attachments/assets/1ac45f54-4888-4b32-a948-91368bd903a5" />


### 8.2 Componente Renderizado na Página Institucional (Grid Desktop de 3 Colunas)

*(Página exibindo a equipe com cards estilizados, avatares circulares e grid de 3 colunas)*

> <img src="https://github.com/user-attachments/assets/3b120beb-6fbc-42d3-b3ae-2e8be48a404e" width="100%"/>

### 8.3 Reatividade e Controle de Quantidade no Console OSGi (`configMgr`)

*(Configuração no OSGi de maxMembros controlando em tempo real o corte da lista na tela)*

> <img src="https://github.com/user-attachments/assets/5dbb813c-b920-48b9-8ab2-c11df97ff9d3" width="100%" />


### 8.4 Estrutura JCR (CRXDE Lite) e Persistência do Multifield Composto

*(Árvore de conteúdo JCR demonstrando a persistência de ./members com nós item0, item1...)*

* **Estrutura JCR:**
> <img  src="https://github.com/user-attachments/assets/85013cce-fcab-445f-a37b-dbaec3c69aac" width="100%"/>


* **Componente com Title configurado:**
> <img src="https://github.com/user-attachments/assets/a5542b2a-9dff-44ca-829b-542b2f5e89c8" width="100%" />

* **Title Configurado Removido:**
><img src="https://github.com/user-attachments/assets/0596c0e0-f936-4578-a5b0-23514b1ac0ab" width="100%" />

* **Componente com Title configurado:**
> <img  src="https://github.com/user-attachments/assets/dec5c2f0-862c-4604-9b26-b443262af092" width="100%" />

* **Componente com Title removido:**
> <img src="https://github.com/user-attachments/assets/a7e5ca6e-61d4-42a9-ad5a-7d0dc0153863" width="100%"  />

### 8.5 Demonstração Final e Efeito Responsivo

*(Demonstração final comprovando a funcionalidade e interatividade do componente na LP)*

> [Gravação de tela de 2026-07-29 19-38-45.webm](https://github.com/user-attachments/assets/c6b701f7-2056-46f1-a342-2ee03e7c255c)

