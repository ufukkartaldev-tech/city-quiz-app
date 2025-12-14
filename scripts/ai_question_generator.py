#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
AI ile Soru Oluşturma Script'i (OpenAI/Gemini)
Kullanım: python ai_question_generator.py
"""

import json
import os
from typing import List, Dict
from datetime import datetime

# ============================================
# AI PROMPT ŞABLONLARI
# ============================================

QUESTION_GENERATION_PROMPT = """
Sen bir Türkiye coğrafyası ve genel kültür uzmanısın. 
Aşağıdaki kriterlere göre {count} adet çoktan seçmeli soru oluştur:

Kategori: {category}
Zorluk: {difficulty}
Level: {level}

Her soru için:
1. Soru metni (açık ve net)
2. 4 seçenek (A, B, C, D)
3. Doğru cevap (A, B, C veya D)
4. Açıklama (neden bu cevap doğru)
5. Etiketler (2-3 anahtar kelime)

Format (JSON):
{{
  "questions": [
    {{
      "questionText": "...",
      "optionA": "...",
      "optionB": "...",
      "optionC": "...",
      "optionD": "...",
      "correctAnswer": "A",
      "explanation": "...",
      "tags": ["tag1", "tag2"]
    }}
  ]
}}

Kurallar:
- Sorular Türkiye ile ilgili olmalı
- Seçenekler birbirine yakın zorlukta olmalı
- Doğru cevap açık olmalı ama kolay tahmin edilmemeli
- Açıklama eğitici olmalı
"""

# ============================================
# OPENAI ENTEGRASYONU
# ============================================

def generate_with_openai(category: str, difficulty: str, level: int, count: int = 10) -> List[Dict]:
    """OpenAI ile soru oluşturur"""
    try:
        import openai
    except ImportError:
        print("❌ OpenAI kütüphanesi yüklü değil!")
        print("📦 Yüklemek için: pip install openai")
        return []
    
    # API key kontrolü
    api_key = os.getenv("OPENAI_API_KEY")
    if not api_key:
        print("❌ OPENAI_API_KEY environment variable bulunamadı!")
        print("💡 Export etmek için: export OPENAI_API_KEY='your-key-here'")
        return []
    
    openai.api_key = api_key
    
    prompt = QUESTION_GENERATION_PROMPT.format(
        count=count,
        category=category,
        difficulty=difficulty,
        level=level
    )
    
    print(f"⏳ OpenAI ile {count} soru oluşturuluyor...")
    
    try:
        response = openai.ChatCompletion.create(
            model="gpt-4",
            messages=[
                {"role": "system", "content": "Sen bir quiz soru oluşturma uzmanısın."},
                {"role": "user", "content": prompt}
            ],
            temperature=0.7,
            max_tokens=2000
        )
        
        content = response.choices[0].message.content
        data = json.loads(content)
        
        print(f"✅ {len(data['questions'])} soru oluşturuldu!")
        return data['questions']
        
    except Exception as e:
        print(f"❌ OpenAI hatası: {e}")
        return []

# ============================================
# GEMINI ENTEGRASYONU
# ============================================

def generate_with_gemini(category: str, difficulty: str, level: int, count: int = 10) -> List[Dict]:
    """Google Gemini ile soru oluşturur"""
    try:
        import google.generativeai as genai
    except ImportError:
        print("❌ Google Generative AI kütüphanesi yüklü değil!")
        print("📦 Yüklemek için: pip install google-generativeai")
        return []
    
    # API key kontrolü
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        print("❌ GEMINI_API_KEY environment variable bulunamadı!")
        print("💡 Export etmek için: export GEMINI_API_KEY='your-key-here'")
        return []
    
    genai.configure(api_key=api_key)
    model = genai.GenerativeModel('gemini-pro')
    
    prompt = QUESTION_GENERATION_PROMPT.format(
        count=count,
        category=category,
        difficulty=difficulty,
        level=level
    )
    
    print(f"⏳ Gemini ile {count} soru oluşturuluyor...")
    
    try:
        response = model.generate_content(prompt)
        content = response.text
        
        # JSON parse et
        # Gemini bazen markdown formatında döndürür, temizle
        if "```json" in content:
            content = content.split("```json")[1].split("```")[0]
        elif "```" in content:
            content = content.split("```")[1].split("```")[0]
        
        data = json.loads(content.strip())
        
        print(f"✅ {len(data['questions'])} soru oluşturuldu!")
        return data['questions']
        
    except Exception as e:
        print(f"❌ Gemini hatası: {e}")
        return []

# ============================================
# SORU İŞLEME
# ============================================

def process_ai_questions(
    ai_questions: List[Dict],
    category: str,
    difficulty: str,
    level: int,
    start_id: int = 1
) -> List[Dict]:
    """AI'dan gelen soruları tam formata dönüştürür"""
    
    difficulty_config = {
        "EASY": {"points": 5, "time": 30},
        "MEDIUM": {"points": 10, "time": 25},
        "HARD": {"points": 15, "time": 20},
        "EXPERT": {"points": 25, "time": 15}
    }
    
    processed = []
    
    for idx, q in enumerate(ai_questions):
        processed_q = {
            "id": start_id + idx,
            "questionText": q["questionText"],
            "optionA": q["optionA"],
            "optionB": q["optionB"],
            "optionC": q["optionC"],
            "optionD": q["optionD"],
            "correctAnswer": q["correctAnswer"],
            "imageName": f"{category.lower()}_{start_id + idx}.png",
            "level": level,
            "category": category,
            "difficulty": difficulty,
            "explanation": q.get("explanation", ""),
            "tags": q.get("tags", []),
            "points": difficulty_config[difficulty]["points"],
            "timeLimit": difficulty_config[difficulty]["time"],
            "isVerified": True,
            "authorId": "ai_generated",
            "createdAt": int(datetime.now().timestamp() * 1000)
        }
        processed.append(processed_q)
    
    return processed

