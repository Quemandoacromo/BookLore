import {Sort} from './sort.model';

export interface Library {
  id?: number;
  name: string;
  sort?: Sort;
  paths: string[];
}
