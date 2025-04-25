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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/4.0.0/STCore.xcframework.zip",
            checksum: "003989f4e1de9c0534a0922fd8a3b63227ec1197e74304b095c614a5456bb06b"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/4.0.0/STDatabase.xcframework.zip",
            checksum: "25ad4296ba7ca9b1b4d25f578b28b72012e944eb69ee13fd331478a8b27b48ad"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/4.0.0/STNetwork.xcframework.zip",
            checksum: "038898724e7bcbd3f2e5fa95d19feedc42789cf91aa777226a7a3f7a1928d31f"
        ),
    ]
)