# ============================================
# TOPLU SORU OLUŞTURMA
# ============================================

def generate_bulk_questions_with_ai(
    total_count: int = 500,
    ai_provider: str = "gemini"
) -> List[Dict]:
    """AI ile toplu soru oluşturur"""
    
    categories = ["GEOGRAPHY", "HISTORY", "CULTURE", "SPORTS", "GENERAL", "SCIENCE", "ART"]
    difficulties = ["EASY", "MEDIUM", "HARD", "EXPERT"]
    
    all_questions = []
    question_id = 1
    
    # Her kategori için dengeli dağıt
    questions_per_category = total_count // len(categories)
    
    for category in categories:
        for difficulty in difficulties:
            for level in range(1, 11):  # 10 level
                # Her kombinasyon için birkaç soru
                count = max(1, questions_per_category // (len(difficulties) * 10))
                
                if len(all_questions) >= total_count:
                    break
                
                print(f"\n📝 {category} - {difficulty} - Level {level}")
                
                # AI ile oluştur
                if ai_provider == "openai":
                    ai_questions = generate_with_openai(category, difficulty, level, count)
                else:
                    ai_questions = generate_with_gemini(category, difficulty, level, count)
                
                if ai_questions:
                    processed = process_ai_questions(
                        ai_questions, category, difficulty, level, question_id
                    )
                    all_questions.extend(processed)
                    question_id += len(processed)
                
                if len(all_questions) >= total_count:
                    break
            
            if len(all_questions) >= total_count:
                break
        
        if len(all_questions) >= total_count:
            break
    
    return all_questions[:total_count]

# ============================================
# ANA FONKSİYON
# ============================================

def main():
    print("🤖 AI ile Soru Oluşturma Script'i")
    print("="*50)
    
    # AI provider seç
    print("\n🔧 AI Provider:")
    print("1. Google Gemini (Ücretsiz)")
    print("2. OpenAI GPT-4 (Ücretli)")
    
    choice = input("\nSeçiminiz (1-2): ").strip()
    ai_provider = "gemini" if choice == "1" else "openai"
    
    # Soru sayısı
    try:
        count = int(input("\n📝 Kaç soru oluşturmak istersiniz? (varsayılan: 100): ") or "100")
    except ValueError:
        count = 100
    
    print(f"\n⏳ {count} soru {ai_provider.upper()} ile oluşturuluyor...")
    print("⚠️  Bu işlem birkaç dakika sürebilir...")
    
    # Soruları oluştur
    questions = generate_bulk_questions_with_ai(count, ai_provider)
    
    if questions:
        # Dosyaya kaydet
        filename = f"ai_questions_{ai_provider}_{count}.json"
        with open(filename, 'w', encoding='utf-8') as f:
            json.dump(questions, f, ensure_ascii=False, indent=2)
        
        print(f"\n✅ {len(questions)} soru '{filename}' dosyasına kaydedildi!")
        
        # İstatistikler
        print("\n📊 İstatistikler:")
        categories = {}
        for q in questions:
            cat = q["category"]
            categories[cat] = categories.get(cat, 0) + 1
        
        for cat, cnt in sorted(categories.items()):
            print(f"  - {cat}: {cnt}")
    else:
        print("\n❌ Soru oluşturulamadı!")

if __name__ == "__main__":
    main()
