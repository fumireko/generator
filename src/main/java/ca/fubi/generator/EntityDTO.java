package ca.fubi.generator;

import java.util.List;

public class EntityDTO {

	private String name;
	private List<AttributeDTO> attributes;
	
	public EntityDTO() {}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public List<AttributeDTO> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(List<AttributeDTO> attributes) {
		this.attributes = attributes;
	}
}

class AttributeDTO {
	private String name;
	private String type;
	private boolean hasParameter;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public boolean hasParameter() {
		return hasParameter;
	}
	
	public void setParameter(boolean parameter) {
		this.hasParameter = hasParameter;
	}
}
