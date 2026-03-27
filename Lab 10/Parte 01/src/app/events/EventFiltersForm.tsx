// =============================================================================
// COMPONENTE EVENT FILTERS FORM - Module 4: Event Pass
// =============================================================================
// Formulario de filtros mejorado con interactividad.
//
// ## Client Component
// Lo hemos convertido a 'use client' para permitir:
// 1. Auto-submit al cambiar selectores (UX más fluida)
// 2. Mantener la URL sincronizada sin recargas completas
// 3. Búsqueda con debounce para escribir y filtrar automáticamente
//
// ## Progressive Enhancement
// Aunque usamos JS para mejorar la UX, el formulario sigue usando
// method="GET" y action="/events", por lo que es robusto y estándar.
// =============================================================================

'use client';

import { Search } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  EVENT_CATEGORIES,
  CATEGORY_LABELS,
  EVENT_STATUSES,
  STATUS_LABELS,
  type EventCategory,
} from '@/types/event';
import { useRef, useState, useEffect, useCallback } from 'react';
import { useDebounce } from '@/hooks/use-debounce';
import { useRouter, usePathname, useSearchParams } from 'next/navigation';

interface EventFiltersFormProps {
  currentFilters: {
    search?: string;
    category?: EventCategory;
    status?: string;
    priceMax?: number;
  };
}

/**
 * Construye un nuevo URLSearchParams omitiendo claves vacías/undefined
 * para mantener la URL limpia.
 */
function buildParams(filters: {
  search?: string;
  category?: string;
  status?: string;
  priceMax?: string;
}): URLSearchParams {
  const params = new URLSearchParams();
  if (filters.search)   params.set('search',   filters.search);
  if (filters.category) params.set('category', filters.category);
  if (filters.status)   params.set('status',   filters.status);
  if (filters.priceMax) params.set('priceMax', filters.priceMax);
  return params;
}

/**
 * Formulario de filtros de eventos (Client Component).
 */
export function EventFiltersForm({ currentFilters }: EventFiltersFormProps): React.ReactElement {
  const router       = useRouter();
  const pathname     = usePathname();
  const searchParams = useSearchParams(); // lee la URL actual (no los props)

  // ── Estado local ─────────────────────────────────────────────────────────────
  const [searchTerm, setSearchTerm] = useState(currentFilters.search ?? '');
  const [category,   setCategory]   = useState(currentFilters.category ?? '');
  const [status,     setStatus]     = useState(currentFilters.status ?? '');
  const [priceMax,   setPriceMax]   = useState(currentFilters.priceMax?.toString() ?? '');

  // Valor debounced del campo de texto (500 ms)
  const debouncedSearch = useDebounce(searchTerm, 500);

  // Evita disparar la navegación en el primer render
  const isFirstRender = useRef(true);

  // ── Helper: navega con router.push sin recarga completa ──────────────────────
  const applyFilters = useCallback(
    (overrides: { search?: string; category?: string; status?: string; priceMax?: string }) => {
      const params = buildParams({
        search: searchTerm,
        category,
        status,
        priceMax,
        ...overrides, // el campo que acaba de cambiar sobreescribe el estado local
      });
      const query = params.toString();
      // router.push actualiza la URL y re-renderiza el Server Component
      // { scroll: false } evita que la página salte al inicio
      router.push(`${pathname}${query ? `?${query}` : ''}`, { scroll: false });
    },
    [router, pathname, searchTerm, category, status, priceMax],
  );

  // ── Auto-apply cuando el texto debounced cambia ──────────────────────────────
  useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false;
      return;
    }
    applyFilters({ search: debouncedSearch });
  }, [debouncedSearch]); // eslint-disable-line react-hooks/exhaustive-deps

  // ── Handlers ─────────────────────────────────────────────────────────────────
  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(e.target.value);
    // La navegación la dispara el efecto de debounce
  };

  const handleCategoryChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value;
    setCategory(value);
    applyFilters({ category: value });
  };

  const handleStatusChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value;
    setStatus(value);
    applyFilters({ status: value });
  };

  const handlePriceMaxChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value;
    setPriceMax(value);
    applyFilters({ priceMax: value });
  };

  // Botón "Buscar" explícito (accesibilidad / submit manual)
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    applyFilters({ search: searchTerm });
  };

  // Limpiar todos los filtros → navega a /events sin query params
  const handleClear = () => {
    setSearchTerm('');
    setCategory('');
    setStatus('');
    setPriceMax('');
    router.push(pathname, { scroll: false });
  };

  // Hay filtros activos si la URL tiene al menos un param
  const hasFilters = searchParams.toString().length > 0;

  // ── Render ───────────────────────────────────────────────────────────────────
  return (
    <div className="space-y-4 rounded-lg border bg-card p-4">
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Búsqueda */}
        <div className="flex gap-2">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              name="search"
              placeholder="Buscar eventos..."
              value={searchTerm}
              onChange={handleSearchChange}
              className="pl-9"
            />
          </div>
          <Button type="submit">Buscar</Button>
        </div>

        {/* Filtros adicionales */}
        <div className="flex flex-wrap gap-4">
          {/* Categoría */}
          <select
            name="category"
            value={category}
            onChange={handleCategoryChange}
            className="h-10 w-[180px] rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2"
          >
            <option value="">Todas las categorías</option>
            {EVENT_CATEGORIES.map((cat) => (
              <option key={cat} value={cat}>
                {CATEGORY_LABELS[cat]}
              </option>
            ))}
          </select>

          {/* Status */}
          <select
            name="status"
            value={status}
            onChange={handleStatusChange}
            className="h-10 w-[180px] rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2"
          >
            <option value="">Todos los estados</option>
            {EVENT_STATUSES.map((s) => (
              <option key={s} value={s}>
                {STATUS_LABELS[s]}
              </option>
            ))}
          </select>

          {/* Precio máximo */}
          <select
            name="priceMax"
            value={priceMax}
            onChange={handlePriceMaxChange}
            className="h-10 w-[180px] rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2"
          >
            <option value="">Cualquier precio</option>
            <option value="0">Gratis</option>
            <option value="25">Hasta $25</option>
            <option value="50">Hasta $50</option>
            <option value="100">Hasta $100</option>
            <option value="200">Hasta $200</option>
          </select>

          {/* Botón limpiar — solo visible cuando hay filtros activos en la URL */}
          {hasFilters && (
            <Button type="button" variant="ghost" onClick={handleClear} className="gap-2">
              Borrar todo
            </Button>
          )}
        </div>
      </form>
    </div>
  );
}