package ca.fubi.generator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

class Entity {
    private String name;
    private Map<String, TypeName> attributes;
    private List<MethodSpec> methods;

    public Entity(String name) {
        this.name = name;
        this.attributes = new HashMap<>();
        this.methods = new ArrayList<>();
    }

    public String getName() {
        return name;
    }
    
    public <T> Entity addAttribute(String attributeName, Class<T> attributeType) {
        attributes.put(attributeName, ClassName.get(attributeType));
        return this;
    }
    
    public Entity addAttribute(String attributeName, TypeName attributeType) {
        attributes.put(attributeName, attributeType);
        return this;
    }

    public void addMethod(MethodSpec methodSpec) {
        methods.add(methodSpec);
    }

    public Map<String, TypeName> getAttributes() {
        return attributes;
    }

    public List<MethodSpec> getMethods() {
        return methods;
    }
}

public class Main {

  public static void main(String[] args) {
	  Entity categoria = new Entity("Categoria");
	  categoria.addAttribute("nome", String.class)
	  		   .addAttribute("produtos", ParameterizedTypeName.get(ClassName.get("java.util","List"), ClassName.get("", "Produto")));
	  			
	  generateEntity(categoria);
	  
	  Entity produto = new Entity("Produto");
	  produto.addAttribute("nome", String.class)
	  		 .addAttribute("preco", Double.class)
	  		 .addAttribute("categoria", ParameterizedTypeName.get(ClassName.get("java.util","List"), ClassName.get("", "Categoria")));
	  
	  generateEntity(produto);
	  
	  Entity pedido = new Entity("Pedido");
	  pedido.addAttribute("status", String.class)
	  		.addAttribute("itens", ParameterizedTypeName.get(ClassName.get("java.util","List"), ClassName.get("", "ItemPedido")));
	  
	  generateEntity(pedido);
	  
	  Entity itemPedido = new Entity("ItemPedido");
	  itemPedido.addAttribute("quantidade", Integer.class)
	  			.addAttribute("pedido", ClassName.get("", "Pedido"))
	  			.addAttribute("pedido", ClassName.get("", "Produto"));
	  
	  generateEntity(itemPedido);
  }

  private static void generateEntity(Entity entity) {
    TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(entity.getName())
      .addModifiers(Modifier.PUBLIC)
      .addAnnotation(jakarta.persistence.Entity.class)
      .addAnnotation(AnnotationSpec.builder(Table.class)
    		  .addMember("name", "\"tb_" + entity.getName().toLowerCase() + "\"")
    		  .build());
    
    FieldSpec id = FieldSpec.builder(Long.class, "id", Modifier.PRIVATE)
    	    .addAnnotation(Id.class)
    	    .addAnnotation(AnnotationSpec.builder(GeneratedValue.class)
    	            .addMember("strategy", "$T.IDENTITY", GenerationType.class)
    	            .build())
    	    .addAnnotation(AnnotationSpec.builder(Column.class)
    	            .addMember("name", "$S", "id_" + entity.getName().toLowerCase())
    	            .build())
    	    .build();
    
    typeSpecBuilder.addField(id);
    
    for (Map.Entry<String, TypeName> entry : entity.getAttributes().entrySet()) {
        FieldSpec.Builder fieldBuilder = FieldSpec.builder(entry.getValue(), entry.getKey(), Modifier.PRIVATE);
        
        if (entry.getValue() instanceof ParameterizedTypeName) {
            fieldBuilder.addAnnotation(AnnotationSpec.builder(OneToMany.class)
                        		.addMember("mappedBy", "$S", entity.getName().toLowerCase())
                        		.build())
                        .addAnnotation(AnnotationSpec.builder(JsonIgnore.class)
                        		.build());
        }
        else {
        	fieldBuilder.addAnnotation(AnnotationSpec.builder(Column.class)
            		.addMember("name", "$S", entity.getName().toLowerCase() + "_" + entry.getKey())
            		.build());
        }
        
        FieldSpec fieldSpec = fieldBuilder.build();
        typeSpecBuilder.addField(fieldSpec);
    }

    MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
    for (Map.Entry < String, TypeName > entry: entity.getAttributes().entrySet()) {
      constructorBuilder.addParameter(entry.getValue(), entry.getKey());
      constructorBuilder.addStatement("this.$N = $N", entry.getKey(), entry.getKey());
    }
    typeSpecBuilder.addMethod(constructorBuilder.build());

    for (Map.Entry < String, TypeName > entry: entity.getAttributes().entrySet()) {
      String attributeName = entry.getKey();
      TypeName attributeType = entry.getValue();

      MethodSpec getterMethod = MethodSpec.methodBuilder("get" + capitalize(attributeName))
        .addModifiers(Modifier.PUBLIC)
        .returns(attributeType)
        .addStatement("return this.$N", attributeName)
        .build();
      typeSpecBuilder.addMethod(getterMethod);

      MethodSpec setterMethod = MethodSpec.methodBuilder("set" + capitalize(attributeName))
        .addModifiers(Modifier.PUBLIC)
        .returns(void.class)
        .addParameter(attributeType, attributeName)
        .addStatement("this.$N = $N", attributeName, attributeName)
        .build();
      typeSpecBuilder.addMethod(setterMethod);
    }

    for (MethodSpec methodSpec: entity.getMethods()) {
      typeSpecBuilder.addMethod(methodSpec);
    }

    JavaFile javaFile = JavaFile.builder("ca.fubi.generator", typeSpecBuilder.build())
    		  .build();
    try {
    	Path path = Paths.get("C:/Users/ITAPERUÇU/");
    	javaFile.writeTo(path);
    	
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String capitalize(String str) {
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }
}