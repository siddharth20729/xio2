# Server

> One Server to rule them all, One Server to find them,
> One Server to bring them all and in the darkness bind them

 * Composes the routes map, server socket channel, acceptor, and event loop pool.
 * Provides a method to add routes to the map.
 * Configures event loop pool to use all of the available cores.
 * Attaches the acceptor to the event loop pool so it will receive incoming events.
 * Runs event loop pool until it terminates (effectively forever).
