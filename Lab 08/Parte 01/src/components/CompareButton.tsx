// =============================================================================
// COMPONENTE: CompareButton - Module 2: Real Estate React
// =============================================================================
// Botón para agregar/quitar una propiedad de la comparación.
// Maneja el estado global de comparación a través de localStorage.
// =============================================================================

import type React from 'react';
import { useState, useEffect } from 'react';
import { GitCompareArrows, X } from 'lucide-react';
import { Button } from '@/components/ui/button';

// Clave usada en localStorage para persistir las propiedades seleccionadas
const COMPARE_KEY = 'compare_properties';
const MAX_COMPARE = 3;

// =============================================================================
// UTILIDADES DE COMPARACIÓN
// Estas funciones se exportan para que otras partes de la app puedan
// leer/modificar la lista de comparación.
// =============================================================================

/**
 * Obtiene los IDs de propiedades actualmente en comparación.
 */
export function getCompareIds(): string[] {
  try {
    return JSON.parse(localStorage.getItem(COMPARE_KEY) ?? '[]');
  } catch {
    return [];
  }
}

/**
 * Guarda los IDs de comparación en localStorage.
 */
export function setCompareIds(ids: string[]): void {
  localStorage.setItem(COMPARE_KEY, JSON.stringify(ids));
  // Disparamos un evento personalizado para que otros componentes se enteren
  window.dispatchEvent(new Event('comparechange'));
}

/**
 * Agrega un ID a la lista de comparación.
 * Retorna false si ya hay 3 propiedades seleccionadas.
 */
export function addToCompare(id: string): boolean {
  const current = getCompareIds();
  if (current.length >= MAX_COMPARE || current.includes(id)) return false;
  setCompareIds([...current, id]);
  return true;
}

/**
 * Quita un ID de la lista de comparación.
 */
export function removeFromCompare(id: string): void {
  setCompareIds(getCompareIds().filter((i) => i !== id));
}

// =============================================================================
// PROPS DEL COMPONENTE
// =============================================================================

interface CompareButtonProps {
  propertyId: string;
}

// =============================================================================
// COMPONENTE
// =============================================================================

/**
 * Botón que permite al usuario agregar o quitar una propiedad de la comparación.
 *
 * ## Comportamiento:
 * - Si la propiedad NO está en comparación → botón "Comparar"
 * - Si la propiedad SÍ está en comparación → botón "Quitar"
 * - Si ya hay 3 propiedades y esta no está → botón deshabilitado con tooltip
 */
export function CompareButton({ propertyId }: CompareButtonProps): React.ReactElement {
  const [compareIds, setCompareIdsState] = useState<string[]>(getCompareIds);

  // Escuchamos cambios en el evento personalizado (desde otras tarjetas)
  useEffect(() => {
    const sync = () => setCompareIdsState(getCompareIds());
    window.addEventListener('comparechange', sync);
    return () => window.removeEventListener('comparechange', sync);
  }, []);

  const isSelected = compareIds.includes(propertyId);
  const isFull = compareIds.length >= MAX_COMPARE && !isSelected;

  const handleClick = () => {
    if (isSelected) {
      removeFromCompare(propertyId);
    } else {
      addToCompare(propertyId);
    }
    setCompareIdsState(getCompareIds());
  };

  return (
    <Button
      variant={isSelected ? 'default' : 'outline'}
      size="sm"
      onClick={handleClick}
      disabled={isFull}
      title={isFull ? 'Máximo 3 propiedades para comparar' : undefined}
      className="gap-1"
    >
      {isSelected ? (
        <>
          <X className="h-3 w-3" />
          Quitar
        </>
      ) : (
        <>
          <GitCompareArrows className="h-3 w-3" />
          {isFull ? 'Máx. 3' : 'Comparar'}
        </>
      )}
    </Button>
  );
}