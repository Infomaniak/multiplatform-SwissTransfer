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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.1.3/STCore.xcframework.zip",
            checksum: "dada5e0f90c50c881b3af5a71d12122459a08a920a904c6a60cd5827fef35a2f"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.1.3/STDatabase.xcframework.zip",
            checksum: "69665ba644295cf839185d4c28940bb439a7d8ca2e0aebbdbf0b169b5f594613"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.1.3/STNetwork.xcframework.zip",
            checksum: "ac0a41258adc85608c0653b101a6bbf2381127f111460610ebca305e82884ef1"
        ),
    ]
)
