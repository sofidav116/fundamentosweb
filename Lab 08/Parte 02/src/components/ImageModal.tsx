// =============================================================================
// COMPONENTE: ImageModal - Module 2: Real Estate React
// =============================================================================
// Modal a pantalla completa para ver una imagen con navegación.
//
// ## Funcionalidades:
// - Muestra la imagen actual a pantalla completa
// - Botones izquierdo/derecho para navegar entre imágenes
// - Contador "X de Y"
// - Botón X para cerrar
// - Clic en el fondo para cerrar
// - Teclado: ArrowLeft, ArrowRight, Escape
// =============================================================================

import type React from 'react';
import { useEffect, useCallback } from 'react';
import { X, ChevronLeft, ChevronRight } from 'lucide-react';

interface ImageModalProps {
  images: string[];
  currentIndex: number;
  onClose: () => void;
  onNext: () => void;
  onPrev: () => void;
}

/**
 * Modal de imagen a pantalla completa con navegación.
 */
export function ImageModal({
  images,
  currentIndex,
  onClose,
  onNext,
  onPrev,
}: ImageModalProps): React.ReactElement {
  // =========================================================================
  // NAVEGACIÓN POR TECLADO
  // =========================================================================
  const handleKeyDown = useCallback(
    (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
      if (e.key === 'ArrowRight') onNext();
      if (e.key === 'ArrowLeft') onPrev();
    },
    [onClose, onNext, onPrev]
  );

  useEffect(() => {
    window.addEventListener('keydown', handleKeyDown);
    // Bloqueamos el scroll del body mientras el modal está abierto
    document.body.style.overflow = 'hidden';

    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      document.body.style.overflow = '';
    };
  }, [handleKeyDown]);

  return (
    // Fondo oscuro - clic fuera de la imagen cierra el modal
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/90"
      onClick={onClose}
    >
      {/* Contenedor de la imagen - detiene propagación para no cerrar al hacer clic en la imagen */}
      <div
        className="relative flex items-center justify-center w-full h-full p-4 md:p-12"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Imagen actual */}
        <img
          src={images[currentIndex]}
          alt={`Imagen ${currentIndex + 1} de ${images.length}`}
          className="max-w-full max-h-full object-contain rounded-lg shadow-2xl"
        />

        {/* Botón cerrar */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-2 rounded-full bg-black/60 text-white hover:bg-black/80 transition-colors"
          aria-label="Cerrar"
        >
          <X className="h-6 w-6" />
        </button>

        {/* Contador "X de Y" */}
        <div className="absolute top-4 left-1/2 -translate-x-1/2 px-3 py-1 rounded-full bg-black/60 text-white text-sm font-medium">
          {currentIndex + 1} de {images.length}
        </div>

        {/* Botón anterior */}
        {images.length > 1 && (
          <button
            onClick={onPrev}
            className="absolute left-4 p-2 rounded-full bg-black/60 text-white hover:bg-black/80 transition-colors"
            aria-label="Imagen anterior"
          >
            <ChevronLeft className="h-8 w-8" />
          </button>
        )}

        {/* Botón siguiente */}
        {images.length > 1 && (
          <button
            onClick={onNext}
            className="absolute right-4 p-2 rounded-full bg-black/60 text-white hover:bg-black/80 transition-colors"
            aria-label="Imagen siguiente"
          >
            <ChevronRight className="h-8 w-8" />
          </button>
        )}
      </div>
    </div>
  );
}