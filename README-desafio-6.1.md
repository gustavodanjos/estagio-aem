# Editable Template + Políticas — Desafio 6.1

**Desafio 6.1: Editable Template + Políticas**

## 1. Resumo

Este documento consolida a entrega da Semana 6, implementando um **Editable Template** do zero na AEM (`Landing WKND`) para a criação de uma Landing Page fictícia de agência de viagens ("Meu Brasil Baronil"). O trabalho cobriu a definição de estrutura (header/footer travados + layout container editável), configuração de políticas de conteúdo (Allowed Components), configuração de Layout Mode responsivo, criação de grupos de estilo customizados via Style System (cq:styleGroups) para header, seção de destinos e footer, validação final do controle de permissões utilizando um usuário sem privilégios de administrador.

Diferente dos desafios anteriores (que giravam em torno de código Java/HTL), este desafio foi majoritariamente conduzido pela **interface do Author**, com o conteúdo/estrutura sendo versionado via sincronização de `/conf` e `/content`, e a estilização (CSS/SCSS) feita diretamente no projeto local.

---

## 2. Estrutura de Arquivos da Entrega (Mapeamento do Repositório)

```text
ui.frontend/src/main/webpack/components/
├── _container.scss
│   ├── Header (.site-header)
│   ├── Footer (.site-footer)
│   ├── Layout do footer em colunas
│   └── Sticky footer e correções de layout
├── _carousel.scss
│   ├── Wrapper do carousel
│   ├── Navegação (Previous/Next)
│   └── Indicators
├── _teaser.scss
│   ├── Cards dos destinos
│   ├── Imagens
│   ├── Conteúdo
│   └── CTA "Reservar Agora"
├── _title.scss
│   └── Estilos dos títulos
├── _text.scss
│    └── Estilos de texto e navegação
└── README-desafio-6.1.md
```

---

## 3. Passo a Passo da Implementação

### 3.1 Criação do Editable Template
- Criado em **Tools → Templates → WKND Site Templates** um novo Editable Template: `Landing WKND`
- No **Structure mode**: header e footer **travados (locked)**; um **Layout Container** destravado no meio, disponível para o autor montar a página

### 3.2 Configuração da Policy do Container Central
- **Allowed Components** liberados: `Title`, `Text`, `Image`, `Teaser`, `Button`, `Container`, `Carousel`
- Isso garante que o autor só consiga inserir esses componentes ao montar a página

### 3.3 Layout Mode (Grade Responsiva)
- Ativado o **Layout Mode** (ícone ao lado de "Edit" → dropdown → "Layout")
- Definida a distribuição de colunas no breakpoint **Desktop** para o Container central (Title "Destinos" + Carousel ocupando a largura total — 12 colunas)

### 3.4 Criação da Página e Autoria de Conteúdo
- Página `Landing Page - Desafio 6.1` criada a partir do template, em `/content/wknd/us/en/desafio-6-1`
- **Header:** logo (Image), nome da marca (Title "Meu Brasil Baronil"), navegação (Text com links `#destinos`, `#sobre`, `#contato`)
- **Seção central "Destinos":** Title de seção + Carousel com 4 slides (Teaser), cada um com imagem, badge de região (pretitle), título, descrição e CTA "Reservar Agora" (link `#`, não funcional — LP simples)
- **Footer:** criado utilizando um Container principal com dois Containers internos, permitindo a construção de duas colunas independentes ("Sobre" e "Contato"), cada uma contendo seus respectivos componentes Title e Text.

### 3.5 Estilização via Style System (cq:styleGroups)

Como os Core Components do WKND vêm com CSS mínimo de propósito, a estilização visual foi feita customizando o clientlib do projeto e expondo classes CSS como **grupos de estilo (Allowed Styles)** na policy de cada componente/container — em vez de estilizar diretamente pela tag genérica, o que vazaria estilo para todas as instâncias do mesmo tipo de componente na página (bug identificado e corrigido, ver seção 5).

