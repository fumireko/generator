package ca.fubi.generator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.Modifier;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Controller
public class EntityController {

	//TODO missing imports, put packages and paths on application.properties
	
	private static final String OUTPUT_PATH = "C:/Users/ITAPERUÇU/";

	@PostMapping("/all")
	public ResponseEntity<?> generateAll(@RequestBody EntityDTO[] entities) {
	    try {
	        for (EntityDTO entity : entities) {
	            Entity e = convertToEntity(entity);
	            writeJavaFile(generateEntity(e));
	            writeJavaFile(generateRepository(e));
	            writeJavaFile(generateController(e));
	        }
	        return ResponseEntity.ok().body("Files generated successfully.");
	    } catch (IOException e) {
	        return ResponseEntity.status(500).body("Error occurred while generating files: \n" + e.toString());
	    }
	}

    @PostMapping("/entities")
    public ResponseEntity<?> generateEntities(@RequestBody EntityDTO[] entities) {
    	String response = "";
        for (EntityDTO entity: entities) {
            response += generateEntity(convertToEntity(entity)).toString();
        }
        return ResponseEntity.ok().body(response);
    }
    
    @PostMapping("/repositories")
    public ResponseEntity<?> generateRepositories(@RequestBody EntityDTO[] entities) {
    	String response = "";
        for (EntityDTO entity: entities) {
            response += generateRepository(convertToEntity(entity)).toString();
        }
        return ResponseEntity.ok().body(response);
    }
    
    @PostMapping("/controllers")
    public ResponseEntity<?> generateControllers(@RequestBody EntityDTO[] entities) {
    	String response = "";
        for (EntityDTO entity: entities) {
            response += generateController(convertToEntity(entity)).toString();
        }
        return ResponseEntity.ok().body(response);
    }

    private static JavaFile generateEntity(Entity entity) {
    	String entityLowercase = entity.getName().toLowerCase();
    	
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(entity.getName())
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(jakarta.persistence.Entity.class)
            .addAnnotation(AnnotationSpec.builder(Table.class)
                .addMember("name", "\"tb_" + entityLowercase + "\"")
                .build());

        FieldSpec id = FieldSpec.builder(Long.class, "id", Modifier.PRIVATE)
            .addAnnotation(Id.class)
            .addAnnotation(AnnotationSpec.builder(GeneratedValue.class)
                .addMember("strategy", "$T.IDENTITY", GenerationType.class)
                .build())
            .addAnnotation(AnnotationSpec.builder(Column.class)
                .addMember("name", "$S", "id_" + entityLowercase)
                .build())
            .build();

        typeSpecBuilder.addField(id);

        for (Map.Entry < String, TypeName > entry: entity.getAttributes().entrySet()) {
            FieldSpec.Builder fieldBuilder = FieldSpec.builder(entry.getValue(), entry.getKey(), Modifier.PRIVATE);

            if (entry.getValue() instanceof ParameterizedTypeName) {
                fieldBuilder.addAnnotation(AnnotationSpec.builder(OneToMany.class)
                        .addMember("mappedBy", "$S", entityLowercase)
                        .build())
                    .addAnnotation(AnnotationSpec.builder(JsonIgnore.class)
                        .build());
            }
            if (entry.getValue().toString().contains(".")){
                fieldBuilder.addAnnotation(AnnotationSpec.builder(Column.class)
                    .addMember("name", "$S", entityLowercase + "_" + entry.getKey())
                    .build());
            }
            else {
            	fieldBuilder.addAnnotation(ManyToOne.class)
            		.addAnnotation(AnnotationSpec.builder(JoinColumn.class)
                        .addMember("name", "$S", "fk_" + entry.getKey())
                        .build());
            }

            FieldSpec fieldSpec = fieldBuilder.build();
            typeSpecBuilder.addField(fieldSpec);
        }
        
        typeSpecBuilder.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build());

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

