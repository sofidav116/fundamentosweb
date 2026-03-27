// =============================================================================
// COMPONENTE REGISTER BUTTON - Module 4: Event Pass
// =============================================================================
// Botón para registrarse en un evento con actualización optimista.
//
// ## useOptimistic (React 19)
// Este hook permite actualizar la UI inmediatamente antes de que
// la operación del servidor complete. Si falla, React revierte
// automáticamente al estado anterior.
//
// ## Patrón de Actualización Optimista
// 1. Usuario hace clic
// 2. UI se actualiza inmediatamente (optimistic)
// 3. Server Action se ejecuta
// 4. Si falla, UI se revierte automáticamente
// 5. Si éxito, estado se confirma
// =============================================================================

'use client';

import { useOptimistic, useTransition, useState } from 'react';
import { Button } from '@/components/ui/button';
import { registerForEventAction } from '@/actions/eventActions';
import { Loader2, CheckCircle, XCircle } from 'lucide-react';
import { cn } from '@/lib/utils';

interface RegisterButtonProps {
  eventId: string;
  availableSpots: number;
  isAvailable: boolean;
}

/**
 * Botón de registro con actualización optimista.
 *
 * ## Flujo
 * 1. Al hacer clic, `addOptimistic` actualiza spots inmediatamente (-1)
 * 2. `startTransition` inicia la Server Action
 * 3. Mientras pending=true, mostramos spinner
 * 4. Si falla, React revierte automáticamente y mostramos error
 * 5. Si éxito, mostramos mensaje de confirmación
 */
export function RegisterButton({
  eventId,
  availableSpots,
  isAvailable,
}: RegisterButtonProps): React.ReactElement {
  /**
   * useTransition permite marcar actualizaciones como no urgentes.
   * isPending indica si hay una transición en progreso.
   */
  const [isPending, startTransition] = useTransition();

  /**
   * Estado para el feedback de éxito/error tras completar la acción.
   * null = sin feedback, 'success' = registro exitoso, 'error' = fallo
   */
  const [feedback, setFeedback] = useState<'success' | 'error' | null>(null);

  /**
   * Mensaje de error del servidor para mostrarlo al usuario.
   */
  const [errorMessage, setErrorMessage] = useState<string>('');

  /**
   * useOptimistic crea un estado optimista.
   *
   * @param initialValue - Valor inicial (plazas disponibles)
   * @param reducer - Función que calcula el nuevo valor optimista
   */
  const [optimisticSpots, addOptimistic] = useOptimistic(
    availableSpots,
    // Reducer: cuando se registra, restamos 1
    (currentSpots: number, _action: 'register') => Math.max(0, currentSpots - 1)
  );

  // Estado derivado
  const showRegistered = feedback === 'success';
  const canRegister = isAvailable && optimisticSpots > 0 && !showRegistered && feedback !== 'success';

  /**
   * Handler del registro.
   */
  async function handleRegister(): Promise<void> {
    // Limpiamos feedback previo
    setFeedback(null);
    setErrorMessage('');

    // 1. Actualización optimista inmediata
    addOptimistic('register');

    // 2. Ejecutar Server Action en una transición
    startTransition(async () => {
      const result = await registerForEventAction(eventId);

      if (!result.success) {
        // El estado optimista se revierte automáticamente al salir de la transición
        setFeedback('error');
        setErrorMessage(result.message ?? 'Error al registrar. Inténtalo de nuevo.');
      } else {
        // Confirmamos el éxito
        setFeedback('success');
      }
    });
  }

  // Si ya se registró exitosamente (confirmado por el servidor)
  if (showRegistered) {
    return (
      <div className="flex flex-col gap-2">
        <Button variant="secondary" disabled className="w-full gap-2">
          <CheckCircle className="h-4 w-4 text-green-500" />
          ¡Registrado!
        </Button>
        <p className="text-center text-sm text-green-600">
          ¡Te has registrado correctamente!
        </p>
      </div>
    );
  }

  // Si no hay plazas o no está disponible
  if (!canRegister && !isPending) {
    return (
      <div className="flex flex-col gap-2">
        <Button variant="secondary" disabled className="w-full">
          {optimisticSpots === 0 ? 'Evento Agotado' : 'No disponible'}
        </Button>
        {/* Mensaje de error si el servidor rechazó el registro */}
        {feedback === 'error' && (
          <p className="flex items-center justify-center gap-1 text-center text-sm text-red-500">
            <XCircle className="h-4 w-4" />
            {errorMessage}
          </p>
        )}
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-2">
      <Button
        onClick={handleRegister}
        disabled={isPending}
        className={cn('w-full gap-2', isPending && 'cursor-wait')}
      >
        {isPending ? (
          <>
            <Loader2 className="h-4 w-4 animate-spin" />
            Registrando...
          </>
        ) : (
          `Registrarme (${optimisticSpots} plazas)`
        )}
      </Button>

      {/* Mensaje de error si el servidor revirtió el registro */}
      {feedback === 'error' && (
        <p className="flex items-center justify-center gap-1 text-center text-sm text-red-500">
          <XCircle className="h-4 w-4" />
          {errorMessage}
        </p>
      )}
    </div>
  );
}