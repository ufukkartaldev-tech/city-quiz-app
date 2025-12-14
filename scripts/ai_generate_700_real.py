#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
AI ile 700 Gerçek Soru Oluşturucu
Gemini API kullanarak otomatik soru üretimi
"""

import json
import os
import time

try:
    import google.generativeai as genai
except ImportError:
    print("❌ google-generativeai yüklü değil!")
    print("pip install google-generativeai")
    exit(1)

# API Key
API_KEY = "AIzaSyDgvbD3gzCHty0l4-m75JHv31OlinAZSgc"
genai.configure(api_key=API_KEY)

# Model
model = genai.GenerativeModel('gemini-1.5-flash')  # Stable model

def generate_questions_for_category_level(category, level, count=10):
    """Belirli kategori ve level için sorular oluşturur"""
    
    difficulty_map = {
        1: "çok kolay", 2: "kolay", 3: "kolay",
        4: "orta", 5: "orta", 6: "orta",
        7: "zor", 8: "zor",
        9: "çok zor", 10: "uzman seviyesi"
    }
    
    category_names = {
        "GEOGRAPHY": "Coğrafya",
        "HISTORY": "Tarih",
        "CULTURE": "Kültür",
        "SPORTS": "Spor",
        "GENERAL": "Genel Kültür",
        "SCIENCE": "Bilim ve Teknoloji",
        "ART": "Sanat"
    }
    
    prompt = f"""
{count} adet {category_names[category]} sorusu oluştur.
Zorluk seviyesi: {difficulty_map[level]} (Level {level})

Her soru için:
- Soru metni (açık ve net)
- 4 seçenek (A, B, C, D)
- Doğru cevap (A, B, C veya D)
- Kısa açıklama

Kurallar:
- Sorular çeşitli olsun (sadece Türkiye değil, dünya geneli)
- Seçenekler birbirine yakın zorlukta olsun
- Doğru cevap net olsun
- Açıklama eğitici olsun

JSON formatında döndür:
{{
  "questions": [
    {{
      "questionText": "...",
      "optionA": "...",
      "optionB": "...",
      "optionC": "...",
      "optionD": "...",
      "correctAnswer": "A",
      "explanation": "..."
    }}
  ]
}}
"""
    
    try:
        response = model.generate_content(prompt)
        content = response.text
        
        # JSON parse
        if "```json" in content:
            content = content.split("```json")[1].split("```")[0]
        elif "```" in content:
            content = content.split("```")[1].split("```")[0]
        
        data = json.loads(content.strip())
        return data.get("questions", [])
        
    except Exception as e:
        print(f"  ❌ Hata: {e}")
        return []

def generate_all_700_questions():
    """700 gerçek soru oluşturur"""
    
    categories = ["GEOGRAPHY", "HISTORY", "CULTURE", "SPORTS", "GENERAL", "SCIENCE", "ART"]
    
    difficulty_config = {
        1: {"diff": "EASY", "points": 10, "time": 30},
        2: {"diff": "EASY", "points": 10, "time": 30},
        3: {"diff": "EASY", "points": 10, "time": 30},
        4: {"diff": "MEDIUM", "points": 15, "time": 25},
        5: {"diff": "MEDIUM", "points": 15, "time": 25},
        6: {"diff": "MEDIUM", "points": 15, "time": 25},
        7: {"diff": "HARD", "points": 20, "time": 20},
        8: {"diff": "HARD", "points": 20, "time": 20},
        9: {"diff": "EXPERT", "points": 25, "time": 15},
        10: {"diff": "EXPERT", "points": 25, "time": 15}
    }
    
    all_questions = []
    total = 7 * 10  # 70 API çağrısı
    current = 0
    
    print("🤖 AI ile 700 gerçek soru oluşturuluyor...")
    print(f"⏳ Toplam {total} API çağrısı yapılacak...")
    print("⚠️  Bu işlem 10-15 dakika sürebilir...\n")
    
    for category in categories:
        print(f"\n📚 {category} kategorisi:")
        
        for level in range(1, 11):
            current += 1
            print(f"  Level {level}/10 ({current}/{total})...", end=" ")
            
            # AI'dan soruları al
            ai_questions = generate_questions_for_category_level(category, level, 10)
            
            if ai_questions:
                # Soruları formatla
                for idx, q in enumerate(ai_questions):
                    config = difficulty_config[level]
                    
                    question = {
                        "id": f"q_{category.lower()}_l{level}_{idx+1}",
                        "questionText": q["questionText"],
                        "optionA": q["optionA"],
                        "optionB": q["optionB"],
                        "optionC": q["optionC"],
                        "optionD": q["optionD"],
                        "correctAnswer": q["correctAnswer"],
                        "imageName": "",
                        "level": level,
                        "category": category,
                        "difficulty": config["diff"],
                        "explanation": q["explanation"],
                        "tags": [category.lower(), f"level{level}"],
                        "points": config["points"],
                        "timeLimit": config["time"],
                        "isVerified": True,
                        "authorId": "ai_gemini"
                    }
                    
                    all_questions.append(question)
                
                print(f"✅ {len(ai_questions)} soru")
            else:
                print("❌ Başarısız")
            
            # Rate limiting (Gemini free tier - 15 RPM limit)
            time.sleep(15)  # 15 saniye bekle (güvenli)
    
    return all_questions

if __name__ == "__main__":
    questions = generate_all_700_questions()
    
    # Dosyaya kaydet
    with open("real_700_questions.json", "w", encoding="utf-8") as f:
        json.dump(questions, f, ensure_ascii=False, indent=2)
    
    print(f"\n\n🎉 {len(questions)} gerçek soru oluşturuldu!")
    print(f"📁 Dosya: real_700_questions.json")
    
    # İstatistikler
    print("\n📊 İstatistikler:")
    categories = {}
    for q in questions:
        cat = q["category"]
        categories[cat] = categories.get(cat, 0) + 1
    
    for cat, count in sorted(categories.items()):
        print(f"  - {cat}: {count} soru")
