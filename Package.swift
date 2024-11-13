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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.8.0/STCore.xcframework.zip",
            checksum: "ada4283dc7ef5e4e2ef08fbfd5142b8c70778e3f43ee3e7df3c3f297fd4d07eb"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.8.0/STDatabase.xcframework.zip",
            checksum: "23fe333c61029f7226e698e6f9030bdf2bfa24a0271b18b99c11919fc4cf09b0"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.8.0/STNetwork.xcframework.zip",
            checksum: "6431e7e760a0f0269af4a3f9b5599e6fe491a89deaef364f65c5d3b1f7c2a668"
        ),
    ]
)
