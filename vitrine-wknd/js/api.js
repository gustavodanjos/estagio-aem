import { CONFIG } from './config.js';
import { DEMO_ADVENTURES } from './mock-data.js';

export async function fetchAdventures(level = null) {
  let url = `${CONFIG.AEM_HOST}${CONFIG.QUERIES.ALL}`;

  if (level && CONFIG.LEVELS[level] && CONFIG.LEVELS[level].param) {
    const parameter = encodeURIComponent(CONFIG.LEVELS[level].param);
    url = `${CONFIG.AEM_HOST}${CONFIG.QUERIES.FILTER};dificuldade=${parameter}`;
  }

  try {
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Authorization': CONFIG.AUTH_HEADER,
        'Accept': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error(`AEM Author retornou erro HTTP ${response.status}: ${response.statusText}`);
    }

    const payload = await response.json();

    if (payload && payload.data && payload.data.aventuraList && payload.data.aventuraList.items) {
      const items = payload.data.aventuraList.items;
      items.isMock = false;
      console.info(`[AEM GraphQL - ONLINE] ${items.length} aventuras consultadas em tempo real do AEM Author (${url})`);
      return items;
    }

    if (payload && payload.data) {
      const firstKey = Object.keys(payload.data)[0];
      if (payload.data[firstKey] && Array.isArray(payload.data[firstKey].items)) {
        const items = payload.data[firstKey].items;
        items.isMock = false;
        console.info(`[AEM GraphQL - ONLINE] ${items.length} aventuras consultadas em tempo real do AEM Author (${url})`);
        return items;
      }
    }

    const emptyList = [];
    emptyList.isMock = false;
    return emptyList;
  } catch (error) {
    if (CONFIG.DEMO_IF_OFFLINE) {
      console.warn('[AEM Offline — Modo de Demonstração Ativado] Exibindo catálogo simulado WKND:', error.message);
      
      await new Promise(resolve => setTimeout(resolve, 350));

      const mockItems = level && CONFIG.LEVELS[level] && CONFIG.LEVELS[level].param
        ? DEMO_ADVENTURES.filter(item => item.dificuldade === CONFIG.LEVELS[level].param)
        : DEMO_ADVENTURES;
      
      const result = [...mockItems];
      result.isMock = true;
      return result;
    }

    console.error('[GraphQL Error] Falha na requisição ao AEM Author:', error);
    throw error;
  }
}

export async function fetchInstructors() {
  const url = `${CONFIG.AEM_HOST}${CONFIG.QUERIES.INSTRUCTORS}`;

  try {
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Authorization': CONFIG.AUTH_HEADER,
        'Accept': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error(`Erro HTTP ${response.status}: ${response.statusText}`);
    }

    const payload = await response.json();

    if (payload && payload.data && payload.data.instrutorList && payload.data.instrutorList.items) {
      const items = payload.data.instrutorList.items;
      items.isMock = false;
      return items;
    }
    
    return [];
  } catch (error) {
    if (CONFIG.DEMO_IF_OFFLINE) {
      console.warn('Fallback para mock de instrutores');
      const mockInstructors = [
        {
          _id: "inst1",
          nome: "Carlos Souza",
          anosDeExperiencia: 8,
          especialidades: ["Escalada", "Montanhismo"],
          bio: { html: "<p>Instrutor experiente de montanha.</p>" }
        },
        {
          _id: "inst2",
          nome: "João Silva",
          anosDeExperiencia: 5,
          especialidades: ["Rapel", "Trekking"],
          bio: { html: "<p>Especialista em trilhas de longo curso.</p>" }
        }
      ];
      mockInstructors.isMock = true;
      return mockInstructors;
    }
    throw error;
  }
}

export async function fetchMagazineArticles() {
  const url = `${CONFIG.AEM_HOST}${CONFIG.QUERIES.MAGAZINE}`;

  try {
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Authorization': CONFIG.AUTH_HEADER,
        'Accept': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error(`Erro HTTP ${response.status}: ${response.statusText}`);
    }

    const payload = await response.json();
    
    if (Array.isArray(payload)) {
      return payload;
    } else if (payload.articles && Array.isArray(payload.articles)) {
      return payload.articles;
    }
    
    return [];
  } catch (error) {
    if (CONFIG.DEMO_IF_OFFLINE) {
      console.warn('Fallback para mock de matérias (Revista)');
      return [
        { type: 'MATERIA', title: 'O Retorno de Chick Hicks', path: '#', imagePath: '', description: 'Mock fallback para offline', lastModified: '01/01/2026' }
      ];
    }
    throw error;
  }
}

export async function fetchImageBlob(assetUrl) {
  const response = await fetch(assetUrl, {
    method: 'GET',
    headers: {
      'Authorization': CONFIG.AUTH_HEADER
    }
  });
  
  if (response.ok) {
    return await response.blob();
  }
  
  throw new Error(`Erro ao buscar imagem: ${response.statusText}`);
}
