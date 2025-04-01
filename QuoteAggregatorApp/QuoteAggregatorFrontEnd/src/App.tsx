import React, { useEffect, useRef, useState } from 'react';
import { Quote } from './types';
import { fetchSimpsonsQuote, fetchNinjasQuote, voteForQuote, fetchFilteredSimpsonsQuote, fetchQuoteSources } from './services/api';
import { QuoteCard } from './components/QuoteCard';
import { TopQuotes } from './components/TopQuotes';
import LoginButton from './components/LoginButton';
import { useKeycloak } from '@react-keycloak/web';

const App: React.FC = () => {
  const [simpsonsQuote, setSimpsonsQuote] = useState<Quote | null>(null);
  const [ninjasQuote, setNinjasQuote] = useState<Quote | null>(null);
  const [loading, setLoading] = useState({ simpsons: false, ninjas: false });
  const [error, setError] = useState({ simpsons: '', ninjas: '' });
  const [activeTab, setActiveTab] = useState<'quotes' | 'top'>('quotes');
  const [quoteSources, setQuoteSources] = useState<Quote[] | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const { keycloak, initialized } = useKeycloak();


  const [filterEnabled, setFilterEnabled] = useState(false);
  const [filterCharacter, setFilterCharacter] = useState('');

  const initialFetchRef = useRef(false);

  const isAdmin = initialized && keycloak.authenticated && keycloak.hasRealmRole('admin');

  const fetchSimpsons = async () => {
    setLoading(prev => ({ ...prev, simpsons: true }));
    setError(prev => ({ ...prev, simpsons: '' }));
    try {
      let quote;
      if (filterEnabled && filterCharacter.trim()) {
        quote = await fetchFilteredSimpsonsQuote(filterCharacter);
      } else {
        quote = await fetchSimpsonsQuote();
      }
      setSimpsonsQuote({ ...quote, source: 'simpsons' });
    } catch (err) {
      setError(prev => ({ ...prev, simpsons: 'Failed to fetch Simpsons quote' }));
    } finally {
      setLoading(prev => ({ ...prev, simpsons: false }));
    }
  };

  const fetchNinjas = async () => {
    setLoading(prev => ({ ...prev, ninjas: true }));
    setError(prev => ({ ...prev, ninjas: '' }));
    try {
      const quote = await fetchNinjasQuote();
      setNinjasQuote({ ...quote, source: 'ninjas' });
    } catch (err) {
      setError(prev => ({ ...prev, ninjas: 'Failed to fetch Ninjas quote' }));
    } finally {
      setLoading(prev => ({ ...prev, ninjas: false }));
    }
  };

  const handleVote = async (id: number, email: string) => {
    try {
      const updatedQuote = await voteForQuote(id, email);
      // Update the quote in our state if it matches
      if (simpsonsQuote && simpsonsQuote.id === id) {
        setSimpsonsQuote({ ...updatedQuote, source: 'simpsons' });
      }
      if (ninjasQuote && ninjasQuote.id === id) {
        setNinjasQuote({ ...updatedQuote, source: 'ninjas' });
      }
    } catch (err) {
      console.error('Error voting for quote:', err);
    }
  };

  // Handle filter form submission
  const handleFilterSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchSimpsons();
  };

  useEffect(() => {
    // Only fetch when initialized and authenticated
    if (initialized && keycloak.authenticated && !initialFetchRef.current) {
      initialFetchRef.current = true;
      fetchSimpsons();
      fetchNinjas();
    }
  }, [initialized, keycloak.authenticated]);

  const LoginScreen = () => (
    <div className="flex flex-col items-center justify-center min-h-[50vh] bg-gray-100 rounded-lg shadow-md p-8 max-w-md mx-auto mt-10">
      <h2 className="text-2xl font-bold mb-6 text-gray-800">Welcome to Quote Aggregator</h2>
      <p className="text-gray-600 mb-8 text-center">
        Please log in to view and interact with quotes.
      </p>
      <LoginButton />
    </div>
  );

  if (!initialized) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center p-8">
          <h1 className="text-3xl font-bold mb-4">Quote Aggregator</h1>
          <p className="text-gray-600">Initializing authentication...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Centered container */}
      <div className="max-w-4xl mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold text-center mb-8">Quote Aggregator</h1>

        {keycloak.authenticated ? (
          // Content visible to logged-in users
          <>
            {/* User status and logout in header */}
            <div className="flex justify-end mb-4">
              <LoginButton />
            </div>

            {/* Tab Navigation */}
            <nav className="flex justify-center border-b pb-4 mb-6 space-x-4" role="tablist">
              <button
                role="tab"
                aria-selected={activeTab === 'quotes'}
                className={`py-2 px-4 font-medium border-b-2 ${activeTab === 'quotes' ? 'border-blue-500 text-blue-600' : 'border-transparent hover:text-gray-600'
                  }`}
                onClick={() => setActiveTab('quotes')}
              >
                Random Quotes
              </button>
              <button
                role="tab"
                aria-selected={activeTab === 'top'}
                className={`py-2 px-4 font-medium border-b-2 ${activeTab === 'top' ? 'border-blue-500 text-blue-600' : 'border-transparent hover:text-gray-600'
                  }`}
                onClick={() => setActiveTab('top')}
              >
                Hall of Fame
              </button>
              {isAdmin && (
                <>
                  <button
                    role="tab"
                    className="py-2 px-4 font-medium border-b-2 border-transparent hover:text-blue-600"
                    onClick={() => {
                      if (quoteSources) {
                        // If sources are already displayed, hide them
                        setQuoteSources(null);
                      } else {
                        // Otherwise fetch and display them
                        setIsLoading(true);
                        fetchQuoteSources()
                          .then(sources => {
                            setQuoteSources(sources);
                            setIsLoading(false);
                          })
                          .catch(error => {
                            console.error('Error fetching quote sources:', error);
                            setIsLoading(false);
                          });
                      }
                    }}
                  >
                    {quoteSources ? 'Hide Quote Sources' : 'List Quote Sources'}
                  </button>

                  {isLoading && (
                    <div className="mt-4">Loading...</div>
                  )}

                  {quoteSources && (
                    <div className="mt-4">
                      <div className="flex justify-between items-center mb-2">
                        <h3 className="text-lg font-medium">Quote Sources</h3>
                        <button
                          className="text-gray-500 hover:text-red-500"
                          onClick={() => setQuoteSources(null)}
                        >
                          Close
                        </button>
                      </div>
                      <table className="min-w-full border border-gray-300">
                        <thead>
                          <tr className="bg-gray-100">
                            <th className="py-2 px-4 border-b text-left">Source Name</th>
                            <th className="py-2 px-4 border-b text-left">URL</th>
                          </tr>
                        </thead>
                        <tbody>
                          {Object.entries(quoteSources).map(([name, url]) => (
                            <tr key={name} className="border-b hover:bg-gray-50">
                              <td className="py-2 px-4">{name}</td>
                              <td className="py-2 px-4">{typeof url === 'string' ? url : JSON.stringify(url)}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </>
              )}
            </nav>

            {/* Content */}
            {activeTab === 'quotes' ? (
              <div className="space-y-8">
                {/* Simpsons Quote Section */}
                <section>
                  <div className="flex items-center justify-between mb-4">
                    <h2 className="text-xl font-semibold">Simpsons Quote</h2>

                    {/* Filter Toggle */}
                    <label className="flex items-center space-x-2">
                      <input
                        type="checkbox"
                        className="h-4 w-4"
                        checked={filterEnabled}
                        onChange={() => setFilterEnabled(!filterEnabled)}
                      />
                      <span className="text-sm text-gray-700">Enable Filtering</span>
                    </label>
                  </div>

                  {/* Filter Form */}
                  {filterEnabled && (
                    <form onSubmit={handleFilterSubmit} className="bg-white rounded-lg shadow p-4 mb-4">
                      <label htmlFor="character" className="block text-sm font-medium text-gray-700 mb-1">
                        Character
                      </label>
                      <input
                        type="text"
                        id="character"
                        className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none 
                                focus:ring-blue-500 focus:border-blue-500"
                        value={filterCharacter}
                        onChange={(e) => setFilterCharacter(e.target.value)}
                        placeholder="e.g. Homer"
                        required
                      />
                      <button
                        type="submit"
                        disabled={loading.simpsons}
                        className={`mt-4 inline-block px-4 py-2 rounded-md font-medium 
                          ${loading.simpsons ? 'bg-blue-300 cursor-not-allowed' : 'bg-blue-500 text-white hover:bg-blue-600'}`}
                      >
                        Apply Filter
                      </button>
                    </form>
                  )}

                  {/* Simpsons Quote Card */}
                  {loading.simpsons ? (
                    <div className="text-center p-4">Loading...</div>
                  ) : error.simpsons ? (
                    <div className="bg-red-100 p-4 rounded-lg text-red-700">{error.simpsons}</div>
                  ) : simpsonsQuote ? (
                    <QuoteCard
                      quote={simpsonsQuote}
                      onRefresh={fetchSimpsons}
                      onVote={handleVote}
                    />
                  ) : null}
                </section>

                {/* Ninjas Quote Section */}
                <section>
                  <h2 className="text-xl font-semibold mb-4">Ninjas Quote</h2>
                  {loading.ninjas ? (
                    <div className="text-center p-4">Loading...</div>
                  ) : error.ninjas ? (
                    <div className="bg-red-100 p-4 rounded-lg text-red-700">{error.ninjas}</div>
                  ) : ninjasQuote ? (
                    <QuoteCard
                      quote={ninjasQuote}
                      onRefresh={fetchNinjas}
                      onVote={handleVote}
                    />
                  ) : null}
                </section>
              </div>
            ) : (
              <TopQuotes />
            )}
          </>
        ) : (
          // Content visible to logged-out users
          <LoginScreen />
        )}
      </div>
    </div>
  );
};

export default App;