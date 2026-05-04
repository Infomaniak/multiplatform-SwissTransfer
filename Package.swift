// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "SwissTransfer-Multiplatform",
    platforms: [
        .iOS(.v14),
        .macOS(.v11)
    ],
    products: [
        .library(name: "STCore", targets: ["Core"]),
        .library(name: "STDatabase", targets: ["Database"]),
        .library(name: "STNetwork", targets: ["Network"])
    ],
    targets: [
        .binaryTarget(
            name: "Core",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/9.0.0/STCore.xcframework.zip",
            checksum: "8dcfc6902a30153b93046b12d9a85cc16556bd4fe376cb6d91b195bfb2cc1344"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/9.0.0/STDatabase.xcframework.zip",
            checksum: "a2a5ae305c3940e3ac9734892cae927e6da4dd43c0f208e401864f2fa489f342"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/9.0.0/STNetwork.xcframework.zip",
            checksum: "4a4d82b7235646fc3465552cc195f9b16b42c50a405725a5c55144ab4be386f1"
        ),
    ]
)
