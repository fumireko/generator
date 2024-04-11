package ca.fubi.generator;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void addAttribute(String attributeName, TypeName attributeType) {
        attributes.put(attributeName, attributeType);
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
    Entity addressEntity = new Entity("Address");
    addressEntity.addAttribute("street", ClassName.get(String.class));
    addressEntity.addAttribute("city", ClassName.get(String.class));
    addressEntity.addAttribute("state", ClassName.get(String.class));
    addressEntity.addAttribute("zipCode", ClassName.get(String.class));
    addressEntity.addAttribute("addressType", ClassName.get("", "AddressType"));

    Entity addressTypeEntity = new Entity("AddressType");
    addressTypeEntity.addAttribute("type", ClassName.get(String.class));

    Entity studentEntity = new Entity("Student");
    studentEntity.addAttribute("name", ClassName.get(String.class));
    studentEntity.addAttribute("email", ClassName.get(String.class));
    studentEntity.addAttribute("address", ClassName.get("", "Address"));
    studentEntity.addAttribute("courses", ParameterizedTypeName.get(ClassName.get("java.util", "List"), ClassName.get("", "Course")));

    Entity courseEntity = new Entity("Course");
    courseEntity.addAttribute("title", ClassName.get(String.class));
    courseEntity.addAttribute("description", ClassName.get(String.class));
    courseEntity.addAttribute("students", ParameterizedTypeName.get(ClassName.get("java.util", "List"), ClassName.get("", "Student")));

    generateEntity(studentEntity);
    generateEntity(courseEntity);
    generateEntity(addressEntity);
    generateEntity(addressTypeEntity);
  }

  private static void generateEntity(Entity entity) {
    TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(entity.getName())
      .addModifiers(Modifier.PUBLIC);

    // Add fields
    for (Map.Entry < String, TypeName > entry: entity.getAttributes().entrySet()) {
      FieldSpec fieldSpec = FieldSpec.builder(entry.getValue(), entry.getKey(), Modifier.PRIVATE).build();
      typeSpecBuilder.addField(fieldSpec);
    }

    // Constructor
    MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
    for (Map.Entry < String, TypeName > entry: entity.getAttributes().entrySet()) {
      constructorBuilder.addParameter(entry.getValue(), entry.getKey());
      constructorBuilder.addStatement("this.$N = $N", entry.getKey(), entry.getKey());
    }
    typeSpecBuilder.addMethod(constructorBuilder.build());

    // Getter and setter methods
    for (Map.Entry < String, TypeName > entry: entity.getAttributes().entrySet()) {
      String attributeName = entry.getKey();
      TypeName attributeType = entry.getValue();

      // Getter method
      MethodSpec getterMethod = MethodSpec.methodBuilder("get" + capitalize(attributeName))
        .addModifiers(Modifier.PUBLIC)
        .returns(attributeType)
        .addStatement("return this.$N", attributeName)
        .build();
      typeSpecBuilder.addMethod(getterMethod);

      // Setter method
      MethodSpec setterMethod = MethodSpec.methodBuilder("set" + capitalize(attributeName))
        .addModifiers(Modifier.PUBLIC)
        .returns(void.class)
        .addParameter(attributeType, attributeName)
        .addStatement("this.$N = $N", attributeName, attributeName)
        .build();
      typeSpecBuilder.addMethod(setterMethod);
    }

    // Add methods
    for (MethodSpec methodSpec: entity.getMethods()) {
      typeSpecBuilder.addMethod(methodSpec);
    }

    JavaFile javaFile = JavaFile.builder("com.example.generated", typeSpecBuilder.build()).build();
    try {
      javaFile.writeTo(System.out);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String capitalize(String str) {
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }
}