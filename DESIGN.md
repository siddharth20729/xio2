# Overview

* Server Start
* Acceptor receives connection
* Channel Context parses request
* Service issues response

# Server

> One Server to rule them all, One Server to find them,

> One Server to bring them all and in the darkness bind them

 * Composes the routes map, server socket channel, acceptor, and event loop pool.
 * Provides a method to add routes to the map.
 * Configures event loop pool to use all of the available cores and starts it.
 * Runs acceptor event loop until it terminates (effectively forever).
 
# Acceptor

 * Abstracts away the implementation details of java nio accept events.
 * Implements an event loop that just deals with accept events.
 * Accepts incoming connections, builds a channel context, attaches the context to an event loop from the pool.

# ChannelContext

 * Composes socket reading with http parsing to build an http request object.
 * Maintains a simple state machine that represents the status of the request/response cycle.
 * Finds a service in the routes map and dispatches it to handle the request, otherwise issues a 404
 * Provides an abstraction to write http responses to the wire.

# Route

 * Implements a sinatra style url matcher using regex.

# EventLoop

 * Abstracts away the implementation details of java nio read/write events.
 * Runs inside of it's own thread.
 * Composes channel contexts with nio events to cause i/o.
