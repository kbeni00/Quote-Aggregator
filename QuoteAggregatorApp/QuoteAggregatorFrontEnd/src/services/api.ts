import keycloak from '../keycloak';
import { Quote } from '../types';

const API_BASE_URL = 'http://localhost:8080';

export const fetchSimpsonsQuote = async (): Promise<Quote> => {
  const response = await fetch(`${API_BASE_URL}/quotes/simpsons`);
  if (!response.ok) {
    throw new Error('Failed to fetch Simpsons quote');
  }
  return await response.json();
};

export const fetchFilteredSimpsonsQuote = async (character: string): Promise<Quote> => {
  const response = await fetch(`${API_BASE_URL}/quotes/simpsons/filtered?character=${character}`);
  if (!response.ok) {
    throw new Error('Failed to fetch Simpsons quote');
  }
  return await response.json();
};

export const fetchNinjasQuote = async (): Promise<Quote> => {
  const response = await fetch(`${API_BASE_URL}/quotes/ninjas`);
  if (!response.ok) {
    throw new Error('Failed to fetch Ninjas quote');
  }
  return await response.json();
};

export const voteForQuote = async (id: number, userEmail: string): Promise<Quote> => {
  const token = keycloak.token;
  
  const response = await fetch(`${API_BASE_URL}/quotes/${id}/vote`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ userEmail })
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || 'Failed to vote for quote');
  }

  return await response.json();
};

export const fetchTopQuotes = async (limit: number = 10): Promise<Quote[]> => {
  const response = await fetch(`${API_BASE_URL}/quotes/top?limit=${limit}`);
  if (!response.ok) {
    throw new Error('Failed to fetch top quotes');
  }
  return await response.json();
};

export const fetchQuoteSources = async (): Promise<Quote[]> => {
  const response = await fetch(`${API_BASE_URL}/quotes/quote-sources`);
  if (!response.ok) {
    throw new Error('Failed to fetch quote sources');
  }
  return await response.json();
};