package bicicletario.aluguel.repository;

import bicicletario.aluguel.model.Aluguel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AluguelRepository extends JpaRepository<Aluguel, Integer>
{

//  Adicionar métodos de busca (ex: findByCiclistaAndHoraFimIsNull)
}