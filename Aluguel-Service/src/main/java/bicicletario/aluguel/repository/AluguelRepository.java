package bicicletario.aluguel.repository;

import bicicletario.aluguel.model.Aluguel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AluguelRepository extends JpaRepository<Aluguel, Integer>
{
// Método para checar se o ciclista PODE alugar (necessário para /permiteAluguel)
Optional<Aluguel> findByCiclistaAndHoraFimIsNull(Integer idCiclista);

// Método CRÍTICO para achar o aluguel que está sendo DEVOLVIDO
Optional<Aluguel> findByBicicletaAndHoraFimIsNull(Integer idBicicleta);
}