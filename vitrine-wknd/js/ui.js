import { CONFIG } from './config.js';
import { fetchImageBlob } from './api.js';
import { 
  renderAdventureCard, 
  renderLoadingState, 
  renderEmptyState, 
  renderErrorState,
  renderHeroBanner
} from './components.js';

export function getGridContainer() {
  return document.getElementById('adventure-grid');
}

export function setupFilterEvents(callback) {
  const buttons = document.querySelectorAll('.filter-btn');
  buttons.forEach(btn => {
    btn.addEventListener('click', () => {
      const level = btn.getAttribute('data-level');
      callback(level);
    });
  });
}

export function setupSearchEvents(callback) {
  const searchInput = document.getElementById('search-input');
  if (!searchInput) return;

  let debounceTimer;
  searchInput.addEventListener('input', (e) => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      callback(e.target.value);
    }, 300);
  });
}

export function updateTelemetry(level, count, isMock = false) {
  const elEndpoint = document.getElementById('telemetry-endpoint');
  const elStatus = document.getElementById('telemetry-status');
  const elCount = document.getElementById('telemetry-count');
  const elDot = document.getElementById('telemetry-dot');
  const elBannerContainer = document.getElementById('demo-banner-container');

  if (elEndpoint) {
    if (level === 'todos') {
      elEndpoint.textContent = CONFIG.QUERIES.ALL;
    } else {
      elEndpoint.textContent = `${CONFIG.QUERIES.FILTER};dificuldade=${CONFIG.LEVELS[level]?.param || ''}`;
    }
  }

  if (elStatus && CONFIG.LEVELS[level]) {
    elStatus.textContent = CONFIG.LEVELS[level].label;
  }

  if (elCount) {
    elCount.textContent = count;
  }

  if (elDot) {
    if (isMock) {
      elDot.classList.remove('dot--online');
      elDot.classList.add('dot--offline');
      elDot.title = 'AEM Author Offline — Modo Demo (Mocks)';
    } else {
      elDot.classList.remove('dot--offline');
      elDot.classList.add('dot--online');
      elDot.title = 'AEM Author Online — Dados Reais via GraphQL';
    }
  }

  if (elBannerContainer) {
    if (isMock) {
      elBannerContainer.innerHTML = `
        <div class="demo-banner" role="alert">
          <span class="demo-banner-icon">⚠️</span>
          <div class="demo-banner-text">
            <strong>Modo de Demonstração Ativado — AEM Author Offline</strong>
            <span>O servidor Adobe Experience Manager (localhost:4502) não está respondendo. Os cards exibidos são mocks locais para verificação visual da interface.</span>
          </div>
        </div>
      `;
    } else {
      elBannerContainer.innerHTML = '';
    }
  }
}

export function updateFilterButtons(activeLevel) {
  const buttons = document.querySelectorAll('.filter-btn');
  buttons.forEach(btn => {
    const isActive = btn.getAttribute('data-level') === activeLevel;
    if (isActive) {
      btn.classList.add('active');
      btn.setAttribute('aria-selected', 'true');
    } else {
      btn.classList.remove('active');
      btn.setAttribute('aria-selected', 'false');
    }
  });
}

export async function hydrateAemImages() {
  const imgs = document.querySelectorAll('img[data-aem-src]');
  imgs.forEach((img) => {
    const assetUrl = img.getAttribute('data-aem-src');
    if (!assetUrl) return;
    
    // O fetch no AEM via localhost aciona erros severos no console do Chrome devido ao CORS.
    // Usamos o carregamento nativo (img.src) diretamente, o que contorna o preflight do CORS
    // e aproveita a sessão de cookie ativa no navegador do usuário automaticamente.
    const originalSrc = img.src;
    img.onerror = () => { img.src = originalSrc; };
    img.src = assetUrl;
  });

  const bgs = document.querySelectorAll('[data-aem-bg]');
  bgs.forEach((el) => {
    const assetUrl = el.getAttribute('data-aem-bg');
    if (!assetUrl) return;

    el.style.backgroundImage = `url('${assetUrl}')`;
  });
}

export function renderLoadingStateContainer(container) {
  container.innerHTML = renderLoadingState();
}

export function renderEmptyStateContainer(container, level) {
  container.innerHTML = renderEmptyState(level);
}

export function renderErrorStateContainer(container, error) {
  container.innerHTML = renderErrorState(error);
}

export function renderAdventures(container, items) {
  const heroContainer = document.getElementById('hero-banner-container');
  if (heroContainer) {
    if (items.length > 0) {
      heroContainer.innerHTML = renderHeroBanner(items[0]);
    } else {
      heroContainer.innerHTML = '';
    }
  }

  const htmlCards = items.map(item => renderAdventureCard(item)).join('');
  container.innerHTML = htmlCards;
}
