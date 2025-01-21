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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.0/STCore.xcframework.zip",
            checksum: "2b29745a83ad99b88f54be209bafa7d14a30117088eac51e1964c074e53e4627"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.0/STDatabase.xcframework.zip",
            checksum: "7c77d3cf5447c0e83f0abb2e0917b0d106a033b64db3e4511354f346c3e1a3b9"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.0/STNetwork.xcframework.zip",
            checksum: "775d3f279a79b574d8ad4f6bc8a18ddf6c3823f55c4e5f7997a658c8d3f01530"
        ),
    ]
)
