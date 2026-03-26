// =============================================================================
// COMPONENTE: PROPERTY FORM - Module 2: Real Estate React
// =============================================================================
//
// ## Educational Note: Formularios Modernos con React Hook Form + Zod
//
// Este componente demuestra el patrón estándar de la industria para
// formularios complejos en React. La combinación RHF + Zod resuelve
// los problemas históricos de los formularios en React.
//
// ### El Problema de los Formularios en React
//
// ```
// ┌─────────────────────────────────────────────────────────────────────────┐
// │                    EVOLUCIÓN DE FORMULARIOS EN REACT                    │
// ├─────────────────────────────────────────────────────────────────────────┤
// │                                                                          │
// │   ERA 1: Controlled Components (useState por cada campo)                │
// │   ─────────────────────────────────────────────────────────────────────  │
// │   const [name, setName] = useState('');                                  │
// │   const [email, setEmail] = useState('');                                │
// │   const [age, setAge] = useState(0);                                     │
// │   // ... 10 más estados, 10 más handlers, re-render en cada keystroke   │
// │   ✗ Mucho boilerplate  ✗ Re-renders excesivos  ✗ Validación manual     │
// │                                                                          │
// │   ERA 2: Librerías (Formik, React Final Form)                           │
// │   ─────────────────────────────────────────────────────────────────────  │
// │   <Formik><Field name="email" /></Formik>                                │
// │   ✓ Menos código  ✗ Bundle grande  ✗ API compleja  ✗ Re-renders        │
// │                                                                          │
// │   ERA 3: React Hook Form (actual)                                        │
// │   ─────────────────────────────────────────────────────────────────────  │
// │   const { register } = useForm();                                        │
// │   <input {...register('email')} />                                       │
// │   ✓ Mínimo boilerplate  ✓ Sin re-renders  ✓ Bundle pequeño  ✓ TypeScript│
// │                                                                          │
// └─────────────────────────────────────────────────────────────────────────┘
// ```
//
// =============================================================================

import type React from 'react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { Plus, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  createPropertySchema,
  type CreatePropertyInput,
  PROPERTY_TYPES,
  OPERATION_TYPES,
  PROPERTY_TYPE_LABELS,
  OPERATION_TYPE_LABELS,
} from '@/types/property';

/**
 * Props del formulario de propiedad.
 */
interface PropertyFormProps {
  defaultValues?: Partial<CreatePropertyInput>;
  onSubmit: (data: CreatePropertyInput) => void;
  isSubmitting?: boolean;
}

/**
 * Formulario para crear o editar una propiedad inmobiliaria.
 */
