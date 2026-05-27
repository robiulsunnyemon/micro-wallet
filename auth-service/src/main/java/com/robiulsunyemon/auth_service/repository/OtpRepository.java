package com.robiulsunyemon.auth_service.repository;
import com.robiulsunyemon.auth_service.entity.OtpToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRepository extends CrudRepository<OtpToken, String> {
}