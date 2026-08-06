import { CONFIG } from './config.js';
import { fetchInstructors } from './api.js';
import { initTheme } from './utils/theme.js';
import { 
  updateTelemetry, 
  hydrateAemImages,
  renderLoadingStateContainer,
  renderEmptyStateContainer,
  renderErrorStateContainer,
  getGridContainer,
  setupSearchEvents
} from './ui.js';
import { renderInstructorCard, renderModal } from './components.js';

const state = {
  allItems: [],
  searchTerm: ''
};

export async function loadInstructors() {
  const container = getGridContainer();
  
  updateTelemetry('todos', '...', false);
  const elEndpoint = document.getElementById('telemetry-endpoint');
  if (elEndpoint) {
    elEndpoint.textContent = CONFIG.QUERIES.INSTRUCTORS;
  }
  const elStatus = document.getElementById('telemetry-status');
  if (elStatus) elStatus.textContent = "Todos os Instrutores";

  renderLoadingStateContainer(container);

  try {
    const items = await fetchInstructors();
    const isMock = Boolean(items && items.isMock);

    if (!items || items.length === 0) {
      state.allItems = [];
      renderEmptyStateContainer(container, 'todos');
      updateTelemetry('todos', 0, isMock);
      return;
    }

    state.allItems = items;
    applySearchFilter();
    updateTelemetry('todos', items.length, isMock);
    hydrateAemImages();

  } catch (error) {
    renderErrorStateContainer(container, error);
    updateTelemetry('todos', 'Erro', true);
  }
}

function applySearchFilter() {
  const container = getGridContainer();
  if (state.allItems.length === 0) return;

  const filtered = state.allItems.filter(item => {
    const title = (item.nome || '').toLowerCase();
    const term = state.searchTerm.toLowerCase();
    return title.includes(term);
  });

  if (filtered.length === 0) {
    container.innerHTML = `
      <div class="state-container" role="status">
        <div class="state-icon">🔍</div>
        <h2 class="state-title">Nenhum resultado</h2>
        <p class="state-desc">Não encontramos instrutores para "${state.searchTerm}".</p>
      </div>
    `;
  } else {
    container.innerHTML = filtered.map(i => renderInstructorCard(i)).join('');
    hydrateAemImages();
  }
}

window.openModal = (id) => {
  const item = state.allItems.find(a => (a._id === id || a.nome === id));
  if (item) {
    const modalItem = {
      ...item,
      titulo: item.nome,
      imagem: item.foto || item.avatar,
      preco: 0,
      descricao: item.bio
    };
    
    const modalRoot = document.getElementById('modal-root');
    modalRoot.innerHTML = renderModal(modalItem);
    
    const priceEl = modalRoot.querySelector('.modal-price');
    if(priceEl) priceEl.style.display = 'none';
    const badgeEl = modalRoot.querySelector('.badge-level');
    if(badgeEl) {
      badgeEl.textContent = item.anosDeExperiencia ? `${item.anosDeExperiencia} ANOS EXP.` : 'INSTRUTOR';
    }

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
  
  setupSearchEvents((term) => {
    state.searchTerm = term;
    applySearchFilter();
  });

  loadInstructors();
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initializeApp);
} else {
  initializeApp();
}
