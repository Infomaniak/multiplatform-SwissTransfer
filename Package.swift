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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.3.0/STCore.xcframework.zip",
            checksum: "98210bb42da715cd04d7c527c52dea16f8ca3129f438f28b546484a7abdbc5cf"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.3.0/STDatabase.xcframework.zip",
            checksum: "5e2f18fefce5db9e31809022b495f0c50e5990a20e68245010dfac7053f2f677"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.3.0/STNetwork.xcframework.zip",
            checksum: "2edde52b40bed7b735b633c987a2fa497fd768a96794f58e8908124f9b372f5d"
        ),
    ]
)
