export const CONFIG = {
  AEM_HOST: 'http://localhost:4502',
  // Configure as credenciais do AEM Author (padrão admin:admin)
  AUTH_HEADER: 'Basic ' + btoa('admin:admin'),
  DEMO_IF_OFFLINE: true,
  QUERIES: {
    ALL: '/graphql/execute.json/wknd/lista-aventuras',
    FILTER: '/graphql/execute.json/wknd/lista-aventuras-filter',
    INSTRUCTORS: '/graphql/execute.json/wknd/listar-instrutores'
  },
  LEVELS: {
    todos: {
      label: 'Todas as Expedições',
      param: null
    },
    facil: {
      label: 'Nível Fácil',
      param: 'facil'
    },
    medio: {
      label: 'Nível Médio',
      param: 'medio'
    },
    dificil: {
      label: 'Nível Difícil',
      param: 'dificil'
    }
  }
};