        return JavaFile.builder("ca.fubi.generator", typeSpecBuilder.build()).build();
    }
    
    private static JavaFile generateController(Entity entity) {
    	String entityLowercase = entity.getName().toLowerCase();
    	
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(entity.getName() + "Controller")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(RestController.class)
                .addAnnotation(AnnotationSpec.builder(RequestMapping.class)
                		.addMember("value", "$S", "/api/" + entityLowercase)
                		.build())
                .addField(FieldSpec.builder(getTypeName(capitalize(entity.getName()) + "Repository"), entityLowercase + "Repo")
                		.addAnnotation(Autowired.class)
                		.build());

        MethodSpec getResource = MethodSpec.methodBuilder("get" + entity.getName())
                .addModifiers(Modifier.PUBLIC)
                .returns(getTypeName("ResponseEntity<" + entity.getName() + ">"))
                .addParameter(ParameterSpec.builder(Long.class, "id")
                		.addAnnotation(AnnotationSpec.builder(PathVariable.class)
                				.addMember("value", "$S", "id")
                				.build()).build())
                .addStatement("return ResponseEntity.of(" + entityLowercase + "Repo.findById(id))")
                .addAnnotation(AnnotationSpec.builder(GetMapping.class)
                		.addMember("value", "$S", "/{id}")
                        .build())
                .build();
        
        MethodSpec getAllResource = MethodSpec.methodBuilder("getAll" + entity.getName() + "s")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(
                        ClassName.get("org.springframework.http", "ResponseEntity"), 
                        ParameterizedTypeName.get(ClassName.get("java.util", "List"), getTypeName(entity.getName()))))
                .addStatement("return ResponseEntity.ok(" + entityLowercase + "Repo.findAll())")
                .addAnnotation(AnnotationSpec.builder(GetMapping.class)
                		.addMember("value", "$S", "/")
                        .build())
                .build();

        MethodSpec createResource = MethodSpec.methodBuilder("create" + entity.getName())
                .addModifiers(Modifier.PUBLIC)
                .returns(getTypeName("ResponseEntity<" + entity.getName() + ">"))
                .addParameter(ParameterSpec.builder(getTypeName(entity.getName()), "body")
                		.addAnnotation(RequestBody.class).build())
                .addStatement("return ResponseEntity.ok(" + entityLowercase + "Repo.save(body))")
                .addAnnotation(AnnotationSpec.builder(PostMapping.class)
                		.addMember("value", "$S", "/")
                        .build())
                .build();

        MethodSpec updateResource = MethodSpec.methodBuilder("update" + entity.getName())
                .addModifiers(Modifier.PUBLIC)
                .returns(getTypeName("ResponseEntity<" + entity.getName() + ">"))
                .addParameter(ParameterSpec.builder(getTypeName(entity.getName()), "body")
                		.addAnnotation(RequestBody.class).build())
                .addParameter(ParameterSpec.builder(Long.class, "id")
                        .addAnnotation(AnnotationSpec.builder(PathVariable.class)
                        		.addMember("value", "$S", "id")
                                .build())
                        .build())
                .addCode(CodeBlock.builder()
                        .beginControlFlow("return " + entityLowercase + "Repo" + ".findById(id).map("+ entityLowercase +" -> ")
                        .addStatement("$T.copyProperties(body, " + entityLowercase + ")", BeanUtils.class)
                        .addStatement("return ResponseEntity.ok(" + entityLowercase + "Repo.save("+ entityLowercase + "))")
                        .endControlFlow(").orElse(ResponseEntity.notFound().build())")
                        .build())
                .addAnnotation(AnnotationSpec.builder(PutMapping.class)
                		.addMember("value", "$S", "/{id}")
                        .build())
                .build();

        MethodSpec deleteResource = MethodSpec.methodBuilder("delete" + entity.getName())
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), getTypeName(entity.getName())))
                .addParameter(ParameterSpec.builder(Long.class, "id")
                		.addAnnotation(AnnotationSpec.builder(PathVariable.class)
                				.addMember("value", "$S", "id")
                				.build()).build())
                .addStatement(entityLowercase + "Repo.deleteById(id)")
                .addStatement("return ResponseEntity.noContent().build()")
                .addAnnotation(AnnotationSpec.builder(DeleteMapping.class)
                		.addMember("value", "$S", "/{id}")
                        .build())
                .build();

        TypeSpec typeSpec = typeSpecBuilder.addMethod(getResource)
        		.addMethod(getAllResource)
                .addMethod(createResource)
                .addMethod(updateResource)
                .addMethod(deleteResource)
                .build();

        return JavaFile.builder("ca.fubi.generator.controller", typeSpec).build();
    }
    
    private static JavaFile generateRepository(Entity entity) {
        String entityName = entity.getName();
        
        TypeSpec repositoryInterface = TypeSpec.interfaceBuilder(entityName + "Repository")
        		.addSuperinterface(ParameterizedTypeName.get(ClassName.get(JpaRepository.class), getTypeName(entityName), TypeName.get(Long.class)))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Repository.class)
                .addMethod(MethodSpec.methodBuilder("findById")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(ParameterizedTypeName.get(ClassName.get(Optional.class), getTypeName(entityName)))
                        .addParameter(Long.class, "id")
                        .build())
                .addMethod(MethodSpec.methodBuilder("findAll")
                	    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                	    .returns(ParameterizedTypeName.get(ClassName.get("java.util", "List"),getTypeName(entityName)))
                	    .build())
                .addMethod(MethodSpec.methodBuilder("save")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(getTypeName(entityName))
                        .addParameter(TypeVariableName.get(entityName), entityName.toLowerCase())
                        .build())
                .addMethod(MethodSpec.methodBuilder("deleteById")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(void.class)
                        .addParameter(Long.class, "id")
                        .build())
                .build();

        return JavaFile.builder("ca.fubi.generator.repository", repositoryInterface).build();
    }
    
	private void writeJavaFile(JavaFile javaFile) throws IOException {
	    Path path = Paths.get(OUTPUT_PATH);
	    javaFile.writeTo(path);
	}	

    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private Entity convertToEntity(EntityDTO entityDTO) {
        Entity entity = new Entity(entityDTO.getName());
        for (AttributeDTO attribute: entityDTO.getAttributes()) entity.addAttribute(attribute.getName(), getTypeName(attribute.getType()));
        return entity;
    }

    private static TypeName getTypeName(String type) {
        Pattern pattern = Pattern.compile("<(.+?)>");
        Matcher matcher = pattern.matcher(type);

        if (matcher.find()) {
            String outerType = type.substring(0, type.indexOf("<"));
            String innerType = matcher.group(1);

            ClassName outerClassName = ClassName.bestGuess(outerType);
            ClassName innerClassName = ClassName.bestGuess(innerType);

            return ParameterizedTypeName.get(outerClassName, innerClassName);
        } else {
            return ClassName.bestGuess(type);
        }
    }
}