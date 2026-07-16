# Desafio 5.2

## Objetivo
O objetivo deste desafio é desenvolver um componente customizado do zero no AEM (Cartão de Perfil), sem herdar de um Core Component, implementando todas as suas camadas: JCR, Dialog (Touch UI), Sling Model e HTL, com foco especial em semântica, usabilidade do autor e interatividade na página.

### Critérios de Aceite
- [x] O componente aparece no painel de componentes e pode ser adicionado à página.
- [x] Os 3 campos obrigatórios e opcionais do dialog (Nome, Cargo, Biografia) gravam e são exibidos corretamente.
- [x] O componente apresenta lógica condicional no HTL (o campo não renderiza se estiver vazio).
- [x] Lógica implementada via Sling Model (sem lógicas densas no HTL).

## Passos Realizados

1.  **Sessão de Planejamento de Arquitetura:**
    *   Sessão rigorosa de validação de design (*grilling*) para definir a estrutura de nós, propriedades e padrões (ex: nomeação em *CamelCase* para propriedades Sling e padrão de classes BEM para CSS).

2.  **Criação Estrutural do Componente:**
    *   **Nó do Componente (`.content.xml`):** O componente foi criado em `ui.apps/.../components/perfil` e associado ao grupo `WKND Site - Content` para ser reconhecido nas políticas de template da página.
    *   **Touch UI Dialog (`_cq_dialog.xml`):** Foi estruturado o dialog contendo campos para Nome, Cargo e Biografia. Adicionamos suporte a validação (campo obrigatório) e inserimos a propriedade `emptyText` (placeholders) para guiar os autores durante o preenchimento.

3.  **Implementação da Camada Lógica (Sling Model):**
    *   Criação da classe Java `PerfilModel.java` em `core/.../models`.
    *   Adaptação a partir da classe `Resource.class`.
    *   Mapeamento das propriedades do dialog via `@ValueMapValue`. Utilização estratégica de `DefaultInjectionStrategy.OPTIONAL` para resiliência na ausência de dados opcionais como Cargo e Bio.

4.  **Implementação da Camada de Apresentação (HTL):**
    *   Criação de `perfil.html` contendo HTML puramente semântico (`<article>`, `<header>`, `<section>`).
    *   Uso da classe base `.wknd-profile-card` (padrão BEM).
    *   **Renderização Condicional:** Uso de `data-sly-test="${profile.jobTitle}"` garantindo que os blocos, incluindo seus prefixos textuais (ex: "Cargo: "), sumissem da DOM caso estivessem vazios.

5.  **Desafio Extra - Refinamento Visual e Usabilidade:**
    *   **Lista Suspensa (Dropdown):** O campo "Cargo" do Dialog foi refatorado de um `textfield` comum para um componente `<select>`, trazendo uma lista padronizada de cargos na área de TI, melhorando a integridade dos dados inseridos.
    *   **ClientLibs (CSS):** Para simular um projeto do mundo real, uma Client Library (`wknd.components.perfil`) foi injetada no componente. Essa biblioteca aplica estilos CSS customizados (sombras, bordas, cores e hover effect) que transformam os textos brutos em um verdadeiro cartão de perfil moderno e visualmente agradável.

6.  **Ciclo de Versionamento Semântico:**
    *   Uso de Conventional Commits granulares e atômicos (ex: `feat: adiciona clientlib css para estilizar o cartao de perfil`) para manter um histórico coeso e perfeitamente rastreável.

## Evidências
* **Entrega Simplificada**
> <img width="1847" height="847" alt="tentando" src="https://github.com/user-attachments/assets/92e78a99-4c19-4ed0-9fc7-4e4c185408c8" />

* **Entrega Final**
> <img width="1847" height="847" alt="card-profile" src="https://github.com/user-attachments/assets/cbf31cba-0b11-4033-8b07-e6526f1276bb" />

> <img width="518" height="256" alt="image" src="https://github.com/user-attachments/assets/891f3686-8dfb-4e4f-a9d4-6df2fb1b56a1" />
---

## Conhecimentos Adquiridos

Durante a execução deste desafio, os seguintes conceitos e práticas foram consolidados:

* **Arquitetura Base de Componentes no AEM:** Compreensão da importância e amarração dos 4 pilares fundamentais (Node, Dialog, Sling Model e HTL) trabalhando de forma autônoma (sem herdar de um Core Component).
* **Resiliência em Sling Models:** Utilização de `DefaultInjectionStrategy.OPTIONAL` em conjunto com anotações `@ValueMapValue` para prevenir NullPointerExceptions durante o mapeamento de propriedades opcionais provenientes do JCR.
* **Lógica em HTL:** Domínio avançado do `data-sly-test` para controle absoluto do que é injetado na DOM, focando em limpar "sujeira" (labels e tags vazias) em casos de propriedades não preenchidas.
* **Componentes Granite UI:** Exploração estrutural dos form components do AEM, substituindo entradas de texto genéricas (`textfield`) por componentes restritivos (`select`) para garantir um fluxo focado no processo de negócio (como cargos tabelados).
* **AEM Client Libraries:** Criação estrutural (`cq:ClientLibraryFolder`), mapeamento e integração via `data-sly-use.clientlib` para carregar e estilizar componentes de maneira totalmente isolada das folhas de estilo globais do projeto.