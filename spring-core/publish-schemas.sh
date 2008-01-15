#!/bin/sh -ex
WWW=/kohsuke/Sun/java.net/www/jax-ws/spring
cp target/xbean/spring-jax-ws-core.xsd $WWW/core.xsd
cp target/xbean/spring-jax-ws-servlet.xsd $WWW/servlet.xsd
cp target/xbean/spring-jax-ws-local-transport.xsd $WWW/local-transport.xsd
