# FileSigner SDK

![Build](https://img.shields.io/github/actions/workflow/status/sara-star-quant/filesigner-sdk-android/build.yml?label=build)
![CodeQL](https://img.shields.io/github/actions/workflow/status/sara-star-quant/filesigner-sdk-android/codeql.yml?label=CodeQL)
![Maven Central](https://img.shields.io/maven-central/v/com.sarastarquant/filesigner-core?label=maven%20central)
![API](https://img.shields.io/badge/API-26%2B-brightgreen)
![License](https://img.shields.io/badge/license-Apache%202.0-blue)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2-purple)

Hardware-backed ECDSA P-256 file signing SDK for Android. Signs any file using keys stored in Android KeyStore (StrongBox or TEE). Private keys never leave the hardware secure enclave.

## Installation

```kotlin
dependencies {
    implementation("com.sarastarquant:filesigner-core:0.1.0")
}
```

## Quick Start

```kotlin
// Create a signer instance
val signer = FileSigner.Builder(context)
    .keyAlias("my_app_key_v1")       // optional, default provided
    .preferStrongBox(true)            // optional, default true
    .maxFileSize(500 * 1024 * 1024L)  // optional, default 500 MB
    .build()

// Sign a file
when (val result = signer.sign(fileUri)) {
    is SignatureResult.Success -> {
        // result.signatureBytes  - DER-encoded ECDSA signature
        // result.algorithm  - "SHA256withECDSA"
        // result.timestamp  - when the signature was produced
        // result.securityLevel  - STRONG_BOX, TEE, or UNKNOWN
    }
    is SignatureResult.Error -> {
        // Handle specific error variants
    }
}

// Verify a file against its signature
when (signer.verify(fileUri, signatureBytes)) {
    VerificationResult.Valid -> { /* file is authentic */ }
    VerificationResult.Invalid -> { /* file was modified */ }
    is VerificationResult.Error -> { /* could not verify */ }
}
```

## Features

| Feature | Detail |
|---------|--------|
| Algorithm | ECDSA P-256 (secp256r1) with SHA-256 |
| Key storage | Android KeyStore  - StrongBox preferred, TEE fallback |
| Signing | Streaming (8 KB chunks)  - constant memory regardless of file size |
| Max file size | Configurable, default 500 MB |
| Output format | DER-encoded detached signature bytes |
| Network | None. No INTERNET permission, no telemetry, no cloud dependency |
| Security | Buffer zeroing after use, non-extractable keys, hardware entropy |

## API Reference

### FileSigner

| Method | Description |
|--------|-------------|
| `sign(fileUri: Uri): SignatureResult` | Sign a file, auto-generating a key if needed |
| `verify(fileUri: Uri, signatureBytes: ByteArray): VerificationResult` | Verify a file against a signature |
| `hasSigningKey(): Boolean` | Check if a signing key exists |
| `getPublicKeyEncoded(): ByteArray?` | Get the X.509-encoded public key |
| `getConfig(): SignerConfig` | Get current configuration |

### SignatureResult

| Variant | Description |
|---------|-------------|
| `Success(signatureBytes, algorithm, timestamp, securityLevel)` | Signing succeeded |
| `Error.FileNotFound` | File URI could not be resolved |
| `Error.FileReadError` | File could not be read |
| `Error.FileTooLarge(sizeBytes, maxBytes)` | File exceeds configured limit |
| `Error.KeyGenerationFailed` | KeyStore key generation failed |
| `Error.SigningFailed(message)` | Signing operation failed |

### VerificationResult

| Variant | Description |
|---------|-------------|
| `Valid` | Signature matches the file |
| `Invalid` | File has been modified since signing |
| `Error(message)` | Verification could not be completed |

### SecurityLevel

| Value | Description |
|-------|-------------|
| `STRONG_BOX` | Key backed by dedicated secure element |
| `TEE` | Key backed by Trusted Execution Environment |
| `UNKNOWN` | Security level could not be determined |

## Security

- Private keys are generated inside and never leave the hardware secure enclave
- StrongBox is attempted first on Android 9+; falls back to TEE automatically
- Signing nonce (`k`) is generated inside hardware TRNG  - no application-level entropy
- Streaming design uses 8 KB buffer, zeroed after each operation
- No network access, no telemetry, no data leaves the device

## Compliance

This SDK aligns with:

- **BSI TR-02102-1**  - Approved algorithm (ECDSA P-256, SHA-256), hardware-backed keys
- **OWASP MASVS**  - MSTG-CRYPTO controls for key management and algorithm selection
- **NIST SP 800-186**  - Recommended curve and hash
- **FIPS 186-4**  - DSS-compliant algorithm
- **GDPR**  - No data collection, no network, no PII processing

## Showcase App

The [File Signer](https://github.com/pzverkov/FileSigner-Android) app is a production-ready Android application built entirely on this SDK. It demonstrates integration with Jetpack Compose, Hilt, and the full sign/verify workflow.

## Requirements

- Android API 26+ (Android 8.0)
- Kotlin 1.9+

## License

Apache License 2.0  - see [LICENSE](LICENSE).

---

## Compliance & Liability

**IMPORTANT: By using, cloning, or forking this repository, you acknowledge and agree to the following:**

### Not a Certified Product
This SDK provides cryptographic signing primitives. It is **NOT** a FIPS 140-3 validated module, a qualified electronic signature tool under eIDAS (EU) 910/2014, or a certified product under any national or international scheme. Compliance alignment statements (BSI, OWASP, NIST) describe algorithm selection and architecture, not formal certification.

### Export Controls
This software implements ECDSA P-256 cryptographic technology. Users are solely responsible for compliance with:
- **EU**: Dual-Use Regulation (EU 2021/821). Open-source exemptions may apply.
- **US**: Export Administration Regulations (EAR), ECCN 5D002. Publicly available open-source may qualify for License Exception TSR.
- **Other**: Local import/export and usage regulations for cryptographic software.

### Jurisdiction Restrictions
Cryptographic software is restricted or regulated in certain jurisdictions. This software **must not** be used to circumvent any applicable laws or regulations. Users deploying in any jurisdiction assume full responsibility for legal compliance.

### No Warranty
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY ARISING FROM THE USE OF THIS SOFTWARE.

See [LEGAL.md](LEGAL.md) for detailed legal notices covering eIDAS, EU Cyber Resilience Act, export controls, and jurisdiction.
