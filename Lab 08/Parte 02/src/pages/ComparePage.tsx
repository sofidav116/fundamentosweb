// =============================================================================
// PÁGINA: ComparePage - Module 2: Real Estate React
// =============================================================================
// Página que muestra una tabla comparativa de hasta 3 propiedades.
//
// ## Lógica principal:
// 1. Lee los IDs de comparación desde localStorage (vía getCompareIds)
// 2. Busca los datos de cada propiedad en localStorage (vía getProperty)
// 3. Renderiza una tabla con métricas clave
// 4. Destaca el mejor valor en cada categoría (precio más bajo, área más grande, etc.)
// =============================================================================

import type React from 'react';
import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { X, ArrowLeft, GitCompareArrows } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { getCompareIds, removeFromCompare } from '@/components/CompareButton';
import { getPropertyById } from '@/lib/storage';
import type { Property } from '@/types/property';
import { PROPERTY_TYPE_LABELS, OPERATION_TYPE_LABELS } from '@/types/property';

// =============================================================================
// UTILIDADES
// =============================================================================

/**
 * Formatea un número como moneda en USD.
 */
function formatPrice(price: number): string {
  return new Intl.NumberFormat('es-GT', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 0,
  }).format(price);
}

/**
 * Formatea el precio por m².
 */
function pricePerM2(property: Property): number {
  return Math.round(property.price / property.area);
}

// =============================================================================
// TIPOS PARA LAS FILAS DE LA TABLA
// =============================================================================

interface MetricRow {
  label: string;
  /** Extrae el valor numérico (para comparación de mejor/peor) */
  getValue: (p: Property) => number;
  /** Renderiza el valor para mostrar al usuario */
  render: (p: Property) => React.ReactNode;
  /** true = más bajo es mejor (precio), false = más alto es mejor (área, habitaciones) */
  lowerIsBetter: boolean;
}

// =============================================================================
// DEFINICIÓN DE MÉTRICAS
// Cada fila de la tabla es una métrica que se compara entre propiedades.
// =============================================================================

const METRICS: MetricRow[] = [
  {
    label: 'Precio',
    getValue: (p) => p.price,
    render: (p) => formatPrice(p.price),
    lowerIsBetter: true,
  },
  {
    label: 'Precio / m²',
    getValue: (p) => pricePerM2(p),
    render: (p) => `${formatPrice(pricePerM2(p))} / m²`,
    lowerIsBetter: true,
  },
  {
    label: 'Área',
    getValue: (p) => p.area,
    render: (p) => `${p.area.toLocaleString()} m²`,
    lowerIsBetter: false,
  },
  {
    label: 'Habitaciones',
    getValue: (p) => p.bedrooms,
    render: (p) => p.bedrooms,
    lowerIsBetter: false,
  },
  {
    label: 'Baños',
    getValue: (p) => p.bathrooms,
    render: (p) => p.bathrooms,
    lowerIsBetter: false,
  },
  {
    label: 'Ciudad',
    getValue: () => 0, // No numérico, no se destaca
    render: (p) => p.city,
    lowerIsBetter: false,
  },
  {
    label: 'Operación',
    getValue: () => 0,
    render: (p) => OPERATION_TYPE_LABELS[p.operationType],
    lowerIsBetter: false,
  },
  {
    label: 'Tipo',
    getValue: () => 0,
    render: (p) => PROPERTY_TYPE_LABELS[p.propertyType],
    lowerIsBetter: false,
  },
];

// =============================================================================
// COMPONENTE PRINCIPAL
// =============================================================================

/**
 * Página de comparación de propiedades.
 */
