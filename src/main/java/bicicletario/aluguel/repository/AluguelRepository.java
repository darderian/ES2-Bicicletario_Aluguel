package bicicletario.aluguel.repository;

import bicicletario.aluguel.model.Aluguel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface AluguelRepository extends JpaRepository<Aluguel, Integer>
{
// Busca por um aluguel de um ciclista específico que ainda não foi finalizado
Optional<Aluguel> findByCiclistaAndHoraFimIsNull(Integer idCiclista);
//  Adicionar métodos de busca (ex: findByCiclistaAndHoraFimIsNull)
}