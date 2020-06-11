#!/usr/bin/env bash

set -x # verbose output
set -e # exit when the first error occurs

### create a server keystore with a public and private key

keytool -genkeypair -keyalg RSA -keysize 4096 -alias server -dname 'CN=server,OU=Pisa,O=RedHat,C=IT' -ext 'SAN:c=DNS:localhost,IP:127.0.0.1' -validity 18250 -keystore server_identity.jks -storepass 'password' -keypass 'password' -deststoretype jks

### create an expired client keystore with a public and private key

keytool -genkeypair -keyalg RSA -keysize 4096 -alias client -dname 'CN=localhost,OU=Pisa,O=RedHat,C=IT' -ext 'SAN:c=DNS:localhost,IP:127.0.0.1' -startdate '2019/01/01 00:00:00' -validity 1 -keystore client_identity.jks -storepass 'password' -keypass 'password' -deststoretype jks

### create a CA keystore with a public and private key
keytool -genkeypair -keyalg RSA -keysize 4096 -alias root-ca -dname "CN=Root-CA,OU=Certificate Authority,O=RedHat,C=IT" -validity 3650 -keystore CAidentity.jks -storepass password -keypass password -deststoretype jks

### Create Certificate Signing Request for the server
keytool -certreq -keystore server_identity.jks -alias server -keypass password -storepass password -keyalg rsa -file server_identity.csr


### Create Certificate Signing Request for the client
keytool -certreq -keystore client_identity.jks -alias client -keypass password -storepass password -keyalg rsa -file client_identity.csr

### Convert java keystore to a p12 file
keytool -importkeystore -srckeystore CAidentity.jks -destkeystore root-ca.p12 -srcstoretype jks -deststoretype pkcs12 -srcstorepass password -deststorepass password

### Create pem file from a p12 file
openssl pkcs12 -in root-ca.p12 -out root-ca.pem -nokeys -passin pass:password -passout pass:password

### Create a key file from a p12 file
openssl pkcs12 -in root-ca.p12 -out root-ca.key -nocerts -passin pass:password -passout pass:password

### Signing the client certificate
openssl x509 -req -in client_identity.csr -CA root-ca.pem -CAkey root-ca.key -CAcreateserial -out client-expired-signed.cer -extfile V3.ext -days -360 -passin pass:password

### Signing the server certificate
openssl x509 -req -in server_identity.csr -CA root-ca.pem -CAkey root-ca.key -CAcreateserial -out server-signed.cer -sha256 -extfile V3.ext -days 1825 -passin pass:password

### client
keytool -importkeystore -srckeystore client_identity.jks -destkeystore client_identity.p12 -srcstoretype jks -deststoretype pkcs12 -srcstorepass password -deststorepass password
openssl pkcs12 -in client_identity.p12 -nodes -out client-private.key -nocerts -passin pass:password
openssl pkcs12 -export -in client-expired-signed.cer -inkey client-private.key -out client-signed.p12 -name client -passout pass:password
keytool -delete -alias client -keystore client_identity.jks -storepass password
keytool -importkeystore -srckeystore client-signed.p12 -srcstoretype PKCS12 -destkeystore client_identity.jks -srcstorepass password -deststorepass password


### Server
keytool -importkeystore -srckeystore server_identity.jks -destkeystore server_identity.p12 -srcstoretype jks -deststoretype pkcs12 -srcstorepass password -deststorepass password
openssl pkcs12 -in server_identity.p12 -nodes -out server-private.key -nocerts -passin pass:password
openssl pkcs12 -export -in server-signed.cer -inkey server-private.key -out server-signed.p12 -name server -passout pass:password
keytool -delete -alias server -keystore server_identity.jks -storepass password
keytool -importkeystore -srckeystore server-signed.p12 -srcstoretype PKCS12 -destkeystore server_identity.jks -srcstorepass password -deststorepass password


### Import CA Certificate Authority in truststore

#Client
keytool -keystore client_truststore.jks -importcert -file root-ca.pem -alias root-ca -storepass password -noprompt
# Server
keytool -keystore server_truststore.jks -importcert -file root-ca.pem -alias root-ca -storepass password -noprompt
