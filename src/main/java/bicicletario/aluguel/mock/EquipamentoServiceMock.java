package bicicletario.aluguel.mock;

import bicicletario.aluguel.dto.BicicletaDTO;
import org.springframework.stereotype.Service;

@Service // Diz ao Spring para gerenciar esta classe
public class EquipamentoServiceMock implements EquipamentoService {

@Override
public BicicletaDTO getBicicletaDaTranca(Integer idTranca) {
    // MOCK LÓGICA (Gap do UC03)
    // Estamos simulando que qualquer tranca tem uma bicicleta "DISPONÍVEL"
    // com ID = 123
    BicicletaDTO bicicletaMock = new BicicletaDTO();
    bicicletaMock.setId(123);
    bicicletaMock.setModelo("Mock: Bicicleta da Tranca " + idTranca);
    bicicletaMock.setStatus("DISPONIVEL");
    return bicicletaMock;
}

@Override
public void destrancarTranca(Integer idTranca) {
    // MOCK LÓGICA (Gap do UC03)
    // Apenas simula a ação. Imprimimos no log para depuração.
    System.out.println("[MOCK EQUIPAMENTO] Destrancando tranca: " + idTranca);
}

@Override
public void trancarTranca(Integer idTranca, Integer idBicicleta) {
    // MOCK LÓGICA (Gap do UC04)
    System.out.println("[MOCK EQUIPAMENTO] Trancando bicicleta " + idBicicleta + " na tranca " + idTranca);
}

@Override
public void alterarStatusBicicleta(Integer idBicicleta, String status) {
    // MOCK LÓGICA (Gap do UC04)
    System.out.println("[MOCK EQUIPAMENTO] Alterando status da bicicleta " + idBicicleta + " para: " + status);
}

@Override
public BicicletaDTO getBicicleta(Integer idBicicleta) {
    // MOCK LÓGICA (Refatoração do GET /bicicletaAlugada)
    BicicletaDTO bicicletaMock = new BicicletaDTO();
    bicicletaMock.setId(idBicicleta);
    bicicletaMock.setMarca("Marca Mockada (DTO)");
    bicicletaMock.setModelo("Modelo Falso (DTO)");
    bicicletaMock.setAno("2024");
    bicicletaMock.setStatus("EM_USO");
    return bicicletaMock;
}
}