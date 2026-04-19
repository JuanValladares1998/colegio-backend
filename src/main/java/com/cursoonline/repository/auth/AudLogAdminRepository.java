package com.cursoonline.repository.auth;
import com.cursoonline.entity.auth.AudLogAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AudLogAdminRepository extends JpaRepository<AudLogAdmin, Integer> {}