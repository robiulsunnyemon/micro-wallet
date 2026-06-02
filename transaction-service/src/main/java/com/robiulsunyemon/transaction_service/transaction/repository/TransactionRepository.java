package com.robiulsunyemon.transaction_service.transaction.repository;
import com.robiulsunyemon.transaction_service.transaction.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    Optional<TransactionEntity>  findByTxId(String txId);
}
