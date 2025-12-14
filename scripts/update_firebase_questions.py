#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Firebase Soru Güncelleme Script'i
1. Eski soruları sil
2. Yeni gerçek soruları yükle
"""

import json
import firebase_admin
from firebase_admin import credentials, firestore

# Firebase'i başlat
try:
    cred = credentials.Certificate("serviceAccountKey.json")
    firebase_admin.initialize_app(cred)
except:
    pass  # Zaten başlatılmış

db = firestore.client()

print("=" * 60)
print("🔥 FIREBASE SORU GÜNCELLEMESİ")
print("=" * 60)

# 1. Eski soruları sil
print("\n⏳ Adım 1: Eski sorular siliniyor...")
collection_ref = db.collection('questions')

docs = collection_ref.stream()
deleted = 0

batch = db.batch()
for doc in docs:
    batch.delete(doc.reference)
    deleted += 1
    
    # Her 500 işlemde bir commit
    if deleted % 500 == 0:
        batch.commit()
        batch = db.batch()
        print(f"  🗑️  {deleted} soru silindi...")

# Kalan işlemleri commit et
if deleted % 500 != 0:
    batch.commit()

print(f"✅ Toplam {deleted} eski soru silindi!")

# 2. Yeni soruları yükle
print("\n⏳ Adım 2: Yeni gerçek sorular yükleniyor...")

with open("groq_700_questions.json", "r", encoding="utf-8") as f:
    questions = json.load(f)

print(f"📁 {len(questions)} soru okundu")

uploaded = 0
batch_size = 500

for i in range(0, len(questions), batch_size):
    batch = db.batch()
    batch_questions = questions[i:i + batch_size]
    
    for q in batch_questions:
        # Timestamp ekle
        q['createdAt'] = firestore.SERVER_TIMESTAMP
        q['updatedAt'] = firestore.SERVER_TIMESTAMP
        
        # Auto ID ile ekle
        doc_ref = collection_ref.document()
        batch.set(doc_ref, q)
        uploaded += 1
        
        if uploaded % 50 == 0:
            print(f"  ⏳ {uploaded}/{len(questions)} yüklendi...")
    
    # Batch'i commit et
    batch.commit()
    print(f"  ✅ Batch {i//batch_size + 1} tamamlandı ({uploaded}/{len(questions)})")

print(f"\n🎉 {uploaded} gerçek soru başarıyla yüklendi!")

# İstatistikler
print("\n" + "=" * 60)
print("📊 FİNAL İSTATİSTİKLER")
print("=" * 60)

categories = {}
for q in questions:
    cat = q["category"]
    categories[cat] = categories.get(cat, 0) + 1

print("\n📚 Kategorilere Göre:")
for cat, count in sorted(categories.items()):
    print(f"  ✅ {cat}: {count} soru")

print("\n" + "=" * 60)
print("✅ GÜNCELLEME TAMAMLANDI!")
print("=" * 60)
print("\n🎮 Artık HybridQuestionRepository gerçek soruları çekebilir!")