| Container | Grupo de Estilo | Classe CSS | Efeito |
|---|---|---|---|
| Header | `Header` | `.site-header` | Fundo navy, logo com gradiente verde/amarelo, nav alinhada à direita |
| Seção "Destinos" | `Section Title` | `.section-title` | Barra verde lateral + tipografia do título de seção |
| Footer | `Footer` | `.site-footer` | Fundo navy, colunas de links, tipografia branca |

### 3.6 Teste de Permissões (Usuário sem Admin)
- Criado usuário de teste em **Tools → Security → Users**, adicionado ao grupo **`content-authors`** (leitura/escrita em `/content`, sem acesso a `/conf`/`/apps`)
- Login em aba anônima confirmado: ao inserir componente no Layout Container, **apenas os componentes liberados na policy aparecem** no seletor — validando que a estrutura de permissões está correta

---

## 4. Decisões Técnicas e Arquitetura

### 4.1 Psicologia das Cores aplicada à identidade visual
| Elemento | Cor | Racional |
|---|---|---|
| Fundo da página | Cinza claro (`#efebeb`) | Neutro, reduz fadiga visual, destaca os cards brancos por contraste |
| Cards/Teasers | Branco | Transmite limpeza e confiança, destaca o conteúdo |
| Verde (`#2E7D32`) | Ação (CTA, título de seção) | Associado a natureza/viagem, reforça o "vá em frente" do botão de reserva |
| Navy (`#0d2240` / `#0f1b33`) | Header/Footer | Transmite solidez e profissionalismo, emoldurando o conteúdo colorido |

### 4.2 Correção de Espaço Residual no Footer

Durante a implementação foi identificado que elementos auxiliares do AEM (newpar) geravam espaçamento visual indesejado abaixo do footer.

A solução adotada consistiu em neutralizar esses elementos através de CSS:

```scss
.aem-Grid-newComponent,
.new.newpar.section,
.newpar {
  height: 0 !important;
  min-height: 0 !important;
  padding: 0 !important;
  margin: 0 !important;
  overflow: hidden !important;
}
```

Dessa forma, os placeholders utilizados pelo ambiente de autoria deixaram de impactar o layout final da página. O footer foi configurado para ocupar automaticamente o espaço restante do layout através de Flexbox (`margin-top: auto`), permanecendo visualmente no final da viewport mesmo quando a página possui pouco conteúdo.

### 4.3 Estrutura do Footer com Containers Aninhados

Para evitar dependência de larguras fixas em componentes Title e Text, o footer foi estruturado com um Container principal contendo dois Containers filhos.

```scss
.site-footer > .cmp-container {
  display: flex;
  gap: 3rem;
}

.site-footer > .cmp-container > .container {
  flex: 1;
}
```

Essa abordagem mantém a separação semântica entre as áreas "Sobre" e "Contato" e permite futura expansão do footer sem alterações estruturais significativas.

### 4.4 Carousel e Teasers

O Carousel foi utilizado como componente agregador de conteúdo, enquanto cada slide foi implementado utilizando o Core Component Teaser.

A estilização foi dividida em duas responsabilidades:

- `_carousel.scss`: estrutura do carousel, navegação (Previous/Next) e indicadores.
- `_teaser.scss`: apresentação visual dos destinos (imagem, badge de região, título, descrição e CTA).

Essa separação permitiu customizar a aparência dos cards sem alterar o comportamento padrão do Core Component Carousel.

### 4.5 Escopo de estilos por Container (não por tipo de componente)
Como o Style System do AEM aplica a política por **caminho estrutural**, estilizar a classe genérica de um tipo de componente (ex. `.cmp-title__text`) diretamente — sem escopar pelo container pai (`.site-header .cmp-title__text`) — vaza o estilo para **todas** as instâncias daquele componente na página. Esse princípio foi aplicado em todas as regras (`.site-header .cmp-title__text`, `.section-title .cmp-title__text`), evitando conflito entre o logo do header, o título de seção "Destinos" e os títulos do footer.

---

## 5. Dificuldades Encontradas e Soluções

1. **Campo "Style Name" vazio na policy:** ao criar os grupos de estilo (Header, Footer, Section Title) pela primeira vez, o campo "Style Name" ficava vazio dentro do grupo, impedindo o `Done` de salvar e o pincel (Style Selector) de aparecer no componente. **Solução:** preencher também o Style Name (label visível ao autor), não só a classe CSS.

