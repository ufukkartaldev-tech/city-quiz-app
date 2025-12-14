#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Firestore'a Soru Yükleme Script'i
Kullanım: python firebase_upload.py
"""

import json
import sys
from pathlib import Path

try:
    import firebase_admin
    from firebase_admin import credentials, firestore
except ImportError:
    print("❌ Firebase Admin SDK yüklü değil!")
    print("📦 Yüklemek için: pip install firebase-admin")
    sys.exit(1)

# ============================================
# FIREBASE BAĞLANTISI
# ============================================

def initialize_firebase(service_account_path: str = "serviceAccountKey.json"):
    """Firebase'i başlatır"""
    try:
        cred = credentials.Certificate(service_account_path)
        firebase_admin.initialize_app(cred)
        print("✅ Firebase bağlantısı başarılı!")
        return firestore.client()
    except Exception as e:
        print(f"❌ Firebase bağlantı hatası: {e}")
        print("\n📝 serviceAccountKey.json dosyasını oluşturmak için:")
        print("  1. Firebase Console > Project Settings > Service Accounts")
        print("  2. 'Generate New Private Key' butonuna tıklayın")
        print("  3. İndirilen dosyayı 'serviceAccountKey.json' olarak kaydedin")
        sys.exit(1)

# ============================================
# SORU YÜKLEME FONKSİYONLARI
# ============================================

def upload_questions_to_firestore(db, questions: list, batch_size: int = 500):
    """Soruları Firestore'a yükler (batch işlemi)"""
    collection_ref = db.collection('questions')
    
    total = len(questions)
    uploaded = 0
    failed = 0
    
    print(f"\n⏳ {total} soru Firestore'a yükleniyor...")
    
    # Batch işlemi (Firestore max 500 işlem/batch)
    for i in range(0, total, batch_size):
        batch = db.batch()
        batch_questions = questions[i:i + batch_size]
        
        for question in batch_questions:
            doc_ref = collection_ref.document(f"question_{question['id']}")
            try:
                batch.set(doc_ref, question)
                uploaded += 1
            except Exception as e:
                print(f"❌ Soru {question['id']} yüklenemedi: {e}")
                failed += 1
        
        try:
            batch.commit()
            print(f"✅ Batch {i//batch_size + 1} yüklendi ({uploaded}/{total})")
        except Exception as e:
            print(f"❌ Batch hatası: {e}")
            failed += len(batch_questions)
    
    print(f"\n📊 Sonuç:")
    print(f"  ✅ Başarılı: {uploaded}")
    print(f"  ❌ Başarısız: {failed}")
    print(f"  📝 Toplam: {total}")

def upload_from_json(db, json_file: str = "questions_extended.json"):
    """JSON dosyasından soruları yükler"""
    try:
        with open(json_file, 'r', encoding='utf-8') as f:
            questions = json.load(f)
        
        print(f"✅ {len(questions)} soru '{json_file}' dosyasından okundu")
        upload_questions_to_firestore(db, questions)
        
    except FileNotFoundError:
        print(f"❌ '{json_file}' dosyası bulunamadı!")
        print("💡 Önce question_generator.py script'ini çalıştırın")
        sys.exit(1)
    except json.JSONDecodeError as e:
        print(f"❌ JSON parse hatası: {e}")
        sys.exit(1)

# ============================================
# SORU SİLME FONKSİYONLARI
# ============================================

def delete_all_questions(db):
    """Tüm soruları siler (DİKKAT!)"""
    collection_ref = db.collection('questions')
    
    print("\n⚠️  UYARI: Tüm sorular silinecek!")
    confirm = input("Devam etmek için 'EVET' yazın: ")
    
    if confirm != "EVET":
        print("❌ İşlem iptal edildi")
        return
    
    print("\n⏳ Sorular siliniyor...")
    
    # Tüm dokümanları al
    docs = collection_ref.stream()
    deleted = 0
    
    batch = db.batch()
    for doc in docs:
        batch.delete(doc.reference)
        deleted += 1
        
        # Her 500 işlemde bir commit et
        if deleted % 500 == 0:
            batch.commit()
            batch = db.batch()
            print(f"  🗑️  {deleted} soru silindi...")
    
    # Kalan işlemleri commit et
    if deleted % 500 != 0:
        batch.commit()
    
    print(f"\n✅ Toplam {deleted} soru silindi")

# ============================================
# SORU GÜNCELLEME FONKSİYONLARI
# ============================================

