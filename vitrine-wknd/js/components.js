import { CONFIG } from './config.js';
import { formatPrice, resolveAssetUrl, normalizeBadge } from './utils.js';

export function renderAdventureCard(item) {
  const title = item.titulo || item.title || 'Aventura WKND';
  
  const fallbackImage = 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&h=500&q=80';
  const imgUrl = resolveAssetUrl(item.imagem || item.imagemPrincipal || item.asset, fallbackImage);
  
  const fallbackGuide = 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&crop=faces&w=200&h=200&q=80';
  const guidePhoto = resolveAssetUrl(item.instrutor && (item.instrutor.foto || item.instrutor.avatar), fallbackGuide);
  
  const guideName = (item.instrutor && item.instrutor.nome) ? item.instrutor.nome : 'Guia WKND';
  const guideExp = (item.instrutor && item.instrutor.anosDeExperiencia !== undefined) 
    ? `${item.instrutor.anosDeExperiencia}+ ANOS EXP.` 
    : '5+ ANOS EXP.';

  const badge = normalizeBadge(item.dificuldade);
  const priceFormatted = formatPrice(item.preco);

  const desc = (item.descricao && item.descricao.plaintext)
    ? item.descricao.plaintext 
    : (typeof item.descricao === 'string' ? item.descricao : 'Expedição de alta montanha e natureza selvagem.');

  return `
    <article class="adventure-card" aria-labelledby="adventure-${item._id || 'card'}">
      <figure class="card-figure">
        <span class="badge-level ${badge.cssClass}">${badge.label}</span>
        <img 
          src="${fallbackImage}" 
          data-aem-src="${imgUrl}" 
          alt="Fotografia de expedição: ${title}" 
          class="card-img" 
          width="800"
          height="500"
          loading="lazy" 
        />
      </figure>
      
      <div class="card-body">
        <h2 id="adventure-${item._id || 'card'}" class="card-title">${title}</h2>
        <p class="card-desc">${desc}</p>
        
        <div class="card-price-row">
          <span class="price-label">Investimento</span>
          <strong class="price-value">${priceFormatted}</strong>
        </div>
      </div>

      <footer class="guide-footer">
        <div class="guide-info">
          <img 
            src="${fallbackGuide}" 
            data-aem-src="${guidePhoto}" 
            alt="Foto do Instrutor ${guideName}" 
            class="guide-avatar" 
            width="200"
            height="200"
          />
          <div class="guide-meta">
            <span class="guide-role">Guia WKND</span>
            <strong class="guide-name">${guideName}</strong>
          </div>
        </div>
        <button class="btn-details" onclick="window.openModal('${item._id || item.titulo || item.title}')" aria-label="Ver detalhes de ${title}">Ver detalhes &rarr;</button>
      </footer>
    </article>
  `;
}

export function renderLoadingState() {
  return `
    <div class="state-container" id="loading-state" role="status">
      <div class="spinner-tech"></div>
      <h2 class="state-title">Sincronizando com AEM Author</h2>
      <p class="state-desc">
        Executando Persisted Query GraphQL e processando Content Fragments da WKND...
      </p>
    </div>
  `;
}

export function renderEmptyState(selectedLevel) {
  const levelLabel = selectedLevel && CONFIG.LEVELS[selectedLevel] 
    ? CONFIG.LEVELS[selectedLevel].label 
    : 'selecionado';

  return `
    <div class="state-container" id="empty-state" role="status">
      <div class="state-icon">🏔️</div>
      <h2 class="state-title">Nenhum Registro no Catálogo</h2>
      <p class="state-desc">
        Não foram localizadas expedições publicadas para o filtro técnico <strong>${levelLabel}</strong> no servidor Adobe Experience Manager.
      </p>
    </div>
  `;
}

export function renderErrorState(error) {
  return `
    <div class="state-container" id="error-state" role="alert">
      <div class="state-icon">📡</div>
      <h2 class="state-title">Falha no Endpoint AEM GraphQL</h2>
      <p class="state-desc">
        Não foi possível restabelecer a telemetria com <code>${CONFIG.AEM_HOST}</code>.<br/>
        <small style="color: var(--color-signal-orange); font-family: var(--font-tech); display: block; margin-top: 0.5rem;">
          ERRO: ${error.message || 'CORS ou Servidor Indisponível'}
        </small>
      </p>
    </div>
  `;
}

export function renderHeroBanner(item) {
  if (!item) return '';
  const title = item.titulo || item.title || 'Aventura em Destaque';
  const fallbackImage = 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1600&h=600&q=80';
  const imgUrl = resolveAssetUrl(item.imagem || item.imagemPrincipal || item.asset, fallbackImage);
  const priceFormatted = formatPrice(item.preco);
  const badge = normalizeBadge(item.dificuldade);

  return `
    <div class="hero-banner" style="background-image: url('${fallbackImage}')" data-aem-bg="${imgUrl}">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <span class="hero-badge">Aventura em Destaque</span>
        <h2 class="hero-title">${title}</h2>
        <div class="hero-meta">
          <span class="badge-level ${badge.cssClass}">${badge.label}</span>
          <strong class="hero-price">${priceFormatted}</strong>
        </div>
        <button class="hero-btn" onclick="window.openModal('${item._id || item.titulo || item.title}')">Ver Experiência Completa &rarr;</button>
      </div>
    </div>
  `;
}

