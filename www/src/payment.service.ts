import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})

export class PaymentService {

    constructor(private httpClient: HttpClient) { }

    httpOptions = {
        headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    };

    createNewAddress(giftCardAmount: number, emailAddress: string) : Observable<any> {
        let request = {
            giftCardAmount: giftCardAmount,
            emailAddress: emailAddress
        };
        return this.httpClient.post("/api/v1/redeem/new", request, this.httpOptions).pipe(
            tap((data) => {
                return data;
            }),
            catchError(this.handleError('createNewAddress'))
        );
    }

    getExchangeRate() : Observable<any> {
        return this.httpClient.get("/api/v1/exchange-rate").pipe(
            tap((data) => {
                return data;
            }),
            catchError(this.handleError('getExchangeRate'))
        );
    }

    handleError<T>(method: string, result?: T) {
        return (error: any) : Observable<T> => {
            console.log(`${method}: ${error.status} ${error.statusText}: ${error.error.errorMessage}`);

            if (result) {
                return of(result as T);
            }

            return of(error.error as T);
        };
    }
}
