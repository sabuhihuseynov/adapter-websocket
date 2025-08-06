package org.example.adapterwebsocket.dao.repository;

import org.example.adapterwebsocket.dao.entity.UnDisabledCryptoPairEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnDisabledCryptoPairRepository extends JpaRepository<UnDisabledCryptoPairEntity, Long> {
}
