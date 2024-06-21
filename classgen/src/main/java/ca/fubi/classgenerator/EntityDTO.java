package ca.fubi.classgenerator;

import java.util.List;

class AttributeDTO {
	private String name;
	private String type;
	private boolean hasParameter;

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean hasParameter() {
		return hasParameter;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParameter(boolean parameter) {
		this.hasParameter = parameter;
	}

	public void setType(String type) {
		this.type = type;
	}
}

public class EntityDTO {

	private String name;
	private List<AttributeDTO> attributes;

	public EntityDTO() {
	}

	public List<AttributeDTO> getAttributes() {
		return attributes;
	}

	public String getName() {
		return name;
	}

	public void setAttributes(List<AttributeDTO> attributes) {
		this.attributes = attributes;
	}

	public void setName(String name) {
		this.name = name;
	}
}
