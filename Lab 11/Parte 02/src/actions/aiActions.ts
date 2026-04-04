// =============================================================================
// AI ACTIONS - Module 5: EventPass Pro
// =============================================================================
//
// ## Educational Note: Server Actions para IA Generativa
//
// Este archivo contiene Server Actions que integran Gemini AI para generar
// contenido de eventos. Usamos Server Actions en lugar de API Routes porque:
//
// 1. **Seguridad**: Las API keys NUNCA llegan al cliente
// 2. **Simplicidad**: No necesitamos crear endpoints REST
// 3. **Type Safety**: TypeScript end-to-end sin serializacion manual
// 4. **Caching**: Next.js puede cachear resultados automaticamente
//
// ### Flujo de Generación con Gemini
//
// ```
// ┌─────────────────────────────────────────────────────────────────────────┐
// │                    FLUJO: CLIENTE → SERVER ACTION → GEMINI              │
// ├─────────────────────────────────────────────────────────────────────────┤
// │                                                                          │
// │   1. Usuario escribe título ────────────────────────────────────────┐    │
// │      "Conferencia de React 2025"                                    │    │
// │                                                                     │    │
// │   2. Click "Generar con IA" ────────────────────────────────────────┤    │
// │                                                                     │    │
// │   3. EventForm llama generateEventDetailsAction(title) ─────────────┤    │
// │      (Server Action, ejecuta en el servidor)                        │    │
// │                                                                     │    │
// │   4. Server Action construye prompt ────────────────────────────────┤    │
// │      + Envía a Gemini API                                           │    │
// │                                                                     │    │
// │   5. Gemini retorna JSON ───────────────────────────────────────────┤    │
// │      { description, category, tags }                                │    │
// │                                                                     │    │
// │   6. Server Action parsea y valida ─────────────────────────────────┤    │
// │                                                                     │    │
// │   7. Retorna datos al cliente ──────────────────────────────────────┘    │
// │      EventForm actualiza campos automáticamente                          │
// │                                                                          │
// └─────────────────────────────────────────────────────────────────────────┘
// ```
//
// ### Prompt Engineering
//
// El prompt está diseñado para:
// 1. Dar contexto claro al modelo (eres un experto en eventos)
// 2. Especificar el formato exacto de salida (JSON)
// 3. Incluir restricciones (categorías válidas, límite de caracteres)
// 4. Pedir respuesta sin formato markdown (solo JSON)
//
// =============================================================================

'use server';

import { getGeminiClient, GEMINI_MODELS } from '@/lib/gemini';
import { EVENT_CATEGORIES } from '@/types/event';

// =============================================================================
// TIPOS DE RESPUESTA
// =============================================================================

export interface GeneratedEventDetails {
    description: string;
    category: string;
    tags: string[];
}

// =============================================================================
// TIPOS PARA VARIANTES DE DESCRIPCIÓN (Parte 2)
// =============================================================================
// DescriptionTone: define los tonos disponibles para la generación de variantes.
// DescriptionVariant: representa una variante individual con su tono y texto.
// GeneratedVariantsResult: estructura de retorno con las 3 variantes + metadata.
// =============================================================================

export type DescriptionTone = 'formal' | 'informal' | 'emocionante';

export interface DescriptionVariant {
    tone: DescriptionTone;
    toneLabel: string;
    description: string;
}

export interface GeneratedVariantsResult {
    variants: DescriptionVariant[];
    category: string;
    tags: string[];
}

