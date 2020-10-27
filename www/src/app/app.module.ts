import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';
import { PriceSelectorComponent } from './price-selector/price-selector.component';
import { PurchaseQrCodeComponent } from './purchase-qr-code/purchase-qr-code.component';

@NgModule({
  declarations: [
    AppComponent,
    PriceSelectorComponent,
    PurchaseQrCodeComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
