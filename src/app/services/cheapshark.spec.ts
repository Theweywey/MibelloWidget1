import { TestBed } from '@angular/core/testing';

import { Cheapshark } from './cheapshark';

describe('Cheapshark', () => {
  let service: Cheapshark;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Cheapshark);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
