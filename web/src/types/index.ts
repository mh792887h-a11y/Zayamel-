export interface Category {
  id: string;
  name: string;
  description?: string | null;
  icon?: string | null;
  sort_order: number;
}

export interface Zamil {
  id: string;
  title: string;
  performer: string;
  description?: string | null;
  category_id?: string | null;
  audio_path: string;
  cover_path?: string | null;
  duration_seconds: number;
  sort_order: number;
  is_published: boolean;
  plays_count?: number;
  audio_path_low?: string | null;
  audio_path_high?: string | null;
  created_at?: string;
  updated_at?: string;
  is_favorite?: boolean;
}

export const AUTHORIZED_OWNER_EMAIL = 'm91857687@gmail.com';
