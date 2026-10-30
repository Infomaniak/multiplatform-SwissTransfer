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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-12.0.0-202610301249-41056686613-1/STCore.xcframework.zip",
            checksum: "4c8851b3cb0cbf4367e535d0c1019ebb3a6798ad2e23c0a6853941d2afbe179f"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-12.0.0-202610301249-41056686613-1/STDatabase.xcframework.zip",
            checksum: "37b4f9de8ea486d4dc79cd11706b61c521dc141ec251c237269885bf80b39542"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-12.0.0-202610301249-41056686613-1/STNetwork.xcframework.zip",
            checksum: "8f20be866e0a4945b5a5bef7424a78462db01823e2a9e10c47a1849563f8845b"
        ),
    ]
)
