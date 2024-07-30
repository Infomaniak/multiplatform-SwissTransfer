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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.0.2/STCore.xcframework.zip",
            checksum: "c3f83598e0103049745b37af8f351b83d63db8a5cee35791a15c94d05f987ca8"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.0.2/STDatabase.xcframework.zip",
            checksum: "4e1ca5dee921e56fd06fdc3ca2bac0585c0e2b3bae71252cb2cf74a13e71f45d"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.0.2/STNetwork.xcframework.zip",
            checksum: "3a493f4df7b51324b8cc432627d14b59525a1700242b9f0dc028f34c8ba3bc85"
        ),
    ]
)
