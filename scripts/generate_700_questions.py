#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
700 Soru Oluşturucu
7 kategori × 10 level × 10 soru = 700 soru
"""

import json

# Soru şablonları
GEOGRAPHY_QUESTIONS = {
    1: [  # Level 1 - Kolay
        {"q": "Türkiye'nin başkenti neresidir?", "a": "İstanbul", "b": "Ankara", "c": "İzmir", "d": "Bursa", "correct": "B", "exp": "Türkiye'nin başkenti 1923'ten beri Ankara'dır."},
        {"q": "Dünyanın en büyük okyanusu hangisidir?", "a": "Atlas", "b": "Hint", "c": "Pasifik", "d": "Arktik", "correct": "C", "exp": "Pasifik Okyanusu dünyanın en büyük okyanusudur."},
        {"q": "Hangi kıta en büyüktür?", "a": "Afrika", "b": "Asya", "c": "Avrupa", "d": "Amerika", "correct": "B", "exp": "Asya, dünya yüzölçümünün yaklaşık %30'unu kaplar."},
        {"q": "Nil Nehri hangi kıtadadır?", "a": "Asya", "b": "Avrupa", "c": "Afrika", "d": "Amerika", "correct": "C", "exp": "Nil Nehri Afrika'nın en uzun nehridir."},
        {"q": "Hangi ülke en kalabalıktır?", "a": "Hindistan", "b": "Çin", "c": "ABD", "d": "Endonezya", "correct": "B", "exp": "Çin, 1.4 milyar nüfusuyla dünyanın en kalabalık ülkesidir."},
        {"q": "Akdeniz hangi kıtalara kıyısı vardır?", "a": "Sadece Avrupa", "b": "Avrupa ve Afrika", "c": "Avrupa, Afrika, Asya", "d": "Sadece Afrika", "correct": "C", "exp": "Akdeniz üç kıtaya kıyısı olan bir denizdir."},
        {"q": "Dünyanın en yüksek dağı hangisidir?", "a": "K2", "b": "Everest", "c": "Kilimanjaro", "d": "Mont Blanc", "correct": "B", "exp": "Everest, 8.849 metre yüksekliğiyle dünyanın en yüksek dağıdır."},
        {"q": "Hangi ülke 'Güneşin Doğduğu Ülke' olarak bilinir?", "a": "Çin", "b": "Japonya", "c": "Kore", "d": "Tayland", "correct": "B", "exp": "Japonya, Güneşin Doğduğu Ülke olarak bilinir."},
        {"q": "Amazon Ormanları hangi kıtadadır?", "a": "Afrika", "b": "Asya", "c": "Güney Amerika", "d": "Avustralya", "correct": "C", "exp": "Amazon Ormanları Güney Amerika'dadır."},
        {"q": "Hangi ülkenin başkenti Paris'tir?", "a": "İtalya", "b": "İspanya", "c": "Fransa", "d": "Almanya", "correct": "C", "exp": "Paris, Fransa'nın başkentidir."}
    ]
}

def generate_all_questions():
    """700 soru oluşturur"""
    
    categories = {
        "GEOGRAPHY": "Coğrafya",
        "HISTORY": "Tarih", 
        "CULTURE": "Kültür",
        "SPORTS": "Spor",
        "GENERAL": "Genel Kültür",
        "SCIENCE": "Bilim",
        "ART": "Sanat"
    }
    
    difficulties = {
        1: "EASY",
        2: "EASY",
        3: "EASY",
        4: "MEDIUM",
        5: "MEDIUM",
        6: "MEDIUM",
        7: "HARD",
        8: "HARD",
        9: "EXPERT",
        10: "EXPERT"
    }
    
    points_map = {
        "EASY": 10,
        "MEDIUM": 15,
        "HARD": 20,
        "EXPERT": 25
    }
    
    time_map = {
        "EASY": 30,
        "MEDIUM": 25,
        "HARD": 20,
        "EXPERT": 15
    }
    
    all_questions = []
    question_id = 1
    
    for category in categories.keys():
        for level in range(1, 11):  # 1-10
            difficulty = difficulties[level]
            
            for q_num in range(1, 11):  # 10 soru per level
                question = {
                    "id": f"q_{category.lower()}_l{level}_{q_num}",
                    "questionText": f"{categories[category]} - Level {level} - Soru {q_num}",
                    "optionA": "Seçenek A",
                    "optionB": "Seçenek B", 
                    "optionC": "Seçenek C",
                    "optionD": "Seçenek D",
                    "correctAnswer": "A",
                    "imageName": "",
                    "level": level,
                    "category": category,
                    "difficulty": difficulty,
                    "explanation": f"Bu {categories[category]} sorusunun açıklaması.",
                    "tags": [category.lower(), f"level{level}"],
                    "points": points_map[difficulty],
                    "timeLimit": time_map[difficulty],
                    "isVerified": True,
                    "authorId": "system"
                }
                
                all_questions.append(question)
                question_id += 1
    
    return all_questions

# Soruları oluştur
print("⏳ 700 soru oluşturuluyor...")
questions = generate_all_questions()

# Dosyaya kaydet
with open("all_700_questions.json", "w", encoding="utf-8") as f:
    json.dump(questions, f, ensure_ascii=False, indent=2)

print(f"✅ {len(questions)} soru oluşturuldu!")
print(f"📁 Dosya: all_700_questions.json")

# İstatistikler
print("\n📊 İstatistikler:")
categories = {}
for q in questions:
    cat = q["category"]
    categories[cat] = categories.get(cat, 0) + 1

for cat, count in sorted(categories.items()):
    print(f"  - {cat}: {count} soru")
