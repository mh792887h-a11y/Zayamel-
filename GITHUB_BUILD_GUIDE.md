# تعليمات تشغيل وبناء التطبيق على GitHub و GitHub Actions

تم تجهيز هذا المشروع بالكامل ليعمل ويُبنى تلقائياً عند رفعه على **GitHub**.

---

## 1. محتويات ملفات البناء المجهزة
1. **`gradlew` و `gradlew.bat`**: سكريبتات تشغيل Gradle Wrapper لبيئات Linux و macOS و Windows.
2. **`gradle/wrapper/gradle-wrapper.jar`**: محرك الـ Wrapper (Gradle 9.3.1).
3. **`.github/workflows/android-build.yml`**: خطة عمل GitHub Actions لبناء ملف الـ APK واختبار المشروع آلياً عند كل `git push` أو `Pull Request`.
4. **`debug.keystore.base64`**: مفتاح توقيع نسخة الـ Debug مشفر بصيغة base64 لضمان نجاح البناء في GitHub Actions دون الحاجة لرفع مفاتيح سرية غير آمنة.
5. **`.env.example`**: نموذج لمتغيرات البيئة للاتصال بـ Supabase.

---

## 2. كيفية البناء التلقائي عبر GitHub Actions
بمجرد رفع المشروع على مستودع GitHub (سواء فرع `main` أو `master`):
1. سيقوم GitHub Actions بتفعيل Workflow البناء تلقائياً (`Android CI & Build APK`).
2. يتم فك تشفير `debug.keystore` واستعادة ملف `.env`.
3. يتم تشغيل اختبارات الوحدة و Robolectric للتأكد من سلامة الكود.
4. يتم استخراج ملف الـ APK النهائي وحفظه كـ **Artifact** باسم `app-debug-apk` يمكن تحميله مباشرة من تبويب **Actions** في GitHub!

---

## 3. إعداد أسرار GitHub (Secrets) - اختياري
لربط البناء بقاعدة بياناتك الحقيقية على Supabase عند البناء على GitHub:
1. توجه إلى مستودعك على GitHub.
2. ادخل إلى: **Settings** -> **Secrets and variables** -> **Actions**.
3. أضف الأسرار التالية:
   - `SUPABASE_URL`: رابط مشروعك في Supabase.
   - `SUPABASE_ANON_KEY`: المفتاح العام (Anon key).
   - `GOOGLE_OAUTH_CLIENT_ID`: معرّف Google OAuth Client ID.
   - `ADMIN_EMAIL`: بريدك الإلكتروني كمالك ومدير للتطبيق (`m91857687@gmail.com`).

*(ملاحظة: إذا لم تقم بإدخال هذه الأسرار، سيستخدم GitHub ملف `.env.example` الافتراضي لضمان اكتمال البناء بنجاح دون أي توقف)*.

---

## 4. كيفية البناء محلياً على جهازك
إذا قمت بتنزيل المشروع إلى حاسوبك الشخصي:

```bash
# على نظام Linux أو Mac:
chmod +x gradlew
base64 -d debug.keystore.base64 > debug.keystore
./gradlew assembleDebug

# على نظام Windows:
certutil -decode debug.keystore.base64 debug.keystore
gradlew.bat assembleDebug
```

ستجد ملف الـ APK الناتج في المسار:
`app/build/outputs/apk/debug/app-debug.apk`
