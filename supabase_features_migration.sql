-- ==============================================================================
-- تطبيق زوامل اليمن - ملف ترقية قاعدة البيانات للميزات الـ13 الإضافية
-- متوافق تماماً مع البنية الحالية دون حذف أي بيانات سابقة
-- ==============================================================================

-- 1. إضافة حقول عدد مرات الاستماع وجودات الصوت لجدول الزوامل
ALTER TABLE public.zawamil 
    ADD COLUMN IF NOT EXISTS plays_count INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS audio_path_low TEXT,
    ADD COLUMN IF NOT EXISTS audio_path_high TEXT;

-- فهرس لترتيب الأكثر استماعاً بسرعة فائقة
CREATE INDEX IF NOT EXISTS idx_zawamil_plays_count ON public.zawamil(plays_count DESC);

-- ------------------------------------------------------------------------------
-- 2. دالة تسجيل مرات الاستماع الفعلية بأمان (RPC Function)
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.record_zamil_play(p_zamil_id UUID)
RETURNS INT
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_new_count INT;
BEGIN
    UPDATE public.zawamil
    SET plays_count = plays_count + 1
    WHERE id = p_zamil_id
    RETURNING plays_count INTO v_new_count;

    RETURN COALESCE(v_new_count, 0);
END;
$$;

-- السماح لجميع المستخدمين (المسجلين والزوار) باستدعاء الدالة لتسجيل الاستماع
GRANT EXECUTE ON FUNCTION public.record_zamil_play(UUID) TO anon, authenticated;

-- ------------------------------------------------------------------------------
-- 3. جدول المفضلة (favorites)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.favorites (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    zamil_id UUID NOT NULL REFERENCES public.zawamil(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    UNIQUE (user_id, zamil_id)
);

ALTER TABLE public.favorites ENABLE ROW LEVEL SECURITY;

-- سياسات أمان صارمة: لا يرى ولا يعدل المستخدم إلا مفضلته الشخصية
CREATE POLICY "Users can view their own favorites"
    ON public.favorites
    FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

CREATE POLICY "Users can add to their favorites"
    ON public.favorites
    FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can remove from their favorites"
    ON public.favorites
    FOR DELETE
    TO authenticated
    USING (auth.uid() = user_id);

-- ------------------------------------------------------------------------------
-- 4. جدول سجل الاستماع (listening_history)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.listening_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    zamil_id UUID NOT NULL REFERENCES public.zawamil(id) ON DELETE CASCADE,
    last_position_ms INT NOT NULL DEFAULT 0,
    played_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    UNIQUE (user_id, zamil_id)
);

ALTER TABLE public.listening_history ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view their own history"
    ON public.listening_history
    FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert/update their history"
    ON public.listening_history
    FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their history position"
    ON public.listening_history
    FOR UPDATE
    TO authenticated
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete from their history"
    ON public.listening_history
    FOR DELETE
    TO authenticated
    USING (auth.uid() = user_id);

-- ------------------------------------------------------------------------------
-- 5. جدول اشتراكات الإشعارات (notification_subscriptions)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.notification_subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    device_token TEXT NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.notification_subscriptions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users manage their own notification subscriptions"
    ON public.notification_subscriptions
    FOR ALL
    USING (auth.uid() = user_id OR auth.uid() IS NULL);

-- ------------------------------------------------------------------------------
-- 6. إضافة التصنيفات الستة المطلوبة في الميزة 7 (دون تكرار)
-- ------------------------------------------------------------------------------
INSERT INTO public.categories (name, description, icon, sort_order)
VALUES
    ('زوامل حماسية', 'زوامل الشجاعة والحماس والشهامة اليمانية', 'bolt', 1),
    ('زوامل قبلية', 'زوامل القبائل العريقة والشعر والأصالة', 'shield', 2),
    ('زوامل وطنية', 'قصائد المجد وصمود وشموخ اليمن', 'flag', 3),
    ('زوامل مناسبات', 'زوامل الأعراس والأعياد واستقبال الضيوف', 'celebration', 4),
    ('زوامل قديمة', 'التراث القديم والباليه الفلكلوري العريق', 'history_edu', 5),
    ('زوامل حديثة', 'أحدث الإصدارات والتسجيلات الفنية الجديدة', 'whatshot', 6)
ON CONFLICT (name) DO UPDATE SET
    description = EXCLUDED.description,
    icon = EXCLUDED.icon;
