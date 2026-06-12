# Roadmap

Direction for the FileSigner SDK. Items are candidates, not commitments, and may
reorder as real-world usage informs priorities.

## Shipped

- **External-key verification** (0.1.1) - `verify(Uri, ByteArray, ByteArray)` verifies
  a signature against a supplied X.509 public key, so a relying party or a second
  device can verify signatures it did not produce.

## Near term

- **Key lifecycle** - `deleteKey()` to remove a signing key from Android KeyStore, for
  rotation and cleanup. Pairs with the existing alias configuration to support
  versioned keys (`..._v1`, `..._v2`).
- **Input flexibility** - `sign` / `verify` overloads taking an `InputStream` or a
  `ByteArray` directly, for callers that do not have a content `Uri`.

## Under consideration

- **Hardware attestation** - export the KeyStore attestation certificate chain so a
  verifier can prove a signature was produced by a hardware-backed key on a genuine
  device. The SDK already requests hardware backing; this would surface the proof.
- **Multiple key aliases** - first-class helpers for managing several signing keys in
  one app (per-purpose or per-tenant keys).

Have a use case that needs one of these sooner? Open an issue.
