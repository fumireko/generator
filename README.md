# generator

This project takes JSON input specifying attributes and types and generates Java classes with Spring Boot annotations, including getters and setters.

#### Input JSON Format

The input JSON should follow this format:

```json
[
    {
        "name": "ClassName",
        "attributes": [
            {
                "name": "attributeName",
                "type": "java.lang.Type"
            },
            {
                "name": "attributeName",
                "type": "java.util.List<ElementType>"
            }
        ]
    },
    ...
]
```

#### Generated Classes

For each class specified in the input JSON, the generator creates a Java class with appropriate annotations and methods, including getters and setters.

#### Example

```json
[
    {
        "name": "Categoria",
        "attributes": [
            {
                "name": "nome",
                "type": "java.lang.String"
            },
            {
                "name": "produtos",
                "type": "java.util.List<Produto>"
            }
        ]
    },
    {
        "name": "Produto",
        "attributes": [
            {
                "name": "nome",
                "type": "java.lang.String"
            },
            {
                "name": "preco",
                "type": "java.lang.Double"
            },
            {
                "name": "categoria",
                "type": "Categoria"
            }
        ]
    },
    {
        "name": "Pedido",
        "attributes": [
            {
                "name": "status",
                "type": "java.lang.String"
            },
            {
                "name": "itens",
                "type": "java.util.List<ItemPedido>"
            }
        ]
    },
    {
        "name": "ItemPedido",
        "attributes": [
            {
                "name": "quantidade",
                "type": "java.lang.Integer"
            },
            {
                "name": "pedido",
                "type": "Pedido"
            },
            {
                "name": "produto",
                "type": "Produto"
            }
        ]
    }
]

# This will output these Java classes:

package ca.fubi.generator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.lang.Long;
import java.lang.String;
import java.util.List;

@Entity
@Table(
    name = "tb_categoria"
)
public class Categoria {
  @Id
  @GeneratedValue(
      strategy = GenerationType.IDENTITY
  )
  @Column(
      name = "id_categoria"
  )
  private Long id;

  @OneToMany(
      mappedBy = "categoria"
  )
  @JsonIgnore
  @Column(
      name = "categoria_produtos"
  )
  private List<Produto> produtos;

  @Column(
      name = "categoria_nome"
  )
  private String nome;

  public Categoria() {
  }

  public Categoria(List<Produto> produtos, String nome) {
    this.produtos = produtos;
    this.nome = nome;
  }

  public List<Produto> getProdutos() {
    return this.produtos;
  }

  public void setProdutos(List<Produto> produtos) {
    this.produtos = produtos;
  }

  public String getNome() {
    return this.nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }
}
package ca.fubi.generator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.lang.Double;
import java.lang.Long;
import java.lang.String;

@Entity
@Table(
    name = "tb_produto"
)
public class Produto {
  @Id
  @GeneratedValue(
      strategy = GenerationType.IDENTITY
  )
  @Column(
      name = "id_produto"
  )
  private Long id;

  @Column(
      name = "produto_preco"
  )
  private Double preco;

  @ManyToOne
  @JoinColumn(
      name = "fk_categoria"
  )
  private Categoria categoria;

  @Column(
      name = "produto_nome"
  )
  private String nome;

  public Produto() {
  }

  public Produto(Double preco, Categoria categoria, String nome) {
    this.preco = preco;
    this.categoria = categoria;
    this.nome = nome;
  }

  public Double getPreco() {
    return this.preco;
  }

  public void setPreco(Double preco) {
    this.preco = preco;
  }

  public Categoria getCategoria() {
    return this.categoria;
  }

  public void setCategoria(Categoria categoria) {
    this.categoria = categoria;
  }

  public String getNome() {
    return this.nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }
}
package ca.fubi.generator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.lang.Long;
import java.lang.String;
import java.util.List;

@Entity
@Table(
    name = "tb_pedido"
)
public class Pedido {
  @Id
  @GeneratedValue(
      strategy = GenerationType.IDENTITY
  )
  @Column(
      name = "id_pedido"
  )
  private Long id;

  @OneToMany(
      mappedBy = "pedido"
  )
  @JsonIgnore
  @Column(
      name = "pedido_itens"
  )
  private List<ItemPedido> itens;

  @Column(
      name = "pedido_status"
  )
  private String status;

  public Pedido() {
  }

  public Pedido(List<ItemPedido> itens, String status) {
    this.itens = itens;
    this.status = status;
  }

  public List<ItemPedido> getItens() {
    return this.itens;
  }

  public void setItens(List<ItemPedido> itens) {
    this.itens = itens;
  }

  public String getStatus() {
    return this.status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
package ca.fubi.generator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.lang.Integer;
import java.lang.Long;

@Entity
@Table(
    name = "tb_itempedido"
)
public class ItemPedido {
  @Id
  @GeneratedValue(
      strategy = GenerationType.IDENTITY
  )
  @Column(
      name = "id_itempedido"
  )
  private Long id;

  @ManyToOne
  @JoinColumn(
      name = "fk_produto"
  )
  private Produto produto;

  @ManyToOne
  @JoinColumn(
      name = "fk_pedido"
  )
  private Pedido pedido;

  @Column(
      name = "itempedido_quantidade"
  )
  private Integer quantidade;

  public ItemPedido() {
  }

  public ItemPedido(Produto produto, Pedido pedido, Integer quantidade) {
    this.produto = produto;
    this.pedido = pedido;
    this.quantidade = quantidade;
  }

  public Produto getProduto() {
    return this.produto;
  }

  public void setProduto(Produto produto) {
    this.produto = produto;
  }

  public Pedido getPedido() {
    return this.pedido;
  }

  public void setPedido(Pedido pedido) {
    this.pedido = pedido;
  }

  public Integer getQuantidade() {
    return this.quantidade;
  }

  public void setQuantidade(Integer quantidade) {
    this.quantidade = quantidade;
  }
}
```

#### Usage

1. Send a POST request with the JSON data to ``http://localhost:8080/entities``
2. Generated Java classes with Spring Boot annotations, getters, and setters will be produced.
3. Copy the classes and save them to the appropriate source files.