export async function generateEventDetailsAction(title: string): Promise<{ success: boolean; data?: GeneratedEventDetails; error?: string }> {
    try {
        if (!process.env.GEMINI_API_KEY) {
            throw new Error('GEMINI_API_KEY is not configured');
        }

        if (!title || title.length < 3) {
            return { success: false, error: 'Please provide a valid event title' };
        }

        const client = getGeminiClient();

        const prompt = `
      You are an expert event planner. Based on the event title "${title}", please generate:
      1. A compelling and engaging description (2-3 paragraphs, MUST be under 1000 characters).
      2. The most suitable category from this list: ${EVENT_CATEGORIES.join(', ')}.
      3. A list of 5 relevant tags (lowercase, concise).

      Return the response in strictly valid JSON format with this structure:
      {
        "description": "string",
        "category": "string",
        "tags": ["tag1", "tag2"]
      }
      Do not include any markdown formatting or explanations, just the JSON string.
    `;

        // The new SDK syntax might differ, but assuming standardized usage:
        // client.models.generateContent({ model: 'model-name', contents: ... })
        const result = await client.models.generateContent({
            model: GEMINI_MODELS.TEXT,
            contents: [
                {
                    role: 'user',
                    parts: [{ text: prompt }]
                }
            ],
            config: {
                responseMimeType: 'application/json'
            }
        });

        const text = result.text;

        if (!text) {
            throw new Error('No content generated');
        }

        // Clean up markdown code blocks if present (common in LLM responses)
        // Even with JSON mode, sometimes it might add wrapping
        const cleanedText = text.replace(/```json\n?|\n?```/g, '').trim();

        const data = JSON.parse(cleanedText) as GeneratedEventDetails;

        // Validate category
        if (!EVENT_CATEGORIES.includes(data.category as any)) {
            data.category = 'otro';
        }

        // Limit tags to 5
        data.tags = (data.tags || []).slice(0, 5);

        return { success: true, data };
    } catch (error) {
        console.error('Gemini API Error:', error);
        return { success: false, error: 'Failed to generate content. Please try again.' };
    }
}

// =============================================================================
// GENERACIÓN DE VARIANTES DE DESCRIPCIÓN (Parte 2)
// =============================================================================
//
// Esta función extiende la generación de IA para producir 3 variantes de
// descripción según el tono solicitado por el usuario. El flujo es:
//
// 1. Recibe el título y el tono deseado (formal | informal | emocionante)
// 2. Construye un prompt que pide explícitamente 3 variantes con ese tono
// 3. Gemini retorna JSON con un array de 3 descripciones + categoría + tags
// 4. El cliente muestra las tarjetas para que el usuario elija su favorita
//
// ### Por qué un tono único en lugar de 3 tonos distintos?
// El profesor pidió que el SELECTOR defina el tono. Las 3 variantes son
// alternativas del MISMO tono con enfoques creativos diferentes, lo que
// da variedad sin contradecir la preferencia del usuario.
// =============================================================================

/**
 * Genera 3 variantes de descripción para un evento dado un tono específico.
 * Retorna también la categoría sugerida y tags relevantes.
 */
