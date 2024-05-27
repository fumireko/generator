import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { MonacoEditorModule, MonacoProviderService } from 'ng-monaco-editor';
import { FormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { HttpClientModule } from '@angular/common/http';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    MonacoEditorModule.forRoot({
      dynamicImport: () => import('monaco-editor')
    }),
    NgbModule,
    HttpClientModule
  ],
  providers: [
    {
      provide: MonacoProviderService
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