export function ComparePage(): React.ReactElement {
  const [properties, setProperties] = useState<Property[]>([]);

  // Carga las propiedades seleccionadas desde localStorage
  const loadProperties = () => {
    const ids = getCompareIds();
    const loaded = ids
      .map((id) => getPropertyById(id) ?? null)
      .filter((p): p is Property => p !== null);
    setProperties(loaded);
  };

  useEffect(() => {
    loadProperties();
    // Recargamos si cambia la lista de comparación (ej. desde otra pestaña)
    window.addEventListener('comparechange', loadProperties);
    return () => window.removeEventListener('comparechange', loadProperties);
  }, []);

  // -------------------------------------------------------------------------
  // ESTADO VACÍO
  // -------------------------------------------------------------------------
  if (properties.length === 0) {
    return (
      <div className="container mx-auto px-4 py-16 text-center">
        <GitCompareArrows className="h-16 w-16 mx-auto text-muted-foreground mb-4" />
        <h1 className="text-2xl font-bold mb-2">No hay propiedades para comparar</h1>
        <p className="text-muted-foreground mb-6">
          Selecciona hasta 3 propiedades desde la página de inicio usando el botón "Comparar".
        </p>
        <Button asChild>
          <Link to="/">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Volver al inicio
          </Link>
        </Button>
      </div>
    );
  }

  // -------------------------------------------------------------------------
  // FUNCIÓN AUXILIAR: Determinar si un valor es el "mejor" de su columna
  // -------------------------------------------------------------------------
  const isBest = (metric: MetricRow, property: Property): boolean => {
    // Métricas no numéricas (ciudad, tipo, operación) no se destacan
    if (metric.label === 'Ciudad' || metric.label === 'Operación' || metric.label === 'Tipo') {
      return false;
    }
    const values = properties.map(metric.getValue);
    const best = metric.lowerIsBetter ? Math.min(...values) : Math.max(...values);
    return metric.getValue(property) === best;
  };

  // -------------------------------------------------------------------------
  // RENDER PRINCIPAL
  // -------------------------------------------------------------------------
  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="flex items-center gap-4 mb-8">
        <Button variant="outline" size="sm" asChild>
          <Link to="/">
            <ArrowLeft className="h-4 w-4 mr-1" />
            Volver
          </Link>
        </Button>
        <div>
          <h1 className="text-3xl font-bold">Comparar Propiedades</h1>
          <p className="text-muted-foreground">
            {properties.length} {properties.length === 1 ? 'propiedad seleccionada' : 'propiedades seleccionadas'}
            {' '}· Máximo 3
          </p>
        </div>
      </div>

      {/* Leyenda */}
      <div className="flex items-center gap-2 mb-4 text-sm text-muted-foreground">
        <span className="inline-block w-3 h-3 rounded-full bg-green-500/20 border border-green-500" />
        Mejor valor en la categoría
      </div>

      {/* Tabla comparativa */}
      <div className="overflow-x-auto rounded-lg border">
        <table className="w-full">
          <thead>
            <tr className="border-b bg-muted/50">
              {/* Columna de etiquetas */}
              <th className="text-left px-4 py-3 font-semibold text-sm w-32 min-w-[8rem]">
                Métricas
              </th>

              {/* Una columna por propiedad */}
              {properties.map((property) => (
                <th key={property.id} className="px-4 py-3 text-left min-w-[200px]">
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <Link
                        to={`/property/${property.id}`}
                        className="font-semibold text-sm hover:text-primary hover:underline line-clamp-2"
                      >
                        {property.title}
                      </Link>
                      <p className="text-xs text-muted-foreground mt-0.5">{property.address}</p>
                    </div>
                    {/* Botón para quitar de la comparación */}
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-6 w-6 shrink-0"
                      onClick={() => {
                        removeFromCompare(property.id);
                        loadProperties();
                      }}
                      title="Quitar de comparación"
                    >
                      <X className="h-3 w-3" />
                    </Button>
                  </div>
                </th>
              ))}
            </tr>
          </thead>

          <tbody>
            {METRICS.map((metric, idx) => (
              <tr
                key={metric.label}
                className={idx % 2 === 0 ? 'bg-background' : 'bg-muted/20'}
              >
                {/* Etiqueta de la métrica */}
                <td className="px-4 py-3 text-sm font-medium text-muted-foreground">
                  {metric.label}
                </td>

                {/* Valor de cada propiedad */}
                {properties.map((property) => {
                  const best = isBest(metric, property);
                  return (
                    <td
                      key={property.id}
                      className={`px-4 py-3 text-sm font-medium ${
                        best ? 'bg-green-500/10 text-green-700 dark:text-green-400' : ''
                      }`}
                    >
                      <div className="flex items-center gap-2">
                        {metric.render(property)}
                        {best && (
                          <Badge
                            variant="outline"
                            className="text-xs border-green-500 text-green-600 dark:text-green-400 py-0 px-1"
                          >
                            ✓ Mejor
                          </Badge>
                        )}
                      </div>
                    </td>
                  );
                })}
              </tr>
            ))}

            {/* Fila de amenidades */}
            <tr className="bg-muted/20">
              <td className="px-4 py-3 text-sm font-medium text-muted-foreground">
                Amenidades
              </td>
              {properties.map((property) => (
                <td key={property.id} className="px-4 py-3">
                  {property.amenities.length > 0 ? (
                    <div className="flex flex-wrap gap-1">
                      {property.amenities.map((a) => (
                        <Badge key={a} variant="secondary" className="text-xs capitalize">
                          {a.replace('_', ' ')}
                        </Badge>
                      ))}
                    </div>
                  ) : (
                    <span className="text-xs text-muted-foreground">Sin amenidades</span>
                  )}
                </td>
              ))}
            </tr>
          </tbody>
        </table>
      </div>

      {/* Acciones al pie */}
      <div className="mt-6 flex justify-end">
        <Button
          variant="outline"
          onClick={() => {
            properties.forEach((p) => removeFromCompare(p.id));
            loadProperties();
          }}
        >
          <X className="h-4 w-4 mr-2" />
          Limpiar comparación
        </Button>
      </div>
    </div>
  );
}