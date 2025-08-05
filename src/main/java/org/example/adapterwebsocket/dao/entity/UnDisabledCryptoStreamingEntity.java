package org.example.adapterwebsocket.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "undisabled_crypto_streaming")
public class UnDisabledCryptoStreamingEntity {

    @Id
    private Long id;

    @Column(name = "from")
    private String from;

    @Column(name = "to")
    private String to;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UnDisabledCryptoStreamingEntity(String from, String to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UnDisabledCryptoStreamingEntity that = (UnDisabledCryptoStreamingEntity) o;
        return Objects.equals(id, that.id)
                && Objects.equals(from, that.from)
                && Objects.equals(to, that.to)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, from, to, createdAt, updatedAt);
    }
}
