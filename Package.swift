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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/8.0.0/STCore.xcframework.zip",
            checksum: "ee9558d09199b454dc39e0fd29b5ddd1616789a86fb5130df8b1f1f132b2e67a"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/8.0.0/STDatabase.xcframework.zip",
            checksum: "d363bd6369574c768c4b79ce55a720c830ae04a374311e389200e8d50774a841"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/8.0.0/STNetwork.xcframework.zip",
            checksum: "2e771f4f2f3087e36833ac070f4b6c116d921ba3298f70754426a57b26a2d9c5"
        ),
    ]
)