def update_question_field(db, question_id: int, field: str, value):
    """Belirli bir sorunun belirli bir alanını günceller"""
    doc_ref = db.collection('questions').document(f"question_{question_id}")
    
    try:
        doc_ref.update({field: value})
        print(f"✅ Soru {question_id} güncellendi: {field} = {value}")
    except Exception as e:
        print(f"❌ Güncelleme hatası: {e}")

# ============================================
# SORU SORGULAMA FONKSİYONLARI
# ============================================

def query_questions(db, category: str = None, difficulty: str = None, level: int = None):
    """Soruları filtreler ve gösterir"""
    collection_ref = db.collection('questions')
    query = collection_ref
    
    if category:
        query = query.where('category', '==', category)
    if difficulty:
        query = query.where('difficulty', '==', difficulty)
    if level:
        query = query.where('level', '==', level)
    
    docs = query.stream()
    questions = [doc.to_dict() for doc in docs]
    
    print(f"\n📊 {len(questions)} soru bulundu")
    
    if questions:
        print("\n📝 İlk 5 soru:")
        for q in questions[:5]:
            print(f"  - [{q['id']}] {q['questionText'][:50]}...")
    
    return questions

# ============================================
# İSTATİSTİK FONKSİYONLARI
# ============================================

def show_statistics(db):
    """Firestore'daki soru istatistiklerini gösterir"""
    collection_ref = db.collection('questions')
    docs = collection_ref.stream()
    questions = [doc.to_dict() for doc in docs]
    
    print("\n" + "="*50)
    print("📊 FIRESTORE SORU İSTATİSTİKLERİ")
    print("="*50)
    
    print(f"\n📝 Toplam Soru: {len(questions)}")
    
    # Kategoriye göre
    categories = {}
    for q in questions:
        cat = q.get('category', 'UNKNOWN')
        categories[cat] = categories.get(cat, 0) + 1
    
    print("\n📚 Kategoriye Göre:")
    for cat, count in sorted(categories.items()):
        print(f"  - {cat}: {count}")
    
    # Zorluk seviyesine göre
    difficulties = {}
    for q in questions:
        diff = q.get('difficulty', 'UNKNOWN')
        difficulties[diff] = difficulties.get(diff, 0) + 1
    
    print("\n⭐ Zorluk Seviyesine Göre:")
    for diff, count in sorted(difficulties.items()):
        print(f"  - {diff}: {count}")
    
    # Level'a göre
    levels = {}
    for q in questions:
        level = q.get('level', 0)
        levels[level] = levels.get(level, 0) + 1
    
    print("\n🎯 Level'a Göre:")
    for level, count in sorted(levels.items()):
        print(f"  - Level {level}: {count}")
    
    print("\n" + "="*50)

# ============================================
# ANA MENÜ
# ============================================

def show_menu():
    """Ana menüyü gösterir"""
    print("\n" + "="*50)
    print("🔥 FIRESTORE SORU YÖNETİMİ")
    print("="*50)
    print("\n1. Soruları Yükle (JSON'dan)")
    print("2. İstatistikleri Göster")
    print("3. Soruları Sorgula")
    print("4. Tüm Soruları Sil (DİKKAT!)")
    print("5. Çıkış")
    print("\n" + "="*50)

def main():
    """Ana fonksiyon"""
    print("🎮 Firestore Soru Yönetimi Script'i")
    
    # Firebase'i başlat
    db = initialize_firebase()
    
    while True:
        show_menu()
        choice = input("\nSeçiminiz (1-5): ").strip()
        
        if choice == "1":
            json_file = input("JSON dosya adı (varsayılan: questions_extended.json): ").strip() or "questions_extended.json"
            upload_from_json(db, json_file)
        
        elif choice == "2":
            show_statistics(db)
        
        elif choice == "3":
            print("\n📋 Filtreler (boş bırakabilirsiniz):")
            category = input("Kategori (GEOGRAPHY, HISTORY, vb.): ").strip() or None
            difficulty = input("Zorluk (EASY, MEDIUM, HARD, EXPERT): ").strip() or None
            level_input = input("Level (1-10): ").strip()
            level = int(level_input) if level_input else None
            
            query_questions(db, category, difficulty, level)
        
        elif choice == "4":
            delete_all_questions(db)
        
        elif choice == "5":
            print("\n👋 Çıkış yapılıyor...")
            break
        
        else:
            print("\n❌ Geçersiz seçim!")

if __name__ == "__main__":
    main()
