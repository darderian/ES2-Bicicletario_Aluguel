package bicicletario.aluguel.repository;

import bicicletario.aluguel.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Integer>
{
// Métodos de busca customizados (ex: findByEmail) podem ser adicionados aqui
}