export interface Word {
  word: string
  partOfSpeech: string
  definition: string
  example: string
  tagalogWord: string
  tagalogDefinition: string
  category: string
  categoryGroup: string
}

export interface WordDetail extends Word {
  isFavorite: boolean
}

export interface WordPage {
  items: Word[]
  total: number
  offset: number
  limit: number
  hasMore: boolean
}

export interface Learning {
  id: number
  word: string
  text: string
  createdAt: number
  updatedAt: number
}

export interface CategorySummary {
  category: string
  categoryGroup: string
  wordCount: number
}

export interface CategoryGroup {
  group: string
  wordCount: number
  categories: CategorySummary[]
}

export interface PartOfSpeechSummary {
  partOfSpeech: string
  wordCount: number
}
