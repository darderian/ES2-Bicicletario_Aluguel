package bicicletario.aluguel.repository;

import bicicletario.aluguel.model.Devolucao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DevolucaoRepository extends JpaRepository<Devolucao, Integer>
{

}