package com.yasarbilgi.ats.pipeline.entity;

public enum PipelineStageType {

    // Adayın işe alım sürecine aktif olarak devam ettiği aşamayı belirtir.
    ACTIVE,

    // Aday sürecinin geçici olarak bekletildiği aşamayı belirtir.
    ON_HOLD,

    // Aday sürecinin başarıyla tamamlandığı aşamayı belirtir.
    HIRED,

    // Aday sürecinin olumsuz tamamlandığı aşamayı belirtir.
    REJECTED
}