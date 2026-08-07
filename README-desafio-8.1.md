
# Catálogo de Aventuras (Content Fragments + GraphQL) — Desafio 8.1

**Desafio 8.1: Modelando o catálogo de aventuras com Content Fragment Models e GraphQL API**

---

## 1. Resumo

Este documento consolida a entrega do **Desafio 8.1**, focada na modelagem de conteúdo desacoplado de página para suportar múltiplos canais (app mobile e TV de loja) da WKND. A implementação abrangeu desde a criação de dois **Content Fragment Models** (`Instrutor` e `Aventura`) com validação de campos e referência cruzada entre eles, até a habilitação de um **endpoint GraphQL** dedicado (`aventuras-wknd`), a execução de três tipos de query no GraphiQL Explorer (listagem com dados aninhados, busca por caminho, e filtro parametrizado por variável), e a persistência dessas queries no servidor para consumo via requisição GET, incluindo passagem de variáveis pela própria URL.

---

## 2. Estrutura de Arquivos da Entrega (Mapeamento do Repositório)

```text
conf/wknd/settings/dam/cfm/models/
├── instrutor/
│   └── .content.xml  (Content Fragment Model: nome, bio, especialidades,
│                       foto, anosDeExperiencia com validação mín. 0)
└── aventura/
    └── .content.xml  (Content Fragment Model: titulo, descricao, dificuldade,
                        preco, instrutor [referência], imagem)

content/dam/wknd/
├── camping/                 (Content Fragment — Aventura)
├── escalada/     (Content Fragment — Aventura)
├── mergulho/  (Content Fragment — Aventura)
├── trilha/     (Content Fragment — Aventura)
├── intrutor-camping/        (Content Fragment — Instrutor)
├── instrutor-escalada/      (Content Fragment — Instrutor)
├── instrutor-trilha/            (Content Fragment — Instrutor)
└── instrutor-mergulho/      (Content Fragment — Instrutor)

conf/wknd/settings/graphql/
└── endpoints/
    └── aventuras-wknd  (GraphQL endpoint associado à configuração WKND Site)

conf/wknd/settings/graphql/persistentQueries/
├── lista-aventuras/
│   └── Query List persistida (query a)
├── lista-aventuras-byPath/
│   └── Query ByPath persistida (query b)
└── lista-aventuras-filter/
    └── Query com filtro por dificuldade via variável persistida (query c)
```

---

## 3. Passo a Passo da Implementação

### 3.1 Modelagem de Conteúdo (Content Fragment Models)

- **Model `Instrutor`**: campos `nome` (texto), `bio` (rich text), `especialidades` (enumeração múltipla), `foto` (referência a asset) e `anosDeExperiencia` (número, validação mínima 0).
- **Model `Aventura`**: campos `titulo` (texto), `descricao` (rich text), `dificuldade` (enumeração), `preco` (número), `instrutor` (referência ao Content Fragment do tipo `Instrutor`) e `imagem` (referência a asset).
- Criados **4 fragments de Aventura** (Camping, Escalada, Mergulho, Trilha) e **3 fragments de Instrutor**, com referências cruzadas reais — cada Aventura aponta para um Instrutor existente via campo de referência.

### 3.2 Habilitação e Testes do Endpoint GraphQL

- Endpoint `aventuras-wknd` criado em **Tools → General → GraphQL**, associado à configuração de projeto **WKND Site** (`/conf/wknd`), restringindo o schema aos Models desse projeto.
- Testes realizados via **GraphiQL Explorer**, acessado diretamente por `/aem/graphiql.html` no Author (sem instância Publish local configurada):
  - **(a) Query List** — `aventuraList` retornando todos os itens com os campos do instrutor aninhados (`nome`, `anosDeExperiencia`).
  - **(b) Query ByPath** — `aventuraByPath` buscando um fragment específico pelo caminho (`/content/dam/wknd/camping`).
  - **(c) Query com filtro por dificuldade via variável** — `aventuraList(filter: ...)` usando `$dificuldade` como variável no painel Query Variables, testada com o valor `"facil"`.

### 3.3 Persisted Queries

- **GraphQL Persisted Queries habilitada** na Cloud Configuration do projeto WKND Site (**Tools → General → Configuration Browser → WKND Site → Properties**) — pré-requisito sem o qual o botão de salvar queries no GraphiQL não fica disponível.
- Query (a) persistida como `lista-aventuras`, validada via GET em `/graphql/execute.json/wknd/lista-aventuras`.
- Query (b) persistida como `lista-aventuras-byPath`, validada via GET em `/graphql/execute.json/wknd/lista-aventuras-byPath`.
- Query (c) persistida como `lista-aventuras-filter`, validada via GET com a variável sobrescrita diretamente na URL: `/graphql/execute.json/wknd/lista-aventuras-filter;dificuldade=facil`.

---

## 4. Decisões Técnicas e de Arquitetura Tomadas

### 4.1 Uso de Enumeração para Dificuldade

- **Decisão:** O campo `dificuldade` do Model `Aventura` foi definido como enumeração de valores fixos (ex.: fácil / médio / difícil), em vez de texto livre.
- **Justificativa:** Garante que o filtro por dificuldade nas queries GraphQL sempre funcione com valores previsíveis e consistentes entre todos os fragments, eliminando risco de erro de digitação do autor de conteúdo (ex.: "Fácil" vs "facil" vs "Facil") que quebraria o filtro silenciosamente.

### 4.2 Referência ao invés de Duplicação de Dados do Instrutor

- **Decisão:** O campo `instrutor` no Model `Aventura` é uma referência a um Content Fragment do tipo `Instrutor`, em vez de campos duplicados (nome, bio, foto) dentro do próprio fragment de Aventura.
- **Justificativa:** Mantém uma única fonte de verdade por instrutor — atualizar a bio ou os anos de experiência de um instrutor uma única vez propaga automaticamente para todas as aventuras vinculadas a ele, evitando inconsistência de dados entre fragments.

