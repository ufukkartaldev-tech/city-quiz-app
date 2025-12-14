#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Hızlı Soru Yükleme Script'i
"""

import json
import firebase_admin
from firebase_admin import credentials, firestore
from datetime import datetime

# Firebase'i başlat
cred = credentials.Certificate("serviceAccountKey.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

# JSON dosyasını oku
with open("firestore_questions.json", "r", encoding="utf-8") as f:
    questions = json.load(f)

print(f"✅ {len(questions)} soru okundu")
print("⏳ Firestore'a yükleniyor...")

# Her soruyu yükle
collection_ref = db.collection('questions')
uploaded = 0

for q in questions:
    # Timestamp ekle
    q['createdAt'] = firestore.SERVER_TIMESTAMP
    q['updatedAt'] = firestore.SERVER_TIMESTAMP
    
    # Firestore'a ekle
    doc_ref = collection_ref.document()  # Auto ID
    doc_ref.set(q)
    uploaded += 1
    print(f"  ✅ {uploaded}/{len(questions)}: {q['questionText'][:50]}...")

print(f"\n🎉 {uploaded} soru başarıyla yüklendi!")
