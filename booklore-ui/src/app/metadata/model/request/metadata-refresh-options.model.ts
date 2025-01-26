export interface MetadataRefreshOptions {
  allP3: string | null;
  allP2: string | null;
  allP1: string | null;
  refreshCovers: boolean;
  mergeCategories: boolean;
  fieldOptions?: FieldOptions;
}

export interface FieldProvider {
  p3: string | null;
  p2: string | null;
  p1: string | null;
}

export interface FieldOptions {
  title: FieldProvider;
  description: FieldProvider;
  authors: FieldProvider;
  categories: FieldProvider;
  cover: FieldProvider;
}
