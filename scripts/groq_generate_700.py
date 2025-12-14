#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
GROQ ile 700 Gerçek Soru Oluşturucu
Çok hızlı ve güvenilir!
"""

import json
import time
from groq import Groq

# Groq Client
client = Groq() # API anahtarını ortam değişkenlerinden alır (vaya kendiniz buraya yazın ama git'e pushlemeyin)

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
- Soru metni (açık ve net, Türkçe)
- 4 seçenek (A, B, C, D)
- Doğru cevap (A, B, C veya D)
- Kısa açıklama

Kurallar:
- Sorular çeşitli olsun (dünya geneli)
- Seçenekler birbirine yakın zorlukta olsun
- Doğru cevap net olsun
- Açıklama eğitici olsun
- Türkçe yaz

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

Sadece JSON döndür, başka bir şey yazma.
"""
    
    try:
        chat_completion = client.chat.completions.create(
            messages=[
                {
                    "role": "user",
                    "content": prompt,
                }
            ],
            model="llama-3.3-70b-versatile",  # En iyi model
            temperature=0.7,
            max_tokens=4000
        )
        
        content = chat_completion.choices[0].message.content
        
        # JSON parse
        if "```json" in content:
            content = content.split("```json")[1].split("```")[0]
        elif "```" in content:
            content = content.split("```")[1].split("```")[0]
        
        data = json.loads(content.strip())
        return data.get("questions", [])
        
    except Exception as e:
        print(f"❌ Hata: {e}")
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
    
    print("🚀 GROQ ile 700 gerçek soru oluşturuluyor...")
    print(f"⏳ Toplam {total} API çağrısı yapılacak...")
    print("⚡ Groq çok hızlı! ~5-7 dakika sürecek...\n")
    
    for category in categories:
        print(f"\n📚 {category} kategorisi:")
        
        for level in range(1, 11):
            current += 1
            print(f"  Level {level}/10 ({current}/{total})...", end=" ", flush=True)
            
            # Groq'dan soruları al
            ai_questions = generate_questions_for_category_level(category, level, 10)
            
            if ai_questions:
                # Soruları formatla
                for idx, q in enumerate(ai_questions):
                    try:
                        config = difficulty_config[level]
                        
                        question = {
                            "id": f"q_{category.lower()}_l{level}_{idx+1}",
                            "questionText": q.get("questionText", "Soru metni eksik"),
                            "optionA": q.get("optionA", "Seçenek A"),
                            "optionB": q.get("optionB", "Seçenek B"),
                            "optionC": q.get("optionC", "Seçenek C"),
                            "optionD": q.get("optionD", "Seçenek D"),
                            "correctAnswer": q.get("correctAnswer", "A"),
                            "imageName": "",
                            "level": level,
                            "category": category,
                            "difficulty": config["diff"],
                            "explanation": q.get("explanation", "Açıklama eksik"),
                            "tags": [category.lower(), f"level{level}"],
                            "points": config["points"],
                            "timeLimit": config["time"],
                            "isVerified": True,
                            "authorId": "ai_groq"
                        }
                        
                        all_questions.append(question)
                    except Exception as e:
                        print(f"\n    ⚠️  Soru {idx+1} atlandı: {e}")
                        continue
                
                print(f"✅ {len([q for q in ai_questions if 'questionText' in q])} soru")
            else:
                print("❌ Başarısız")
            
            # Rate limiting (Groq: 30 req/min)
            time.sleep(2)  # 2 saniye yeterli
    
    return all_questions

if __name__ == "__main__":
    print("=" * 60)
    print("🤖 GROQ AI - 700 Soru Oluşturucu")
    print("=" * 60)
    
    questions = generate_all_700_questions()
    
    # Dosyaya kaydet
    with open("groq_700_questions.json", "w", encoding="utf-8") as f:
        json.dump(questions, f, ensure_ascii=False, indent=2)
    
    print(f"\n\n{'=' * 60}")
    print(f"🎉 {len(questions)} gerçek soru oluşturuldu!")
    print(f"📁 Dosya: groq_700_questions.json")
    print("=" * 60)
    
    # İstatistikler
    print("\n📊 İstatistikler:")
    categories = {}
    for q in questions:
        cat = q["category"]
        categories[cat] = categories.get(cat, 0) + 1
    
    for cat, count in sorted(categories.items()):
        print(f"  ✅ {cat}: {count} soru")
    
    print("\n🚀 Sonraki adım: Firebase'e yükle!")
    print("   python upload_groq_questions.py")
