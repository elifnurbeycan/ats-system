package com.yasarbilgi.ats.common.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@SuperBuilder
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @CreatedDate
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @LastModifiedDate
    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    @CreatedBy
    @Column(
            name = "created_by",
            updatable = false
    )
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    private Long version;

    @Builder.Default
    @Column(
            name = "active",
            nullable = false
    )
    private boolean active = true;

    @Column(name = "deactivated_at")
    private Instant deactivatedAt;

    // Pasif durumdaki kaydı yeniden aktif hâle getirir.
    public void activate() {
        if (active) {
            return;
        }

        active = true;
        deactivatedAt = null;
    }

    // Aktif kaydı fiziksel olarak silmeden pasif hâle getirir.
    public void deactivate() {
        if (!active) {
            return;
        }

        active = false;
        deactivatedAt = Instant.now();
    }

    // İki entity kaydının aynı sınıfa ve veritabanı kimliğine sahip olup olmadığını karşılaştırır.
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        Class<?> otherEffectiveClass =
                other instanceof HibernateProxy hibernateProxy
                        ? hibernateProxy
                        .getHibernateLazyInitializer()
                        .getPersistentClass()
                        : other.getClass();

        Class<?> thisEffectiveClass =
                this instanceof HibernateProxy hibernateProxy
                        ? hibernateProxy
                        .getHibernateLazyInitializer()
                        .getPersistentClass()
                        : getClass();

        if (!thisEffectiveClass.equals(otherEffectiveClass)) {
            return false;
        }

        BaseEntity baseEntity = (BaseEntity) other;

        return id != null && id.equals(baseEntity.getId());
    }

    // Entity ve Hibernate proxy sınıfları için tutarlı bir hash değeri üretir.
    @Override
    public int hashCode() {
        return this instanceof HibernateProxy hibernateProxy
                ? hibernateProxy
                .getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode()
                : getClass().hashCode();
    }
}