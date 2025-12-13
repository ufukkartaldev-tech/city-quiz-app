package com.example.oyun.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bu, Room veritabanındaki "profiles" tablosunun planıdır.
 * @Entity: Room'a "Bu sınıf, bir veritabanı tablosudur" der.
 * tableName = "profiles": Tablonun adını "profiles" olarak belirler.
 *
 * @PrimaryKey: "Bu sütun, her bir satırı benzersiz kılan anahtar olacaktır."
 * (autoGenerate = true) sayesinde, biz her yeni profil eklediğimizde Room ona
 * otomatik olarak artan bir numara (1, 2, 3...) verir.
 */
@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Her profilin benzersiz kimlik numarası

    val name: String // Profilin adı (Ahmet, Ayşe vb.)
)