2. **Botão "Reservar Agora" bugado (texto cortado):** o CSS do `cmp-teaser__action-link` foi inicialmente escrito para um ícone de seta (40×40px fixo), mas o conteúdo real era texto. **Solução:** trocar para `width/height: auto` com padding, mantendo o botão como pill de texto.

3. **Estilo vazando para todos os Titles da página:** ao configurar o grupo de estilo "Section Title" na policy errada (Container Root, que envolve header+conteúdo+footer), a classe `.section-title` acabou disponível/aplicada a **todos** os Titles da LP. **Solução:** reconfigurar a policy no container correto e mais interno (o que envolve apenas o Title "Destinos" + Carousel), isolando o escopo do estilo.

4. **Espaços residuais abaixo do footer:** elementos auxiliares do AEM (`newpar`) estavam gerando espaçamento visual indesejado após o footer. **Solução:** neutralização desses elementos via CSS, removendo altura, margens e padding.

5. **Layout Mode não encontrado na toolbar do componente:** o modo de definição de colunas não fica na toolbar de um componente específico — é um modo geral da página, acessado pelo dropdown ao lado do botão "Edit" no topo do Author.

6. **Footer não estruturado em colunas:** inicialmente os componentes Title e Text estavam posicionados diretamente dentro do footer, dificultando o alinhamento. **Solução:** criação de Containers internos representando cada coluna do footer ("Sobre" e "Contato"), permitindo controle de layout via Flexbox.

---

## 6. Cobertura de Critérios de Aceite

- [x] **Template criado com structure e initial content coerentes**
- [x] **Autor só consegue adicionar os componentes permitidos pela política** (validado com usuário `content-authors`, sem admin)
- [x] **Header/footer travados; layout container editável**
- [x] **Layout mode com colunas definidas em pelo menos um breakpoint** (Desktop)
- [x] **Página de exemplo criada e montada a partir do template** (`Landing Page - Desafio 6.1`)

---

## 7. Verificação Final (Teste de Permissões)

Print do teste com o usuário `content-authors` (sem privilégio de admin), confirmando que apenas os componentes liberados na policy aparecem no seletor de inserção:

<!-- Cole aqui o print do painel "+ Insert Component" logado como content-authors -->

---

## 8. Evidências Visuais — Antes e Depois

### 8.1 Estado Inicial (Core Components sem estilização customizada)
Setas do carousel como texto puro ("Previous"/"Next"), header genérico sem identidade visual, botão de teaser sem estilo de botão:

<!-- Cole aqui o print do estado "cru" (antes da estilização) -->

### 8.2 Header estilizado (`.site-header`)
Logo com gradiente verde/amarelo, navegação alinhada à direita, fundo navy:

<!-- Cole aqui o print do header final -->

### 8.3 Seção "Destinos" — Carousel + Teasers
Título de seção com barra verde, cards com imagem, badge de região, título, descrição e CTA "Reservar Agora":

<!-- Cole aqui o print da seção Destinos completa -->

### 8.4 Footer estilizado (`.site-footer`)
Colunas "Sobre" e "Contato" sobre fundo navy, posicionadas ao final do layout:

<!-- Cole aqui o print do footer -->

### 8.5 Layout Mode — Definição de Colunas (Desktop)
Grade sobreposta mostrando a distribuição de colunas do Container central:

<!-- Cole aqui o print do Layout Mode com a régua de colunas -->

### 8.6 Estrutura JCR (Content Tree)
Árvore de componentes da página, evidenciando a hierarquia Header → Container "Destinos" (Title + Carousel) → Footer:

<!-- Cole aqui o print do Content Tree do Author -->

### 8.7 Página Final — Modo Padrão (localhost:4502)
Renderização completa da LP, deslogado/aba anônima, confirmando que o resultado funciona fora do modo de edição:

<!-- Cole aqui o print de localhost:4502/content/wknd/us/en/desafio-6-1.html em aba anônima -->

---
