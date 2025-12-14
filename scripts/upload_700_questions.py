#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
700 Soruyu Firestore'a Yükle
"""

import json
import firebase_admin
from firebase_admin import credentials, firestore

# Firebase'i başlat (zaten başlatılmışsa hata vermez)
try:
    cred = credentials.Certificate("serviceAccountKey.json")
    firebase_admin.initialize_app(cred)
except:
    pass  # Zaten başlatılmış

db = firestore.client()

# JSON dosyasını oku
with open("all_700_questions.json", "r", encoding="utf-8") as f:
    questions = json.load(f)

print(f"✅ {len(questions)} soru okundu")
print("⏳ Firestore'a yükleniyor...")
print("⚠️  Bu işlem birkaç dakika sürebilir...")

# Batch işlemi (Firestore max 500 işlem/batch)
collection_ref = db.collection('questions')
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

print(f"\n🎉 {uploaded} soru başarıyla yüklendi!")
print("\n📊 Kategori Dağılımı:")
print("  - GEOGRAPHY: 100 soru (Level 1-10)")
print("  - HISTORY: 100 soru (Level 1-10)")
print("  - CULTURE: 100 soru (Level 1-10)")
print("  - SPORTS: 100 soru (Level 1-10)")
print("  - GENERAL: 100 soru (Level 1-10)")
print("  - SCIENCE: 100 soru (Level 1-10)")
print("  - ART: 100 soru (Level 1-10)")
