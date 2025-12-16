package bicicletario.aluguel.service;

import bicicletario.aluguel.dto.NovoFuncionarioDTO;
import bicicletario.aluguel.model.Funcionario;
import bicicletario.aluguel.repository.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FuncionarioService {

@Autowired
private FuncionarioRepository funcionarioRepository;

public Funcionario cadastrarFuncionario(NovoFuncionarioDTO dto) {
    // Validação de Negócio: Senhas iguais
    if (dto.getSenha() == null || !dto.getSenha().equals(dto.getConfirmacaoSenha())) {
        throw new IllegalArgumentException("A senha e a confirmação de senha devem ser iguais");
    }

    Funcionario novoFuncionario = converterDtoParaEntidade(dto);
    return funcionarioRepository.save(novoFuncionario);
}

public List<Funcionario> recuperarTodos() {
    return funcionarioRepository.findAll();
}

public Optional<Funcionario> recuperarPorId(Integer id) {
    return funcionarioRepository.findById(id);
}

public Funcionario editarFuncionario(Integer id, NovoFuncionarioDTO dto) {
    Optional<Funcionario> funcionarioOptional = funcionarioRepository.findById(id);

    if (!funcionarioOptional.isPresent()) {
        throw new IllegalArgumentException("Funcionário não encontrado");
    }

    Funcionario funcionarioExistente = funcionarioOptional.get();

    // Atualiza os dados
    funcionarioExistente.setNome(dto.getNome());
    funcionarioExistente.setEmail(dto.getEmail());
    funcionarioExistente.setIdade(dto.getIdade());
    funcionarioExistente.setFuncao(dto.getFuncao());
    funcionarioExistente.setCpf(dto.getCpf());
    // Nota: Em um app real, atualizaríamos a senha aqui também se necessário

    return funcionarioRepository.save(funcionarioExistente);
}

public void removerFuncionario(Integer id) {
    if (!funcionarioRepository.existsById(id)) {
        throw new IllegalArgumentException("Funcionário não encontrado");
    }
    funcionarioRepository.deleteById(id);
}

// --- Método Auxiliar (Movido para o Service) ---
private Funcionario converterDtoParaEntidade(NovoFuncionarioDTO dto) {
    Funcionario funcionario = new Funcionario();
    funcionario.setNome(dto.getNome());
    funcionario.setEmail(dto.getEmail());
    funcionario.setSenha(dto.getSenha());
    funcionario.setIdade(dto.getIdade());
    funcionario.setFuncao(dto.getFuncao());
    funcionario.setCpf(dto.getCpf());
    return funcionario;
}
}