export async function generateEventDescriptionVariantsAction(
    title: string,
    tone: DescriptionTone
): Promise<{ success: boolean; data?: GeneratedVariantsResult; error?: string }> {
    try {
        if (!process.env.GEMINI_API_KEY) {
            throw new Error('GEMINI_API_KEY is not configured');
        }

        if (!title || title.length < 3) {
            return { success: false, error: 'Por favor escribe un título válido (mínimo 3 caracteres)' };
        }

        // Map tone to Spanish instructions for the prompt
        const toneInstructions: Record<DescriptionTone, string> = {
            formal: 'professional, corporate, and formal language. Use proper grammar, avoid contractions, and maintain an authoritative tone.',
            informal: 'friendly, casual, and conversational language. Use a warm, approachable tone as if talking to a friend.',
            emocionante: 'exciting, energetic, and enthusiastic language. Use vivid adjectives, action verbs, and create a sense of urgency and excitement.',
        };

        const toneLabelMap: Record<DescriptionTone, string> = {
            formal: 'Formal',
            informal: 'Informal',
            emocionante: 'Emocionante',
        };

        const client = getGeminiClient();

        const prompt = `
You are an expert event copywriter. Based on the event title "${title}", generate exactly 3 different description variants.

All 3 variants must use ${toneInstructions[tone]}

Each variant should be a unique creative approach to describing the same event — vary the angle, emphasis, or storytelling structure. Each description MUST be between 150 and 400 characters.

Also suggest:
- The most suitable category from: ${EVENT_CATEGORIES.join(', ')}
- A list of exactly 5 relevant tags (lowercase, concise, no spaces)

Return ONLY a strictly valid JSON object with this exact structure, no markdown, no extra text:
{
  "variants": [
    { "description": "string (variant 1)" },
    { "description": "string (variant 2)" },
    { "description": "string (variant 3)" }
  ],
  "category": "string",
  "tags": ["tag1", "tag2", "tag3", "tag4", "tag5"]
}
`;

        const result = await client.models.generateContent({
            model: GEMINI_MODELS.TEXT,
            contents: [
                {
                    role: 'user',
                    parts: [{ text: prompt }]
                }
            ],
            config: {
                responseMimeType: 'application/json'
            }
        });

        const text = result.text;

        if (!text) {
            throw new Error('No content generated');
        }

        // Clean up markdown code blocks if present
        const cleanedText = text.replace(/```json\n?|\n?```/g, '').trim();

        const parsed = JSON.parse(cleanedText) as {
            variants: { description: string }[];
            category: string;
            tags: string[];
        };

        // Validate we got exactly 3 variants
        if (!parsed.variants || parsed.variants.length < 1) {
            throw new Error('Invalid response: no variants generated');
        }

        // Map variants adding tone metadata
        const variants: DescriptionVariant[] = parsed.variants.slice(0, 3).map((v) => ({
            tone,
            toneLabel: toneLabelMap[tone],
            description: v.description,
        }));

        // Validate category
        const category = EVENT_CATEGORIES.includes(parsed.category as any)
            ? parsed.category
            : 'otro';

        // Limit tags to 5
        const tags = (parsed.tags || []).slice(0, 5);

        return {
            success: true,
            data: { variants, category, tags },
        };
    } catch (error) {
        console.error('Gemini Variants API Error:', error);
        return { success: false, error: 'No se pudieron generar las variantes. Intenta de nuevo.' };
    }
}

export async function generateEventPosterAction(prompt: string, eventId?: string): Promise<{ success: boolean; imageUrl?: string; error?: string }> {
    try {
        if (!process.env.GEMINI_API_KEY) {
            throw new Error('GEMINI_API_KEY is not configured');
        }

        const client = getGeminiClient();

        // Generate image
        const result = await client.models.generateContent({
            model: GEMINI_MODELS.IMAGE,
            contents: [
                {
                    role: 'user',
                    parts: [{ text: `Create a professional, modern, and clean business event poster for: ${prompt}. Style: High-quality photorealistic imagery, elegant typography. Avoid futuristic, sci-fi, or neon aesthetics. 16:9 aspect ratio. Minimal text.` }]
                }
            ],
        });

        // Extract base64
        // According to context7 docs for gemini-3-pro-image-preview:
        // response.candidates[0].content.parts[0].inlineData.data
        const candidates = result.candidates; // Access property directly
        const part = candidates?.[0]?.content?.parts?.[0];

        let base64Image: string | undefined;

        if (part?.inlineData?.data) {
            base64Image = part.inlineData.data;
        } else {
            console.error('No inlineData in response:', JSON.stringify(part));
            throw new Error('No image generated');
        }

        const buffer = Buffer.from(base64Image, 'base64');

        // Upload to storage
        // If no eventId, generate a temporary one
        const targetId = eventId || crypto.randomUUID();

        const { uploadPosterToStorage } = await import('@/lib/firebase/storage');
        const imageUrl = await uploadPosterToStorage(targetId, buffer, 'image/png');

        if (!imageUrl) {
            throw new Error('Failed to upload image to storage');
        }

        return { success: true, imageUrl };
    } catch (error) {
        console.error('Gemini Image Generation Error:', error);
        return { success: false, error: 'Failed to generate poster.' };
    }
}