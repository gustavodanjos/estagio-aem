import { CONFIG } from './config.js';

export function formatPrice(price) {
  if (price === undefined || price === null) return '$ ---';
  const number = Number(price);
  if (isNaN(number)) return '$ ---';
  return `$ ${number.toFixed(2)}`;
}

export function resolveAssetUrl(imagePath, fallback) {
  if (!imagePath) return fallback;
  if (typeof imagePath === 'object' && imagePath._path) {
    if (imagePath._path.startsWith('http')) {
      return imagePath._path;
    }
    return `${CONFIG.AEM_HOST}${imagePath._path}`;
  }
  if (typeof imagePath === 'string') {
    if (imagePath.startsWith('http')) {
      return imagePath;
    }
    if (imagePath.startsWith('/content')) {
      return `${CONFIG.AEM_HOST}${imagePath}`;
    }
    return imagePath;
  }
  return fallback;
}

export function normalizeBadge(difficulty) {
  const dif = String(difficulty || '').toLowerCase();
  if (dif === 'facil' || dif === 'fácil') {
    return { label: 'EXPEDIÇÃO • FÁCIL', cssClass: 'badge--facil' };
  }
  if (dif === 'medio' || dif === 'médio' || dif === 'mediano') {
    return { label: 'EXPEDIÇÃO • MÉDIO', cssClass: 'badge--medio' };
  }
  if (dif === 'dificil' || dif === 'difícil') {
    return { label: 'EXPEDIÇÃO • DIFÍCIL', cssClass: 'badge--dificil' };
  }
  return { label: dif.toUpperCase() || 'GERAL', cssClass: 'badge--medio' };
}
