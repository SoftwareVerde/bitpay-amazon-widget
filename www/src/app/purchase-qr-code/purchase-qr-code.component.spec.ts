import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PurchaseQrCodeComponent } from './purchase-qr-code.component';

describe('PurchaseQrCodeComponent', () => {
  let component: PurchaseQrCodeComponent;
  let fixture: ComponentFixture<PurchaseQrCodeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PurchaseQrCodeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PurchaseQrCodeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
