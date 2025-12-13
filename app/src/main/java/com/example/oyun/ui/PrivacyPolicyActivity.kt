package com.example.oyun.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.oyun.databinding.ActivityPrivacyPolicyBinding

class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadPrivacyPolicy()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadPrivacyPolicy() {
        val privacyText = """
            GİZLİLİK POLİTİKASI
            
            Son Güncelleme: 10 Aralık 2025
            
            1. GİRİŞ
            Şehir Bilgi Yarışması uygulaması, kullanıcı gizliliğini korumayı taahhüt eder.
            
            2. TOPLANAN BİLGİLER
            • Google Hesabı Bilgileri (ad, e-posta, profil fotoğrafı)
            • Oyun verileri (puanlar, seviyeler, başarımlar)
            • Cihaz bilgileri (model, işletim sistemi)
            
            3. BİLGİLERİN KULLANIMI
            • Oyun deneyiminin kişiselleştirilmesi
            • Skor tablolarının görüntülenmesi
            • Uygulamanın geliştirilmesi
            
            4. REKLAMLAR VE ANALİTİK
            • Google AdMob reklamları (GDPR uyumlu)
            • Firebase Analytics (anonim veriler)
            
            5. BİLGİLERİN PAYLAŞIMI
            • Kişisel bilgiler asla satılmaz veya kiralanmaz
            • Yasal zorunluluk durumunda paylaşılabilir
            
            6. VERİ GÜVENLİĞİ
            Tüm veriler şifrelenmiş olarak saklanır.
            
            7. ÇOCUKLARIN GİZLİLİĞİ
            13 yaş altı çocukları hedef almıyoruz.
            
            8. HAKLARINIZ
            • Kişisel bilgilerinize erişim
            • Bilgilerin düzeltilmesi veya silinmesi
            • Veri işlemeyi reddetme
            
            9. İLETİŞİM
            E-posta: destek@sehirbilgiyarismasi.com
            
            10. ONAY
            Uygulamayı kullanarak bu gizlilik politikasını kabul etmiş olursunuz.
            
            Daha fazla bilgi için:
            https://ufukkartaldev-tech.github.io/privacy-policy/
        """.trimIndent()

        binding.policyText.text = privacyText
    }
}