export function renderModal(item) {
  const isInstructor = !!item.nome;
  const title = item.titulo || item.title || item.nome;
  
  const fallbackImage = isInstructor 
    ? 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&crop=faces&w=800&h=500&q=80'
    : 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&h=500&q=80';
    
  const imgUrl = resolveAssetUrl(item.imagem || item.imagemPrincipal || item.asset || item.foto || item.avatar, fallbackImage);
  
  const priceFormatted = item.preco ? formatPrice(item.preco) : '';
  const badgeLabel = item.dificuldade ? normalizeBadge(item.dificuldade).label : (item.anosDeExperiencia ? `${item.anosDeExperiencia}+ ANOS EXP.` : 'GUIA WKND');
  const badgeClass = item.dificuldade ? normalizeBadge(item.dificuldade).cssClass : 'badge--medio';
  
  const desc = (item.descricao && item.descricao.html) 
    ? item.descricao.html 
    : (item.descricao && item.descricao.plaintext) 
      ? `<p>${item.descricao.plaintext}</p>` 
      : (item.bio && item.bio.html) ? item.bio.html : `<p>${item.descricao || item.bio || ''}</p>`;

  let specialtiesHtml = '';
  if (isInstructor && item.especialidades) {
    const specialties = Array.isArray(item.especialidades) ? item.especialidades : 
                        (typeof item.especialidades === 'string' ? [item.especialidades] : []);
    if (specialties.length > 0) {
      const badges = specialties.map(s => `<span class="badge-level badge--facil">${s}</span>`).join('');
      specialtiesHtml = `
        <div class="modal-specialties" style="display: flex; flex-wrap: wrap; gap: 0.5rem; margin-bottom: 1.5rem;">
          <span style="font-size: 0.8rem; color: var(--color-granite-muted); text-transform: uppercase; letter-spacing: 0.05em; display: flex; align-items: center; margin-right: 0.5rem;">Especialidades:</span>
          ${badges}
        </div>
      `;
    }
  }

  return `
    <div class="modal-backdrop" onclick="window.closeModal()"></div>
    <div class="modal-content" role="dialog" aria-modal="true" aria-labelledby="modal-title">
      <button class="modal-close" onclick="window.closeModal()" aria-label="Fechar">&times;</button>
      <div class="modal-header">
        <img src="${imgUrl}" alt="${title}" class="modal-hero-img ${isInstructor ? 'instructor-modal-img' : ''}" />
        <div class="modal-header-content">
          <span class="badge-level ${badgeClass}">${badgeLabel}</span>
          <h2 id="modal-title" class="modal-title">${title}</h2>
          ${priceFormatted ? `<strong class="modal-price">${priceFormatted}</strong>` : ''}
        </div>
      </div>
      <div class="modal-body">
        ${specialtiesHtml}
        <div class="modal-desc prose">
          ${desc}
        </div>
      </div>
    </div>
  `;
}

export function renderInstructorCard(item) {
  const name = item.nome || 'Guia WKND';
  const exp = item.anosDeExperiencia !== undefined ? `${item.anosDeExperiencia}+ ANOS EXP.` : '5+ ANOS EXP.';
  
  const fallbackImage = 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&crop=faces&w=400&h=400&q=80';
  const imgUrl = resolveAssetUrl(item.foto || item.avatar, fallbackImage);
  
  const specialties = Array.isArray(item.especialidades) ? item.especialidades : 
                      (typeof item.especialidades === 'string' ? [item.especialidades] : ['Natureza']);
  
  const badges = specialties.map(s => `<span class="badge-level badge--medio">${s}</span>`).join('');
  
  return `
    <article class="adventure-card instructor-card" style="align-items: center; text-align: center; padding-bottom: 1.5rem;">
      <figure class="card-figure" style="width: 120px; height: 120px; border-radius: 50%; margin: 2rem auto 0; border: 3px solid var(--color-signal-orange); overflow: hidden; aspect-ratio: auto;">
        <img 
          src="${fallbackImage}" 
          data-aem-src="${imgUrl}" 
          alt="Foto de perfil: ${name}" 
          class="card-img" 
          width="400"
          height="400"
          loading="lazy" 
        />
      </figure>
      <div class="card-body" style="padding-top: 1rem;">
        <h2 class="card-title">${name}</h2>
        <span class="guide-exp" style="display: inline-block; margin-top: 0.5rem;" title="Anos de experiência comprovada">${exp}</span>
        <div class="instructor-badges" style="display: flex; justify-content: center; flex-wrap: wrap; gap: 0.5rem; margin-top: 1rem;">
          ${badges}
        </div>
      </div>
      <button class="btn-details" style="margin: 0 auto; margin-top: auto;" onclick="window.openModal('${item._id || item.nome}')">Ver detalhes &rarr;</button>
    </article>
  `;
}
