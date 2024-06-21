package ca.fubi.classgenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

public class Entity {
	private String name;
	private Map<String, TypeName> attributes;
	private List<MethodSpec> methods;

	public Entity(String name) {
		this.name = name;
		this.attributes = new HashMap<>();
		this.methods = new ArrayList<>();
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

	public String getName() {
		return name;
	}
}