// src/components/TopQuotes.tsx
import React, { useEffect, useState } from 'react';
import { Quote } from '../types';
import { fetchTopQuotes, voteForQuote } from '../services/api';
import { QuoteCard } from './QuoteCard';

export const TopQuotes: React.FC = () => {
    const [topQuotes, setTopQuotes] = useState<Quote[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [limit, setLimit] = useState(10); // Default to 10 quotes

    const loadTopQuotes = async () => {
        setLoading(true);
        setError('');
        try {
            const quotes = await fetchTopQuotes(limit);
            setTopQuotes(quotes);
        } catch (err) {
            setError('Failed to load top quotes');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleVote = async (id: number, email: string) => {
        try {
            const updatedQuote = await voteForQuote(id, email);
            // Update the quote in our list
            setTopQuotes(quotes => quotes.map(q =>
                q.id === updatedQuote.id ? updatedQuote : q
            ).sort((a, b) => b.votes - a.votes)); // Re-sort after vote
        } catch (err) {
            console.error('Error voting for quote:', err);
        }
    };

    useEffect(() => {
        loadTopQuotes();

        // Optionally refresh the top quotes periodically
        const interval = setInterval(loadTopQuotes, 30000); // Refresh every 30 seconds
        return () => clearInterval(interval);
    }, [limit]); // Re-fetch when limit changes

    if (loading && topQuotes.length === 0) {
        return <div className="text-center p-4">Loading top quotes...</div>;
    }

    if (error && topQuotes.length === 0) {
        return <div className="bg-red-100 p-4 rounded-lg text-red-700">{error}</div>;
    }

    return (
        <div>
            <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold">Hall of Fame</h2>
                <div className="flex space-x-2">
                    <span className="self-center mr-2">Show:</span>
                    {[5, 10, 20].map((value) => (
                        <button
                            key={value}
                            onClick={() => setLimit(value)}
                            className={`px-3 py-1 rounded ${
                                limit === value
                                    ? 'bg-blue-600 text-white'
                                    : 'bg-gray-200 hover:bg-gray-300'
                            }`}
                        >
                            {value}
                        </button>
                    ))}
                </div>
            </div>
            {topQuotes.length === 0 ? (
                <p>No quotes have been voted for yet. Be the first to vote!</p>
            ) : (
                <div>
                    {topQuotes.map(quote => (
                        <QuoteCard
                            key={quote.id}
                            quote={quote}
                            onVote={handleVote}
                        />
                    ))}
                </div>
            )}
            <button
                onClick={loadTopQuotes}
                className="mt-4 bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600"
            >
                Refresh Top Quotes
            </button>
        </div>
    );
};