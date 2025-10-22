package bicicletario.aluguel.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Ciclista
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nome;
    private String nascimento; // "AAAA-MM-DD"
    private String cpf;
    @Embedded // Marca o objeto embutido
    private Passaporte passaporte;
    private String nacionalidade;
    @Column(unique = true) // Boa prática, garante que o email seja único
    private String email;
    private String urlFotoDocumento;
    private String senha;
    private String status; // 'ATIVO', 'INATIVO', 'AGUARDANDO_CONFIRMACAO'

    public Ciclista() {
        // Construtor vazio (obrigatório pelo JPA)
    }

    // Getters e Setters
    public void setId(Integer id) {this.id = id;}
    public Integer getId() {return id;}
    public void setNome(String nome) {this.nome = nome;}
    public String getNome() {return nome;}
    public void setNascimento(String nascimento) {this.nascimento = nascimento;}
    public String getNascimento() {return nascimento;}
    public void setCpf(String cpf) {this.cpf = cpf;}
    public String getCpf() {return cpf;}
    public void setPassaporte(Passaporte passaporte) {this.passaporte = passaporte;}
    public Passaporte getPassaporte() {return passaporte;}
    public void setNacionalidade(String nacionalidade) {this.nacionalidade = nacionalidade;}
    public String getNacionalidade() {return nacionalidade;}
    public void setEmail(String email) {this.email = email;}
    public String getEmail() {return email;}
    public void setUrlFotoDocumento(String urlFotoDocumento) {this.urlFotoDocumento = urlFotoDocumento;}
    public String getUrlFotoDocumento() {return urlFotoDocumento;}
    public void setSenha(String senha) {this.senha = senha;}
    public String getSenha() {return senha;}
    public void setStatus(String status) {this.status = status;}
    public String getStatus() {return status;}
}