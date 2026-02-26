# 🎯 Rol ve Görev
Sen uzman bir **Android Geliştirici** ve **UI/UX Tasarımcısısın**. Senden, Clipdrop API'lerini kullanarak "AI Destekli Fotoğraf Düzenleme Mobil Uygulaması" (Object Remover & Enhancer) geliştirmek için eksiksiz, modern ve üretime hazır (production-ready) bir Android projesi yazmanı istiyorum.

---

Projeyi Android studioda açtım.

## ⚙️ Teknik Gereksinimler ve Mimari
* **Dil:** Java
* **Platform:** Minimum API 30 (Android 11.0+) şeklinde oluşturdum.
* **Mimari:** MVVM (Model-View-ViewModel) veya Clean Architecture. Repository pattern kullanılmalı.
* **UI / UX:** Modern Material Design 3 bileşenleri, şık animasyonlar, karanlık mod desteği. Gelişmiş bir kullanıcı deneyimi hedefleniyor.
* **Ağ Katmanı:** Retrofit2 ve OkHttp3. API istekleri asenkron yapılmalı (Java'ya uygun şekilde Retrofit Callbacks, ExecutorService veya RxJava kullanarak, ana thread'i asla bloklamadan).
* **Görsel Yükleme:** Glide veya Picasso.
* **Güvenlik:** API Key doğrudan kod içinde (hardcoded) olmamalı. `local.properties` üzerinden `BuildConfig` ile çekilmeli veya basit bir Node.js proxy yapısı kurulmuş gibi tasarlanıp Base URL ona göre ayarlanmalı. (Şimdilik `local.properties` yaklaşımını koda dök).
* **Performans:** Büyük çözünürlüklü görseller (Out of Memory - OOM hatalarını önlemek için) işlenirken bellekte optimize edilmeli (Bitmap ölçeklendirme). İşlem sırasında modern bir Progress Indicator (örneğin Lottie animasyonu veya Shimmer) gösterilmeli.

---

## 📱 Uygulama Özellikleri (Hedeflenen 3 Ana Modül)

### 1. Nesne/Kişi Silme (Cleanup / Inpainting)
* Kullanıcı galeriden veya kameradan görsel seçer.
* **Önemli:** Ekranda fotoğrafın üzerine parmakla çizim yapılabilen bir Custom View (`MaskDrawingView`) olmalıdır.
* Çizilen maske, API'nin beklediği formata (orijinal resimle aynı boyutta, sadece 0 (siyah) ve 255 (beyaz) piksellerinden oluşan Siyah-Beyaz bir PNG maskesi) dönüştürülmelidir.
* **API:** `POST https://clipdrop-api.co/cleanup/v1` (Multipart: `image_file` ve `mask_file` PNG).
* Sonuç geldiğinde Custom bir "Önce / Sonra (Before/After) Slider" View ile kullanıcıya sunulmalıdır.

### 2. Arka Plan Silme (Remove Background)
* Kullanıcı görsel seçer, tek tuşla işlem başlar.
* **API:** `POST https://clipdrop-api.co/remove-background/v1` (Multipart: `image_file`).
* Dönen şeffaf PNG ekranda gösterilir. Kullanıcıya sonucun arkasına galeriden yeni bir arka plan ekleme veya şeffaf PNG olarak kaydetme opsiyonu sunulur.

### 3. Görüntü Kalite Artırma (Upscale)
* Kullanıcı görsel seçer. Orijinal genişlik ve yükseklik değerleri (maksimum 4096 px olacak şekilde) 2 katına çıkarılacak şekilde hesaplanır (`target_width` ve `target_height`).
* **API:** `POST https://clipdrop-api.co/image-upscaling/v1/upscale` (Multipart: `image_file`, data: `target_width`, `target_height`).
* İşlem bitince "Önce / Sonra Slider" ile detaylar gösterilir.

---
Projeyi adımlar halide yap, tek seferde yapmaan gerek yok. Bir plan hazırla ve o planı uygun bir şekilde yap.
