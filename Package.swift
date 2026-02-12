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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/6.1.0/STCore.xcframework.zip",
            checksum: "88c5cc0de6f71c90710ab3566aa77b43d9db8a009a309e7a617d3c080719fae1"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/6.1.0/STDatabase.xcframework.zip",
            checksum: "eaa879c2636a0bab182e4b95ea67529a8bc784376f2675b8577eb1784aa9c852"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/6.1.0/STNetwork.xcframework.zip",
            checksum: "3d602842ff6717301997636a0c7e0d16eb31258879183f0cdfd5cb55f99a1527"
        ),
    ]
)
