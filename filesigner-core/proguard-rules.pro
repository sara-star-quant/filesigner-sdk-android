# Keep Java security and Android keystore classes used via reflection
-keep class java.security.** { *; }
-keep class android.security.keystore.** { *; }

# Keep the public API
-keep class io.github.pzverkov.filesigner.FileSigner { *; }
-keep class io.github.pzverkov.filesigner.FileSigner$Builder { *; }
-keep class io.github.pzverkov.filesigner.SignatureResult { *; }
-keep class io.github.pzverkov.filesigner.SignatureResult$* { *; }
-keep class io.github.pzverkov.filesigner.VerificationResult { *; }
-keep class io.github.pzverkov.filesigner.VerificationResult$* { *; }
-keep class io.github.pzverkov.filesigner.SignerConfig { *; }
-keep class io.github.pzverkov.filesigner.SecurityLevel { *; }
