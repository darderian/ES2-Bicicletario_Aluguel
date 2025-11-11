package bicicletario.aluguel.repository;

import bicicletario.aluguel.model.CartaoDeCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface CartaoDeCreditoRepository extends JpaRepository<CartaoDeCredito, Integer> {
// Busca o cartão associado a um ID de ciclista específico
Optional<CartaoDeCredito> findByIdCiclista(Integer idCiclista);
}