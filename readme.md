# EDC Mobile - Sistem Otorisasi dan Manajemen PIN

## 📋 Deskripsi Aplikasi

EDC Mobile adalah aplikasi mobile untuk Electronic Data Capture (EDC) yang digunakan dalam sistem perbankan. Aplikasi ini menyediakan fungsi-fungsi kritis untuk operasional harian, manajemen keamanan, dan pengelolaan PIN dengan sistem otorisasi bertingkat.

## 🔑 Hierarki Otorisasi Peran

| Level | Hak Akses | Fungsi Kritis |
|-------|-----------|---------------|
| **Supervisor** | Level tertinggi | Approve, mengaktifkan sesi harian, reset PIN, otorisasi perubahan |
| **Operator** | Eksekusi teknis | Operasional harian, penginputan data, generate atau proses PIN |
| **Teller/User** | Level terbatas | Akses transaksi dasar, verifikasi PIN pengguna akhir |

## 📌 Alur Fungsi Utama

### 1. Start Date (Pembukaan Operasional Harian)

**Proses:**
- Supervisor logon → memasukkan otorisasi → membuka sesi EDC
- Operator dapat melanjutkan menjalankan transaksi setelah sesi terbuka
- Teller tidak dapat memulai, hanya mengikuti sesi yang sudah aktif

**Alasan:** Start Date = fungsi otorisasi utama untuk memastikan perangkat siap digunakan dengan audit yang jelas.

### 2. Close Date (Penutupan Harian)

**Proses:**
- Operator mengirim request tutup sesi
- Supervisor wajib approve dan melakukan closing
- Setelah close, log transaksi harian terkunci dan dikirim ke host

### 3. Logon

| Peran | Aksi |
|-------|------|
| **Operator** | Logon menggunakan User ID sendiri |
| **Supervisor** | Logon dengan supervisor ID untuk aktivasi fitur-fitur otorisasi |
| **Teller** | Jika teller memakai EDC, biasanya melalui operator session atau role khusus selain supervisor |

### 4. Logoff

- Dilakukan oleh Operator saat selesai shift
- Supervisor hanya melakukan logoff jika ingin menonaktifkan akses otorisasi khusus

### 5. Create PIN

**Proses:**
- Permintaan dibuat oleh Operator atau sistem saat kartu baru
- Supervisor menyetujui (authorize)
- PIN diberikan ke Teller/customer secara aman

### 6. Reissue PIN (Permintaan PIN Baru)

**Proses:**
- Operator input alasan reissue
- Supervisor wajib approve
- Teller bisa menjadi pihak yang menyerahkan PIN baru ke nasabah

### 7. Change PIN (Perubahan PIN oleh Nasabah)

**Proses:**
- Teller atau Operator membantu proses
- Tidak membutuhkan supervisor jika dilakukan oleh nasabah sendiri (self-service)
- Jika dilakukan internal (mis: PIN device rusak), Supervisor approve

### 8. Verification PIN

- Bisa dilakukan oleh Teller atau Operator
- Supervisor tidak terlibat kecuali verifikasi dilakukan untuk audit atau bypass kondisi tertentu

## ✨ Ringkasan Flow Kewenangan

| Fungsi | Teller | Operator | Supervisor |
|--------|--------|----------|------------|
| **Start Date** | ❌ | ❌ | ✅ |
| **Close Date** | ❌ | Request | ✅ Approve |
| **Logon** | ⚠️ (Jika user) | ✅ | ✅ |
| **Logoff** | ❌ | ✅ | ✅ |
| **Create PIN** | ❌ | Input | ✅ Approve |
| **Reissue PIN** | ❌ | Input | ✅ Approve |
| **Change PIN** | Nasabah/Teller | ✅ | ⚠️ Approve jika perlu |
| **Verification PIN** | ✅ | ✅ | ⚠️ Opsional |

## 📡 Catatan Operasional

- Proses dibuat bertingkat untuk menjamin keamanan transaksi dan audit trail
- Supervisor berfungsi sebagai authorizer, Operator sebagai executor, Teller sebagai interface ke nasabah
- EDC bank biasanya menerapkan dual control: input oleh operator → approval oleh supervisor

## 🛠️ Teknologi yang Digunakan

- **Platform:** Android (Kotlin)
- **UI Framework:** Jetpack Compose
- **Architecture:** MVVM Pattern
- **Minimum SDK:** API 24 (Android 7.0)

## 📱 Fitur Aplikasi

### Halaman Utama
- Dashboard dengan status terminal
- Menu navigasi ke fungsi-fungsi utama
- Informasi terminal dan versi aplikasi

### Manajemen Keamanan
- Logon/Logoff dengan otorisasi bertingkat
- Manajemen sesi pengguna

### Manajemen PIN
- **Create PIN:** Pembuatan PIN baru dengan validasi 6 digit
- **Change PIN:** Perubahan PIN yang sudah ada
- **Verification PIN:** Verifikasi PIN untuk keperluan audit
- **Reissue PIN:** Permintaan PIN baru dengan alasan

## 🔒 Keamanan

- Sistem otorisasi bertingkat (Supervisor, Operator, Teller)
- Validasi PIN 6 digit dengan enkripsi
- Audit trail untuk semua operasi
- Dual control untuk operasi kritis

## 📄 Lisensi

Aplikasi ini dikembangkan untuk keperluan internal perbankan dengan standar keamanan tinggi.