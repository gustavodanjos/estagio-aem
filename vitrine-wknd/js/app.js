import { CONFIG } from './config.js';
import { fetchAdventures, fetchMagazineArticles } from './api.js';
import { initTheme } from './utils/theme.js';
import { 
  updateTelemetry, 
  updateFilterButtons, 
  hydrateAemImages,
  renderLoadingStateContainer,
  renderEmptyStateContainer,
  renderErrorStateContainer,
  renderAdventures,
  getGridContainer,
  setupFilterEvents,
  setupSearchEvents
} from './ui.js';
import { renderModal } from './components.js';

const state = {
  currentLevel: 'todos',
  allItems: [],
  searchTerm: ''
};

export async function loadAdventures(level = 'todos', updateUrl = true) {
  state.currentLevel = level;
  const container = getGridContainer();

  updateFilterButtons(level);
  updateTelemetry(level, '...', false);

  if (updateUrl) {
    const paramUrl = CONFIG.LEVELS[level] && CONFIG.LEVELS[level].param 
      ? `?dificuldade=${encodeURIComponent(CONFIG.LEVELS[level].param)}` 
      : window.location.pathname;
    try {
      window.history.pushState({ level }, '', paramUrl);
    } catch (e) {
    }
  }

  renderLoadingStateContainer(container);

  try {
    const items = await fetchAdventures(level === 'todos' ? null : level);
    const isMock = Boolean(items && items.isMock);

    if (!items || items.length === 0) {
      state.allItems = [];
      renderEmptyStateContainer(container, level);
      updateTelemetry(level, 0, isMock);
      return;
    }

    state.allItems = items;
    applySearchFilter();
    updateTelemetry(level, items.length, isMock);
    hydrateAemImages();

  } catch (error) {
    renderErrorStateContainer(container, error);
    updateTelemetry(level, 'Erro', true);
  }
}

function applySearchFilter() {
  const container = getGridContainer();
  if (state.allItems.length === 0) return;

  const filtered = state.allItems.filter(item => {
    const title = (item.titulo || item.title || '').toLowerCase();
    const guide = (item.instrutor && item.instrutor.nome ? item.instrutor.nome : '').toLowerCase();
    const term = state.searchTerm.toLowerCase();
    return title.includes(term) || guide.includes(term);
  });

  if (filtered.length === 0) {
    container.innerHTML = `
      <div class="state-container" role="status">
        <div class="state-icon">🔍</div>
        <h2 class="state-title">Nenhum resultado</h2>
        <p class="state-desc">Não encontramos expedições para "${state.searchTerm}".</p>
      </div>
    `;
  } else {
    renderAdventures(container, filtered);
    hydrateAemImages();
  }
}

window.openModal = (id) => {
  const item = state.allItems.find(a => (a._id === id || a.titulo === id || a.title === id));
  if (item) {
    const modalRoot = document.getElementById('modal-root');
    modalRoot.innerHTML = renderModal(item);
    modalRoot.classList.add('active');
    
    document.addEventListener('keydown', handleEsc);
  }
};

window.closeModal = () => {
  const modalRoot = document.getElementById('modal-root');
  modalRoot.classList.remove('active');
  setTimeout(() => modalRoot.innerHTML = '', 300);
  document.removeEventListener('keydown', handleEsc);
};

function handleEsc(e) {
  if (e.key === 'Escape') {
    window.closeModal();
  }
}

function initializeApp() {
  initTheme();
  
  setupFilterEvents((level) => {
    loadAdventures(level, true);
  });

  setupSearchEvents((term) => {
    state.searchTerm = term;
    applySearchFilter();
  });

  window.addEventListener('popstate', (e) => {
    const level = (e.state && e.state.level) ? e.state.level : 'todos';
    loadAdventures(level, false);
  });

  const params = new URLSearchParams(window.location.search);
  const diffParam = params.get('dificuldade');
  
  let initialLevel = 'todos';
  if (diffParam) {
    for (const [key, value] of Object.entries(CONFIG.LEVELS)) {
      if (value.param === diffParam) {
        initialLevel = key;
        break;
      }
    }
  }

  loadAdventures(initialLevel, false);
  loadMagazineBlock();
}

async function loadMagazineBlock() {
  const container = document.getElementById('magazine-grid');
  if (!container) return;
  
  container.innerHTML = '<div class="spinner-tech"></div>';
  
  try {
    const items = await fetchMagazineArticles();
    const articles = items.filter(item => item.type === 'MATERIA' || !item.type);
    
    if (articles.length === 0) {
      container.innerHTML = `
        <div class="state-container" role="status" style="grid-column: 1 / -1;">
          <div class="state-icon">📰</div>
          <h2 class="state-title">Nenhuma matéria</h2>
          <p class="state-desc">Ainda não há publicações exclusivas do portal WKND no momento.</p>
        </div>`;
      return;
    }
    
    const fallbackImage = 'https://images.unsplash.com/photo-1493246507139-91e8fad9978e?auto=format&fit=crop&w=800&h=500&q=80';
    
    const htmlCards = articles.map(item => `
      <article class="adventure-card">
        <figure class="card-figure">
          <span class="badge-level badge--materia">MATÉRIA</span>
          <img src="${fallbackImage}" data-aem-src="${item.imagePath || ''}" class="card-img" alt="${item.title || ''}" loading="lazy"/>
        </figure>
        <div class="card-body">
          <h2 class="card-title">${item.title}</h2>
          <p class="card-desc">${item.description || ''}</p>
        </div>
        <footer class="guide-footer guide-footer--end">
          <a href="${item.path ? (item.path.startsWith('/content/') ? CONFIG.AEM_HOST + item.path : item.path) : '#'}" target="_blank" rel="noopener noreferrer" class="btn-details">Ler Matéria &rarr;</a>
        </footer>
      </article>
    `).join('');
    
    container.innerHTML = htmlCards;
    hydrateAemImages();
  } catch (error) {
    container.innerHTML = `
      <div class="state-container" role="alert" style="grid-column: 1 / -1;">
        <div class="state-icon">📡</div>
        <h2 class="state-title">Falha no Endpoint AEM (Revista)</h2>
        <p class="state-desc">${error.message}</p>
      </div>`;
  }
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initializeApp);
} else {
  initializeApp();
}
