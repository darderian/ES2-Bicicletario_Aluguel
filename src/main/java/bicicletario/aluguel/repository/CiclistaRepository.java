package bicicletario.aluguel.repository;

import bicicletario.aluguel.model.Ciclista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CiclistaRepository extends JpaRepository<Ciclista, Integer> {
// JpaRepository já nos dá: save(), findById(), findAll(), delete(), ...
}