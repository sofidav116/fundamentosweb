// =============================================================================
// COMPONENTE: ImageGallery - Module 2: Real Estate React
// =============================================================================
// Cuadrícula de miniaturas que abre un modal al hacer clic.
//
// ## Responsabilidades:
// - Renderizar todas las imágenes como miniaturas clicables
// - Manejar el estado del modal (abierto/cerrado, índice actual)
// - Navegar entre imágenes (siguiente, anterior) con wrap-around
// =============================================================================

import type React from 'react';
import { useState } from 'react';
import { ImageModal } from '@/components/ImageModal';

interface ImageGalleryProps {
  images: string[];
  altPrefix?: string;
}

/**
 * Galería de imágenes con cuadrícula de miniaturas y modal a pantalla completa.
 *
 * ## Uso:
 * <ImageGallery images={property.images} altPrefix={property.title} />
 */
export function ImageGallery({
  images,
  altPrefix = 'Imagen',
}: ImageGalleryProps): React.ReactElement | null {
  // Índice de la imagen actualmente abierta en el modal (-1 = cerrado)
  const [activeIndex, setActiveIndex] = useState<number>(-1);

  // Si no hay imágenes, no renderizamos nada
  if (images.length === 0) return null;

  const isOpen = activeIndex >= 0;

  // Navega a la siguiente imagen (circular)
  const handleNext = () => {
    setActiveIndex((prev) => (prev + 1) % images.length);
  };

  // Navega a la imagen anterior (circular)
  const handlePrev = () => {
    setActiveIndex((prev) => (prev - 1 + images.length) % images.length);
  };

  const handleClose = () => {
    setActiveIndex(-1);
  };

  return (
    <>
      {/* Cuadrícula de miniaturas */}
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-2">
        {images.map((img, index) => (
          <button
            key={index}
            onClick={() => setActiveIndex(index)}
            className="relative group overflow-hidden rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
            aria-label={`Ver ${altPrefix} - imagen ${index + 1}`}
          >
            <img
              src={img}
              alt={`${altPrefix} - ${index + 1}`}
              className="w-full h-24 object-cover transition-transform duration-200 group-hover:scale-105"
              loading="lazy"
            />
            {/* Overlay al hacer hover */}
            <div className="absolute inset-0 bg-black/0 group-hover:bg-black/30 transition-colors duration-200 flex items-center justify-center">
              <span className="text-white text-xs font-medium opacity-0 group-hover:opacity-100 transition-opacity">
                Ver
              </span>
            </div>
          </button>
        ))}
      </div>

      {/* Modal - solo se renderiza cuando hay una imagen activa */}
      {isOpen && (
        <ImageModal
          images={images}
          currentIndex={activeIndex}
          onClose={handleClose}
          onNext={handleNext}
          onPrev={handlePrev}
        />
      )}
    </>
  );
}