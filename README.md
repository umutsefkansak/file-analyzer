# Projenin Amacı

Bu proje, metin dosyalarının analizini yapan ve bu dosyaları ZIP arşivine dönüştüren çok iş parçacıklı bir uygulamadır. Büyük miktarda metin dosyasını hızlı ve verimli bir şekilde işlemek için tasarlanmıştır.

Proje, kullanıcıların .txt dosyalarını yükleyebilmesini ve bu dosyaların detaylı analizini gerçekleştirmesini sağlar. Her dosya için satır sayısı ve karakter sayısı hesaplanır, işlem süresi takip edilir. Sistem ayrıca aynı anda birden fazla dosyayı işleyebilme kapasitesine de sahiptir.

Analiz işlemi tamamlandıktan sonra, sistem işlenen dosyaları ZIP arşivine dönüştürür. Bu arşivleme işlemi, dosyaların saklanmasını sağlar. Arşiv dosyaları kullanıcılar tarafından indirebilir.

# Nasıl Çalıştırılır?

## Gereksinimler

Bu projeyi çalıştırmak için sisteminizde aşağıdaki yazılımların kurulu olması gerekmektedir:

• Java 17 veya üzeri sürüm

• Maven 3.6 veya üzeri sürüm

• Spring Boot 3.x framework'ü

• Git (projeyi klonlamak için)

## Kurulum Adımları

**1. Projeyi Klonlama**
```bash
git clone https://github.com/umutsefkansak/file-analyzer.git
```

**2. Bağımlılıkları Yükleme**
```bash
mvn clean install
```

**3. Uygulamayı Çalıştırma**
```bash
mvn spring-boot:run
```

## API Endpoint'leri

Uygulama çalıştıktan sonra aşağıdaki API endpoint'lerini kullanabilirsiniz:

### Dosya Analizi Endpoint'leri:

• `POST /api/v1/files/analyze` - Mevcut dizindeki tüm .txt dosyalarını analiz eder

• `POST /api/v1/files/upload-and-analyze` - Tek dosya yükler ve analiz eder

• `POST /api/v1/files/upload-multiple-and-analyze` - Çoklu dosya yükler ve analiz eder

### Arşiv İşlemleri:

• `POST /api/v1/files/unzip` - ZIP dosyasını çıkarır

• `GET /api/v1/files/download/{filename}` - Dosya indirir

### Sistem Endpoint'leri:

• `GET /api/v1/files/config` - Dizin konfigürasyonunu getirir

# Örnek Çıktılar

### 1. Ana Ekran

![WhatsApp Image 2025-07-20 at 21 11 42](https://github.com/user-attachments/assets/cfee9ffd-6b86-4543-9fc1-ed8c35689fb0)


### 2. Çoklu Dosya Seçimi ve Analiz Ekranı:

![WhatsApp Image 2025-07-20 at 21 11 48](https://github.com/user-attachments/assets/1eb55c6f-5e76-4fa5-8381-ecfa4504c25d)

### 3. Arşiv Bilgileri

![WhatsApp Image 2025-07-20 at 21 11 54](https://github.com/user-attachments/assets/36954cd4-a327-4496-8aba-19274d2baad7)

# Görev Dağılımları

Ağcanur Beyza Kaynar:

* FileProccessingService geliştirilmesi.

* FrontEnd geliştirilmesi için gerekli teknoloji araştırılması.

* FrontEnd’in baştan sona geliştirilmesi.

* Rapor dosyasının hazırlanması.

Atalay Berk Çırak:

* ArchiveService geliştirilmesi.

* Hatalı branch isimlerinin düzeltilmesi

* Opsiyonel olan Unzip metodunun geliştirilmesi

* README.md dosyasının hazırlanması

Barış Dalyan Emre:

* Global Exception Handler

* Geliştirilen service’lere Global Exception Handling entegrasyonu

* FileAnalysisService geliştirilmesi

* CoreFileAnalvsisService geliştirilmesi

* Analiz işlemini yapacak Controller yazılması.

* Controller için gerekli DTO’ların oluşturulması

* Global ExceptionHandling entegrasyonunun devamı

Umut Sefkan Sak:

* Github repository oluşturma.

* ThreadConfig dosyasının oluşturulması, Entity sınıflarının oluşturulması, Paketlerin
oluşturulması.

* Tespit edilen eksiklerin giderilmesi (eksik anotasyon, metot, interface vs.)

* ThreadManagementService geliştirilmesi.

* FileUploadService geliştirilmesi

* FileDownloadService geliştirilmesi

* FileUploadService ile ilgili endpointlerin eklenmesi

* FileDownloadService ile ilgili endpointlerin eklenmesi


# Akış Şeması

![WhatsApp Image 2025-07-20 at 21 16 48](https://github.com/user-attachments/assets/2c2deb26-b1ba-4151-8bfc-8798334d3a15)
