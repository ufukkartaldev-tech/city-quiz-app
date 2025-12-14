#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Soru Oluşturma ve Firestore'a Yükleme Script'i
Kullanım: python question_generator.py
"""

import json
import random
from datetime import datetime
from typing import List, Dict

# ============================================
# SORU KATEGORİLERİ VE ZORLUK SEVİYELERİ
# ============================================

CATEGORIES = {
    "GEOGRAPHY": "Coğrafya",
    "HISTORY": "Tarih",
    "CULTURE": "Kültür",
    "SPORTS": "Spor",
    "GENERAL": "Genel Kültür",
    "SCIENCE": "Bilim",
    "ART": "Sanat"
}

DIFFICULTIES = {
    "EASY": {"points": 5, "time": 30},
    "MEDIUM": {"points": 10, "time": 25},
    "HARD": {"points": 15, "time": 20},
    "EXPERT": {"points": 25, "time": 15}
}

# ============================================
# SORU ŞABLONLARİ
# ============================================

QUESTION_TEMPLATES = {
    "GEOGRAPHY": [
        {
            "question": "Türkiye'nin en kalabalık şehri hangisidir?",
            "options": ["İstanbul", "Ankara", "İzmir", "Bursa"],
            "correct": "A",
            "difficulty": "EASY",
            "tags": ["istanbul", "nüfus"],
            "explanation": "İstanbul, yaklaşık 15 milyon nüfusuyla Türkiye'nin en kalabalık şehridir."
        },
        {
            "question": "Karadeniz'e kıyısı olmayan il hangisidir?",
            "options": ["Trabzon", "Rize", "Ankara", "Samsun"],
            "correct": "C",
            "difficulty": "EASY",
            "tags": ["karadeniz", "coğrafya"],
            "explanation": "Ankara iç Anadolu'da yer alır ve denize kıyısı yoktur."
        },
        {
            "question": "Türkiye'nin en büyük gölü hangisidir?",
            "options": ["Van Gölü", "Tuz Gölü", "Beyşehir Gölü", "Eğirdir Gölü"],
            "correct": "A",
            "difficulty": "MEDIUM",
            "tags": ["göl", "van"],
            "explanation": "Van Gölü, 3.713 km² yüzölçümüyle Türkiye'nin en büyük gölüdür."
        },
        {
            "question": "Nemrut Dağı hangi ilde bulunur?",
            "options": ["Adıyaman", "Malatya", "Gaziantep", "Şanlıurfa"],
            "correct": "A",
            "difficulty": "MEDIUM",
            "tags": ["nemrut", "adıyaman"],
            "explanation": "Nemrut Dağı, Adıyaman ilinde yer alan ünlü tarihi bir yerdir."
        },
        {
            "question": "Türkiye'nin en uzun nehri hangisidir?",
            "options": ["Kızılırmak", "Sakarya", "Fırat", "Dicle"],
            "correct": "A",
            "difficulty": "HARD",
            "tags": ["nehir", "kızılırmak"],
            "explanation": "Kızılırmak, 1.355 km uzunluğuyla Türkiye'nin en uzun nehridir."
        }
    ],
    "HISTORY": [
        {
            "question": "Türkiye Cumhuriyeti hangi yıl kurulmuştur?",
            "options": ["1923", "1920", "1919", "1922"],
            "correct": "A",
            "difficulty": "EASY",
            "tags": ["cumhuriyet", "tarih"],
            "explanation": "Türkiye Cumhuriyeti 29 Ekim 1923'te ilan edilmiştir."
        },
        {
            "question": "İstanbul'un fethi hangi yıl gerçekleşmiştir?",
            "options": ["1453", "1451", "1456", "1461"],
            "correct": "A",
            "difficulty": "MEDIUM",
            "tags": ["istanbul", "fetih"],
            "explanation": "İstanbul, Fatih Sultan Mehmet tarafından 29 Mayıs 1453'te fethedilmiştir."
        },
        {
            "question": "Osmanlı İmparatorluğu'nun ilk padişahı kimdir?",
            "options": ["Osman Gazi", "Orhan Gazi", "I. Murad", "Yıldırım Bayezid"],
            "correct": "A",
            "difficulty": "EASY",
            "tags": ["osmanlı", "padişah"],
            "explanation": "Osman Gazi, Osmanlı İmparatorluğu'nun kurucusu ve ilk padişahıdır."
        }
    ],
    "CULTURE": [
        {
            "question": "Türk kahvesi UNESCO Somut Olmayan Kültürel Miras Listesi'ne hangi yıl alındı?",
            "options": ["2013", "2010", "2015", "2018"],
            "correct": "A",
            "difficulty": "HARD",
            "tags": ["kahve", "unesco"],
            "explanation": "Türk kahvesi kültürü ve geleneği 2013 yılında UNESCO listesine alınmıştır."
        },
        {
            "question": "Nasreddin Hoca hangi şehirle özdeşleşmiştir?",
            "options": ["Akşehir", "Konya", "Ankara", "Eskişehir"],
            "correct": "A",
            "difficulty": "MEDIUM",
            "tags": ["nasreddin hoca", "akşehir"],
            "explanation": "Nasreddin Hoca, Akşehir'de yaşamış ve burada gömülmüştür."
        }
    ],
    "SPORTS": [
        {
            "question": "Türkiye'nin ilk olimpiyat madalyası hangi spor dalındadır?",
            "options": ["Güreş", "Halter", "Atletizm", "Boks"],
            "correct": "A",
            "difficulty": "MEDIUM",
            "tags": ["olimpiyat", "güreş"],
            "explanation": "Türkiye'nin ilk olimpiyat madalyası 1936'da güreşte kazanılmıştır."
        },
        {
            "question": "Galatasaray hangi yıl UEFA Kupası'nı kazandı?",
            "options": ["2000", "1998", "2001", "1999"],
            "correct": "A",
            "difficulty": "MEDIUM",
            "tags": ["galatasaray", "uefa"],
            "explanation": "Galatasaray, 2000 yılında UEFA Kupası'nı kazanan ilk Türk takımı olmuştur."
        }
    ]
}

# ============================================
# SORU OLUŞTURMA FONKSİYONLARI
# ============================================

def create_question(
    question_id: int,
    question_text: str,
    options: List[str],
    correct_answer: str,
    category: str,
    difficulty: str,
    level: int,
    tags: List[str],
    explanation: str = "",
    image_name: str = "default_question.png"
) -> Dict:
    """Tek bir soru objesi oluşturur"""
    
    diff_config = DIFFICULTIES[difficulty]
    
    return {
        "id": question_id,
        "questionText": question_text,
        "optionA": options[0],
        "optionB": options[1],
        "optionC": options[2],
        "optionD": options[3],
        "correctAnswer": correct_answer,
        "imageName": image_name,
        "level": level,
        "category": category,
        "difficulty": difficulty,
        "explanation": explanation,
        "tags": tags,
        "points": diff_config["points"],
        "timeLimit": diff_config["time"],
        "isVerified": True,
        "authorId": "system",
        "createdAt": int(datetime.now().timestamp() * 1000)
    }

def generate_questions_from_templates() -> List[Dict]:
    """Şablonlardan sorular oluşturur"""
    questions = []
    question_id = 1
    
    for category, templates in QUESTION_TEMPLATES.items():
        for template in templates:
            # Her şablondan birkaç seviye için soru oluştur
            for level in range(1, 4):  # 1, 2, 3. seviyeler için
                question = create_question(
                    question_id=question_id,
                    question_text=template["question"],
                    options=template["options"],
                    correct_answer=template["correct"],
                    category=category,
                    difficulty=template["difficulty"],
                    level=level,
                    tags=template["tags"],
                    explanation=template.get("explanation", ""),
                    image_name=f"{category.lower()}_{question_id}.png"
                )
                questions.append(question)
                question_id += 1
    
    return questions

def generate_bulk_questions(count: int = 500) -> List[Dict]:
    """Toplu soru üretir (AI ile genişletilebilir)"""
    base_questions = generate_questions_from_templates()
    
    # Şimdilik mevcut soruları çoğalt ve varyasyonlar oluştur
    questions = []
    question_id = 1
    
    while len(questions) < count:
        for base_q in base_questions:
            if len(questions) >= count:
                break
            
            # Yeni soru oluştur (varyasyon)
            new_question = base_q.copy()
            new_question["id"] = question_id
            new_question["level"] = (question_id % 10) + 1  # 1-10 arası level
            
            questions.append(new_question)
            question_id += 1
    
    return questions[:count]

# ============================================
# DOSYA İŞLEMLERİ
# ============================================

def save_to_json(questions: List[Dict], filename: str = "questions_extended.json"):
    """Soruları JSON dosyasına kaydeder"""
    with open(filename, 'w', encoding='utf-8') as f:
        json.dump(questions, f, ensure_ascii=False, indent=2)
    print(f"✅ {len(questions)} soru '{filename}' dosyasına kaydedildi!")

def save_to_firestore_format(questions: List[Dict], filename: str = "firestore_import.json"):
    """Firestore import formatında kaydeder"""
    firestore_data = {
        "questions": {}
    }
    
    for q in questions:
        doc_id = f"question_{q['id']}"
        firestore_data["questions"][doc_id] = q
    
    with open(filename, 'w', encoding='utf-8') as f:
        json.dump(firestore_data, f, ensure_ascii=False, indent=2)
    print(f"✅ Firestore import dosyası '{filename}' oluşturuldu!")

# ============================================
# İSTATİSTİKLER
# ============================================

def print_statistics(questions: List[Dict]):
    """Soru istatistiklerini gösterir"""
    print("\n" + "="*50)
    print("📊 SORU İSTATİSTİKLERİ")
    print("="*50)
    
    # Toplam
    print(f"\n📝 Toplam Soru: {len(questions)}")
    
    # Kategoriye göre
    print("\n📚 Kategoriye Göre:")
    for category in CATEGORIES.keys():
        count = len([q for q in questions if q["category"] == category])
        print(f"  - {CATEGORIES[category]}: {count}")
    
    # Zorluk seviyesine göre
    print("\n⭐ Zorluk Seviyesine Göre:")
    for difficulty in DIFFICULTIES.keys():
        count = len([q for q in questions if q["difficulty"] == difficulty])
        print(f"  - {difficulty}: {count}")
    
    # Level'a göre
    print("\n🎯 Level'a Göre:")
    for level in range(1, 11):
        count = len([q for q in questions if q["level"] == level])
        print(f"  - Level {level}: {count}")
    
    print("\n" + "="*50)

# ============================================
# ANA FONKSİYON
# ============================================

def main():
    print("🎮 Soru Oluşturma Script'i")
    print("="*50)
    
    # Kullanıcıdan soru sayısı al
    try:
        count = int(input("\n📝 Kaç soru oluşturmak istersiniz? (varsayılan: 500): ") or "500")
    except ValueError:
        count = 500
    
    print(f"\n⏳ {count} soru oluşturuluyor...")
    
    # Soruları oluştur
    questions = generate_bulk_questions(count)
    
    # İstatistikleri göster
    print_statistics(questions)
    
    # Dosyalara kaydet
    print("\n💾 Dosyalara kaydediliyor...")
    save_to_json(questions, "questions_extended.json")
    save_to_firestore_format(questions, "firestore_import.json")
    
    print("\n✅ İşlem tamamlandı!")
    print("\n📁 Oluşturulan dosyalar:")
    print("  - questions_extended.json (Android için)")
    print("  - firestore_import.json (Firestore için)")
    
    print("\n🚀 Sonraki adımlar:")
    print("  1. questions_extended.json dosyasını app/src/main/assets/ klasörüne kopyalayın")
    print("  2. Firestore'a yüklemek için: firebase_upload.py script'ini çalıştırın")

if __name__ == "__main__":
    main()
