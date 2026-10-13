# دليل الإعداد الشامل لتطبيق زوامل اليمن مع Supabase و Google OAuth

هذا الدليل يوضح الخطوات الدقيقة لربط التطبيق بمشروعك في Supabase وضبط سياسات الأمان وتفعيل تسجيل الدخول بحساب Google المعتمد للمالك (`m91857687@gmail.com`).

---

## 1. إنشاء مشروع Supabase وتطبيق ملف SQL

1. سجل الدخول إلى [Supabase](https://supabase.com).
2. أنشئ مشروعاً جديداً (New Project) واختر المنطقة الأقرب إليك (مثل `eu-central-1` أو `me-central-1`).
3. توجه إلى قسم **SQL Editor** في القائمة الجانبية.
4. انقر على **New Query** وانسخ كامل محتوى الملف `supabase_schema.sql`.
5. انقر على **Run** لتنفيذ الاستعلام.
   - سيتم إنشاء جداول `categories`, `zawamil`, `app_admins`.
   - سيتم تفعيل سياسات Row Level Security (RLS) الصارمة.
   - سيتم إنشاء دالة الحماية `is_app_admin()` ومشغل التسجيل التلقائي للمالك.
   - سيتم إنشاء حزم التخزين `zawamil-audio` و `zawamil-covers` مع سياسات الأمان.
   - سيتم إدراج التصنيفات والزوامل الأولية.

---

## 2. تعداد متغيرات البيئة (Environment Variables)

في لوحة تحكم Supabase، اذهب إلى:
`Project Settings` -> `API`

انسخ القيم التالية وضعها في ملف `.env` في التطبيق (أو أسرار AI Studio):

```env
SUPABASE_URL=https://xxxxxxxxxxxxxxxx.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.xxxxxxxxxxxx
GOOGLE_OAUTH_CLIENT_ID=xxxxxxxxxx.apps.googleusercontent.com
ADMIN_EMAIL=m91857687@gmail.com
```

> **تنبيه أمني شديد الأهمية:**
> استخدم دائماً `anon` (public) key في التطبيق. **إياك** أن تضع `service_role` key في التطبيق أو أي ملف مفتوح المصدر! نظام RLS الذي أنشأناه يتولى التحقق الصارم من الصلاحيات.

---

## 3. إعداد تسجيل الدخول عبر Google OAuth

### أ. في Google Cloud Console:
1. افتح [Google Cloud Console](https://console.cloud.google.com).
2. أنشئ مشروعاً جديداً أو اختر مشروعك الحالي.
3. اذهب إلى **APIs & Services** -> **OAuth consent screen**:
   - نوع المستخدم: **External**.
   - املأ اسم التطبيق: `زوامل اليمن`.
   - بريد الدعم: `m91857687@gmail.com`.
4. اذهب إلى **Credentials** -> **Create Credentials** -> **OAuth client ID**:
   - نوع التطبيق: **Web application** (للتعامل مع Supabase Auth Callback).
   - في خانة **Authorized redirect URIs**، أضف رابط رد الاتصال الخاص بمشروعك في Supabase:
     ```
     https://<YOUR-PROJECT-ID>.supabase.co/auth/v1/callback
     ```
5. انسخ **Client ID** و **Client Secret**.

### ب. في Supabase Dashboard:
1. اذهب إلى **Authentication** -> **Providers** -> **Google**.
2. فعّل خيار **Enable Google**.
3. الصق **Client ID** و **Client Secret**.
4. احفظ الإعدادات.

---

## 4. كيفية تفعيل حساب المدير المعتمد أول مرة

لقد أعددنا في ملف `supabase_schema.sql` مشغلاً آمناً تلقائياً (`tr_on_admin_google_signup`):
1. عندما يقوم المالك (`m91857687@gmail.com`) بالضغط على زر "تسجيل الدخول باستخدام Google":
2. بعد المصادقة الناجحة من Google، يتعرف المشغل على البريد تلقائياً ويدرج معرّفه (`user_id`) في جدول `app_admins`.
3. لا يمكن لأي مستخدم آخر يحمل بريداً مختلفاً أن يُسجل في `app_admins` أو يمنح نفسه صلاحية المدير.

---

## 5. اختبارات الأمان للتحقق من منع المستخدمين العاديين

للتأكد 100% من أن التطبيق وقاعدة البيانات محميان حتى لو استُدعي Supabase مباشرة عبر cURL أو Postman:

### الاختبار الأول: محاولة قراءة الزوامل المخفية
- كزائر عادي أو مستخدم مسجل ببريد غير مصرح:
  ```bash
  curl -X GET "https://<PROJECT-ID>.supabase.co/rest/v1/zawamil?is_published=eq.false" \
       -H "apikey: <ANON_KEY>"
  ```
  **النتيجة المتوقعة:** قائمة فارغة `[]` لأن RLS تمنع إظهار السجلات المخفية.

### الاختبار الثاني: محاولة إضافة زامل جديد بواسطة مستخدم عادي
- أرسل طلب POST لإضافة سجل مع مفتاح anon key دون رمز مصادقة المدير:
  ```bash
  curl -X POST "https://<PROJECT-ID>.supabase.co/rest/v1/zawamil" \
       -H "apikey: <ANON_KEY>" \
       -H "Content-Type: application/json" \
       -d '{"title": "اختراق", "performer": "مخترق", "audio_path": "test.mp3"}'
  ```
  **النتيجة المتوقعة:** خطأ `403 Forbidden` أو `new row violates row-level security policy for table "zawamil"`.

### الاختبار الثالث: محاولة رفع ملف صوتي في مساحة التخزين
- محاولة رفع ملف عبر API التخزين بدون رمز المالك:
  **النتيجة المتوقعة:** رفض فوري مع كود `403 Access Denied`.

---

## 6. إعداد مساحة التخزين (Storage)

حزم التخزين المنشأة هي:
- `zawamil-audio`: مخصصة لملفات MP3 و WAV و M4A بحجم أقصى 50 ميجابايت. تم ضبطها كـ Public لتسمح بمشغل الصوت بتشغيل المقاطع وتدفق HTTP Range لتقديم وتأخير الصوت بسلاسة.
- `zawamil-covers`: مخصصة لصور الأغلفة بحجم أقصى 10 ميجابايت.


---

## 7. ترقية وتطبيق الميزات الـ13 الإضافية (ملف supabase_features_migration.sql)

إذا كنت قد أنشأت المشروع مسبقاً وترغب في ترقيته إلى أحدث إصدار مع الحفاظ التام على جميع بياناتك وزواملك السابقة دون حذف أي شيء:

1. افتح **SQL Editor** في لوحة تحكم Supabase.
2. أنشئ استعلاماً جديداً والصق كامل كود الملف `supabase_features_migration.sql`.
3. انقر على **Run**.

### ما الذي يضيفه هذا التحديث لقاعدة البيانات؟
1. **الميزة 1 (الأكثر استماعاً):** يضيف حقل `plays_count` مع فهرس سريع ودالة RPC آمنة (`record_zamil_play`) تمنع التلاعب المباشر بعدادات الاستماع.
2. **الميزة 2 (المفضلة):** جدول `favorites` مع سياسات RLS صارمة مرتبطة بـ `auth.uid()`.
3. **الميزة 4 (سجل الاستماع وموضع التوقف):** جدول `listening_history` لتتبع آخر موضع استماع وزمن التشغيل.
4. **الميزة 6 (الإشعارات):** جدول `notification_subscriptions` لتسجيل رموز الأجهزة المشتركة.
5. **الميزة 7 (التصنيفات الستة):** إدراج التصنيفات التراثية الستة المعتمدة دون تكرار.
6. **الميزة 8 (الجودات المتعددة):** إضافة حقول `audio_path_low` و `audio_path_high` لروابط البث بجودات متعددة.
