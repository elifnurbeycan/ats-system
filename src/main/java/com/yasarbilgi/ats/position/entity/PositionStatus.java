package com.yasarbilgi.ats.position.entity;

public enum PositionStatus {

    // Pozisyon hazırlanıyor.
    DRAFT,

    // Pozisyon için aktif olarak aday aranıyor.
    OPEN,

    // Pozisyonun işe alım süreci geçici olarak durduruluyor.
    ON_HOLD,

    // Pozisyon dolduruldu veya işe alım süreci tamamlandı.
    CLOSED,

    // Pozisyon için işe alım yapılmasından vazgeçildi.
    CANCELLED
}