---

## 5. Dificuldades Encontradas e Soluções

1. **Nomes técnicos de campos GraphQL gerados incorretamente a partir de acentuação**

   - **Problema:** Campos nomeados com acento no Content Fragment Model (ex.: "Título", "Anos de Experiência") geraram nomes técnicos no schema GraphQL removendo a letra acentuada inteira em vez de substituí-la por uma versão sem acento — resultando em `ttulo` em vez de `titulo`, e `anosDeExperincia` em vez de `anosDeExperiencia`.
   - **Solução:** Edição do "Property Name" de cada campo afetado diretamente no Content Fragment Model para a grafia correta sem acento. Como a renomeação do campo não migra automaticamente o conteúdo já salvo sob o nome antigo, foi necessário reabrir cada Content Fragment afetado (4 de Aventura, 3 de Instrutor) e resalvar os valores para que passassem a ser persistidos sob a nova propriedade.
2. **Painel de Persisted Queries indisponível no GraphiQL ("Not enabled")**

   - **Problema:** O painel lateral "Persisted Queries" do GraphiQL Explorer exibia a mensagem "Not enabled — Try enabling Persisted Queries for this configuration", sem nenhum botão para adicionar uma nova query persistida.
   - **Solução:** A permissão fica associada à Cloud Configuration do projeto, não ao endpoint em si. Habilitada em **Tools → General → Configuration Browser**, na configuração **WKND Site**, marcando a opção **GraphQL Persisted Queries** nas propriedades da configuração. Após habilitar e recarregar o GraphiQL, o botão **+** para salvar queries passou a aparecer.

---

## 6. Critérios de Aceite

- [x] **Models com validação e referência aninhada funcionando:** Model `Instrutor` com validação mínima 0 em `anosDeExperiencia`; Model `Aventura` referenciando `Instrutor` e retornando os campos aninhados corretamente nas queries.
- [x] **As 3 queries rodando no GraphiQL:** List (a), ByPath (b) e filtro por dificuldade via variável (c), todas testadas com resultado correto.
- [x] **Persisted query respondendo via GET:** Query (a), (b) e (c) validadas via GET, incluindo passagem de variável pela URL.
- [x] **README explica cada decisão de modelagem:** seção 4 detalha o porquê da enumeração em `dificuldade`, da referência em `instrutor` e do escopo do endpoint.

---

## 7. Extras

1. **Persistência das 3 queries, além da query List mínima exigida** — `lista-aventuras`, `lista-aventuras-byPath` e `lista-aventuras-filter` foram todas persistidas, não apenas a query (a).
2. **Validação de variável via parâmetro de URL em GET** — confirmado que `$dificuldade` de uma persisted query pode ser sobrescrita diretamente na URL de execução, demonstrando uso realista de persisted queries parametrizadas por um cliente (ex.: app mobile filtrando sem reenviar a query inteira).

---

## 8. Evidências Técnicas e Visuais

### 8.1 Content Fragment Models (Instrutor e Aventura)

*(Campos do Content Fragments model - Instrutor)*
> <img width="100%" src="https://github.com/user-attachments/assets/a7e115ba-0e8f-41e9-a1b2-8b61344491ae" />

*(Campos do Content Fragments model - Aventura)*
> <img width="100%" src="https://github.com/user-attachments/assets/60dd1bbb-defa-4d1a-b0a6-dc7db64fe6dc" />

> <img width="100%" src="https://github.com/user-attachments/assets/38dd5ed6-eebe-4f52-a0da-f3e9ac0a04e0" />

*(Editor do Content Fragment **Aventura** mostrando os campos preenchidos e a referência ao instrutor)*
> <img width="100%" src="https://github.com/user-attachments/assets/7c99ac79-ce1b-45ef-b604-9e2e12a70e9f" />

*(Editor do Content Fragment **Instrutor** mostrando os campos preechidos)*
> <img width="100%" src="https://github.com/user-attachments/assets/cc414d4f-1e14-4469-8c4f-0a112556c4a0" />


### 8.2 Queries no GraphiQL Explorer

*(Query List, ByPath e filtro por dificuldade rodando com resultado no painel direito)*

*(List)*
> <img width="100%" src="https://github.com/user-attachments/assets/8f6fb39a-f159-4419-ad5e-6e1f16e80a27" />

*(ByPath)*
> <img width="100%" src="https://github.com/user-attachments/assets/55adedf1-91e9-438f-b12f-0b12cf037138" />

*(Filter - facil)*
> <img width="100%" src="https://github.com/user-attachments/assets/fc06eea5-809d-4737-887a-1d9f613667b2" />

### 8.3 Configuração de Persisted Queries

*(Configuration Browser com a permissão GraphQL Persisted Queries habilitada)*
> <img width="100%" src="https://github.com/user-attachments/assets/f903ac83-4a83-4a58-a2bc-d4c28315a74b" />
> <img width="380" height="275" alt="image" src="https://github.com/user-attachments/assets/02bdfbdb-3b00-48a5-93b5-91fdd6595599" />


### 8.4 Persisted Query respondendo via GET

> <img width="775" height="830" alt="image" src="https://github.com/user-attachments/assets/51ba7cf0-66f5-47d1-9b32-24868cb4a475" />
> <img width="775" height="830" alt="image" src="https://github.com/user-attachments/assets/a4710c25-a365-463d-b12b-b374aceb17ad" />
> <img width="775" height="830" alt="image" src="https://github.com/user-attachments/assets/dc89c435-4df2-476e-b086-90b52174bdb9" />
