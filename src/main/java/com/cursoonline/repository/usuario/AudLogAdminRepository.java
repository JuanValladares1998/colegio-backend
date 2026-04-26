package com.cursoonline.repository.usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cursoonline.entity.usuario.AudLogAdmin;

@Repository
public interface AudLogAdminRepository extends JpaRepository<AudLogAdmin, Integer> {}