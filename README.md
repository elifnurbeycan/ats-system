# ATS System Backend

ATS System; adayların, ilk iletişim kayıtlarının, başvuruların, departmanların, pozisyonların ve işe alım pipeline'larının yönetilmesini sağlayan rol ve yetki bazlı bir Applicant Tracking System backend uygulamasıdır.

Frontend repository:  
https://github.com/elifnurbeycan/ats-system-frontend

## Özellikler

- JWT tabanlı kimlik doğrulama
- Rol ve izin bazlı yetkilendirme
- Çok şirketli veri yapısı
- Aday ve başvuru yönetimi
- İlk iletişim havuzu
- İletişim ret nedenleri ve notları
- Departman ve pozisyon yönetimi
- Özelleştirilebilir pipeline ve aşamalar
- Aday süreç geçmişi
- Audit log kayıtları
- CV yükleme ve indirme
- Sayfalama, filtreleme ve sıralama
- Dashboard ve raporlama servisleri
- Excel dışa aktarma desteği
- E-posta bildirimleri
- Rate limiting ve brute-force koruması
- Merkezi exception yönetimi
- Soft-delete ve arşivleme desteği

## Kullanılan Teknolojiler

- Java 21
- Spring Boot 3.5.16
- Spring Web
- Spring Security
- OAuth2 Resource Server
- Spring Data JPA
- Spring Validation
- Spring AOP
- Spring Mail
- Spring Actuator
- PostgreSQL 17
- Flyway
- Redis
- JWT
- MapStruct
- Lombok
- Maven

## Gereksinimler

- JDK 21
- PostgreSQL 17
- Maven 3.9 veya Maven Wrapper
- Git
- İsteğe bağlı olarak Redis
- İsteğe bağlı olarak Docker ve Mailpit

## Repository'yi Klonlama

```powershell
git clone https://github.com/elifnurbeycan/ats-system.git
cd ats-system
```

## Veritabanı Oluşturma

PostgreSQL kurulumu `SQL_ASCII` template kullanıyorsa UTF-8 veritabanı doğrudan oluşturulamayabilir. Bu nedenle veritabanı `template0` üzerinden oluşturulmalıdır:

```powershell
& "C:\Program Files\PostgreSQL\17\bin\createdb.exe" `
  -h localhost `
  -p 5432 `
  -U postgres `
  -T template0 `
  -E UTF8 `
  ats_system
```

Alternatif SQL komutu:

```sql
CREATE DATABASE ats_system
    WITH
    OWNER = postgres
    TEMPLATE = template0
    ENCODING = 'UTF8';
```

Veritabanı kodlamasını kontrol etmek için:

```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
  -h localhost `
  -p 5432 `
  -U postgres `
  -d postgres `
  -c "\l ats_system"
```

## Geliştirme Veritabanını Geri Yükleme

Repository içerisinde anonimleştirilmiş geliştirme veritabanı bulunmaktadır:

```text
database/ats_system_seed.dump
```

Yedeği geri yüklemek için:

```powershell
& "C:\Program Files\PostgreSQL\17\bin\pg_restore.exe" `
  -h localhost `
  -p 5432 `
  -U postgres `
  -d ats_system `
  --no-owner `
  --no-privileges `
  -v `
  ".\database\ats_system_seed.dump"
```

Bu yedek gerçek parola, token, audit log, CV dosyası veya kişisel aday bilgisi içermez.

## Yerel Konfigürasyon

Örnek geliştirme konfigürasyonunu kopyalayın:

```powershell
Copy-Item `
  src/main/resources/application-dev.example.yaml `
  src/main/resources/application-dev.yaml
```

`application-dev.yaml` içerisindeki PostgreSQL bağlantı bilgilerini kendi ortamınıza göre düzenleyin:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ats_system
    username: postgres
    password: POSTGRESQL_PAROLANIZ
```

`application-dev.yaml` yerel parola içerebileceği için GitHub'a gönderilmemelidir.

## Uygulamayı Çalıştırma

Maven Wrapper ile:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Sistemde Maven kuruluysa:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Backend varsayılan olarak aşağıdaki adreste çalışır:

```text
http://localhost:8080
```

## Testleri Çalıştırma

Maven Wrapper ile:

```powershell
.\mvnw.cmd clean test
```

Maven ile:

```powershell
mvn clean test
```

## Mailpit ile E-posta Testi

Mailpit container'ını çalıştırın:

```powershell
docker run --name ats-mailpit -d `
  -p 1025:1025 `
  -p 8025:8025 `
  axllent/mailpit
```

Mailpit arayüzü:

```text
http://localhost:8025
```

SMTP bağlantısı:

```text
Host: localhost
Port: 1025
```

Daha önce oluşturulmuş container'ı yeniden çalıştırmak için:

```powershell
docker start ats-mailpit
```

## Yerel Portlar

| Servis | Port |
|---|---:|
| Frontend | `3000` |
| Backend | `8080` |
| PostgreSQL | `5432` |
| Mailpit SMTP | `1025` |
| Mailpit arayüzü | `8025` |

## Proje Yapısı

```text
src/main/java/com/yasarbilgi/ats/
├── auth/               Kimlik doğrulama
├── candidate/          Aday işlemleri
├── candidateprocess/   Başvuru ve süreç yönetimi
├── communication/      İlk iletişim yönetimi
├── department/         Departman yönetimi
├── position/           Pozisyon yönetimi
├── pipeline/           Pipeline ve aşama yönetimi
├── dashboard/          Dashboard ve raporlama
├── audit/              Audit kayıtları
├── security/           Güvenlik yapılandırmaları
├── common/             Ortak sınıflar ve exception yönetimi
├── company/            Şirket yönetimi
├── role/               Rol yönetimi
├── permission/         İzin yönetimi
└── user/               Kullanıcı yönetimi

src/main/resources/
├── db/migration/       Flyway migration dosyaları
├── application.yaml
├── application-dev.example.yaml
└── application-dev.yaml
```

## Güvenlik Notları

- Gerçek parolalar ve token'lar repository'ye eklenmemelidir.
- `application-dev.yaml` Git tarafından takip edilmemelidir.
- Yetki kontrolleri yalnızca frontend'e bırakılmamalıdır.
- Üretim ortamında JWT anahtarları ve veritabanı parolaları environment variable veya secret manager üzerinden sağlanmalıdır.
- Üretim ortamında dosya depolama ve e-posta ayarları ayrıca yapılandırılmalıdır.

## Commit Standardı

```text
feat: yeni özellik
fix: hata düzeltmesi
refactor: davranışı değiştirmeyen kod düzenlemesi
docs: dokümantasyon değişikliği
test: test ekleme veya güncelleme
build: bağımlılık veya build değişikliği
chore: bakım işlemi
```

## Proje Durumu

Proje aktif olarak geliştirilmektedir.
