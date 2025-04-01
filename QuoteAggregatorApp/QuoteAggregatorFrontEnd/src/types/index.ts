export interface Quote {
    id: number;
    quoteText: string;
    character?: string;
    author?: string;
    image?: string;
    characterDirection?: string;
    category?: string;
    votes: number;
    source: string;
  }