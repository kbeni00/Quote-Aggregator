import React, { useState } from 'react';
import { Quote } from '../types';
import { useKeycloak } from '@react-keycloak/web';

interface QuoteCardProps {
  quote: Quote;
  onRefresh?: () => void;
  onVote: (id: number, email: string) => void;
}

export const QuoteCard: React.FC<QuoteCardProps> = ({ quote, onRefresh, onVote }) => {
  const [hasVoted, setHasVoted] = useState(false);

  const isSimpsons = quote.source === 'simpsons';
  const borderColor = isSimpsons ? 'border-yellow-400' : 'border-purple-400';

  const keycloak = useKeycloak().keycloak;
  const handleVoteClick = async () => {
    if (!keycloak.authenticated) {
      alert("You need to log in to vote!");
      return;
    }

    const userEmail = keycloak.tokenParsed?.email;
    console.log(keycloak.tokenParsed)
    if (!userEmail) {
      alert("Could not retrieve your email from Keycloak!");
      return;
    }

    if (hasVoted) {
      alert("You have already voted for this quote.");
      return;
    }

    try {
      await onVote(quote.id, userEmail);
      setHasVoted(true);
    } catch (error) {
      if (error instanceof Error) {
        alert(error.message);
      } else {
        alert("An unknown error occurred.");
      }
    }
  };


  return (
    <div
      className={`border-l-4 ${borderColor} bg-white p-6 rounded-lg shadow-md mb-6 transition-all duration-200`}
    >
      <div className="flex items-start space-x-6">
        {quote.image && (
          <img
            src={quote.image}
            alt={`Avatar of ${quote.character || quote.author}`}
            className="w-36 h-36 object-contain"
            onError={(e) => {
              e.currentTarget.src = '/api/placeholder/150/150';
            }}
          />
        )}
        <div className="flex-1">
          <blockquote className="text-2xl italic text-gray-800 mb-2">
            “{quote.quoteText}”
          </blockquote>
          <p className="text-right text-lg text-gray-700 font-semibold">
            — {quote.character || quote.author}
          </p>
          {quote.category && (
            <span className="inline-block mt-2 bg-gray-300 text-gray-800 text-xs px-3 py-1 rounded-full">
              {quote.category}
            </span>
          )}
        </div>
      </div>

      <div className="flex justify-between items-center mt-6">
        <div className="flex items-center space-x-3">
          <button
            onClick={handleVoteClick}
            disabled={hasVoted}
            style={{ backgroundColor: hasVoted ? "#9CA3AF" : "#047857" }} // Explicitly set colors
            className="flex items-center px-5 py-3 text-white text-base rounded-md focus:outline-none focus:ring-2 transition-colors duration-200"
          >
            <span className="mr-2">👍</span> Vote
          </button>
          <span className="font-bold text-gray-800 text-lg">{quote.votes} votes</span>
        </div>

        {onRefresh && (
          <button
          onClick={onRefresh}
          style={{
            backgroundColor: "#2563EB" // Blue or Purple
          }}
          className="flex items-center px-6 py-3 rounded-md text-white text-base focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors duration-200"
          >
            <svg
              className="w-5 h-5 mr-2"
              fill="currentColor"
              viewBox="0 0 20 20"
            >
              <path d="M4.93 11H2a8 8 0 1 0 8-8v2a6 6 0 1 1-6 6Z" />
            </svg>
            New Quote
          </button>
        )}
      </div>
    </div>
  );
};