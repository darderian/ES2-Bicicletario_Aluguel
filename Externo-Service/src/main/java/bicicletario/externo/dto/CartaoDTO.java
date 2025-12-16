package bicicletario.externo.dto;

public class CartaoDTO {
    private String nomeTitular;
    private String numero;
    private String validade;
    private String cvv;

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    // Outros getters/setters se precisar validar mais coisas
}