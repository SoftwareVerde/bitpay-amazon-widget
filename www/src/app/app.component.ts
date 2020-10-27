import { Component } from '@angular/core';
import { PaymentService } from "../payment.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
    shouldDisplayQrCode = false;
    giftCardAmount = 2000;
    emailAddress = "";

    constructor(private paymentService: PaymentService) {}

    getQrCode() {
        console.log("Amount: " + this.giftCardAmount);
        this.paymentService.createNewAddress(this.giftCardAmount, this.emailAddress).subscribe(response => {
            if (response.wasSuccess) {
                this.shouldDisplayQrCode = true;
                alert(response.address.base32CheckEncoded);
            }
            else {
                alert(response.errorMessage);
            }
        });
    }
}

