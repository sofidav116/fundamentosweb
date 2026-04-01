// =============================================================================
// RUTAS DE PROPIEDADES - Module 3: RealEstate Hub API
// =============================================================================
// Define las rutas (endpoints) de la API de propiedades.
//
// ## Diseño RESTful
// - GET /api/properties         - Listar todas (con paginación)
// - GET /api/properties/stats   - Estadísticas de propiedades
// - GET /api/properties/:id     - Obtener una
// - POST /api/properties        - Crear nueva
// - PUT /api/properties/:id     - Actualizar
// - DELETE /api/properties/:id  - Eliminar
//
// IMPORTANTE: /stats debe ir ANTES de /:id para que Express no
// interprete "stats" como un parámetro de ID.
// =============================================================================

import { Router } from 'express';
import {
  getAllProperties,
  getPropertyStats,
  getPropertyById,
  createProperty,
  updateProperty,
  deleteProperty,
} from '../controllers/propertyController.js';

const router = Router();

// =============================================================================
// RUTAS
// =============================================================================

/**
 * GET /api/properties
 * Lista propiedades con filtros opcionales y paginación.
 *
 * Query params de paginación:
 * - page:  Número de página (entero >= 1, default: 1)
 * - limit: Registros por página (entero >= 1, default: 10)
 *
 * Query params de filtro:
 * - search, propertyType, operationType, minPrice, maxPrice, minBedrooms, city
 *
 * Respuesta:
 * {
 *   success: true,
 *   data: Property[],
 *   meta: { total, page, limit, pages }
 * }
 */
router.get('/', (req, res) => {
  void getAllProperties(req, res);
});

/**
 * GET /api/properties/stats
 * Devuelve estadísticas agregadas de todas las propiedades.
 *
 * Respuesta:
 * {
 *   success: true,
 *   data: {
 *     total: number,
 *     byType: { house: 10, apartment: 15, ... },
 *     averagePriceByType: { house: 350000, apartment: 180000, ... },
 *     priceRange: { min: 50000, max: 2000000 }
 *   }
 * }
 *
 * NOTA: Esta ruta debe estar ANTES de /:id para evitar conflictos.
 */
router.get('/stats', (req, res) => {
  void getPropertyStats(req, res);
});

/**
 * GET /api/properties/:id
 * Obtiene una propiedad específica por su ID.
 */
router.get('/:id', (req, res) => {
  void getPropertyById(req, res);
});

/**
 * POST /api/properties
 * Crea una nueva propiedad.
 */
router.post('/', (req, res) => {
  void createProperty(req, res);
});

/**
 * PUT /api/properties/:id
 * Actualiza una propiedad existente.
 */
router.put('/:id', (req, res) => {
  void updateProperty(req, res);
});

/**
 * DELETE /api/properties/:id
 * Elimina una propiedad.
 */
router.delete('/:id', (req, res) => {
  void deleteProperty(req, res);
});

export default router;