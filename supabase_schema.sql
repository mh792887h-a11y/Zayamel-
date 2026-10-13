-- ==============================================================================
-- تطبيق زوامل اليمن - ملف البنية وقواعد الأمان وقاعدة البيانات (Supabase PostgreSQL)
-- ==============================================================================
-- البريد المعتمد حصرياً لإدارة التطبيق: m91857687@gmail.com
-- ==============================================================================

-- 1. تفعيل الامتدادات الضرورية
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ------------------------------------------------------------------------------
-- 2. جدول المديرين المعتمدين (app_admins)
-- ------------------------------------------------------------------------------
-- يتم تخزين المعرف الحقيقي من auth.users لضمان عدم إمكانية انتحال الهوية
CREATE TABLE IF NOT EXISTS public.app_admins (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT NOT NULL UNIQUE,
    role TEXT NOT NULL DEFAULT 'super_admin' CHECK (role IN ('super_admin', 'editor')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.app_admins ENABLE ROW LEVEL SECURITY;

-- ------------------------------------------------------------------------------
-- 3. دالة التحقق الأمنية الصارمة (Security Definer Function)
-- ------------------------------------------------------------------------------
-- هذه الدالة تتحقق من:
-- أ. أن المستخدم مسجل وموثق الجلسة (auth.uid() IS NOT NULL)
-- ب. أن البريد في رمز المصادقة (JWT) هو حصراً: m91857687@gmail.com
-- ج. أن هناك سجلاً مطابقاً في جدول المديرين app_admins لنفس الـ user_id
CREATE OR REPLACE FUNCTION public.is_app_admin()
RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, auth
AS $$
DECLARE
    v_user_email TEXT;
    v_is_admin BOOLEAN := false;
BEGIN
    -- التحقق من وجود مستخدم مسجل
    IF auth.uid() IS NULL THEN
        RETURN false;
    END IF;

    -- استخراج البريد الموثق من JWT
    v_user_email := lower(auth.jwt() ->> 'email');

    -- شرط صارم: البريد يجب أن يطابق تماماً بريد المالك
    IF v_user_email <> 'm91857687@gmail.com' THEN
        RETURN false;
    END IF;

    -- التحقق من وجود المعرف في جدول المديرين
    SELECT EXISTS (
        SELECT 1 
        FROM public.app_admins 
        WHERE user_id = auth.uid() 
          AND lower(email) = 'm91857687@gmail.com'
    ) INTO v_is_admin;

    RETURN v_is_admin;
END;
$$;

-- سياسات RLS لجدول app_admins: لا يمكن قراءته أو تعديله إلا للمدير الحقيقي
CREATE POLICY "Admins can view admins list"
    ON public.app_admins
    FOR SELECT
    TO authenticated
    USING (public.is_app_admin());

-- ------------------------------------------------------------------------------
-- 4. مشغل تلقائي (Trigger) لربط المالك المعتمد عند أول تسجيل دخول عبر Google
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.handle_admin_google_signup()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, auth
AS $$
BEGIN
    -- إذا كان المستخدم المسجل بـ Google يحمل بريد المالك
    IF lower(NEW.email) = 'm91857687@gmail.com' THEN
        INSERT INTO public.app_admins (user_id, email, role)
        VALUES (NEW.id, lower(NEW.email), 'super_admin')
        ON CONFLICT (email) 
        DO UPDATE SET user_id = EXCLUDED.user_id;
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS tr_on_admin_google_signup ON auth.users;
CREATE TRIGGER tr_on_admin_google_signup
    AFTER INSERT OR UPDATE ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_admin_google_signup();

-- ------------------------------------------------------------------------------
-- 5. جدول التصنيفات (categories)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    icon TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.categories ENABLE ROW LEVEL SECURITY;

-- السماح للجميع (عامة ومسجلين) بقراءة التصنيفات
CREATE POLICY "Categories are readable by everyone"
    ON public.categories
    FOR SELECT
    USING (true);

-- التعديل والإضافة والحذف فقط للمدير المعتمد
CREATE POLICY "Only admin can insert categories"
    ON public.categories
    FOR INSERT
    TO authenticated
    WITH CHECK (public.is_app_admin());

CREATE POLICY "Only admin can update categories"
    ON public.categories
    FOR UPDATE
    TO authenticated
    USING (public.is_app_admin())
    WITH CHECK (public.is_app_admin());

CREATE POLICY "Only admin can delete categories"
    ON public.categories
    FOR DELETE
    TO authenticated
    USING (public.is_app_admin());

-- ------------------------------------------------------------------------------
-- 6. جدول الزوامل (zawamil)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.zawamil (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL CHECK (char_length(trim(title)) > 0),
    performer TEXT NOT NULL CHECK (char_length(trim(performer)) > 0),
    description TEXT,
    category_id UUID REFERENCES public.categories(id) ON DELETE SET NULL,
    audio_path TEXT NOT NULL CHECK (char_length(trim(audio_path)) > 0),
    cover_path TEXT,
    duration_seconds INT NOT NULL DEFAULT 0 CHECK (duration_seconds >= 0),
    sort_order INT NOT NULL DEFAULT 0,
    is_published BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- تحديث تلقائي لحقل updated_at
CREATE OR REPLACE FUNCTION public.handle_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = timezone('utc'::text, now());
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS tr_zawamil_updated_at ON public.zawamil;
CREATE TRIGGER tr_zawamil_updated_at
    BEFORE UPDATE ON public.zawamil
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_updated_at();

-- إنشاء الفهارس لتحسين الأداء وسرعة البحث والترتيب
CREATE INDEX IF NOT EXISTS idx_zawamil_published_sort ON public.zawamil(is_published, sort_order ASC);
CREATE INDEX IF NOT EXISTS idx_zawamil_category ON public.zawamil(category_id);
CREATE INDEX IF NOT EXISTS idx_zawamil_created_at ON public.zawamil(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_zawamil_title_trgm ON public.zawamil USING btree(title);
CREATE INDEX IF NOT EXISTS idx_zawamil_performer_trgm ON public.zawamil USING btree(performer);

-- تفعيل سياسات الأمان RLS على جدول الزوامل
ALTER TABLE public.zawamil ENABLE ROW LEVEL SECURITY;

-- سياسة القراءة العامة:
-- المستخدم العادي يرى الزوامل المنشورة فقط (is_published = true).
-- المدير المعتمد يرى جميع الزوامل بما فيها المخفية.
CREATE POLICY "Public users can view published zawamil"
    ON public.zawamil
    FOR SELECT
    USING (
        is_published = true 
        OR (auth.role() = 'authenticated' AND public.is_app_admin())
    );

-- سياسات الإضافة والتعديل والحذف: محصورة بالمدير فقط
CREATE POLICY "Admin can insert zawamil"
    ON public.zawamil
    FOR INSERT
    TO authenticated
    WITH CHECK (public.is_app_admin());

CREATE POLICY "Admin can update zawamil"
    ON public.zawamil
    FOR UPDATE
    TO authenticated
    USING (public.is_app_admin())
    WITH CHECK (public.is_app_admin());

CREATE POLICY "Admin can delete zawamil"
    ON public.zawamil
    FOR DELETE
    TO authenticated
    USING (public.is_app_admin());

-- ------------------------------------------------------------------------------
-- 7. دالة إعادة ترتيب الزوامل بأمان وثبات (Atomic Reordering)
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.reorder_zamil(
    p_zamil_id UUID,
    p_direction TEXT -- 'up' أو 'down'
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_curr_order INT;
    v_swap_id UUID;
    v_swap_order INT;
BEGIN
    -- التأكد من صلاحية المدير
    IF NOT public.is_app_admin() THEN
        RAISE EXCEPTION 'غير مصرح لك بتغيير الترتيب';
    END IF;

    SELECT sort_order INTO v_curr_order
    FROM public.zawamil
    WHERE id = p_zamil_id;

    IF v_curr_order IS NULL THEN
        RAISE EXCEPTION 'الزامل غير موجود';
    END IF;

    IF p_direction = 'up' THEN
        -- البحث عن الزامل السابق
        SELECT id, sort_order INTO v_swap_id, v_swap_order
        FROM public.zawamil
        WHERE sort_order < v_curr_order
        ORDER BY sort_order DESC
        LIMIT 1;
    ELSIF p_direction = 'down' THEN
        -- البحث عن الزامل التالي
        SELECT id, sort_order INTO v_swap_id, v_swap_order
        FROM public.zawamil
        WHERE sort_order > v_curr_order
        ORDER BY sort_order ASC
        LIMIT 1;
    ELSE
        RAISE EXCEPTION 'الاتجاه يجب أن يكون up أو down';
    END IF;

    -- إذا وجد عنصر للتبديل معه
    IF v_swap_id IS NOT NULL THEN
        UPDATE public.zawamil SET sort_order = v_swap_order WHERE id = p_zamil_id;
        UPDATE public.zawamil SET sort_order = v_curr_order WHERE id = v_swap_id;
    END IF;
END;
$$;

-- ------------------------------------------------------------------------------
-- 8. إعدادات وسياسات التخزين السحابي (Supabase Storage)
-- ------------------------------------------------------------------------------
-- إنشاء حزم التخزين (Buckets)
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES 
    ('zawamil-audio', 'zawamil-audio', true, 52428800, ARRAY['audio/mpeg', 'audio/mp3', 'audio/wav', 'audio/x-m4a', 'audio/aac']),
    ('zawamil-covers', 'zawamil-covers', true, 10485760, ARRAY['image/jpeg', 'image/png', 'image/webp'])
ON CONFLICT (id) DO UPDATE SET 
    public = true,
    file_size_limit = EXCLUDED.file_size_limit,
    allowed_mime_types = EXCLUDED.allowed_mime_types;

-- سياسات Storage:
-- قراءة عامة للجميع للاستماع وعرض الصور
CREATE POLICY "Public read for audio files"
    ON storage.objects
    FOR SELECT
    USING (bucket_id IN ('zawamil-audio', 'zawamil-covers'));

-- الرفع والتعديل والحذف محصور بالمدير فقط
CREATE POLICY "Admin upload to zawamil storage"
    ON storage.objects
    FOR INSERT
    TO authenticated
    WITH CHECK (
        bucket_id IN ('zawamil-audio', 'zawamil-covers') 
        AND public.is_app_admin()
    );

CREATE POLICY "Admin update zawamil storage"
    ON storage.objects
    FOR UPDATE
    TO authenticated
    USING (
        bucket_id IN ('zawamil-audio', 'zawamil-covers') 
        AND public.is_app_admin()
    );

CREATE POLICY "Admin delete from zawamil storage"
    ON storage.objects
    FOR DELETE
    TO authenticated
    USING (
        bucket_id IN ('zawamil-audio', 'zawamil-covers') 
        AND public.is_app_admin()
    );

-- ------------------------------------------------------------------------------
-- 9. بذور البيانات الأولية (Initial Seed Data)
-- ------------------------------------------------------------------------------
-- تصنيفات تراثية
INSERT INTO public.categories (id, name, description, icon, sort_order)
VALUES
    ('c1111111-1111-1111-1111-111111111111', 'زوامل حماسية', 'زوامل الشجاعة والإقدام والشهامة اليمانية', 'bolt', 1),
    ('c2222222-2222-2222-2222-222222222222', 'زوامل تراثية', 'زوامل القبائل القديمة والشعر التراثي الأصيل', 'history_edu', 2),
    ('c3333333-3333-3333-3333-333333333333', 'زوامل شعبية', 'الألحان الشعبية والفلكلور اليمني العريق', 'music_note', 3),
    ('c4444444-4444-4444-4444-444444444444', 'مناسبات وأعياد', 'زوامل الأعراس والمناسبات والترحيب بالضيوف', 'celebration', 4),
    ('c5555555-5555-5555-5555-555555555555', 'باليه ومحاورة', 'فن الباليه والمحاورات الشعرية القبلية', 'forum', 5)
ON CONFLICT (name) DO NOTHING;

-- زوامل نموذجية أصيلة
INSERT INTO public.zawamil (title, performer, description, category_id, audio_path, cover_path, duration_seconds, sort_order, is_published)
VALUES
    (
        'زامل عهد الأحرار',
        'عيسى الليث',
        'زامل حماسي أصيل يعبر عن الصمود والمجد اليمني',
        'c1111111-1111-1111-1111-111111111111',
        'https://commondatastorage.googleapis.com/codeskulptor-assets/Epoq-Lepidoptera.ogg',
        'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600',
        245,
        1,
        true
    ),
    (
        'زامل صدى الجبال السامقة',
        'قناف المقبلي',
        'لحن تراثي أصيل يحاكي شموخ جبال اليمن الشماء',
        'c2222222-2222-2222-2222-222222222222',
        'https://commondatastorage.googleapis.com/codeskulptor-assets/Epoq-Lepidoptera.ogg',
        'https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600',
        198,
        2,
        true
    ),
    (
        'زامل أهلاً بصناديد الرجال',
        'عبد الخالق النبهان',
        'زامل ترحيبي قبلي للمناسبات ووفود القبائل الكريمة',
        'c4444444-4444-4444-4444-444444444444',
        'https://commondatastorage.googleapis.com/codeskulptor-assets/Epoq-Lepidoptera.ogg',
        'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600',
        215,
        3,
        true
    ),
    (
        'زامل فخر اليماني',
        'صقر الوعيل',
        'كلمات معبرة في الكرم والنجدة وعادات الآباء والأجداد',
        'c3333333-3333-3333-3333-333333333333',
        'https://commondatastorage.googleapis.com/codeskulptor-assets/Epoq-Lepidoptera.ogg',
        'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=600',
        260,
        4,
        true
    ),
    (
        'مسودة زامل جديد (قيد المراجعة)',
        'أبو مجلي',
        'تسجيل تجريبي أولي - خاص بالإدارة فقط',
        'c1111111-1111-1111-1111-111111111111',
        'https://commondatastorage.googleapis.com/codeskulptor-assets/Epoq-Lepidoptera.ogg',
        'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600',
        180,
        5,
        false -- مخفي! لا يظهر إلا للمدير m91857687@gmail.com
    )
ON CONFLICT DO NOTHING;
