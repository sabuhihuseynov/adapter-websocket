package org.example.adapterwebsocket.dao.repository.cache;

import org.example.adapterwebsocket.dao.entity.UnDisabledCryptoStreamingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnDisabledCryptoStreamingRepository extends JpaRepository<UnDisabledCryptoStreamingEntity, Long> {
}
