package bicicletario.aluguel.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class CartaoDeCredito {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer id;

// Chave estrangeira para o ciclista
private Integer idCiclista;

private String nomeTitular;
private String numero; // Salve apenas os 4 últimos dígitos em produção!
private String validade;
private String cvv; // Não salve o CVV em produção!

// Construtor vazio
public CartaoDeCredito() {}

// Getters e Setters
public Integer getId() {return id;}
public void setId(Integer id) {this.id = id;}
public Integer getIdCiclista() {return idCiclista;}
public void setIdCiclista(Integer idCiclista) {this.idCiclista = idCiclista;}
public String getNomeTitular() {return nomeTitular;}
public void setNomeTitular(String nomeTitular) {this.nomeTitular = nomeTitular;}
public String getNumero() {return numero;}
public void setNumero(String numero) {this.numero = numero;}
public String getValidade() {return validade;}
public void setValidade(String validade) {this.validade = validade;}
public String getCvv() {return cvv;}
public void setCvv(String cvv) {this.cvv = cvv;}
}