# Server

> One Server to rule them all, One Server to find them,

> One Server to bring them all and in the darkness bind them

 * Composes the routes map, server socket channel, acceptor, and event loop pool.
 * Provides a method to add routes to the map.
 * Configures event loop pool to use all of the available cores and starts it.
 * Runs acceptor event loop until it terminates (effectively forever).
 
# Acceptor

 * Abstracts away the implementation details of java nio.
 * Implements an event loop that just deals with accept events.
 * Accepts incoming connections, builds a channel context, attaches the context to an event loop from the pool.
