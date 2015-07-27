#!/bin/bash

## Generates the keystore
yes xioxio | keytool -keystore xio2.jks -genkeypair -keyalg EC -alias xio2 \
      -dname 'CN=localhostl,L=Chicago,ST=IL,C=US,OU=xio2' \
      -keysize 384 \
      -sigalg SHA384withECDSA

## Converts the keystore to PKCS12 format
yes xioxio | keytool -importkeystore -srckeystore xio2.jks \
  -destkeystore xio2.p12 \
  -srcalias xio2 \
  -srcstoretype jks \
  -deststoretype pkcs12

## Creates an x509 pem for use with OpenSSL clients / servers
yes xioxio | openssl pkcs12 -in xio2.p12 -out xio2.pem -aes256

## Validate certs | no actual key
yes xioxio | keytool -keystore xio2.jks -exportcert -alias xio2 | \
  openssl x509 -inform der -text
yes xioxio | openssl x509 -text -in xio2.pem

## Exports the CA of the self signed cert for known clients
keytool -export -rfc -keystore xio2.p12 -storetype PKCS12 -providername SunJSSE -v -alias xio2 > xio2.crt

## Imports CA certs for known clients into a new truststore
keytool -import -file xio2.crt -alias xio2 -keystore xio2.ts -storepass xioxio
