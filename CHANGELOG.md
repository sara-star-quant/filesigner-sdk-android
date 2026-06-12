# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.1] - 2026-06-12

### Added
- `FileSigner.verify(Uri, ByteArray, ByteArray)` overload for verifying a signature against an externally supplied X.509 public key, enabling relying-party and cross-device verification

## [0.1.0]

### Added
- `FileSigner` builder with configurable key alias, StrongBox preference, and max file size
- `FileSigner.sign(Uri)` for streaming ECDSA P-256 file signing via Android KeyStore
- `FileSigner.verify(Uri, ByteArray)` for streaming signature verification
- `SignatureResult` sealed class with `Success` and `Error` variants
- `VerificationResult` sealed class with `Valid`, `Invalid`, and `Error` variants
- `SecurityLevel` enum reporting StrongBox / TEE key backing
- Hardware-backed key generation with StrongBox-first, TEE-fallback strategy
- Streaming signature computation with 8 KB buffer and post-use zeroing
- ProGuard consumer rules for library consumers
- Maven Central publishing configuration
- GitHub Actions CI with build, test, lint, and CodeQL analysis
