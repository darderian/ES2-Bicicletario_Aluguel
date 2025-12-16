package bicicletario.externo.repository;

import bicicletario.externo.model.Cobranca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CobrancaRepository extends JpaRepository<Cobranca, Integer> {
}