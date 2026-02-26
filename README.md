# 🎨 AI Photo Editor

Android platformu için geliştirilmiş, yapay zeka ile fotoğraf düzenleme uygulaması. İstenmeyen nesneler silinir, arka planlar kaldırılır, çözünürlük artırılır ve yeni arka planlar oluşturulur.

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Material Design](https://img.shields.io/badge/Material%20Design%203-757575?style=for-the-badge&logo=material-design&logoColor=white)

## ✨ Temel Özellikler

Uygulama 4 ana yapay zeka modülünden oluşmaktadır:

* **🪄 Nesne & Kişi Silme (Inpainting):** Ekrana dokunarak çizilen maske ile fotoğraflardaki istenmeyen detaylar kusursuzca yok edilir.
* **✂️ Arka Plan Kaldırma:** Tek dokunuşla portrelerin veya objelerin arka planını şeffaf (PNG) hale getirir.
* **🌌 Arka Plan Değiştirme (Teleport):** Bir metin (prompt) girilerek ve yapay zeka yeni arka plan oluşturur.
* **🔍 Kalite Artırma (Upscaling):** Düşük çözünürlüklü fotoğrafları yapay zeka ile kalite kaybı yaşamadan 2 katına kadar (maks 4096px) büyütür.

### 🚀 Gelişmiş Sistem Özellikleri
* **Yerel Çalışma Geçmişi:** İşlenen tüm fotoğraflar cihaz hafızasında ve Room Database üzerinde tutulur. İstenilen zamanda eski çalışmalara dönülebilir.
* **Akıllı Kota Sistemi:** API maliyetlerini optimize etmek için günlük 8 kullanım hakkı (Gece yarısı otomatik yenilenme).
* **Yedekli API (Failover) Mimarisi:** Kredi bitimi (HTTP 402) durumunda OkHttp Interceptor ile otomatik olarak yedek API anahtarına geçiş.
* **Çoklu Dil Desteği:** İngilizce ve Türkçe dil seçenekleri (Android 13+ Per-App Language uyumlu).
* **Önce/Sonra Karşılaştırması:** Sonuçları anında değerlendirmek için parmakla kaydırılabilir özel "Before/After Slider" görünümü.

## 📱 Ekran Görüntüleri

<div align="center">
  <img src="screenshots/home_page_en.jpeg" width="22%" />
  <img src="screenshots/home_page_tr.jpeg" width="22%" />
  <img src="screenshots/remove_page.jpg" width="22%" />
  <img src="screenshots/history_page.jpg" width="22%" />
  <img src="screenshots/history_bg_clear.jpg" width="22%" />
  <img src="screenshots/history_bg_change.jpg" width="22%" />
</div>

## 🛠️ Teknolojiler ve Mimari

Bu proje, modern Android geliştirme standartlarına uygun olarak tasarlanmıştır.

* **Dil:** Java
* **Minimum SDK:** 30 (Android 11.0)
* **Mimari:** MVVM (Model-View-ViewModel) + Repository Pattern
* **Ağ Katmanı:** Retrofit2 & OkHttp3 (Multipart Form Data)
* **Veritabanı:** Room Database
* **Asenkron İşlemler:** ExecutorService & LiveData
* **Görsel İşleme:** Glide / Özel Bitmap Optimizasyonları (OOM Koruması)
* **UI/UX:** Material Design 3, ViewPager2 (Onboarding), Custom Views (`MaskDrawingView`, `BeforeAfterSliderView`), Core Splashscreen API.