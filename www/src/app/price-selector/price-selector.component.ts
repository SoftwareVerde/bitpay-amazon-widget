import { Component, OnInit } from '@angular/core';
import { Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-price-selector',
  templateUrl: './price-selector.component.html',
  styleUrls: ['./price-selector.component.css']
})
export class PriceSelectorComponent implements OnInit {
  @Output() priceSelectedEvent = new EventEmitter<Number>();

  constructor() { }

  ngOnInit(): void {
  }

}

