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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.0.2/STCore.xcframework.zip",
            checksum: "56169f1ed4c9e1cea8c91660e14671b5ae03d1a3f9dcdcdef355567cae879335"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.0.2/STDatabase.xcframework.zip",
            checksum: "3cf3e0b4dca29967fb4dfa65ef62abfba096a27eda0c55c44f39105863dc765c"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.0.2/STNetwork.xcframework.zip",
            checksum: "8a0a19c70f1b3ac59525a5d5208ca18f2591e4cc32764d0d4f282de5696c68e1"
        ),
    ]
)
