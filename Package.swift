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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.2/STCore.xcframework.zip",
            checksum: "6f0c09e72388d4f3b9df54d629a05ba33b70c3445c05d0fde17752fa8173da88"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.2/STDatabase.xcframework.zip",
            checksum: "2abded8b36fa005b9f6480e35d0c70e0b0a960f7eb77f3b022d7bf3a6b0e1ae1"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.2/STNetwork.xcframework.zip",
            checksum: "995876ddd980d194ec205b80874649432fa22dde55959e8adca398db49f5ec74"
        ),
    ]
)
