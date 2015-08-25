package com.pdex.asyncio;

interface RequestConnectionFactory {
  ConnectionFactory build(Request request);
}
