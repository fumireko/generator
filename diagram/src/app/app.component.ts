import { Component } from '@angular/core';
import { Entity } from './models/entity.class';
import { Attribute } from './models/attribute.class';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})

export class AppComponent {  

  constructor(
    private http: HttpClient
  ){}

  model = '[]';
  response: any;
  entity: Entity = new Entity('', []);
  newAttribute: Attribute = new Attribute('', '');

  options = {
    folding: true,
    minimap: { enabled: false },
    readOnly: false,
    language: 'java',
    theme: 'vs-dark'
  };

  addAttribute() {
    if (!this.newAttribute.name.trim() || !this.newAttribute.type.trim() || !/^[a-z]/.test(this.newAttribute.name)) {
      alert('Please ensure the attribute name and type are correctly filled out and the name starts with a lowercase letter.');
      return;
    }

    this.entity.attributes.push(new Attribute(this.newAttribute.name, this.newAttribute.type));
    this.newAttribute.name = '';
    this.newAttribute.type = '';
  }

  removeAttribute(index: number) {
    this.entity.attributes.splice(index, 1);
  }

  generateEntity() {
    if (!this.validateEntity()) {
      alert('Please ensure the attributes are correctly filled out.');
      return;
    }

    const newEntity = new Entity(this.entity.name, [...this.entity.attributes]);

    const entities = JSON.parse(this.model);
    entities.push(newEntity);
    this.model = JSON.stringify(entities, null, 4);

    this.resetForm();
  }

  validateEntity(): boolean {
    if (!this.entity.name.trim()) {
      return false;
    }

    for (let attribute of this.entity.attributes) {
      if (!attribute.name.trim() || !attribute.type.trim() || !/^[a-z]/.test(attribute.name)) {
        return false;
      }
    }
    return true;
  }

  sendRequest(location: string) {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });
    this.http.post('http://localhost:8808/' + location, this.model, { headers: headers })
      .subscribe(response => {
        this.response = response;

        if(this.response){
          this.resetModel();
          this.model = this.response.body;
          this.options.language = 'java';  
        }
      }, error => {
        this.response = error;
      });
  }

  resetForm() {
    this.entity.name = '';
    this.entity.attributes = [];
  }

  resetModel() {
    this.model = '[]';
  }
}