export function PropertyForm({
  defaultValues,
  onSubmit,
  isSubmitting = false,
}: PropertyFormProps): React.ReactElement {
  // =========================================================================
  // ESTADO LOCAL PARA IMÁGENES
  // Manejamos las URLs de imágenes como estado separado para poder
  // agregar/quitar dinámicamente sin re-renderizar todo el formulario.
  // =========================================================================
  const [imageUrls, setImageUrls] = useState<string[]>(defaultValues?.images ?? []);
  const [newImageUrl, setNewImageUrl] = useState('');

  /**
   * Agrega una nueva URL de imagen a la lista.
   */
  const handleAddImage = () => {
    const trimmed = newImageUrl.trim();
    if (!trimmed) return;
    // Validación básica de URL
    try {
      new URL(trimmed);
    } catch {
      toast.error('Por favor ingresa una URL válida (debe empezar con http:// o https://)');
      return;
    }
    setImageUrls((prev) => [...prev, trimmed]);
    setNewImageUrl('');
  };

  /**
   * Quita una imagen de la lista por su índice.
   */
  const handleRemoveImage = (index: number) => {
    setImageUrls((prev) => prev.filter((_, i) => i !== index));
  };

  // =========================================================================
  // CONFIGURACIÓN DE REACT HOOK FORM
  // =========================================================================
  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<CreatePropertyInput>({
    resolver: async (values) => {
      try {
        // Inyectamos las imágenes del estado local antes de validar
        const valuesWithImages = { ...values, images: imageUrls };
        const result = createPropertySchema.safeParse(valuesWithImages);

        if (result.success) {
          return { values: result.data, errors: {} };
        }

        const errors = result.error.issues.reduce(
          (allErrors, currentError) => ({
            ...allErrors,
            [currentError.path[0]]: {
              type: currentError.code,
              message: currentError.message,
            },
          }),
          {} as Record<string, { type: string; message: string }>
        );

        return { values: {}, errors };
      } catch (error) {
        console.error('Error crítico de validación:', error);
        return {
          values: {},
          errors: {
            root: { type: 'server', message: 'Error inesperado al validar el formulario' },
          },
        };
      }
    },
    defaultValues: {
      title: '',
      description: '',
      propertyType: 'apartamento',
      operationType: 'venta',
      price: 0,
      address: '',
      city: '',
      bedrooms: 1,
      bathrooms: 1,
      area: 50,
      amenities: [],
      images: [],
      ...defaultValues,
    },
    mode: 'onTouched',
    reValidateMode: 'onChange',
  });

  const propertyType = watch('propertyType');
  const operationType = watch('operationType');
  const descriptionValue = watch('description');

  /**
   * Antes de enviar, sincronizamos las imágenes del estado local al formulario.
   */
  const handleFormSubmit = handleSubmit(
    (data) => {
      onSubmit({ ...data, images: imageUrls });
    },
    () => {
      toast.error('Por favor corrige los errores señalados en el formulario');
    }
  );

  return (
    <form onSubmit={handleFormSubmit} className="space-y-6">

      {/* Sección: Información Básica */}
      <Card>
        <CardHeader>
          <CardTitle>Información Básica</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="title">Título de la propiedad *</Label>
            <Input
              id="title"
              placeholder="Ej: Elegante apartamento con vista al mar"
              {...register('title')}
            />
            {errors.title && (
              <p className="text-sm text-destructive">{errors.title.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">Descripción *</Label>
            <Textarea
              id="description"
              placeholder="Describe la propiedad en detalle (mínimo 50 caracteres)..."
              rows={5}
              {...register('description')}
            />
            <div className="flex justify-between items-center">
              <span className={`text-sm ${(descriptionValue?.length ?? 0) < 50 ? 'text-destructive' : 'text-muted-foreground'}`}>
                {descriptionValue?.length ?? 0}/50 caracteres mínimos
              </span>
              {errors.description && (
                <p className="text-sm text-destructive">{errors.description.message}</p>
              )}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Sección: Tipo y Operación */}
      <Card>
        <CardHeader>
          <CardTitle>Tipo y Operación</CardTitle>
        </CardHeader>
        <CardContent className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label>Tipo de propiedad *</Label>
            <Select
              value={propertyType}
              onValueChange={(value) =>
                setValue('propertyType', value as CreatePropertyInput['propertyType'])
              }
            >
              <SelectTrigger>
                <SelectValue placeholder="Selecciona un tipo" />
              </SelectTrigger>
              <SelectContent>
                {PROPERTY_TYPES.map((type) => (
                  <SelectItem key={type} value={type}>
                    {PROPERTY_TYPE_LABELS[type]}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.propertyType && (
              <p className="text-sm text-destructive">{errors.propertyType.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label>Tipo de operación *</Label>
            <Select
              value={operationType}
              onValueChange={(value) =>
                setValue('operationType', value as CreatePropertyInput['operationType'])
              }
            >
              <SelectTrigger>
                <SelectValue placeholder="Selecciona operación" />
              </SelectTrigger>
              <SelectContent>
                {OPERATION_TYPES.map((type) => (
                  <SelectItem key={type} value={type}>
                    {OPERATION_TYPE_LABELS[type]}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.operationType && (
              <p className="text-sm text-destructive">{errors.operationType.message}</p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Sección: Precio */}
      <Card>
        <CardHeader>
          <CardTitle>Precio</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            <Label htmlFor="price">
              Precio {operationType === 'alquiler' ? '(mensual)' : ''} *
            </Label>
            <div className="relative">
              <span className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground">
                $
              </span>
              <Input
                id="price"
                type="number"
                className="pl-7"
                placeholder="0"
                {...register('price', { valueAsNumber: true })}
              />
            </div>
            {errors.price && (
              <p className="text-sm text-destructive">{errors.price.message}</p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Sección: Ubicación */}
      <Card>
        <CardHeader>
          <CardTitle>Ubicación</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="address">Dirección *</Label>
            <Input
              id="address"
              placeholder="Calle, número, piso..."
              {...register('address')}
            />
            {errors.address && (
              <p className="text-sm text-destructive">{errors.address.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="city">Ciudad *</Label>
            <Input id="city" placeholder="Ej: Madrid" {...register('city')} />
            {errors.city && (
              <p className="text-sm text-destructive">{errors.city.message}</p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Sección: Características */}
      <Card>
        <CardHeader>
          <CardTitle>Características</CardTitle>
        </CardHeader>
        <CardContent className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="space-y-2">
            <Label htmlFor="bedrooms">Habitaciones</Label>
            <Input
              id="bedrooms"
              type="number"
              min="0"
              {...register('bedrooms', { valueAsNumber: true })}
            />
            {errors.bedrooms && (
              <p className="text-sm text-destructive">{errors.bedrooms.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="bathrooms">Baños</Label>
            <Input
              id="bathrooms"
              type="number"
              min="0"
              {...register('bathrooms', { valueAsNumber: true })}
            />
            {errors.bathrooms && (
              <p className="text-sm text-destructive">{errors.bathrooms.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="area">Área (m²)</Label>
            <Input
              id="area"
              type="number"
              min="1"
              {...register('area', { valueAsNumber: true })}
            />
            {errors.area && (
              <p className="text-sm text-destructive">{errors.area.message}</p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* ===================================================================
        SECCIÓN: IMÁGENES
        Permite agregar URLs de imágenes una por una.
        Se muestra una previsualización de cada imagen agregada.
        =================================================================== */}
      <Card>
        <CardHeader>
          <CardTitle>Imágenes</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Input para agregar nueva URL */}
          <div className="flex gap-2">
            <Input
              placeholder="https://ejemplo.com/imagen.jpg"
              value={newImageUrl}
              onChange={(e) => setNewImageUrl(e.target.value)}
              onKeyDown={(e) => {
                // Permite agregar con Enter sin enviar el formulario
                if (e.key === 'Enter') {
                  e.preventDefault();
                  handleAddImage();
                }
              }}
            />
            <Button type="button" variant="outline" onClick={handleAddImage}>
              <Plus className="h-4 w-4 mr-1" />
              Agregar
            </Button>
          </div>

          <p className="text-xs text-muted-foreground">
            Ingresa la URL de cada imagen y presiona "Agregar" o Enter.
          </p>

          {/* Lista de imágenes agregadas con previsualización */}
          {imageUrls.length > 0 && (
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
              {imageUrls.map((url, index) => (
                <div key={index} className="relative group rounded-lg overflow-hidden border">
                  <img
                    src={url}
                    alt={`Imagen ${index + 1}`}
                    className="w-full h-24 object-cover"
                    onError={(e) => {
                      // Si la imagen no carga, mostramos placeholder
                      (e.target as HTMLImageElement).src =
                        'https://placehold.co/200x150/e2e8f0/64748b?text=Error';
                    }}
                  />
                  {/* Botón para eliminar la imagen */}
                  <button
                    type="button"
                    onClick={() => handleRemoveImage(index)}
                    className="absolute top-1 right-1 p-1 rounded-full bg-black/60 text-white opacity-0 group-hover:opacity-100 transition-opacity hover:bg-red-600"
                    aria-label="Eliminar imagen"
                  >
                    <X className="h-3 w-3" />
                  </button>
                  <span className="absolute bottom-1 left-1 text-xs bg-black/60 text-white px-1 rounded">
                    {index + 1}
                  </span>
                </div>
              ))}
            </div>
          )}

          {imageUrls.length === 0 && (
            <p className="text-sm text-muted-foreground text-center py-4 border rounded-lg border-dashed">
              No hay imágenes agregadas
            </p>
          )}
        </CardContent>
      </Card>

      {/* Botón de envío */}
      <Button type="submit" size="lg" className="w-full" disabled={isSubmitting}>
        {isSubmitting ? 'Guardando...' : 'Guardar Propiedad'}
      </Button>
    </form>
  );
}